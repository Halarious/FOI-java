package org.foi.nwtis.robhalar.web;

import static java.lang.Thread.sleep;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.robhalar.rest.klijenti.OWMKlijent;
import org.foi.nwtis.robhalar.web.podaci.MeteoPodaci;

public class PreuzimanjeMeteoPodataka extends Thread
{
    private final ServletContext   context;
    private final Konfiguracija    konfiguracija;
    private final BP_Konfiguracija bp_konfiguracija;
    private boolean running;
    
    public PreuzimanjeMeteoPodataka(ServletContext context)
    {
        this.context          = context;
        this.bp_konfiguracija = (BP_Konfiguracija)context.getAttribute("BP_Konfiguracija");
        this.konfiguracija    = (Konfiguracija) context.getAttribute("Konfiguracija");
        this.running          = true;
    }
    
    @Override
    public void interrupt()
    {
        running = false;
        super.interrupt();
    }

    @Override
    public void run()
    {
        int trajanjeCiklusaObrade = Integer.parseInt(konfiguracija.dajPostavku("intervalDretveZaMeteoPodatke"));

        while(running)
        {
            long pocetakObradeCiklusa = System.currentTimeMillis();
            
            try
            {
                Class.forName(bp_konfiguracija.getDriverDatabase());
                Connection connection = DriverManager.getConnection(
                                            bp_konfiguracija.getServerDatabase() + 
                                                    bp_konfiguracija.getUserDatabase(),
                                            bp_konfiguracija.getUserUsername(),
                                            bp_konfiguracija.getUserPassword());
                String query = "SELECT id, latitude, longitude FROM uredaji";
                
                PreparedStatement queryStatement = connection.prepareStatement(query);

                String apiKey = konfiguracija.dajPostavku("apikey");
                OWMKlijent owmKlijent = new OWMKlijent(apiKey);
                
                ResultSet resultSet = queryStatement.executeQuery(query);
                while(resultSet.next())
                {
                    int    id        = resultSet.getInt("id");
                    float  latitude  = resultSet.getFloat("latitude");
                    float  longitude = resultSet.getFloat("longitude");

                    MeteoPodaci meteoPodaci = owmKlijent.getRealTimeWeather(Float.toString(latitude), 
                                                                            Float.toString(longitude));
                    if(meteoPodaci != null)
                    {
                        float  temp          = meteoPodaci.getTemperatureValue();
                        float  tempMin       = meteoPodaci.getTemperatureMin(); 
                        float  tempMax       = meteoPodaci.getTemperatureMax();
                        float  vlaga         = meteoPodaci.getHumidityValue();
                        float  tlak          = meteoPodaci.getPressureValue();
                        float  vjetar        = meteoPodaci.getWindSpeedValue();
                        float  vjetarSmjer   = meteoPodaci.getWindDirectionValue();
                        String adresaStanice = "robhalar";
                        String vrijeme       = String.valueOf(meteoPodaci.getWeatherNumber());
                        String vrijemeOpis   = meteoPodaci.getWeatherValue();

                        PreparedStatement preparedStatement = connection.
                                prepareStatement("INSERT INTO meteo (id, adresaStanice, latitude, longitude,"
                                                                  + "vrijeme, vrijemeOpis, temp, tempMin, tempMax,"
                                                                  + "vlaga, tlak, vjetar, vjetarSmjer) "
                                               + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                        preparedStatement.setInt   (1,  id);
                        preparedStatement.setString(2,  adresaStanice);
                        preparedStatement.setFloat (3,  latitude);
                        preparedStatement.setFloat (4,  longitude);
                        preparedStatement.setString(5,  vrijeme);
                        preparedStatement.setString(6,  vrijemeOpis);
                        preparedStatement.setFloat (7,  temp);
                        preparedStatement.setFloat (8,  tempMin);
                        preparedStatement.setFloat (9,  tempMax);
                        preparedStatement.setFloat (10, vlaga);
                        preparedStatement.setFloat (11, tlak);
                        preparedStatement.setFloat (12, vjetar);
                        preparedStatement.setFloat (13, vjetarSmjer);
                        preparedStatement.execute();
                    }
                }
            } catch (ClassNotFoundException | SQLException ex)
            {
                Logger.getLogger(PreuzimanjeMeteoPodataka.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try
            {
                long trajanjeObrade = System.currentTimeMillis() - pocetakObradeCiklusa;
                sleep( (trajanjeCiklusaObrade * 1000) - 
                        (trajanjeObrade % (trajanjeCiklusaObrade * 1000)) );
            } 
            catch (InterruptedException ex)
            {
                Logger.getLogger(PreuzimanjeMeteoPodataka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start()
    {
        super.start();
    }
    
}
