package org.foi.nwtis.robhalar.rest.serveri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.rest.klijenti.OWMKlijent;
import org.foi.nwtis.robhalar.web.podaci.MeteoPodaci;
import org.foi.nwtis.robhalar.ws.ConnectionDistributor;

public class MeteoRestResource
{   
    private String id;
    
    private ServletContext servletContext;

    /**
     * Creates a new instance of MeteoRestResource
     */
    private MeteoRestResource(ServletContext context, String id)
    {
        this.servletContext = context;
        this.id = id;
    }

    /**
     * Get instance of the MeteoRestResource
     */
    public static MeteoRestResource getInstance(ServletContext context, String id)
    {
        // The user may use some kind of persistence mechanism
        // to store and restore instances of MeteoRestResource class.
        return new MeteoRestResource(context, id);
    }

    /**
     * Retrieves representation of an instance of org.foi.nwtis.robhalar.rest.serveri.MeteoRestResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String getJson()
    {
        Konfiguracija konfiguracija 
                = (Konfiguracija) servletContext.getAttribute("Konfiguracija");
        JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();
        try
        {
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();

            int uid = Integer.parseInt(id);
            PreparedStatement preparedStatement = 
                    connectionDistributor.prepareStatement("SELECT id, latitude, longitude FROM uredaji "
                                                         + "WHERE id = ?");
            preparedStatement.setInt(1, uid);
            
            ResultSet resultSet = connectionDistributor.query(preparedStatement);
            //todo Check if this will work
            if (resultSet != null)
            {
                if(resultSet.first())
                {
                    String latitude  = String.valueOf(resultSet.getFloat("latitude"));
                    String longitude = String.valueOf(resultSet.getFloat("longitude"));
                    
                    String apiKey = konfiguracija.dajPostavku("apikey");
                    if(apiKey != null)
                    {
                        OWMKlijent owmKlijent   = new OWMKlijent(apiKey);
                        MeteoPodaci meteoPodaci = owmKlijent.getRealTimeWeather(latitude, longitude);
                        if(meteoPodaci != null)
                        {
                            String temp  = meteoPodaci.getTemperatureValue().toString();
                            String vlaga = meteoPodaci.getHumidityValue().toString();
                            String tlak  = meteoPodaci.getPressureValue().toString();

                            rootObjectBuilder.add("id", uid);
                            rootObjectBuilder.add("temperatura", temp);
                            rootObjectBuilder.add("vlaga", vlaga);
                            rootObjectBuilder.add("tlak", tlak);
                            
                            rootObjectBuilder.add("error", 0);
                            
                            return rootObjectBuilder.build().toString();
                        }
                    }
                }
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(MeteoRestResource.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(MeteoRestResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        rootObjectBuilder.add("error", 1);
        rootObjectBuilder.add("errorMsg", "Internal error");
        
        return rootObjectBuilder.build().toString();
    }

    /**
     * PUT method for updating or creating an instance of MeteoRestResource
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putJson(String content)
    {
    }

    /**
     * DELETE method for resource MeteoRestResource
     */
    @DELETE
    public void delete()
    {
    }
}
