package bakas.it.objectdetection;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DisplayActivity extends AppCompatActivity {

    ImageView lastPhoto;
    Toolbar toolbar;
    TextView classifierResultText;
    String lastPhotoFileDir="";//Keeps the last screenshots' file's directory
    String predictionResult="";//Keeps the prediction result
    /*Classifier classifier=new Classifier();//Classifier object*/
    SessionManagement session;//Class for keeping user settings
    ProgressDialog dialog;//Mail sending dialog
    HashMap<String, String> userPrefs;
    int fileAmount, language, capture, edgeType, saveScreenType;
    String userId, safe, violence, suspicious;
    String logs="";

    float interval;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();
        /*classifier.initialize();*/
        lastPhoto = findViewById(R.id.dp_lastPhoto);
        classifierResultText = findViewById(R.id.dp_classifierResultText);
        Bundle bundle = getIntent().getExtras();

        if(getUserId()!=""){
            logs=getUserId()+"\n";
        }
        //Setting toolbar as designed
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new SessionManagement(getApplicationContext());
        userPrefs = session.getUserDetails();

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

        //if(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE)==null){
        //    session.setSaveScreen("0");
        //}
        //else {
        //    saveScreenType= Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
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
        if(bundle != null){
            lastPhotoFileDir = bundle.getString("filePath");
            updatePicture(lastPhotoFileDir);
        }
    }

    private void updatePicture(String lastPhotoFileDir){

        Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + File.separator + lastPhotoFileDir);
        checkPicture(bmp);
        //
    }

    private void checkPicture(Bitmap bitmap) {
        String prediction="";//result of prediction;

        try {//Compress bitmap and write to file
            Bitmap bmp=Bitmap.createScaledBitmap(bitmap, 600, 800, false);//Resizing to 800x600
            bmp=Bitmap.createBitmap(bmp, 0,100,600, 600);//Cropping top 100 and bottom 100 pixels
            bmp=Bitmap.createScaledBitmap(bmp, 320, 320, false);//Resizing to 320x320
           /* classifier.classify(bmp);//Sending 320x320 bitmap to classifier
            prediction=classifier.predict();*/
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
            bitmap=drawTextToBitmap(predictionResult,bitmap);
            setText(predictionResult);
            lastPhoto.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        savePicture(bitmap);
    }

    private void setText(String predictionResult) {
        if(predictionResult=="SAFE" || predictionResult == safe){
            if(safe!="SAFE") {
                classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SAFE));
            }
            else {
                classifierResultText.setText("SAFE");
            }
            classifierResultText.setTextColor(Color.GREEN);
        }


        else if (predictionResult=="VIOLENCE" || predictionResult== violence) {

            if (violence!="HARMFUL") {
                classifierResultText.setText(userPrefs.get(SessionManagement.KEY_VIOLENCE));
            }
            else {
                classifierResultText.setText("HARMFUL");
            }
            classifierResultText.setTextColor(Color.RED);
        }

        else if (predictionResult=="SEXUALITY" || predictionResult== suspicious) {
            if (userPrefs.get(SessionManagement.KEY_SUS) != null) {
                classifierResultText.setText(userPrefs.get(SessionManagement.KEY_SUS));
            }
            else {
                classifierResultText.setText("SEXUALITY");
            }
            classifierResultText.setTextColor(Color.YELLOW);
        }
    }

    private void savePicture(Bitmap bitmap) {
        final File file = new File(Environment.getExternalStorageDirectory() + lastPhotoFileDir);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
         //   String directory=createLogFile();
         //   sendEmail(directory);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private void sendEmail(final String directory) {
        //Opening progress dialog with text sending mail
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending mail...");
        dialog.show();



        Handler handler1 = new Handler();//Timer to wait before mail sending due to problems can occur while stopping screenshot
        handler1.postDelayed(new Runnable() {
            public void run() {
                Mail mail =new Mail(DisplayActivity.this, directory,Environment.getExternalStorageDirectory() + File.separator + lastPhotoFileDir, "koray.cirak@yahoo.com.tr");//Create mail object
                mail.execute();//Execute mail sending
            }
        }, 1000);   //1 seconds

    }
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
    public Bitmap drawTextToBitmap(String gText,Bitmap bitmap) {

        Matrix matrix = new Matrix(); // create new matrix object

        matrix.postRotate(90); // rotate 90 degrees

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        bitmap = Bitmap.createBitmap(scaledBitmap
                , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

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
    private String getUserId(){
        return String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
    }
}
