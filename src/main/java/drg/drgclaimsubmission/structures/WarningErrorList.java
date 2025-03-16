/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
public class WarningErrorList {

    public WarningErrorList() {
    }
    private String code;
    private String errorcode;
    private String details;
}
