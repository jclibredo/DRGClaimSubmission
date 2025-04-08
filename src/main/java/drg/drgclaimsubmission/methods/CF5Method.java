/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.CombinationCode;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.ICD10PreMDCResult;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
public class CF5Method {

    public CF5Method() {
    }

    private final Utility utility = new Utility();

    //GET ICD10 FOR KEY VALUE PAIR VALIDATION
    public DRGWSResult GetICD10(
            final DataSource datasource,
            final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :p_validcode := MINOSUN.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            statement.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            statement.setString("p_icd10_code", utility.CleanCode(p_icd10_code));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_validcode");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setResult(resultset.getString("validcode"));
                result.setMessage("Record Found");
            } else {
                result.setMessage("No ICD10 Record Found");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //RVS CONVERTER TO ICD9CM
    public DRGWSResult GetICD9cm(
            final DataSource datasource,
            final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            List<String> FinalNewProcList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
            statement.registerOutParameter("converter", OracleTypes.CURSOR);
            statement.setString("rvs_code", utility.CleanCode(rvs_code).trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("converter");
            if (resultset.next()) {
                List<String> ConverterResult = Arrays.asList(resultset.getString("ICD9CODE").split(","));
                for (int g = 0; g < ConverterResult.size(); g++) {
                    if (!ConverterResult.get(g).trim().isEmpty()) {
                        FinalNewProcList.add(ConverterResult.get(g).trim());
                    }
                }
                if (FinalNewProcList.size() > 0) {
                    result.setResult(String.join(",", FinalNewProcList));
                    result.setSuccess(true);
                }
            }

        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET ALL ICD10
//    public DRGWSResult GetAllICD10(final DataSource datasource) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setSuccess(false);
//        result.setMessage("");
//        result.setResult("");
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :icd10 := MINOSUN.DRGPKGFUNCTION.get_all_icd10(); end;");
//            statement.registerOutParameter("icd10", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("icd10");
//            ArrayList<ValidICD10> list_icd = new ArrayList<>();
//            while (resultset.next()) {
//                ValidICD10 cd = new ValidICD10();
//                cd.setValidcode(resultset.getString("validcode"));
//                cd.setDescription(resultset.getString("description"));
//                cd.setCode(resultset.getString("code").trim());
//                list_icd.add(cd);
//            }
//            if (list_icd.size() > 0) {
//                result.setResult(utility.objectMapper().writeValueAsString(list_icd));
//                result.setMessage("OK");
//                result.setSuccess(true);
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //Removed duplication for Proc Code with extension code
    public static <T> ArrayList<T> RemovedDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    // GET ICD10 FOR PRE MDC VALIDATION PROCESS
    public DRGWSResult GetICD10PreMDC(final DataSource datasource, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statementA = connection.prepareCall("begin :accpdxs := MINOSUN.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
            statementA.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            statementA.setString("pdx", utility.CleanCode(pdx));
            statementA.execute();
            ResultSet resultsetA = (ResultSet) statementA.getObject("accpdxs");
            if (resultsetA.next()) {
                ICD10PreMDCResult premdc = new ICD10PreMDCResult();
                premdc.setAccPDX(resultsetA.getString("ACCPDX"));
                premdc.setAgeDMin(resultsetA.getString("AGEDMIN"));
                premdc.setAgeDUse(resultsetA.getString("AGEDUSE"));
                premdc.setAgeMax(resultsetA.getString("AGEMAX"));
                premdc.setAgeMin(resultsetA.getString("AGEMIN"));
                premdc.setCC(resultsetA.getString("CC"));
                premdc.setCCRow(resultsetA.getString("CCROW"));
                premdc.setCode(resultsetA.getString("CODE"));
                premdc.setHIV_AX(resultsetA.getString("HIV_AX"));
                premdc.setMDC(resultsetA.getString("MDC"));
                premdc.setMainCC(resultsetA.getString("MAINCC"));
                premdc.setPDC(resultsetA.getString("PDC"));
                premdc.setSex(resultsetA.getString("SEX"));
                premdc.setTrauma(resultsetA.getString("TRAUMA"));
                result.setMessage(resultsetA.getString("CCROW"));
                result.setResult(utility.objectMapper().writeValueAsString(premdc));
                result.setSuccess(true);
            } else {
                CallableStatement statement = connection.prepareCall("begin :accpdxs := MINOSUN.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
                statement.registerOutParameter("accpdxs", OracleTypes.CURSOR);
                statement.setString("pdx", utility.CleanCode(pdx.substring(0, pdx.length() - 1)));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("accpdxs");
                if (resultset.next()) {
                    ICD10PreMDCResult premdc = new ICD10PreMDCResult();
                    premdc.setAccPDX(resultset.getString("ACCPDX"));
                    premdc.setAgeDMin(resultset.getString("AGEDMIN"));
                    premdc.setAgeDUse(resultset.getString("AGEDUSE"));
                    premdc.setAgeMax(resultset.getString("AGEMAX"));
                    premdc.setAgeMin(resultset.getString("AGEMIN"));
                    premdc.setCC(resultset.getString("CC"));
                    premdc.setCCRow(resultset.getString("CCROW"));
                    premdc.setCode(resultset.getString("CODE"));
                    premdc.setHIV_AX(resultset.getString("HIV_AX"));
                    premdc.setMDC(resultset.getString("MDC"));
                    premdc.setMainCC(resultset.getString("MAINCC"));
                    premdc.setPDC(resultset.getString("PDC"));
                    premdc.setSex(resultset.getString("SEX"));
                    premdc.setTrauma(resultset.getString("TRAUMA"));
                    result.setMessage(resultset.getString("CCROW"));
                    result.setResult(utility.objectMapper().writeValueAsString(premdc));
                    result.setSuccess(true);
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
    public DRGWSResult GetClaimDuplication(final DataSource datasource, final String accre, final String claimnum, final String series) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :dupnclaims := MINOSUN.DRGPKGFUNCTION.GET_CHECK_DUPLICATE(:accre,:claimnum,:series); end;");
            statement.registerOutParameter("dupnclaims", OracleTypes.CURSOR);
            statement.setString("accre", accre.trim());
            statement.setString("claimnum", claimnum.trim());
            statement.setString("series", series.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("dupnclaims");
            if (resultSet.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//count rvs value
    public DRGWSResult CountProc(final DataSource datasource, final String codes) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        //  int icd9 = 0;
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :count_output := MINOSUN.DRGPKGFUNCTION.GET_COUNT(:codes); end;");
            statement.registerOutParameter("count_output", OracleTypes.CURSOR);
            statement.setString("codes", utility.CleanCode(codes).trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("count_output");
            if (resultSet.next()) {
                // icd9 = Integer.parseInt(rest.getString("countrest"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public Method for Insert DRG Auditrail
    public DRGWSResult InsertDRGAuditTrail(final DataSource datasource,
            final String details,
            final String status,
            final String series,
            final String claimnum,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_AUDITRAIL(:Message,:Code,:udatein,:udesc,:ustats,:useries,:uclaimnumber,:ufilecontent)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("udatein", utility.SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
            auditrail.setString("udesc", details);
            auditrail.setString("ustats", status);
            auditrail.setString("useries", series);
            auditrail.setString("uclaimnumber", claimnum);
            auditrail.setString("ufilecontent", filecontent);
            auditrail.executeUpdate();
            //======================================================================
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("success Date:" + utility.SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
            } else {
                result.setMessage(auditrail.getString("Message") + " Date:" + utility.SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET PDC MAIN TABLE
    public DRGWSResult InsertDRGResult(final DataSource datasource,
            final String claimnum,
            final String series,
            final String lhio,
            final String pdx,
            final String sdx,
            final String proc,
            final String result_id) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement grouperdata = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_DRG_RESULT(:Message,:Code,:uclaimid,:uresultid,:useries,:utags,:ulhio,:updxcode,:usdxcode,:uproc)");
            grouperdata.registerOutParameter("Message", OracleTypes.VARCHAR);
            grouperdata.registerOutParameter("Code", OracleTypes.INTEGER);
            //=====================================================================Process SDx duplication================================
            List<String> SDXList = Arrays.asList(sdx.split(","));
            LinkedList<String> duplicate = new LinkedList<>();
            LinkedList<String> newlist = new LinkedList<>();
            for (int y = 0; y < SDXList.size(); y++) {
                newlist.add(SDXList.get(y));
            }
            for (int i = 0; i < SDXList.size() - 1; i++) {
                for (int j = i + 1; j < SDXList.size(); j++) {
                    if (SDXList.get(i).equals(SDXList.get(j)) && (i != j)) {
                        duplicate.add(SDXList.get(j));
                        newlist.remove(SDXList.get(j));
                    }
                }
            }
            //==================================================================
            //START HERE
            List<String> ProcList = Arrays.asList(proc.split(","));
            LinkedList<String> procduplicate = new LinkedList<>();
            LinkedList<String> procnewlist = new LinkedList<>();
            for (int y = 0; y < ProcList.size(); y++) {
                procnewlist.add(ProcList.get(y));
            }
            for (int i = 0; i < ProcList.size() - 1; i++) {
                for (int j = i + 1; j < ProcList.size(); j++) {
                    if (ProcList.get(i).equals(ProcList.get(j)) && (i != j)) {
                        procduplicate.add(ProcList.get(j));
                        procnewlist.remove(ProcList.get(j));
                    }
                }
            }
            //=====================================================================End Process SDx duplication================================ 
            grouperdata.setString("uclaimid", claimnum.trim());
            grouperdata.setString("uresultid", result_id.trim());
            grouperdata.setString("useries", series.trim());
            grouperdata.setString("utags", "FG");
            grouperdata.setString("ulhio", lhio.trim());
            grouperdata.setString("updxcode", pdx.trim());
            if (duplicate.isEmpty()) {
                grouperdata.setString("usdxcode", sdx.trim());
            } else {
                grouperdata.setString("usdxcode", String.join(",", newlist));
            }
            grouperdata.setString("uproc", proc.trim());
            grouperdata.executeUpdate();
            result.setMessage(grouperdata.getString("Code"));
            result.setResult(grouperdata.getString("Message"));
            if (grouperdata.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(grouperdata.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult PROCESS PROCEDURE
    public String ProcedureExecute(final CombinationCode combinationcode) {
        String result = "";
        List<String> indexlist = Arrays.asList(combinationcode.getIndexlist().split(","));
        List<String> comcode = Arrays.asList(combinationcode.getComcode().split(","));
        List<String> proclist = Arrays.asList(combinationcode.getProclist().split(","));
        Set<String> set1 = new HashSet<>(proclist);
        set1.removeAll(indexlist);
        for (int y = 0; y < comcode.size(); y++) {
            set1.add(comcode.get(y));
        }
        result = String.join(", ", set1);
        return result;
    }

    //public MethodResult PROCESS PROCEDURE BEFORE SAVING SA DATA TO DATABASE
    public String FrontProcedureExecute(final CombinationCode combinationcode) {
        String result = "";
        List<String> indexlist = Arrays.asList(combinationcode.getIndexlist().split(","));
        List<String> comcode = Arrays.asList(combinationcode.getComcode().split(","));
        List<String> proclist = Arrays.asList(combinationcode.getProclist().split(","));
        Set<String> set1 = new HashSet<>(proclist);
        set1.removeAll(indexlist);
        for (int y = 0; y < comcode.size(); y++) {
            if (comcode.get(y).equals("")) {
            } else {
                set1.add(comcode.get(y));
            }
        }
        result = set1.toString();
        return result;
    }

    // GET AGE VALIDATION THIS AREA
    public DRGWSResult AgeConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String age_day,
            final String age_min_year) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement stateA = connection.prepareCall("begin :age_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_AGE(:p_pdx_code,:age_day,:age_min_year); end;");
            stateA.registerOutParameter("age_validation", OracleTypes.CURSOR);
            stateA.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            stateA.setString("age_day", age_day);
            stateA.setString("age_min_year", age_min_year);
            stateA.execute();
            ResultSet resultSetA = (ResultSet) stateA.getObject("age_validation");
            if (resultSetA == null) {
                CallableStatement stateB = connection.prepareCall("begin :age_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_AGE(:p_pdx_code,:age_day,:age_min_year); end;");
                stateB.registerOutParameter("age_validation", OracleTypes.CURSOR);
                stateB.setString("p_pdx_code", p_pdx_code.substring(0, p_pdx_code.length() - 1).replaceAll("\\.", "").toUpperCase().trim());
                stateB.setString("age_day", age_day);
                stateB.setString("age_min_year", age_min_year);
                stateB.execute();
                ResultSet resultSetB = (ResultSet) stateB.getObject("age_validation");
                if (resultSetB == null) {
                } else {
                    if (resultSetB.next()) {
                        result.setSuccess(true);
                    }
                }
            } else {
                if (resultSetA.next()) {
                    result.setSuccess(true);
                }
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET GENDER VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexValidation = connection.prepareCall("begin :gender_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gender); end;");
            getSexValidation.registerOutParameter("gender_validation", OracleTypes.CURSOR);
            getSexValidation.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            getSexValidation.setString("gender", gender);
            getSexValidation.execute();
            ResultSet getSexValidationResult = (ResultSet) getSexValidation.getObject("gender_validation");
            if (getSexValidationResult.next()) {
                result.setSuccess(true);
            } else {
                CallableStatement statement = connection.prepareCall("begin :gender_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gender); end;");
                statement.registerOutParameter("gender_validation", OracleTypes.CURSOR);
                statement.setString("p_pdx_code", utility.CleanCode(p_pdx_code.substring(0, p_pdx_code.length() - 1)).trim());
                statement.setString("gender", gender);
                statement.execute();
                ResultSet resultSet = (ResultSet) statement.getObject("gender_validation");
                if (resultSet.next()) {
                    result.setSuccess(true);
                }
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET SEX PROC VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidationProc(final DataSource datasource,
            final String procode,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexProcValidation = connection.prepareCall("begin :age_proc_validation := MINOSUN.DRGPKGFUNCTION.PROC_AGE_VALIDATION(:procode,:gender); end;");
            getSexProcValidation.registerOutParameter("age_proc_validation", OracleTypes.CURSOR);
            getSexProcValidation.setString("procode", utility.CleanCode(procode).trim());
            getSexProcValidation.setString("gender", gender.toUpperCase());
            getSexProcValidation.execute();
            ResultSet getSexProcValidationResult = (ResultSet) getSexProcValidation.getObject("age_proc_validation");
            if (getSexProcValidationResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //CHECKING OF ICD9CM CODE IF EXIST IN THE LIBRARY
    // GET SEX PROC VALIDATION THIS AREA
    public DRGWSResult CheckICD9cm(final DataSource datasource,
            final String rvs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getResult = connection.prepareCall("begin :icd9code_output := MINOSUN.DRGPKGFUNCTION.GET_ICD9(:rvs); end;");
            getResult.registerOutParameter("icd9code_output", OracleTypes.CURSOR);
            getResult.setString("rvs", rvs.trim());
            getResult.execute();
            ResultSet getResultSet = (ResultSet) getResult.getObject("icd9code_output");
            if (getResultSet.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult ValidateHcfSector(final DataSource datasource,
            final String upmcc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := MINOSUN.UHCDRGPKG.GETPRIVATEFACILITY(:upmcc); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmcc", upmcc.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("v_result");
            if (resultSet.next()) {
                result.setMessage("OK");
                result.setResult(resultSet.getString("SOC_SECTOR"));
                result.setSuccess(true);
            } else {
                result.setMessage("Facility sector not found");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CF5Method.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
