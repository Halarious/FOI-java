package org.foi.nwtis.robhalar.zadaca_1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.robhalar.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.NemaKonfiguracije;

public class RadnaDretva extends Thread
{
    public interface RadnaDretvaListener
    {
        boolean onPauseCommand();
        boolean onStartCommand();
        boolean onStopCommand();
        
        boolean isPaused();
    }
    
    private final Socket              clientSocket;
    private final Konfiguracija       konf;
    private final RadnaDretvaListener listener;

    public long startTime;
    
    public RadnaDretva(RadnaDretvaListener listener, Konfiguracija konf, 
                       ThreadGroup group, String name, 
                       Socket clientSocket)
    {
        super(group, name);
        this.clientSocket = clientSocket;
        this.konf         = konf;
        this.listener     = listener;
    }

    @Override
    public void interrupt()
    {
        super.interrupt();
    }
    
    
    InputStream  inputStream  = null;
    OutputStream outputStream = null;
        
    @Override
    public void run()
    {
        System.out.println("Started thread: " + this.getName());
        
        startTime = System.currentTimeMillis();
        
        try
        {
            inputStream  = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
            
            StringBuilder builder = new StringBuilder();
            byte inputByte;
            while( (inputByte = (byte)inputStream.read()) != -1)
            {
                builder.append((char)inputByte);
            }
            String recievedString = builder.toString().trim();

            System.out.println("Primljena naredba:" + recievedString);
            
            String outputMsg;
            if(provjeriKomandu(recievedString))
            {
                switch(recievedCommand.command)
                {
                    case "PAUSE":
                    case "START":
                    case "STOP":
                    case "STAT":
                        outputMsg = processAdminCommand();
                        break;
                    case "ADD":
                        outputMsg = processAddCommand();
                        break;
                    case "TEST":
                        outputMsg = processTestCommand();
                        break;
                    case "WAIT":
                        outputMsg = processWaitCommand();
                        break;
                    default:
                        outputMsg = "Could not process command " + recievedCommand.command;
                }
                recievedCommand.clear();
            }
            else
                outputMsg = Errors.ERROR_BAD_COMMAND + " Pogreska u naredbi";
            
            outputStream.write(outputMsg.getBytes());
            outputStream.flush();
        }
        catch (IOException ex)
        {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                if(inputStream != null)
                    inputStream.close();
                if(outputStream != null)
                    outputStream.close();
                clientSocket.close();
            } catch (IOException ex)
            {
                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        Evidencija.getInstance().incrementTotalWorkTime(System.currentTimeMillis() - startTime);
    }

    @Override
    public synchronized void start()
    {
        super.start(); 
    }

    boolean provjeriKomandu(String komandaString)
    {

        final String sintaksa      = "^USER ([^\\s]+); ((PASSWD ([^\\s]+); (PAUSE|START|STOP|STAT))?|"
                                   + "((ADD|TEST) (http://((([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?)))|"
                                   + "((WAIT) (([1-9][0-9]{3}[0-9]?)|([1-5][0-9]{5})|(600000))));$";
        
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher matcher = pattern.matcher(komandaString);
    
        if(matcher.matches())
        {
            recievedCommand.user = matcher.group(1);
            if(matcher.group(3) != null)
            {
                recievedCommand.command  = matcher.group(5);
                recievedCommand.argument = matcher.group(4);
            }
            else
            {
                if(matcher.group(6) != null)
                {
                    recievedCommand.command  = matcher.group(7); 
                    recievedCommand.argument = matcher.group(8);
                }
                else
                {
                    recievedCommand.command  = matcher.group(18); 
                    recievedCommand.argument = matcher.group(19);
                }
            }
        }
        
        return matcher.matches();
    }
    
    private String processAdminCommand()
    {
        String outputMsg;
        String datoteka = konf.dajPostavku(KonfiguracijaKeys.ADMIN_DATOTEKA);
        
        File adminFile  = new File(datoteka);
        if(adminFile.exists() && adminFile.isFile())
        {
            try
            {
                Properties adminKonf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka).dajSvePostavke();
                if(adminKonf.containsKey(recievedCommand.user))
                {
                    if(adminKonf.getProperty(recievedCommand.user).equals(recievedCommand.argument))
                    {
                        switch(recievedCommand.command)
                        {
                            case "PAUSE":
                                outputMsg = processAdminPause();
                                break;
                            case "START":
                                outputMsg = processAdminStart();
                                break;
                            case "STOP":
                                outputMsg = processAdminStop();
                                break;
                            case "STAT":
                                outputMsg = processAdminStat();
                                break;
                            default:
                                outputMsg = "Internal error; Could not recognize command";
                        }
                    }
                    else
                        outputMsg = Errors.ERROR_NOT_AUTHORIZED + " Korisnik neautoriziran ili lozinka neispravna";
                }
                else
                    outputMsg = Errors.ERROR_NOT_AUTHORIZED + " Korisnik neautoriziran ili lozinka neispravna";
            }
            catch (NemaKonfiguracije | NeispravnaKonfiguracija ex)
            {
                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
                return "";
            }
        }
        else
            outputMsg = Errors.ERROR_NOT_AUTHORIZED + " Korisnik neautoriziran ili lozinka neispravna";
        
        return outputMsg;
    }
    
