/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.utilities.GrouperMethod;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class ValidateXMLValues {

    public ValidateXMLValues() {
    }
    private final Utility utility = new Utility();
    private final InsertDRGClaims insertDRGClaims = new InsertDRGClaims();
    private final ValidateDRGClaims VDC = new ValidateDRGClaims();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult ValidateXMLValues(final DataSource datasource,
            final CF5 drg,
            final String lhio,
            final String claimseries,
            final String filecontent) throws SQLException, IOException {
        DRGWSResult result = utility.DRGWSResult();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        ArrayList<NClaimsData> nclaimsdataList = new ArrayList<>();
        ArrayList<String> xmlidlist = new ArrayList<>();
        ArrayList<String> listid = new ArrayList<>();

        SimpleDateFormat timeformat = utility.SimpleDateFormat("hh:mm a");
        SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
        String stats = "FAILED";

        try (Connection connection = datasource.getConnection()) {
            // GET DATA FROM ECLAIMS TABLE FOR FROMT VALIDATION
            CallableStatement getdrg_nclaims = connection.prepareCall("begin :nclaims := MINOSUN.UHCDRGPKG.GET_NCLAIMS(:seriesnumss); end;");
            getdrg_nclaims.registerOutParameter("nclaims", OracleTypes.CURSOR);
            getdrg_nclaims.setString("seriesnumss", claimseries.trim());
            getdrg_nclaims.execute();
            ResultSet getdrg_nclaims_result = (ResultSet) getdrg_nclaims.getObject("nclaims");
            while (getdrg_nclaims_result.next()) {
                NClaimsData nclaimsdata = new NClaimsData();

                //EXPIREDTIME
                if (getdrg_nclaims_result.getTimestamp("EXPIREDTIME") == null) {
                    nclaimsdata.setExpiredTime("");
                } else {
                    nclaimsdata.setExpiredTime(timeformat.format(getdrg_nclaims_result.getTimestamp("EXPIREDTIME")));
                }
                //TIMEADMISSION
                if (getdrg_nclaims_result.getTimestamp("TIMEADMISSION") == null) {
                    nclaimsdata.setTimeAdmission("");
                } else {
                    nclaimsdata.setTimeAdmission(timeformat.format(getdrg_nclaims_result.getTimestamp("TIMEADMISSION")));
                }
                //TIMEDISCHARGE
                if (getdrg_nclaims_result.getTimestamp("TIMEDISCHARGE") == null) {
                    nclaimsdata.setTimeDischarge("");
                } else {
                    nclaimsdata.setTimeDischarge(timeformat.format(getdrg_nclaims_result.getTimestamp("TIMEDISCHARGE")));
                }
                //DISCHARGEDATE
                if (getdrg_nclaims_result.getTimestamp("DISCHARGEDATE") == null) {
                    nclaimsdata.setDischargeDate("");
                } else {
                    nclaimsdata.setDischargeDate(dateformat.format(getdrg_nclaims_result.getTimestamp("DISCHARGEDATE")));
                }
                //ADMISSIONDATE
                if (getdrg_nclaims_result.getTimestamp("ADMISSIONDATE") == null) {
                    nclaimsdata.setAdmissionDate("");
                } else {
                    nclaimsdata.setAdmissionDate(dateformat.format(getdrg_nclaims_result.getTimestamp("ADMISSIONDATE")));
                }
                //EXPIREDDATE
                if (getdrg_nclaims_result.getTimestamp("EXPIREDDATE") == null) {
                    nclaimsdata.setExpiredDate("");
                } else {
                    nclaimsdata.setExpiredDate(dateformat.format(getdrg_nclaims_result.getTimestamp("EXPIREDDATE")));
                }
                //DATEOFBIRTH
                if (getdrg_nclaims_result.getTimestamp("DATEOFBIRTH") == null) {
                    nclaimsdata.setDateofBirth("");
                } else {
                    nclaimsdata.setDateofBirth(dateformat.format(getdrg_nclaims_result.getTimestamp("DATEOFBIRTH")));
                }
                //CLAIMNUMBER
                nclaimsdata.setPclaimnumber(getdrg_nclaims_result.getString("CLAIMNUMBER").trim());
                //GENDER
                nclaimsdata.setGender(getdrg_nclaims_result.getString("GENDER").trim());
                nclaimsdata.setSeries(getdrg_nclaims_result.getString("SERIES").trim());
                //DISCHARGETYPE
                nclaimsdata.setDischargeType(getdrg_nclaims_result.getString("DISCHARGETYPE").trim());
                nclaimsdataList.add(nclaimsdata);
            }

            CF5 drgs = new CF5();
            if (!drg.getpHospitalCode().isEmpty()) {
                KeyPerValueError viewerrors = utility.KeyPerValueError();
                // VALIDATE IF CLAIMS IS EXISTED ALREADY
                DRGWSResult getdupresults = gm.GetClaimDuplication(datasource, drg.getpHospitalCode().trim(), drg.getDrgclaim().getClaimNumber(), claimseries);
                System.out.println(getdupresults);
                if (!getdupresults.isSuccess()) {
                    String claimID = drg.getDrgclaim().getClaimNumber();
                    if (claimID.trim().length() > 0) {
                        xmlidlist.add(claimID);
                        //---------------------------------------------------------------------
                        for (int y = 0; y < nclaimsdataList.size(); y++) {
                            String eclaimsid = nclaimsdataList.get(y).getPclaimnumber();
                            //-----------------------------------------------------------
                            if (claimID.equals(eclaimsid)) {
                                DRGCLAIM drgclaims = new DRGCLAIM();
                                ArrayList<String> errorlist = new ArrayList<>();
                                ArrayList<String> SecondaryData = new ArrayList<>();
                                ArrayList<String> ProcedureData = new ArrayList<>();
                                ArrayList<String> warningerror = new ArrayList<>();
                                ArrayList<String> duplproc = new ArrayList<>();
                                ArrayList<String> duplsdx = new ArrayList<>();
                                //VALIDATE DRG VALUES
                                DRGWSResult vprodResult = VDC.ValidateDRGClaims(datasource, drg.getDrgclaim(), nclaimsdataList.get(y));
                                DRGCLAIM drgclaim = utility.objectMapper().readValue(vprodResult.getResult(), DRGCLAIM.class);
                                drgclaims.setClaimNumber(drgclaim.getClaimNumber());
                                drgclaims.setPrimaryCode(drgclaim.getPrimaryCode());
                                drgclaims.setNewBornAdmWeight(drgclaim.getNewBornAdmWeight());
                                drgclaims.setRemarks(drgclaim.getRemarks());
                                drgclaims.setPROCEDURES(drgclaim.getPROCEDURES());
                                drgclaims.setSECONDARYDIAGS(drgclaim.getSECONDARYDIAGS());
                                //-----------------------------------------------------

                                if (!drgclaims.getRemarks().isEmpty()) {
                                    errorlist.add(drgclaims.getRemarks());
                                }
                                for (int proc = 0; proc < drgclaims.getPROCEDURES().getPROCEDURE().size(); proc++) {
                                    //=============================================================================
                                    String ProcsCode = drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode();
                                    String ext1 = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt1();
                                    String ext2 = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt2();
                                    String lat = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getLaterality();
                                    String remarks = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks();
                                    //=========================================================================
                                    //------------------------------------------------------------------------------
                                    if (!remarks.isEmpty()) {
                                        warningerror.add(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks());
                                    }
                                    ProcAssign procassign = new ProcAssign();
                                    if (ext1.isEmpty()) {
                                        procassign.setEx1("1");
                                    }
                                    if (ext2.isEmpty()) {
                                        procassign.setEx2("1");
                                    }
                                    if (ext2.isEmpty()) {
                                    }
                                    ProcedureData.add(ProcsCode + "+" + ext1 + "" + ext2);
                                }
                                for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                                    String SDxCode = drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode();
                                    if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                                        warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                                    }
                                    SecondaryData.add(SDxCode);
                                }
                                for (int i = 0; i < SecondaryData.size() - 1; i++) {
                                    for (int j = i + 1; j < SecondaryData.size(); j++) {
                                        if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                            warningerror.add("503");
                                            duplsdx.add(String.valueOf(j));
                                        }
                                    }
                                }
                                //==================================================================
                                for (int i = 0; i < ProcedureData.size() - 1; i++) {
                                    for (int j = i + 1; j < ProcedureData.size(); j++) {
                                        if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                                            // warningerror.add(" RVS : " + ProcedureData.get(j) + " has duplicate");
                                            warningerror.add("508");
                                            duplproc.add(String.valueOf(j));
                                        }
                                    }
                                }

                                //  drgs.getDRGCLAIM().add(drgclaim);
                                drgs.setDrgclaim(drgclaim);
                                //   ================================================================== 
                                DRGWSResult insertDRGClaimsResult = insertDRGClaims.InsertDRGClaims(drgs.getDrgclaim(),
                                        datasource, nclaimsdataList.get(y), lhio, claimseries, drg.getpHospitalCode(),
                                        drgs.getDrgclaim().getClaimNumber(),
                                        String.join(",", duplproc), String.join(",", duplsdx), filecontent);

                                //RETRURNING ERROR MESSAGE
                                viewerrors.setRemarks("SAVE TO DB: " + insertDRGClaimsResult.getMessage());
                                viewerrors.setSeries(claimseries);
                                viewerrors.setClaimid(claimID);
                                viewerrors.setErrors(String.join(",", errorlist));
                                viewerrors.setWarningerror(String.join(",", warningerror));
                                allErrorList.add(viewerrors);
                                listid.add(nclaimsdataList.get(y).getPclaimnumber());
                                break;
                            }

                        }

                    } else {

                        String details = "DRG ClaimNumber is required";
                        String series = claimseries;
                        String claimnum = "";
                        DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, claimnum, filecontent);
                        viewerrors.setClaimid(drg.getDrgclaim().getClaimNumber());
                        viewerrors.setRemarks("");
                        viewerrors.setSeries(claimseries);
                        viewerrors.setErrors(details + "| logs " + auditrail.getMessage());
                        viewerrors.setWarningerror("");
                        allErrorList.add(viewerrors);

                    }
                } else {
                    //DUPLICATE OF SUBMISSION
                    String details = drg.getDrgclaim().getClaimNumber() + " Claim Number has duplicate from existing claim";
                    String series = claimseries;
                    String claimnum = drg.getDrgclaim().getClaimNumber();
                    DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, claimnum, filecontent);
                    viewerrors.setClaimid(drg.getDrgclaim().getClaimNumber());
                    viewerrors.setRemarks("");
                    viewerrors.setSeries(claimseries);
                    viewerrors.setErrors("Claim Number " + details + " | " + auditrail.getMessage());
                    viewerrors.setWarningerror("");
                    allErrorList.add(viewerrors);

                }
                result.setSuccess(true);
                for (int x = 0; x < xmlidlist.size(); x++) {
                    if (!listid.contains(xmlidlist.get(x))) {
                        KeyPerValueError viewerrorss = utility.KeyPerValueError();
                        viewerrorss.setClaimid(xmlidlist.get(x));
                        viewerrorss.setRemarks("");
                        viewerrorss.setSeries(claimseries);
                        String details = "Claim Number " + xmlidlist.get(x) + " Not Found in EClaims Database";
                        DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, claimseries, xmlidlist.get(x), filecontent);
                        viewerrorss.setErrors(details + " DRG Claims status " + auditrail.getMessage());
                        viewerrorss.setWarningerror("");
                        allErrorList.add(viewerrorss);
                    }
                }

            } else {
                //ACCRE NO IS EMPTY OR HOSPITAL CODE
                String details = "Accre/Hospital is empty";
                String series = claimseries;
                String claimnum = "";
                KeyPerValueError viewerrorsss = utility.KeyPerValueError();
                DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, claimnum, filecontent);
                viewerrorsss.setRemarks("");
                viewerrorsss.setClaimid(claimseries);
                viewerrorsss.setErrors(details + " DRG Claims status " + auditrail.getMessage());
                viewerrorsss.setWarningerror("");
                allErrorList.add(viewerrorsss);
                result.setSuccess(false);

            }
            // END OF LINE GET DATA FROM ECLAIMS TABLE FOR FROMT VALIDATION

            //---------------------------------------------------------
            result.setMessage("OK");
            result.setResult(utility.objectMapper().writeValueAsString(allErrorList));

        } catch (Exception ex) {
            result.setMessage(ex.getLocalizedMessage());
            Logger.getLogger(ValidateXMLValues.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

    public class ProcAssign {

        private String lat;
        private String ex1;
        private String ex2;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getEx1() {
            return ex1;
        }

        public void setEx1(String ex1) {
            this.ex1 = ex1;
        }

        public String getEx2() {
            return ex2;
        }

        public void setEx2(String ex2) {
            this.ex2 = ex2;
        }
    }

}
