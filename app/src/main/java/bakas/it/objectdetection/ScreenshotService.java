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
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bakas.it.objectdetection.FaceDetection.FaceDetection;
import bakas.it.objectdetection.ObjectDetection.ObjectDetection;
import bakas.it.objectdetection.ObjectDetection.Result;
import bakas.it.objectdetection.ParentControl.ContentDetection;
import bakas.it.objectdetection.classifier.Classifier;

public class ScreenshotService extends Service {

    int mWidth ;//Screen width
    int mHeight ;//Screen height
    int mDensity ;//Screen density
    ImageReader mImageReader;//Image reader
    MediaProjection mProjection;//Media Projection variable for screenshot
    Handler screenshotHandler = new Handler();//Timer for screenshot timed to 10 sec
    private final IBinder mBinder = new LocalBinder();//Gets current service object
    String logs="";//Text that will be written to log file
    Classifier classifier=new Classifier();//Classifier object*/
    int screenshotCount=0;//A counter for last screenshot session and counts the amount how many screenshot taken
    public boolean stoppedAtBackground=false;// Shows if app stopped taking screenshots while on background
    int maxScreenshot=12;//Maximum amount of screenshot per season
    String lastScreenshotsFileDir="";//Direction of screenshots from last session
    String predictionResult="";//Keeps the prediction result
    ArrayList<Result> results = new ArrayList<>();
    List<Result> subResult = new ArrayList<Result>();
    List<Integer> subResultCat = new ArrayList<Integer>();
    SessionManagement session;//Class for keeping user settings
    HashMap<String, String> userPrefs;
    int sendMail = 1;
    float MIN_YELLOW_THRESOLD = 0.7f;
    float MAX_YELLOW_THRESOLD = 1.0f;
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

    public ScreenshotService() {//Empty constructor
        OpenCVLoader.initDebug();
    }

    //Returns current service object
    public class LocalBinder extends Binder {
        public ScreenshotService getService() {
            return ScreenshotService.this;
        }
    }
    //On service bound
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    //Starts auto screenshot and takes a screenshot every 10 secs
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
        this.mProjection=mProjection;//Currently running media projection
        this.lastScreenshotsFileDir=Environment.getExternalStorageDirectory().toString()+"/Parental_Control_Screenshots/"+timeStamp;
        screenshotHandler.postDelayed(new Runnable() {//10 sec timer for screenshot
            @Override
            public void run() {
                if(screenshotCount<getFileAmount()){
                    startScreenshot(timeStamp);//Start taking screenshots
                    screenshotHandler.postDelayed(this,(long)(getInterval()*1000));//creating loop with value of interval secs delay
                }
            }
        }, 0);//0 secs delay

