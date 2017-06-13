package me.thesquare;

import android.media.MediaRecorder;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**
 * Created by larsh on 13-6-2017.
 */

public class Recorder extends TimerTask implements Runnable {
    private static final int CAPTURE_TIME = 3000;

    private MediaRecorder mediaRecorder;
    private SurfaceView cameraPreview;
    private FragmentWriter writer;
    private File tmpFile;
    private RandomAccessFile outputFile;
    private Timer captureTimer;

    private Object isRecordingMut;
    private boolean isRecording;

    public Recorder(SurfaceView cameraPreview, FragmentWriter writer) throws IOException {
        this.cameraPreview = cameraPreview;
        this.writer = writer;
        this.mediaRecorder = new MediaRecorder();
        this.captureTimer = new Timer();
        this.tmpFile = File.createTempFile("the_square_tmp_video", ".mp4");
        this.outputFile = new RandomAccessFile(tmpFile, "rw");
        this.initMediaRecorder();
    }

    private void initMediaRecorder() throws IOException {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncodingBitRate(128);
        mediaRecorder.setVideoFrameRate(25);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setPreviewDisplay(this.cameraPreview.getHolder().getSurface());
        mediaRecorder.setOutputFile(outputFile.getFD());
        mediaRecorder.prepare();
    }

    public void cleanup() throws IOException {
        outputFile.close();
        tmpFile.delete();
    }

    public void start() {
        synchronized (isRecordingMut) {
            isRecording = true;
        }
        captureTimer.schedule(this, 0);
    }

    public void stop() {
        synchronized (isRecordingMut) {
            isRecording = false;
        }
    }

    @Override
    public void run() {
        try {
            mediaRecorder.start();
            sleep(CAPTURE_TIME);
            mediaRecorder.stop();

            byte[] buffer = new byte[(int) outputFile.length()];
            outputFile.readFully(buffer);
            writer.writeFragment(buffer);
            outputFile.setLength(0);

            synchronized (isRecordingMut) {
                if (isRecording) {
                    mediaRecorder.reset();
                    initMediaRecorder();
                    captureTimer.schedule(this, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
