//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.11.18 at 07:00:00 PM MEZ 
//


package com.catify.core.process.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Request complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Request">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.catify.com/api/1.0}ServiceNode">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.catify.com/api/1.0}outPipeline" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Request", propOrder = {
    "outPipeline"
})
public class Request
    extends ServiceNode
{

    protected OutPipeline outPipeline;

    /**
     * Gets the value of the outPipeline property.
     * 
     * @return
     *     possible object is
     *     {@link OutPipeline }
     *     
     */
    public OutPipeline getOutPipeline() {
        return outPipeline;
    }

    /**
     * Sets the value of the outPipeline property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutPipeline }
     *     
     */
    public void setOutPipeline(OutPipeline value) {
        this.outPipeline = value;
    }

}
