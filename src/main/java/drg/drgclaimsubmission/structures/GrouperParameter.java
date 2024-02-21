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
public class GrouperParameter {

    public GrouperParameter() {
    }
    private String claimseries;
    private String result_id;
    private String Pdx;
    private String Sdx;
    private String Proc;
    private String BirthDate;
    private String AdmissionDate;
    private String DischargeDate;
    private String Gender;
    private String TimeAdmission;
    private String TimeDischarge;
    private String DischargeType;
    private String ExpireTime;
    private String ExpiredDate;
    private String TimeOfBirth;
    private String AdmissionWeight;
    private String idseries;

}
