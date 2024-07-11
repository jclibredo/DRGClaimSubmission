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
    "drgclaim"
})

@XmlRootElement(name = "CF5")
public class CF5 {

    public CF5() {
    }

    @XmlAttribute(name = "pHospitalCode", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String pHospitalCode;
    @XmlElement(name = "DRGCLAIM", required = true)
    protected DRGCLAIM drgclaim;

    public String getpHospitalCode() {
        return pHospitalCode;
    }
    public void setpHospitalCode(String pHospitalCode) {
        this.pHospitalCode = pHospitalCode;
    }
    public DRGCLAIM getDrgclaim() {
        return drgclaim;
    }
    public void setDrgclaim(DRGCLAIM drgclaim) {
        this.drgclaim = drgclaim;
    }

    

    

}
