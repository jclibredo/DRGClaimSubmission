/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * @author MINOSUN
 */
@RequestScoped
public class ValidateProcedures {

    public ValidateProcedures() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ValidateProcedures(final DataSource datasource, final PROCEDURE procedure, final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        ArrayList<String> errors = new ArrayList<>();
        PROCEDURE validateprocedure;
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!procedure.getRvsCode().trim().isEmpty()) {
                if (procedure.getRvsCode().trim().length() > 10) {
                    errors.add("506");
                }
                if (procedure.getLaterality().trim().isEmpty()) {
                    procedure.setLaterality("N");
                    errors.add("204");
                } else if (!Arrays.asList("L", "R", "B", "N").contains(procedure.getLaterality().trim())) {
                    procedure.setLaterality("N");
                    errors.add("204");
                }
                if (procedure.getExt1().trim().equals("")) {
                    procedure.setExt1("1");
                    errors.add("205");
                } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(procedure.getExt1().trim())) {
                    procedure.setExt1("1");
                    errors.add("206");
                } else if (procedure.getExt1().trim().length() > 1) {
                    procedure.setExt1("1");
                    errors.add("206");
                }
                if (procedure.getExt2().trim().equals("")) {
                    procedure.setExt1("1");
                    errors.add("207");
                } else if (!Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(procedure.getExt2().trim())) {
                    procedure.setExt1("1");
                    errors.add("208");
                } else if (procedure.getExt2().trim().length() > 1) {
                    errors.add("208");
                    procedure.setExt1("1");
                }
                if (!new CF5Method().CheckICD9cm(datasource, procedure.getRvsCode().trim()).isSuccess()) {
                    int gendercounter = 0;
                    CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
                    statement.registerOutParameter("converter", OracleTypes.CURSOR);
                    statement.setString("rvs_code", procedure.getRvsCode().trim());
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("converter");
                    if (resultset.next()) {
                        if (!resultset.getString("ICD9CODE").trim().isEmpty()) {
                            List<String> listicd9cm = Arrays.asList(resultset.getString("ICD9CODE").trim().split(","));
                            for (int ptr = 0; ptr < listicd9cm.size(); ptr++) {
                                if (new CF5Method().CountProc(datasource, listicd9cm.get(ptr).trim()).isSuccess()) {
                                    DRGWSResult sexvalidationresult = new CF5Method().GenderConfictValidationProc(datasource, listicd9cm.get(ptr).trim(), gender);
                                    if (!sexvalidationresult.isSuccess()) {
                                        gendercounter++;
                                    }
                                }
                            }
                        } else {
                            errors.add("203");
                        }
                    } else {
                        errors.add("203");
                    }
                    if (gendercounter > 0) {
                        errors.add("508");
                    }
                }

            }
            validateprocedure = procedure;
            if (errors.size() > 0) {
                result.setMessage("PROCEDURES has errors");
                validateprocedure.setRemarks(String.join(",", errors));
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(validateprocedure)); //validateprocedure.toString());
        } catch (IOException | SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateProcedures.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
