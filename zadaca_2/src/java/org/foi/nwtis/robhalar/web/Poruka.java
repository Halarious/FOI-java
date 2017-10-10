package org.foi.nwtis.robhalar.web;

import java.util.Date;
import java.util.List;
import javax.mail.Flags;

public class Poruka {

    private String id;
    private Date vrijemeSlanja;
    private Date vrijemePrijema;
    private String salje;
    private String predmet;
    private String sadrzaj;
    private String vrsta;

    public Poruka(String id, Date vrijemeSlanja, Date vrijemePrijema, 
                  String salje, String predmet, String sadrzaj, String vrsta) 
    {
        this.id = id;
        this.vrijemeSlanja = vrijemeSlanja;
        this.vrijemePrijema = vrijemePrijema;
        this.salje = salje;
        this.predmet = predmet;
        this.sadrzaj = sadrzaj;
        this.vrsta = vrsta;
    }

    public String getId() {
        return id;
    }

    public Date getVrijemeSlanja() {
        return vrijemeSlanja;
    }

    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    public String getPredmet() {
        return predmet;
    }

    public String getSalje() {
        return salje;
    }

    public String getVrsta() {
        return vrsta;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

}
