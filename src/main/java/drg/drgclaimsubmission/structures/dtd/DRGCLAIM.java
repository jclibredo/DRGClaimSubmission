/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures.dtd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author MinoSun
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "secondarydiags",
    "procedures"
})
@XmlRootElement(name = "DRGCLAIM")
public class DRGCLAIM {

    public DRGCLAIM() {
    }

    @XmlAttribute(name = "PrimaryCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String PrimaryCode;

    @XmlAttribute(name = "NewBornAdmWeight", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String NewBornAdmWeight;

    @XmlAttribute(name = "Remarks", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Remarks;

    @XmlAttribute(name = "ClaimNumber", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String ClaimNumber;

    @XmlElement(name = "SECONDARYDIAGS", required = true)
    protected SECONDARYDIAGS secondarydiags;

    @XmlElement(name = "PROCEDURES", required = true)
    protected PROCEDURES procedures;

    public String getPrimaryCode() {
        return PrimaryCode;
    }

    public void setPrimaryCode(String PrimaryCode) {
        this.PrimaryCode = PrimaryCode;
    }

    public String getNewBornAdmWeight() {
        return NewBornAdmWeight;
    }

    public void setNewBornAdmWeight(String NewBornAdmWeight) {
        this.NewBornAdmWeight = NewBornAdmWeight;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String Remarks) {
        this.Remarks = Remarks;
    }

    public String getClaimNumber() {
        return ClaimNumber;
    }

    public void setClaimNumber(String ClaimNumber) {
        this.ClaimNumber = ClaimNumber;
    }
    public SECONDARYDIAGS getSECONDARYDIAGS() {
        return secondarydiags;
    }

    public void setSECONDARYDIAGS(SECONDARYDIAGS value) {
        this.secondarydiags = value;
    }

    public PROCEDURES getPROCEDURES() {
        return procedures;
    }

    public void setPROCEDURES(PROCEDURES value) {
        this.procedures = value;
    }

}
