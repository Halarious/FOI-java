package org.foi.nwtis.robhalar.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.robhalar.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.robhalar.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.robhalar.web.PreuzimanjeMeteoPodataka;

@WebListener
public class SlusacAplikacije implements ServletContextListener
{
    private ServletContext context;
    
    private PreuzimanjeMeteoPodataka preuzimanjeMeteoPodataka;
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        context = sce.getServletContext();
        String datoteka = context.getRealPath("/WEB-INF") 
                          + File.separator 
                          + context.getInitParameter("konfiguracija");
        
        BP_Konfiguracija bp_konf = new BP_Konfiguracija(datoteka);
        context.setAttribute("BP_Konfiguracija", bp_konf);
        
        System.out.println("Ucitana konfiguracija baze podataka");
        
        Konfiguracija konf = null;
        try 
        {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            context.setAttribute("Konfiguracija", konf);

            System.out.println("Ucitana konfiguracija baze podataka");
        } 
        catch (NemaKonfiguracije | NeispravnaKonfiguracija ex)
        {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        preuzimanjeMeteoPodataka = new PreuzimanjeMeteoPodataka(context);        
        preuzimanjeMeteoPodataka.start();
   }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        if(preuzimanjeMeteoPodataka != null)
        {
            try 
            {
                preuzimanjeMeteoPodataka.interrupt();
                preuzimanjeMeteoPodataka.join();
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);                
            }
        }
    }

    public ServletContext getContext()
    {
        return context;
    }
}
