/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods.upload;

import drg.drgclaimsubmission.methods.CF5Method;
import drg.drgclaimsubmission.methods.InsertDRGClaims;
import drg.drgclaimsubmission.methods.ValidateProcedures;
import drg.drgclaimsubmission.methods.ValidateSecondaryDiag;
import drg.drgclaimsubmission.methods.phic.phic;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.XMLErrors;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.structures.dtd.DRGCLAIM;
import drg.drgclaimsubmission.structures.dtd.PROCEDURE;
import drg.drgclaimsubmission.structures.dtd.PROCEDURES;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAG;
import drg.drgclaimsubmission.structures.dtd.SECONDARYDIAGS;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
//import javax.xml.bind.Unmarshaller;
//import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class Upload {

    public Upload() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult ValidateXMLWithDTD(
            final String stringdrgxml,
            final DataSource datasource,
            final String lhio,
            final String claimseries,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        XMLErrors xmlerrors = new XMLErrors();
        try {
            //End line to Generate DTD File 
            String stringxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE CF5 [" + utility.DTDFilePath() + "]>\n" + stringdrgxml;
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory.newInstance().setValidating(true);
//            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final ArrayList<String> arraywarning = new ArrayList<>();
            final ArrayList<String> arrayerror = new ArrayList<>();
            final ArrayList<String> arrayfatalerror = new ArrayList<>();
            DocumentBuilderFactory.newInstance().newDocumentBuilder().setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    int lineno = exception.getLineNumber() - 2;
                    arraywarning.add("Line No. " + lineno + " : " + exception.getMessage());
                }
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    int lineno = exception.getLineNumber() - 2;
                    arrayfatalerror.add("Line No. " + lineno + " : " + exception.getMessage());
                }
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    int lineno = exception.getLineNumber() - 2;
                    arrayerror.add("Line No. " + lineno + " : " + exception.getMessage());
                }
            });
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(stringxml)));
//            JAXBContext jaxbcontext = JAXBContext.newInstance(CF5.class);
//            Unmarshaller jaxbnmarsaller = JAXBContext.newInstance(CF5.class).createUnmarshaller();
//            StringReader readers = new StringReader(stringdrgxml);
//            CF5 drg = (CF5) JAXBContext.newInstance(CF5.class).createUnmarshaller().unmarshal(new StringReader(stringdrgxml));
            if ((arrayfatalerror.isEmpty()) && (arrayerror.isEmpty())) {
                DRGWSResult keypervalue = this.ValidateXMLValues(datasource, (CF5) JAXBContext.newInstance(CF5.class).createUnmarshaller().unmarshal(new StringReader(stringdrgxml)), lhio, claimseries, filecontent);
                result.setMessage(keypervalue.getMessage());
                result.setSuccess(keypervalue.isSuccess());
                result.setResult(keypervalue.getResult());
            } else {
                if (arrayfatalerror.size() > 0) {
                    ArrayList<String> fatalerrors = new ArrayList<>();
                    for (int a = 0; a < arrayfatalerror.size(); a++) {
                        fatalerrors.add(arrayfatalerror.get(a).replaceAll("\"", ""));
                    }
                    xmlerrors.setErrors(fatalerrors);
                }
                if (arrayerror.size() > 0) {
                    ArrayList<String> errors = new ArrayList<>();
                    for (int a = 0; a < arrayerror.size(); a++) {
                        errors.add(arrayerror.get(a).replaceAll("\"", ""));
                    }
                    xmlerrors.setErrors(errors);
                }
                DRGWSResult auditrail = new CF5Method().InsertDRGAuditTrail(datasource, "CF5 XML format have errors : " + utility.objectMapper().writeValueAsString(xmlerrors), "FAILED", claimseries, "N/A", filecontent);
                result.setMessage("CF5 XML format have errors , Logs Stats :" + auditrail.getMessage());
                result.setResult(utility.objectMapper().writeValueAsString(xmlerrors));
            }

        } catch (ParserConfigurationException | IOException | SAXException | JAXBException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult ValidateXMLValues(final DataSource datasource,
            final CF5 drg,
            final String lhio,
            final String claimseries,
            final String filecontent) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<KeyPerValueError> allErrorList = new ArrayList<>();
            ArrayList<String> detailList = new ArrayList<>();
            ArrayList<String> error = new ArrayList<>();
            NClaimsData nClaimsData = new NClaimsData();
            // GET DATA FROM ECLAIMS TABLE FOR FROMT VALIDATION
            DRGWSResult getClaimsData = new phic().GeteClaims(datasource, claimseries);
            if (getClaimsData.isSuccess()) {
                nClaimsData = utility.objectMapper().readValue(getClaimsData.getResult(), NClaimsData.class);
            } else {
                detailList.add(getClaimsData.getMessage());
                error.add(getClaimsData.getResult());
            }
            CF5 drgs = new CF5();
            DRGWSResult getdupresults = new CF5Method().GetClaimDuplication(datasource,
                    drg.getPHospitalCode().trim(),
                    drg.getDRGCLAIM().getClaimNumber().trim(), claimseries);
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
                    detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber already exist");
                    error.add("513");
                    detailList.add("Claim Number has duplicate");
                }
                if (!nClaimsData.getPclaimnumber().isEmpty() || nClaimsData.getPclaimnumber() != null || !nClaimsData.getPclaimnumber().equals("")) {
                    if (!nClaimsData.getPclaimnumber().trim().equals(drg.getDRGCLAIM().getClaimNumber().trim())) {
                        detailList.add("CF5 " + drg.getDRGCLAIM().getClaimNumber() + " ClaimNumber not found in Eclaims DB");
                        error.add("512");
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
                    DRGWSResult vprodResult = this.ValidateDRGClaims(datasource, drg.getDRGCLAIM(), nClaimsData);
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
                result.setMessage(String.join(",", detailList) + " CF5 DATA ENCOUNTER ERROR EXPECT THAT GROUPING LOGIC CAN'T BE PROCEED, Logs Stats: " + auditrail.getMessage());
            }
            result.setResult(utility.objectMapper().writeValueAsString(allErrorList));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult ValidateDRGClaims(
            final DataSource datasource,
            final DRGCLAIM drgclaim,
            final NClaimsData nclaimsdata) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGCLAIM validatedrgclaim;
        ArrayList<String> errors = new ArrayList<>();
        ArrayList<String> errorsMessage = new ArrayList<>();
        DRGWSResult NewResult = new CF5Method().GetICD10(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim());
        try {

            if (!NewResult.isSuccess()) {
                errors.add("411");
            } else if (!new CF5Method().GetICD10PreMDC(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim()).isSuccess()) {
                errors.add("201");
            }
            if (utility.CleanCode(drgclaim.getPrimaryCode()).trim().isEmpty()) {
                // error.add("CF5 Err. code 101 PrimaryCode is required");
                errors.add("101");
            }
            //END PRIMARY CODES VALIDATION
            if (nclaimsdata.getGender().trim().isEmpty()) {
                errors.add("102");
            } else if (!Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                errors.add("209");
            }
            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                String days = String.valueOf(utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                String year = String.valueOf(utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()));
                int finalDays = 0;
                if (Integer.parseInt(year) > 0) {
                    finalDays = Integer.parseInt(year) * 365;
                } else {
                    finalDays = Integer.parseInt(days);
                }
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) > 124) {
                    errors.add("416");
                }
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                        && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0) {
                    if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                        if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) >= 0
                                && utility.ComputeDay(nclaimsdata.getDateofBirth(),
                                        nclaimsdata.getAdmissionDate()) >= 0 && !utility.CleanCode(drgclaim.getPrimaryCode()).trim().isEmpty()) {
                            if (NewResult.isSuccess()) {
                                DRGWSResult icd10preMDC = new CF5Method().GetICD10PreMDC(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim());
                                if (icd10preMDC.isSuccess()) {
                                    //CHECKING FOR AGE CONFLICT
                                    DRGWSResult getAgeConfictResult = new CF5Method().AgeConfictValidation(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim(), String.valueOf(finalDays), year);
                                    if (!getAgeConfictResult.isSuccess()) {
                                        errors.add("414");
                                    }
                                    //  AGE VALIDATION AND GENDER
                                    if (!nclaimsdata.getGender().trim().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                                        //CHECKING FOR GENDER CONFLICT
                                        DRGWSResult getSexConfictResult = new CF5Method().GenderConfictValidation(datasource, utility.CleanCode(drgclaim.getPrimaryCode()).trim(), nclaimsdata.getGender());
                                        if (!getSexConfictResult.isSuccess()) {
                                            errors.add("415");
                                        }
                                    }
                                } else {
                                    errors.add("201");
                                }
                            }
                        }
                    }
                }
            }

            if (!nclaimsdata.getDateofBirth().isEmpty() && !nclaimsdata.getAdmissionDate().isEmpty()) {
                if (utility.ComputeYear(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) <= 0
                        && utility.ComputeDay(nclaimsdata.getDateofBirth(), nclaimsdata.getAdmissionDate()) < 0) {
                    // errors.add("DateofBirth Must be less than or equal to AdmissionDate : ");
                    errors.add("219");
                }
            }
            validatedrgclaim = drgclaim;
            if (!nclaimsdata.getDateofBirth().isEmpty()
                    && !nclaimsdata.getAdmissionDate().isEmpty()
                    && !nclaimsdata.getTimeAdmission().isEmpty()
                    && !nclaimsdata.getDischargeDate().isEmpty()
                    && !nclaimsdata.getTimeDischarge().isEmpty()) {
                int oras = 0;
                int araw = 0;
                int taon = 0;
                int los = 0;
//                int minuto = 0;
                if (utility.IsSURGEValidTime(nclaimsdata.getTimeAdmission()) && utility.IsSURGEValidTime(nclaimsdata.getTimeDischarge())) {
                    los = utility.ComputeSURGELOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                    oras = utility.ComputeSURGETime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
//                    minuto = utility.MinutesSURGECompute(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                } else if (utility.IsAITMDValidTime(nclaimsdata.getTimeAdmission()) && utility.IsAITMDValidTime(nclaimsdata.getTimeDischarge())) {
                    oras = utility.ComputeITMDTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                    los = utility.ComputeITMDLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
//                    minuto = utility.MinutesITMDCompute(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                } else if (utility.IsBITMDValidTime(nclaimsdata.getTimeAdmission()) && utility.IsBITMDValidTime(nclaimsdata.getTimeDischarge())) {
                    oras = utility.ComputeITMDTime(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                    los = utility.ComputeITMDLOS(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
//                    minuto = utility.MinutesITMDCompute(nclaimsdata.getAdmissionDate(), nclaimsdata.getTimeAdmission(), nclaimsdata.getDischargeDate(), nclaimsdata.getTimeDischarge());
                }
                //------------------------------------------------------------------
                taon = utility.ComputeYear(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
                araw = utility.ComputeDay(nclaimsdata.getAdmissionDate(), nclaimsdata.getDischargeDate());
                //------------------------------------------------------------------
                if (los == 0) {
                    if (araw <= 0 && oras < 0) {
                        //errors.add("AdmissionTime Greater than DischargeTime not valid in same date");
                        errors.add("220");
                    }
                } else if (taon <= 0 && araw < 0) {
                    // errors.add("DischargeDat Must be greater than or equal to AdmissionDate");
                    errors.add("221");
                }
                if (araw < 1) {
                    if (los < 24) {
                        errors.add("417");
                    }
                }
                if (nclaimsdata.getDischargeType().isEmpty()) {
                    //errors.add("DischargeType is required");
                    errors.add("108");
                } else if (!Arrays.asList("I", "R", "H", "A", "T", "E", "O").contains(nclaimsdata.getDischargeType())) {
                    // errors.add("DischargeType " + nclaimsdata.getDischargeType() + " is invalid");
                    errors.add("226");
                }
                if (taon <= 0 && araw < 28) {
                    if (!drgclaim.getNewBornAdmWeight().equals("")) {
                        if (!utility.isValidNumeric(drgclaim.getNewBornAdmWeight())) {
                            // errors.add("NewBordAdmWeight  value , " + drgclaim.getNewBornAdmWeight() + ", is non-numeric value");
                            errors.add("227");
                        } else if (Double.parseDouble(drgclaim.getNewBornAdmWeight()) < 0.3) {
                            //errors.add("NewBordAdmWeight value , " + drgclaim.getNewBornAdmWeight() + ", not meet the minimum requirements 0.3 Kilograms");
                            errors.add("228");
                        }
                    } else {
                        // errors.add("NewBordAdmWeight is required");
                        errors.add("109");
                    }
                }
                //END VALIDATION FOR NEW BORN DATA
                validatedrgclaim.setRemarks(String.join(",", errors));
                if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                    SECONDARYDIAGS secondarydiags = new SECONDARYDIAGS();
                    for (int a = 0; a < drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().size(); a++) {
                        // sdx validation
                        DRGWSResult VSDResultS = new ValidateSecondaryDiag().ValidateSecondaryDiag(datasource, drgclaim.getSECONDARYDIAGS().getSECONDARYDIAG().get(a), drgclaim.getPrimaryCode(), nclaimsdata);
                        //mapping
                        if (VSDResultS.isSuccess()) {
                            SECONDARYDIAG secondarydiag = utility.objectMapper().readValue(VSDResultS.getResult(), SECONDARYDIAG.class);
                            secondarydiags.getSECONDARYDIAG().add(secondarydiag);
                            if (secondarydiag.getRemarks().equals("")) {
                                errorsMessage.add(VSDResultS.getMessage());
                            }
                        }
                    }
                    validatedrgclaim.setSECONDARYDIAGS(secondarydiags);
                }
                if (!nclaimsdata.getGender().isEmpty() && Arrays.asList("M", "F").contains(nclaimsdata.getGender().toUpperCase())) {
                    PROCEDURES procedures = new PROCEDURES();
                    for (int b = 0; b < drgclaim.getPROCEDURES().getPROCEDURE().size(); b++) {
                        DRGWSResult VPResult = new ValidateProcedures().ValidateProcedures(datasource, drgclaim.getPROCEDURES().getPROCEDURE().get(b), nclaimsdata.getGender());
                        if (VPResult.isSuccess()) {
                            PROCEDURE procedure = utility.objectMapper().readValue(VPResult.getResult(), PROCEDURE.class);
                            procedures.getPROCEDURE().add(procedure);
                            if (procedure.getRemarks().equals("")) {
                                errorsMessage.add(VPResult.getMessage());
                            }
                        }
                    }
                    validatedrgclaim.setPROCEDURES(procedures);
                }
            }

            // RETURN RESULT
            if (!errors.isEmpty()) {
                errorsMessage.add("Patient information area has an errors");
                result.setMessage(errorsMessage.toString());
            }
            result.setResult(utility.objectMapper().writeValueAsString(validatedrgclaim));
            result.setSuccess(true);
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Upload.class.getName()).log(Level.SEVERE, null, ex);
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
