package org.foi.nwtis.robhalar.zadaca_1;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;

public class SerijalizatorEvidencije extends Thread
{
    private volatile        boolean running = true;  
    private volatile        boolean shouldSerialize = false;
    
    private Konfiguracija konf;
    
    public SerijalizatorEvidencije(ThreadGroup group, String name, 
                                   Konfiguracija konf)
    {
        super(group, name);
        
        this.konf  = konf;
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
        System.out.println("Started thread: " + this.getName());
        
        synchronized(SerijalizatorEvidencije.this)
        {
            while(running)
            {
                if(!shouldSerialize)
                {
                    try
                    {
                        SerijalizatorEvidencije.this.wait();
                    } 
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(SerijalizatorEvidencije.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                }
                shouldSerialize = false;
                
                Evidencija.getInstance().serializeRecord(konf.dajPostavku(KonfiguracijaKeys.EVIDENCIJSKA_DATOTEKA));
            }
        }
    }

    @Override
    public synchronized void start()
    {
        super.start(); 
    }

    public synchronized void triggerSerialization()
    {
        if(!shouldSerialize && this.getState() == Thread.State.WAITING)
        {
            shouldSerialize = true;
            SerijalizatorEvidencije.this.notify();
        }
    }
    
    public synchronized boolean triggerSerializationWaitResponse()
    {
        if(!shouldSerialize && this.getState() == Thread.State.WAITING)
        {
            shouldSerialize = true;
            SerijalizatorEvidencije.this.notify();
            
            return true;
        }
        else
            return true;
    }
}
