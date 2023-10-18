package bakas.it.objectdetection;
/**
 *      ArtificialIntelligenceFrameworktoProtectChildrenfromHarmfulDigitalContent
 *      Copyright (C) 2021 BAKAS BİLİŞİM ELEKTRONİK YAZILIM DANIŞMANLIK SANAYİ VE TİCARET LİMİTED ŞİRKETİ
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.

 *     This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.

 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import bakas.it.objectdetection.ObjectDetection.Result;

public class HomePageActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    ImageView lastScreenshot;//gallery grid view
    GridViewAdapter customGridAdapter;//Adapter for gallery grid view
    Button btn_startStop;//Start Stop button
    Button btn_menu;
    TextView classifierResultText;
    Toolbar toolbar;
    MediaProjection mProjection;//Media Projection variable for screenshot
    int mWidth ;//Screen width
    int mHeight ;//Screen height
    int mDensity ;//Screen density
    int resultCode;//Result after user permission request to screenshot
    Intent data;//Data from permission request
    int startStopState=0;//0 for stop 1 for start
    Handler gwRefreshHandler = new Handler();//Timer for refreshing gallery timed to 1 sec
    public static ScreenshotService screenshotService;//Screenshot service that runs in background and takes screenshots

    public static CameraService cameraService;//Camera service that runs in background and capture

    public static VideoRecorderService videoService;//Video service that runs in background and capture
    private static boolean mServiceConnected;//Boolean value that shows if screenshot service is connected
    ProgressDialog dialog;//Mail sending dialog
    String lastLogFileDir="";//Keeps the last log file's directory
    String lastScreenshotsFileDir="invalid_path";//Keeps the last screenshots' file's directory
    SessionManagement session;//Class for keeping user settings
    HashMap<String, String> userPrefs;
    int fileAmount, language, capture, edgeType, saveScreenType, modelType;
    String userId, safe, violence, suspicious;
    TextView lbllastscreenshot, lblAnaSayfaHeader;

    float interval;

    int sendMail = 1;

    final Handler handler = new Handler();
    final int delay = 1000; // 1000 milliseconds == 1 second
    private static final int CAMERA_PERMISSION_CODE = 100; //Camera permission code
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private static Map<String,Boolean> mapPhoto =  new HashMap<String,Boolean>();

    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static LinearLayout mLinearLayout;

    //Connects service
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        //On service connected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            screenshotService = ((ScreenshotService.LocalBinder) service).getService();//Get service and assign
            mServiceConnected = true;//set service connected true
        }
        //On service disconnected
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            screenshotService = null;//drop service
            mServiceConnected=false;//set service connected false
        }
    };

    private final ServiceConnection mServiceConnectionCamera = new ServiceConnection() {
        //On service connected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            cameraService = ((CameraService.LocalBinder) service).getService();//Get service and assign
            mServiceConnected = true;//set service connected true
        }
        //On service disconnected
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            cameraService = null;
            mServiceConnected=false;//set service connected false
        }
    };

    private final ServiceConnection mServiceConnectionVideo = new ServiceConnection() {
        //On service connected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            videoService = ((VideoRecorderService.LocalBinder) service).getService();//Get service and assign
            mServiceConnected = true;//set service connected true
        }
        //On service disconnected
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            videoService = null;
            mServiceConnected=false;//set service connected false
        }
    };

    //Binds service to activity
    public void doBindService() {

        if (capture == 0){

            Intent ssServiceIntent = new Intent(this, ScreenshotService.class);//Create intent with this activity and Screenshot service
            bindService(ssServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//Binding service and calls connection method

        }
        else if (capture == 1 || capture == 2){

            Intent cameraServiceIntent = new Intent(this, CameraService.class);//Create intent with this activity and Screenshot service
            bindService(cameraServiceIntent, mServiceConnectionCamera, BIND_AUTO_CREATE);//Binding service and calls connection method

        }
        else if (capture == 3 || capture == 4){

            Intent videoServiceIntent = new Intent(this, VideoRecorderService.class);//Create intent with this activity and Screenshot service
            bindService(videoServiceIntent, mServiceConnectionVideo, BIND_AUTO_CREATE);//Binding service and calls connection method

        }

    }

    //On activity create
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if(userPrefs.get(SessionManagement.KEY_COMP_NAME)==null){
            setTitle("mobileAI-IT");
        }
        else {
            setTitle(userPrefs.get(SessionManagement.KEY_COMP_NAME));
        }*/

        setContentView(R.layout.activity_home_page);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        mLinearLayout = (LinearLayout) findViewById(R.id.linearView1);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Setting toolbar as designed
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();

        //session.setCompName("mobileAI-IT");

        if(userPrefs.get(SessionManagement.KEY_COMP_NAME)==null){
            toolbar.setTitle("");//mobileAI-IT
        }
        else {
            toolbar.setTitle(userPrefs.get(SessionManagement.KEY_COMP_NAME));
        }

        if(userPrefs.get(SessionManagement.KEY_INTERVAL)==null){
            session.setInterval("10");
        }
        else {
            interval = Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
        }

        if(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT)==null){
            session.setFileAmount("5");
        }
        else {
            fileAmount = Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        }

        if(userPrefs.get(SessionManagement.KEY_USER_ID)==null){
            session.setUserId("");//koray.cirak@yahoo.com.tr
        }
        else {
            userId = userPrefs.get(SessionManagement.KEY_USER_ID);
        }

        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null){
            session.setLanguage("0");
        }
        else {
            language = Integer.parseInt(userPrefs.get(SessionManagement.KEY_LANGUAGE));
        }

        if(userPrefs.get(SessionManagement.KEY_CAPTURE)==null){
            session.setCapture("0");
        }
        else {
            capture = Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
        }

        if(userPrefs.get(SessionManagement.KEY_EDGETYPE)==null){
            session.setEdge("0");
        }
        else {
            edgeType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
        }

        if(userPrefs.get(SessionManagement.KEY_MODEL)==null){
            session.setModelType("0");
        }
        else {
            modelType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
        }

        if(userPrefs.get(SessionManagement.KEY_DRAWBOX)==null){
            session.setDrawBox(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL)==null){
            session.setRunAllModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL)==null){
            session.setRunAllBabyModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD)==null){
            session.setMinThreshold("0.7");
        }

        if(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD)==null){
            session.setMaxThreshold("1.0");
        }


        //if(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE)==null){
        //    session.setSaveScreen("0");
        //}
        //else {
        //    saveScreenType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
        //}

        if(userPrefs.get(SessionManagement.KEY_SAFE)==null){
            session.setSafe("SAFE");
        }
        else {
            safe = userPrefs.get(SessionManagement.KEY_SAFE);
        }

        if(userPrefs.get(SessionManagement.KEY_SUS)==null){
            session.setSus("SUSPICIOUS");
        }
        else {
            suspicious = userPrefs.get(SessionManagement.KEY_SUS);
        }

        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)==null){
            session.setViolence("HARMFUL");
        }
        else {
            violence = userPrefs.get(SessionManagement.KEY_VIOLENCE);
        }

