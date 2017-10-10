
package org.foi.nwtis.robhalar.ws.klijenti;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for uredjaj complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="uredjaj"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoloc" type="{http://serveri.ws.robhalar.nwtis.foi.org/}lokacija" minOccurs="0"/&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="naziv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uredjaj", propOrder = {
    "geoloc",
    "id",
    "naziv"
})
public class Uredjaj {

    protected Lokacija geoloc;
    protected int id;
    protected String naziv;

    /**
     * Gets the value of the geoloc property.
     * 
     * @return
     *     possible object is
     *     {@link Lokacija }
     *     
     */
    public Lokacija getGeoloc() {
        return geoloc;
    }

    /**
     * Sets the value of the geoloc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Lokacija }
     *     
     */
    public void setGeoloc(Lokacija value) {
        this.geoloc = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the naziv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNaziv() {
        return naziv;
    }

    /**
     * Sets the value of the naziv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNaziv(String value) {
        this.naziv = value;
    }

}
