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
 * <p>Java class for Start complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Start">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.catify.com/api/1.0}Node">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.catify.com/api/1.0}inPipeline" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Start", propOrder = {
    "inPipeline"
})
public class Start
    extends Node
{

    protected InPipeline inPipeline;

    /**
     * Gets the value of the inPipeline property.
     * 
     * @return
     *     possible object is
     *     {@link InPipeline }
     *     
     */
    public InPipeline getInPipeline() {
        return inPipeline;
    }

    /**
     * Sets the value of the inPipeline property.
     * 
     * @param value
     *     allowed object is
     *     {@link InPipeline }
     *     
     */
    public void setInPipeline(InPipeline value) {
        this.inPipeline = value;
    }

}