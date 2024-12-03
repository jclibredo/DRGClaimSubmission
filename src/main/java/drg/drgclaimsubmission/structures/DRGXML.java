/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import java.io.Serializable;

/**
 *
 * @author DRG_SHADOWBILLING
 */

public class DRGXML implements Serializable {

    public DRGXML() {
    }
    private String accreno;
    private String reporttag;
    private EncryptedXML encryptedxml;
    private String userid;

    public String getAccreno() {
        return accreno;
    }

    public void setAccreno(String accreno) {
        this.accreno = accreno;
    }

    public String getReporttag() {
        return reporttag;
    }

    public void setReporttag(String reporttag) {
        this.reporttag = reporttag;
    }

    public EncryptedXML getEncryptedxml() {
        return encryptedxml;
    }

    public void setEncryptedxml(EncryptedXML encryptedxml) {
        this.encryptedxml = encryptedxml;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
    
    
    

}
