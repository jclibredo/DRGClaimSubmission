/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import lombok.Data;

/**
 *
 * @author MinoSun
 */
@Data
public class UploadXMLResult {

    public UploadXMLResult() {

    }

    private String transactionno;
    private String userid;
    private String dateuploaded;
    private String errors;

}
