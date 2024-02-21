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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ParseEClaimsDrgXML {

    private final Utility utility = new Utility();
    private final ValidateDRGClaims VDC = new ValidateDRGClaims();
    private final AccessGrouperFrontValidation accessgrouper = new AccessGrouperFrontValidation();

    public DRGWSResult ParseEClaimsDrgXML(final DataSource datasource,
            final CF5 drg,
            final List<NClaimsData> nclaimsdatalist, final List<String> listid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> xmlidlist = new ArrayList<>();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        ArrayList<String> dupclaim = new ArrayList<>();

        ArrayList<String> dupmessage = new ArrayList<>();
        try {

            // IDENTIFYING DUPLICATE CF5 CLAIMS
            for (int i = 0; i < dupclaim.size() - 1; i++) {
                for (int j = i + 1; j < dupclaim.size(); j++) {
                    if (dupclaim.get(i).equals(dupclaim.get(j)) && (i != j)) {
                        // warningerror.add(" RVS : " + ProcedureData.get(j) + " has duplicate");
                        dupmessage.add(dupclaim.get(j));
                    }
                }
            }
            if (dupmessage.isEmpty()) {
                CF5 drgs = new CF5();
                if (!drg.getpHospitalCode().isEmpty()) {
                    String claimID = drg.getDrgclaim().getClaimNumber();
                    if (claimID.length() > 0) {
                        xmlidlist.add(claimID);
                        for (int y = 0; y < nclaimsdatalist.size(); y++) {
                            String eclaimsid = nclaimsdatalist.get(y).getPclaimnumber();
                            if (claimID.trim().equals(eclaimsid.trim())) {
                                DRGCLAIM drgclaims = new DRGCLAIM();
                                ArrayList<String> SecondaryData = new ArrayList<>();
                                ArrayList<String> ProcedureData = new ArrayList<>();
                                KeyPerValueError viewerror = utility.KeyPerValueError();
                                ArrayList<String> errorlist = new ArrayList<>();
                                ArrayList<String> warningerror = new ArrayList<>();

                                DRGWSResult vprodResult = VDC.ValidateDRGClaims(datasource, drg.getDrgclaim(), nclaimsdatalist.get(y));
                                DRGCLAIM drgclaim = utility.objectMapper().readValue(vprodResult.getResult(), DRGCLAIM.class);
                                //drgs.add(drgclaim);
                                //---------------------------------------------------

                                drgclaims.setClaimNumber(drgclaim.getClaimNumber());
                                drgclaims.setPrimaryCode(drgclaim.getPrimaryCode());
                                drgclaims.setNewBornAdmWeight(drgclaim.getNewBornAdmWeight());
                                drgclaims.setRemarks(drgclaim.getRemarks());
                                drgclaims.setPROCEDURES(drgclaim.getPROCEDURES());
                                drgclaims.setSECONDARYDIAGS(drgclaim.getSECONDARYDIAGS());
                                //-----------------------------------------------------

                                //-----------------------------------------------------
//                                    if (!drgs.getDRGCLAIM().get(b).getRemarks().isEmpty()) {
//                                        errorlist.add(drgclaim.getRemarks());
//                                    }
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
                                    //validate extension code
                                }

                                for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                                    String SDxCode = drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode();
                                    if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                                        warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                                    }
                                    SecondaryData.add(SDxCode);
                                }
                                ArrayList<String> duplsdx = new ArrayList<>();
                                for (int i = 0; i < SecondaryData.size() - 1; i++) {
                                    for (int j = i + 1; j < SecondaryData.size(); j++) {
                                        if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                            // warningerror.add(SecondaryData.get(j) + " has duplicate");
                                            warningerror.add("503");
                                            duplsdx.add(String.valueOf(j));
                                        }
                                    }
                                }
                                ArrayList<String> duplproc = new ArrayList<>();
                                for (int i = 0; i < ProcedureData.size() - 1; i++) {
                                    for (int j = i + 1; j < ProcedureData.size(); j++) {
                                        if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                                            // warningerror.add(ProcedureData.get(j) + " has duplicate");
                                            warningerror.add("508");
                                            duplproc.add(String.valueOf(j));
                                        }
                                    }
                                }
                                // drgs.getDRGCLAIM().add(drgclaim);
                                drgs.setDrgclaim(drgclaim);
                                ArrayList<String> drgcodelist = new ArrayList<>();
                                ArrayList<String> drgnamelist = new ArrayList<>();
//                                    DRGWSResult grouperresult = accessgrouper.AccessGrouperFrontValidation(drgs.getDRGCLAIM().get(b), datasource, nclaimsdatalist.get(y), String.join(",", duplproc), String.join(",", duplsdx), errorlist);
//                                    drgcodelist.add(grouperresult.getResult());
//                                    drgnamelist.add(grouperresult.getMessage());
                                viewerror.setWarningerror(warningerror.toString());
                                viewerror.setErrors(errorlist.toString());
                                viewerror.setClaimid(claimID);
                                viewerror.setDrg(drgcodelist);
                                viewerror.setDrgname(drgnamelist);
                                allErrorList.add(viewerror);
                                break;
                            }
                        }
                    } else {
                        KeyPerValueError viewerrors = utility.KeyPerValueError();
                        viewerrors.setWarningerror("");
                        viewerrors.setErrors("DRG ClaimNumber is required");
                        viewerrors.setClaimid("");
                        allErrorList.add(viewerrors);
                    }
                    //END OF ECLAIMS XML FILE PARSING-------------------------------------------

                } else {
                    KeyPerValueError viewerrors = utility.KeyPerValueError();
                    viewerrors.setWarningerror("");
                    viewerrors.setErrors("303");
                    viewerrors.setClaimid("");
                    allErrorList.add(viewerrors);
                }

            } else {
                for (int x = 0; x < dupmessage.size(); x++) {
                    KeyPerValueError viewerrors = utility.KeyPerValueError();
                    viewerrors.setWarningerror("");
                    viewerrors.setErrors(dupmessage.get(x) + " Repetition of claim number in CF5 Form is not allowed");
                    viewerrors.setClaimid(dupmessage.get(x));
                    allErrorList.add(viewerrors);
                }
            }

            for (int x = 0; x < xmlidlist.size(); x++) {
                if (!listid.contains(xmlidlist.get(x))) {
                    KeyPerValueError viewerrors = utility.KeyPerValueError();
                    viewerrors.setClaimid(xmlidlist.get(x));
                    viewerrors.setErrors("Claim Number: " + xmlidlist.get(x) + " not found in EClaims XML");
                    viewerrors.setWarningerror("");
                    allErrorList.add(viewerrors);
                }
            }
            //---------------------------------------------------------
            ArrayList<String> errorlist = new ArrayList<>();
            ArrayList<String> warninglist = new ArrayList<>();
            for (int x = 0; x < allErrorList.size(); x++) {
                errorlist.add(allErrorList.get(x).getErrors());
                warninglist.add(allErrorList.get(x).getWarningerror());
            }

            String errors = String.join(",", errorlist.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim();
            String warnings = String.join(",", warninglist.toString()).replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\\,", "").trim();
            if (errors.length() > 0) {
                result.setMessage("CLAIM DATA HAS AN ERROR");
                result.setSuccess(false);
            } else if (warnings.length() > 0) {
                result.setSuccess(true);
                result.setMessage("CLAIM DATA HAS WARNING ERROR");
            } else {
                result.setMessage("CLAIM DATA IS CLEAN");
                result.setSuccess(true);
            }
            result.setResult(utility.objectMapper().writeValueAsString(allErrorList));

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ParseEClaimsDrgXML.class.getName()).log(Level.SEVERE, null, ex);
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
