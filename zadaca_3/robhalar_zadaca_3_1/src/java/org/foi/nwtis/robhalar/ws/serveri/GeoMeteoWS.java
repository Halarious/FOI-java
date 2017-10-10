package org.foi.nwtis.robhalar.ws.serveri;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.robhalar.rest.klijenti.GMKlijent;
import org.foi.nwtis.robhalar.web.podaci.Lokacija;
import org.foi.nwtis.robhalar.web.podaci.MeteoPodaci;
import org.foi.nwtis.robhalar.web.podaci.Uredjaj;
import org.foi.nwtis.robhalar.ws.ConnectionDistributor;

@WebService(serviceName = "GeoMeteoWS")
public class GeoMeteoWS
{
    @Resource
    private WebServiceContext context;
    
    /**
     * Web service operation
     * @return 
     */
    @WebMethod(operationName = "dajSveUredjaje")
    public List<Uredjaj> dajSveUredjaje()
    {
        try
        {
            ServletContext servletContext
                    = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();

            String query = "SELECT id, naziv, latitude, longitude FROM uredaji";
            ResultSet resultSet = connectionDistributor.simpleQuery(query);

            JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();

            //todo maybe not good
            if (resultSet != null)
            {
                ArrayList<Uredjaj> uredjaji = new ArrayList<>();
                while (resultSet.next())
                {
                    int id = resultSet.getInt("id");
                    String naziv = resultSet.getString("naziv");
                    float latitude = resultSet.getFloat("latitude");
                    float longitude = resultSet.getFloat("longitude");

                    Lokacija lokacija = new Lokacija(Float.toString(latitude),
                                                     Float.toString(longitude));
                    
                    Uredjaj uredaj = new Uredjaj(id, naziv, lokacija);
                    uredjaji.add(uredaj);
                }
                
                return uredjaji;
            }            
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
        return null;
    }
    
    /**
     * Web service operation
     * @return 
     */
    @WebMethod(operationName = "dajSveUredjajeJson")
    public String dajSveUredjajeJson()
    {
        try
        {
            ServletContext servletContext
                    = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();

            String query = "SELECT id, naziv, latitude, longitude FROM uredaji";
            ResultSet resultSet = connectionDistributor.simpleQuery(query);

            JsonObjectBuilder rootObjectBuilder = Json.createObjectBuilder();

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
        return null;
    }
    
    /**
     * Web service operation
     * @param uredjaj
     * @return 
     */
    @WebMethod(operationName = "dodajUredjaj")
    public Boolean dodajUredjaj(@WebParam(name = "uredjaj") Uredjaj uredjaj)
    {
        if(uredjaj != null)
        {
            try
            {
                ServletContext servletContext =
                        (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
                ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
                connectionDistributor.connect();

                PreparedStatement preparedStatement = connectionDistributor.
                        prepareStatement("INSERT INTO uredaji (id, naziv, latitude, longitude) "
                                       + "VALUES (?, ?, ?, ?)");
                if(preparedStatement != null)
                {
                    preparedStatement.setInt   (1, uredjaj.getId());
                    preparedStatement.setString(2, uredjaj.getNaziv());

                    Lokacija lokacija = uredjaj.getGeoloc();
                    float latitude = Float.parseFloat(lokacija.getLatitude());
                    float longitude = Float.parseFloat(lokacija.getLongitude());

                    preparedStatement.setFloat(3, latitude);
                    preparedStatement.setFloat(4, longitude);

                    return connectionDistributor.runStatement(preparedStatement);
                }
            } 
            catch (SQLException ex)
            {
                Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (ClassNotFoundException ex)
            {
                Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(Exception ex)
            {
                Logger.getLogger(GeoMeteoWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return false;
    }
    
    /**
     * Web service operation
     * @param naziv
     * @param adresa
     * @return 
     */
    @WebMethod(operationName = "dodajUredjajPremaAdresi")
    public Boolean dodajUredjajPremaAdresi(@WebParam(name = "naziv") String naziv, @WebParam(name = "adresa") String adresa)
    {
        try
        {
            GMKlijent gmKlijent = new GMKlijent();
            Lokacija lokacija = gmKlijent.getGeoLocation(adresa);
            if(lokacija != null || naziv.trim().isEmpty())
            {
            
                ServletContext servletContext =
                        (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
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

                    return connectionDistributor.runStatement(preparedStatement);
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
        
        return false;
    }
    
    /**
     * Web service operation
     * @param id
     * @param from
     * @param to
     * @return 
     */
    @WebMethod(operationName = "dajSveMeteoPodatkeZaUredjaj")
    public List<MeteoPodaci> dajSveMeteoPodatkeZaUredjaj(@WebParam(name = "id") int id, @WebParam(name = "from") long from, @WebParam(name = "to") long to)
    {
        try
        {
            ServletContext servletContext =
                    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();
        
            PreparedStatement preparedStatement 
                    = connectionDistributor.prepareStatement("SELECT * FROM meteo WHERE id = ? "
                                                           + "AND preuzeto > ? "
                                                           + "AND preuzeto < ? ");
            if(preparedStatement != null)
            {
                List<MeteoPodaci> meteoPodaci = new ArrayList<>();

                Date fromDate = new Date(from);
                Date toDate   = new Date(to);
            
                preparedStatement.setInt(1, id);
                preparedStatement.setDate(2, fromDate);
                preparedStatement.setDate(3, toDate);
                ResultSet resultSet = preparedStatement.executeQuery();
                while(resultSet.next())
                {
                    String adresaStanice = resultSet.getString("adresaStanice");
                    float latitude     = resultSet.getFloat ("latitude");
                    float longitude    = resultSet.getFloat ("longitude");
                    String vrijeme     = resultSet.getString("vrijeme");
                    String vrijemeOpis = resultSet.getString("vrijemeOpis");
                    float temp         = resultSet.getFloat ("temp");
                    float tempMin      = resultSet.getFloat ("tempMin");
                    float tempMax      = resultSet.getFloat ("tempMax");
                    float vlaga        = resultSet.getFloat ("vlaga");
                    float tlak         = resultSet.getFloat ("tlak");
                    float vjetar       = resultSet.getFloat ("vjetar");
                    float vjetarSmjer  = resultSet.getFloat ("vjetarSmjer");
                    Date preuzeto      = resultSet.getDate  ("preuzeto");

                    MeteoPodaci meteoPodatak 
                            = new MeteoPodaci(new java.util.Date(), new java.util.Date(),
                                              temp, tempMin, tempMax, "Kelvin", 
                                              vlaga, "%", tlak, "hPa", 
                                              vjetar, "mph", vjetarSmjer, 
                                              "<wind-dierction-code", "<wind-direction-name>", 
                                              0, "", "", 0.0f, "", "mm", 
                                              0, vrijeme, "", preuzeto);

                    meteoPodaci.add(meteoPodatak);
                }
                return meteoPodaci;
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
        
        return null;
    }

    /**
     * Web service operation
     * @param id
     * @return 
     */
    @WebMethod(operationName = "dajZadnjeMeteoPodatkeZaUredjaj")
    public MeteoPodaci dajZadnjeMeteoPodatkeZaUredjaj(@WebParam(name = "id") int id)
    {
        try
        {
            ServletContext servletContext =
                    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();
           
            PreparedStatement preparedStatement = connectionDistributor.
                    prepareStatement("SELECT * FROM meteo WHERE id = ? "
                                   + "ORDER BY preuzeto DESC");
            if(preparedStatement != null)
            {
                preparedStatement.setInt(1, id);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.first())
                {
                    String adresaStanice = resultSet.getString("adresaStanice");
                    float latitude     = resultSet.getFloat ("latitude");
                    float longitude    = resultSet.getFloat ("longitude");
                    String vrijeme     = resultSet.getString("vrijeme");
                    String vrijemeOpis = resultSet.getString("vrijemeOpis");
                    float temp         = resultSet.getFloat ("temp");
                    float tempMin      = resultSet.getFloat ("tempMin");
                    float tempMax      = resultSet.getFloat ("tempMax");
                    float vlaga        = resultSet.getFloat ("vlaga");
                    float tlak         = resultSet.getFloat ("tlak");
                    float vjetar       = resultSet.getFloat ("vjetar");
                    float vjetarSmjer  = resultSet.getFloat ("vjetarSmjer");
                    Date preuzeto      = resultSet.getDate  ("preuzeto");

                    MeteoPodaci meteoPodatak 
                            = new MeteoPodaci(new java.util.Date(), new java.util.Date(),
                                              temp, tempMin, tempMax, "Kelvin", 
                                              vlaga, "%", tlak, "hPa", 
                                              vjetar, "mph", vjetarSmjer, 
                                              "<wind-dierction-code", "<wind-direction-name>", 
                                              0, "", "", 0.0f, "", "mm", 
                                              0, vrijeme, "", preuzeto);

                    return meteoPodatak;
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
        //todo In general, do we rather return null or an empty object?
        return null;
    }

    /**
     * Web service operation
     * @param id
     * @param from
     * @param to
     * @return 
     */
    @WebMethod(operationName = "dajMinMaxTempZaUredjaj")
    public float[] dajMinMaxTempZaUredjaj(@WebParam(name = "id") int id, @WebParam(name = "from") long from, @WebParam(name = "to") long to)
    {
        try
        {
            ServletContext servletContext =
                    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();
           
            PreparedStatement preparedStatement = connectionDistributor.
                    prepareStatement("SELECT * FROM meteo WHERE id = ? "
                                   + "AND preuzeto > ? "
                                   + "AND preuzeto < ? ");
            if(preparedStatement != null)
            {
                Date fromDate = new Date(from);
                Date toDate   = new Date(to);
            
                preparedStatement.setInt (1, id);
                preparedStatement.setDate(2, fromDate);
                preparedStatement.setDate(3, toDate);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.first())
                {
                    float allTimeMin = resultSet.getFloat("tempMin");
                    float allTimeMax = resultSet.getFloat("tempMax");
                    while(resultSet.next())
                    {
                        float tempMin = resultSet.getFloat ("tempMin");
                        if(tempMin < allTimeMin)
                            allTimeMin = tempMin;
                        
                        float tempMax = resultSet.getFloat ("tempMax");
                        if(tempMax > allTimeMax)
                            allTimeMax = tempMax;
                    }
                    
                    float[] minMaxTemps = {allTimeMin, allTimeMax};
                    return minMaxTemps;
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
        //todo In general, do we rather return null or an empty object?
        return null;
    }

    /**
     * Web service operation
     * @param id
     * @param from
     * @param to
     * @return 
     */
    @WebMethod(operationName = "dajMinMaxVlagaZaUredjaj")
    public float[] dajMinMaxVlagaZaUredjaj(@WebParam(name = "id") int id, @WebParam(name = "from") long from, @WebParam(name = "to") long to)
    {
        try
        {
            ServletContext servletContext =
                    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();
           
            PreparedStatement preparedStatement = connectionDistributor.
                    prepareStatement("SELECT * FROM meteo WHERE id = ? "
                                   + "AND preuzeto > ? "
                                   + "AND preuzeto < ? ");
            if(preparedStatement != null)
            {
                Date fromDate = new Date(from);
                Date toDate   = new Date(to);
            
                preparedStatement.setInt (1, id);
                preparedStatement.setDate(2, fromDate);
                preparedStatement.setDate(3, toDate);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.first())
                {
                    float allTimeMin = resultSet.getFloat("vlaga");
                    float allTimeMax = resultSet.getFloat("vlaga");
                    while(resultSet.next())
                    {
                        float humidityMin = resultSet.getFloat ("vlaga");
                        if(humidityMin < allTimeMin)
                            allTimeMin = humidityMin;
                        
                        float humidityMax = resultSet.getFloat ("vlaga");
                        if(humidityMax > allTimeMax)
                            allTimeMax = humidityMax;
                    }
                    
                    float[] minMaxHumidity = {allTimeMin, allTimeMax};
                    return minMaxHumidity;
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
        return null;
    }

    /**
     * Web service operation
     * @param id
     * @param from
     * @param to
     * @return 
     */
    @WebMethod(operationName = "dajMinMaxTlakZaUredjaj")
    public float[] dajMinMaxTlakZaUredjaj(@WebParam(name = "id") int id, @WebParam(name = "from") long from, @WebParam(name = "to") long to)
    {
        try
        {
            ServletContext servletContext =
                    (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            ConnectionDistributor connectionDistributor = new ConnectionDistributor(servletContext);
            connectionDistributor.connect();
           
            PreparedStatement preparedStatement = connectionDistributor.
                    prepareStatement("SELECT * FROM meteo WHERE id = ? "
                                   + "AND preuzeto > ? "
                                   + "AND preuzeto < ? ");
            if(preparedStatement != null)
            {
                Date fromDate = new Date(from);
                Date toDate   = new Date(to);
                
                preparedStatement.setInt (1, id);
                preparedStatement.setDate(2, fromDate);
                preparedStatement.setDate(3, toDate);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.first())
                {
                    float allTimeMin = resultSet.getFloat("tlak");
                    float allTimeMax = resultSet.getFloat("tlak");
                    while(resultSet.next())
                    {
                        float pressureMin = resultSet.getFloat ("tlak");
                        if(pressureMin < allTimeMin)
                            allTimeMin = pressureMin;
                        
                        float pressureMax = resultSet.getFloat ("tlak");
                        if(pressureMax > allTimeMax)
                            allTimeMax = pressureMax;
                    }
                    
                    float[] minMaxPressure = {allTimeMin, allTimeMax};
                    return minMaxPressure;
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
        return null;
    }    
}
