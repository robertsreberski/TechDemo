package pl.robertmikolaj.techdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.robertmikolaj.techdemo.helper.CameraPreview;
import pl.robertmikolaj.techdemo.helper.GeolocationService;
import pl.robertmikolaj.techdemo.helper.MeasurmentEngine;
import pl.robertmikolaj.techdemo.helper.SoundMsgHandler;
import pl.robertmikolaj.techdemo.helper.googleplaces.GooglePlaces;
import pl.robertmikolaj.techdemo.helper.googleplaces.POJOs.Place;
import pl.robertmikolaj.techdemo.helper.googleplaces.POJOs.PlacesList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Camera camera = null;

    FloatingActionButton fab = null;

    public Context context;

    public TextView mStatusView;

    MeasurmentEngine mEngine;

    public double currentDecibels = 0;

    SoundMsgHandler handler;

    public GeolocationService mService;

    boolean mBound = false;

    GooglePlaces googlePlaces;

    PlacesList placesList;

    public double lng;
    public double lat;
    // ListItems data
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();

    // KEY Strings
    public static String KEY_REFERENCE = "reference"; // id of the place

    public static String KEY_NAME = "name"; // name of the place

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
        if(mService.getCurrentLocation() != null){

        lat = mService.getCurrentLocation().getLatitude();

        lng = mService.getCurrentLocation().getLongitude();

        new LoadPlaces().execute();
        }else{
            Toast.makeText(getApplicationContext() ,R.string.loc_failed, Toast.LENGTH_SHORT).show();
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
            // Jesli sie podepniemy, od razu startuje lokalizacje
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

    class LoadPlaces extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */


        /**
         * getting Places JSON
         */
        protected String doInBackground(String... args) {


            googlePlaces = new GooglePlaces();

            try {
                // tutaj typy, najlepiej tylko stadiums ale dalem reszte zeby zawsze cos sie pojawilo
                String types = "stadium|school";

                // W metrach, promien poszukiwan
                double radius = 500;

               Log.d("ok", lat + "" + lng);

                placesList = googlePlaces.search(lat,
                        lng, radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed Places into LISTVIEW
                     * */
                    // Get json response status
                    String status = placesList.status;
                    switch (status) {
                        case "OK": {
                            // Successfully got places details
                            if (placesList.results != null) {
                                // loop through each place
                                for (Place p : placesList.results) {
                                    HashMap<String, String> map = new HashMap<String, String>();



                                    // Place name
                                    map.put(KEY_NAME, p.name);


                                    // Pozniej bede mmogl sie bawic z ta arraylista
                                    placesListItems.add(map);
                                    Toast.makeText(getApplicationContext(), p.name, Toast.LENGTH_SHORT).show();
                                }

                            }
                            break;
                        }
                        case "ZERO_RESULTS": {
                            Log.d("List Status", "ZERO_RESULTS");
                            break;
                        }
                        case "UNKNOWN_ERROR": {
                            Log.d("List Status", "UNKNOWN_ERROR");
                            break;
                        }
                        case "OVER_QUERY_LIMIT": {
                            Log.d("List Status", "OVER_QUERY_LIMIT");
                            break;
                        }
                        case "REQUEST_DENIED": {
                            Log.d("List Status", "REQUEST_DENIED");
                            break;
                        }
                        case "INVALID_REQUEST": {
                            Log.d("List Status", "INVALID_REQUES");
                            break;
                        }
                        default: {
                            Log.d("List Status", "ERROR_OCCURED");
                            break;
                        }
                    }
                }
            });
        }


    }



}
