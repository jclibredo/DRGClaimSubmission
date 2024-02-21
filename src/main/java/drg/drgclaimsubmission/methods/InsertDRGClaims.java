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
import drg.drgclaimsubmission.utilities.DRGUtility;
import drg.drgclaimsubmission.utilities.GrouperMethod;
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
    private final DRGUtility drgutility = new DRGUtility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult InsertDRGClaims(final DRGCLAIM drgclaim,
            final DataSource datasource,
            final NClaimsData nclaimsdata,
            final String lhio,
            final String series,
            final String accreno,
            final String claimnumber,
            final String duplicateproc,
            final String duplcatesdx,
            final String filecontent) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        String[] laterality = {"L", "R", "B"};
        String[] exten = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        //===============================================PACKAGE CODE=======================================
        try (Connection connection = datasource.getConnection()) {
            PROCEDURE procedure;
            LinkedList<String> ErrMessage = new LinkedList<>();
            connection.setAutoCommit(false);

            //Result ID Process Here
            SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyHHmmss");
            Date datetoday = new Date();
            String result_id = utility.RandomNumeric(6) + "" + formatter.format(datetoday);
            //---------------------------
            String days = String.valueOf(drgutility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
            String year = String.valueOf(drgutility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
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
            ps.setString("PDX_CODE", drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase());
            ps.setString("NB_TOB", drgclaim.getNewBornAdmWeight());
            ps.setString("NB_ADMWEIGHT", drgclaim.getNewBornAdmWeight());
            ps.setString("SERIES", series);
            ps.setString("LHIO", lhio);
            ps.setString("ACCRENO", accreno);
            ps.setString("CLAIMNUMBER", drgclaim.getClaimNumber());
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
                String datas = drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode();
                DRGWSResult SDxResult = gm.GetICD10(datasource, datas.replaceAll("\\.", "").toUpperCase());
                DRGWSResult gendervalidation = gm.GenderConfictValidation(datasource, datas.replaceAll("\\.", "").toUpperCase(), nclaimsdata.getGender());
                DRGWSResult agevalidation = gm.AgeConfictValidation(datasource, datas.replaceAll("\\.", "").toUpperCase(), days, year);
                if (datas.length() != 0) {
                    int indexvalue = duplcatesdx.indexOf(String.valueOf(second));
                    if (indexvalue >= 0) {
                        String desc_error = "SDx is the repetition with other SDx";
                        String code_error = "503";
                        error.registerOutParameter("Message", OracleTypes.VARCHAR);
                        error.registerOutParameter("Code", OracleTypes.INTEGER);
                        error.setString("claimNum", drgclaim.getClaimNumber());
                        error.setString("rest_id", result_id);
                        error.setString("series_error", series);
                        error.setString("code_error", code_error);
                        error.setString("data_error", datas.replaceAll("\\.", "").toUpperCase());
                        error.setString("desc_error", desc_error);
                        error.setString("lhio_error", lhio);
                        error.executeUpdate();
                        if (!error.getString("Message").equals("SUCC")) {
                            ErrMessage.add(error.getString("Message"));
                        }
                    } else {
                        if (datas.replaceAll("\\.", "").toUpperCase().equals(drgclaim.getPrimaryCode().replaceAll("\\.", "").toUpperCase())) {
                            String desc_error = "Case that SDx is the repetition with PDx";
                            String code_error = "502";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id);
                            error.setString("series_error", series);
                            error.setString("code_error", code_error);
                            error.setString("data_error", datas.replaceAll("\\.", "").toUpperCase());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio);
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!SDxResult.isSuccess()) {
                            String desc_error = "SDx Invalid Code";
                            String code_error = "501";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id);
                            error.setString("series_error", series);
                            error.setString("code_error", code_error);
                            error.setString("data_error", datas.replaceAll("\\.", "").toUpperCase());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio);
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!gendervalidation.isSuccess()) {
                            String desc_error = "SDx Conflict with sex ";
                            String code_error = "505";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id);
                            error.setString("series_error", series);
                            error.setString("code_error", code_error);
                            error.setString("data_error", datas.replaceAll("\\.", "").toUpperCase());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio);
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!agevalidation.isSuccess()) {
                            String desc_error = "SDx Conflict with age ";
                            String code_error = "504";
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("claimNum", drgclaim.getClaimNumber());
                            error.setString("rest_id", result_id);
                            error.setString("series_error", series);
                            error.setString("code_error", code_error);
                            error.setString("data_error", datas.replaceAll("\\.", "").toUpperCase());
                            error.setString("desc_error", desc_error);
                            error.setString("lhio_error", lhio);
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else {
                            insertsecondary.setString("claimNum", drgclaim.getClaimNumber());
                            insertsecondary.setString("SDX_CODE", datas.replaceAll("\\.", "").toUpperCase());
                            insertsecondary.setString("SERIES", series);
                            insertsecondary.setString("LHIO", lhio);
                            secondaryjoin.add(datas.replaceAll("\\.", "").toUpperCase());
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
                procedure = drgclaim.getPROCEDURES().getPROCEDURE().get(proc);
                String rvs_code = procedure.getRvsCode();
                int indexvalue = duplicateproc.indexOf(String.valueOf(proc));
                if (indexvalue >= 0) {
                    String desc_error = "RVS Code has duplicate";
                    String code_error = "508";
                    error.registerOutParameter("Message", OracleTypes.VARCHAR);
                    error.registerOutParameter("Code", OracleTypes.INTEGER);
                    error.setString("claimNum", drgclaim.getClaimNumber());
                    error.setString("rest_id", result_id);
                    error.setString("series_error", series);
                    error.setString("code_error", code_error);
                    error.setString("data_error", rvs_code);
                    error.setString("desc_error", desc_error);
                    error.setString("lhio_error", lhio);
                    error.executeUpdate();
                    if (!error.getString("Message").equals("SUCC")) {
                        ErrMessage.add(error.getString("Message"));
                    }

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
                    //===========================================================CONVERTER===============================

                    DRGWSResult checkRVStoICD9cm = gm.CheckICD9cm(datasource, rvs_code.trim().replaceAll("\\.", ""));
                    if (!checkRVStoICD9cm.isSuccess()) {
                        CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                        statement.registerOutParameter("converter", OracleTypes.CURSOR);
                        statement.setString("rvs_code", rvs_code.trim().replaceAll("\\.", ""));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("converter");
                        if (resultset.next()) {
                            int conflictcounter = 0;
                            String ProcList = resultset.getString("ICD9CODE");
                            List<String> ConverterResult = Arrays.asList(ProcList.split(","));
                            for (int g = 0; g < ConverterResult.size(); g++) {
                                int datafound = gm.CountProc(datasource, ConverterResult.get(g).trim());
                                if (datafound != 0) {
                                    DRGWSResult procgendervalidation = gm.GenderConfictValidationProc(datasource, ConverterResult.get(g).trim(), nclaimsdata.getGender());
                                    if (!procgendervalidation.isSuccess()) {
                                        conflictcounter++;
                                    }
                                }
                            }
                            if (conflictcounter > 0) {
                                String desc_error = "RVS Sex Conflict";
                                String code_error = "509";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("claimNum", drgclaim.getClaimNumber());
                                error.setString("rest_id", result_id);
                                error.setString("series_error", series);
                                error.setString("code_error", code_error);
                                error.setString("data_error", procedure.getRvsCode());
                                error.setString("desc_error", desc_error);
                                error.setString("lhio_error", lhio);
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }

                            } else {
                                for (int g = 0; g < ConverterResult.size(); g++) {
                                    String ICD9Codes = ConverterResult.get(g);
                                    int datafound = gm.CountProc(datasource, ICD9Codes.trim());
                                    if (datafound != 0) {
                                        insertprocedure.setString("claimNum", drgclaim.getClaimNumber());
                                        insertprocedure.setString("RVS", procedure.getRvsCode());
                                        insertprocedure.setString("LATERALITY", procedure.getLaterality());
                                        insertprocedure.setString("EXT1_CODE", procedure.getExt1());
                                        insertprocedure.setString("EXT2_CODE", procedure.getExt2());
                                        if (procedure.getExt2().trim().equals("1")) {
                                            procedurejoin.add(ICD9Codes.trim());
                                            insertprocedure.setString("ICD9_CODE", ICD9Codes.trim());
                                        } else {
                                            procedurejoin.add(ICD9Codes.trim() + ">1");
                                            insertprocedure.setString("ICD9_CODE", ICD9Codes.trim() + ">1");
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

                            if (!procedure.getRvsCode().trim().isEmpty()) {
                                String desc_error = "Invalid RVS not in the library";
                                String code_error = "506";
                                error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                error.registerOutParameter("Code", OracleTypes.INTEGER);
                                error.setString("claimNum", drgclaim.getClaimNumber());
                                error.setString("rest_id", result_id);
                                error.setString("series_error", series);
                                error.setString("code_error", code_error);
                                error.setString("data_error", procedure.getRvsCode());
                                error.setString("desc_error", desc_error);
                                error.setString("lhio_error", lhio);
                                error.executeUpdate();
                                if (!error.getString("Message").equals("SUCC")) {
                                    ErrMessage.add(error.getString("Message"));
                                }
                            }

                        }

                    } else {
                        insertprocedure.setString("claimNum", drgclaim.getClaimNumber());
                        insertprocedure.setString("RVS", procedure.getRvsCode().trim().replaceAll("\\.", ""));
                        insertprocedure.setString("LATERALITY", procedure.getLaterality());
                        insertprocedure.setString("EXT1_CODE", procedure.getExt1());
                        insertprocedure.setString("EXT2_CODE", procedure.getExt2());
                        procedurejoin.add(rvs_code.trim().replaceAll("\\.", ""));
                        insertprocedure.setString("ICD9_CODE", rvs_code.trim().replaceAll("\\.", ""));
                        insertprocedure.setString("SERIES", series);
                        insertprocedure.setString("LHIO", lhio);
                        insertprocedure.executeUpdate();
                    }

                }
            }
            //END HERE

            if (ErrMessage.isEmpty()) {
                connection.commit();
                String details = "CLAIMS SUCCESSFULLY INSERTED";
                String stats = "SUCCESS";
                //INSERTION OF DRG RESULT
                String finalsdx = String.join(",", secondaryjoin);
                String finalproc = String.join(",", procedurejoin);
                DRGWSResult gpdata = gm.InsertDRGResult(datasource, drgclaim.getClaimNumber(), series, lhio, drgclaim.getPrimaryCode().trim().replaceAll("\\.", "").toUpperCase(), finalsdx, finalproc, result_id);
                //INSERTION OF DRG RESULT
                DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, drgclaim.getClaimNumber(), filecontent);
                if (gpdata.isSuccess() && auditrail.isSuccess()) {
                    result.setMessage(details);
                } else {
                    result.setMessage(gpdata.getMessage() + " , " + auditrail.getMessage());
                }
                result.setResult(gpdata.getResult());
                result.setSuccess(true);

            } else {
                connection.rollback();
                String details = "CLAIMS INSERTION FAIL";
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
