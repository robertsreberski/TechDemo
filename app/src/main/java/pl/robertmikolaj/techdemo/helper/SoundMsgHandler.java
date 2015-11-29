package pl.robertmikolaj.techdemo.helper;


import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import pl.robertmikolaj.techdemo.MainActivity;

/**
 * Created by Spajki on 2015-11-27.
 */

public class SoundMsgHandler extends Handler{
    static final int MY_MSG = 1;
    private final WeakReference<MainActivity> mainActivityWeakReference;

    public SoundMsgHandler(MainActivity context) {
        mainActivityWeakReference = new WeakReference<pl.robertmikolaj.techdemo.MainActivity>(context);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity mainActivity = mainActivityWeakReference.get();
        if(mainActivity != null) {
            if (msg.what == MY_MSG) {


                mainActivity.mStatusView.setText((String)msg.obj);
                mainActivity.currentDecibels = (double) msg.obj;
            } else {

                Toast.makeText(
                        mainActivity.context,
                        "Error " + msg.obj, Toast.LENGTH_LONG).show();
                mainActivity.stopMeter();
            }
        }
    }

}


