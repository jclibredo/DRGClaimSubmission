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
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try {
            SECONDARYDIAG validatesecondiag;
            ArrayList<String> errors = new ArrayList<>();
            if (!secondarydiag.getSecondaryCode().trim().equals("")) {
//                if (secondarydiag.getSecondaryCode().trim().length() > 10) {
//                    errors.add("501");
//                } else 
                if (secondarydiag.getSecondaryCode().trim().equals(pdxS.trim())) {
                    errors.add("502");
                } else {
                    if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                        int finalDays = 0;
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) > 0) {
                            finalDays = utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) * 365;
                        } else {
                            finalDays = utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate());
                        }
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0) {
                            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                        && utility.ComputeDay(nclaimsdata.getDateofBirth(),
                                                nclaimsdata.getAdmissionDate()) >= 0 && !secondarydiag.getSecondaryCode().isEmpty()) {
                                    if (new CF5Method().GetICD10(datasource, secondarydiag.getSecondaryCode().trim()).isSuccess()) {
                                        if (new CF5Method().GetICD10PreMDC(datasource, secondarydiag.getSecondaryCode().trim()).isSuccess()) {
                                            //CHECKING FOR AGE CONFLICT
                                            if (!new CF5Method().AgeConfictValidation(datasource, secondarydiag.getSecondaryCode().trim(), String.valueOf(finalDays),
                                                    String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()))).isSuccess()) {
                                                errors.add("504");
                                            }
                                            //CHECKING FOR GENDER CONFLICT
                                            if (!new CF5Method().GenderConfictValidation(datasource, secondarydiag.getSecondaryCode().trim(), nclaimsdata.getGender()).isSuccess()) {
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
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateSecondaryDiag.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
