/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.seekermethod;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class GetCF5Parameter {

    private final Utility utility = new Utility();

    //  GET DRG DATAINFO
    public SeekerResult CombinedResult(final DataSource datasource, final String series) {
        SeekerResult result = utility.SeekerResult();
        result.setSuccess(false);
        try {
            DRGWSResult getinfo = this.GETCF5DATA(datasource, series);
            if (getinfo.isSuccess()) {
                result.setInfo(getinfo.getResult());
                ENDRGINFO endrginfo = utility.objectMapper().readValue(getinfo.getResult(), ENDRGINFO.class);
                DRGWSResult dxresult = this.GET_DIAGNOSIS(datasource, series, endrginfo.getClaimid());
                if (dxresult.isSuccess() && dxresult.getResult() != null) {
                    result.setDxdiag(dxresult.getResult());
                }
                DRGWSResult rvsresult = this.GET_PROCEDURE(datasource, series, endrginfo.getClaimid());
                if (rvsresult.isSuccess() && rvsresult.getResult() != null) {
                    result.setProcedure(rvsresult.getResult());
                }
                DRGWSResult warningresult = this.GET_WARNING_ERROR(datasource, series, endrginfo.getClaimid());
                if (warningresult.isSuccess() && warningresult.getResult() != null) {
                    result.setWarning(warningresult.getResult());
                }
            }
            result.setSuccess(getinfo.isSuccess());
            result.setMessage(getinfo.getMessage());
        } catch (IOException ex) {
            result.setMessage(ex.getMessage());
            Logger.getLogger(GetCF5Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //  GET DRG DATAINFO
    public DRGWSResult GETCF5DATA(final DataSource datasource, final String claimseries) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :cf5data := DRG_SHADOWBILLING.GETCF5DATA(:claimseries); end;");
            statement.registerOutParameter("cf5data", OracleTypes.CURSOR);
            statement.setString("claimseries", claimseries);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("cf5data");
            if (resultset.next()) {
                ENDRGINFO endrginfo = new ENDRGINFO();
                endrginfo.setSeries(resultset.getString("SERIES"));
                endrginfo.setAdmweight(resultset.getString("NB_ADMWEIGHT"));
                endrginfo.setPdx(resultset.getString("PDX_CODE"));
                endrginfo.setLhio(resultset.getString("LHIO"));
                endrginfo.setClaimid(resultset.getString("CLAIMNUMBER"));
                endrginfo.setAccreno(resultset.getString("ACCRENO"));
                result.setMessage("DATA HAS RESULT");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(endrginfo));
            } else {
                result.setMessage("NO DATA FOUND");
                result.setSuccess(false);
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetCF5Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //  GET DRG SECONDARY DIAGNOSIS
    public DRGWSResult GET_DIAGNOSIS(final DataSource datasource, final String claimseries, final String claim_id) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :cf5data := DRG_SHADOWBILLING.GET_DIAGNOSIS(:claimseries,:claim_id); end;");
            statement.registerOutParameter("cf5data", OracleTypes.CURSOR);
            statement.setString("claimseries", claimseries);
            statement.setString("claim_id", claim_id);
            statement.execute();
            ArrayList<ENSDX> ensdxlist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("cf5data");
            while (resultset.next()) {
                ENSDX ensdx = new ENSDX();
                ensdx.setClaimid(resultset.getString("CLAIM_ID"));
                ensdx.setLhio(resultset.getString("LHIO"));
                ensdx.setSdx(resultset.getString("SDX_CODE"));
                ensdx.setSeries(resultset.getString("SERIES"));
                ensdxlist.add(ensdx);
            }

            if (ensdxlist.size() > 0) {
                result.setMessage("DATA HAS RESULT");
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(ensdxlist));

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetCF5Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //  GET DRG PROCEDURES
    public DRGWSResult GET_PROCEDURE(final DataSource datasource, final String claimseries, final String c_claimid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :cf5data := DRG_SHADOWBILLING.GET_PROCEDURE(:claimseries,:c_claimid); end;");
            statement.registerOutParameter("cf5data", OracleTypes.CURSOR);
            statement.setString("claimseries", claimseries);
            statement.setString("claim_id", c_claimid);
            statement.execute();
            ArrayList<ENRVS> enrvslist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("cf5data");
            while (resultset.next()) {
                ENRVS enrvs = new ENRVS();
                enrvs.setRvs(resultset.getString("RVS"));
                enrvs.setLaterality(resultset.getString("LATERALITY"));
                enrvs.setExt1(resultset.getString("EXT1_CODE"));
                enrvs.setExt2(resultset.getString("EXT2_CODE"));
                enrvs.setSeries(resultset.getString("SERIES"));
                enrvs.setLhio(resultset.getString("LHIO"));
                enrvs.setClaimid(resultset.getString("CLAIM_ID"));
                enrvslist.add(enrvs);

            }

            if (enrvslist.size() > 0) {
                result.setMessage("DATA HAS RESULT");
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(enrvslist));

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetCF5Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //  GET DRG WARNING ERROR
    public DRGWSResult GET_WARNING_ERROR(final DataSource datasource, final String claimseries, final String claim_id) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :warningresult := DRG_SHADOWBILLING.GET_WARNING_ERROR(:claimseries,:claim_id); end;");
            statement.registerOutParameter("warningresult", OracleTypes.CURSOR);
            statement.setString("claimseries", claimseries);
            statement.setString("claim_id", claim_id);
            statement.execute();
            ArrayList<ENWARNINGERROR> enwarningerrorlist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("warningresult");
            while (resultset.next()) {
                ENWARNINGERROR warningerror = new ENWARNINGERROR();
                warningerror.setSeries(resultset.getString("SERIES"));
                warningerror.setErrorcode(resultset.getString("ERROR_CODE"));
                warningerror.setData(resultset.getString("DATA"));
                warningerror.setDescription(resultset.getString("DESCRIPTION"));
                warningerror.setLhio(resultset.getString("LHIO"));
                warningerror.setResultid(resultset.getString("RESULT_ID"));
                warningerror.setClaimid(resultset.getString("CLAIM_ID"));
                enwarningerrorlist.add(warningerror);
            }

            if (enwarningerrorlist.size() > 0) {
                result.setMessage("DATA HAS RESULT");
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(enwarningerrorlist));

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetCF5Parameter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
