package pl.robertmikolaj.techdemo.helper.googleplaces;

import android.util.Log;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import pl.robertmikolaj.techdemo.R;

import pl.robertmikolaj.techdemo.helper.googleplaces.POJOs.PlacesList;
import retrofit.Call;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Spajki on 2015-11-29.
 */


/*@SuppressWarnings("deprecation")*/
public class GooglePlaces {

        // z tym retrofitem będę jeszcze próbował, poki co zostawiam jak jest
   // private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();



    // Google places URL
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";

    private static final String API_KEY = "AIzaSyC0I54Mkd03iUhcftnda5m8P3WZlHAxWJk";
    //retrofit try:
    private static final String PLACES_SEARCH_API_URL ="https://maps.googleapis.com";

     double mLatitude;
     double mLongtitude;
    double mRadius;


    public interface ApiPlaces {
    @GET("/maps/api/place/search/json?")
        Call<PlacesList> getPlaces(@Query("key") String API_KEY,
                                    @Query("location") String location,
                                    @Query("radius") double radius,
                                    @Query("sensor") boolean sensor,
                                    @Query(value = "types", encoded=true) String types);


    }


    public PlacesList search(double latitude, double longtitude, double radius, String types)
            throws Exception {

        this.mLatitude = latitude;
        this.mLongtitude = longtitude;
        this.mRadius = radius;
        String encodedTypes = URLEncoder.encode(types, "UTF-8");
        Retrofit retrofit = new Retrofit.Builder().baseUrl(PLACES_SEARCH_API_URL).addConverterFactory(JacksonConverterFactory.create()).build();

        ApiPlaces places = retrofit.create(ApiPlaces.class);

        Call<PlacesList> call = places.getPlaces(API_KEY, mLatitude + "," + mLongtitude, mRadius, false, encodedTypes);

        PlacesList placesList = call.execute().body();
        Log.d("Places status", "" + placesList.status);

/*
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

        */




        return placesList;



    }

    /*public static HttpRequestFactory createRequestFactory(final HttpTransport transport){
        return transport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                httpRequest.setParser(parser);
            }
        });*/

    }








