package org.foi.nwtis.robhalar.zadaca_1;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;

public class ProvjeraAdresa extends Thread
{
    private volatile boolean running = true;

    private final Konfiguracija konf;
    private final LimitedArrayList<String> adrese;
    
    public ProvjeraAdresa(ThreadGroup group, String name, 
                          Konfiguracija konf)
    {        
        super(group, name);
        
        this.konf = konf;
        
        int maxAdresa = Integer.parseInt(konf.dajPostavku("maksAdresa"));
        this.adrese   = new LimitedArrayList<>(maxAdresa);
        
        Set keys = Evidencija.getInstance().getAdrese();
        
        if(keys.size() < maxAdresa)
            this.adrese.addAll(keys);
    }
    
    @Override
    public synchronized void start()
    {
        super.start(); 
    }
    
    @Override
    public void interrupt()
    {
        running = false;
        super.interrupt();
    }

    private Boolean checkAdresa(String adresa)
    {
        try
        {
            final URL url = new URL(adresa);
            
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            
            boolean exists;
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 200)
                return true;
            else
                return false;
            
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(ProvjeraAdresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex)
        {
            Logger.getLogger(ProvjeraAdresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(ProvjeraAdresa.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null; 
    }
    
    @Override
    public void run()
    {
        System.out.println("Started thread: " + this.getName());

        int trajanjeSpavanja = Integer.parseInt(konf.dajPostavku(KonfiguracijaKeys.INTERVAL_ADRESNE_DRETVE));
        while(running)
        {
            long pocetnoVrijeme  = System.currentTimeMillis();
            try
            {
                for(String adresa : adrese)
                {
                    boolean exists;
                    exists = checkAdresa(adresa);
                    
                    Evidencija.getInstance().updateAdresaStatus(adresa, exists);
                }

                long vrijemeSpavanja 
                        = trajanjeSpavanja - (System.currentTimeMillis() - pocetnoVrijeme) % trajanjeSpavanja;
                sleep(vrijemeSpavanja);
            } 
            catch (InterruptedException ex)
            {
                Logger.getLogger(ProvjeraAdresa.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    public synchronized String addAdresa(String adresa)
    {
        if(adrese.isFull())
            return Errors.ERROR_FULL + " Address not added; queue is full";
        
        Boolean check = checkAdresa(adresa);
        if(check == null)
            return "Error; Malformed URL (" + adresa + ")";
        
        boolean added= 
                Evidencija.getInstance().addAdresa(adresa, check);
        
        if(added)
        {
            if(adrese.add(adresa))
                return Errors.SUCCESS;
            else 
                return "Internal error";
        }
        else
            return Errors.ERROR_DUPLICATE + " Address already in queue";

    }
}
