package org.foi.nwtis.robhalar.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.foi.nwtis.robhalar.web.Izbornik;

@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable
{
    private final static ArrayList<Izbornik> izbornikJezika = new ArrayList<>();
    private String odabraniJezikIsoAlpha2;
    
    static
    {
        izbornikJezika.add(new Izbornik("hrvatski", "hr"));
        izbornikJezika.add(new Izbornik("english", "en"));
        izbornikJezika.add(new Izbornik("deutsch", "de"));
    }

    public Lokalizacija()
    {
    }
    
    @PostConstruct
    public void init()
    {
        Locale currentLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        odabraniJezikIsoAlpha2 = currentLocale.getLanguage();
    }
    
    public String getOdabraniJezikIsoAlpha2()
    {
        return odabraniJezikIsoAlpha2;
    }

    public void setOdabraniJezikIsoAlpha2(String odabraniJezik)
    {
        Locale newLocale = new Locale(odabraniJezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(newLocale);
        this.odabraniJezikIsoAlpha2 = odabraniJezik;
    }

    public ArrayList<Izbornik> getIzbornikJezika()
    {
        return izbornikJezika;
    }

    public Object odaberiJezik()
    {
        return "promjenaJezika";
    }
    
    public String saljiPoruku()
    {
        return "saljiPoruku";
    }
    
    public String pregledPoruka()
    {
        return "pregledPoruka";
    }
}
