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
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;

import android.widget.Toast;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mail extends AsyncTask<Void,Void,Void> {

    private Context context;//Context
    private Session session;//Session
    public String email="info@compositeware.com";//Send to this email
    public String subject="Screenshots";//Email subject
    public String message="Screenshots from the last recording sessions has been attached";//Text part of email
    public String logFileDir="";//Last log file's directory
    public String logFileName="";//Last log file's name
    public String screenshotsFileDir="";//Last screenshots' file name


    //Constructor
    public Mail(Context context,String logFileDir,String screenshotsFileDir, String email){
        this.context=context;
        this.logFileDir=logFileDir;
        this.logFileName=logFileDir.substring(logFileDir.length()-18);
        this.screenshotsFileDir=screenshotsFileDir;
        this.email = email;
    }


    //Background tasks while sending mail
    @Override
    protected Void doInBackground(Void... params) {
        Properties props=new Properties();

        /*//Smtp server Sendinblue
        props.setProperty("mail.transport.protocol", "smtp");//SMTP protocol
        props.setProperty("mail.host", "smtp-relay.sendinblue.com");//Host address
        props.put("mail.smtp.auth", "true");//Authorization enabled
        props.put("mail.smtp.port", "587");//Port
        props.setProperty("mail.smtp.quitwait", "false");*/


        /*//Smtp server Elasticemail
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.elasticemail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "2525");
        props.setProperty("mail.smtp.quitwait", "false");*/

        //Smtp server Mailjet
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "in-v3.mailjet.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.setProperty("mail.smtp.quitwait", "false");


        //Smtp server user pass
        session=Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("","");
            }
        });

        //Mail sending commands
        try{
            MimeMessage mimeMessage=new MimeMessage(session);//New mail
            mimeMessage.setFrom(new InternetAddress(""));//From
            mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(email));//To
            mimeMessage.setSubject(subject);//Subject
            mimeMessage.setText(message);//Message


            Multipart multipart = new MimeMultipart();//Adding parts to mail

            File folder = new File(screenshotsFileDir);//Folder that keeps screenshots
            if (!folder.exists()) {//If folder doesn't exist
                folder.mkdirs();//Create folder
            }

            File[] imageFiles = folder.listFiles();//Getting list of files in folder
            for (int i = 0; i < imageFiles.length; i++) {//for all files in folder
                MimeBodyPart imgPart = new MimeBodyPart();//Image part for mail
                String file = imageFiles[i].getAbsolutePath();//Get image file
                String fileName = imageFiles[i].getName();//Get image name
                DataSource source = new FileDataSource(file);//Data source
                imgPart.setDataHandler(new DataHandler(source));//Handler for data
                imgPart.setFileName(fileName);//Setting image file name
                multipart.addBodyPart(imgPart);//Add image to mail
            }

            MimeBodyPart logPart = new MimeBodyPart();
            String file5 = logFileDir;
            String fileName5 = logFileName;
            DataSource source5 = new FileDataSource(file5);
            logPart.setDataHandler(new DataHandler(source5));
            logPart.setFileName(fileName5);
            multipart.addBodyPart(logPart);

            MimeBodyPart textPart = new MimeBodyPart();//Text part for mail
            textPart.setText(message);//Setting text as message
            multipart.addBodyPart(textPart);//Add text to mail

            mimeMessage.setContent(multipart);//Set mail content
            Transport.send(mimeMessage);//Send mail

        }

        //Exception
        catch (MessagingException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    //Toast on sending completed
    @Override
    protected void onPostExecute(Void aVoid) {
        if(context!=null){
            super.onPostExecute(aVoid);
            broadcastUpdate("Mail_Sent");//Broadcast to close dialog
            Toast.makeText(context,"Mail Sent",Toast.LENGTH_LONG).show();//Toast
        }

    }

    //Before executing mail sending
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    //Broadcast updater
    private void broadcastUpdate(final String action) {
        if(context!=null){
            final Intent intent = new Intent(action);
            context.sendBroadcast(intent);
        }
    }

}
