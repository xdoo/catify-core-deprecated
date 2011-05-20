//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.08 at 03:37:41 PM MESZ 
//


package com.catify.core.process.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ToEndpoint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ToEndpoint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://www.catify.com/api/1.0}endpoints"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ToEndpoint", propOrder = {
    "generic",
    "ftp",
    "rest",
    "file",
    "hazelcast"
})
public class ToEndpoint {

    protected GenericEndpoint generic;
    protected FtpEndpoint ftp;
    protected RestEndpoint rest;
    protected FileEndpoint file;
    protected HazelcastEndpoint hazelcast;

    /**
     * Gets the value of the generic property.
     * 
     * @return
     *     possible object is
     *     {@link GenericEndpoint }
     *     
     */
    public GenericEndpoint getGeneric() {
        return generic;
    }

    /**
     * Sets the value of the generic property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenericEndpoint }
     *     
     */
    public void setGeneric(GenericEndpoint value) {
        this.generic = value;
    }

    /**
     * Gets the value of the ftp property.
     * 
     * @return
     *     possible object is
     *     {@link FtpEndpoint }
     *     
     */
    public FtpEndpoint getFtp() {
        return ftp;
    }

    /**
     * Sets the value of the ftp property.
     * 
     * @param value
     *     allowed object is
     *     {@link FtpEndpoint }
     *     
     */
    public void setFtp(FtpEndpoint value) {
        this.ftp = value;
    }

    /**
     * Gets the value of the rest property.
     * 
     * @return
     *     possible object is
     *     {@link RestEndpoint }
     *     
     */
    public RestEndpoint getRest() {
        return rest;
    }

    /**
     * Sets the value of the rest property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestEndpoint }
     *     
     */
    public void setRest(RestEndpoint value) {
        this.rest = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link FileEndpoint }
     *     
     */
    public FileEndpoint getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileEndpoint }
     *     
     */
    public void setFile(FileEndpoint value) {
        this.file = value;
    }

    /**
     * Gets the value of the hazelcast property.
     * 
     * @return
     *     possible object is
     *     {@link HazelcastEndpoint }
     *     
     */
    public HazelcastEndpoint getHazelcast() {
        return hazelcast;
    }

    /**
     * Sets the value of the hazelcast property.
     * 
     * @param value
     *     allowed object is
     *     {@link HazelcastEndpoint }
     *     
     */
    public void setHazelcast(HazelcastEndpoint value) {
        this.hazelcast = value;
    }

}