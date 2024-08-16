/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.seekermethod;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.GrouperParameter;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.methods.CF5Method;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
public class DataArrangement {

    private final Utility utility = new Utility();
    private final CF5Method gm = new CF5Method();
    private final DataProcess dataprocess = new DataProcess();
    // GrouperParameter newGrouperParam = utility.GrouperParameter();

    public DRGWSResult DataArrangement(final DataSource datasource,
            final GrouperParameter grouperparam) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> errors = new ArrayList<>();
        ArrayList<String> warningerror = new ArrayList<>();

        String[] dischargefromnclaims = {"1", "2", "3", "4", "5", "8", "9"};
        String[] gender = {"M", "F"};
        String PDx = grouperparam.getPdx().replaceAll("\\.", "").toUpperCase();
        DRGWSResult NewResult = gm.GetICD10(datasource, PDx);
        List<String> ProcedureList = Arrays.asList(grouperparam.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparam.getSdx().split(","));
        ArrayList<String> SecondaryData = new ArrayList<>();
        ArrayList<String> ProcedureData = new ArrayList<>();
        KeyPerValueError viewerrors = utility.KeyPerValueError();
        try {

            if (PDx.equals("")) {
                //errors.add("PrimaryCode is required");
                errors.add("101");
            } else if (String.valueOf(NewResult.isSuccess()).equals("false")) {
                //errors.add("PrimaryCode , " + drgclaim.getPrimaryCode().trim() + ", not found");
                errors.add("201");
            }
            //END PRIMARY CODES VALIDATION

//  AGE VALIDATION AND GENDER
            if (grouperparam.getGender().trim().isEmpty()) {
                // errors.add("Patient sex is required");
                errors.add("102");
            } else if (!Arrays.asList(gender).contains(grouperparam.getGender().toUpperCase())) {
                // errors.add("Patient sex " + nclaimsdata.getGender() + " is not valid");
                errors.add("209");
            }
//----------------------------------------------------------------
            //LOS VALIDATION MUST NOT BE LESS THAN 6 HOURS
            if (!grouperparam.getBirthDate().isEmpty() && !grouperparam.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(grouperparam.getBirthDate()) || !utility.IsValidDate(grouperparam.getAdmissionDate())) {
                } else {
                    if (utility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) <= 0
                            && utility.ComputeDay(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) < 0) {
                        // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                        errors.add("219");
                    }
                }
            }
            if (!grouperparam.getAdmissionDate().isEmpty()
                    && !grouperparam.getDischargeDate().isEmpty()
                    && !grouperparam.getTimeDischarge().isEmpty()
                    && !grouperparam.getTimeAdmission().isEmpty()) {
                if (!utility.IsValidDate(grouperparam.getBirthDate()) || !utility.IsValidDate(grouperparam.getAdmissionDate()) || !utility.IsValidDate(grouperparam.getDischargeDate())) {
                } else if (!utility.IsValidTime(grouperparam.getTimeAdmission()) || !utility.IsValidTime(grouperparam.getTimeDischarge())) {
                } else {
                    int oras = utility.ComputeTime(grouperparam.getAdmissionDate(), grouperparam.getTimeAdmission(), grouperparam.getDischargeDate(), grouperparam.getTimeDischarge());
                    int araw = utility.ComputeDay(grouperparam.getAdmissionDate(), grouperparam.getDischargeDate());
                    int minuto = utility.MinutesCompute(grouperparam.getAdmissionDate(), grouperparam.getTimeAdmission(), grouperparam.getDischargeDate(), grouperparam.getTimeDischarge());
                    int taon = utility.ComputeYear(grouperparam.getAdmissionDate(), grouperparam.getDischargeDate());

                    if (utility.ComputeLOS(grouperparam.getAdmissionDate(), grouperparam.getTimeAdmission(), grouperparam.getDischargeDate(), grouperparam.getTimeDischarge()) == 0) {
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
            if (grouperparam.getAdmissionDate().isEmpty()) { //GET THE DATE OF ADMISSION
                //errors.add("AdmissionDate is required");
                errors.add("104");
            } else if (!utility.IsValidDate(grouperparam.getAdmissionDate())) {
                //errors.add("AdmissionDate " + nclaimsdata.getAdmissionDate().trim() + ",is Invalid");
                errors.add("214");
            }
            if (grouperparam.getDischargeDate().isEmpty()) { //GET THE DISCHARGE DATA
                //errors.add("DischargeDate is required");
                errors.add("106");
            } else if (!utility.IsValidDate(grouperparam.getDischargeDate())) {
                //errors.add("DischargeDate " + nclaimsdata.getDischargeDate().trim() + ",is Invalid");
                errors.add("216");
            }
            if (grouperparam.getBirthDate().isEmpty()) { //GET THE DATE OF BIRTH
                //  errors.add("DateofBirth is required");
                errors.add("103");
            } else if (!utility.IsValidDate(grouperparam.getBirthDate())) {
                //  errors.add("DateofBirth " + nclaimsdata.getDateofBirth().trim() + ",is Invalid");
                errors.add("213");
            }
            //END REQUIRED DATE DATA NEEDED FOR GROUPER
//---------------------------------------------------------------------------
            // GET THE TIME DATA REQUIRED FOR THE GROUPER
            if (grouperparam.getTimeAdmission().isEmpty()) { //GET THE DATE OF ADMISSION
                // errors.add("TimeAdmission is required");
                errors.add("105");
            } else if (!utility.IsValidTime(grouperparam.getTimeAdmission())) {
                // errors.add("TimeAdmission " + nclaimsdata.getTimeAdmission().trim() + ",is Invalid");
                errors.add("223");
            }
            if (grouperparam.getTimeDischarge().isEmpty()) { //GET THE DATE OF ADMISSION
                //errors.add("TimeDischarge is required : " + nclaimsdata.getTimeDischarge());
                errors.add("107");
            } else if (!utility.IsValidTime(grouperparam.getTimeDischarge())) {
                // errors.add("TimeDischarge " + nclaimsdata.getTimeDischarge().trim() + ",is Invalid");
                errors.add("224");
            }
            // END GET THE TIME DATA REQUIRED FOR THE GROUPER
//----------------------------------------------------------------------
            //VALIDATION FOR NEW BORN DATA
            if (grouperparam.getDischargeType().isEmpty()) {
                //errors.add("DischargeType is required");
                errors.add("108");
            } else if (!Arrays.asList(dischargefromnclaims).contains(grouperparam.getDischargeType())) {
                // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                errors.add("222");
            }
            if (!grouperparam.getBirthDate().isEmpty() && !grouperparam.getAdmissionDate().isEmpty()) {
                if (!utility.IsValidDate(grouperparam.getBirthDate()) || !utility.IsValidDate(grouperparam.getAdmissionDate())) {
                } else {
                    if (utility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) == 0
                            && utility.ComputeDay(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) < 28) {
                        if (!grouperparam.getAdmissionWeight().equals("")) {
                            if (!utility.isValidNumeric(grouperparam.getAdmissionWeight())) {
                                errors.add("227");
                            } else if (Double.parseDouble(grouperparam.getAdmissionWeight()) < 0.3) {
                                errors.add("228");
                            }
                        } else {
                            errors.add("109");
                        }
                    }
                }
            }

            if (!grouperparam.getGender().isEmpty() && Arrays.asList(gender).contains(grouperparam.getGender().toUpperCase())) {
                if (!grouperparam.getProc().isEmpty()) {
                    for (int proc = 0; proc < ProcedureList.size(); proc++) {
                        try (Connection connection = datasource.getConnection()) {
                            String ProcCode = ProcedureList.get(proc).replaceAll("\\+", " +");
                            String[] splirproc = ProcCode.split(",");
                            for (int a = 0; a < splirproc.length; a++) {
                                String[] seconds = splirproc[a].split(" +");
                                for (int b = 0; b < seconds.length; b++) {
                                    if (b == 0) {
                                        String rvs_code = seconds[b].trim().replaceAll("\\s", "");
                                        DRGWSResult checkRVStoICD9cm = gm.CheckICD9cm(datasource, rvs_code);
                                        if (!checkRVStoICD9cm.isSuccess()) {
                                            int gendercounter = 0;
                                            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                                            statement.registerOutParameter("converter", OracleTypes.CURSOR);
                                            statement.setString("rvs_code", rvs_code);
                                            statement.execute();
                                            ResultSet resultset = (ResultSet) statement.getObject("converter");
                                            if (resultset.next()) {
                                                String ProcList = resultset.getString("ICD9CODE");
                                                if (!ProcList.trim().isEmpty()) {
                                                    List<String> ConverterResult = Arrays.asList(ProcList.trim().split(","));
                                                    for (int ptr = 0; ptr < ConverterResult.size(); ptr++) {
                                                        String ICD9Codes = ConverterResult.get(ptr);
                                                        //int datafound = gm.CountProc(datasource, ICD9Codes.trim());
                                                        if (gm.CountProc(datasource, ICD9Codes.trim()).isSuccess()) {
                                                            DRGWSResult sexvalidationresult = gm.GenderConfictValidationProc(datasource, ICD9Codes.trim(), grouperparam.getGender());
                                                            if (!sexvalidationresult.isSuccess()) {
                                                                gendercounter++;
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    warningerror.add("203");
                                                }
                                            } else {
                                                // if (!rvs_code.isEmpty()) {
                                                warningerror.add("203");
                                                // }
                                            }
                                            if (gendercounter > 0) {
                                                warningerror.add("507");
                                            }
                                        }

                                        String ext = seconds[b + 1].trim().replaceAll("\\+", "");
                                        if (ext.trim().isEmpty()) {
                                            ProcedureData.add(rvs_code + "+11");
                                        } else {
                                            ProcedureData.add(rvs_code + "+" + ext.replaceAll("\\s", ""));
                                        }

                                    }
                                }
                            }

                        } catch (SQLException ex) {
                            result.setMessage(ex.toString());
                            Logger.getLogger(DataArrangement.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }

            }

            for (int sdx = 0; sdx < SecondaryList.size(); sdx++) {
                String SDxCode = SecondaryList.get(sdx);
                if (SDxCode.length() > 10) {
                    warningerror.add("501");
                } else if (SDxCode.equals(PDx)) {
                    warningerror.add("502");
                } else {
                    if (!grouperparam.getBirthDate().isEmpty() && !grouperparam.getAdmissionDate().isEmpty()) {
                        String days = String.valueOf(utility.ComputeDay(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()));
                        String year = String.valueOf(utility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()));
                        if (utility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) >= 0
                                && utility.ComputeDay(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) >= 0) {
                            if (!grouperparam.getBirthDate().isEmpty() && !grouperparam.getAdmissionDate().isEmpty()) {
                                if (utility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()) >= 0
                                        && utility.ComputeDay(grouperparam.getBirthDate(),
                                                grouperparam.getAdmissionDate()) >= 0 && !SDxCode.isEmpty()) {
                                    DRGWSResult SDxResult = gm.GetICD10(datasource, SDxCode);
                                    if (SDxResult.isSuccess()) {
                                        //CHECKING FOR AGE CONFLICT
                                        DRGWSResult getAgeConfictResult = gm.AgeConfictValidation(datasource, SDxCode, days, year);
                                        if (!getAgeConfictResult.isSuccess()) {
                                            warningerror.add("504");
                                        }
                                        //CHECKING FOR GENDER CONFLICT
                                        DRGWSResult getSexConfictResult = gm.GenderConfictValidation(datasource, SDxCode, grouperparam.getGender());
                                        if (!getSexConfictResult.isSuccess()) {
                                            warningerror.add("505");
                                        }
                                    } else {
                                        warningerror.add("501");
                                    }
                                }
                            }
                        }
                    }
                }
                SecondaryData.add(SDxCode);
            }

            ArrayList<String> duplproc = new ArrayList<>();
            ArrayList<String> duplsdx = new ArrayList<>();

            for (int i = 0; i < SecondaryData.size() - 1; i++) {
                for (int j = i + 1; j < SecondaryData.size(); j++) {
                    if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                        warningerror.add("503");
                        duplsdx.add(String.valueOf(j));
                    }
                }
            }
            //==================================================================
            for (int i = 0; i < ProcedureData.size() - 1; i++) {
                for (int j = i + 1; j < ProcedureData.size(); j++) {
                    if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                        // warningerror.add(" RVS : " + ProcedureData.get(j) + " has duplicate");
                        warningerror.add("508");
                        duplproc.add(String.valueOf(j));
                    }
                }
            }

            ArrayList<String> drgcodelist = new ArrayList<>();
            ArrayList<String> drgnamelist = new ArrayList<>();
            DRGWSResult grouperresult = dataprocess.DataProcess(datasource, grouperparam, String.join(",", duplproc), String.join(",", duplsdx), errors, ProcedureData);
            drgcodelist.add(grouperresult.getResult());
            drgnamelist.add(grouperresult.getMessage());
            viewerrors.setRemarks("");
            viewerrors.setSeries("");
            viewerrors.setClaimid("");
            viewerrors.setDrg(drgcodelist);
            viewerrors.setDrgname(drgnamelist);
            viewerrors.setErrors(String.join(",", errors));
            viewerrors.setWarningerror(String.join(",", warningerror));
            result.setResult(utility.objectMapper().writeValueAsString(viewerrors));

            String errorss = String.join(",", errors.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim();
            //  String warningss = String.join(",", viewerrors.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim();
            if (errorss.length() > 0) {
                result.setMessage("CLAIM DATA HAS AN ERROR");
                result.setSuccess(false);
            } else if (!viewerrors.getWarningerror().isEmpty()) {
                result.setSuccess(true);
                result.setMessage("CLAIM DATA HAS WARNING ERROR");
            } else {
                result.setMessage("CLAIM DATA IS CLEAN");
                result.setSuccess(true);
            }
        } catch (NumberFormatException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(DataArrangement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
