package org.foi.nwtis.robhalar.zadaca_1;

import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;

public class NadzorDretvi extends Thread
{
    private volatile boolean running = true;

    private Konfiguracija konf;
    private ThreadGroup   watchGroup;
    private Thread[]      activeThreads;

    
    public NadzorDretvi(ThreadGroup group, String name, 
                        Konfiguracija konf, ThreadGroup watchGroup)
    {
        super(group, name);
        
        this.konf  = konf;
        this.watchGroup = watchGroup;
        
        int maxThreads = Integer.parseInt(konf.dajPostavku(KonfiguracijaKeys.MAX_BROJ_RADNIH_DRETVI));
        this.activeThreads = new Thread[maxThreads];
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

    @Override
    public void run()
    {
        System.out.println("Started thread: " + this.getName());
    
        int maxTrajanjeRadne = Integer.parseInt(konf.dajPostavku("maksVrijemeRadneDretve"));
        int trajanjeSpavanja = Integer.parseInt(konf.dajPostavku("intervalNadzorneDretve"));
        while(running)
        {
            long pocetnoVrijeme  = System.currentTimeMillis();
            
            int numThreads = watchGroup.enumerate(activeThreads);
            for(int index = 0; index < numThreads; ++index)
            {
                Thread thread = activeThreads[index];
                if(thread.isAlive() && thread instanceof RadnaDretva)
                {
                    long startTime = ((RadnaDretva)thread).startTime;
                    if( (System.currentTimeMillis() - startTime) > maxTrajanjeRadne &&
                         !thread.isInterrupted())
                    {
                        thread.interrupt();
                        System.out.println("Interrupted: " + thread.getName());
                    }
                }
            }
            try
            {
                long vrijemeSpavanja 
                        = trajanjeSpavanja - (System.currentTimeMillis() - pocetnoVrijeme) % trajanjeSpavanja;
                sleep(vrijemeSpavanja);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(ProvjeraAdresa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        int numThreads = watchGroup.enumerate(activeThreads);
        for(int index = 0; index < numThreads; ++index)
        {
            Thread thread = activeThreads[index];
            thread.interrupt();
            try
            {
                thread.join();
            } 
            catch (InterruptedException ex)
            {
                Logger.getLogger(NadzorDretvi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        int numControlThreads = getThreadGroup().enumerate(activeThreads);
        for(int index = 0; index < numControlThreads; ++index)
        {
            Thread thread = activeThreads[index];
            if(thread.getName().equals(this.getName()))
                continue;
            
            thread.interrupt();
            try
            {
                thread.join();
            } 
            catch (InterruptedException ex)
            {
                Logger.getLogger(NadzorDretvi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
