package pl.robertmikolaj.techdemo.helper.googleplaces.POJOs;


import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.util.Key;

/**
 * Created by Spajki on 2015-11-29.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place implements Serializable{


    @Key
    public String name;




}



