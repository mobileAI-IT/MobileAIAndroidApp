package bakas.it.objectdetection;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bakas.it.objectdetection.FaceDetection.FaceDetection;
import bakas.it.objectdetection.ObjectDetection.ObjectDetection;
import bakas.it.objectdetection.ObjectDetection.Result;
import bakas.it.objectdetection.ParentControl.ContentDetection;
import bakas.it.objectdetection.classifier.Classifier;

public class CameraService extends Service {

    private final IBinder mBinder = new LocalBinder();//Gets current service object
    int screenshotCount=0;//A counter for last screenshot session and counts the amount how many screenshot taken
    SessionManagement session;//Class for keeping user settings
    HashMap<String, String> userPrefs;
    String logs="";//Text that will be written to log file
    String lastScreenshotsFileDir="";//Direction of screenshots from last session
    Handler cameraCaptureHandler = new Handler();//Timer for screenshot timed to 10 sec
    public boolean stoppedAtBackground=false;// Shows if app stopped taking screenshots while on background
    String predictionResult="";//Keeps the prediction result
    ArrayList<Result> results = new ArrayList<>();
    Classifier classifier=new Classifier();//Classifier object*/
    List<Result> subResult = new ArrayList<Result>();
    List<Integer> subResultCat = new ArrayList<Integer>();

    ContentDetection contentDetection_ass = null;
    ContentDetection contentDetection_gun = null;
    ContentDetection contentDetection_pussy = null;
    ContentDetection contentDetection_tits = null;
    ContentDetection contentDetection_openedeye = null;
    ContentDetection contentDetection_closedeye = null;
    ContentDetection contentDetection_ear = null;
    ContentDetection contentDetection_mouth = null;
    ContentDetection contentDetection_nose = null;
    FaceDetection faceDetection = null;

    static int x=0;
    static int camId = 0;
    int rotationPicture=180;
    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Camera.Parameters parameters;
    /** Called when the activity is first created. */
    @Override
    public void onCreate()
    {
        OpenCVLoader.initDebug();
        if (mCamera != null){
            parameters = mCamera.getParameters();
            int orientation = getDisplayOrientation(camId);
            parameters.setRotation(rotationPicture);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(orientation);

        }
        super.onCreate();

    }

