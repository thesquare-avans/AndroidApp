package me.thesquare;

import android.app.Activity;
import android.hardware.*;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import static java.lang.Thread.sleep;

/**
 * Created by jensderond on 12/06/2017.
 */

public class Record implements Runnable {
    private static final int CAPTURE_TIME = 5000;
    private android.hardware.Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;
    private static final String TAG = "Record class";
    private String filename;
    private Activity activity;
    private Thread captureThread;
    private FragmentWriter writer;
    private File tmpFile;
    private RandomAccessFile outputFile;

    private static final Object isRecordingMut = new Object();
    private boolean isRecording;


    public Record(Activity activity, TextureView view){
        this.activity = activity;
        this.mPreview = view;
    }


    public Record(TextureView cameraPreview, FragmentWriter writer) throws IOException {
        this.mPreview = cameraPreview;
        this.writer = writer;
        this.mMediaRecorder = new MediaRecorder();
        this.tmpFile = File.createTempFile("the_square_tmp_video", ".tmp");
        this.outputFile = new RandomAccessFile(tmpFile, "rw");
    }

    public void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    public void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    public void Capture(){
        try {
            mMediaRecorder.stop();  // stop the recording
            Log.d(TAG, mOutputFile.getPath() );
            Log.d(TAG, mOutputFile.length() + "");
        } catch (RuntimeException e) {
            mOutputFile.delete();
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        isRecording = false;
        this.releaseCamera();
    }
//    public  void prepareTask(){
//        new MediaPrepareTask().execute(null, null, null);
//    }

    public void removeTempFiles(){
        File dir = new File(Environment.getExternalStorageDirectory()+"/theSquare/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

    public boolean prepareVideoRecorder(){

        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<android.hardware.Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        android.hardware.Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getHeight(), mPreview.getWidth());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;


        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        parameters.setVideoStabilization(true);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
            mCamera.setDisplayOrientation(90);
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);
        mMediaRecorder.setOrientationHint(90);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        Log.d(TAG, mOutputFile.getPath());
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/theSquare/");
        if(!file.exists()){
            file.mkdirs();
        }

        Log.d(TAG, filename);

        try {
            mMediaRecorder.setOutputFile(outputFile.getFD());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, Environment.getExternalStorageDirectory().getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    public boolean getRecordingState(){
        return isRecording;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }

//    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            // initialize video camera
//            if (prepareVideoRecorder()) {
//                // Camera is available and unlocked, MediaRecorder is prepared,
//                // now you can start recording
//                mMediaRecorder.start();
//
//                isRecording = true;
//            } else {
//                // prepare didn't work, release the camera
//                releaseMediaRecorder();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (!result) {
//                activity.finish();
//            }
//        }
//    }


    public void start() {

        synchronized (isRecordingMut) {
            isRecording = true;
        }
        this.captureThread = new Thread(this);
        this.captureThread.start();
    }

    public void stop() throws InterruptedException {
        synchronized (isRecordingMut) {
            isRecording = false;
        }
        this.captureThread.join();
    }

    @Override
    public void run() {
        try {
            for (;;) {
                prepareVideoRecorder();
                Log.i("Recorder", "Start capture");
                mMediaRecorder.start();
                sleep(CAPTURE_TIME);

                mMediaRecorder.stop();

                Log.i("Recorder", "Stop capture");

                byte[] buffer = new byte[(int) outputFile.length()];
                Log.i("Recorder", "outputFile length: " + outputFile.length());
                outputFile.seek(0);
                outputFile.read(buffer);
                writer.writeFragment(buffer);
                outputFile.setLength(0);



                synchronized (isRecordingMut) {
                    if (!isRecording) {
                        break;
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
