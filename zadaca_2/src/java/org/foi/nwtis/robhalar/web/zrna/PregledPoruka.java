package org.foi.nwtis.robhalar.web.zrna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletContext;
import org.foi.nwtis.robhalar.web.Poruka;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.web.Izbornik;
import org.foi.nwtis.robhalar.web.MailConstants;

@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka
{
    private final ServletContext context;
    
    private Store store;
    
    private final String adresaPosluzitelja;
    private final String korisnickoIme;
    private final String lozinka;

    private final ArrayList<Izbornik> mape;
    private String odabranaMapa;
    
    private final ArrayList<Poruka> poruke;
    private final int brojPorukaZaPrikaz;
    private int ukupanBrojPorukaUMapi; 
    private int odPoruke;
    private int doPoruke;
    
    private boolean disableNext;
    private boolean disablePrevious;
    
    private String pretrazivanjePoruka;
    
    public PregledPoruka()
    {
        this.mape               = new ArrayList<>();
        this.poruke             = new ArrayList<>();
        this.context            = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        
        Konfiguracija konfiguracija = (Konfiguracija) context.getAttribute("Mail_Konfig");
        this.adresaPosluzitelja = konfiguracija.dajPostavku(MailConstants.MAIL_SERVER);
        this.korisnickoIme      = konfiguracija.dajPostavku(MailConstants.MAIL_VIEW_USERNAME);
        this.lozinka            = konfiguracija.dajPostavku(MailConstants.MAIL_VIEW_PASSWORD);
        this.brojPorukaZaPrikaz = Integer.parseInt(
                                    konfiguracija.dajPostavku(MailConstants.MAIL_NUM_MESSAGES));
        
        this.odabranaMapa        = "INBOX";
        this.pretrazivanjePoruka = "";
        
        this.odPoruke = 1;
        this.doPoruke = odPoruke + (brojPorukaZaPrikaz - 1);
        
        this.disableNext     = false;
        this.disablePrevious = false;
    }
    
    @PreDestroy
    public void die()
    {
        if(store.isConnected())
        {
            try
            {
                store.close();
            } catch (MessagingException ex)
            {
                Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @PostConstruct
    public void init()
    {
        Properties properties = System.getProperties();
        try
        {
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imap");

            if(!store.isConnected())
                store.connect(adresaPosluzitelja, korisnickoIme, lozinka);
            
            preuzmiMape();
            
        } catch (NoSuchProviderException ex)
        {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex)
        {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public String getOdabranaMapa()
    {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa)
    {
        this.odabranaMapa = odabranaMapa;
    }
    
    public ArrayList<Izbornik> getMape()
    {
        return mape;
    }

    public ArrayList<Poruka> getPoruke()
    {
        return poruke;
    }

    public String getPretrazivanjePoruka()
    {
        return pretrazivanjePoruka;
    }

    public void setPretrazivanjePoruka(String pretrazivanjePoruka)
    {
        this.pretrazivanjePoruka = pretrazivanjePoruka;
    }

    public int getUkupanBrojPorukaUMapi()
    {
        return ukupanBrojPorukaUMapi;
    }

    public int getOdPoruke()
    {
        return odPoruke;
    }

    public void setOdPoruke(int odPoruke)
    {
        this.odPoruke = odPoruke;
    }

    public int getDoPoruke()
    {
        return doPoruke;
    }

    public void setDoPoruke(int doPoruke)
    {
        this.doPoruke = doPoruke;
    }

    public boolean isDisableNext()
    {
        return disableNext;
    }

    public boolean isDisablePrevious()
    {
        return disablePrevious;
    }
    
    
    public String promjenaMape()
    { 
        odPoruke = 1;
        doPoruke = odPoruke + (brojPorukaZaPrikaz - 1);
        return "promjenaMape";
    }
    public String traziPoruke()
    { 
        return "traziPoruke";
    }
                
    public String prethodnePoruke()
    {
        odPoruke -= brojPorukaZaPrikaz;
        if(odPoruke < 1)
            odPoruke = 1;
        doPoruke = odPoruke + (brojPorukaZaPrikaz - 1);
        return "prethodnePoruke";
    }
    
    public String sljedecePoruke()
    {
        odPoruke += brojPorukaZaPrikaz;
        doPoruke =  odPoruke + (brojPorukaZaPrikaz - 1); 
        return "sljedecePoruke";
    }
    
    public String promjenaJezika()
    {
        return "promjenaJezika";
    }
    
    public String saljiPoruku()
    { 
        return "saljiPoruku";
    }
    
    public void preRender()
    {
        if(odPoruke == 1)
            disablePrevious = true;
        
        preuzmiPoruke();
    }
    
    private void preuzmiPoruke()
    {
        try
        {
            Folder odabranaMapaFolder = store.getFolder(odabranaMapa);
            ukupanBrojPorukaUMapi     = odabranaMapaFolder.getMessageCount();
            if(ukupanBrojPorukaUMapi == 0)
            {
                disableNext = true;
                return;
            }
            
            if(doPoruke >= ukupanBrojPorukaUMapi)
            {
                doPoruke   = ukupanBrojPorukaUMapi;
                disableNext = true;
            }
            
            odabranaMapaFolder.open(Folder.READ_ONLY);
            Message[] porukeMape = odabranaMapaFolder.getMessages(odPoruke, doPoruke);
            
            for(Message msg : porukeMape)
            {
                String id           = "";
                Date vrijemeSlanja  = msg.getSentDate();
                Date vrijemePrijema = msg.getReceivedDate();
                
                Address[] adresePosiljatelja = msg.getFrom();
                String salje = "<unknown>";
                if(adresePosiljatelja != null)
                {
                    salje    = adresePosiljatelja[0].toString();
                }
                
                String predmet      = msg.getSubject();
                String vrsta        = msg.getContentType();
                String sadrzaj      = msg.getContent().toString();
                Poruka poruka = new Poruka(id, vrijemeSlanja, vrijemePrijema,
                        salje,predmet, sadrzaj, vrsta);
                
                poruke.add(poruka);
            }
            
            odabranaMapaFolder.close(false);
        } catch (MessagingException | IOException ex)
        {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void preuzmiMape() throws MessagingException
    {
        Folder[] folderArray = store.getDefaultFolder().list();         
        for(Folder folder : folderArray)
        {
            int brojPorukaUMapi = folder.getMessageCount();
            String folderLabel  = folder.getName().toLowerCase() + " - " + brojPorukaUMapi;
            mape.add(new Izbornik(folderLabel, folder.getFullName()));
        }
    }
}
