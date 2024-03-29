//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.10.13 at 06:05:33 PM MESZ 
//


package com.catify.core.process.xml.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SplitNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SplitNode">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.catify.com/api/1.0}Node">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.catify.com/api/1.0}timeEvent" minOccurs="0"/>
 *         &lt;element ref="{http://www.catify.com/api/1.0}line" maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SplitNode", propOrder = {
    "timeEvent",
    "line"
})
@XmlSeeAlso({
    Fork.class,
    Decision.class
})
public class SplitNode
    extends Node
{

    protected TimeEvent timeEvent;
    @XmlElement(required = true)
    protected List<Line> line;

    /**
     * Gets the value of the timeEvent property.
     * 
     * @return
     *     possible object is
     *     {@link TimeEvent }
     *     
     */
    public TimeEvent getTimeEvent() {
        return timeEvent;
    }

    /**
     * Sets the value of the timeEvent property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeEvent }
     *     
     */
    public void setTimeEvent(TimeEvent value) {
        this.timeEvent = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the line property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLine().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Line }
     * 
     * 
     */
    public List<Line> getLine() {
        if (line == null) {
            line = new ArrayList<Line>();
        }
        return this.line;
    }

}
