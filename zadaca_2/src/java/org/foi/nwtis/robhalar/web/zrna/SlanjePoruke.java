package org.foi.nwtis.robhalar.web.zrna;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.foi.nwtis.robhalar.konfiguracije.Konfiguracija;
import org.foi.nwtis.robhalar.web.MailConstants;

@Named(value = "slanjePoruke")
@RequestScoped
public class SlanjePoruke
{
    private final ServletContext context;
    private final Konfiguracija  konfiguracija;
    private final String adresaPosluzitelja;
    
    private String primatelj;
    private String posiljatelj; 
    private String predmet; 
    private String sadrzaj;
    
    public SlanjePoruke()
    {
        context = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
        konfiguracija = (Konfiguracija) context.getAttribute("Mail_Konfig");
        adresaPosluzitelja = konfiguracija.dajPostavku(MailConstants.MAIL_SERVER);
    }
    
    public String getPrimatelj()
    {
        return primatelj;
    }

    public void setPrimatelj(String primatelj)
    {
        this.primatelj = primatelj;
    }

    public String getPosiljatelj()
    {
        return posiljatelj;
    }

    public void setPosiljatelj(String posiljatelj)
    {
        this.posiljatelj = posiljatelj;
    }

    public String getPredmet()
    {
        return predmet;
    }

    public void setPredmet(String predmet)
    {
        this.predmet = predmet;
    }

    public String getSadrzaj()
    {
        return sadrzaj;
    }

    public void setSadrzaj(String sadrzaj)
    {
        this.sadrzaj = sadrzaj;
    }
    
    public String saljiPoruku()
    {
        String status = "";

        try 
        {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", adresaPosluzitelja);

            Session session =
                    Session.getInstance(properties, null);

            boolean hasError = false;
            InternetAddress fromAddress = parseEmailAddress(posiljatelj);
            if(fromAddress == null)
            {
                FacesContext.getCurrentInstance()
                        .addMessage("slanjeForm:posiljatelj", new FacesMessage("Treba biti valjana email adresa"));
                hasError = true;
            }

            InternetAddress toAddress = parseEmailAddress(primatelj);
            if(toAddress == null)
            {                
                FacesContext.getCurrentInstance()
                        .addMessage("slanjeForm:primatelj", new FacesMessage("Treba biti valjana email adresa"));
                hasError = true;
            }
            
            if(hasError)
                return "err";
            
            MimeMessage message = new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(predmet);
            message.setText(sadrzaj);

            Transport.send(message);

            clearFields();
            status = "OK";

        }
        catch (MessagingException ex)
        {
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return status;
    }
    
    public String promjenaJezika()
    {
        return "promjenaJezika";
    }
    
    public String pregledPoruka()
    { 
        return "pregledPoruka";
    }
    
    private void clearFields()
    {
        this.primatelj  = "";
        posiljatelj     = ""; 
        this.predmet    = ""; 
        this.sadrzaj    = "";
    }
    
    private InternetAddress parseEmailAddress(String email)
    {
        InternetAddress returnAddress;
        try
        {
             returnAddress = new InternetAddress(email);
             returnAddress.validate();
        } catch (AddressException ex)
        {
            Logger.getLogger(SlanjePoruke.class.getName()).log(Level.SEVERE, null, ex);
            returnAddress = null;
        }
        return returnAddress;
    }
}
