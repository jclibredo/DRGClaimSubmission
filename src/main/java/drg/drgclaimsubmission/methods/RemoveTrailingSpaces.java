/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.utilities.Utility;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class RemoveTrailingSpaces {

    private final Utility utility = new Utility();

    public RemoveTrailingSpaces() {
    }

    public DRGWSResult RemoveTrailingSpaces(String output) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        int starttag = output.indexOf("<CF5");
        int endtag = output.indexOf("</CF5>");
        if ((starttag >= 0) && (endtag >= 0)) {
            result.setSuccess(true);
            output = output.substring(0, endtag + 6);
            result.setResult(output);
        } else if ((starttag >= 0) && (starttag < 0)) {
            result.setMessage("CF5 XML document structures must start and end within the same entity");
        } else if ((starttag < 0) && (endtag < 0)) {
            result.setMessage("Invalid XML");
        } else {
            result.setMessage("Unparsable CF5 XML File");
        }
        return result;
    }
}
