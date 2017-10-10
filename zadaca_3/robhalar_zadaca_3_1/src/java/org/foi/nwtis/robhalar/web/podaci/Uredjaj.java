package org.foi.nwtis.robhalar.web.podaci;

public class Uredjaj {
    private int id;
    private String naziv;
    private Lokacija geoloc;

    public Uredjaj() {
    }

    public Uredjaj(int id, String naziv, Lokacija geoloc) {
        this.id = id;
        this.naziv = naziv;
        this.geoloc = geoloc;
    }

    public Lokacija getGeoloc() {
        return geoloc;
    }

    public void setGeoloc(Lokacija geoloc) {
        this.geoloc = geoloc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }        
}
