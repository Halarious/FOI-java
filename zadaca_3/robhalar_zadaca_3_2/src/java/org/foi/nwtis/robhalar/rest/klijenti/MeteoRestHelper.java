package org.foi.nwtis.robhalar.rest.klijenti;

public class MeteoRestHelper
{
    private static final String Meteo_BASE_URI = "http://localhost:8080/robhalar_zadaca_3_1/";    

    public MeteoRestHelper() 
    {
    }

    public static String getMETEO_BASE_URI() {
        return Meteo_BASE_URI;
    }   
}
