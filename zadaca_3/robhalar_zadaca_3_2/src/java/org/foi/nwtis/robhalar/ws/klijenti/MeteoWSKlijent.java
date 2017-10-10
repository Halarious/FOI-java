package org.foi.nwtis.robhalar.ws.klijenti;

public class MeteoWSKlijent
{

    public static java.util.List<org.foi.nwtis.robhalar.ws.klijenti.Uredjaj> dajSveUredjaje()
    {
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service service = new org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service();
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSveUredjaje();
    }

    public static Boolean dodajUredjajPremaAdresi(java.lang.String naziv, java.lang.String adresa)
    {
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service service = new org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service();
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dodajUredjajPremaAdresi(naziv, adresa);
    }

    public static java.util.List<org.foi.nwtis.robhalar.ws.klijenti.MeteoPodaci> dajSveMeteoPodatkeZaUredjaj(int id, long from, long to)
    {
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service service = new org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS_Service();
        org.foi.nwtis.robhalar.ws.klijenti.GeoMeteoWS port = service.getGeoMeteoWSPort();
        return port.dajSveMeteoPodatkeZaUredjaj(id, from, to);
    }
    
    
}
