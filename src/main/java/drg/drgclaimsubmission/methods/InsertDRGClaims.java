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
 * @author DRG_SHADOWBILLING
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
        result.setMessage("InsertDRGClaims");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            LinkedList<String> ErrMessage = new LinkedList<>();
            connection.setAutoCommit(false);
            //Result ID Process Here
            String result_id = utility.RandomNumeric(6) + "" + utility.SimpleDateFormat("MMddyyyyHHmm").format(new Date());
            //---------------------------
            int days = 0;
            int year = 0;
            int finalDays = 0;
            if (nclaimsdata.getDateofBirth().isEmpty() && nclaimsdata.getAdmissionDate().isEmpty()) {
            } else if (nclaimsdata.getDateofBirth().isEmpty()) {
            } else if (nclaimsdata.getAdmissionDate().isEmpty()) {
            } else {
                days = utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate());
                year = utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate());
            }
            if (year > 0) {
                finalDays = year * 365;
            } else {
                finalDays = days;
            }
            //======================================================
            //Process Procedure
            ArrayList<String> procedurejoin = new ArrayList<>();
            //Process Secondary Diagnosis
            ArrayList<String> secondaryjoin = new ArrayList<>();
            //======================================================
            //INSERTION OF SECONDARY DIAGNOSIS 
            //END INSERTION OF SECONDARY DIAGNOSIS 
            CallableStatement ps = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_PATIENT_INFO("
                    + ":Message,:Code,"
                    + ":updxcode,:unbtob, "
                    + ":unadmweight,:useries,"
                    + ":ulhio,:uaccreno,:uclaimnumber)");
            ps.registerOutParameter("Message", OracleTypes.VARCHAR);
            ps.registerOutParameter("Code", OracleTypes.INTEGER);
            ps.setString("updxcode", drgclaim.getPrimaryCode());
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
            CallableStatement error = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_DRG_WARNING_ERROR("
                    + ":Message, :Code,"
                    + ":uclaimid,:uresultid,:useries,:ucode,"
                    + ":udata,:udesc,:ulhio)");
            //SECONDARY DIAGNOSIS INSERTION OF DATA TO DATABASE
            for (int second = 0; second < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); second++) {
                if (drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().trim().isEmpty() || drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().trim().equals("")) {
                } else {
                    CallableStatement insertsecondary = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_SECONDARY(:Message,:Code,:uclaimid,:usdxcode,:useries,:ulhio)");
                    insertsecondary.registerOutParameter("Message", OracleTypes.VARCHAR);
                    insertsecondary.registerOutParameter("Code", OracleTypes.NUMBER);
                    DRGWSResult SDxResult = new CF5Method().GetICD10(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                    DRGWSResult gendervalidation = new CF5Method().GenderConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode(), nclaimsdata.getGender());
                    DRGWSResult agevalidation = new CF5Method().AgeConfictValidation(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode(), String.valueOf(finalDays), String.valueOf(year));
                    if (duplcatesdx.contains(String.valueOf(second))) {
                        error.registerOutParameter("Message", OracleTypes.VARCHAR);
                        error.registerOutParameter("Code", OracleTypes.INTEGER);
                        error.setString("uclaimid", drgclaim.getClaimNumber());
                        error.setString("uresultid", result_id.trim());
                        error.setString("useries", series.trim());
                        error.setString("ucode", "503".trim());
                        error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                        error.setString("udesc", "CF5 SDx is the repetition with other SDx");
                        error.setString("ulhio", lhio.trim());
                        error.executeUpdate();
                        if (!error.getString("Message").equals("SUCC")) {
                            ErrMessage.add(error.getString("Message"));
                        }
                    } else {
                        if (drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode().equals(drgclaim.getPrimaryCode())) {
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", "502".trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                            error.setString("udesc", "CF5 Case that SDx is the repetition with PDx");
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else if (!SDxResult.isSuccess()) {
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id);
                            error.setString("useries", series.trim());
                            error.setString("ucode", "501".trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                            error.setString("udesc", "CF5 SDx Invalid Code");
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!gendervalidation.isSuccess()) {
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", "505".trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                            error.setString("udesc", "CF5 SDx Conflict with sex ");
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }

                        } else if (!agevalidation.isSuccess()) {
                            error.registerOutParameter("Message", OracleTypes.VARCHAR);
                            error.registerOutParameter("Code", OracleTypes.INTEGER);
                            error.setString("uclaimid", drgclaim.getClaimNumber());
                            error.setString("uresultid", result_id.trim());
                            error.setString("useries", series.trim());
                            error.setString("ucode", "504".trim());
                            error.setString("udata", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                            error.setString("udesc", "CF5 SDx Conflict with age");
                            error.setString("ulhio", lhio.trim());
                            error.executeUpdate();
                            if (!error.getString("Message").equals("SUCC")) {
                                ErrMessage.add(error.getString("Message"));
                            }
                        } else {
                            insertsecondary.setString("uclaimid", drgclaim.getClaimNumber());
                            insertsecondary.setString("usdxcode", drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode());
                            insertsecondary.setString("useries", series.trim());
                            insertsecondary.setString("ulhio", lhio.trim());
                            secondaryjoin.add(utility.CleanCode(drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(second).getSecondaryCode()));
                            insertsecondary.executeUpdate();
                            if (!insertsecondary.getString("Message").equals("SUCC")) {
                                ErrMessage.add(insertsecondary.getString("Message"));
                            }
                        }
                    }
                }
            }

            for (int proc = 0; proc < drgclaim.getPROCEDURES().getPROCEDURE().size(); proc++) {
                if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().isEmpty() || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().equals("")) {
                } else {
                    CallableStatement insertprocedure = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_PROCEDURE("
                            + ":Message,:Code, "
                            + ":uclaimid,:urvs,"
                            + ":ulaterality,:uext1code, "
                            + ":uext2code,:uicd9code, "
                            + ":useries,:ulhio)");
                    insertprocedure.registerOutParameter("Message", OracleTypes.VARCHAR);
                    insertprocedure.registerOutParameter("Code", OracleTypes.INTEGER);
                    if (duplicateproc.contains(String.valueOf(proc))) {
                        error.registerOutParameter("Message", OracleTypes.VARCHAR);
                        error.registerOutParameter("Code", OracleTypes.INTEGER);
                        error.setString("uclaimid", drgclaim.getClaimNumber());
                        error.setString("uresultid", result_id.trim());
                        error.setString("useries", series.trim());
                        error.setString("ucode", "507".trim());
                        error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                        error.setString("udesc", "CF5 RVS Code has duplicate");
                        error.setString("ulhio", lhio.trim());
                        error.executeUpdate();
                        if (!error.getString("Message").equals("SUCC")) {
                            ErrMessage.add(error.getString("Message"));
                        }
                    } else {
                        if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim() == null
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim().isEmpty()
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim().isEmpty()) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                        } else if (!Arrays.asList("L", "R", "B", "N").contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim())) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setLaterality("N");
                        }
                        if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim() == null
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().equals("")
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().isEmpty()) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                        } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim())) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                        } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim().length() > 1) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt1("1");
                        }
                        if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim() == null
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().equals("")
                                || drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().isEmpty()) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                        } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim())) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                        } else if (drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim().length() > 1) {
                            drgclaim.getPROCEDURES().getPROCEDURE().get(proc).setExt2("1");
                        }
                        DRGWSResult checkRVStoICD9cm = new CF5Method().CheckICD9cm(datasource, utility.CleanCode(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode()));
                        if (!checkRVStoICD9cm.isSuccess()) {
                            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                            statement.registerOutParameter("converter", OracleTypes.CURSOR);
                            statement.setString("rvs_code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode());
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
                                    error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                    error.registerOutParameter("Code", OracleTypes.INTEGER);
                                    error.setString("uclaimid", drgclaim.getClaimNumber().trim());
                                    error.setString("uresultid", result_id.trim());
                                    error.setString("useries", series.trim());
                                    error.setString("ucode", "508".trim());
                                    error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                    error.setString("udesc", "CF5 RVS Sex Conflict");
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
                                            if ((drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim() + "" + drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim()).equals("11")) {
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
                                    error.registerOutParameter("Message", OracleTypes.VARCHAR);
                                    error.registerOutParameter("Code", OracleTypes.INTEGER);
                                    error.setString("uclaimid", drgclaim.getClaimNumber().trim());
                                    error.setString("uresultid", result_id.trim());
                                    error.setString("useries", series.trim());
                                    error.setString("ucode", "506".trim());
                                    error.setString("udata", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode().trim());
                                    error.setString("udesc", "CF5 Invalid RVS not found in the library");
                                    error.setString("ulhio", lhio.trim());
                                    error.executeUpdate();
                                    if (!error.getString("Message").equals("SUCC")) {
                                        ErrMessage.add(error.getString("Message"));
                                    }
                                }
                            }
                        } else {
                            insertprocedure.setString("uclaimid", drgclaim.getClaimNumber().trim());
                            insertprocedure.setString("urvs", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode());
                            insertprocedure.setString("ulaterality", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getLaterality().trim());
                            insertprocedure.setString("uext1code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt1().trim());
                            insertprocedure.setString("uext2code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getExt2().trim());
                            procedurejoin.add(utility.CleanCode(drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode()));
                            insertprocedure.setString("uicd9code", drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode());
                            insertprocedure.setString("useries", series);
                            insertprocedure.setString("ulhio", lhio);
                            insertprocedure.executeUpdate();
                        }
                    }
                }
            }
            //END HERE
            if (ErrMessage.isEmpty()) {
                DRGWSResult gpdata = new CF5Method().InsertDRGResult(datasource, drgclaim.getClaimNumber(), series, lhio, utility.CleanCode(drgclaim.getPrimaryCode()), String.join(",", secondaryjoin), String.join(",", procedurejoin), result_id);
                if (gpdata.isSuccess()) {
                    new CF5Method().InsertDRGAuditTrail(datasource, "CLAIMS SUCCESSFULLY INSERTED", "SUCCESS", series, drgclaim.getClaimNumber(), filecontent);
                    result.setSuccess(true);
                }

//                else {
//                    new CF5Method().InsertDRGAuditTrail(datasource, "FAILED IN INSERTING RESULT " + gpdata.getMessage(), "FAILED", series, drgclaim.getClaimNumber(), filecontent);
//                }
            } else {
                new CF5Method().InsertDRGAuditTrail(datasource, String.join(",", ErrMessage), "FAILED", series, drgclaim.getClaimNumber(), filecontent);
            }
        } catch (Exception ex) {
            new CF5Method().InsertDRGAuditTrail(datasource, ex.toString(), "FAILED", series, drgclaim.getClaimNumber(), "CF5 XML DATA");
//            result.setMessage(ex.toString());
            Logger.getLogger(InsertDRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