    public class LocalBinder extends Binder {
        public CameraService getService() {
            return CameraService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean initialize(MediaProjection mProjection, final String timeStamp, final float interval) {
        if (contentDetection_ass == null){
            contentDetection_ass = new ContentDetection(getApplicationContext(),"ass_Y_FHD_1409.ptl");
        }
        if (contentDetection_gun == null){
            contentDetection_gun = new ContentDetection(getApplicationContext(), "gunmodelv1.ptl");
        }
        if (contentDetection_pussy == null){
            contentDetection_pussy = new ContentDetection(getApplicationContext(),  "pussy_Y_FHD_1409.ptl");
        }
        if (contentDetection_tits == null){
            contentDetection_tits = new ContentDetection(getApplicationContext(),  "tits_Y_FHD_1409.ptl");
        }
        if (contentDetection_openedeye == null){
            contentDetection_openedeye = new ContentDetection(getApplicationContext(),"openedeye_Y_480_1109.ptl");
        }
        if (contentDetection_closedeye == null){
            contentDetection_closedeye = new ContentDetection(getApplicationContext(), "closedeye_Y_480_1109.ptl");
        }
        if (contentDetection_ear == null){
            contentDetection_ear = new ContentDetection(getApplicationContext(),  "ear_Y_480_1109.ptl");
        }
        if (contentDetection_mouth == null){
            contentDetection_mouth = new ContentDetection(getApplicationContext(),  "mouth_Y_480_1109.ptl");
        }
        if (contentDetection_nose == null){
            contentDetection_nose = new ContentDetection(getApplicationContext(),  "nose_Y_480_1109.ptl");
        }
        if (faceDetection == null){
            faceDetection = new FaceDetection(getApplicationContext());
        }
        screenshotCount=0;//Reset taken screenshots counter
        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();
        if(getUserId()!=""){
            logs=getUserId()+"\n";
        }
        /*this.mProjection=mProjection;//Currently running media projection*/
        this.lastScreenshotsFileDir= Environment.getExternalStorageDirectory().toString()+"/Parental_Control_Screenshots/"+timeStamp;
        cameraCaptureHandler.postDelayed(new Runnable() {//10 sec timer for screenshot
            @Override
            public void run() {
                if(screenshotCount<getFileAmount()){
                    startCapture(timeStamp);//Start taking screenshots
                    cameraCaptureHandler.postDelayed(this,(long)(getInterval()*1000));//creating loop with value of interval secs delay
                }
            }
        }, 0);//0 secs delay

        return true;
    }

    public String stopScreenshot(){
        cameraCaptureHandler.removeCallbacksAndMessages(null);
        screenshotCount = getFileAmount();//Stopping screen capture
        String directory=createLogFile();//Creating log file
        return directory;
    }

    public String createLogFile(){
        String timeStamp = new SimpleDateFormat("ddMM-HHmm").format(new Date());//Getting timestamp
        String fname = "BBPC-"+ timeStamp +".txt";//File name
            /*OutputStreamWriter outputStreamWriter = new OutputStreamWriter(MyApplication.getInstance().openFileOutput(fname, Context.MODE_PRIVATE));//File writer
            outputStreamWriter.write(logs);//Writing logs to file
            outputStreamWriter.close();//Close writer*/

        String root = Environment.getExternalStorageDirectory().toString();//External Storage Path
        File myDir = new File(root + "/Parental_Control_Log_Files");//Adding our folder to path
        myDir.mkdirs();//Creating our folder if doesn't exist

        File file = new File(myDir, fname);//Creating file
        if (file.exists()) file.delete ();//Overwriting file if file already exist
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);
            out.write(logs.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logs="";//Reset logs
        return myDir+"/"+fname;
    }

    @SuppressLint("WrongConstant")
    public void startCapture(final String newFolderName){
        classifier.initialize();
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (getCapture() == 1){
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        camId = camIdx;
                        mCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                if (getCapture() == 2){
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        try {
                            camId = camIdx;
                            mCamera = Camera.open(camIdx);
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
/*
        mCamera = Camera.open();
*/
/*
        SurfaceView sv = new SurfaceView(getApplicationContext());
*/
        mCamera.stopPreview();
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        int orientation = getDisplayOrientation(camId);


        try {
            mCamera.setPreviewTexture(surfaceTexture);
            parameters = mCamera.getParameters();


            //set camera parameters

           List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            Camera.Size mSize = null;
            for (Camera.Size size : sizes) {
                mSize = size;
                break;
            }

            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size mPreviewSize = null;
            for (Camera.Size size : previewSizes) {
                mPreviewSize = size;
                break;
            }

            parameters.setPictureSize(mSize.width, mSize.height);
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            parameters.setRotation(rotationPicture);




            /*setDisplayOrientation(mCamera, 180);*/
            /*parameters.set("orientation", "portrait");*/
            mCamera.setParameters(parameters);

            mCamera.setDisplayOrientation(orientation);



            /*mCamera.setDisplayOrientation(90);*/
            mCamera.startPreview();
            mCamera.takePicture(null, null, mCall);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*//Get a surface
        sHolder = surfaceTexture.getHolder();
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/

    }

    private int getDisplayOrientation(int cameraId) {

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        WindowManager windowService = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowService.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
            rotationPicture = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
            rotationPicture = result;
        }

        return result;
    }




    Camera.PictureCallback mCall = new Camera.PictureCallback()
    {

        public void onPictureTaken(byte[] data, Camera camera)
        {

           /* Matrix rotationMatrix = new Matrix();
            if (getCapture() == 1){
                rotationMatrix.postRotate(270);
            }
            else{
                rotationMatrix.postRotate(90);
            }


            Bitmap img = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap img2 = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(),
                    rotationMatrix, true);


            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            img2.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            img2.recycle();*/

            //decode the data obtained by the camera into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            File myDir = new File(lastScreenshotsFileDir);//Adding our folder to path
            myDir.mkdirs();//Creating our folder if doesn't exist

            FileOutputStream outStream = null;
            try{
                x++;
                outStream = new FileOutputStream(lastScreenshotsFileDir+"/Image"+x+".jpg");
//                outStream.write(data);
                bitmap.compress(Bitmap.CompressFormat.JPEG,50, outStream);
                outStream.close();
                mCamera.release();
                Toast.makeText(getApplicationContext(), "picture clicked", Toast.LENGTH_LONG).show();
                classifyPhoto();
                screenshotCount ++;
                if(screenshotCount==getFileAmount()){
                    finishSession();
                }

            } catch (FileNotFoundException e){
                Log.d("CAMERA", e.getMessage());
            } catch (IOException e){
                Log.d("CAMERA", e.getMessage());
            }

        }
    };

    private void finishSession(){
        stoppedAtBackground=true;//finish flag
        String lastLogFileDir=stopScreenshot();//Stop taking screenshots
        sendEmail(lastLogFileDir);//Send email on stop
    }

    public void sendEmail(final String lastLogFileDir)
    {
        if(!TextUtils.isEmpty(getUserId())){
            Mail mail =new Mail(null,lastLogFileDir,lastScreenshotsFileDir, getUserId());//Create mail object
            mail.execute();//Execute mail sending
        }

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
        //Imgproc.Canny(imageGray1, imageCny1, 10, 100, 3, true);

        ///////////////////////////////////////////////////////////////////

        //////////////////Transform Canny to Bitmap/////////////////////////////////////////
        image1= Bitmap.createBitmap(imageGray1.cols(), imageGray1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageGray1, image1);

        return image1;
    }

    public static Bitmap HistogramEqulaizationNew(Bitmap first) {

        Bitmap image1;

        ///////////////transform back to Mat to be able to get Canny images//////////////////
        Mat img1=new Mat();
        Utils.bitmapToMat(first,img1);
        /////////////////////////////////////////////////////////////////


        // Applying color
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
        Mat dst = new Mat();
        Imgproc.equalizeHist( img1, dst );

        ///////////////////////////////////////////////////////////////////

        //////////////////Transform Canny to Bitmap/////////////////////////////////////////
        image1= Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, image1);

        return image1;
    }

    public static Bitmap EdgeDetection(Bitmap first) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(first, rgba);

        Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.Canny(edges, edges, 80, 100);
        Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
        return  resultBitmap;
    }

    private String classifyPhoto(){
        String prediction="";//result of prediction;
        Bitmap bitmap = BitmapFactory.decodeFile(lastScreenshotsFileDir+"/Image"+x+".jpg");
        Bitmap finalBitMap;
        if (getEdgeType() == 1){

            finalBitMap = EdgeGray(bitmap);

        }
        else if(getEdgeType() == 2){
            finalBitMap = HistogramEqulaizationNew(bitmap);

        }
        else if(getEdgeType() == 3){
            finalBitMap =EdgeDetection(bitmap);
        }
        else{
            finalBitMap = bitmap;
        }

        File myDir = new File(lastScreenshotsFileDir);
        File file = new File(myDir, "Image"+x+"_1.jpg");//Creating file
        /*if (file.exists()) file.delete ();//Overwriting file if file already exist*/
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);
            if(getRunAllModel()){
                Bitmap bmp=Bitmap.createScaledBitmap(finalBitMap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1088, 1920, false);//Resizing to 320x320
                // 1. item beginning

//                ObjectDetection objectDetection = new ObjectDetection();
//                results = objectDetection.analyzeImage(finalBitMap, getApplicationContext());
//
//                Result maxScore = null;
//                for (Result resultItem : results)
//                { if (maxScore == null)
//                    maxScore = resultItem;
//                else{
//                    if (maxScore.score < resultItem.score)
//                        maxScore = resultItem;
//                }
//                }
//                if (maxScore != null)
//                    subResult.add(maxScore);
                // End of 1. item
                // All four models beginning
                ContentDetection[] modelNames = {contentDetection_ass, contentDetection_gun, contentDetection_pussy,contentDetection_tits};
                for (ContentDetection model : modelNames) {
                    results = model.analyzeImage(finalBitMap);
                    Result maxScore = null;
                    for (Result resultItem : results)
                    { if (maxScore == null)
                        maxScore = resultItem;
                    else{
                        if (maxScore.score < resultItem.score)
                            maxScore = resultItem;
                    }
                    }
                    if (maxScore != null)
                        subResult.add(maxScore);
                }
            }
            else if(getRunAllBabyModel()){

                Bitmap bmp=Bitmap.createScaledBitmap(finalBitMap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(finalBitMap, getApplicationContext(), false);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else{
                    Result maxScore = null;
                    for (Result resultItem : results)
                    { if (maxScore == null)
                        maxScore = resultItem;
                    else{
                        if (maxScore.score < resultItem.score)
                            maxScore = resultItem;
                    }
                    }
                    Bitmap resizedBmp = Bitmap.createBitmap(finalBitMap, maxScore.rect.left, maxScore.rect.top,maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
                    finalBitMap=Bitmap.createScaledBitmap(resizedBmp, 480, 480, false);//Resizing

                    ContentDetection[] modelNames = {contentDetection_openedeye, contentDetection_closedeye, contentDetection_ear,contentDetection_mouth,contentDetection_nose};
                    for (ContentDetection model : modelNames) {
                        results = model.analyzeImage(finalBitMap);
                        maxScore = null;
                        for (Result resultItem : results)
                        { if (maxScore == null)
                            maxScore = resultItem;
                        else{
                            if (maxScore.score < resultItem.score)
                                maxScore = resultItem;
                        }
                        }
                        if (maxScore != null)
                            subResult.add(maxScore);
                    }
                }

            }
            else if (getModelType() == 0) {
                Bitmap bmp=Bitmap.createScaledBitmap(finalBitMap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                bmp=Bitmap.createScaledBitmap(bmp, 320, 320, false);//Resizing to 320x320
                classifier.classify(bmp);//Sending 320x320 bitmap to classifier
                prediction=classifier.predict();

                if(prediction.equals("BAKAS BILISIM framework classification result: Normal Content")){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else if(prediction.equals("BAKAS BILISIM framework classification result: Violence Content")){
                    if(userPrefs.get(SessionManagement.KEY_VIOLENCE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_VIOLENCE));
                    }else {
                        predictionResult="VIOLENCE";
                    }
                }
                else{
                    if(userPrefs.get(SessionManagement.KEY_SUS)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SUS));
                    }else {
                        predictionResult="SUSPICIOUS";
                    }
                }
            }
            else if (getModelType() == 1){
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                ObjectDetection objectDetection = new ObjectDetection();
                results = objectDetection.analyzeImage(finalBitMap, getApplicationContext());
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else{
                    if(results.size() > 3){
                        subResult = results.subList(0, 3);
                    }
                    else{
                        subResult = results;
                    }

                    for (Result resultItem : subResult)
                    {
                        if (resultItem.score > getMinThreshold() &&  resultItem.score < getMaxThreshold()){
                            subResultCat.add(2);
                        }
                        else{
                            subResultCat.add(3);
                        }
                    }

                    if (subResultCat.contains(3)){
                        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_VIOLENCE));
                        }else {
                            predictionResult="VIOLENCE";
                        }
                    }
                    else{
                        if(userPrefs.get(SessionManagement.KEY_SUS)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_SUS));
                        }else {
                            predictionResult="SUSPICIOUS";
                        }
                    }
                }
            }
            else if (getModelType() == 6){
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(finalBitMap, getApplicationContext(), false);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else{

                    for (Result resultItem : results)
                    {
                        if (resultItem.score > getMinThreshold() &&  resultItem.score < getMaxThreshold()){
                            subResultCat.add(2);
                        }
                        else{
                            subResultCat.add(3);
                        }
                    }

                    if (subResultCat.contains(3)){
                        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_VIOLENCE));
                        }else {
                            predictionResult="VIOLENCE";
                        }
                    }
                    else{
                        if(userPrefs.get(SessionManagement.KEY_SUS)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_SUS));
                        }else {
                            predictionResult="SUSPICIOUS";
                        }
                    }
                }
                subResult = results;
            }

            else if (getModelType() == 2 || getModelType() == 3 || getModelType() == 4 || getModelType() == 5){

                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1088, 1920, false);//Resizing to 320x320
                ContentDetection[] modelNames = {contentDetection_ass, contentDetection_gun, contentDetection_pussy,contentDetection_tits};
                results = modelNames[getModelType() -2].analyzeImage(finalBitMap);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else{
                    if(results.size() > 3){
                        subResult = results.subList(0, 3);
                    }
                    else{
                        subResult = results;
                    }
                    for (Result resultItem : subResult)
                    {
                        if (resultItem.score > getMinThreshold() &&  resultItem.score < getMaxThreshold()){
                            subResultCat.add(2);
                        }
                        else{
                            subResultCat.add(3);
                        }
                    }

                    if (subResultCat.contains(3)){
                        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_VIOLENCE));
                        }else {
                            predictionResult="VIOLENCE";
                        }
                    }
                    else{
                        if(userPrefs.get(SessionManagement.KEY_SUS)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_SUS));
                        }else {
                            predictionResult="SUSPICIOUS";
                        }
                    }

                }

            }
            else if (getModelType() == 7 || getModelType() == 8 || getModelType() == 9|| getModelType() == 10|| getModelType() == 11){
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                finalBitMap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(finalBitMap, getApplicationContext(), false);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                    predictionResult="";
                }
                else {
                    Result maxScore = null;
                    for (Result resultItem : results) {
                        if (maxScore == null)
                            maxScore = resultItem;
                        else {
                            if (maxScore.score < resultItem.score)
                                maxScore = resultItem;
                        }
                    }
                    if(maxScore != null){
                        Bitmap resizedBmp = Bitmap.createBitmap(finalBitMap, maxScore.rect.left, maxScore.rect.top, maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
                        finalBitMap = Bitmap.createScaledBitmap(resizedBmp, 480, 480, false);//Resizing
                    }

                }
                ContentDetection[] modelNames = {contentDetection_openedeye, contentDetection_closedeye, contentDetection_ear,contentDetection_mouth,contentDetection_nose};
                results = modelNames[getModelType() -7].analyzeImage(finalBitMap);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = (userPrefs.get(SessionManagement.KEY_SAFE));
                    }else {
                        predictionResult="SAFE";
                    }
                }
                else{
                    if(results.size() > 3){
                        subResult = results.subList(0, 3);
                    }
                    else{
                        subResult = results;
                    }
                    for (Result resultItem : subResult)
                    {
                        if (resultItem.score > getMinThreshold() &&  resultItem.score < getMaxThreshold()){
                            subResultCat.add(2);
                        }
                        else{
                            subResultCat.add(3);
                        }
                    }

                    if (subResultCat.contains(3)){
                        if(userPrefs.get(SessionManagement.KEY_VIOLENCE)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_VIOLENCE));
                        }else {
                            predictionResult="VIOLENCE";
                        }
                    }
                    else{
                        if(userPrefs.get(SessionManagement.KEY_SUS)!=null) {
                            predictionResult = (userPrefs.get(SessionManagement.KEY_SUS));
                        }else {
                            predictionResult="SUSPICIOUS";
                        }
                    }
                }
            }
            finalBitMap=drawTextToBitmap(predictionResult,finalBitMap);
            if(getDrawBox() && (getModelType() == 1 || getModelType() == 2 || getModelType() == 3 || getModelType() == 4 || getModelType() == 5 || getModelType() == 6 || getModelType() == 7 || getModelType() == 8 || getModelType() == 9|| getModelType() == 10|| getModelType() == 11)){
                finalBitMap = drawBoundingBoxes(finalBitMap);
            }

            finalBitMap.compress(Bitmap.CompressFormat.JPEG, 100, out);//Saving screenshot
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file1 = new File(myDir, "Image"+x+".jpg");//Creating file

        if (file1.exists()) file1.delete ();

        return predictionResult;

    }

    private Bitmap drawBoundingBoxes(Bitmap selectedImage) {
        Bitmap mutableBitmap = selectedImage.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(15);

        int index = 0;
        for (Result result : results) {
            if (subResultCat.size() > 0 && subResultCat.get(index) == 2){
                boxPaint.setColor(Color.YELLOW);
            }
            else if (subResultCat.size() > 0 && subResultCat.get(index) == 3){
                boxPaint.setColor(Color.RED);
            }
            float left = result.rect.left;
            float top = result.rect.top;
            float right = result.rect.right;
            float bottom = result.rect.bottom;
            canvas.drawRect(left, top, right, bottom, boxPaint);

            String objectName = "Object";
            float textX = left;
            float textY = top - 5;
            canvas.drawText(objectName, textX, textY, textPaint);

            float scoreX = textX + textPaint.measureText(objectName) + 10;
            float confidence = result.score;
            @SuppressLint("DefaultLocale") String confidenceText = String.format("%.2f", confidence);
            canvas.drawText(confidenceText, scoreX, textY, textPaint);

        }


        /*lastScreenshot.setImageBitmap(mutableBitmap);*/
        return mutableBitmap;
    }

    public Bitmap drawTextToBitmap(String gText,Bitmap bitmap) {

        float scale = MyApplication.getInstance().getResources().getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        if(gText=="SAFE" || gText==userPrefs.get(SessionManagement.KEY_SAFE)) {
            paint.setColor(Color.rgb(32, 197, 14));//green
        }
        else if(gText=="SUSPICIOUS" || gText==userPrefs.get(SessionManagement.KEY_SUS)) {
            paint.setColor(Color.rgb(214, 209, 56));//yellow
        }
        else {
            paint.setColor(Color.rgb(214, 53, 18));//red
        }
        // text size in pixels
        paint.setTextSize((int) (20 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;


        if (getCapture() == 2){
            canvas.rotate(270, x, y);
        }
        else{
            canvas.rotate(90, x, y);
        }
        canvas.drawText(gText, x, y, paint);


        return bitmap;
    }

    private String getUserId(){
        return String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
    }

    private int getFileAmount(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
    }

    private float getInterval(){
        return Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
    }

    private float getCapture(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
    }

    private int getEdgeType(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
    }

    private boolean getDrawBox(){
        return Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_DRAWBOX));
    }

    private int getModelType(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
    }

    private boolean getRunAllModel(){
        return Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL));
    }

    private boolean getRunAllBabyModel(){
        return Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL));
    }

    private float getMinThreshold(){
        return Float.parseFloat(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD));
    }

    private float getMaxThreshold(){
        return Float.parseFloat(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD));
    }

}
