/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.methods.phic.phic;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ValidateXMLValues {

    public ValidateXMLValues() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ValidateXMLValues(final DataSource datasource,
            final CF5 drg,
            final String lhio,
            final String claimseries,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        ArrayList<String> detailList = new ArrayList<>();
        ArrayList<String> error = new ArrayList<>();
        NClaimsData nClaimsData = new NClaimsData();
        try {
            // GET DATA FROM ECLAIMS TABLE FOR FROMT VALIDATION
            DRGWSResult getClaimsData = new phic().GeteClaims(datasource, claimseries);
            if (getClaimsData.isSuccess()) {
                nClaimsData = utility.objectMapper().readValue(getClaimsData.getResult(), NClaimsData.class);
            } else {
                detailList.add(getClaimsData.getMessage());
                error.add(getClaimsData.getResult());
            }
            CF5 drgs = new CF5();
            DRGWSResult getdupresults = new CF5Method().GetClaimDuplication(datasource, drg.getPHospitalCode().trim(), drg.getDRGCLAIM().getClaimNumber().trim(), claimseries);
            //-------------------------------------------
            if (drg.getDRGCLAIM().getClaimNumber().trim().isEmpty()) {
                detailList.add("CF5 ClaimNumber required");
                error.add("511");
            }
            if (drg.getPHospitalCode().isEmpty()) {
                detailList.add("CF5 pHospitalCode required");
                error.add("303");
            }
            if (!drg.getDRGCLAIM().getClaimNumber().trim().isEmpty() && getClaimsData.isSuccess()) {
                if (getdupresults.isSuccess()) {
                    detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber is exist already and duplicated");
                    error.add("513");
                    detailList.add("Claim Number has duplicate");
                }

                if (!nClaimsData.getPclaimnumber().isEmpty() || nClaimsData.getPclaimnumber() != null || !nClaimsData.getPclaimnumber().equals("")) {
                    if (!nClaimsData.getPclaimnumber().trim().equals(drg.getDRGCLAIM().getClaimNumber().trim())) {
                        detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber not found in Eclaims DB");
                        error.add("512");
                    } else {
                        if (nClaimsData.getTimeDischarge().isEmpty() || nClaimsData.getTimeDischarge() == null || nClaimsData.getTimeDischarge().equals("")) {
                            error.add("107");
                            detailList.add("TimeDischarge is required");
                        }
                        if (nClaimsData.getTimeAdmission().isEmpty() || nClaimsData.getTimeAdmission() == null || nClaimsData.getTimeAdmission().equals("")) {
                            error.add("105");
                            detailList.add("TimeAdmission is required");
                        }
                        if (nClaimsData.getAdmissionDate().isEmpty() || nClaimsData.getAdmissionDate() == null || nClaimsData.getAdmissionDate().equals("")) {
                            error.add("104");
                            detailList.add("AdmissionDate is required");
                        }
                        if (nClaimsData.getDischargeDate().isEmpty() || nClaimsData.getDischargeDate() == null || nClaimsData.getDischargeDate().equals("")) {
                            error.add("106");
                            detailList.add("DischargeDate is required");
                        }
                        if (nClaimsData.getDateofBirth().isEmpty() || nClaimsData.getDateofBirth() == null || nClaimsData.getDateofBirth().equals("")) {
                            error.add("103");
                            detailList.add("BirthDate is required");
                        } else if (!utility.IsValidDate(nClaimsData.getDateofBirth().trim())) {
                            error.add("403");
                            detailList.add("BirthDate invalid format");
                        }
                    }
                }
            }
            //---------------------------------------------------------------------
            if (error.isEmpty()) {
                if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nClaimsData.getPclaimnumber().trim())) {
                    DRGCLAIM drgclaims = new DRGCLAIM();
                    ArrayList<String> SecondaryData = new ArrayList<>();
                    ArrayList<String> ProcedureData = new ArrayList<>();
                    ArrayList<String> warningerror = new ArrayList<>();
                    ArrayList<String> duplproc = new ArrayList<>();
                    ArrayList<String> duplsdx = new ArrayList<>();
                    //VALIDATE DRG VALUES
                    DRGWSResult vprodResult = new ValidateDRGClaims().ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nClaimsData);
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
                        } else if (Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(ext1.trim().toUpperCase())) {
                            procassign.setEx1("1");
                        } else {
                            procassign.setEx1(ext1);
                        }
                        if (ext2.isEmpty()) {
                            procassign.setEx2("1");
                        } else if (Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").contains(ext2.trim().toUpperCase())) {
                            procassign.setEx2("1");
                        } else {
                            procassign.setEx2(ext2);
                        }
                        if (lat.isEmpty()) {
                            procassign.setLat("N");
                        } else if (!Arrays.asList("L", "R", "B", "N").contains(lat.trim().toUpperCase())) {
                            procassign.setLat("N");
                        } else {
                            procassign.setLat(lat);
                        }
                        ProcedureData.add(ProcsCode + "+" + procassign.getLat() + "" + procassign.getEx1() + "" + procassign.getEx2());
                    }
                    //IDENTIFY SECONDARY DIAGNOSIS WARNING ERROR
                    for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                        if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                            warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                            detailList.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                        }
                        SecondaryData.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode());
                    }
                    //IDENTIFY SECONDARY DIAGNOSIS DUPLICATES
                    for (int i = 0; i < SecondaryData.size() - 1; i++) {
                        for (int j = i + 1; j < SecondaryData.size(); j++) {
                            if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                warningerror.add("503");
                                duplsdx.add(String.valueOf(j));
                                detailList.add(SecondaryData.get(j) + " SDx has duplicate");
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
                                detailList.add(ProcedureData.get(j) + " Procedure has duplicate");
                                break;
                            }
                        }
                    }
                    //MAP VALIDATE DATA TO NEW CF5 OBJECT
                    drgs.setDRGCLAIM(drgclaim);
                    //INSERT CF5 CLAIMS DATA
                    DRGWSResult insertDRGClaimsResult = new InsertDRGClaims().InsertDRGClaims(
                            drgs.getDRGCLAIM(),
                            datasource,
                            nClaimsData,
                            lhio,
                            claimseries,
                            drg.getPHospitalCode(),
                            drgs.getDRGCLAIM().getClaimNumber(),
                            duplproc,
                            duplsdx,
                            filecontent);
                    if (!insertDRGClaimsResult.isSuccess()) {
                        detailList.add(insertDRGClaimsResult.getMessage());
                        error.add(insertDRGClaimsResult.getMessage());
                        result.setMessage(insertDRGClaimsResult.getMessage());
                    } else {
                        detailList.add(insertDRGClaimsResult.getMessage());
                        result.setSuccess(true);
                        result.setMessage(insertDRGClaimsResult.getMessage());
                    }
                    //SET RETURN RESULT
                    KeyPerValueError viewerrors = utility.KeyPerValueError();
                    viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber());
                    viewerrors.setSeries(claimseries);
                    viewerrors.setErrors(String.join(",", error));
                    viewerrors.setWarningerror(String.join(",", warningerror));
                    allErrorList.add(viewerrors);
                    //SET AUDITRAIL
