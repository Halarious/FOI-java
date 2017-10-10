package org.foi.nwtis.robhalar.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.robhalar.rest.klijenti.GMKlijent;
import org.foi.nwtis.robhalar.rest.klijenti.OWMKlijent;
import org.foi.nwtis.robhalar.web.podaci.Lokacija;
import org.foi.nwtis.robhalar.web.podaci.MeteoPodaci;

@WebServlet(name = "DodajUredjaj", urlPatterns =
{
    "/DodajUredjaj"
})
public class DodajUredjaj extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        
        RequestDispatcher dispatcher = this.getServletContext()
                                        .getRequestDispatcher("/index.jsp");
        dispatcher.forward(request, response);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String izbor = request.getParameter("odabir");
        switch(izbor)
        {
            case "Geo lokacija":
            {   
                String message = geoLokacija(request);
                if(message.startsWith("Success;"))
                {
                    message = message.split(";")[1];
                    request.setAttribute("successMessage", message);
                }
                else
                    request.setAttribute("errorMessage", message);

                String naziv = request.getParameter("naziv");
                if(naziv != null && !naziv.trim().isEmpty())
                    request.setAttribute("naziv", naziv);
            }break;
            
            case "Spremi":
            {   
                String message = spremi(request);
                if(message.startsWith("Success;"))
                {
                    message = message.split(";")[1];
                    request.setAttribute("successMessage", message);
                }
                else
                    request.setAttribute("errorMessage", message);
                
            } break;
            
            case "Meteo podaci":
            {
                String message = meteoPodaci(request);
                if(message.startsWith("Success;"))
                {
                    request.setAttribute("meteo", true);

                    message = message.split(";")[1];
                    request.setAttribute("successMessage", message);
                }
                else
                    request.setAttribute("errorMessage", message);
                
                String naziv = request.getParameter("naziv");
                if(naziv != null && !naziv.trim().isEmpty())
                    request.setAttribute("naziv", naziv);
                
                String adresa = request.getParameter("adresa");
                if(adresa != null && !adresa.trim().isEmpty())
                    request.setAttribute("adresa", adresa);
            } break;
            
            default:
                break;
        }
        
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

    private String geoLokacija(HttpServletRequest request)
    {
        String adresa = request.getParameter("adresa");
        if(adresa != null && !adresa.trim().isEmpty())
        {
            request.setAttribute("adresa", adresa);
            
            GMKlijent gmKlijent = new GMKlijent();
            Lokacija lokacija = gmKlijent.getGeoLocation(adresa);
            if(lokacija != null)
            {
                String lokacijaUredaja = lokacija.getLatitude() + 
                                         "; " + lokacija.getLongitude();

                request.setAttribute("lokacija", lokacijaUredaja);
                
                return "Success; Geolokacijski podaci uspješno dohvaćeni";
            }
            return "Lokacija se nije mogla dohvatiti";
        }
        return "Pogreška kod unešene adrese!";
    }

    private String spremi(HttpServletRequest request)
    {
        BP_Konfiguracija bp_konfiguracija  
                            = (BP_Konfiguracija)getServletContext().getAttribute("BP_Konfiguracija");
        
        String naziv = request.getParameter("naziv");
        if(naziv != null && !naziv.trim().isEmpty())
        {
            request.setAttribute("naziv", naziv);
            try
            {
                String lokacija = request.getParameter("lokacija");
                if(lokacija != null && !lokacija.trim().isEmpty())
                {
                    request.setAttribute("lokacija", lokacija);
                    String[] coords = lokacija.split(";");
                    if(coords.length != 2)
                        return "Pogreška kod formata koordinata!";

                    String lat = coords[0].trim();
                    Double.parseDouble(lat);
                    String lon = coords[1].trim();
                    Double.parseDouble(lon);
                    Class.forName(bp_konfiguracija.getDriverDatabase());
                    Connection connection = DriverManager.getConnection(
                                                bp_konfiguracija.getServerDatabase() + 
                                                        bp_konfiguracija.getUserDatabase(),
                                                bp_konfiguracija.getUserUsername(),
                                                bp_konfiguracija.getUserPassword());
                    String query = "SELECT id, naziv FROM uredaji ORDER BY id DESC";

                    PreparedStatement queryStatement = connection.prepareStatement(query);
                    ResultSet resultSet = queryStatement.executeQuery(query);

                    int id;
                    if(resultSet.first())
                        id = resultSet.getInt("id") + 1;
                    else
                        id = 1;

                    PreparedStatement preparedStatement = connection.
                            prepareStatement("INSERT INTO uredaji (id, naziv, latitude, longitude) VALUES (?, ?, ?, ?)");
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, naziv);
                    preparedStatement.setFloat(3, Float.parseFloat(coords[0]));
                    preparedStatement.setFloat(4, Float.parseFloat(coords[1]));
                    preparedStatement.execute();
                    
                    return "Success; Uredaj uspjesno dodan u bazu podataka";
                }
                return "Obavezno je prvo dohvatiti koordinate uredaja";
            } 
            catch (ClassNotFoundException | SQLException ex)
            {
                Logger.getLogger(DodajUredjaj.class.getName()).log(Level.SEVERE, null, ex);
                return "Problem u radu s bazom podataka";
            }
            catch(NumberFormatException ex)
            {
                return "Koordinate u neispravnom formatu";
            }
        }
        return "Naziv uredaja je obavezan";
    }

    private String meteoPodaci(HttpServletRequest request)
    {
        Konfiguracija konfiguracija 
                = (Konfiguracija) getServletContext().getAttribute("Konfiguracija");
        
        String lokacija = request.getParameter("lokacija");
        if(lokacija != null && !lokacija.trim().isEmpty())
        {
            request.setAttribute("lokacija", lokacija);
            try
            {
                String[] coords = lokacija.split(";");
                if(coords.length != 2)
                    return "Pogreška kod formata koordinata!";
                
                String lat = coords[0].trim();
                Double.parseDouble(lat);
                String lon = coords[1].trim();
                Double.parseDouble(lon);
                String apiKey = konfiguracija.dajPostavku("apikey");
                if(apiKey != null)
                {
                    OWMKlijent owmKlijent = new OWMKlijent(apiKey);
                    MeteoPodaci meteoPodaci = owmKlijent.getRealTimeWeather(lon, lat);
                    if(meteoPodaci != null)
                    {
                        String temp  = meteoPodaci.getTemperatureValue().toString();
                        String vlaga = meteoPodaci.getHumidityValue().toString();
                        String tlak  = meteoPodaci.getPressureValue().toString();

                        request.setAttribute("temperatura", temp  + " " + meteoPodaci.getTemperatureUnit());
                        request.setAttribute("vlaga"      , vlaga + " " + meteoPodaci.getHumidityUnit());
                        request.setAttribute("tlak"       , tlak  + " " + meteoPodaci.getPressureUnit());

                        return "Success; Uspješko dohvaćeni metereološki podaci za koordinate";
                    }
                }
            }
            catch(PatternSyntaxException | NumberFormatException ex)
            {
                return "Pogreška kod formata koordinata!";
            }
        }
        return "Dohvaćanje lokacije iz adrese obavezno prije pokretanja dohvate metereoloških podataka";
    }
}
