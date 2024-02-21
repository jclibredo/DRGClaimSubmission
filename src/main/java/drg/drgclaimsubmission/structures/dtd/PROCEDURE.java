/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures.dtd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author MinoSun
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "PROCEDURE")
public class PROCEDURE {

    @XmlAttribute(name = "RvsCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String RvsCode;

    @XmlAttribute(name = "Laterality", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Laterality;

    @XmlAttribute(name = "Ext1", required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Ext1;

    @XmlAttribute(name = "Ext2", required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Ext2;

    @XmlAttribute(name = "Remarks", required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Remarks;

    public String getRvsCode() {
        return RvsCode;
    }

    public void setRvsCode(String RvsCode) {
        this.RvsCode = RvsCode;
    }

    public String getLaterality() {
        return Laterality;
    }

    public void setLaterality(String Laterality) {
        this.Laterality = Laterality;
    }

    public String getExt1() {
        return Ext1;
    }

    public void setExt1(String Ext1) {
        this.Ext1 = Ext1;
    }

    public String getExt2() {
        return Ext2;
    }

    public void setExt2(String Ext2) {
        this.Ext2 = Ext2;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String Remarks) {
        this.Remarks = Remarks;
    }

}
