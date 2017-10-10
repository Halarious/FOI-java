package org.foi.nwtis.robhalar.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Evidencija implements Serializable
{
    private static final Evidencija INSTANCE = new Evidencija();
    
    private HashMap<String, String>  statusiAdresa    = new HashMap<>();    
    private HashMap<String, Integer> brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev = new HashMap<>();

    private int ukupnoZahtjeva         = 0;
    private int brojUspjesnihZahtjeva  = 0;
    private int brojPrekinutihZahtjeva = 0;
    private int ukupnoVrijemeRadaRadnihDretvi = 0;
    private short zadnjiBrojRadneDretve  = 0;
    
    public static Evidencija getInstance()
    {
        return INSTANCE;
    }

    public synchronized void setZadnjiBrojRadneDretve(short index)
    {
        zadnjiBrojRadneDretve = index;
    }
    
    public synchronized Set<String> getAdrese()
    {
        return new HashSet<>(statusiAdresa.keySet());
    }
    
    public String getAdresaStatus(String adresa)
    {
        return statusiAdresa.get(adresa);
    }
    
    public synchronized void increment(String adresa)
    {
        if(brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev.containsKey(adresa))
        {
            Integer value = brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev.get(adresa);
            ++value;
        }
        else
            brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev.put(adresa, 1);
    }
    
    public synchronized void incrementTotalWorkTime(long time)
    {
        ukupnoVrijemeRadaRadnihDretvi += time;
    }
    
    public synchronized void incrementTotalRequests()
    {
        ++ukupnoZahtjeva;
    }
    
    public synchronized void incrementTotalFailedRequests()
    {
        ++brojPrekinutihZahtjeva;
        incrementTotalRequests();
    }

    public synchronized void incrementTotalSuccessRequests()
    {
        ++brojUspjesnihZahtjeva;
        incrementTotalRequests();
    }
    
    public synchronized boolean addAdresa(String adresa, boolean exists)
    {
        if(!statusiAdresa.containsKey(adresa))
        {
            if(exists)
                statusiAdresa.put(adresa, "YES");
            else
                statusiAdresa.put(adresa, "NO");
            
            return true;
        }
        else
            return false;
    }
    
    public synchronized void updateAdresaStatus(String adresa, boolean exists)
    {
        if(exists)
            statusiAdresa.put(adresa, "YES");
        else
            statusiAdresa.put(adresa, "NO");
    }
    
    public synchronized boolean serializeRecord(String datoteka)
    {
        if(datoteka == null || datoteka.isEmpty())
            return false;
            
        File file = new File(datoteka);
        OutputStream outStream = null;
        try
        {
            outStream = new FileOutputStream(file);
            ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream);
            objOutputStream.writeObject(this);
        } 
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(Evidencija.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        catch (IOException ex)
        {
            Logger.getLogger(Evidencija.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally
        {
            if(outStream != null)
            {
                try 
                {
                    outStream.close();
                } catch (IOException ex) 
                {
                    Logger.getLogger(Evidencija.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return true;
    }
    
    public synchronized void printEvidencija()
    {
        System.out.println("Ukupno zahtjeva: "                   + ukupnoZahtjeva);
        System.out.println("Broj uspjesnih zahtjeva: "           + brojUspjesnihZahtjeva);
        System.out.println("Broj prekinutih zahtjeva: "          + brojPrekinutihZahtjeva);
        System.out.println("Ukupno vrijeme rada radnih dretvi: " + ukupnoVrijemeRadaRadnihDretvi);
        System.out.println("Zadnji broj radne dretve: "          + zadnjiBrojRadneDretve);
        
        System.out.println("\nStatusi adresa: ");
        Set<Map.Entry<String, String>> statusSet = statusiAdresa.entrySet();
        for(Map.Entry entry : statusSet)
        {
            String key   = (String)entry.getKey();
            String value = (String)entry.getValue();
            System.out.println(key + " : \t" + value);
        }   
            
        System.out.println("\nBroj zahtjeva sa adresa: ");
        Set<Map.Entry<String, Integer>> brojZahtjevaSet = brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev.entrySet();
        for(Map.Entry entry : brojZahtjevaSet)
        {
            String  key   = (String)entry.getKey();
            Integer value = (Integer)entry.getValue();
            System.out.println(key + " : \t" + value);
        }
    }
    
    public synchronized void ucitajEvidenciju(Evidencija evidencija)
    {
        ukupnoZahtjeva                = evidencija.ukupnoZahtjeva;
        brojUspjesnihZahtjeva         = evidencija.brojUspjesnihZahtjeva;
        brojPrekinutihZahtjeva        = evidencija.brojPrekinutihZahtjeva;
        zadnjiBrojRadneDretve         = evidencija.zadnjiBrojRadneDretve;
        ukupnoVrijemeRadaRadnihDretvi = evidencija.ukupnoVrijemeRadaRadnihDretvi;
        
        brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev = new HashMap<>(evidencija.brojZahtjevaSPojedineAdreseSKojeJePoslanZahtjev);
        statusiAdresa    = new HashMap<>(evidencija.statusiAdresa);
    }
}
