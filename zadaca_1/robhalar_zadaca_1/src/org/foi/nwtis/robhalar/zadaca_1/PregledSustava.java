package org.foi.nwtis.robhalar.zadaca_1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.robhalar.konfiguracije.KonfiguracijaBIN;

public class PregledSustava
{
   String filepath;
    
   public PregledSustava(String[] arguments)
   {
       this.filepath = arguments[0];
   }
   
   public void start()
    {
        File file = new File("evidencija.bin");
        OutputStream outStream;
        try
        {
            outStream = new FileOutputStream(file);
            ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream);
            objOutputStream.writeObject(Evidencija.getInstance());
        } 
        catch (IOException ex)
        {
            Logger.getLogger(KonfiguracijaBIN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
