package org.foi.nwtis.robhalar.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;

public class RezervnaDretva extends Thread
{
    private volatile boolean running = true;
    
    private Konfiguracija konf;
    private BlockingQueue queue;
    
    public RezervnaDretva(ThreadGroup group, String name, 
                          Konfiguracija konf, BlockingQueue queue)
    {
        super(group, name);

        this.konf  = konf;
        this.queue = queue;
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

        Socket clientSocket = null;
        OutputStream clientOutputStream = null;
        InputStream clientInputStream   = null;
        while(running)
        {
            try
            {
                clientSocket = (Socket) queue.take();
                clientOutputStream = clientSocket.getOutputStream();
                clientInputStream  = clientSocket.getInputStream();
            
                StringBuilder outputString = new StringBuilder();
                outputString.append(Errors.ERROR_NO_THREADS).append(" No free threads");
                
                clientOutputStream.write(outputString.toString().getBytes());
                clientOutputStream.flush();
                
            } 
            catch (InterruptedException | IOException ex)
            {
                Logger.getLogger(RezervnaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                try
                {
                    if(clientInputStream != null)
                        clientInputStream.close();
                    if(clientOutputStream != null)
                        clientOutputStream.close();
                    if(clientSocket != null)
                        clientSocket.close();

                } catch (IOException ex)
                {
                    Logger.getLogger(RezervnaDretva.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public synchronized void start()
    {
        super.start(); 
    }

    
}
