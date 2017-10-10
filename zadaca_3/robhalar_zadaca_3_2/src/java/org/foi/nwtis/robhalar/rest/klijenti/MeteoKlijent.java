package org.foi.nwtis.robhalar.rest.klijenti;

import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import org.foi.nwtis.robhalar.ws.klijenti.MeteoPodaci;

public class MeteoKlijent
{
    MeteoRestHelper helper;
    Client client;

    public MeteoKlijent() 
    {
        client = ClientBuilder.newClient();
    }

    public List<MeteoPodaci> getRestVazeciMeteoPodaci(List<String> ids) 
    {
        ArrayList<MeteoPodaci> meteoPodaci = new ArrayList<>();
        try
        {
            WebTarget webResourceBase = client.target(MeteoRestHelper.getMETEO_BASE_URI())
                    .path("webresources/meteoREST");

            for(String id : ids)
            {
                WebTarget webResource = webResourceBase.path(id);
                
                String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);    
                
                JsonReader reader = Json.createReader(new StringReader(odgovor));
                JsonObject jo = reader.readObject();
            
                int error = jo.getInt("error");

                if(error == 0)
                {
                    String  temp  = jo.getString("temperatura");
                    String  vlaga = jo.getString("vlaga");
                    String  tlak  = jo.getString("tlak");
                    
                    MeteoPodaci meteoPodatak = new MeteoPodaci();
                    meteoPodatak.setTemperatureValue(Float.parseFloat(temp));
                    meteoPodatak.setTemperatureUnit("K");
                    meteoPodatak.setHumidityValue(Float.parseFloat(vlaga));
                    meteoPodatak.setHumidityUnit("%");
                    meteoPodatak.setPressureValue(Float.parseFloat(tlak));
                    meteoPodatak.setPressureUnit("hPa");
                    
                    meteoPodaci.add(meteoPodatak);
                }
            }
        }
        catch(Exception ex)
        {
            Logger.getLogger(MeteoKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return meteoPodaci;
    }
    
    public boolean dodajUredjaj(String naziv, String adresa)
    {
        boolean added = false;
        if(naziv != null && adresa != null &&
           !naziv.trim().isEmpty() && !adresa.trim().isEmpty())
        {
            try
            {
                WebTarget webResourceBase = client.target(MeteoRestHelper.getMETEO_BASE_URI())
                        .path("webresources/meteoREST");

                JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();
                rootObjectBuilder.add("naziv",  naziv);
                rootObjectBuilder.add("adresa", adresa);
                JsonObject object = rootObjectBuilder.build();

                String odgovor = webResourceBase.request(MediaType.APPLICATION_JSON).post(Entity.json(object), String.class);

                JsonReader reader = Json.createReader(new StringReader(odgovor));
                JsonObject jo = reader.readObject();

                int error = jo.getInt("error");
                if(error == 0)
                    added = true;
            }
            catch(Exception ex)
            {
                Logger.getLogger(MeteoKlijent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return added;
    }
}
