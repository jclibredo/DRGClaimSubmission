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
public class ValidICD10 {

    public ValidICD10() {
    }

    private String validcode;
    private String description;
    private String code;

}
