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
import drg.drgclaimsubmission.utilities.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
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
    private final CF5Method gm = new CF5Method();

    public DRGWSResult ValidateXMLValues(final DataSource datasource,
            final CF5 drg,
            final String lhio,
            final String claimseries,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        ArrayList<NClaimsData> nclaimsdataList = new ArrayList<>();
        SimpleDateFormat timeformat = utility.SimpleDateFormat("hh:mm:ssa");
        SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
        ArrayList<String> detailList = new ArrayList<>();
        ArrayList<String> error = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            // GET DATA FROM ECLAIMS TABLE FOR FROMT VALIDATION
            CallableStatement getdrg_nclaims = connection.prepareCall("begin :nclaims := DRG_SHADOWBILLING.UHCDRGPKG.GET_NCLAIMS(:seriesnumss); end;");
            getdrg_nclaims.registerOutParameter("nclaims", OracleTypes.CURSOR);
            getdrg_nclaims.setString("seriesnumss", claimseries.trim());
            getdrg_nclaims.execute();
            ResultSet getdrg_nclaims_result = (ResultSet) getdrg_nclaims.getObject("nclaims");
            if (getdrg_nclaims_result.next()) {
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
            } else {
                detailList.add("CF5 " + claimseries + " not found in eClaims DB");
                // error.add("CF5 " + claimseries + " not found in eClaims DB");
                error.add("514");
            }
            CF5 drgs = new CF5();
            DRGWSResult getdupresults = gm.GetClaimDuplication(datasource, drg.getPHospitalCode().trim(), drg.getDRGCLAIM().getClaimNumber().trim(), claimseries);
            //-------------------------------------------
            if (drg.getDRGCLAIM().getClaimNumber().trim().isEmpty()) {
                detailList.add("CF5 ClaimNumber required");
//                error.add("CF5 ClaimNumber required");
                error.add("511");
            }
            if (drg.getPHospitalCode().isEmpty()) {
                detailList.add("CF5 pHospitalCode required");
//                error.add("CF5 pHospitalCode required");
                error.add("510");
            }
            if (!drg.getDRGCLAIM().getClaimNumber().trim().isEmpty()) {
                if (getdupresults.isSuccess()) {
                    detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber is exist already");
                    // error.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber is exist already");
                    error.add("513");
                }
                if (!nclaimsdataList.get(0).getPclaimnumber().trim().equals(drg.getDRGCLAIM().getClaimNumber().trim())) {
                    detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber not found in Eclaims DB");
                    //error.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber not found in Eclaims DB");
                    error.add("512");
                }
            }
//            System.out.println("NCLAIMS " + nclaimsdataList.get(0).getPclaimnumber());
//            System.out.println("CF5 " + drg.getDRGCLAIM().getClaimNumber().trim());
//            if (drg.getDRGCLAIM().getPrimaryCode().isEmpty()) {
//                detailList.add("CF5 PrimaryCode required");
//                error.add("CF5 PrimaryCode required");
//            }
//
//            if (!drg.getDRGCLAIM().getPrimaryCode().trim().isEmpty()) {
//                DRGWSResult NewResult = gm.GetICD10(datasource, drg.getDRGCLAIM().getPrimaryCode().trim());
//                if (!NewResult.isSuccess()) {
//                    error.add("CF5 Err. code 201 " + drg.getDRGCLAIM().getPrimaryCode().trim() + " PrimaryCode is not valid");
//                    detailList.add("CF5 Err. code 201 " + drg.getDRGCLAIM().getPrimaryCode().trim() + " PrimaryCode is not valid");
//                }
//            }
//            if (drg.getDRGCLAIM().getPrimaryCode().trim().isEmpty()) {
//                error.add("CF5 Err. code 101 PrimaryCode is required");
//                detailList.add("CF5 Err. code 101 PrimaryCode is required");
//            }
            //---------------------------------------------------------------------
            if (error.isEmpty()) {
                for (int y = 0; y < nclaimsdataList.size(); y++) {
                    if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nclaimsdataList.get(y).getPclaimnumber().trim())) {
                        DRGCLAIM drgclaims = new DRGCLAIM();
                        ArrayList<String> SecondaryData = new ArrayList<>();
                        ArrayList<String> ProcedureData = new ArrayList<>();
                        ArrayList<String> warningerror = new ArrayList<>();
                        ArrayList<String> duplproc = new ArrayList<>();
                        ArrayList<String> duplsdx = new ArrayList<>();
                        //VALIDATE DRG VALUES
                        DRGWSResult vprodResult = VDC.ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nclaimsdataList.get(y));
                        DRGCLAIM drgclaim = utility.objectMapper().readValue(vprodResult.getResult(), DRGCLAIM.class);
                        drgclaims.setClaimNumber(drgclaim.getClaimNumber());
                        drgclaims.setPrimaryCode(drgclaim.getPrimaryCode());
                        drgclaims.setNewBornAdmWeight(drgclaim.getNewBornAdmWeight());
                        drgclaims.setRemarks(drgclaim.getRemarks());
                        drgclaims.setPROCEDURES(drgclaim.getPROCEDURES());
                        drgclaims.setSECONDARYDIAGS(drgclaim.getSECONDARYDIAGS());
                        //IDENTIFY PRIMARY DIAGNOSIS ERRORS
                        if (!drgclaims.getRemarks().isEmpty()) {
                            detailList.add(drgclaims.getRemarks());
                            error.add(drgclaims.getRemarks());
                        }
                        //IDENTIFY PROCEDURES WARNING ERROR
                        for (int proc = 0; proc < drgclaims.getPROCEDURES().getPROCEDURE().size(); proc++) {
                            String ProcsCode = drgclaim.getPROCEDURES().getPROCEDURE().get(proc).getRvsCode();
                            String ext1 = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt1();
                            String ext2 = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getExt2();
                            String lat = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getLaterality();
                            String remarks = drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks();
                            //------------------------------------------------------------------------------
                            if (!remarks.isEmpty()) {
                                warningerror.add(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks());
                                detailList.add(drgclaims.getPROCEDURES().getPROCEDURE().get(proc).getRemarks());
                            }
                            ProcAssign procassign = new ProcAssign();
                            if (ext1.isEmpty()) {
                                procassign.setEx1("1");
                            }
                            if (ext2.isEmpty()) {
                                procassign.setEx2("1");
                            }
                            
                            ProcedureData.add(ProcsCode + "+" + ext1 + "" + ext2);
                        }
                        //IDENTIFY SECONDARY DIAGNOSIS WARNING ERROR
                        for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                            String SDxCode = drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode();
                            if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                                warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                                detailList.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                            }
                            SecondaryData.add(SDxCode);
                        }
                        //IDENTIFY SECONDARY DIAGNOSIS DUPLICATES
                        for (int i = 0; i < SecondaryData.size() - 1; i++) {
                            for (int j = i + 1; j < SecondaryData.size(); j++) {
                                if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                    warningerror.add("503");
                                    duplsdx.add(String.valueOf(j));
                                    detailList.add("CF5 Warning Err. 503");
                                    break;
                                }
                            }
                        }
                        // IDENTIFY DUPLICATE PROCEDURES
                        for (int i = 0; i < ProcedureData.size() - 1; i++) {
                            for (int j = i + 1; j < ProcedureData.size(); j++) {
                                if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                                    warningerror.add("508");
                                    duplproc.add(String.valueOf(j));
                                    detailList.add("CF5 Warning Err. 508");
                                    break;
                                }
                            }
                        }
                        //MAP VALIDATE DATA TO NEW CF5 OBJECT
                        drgs.setDRGCLAIM(drgclaim);
                        //INSERT CF5 CLAIMS DATA
                        DRGWSResult insertDRGClaimsResult = insertDRGClaims.InsertDRGClaims(drgs.getDRGCLAIM(),
                                datasource, nclaimsdataList.get(y), lhio, claimseries, drg.getPHospitalCode(),
                                drgs.getDRGCLAIM().getClaimNumber(),
                                duplproc, duplsdx, filecontent);
                        //System.out.println(insertDRGClaimsResult.getMessage());
                        if (!insertDRGClaimsResult.isSuccess()) {
                            detailList.add(insertDRGClaimsResult.getMessage());
                            error.add(insertDRGClaimsResult.getMessage());
                        } else {
                            detailList.add(insertDRGClaimsResult.getMessage());
                            result.setSuccess(true);
                        }
                        //SET RETURN RESULT
                        KeyPerValueError viewerrors = utility.KeyPerValueError();
                        viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber());
                        viewerrors.setSeries(claimseries);
                        viewerrors.setErrors(String.join(",", error));
                        viewerrors.setWarningerror(String.join(",", warningerror));
                        allErrorList.add(viewerrors);
                        //SET AUDITRAIL
//                        DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, String.join(",", detailList),
//                                String.valueOf(insertDRGClaimsResult.isSuccess()).toUpperCase(), claimseries, drg.getDRGCLAIM().getClaimNumber(), filecontent);
//                        result.setMessage("SAVE CLAIMS STATS : " + insertDRGClaimsResult.getMessage() + " , LOGS STATS: " + auditrail.getMessage());
                        break;
                    }
                }
            } else {
                KeyPerValueError viewerrors = utility.KeyPerValueError();
                viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber());
                viewerrors.setSeries(claimseries + "");
                viewerrors.setErrors(String.join(",", error));
                viewerrors.setWarningerror("");
                allErrorList.add(viewerrors);
                //SET AUDITRAIL
                DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, String.join(",", detailList), "FALSE", claimseries, drg.getDRGCLAIM().getClaimNumber(), filecontent);
                result.setMessage("CF5 XML Has an error , Logs Stats: " + auditrail.getMessage());
            }
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
