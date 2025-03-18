/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods.phic;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.utilities.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
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
public class phic {

    public phic() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GETPATIENTBDAY(
            final DataSource datasource,
            final String seriesnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :nclaims := DRG_SHADOWBILLING.UHCDRGPKG.GET_NCLAIMS(:seriesnum); end;");
            statement.registerOutParameter("nclaims", OracleTypes.CURSOR);
            statement.setString("seriesnum", seriesnum.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("nclaims");
            if (resultSet.next()) {
                if (resultSet.getString("DATEOFBIRTH") != null && !resultSet.getString("DATEOFBIRTH").isEmpty() && !resultSet.getString("DATEOFBIRTH").equals("")) {
                    result.setResult(resultSet.getString("DATEOFBIRTH"));
                    result.setSuccess(true);
                    result.setMessage("OK");
                }
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(phic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult GeteClaims(
            final DataSource datasource,
            final String seriesnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.UHCDRGPKG.GETPATIENTDATA(:seriesnum); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("seriesnum", seriesnum.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("v_result");
            if (resultSet.next()) {
                NClaimsData nclaimsdata = new NClaimsData();
                // EXPIREDDATE
                nclaimsdata.setExpiredDate(resultSet.getString("EXPIREDDATE") == null
                        || resultSet.getString("EXPIREDDATE").isEmpty()
                        || resultSet.getString("EXPIREDDATE").equals("") ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("EXPIREDDATE")));
                //EXPIREDTIME
                nclaimsdata.setExpiredTime(resultSet.getString("EXPIREDTIME") == null
                        || resultSet.getString("EXPIREDTIME").isEmpty()
                        || resultSet.getString("EXPIREDTIME").equals("") ? "" : utility.SimpleDateFormat("hh:mmaa").format(resultSet.getTimestamp("EXPIREDTIME")));
                //ADMISSIONDATE
                nclaimsdata.setAdmissionDate(resultSet.getString("ADMISSIONDATE") == null
                        || resultSet.getString("ADMISSIONDATE").isEmpty()
                        || resultSet.getString("ADMISSIONDATE").equals("") ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("ADMISSIONDATE")));
                //TIMEADMISSION
                nclaimsdata.setTimeAdmission(resultSet.getString("TIMEADMISSION") == null
                        || resultSet.getString("TIMEADMISSION").isEmpty()
                        || resultSet.getString("TIMEADMISSION").equals("") ? "" : utility.SimpleDateFormat("hh:mmaa").format(resultSet.getTimestamp("TIMEADMISSION")));
                //DISCHARGETIME
                nclaimsdata.setTimeDischarge(resultSet.getString("TIMEDISCHARGE") == null
                        || resultSet.getString("TIMEDISCHARGE").isEmpty()
                        || resultSet.getString("TIMEDISCHARGE").equals("") ? "" : utility.SimpleDateFormat("hh:mmaa").format(resultSet.getTimestamp("TIMEDISCHARGE")));
                //DISCHARGEDATE
                nclaimsdata.setDischargeDate(resultSet.getString("DISCHARGEDATE") == null
                        || resultSet.getString("DISCHARGEDATE").isEmpty()
                        || resultSet.getString("DISCHARGEDATE").equals("") ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("DISCHARGEDATE")));
                //DATEOFBIRTH
                if (this.GETPATIENTBDAY(datasource, seriesnum).isSuccess()) {
                    if (utility.isParsableDate(this.GETPATIENTBDAY(datasource, seriesnum).getResult(), "MM/dd/yyyy")) {
                        nclaimsdata.setDateofBirth(!this.GETPATIENTBDAY(datasource, seriesnum).isSuccess() ? ""
                                : utility.SimpleDateFormat("MM-dd-yyyy").format(utility.SimpleDateFormat("MM/dd/yyyy").parse(this.GETPATIENTBDAY(datasource, seriesnum).getResult())));
                    } else {
                        nclaimsdata.setDateofBirth("");
                    }
                } else {
                    nclaimsdata.setDateofBirth("");
                }
                //CLAIMNUMBER
                nclaimsdata.setPclaimnumber(resultSet.getString("CLAIMNUMBER") == null
                        || resultSet.getString("CLAIMNUMBER").isEmpty()
                        || resultSet.getString("CLAIMNUMBER").equals("") ? "" : resultSet.getString("CLAIMNUMBER"));
                //GENDER
                nclaimsdata.setGender(resultSet.getString("GENDER") == null ? "" : resultSet.getString("GENDER"));
                //SERIES
                nclaimsdata.setSeries(resultSet.getString("SERIES") == null ? "" : resultSet.getString("SERIES"));
                //DISCHARGETYPE
                nclaimsdata.setDischargeType(resultSet.getString("DISCHARGETYPE") == null
                        || resultSet.getString("DISCHARGETYPE").isEmpty()
                        || resultSet.getString("DISCHARGETYPE").equals("") ? "" : resultSet.getString("DISCHARGETYPE"));
                result.setResult(utility.objectMapper().writeValueAsString(nclaimsdata));
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setResult("514");
                result.setMessage("CF5 " + seriesnum + " not found in eClaims DB");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(phic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }
}
