/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.GrouperParameter;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.WarningErrorList;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class AccessGrouperFrontValidation {

    public AccessGrouperFrontValidation() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult AccessGrouperFrontValidation(final DRGCLAIM drgclaim,
            final DataSource datasource,
            final NClaimsData nclaimsdata,
            final String duplicateproc,
            final String duplcatesdx, List<String> errors) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        String[] laterality = {"L", "R", "B"};
        String[] exten = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        ArrayList<WarningErrorList> warningerrorlist = new ArrayList<>();
        GrouperParameter grouperparameterlist = utility.GrouperParameter();
        //===============================================PACKAGE CODE=======================================
        try (Connection connection = datasource.getConnection()) {
            PROCEDURE procedure;
            connection.setAutoCommit(false);
            ArrayList<String> drgCode = new ArrayList<>();
            ArrayList<String> drgName = new ArrayList<>();
            if (errors.size() > 0) {
                for (int x = 0; x < errors.size(); x++) {
                    List<String> errorlist = Arrays.asList(errors.get(x).split(","));
                    for (int h = 0; h < errorlist.size(); h++) {
                        switch (Integer.parseInt(errorlist.get(h))) {
                            case 101:
                                //Principal diagnosis is required
                                drgCode.add("26509");
                                drgName.add("Primary diagnosis is required");
                                break;
                            case 201:
                                //errors.add("PrimaryCode , " + drgclaim.getPrimaryCode().trim() + ", not found");
                                drgCode.add("26509");
                                drgName.add("Primary diagnosis is not a valid ICD-10 code");
                                break;
                            case 102:
                                // errors.add("Patient sex is required");
                                drgCode.add("26509");
                                drgName.add("Patient sex is required");
                                break;
                            case 209:
                                // errors.add("Patient sex " + nclaimsdata.getGender() + " is not valid");
                                drgCode.add("26509");
                                drgName.add("Patient sex is not valid");
                                break;
                            case 219:
                                // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                                drgCode.add("26539");
                                drgName.add("Invalid age");
                                break;
                            case 220:
                                //errors.add("AdmissionTime Greater than DischargeTime not valid in same date");
                                drgCode.add("26549");
                                drgName.add("Invalid LOS");
                                break;
                            case 221:
                                // errors.add("DischargeDat Must be greater than or equal to AdmissionDate");
                                drgCode.add("26549");
                                drgName.add("Invalid LOS");
                                break;
                            case 104:
                                //errors.add("AdmissionDate is required");
                                drgCode.add("26549");
                                drgName.add("Invalid LOS");
                                break;
                            case 214:
                                //errors.add("AdmissionDate " + nclaimsdata.getAdmissionDate().trim() + ",is Invalid");
                                drgCode.add("26549");
                                drgName.add("Invalid LOS");
                                break;
                            case 106:
                                //errors.add("DischargeDate is required");
                                drgCode.add("26549");
                                drgName.add("DischargeDate is required");
                                break;
                            case 216:
                                //errors.add("DischargeDate " + nclaimsdata.getDischargeDate().trim() + ",is Invalid");
                                drgCode.add("26549");
                                drgName.add("nvalid LOS");
                                break;
                            case 103:
                                //  errors.add("DateofBirth is required");
                                drgCode.add("26539");
                                drgName.add("nvalid Age");
                                break;
                            case 213:
                                //  errors.add("DateofBirth " + nclaimsdata.getDateofBirth().trim() + ",is Invalid");
                                drgCode.add("26509");
                                drgName.add("Invalid Age");
                                break;
                            case 105:
                                // errors.add("TimeAdmission is required");
                                drgCode.add("26509");
                                drgName.add("Invalid LOS");
                                break;
                            case 223:
                                // errors.add("TimeAdmission " + nclaimsdata.getTimeAdmission().trim() + ",is Invalid");
                                drgCode.add("26509");
                                drgName.add("Invalid LOS");
                                break;
                            case 107:
                                //errors.add("TimeDischarge is required : " + nclaimsdata.getTimeDischarge());
                                drgCode.add("26509");
                                drgName.add("Invalid LOS");
                                break;
                            case 224:
                                // errors.add("TimeDischarge " + nclaimsdata.getTimeDischarge().trim() + ",is Invalid");
                                drgCode.add("26509");
                                drgName.add("Invalid LOS");
                                break;
                            case 108:
                                //errors.add("DischargeType is required");
                                drgCode.add("26509");
                                drgName.add("Disposition is empty");
                                break;
                            case 222:
                                // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                                drgCode.add("26509");
                                drgName.add("Disposition is not valid");
                                break;
                            case 227:
                                // errors.add("NewBordAdmWeight  value , " + drgclaim.getNewBornAdmWeight() + ", is non-numeric value");
                                drgCode.add("26509");
                                drgName.add("PAdmission Weight is not valid");
                                break;
                            case 228:
                                // errors.add("NewBordAdmWeight value , " + drgclaim.getNewBornAdmWeight() + ", not meet the minimum requirements 0.3 Kilograms");
                                drgCode.add("26509");
                                drgName.add("Admission Weight less than 0.3 kg is not valid");
                                break;
                            case 109:
                                // errors.add("NewBordAdmWeight is required");
                                drgCode.add("26509");
                                drgName.add("Admission Weight is empty");
                                break;
                        }
                    }
                    result.setMessage(drgName.toString());
                    result.setResult(drgCode.toString());
                    result.setSuccess(false);
                }
            } else {
                String days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                String year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                //======================================================
                //Process Procedure
                ArrayList<String> procedurejoin = new ArrayList<>();
                //Process Secondary Diagnosis
                ArrayList<String> secondaryjoin = new ArrayList<>();
                //=======================================================================================  
                //SECONDARY DIAGNOSIS INSERTION OF DATA TO DATABASE
                for (int second = 0; second < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); second++) {
                    WarningErrorList warningerror = utility.WarningErrorList();
                    String datas = drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode();
                    DRGWSResult SDxResult = new CF5Method().GetICD10(datasource, utility.CleanCode(datas).trim());
                    DRGWSResult gendervalidation = new CF5Method().GenderConfictValidation(datasource, utility.CleanCode(datas).trim(), nclaimsdata.getGender());
                    DRGWSResult agevalidation = new CF5Method().AgeConfictValidation(datasource, utility.CleanCode(datas).trim(), days, year);
                    if (datas.length() != 0) {
                        int indexvalue = duplcatesdx.indexOf(String.valueOf(second));
                        if (indexvalue >= 0) {
                            warningerror.setCode(datas);
                            warningerror.setErrorcode("503");
                            warningerror.setDetails("SDx is the repetition with other SDx");
                            warningerrorlist.add(warningerror);
                        } else {
                            if (utility.CleanCode(datas).trim().equals(utility.CleanCode(drgclaim.getPrimaryCode()).trim())) {
                                warningerror.setCode(datas);
                                warningerror.setErrorcode("502");
                                warningerror.setDetails("Case that SDx is the repetition with PDx");
                                warningerrorlist.add(warningerror);
                            } else if (!SDxResult.isSuccess()) {
                                warningerror.setCode(datas);
                                warningerror.setErrorcode("501");
                                warningerror.setDetails("SDx Invalid Code");
                                warningerrorlist.add(warningerror);
                            } else if (!gendervalidation.isSuccess()) {
                                warningerror.setCode(datas);
                                warningerror.setErrorcode("505");
                                warningerror.setDetails("SDx Conflict with sex");
                                warningerrorlist.add(warningerror);
                            } else if (!agevalidation.isSuccess()) {
                                warningerror.setCode(datas);
                                warningerror.setErrorcode("504");
                                warningerror.setDetails("SDx Conflict with age");
                                warningerrorlist.add(warningerror);
                            } else {
                                secondaryjoin.add(utility.CleanCode(datas).trim());
                            }

                        }
                    }
                }
                // RVS MANIPULATION AREA ==========PROCESS FIRST THE REPITITION OF RVS 
                for (int proc = 0; proc < drgclaim.getPROCEDURES().getPROCEDURE().size(); proc++) {
                    WarningErrorList warningerror = utility.WarningErrorList();
                    procedure = drgclaim.getPROCEDURES().getPROCEDURE().get(proc);
                    String rvs_code = procedure.getRvsCode();
                    int indexvalue = duplicateproc.indexOf(String.valueOf(proc));
                    if (indexvalue >= 0) {
                        warningerror.setCode(rvs_code);
                        warningerror.setErrorcode("508");
                        warningerror.setDetails("RVS Code has duplicate");
                        warningerrorlist.add(warningerror);
                    } else {
                        //=============================================================================
                        if (procedure.getLaterality().trim().isEmpty()) {
                            procedure.setLaterality("N");
                        } else if (!Arrays.asList(laterality).contains(procedure.getLaterality().trim())) {
                            procedure.setLaterality("N");
                        }
                        if (procedure.getExt1().trim().equals("")) {
                            procedure.setExt1("1");
                        } else if (!Arrays.asList(exten).contains(procedure.getExt1().trim())) {
                            procedure.setExt1("1");
                        } else if (procedure.getExt1().trim().length() > 1) {
                            procedure.setExt1("1");
                        }
                        if (procedure.getExt2().trim().equals("")) {
                            procedure.setExt1("1");
                        } else if (!Arrays.asList(exten).contains(procedure.getExt2().trim())) {
                            procedure.setExt1("1");
                        } else if (procedure.getExt2().trim().length() > 1) {
                            procedure.setExt1("1");
                        }

                        //IF RVS NOT FOUND IN THE ICD9 CODES LIBRARY CONVERT THE CODE
                        DRGWSResult checkRVStoICD9cm = new CF5Method().CheckICD9cm(datasource, utility.CleanCode(rvs_code).trim());
                        if (!checkRVStoICD9cm.isSuccess()) {
                            //===========================================================CONVERTER===============================
                            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                            statement.registerOutParameter("converter", OracleTypes.CURSOR);
                            statement.setString("rvs_code", rvs_code);
                            statement.execute();
                            ResultSet resultset = (ResultSet) statement.getObject("converter");
                            if (resultset.next()) {
                                int conflictcounter = 0;
                                String ProcList = resultset.getString("ICD9CODE");
                                List<String> ConverterResult = Arrays.asList(ProcList.split(","));
                                for (int g = 0; g < ConverterResult.size(); g++) {
                                    //  int datafound = gm.CountProc(datasource, ConverterResult.get(g).trim());
                                    if (new CF5Method().CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                        DRGWSResult procgendervalidation = new CF5Method().GenderConfictValidationProc(datasource, ConverterResult.get(g).trim(), nclaimsdata.getGender());
                                        if (!procgendervalidation.isSuccess()) {
                                            conflictcounter++;
                                        }
                                    }
                                }
                                if (conflictcounter > 0) {
                                    warningerror.setCode(rvs_code);
                                    warningerror.setErrorcode("509");
                                    warningerror.setDetails("RVS Sex Conflict");
                                    warningerrorlist.add(warningerror);
                                } else {
                                    for (int g = 0; g < ConverterResult.size(); g++) {
                                        String ICD9Codes = ConverterResult.get(g);
                                        // int datafound = gm.CountProc(datasource, ICD9Codes.trim());
                                        if (new CF5Method().CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                            if (procedure.getExt2().trim().equals("1") && procedure.getExt1().trim().equals("1")) {
                                                procedurejoin.add(ICD9Codes.trim());
                                            } else {
                                                procedurejoin.add(ICD9Codes.trim() + ">1");
                                            }
                                        }
                                    }

                                }
                            } else {
                                warningerror.setCode(rvs_code);
                                warningerror.setErrorcode("506");
                                warningerror.setDetails("Invalid RVS not in the library");
                                warningerrorlist.add(warningerror);
                            }

                        } else {
                            procedurejoin.add(utility.CleanCode(rvs_code).trim());
                        }
                    }
                }

                String finalsdx = String.join(",", secondaryjoin);
                String finalproc = String.join(",", procedurejoin);
                grouperparameterlist.setClaimseries(drgclaim.getClaimNumber());
                grouperparameterlist.setAdmissionDate(nclaimsdata.getAdmissionDate());
                grouperparameterlist.setAdmissionWeight(drgclaim.getNewBornAdmWeight());
                grouperparameterlist.setBirthDate(nclaimsdata.getDateofBirth());
                grouperparameterlist.setDischargeDate(nclaimsdata.getDischargeDate());
                switch (nclaimsdata.getDischargeType()) {
                    case "E":
                        grouperparameterlist.setDischargeType("8");
                        break;
                    case "O":
                        grouperparameterlist.setDischargeType("5");
                        break;
                    case "I":
                    case "R":
                        grouperparameterlist.setDischargeType("1");
                        break;
                    case "A":
                        grouperparameterlist.setDischargeType("3");
                        break;
                    case "T":
                        grouperparameterlist.setDischargeType("4");
                        break;
                    case "H":
                        grouperparameterlist.setDischargeType("2");
                        break;
                }

                grouperparameterlist.setGender(nclaimsdata.getGender());
                grouperparameterlist.setPdx(drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase());
                grouperparameterlist.setProc(finalproc);
                grouperparameterlist.setSdx(finalsdx);
                grouperparameterlist.setTimeAdmission(nclaimsdata.getTimeAdmission());
                grouperparameterlist.setTimeDischarge(nclaimsdata.getTimeDischarge());

                //CALL GROUPER METHOD
                String stringurl = "http://localhost:7001/PHILGROUPER/Grouper/PROCESSGrouper";
                URL url = new URL(stringurl);
                HttpURLConnection connections = (HttpURLConnection) url.openConnection();
                connections.setDoOutput(true);
                connections.setRequestMethod("POST");
                connections.setRequestProperty("Content-Type", "application/json");
                OutputStream stream = connections.getOutputStream();
                stream.write(utility.objectMapper().writeValueAsString(grouperparameterlist).getBytes());
                stream.flush();
                if (connections.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connections.getInputStream(), "UTF-8"));
                    String output;
                    String stringresult = "";
                    while ((output = reader.readLine()) != null) {
                        stringresult = output;
                    }
                    DRGWSResult gettresult = utility.objectMapper().readValue(stringresult, DRGWSResult.class);
                    if (gettresult.isSuccess()) {
                        result.setMessage(gettresult.getMessage());
                        result.setResult(gettresult.getResult());
                        result.setSuccess(gettresult.isSuccess());
                    }
                } else {
                    result.setMessage(connections.getResponseMessage());
                }
                connections.disconnect();

            }

        } catch (MalformedURLException ex) {
            result.setMessage(ex.getMessage());
            Logger.getLogger(AccessGrouperFrontValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(AccessGrouperFrontValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
