package org.foi.nwtis.robhalar.web;

public class Izbornik
{
    private final String labela;
    private final String vrijednost;

    public Izbornik(String labela, String vrijednost)
    {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }

    public String getLabela()
    {
        return labela;
    }

    public String getVrijednost()
    {
        return vrijednost;
    }
    
    
}
