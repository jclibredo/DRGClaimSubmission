/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import java.util.ArrayList;
import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
public class KeyPerValueError {

    public KeyPerValueError() {
    }
    private String series;
    private String claimid;
    private String errors;
    private String warningerror;
    private String remarks;
    private ArrayList<String> drg;
    private ArrayList<String> drgname;

}
