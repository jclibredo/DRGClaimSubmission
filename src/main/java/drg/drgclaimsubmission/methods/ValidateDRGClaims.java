/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.structures.dtd.PROCEDURES;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAG;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAGS;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class ValidateDRGClaims {

    public ValidateDRGClaims() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult ValidateDRGClaims(
            final DataSource datasource,
            final DRGCLAIM drgclaim,
            final NClaimsData nclaimsdata) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGCLAIM validatedrgclaim;
        ArrayList<String> errors = new ArrayList<>();
        ArrayList<String> errorsMessage = new ArrayList<>();
        DRGWSResult NewResult = new CF5Method().GetICD10(datasource, drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim());
        try {
            if (!NewResult.isSuccess()) {
                errors.add("411");
            } else if (!new CF5Method().GetICD10PreMDC(datasource, drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim()).isSuccess()) {
                errors.add("201");
            }
            if (drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim().isEmpty()) {
                // error.add("CF5 Err. code 101 PrimaryCode is required");
                errors.add("101");
            }
            //END PRIMARY CODES VALIDATION
            if (nclaimsdata.getGender().trim().isEmpty()) {
                errors.add("102");
            } else if (!Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                errors.add("209");
            }
            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                String days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                String year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                int finalDays = 0;
                if (Integer.parseInt(year) > 0) {
                    finalDays = Integer.parseInt(year) * 365;
                } else {
                    finalDays = Integer.parseInt(days);
                }
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) > 124) {
                    errors.add("416");
                }
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                        && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0) {
                    if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                && utility.ComputeDay(nclaimsdata.getDateofBirth(),
                                        nclaimsdata.getAdmissionDate()) >= 0 && !drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim().isEmpty()) {
                            if (NewResult.isSuccess()) {
                                DRGWSResult icd10preMDC = new CF5Method().GetICD10PreMDC(datasource, drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim());
                                if (icd10preMDC.isSuccess()) {
                                    //CHECKING FOR AGE CONFLICT
                                    DRGWSResult getAgeConfictResult = new CF5Method().AgeConfictValidation(datasource, drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim(), String.valueOf(finalDays), year);
                                    if (!getAgeConfictResult.isSuccess()) {
                                        errors.add("414");
                                    }
                                    //  AGE VALIDATION AND GENDER
                                    if (!nclaimsdata.getGender().trim().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                                        //CHECKING FOR GENDER CONFLICT
                                        DRGWSResult getSexConfictResult = new CF5Method().GenderConfictValidation(datasource, drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase().trim(), nclaimsdata.getGender());
                                        if (!getSexConfictResult.isSuccess()) {
                                            errors.add("415");
                                        }
                                    }
                                } else {
                                    errors.add("201");
                                }
                            }
                        }
                    }
                }
            }
            if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) <= 0
                    && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 0) {
                // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                errors.add("219");
            }
            int oras = utility.ComputeTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
            int araw = utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
//                    int minuto = utility.MinutesCompute(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
            int taon = utility.ComputeYear(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
            if (utility.ComputeLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge()) == 0) {
                if (araw <= 0 && oras < 0) {
                    //errors.add("AdmissionTime Greater than DischargeTime not valid in same date");
                    errors.add("220");
                }
            } else if (taon <= 0 && araw < 0) {
                // errors.add("DischargeDat Must be greater than or equal to AdmissionDate");
                errors.add("221");
            }
            if (utility.ComputeLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge()) < 24) {
                errors.add("417");
            }
            if (nclaimsdata.getDischargeType().isEmpty()) {
                //errors.add("DischargeType is required");
                errors.add("108");
            } else if (!Arrays.asList("I", "R", "H", "A", "T", "E", "O").contains(nclaimsdata.getDischargeType())) {
                // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                errors.add("226");
            }
            if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) == 0
                    && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 28) {
                if (!drgclaim.getNewBornAdmWeight().equals("")) {
                    if (!utility.isValidNumeric(drgclaim.getNewBornAdmWeight())) {
                        // errors.add("NewBordAdmWeight  value , " + drgclaim.getNewBornAdmWeight() + ", is non-numeric value");
                        errors.add("227");
                    } else if (Double.parseDouble(drgclaim.getNewBornAdmWeight()) < 0.3) {
                        //errors.add("NewBordAdmWeight value , " + drgclaim.getNewBornAdmWeight() + ", not meet the minimum requirements 0.3 Kilograms");
                        errors.add("228");
                    }
                } else {
                    // errors.add("NewBordAdmWeight is required");
                    errors.add("109");
                }
            }
            //END VALIDATION FOR NEW BORN DATA
            validatedrgclaim = drgclaim;
            validatedrgclaim.setRemarks(String.join(",", errors));
            if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                SECONDARYDIAGS secondarydiags = new SECONDARYDIAGS();
                for (int a = 0; a < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); a++) {
                    // sdx validation
                    DRGWSResult VSDResultS = new ValidateSecondaryDiag().ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata);
                    //mapping
                    if (VSDResultS.isSuccess()) {
                        SECONDARYDIAG secondarydiag = utility.objectMapper().readValue(VSDResultS.getResult(), SECONDARYDIAG.class);
                        secondarydiags.getSECONDARYDIAG().add(secondarydiag);
                        if (secondarydiag.getRemarks().equals("")) {
                            errorsMessage.add(VSDResultS.getMessage());
                        }
                    }
                }
                validatedrgclaim.setSECONDARYDIAGS(secondarydiags);
            }
            if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                PROCEDURES procedures = new PROCEDURES();
                for (int b = 0; b < drgclaim.getPROCEDURES().getPROCEDURE().size(); b++) {
                    DRGWSResult VPResult = new ValidateProcedures().ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender());
                    if (VPResult.isSuccess()) {
                        PROCEDURE procedure = utility.objectMapper().readValue(VPResult.getResult(), PROCEDURE.class);
                        procedures.getPROCEDURE().add(procedure);
                        if (procedure.getRemarks().equals("")) {
                            errorsMessage.add(VPResult.getMessage());
                        }
                    }
                }
                validatedrgclaim.setPROCEDURES(procedures);
            }
            // RETURN RESULT
            if (!errors.isEmpty()) {
                errorsMessage.add("Patient information area has an errors");
                result.setMessage(errorsMessage.toString());
            }
            result.setResult(utility.objectMapper().writeValueAsString(validatedrgclaim));
            result.setSuccess(true);
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateDRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
