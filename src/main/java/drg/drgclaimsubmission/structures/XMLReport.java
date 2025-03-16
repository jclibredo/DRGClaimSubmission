/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

/**
 *
 * @author MINOSUN
 */
public class XMLReport {

    public XMLReport() {
    }

    private String accreno;
    private String reporttag;
    private String encryptedxml;
    private String userid;
    private String transmittalid;

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

    public String getEncryptedxml() {
        return encryptedxml;
    }

    public void setEncryptedxml(String encryptedxml) {
        this.encryptedxml = encryptedxml;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTransmittalid() {
        return transmittalid;
    }

    public void setTransmittalid(String transmittalid) {
        this.transmittalid = transmittalid;
    }

}
