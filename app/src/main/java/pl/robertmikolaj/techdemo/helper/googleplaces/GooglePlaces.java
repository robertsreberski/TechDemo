package pl.robertmikolaj.techdemo.helper.googleplaces;

import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;

import pl.robertmikolaj.techdemo.R;
import pl.robertmikolaj.techdemo.helper.googleplaces.POJOs.PlacesList;

/**
 * Created by Spajki on 2015-11-29.
 */


/*@SuppressWarnings("deprecation")*/
public class GooglePlaces {

        // z tym retrofitem będę jeszcze próbował, poki co zostawiam jak jest
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();



    // Google places URL
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";


     double mLatitude;
     double mLongtitude;
    double mRadius;

    public PlacesList search(double latitude, double longtitude, double radius, String types)
            throws Exception {

        this.mLatitude = latitude;
        this.mLongtitude = longtitude;
        this.mRadius = radius;

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", R.string.API_KEY);
            request.getUrl().put("location", mLatitude + "," + mLongtitude);
            request.getUrl().put("radius", mRadius); // w metrach
            request.getUrl().put("sensor", false);
            if (types != null) {
                request.getUrl().put("types", types);
            }

            PlacesList list = request.execute().parseAs(PlacesList.class);
            Log.d("Places status", "" + list.status);
            return list;
        } catch (HttpResponseException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

    }

    public static HttpRequestFactory createRequestFactory(final HttpTransport transport){
        return transport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                httpRequest.setParser(parser);
            }
        });

    }



}
