package me.thesquare;


import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.Thread.sleep;

/**
 * Created by larsh on 13-6-2017.
 */

public class Recorder implements Runnable {
    private static final int CAPTURE_TIME = 5000;
    private MediaRecorder mediaRecorder;
    private TextureView cameraPreview;
    private FragmentWriter writer;
    private File tmpFile;
    private RandomAccessFile outputFile;
    private Handler captureHandler;

    private static final Object isRecordingMut = new Object();
    private boolean isRecording;

    public Recorder(TextureView cameraPreview, FragmentWriter writer) throws IOException {
        this.cameraPreview = cameraPreview;
        this.writer = writer;
        this.mediaRecorder = new MediaRecorder();
        this.captureHandler = new Handler();
        this.tmpFile = File.createTempFile("the_square_tmp_video", ".tmp");
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
        captureHandler.post(this);
    }

    public void stop() {
        synchronized (isRecordingMut) {
            isRecording = false;
        }
    }

    @Override
    public void run() {
        try {
            Log.i("Recorder", "Start capture");
            mediaRecorder.start();
            sleep(CAPTURE_TIME);
            mediaRecorder.stop();
            Log.i("Recorder", "Stop capture");

            byte[] buffer = new byte[(int) outputFile.length()];
            Log.i("Recorder", "outputFile length: " + outputFile.length());
            outputFile.seek(0);
            outputFile.read(buffer);
            writer.writeFragment(buffer);
            outputFile.setLength(0);

            synchronized (isRecordingMut) {
                if (isRecording) {
                    mediaRecorder.reset();
                    initMediaRecorder();
                    captureHandler.post(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
