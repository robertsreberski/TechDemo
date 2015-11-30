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

import pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces.PlaceDetails;
import pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces.PlacesList;

/**
 * Created by Spajki on 2015-11-29.
 */


/*@SuppressWarnings("deprecation")*/
public class GooglePlaces {


    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final String API_KEY = "AIzaSyC0I54Mkd03iUhcftnda5m8P3WZlHAxWJk";

    // jakies urlsy o ktorych nei mam pojecia
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    private double mLatitude;
    private double mLongtitude;
    private double mRadius;

    public PlacesList search(double latitude, double longtitude, double radius, String types)
            throws Exception {

        this.mLatitude = latitude;
        this.mLongtitude = longtitude;
        this.mRadius = radius;

        try {

            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
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

    public PlaceDetails getPlaceDetails(String reference) throws Exception {
        try{
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory
                    .buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");



            return request.execute().parseAs(PlaceDetails.class);
        }catch(HttpResponseException e){
            Log.e("Error in Details: ", e.getMessage());
            throw e;
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
