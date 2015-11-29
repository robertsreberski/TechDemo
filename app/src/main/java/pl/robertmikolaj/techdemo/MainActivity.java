package pl.robertmikolaj.techdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pl.robertmikolaj.techdemo.helper.CameraPreview;
import pl.robertmikolaj.techdemo.helper.GeolocationService;
import pl.robertmikolaj.techdemo.helper.MeasurmentEngine;
import pl.robertmikolaj.techdemo.helper.SoundMsgHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Camera camera = null;
    static final int MY_MSG = 1;

    FloatingActionButton fab = null;
    public Context context;
    public TextView mStatusView;
    MeasurmentEngine mEngine;
    public double currentDecibels = 0;
    SoundMsgHandler handler;
    GeolocationService mService;
    boolean mBound = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        openCamera();
        CameraPreview cameraPreview = new CameraPreview(getApplicationContext(), camera);
        FrameLayout previewContainer = (FrameLayout) findViewById(R.id.camera_container);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mStatusView = (TextView) findViewById(R.id.status_view);
        handler = new SoundMsgHandler(this);
        previewContainer.addView(cameraPreview);
        fab.setOnClickListener(this);
        startMeter();

    }

    @Override
    public void onStart(){
        super.onStart();

// PAMIETAC zeby sprawdzac zawsze if(mbound)!!!!!!
        Intent intent = new Intent(this, GeolocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if(mBound) {
            mService.startLocating();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restartMeter();
        try {
            camera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {

        this.finish();
        stopMeter();
        camera.lock();
        super.onPause();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    @Override
    public void onStop() {
        camera.release();
        this.finish();
        stopMeter();
        super.onStop();
        if(mBound) {
            mService.stopLocating();
        }
    }

    @Override
    public void onDestroy(){
        this.finish();
        stopMeter();
        camera.release();
        super.onDestroy();
    }
    @Override
    public void onRestart(){
        super.onRestart();
        restartMeter();
        try {
            camera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startMeter() {
        mEngine = new MeasurmentEngine(handler, context);
    }
    public void stopMeter() {
        mEngine.stopEngine();
    }
    public void restartMeter(){
        mEngine.restartEngine();
    }

    private void openCamera(){
        camera = Camera.open();
        Camera.Parameters cameraParams = camera.getParameters();
        List<Camera.Size> availableSizes = cameraParams.getSupportedPictureSizes();
        Camera.Size chosenSize = availableSizes.get(0);
        cameraParams.setPictureSize(chosenSize.width,chosenSize.height);
        cameraParams.setJpegQuality(100);
        cameraParams.setPictureFormat(ImageFormat.JPEG);
        camera.setParameters(cameraParams);
    }
    @Override
    public void onClick(View v) {
        final Double pTakenDecibels = currentDecibels;
        if(mBound){
            Toast.makeText(getApplicationContext(), Double.toString(mService.getCurrentLocation().getLatitude()), Toast.LENGTH_SHORT).show();
        }
       /* camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bitmap;
                Canvas canvas;
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inMutable = true;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.e("BITMAP_SIZE", Integer.toString(bitmap.getHeight()) + Integer.toString(bitmap.getWidth()));
                Matrix pMatrixPhoto = new Matrix();
                pMatrixPhoto.preRotate(90);
                Bitmap rotatedPhoto = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), pMatrixPhoto, true);
                Bitmap filter = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.gdansk_filter);
                Bitmap bmOverlay = Bitmap.createBitmap(rotatedPhoto.getWidth(), rotatedPhoto.getHeight(), rotatedPhoto.getConfig());
                canvas = new Canvas(bmOverlay);
                Bitmap scaledFilter = Bitmap.createScaledBitmap(filter, rotatedPhoto.getWidth(), filter.getHeight() + ((rotatedPhoto.getWidth() - filter.getWidth())/2), true);
                canvas.drawBitmap(rotatedPhoto, new Matrix(), null);
                canvas.drawBitmap(scaledFilter, 0, bmOverlay.getHeight() - scaledFilter.getHeight(), null);
                Paint textPaint = new Paint();

                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(200);
                canvas.drawText(Double.toString(pTakenDecibels)+"dB", 10, 190, textPaint);
//                ((ImageView) findViewById(R.id.test_result)).setImageBitmap(bmOverlay);
                MediaStore.Images.Media.insertImage(getContentResolver(), bmOverlay, "Test1", "Test");
                Toast.makeText(getApplicationContext(), "Photo saved", Toast.LENGTH_SHORT).show();
                camera.startPreview();
            }
        });*/
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GeolocationService.LocalBinder binder = (GeolocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startLocating();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
