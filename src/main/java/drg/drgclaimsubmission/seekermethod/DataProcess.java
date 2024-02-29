/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.seekermethod;

import drg.drgclaimsubmission.structures.DRGOutput;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.GrouperParameter;
import drg.drgclaimsubmission.structures.WarningErrorList;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.utilities.DRGUtility;
import drg.drgclaimsubmission.utilities.GrouperMethod;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.BufferedReader;
import java.io.IOException;
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
 * @author MinoSun
 */
@RequestScoped
public class DataProcess {

    public DataProcess() {
    }
    private final Utility utility = new Utility();
    private final DRGUtility drgutility = new DRGUtility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult DataProcess(
            final DataSource datasource,
            final GrouperParameter grouperparam,
            final String duplicateproc,
            final String duplcatesdx, List<String> errors, List<String> rvslist) throws IOException {
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
            //System.out.println(errors.toString());
            DRGOutput drgresult = utility.DRGOutput();
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
                String days = String.valueOf(drgutility.ComputeDay(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()));
                String year = String.valueOf(drgutility.ComputeYear(grouperparam.getBirthDate(), grouperparam.getAdmissionDate()));
                //======================================================
                ArrayList<String> procedurejoin = new ArrayList<>();
                ArrayList<String> secondaryjoin = new ArrayList<>();
                //=======================================================================================  
                //SECONDARY DIAGNOSIS INSERTION OF DATA TO DATABASE
                List<String> ProcedureList = Arrays.asList(grouperparam.getProc().split(","));
                List<String> SecondaryList = Arrays.asList(grouperparam.getSdx().split(","));
                for (int second = 0; second < SecondaryList.size(); second++) {
                    WarningErrorList warningerror = utility.WarningErrorList();
                    String datas = SecondaryList.get(second);
                    DRGWSResult SDxResult = gm.GetICD10(datasource, datas.replaceAll("\\.", "").toUpperCase());
                    DRGWSResult gendervalidation = gm.GenderConfictValidation(datasource, datas.replaceAll("\\.", "").toUpperCase(), grouperparam.getGender());
                    DRGWSResult agevalidation = gm.AgeConfictValidation(datasource, datas.replaceAll("\\.", "").toUpperCase(), days, year);
                    if (datas.length() != 0) {
                        int indexvalue = duplcatesdx.indexOf(String.valueOf(second));
                        if (indexvalue >= 0) {
                            warningerror.setCode(datas);
                            warningerror.setErrorcode("503");
                            warningerror.setDetails("SDx is the repetition with other SDx");
                            warningerrorlist.add(warningerror);
                        } else {
                            if (datas.replaceAll("\\.", "").toUpperCase().equals(grouperparam.getPdx().replaceAll("\\.", "").toUpperCase())) {
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
                                secondaryjoin.add(datas.replaceAll("\\.", "").toUpperCase());
                            }

                        }
                    }
                }

                // RVS MANIPULATION AREA ==========PROCESS FIRST THE REPITITION OF RVS 
                for (int proc = 0; proc < rvslist.size(); proc++) {
                    WarningErrorList warningerror = utility.WarningErrorList();
                    //----------------------------------------------------

                    String rvs_code = ProcedureList.get(proc);
                    int indexvalue = duplicateproc.indexOf(String.valueOf(proc));
                    if (indexvalue >= 0) {
                        warningerror.setCode(rvs_code);
                        warningerror.setErrorcode("508");
                        warningerror.setDetails("RVS Code has duplicate");
                        warningerrorlist.add(warningerror);
                    } else {
                        String newset = rvslist.get(proc).replaceAll("\\+", " +");
                        String[] seconds = newset.split(" +");
                        for (int b = 0; b < seconds.length; b++) {
                            if (b == 0) {
                                //IF RVS NOT FOUND IN THE ICD9 CODES LIBRARY CONVERT THE CODE
                                DRGWSResult checkRVStoICD9cm = gm.CheckICD9cm(datasource, rvs_code.trim().replaceAll("\\.", ""));
                                if (!checkRVStoICD9cm.isSuccess()) {
                                    //===========================================================CONVERTER===============================
                                    CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                                    statement.registerOutParameter("converter", OracleTypes.CURSOR);
                                    statement.setString("rvs_code", seconds[b]);
                                    statement.execute();
                                    ResultSet resultset = (ResultSet) statement.getObject("converter");
                                    if (resultset.next()) {
                                        int conflictcounter = 0;
                                        String ProcList = resultset.getString("ICD9CODE");
                                        List<String> ConverterResult = Arrays.asList(ProcList.split(","));
                                        for (int g = 0; g < ConverterResult.size(); g++) {
                                            int datafound = gm.CountProc(datasource, ConverterResult.get(g).trim());
                                            if (datafound != 0) {
                                                DRGWSResult procgendervalidation = gm.GenderConfictValidationProc(datasource, ConverterResult.get(g).trim(), grouperparam.getGender());
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
                                                int datafound = gm.CountProc(datasource, ICD9Codes.trim());
                                                if (datafound != 0) {
                                                    if (seconds[b + 1].equals("+11")) {
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
                                    procedurejoin.add(rvs_code.trim().replaceAll("\\.", ""));
                                }

                            }
                        }
                    }
                    //---------------------------------------------

                }

                String finalsdx = String.join(",", secondaryjoin);
                String finalproc = String.join(",", procedurejoin);
                System.out.println(finalproc);
                grouperparameterlist.setClaimseries("");
                grouperparameterlist.setAdmissionDate(grouperparam.getAdmissionDate());
                grouperparameterlist.setAdmissionWeight(grouperparam.getAdmissionWeight());
                grouperparameterlist.setBirthDate(grouperparam.getBirthDate());
                grouperparameterlist.setDischargeDate(grouperparam.getDischargeDate());
                grouperparameterlist.setDischargeType(grouperparam.getDischargeType());
                grouperparameterlist.setGender(grouperparam.getGender());
                grouperparameterlist.setPdx(grouperparam.getPdx().replaceAll("\\.", "").toUpperCase());
                grouperparameterlist.setProc(finalproc);
                grouperparameterlist.setSdx(finalsdx);
                grouperparameterlist.setTimeAdmission(grouperparam.getTimeAdmission());
                grouperparameterlist.setTimeDischarge(grouperparam.getTimeDischarge());

                //CALL GROUPER METHOD
                String stringurl = "http://localhost:7001/PHILGROUPER/Grouper/Seeker";
                URL url = new URL(stringurl);
                HttpURLConnection connections = (HttpURLConnection) url.openConnection();
                connections.setDoOutput(true);
                connections.setRequestMethod("POST");
                connections.setRequestProperty("Content-Type", "application/json");
                // connections.setRequestProperty("Accept", "application/json");
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
                        DRGOutput drgout = utility.objectMapper().readValue(gettresult.getResult(), DRGOutput.class);
                        result.setMessage(drgout.getDRGName());
                        result.setResult(drgout.getDRG());
                        result.setSuccess(gettresult.isSuccess());
                    }
                } else {
                    result.setMessage(connections.getResponseMessage());
                }
                connections.disconnect();
            }
        } catch (MalformedURLException ex) {
            result.setMessage(ex.getMessage());
            Logger.getLogger(DataProcess.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(DataProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
