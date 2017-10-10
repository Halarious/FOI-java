package org.foi.nwtis.robhalar.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.robhalar.konfiguracije.KonfiguracijaBIN;
import org.foi.nwtis.robhalar.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.NemaKonfiguracije;

public class ServerSustava implements RadnaDretva.RadnaDretvaListener
{
    private volatile boolean paused    = false;
    private volatile boolean stopped   = false;
    private ServerSocket serverSocket  = null;

    @Override
    public boolean isPaused()
    {
        return paused;
    }
    
    @Override
    public boolean onPauseCommand()
    {
        if(paused)
            return false;
        
        paused = true;
        return paused;
    }
    
    @Override
    public boolean onStartCommand()
    {
        if(!paused)
            return false;
        paused = false;
        return !paused;
    }

    @Override
    public boolean onStopCommand()
    {
        if(stopped)
            return false;
        
        try
        {
            stopped = true;
            
            Socket dummySocket = new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
        } catch (IOException ex)
        {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return stopped;
    }
    
   public static void main(String args[])
   {
       String stringPattern = "^-server -konf ([^\\s]+\\.(?i))(txt|xml)( +-load)?$";
       if(args.length < 3)
       {
           System.out.println("Usage: ServerSustava -server -konf <filepath> {-load}");
           return;
       }
       
       StringBuilder stringBuilder = new StringBuilder();
       for (String arg : args)
       {
           stringBuilder.append(arg).append(" ");
       }
       String stringArgs = stringBuilder.toString().trim();
       Pattern pattern = Pattern.compile(stringPattern);
       Matcher matcher = pattern.matcher(stringArgs);
       if(!matcher.matches())
       {
           System.out.println("Usage: ServerSustava -server -konf <filepath> {-load}");
           return;
       }
       
       String nazivDatoteke = matcher.group(1) + matcher.group(2); 
       
       boolean ucitatiEvidenciju = false;
       if(matcher.group(3) != null)
           ucitatiEvidenciju = true;
       
       ServerSustava server = new ServerSustava();
       server.pokreniServer(nazivDatoteke, ucitatiEvidenciju);
   }

    private void pokreniServer(String nazivDatoteke, boolean ucitatiEvidenciju)
    {  
        ThreadGroup grupaRadnihDretvi     = new ThreadGroup("robhalar-groupRadnihDretvi");
        ThreadGroup grupaKontrolnihDretvi = new ThreadGroup("robhalar-groupKontrolnihDretvi");
        Konfiguracija konf;
        try
        {
           konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
           if(ucitatiEvidenciju)
           {
               String nazivEvidencije = konf.dajPostavku(KonfiguracijaKeys.EVIDENCIJSKA_DATOTEKA);
               if(!ucitajEvidenciju(nazivEvidencije))
               {
                   System.out.println("Greska kod ucitavana evidencije");
                   return;
               }
           }
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex)
        {
            System.out.println("Greska kod ucitavana evidencije");
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        int port                          = Integer.parseInt(konf.dajPostavku(KonfiguracijaKeys.PORT));
        int maksBrojRadnihDretvi          = Integer.parseInt(konf.dajPostavku(KonfiguracijaKeys.MAX_BROJ_RADNIH_DRETVI));
        BlockingQueue<Socket> socketQueue = new ArrayBlockingQueue<>(maksBrojRadnihDretvi*2);

        NadzorDretvi 
                nadzorDretvi   = new NadzorDretvi(grupaKontrolnihDretvi, NadzorDretvi.class.getSimpleName(), 
                                                  konf, grupaRadnihDretvi);
        ProvjeraAdresa 
                provjeraAdresa = new ProvjeraAdresa(grupaKontrolnihDretvi, ProvjeraAdresa.class.getSimpleName(), 
                                                    konf);
        SerijalizatorEvidencije 
                serijalizatorEvidencije = new SerijalizatorEvidencije(grupaKontrolnihDretvi,SerijalizatorEvidencije.class.getSimpleName(), 
                                                                      konf);
        RezervnaDretva 
                rezervnaDretva = new RezervnaDretva(grupaKontrolnihDretvi, RezervnaDretva.class.getSimpleName(), 
                                                    konf, socketQueue);

        nadzorDretvi  .start();
        provjeraAdresa.start();                     
        rezervnaDretva.start();
        serijalizatorEvidencije.start();

        try
        {
            serverSocket = new ServerSocket(port);

        }
        catch (IOException ex)
        {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        short threadIndex = 0;
        while(true)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                if(stopped)
                    break;
                
                if(grupaRadnihDretvi.activeCount() < maksBrojRadnihDretvi)
                {
                    Evidencija.getInstance().setZadnjiBrojRadneDretve(threadIndex);
                    if(threadIndex == Short.MAX_VALUE)
                        threadIndex = 0;
                    else
                        ++threadIndex;
                    RadnaDretva radnaDretva = new RadnaDretva(this, konf,
                                                              grupaRadnihDretvi,
                                                              "robhalar-" + (threadIndex),
                                                              clientSocket);
                    radnaDretva.start();
                }
                else
                {
                    try
                    {
                        socketQueue.put(clientSocket);
                    } 
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            catch(IOException ex)
            {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //todo The main thread should wait to join all control threads, 
        //     this works now because it is "guaranteed" that NadzorDretvi will finish last
        try
        {
            nadzorDretvi.interrupt();
            nadzorDretvi.join();
        } 
        catch (InterruptedException ex)
        {
            Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                serverSocket.close();
            } catch (IOException ex)
            {
                Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private boolean ucitajEvidenciju(String datoteka)
    {
        if(datoteka == null || datoteka.isEmpty())
        {
            System.out.println("Pogreška u nazivu datoteke");
            return false;
        }
        
        File file = new File(datoteka);
        if(!file.exists() || !file.isFile())
        {
            System.out.println("Datoteka evidencije ne postoji");
            return false;
        }
        
        try
        {
            InputStream inStream = new FileInputStream(file);
            ObjectInputStream objInputStream = new ObjectInputStream(inStream);
            
            Object readObject = objInputStream.readObject();
            if(readObject instanceof Evidencija)
                Evidencija.getInstance().ucitajEvidenciju((Evidencija)readObject);
            
            return true;
        } 
        catch (IOException exception)
        {
            Logger.getLogger(KonfiguracijaBIN.class.getName()).log(Level.SEVERE, null, exception);
        } catch (ClassNotFoundException exception)
        {
           Logger.getLogger(ServerSustava.class.getName()).log(Level.SEVERE, null, exception);
        }
        
        return false;
    }
}
