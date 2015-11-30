package pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * Created by Spajki on 2015-11-29.
 */
public class Geometry implements Serializable {
    @Key
    public Location location;
}

