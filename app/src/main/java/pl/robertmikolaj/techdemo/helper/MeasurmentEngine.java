package pl.robertmikolaj.techdemo.helper;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by Spajki on 2015-11-21.
 */
public class MeasurmentEngine extends Thread{
    private static final int FREQUENCY = 44100;

    // nie wiem czy poprawne - deprecated
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final int MY_MSG = 1;
    private static final int MAXOVER_MSG = 2;
    private static final int ERROR_MSG = -1;

    private volatile int BUFFSIZE = 0;
    private static final double P0 = 0.000002;


    private static final int CALIB_INCREMENT = 3;
    private static final int CALIB_DEFAULT = -100;
    private int mCaliberationValue = CALIB_DEFAULT;

    private double mMaxValue = 0.0;
    private volatile boolean mShowMaxValue = false;
    private volatile boolean mIsRunning = false;
    private Handler mHandle = null;
    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
 //   private boolean hasBeenStarted = false;
    AudioRecord mRecordInstance = null;
    Context mContext = null;



public MeasurmentEngine(Handler handle, Context context){
    this.mHandle = handle;
    this.mContext = context;
    this.mMaxValue = 0.0;
    this.mShowMaxValue = false;

    BUFFSIZE = AudioRecord.getMinBufferSize(
            FREQUENCY,
            CHANNEL,
            ENCODING);

    mRecordInstance = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            FREQUENCY, CHANNEL,
            ENCODING, BUFFSIZE*2);
    startEngine();
}


public void startEngine(){

        this.mIsRunning = true;
   //     if(!hasBeenStarted) {
       //     hasBeenStarted = true;
            this.start();
     //   }
}
    public void restartEngine(){
        this.mIsRunning = true;
    }

    public void stopEngine(){
        this.mIsRunning = false;

    }

    public void run(){
        try{
            mRecordInstance.startRecording();


            double splValue = 0.0;
            double rmsValue = 0.0;
            while (this.mIsRunning){
                short[] tempBuffer = new short[BUFFSIZE];

                mRecordInstance.read(tempBuffer, 0, BUFFSIZE);
                //idk what theyre doing right now
                for (int i = 0; i < BUFFSIZE - 1; i++) {
                    rmsValue += tempBuffer[i] * tempBuffer[i];
                }
                rmsValue = rmsValue / BUFFSIZE;
                rmsValue = Math.sqrt(rmsValue);

                splValue = 20 * Math.log10(rmsValue / P0);
                splValue = splValue + mCaliberationValue;
                splValue = round(splValue, 2);

                Message msg = mHandle.obtainMessage(MY_MSG, splValue);
                mHandle.sendMessage(msg);

            }
        }
            catch (Exception e) {
                e.printStackTrace();
                Message msg = mHandle.obtainMessage(ERROR_MSG,
                        e.getLocalizedMessage()+"");
                mHandle.sendMessage(msg);
            }


            if(mRecordInstance != null){
                mRecordInstance.stop();
                mRecordInstance.release();
                mRecordInstance = null;
            }



    }


    public double round(double d, int decimalPlace) {
        // see the Javadoc about why we use a String in the constructor
        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)

       // .getClass().getName();
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();



    }

}