//        if(userPrefs.get(SessionManagement.KEY_USER_ID)==null){
//            session.setUserId("info@compositeware.com");
//        }
//        else {
//            userId = userPrefs.get(SessionManagement.KEY_USER_ID);
//        }

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(this, PERMISSIONS)) {

            /*for(String currentX : PERMISSIONS) {
                // Do something with the value

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(HomePageActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);}});

                    AlertDialog alert = alertBuilder.create();
                    alert.show();

            }*/



            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        //Checking write on disk permission
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_CODE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);

        }*/

        //Items from xml file
        lastScreenshot=findViewById(R.id.lastScreenshotImage);
        btn_startStop=findViewById(R.id.btn_start_stop);
        btn_menu=findViewById(R.id.btn_menu);
        lbllastscreenshot = findViewById(R.id.lbl_last_screenshot);
        lblAnaSayfaHeader = findViewById(R.id.ana_sayfa_header);
        classifierResultText=findViewById(R.id.classifierResultText);

        //Starting gallery refreshing
        startRefreshHandler();

        //Getting screen size and density
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        mDensity = metrics.densityDpi;




        if(capture == 0){

            Intent ssServiceIntent = new Intent(this, ScreenshotService.class);//Create intent with this activity and Screenshot service
            bindService(ssServiceIntent, mServiceConnection, BIND_AUTO_CREATE);//Binding service and calls connection method

        }
        else if(capture == 1 || capture == 2){

            Intent cameraServiceIntent = new Intent(this, CameraService.class);//Create intent with this activity and Screenshot service
            bindService(cameraServiceIntent, mServiceConnectionCamera, BIND_AUTO_CREATE);//Binding service and calls connection method

        }
        else if(capture == 3 || capture == 4){

            Intent videoServiceIntent = new Intent(this, VideoRecorderService.class);//Create intent with this activity and Screenshot service
            bindService(videoServiceIntent, mServiceConnectionVideo, BIND_AUTO_CREATE);//Binding service and calls connection method

        }




        //Listener for Start Stop button click
        btn_startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartStopCall();
            }
        });

        //Listener for Menu button click
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startStopState==0){//If start stop button is on stop state
                    Intent intent=new Intent(HomePageActivity.this,MenuActivity.class);
                    startActivity(intent);//Start menu activity
                }
            }
        });

        setLabelTextes();
        if (capture == 0){

            handler.postDelayed(new Runnable() {
                public void run() {
                    if(startStopState == 1 && screenshotService.stoppedAtBackground){
                        btnStartStopCall();
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);
        }
        else if(capture == 1 || capture == 2){

            handler.postDelayed(new Runnable() {
                public void run() {
                    if(startStopState == 1 && cameraService.stoppedAtBackground){
                        btnStartStopCall();
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);

        }
        else if(capture == 3 || capture == 4){
            handler.postDelayed(new Runnable() {
                public void run() {
                    if(startStopState == 1 && videoService.stoppedAtBackground){
                        btnStartStopCall();
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);

        }


    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //On activity resume
    @Override
    protected void onResume() {
        super.onResume();
        userPrefs = session.getUserDetails();
        /*if(userPrefs.get(SessionManagement.KEY_USER_ID)==null || "".equals(userPrefs.get(SessionManagement.KEY_USER_ID))){
            btn_startStop.setEnabled(false);
        }
        else {
            btn_startStop.setEnabled(true);
        }*/

        if(userPrefs.get(SessionManagement.KEY_CAPTURE)==null){
            session.setCapture("0");
        }
        else {
            capture = Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
        }

        if(userPrefs.get(SessionManagement.KEY_EDGETYPE)==null){
            session.setEdge("0");
        }
        else {
            edgeType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
        }

        if(userPrefs.get(SessionManagement.KEY_MODEL)==null){
            session.setModelType("0");
        }
        else {
            modelType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
        }

        if(userPrefs.get(SessionManagement.KEY_DRAWBOX)==null){
            session.setDrawBox(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL)==null){
            session.setRunAllModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL)==null){
            session.setRunAllBabyModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD)==null){
            session.setMinThreshold("0.7");
        }

        if(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD)==null){
            session.setMaxThreshold("1.0");
        }

        //if(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE)==null){
        //     session.setSaveScreen("0");
        // }
        //else {
        //     saveScreenType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
        //}

        if(userPrefs.get(SessionManagement.KEY_SAFE)==null){
            session.setSafe("SAFE");
        }
        else {
            safe = userPrefs.get(SessionManagement.KEY_SAFE);
        }

        if(userPrefs.get(SessionManagement.KEY_SUS)==null){
            session.setSus("SUSPICIOUS");
        }
        else {
            suspicious = userPrefs.get(SessionManagement.KEY_SUS);
        }

        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)==null){
            session.setViolence("HARMFUL");
        }
        else {
            violence = userPrefs.get(SessionManagement.KEY_VIOLENCE);
        }

        if(userPrefs.get(SessionManagement.KEY_USER_ID)==null){
            session.setCompName("");//mobileAI-IT
        }
        else {
            userId = userPrefs.get(SessionManagement.KEY_USER_ID);
        }

        if(userPrefs.get(SessionManagement.KEY_COMP_NAME)==null){
            toolbar.setTitle("");//mobileAI-IT
        }
        else {
            toolbar.setTitle(userPrefs.get(SessionManagement.KEY_COMP_NAME));
        }

        if(capture == 0 && screenshotService==null){//If no services bound
            doBindService();//Bind service
        } else if (cameraService==null) {
            doBindService();//Bind service

        } else if(capture == 0 && screenshotService.stoppedAtBackground==true){
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        } else if ((capture == 1 || capture == 2) && cameraService.stoppedAtBackground) {
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        }
        else if ((capture == 3 || capture == 4) && videoService != null && videoService.stoppedAtBackground) {
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        }
        final IntentFilter filter = new IntentFilter();// Intent filter
        filter.addAction("Mail_Sent");//Action mail sent
        registerReceiver(mailUpdateReceiver, filter);//Registering the broadcast receiver
        setLabelTextes();
    }

    //On activity pause
    @Override
    protected void onPause() {
        unregisterReceiver(mailUpdateReceiver);//Unregistering the broadcast receiver
        super.onPause();
    }

    //On permission request result this method starts getting screen data
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.resultCode=resultCode;
        this.data=data;
        //Starting media stream
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mProjection = projectionManager.getMediaProjection(resultCode, data);
        lastScreenshotsFileDir =  new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());//Getting timestamp new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());//Getting timestamp


        if (capture == 0){
            screenshotService.initialize(mProjection, lastScreenshotsFileDir,interval);//starting auto screenshot

        }
        else if (capture == 1 || capture == 2){
            cameraService.initialize(mProjection, lastScreenshotsFileDir,interval);//starting auto screenshot

        }
        else if (capture == 3 || capture == 4){
            videoService.initialize(mProjection, lastScreenshotsFileDir,interval);//starting auto screenshot

        }


    }

    //Getting all pictures in specified folder adding them to array list and returns array list
    //For grid view gallery
    /*private ArrayList updateGallery(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots");
        if (!folder.exists()) {//If folder doesn't exist
            folder.mkdirs();//Create folder
        }

        final ArrayList imageItems = new ArrayList();//Declaring an array list of pictures

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        for (int i = 0; i < imageFiles.length; i++) {//for all files in folder
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());//Create bitmap from pictures
            imageItems.add(new ImageItem(bitmap, "Image#" + i));//add bitmaps to array list
        }

        return imageItems;// return array list
    }*/

    //Updates the last screenshot image every 1 sec
    private void updatePicture(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots/"+lastScreenshotsFileDir);
        if (!folder.exists()) {//If folder doesn't exist
            return;
        }

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        if(imageFiles.length>0 && imageFiles[imageFiles.length - 1].getAbsolutePath().toUpperCase().contains("JPG")) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[imageFiles.length - 1].getAbsolutePath());//Create bitmap from pictures

            if (mapPhoto.get(imageFiles[imageFiles.length - 1].getAbsolutePath()) == null){
                Matrix matrix = new Matrix();
                if(getCapture()== 2){
                    matrix.postRotate(90);
                }
                else if (getCapture()== 1){
                    matrix.postRotate(270);
                }
                if(bitmap != null){
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
               /* if (capture == 0){
                    drawBoundingBoxes(screenshotService.results,rotatedBitmap);
                }
                else if(capture == 1 || capture == 2){
                    drawBoundingBoxes(cameraService.results,rotatedBitmap);
                }*/

                    lastScreenshot.setImageBitmap(Bitmap.createScaledBitmap(rotatedBitmap, 600, 800, false));//Resizing to 800x600*/
                    File file = new File(imageFiles[imageFiles.length - 1].getAbsolutePath()); // the File to save to
                    try {
                        FileOutputStream fOut = new FileOutputStream(file);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                        fOut.flush();
                        fOut.close(); // do not forget to close the stream

                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mapPhoto.put(imageFiles[imageFiles.length - 1].getAbsolutePath(),true);
                }

            }
            else{
                lastScreenshot.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 600, 800, false));//Resizing to 800x600
            }


            /*lastScreenshot.setRotation(90);*/

           /* classifierResultText.setVisibility(View.VISIBLE);//Set prediction result text visible



            if (capture == 0){
                if(screenshotService.predictionResult=="SAFE" || screenshotService.predictionResult == safe){
                    if(safe!="SAFE") {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SAFE));
                    }
                    else {
                        classifierResultText.setText("SAFE");
                    }
                    classifierResultText.setTextColor(Color.GREEN);
                }


                else if (screenshotService.predictionResult=="VIOLENCE" || screenshotService.predictionResult == violence) {

                    if (violence!="HARMFUL") {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_VIOLENCE));
                    }
                    else {
                        classifierResultText.setText("HARMFUL");
                    }
                    classifierResultText.setTextColor(Color.RED);
                }

                else if (screenshotService.predictionResult=="SEXUALITY" || screenshotService.predictionResult == suspicious) {
                    if (userPrefs.get(SessionManagement.KEY_SUS) != null) {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SUS));
                    }
                    else {
                        classifierResultText.setText("SEXUALITY");
                    }
                    classifierResultText.setTextColor(Color.YELLOW);
                }
            }
            else if(capture == 1 || capture == 2){

                if(cameraService.predictionResult=="SAFE" || cameraService.predictionResult == safe){
                    if(safe!="SAFE") {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SAFE));
                    }
                    else {
                        classifierResultText.setText("SAFE");
                    }
                    classifierResultText.setTextColor(Color.GREEN);
                }


                else if (cameraService.predictionResult=="VIOLENCE" || cameraService.predictionResult == violence) {

                    if (violence!="HARMFUL") {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_VIOLENCE));
                    }
                    else {
                        classifierResultText.setText("HARMFUL");
                    }
                    classifierResultText.setTextColor(Color.RED);
                }

                else if (cameraService.predictionResult=="SEXUALITY" || cameraService.predictionResult == suspicious) {
                    if (userPrefs.get(SessionManagement.KEY_SUS) != null) {
                        classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SUS));
                    }
                    else {
                        classifierResultText.setText("SEXUALITY");
                    }
                    classifierResultText.setTextColor(Color.YELLOW);
                }

            }*/

        }

        if(capture == 0 &&screenshotService != null &&  screenshotService.stoppedAtBackground==true){
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        } else if ((capture == 1 || capture == 2) && cameraService != null && cameraService.stoppedAtBackground) {
            startStopState=0;//Set state as stop
            btn_startStop.setText("Start");//Set button text as Start
        }
    }

    private void drawBoundingBoxes(ArrayList<Result> results, Bitmap selectedImage) {
        Bitmap mutableBitmap = selectedImage.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(15);

        for (Result result : results) {
            float left = result.rect.left;
            float top = result.rect.top;
            float right = result.rect.right;
            float bottom = result.rect.bottom;
            canvas.drawRect(left, top, right, bottom, boxPaint);

            String objectName = "gun";
            float textX = left;
            float textY = top - 5;
            canvas.drawText(objectName, textX, textY, textPaint);

            float scoreX = textX + textPaint.measureText(objectName) + 10;
            float confidence = result.score;
            @SuppressLint("DefaultLocale") String confidenceText = String.format("%.2f", confidence);
            canvas.drawText(confidenceText, scoreX, textY, textPaint);

        }


        /*lastScreenshot.setImageBitmap(mutableBitmap);*/
        lastScreenshot.setImageBitmap(Bitmap.createScaledBitmap(mutableBitmap, 600, 800, false));//Resizing to 800x600
    }


    private float getCapture(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
    }

    public static Bitmap EdgeGray(Bitmap first) {

        Bitmap image1;

        ///////////////transform back to Mat to be able to get Canny images//////////////////
        Mat img1=new Mat();
        Utils.bitmapToMat(first,img1);

        //mat gray img1 holder
        Mat imageGray1 = new Mat();

        //mat canny image
        Mat imageCny1 = new Mat();

        //mat canny image
        Mat imageCny2 = new Mat();

        /////////////////////////////////////////////////////////////////

        //Convert img1 into gray image
        Imgproc.cvtColor(img1, imageGray1, Imgproc.COLOR_BGR2GRAY);

        //Canny Edge Detection
        Imgproc.Canny(imageGray1, imageCny1, 10, 100, 3, true);

        ///////////////////////////////////////////////////////////////////

        //////////////////Transform Canny to Bitmap/////////////////////////////////////////
        image1= Bitmap.createBitmap(imageCny1.cols(), imageCny1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageCny1, image1);

        return image1;
    }

    //Loop that refreshing gallery every 1 sec
    private void startRefreshHandler(){
        gwRefreshHandler.postDelayed(new Runnable() {
            public void run() {
                if (capture != 3 && capture != 4)
                    updatePicture();//Update picture

                //For grid view gallery
                /*gw_gallery.setAdapter(null);//Dropping adapter
                customGridAdapter = new GridViewAdapter(context, R.layout.row_grid, updateGallery());//Re-creating adapter
                gw_gallery.setAdapter(customGridAdapter);//Setting adapter*/

                gwRefreshHandler.postDelayed(this, 1000);//Creating loop with 1 sec
            }
        }, 1000);//1 sec delay
    }

    //Sending email of screenshots
    public void sendEmail()
    {
        if(!TextUtils.isEmpty(userId)){
            //Opening progress dialog with text sending mail
            dialog = new ProgressDialog(this);
            dialog.setMessage("Sending mail...");
            dialog.show();



            Handler handler1 = new Handler();//Timer to wait before mail sending due to problems can occur while stopping screenshot
            handler1.postDelayed(new Runnable() {
                public void run() {
                    Mail mail =new Mail(HomePageActivity.this,lastLogFileDir,Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots/"+lastScreenshotsFileDir, userId);//Create mail object
                    mail.execute();//Execute mail sending
                }
            }, 1000);   //1 seconds
        }
    }

    /*//Deletes all screenshots saved
    private void deleteScreenshots(){
        //Picture folder path
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Parental_Control_Screenshots");
        if (!folder.exists()) {//If folder doesn't exist
            return;
        }

        File[] imageFiles = folder.listFiles();//Getting list of files in folder
        for(int i=imageFiles.length-1;i>=0;i--){
            imageFiles[i].delete();//Delete file
        }
    }*/

    // private int getSaveScreenType(){
    //     return Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
    // }

    //Listening the broadcasts
    private final BroadcastReceiver mailUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case "Mail_Sent"://Case of mail sending completed
                    dialog.dismiss();//Close dialog
                    break;

                default:
                    break;
            }
        }
    };

    public void btnStartStopCall() {
        if (capture == 0) {

            if(mLinearLayout.getVisibility() == View.GONE){
                mLinearLayout.setVisibility(View.VISIBLE);
            }

            if ( ((SurfaceView) findViewById(R.id.surfaceView1)).getVisibility() == View.VISIBLE){
                ((SurfaceView) findViewById(R.id.surfaceView1)).setVisibility(View.GONE);
            }


            if (startStopState == 0) {//If state is stop
                screenshotService.stoppedAtBackground = false;
                startStopState = 1;//Set state as start
                btn_startStop.setText("Stop");//Set button text as Stop
                updatePrefs();

                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(projectionManager.createScreenCaptureIntent(), 1);


            } else if (startStopState == 1 &&  screenshotService.stoppedAtBackground) {//If state is start
                startStopState = 0;//Set state as stop
                btn_startStop.setText("Start");//Set button text as Start
                lastLogFileDir = screenshotService.stopScreenshot();//Stop taking screenshots
                if(sendMail == 1){
                    sendEmail();//Send email on stop
                }

            }
        }
        else if (capture == 1 || capture == 2){
            if(mLinearLayout.getVisibility() == View.GONE){
                mLinearLayout.setVisibility(View.VISIBLE);
            }

            if ( ((SurfaceView) findViewById(R.id.surfaceView1)).getVisibility() == View.VISIBLE){
                ((SurfaceView) findViewById(R.id.surfaceView1)).setVisibility(View.GONE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

                // Requesting the permission
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_CODE);
            }

            /*checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);*/
            if (startStopState == 0) {//If state is stop
                cameraService.stoppedAtBackground = false;
                startStopState = 1;//Set state as start
                btn_startStop.setText("Stop");//Set button text as Stop
                updatePrefs();

                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(projectionManager.createScreenCaptureIntent(), 1);


            } else if (startStopState == 1) {//If state is start
                startStopState = 0;//Set state as stop
                btn_startStop.setText("Start");//Set button text as Start
                lastLogFileDir = cameraService.stopScreenshot();//Stop taking screenshots
                if(sendMail == 1){
                    sendEmail();//Send email on stop
                }

            }
        }
        else if (capture == 3 || capture == 4){
            mLinearLayout.setVisibility(View.GONE);
            if ( ((SurfaceView) findViewById(R.id.surfaceView1)).getVisibility() == View.GONE){
                ((SurfaceView) findViewById(R.id.surfaceView1)).setVisibility(View.VISIBLE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {

                // Requesting the permission
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_CODE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);

            }
            /*checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);*/
            if (startStopState == 0) {//If state is stop
                videoService.stoppedAtBackground = false;
                startStopState = 1;//Set state as start
                btn_startStop.setText("Stop");//Set button text as Stop
                updatePrefs();

               /* Intent intent = new Intent(HomePageActivity.this, VideoRecorderService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);
                finish();*/

                MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(projectionManager.createScreenCaptureIntent(), 1);


            } else if (startStopState == 1 && videoService.stoppedAtBackground) {//If state is start
                startStopState = 0;//Set state as stop
                btn_startStop.setText("Start");//Set button text as Start
                videoService.stopVideoRecorder();
                if(mLinearLayout.getVisibility() == View.GONE){
                    mLinearLayout.setVisibility(View.VISIBLE);
                }

                if ( ((SurfaceView) findViewById(R.id.surfaceView1)).getVisibility() == View.VISIBLE){
                    ((SurfaceView) findViewById(R.id.surfaceView1)).setVisibility(View.GONE);
                }
                if(sendMail == 1){
                    sendEmail();//Send email on stop
                }

            }
        }
    }

    public void updatePrefs(){ // Update user Prefers
        userPrefs = session.getUserDetails();

        if(userPrefs.get(SessionManagement.KEY_INTERVAL)==null){
            session.setInterval("10");
        }
        else {
            interval = Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
        }
        if(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT)==null){
            session.setFileAmount("5");
        }
        else {
            fileAmount = Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        }

        if(userPrefs.get(SessionManagement.KEY_USER_ID)==null){
            session.setUserId("");//koray.cirak@yahoo.com.tr
        }
        else {
            userId = userPrefs.get(SessionManagement.KEY_USER_ID);
        }

        if(userPrefs.get(SessionManagement.KEY_EDGETYPE)==null){
            session.setEdge("0");
        }
        else {
            edgeType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
        }

        if(userPrefs.get(SessionManagement.KEY_MODEL)==null){
            session.setModelType("0");
        }
        else {
            modelType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
        }

        if(userPrefs.get(SessionManagement.KEY_DRAWBOX)==null){
            session.setDrawBox(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL)==null){
            session.setRunAllModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL)==null){
            session.setRunAllBabyModel(false);
        }

        if(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD)==null){
            session.setMinThreshold("0.7");
        }

        if(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD)==null){
            session.setMaxThreshold("1.0");
        }

        //if(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE)==null){
        //    session.setSaveScreen("0");
        //}
        //else {
        //    saveScreenType = Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
        //}
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
        else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /*openCamera();*/

                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show(); // Showing the toast message
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();  // Showing the toast message
            }
        }
    }

    private void openCamera() { //open camera
        if (capture == 1) { // if capture type front camera
            Intent intent = new Intent(HomePageActivity.this, CameraActivity.class);
            intent.putExtra("cameraId", 1);
            startActivity(intent);
        }

        else if (capture == 2) { // if capture type back camera
            Intent intent = new Intent(HomePageActivity.this, CameraActivity.class);
            intent.putExtra("cameraId", 0);
            startActivity(intent);
        }
    }

    private void setLabelTextes(){
        HashMap<String, String> userPrefs = session.getUserDetails();

        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            lbllastscreenshot.setText("Son Ekran Görüntüsü");
            lblAnaSayfaHeader.setText("Ana Sayfa");
        }
        else {
            lbllastscreenshot.setText("Last Screenshot");
            lblAnaSayfaHeader.setText("Home Page");
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}