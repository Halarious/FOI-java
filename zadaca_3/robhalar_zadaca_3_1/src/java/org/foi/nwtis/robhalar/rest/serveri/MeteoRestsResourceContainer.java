package org.foi.nwtis.robhalar.rest.serveri;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.robhalar.rest.klijenti.GMKlijent;
import org.foi.nwtis.robhalar.web.podaci.Lokacija;
import org.foi.nwtis.robhalar.ws.ConnectionDistributor;
import org.foi.nwtis.robhalar.ws.serveri.GeoMeteoWS;

@Path("/meteoREST")
public class MeteoRestsResourceContainer
{
    @Context
    private ServletContext servletContext;
    
    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MeteoRestsResourceContainer
     */
    public MeteoRestsResourceContainer()
    {
    }

    /**
     * Retrieves representation of an instance of org.foi.nwtis.robhalar.rest.serveri.MeteoRestsResourceContainer
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8") 
    public String getJson()
    {
        JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();
        try
        {
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();

            String query = "SELECT id, naziv, latitude, longitude FROM uredaji";
            ResultSet resultSet = connectionDistributor.simpleQuery(query);

            //todo Check if this will work
            if (resultSet != null)
            {
                JsonArrayBuilder uredajiArrayBuilder = Json.createArrayBuilder();
                while (resultSet.next())
                {
                    JsonObjectBuilder uredajObjectBuilder = Json.createObjectBuilder();

                    int id = resultSet.getInt("id");
                    String naziv = resultSet.getString("naziv");
                    float latitude = resultSet.getFloat("latitude");
                    float longitude = resultSet.getFloat("longitude");

                    //Lokacija lokacija = new Lokacija(Float.toString(latitude),
                    //                                 Float.toString(longitude));
                    uredajObjectBuilder.add("id", id);
                    uredajObjectBuilder.add("naziv", naziv);
                    uredajObjectBuilder.add("latitude", latitude);
                    uredajObjectBuilder.add("longitude", longitude);

                    uredajiArrayBuilder.add(uredajObjectBuilder);
                }
                rootObjectBuilder.add("uredaji", uredajiArrayBuilder);
                rootObjectBuilder.add("error", 0);
            } 
            else
            {
                rootObjectBuilder.add("error", 1);
                rootObjectBuilder.add("errorMsg", "Internal error");
            }
            
            return rootObjectBuilder.build().toString();
            
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return rootObjectBuilder.build().toString();
    }

    /**
     * POST method for creating an instance of MeteoRestResource
     * @param content representation for the new resource
     * @return an HTTP response with content of the created resource
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String postJson(String content)
    {
        JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();
        try   
        {
            JsonReader reader = Json.createReader(new StringReader(content));
            JsonObject jo = reader.readObject();

            String naziv  = jo.getString("naziv");
            String adresa = jo.getString("adresa");

            //todo Is this what we are supposed to do at all?
            GMKlijent gmKlijent = new GMKlijent();
            Lokacija lokacija = gmKlijent.getGeoLocation(adresa);
            if(lokacija != null)
            {
                ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
                connectionDistributor.connect();

                String query = "SELECT id, naziv FROM uredaji ORDER BY id DESC";

                ResultSet resultSet= connectionDistributor.simpleQuery(query);

                int id;
                if(resultSet.first())
                    id = resultSet.getInt("id") + 1;
                else
                    id = 1;

                PreparedStatement preparedStatement = connectionDistributor.
                        prepareStatement("INSERT INTO uredaji (id, naziv, latitude, longitude) "
                                       + "VALUES (?, ?, ?, ?)");
                if(preparedStatement != null)
                {
                    preparedStatement.setInt   (1, id);
                    preparedStatement.setString(2, naziv);

                    float latitude = Float.parseFloat(lokacija.getLatitude());
                    float longitude = Float.parseFloat(lokacija.getLongitude());

                    preparedStatement.setFloat(3, latitude);
                    preparedStatement.setFloat(4, longitude);

                    if(connectionDistributor.runStatement(preparedStatement))
                    {
                        rootObjectBuilder.add("error", 0);
                        return rootObjectBuilder.build().toString();
                    }
                }
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(Exception ex)
        {
            Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        rootObjectBuilder.add("error", 1);
        return rootObjectBuilder.build().toString();
    }

    /**
     * Sub-resource locator method for {id}
     * @param id
     * @return 
     */
    @Path("{id}")
    public MeteoRestResource getMeteoRestResource(@PathParam("id") String id)
    {
        return MeteoRestResource.getInstance(servletContext, id);
    }
}
