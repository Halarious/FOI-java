package org.foi.nwtis.robhalar.web.zrna;

import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import org.foi.nwtis.robhalar.rest.klijenti.MeteoKlijent;
import org.foi.nwtis.robhalar.ws.klijenti.MeteoPodaci;
import org.foi.nwtis.robhalar.ws.klijenti.MeteoWSKlijent;
import org.foi.nwtis.robhalar.ws.klijenti.Uredjaj;

@Named(value = "odabirUredjaja")
@RequestScoped
public class OdabirUredjaja
{
    private List<MeteoPodaci> meteoPodaci;
    private List<Uredjaj> uredjaji;
    private List<String> ids;
    
    private String naziv;
    private String adresa;
    
    private long from;
    private long to;
    
    public OdabirUredjaja()
    {
    }

    public List<MeteoPodaci> getMeteoPodaci()
    {
        return meteoPodaci;
    }

    public void setMeteoPodaci(List<MeteoPodaci> meteoPodaci)
    {
        this.meteoPodaci = meteoPodaci;
    }

    public List<Uredjaj> getUredjaji()
    {
        uredjaji = MeteoWSKlijent.dajSveUredjaje();
        return uredjaji;
    }

    public void setUredjaji(List<Uredjaj> uredjaji)
    {
        this.uredjaji = uredjaji;
    }

    public List<String> getIds()
    {
        return ids;
    }

    public void setIds(List<String> ids)
    {
        this.ids = ids;
    }

    public String getNaziv()
    {
        return naziv;
    }

    public void setNaziv(String naziv)
    {
        this.naziv = naziv;
    }

    public String getAdresa()
    {
        return adresa;
    }

    public void setAdresa(String adresa)
    {
        this.adresa = adresa;
    }

    public long getFrom()
    {
        return from;
    }

    public void setFrom(long from)
    {
        this.from = from;
    }

    public long getTo()
    {
        return to;
    }

    public void setTo(long to)
    {
        this.to = to;
    }
    
    public String upisiSoap()
    {
        boolean success = MeteoWSKlijent.dodajUredjajPremaAdresi(naziv, adresa);

        return "upisiSoap";
    }
    
    public String upisiRest()
    {
        MeteoKlijent meteoKlijent = new MeteoKlijent();
        boolean success = 
                meteoKlijent.dodajUredjaj(naziv, adresa);
        
        return "upisiRest";
    }
    
    public String preuzmiSoap()
    {
        int id = 0;
        if(ids != null && !ids.isEmpty())
            id = Integer.parseInt(ids.get(0));
        meteoPodaci = MeteoWSKlijent.dajSveMeteoPodatkeZaUredjaj(id, from, to);
        
        return "preuzmiSoap";
    }
    
    public String preuzmiRest()
    {
        MeteoKlijent meteoKlijent = new MeteoKlijent();
        meteoPodaci = meteoKlijent.getRestVazeciMeteoPodaci(ids);
        
        return "preuzmiRest";
    }
}
