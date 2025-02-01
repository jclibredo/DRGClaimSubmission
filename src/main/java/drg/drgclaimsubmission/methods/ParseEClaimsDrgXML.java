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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ParseEClaimsDrgXML {

    private final Utility utility = new Utility();

    //private final AccessGrouperFrontValidation accessgrouper = new AccessGrouperFrontValidation();
    public DRGWSResult ParseEClaimsDrgXML(
            final DataSource datasource,
            final CF5 drg,
            final List<NClaimsData> nclaimsdatalist,
            final List<String> listid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> error = new ArrayList<>();
        ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
        try {
            CF5 drgs = new CF5();
            if (!drg.getPHospitalCode().isEmpty()) {
                if (!drg.getPHospitalCode().trim().equals(nclaimsdatalist.get(0).getHospitalcode().trim())) {
                    // error.add("CF5 " + drg.getPHospitalCode() + " pHospitalCode code not found in eclaims xml");
                    error.add("509");// ERROR
                }
            }
            if (drg.getPHospitalCode().isEmpty()) {
                //error.add("CF5 pHospitalCode Code is required");
                error.add("303");// ERROR
            }
            if (drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                // error.add("CF5 ClaimNumber is required");
                error.add("511"); // ERROR
            }

            if (!drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                if (!listid.contains(drg.getDRGCLAIM().getClaimNumber().trim())) {
                    // error.add("CF5 ClaimNumber: " + drg.getDRGCLAIM().getClaimNumber().trim() + " not found in EClaims XML");
                    error.add("222"); // ERROR
                }
            }

            //Validate Eclaims XML
            for (int x = 0; x < nclaimsdatalist.size(); x++) {
                if (!drg.getDRGCLAIM().getClaimNumber().isEmpty()) {
                    if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nclaimsdatalist.get(x).getPclaimnumber().trim())) {
                        if (!nclaimsdatalist.get(x).getTimeDischarge().trim().isEmpty()) {
                            if (!utility.IsValidTime(nclaimsdatalist.get(x).getTimeDischarge())) {
                                error.add("516");
                            }
                        }
                        if (!nclaimsdatalist.get(x).getTimeAdmission().trim().isEmpty()) {
                            if (!utility.IsValidTime(nclaimsdatalist.get(x).getTimeAdmission())) {
                                error.add("515");
                            }
                        }
                        if (!nclaimsdatalist.get(x).getAdmissionDate().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getAdmissionDate().trim())) {
                                error.add("517");
                            }
                        }
                        if (!nclaimsdatalist.get(x).getDischargeDate().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getDischargeDate().trim())) {
                                error.add("518");
                            }
                        }
                        if (nclaimsdatalist.get(x).getTimeDischarge().isEmpty()) {
                            error.add("107");
                        }
                        if (nclaimsdatalist.get(x).getTimeAdmission().isEmpty()) {
                            error.add("105");
                        }
                        if (nclaimsdatalist.get(x).getAdmissionDate().isEmpty()) {
                            error.add("104");
                        }
                        if (nclaimsdatalist.get(x).getDischargeDate().isEmpty()) {
                            error.add("106");
                        }
                        if (nclaimsdatalist.get(x).getDateofBirth().isEmpty()) {
                            error.add("103");
                        }
                        if (!nclaimsdatalist.get(x).getDateofBirth().trim().isEmpty()) {
                            if (!utility.IsValidDate(nclaimsdatalist.get(x).getDateofBirth().trim())) {
                                error.add("403");
                            }
                        }
                        break;
                    }
                }
            }
            if (error.size() > 0) {
                KeyPerValueError viewerrors = utility.KeyPerValueError();
                viewerrors.setErrors(String.join(",", error));
                viewerrors.setSeries("");
                viewerrors.setClaimid(drg.getDRGCLAIM().getClaimNumber() + "");
                viewerrors.setWarningerror("");
                viewerrors.setRemarks("Major Error");
                allErrorList.add(viewerrors);
            }
            if (error.isEmpty()) {
                for (int y = 0; y < nclaimsdatalist.size(); y++) {
                    if (drg.getPHospitalCode().trim().equals(nclaimsdatalist.get(0).getHospitalcode().trim())) {
                        if (drg.getDRGCLAIM().getClaimNumber().trim().equals(nclaimsdatalist.get(y).getPclaimnumber().trim())) {
                            DRGCLAIM drgclaims = new DRGCLAIM();
                            ArrayList<String> SecondaryData = new ArrayList<>();
                            ArrayList<String> ProcedureData = new ArrayList<>();
                            KeyPerValueError viewerror = utility.KeyPerValueError();
                            ArrayList<String> errorlist = new ArrayList<>();
                            ArrayList<String> warningerror = new ArrayList<>();
                            //KEY VALUE PAIR VALIDATION
                            DRGWSResult vprodResult = new ValidateDRGClaims().ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nclaimsdatalist.get(y));
                            //KEY VALUE PAIR VALIDATION
                            DRGCLAIM drgclaim = utility.objectMapper().readValue(vprodResult.getResult(), DRGCLAIM.class);
                            //drgs.add(drgclaim);
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
                                ProcedureData.add(ProcsCode.trim() + "+" + ext1.trim() + "" + ext2.trim());
                                //validate extension code
                            }

                            for (int sdx = 0; sdx < drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().size(); sdx++) {
                                if (!drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks().isEmpty()) {
                                    warningerror.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getRemarks());
                                }
                                SecondaryData.add(drgclaims.getSECONDARYDIAGS().getSECONDARYDIAG().get(sdx).getSecondaryCode().trim());
                            }

                            ArrayList<String> duplsdx = new ArrayList<>();
                            for (int i = 0; i < SecondaryData.size() - 1; i++) {
                                for (int j = i + 1; j < SecondaryData.size(); j++) {
                                    if (SecondaryData.get(i).equals(SecondaryData.get(j)) && (i != j)) {
                                        warningerror.add("503");
                                        duplsdx.add(String.valueOf(j));
                                    }
                                }
                            }
                            ArrayList<String> duplproc = new ArrayList<>();
                            for (int i = 0; i < ProcedureData.size() - 1; i++) {
                                for (int j = i + 1; j < ProcedureData.size(); j++) {
                                    if (ProcedureData.get(i).equals(ProcedureData.get(j)) && (i != j)) {
                                        warningerror.add("507");
                                        duplproc.add(String.valueOf(j));
                                        break;
                                    }
                                }
                            }
                            drgs.setDRGCLAIM(drgclaim);

                            ArrayList<String> drgcodelist = new ArrayList<>();
                            ArrayList<String> drgnamelist = new ArrayList<>();
                            viewerror.setWarningerror(String.join(",", warningerror));
                            viewerror.setErrors(String.join(",", errorlist));
                            viewerror.setClaimid(drg.getDRGCLAIM().getClaimNumber().trim());
                            viewerror.setDrg(drgcodelist);
                            viewerror.setDrgname(drgnamelist);
                            allErrorList.add(viewerror);
                            break;
                        }
                    }
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
            //---------------------------------------------------------------------------
            if (error.size() > 0) {
                result.setMessage("CF5 DATA ECOUNTER ERROR EXPECT THAT GROUPING LOGIC CAN'T BE PROCEED");
            } else if (errors.trim().length() > 0) {
                result.setSuccess(true);
                result.setMessage("CF5 DATA HAS AN ERROR EXPECT UNGROUPABLE DRG CODES RESULT");
            } else if (warnings.trim().length() > 0) {
                result.setSuccess(true);
                result.setMessage("CF5 DATA HAS WARNING ERROR EXPECT THAT SOME DATA WILL NOT BE CONSIDERED IN GROUPING LOGIC");
            } else {
                result.setMessage("CF5 DATA IS CLEAN");
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
