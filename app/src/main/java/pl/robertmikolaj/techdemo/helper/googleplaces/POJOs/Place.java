package pl.robertmikolaj.techdemo.helper.googleplaces.POJOs;


import java.io.Serializable;
import com.google.api.client.util.Key;

/**
 * Created by Spajki on 2015-11-29.
 */
public class Place implements Serializable{
    @Key
    public String id;

    @Key
    public String name;


}



