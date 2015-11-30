package pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces;


import java.io.Serializable;
import com.google.api.client.util.Key;

import pl.robertmikolaj.techdemo.helper.googleplaces.Interfaces.Geometry;

/**
 * Created by Spajki on 2015-11-29.
 */
public class Place implements Serializable{
    @Key
    public String id;

    @Key
    public String name;

    @Key
    public String address;

    @Key
    public Geometry geometry;

    @Key
    public String reference;

}



