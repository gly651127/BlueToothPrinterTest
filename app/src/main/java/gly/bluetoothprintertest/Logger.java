package gly.bluetoothprintertest;

import android.util.Log;

public class Logger {

    public static void log(String responseObj) {
        try {
        String[] split = responseObj.split(":",2);
        Log.d(split[0], split[1]);
        }catch (Exception e){
            Log.d("MYLOG", responseObj);
        }
    }
}