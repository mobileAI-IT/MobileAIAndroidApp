package bakas.it.objectdetection;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManagement {
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "UserPref";

    // Interval
    public static final String KEY_INTERVAL = "interval";

    // User ID
    public static final String KEY_USER_ID = "info@mobileai-it.com";

    // File Amount
    public static final String KEY_FILE_AMOUNT = "fileAmount";

    // File Amount
    public static final String KEY_COMP_NAME = "compName";

    // Interval
    public static final String KEY_LANGUAGE = "language";

    // Safe
    public static final String KEY_SAFE = "SAFE";

    // Suspicious
    public static final String KEY_SUS = "SUSPICIOUS";

    // Violence
    public static final String KEY_VIOLENCE = "HARMFUL";

    // Capture Type
    public static final String KEY_CAPTURE = "capture";

    // Edge Type
    public static final String KEY_EDGETYPE = "edgetype";

    public static final String KEY_MODEL = "modeltype";

    public static final String KEY_DRAWBOX = "drawbox";

    public static final String KEY_RUN_ALL_MODEL = "runAllModels";

    public static final String KEY_RUN_ALL_BABY_MODEL = "runAllBabyModels";

    public static final String KEY_MIN_THRESHOLD = "minThreshold";

    public static final String KEY_MAX_THRESHOLD = "maxThreshold";

    // Save Screen Type
    //public static final String KEY_SAVESCREENTYPE = "savescreentype";

    // Constructor
    public SessionManagement(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void saveUserPref(String userID,String interval, String fileAmount,
                             String compName, String lang, String safe, String sus,
                             String violence, String capture, String edge, String model, boolean drawBox, String minThreshold, String maxThreshold, boolean runAllModel, boolean runAllBabyModel){

        // Storing userID in pref
        editor.putString(KEY_USER_ID, userID);

        // Storing interval in pref
        editor.putString(KEY_INTERVAL, interval);

        // Storing fileAmount in pref
        editor.putString(KEY_FILE_AMOUNT, fileAmount);

        // Storing compName in pref
        editor.putString(KEY_COMP_NAME, compName);

        // Storing language in pref
        editor.putString(KEY_LANGUAGE, lang);

        editor.putString(KEY_SAFE, safe);

        editor.putString(KEY_SUS, sus);

        editor.putString(KEY_VIOLENCE, violence);

        editor.putString(KEY_CAPTURE, capture);

        editor.putString(KEY_EDGETYPE, edge);

        editor.putString(KEY_MODEL, model);

        editor.putString(KEY_DRAWBOX, String.valueOf(drawBox));
        editor.putString(KEY_RUN_ALL_MODEL, String.valueOf(runAllModel));
        editor.putString(KEY_RUN_ALL_BABY_MODEL, String.valueOf(runAllBabyModel));
        editor.putString(KEY_MIN_THRESHOLD, minThreshold);
        editor.putString(KEY_MAX_THRESHOLD, maxThreshold);

        //editor.putString(KEY_SAVESCREENTYPE, saveScreenType);

        // commit changes
        editor.commit();
    }
    public void setUserId(String userID){

        // Storing userID in pref
        editor.putString(KEY_USER_ID, userID);

        // commit changes
        editor.commit();
    }

    public void setCompName(String comp) {

        // Storing compName in pref
        editor.putString(KEY_COMP_NAME, comp);

        // commit changes
        editor.commit();
    }

    public void setLanguage(String lang){

        // Storing userID in pref
        editor.putString(KEY_LANGUAGE, lang);

        // commit changes
        editor.commit();
    }
    public void setInterval(String interval){

        // Storing userID in pref
        editor.putString(KEY_INTERVAL, interval);

        // commit changes
        editor.commit();
    }
    public void setFileAmount(String fileAmount){

        // Storing userID in pref
        editor.putString(KEY_FILE_AMOUNT, fileAmount);

        // commit changes
        editor.commit();
    }


    public void setCapture(String capture){
        editor.putString(KEY_CAPTURE, capture);

        editor.commit();
    }

    public void setEdge(String edge){

        // Storing userID in pref
        editor.putString(KEY_EDGETYPE, edge);

        // commit changes
        editor.commit();
    }

    public void setModelType(String modelType){

        // Storing userID in pref
        editor.putString(KEY_MODEL, modelType);

        // commit changes
        editor.commit();
    }

    public void setDrawBox(boolean draw){

        // Storing userID in pref
        editor.putString(KEY_DRAWBOX, String.valueOf(draw));

        // commit changes
        editor.commit();
    }

    public void setRunAllModel(boolean run){

        // Storing userID in pref
        editor.putString(KEY_RUN_ALL_MODEL, String.valueOf(run));

        // commit changes
        editor.commit();
    }

    public void setRunAllBabyModel(boolean run){

        // Storing userID in pref
        editor.putString(KEY_RUN_ALL_BABY_MODEL, String.valueOf(run));

        // commit changes
        editor.commit();
    }

    public void setMinThreshold(String minThreshold){

        // Storing userID in pref
        editor.putString(KEY_MIN_THRESHOLD, minThreshold);

        // commit changes
        editor.commit();
    }

    public void setMaxThreshold(String maxThreshold){

        // Storing userID in pref
        editor.putString(KEY_MAX_THRESHOLD, maxThreshold);

        // commit changes
        editor.commit();
    }

   // public void setSaveScreen(String saveScreen){

        // Storing userID in pref
    //    editor.putString(KEY_SAVESCREENTYPE, saveScreen);

        // commit changes
    //    editor.commit();
    //}

    public void setSafe(String safe){
        editor.putString(KEY_SAFE, safe);

        editor.commit();
    }

    public void setSus(String sus){
        editor.putString(KEY_SUS, sus);

        editor.commit();
    }

    public void setViolence(String violence){
        editor.putString(KEY_VIOLENCE, violence);

        editor.commit();
    }


    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user id
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        // user interval
        user.put(KEY_INTERVAL, pref.getString(KEY_INTERVAL, null));

        // user fileAmount
        user.put(KEY_FILE_AMOUNT, pref.getString(KEY_FILE_AMOUNT, null));

        user.put(KEY_DRAWBOX, pref.getString(KEY_DRAWBOX, null));

        user.put(KEY_RUN_ALL_MODEL, pref.getString(KEY_RUN_ALL_MODEL, null));

        user.put(KEY_RUN_ALL_BABY_MODEL, pref.getString(KEY_RUN_ALL_BABY_MODEL, null));

        // user fileAmount
        user.put(KEY_COMP_NAME, pref.getString(KEY_COMP_NAME, null));

        // user language choice
        user.put(KEY_LANGUAGE, pref.getString(KEY_LANGUAGE, null));

        // user safe word
        user.put(KEY_SAFE, pref.getString(KEY_SAFE, null));

        // user suspicious word
        user.put(KEY_SUS, pref.getString(KEY_SUS, null));

        // user violence word
        user.put(KEY_VIOLENCE, pref.getString(KEY_VIOLENCE, null));

        // user capture type choice
        user.put(KEY_CAPTURE, pref.getString(KEY_CAPTURE, null));

        //user edge type choice
        user.put(KEY_EDGETYPE, pref.getString(KEY_EDGETYPE, null));

        user.put(KEY_MODEL, pref.getString(KEY_MODEL, null));

        user.put(KEY_MIN_THRESHOLD, pref.getString(KEY_MIN_THRESHOLD, null));

        user.put(KEY_MAX_THRESHOLD, pref.getString(KEY_MAX_THRESHOLD, null));

        //user save screen type choice
        //user.put(KEY_SAVESCREENTYPE, pref.getString(KEY_SAVESCREENTYPE, null));
        // return user
        return user;
    }



}
