//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.01 at 03:58:04 PM IST 
//


package com.twc.fatcaone.notification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;Component xmlns="urn:fatca:fatcanotificationbase" xmlns:xmime="http://www.w3.org/2005/05/xmlmime" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;DictionaryEntryNm&gt;Record Identifier Type&lt;/DictionaryEntryNm&gt;&lt;MajorVersionNum&gt;1&lt;/MajorVersionNum&gt;&lt;MinorVersionNum&gt;0&lt;/MinorVersionNum&gt;&lt;VersionEffectiveBeginDt&gt;2014-08-12&lt;/VersionEffectiveBeginDt&gt;&lt;VersionDescriptionTxt&gt;Initial Version&lt;/VersionDescriptionTxt&gt;&lt;DescriptionTxt&gt;Details of Record Identifier&lt;/DescriptionTxt&gt;
 * 				&lt;/Component&gt;
 * </pre>
 * 
 * 			
 * 
 * <p>Java class for RecordIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecordIdentifierType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:fatca:fatcanotificationbase}PaperRecordInd"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}PaperRecordRefId"/>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}AttachedPaperSubmissionFileNm" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}MessageRefId"/>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}DocRefId"/>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}CorrMessageRefId" minOccurs="0"/>
 *             &lt;element ref="{urn:fatca:fatcanotificationbase}CorrDocRefId" minOccurs="0"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecordIdentifierType", propOrder = {
    "paperRecordInd",
    "paperRecordRefId",
    "attachedPaperSubmissionFileNm",
    "messageRefId",
    "docRefId",
    "corrMessageRefId",
    "corrDocRefId"
})
public class RecordIdentifierType {

    @XmlElement(name = "PaperRecordInd")
    protected boolean paperRecordInd;
    @XmlElement(name = "PaperRecordRefId")
    protected String paperRecordRefId;
    @XmlElement(name = "AttachedPaperSubmissionFileNm")
    protected String attachedPaperSubmissionFileNm;
    @XmlElement(name = "MessageRefId")
    protected String messageRefId;
    @XmlElement(name = "DocRefId")
    protected String docRefId;
    @XmlElement(name = "CorrMessageRefId")
    protected String corrMessageRefId;
    @XmlElement(name = "CorrDocRefId")
    protected String corrDocRefId;

    /**
     * Gets the value of the paperRecordInd property.
     * 
     */
    public boolean isPaperRecordInd() {
        return paperRecordInd;
    }

    /**
     * Sets the value of the paperRecordInd property.
     * 
     */
    public void setPaperRecordInd(boolean value) {
        this.paperRecordInd = value;
    }

    /**
     * Gets the value of the paperRecordRefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaperRecordRefId() {
        return paperRecordRefId;
    }

    /**
     * Sets the value of the paperRecordRefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaperRecordRefId(String value) {
        this.paperRecordRefId = value;
    }

    /**
     * Gets the value of the attachedPaperSubmissionFileNm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachedPaperSubmissionFileNm() {
        return attachedPaperSubmissionFileNm;
    }

    /**
     * Sets the value of the attachedPaperSubmissionFileNm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachedPaperSubmissionFileNm(String value) {
        this.attachedPaperSubmissionFileNm = value;
    }

    /**
     * Gets the value of the messageRefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageRefId() {
        return messageRefId;
    }

    /**
     * Sets the value of the messageRefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageRefId(String value) {
        this.messageRefId = value;
    }

    /**
     * Gets the value of the docRefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocRefId() {
        return docRefId;
    }

    /**
     * Sets the value of the docRefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocRefId(String value) {
        this.docRefId = value;
    }

    /**
     * Gets the value of the corrMessageRefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrMessageRefId() {
        return corrMessageRefId;
    }

    /**
     * Sets the value of the corrMessageRefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrMessageRefId(String value) {
        this.corrMessageRefId = value;
    }

    /**
     * Gets the value of the corrDocRefId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorrDocRefId() {
        return corrDocRefId;
    }

    /**
     * Sets the value of the corrDocRefId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorrDocRefId(String value) {
        this.corrDocRefId = value;
    }

}
