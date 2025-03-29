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
public class NClaimsData {

    public NClaimsData() {
    }
    private String hospitalcode;
    private String series;
    private String pclaimnumber;
    private String dateofBirth;
    private String admissionDate;
    private String dischargeDate;
    private String gender;
    private String dischargeType;
    private String expiredDate;
    private String expiredTime;
    private String timeAdmission;
    private String timeDischarge;
}