//                        DRGWSResult auditrail = new CF5Method().InsertDRGAuditTrail(datasource, String.join(",", detailList),
//                                String.valueOf(insertDRGClaimsResult.isSuccess()).toUpperCase(), claimseries, drg.getDRGCLAIM().getClaimNumber(), filecontent);
//                        result.setMessage("SAVE CLAIMS STATS : " + insertDRGClaimsResult.getMessage() + " , LOGS STATS: " + auditrail.getMessage());
//                        break;
                }
            } else {
                KeyPerValueError viewerrors = utility.KeyPerValueError();
                viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber());
                viewerrors.setSeries(claimseries + "");
                viewerrors.setErrors(String.join(",", error));
                viewerrors.setWarningerror("");
                allErrorList.add(viewerrors);
                //SET AUDITRAIL
                DRGWSResult auditrail = new CF5Method().InsertDRGAuditTrail(datasource, String.join(",", detailList), "FALSE", claimseries, drg.getDRGCLAIM().getClaimNumber(), filecontent);
                result.setMessage(String.join(",", detailList) + " CF5 DATA ECOUNTER ERROR EXPECT THAT GROUPING LOGIC CAN'T BE PROCEED, Logs Stats: " + auditrail.getMessage());
            }
            result.setResult(utility.objectMapper().writeValueAsString(allErrorList));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
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
