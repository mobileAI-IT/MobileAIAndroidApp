package bakas.it.objectdetection;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.hardware.Camera.Size;

public class VideoRecorderService extends Service  {
    private final IBinder mBinder = new VideoRecorderService.LocalBinder();//Gets current service object
    private static final String TAG = "RecorderService";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static Camera mServiceCamera;
    private boolean mRecordingStatus;
    private MediaRecorder mMediaRecorder;
    HashMap<String, String> userPrefs;
    String lastScreenshotsFileDir="";//Direction of screenshots from last session
    Handler cameraCaptureHandler = new Handler();//Timer for screenshot timed to 10 sec
    public boolean stoppedAtBackground=false;// Shows if app stopped taking screenshots while on background
    int screenshotTime=0;
    String logs="";//Text that will be written to log file
    SessionManagement session;//Class for keeping user settings
    long startTime;
    public VideoRecorderService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean initialize(MediaProjection mProjection, final String timeStamp, final float interval) {
        screenshotTime=0;//Reset taken screenshots counter
        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();
        if(getUserId()!=""){
            logs=getUserId()+"\n";
        }
        /*this.mProjection=mProjection;//Currently running media projection*/
        this.lastScreenshotsFileDir= Environment.getExternalStorageDirectory().toString()+"/Parental_Control_Screenshots/"+timeStamp;
        if(!stoppedAtBackground){
            startCapture("");
        }
        cameraCaptureHandler.postDelayed(new Runnable() {//10 sec timer for screenshot
            @Override
            public void run() {
                long estimatedTime = System.currentTimeMillis() - startTime;
                if(estimatedTime / 1000 >=  getFileAmount() || stoppedAtBackground){
                    stopVideoRecorder();
                    stoppedAtBackground = true;
                    cameraCaptureHandler.removeCallbacks(this);

                }
                else{
                    cameraCaptureHandler.postDelayed(this,(long)(1000));//creating loop with value of interval secs delay
                }
            }
        }, 1000);//0 secs delay

        return true;
    }

    private float getInterval(){
        return Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
    }

    private void startRefreshHandler(){
        cameraCaptureHandler.postDelayed(new Runnable() {
            public void run() {
                long estimatedTime = System.currentTimeMillis() - startTime;
                if(estimatedTime / 1000 >=  getFileAmount() || stoppedAtBackground) {
                    mRecordingStatus = false;
                    stoppedAtBackground = true;
                    stopVideoRecorder();
                    cameraCaptureHandler.removeCallbacks(this);

                }
                else {

                    cameraCaptureHandler.postDelayed(this, 1000);//Creating loop with 1 sec
                }
            }
        }, 1000);//1 sec delay
    }

    @Override
    public void onCreate() {
        this.lastScreenshotsFileDir= Environment.getExternalStorageDirectory().toString()+"/Parental_Control_Screenshots/";
        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();

        mRecordingStatus = false;
        //mServiceCamera = CameraRecorder.mCamera;


        super.onCreate();
      /*  if (mRecordingStatus == false)
            stopVideoRecorder();*/

    }

    @Override
    public void onDestroy() {
        stopVideoRecorder();
        mRecordingStatus = false;

        super.onDestroy();
    }

    public boolean startCapture(final String newFolderName){
        try {
            startTime = System.currentTimeMillis();
            startRefreshHandler();
            Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();
            mServiceCamera = Camera.open(1);
            mSurfaceView = HomePageActivity.mSurfaceView;
            mSurfaceHolder = HomePageActivity.mSurfaceHolder;

            //mServiceCamera = Camera.open();
            Camera.Parameters params = mServiceCamera.getParameters();
            mServiceCamera.setParameters(params);
            Camera.Parameters p = mServiceCamera.getParameters();

            final List<Size> listSize = p.getSupportedPreviewSizes();
            Size mPreviewSize = listSize.get(2);
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);
            p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(p);

            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            File myDir = new File(this.lastScreenshotsFileDir);//Adding our folder to path
            myDir.mkdirs();//Creating our folder if doesn't exist
            mMediaRecorder.setOutputFile(this.lastScreenshotsFileDir+"/video.mp4");
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecordingStatus = true;

            return true;
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }





    private int getFileAmount(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
    }

    private String getUserId(){
        return String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
    }

    public class LocalBinder extends Binder {
        public VideoRecorderService getService() {
            return VideoRecorderService.this;
        }
    }

    public void stopVideoRecorder(){
        stoppedAtBackground=true;//finish flag
        /*Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();*/
        try {
            if(mServiceCamera != null)
                mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(mMediaRecorder != null && mRecordingStatus){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }

        if (mServiceCamera != null)
            mServiceCamera.stopPreview();
        if(mMediaRecorder != null)
            mMediaRecorder.release();
        if (mServiceCamera != null){
            mServiceCamera.release();
            mServiceCamera = null;
        }

    }
    private float getCapture(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
    }

}