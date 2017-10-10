
package org.foi.nwtis.robhalar.ws.klijenti;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dodajUredjaj complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dodajUredjaj"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uredjaj" type="{http://serveri.ws.robhalar.nwtis.foi.org/}uredjaj" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dodajUredjaj", propOrder = {
    "uredjaj"
})
public class DodajUredjaj {

    protected Uredjaj uredjaj;

    /**
     * Gets the value of the uredjaj property.
     * 
     * @return
     *     possible object is
     *     {@link Uredjaj }
     *     
     */
    public Uredjaj getUredjaj() {
        return uredjaj;
    }

    /**
     * Sets the value of the uredjaj property.
     * 
     * @param value
     *     allowed object is
     *     {@link Uredjaj }
     *     
     */
    public void setUredjaj(Uredjaj value) {
        this.uredjaj = value;
    }

}
