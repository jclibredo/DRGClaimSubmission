/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities;

import drg.drgclaimsubmission.structures.CombinationCode;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.ICD10PreMDCResult;
import drg.drgclaimsubmission.structures.ValidICD10;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
 * @author MinoSun
 */
@RequestScoped
public class GrouperMethod {

    public GrouperMethod() {
    }

    private final Utility utility = new Utility();

    //GET ICD10 FOR KEY VALUE PAIR VALIDATION
    public DRGWSResult GetICD10(final DataSource datasource, final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :p_validcode := MINOSUN.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            statement.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            statement.setString("p_icd10_code", p_icd10_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_validcode");
            if (resultset.next()) {

                result.setSuccess(true);
                result.setResult(resultset.getString("validcode"));
                result.setMessage("Record Found");
            } else {
                result.setSuccess(false);
                result.setMessage("No ICD10 Record Found");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //RVS CONVERTER TO ICD9CM
    public DRGWSResult GetICD9cm(final DataSource datasource, final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            String ProcListNew = "";
            List<String> FinalNewProcList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
            statement.registerOutParameter("converter", OracleTypes.CURSOR);
            statement.setString("rvs_code", rvs_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("converter");
            if (resultset.next()) {
                ProcListNew = resultset.getString("ICD9CODE");
                List<String> ConverterResult = Arrays.asList(ProcListNew.split(","));
                for (int g = 0; g < ConverterResult.size(); g++) {
                    String ICD9Codes = ConverterResult.get(g);
                    FinalNewProcList.add(ICD9Codes);
                }
                result.setResult(ProcListNew);
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET ALL ICD10
    public DRGWSResult GetAllICD10(final DataSource datasource) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :icd10 := MINOSUN.DRGPKGFUNCTION.get_all_icd10(); end;");
            statement.registerOutParameter("icd10", OracleTypes.CURSOR);

            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("icd10");
            ArrayList<ValidICD10> list_icd = new ArrayList<>();
            while (resultset.next()) {
                result.setSuccess(true);
                ValidICD10 cd = new ValidICD10();
                cd.setValidcode(resultset.getString("validcode"));
                cd.setDescription(resultset.getString("description"));
                cd.setCode(resultset.getString("code"));
                list_icd.add(cd);
            }

            if (list_icd.size() > 0) {
                String datas = utility.objectMapper().writeValueAsString(list_icd);// supplier.toString();
                result.setResult(datas);
                result.setMessage("Succesfully Retrieved Data");
                result.setSuccess(true);
            } else {
                result.setMessage("No Record Found");
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :accpdxs := MINOSUN.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
            statement.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            statement.setString("pdx", pdx);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("accpdxs");
            if (resultset.next()) {
                result.setSuccess(true);
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
                result.setResult(utility.objectMapper().writeValueAsString(premdc));
                result.setMessage(resultset.getString("CCROW"));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
    public DRGWSResult GetClaimDuplication(final DataSource datasource, final String accre, final String claimnum, final String series) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getduplication = connection.prepareCall("begin :dupnclaims := MINOSUN.DRGPKGFUNCTION.GET_CHECK_DUPLICATE(:accre,:claimnum,:series); end;");
            getduplication.registerOutParameter("dupnclaims", OracleTypes.CURSOR);
            getduplication.setString("accre", accre);
            getduplication.setString("claimnum", claimnum);
            getduplication.setString("series", series);
            getduplication.execute();
            ResultSet getduplicationResult = (ResultSet) getduplication.getObject("dupnclaims");
            if (getduplicationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//count rvs value
    public int CountProc(final DataSource datasource, final String codes) {
        int icd9 = 0;
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getResult = connection.prepareCall("begin :count_output := MINOSUN.DRGPKGFUNCTION.GET_COUNT(:codes); end;");
            getResult.registerOutParameter("count_output", OracleTypes.CURSOR);
            getResult.setString("codes", codes);
            getResult.execute();
            ResultSet rest = (ResultSet) getResult.getObject("count_output");
           if (rest.next()) {
                icd9 = Integer.parseInt(rest.getString("countrest"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return icd9;
        
    }

    //public Method for Insert DRG Auditrail
    public DRGWSResult InsertDRGAuditTrail(final DataSource datasource,
            final String details,
            final String status,
            final String series,
            final String claimnum,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {

            SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            Date date = new Date();
            CallableStatement auditrail = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_AUDITRAIL(:Message,:Code,:datein,:discreption,:stats,:seriesnum,:claimnum,:filecontent)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            //=====================================================================End Process SDx duplication================================ 
            auditrail.setString("datetime", sdf.format(date));
            auditrail.setString("details", details);
            auditrail.setString("stats", status);
            auditrail.setString("seriesnum", series);
            auditrail.setString("claimnum", claimnum);
            auditrail.setString("filecontent", filecontent);
            auditrail.executeUpdate();
            //======================================================================
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(" Successfully save to logs Date:" + sdf.format(date));
            } else {
                result.setSuccess(false);
                result.setMessage(auditrail.getString("Message") + " Date:" + sdf.format(date));
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
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
        try (Connection connection = datasource.getConnection()) {
            String tagss = "FG";
            CallableStatement grouperdata = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_DRG_RESULT(:Message,:Code,:claimNum,:rest_id,:series,:tags,:lhio,:pdx,:sdx,:proc)");
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
            grouperdata.setString("claimNum", claimnum);
            grouperdata.setString("rest_id", result_id);
            grouperdata.setString("series", series);
            grouperdata.setString("tags", tagss.trim());
            grouperdata.setString("lhio", lhio);
            grouperdata.setString("pdx", pdx);
            if (duplicate.isEmpty()) {
                grouperdata.setString("sdx", sdx);
            } else {
                grouperdata.setString("sdx", String.join(",", newlist));
            }
            grouperdata.setString("proc", proc);
            grouperdata.executeUpdate();
            result.setMessage(grouperdata.getString("Code"));
            result.setResult(grouperdata.getString("Message"));
            if (grouperdata.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
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
    public String FrontProcedureExecute(final CombinationCode combinationcode) throws IOException {
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
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getAgeValidation = connection.prepareCall("begin :age_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_AGE(:p_pdx_code,:age_day,:age_min_year); end;");
            getAgeValidation.registerOutParameter("age_validation", OracleTypes.CURSOR);
            getAgeValidation.setString("p_pdx_code", p_pdx_code);
            getAgeValidation.setString("age_day", age_day);
            getAgeValidation.setString("age_min_year", age_min_year);
            getAgeValidation.execute();
            ResultSet getAgeValidationResult = (ResultSet) getAgeValidation.getObject("age_validation");
            if (getAgeValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET GENDER VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexValidation = connection.prepareCall("begin :gender_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gender); end;");
            getSexValidation.registerOutParameter("gender_validation", OracleTypes.CURSOR);
            getSexValidation.setString("p_pdx_code", p_pdx_code);
            getSexValidation.setString("gender", gender);
            getSexValidation.execute();
            ResultSet getSexValidationResult = (ResultSet) getSexValidation.getObject("gender_validation");
            if (getSexValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET SEX PROC VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidationProc(final DataSource datasource,
            final String procode,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexProcValidation = connection.prepareCall("begin :age_proc_validation := MINOSUN.DRGPKGFUNCTION.PROC_AGE_VALIDATION(:procode,:gender); end;");
            getSexProcValidation.registerOutParameter("age_proc_validation", OracleTypes.CURSOR);
            getSexProcValidation.setString("procode", procode);
            getSexProcValidation.setString("gender", gender);
            getSexProcValidation.execute();
            ResultSet getSexProcValidationResult = (ResultSet) getSexProcValidation.getObject("age_proc_validation");
            if (getSexProcValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //CHECKING OF ICD9CM CODE IF EXIST IN THE LIBRARY
    // GET SEX PROC VALIDATION THIS AREA
    public DRGWSResult CheckICD9cm(final DataSource datasource,
            final String rvs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getResult = connection.prepareCall("begin :icd9code_output := MINOSUN.DRGPKGFUNCTION.GET_ICD9(:rvs); end;");
            getResult.registerOutParameter("icd9code_output", OracleTypes.CURSOR);
            getResult.setString("rvs", rvs);
            getResult.execute();
            ResultSet getResultSet = (ResultSet) getResult.getObject("icd9code_output");
            if (getResultSet.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