    private String processAdminPause()
    {
        if(listener.onPauseCommand())
            return Errors.SUCCESS;
        else
            return Errors.ERROR_PAUSED + " Server already paused";
    }
    
    private String processAdminStart()
    {
        if(listener.onStartCommand())
            return Errors.SUCCESS;
        else
            return Errors.ERROR_NOT_PAUSED + " Server not in paused state";
    }
    
    private Thread getThreadByName(String groupName, String threadName)
    {
        ThreadGroup parentGroup = this.getThreadGroup().getParent();
        ThreadGroup[] threadGroups = new ThreadGroup[parentGroup.activeGroupCount()];
        int activeGroupNum = this.getThreadGroup().getParent().enumerate(threadGroups);
        for(;activeGroupNum > 0; --activeGroupNum)
        {
            ThreadGroup threadGroup = threadGroups[activeGroupNum-1];
            if(threadGroup.getName().equals(groupName))
            {
                Thread[] threads = new Thread[threadGroup.activeCount()];
                int activeThreadNum = threadGroup.enumerate(threads);
                for(;activeThreadNum > 0; --activeThreadNum)
                {
                    Thread thread = threads[activeThreadNum-1];
                    if(thread.getName().equals(threadName))
                    {
                        return thread;
                    }
                }
            }           
        }
        
        return null;
    }
    
    private String processAdminStop()
    {
        if(listener.onStopCommand())
        {
            Thread thread = getThreadByName("robhalar-groupKontrolnihDretvi", 
                                            SerijalizatorEvidencije.class.getSimpleName());
            if(thread != null && thread instanceof SerijalizatorEvidencije)
                ((SerijalizatorEvidencije)thread).triggerSerializationWaitResponse();
            else
                return Errors.ERROR_SHUTDOWN + "Could not serialize; Serialization thread deceased";
            
            return Errors.SUCCESS;
        }
        else
            return Errors.ERROR_SHUTDOWN + "Problem while stopping server";
    }
    
    private String processAdminStat()
    {
        ByteArrayOutputStream byteOutputStream = null;
        ObjectOutputStream    objectOutputStream  = null;
        try
        {
            byteOutputStream   = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteOutputStream);
                        
            objectOutputStream.writeObject(Evidencija.getInstance());
            objectOutputStream.flush();
            objectOutputStream.close();
            
            int size = byteOutputStream.size();
            
            String outputString = Errors.SUCCESS + " LENGTH "+ size + "\r\n";
            
            outputStream.write(outputString.getBytes());
            outputStream.flush();
            
            byteOutputStream.writeTo(outputStream);
            byteOutputStream.flush();
        } catch (IOException ex)
        {
            Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                if(objectOutputStream != null)
                    objectOutputStream.close();
                if(byteOutputStream != null)
                    byteOutputStream.close();
                
            } catch (IOException ex)
            {
                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Errors.SUCCESS;
    }
    
    private String processAddCommand()
    {
        if(!listener.isPaused())
        {
            String outputMsg;
            Thread thread = getThreadByName("robhalar-groupKontrolnihDretvi", 
                                            ProvjeraAdresa.class.getSimpleName());
            if(thread != null && thread instanceof ProvjeraAdresa)
            {
                outputMsg = ((ProvjeraAdresa)thread).addAdresa(recievedCommand.argument);
            }
            else
                outputMsg = "Error; Thread deceased";
            
            return outputMsg;
        }
        else
            return Errors.ERROR_NOT_PAUSED + " Cannot process command, server paused";
    }
    
    private String processTestCommand()
    {
        if(!listener.isPaused())
        {
            String status = Evidencija.getInstance().getAdresaStatus(recievedCommand.argument);
            if(status != null)
                return status;
            else
                return Errors.ERROR_NONEXIST + " Test failed, address not in queue";
        }
        else
            return Errors.ERROR_NOT_PAUSED + " Cannot process command, server paused";
    }
    
    private String processWaitCommand()
    {
        if(!listener.isPaused())
        {
            long sleepTime = Long.parseLong(recievedCommand.argument);
            try
            {
                sleep(sleepTime);

                return Errors.SUCCESS;
            } catch (InterruptedException ex)
            {
                Logger.getLogger(RadnaDretva.class.getName()).log(Level.SEVERE, null, ex);
                return Errors.ERROR_INTERRUPTED + " Wait interrupted";
            }
        }
        else
            return Errors.ERROR_NOT_PAUSED + " Cannot process command, server paused";
    }
    
    private static class recievedCommand
    {
        static String user;
        static String command;
        static String argument;
        
        public static void clear()
        {
            user     = null;
            command  = null;
            argument = null;
        }
    }
    
    public String getUserTest()
    {
        return recievedCommand.user;
    }
    
    public String getCommandTest()
    {
        return recievedCommand.command;
    }
    
    public String getArgumentTest()
    {
        return recievedCommand.argument;
    }
}
