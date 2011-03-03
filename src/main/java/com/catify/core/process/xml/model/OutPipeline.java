//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.03.03 at 02:15:23 PM MEZ 
//


package com.catify.core.process.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OutPipeline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OutPipeline">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.catify.com/api/1.0}toEndpoint"/>
 *         &lt;element ref="{http://www.catify.com/api/1.0}marshaller" minOccurs="0"/>
 *         &lt;element ref="{http://www.catify.com/api/1.0}correlation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutPipeline", propOrder = {
    "toEndpoint",
    "marshaller",
    "correlation"
})
public class OutPipeline {

    @XmlElement(required = true)
    protected ToEndpoint toEndpoint;
    protected Marshaller marshaller;
    protected Correlation correlation;

    /**
     * Gets the value of the toEndpoint property.
     * 
     * @return
     *     possible object is
     *     {@link ToEndpoint }
     *     
     */
    public ToEndpoint getToEndpoint() {
        return toEndpoint;
    }

    /**
     * Sets the value of the toEndpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link ToEndpoint }
     *     
     */
    public void setToEndpoint(ToEndpoint value) {
        this.toEndpoint = value;
    }

    /**
     * Gets the value of the marshaller property.
     * 
     * @return
     *     possible object is
     *     {@link Marshaller }
     *     
     */
    public Marshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the value of the marshaller property.
     * 
     * @param value
     *     allowed object is
     *     {@link Marshaller }
     *     
     */
    public void setMarshaller(Marshaller value) {
        this.marshaller = value;
    }

    /**
     * Gets the value of the correlation property.
     * 
     * @return
     *     possible object is
     *     {@link Correlation }
     *     
     */
    public Correlation getCorrelation() {
        return correlation;
    }

    /**
     * Sets the value of the correlation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Correlation }
     *     
     */
    public void setCorrelation(Correlation value) {
        this.correlation = value;
    }

}