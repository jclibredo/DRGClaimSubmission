/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.seekermethod;

import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
public class SeekerResult {

    private String message;
    private boolean success;
    private String warning;
    private String info;
    private String dxdiag;
    private String procedure;
}
