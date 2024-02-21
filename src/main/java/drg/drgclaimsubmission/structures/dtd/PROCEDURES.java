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
    "procedure"
})
@XmlRootElement(name = "PROCEDURES")

public class PROCEDURES {
    
      @XmlElement(name = "PROCEDURE", required = true)
    protected List<PROCEDURE> procedure;
     
     
    public List<PROCEDURE> getPROCEDURE() {
        if (procedure == null) {
            procedure = new ArrayList<>();
        }
        return this.procedure;
    } 
       
    
}
