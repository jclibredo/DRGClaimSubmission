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
import drg.drgclaimsubmission.utilities.GrouperMethod;
import drg.drgclaimsubmission.utilities.Utility;
//import drg.drgclaimsubmission.utilities.dtd.DRGCLAIM;
//import drg.drgclaimsubmission.utilities.dtd.PROCEDURES;
//import drg.drgclaimsubmission.utilities.dtd.SECONDARYDIAG;
//import drg.drgclaimsubmission.utilities.dtd.SECONDARYDIAGS;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ValidateDRGClaims {

    public ValidateDRGClaims() {
    }

    private final Utility utility = new Utility();
    private final GrouperMethod gm = new GrouperMethod();
    private final ValidateSecondaryDiag VSD = new ValidateSecondaryDiag();
    private final ValidateProcedures VP = new ValidateProcedures();

    public DRGWSResult ValidateDRGClaims(final DataSource datasource, final DRGCLAIM drgclaim, final NClaimsData nclaimsdata) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        DRGCLAIM validatedrgclaim;
        ArrayList<String> errors = new ArrayList<>();
        ArrayList<String> errorsMessage = new ArrayList<>();
        //String[] discharges = {"1", "2", "3", "4", "5", "8", "9"};
        String[] dischargefromnclaims = {"I", "R", "H", "A", "T", "E", "O"};
        String[] gender = {"M", "F"};
        String PDx = drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase();
        DRGWSResult NewResult = gm.GetICD10(datasource, PDx);
        try {
            result.setMessage("");
            result.setResult("");
            result.setSuccess(false);
            //  DRGWSResult getvalidicd10 = gm.GetValidCodeICD10(datasource, PDx);
//-------------------------------------------------------------------------------
            //PRIMARY CODES VALIDATION
            if (drgclaim.getClaimNumber().equals("")) {
                //errors.add("ClaimSeries is required");
            } else if (drgclaim.getClaimNumber().length() > 20) {
                // errors.add("ClaimSeries, " + drgclaim.getPrimaryCode().trim() + ", should not be more than 20 lenght");
            }
            if (drgclaim.getPrimaryCode().equals("")) {
                //errors.add("PrimaryCode is required");
                errors.add("101");
            } else if (!NewResult.isSuccess()) {
                //errors.add("PrimaryCode , " + drgclaim.getPrimaryCode().trim() + ", not found");
                errors.add("201");
            }
            //END PRIMARY CODES VALIDATION

//  AGE VALIDATION AND GENDER
            if (nclaimsdata.getGender().trim().isEmpty()) {
                // errors.add("Patient sex is required");
                errors.add("102");
            } else if (!Arrays.asList(gender).contains(nclaimsdata.getGender().toUpperCase())) {
                // errors.add("Patient sex " + nclaimsdata.getGender() + " is not valid");
                errors.add("209");
            }
//----------------------------------------------------------------
            //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(nclaimsdata.getDateofBirth()) || !utility.IsValidDate(nclaimsdata.getAdmissionDate())) {
                } else {
                    if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) <= 0
                            && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 0) {
                        // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                        errors.add("219");
                    }
                }
            }
            if (!nclaimsdata.getAdmissionDate().isEmpty()
                    && !nclaimsdata.getDischargeDate().isEmpty()
                    && !nclaimsdata.getTimeDischarge().isEmpty()
                    && !nclaimsdata.getTimeAdmission().isEmpty()) {
                if (!utility.IsValidDate(nclaimsdata.getDateofBirth()) || !utility.IsValidDate(nclaimsdata.getAdmissionDate()) || !utility.IsValidDate(nclaimsdata.getDischargeDate())) {
                } else if (!utility.IsValidTime(nclaimsdata.getTimeAdmission()) || !utility.IsValidTime(nclaimsdata.getTimeDischarge())) {
                } else {
                    int oras = utility.ComputeTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                    int araw = utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
                    int minuto = utility.MinutesCompute(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
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
                }
            }
            //END LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
//---------------------------------------------------------------
            //REQUIRED DATE DATA NEEDED FOR GROUPER
            if (nclaimsdata.getAdmissionDate().isEmpty()) { //GET THE DATE OF ADMISSION
                //errors.add("AdmissionDate is required");
                errors.add("104");
            } else if (!utility.IsValidDate(nclaimsdata.getAdmissionDate())) {
                //errors.add("AdmissionDate " + nclaimsdata.getAdmissionDate().trim() + ",is Invalid");
                errors.add("214");
            }
            if (nclaimsdata.getDischargeDate().isEmpty()) { //GET THE DISCHARGE DATA
                //errors.add("DischargeDate is required");
                errors.add("106");
            } else if (!utility.IsValidDate(nclaimsdata.getDischargeDate())) {
                //errors.add("DischargeDate " + nclaimsdata.getDischargeDate().trim() + ",is Invalid");
                errors.add("216");
            }
            if (nclaimsdata.getDateofBirth().isEmpty()) { //GET THE DATE OF BIRTH
                //  errors.add("DateofBirth is required");
                errors.add("103");
            } else if (!utility.IsValidDate(nclaimsdata.getDateofBirth())) {
                //  errors.add("DateofBirth " + nclaimsdata.getDateofBirth().trim() + ",is Invalid");
                errors.add("213");
            }
            //END REQUIRED DATE DATA NEEDED FOR GROUPER
//---------------------------------------------------------------------------
            // GET THE TIME DATA REQUIRED FOR THE GROUPER
            if (nclaimsdata.getTimeAdmission().isEmpty()) { //GET THE DATE OF ADMISSION
                // errors.add("TimeAdmission is required");
                errors.add("105");
            } else if (!utility.IsValidTime(nclaimsdata.getTimeAdmission())) {
                // errors.add("TimeAdmission " + nclaimsdata.getTimeAdmission().trim() + ",is Invalid");
                errors.add("223");
            }
            if (nclaimsdata.getTimeDischarge().isEmpty()) { //GET THE DATE OF ADMISSION
                //errors.add("TimeDischarge is required : " + nclaimsdata.getTimeDischarge());
                errors.add("107");
            } else if (!utility.IsValidTime(nclaimsdata.getTimeDischarge())) {
                // errors.add("TimeDischarge " + nclaimsdata.getTimeDischarge().trim() + ",is Invalid");
                errors.add("224");
            }
            // END GET THE TIME DATA REQUIRED FOR THE GROUPER
//----------------------------------------------------------------------
            //VALIDATION FOR NEW BORN DATA
            if (nclaimsdata.getDischargeType().isEmpty()) {
                //errors.add("DischargeType is required");
                errors.add("108");
            } else if (!Arrays.asList(dischargefromnclaims).contains(nclaimsdata.getDischargeType())) {
                // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                errors.add("222");
            }
            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(nclaimsdata.getDateofBirth()) || !utility.IsValidDate(nclaimsdata.getAdmissionDate())) {
                } else {
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
                }
            }

            //END VALIDATION FOR NEW BORN DATA
            validatedrgclaim = drgclaim;
            if (!utility.IsValidDate(nclaimsdata.getDateofBirth()) || !utility.IsValidDate(nclaimsdata.getAdmissionDate())) {
            } else if (nclaimsdata.getGender().isEmpty() || !Arrays.asList(gender).contains(nclaimsdata.getGender().toUpperCase())) {
            } else {

                SECONDARYDIAGS secondarydiags = new SECONDARYDIAGS();
                for (int a = 0; a < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); a++) {
                    DRGWSResult VSDResultS = VSD.ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata);
                    SECONDARYDIAG secondarydiag = utility.objectMapper().readValue(VSDResultS.getResult(), SECONDARYDIAG.class);
                    secondarydiags.getSECONDARYDIAG().add(secondarydiag);
                    if (secondarydiag.getRemarks().equals("")) {
                        errorsMessage.add(VSDResultS.getMessage());
                    }
                }
                validatedrgclaim.setSECONDARYDIAGS(secondarydiags);
            }

            if (nclaimsdata.getGender().isEmpty() || !Arrays.asList(gender).contains(nclaimsdata.getGender().toUpperCase())) {
            } else {
                PROCEDURES procedures = new PROCEDURES();
                for (int b = 0; b < drgclaim.getPROCEDURES().getPROCEDURE().size(); b++) {
                    DRGWSResult VPResult = VP.ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender());
                    PROCEDURE procedure = utility.objectMapper().readValue(VPResult.getResult(), PROCEDURE.class);
                    procedures.getPROCEDURE().add(procedure);
                    if (procedure.getRemarks().equals("")) {
                        errorsMessage.add(VPResult.getMessage());
                    }
                }
                validatedrgclaim.setPROCEDURES(procedures);
            }
            if (errors.isEmpty()) {
                result.setSuccess(true);
            } else {
                errorsMessage.add("Patient information area has an errors");
                result.setMessage(errorsMessage.toString());
                validatedrgclaim.setRemarks(String.join(",", errors));
            }
            result.setResult(utility.objectMapper().writeValueAsString(validatedrgclaim));

        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateDRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
