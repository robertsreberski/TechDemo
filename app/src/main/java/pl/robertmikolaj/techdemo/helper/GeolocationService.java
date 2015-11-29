package pl.robertmikolaj.techdemo.helper;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Spajki on 2015-11-29.
 */
public class GeolocationService extends Service {
    //binder
    private final IBinder mBinder = new LocalBinder();
    //customlistener, inner class
    CustomLocationListener listener = new CustomLocationListener();
    //currently best location
    Location mCurrentlyUsedLocation;
    //constants, pozniej wrzucic do jakiegos osobnego interface
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final int MIN_TIME_BETWEEN_UPDATES = 1000 * 30;
    //location menager, definiowany @startLocating()
    LocationManager locationManager;

    // flags
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    //lat&long
    double latitude;
    double longtitude;





    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    protected boolean isBetterLocation(Location location, Location currentlyUsedLocation) {
        if (currentlyUsedLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentlyUsedLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentlyUsedLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentlyUsedLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }





    public Location getCurrentLocation(){
        return mCurrentlyUsedLocation;
    }

    public void startLocating() throws SecurityException{
        locationManager =  (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //just checking
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if(!isGPSEnabled && !isNetworkEnabled){

        }else{

            if(isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);


            }
            if(isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
            }
        }
    }

    public void stopLocating() throws SecurityException{
        locationManager.removeUpdates(listener);
    }

    public class LocalBinder extends Binder {
        public GeolocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GeolocationService.this;
        }
    }

    private class CustomLocationListener implements LocationListener{
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if(isBetterLocation(location, mCurrentlyUsedLocation)){
                mCurrentlyUsedLocation = location;
            }

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    }

    // simple getters
    public double getLatitude(){
        if(mCurrentlyUsedLocation != null){
            latitude = mCurrentlyUsedLocation.getLatitude();
        }
        return latitude;
    }

    public double getLongtitude(){
        if(mCurrentlyUsedLocation != null){
            longtitude = mCurrentlyUsedLocation.getLongitude();
        }
        return longtitude;
    }


}
