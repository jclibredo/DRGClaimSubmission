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
@XmlRootElement(name = "SECONDARYDIAG")
public class SECONDARYDIAG {

    public SECONDARYDIAG() {
    }

    @XmlAttribute(name = "SecondaryCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String SecondaryCode;

    @XmlAttribute(name = "Remarks", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String Remarks;

    public String getSecondaryCode() {
        return SecondaryCode;
    }

    public void setSecondaryCode(String SecondaryCode) {
        this.SecondaryCode = SecondaryCode;
    }
    
    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String Remarks) {
        this.Remarks = Remarks;
    }

}
