package bakas.it.objectdetection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

public class HelpActivity extends AppCompatActivity {

    String head ="";
    String body ="";
    SessionManagement session;
    Toolbar toolbar;
    TextView lblAnaSAyfaHeader, lblIntervalText, lblFileNumber, lblUserId, lblBack, lblCaptureType, lblEdgeType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        session = new SessionManagement(getApplicationContext());
        toolbar=findViewById(R.id.toolbar);
        lblAnaSAyfaHeader=findViewById(R.id.ana_sayfa_header);
        lblIntervalText=findViewById(R.id.set_interval_text);
        lblUserId=findViewById(R.id.set_user_id_text);
        lblFileNumber=findViewById(R.id.set_file_number_text);
        lblCaptureType=findViewById(R.id.set_capture_type_text);
        lblEdgeType=findViewById(R.id.set_edge_type_text);
        lblBack=findViewById(R.id.back_text);

        setLabelTextes();

    }
    public void setInterval(View view){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            head ="Aralık Ayarla";
            body ="\nBu seçenek ile ekran görüntüleri arasındaki gecikmeyi seçebilirsiniz.\n" +
                    "\n" +
                    "Minimum 0.5, maksimum 120 saniye seçilebilir.";

        }
        else{



            head ="Set Interval";
            body ="\nWith this option, you can select the delay between screenshots.\n" +
                    "\n" +
                    "Minimum 0.5, maximum 120 seconds can be selected.";

        }

        create_alert();
    }
    public void setUserId(View view){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            head ="Email Ayarla";
            body ="\nBu seçenekle, log dosyasında görünecek bir kullanıcı adı girebilirsiniz.";
        }
        else{

            head ="Set User Email";
            body ="\nWith this option, you can enter a user name which will be appear on log file.";
        }

        create_alert();
    }


    public void setFileNumber(View view){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){

            head ="Dosya Numarasını Ayarla";
            body ="\nBu seçenekle, bir oturumdaki ekran görüntüsü miktarını seçebilirsiniz.\n" +
                    "\n" +
                    "Minimum 1, maksimum 9999 ekran görüntüsü seçilebilir.";
        }
        else{
            head ="Set File Number";
            body ="\nWith this option, you can select the amount of screenshots in a session.\n" +
                    "\n" +
                    "Minimum 1, maximum 9999 screenshots can be selected.";
        }

        create_alert();
    }

    public void setCaptureType(View view){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            head ="Yakalama Turu Ayarla";
            body ="\nBu seçenekle, yakalanacak goruntusunun, ekran, on kamera veya arka kemaradan alinacagini secebilirsiniz.";
        }
        else{

            head ="Set Capture Type";
            body ="\nWith this option, you can select the image to be captured on the screen, front camera and rear camera.";
        }

        create_alert();
    }

    public void setEdgeType(View view){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            head ="Edge Turu Ayarla";
            body ="\nBu seçenekle, yakalanacak goruntusunun, rebkli, gri ton, histogram veya kenar belirleme turunde olacagini secebilirsiniz.";
        }
        else{

            head ="Set Edge Type";
            body ="\nWith this option, you can choose whether the image to be captured will be colored, grayscale, histogram, or edge detection type.";
        }

        create_alert();
    }


    public void create_alert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this,R.style.AlertDialogStyle);
        builder.setCancelable(true);
        builder.setTitle(head);
        builder.setMessage(body);

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    public void back_buton(View view)
    {
        this.finish();
    }

    private void setLabelTextes(){
        HashMap<String, String> userPrefs = session.getUserDetails();
        if(userPrefs.get(SessionManagement.KEY_COMP_NAME)==null){
            toolbar.setTitle("");
        }
        else
            toolbar.setTitle(userPrefs.get(SessionManagement.KEY_COMP_NAME));

        if(userPrefs.get(SessionManagement.KEY_LANGUAGE)==null || "0".equals(userPrefs.get(SessionManagement.KEY_LANGUAGE))){
            lblIntervalText.setText("Aralığı Ayarla");
            lblUserId.setText("Email Ayarla");
            lblFileNumber.setText("Dosya Sayısı Ayarla");
            lblAnaSAyfaHeader.setText("Yardım");
            lblBack.setText("Geri");
            lblCaptureType.setText("Yakalama Turu");
            lblEdgeType.setText("Edge Turu");
        }
        else{
            lblIntervalText.setText("Set Interval");
            lblUserId.setText("Set User E-mail");
            lblFileNumber.setText("Set File Number");
            lblAnaSAyfaHeader.setText("Help");
            lblBack.setText("Back");
            lblCaptureType.setText("Capture Type");
            lblEdgeType.setText("Edge Type");

        }
    }

}