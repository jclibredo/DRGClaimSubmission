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
public class DRGWSResult {
    private boolean success;
    private String message;
    private String result;
    
    
    
}


