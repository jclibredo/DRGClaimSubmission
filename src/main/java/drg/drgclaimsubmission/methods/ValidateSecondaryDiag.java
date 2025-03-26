/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAG;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ValidateSecondaryDiag {

    public ValidateSecondaryDiag() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ValidateSecondaryDiag(final DataSource datasource, final SECONDARYDIAG secondarydiag, final String pdxS, final NClaimsData nclaimsdata) {
        DRGWSResult result = utility.DRGWSResult();
        SECONDARYDIAG validatesecondiag;
        ArrayList<String> errors = new ArrayList<>();
        try {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            if (!utility.CleanCode(secondarydiag.getSecondaryCode()).trim().equals("")) {
                if (utility.CleanCode(secondarydiag.getSecondaryCode()).trim().length() > 10) {
                    errors.add("501");
                } else if (utility.CleanCode(secondarydiag.getSecondaryCode()).trim().equals(utility.CleanCode(pdxS).trim())) {
                    errors.add("502");
                } else {
                    if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                        String days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                        String year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                        int finalDays = 0;
                        if (Integer.parseInt(year) > 0) {
                            finalDays = Integer.parseInt(year) * 365;
                        } else {
                            finalDays = Integer.parseInt(days);
                        }
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0) {
                            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                        && utility.ComputeDay(nclaimsdata.getDateofBirth(),
                                                nclaimsdata.getAdmissionDate()) >= 0 && !utility.CleanCode(secondarydiag.getSecondaryCode()).isEmpty()) {
                                    DRGWSResult SDxResult = new CF5Method().GetICD10(datasource, utility.CleanCode(secondarydiag.getSecondaryCode()).trim());
                                    if (SDxResult.isSuccess()) {
                                        DRGWSResult icd10preMDC = new CF5Method().GetICD10PreMDC(datasource, utility.CleanCode(secondarydiag.getSecondaryCode()).trim());
                                        if (icd10preMDC.isSuccess()) {
                                            //CHECKING FOR AGE CONFLICT
                                            DRGWSResult getAgeConfictResult = new CF5Method().AgeConfictValidation(datasource, utility.CleanCode(secondarydiag.getSecondaryCode()).trim(), String.valueOf(finalDays), year);
                                            if (!getAgeConfictResult.isSuccess()) {
                                                errors.add("504");
                                            }
                                            //CHECKING FOR GENDER CONFLICT
                                            DRGWSResult getSexConfictResult = new CF5Method().GenderConfictValidation(datasource, utility.CleanCode(secondarydiag.getSecondaryCode()).trim(), nclaimsdata.getGender());
                                            if (!getSexConfictResult.isSuccess()) {
                                                errors.add("505");
                                            }
                                        } else {
                                            errors.add("501");
                                        }
                                    } else {
                                        errors.add("501");
                                    }
                                }
                            }
                        }
                    }
                }

            }
            validatesecondiag = secondarydiag;
            if (!errors.isEmpty()) {
                result.setMessage("Secondary Diagnosis has an error");
                validatesecondiag.setRemarks(String.join(",", errors));
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(validatesecondiag));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateSecondaryDiag.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
