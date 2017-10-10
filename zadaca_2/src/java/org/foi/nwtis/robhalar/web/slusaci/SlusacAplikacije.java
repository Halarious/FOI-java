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
import org.foi.nwtis.robhalar.web.dretve.ObradaPoruka;

@WebListener
public class SlusacAplikacije implements ServletContextListener
{
    private ObradaPoruka obradaPoruka;
    
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ServletContext context = sce.getServletContext();
        String datoteka = context.getRealPath("/WEB-INF") 
                          + File.separator 
                          + context.getInitParameter("konfiguracija");
        
        BP_Konfiguracija bp_konf = new BP_Konfiguracija(datoteka);
        context.setAttribute("BP_Konfiguracija", bp_konf);
        
        System.out.println("Ucitana konfiguracija");
        
        Konfiguracija konf = null;
        try 
        {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            context.setAttribute("Mail_Konfig", konf);
        } 
        catch (NemaKonfiguracije | NeispravnaKonfiguracija ex)
        {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        obradaPoruka = new ObradaPoruka(context);        
        obradaPoruka.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        if(obradaPoruka != null)
        {
            try 
            {
                obradaPoruka.interrupt();
                obradaPoruka.join();
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
