/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import java.io.Serializable;

/**
 *
 * @author MinoSun
 */
public class ValidateXMLResult implements Serializable {

    public ValidateXMLResult() {

    }

    private boolean success;
    private String message;
    XMLErrors xmlerrors;
    UploadXMLResult uploadxmlresult;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public XMLErrors getXmlerrors() {
        return xmlerrors;
    }

    public void setXmlerrors(XMLErrors xmlerrors) {
        this.xmlerrors = xmlerrors;
    }

    public UploadXMLResult getUploadxmlresult() {
        return uploadxmlresult;
    }

    public void setUploadxmlresult(UploadXMLResult uploadxmlresult) {
        this.uploadxmlresult = uploadxmlresult;
    }

}
