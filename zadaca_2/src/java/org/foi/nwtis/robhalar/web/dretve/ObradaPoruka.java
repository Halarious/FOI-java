package org.foi.nwtis.robhalar.web.dretve;

import com.sun.mail.imap.IMAPFolder;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.servlet.ServletContext;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.robhalar.web.MailConstants;

public class ObradaPoruka extends Thread
{
    private final ServletContext context;
    private final Konfiguracija  konfiguracija;
    
    private boolean running;
    private int     cycleCount;
    
    public ObradaPoruka(ServletContext context)
    {
        this.context         = context;
        this.konfiguracija   = (Konfiguracija) context.getAttribute("Mail_Konfig");
        this.cycleCount      = 1;
        this.running         = true;
    }
    
    @Override
    public void interrupt()
    {
        running = false;
        super.interrupt();
    }

    @Override
    public synchronized void start()
    {
        super.start();
    }
    
    @Override
    public void run()
    {                               
        int    trajanjeCiklusaObrade = Integer.parseInt(konfiguracija.dajPostavku((MailConstants.MAIL_THREAD_CYCLETIME)));
        String server   = konfiguracija.dajPostavku(MailConstants.MAIL_SERVER);
        String port     = konfiguracija.dajPostavku(MailConstants.MAIL_PORT);
        String korisnik = konfiguracija.dajPostavku(MailConstants.MAIL_THREAD_USERNAME);
        String lozinka  = konfiguracija.dajPostavku(MailConstants.MAIL_THREAD_PASSWORD);
        String predmetZaObradu  = konfiguracija.dajPostavku(MailConstants.MAIL_SUBJECT);
        String folderNWTiS = konfiguracija.dajPostavku(MailConstants.MAIL_FOLDER_NWTIS);
        String folderOther = konfiguracija.dajPostavku(MailConstants.MAIL_FOLDER_OTHER);
   
        java.util.Properties properties = System.getProperties();
        properties.put("mail.imap.host", server);
        properties.put("mail.imap.port", port);
        
        Store store;
        try
        {
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");
        } 
        catch (NoSuchProviderException ex)
        {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            //todo Yeah, no
            return;
        }
        
        int addedIOTCount          = 0; 
        int tempProcessed          = 0; 
        int eventsProcessed        = 0; 
        int errorCount             = 0; 
        int messagesProcessedCount = 0; 
        for (; running; ++cycleCount) 
        {
            System.out.println("Obrada prouka u ciklusu: " + cycleCount);
            long pocetakObradeCiklusa = System.currentTimeMillis();    
            try 
            {                
                if(!store.isConnected())
                    store.connect(server, korisnik, lozinka);

                Folder inboxFolder = store.getFolder("INBOX");
                inboxFolder.open(Folder.READ_ONLY);
                
                IMAPFolder nwtisFolder = (IMAPFolder)store.getFolder(folderNWTiS);
                if(!nwtisFolder.exists())
                    nwtisFolder.create(Folder.HOLDS_MESSAGES);
                
                IMAPFolder otherFolder = (IMAPFolder)store.getFolder(folderOther);
                if(!otherFolder.exists())
                    otherFolder.create(Folder.HOLDS_MESSAGES);

                int unreadMessageCount = inboxFolder.getUnreadMessageCount();

                ArrayList<Message> nwtisMsgs = new ArrayList<>();
                ArrayList<Message> otherMsgs = new ArrayList<>();
                
                Flags seen = new Flags(Flags.Flag.SEEN);
                FlagTerm unreadTerm = new FlagTerm(seen, false);
                
                Message[] messages = inboxFolder.search(unreadTerm);
                for(Message message : messages)
                {
                    MimeMessage mimeMessage = (MimeMessage) message;
                    
                    String messageSubject = mimeMessage.getSubject();
                    if(messageSubject.equals(predmetZaObradu))
                    {
                        String status = processMsg(message);
                        switch(status)
                        {
                            case "add":
                                ++addedIOTCount;
                                break;
                            case "temp":
                                ++tempProcessed;
                                break;
                            case "event":
                                ++eventsProcessed;
                                break;
                            case "err":
                                ++errorCount;
                        }
                        
                        nwtisMsgs.add(message);
                        continue;
                    }
                    otherMsgs.add(message);
                }
                
                messagesProcessedCount = nwtisMsgs.size();
                if(nwtisMsgs.size() > 0)
                {
                    nwtisFolder.open(Folder.READ_WRITE);
                    Message[] nwtisMsgArray = new Message[nwtisMsgs.size()];
                    nwtisMsgs.toArray(nwtisMsgArray);
                    nwtisFolder.addMessages(nwtisMsgArray);
                    nwtisFolder.close(true);
                }
                
                if(otherMsgs.size() > 0)
                {
                    otherFolder.open(Folder.READ_WRITE);
                    Message[] otherMsgArray = new Message[otherMsgs.size()];
                    otherMsgs.toArray(otherMsgArray);
                    otherFolder.addMessages(otherMsgArray);
                    otherFolder.close(true);
                }
                
                inboxFolder.close(true);
                store.close();
            }
            catch (MessagingException ex)
            {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            sendReportMessage(server, messagesProcessedCount, addedIOTCount, 
                              tempProcessed, eventsProcessed,
                              errorCount, pocetakObradeCiklusa);
            try
            {
                long trajanjeObrade = System.currentTimeMillis() - pocetakObradeCiklusa;
                sleep( (trajanjeCiklusaObrade * 1000) - 
                        (trajanjeObrade % (trajanjeCiklusaObrade * 1000)) );
            } 
            catch (InterruptedException ex)
            {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private String processMsg(Message message)
    {
        try
        {
            ContentType contentType = new ContentType(message.getContentType());
            if("text/plain".equalsIgnoreCase(contentType.getBaseType()))
            {
                String   content     = message.getContent().toString().trim();
                String[] parseResult = parseMessageContent(content);
                if(parseResult != null)
                {
                    String  command = parseResult[0];
                    int     id      = Integer.parseInt(parseResult[1]);
                    
                    BP_Konfiguracija bp_konfiguracija  
                            = (BP_Konfiguracija)context.getAttribute("BP_Konfiguracija");
                    try
                    {
                        Class.forName(bp_konfiguracija.getDriverDatabase());
                        Connection connection = DriverManager.getConnection(
                                                    bp_konfiguracija.getServerDatabase() + 
                                                            bp_konfiguracija.getUserDatabase(),
                                                    bp_konfiguracija.getUserUsername(),
                                                    bp_konfiguracija.getUserPassword());
                        String query = "SELECT id, naziv FROM uredaji";

                        PreparedStatement statement = connection.prepareStatement(query);
                        ResultSet resultSet = statement.executeQuery(query);
                        
                        HashMap<Integer, String> uredaji = new HashMap<>();
                        while(resultSet.next())
                        {
                            int idUredaja  = resultSet.getInt("id");
                            String naziv   = resultSet.getString("naziv");
                            uredaji.put(idUredaja, naziv);
                        }
                        
                        PreparedStatement preparedStatement;
                        if(uredaji.containsKey(id))
                        {
                            switch(command)
                            {
                                case "TEMP":
                                {
                                    String temp = parseResult[3];
                                    String vrijemeMjerenja = parseResult[2];
                                    SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                    
                                    java.sql.Date resultDate;
                                    //ask netbeans, not me

                                    try                                    
                                    {
                                        Date date  = dateFormat.parse(vrijemeMjerenja);
                                        resultDate = new java.sql.Date(date.getTime());

                                    } catch (ParseException ex)
                                    {
                                        Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                        return "err";
                                    }
                                                               
                                    preparedStatement = connection.
                                            prepareStatement("INSERT INTO temperature (id, temp, vrijeme_mjerenja) VALUES (?, ?,?)");
                                    preparedStatement.setInt(1, id);
                                    preparedStatement.setString(2, temp);
                                    preparedStatement.setDate(3, resultDate);
                                    preparedStatement.execute();

                                    return "temp";
                                }
                                case "EVENT":
                                {
                                    int    vrsta              = Integer.parseInt(parseResult[3]);
                                    String vrijemeIzvrsavanja = parseResult[2];
                                    SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                    
                                    java.sql.Date resultDate;
                                    //ask netbeans, not me
                                    {
                                        try                                    
                                        {
                                            Date date  = dateFormat.parse(vrijemeIzvrsavanja);
                                            resultDate = new java.sql.Date(date.getTime());
                                            
                                        } catch (ParseException ex)
                                        {
                                            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                            return "err";
                                        }
                                    }
                                    
                                    preparedStatement = connection.
                                            prepareStatement("INSERT INTO dogadaji (id, vrsta, vrijeme_izvrsavanja) VALUES (?, ?, ?)");
                                    preparedStatement.setInt(1,  id);
                                    preparedStatement.setInt(2,  vrsta);
                                    preparedStatement.setDate(3, resultDate);
                                    preparedStatement.execute();
                                    
                                    return "event";
                                }
                            }
                        }
                        else if(command.equalsIgnoreCase("ADD"))
                        {
                            String naziv     = parseResult[2];
                            String[] coords  = parseResult[3].split(",");

                            preparedStatement = connection.
                                    prepareStatement("INSERT INTO uredaji (id, naziv, latitude, longitude) VALUES (?, ?, ?, ?)");
                            preparedStatement.setInt(1, id);
                            preparedStatement.setString(2, naziv);
                            preparedStatement.setString(3, coords[0]);
                            preparedStatement.setString(4, coords[1]);
                            preparedStatement.execute();
                            
                            return "add";
                        }
                        else
                        {
                            return "err";
                        }
                    } 
                    catch(SQLException  ex)             
                    {
                        Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        return "err";
                    }
                    catch(ClassNotFoundException ex)
                    {                        
                        Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        return "err";
                    }
                }
                else
                {
                    return "err";
                }
            }
            else
            {
                return "err";
            }
        } 
        catch (MessagingException | IOException ex)
        {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            return "err";
        }
        
        return "err";
    }
    
    public final String addPattern   
            = "^(ADD) IoT (\\d{1,6}) \"(.{1,30})\" GPS: (-?\\d{1,3}\\.\\d{6},-?\\d{1,3}\\.\\d{6});$";
    public final String tempPattern  
            = "^(TEMP) IoT (\\d{1,6}) T: ((?:\\d{1,4}\\.(?:(?:1[0-2])|(?:0?\\d))\\.(?:0[1-9]|[12]\\d)|3[01]) (?:[0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]) C: (-?\\d{1,2}.\\d);$"; 
    public final String eventPattern 
            = "^(EVENT) IoT (\\d{1,6}) T: ((?:\\d{1,4}\\.(?:(?:1[0-2])|(?:0?\\d))\\.(?:0[1-9]|[12]\\d)|3[01]) (?:[0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]) F: (\\d{1,2});$";
    private String[] parseMessageContent(String string)
    {
        String[] result = null;
        boolean hasMatch;

        Pattern patern = Pattern.compile(addPattern);
        Matcher matcher = patern.matcher(string);

        hasMatch = matcher.matches();
        if(hasMatch)
        {
            result = new String[4];
            
            result[0] = matcher.group(1);
            result[1] = matcher.group(2);
            result[2] = matcher.group(3);
            result[3] = matcher.group(4);
        }
        else
        {
            patern = Pattern.compile(tempPattern);
            matcher = patern.matcher(string);
            
            hasMatch = matcher.matches();
            if(hasMatch)
            {
                result = new String[4];
                
                result[0] = matcher.group(1);
                result[1] = matcher.group(2);
                result[2] = matcher.group(3);
                result[3] = matcher.group(4);
            }   
            else
            {
                patern = Pattern.compile(eventPattern);
                matcher = patern.matcher(string);
            
                hasMatch = matcher.matches();
                if(hasMatch)
                {
                    result = new String[4];

                    result[0] = matcher.group(1);
                    result[1] = matcher.group(2);
                    result[2] = matcher.group(3);
                    result[3] = matcher.group(4);
                }
            }
        }

        return result;
    }
    
    private void
    sendReportMessage(String webmailAddress,
                      int messagesProcessedCount, int addedIOTCount, int tempProcessed,
                      int eventsProcessed, int errorCount, long cycleStartTime)
    {
        String toAddress     = konfiguracija.dajPostavku(MailConstants.MAIL_USERNAME_STATS);
        String subjectPrefix = konfiguracija.dajPostavku(MailConstants.MAIL_SUBJECT_STATS);

        long cycleEndTime = System.currentTimeMillis();
        Date startDate    = new Date(cycleStartTime);
        Date endDate      = new Date(cycleEndTime);
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy hh.mm.ss.zzz");
        String reportText =   "Obrada započela u: " + DATE_FORMAT.format(startDate)    +"\n\r"
                            + "Obrada završila u: " + DATE_FORMAT.format(endDate)      +"\n\r\n\r"
                            + "Trajanje obrade u ms: " + (cycleEndTime-cycleStartTime) +"\n\r"
                            + "Broj poruka: " + messagesProcessedCount                 +"\n\r"
                            + "Broj dodanih IOT: " + addedIOTCount                     +"\n\r"
                            + "Broj mjerenih TEMP: " + tempProcessed                   +"\n\r"
                            + "Broj izvršenih EVENT: " + eventsProcessed               +"\n\r"
                            + "Broj pogrešaka: " + errorCount;

        try
        {
            Properties sysProperties = System.getProperties();
            sysProperties.put("mail.smtp.host", webmailAddress);

            Session session = Session.getInstance(sysProperties, null);

            MimeMessage message = new MimeMessage(session);

            Address fromAddress = new InternetAddress("Thread@nwtis.nastava.foi.hr");
            message.setFrom(fromAddress);

            Address[] toAddresses = InternetAddress.parse(toAddress);
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat myFormatter = new DecimalFormat("#,##0", symbols);
            String formattedSubject = String.format(subjectPrefix + 
                                                    "_%4s", myFormatter.format(cycleCount));

            message.setSubject(formattedSubject);
            message.setText(reportText, "UTF-8");

            Transport.send(message);

        } 
        catch (AddressException ex )
        {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (MessagingException ex)
        {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
