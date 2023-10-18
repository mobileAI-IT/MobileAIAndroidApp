package bakas.it.objectdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



import java.util.HashMap;

public class MenuActivity extends AppCompatActivity {

    Spinner spInterval;
    EditText spFileAmount;
    EditText txtUserId;
    EditText txtCompName;

    EditText spYellowMinThr;

    EditText spYellowMaxThr;
    Button save;
    float interval=10;
    int fileAmount=5;
    int lang=0;
    int capture=0;

    int edgeType=0;
    int modelType=0;

    boolean drawBox = false;


    int saveScreenType=0;
    String userId="";
    String compName="";
    Spinner spLanguage;
    Toolbar toolbar;
    String safe="";
    String sus="";
    String violence="";
    EditText txtWordSafe;
    EditText txtWordSuspicious;
    EditText txtWordViolence;

    CheckBox chbDrawBox;

    CheckBox chbRunAllModel;

    CheckBox chbRunAllBabyModel;

    Spinner spCapture;

    Spinner spEdgeType;
    Spinner spModelType;



    SessionManagement session;

    TextView lblInterval, lblUserId, lblFileNumber, lblCompName, lblLanguage, lblSave, lblHelp, lblWordSafe, lblWordSuspicious, lblWordViolence, lblCapture, lblEdgeDetection, lblBox, lblSetModelType;

    String[] intervals = new String[121];
    String[] languages = {"Turkish", "English"};
    String[] captures = {"S","Cf","Cb","Vf","Vb"};
    String[] edges = {"Color","Gray","Hist","Edge"};

    String[] models = {"tsfl GUN","gunmain", "ass_Y_FHD", "gunmodelv1", "pussy_Y_FHD","tits_Y_FHD", "face3", "openedeye_Y_480", "closedeye_Y_480", "ear_Y_480", "mouth_Y_480", "nose_Y_480"};

    String[] saveScreens = {"All","Harmful","None"};