        return true;
    }

    //Gets a bitmap and compressing it to JPG and saving
    public String saveBitmap(Bitmap bitmap,String newFolderName) {
        subResult.clear();
        File myDir = new File(lastScreenshotsFileDir);//Adding our folder to path
        myDir.mkdirs();//Creating our folder if doesn't exist
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());//Getting timestamp
        String fname = "Screenshot_"+ timeStamp +".jpg";//File name
        String prediction="";//result of prediction;
        File file = new File(myDir, fname);//Creating file
        if (file.exists()) file.delete ();//Overwriting file if file already exist
        try {//Compress bitmap and write to file
            FileOutputStream out = new FileOutputStream(file);

            if(getRunAllModel()){
                // 1. item beginning
//
//                ObjectDetection objectDetection = new ObjectDetection();
//                results = objectDetection.analyzeImage(bitmap, getApplicationContext());
//
//                Result maxScore = null;
//                for (Result resultItem : results)
//                { if (maxScore == null)
//                        maxScore = resultItem;
//                    else{
//                        if (maxScore.score < resultItem.score)
//                            maxScore = resultItem;
//                        }
//                }
//                if (maxScore != null)
//                    subResult.add(maxScore);
                // End of 1. item
                // All four models beginning
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                bitmap=Bitmap.createScaledBitmap(bmp, 1088, 1920, false);//Resizing to 320x320
                ContentDetection[] modelNames = {contentDetection_ass, contentDetection_gun, contentDetection_pussy,contentDetection_tits};
                for (ContentDetection model : modelNames) {
                    results = model.analyzeImage(bitmap);
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

                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                bitmap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(bitmap, getApplicationContext(), false);
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
                    Bitmap resizedBmp = Bitmap.createBitmap(bitmap, maxScore.rect.left, maxScore.rect.top,maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
                    bitmap=Bitmap.createScaledBitmap(resizedBmp, 480, 480, false);//Resizing

                    ContentDetection[] modelNames = {contentDetection_openedeye, contentDetection_closedeye, contentDetection_ear,contentDetection_mouth,contentDetection_nose};
                    for (ContentDetection model : modelNames) {

                        results = model.analyzeImage(bitmap);
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
                predictionResult = "";
            }
            else if (getModelType() == 0) {
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
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
                bitmap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                ObjectDetection objectDetection = new ObjectDetection();
                results = objectDetection.analyzeImage(bitmap, getApplicationContext());
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
                bitmap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(bitmap, getApplicationContext(), false);
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
                bitmap=Bitmap.createScaledBitmap(bmp, 1088, 1920, false);//Resizing to 320x320
                ContentDetection[] modelNames = {contentDetection_ass, contentDetection_gun, contentDetection_pussy,contentDetection_tits};
                results =  modelNames[getModelType() -2].analyzeImage(bitmap);
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
                bitmap=Bitmap.createScaledBitmap(bmp, 1080, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(bitmap, getApplicationContext(), false);
                if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult = "";
                    }else {
                        predictionResult="";
                    }
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
                    if(maxScore != null) {
                        Mat uncropped = new Mat();
                        Utils.bitmapToMat(bitmap,uncropped);
                        org.opencv.core.Rect roi = new org.opencv.core.Rect(maxScore.rect.left, maxScore.rect.top, maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
                        Mat cropped = new Mat(uncropped, roi);

                        //Bitmap resizedBmp = Bitmap.createBitmap(bitmap, maxScore.rect.left, maxScore.rect.top, maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
//                        Mat croppedimage = new Mat();
//                        Utils.bitmapToMat(resizedBmp,croppedimage);
                        Mat resizeimage = new Mat();
                        Size sz = new Size(480,480);
                        Imgproc.resize( cropped, resizeimage, sz );
                        Bitmap image1= Bitmap.createBitmap(resizeimage.cols(), resizeimage.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(resizeimage, image1);
                        bitmap = image1;//Bitmap.createScaledBitmap(resizedBmp, 480, 480, false);//Resizing
                    }
                }
                ContentDetection[] modelNames = {contentDetection_openedeye, contentDetection_closedeye, contentDetection_ear,contentDetection_mouth,contentDetection_nose};
                results = modelNames[getModelType() -7].analyzeImage(bitmap);
               if(results == null || results.isEmpty()){
                    if(userPrefs.get(SessionManagement.KEY_SAFE)!=null) {
                        predictionResult ="";
                    }else {
                        predictionResult="";
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
                    predictionResult = "";
                }
            }

            else{
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
                bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
                bitmap=Bitmap.createScaledBitmap(bmp, 1088, 1920, false);//Resizing to 320x320
                results = faceDetection.analyzeImage(bitmap, getApplicationContext(), false);
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

                    int x_center = (bitmap.getWidth() - (maxScore.rect.left + maxScore.rect.right)) / 2;
                    int y_center = (bitmap.getHeight() - (maxScore.rect.top + maxScore.rect.bottom)) / 2;
                    int object_width =  (bitmap.getWidth() - (maxScore.rect.left + maxScore.rect.right));
                    int object_height = (bitmap.getHeight() - (maxScore.rect.top + maxScore.rect.bottom));


                    Bitmap resizedBmp = Bitmap.createBitmap(bitmap, maxScore.rect.left, maxScore.rect.top,maxScore.rect.right - maxScore.rect.left, maxScore.rect.bottom - maxScore.rect.top);
                    bitmap = resizedBmp;

                }

            }


            bitmap=drawTextToBitmap(predictionResult,bitmap);
            if(getDrawBox() && (getModelType() == 1 || getModelType() == 2 || getModelType() == 3 || getModelType() == 4 || getModelType() == 5 || getModelType() == 6 || getModelType() == 7 || getModelType() == 8 || getModelType() == 9|| getModelType() == 10|| getModelType() == 11)){
                bitmap = drawBoundingBoxes(bitmap);
            }


            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);//Saving screenshot
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return predictionResult;

    }

    private int getModelType(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
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
        for (Result result : subResult) {
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

    //Draws text at the center of Bitmap
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

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    //Gets media projection and placing it on image reader then saving it
    @SuppressLint("WrongConstant")
    public void startScreenshot(final String newFolderName){
        classifier.initialize();
        final MediaProjection Projection=mProjection;//copying the projection
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);//Getting device window manager
        Display display = wm.getDefaultDisplay();//Display variable
        final DisplayMetrics metrics = new DisplayMetrics();//Metric values of current display
        display.getMetrics(metrics);//Getting metrics
        Point size = new Point();//Point variable to get real size
        display.getRealSize(size);//getting sizes of screen
        mWidth = size.x;//screen width
        mHeight = size.y;//screen height
        mDensity = metrics.densityDpi;//screen density

        mImageReader= ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);//Image reader to get image from media projection

        final Handler handler = new Handler();//Handler

        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;//Projection flags
        Projection.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.getSurface(), null, handler);//Creating virtual display and saving it to mImageReader

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {//Saving an image on image eradder after calculations
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(null, handler);//Setting listener to null

                Image image = reader.acquireLatestImage();//Creating image

                final Image.Plane[] planes = image.getPlanes();//Planes of image
                final ByteBuffer buffer = planes[0].getBuffer();//Byte buffer that will be a bitmap

                //bitmap sizes calculations
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                // create bitmap
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride), metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);//getting pixels from buffer to bitmap

                image.close();
                reader.close();

                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());//Getting real size bitmap
                bmp.recycle();
                //predictionResult=saveBitmap(realSizeBitmap,newFolderName);//Saving bitmap
                if(getEdgeType() == 0){
                    predictionResult=saveBitmap(realSizeBitmap,newFolderName);//Saving bitmap
                }
                else if (getEdgeType() == 1){

                    Bitmap edgeGrayBitmap = EdgeGray(realSizeBitmap);
                    predictionResult=saveBitmap(edgeGrayBitmap,newFolderName);
                }
                else if(getEdgeType() == 2){
                    Bitmap histEquBitmap = HistogramEqulaizationNew(realSizeBitmap);
                    predictionResult = saveBitmap(histEquBitmap,newFolderName);
                }
                else{
                    Bitmap edgeDetectionBitmap =EdgeDetection(realSizeBitmap);
                    predictionResult = saveBitmap(edgeDetectionBitmap,newFolderName);
                }
                String timeStamp = new SimpleDateFormat("dd.MM-HH:mm:ss").format(new Date());//Getting timestamp
                logs+=timeStamp+" "+predictionResult+"\n";//Adding a new line to logs
            }
        }, handler);



        screenshotCount+=1;
        if(screenshotCount==getFileAmount()){
            finishSession();
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

        //Convert img1 into gray image
        Imgproc.cvtColor(img1, imageGray1, Imgproc.COLOR_BGR2GRAY);


        //////////////////Transform Canny to Bitmap/////////////////////////////////////////
        image1= Bitmap.createBitmap(imageGray1.cols(), imageGray1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageGray1, image1);

        return image1;
    }

    public static Bitmap HistogramEqulaization(Bitmap first) {

        Bitmap image1;

        ///////////////transform back to Mat to be able to get Canny images//////////////////
        Mat img1=new Mat();
        Utils.bitmapToMat(first,img1);
        /////////////////////////////////////////////////////////////////

        // Applying blur
        Imgproc.blur(img1, img1, new Size(3, 3));
        // Applying color
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2YCrCb);
        List<Mat> channels = new ArrayList<Mat>();

        // Splitting the channels
        Core.split(img1, channels);

        // Equalizing the histogram of the image
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        Core.merge(channels, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_YCrCb2BGR);

        Mat gray = new Mat();
        Imgproc.cvtColor(img1, gray, Imgproc.COLOR_BGR2GRAY);
        Mat grayOrig = new Mat();
        Imgproc.cvtColor(img1, grayOrig, Imgproc.COLOR_BGR2GRAY);

        ///////////////////////////////////////////////////////////////////

        //////////////////Transform Canny to Bitmap/////////////////////////////////////////
        image1= Bitmap.createBitmap(img1.cols(), img1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img1, image1);

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

    //On stop button pressed it stops screen capturing and drops screenshotHandler timer
    public String stopScreenshot(){
        screenshotHandler.removeCallbacksAndMessages(null);
        mProjection.stop();//Stopping screen capture
        String directory=createLogFile();//Creating log file
        return directory;
    }

    //Creates log file with logs string and timestamp as name
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

    private void finishSession(){
        stoppedAtBackground=true;//finish flag
        String lastLogFileDir=stopScreenshot();//Stop taking screenshots
        if(sendMail == 1){
            sendEmail(lastLogFileDir);//Send email on stop
        }


    }

    //Sending email of screenshots
    public void sendEmail(final String lastLogFileDir)
    {
        if(!TextUtils.isEmpty(getUserId())){
            Mail mail =new Mail(null,lastLogFileDir,lastScreenshotsFileDir, getUserId());//Create mail object
            mail.execute();//Execute mail sending
        }

    }

    private float getInterval(){
        return Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
    }
    private int getFileAmount(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
    }
    private String getUserId(){
        return String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
    }

    private int getEdgeType(){
        return Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
    }

    private boolean getDrawBox(){
        return Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_DRAWBOX));
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

    //private int getSaveScreenType(){
    //    return Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
    //}

}
