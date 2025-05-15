/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods.frontvalidation;

import drg.drgclaimsubmission.methods.CF5Method;
import drg.drgclaimsubmission.methods.ValidateProcedures;
import drg.drgclaimsubmission.methods.ValidateSecondaryDiag;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.structures.dtd.PROCEDURES;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAG;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAGS;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class FrontValidation {

    public FrontValidation() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ParseEClaimsDrgXML(
            final DataSource datasource,
            final CF5 drg,
            final List<NClaimsData> nclaimsdatalist,
            final List<String> listid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> error = new ArrayList<>();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        try {
            CF5 drgs = new CF5();
            if (!drg.getPHospitalCode().isEmpty()) {
                if (!drg.getPHospitalCode().trim().equals(nclaimsdatalist.get(0).getHospitalcode().trim())) {
                    // error.add("CF5 " + drg.getPHospitalCode() + " pHospitalCode code not found in eclaims xml");
                    error.add("509");// ERROR
                }
            }
            if (drg.getPHospitalCode().isEmpty()) {
                //error.add("CF5 pHospitalCode Code is required");
                error.add("303");// ERROR
            }
            if (drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                // error.add("CF5 ClaimNumber is required");
                error.add("511"); // ERROR
            }

            if (!drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                if (!listid.contains(drg.getDRGCLAIM().getClaimNumber().trim())) {
                    // error.add("CF5 ClaimNumber: " + drg.getDRGCLAIM().getClaimNumber().trim() + " not found in EClaims XML");
                    error.add("222"); // ERROR
                }
            }

            //Validate Eclaims XML
            for (int x = 0; x < nclaimsdatalist.size(); x++) {
                if (!drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                    if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nclaimsdatalist.get(x).getPclaimnumber().trim())) {
                        if (!nclaimsdatalist.get(x).getTimeDischarge().trim().isEmpty()) {
                            if (utility.IsSURGEValidTime(nclaimsdatalist.get(x).getTimeDischarge())
                                    || utility.IsAITMDValidTime(nclaimsdatalist.get(x).getTimeDischarge())
                                    || utility.IsBITMDValidTime(nclaimsdatalist.get(x).getTimeDischarge())) {
                            } else {
                                error.add("516");
                            }
                        }
                        //------------------------------------------------------
                        if (!nclaimsdatalist.get(x).getTimeAdmission().trim().isEmpty()) {
                            if (utility.IsSURGEValidTime(nclaimsdatalist.get(x).getTimeAdmission())
                                    || utility.IsAITMDValidTime(nclaimsdatalist.get(x).getTimeAdmission())
                                    || utility.IsBITMDValidTime(nclaimsdatalist.get(x).getTimeAdmission())) {
                            } else {
                                error.add("515");
                            }
                        }
                        //------------------------------------------------------
                        if (!nclaimsdatalist.get(x).getAdmissionDate().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getAdmissionDate().trim())) {
                                error.add("517");
                            }
                        }
                        if (!nclaimsdatalist.get(x).getDischargeDate().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getDischargeDate().trim())) {
                                error.add("518");
                            }
                        }
                        if (nclaimsdatalist.get(x).getTimeDischarge().isEmpty()) {
                            error.add("107");
                        }
                        if (nclaimsdatalist.get(x).getTimeAdmission().isEmpty()) {
                            error.add("105");
                        }
                        if (nclaimsdatalist.get(x).getAdmissionDate().isEmpty()) {
                            error.add("104");
                        }
                        if (nclaimsdatalist.get(x).getDischargeDate().isEmpty()) {
                            error.add("106");
                        }
                        if (nclaimsdatalist.get(x).getDateofBirth().isEmpty()) {
                            error.add("103");
                        }
                        if (!nclaimsdatalist.get(x).getDateofBirth().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getDateofBirth().trim())) {
                                error.add("403");
                            }
                        }
                        break;
                    }
                }
            }
            if (error.size() > 0) {
                KeyPerValueError viewerrors = utility.KeyPerValueError();
                viewerrors.setErrors(String.join(",", error));
                viewerrors.setSeries("");
                viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber() + "");
                viewerrors.setWarningerror("");
                viewerrors.setRemarks("Major Error");
                allErrorList.add(viewerrors);
            }
            //VALIDATE HCF SECTOR
            if (error.isEmpty()) {
                if (new CF5Method().ValidateHcfSector(datasource, drg.getPHospitalCode()).isSuccess()) {
                    for (int y = 0; y < nclaimsdatalist.size(); y++) {
                        if (drg.getPHospitalCode().trim().equals(nclaimsdatalist.get(0).getHospitalcode().trim())) {
                            if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nclaimsdatalist.get(y).getPclaimnumber().trim())) {
                                DRGCLAIM drgclaims = new DRGCLAIM();
                                ArrayList<String> SecondaryData = new ArrayList<>();
                                ArrayList<String> ProcedureData = new ArrayList<>();
                                KeyPerValueError viewerror = utility.KeyPerValueError();
                                ArrayList<String> errorlist = new ArrayList<>();
                                ArrayList<String> warningerror = new ArrayList<>();
                                //KEY VALUE PAIR VALIDATION
//                                    DRGWSResult vprodResult = this.ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nclaimsdatalist.get(y));
                                //KEY VALUE PAIR VALIDATION
                                DRGCLAIM drgclaim = utility.objectMapper().readValue(this.ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nclaimsdatalist.get(y)).getResult(), DRGCLAIM.class);
                                //drgs.add(drgclaim);
                                drgclaims.setClaimNumber(drgclaim.getClaimNumber());
                                drgclaims.setPrimaryCode(drgclaim.getPrimaryCode());
                                drgclaims.setNewBornAdmWeight(drgclaim.getNewBornAdmWeight());
                                drgclaims.setRemarks(drgclaim.getRemarks());
                                drgclaims.setPROCEDURES(drgclaim.getPROCEDURES());
                                drgclaims.setSECONDARYDIAGS(drgclaim.getSECONDARYDIAGS());
                                //-----------------------------------------------------
                                if (!drgclaims.getRemarks().isEmpty()) {
                                    errorlist.add(drgclaims.getRemarks());
                                }
                                for (int proc = 0; proc < drgclaims.getPROCEDURES().getPROCEDURE().size(); proc++) {
                                    if (drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().isEmpty()
                                            || drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().equals("")) {
                                    } else {
                                        if (!drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks().isEmpty()) {
                                            warningerror.add(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks());
                                        }
                                        ProcAssign procassign = new ProcAssign();
                                        if (drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt1().isEmpty()) {
                                            procassign.setEx1("1");
                                        } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim())) {
                                            procassign.setEx1("1");
                                        } else {
                                            procassign.setEx1(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt1());
                                        }
                                        if (drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt2().isEmpty()) {
                                            procassign.setEx2("1");
                                        } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim())) {
                                            procassign.setEx2("1");
                                        } else {
                                            procassign.setEx2(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt2());
                                        }
                                        if (drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().isEmpty()) {
                                            procassign.setLat("N");
                                        } else if (!Arrays.asList("L", "R", "B", "N").contains(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim())) {
                                            procassign.setLat("N");
                                        } else {
                                            procassign.setLat(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getLaterality());
                                        }
                                        ProcedureData.add(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim() + "+" + procassign.getLat().trim() + "" + procassign.getEx1().trim() + "" + procassign.getEx2().trim());
                                        //validate extension code
                                    }
                                }

                                for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                                    if (drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode().isEmpty()
                                            || drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode().equals("")) {
                                    } else {
                                        if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                                            warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                                        }
                                        SecondaryData.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode().trim());
                                    }
                                }

                                ArrayList<String> duplsdx = new ArrayList<>();
                                for (int i = 0; i < SecondaryData.size() - 1; i++) {
                                    for (int j = i + 1; j < SecondaryData.size(); j++) {
                                        if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                            warningerror.add("503");
                                            duplsdx.add(String.valueOf(j));
                                        }
                                    }
                                }
                                ArrayList<String> duplproc = new ArrayList<>();
                                for (int i = 0; i < ProcedureData.size() - 1; i++) {
                                    for (int j = i + 1; j < ProcedureData.size(); j++) {
                                        if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                                            warningerror.add("507");
                                            duplproc.add(String.valueOf(j));
                                            break;
                                        }
                                    }
                                }
                                drgs.setDRGCLAIM(drgclaim);
                                ArrayList<String> drgcodelist = new ArrayList<>();
                                ArrayList<String> drgnamelist = new ArrayList<>();
                                viewerror.setWarningerror(String.join(",", warningerror));
                                viewerror.setErrors(String.join(",", errorlist));
                                viewerror.setClaimid(drg.getDRGCLAIM().getClaimNumber().trim());
                                viewerror.setDrg(drgcodelist);
                                viewerror.setDrgname(drgnamelist);
                                allErrorList.add(viewerror);
                                break;
                            }
                        }
                    }
                    ArrayList<String> errorlist = new ArrayList<>();
                    ArrayList<String> warninglist = new ArrayList<>();
                    for (int x = 0; x < allErrorList.size(); x++) {
                        errorlist.add(allErrorList.get(x).getErrors());
                        warninglist.add(allErrorList.get(x).getWarningerror());
                    }
                    if (error.size() > 0) {
                        if (new CF5Method().ValidateHcfSector(datasource, drg.getPHospitalCode()).getResult().trim().toUpperCase().equals("P")) {
                            result.setMessage("CF5 DATA ENCOUNTER ERROR EXPECT THAT GROUPING LOGIC CAN'T BE PROCEED");
                        } else {
                            result.setSuccess(true);
                            result.setMessage("PUBLIC FACILITY");
                        }
                    } else if (String.join(",", errorlist.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim().length() > 0) {
                        result.setSuccess(true);
                        result.setMessage("CF5 DATA HAS AN ERROR EXPECT UNGROUPABLE DRG CODES RESULT");
                    } else if (String.join(",", warninglist.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim().length() > 0) {
                        result.setSuccess(true);
                        result.setMessage("CF5 DATA HAS WARNING ERROR EXPECT THAT SOME DATA WILL NOT BE CONSIDERED IN GROUPING LOGIC");
                    } else {
                        result.setMessage("CF5 DATA IS CLEAN");
                        result.setSuccess(true);
                    }
                    result.setResult(utility.objectMapper().writeValueAsString(allErrorList));
                } else {
                    result.setMessage(new CF5Method().ValidateHcfSector(datasource, drg.getPHospitalCode()).getMessage());
                }
            } else {
                result.setResult(utility.objectMapper().writeValueAsString(allErrorList));
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FrontValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
//        DRGWSResult NewResult = new CF5Method().GetICD10(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim());
//token.substring(0, token.length() - 1)
        try {
            if (!new CF5Method().GetICD10(datasource, drgclaim.getPrimaryCode().trim()).isSuccess()) {
                errors.add("411");
            } else if (!new CF5Method().GetICD10PreMDC(datasource, drgclaim.getPrimaryCode().trim()).isSuccess()) {
                errors.add("201");
            }
            if (drgclaim.getPrimaryCode().trim().isEmpty()) {
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
                int days = utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate());
                int year = utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate());
                int finalDays = 0;
                if (year > 0) {
                    finalDays = year * 365;
                } else {
                    finalDays = days;
                }
                //--------------------------------------------------------------
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) > 124) {
                    errors.add("416");
                }
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                        && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0) {
                    if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                && utility.ComputeDay(nclaimsdata.getDateofBirth(),
                                        nclaimsdata.getAdmissionDate()) >= 0 && !drgclaim.getPrimaryCode().trim().isEmpty()) {
                            if (new CF5Method().GetICD10(datasource, drgclaim.getPrimaryCode().trim()).isSuccess()) {
                                if (new CF5Method().GetICD10PreMDC(datasource, drgclaim.getPrimaryCode().trim()).isSuccess()) {
                                    //CHECKING FOR AGE CONFLICT
                                    if (!new CF5Method().AgeConfictValidation(datasource, drgclaim.getPrimaryCode().trim(), String.valueOf(finalDays), String.valueOf(year)).isSuccess()) {
                                        errors.add("414");
                                    }
                                    //  AGE VALIDATION AND GENDER
                                    if (!nclaimsdata.getGender().trim().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                                        //CHECKING FOR GENDER CONFLICT
                                        if (!new CF5Method().GenderConfictValidation(datasource, drgclaim.getPrimaryCode().trim(), nclaimsdata.getGender()).isSuccess()) {
                                            errors.add("415");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 1
                    && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 0) {
                // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                errors.add("219");
            }

            int los = 0;
            if (utility.IsSURGEValidTime(nclaimsdata.getTimeAdmission()) && utility.IsSURGEValidTime(nclaimsdata.getTimeDischarge())) {
                los = utility.ComputeSURGELOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
//                oras = utility.ComputeSURGETime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
            } else if (utility.IsAITMDValidTime(nclaimsdata.getTimeAdmission()) && utility.IsAITMDValidTime(nclaimsdata.getTimeDischarge())) {
//                oras = utility.ComputeITMDTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                los = utility.ComputeITMDLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
            } else if (utility.IsBITMDValidTime(nclaimsdata.getTimeAdmission()) && utility.IsBITMDValidTime(nclaimsdata.getTimeDischarge())) {
//                oras = utility.ComputeITMDTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                los = utility.ComputeITMDLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
            }
            if (los == 0) {
                if (utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate()) < 1) {
                    //errors.add("AdmissionTime Greater than DischargeTime not valid in same date");
                    if (utility.TimeDif(nclaimsdata.getTimeAdmission(), nclaimsdata.getTimeDischarge())) {
                        errors.add("220");
                    }
                }
            }
            if (utility.ComputeYear(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate()) < 1
                    && utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate()) < 0) {
                // errors.add("DischargeDat Must be greater than or equal to AdmissionDate");
                errors.add("221");
            }
            //VALIDATE PATIENT LOS
            if (utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate()) < 1) {
                if (los < 24) {
                    errors.add("417");
                }
            }
            //VALIDATE DISCHARGE TYPE
            if (nclaimsdata.getDischargeType().isEmpty()) {
                //errors.add("DischargeType is required");
                errors.add("108");
            } else if (!Arrays.asList("I", "R", "H", "A", "T", "E", "O").contains(nclaimsdata.getDischargeType())) {
                // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                errors.add("226");
            }
            if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) == 0
                    && utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate()) < 28) {
                int finalDays = 0;
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) > 0) {
                    finalDays = utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) * 365;
                } else {
                    finalDays = utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
                }
                DRGWSResult getAgeConfictResult = new CF5Method().AgeConfictValidation(datasource, drgclaim.getPrimaryCode().trim(), String.valueOf(finalDays), String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate())));
                if (getAgeConfictResult.isSuccess()) {
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

            //END VALIDATION FOR NEW BORN DATA
            validatedrgclaim = drgclaim;
            validatedrgclaim.setRemarks(String.join(",", errors));
            if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                SECONDARYDIAGS secondarydiags = new SECONDARYDIAGS();
                for (int a = 0; a < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); a++) {
                    if (new ValidateSecondaryDiag().ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata).isSuccess()) {
                        SECONDARYDIAG secondarydiag = utility.objectMapper().readValue(new ValidateSecondaryDiag().ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata).getResult(), SECONDARYDIAG.class
                        );
                        secondarydiags.getSECONDARYDIAG().add(secondarydiag);
                        if (secondarydiag.getRemarks().equals("")) {
                            errorsMessage.add(new ValidateSecondaryDiag().ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata).getMessage());
                        }
                    }
                }
                validatedrgclaim.setSECONDARYDIAGS(secondarydiags);
            }
            if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                PROCEDURES procedures = new PROCEDURES();
                for (int b = 0; b < drgclaim.getPROCEDURES().getPROCEDURE().size(); b++) {
                    if (new ValidateProcedures().ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender()).isSuccess()) {
                        PROCEDURE procedure = utility.objectMapper().readValue(new ValidateProcedures().ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender()).getResult(), PROCEDURE.class
                        );
                        procedures.getPROCEDURE().add(procedure);
                        if (procedure.getRemarks().equals("")) {
                            errorsMessage.add(new ValidateProcedures().ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender()).getMessage());
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
            result.setMessage("Something went wrong");
            Logger.getLogger(FrontValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public class ProcAssign {

        private String lat;
        private String ex1;
        private String ex2;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getEx1() {
            return ex1;
        }

        public void setEx1(String ex1) {
            this.ex1 = ex1;
        }

        public String getEx2() {
            return ex2;
        }

        public void setEx2(String ex2) {
            this.ex2 = ex2;
        }

    }

}
