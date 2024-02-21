/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures.dtd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author MinoSun
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "secondarydiag"
})
@XmlRootElement(name = "SECONDARYDIAGS")
public class SECONDARYDIAGS {
 
    
     @XmlElement(name = "SECONDARYDIAG", required = true)
    protected List<SECONDARYDIAG> secondarydiag;
     
     
    public List<SECONDARYDIAG> getSECONDARYDIAG() {
        if (secondarydiag == null) {
            secondarydiag = new ArrayList<>();
        }
        return this.secondarydiag;
    } 
     
}
