/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.EncryptedXML;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ValidateEncryptedXML {

    public ValidateEncryptedXML() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult ValidateEncryptedXML(String xml, String key) {
        DRGWSResult result = utility.DRGWSResult();

        try {
            EncryptedXML encxml = new EncryptedXML();
            encxml.setDoc("");
            encxml.setDocMimeType("");
            encxml.setHash("");
            encxml.setIv("");
            encxml.setKey1("");
            encxml.setKey2("");
            result.setResult(utility.objectMapper().writeValueAsString(encxml));
        } catch (IOException ex) {
            Logger.getLogger(ValidateEncryptedXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

}
