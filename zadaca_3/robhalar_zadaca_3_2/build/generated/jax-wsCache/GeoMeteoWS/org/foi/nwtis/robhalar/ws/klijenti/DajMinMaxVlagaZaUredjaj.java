
package org.foi.nwtis.robhalar.ws.klijenti;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dajMinMaxVlagaZaUredjaj complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dajMinMaxVlagaZaUredjaj"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="to" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dajMinMaxVlagaZaUredjaj", propOrder = {
    "id",
    "from",
    "to"
})
public class DajMinMaxVlagaZaUredjaj {

    protected int id;
    protected long from;
    protected long to;

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
     * Gets the value of the from property.
     * 
     */
    public long getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     */
    public void setFrom(long value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     */
    public long getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     */
    public void setTo(long value) {
        this.to = value;
    }

}
