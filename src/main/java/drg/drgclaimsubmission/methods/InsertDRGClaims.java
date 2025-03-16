/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.utilities.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class InsertDRGClaims {

    public InsertDRGClaims() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult InsertDRGClaims(final DRGCLAIM drgclaim,
            final DataSource datasource,
            final NClaimsData nclaimsdata,
            final String lhio,
            final String series,
            final String accreno,
            final String claimnumber,
            final List<String> duplicateproc,
            final List<String> duplcatesdx,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        String[] laterality = {"L", "R", "B"};
        String[] exten = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        //===============================================PACKAGE CODE=======================================
        try (Connection connection = datasource.getConnection()) {
            LinkedList<String> ErrMessage = new LinkedList<>();
            connection.setAutoCommit(false);
            //Result ID Process Here
            SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmm");
            Date datetoday = new Date();
            String result_id = utility.RandomNumeric(6) + "" + formatter.format(datetoday);
            //---------------------------
            String days = "0";
            String year = "0";
            if (nclaimsdata.getDateofBirth().isEmpty() && nclaimsdata.getAdmissionDate().isEmpty()) {
            } else if (nclaimsdata.getDateofBirth().isEmpty()) {
            } else if (nclaimsdata.getAdmissionDate().isEmpty()) {
            } else {
                days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
            }

            int finalDays = 0;
            if (Integer.parseInt(year) > 0) {
                finalDays = Integer.parseInt(year) * 365;
            } else {
                finalDays = Integer.parseInt(days);
            }
            //======================================================
            //Process Procedure
            ArrayList<String> procedurejoin = new ArrayList<>();
            //Process Secondary Diagnosis
            ArrayList<String> secondaryjoin = new ArrayList<>();
            //======================================================
            //INSERTION OF SECONDARY DIAGNOSIS 
            //END INSERTION OF SECONDARY DIAGNOSIS 
            CallableStatement ps = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_PATIENT_INFO("
                    + ":Message, :Code, "
                    + ":updxcode, :unbtob, "
                    + ":unadmweight,:useries, "
                    + ":ulhio,:uaccreno,:uclaimnumber)");
            ps.registerOutParameter("Message", OracleTypes.VARCHAR);
            ps.registerOutParameter("Code", OracleTypes.INTEGER);
            ps.setString("updxcode", drgclaim.getPrimaryCode().trim().replaceAll("\\.", "").toUpperCase());
            ps.setString("unbtob", drgclaim.getNewBornAdmWeight());
            ps.setString("unadmweight", drgclaim.getNewBornAdmWeight());
            ps.setString("useries", series.trim());
            ps.setString("ulhio", lhio.trim());
            ps.setString("uaccreno", accreno.trim());
            ps.setString("uclaimnumber", drgclaim.getClaimNumber().trim());
            ps.executeUpdate();
            if (!ps.getString("Message").equals("SUCC")) {
                ErrMessage.add(ps.getString("Message"));
            }
            //====================================================== INSERTION FOR WARNING ERROR
            CallableStatement error = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_DRG_WARNING_ERROR("
                    + ":Message, :Code,"
                    + ":uclaimid,:uresultid,:useries,:ucode,"
                    + ":udata,:udesc,:ulhio)");
            //=======================================================================================  
            //SECONDARY DIAGNOSIS INSERTION OF DATA TO DATABASE
            for (int second = 0; second < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); second++) {
                CallableStatement insertsecondary = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_SECONDARY(:Message,:Code,:uclaimid,:usdxcode,:useries,:ulhio)");
                insertsecondary.registerOutParameter("Message", OracleTypes.VARCHAR);
                insertsecondary.registerOutParameter("Code", OracleTypes.NUMBER);
                DRGWSResult SDxResult = new CF5Method().GetICD10(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase());
                DRGWSResult gendervalidation = new CF5Method().GenderConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase(), nclaimsdata.getGender());
                DRGWSResult agevalidation = new CF5Method().AgeConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase(), String.valueOf(finalDays), year);
                if (drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().length() != 0) {
                    if (duplcatesdx.contains(String.valueOf(second))) {
                        String desc_error = "CF5 SDx is the repetition with other SDx";
                        String code_error = "503";
                        error.registerOutParameter("Message", OracleTypes.VARCHAR);
                        error.registerOutParameter("Code", OracleTypes.INTEGER);
                        error.setString("uclaimid", drgclaim.getClaimNumber());
                        error.setString("uresultid", result_id.trim());
                        error.setString("useries", series.trim());
                        error.setString("ucode", code_error.trim());
                        error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                        error.setString("udesc", desc_error);
                        error.setString("ulhio", lhio.trim());
                        error.executeUpdate();
                        if (!error.getString("Message").equals("SUCC")) {
                            ErrMessage.add(error.getString("Message"));
                        }
                    } else {
                        if (drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().equals(drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase())) {
                            String desc_error = "CF5 Case that SDx is the repetition with PDx";
                            String code_error = "502";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", code_error.trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("udesc", desc_error);
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else if (!SDxResult.isSuccess()) {
                            String desc_error = "CF5 SDx Invalid Code";
                            String code_error = "501";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id);
                            error.setString("useries", series.trim());
                            error.setString("ucode", code_error.trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("udesc", desc_error);
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!gendervalidation.isSuccess()) {
                            String desc_error = "CF5 SDx Conflict with sex ";
                            String code_error = "505";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", code_error.trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("udesc", desc_error);
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!agevalidation.isSuccess()) {
                            String desc_error = "CF5 SDx Conflict with age ";
                            String code_error = "504";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", code_error.trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("udesc", desc_error);
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else {
                            insertsecondary.setString("uclaimid", drgclaim.getClaimNumber());
                            insertsecondary.setString("usdxcode", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase());
                            insertsecondary.setString("useries", series.trim());
                            insertsecondary.setString("ulhio", lhio.trim());
                            secondaryjoin.add(drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase());
                            insertsecondary.executeUpdate();
                            if (!insertsecondary.getString("Message").equals("SUCC")) {
                                ErrMessage.add(insertsecondary.getString("Message"));
                            }
                        }
                    }
                }
            }
            // RVS MANIPULATION AREA ==========PROCESS FIRST THE REPITITION OF RVS 
            for (int proc = 0; proc < drgclaim.getPROCEDURES().getPROCEDURE().size(); proc++) {
                CallableStatement insertprocedure = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_PROCEDURE("
                        + ":Message,:Code, "
                        + ":uclaimid,:urvs,"
                        + ":ulaterality,:uext1code, "
                        + ":uext2code,:uicd9code, "
                        + ":useries,:ulhio)");
                insertprocedure.registerOutParameter("Message", OracleTypes.VARCHAR);
                insertprocedure.registerOutParameter("Code", OracleTypes.INTEGER);
                if (duplicateproc.contains(String.valueOf(proc))) {
                    String desc_error = "CF5 RVS Code has duplicate";
                    String code_error = "507";
                    error.registerOutParameter("Message", OracleTypes.VARCHAR);
                    error.registerOutParameter("Code", OracleTypes.INTEGER);
                    error.setString("uclaimid", drgclaim.getClaimNumber());
                    error.setString("uresultid", result_id.trim());
                    error.setString("useries", series.trim());
                    error.setString("ucode", code_error.trim());
                    error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                    error.setString("udesc", desc_error);
                    error.setString("ulhio", lhio.trim());
                    error.executeUpdate();
                    if (!error.getString("Message").equals("SUCC")) {
                        ErrMessage.add(error.getString("Message"));
                    }
                } else {
                    //=============================================================================
                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim().isEmpty()
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim().isEmpty()
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim() == null) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                    } else if (!Arrays.asList(laterality).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                    }
                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().equals("")
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().isEmpty()
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim() == null) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (!Arrays.asList(exten).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().length() > 1) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    }

                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().equals("")
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().isEmpty()
                            || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim() == null) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                    } else if (!Arrays.asList(exten).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                    } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().length() > 1) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                    }
                    //===========================================================CONVERTER===============================
                    DRGWSResult checkRVStoICD9cm = new CF5Method().CheckICD9cm(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                    if (!checkRVStoICD9cm.isSuccess()) {
                        CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                        statement.registerOutParameter("converter", OracleTypes.CURSOR);
                        statement.setString("rvs_code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("converter");
                        if (resultset.next()) {
                            int conflictcounter = 0;
                            List<String> ConverterResult = Arrays.asList(resultset.getString("ICD9CODE").trim().split(","));
                            for (int g = 0; g < ConverterResult.size(); g++) {
                                if (new CF5Method().CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                    DRGWSResult procgendervalidation = new CF5Method().GenderConfictValidationProc(datasource, ConverterResult.get(g).trim(), nclaimsdata.getGender());
                                    if (!procgendervalidation.isSuccess()) {
                                        conflictcounter++;
                                    }
                                }
                            }
                            if (conflictcounter > 0) {
                                String desc_error = "CF5 RVS Sex Conflict";
                                String code_error = "508";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("uclaimid", drgclaim.getClaimNumber().trim());
                                error.setString("uresultid", result_id.trim());
                                error.setString("useries", series.trim());
                                error.setString("ucode", code_error.trim());
                                error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                error.setString("udesc", desc_error);
                                error.setString("ulhio", lhio);
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }
                            } else {
                                for (int g = 0; g < ConverterResult.size(); g++) {
                                    if (new CF5Method().CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                        insertprocedure.setString("uclaimid", drgclaim.getClaimNumber().trim());
                                        insertprocedure.setString("urvs", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                        insertprocedure.setString("ulaterality", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim());
                                        insertprocedure.setString("uext1code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim());
                                        insertprocedure.setString("uext2code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim());
                                        String extcom = drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim() + "" + drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim();
                                        if (extcom.trim().equals("11")) {
                                            procedurejoin.add(ConverterResult.get(g).trim());
                                            insertprocedure.setString("uicd9code", ConverterResult.get(g).trim());
                                        } else {
                                            procedurejoin.add(ConverterResult.get(g).trim() + ">1");
                                            insertprocedure.setString("uicd9code", ConverterResult.get(g).trim() + ">1");
                                        }
                                        insertprocedure.setString("useries", series);
                                        insertprocedure.setString("ulhio", lhio);
                                        insertprocedure.executeUpdate();
                                    }
                                }
                                if (!insertprocedure.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(insertprocedure.getString("Message"));
                                }
                            }
                        } else {
                            if (!drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().isEmpty()) {
                                String desc_error = "CF5 Invalid RVS not found in the library";
                                String code_error = "506";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("uclaimid", drgclaim.getClaimNumber().trim());
                                error.setString("uresultid", result_id.trim());
                                error.setString("useries", series.trim());
                                error.setString("ucode", code_error.trim());
                                error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                error.setString("udesc", desc_error);
                                error.setString("ulhio", lhio.trim());
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }
                            }
                        }
                    } else {
                        insertprocedure.setString("uclaimid", drgclaim.getClaimNumber().trim());
                        insertprocedure.setString("urvs", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("ulaterality", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim());
                        insertprocedure.setString("uext1code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim());
                        insertprocedure.setString("uext2code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim());
                        procedurejoin.add(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("uicd9code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("useries", series);
                        insertprocedure.setString("ulhio", lhio);
                        insertprocedure.executeUpdate();
                    }

                }
            }

            //END HERE
            if (ErrMessage.isEmpty()) {
                connection.commit();
                //INSERTION OF DRG RESULT
                DRGWSResult gpdata = new CF5Method().InsertDRGResult(datasource, drgclaim.getClaimNumber(), series, lhio,
                        drgclaim.getPrimaryCode().trim().replaceAll("\\.", "").toUpperCase(),
                        String.join(",", secondaryjoin), String.join(",", procedurejoin), result_id);
                //INSERTION OF DRG RESULT
                DRGWSResult auditrail = new CF5Method().InsertDRGAuditTrail(datasource, "CLAIMS SUCCESSFULLY INSERTED", "SUCCESS", series, drgclaim.getClaimNumber(), filecontent);
                if (gpdata.isSuccess() && auditrail.isSuccess()) {
                    result.setMessage("CF5 " + series + " CLAIMS SUCCESSFULLY INSERTED");
                } else {
                    result.setMessage(gpdata.getMessage() + " , " + auditrail.getMessage());
                }
                result.setResult(gpdata.getResult());
                result.setSuccess(true);

            } else {
                connection.rollback();
                String details = "CF5 CLAIMS INSERTION FAIL";
                String stats = "FAILED";
                DRGWSResult auditrail = new CF5Method().InsertDRGAuditTrail(datasource, details, stats, series, drgclaim.getClaimNumber(), filecontent);
                result.setMessage(details + " Claims Status:" + auditrail);
                result.setResult(utility.objectMapper().writeValueAsString(ErrMessage));
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertDRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }
}
