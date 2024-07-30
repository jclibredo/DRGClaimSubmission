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
import java.io.IOException;
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
 * @author MinoSun
 */
@RequestScoped
public class InsertDRGClaims {

    public InsertDRGClaims() {
    }

    private final Utility utility = new Utility();
    private final CF5Method gm = new CF5Method();

    public DRGWSResult InsertDRGClaims(final DRGCLAIM drgclaim,
            final DataSource datasource,
            final NClaimsData nclaimsdata,
            final String lhio,
            final String series,
            final String accreno,
            final String claimnumber,
            final List<String> duplicateproc,
            final List<String> duplcatesdx,
            final String filecontent) throws IOException {
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
            SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmmss");
            Date datetoday = new Date();
            String result_id = utility.RandomNumeric(6) + "" + formatter.format(datetoday);
            //---------------------------
            String days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
            String year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
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
                    + ":PDX_CODE, :NB_TOB, "
                    + ":NB_ADMWEIGHT,:SERIES, "
                    + ":LHIO,:ACCRENO,:CLAIMNUMBER)");
            ps.registerOutParameter("Message", OracleTypes.VARCHAR);
            ps.registerOutParameter("Code", OracleTypes.INTEGER);
            ps.setString("PDX_CODE", drgclaim.getPrimaryCode().trim().replaceAll("\\.", "").toUpperCase());
            ps.setString("NB_TOB", drgclaim.getNewBornAdmWeight());
            ps.setString("NB_ADMWEIGHT", drgclaim.getNewBornAdmWeight());
            ps.setString("SERIES", series.trim());
            ps.setString("LHIO", lhio.trim());
            ps.setString("ACCRENO", accreno.trim());
            ps.setString("CLAIMNUMBER", drgclaim.getClaimNumber().trim());
            ps.executeUpdate();
            if (!ps.getString("Message").equals("SUCC")) {
                ErrMessage.add(ps.getString("Message"));
            }
            //====================================================== INSERTION FOR WARNING ERROR
            CallableStatement error = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_DRG_WARNING_ERROR("
                    + ":Message, :Code, "
                    + ":claimNum,:rest_id,:series_error, :code_error, "
                    + ":data_error,:desc_error, "
                    + ":lhio_error)");
            //=======================================================================================  
            //SECONDARY DIAGNOSIS INSERTION OF DATA TO DATABASE
            for (int second = 0; second < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); second++) {
                CallableStatement insertsecondary = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_SECONDARY(:Message,:Code,:claimNum,:SDX_CODE,:SERIES,:LHIO)");
                insertsecondary.registerOutParameter("Message", OracleTypes.VARCHAR);
                insertsecondary.registerOutParameter("Code", OracleTypes.NUMBER);
                DRGWSResult SDxResult = gm.GetICD10(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase());
                DRGWSResult gendervalidation = gm.GenderConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase(), nclaimsdata.getGender());
                DRGWSResult agevalidation = gm.AgeConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase(), String.valueOf(finalDays), year);
                if (drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().length() != 0) {
                    if (duplcatesdx.contains(String.valueOf(second))) {
                        String desc_error = "CF5 SDx is the repetition with other SDx";
                        String code_error = "503";
                        error.registerOutParameter("Message", OracleTypes.VARCHAR);
                        error.registerOutParameter("Code", OracleTypes.INTEGER);
                        error.setString("claimNum", drgclaim.getClaimNumber());
                        error.setString("rest_id", result_id.trim());
                        error.setString("series_error", series.trim());
                        error.setString("code_error", code_error.trim());
                        error.setString("data_error", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                        error.setString("desc_error", desc_error);
                        error.setString("lhio_error", lhio.trim());
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
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id.trim());
                            error.setString("series_error", series.trim());
                            error.setString("code_error", code_error.trim());
                            error.setString("data_error", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else if (!SDxResult.isSuccess()) {
                            String desc_error = "CF5 SDx Invalid Code";
                            String code_error = "501";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id);
                            error.setString("series_error", series.trim());
                            error.setString("code_error", code_error.trim());
                            error.setString("data_error", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!gendervalidation.isSuccess()) {
                            String desc_error = "CF5 SDx Conflict with sex ";
                            String code_error = "505";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id.trim());
                            error.setString("series_error", series.trim());
                            error.setString("code_error", code_error.trim());
                            error.setString("data_error", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!agevalidation.isSuccess()) {
                            String desc_error = "CF5 SDx Conflict with age ";
                            String code_error = "504";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id.trim());
                            error.setString("series_error", series.trim());
                            error.setString("code_error", code_error.trim());
                            error.setString("data_error", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase().trim());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else {
                            insertsecondary.setString("claimNum", drgclaim.getClaimNumber());
                            insertsecondary.setString("SDX_CODE", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().replaceAll("\\.", "").toUpperCase());
                            insertsecondary.setString("SERIES", series.trim());
                            insertsecondary.setString("LHIO", lhio.trim());
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
                        + ":Message, :Code, "
                        + ":claimNum,:RVS, "
                        + ":LATERALITY, :EXT1_CODE, "
                        + ":EXT2_CODE, :ICD9_CODE, "
                        + ":SERIES, :LHIO)");
                insertprocedure.registerOutParameter("Message", OracleTypes.VARCHAR);
                insertprocedure.registerOutParameter("Code", OracleTypes.INTEGER);
                if (duplicateproc.contains(String.valueOf(proc))) {
                    String desc_error = "CF5 RVS Code has duplicate";
                    String code_error = "508";
                    error.registerOutParameter("Message", OracleTypes.VARCHAR);
                    error.registerOutParameter("Code", OracleTypes.INTEGER);
                    error.setString("claimNum", drgclaim.getClaimNumber());
                    error.setString("rest_id", result_id.trim());
                    error.setString("series_error", series.trim());
                    error.setString("code_error", code_error.trim());
                    error.setString("data_error", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                    error.setString("desc_error", desc_error);
                    error.setString("lhio_error", lhio.trim());
                    error.executeUpdate();
                    if (!error.getString("Message").equals("SUCC")) {
                        ErrMessage.add(error.getString("Message"));
                    }
                } else {
                    //=============================================================================
                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim().isEmpty()) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                    } else if (!Arrays.asList(laterality).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                    }
                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().equals("")) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (!Arrays.asList(exten).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().length() > 1) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    }
                    if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().equals("")) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (!Arrays.asList(exten).contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim())) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().length() > 1) {
                        drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                    }
                    //===========================================================CONVERTER===============================
                    DRGWSResult checkRVStoICD9cm = gm.CheckICD9cm(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
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
                                if (gm.CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                    DRGWSResult procgendervalidation = gm.GenderConfictValidationProc(datasource, ConverterResult.get(g).trim(), nclaimsdata.getGender());
                                    if (!procgendervalidation.isSuccess()) {
                                        conflictcounter++;
                                    }
                                }
                            }
                            if (conflictcounter > 0) {
                                String desc_error = "CF5 RVS Sex Conflict";
                                String code_error = "509";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("claimNum", drgclaim.getClaimNumber().trim());
                                error.setString("rest_id", result_id.trim());
                                error.setString("series_error", series.trim());
                                error.setString("code_error", code_error.trim());
                                error.setString("data_error", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                error.setString("desc_error", desc_error);
                                error.setString("lhio_error", lhio);
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }
                            } else {
                                for (int g = 0; g < ConverterResult.size(); g++) {
                                    if (gm.CountProc(datasource, ConverterResult.get(g).trim()).isSuccess()) {
                                        insertprocedure.setString("claimNum", drgclaim.getClaimNumber().trim());
                                        insertprocedure.setString("RVS", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                        insertprocedure.setString("LATERALITY", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim());
                                        insertprocedure.setString("EXT1_CODE", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim());
                                        insertprocedure.setString("EXT2_CODE", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim());
                                        String extcom = drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim() + "" + drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim();
                                        if (extcom.trim().equals("11")) {
                                            procedurejoin.add(ConverterResult.get(g).trim());
                                            insertprocedure.setString("ICD9_CODE", ConverterResult.get(g).trim());
                                        } else {
                                            procedurejoin.add(ConverterResult.get(g).trim() + ">1");
                                            insertprocedure.setString("ICD9_CODE", ConverterResult.get(g).trim() + ">1");
                                        }
                                        insertprocedure.setString("SERIES", series);
                                        insertprocedure.setString("LHIO", lhio);
                                        insertprocedure.executeUpdate();
                                    }
                                }
                                if (!insertprocedure.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(insertprocedure.getString("Message"));
                                }
                            }
                        } else {
                            if (!drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().isEmpty()) {
                                String desc_error = "CF5 Invalid RVS not in the library";
                                String code_error = "506";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("claimNum", drgclaim.getClaimNumber().trim());
                                error.setString("rest_id", result_id.trim());
                                error.setString("series_error", series.trim());
                                error.setString("code_error", code_error.trim());
                                error.setString("data_error", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                error.setString("desc_error", desc_error);
                                error.setString("lhio_error", lhio.trim());
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }
                            }
                        }
                    } else {
                        insertprocedure.setString("claimNum", drgclaim.getClaimNumber().trim());
                        insertprocedure.setString("RVS", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("LATERALITY", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim());
                        insertprocedure.setString("EXT1_CODE", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim());
                        insertprocedure.setString("EXT2_CODE", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim());
                        procedurejoin.add(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("ICD9_CODE", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("SERIES", series);
                        insertprocedure.setString("LHIO", lhio);
                        insertprocedure.executeUpdate();
                    }
                }
            }
            //END HERE
            //   System.out.println(String.join(",", procedurejoin));
            if (ErrMessage.isEmpty()) {
                connection.commit();
                //INSERTION OF DRG RESULT
                DRGWSResult gpdata = gm.InsertDRGResult(datasource, drgclaim.getClaimNumber(), series, lhio,
                        drgclaim.getPrimaryCode().trim().replaceAll("\\.", "").toUpperCase(),
                        String.join(",", secondaryjoin), String.join(",", procedurejoin), result_id);
                //INSERTION OF DRG RESULT
                DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, "CLAIMS SUCCESSFULLY INSERTED", "SUCCESS", series, drgclaim.getClaimNumber(), filecontent);
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
                DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, drgclaim.getClaimNumber(), filecontent);
                result.setMessage(details + " Claims Status:" + auditrail);
                result.setResult(utility.objectMapper().writeValueAsString(ErrMessage));
                result.setSuccess(false);
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertDRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }
}