    @Override
    protected void onResume() {
        super.onResume();
        setLabelTextes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        toolbar=findViewById(R.id.toolbar);

        session = new SessionManagement(getApplicationContext());

        spInterval=findViewById(R.id.sp_set_interval);
        spLanguage=findViewById(R.id.sp_set_language);
        spFileAmount=findViewById(R.id.txt_set_file_number);
        spYellowMinThr=findViewById(R.id.txt_set_yellow_min_threshold);
        spYellowMaxThr=findViewById(R.id.txt_set_yellow_max_threshold);
        spCapture=findViewById(R.id.sp_set_capture);
        spEdgeType=findViewById(R.id.sp_set_edgedetection);
        spModelType=findViewById(R.id.sp_set_model_type);
        //spSaveScreen=findViewById(R.id.sp_set_save_screen);
        txtUserId=findViewById(R.id.txt_set_user_id);
        txtCompName=findViewById(R.id.txt_set_comp_name);
        lblInterval=findViewById(R.id.lbl_set_interval);
        lblEdgeDetection=findViewById(R.id.lbl_set_edgedetection);
        //lblSaveScreen=findViewById(R.id.lbl_set_save_screen);
        lblUserId = findViewById(R.id.lbl_set_user_id);
        lblFileNumber = findViewById(R.id.lbl_set_file_number);
        lblCompName = findViewById(R.id.lbl_set_comp_name);
        lblLanguage = findViewById(R.id.lbl_set_language);
        lblSave = findViewById(R.id.lbl_save);
        lblHelp = findViewById(R.id.lbl_help);
        save=findViewById(R.id.btn_save);
        txtWordSafe=findViewById(R.id.txt_change_safe);
        lblWordSafe=findViewById(R.id.lbl_change_safe);
        lblBox = findViewById(R.id.lbl_set_box);
        lblSetModelType = findViewById(R.id.lbl_set_model_type);
        txtWordSuspicious=findViewById(R.id.txt_change_sus);
        lblWordSuspicious=findViewById(R.id.lbl_change_sus);
        txtWordViolence=findViewById(R.id.txt_change_violence);
        chbDrawBox = findViewById(R.id.sp_set_box);
        chbRunAllModel = findViewById(R.id.sp_all_mdl_box);
        chbRunAllBabyModel = findViewById(R.id.sp_baby_all_mdl_box);
        lblWordViolence=findViewById(R.id.lbl_change_violence);
        lblCapture=findViewById(R.id.lbl_set_capture);
        lblEdgeDetection=findViewById(R.id.lbl_set_edgedetection);
        //lblSaveScreen=findViewById(R.id.lbl_set_save_screen);

        intervals[0] = String.valueOf(0.5);
        for (int i = 1; i <= 120 ; i++) {
            intervals[i] = String.valueOf((i));
        }

        ArrayAdapter adapterInterval=new ArrayAdapter(this, R.layout.spinner_item,intervals);
        adapterInterval.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spInterval.setAdapter(adapterInterval);

        ArrayAdapter adapterLanguages=new ArrayAdapter(this, R.layout.spinner_item,languages);
        adapterLanguages.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spLanguage.setAdapter(adapterLanguages);

        ArrayAdapter adapterCaptures=new ArrayAdapter(this, R.layout.spinner_item, captures);
        adapterCaptures.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spCapture.setAdapter(adapterCaptures);

        ArrayAdapter adapterEdgeTypes=new ArrayAdapter(this, R.layout.spinner_item, edges);
        adapterEdgeTypes.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spEdgeType.setAdapter(adapterEdgeTypes);

        ArrayAdapter adapterModelTypes=new ArrayAdapter(this, R.layout.spinner_item, models);
        adapterModelTypes.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spModelType.setAdapter(adapterModelTypes);

        //ArrayAdapter adapterSaveScreens=new ArrayAdapter(this, R.layout.spinner_item, saveScreens);
        //adapterSaveScreens.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //spSaveScreen.setAdapter(adapterSaveScreens);

        HashMap<String, String> userPrefs = session.getUserDetails();

        interval=Float.parseFloat(userPrefs.get(SessionManagement.KEY_INTERVAL));
        if(interval > 120){
            interval = 10;
        }
        if (interval == 0.5){
            spInterval.setSelection(0);
        }
        else{
            spInterval.setSelection((int)interval);
        }


        lang=Integer.parseInt(userPrefs.get(SessionManagement.KEY_LANGUAGE));
        spLanguage.setSelection(lang);

        capture=Integer.parseInt(userPrefs.get(SessionManagement.KEY_CAPTURE));
        spCapture.setSelection(capture);

        edgeType=Integer.parseInt(userPrefs.get(SessionManagement.KEY_EDGETYPE));
        spEdgeType.setSelection(edgeType);

        if(userPrefs.get(SessionManagement.KEY_MODEL) != null){
            modelType=Integer.parseInt(userPrefs.get(SessionManagement.KEY_MODEL));
            spModelType.setSelection(modelType);
        }
        else{
            spModelType.setSelection(0);
        }


        chbDrawBox.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_DRAWBOX)));
        chbRunAllModel.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL)));
        chbRunAllBabyModel.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL)));

        //saveScreenType=Integer.parseInt(userPrefs.get(SessionManagement.KEY_SAVESCREENTYPE));
        //spSaveScreen.setSelection(saveScreenType);

        if (!userPrefs.get(SessionManagement.KEY_USER_ID).isEmpty()) {
            userId = String.valueOf(userPrefs.get(SessionManagement.KEY_USER_ID));
        }
        else {
            userId = "";//koray.cirak@yahoo.com.tr
        }
        txtUserId.setText(userId);

        if(userPrefs.get(SessionManagement.KEY_COMP_NAME)!=null) {
            compName = String.valueOf(userPrefs.get(SessionManagement.KEY_COMP_NAME));
        }
        else {
            compName = "";//mobileAI-IT
        }
        txtCompName.setText(compName);

        safe=String.valueOf(userPrefs.get(SessionManagement.KEY_SAFE));
        txtWordSafe.setText(safe);

        violence=String.valueOf(userPrefs.get(SessionManagement.KEY_VIOLENCE));
        txtWordViolence.setText(violence);

        sus=String.valueOf(userPrefs.get(SessionManagement.KEY_SUS));
        txtWordSuspicious.setText(sus);

        chbDrawBox.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_DRAWBOX)));
        chbRunAllModel.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_MODEL)));
        chbRunAllBabyModel.setChecked(Boolean.parseBoolean(userPrefs.get(SessionManagement.KEY_RUN_ALL_BABY_MODEL)));

        fileAmount=Integer.parseInt(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));
        spFileAmount.setText(userPrefs.get(SessionManagement.KEY_FILE_AMOUNT));

        if(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD)!=null) {
            spYellowMinThr.setText(userPrefs.get(SessionManagement.KEY_MIN_THRESHOLD));
        }
        else {
            spYellowMinThr.setText("0.7");
        }

        if(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD)!=null) {
            spYellowMaxThr.setText(userPrefs.get(SessionManagement.KEY_MAX_THRESHOLD));
        }
        else {
            spYellowMaxThr.setText("1.0");
        }
        if(userPrefs.get(SessionManagement.KEY_COMP_NAME)==null){
            toolbar.setTitle("");//mobileAI-IT

        }
        else
            toolbar.setTitle(userPrefs.get(SessionManagement.KEY_COMP_NAME));
        setLabelTextes();
    }

    public void save_button(View view){
        // Check if email id is valid or not
        boolean result = isEmailValid(txtUserId.getText().toString());
        if(!result){
            Toast.makeText(MenuActivity.this, "Please enter valid email!",
                    Toast.LENGTH_LONG).show();
        }
        else{
            session.saveUserPref(txtUserId.getText().toString(),
                    String.valueOf(spInterval.getSelectedItemPosition()),
                    spFileAmount.getText().toString(), txtCompName.getText().toString(),
                    String.valueOf(spLanguage.getSelectedItemPosition()),
                    txtWordSafe.getText().toString(),txtWordSuspicious.getText().toString(),
                    txtWordViolence.getText().toString(), String.valueOf(spCapture.getSelectedItemPosition()), String.valueOf(spEdgeType.getSelectedItemPosition()), String.valueOf(spModelType.getSelectedItemPosition()), chbDrawBox.isChecked(), spYellowMinThr.getText().toString(), spYellowMaxThr.getText().toString(), chbRunAllModel.isChecked(), chbRunAllBabyModel.isChecked());
            finish();
        }
    }

    public void help_button(View view){
        Intent intent=new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    boolean isEmailValid(CharSequence email) {
        if(email == null || email.toString().isEmpty()){
            return  true;

        }
        else{
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    private void setLabelTextes(){
        HashMap<String, String> userPrefs = session.getUserDetails();

        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null
                || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            lblInterval.setText("Aralığı Ayarla: ");
            lblUserId.setText("Email Ayarla: ");
            lblFileNumber.setText("Dosya Sayısı Ayarla: ");
            lblCompName.setText("Başlık Ayarla: ");
            lblWordSafe.setText("Mode 1: ");
            lblWordSuspicious.setText("Mode 2: ");
            lblWordViolence.setText("Mode 3: ");
            lblLanguage.setText("Dil Ayarla: ");
            lblSave.setText("Kaydet");
            lblHelp.setText("Yardım");
            lblCapture.setText("Yakalama Türü: ");
            lblEdgeDetection.setText("Edge Türü: ");
            lblBox.setText("Kutu Ciz: ");
            lblSetModelType.setText("AI Model: ");
            //lblSaveScreen.setText("Ekran Kaydetme: ");
        }
        else{
            lblInterval.setText("Set Interval: ");
            lblUserId.setText("Set User E-mail: ");
            lblFileNumber.setText("Set File Number: ");
            lblCompName.setText("Set Comp: ");
            lblWordSafe.setText("Mode 1: ");
            lblWordSuspicious.setText("Mode 2: ");
            lblWordViolence.setText("Mode 3: ");
            lblLanguage.setText("Set Language: ");
            lblSave.setText("Save Changes");
            lblHelp.setText("Help");
            lblCapture.setText("Set Capture Type: ");
            lblEdgeDetection.setText("Set Edge Type: ");
            lblBox.setText("Set Box: ");
            lblSetModelType.setText("AI Model: ");
            //lblSaveScreen.setText("Save Screen: ");
        }
    }
}