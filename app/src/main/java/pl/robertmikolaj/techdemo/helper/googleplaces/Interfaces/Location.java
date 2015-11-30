package pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * Created by Spajki on 2015-11-29.
 */
public class Location  implements Serializable {
    @Key
    public double lat;
    @Key
    public double lng;
}