/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import drg.drgclaimsubmission.methods.ParseEClaimsDrgXML;
import drg.drgclaimsubmission.methods.RemoveTrailingSpaces;
import drg.drgclaimsubmission.methods.ValidateXMLWithDTD;
import drg.drgclaimsubmission.seekermethod.DataArrangement;
import drg.drgclaimsubmission.seekermethod.GetCF5Parameter;
import drg.drgclaimsubmission.seekermethod.SeekerResult;
import drg.drgclaimsubmission.seekermethod.Series;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.GrouperParameter;
import drg.drgclaimsubmission.structures.NClaimsData;
import drg.drgclaimsubmission.structures.XMLErrors;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.utilities.GrouperMethod;
import drg.drgclaimsubmission.utilities.NamedParameterStatement;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * REST Web Service
 *
 * @author MinoSun
 */
@Path("DRGClaim")
@RequestScoped
public class DRGClaims {

    public DRGClaims() {
    }

    @Resource(lookup = "jdbc/drgsbuser")
    private DataSource datasource;

    private final Utility utility = new Utility();
    private final ValidateXMLWithDTD vxwdtd = new ValidateXMLWithDTD();
    private final RemoveTrailingSpaces removedSpace = new RemoveTrailingSpaces();
    private final ParseEClaimsDrgXML ped = new ParseEClaimsDrgXML();
    private final GrouperMethod gm = new GrouperMethod();
    private final DataArrangement dataarrange = new DataArrangement();
    private final GetCF5Parameter gp = new GetCF5Parameter();

    //Gget Server Data and Time
    @GET
    @Path("GetServerDateTime")
    @Produces(MediaType.APPLICATION_JSON)
    public String GetServerDateTime() {
        String result = "";
        SimpleDateFormat sdf = utility.SimpleDateFormat("hh:mm:ss a");
        try (Connection connection = datasource.getConnection()) {
            String query = "SELECT SYSDATE FROM DUAL";
            NamedParameterStatement SDxVal = new NamedParameterStatement(connection, query);
            SDxVal.execute();
            ResultSet rest = SDxVal.executeQuery();
            if (rest.next()) {
                result = sdf.format(rest.getDate("SYSDATE"));
            }
        } catch (SQLException ex) {
            result = ex.toString();
            Logger.getLogger(DRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // This Area will accept encrypted XML file from the hospital and Validate with DTD and KEY per Value
    @POST
    @Path("UploadXML")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult UploadXML(
            @FormDataParam("drg") InputStream uploadeddrg,
            @FormDataParam("drg") FormDataContentDisposition drgdetail,
            @FormDataParam("ClaimSeriesNum") String ClaimSeriesNum) throws JAXBException, SQLException {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        // String ClaimSeriesNum = ClaimSeriesNums.replaceAll("\\s+", "");
        if (uploadeddrg == null || ClaimSeriesNum == null) {
            String details = "Unreadable file directory or variable name error in FormDataParam";
            DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, "FAILED", "", "", "CF5 Claim Form");
            result.setMessage("Variable name for DRGXML not equal to (drg) OR ClaimSeries not equal to (ClaimSeriesNum) or file directory not found");
            result.setResult(" Request status :" + auditrail.getMessage());
        } else {
            String drgfilename = drgdetail.getFileName();
            BufferedReader reader = new BufferedReader(new InputStreamReader(uploadeddrg));
            if (drgfilename.length() == 0 && ClaimSeriesNum.replaceAll("\\s+", "").length() == 0) {
                result.setMessage("CF5 DRG XML File  and ClaimSeriesNumber is Empty");
            } else if (drgfilename.length() == 0) {
                result.setMessage("CF5 DRG XML File NOT FOUND");
            } else if (ClaimSeriesNum.replaceAll("\\s+", "").length() == 0) {
                String drgfilelines = "";
                String drgfilecontents = "";
                try {
                    while ((drgfilelines = reader.readLine()) != null) {
                        drgfilecontents += drgfilelines;
                    }
                    String stats = "FAILED";
                    String series = "";
                    String claimnum = "";
                    String details = "";
                    if (ClaimSeriesNum.replaceAll("\\s+", "").length() == 0) {
                        details = "CF5 ClaimSeriesNum is Empty";
                    } else {
                        details = "CF5 Claim Series size is not valid and does not match the 14 digit format";
                    }
                    DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource, details, stats, series, claimnum, drgfilename);
                    result.setMessage(details + " DRG Claims Status " + auditrail.getMessage());
                } catch (IOException ex) {
                    result.setMessage(ex.toString());
                    Logger.getLogger(DRGClaims.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                String drgfileline = "";
                String drgfilecontent = "";
                try {
                    while ((drgfileline = reader.readLine()) != null) {
                        drgfilecontent += drgfileline;
                    }
                    //  METHOD THIS AREA SI TO GET DATA FROM NCLAIMS USING CLAIM SERRIES NUMBER
                    String claimsSeriesLhioNums = ClaimSeriesNum.replaceAll("\\s+", "");
                    String claimsSerries = claimsSeriesLhioNums.substring(0, Math.min(claimsSeriesLhioNums.length(), 13));
                    String lhio = claimsSeriesLhioNums.substring(Math.max(claimsSeriesLhioNums.length() - 2, 0));
                    DRGWSResult cleanData = removedSpace.RemoveTrailingSpaces(drgfilecontent);
                    if (cleanData.isSuccess()) {
                        DRGWSResult validatedData = vxwdtd.ValidateXMLWithDTD(cleanData.getResult(), datasource, lhio, claimsSerries, drgfilename);
                        result.setResult(validatedData.getResult());
                        result.setMessage(validatedData.getMessage());
                        result.setSuccess(validatedData.isSuccess());
                    } else {
                        DRGWSResult auditrail = gm.InsertDRGAuditTrail(datasource,
                                cleanData.getMessage(),
                                String.valueOf(cleanData.isSuccess()).toUpperCase(),
                                claimsSerries,
                                claimsSeriesLhioNums,
                                drgfilename);
                        result.setMessage(cleanData.getMessage() + " , " + auditrail.getMessage());
                        result.setSuccess(cleanData.isSuccess());
                    }

                } catch (IOException ex) {
                    result.setMessage(ex.toString());
                    Logger.getLogger(DRGClaims.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return result;
    }

    //THIS AREA IS TO VALIDATE VALUE FOR DRG DATA BEFORE SUBMISSION
    @POST
    @Path("KeyValuePairValidation")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult KeyValuePairValidation(
            @FormDataParam("drg") InputStream uploadeddrg,
            @FormDataParam("drg") FormDataContentDisposition drgdetail,
            @FormDataParam("eclaims") InputStream uploadedeclaims,
            @FormDataParam("eclaims") FormDataContentDisposition eclaimsdetail) throws JAXBException, SQLException {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        XMLErrors xmlerrors = new XMLErrors();
//------------------------------------
        try {
            if (uploadeddrg == null || uploadedeclaims == null) {
                result.setMessage("Variable name for DRGXML not equal to (drg) OR ECLAIMSXML not equal to (eclaims) or file directory not found");
                result.setResult("");
            } else {
                String drgfilename = drgdetail.getFileName();
                String eclaimsfilename = eclaimsdetail.getFileName();
                if (drgfilename.length() == 0) {
                    result.setMessage("CF5 XML File NOT FOUND");
                    result.setResult("");
                } else if (eclaimsfilename.length() == 0) {
                    result.setMessage("ECLAIMS XML File NOT FOUND");
                    result.setResult("");
                } else {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(uploadeddrg));
                    String drgfileline = "";
                    String stringdrgxml = "";
                    while ((drgfileline = reader.readLine()) != null) {
                        stringdrgxml += drgfileline;
                    }
                    //  XML CONTENT AND DTD CONTENT COMBINE AREA
                    String stringxml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<!DOCTYPE CF5 [" + utility.DTDFilePath() + "]>\n" + stringdrgxml;
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setValidating(true);
                    DocumentBuilder db;
                    db = dbf.newDocumentBuilder();
                    //  END XML CONTENT AND DTD CONTENT COMBINE AREA
                    final ArrayList<String> arraywarning = new ArrayList<>();
                    final ArrayList<String> arrayerror = new ArrayList<>();
                    final ArrayList<String> arrayfatalerror = new ArrayList<>();
                    db.setErrorHandler(new ErrorHandler() {
                        @Override
                        public void warning(SAXParseException exception) throws SAXException {
                            int lineno = exception.getLineNumber();
                            arraywarning.add("Line No. " + lineno + " : " + exception.getMessage());
                        }

                        @Override
                        public void fatalError(SAXParseException exception) throws SAXException {
                            int lineno = exception.getLineNumber();
                            arrayfatalerror.add("Line No. " + lineno + " : " + exception.getMessage());
                        }

                        @Override
                        public void error(SAXParseException exception) throws SAXException {
                            int lineno = exception.getLineNumber();
                            arrayerror.add("Line No. " + lineno + " : " + exception.getMessage());
                        }
                    });
                    //-------------------------------------------------------
                    Document doc = db.parse(new InputSource(new StringReader(stringxml)));
                    JAXBContext jaxbcontext = JAXBContext.newInstance(CF5.class);
                    Unmarshaller jaxbnmarsaller = jaxbcontext.createUnmarshaller();
                    StringReader readers = new StringReader(stringdrgxml);
                    CF5 drg = (CF5) jaxbnmarsaller.unmarshal(readers);

                    //E-CLAIMS XML PARSING AREA
                    BufferedReader rd = new BufferedReader(new InputStreamReader(uploadedeclaims));
                    String eclaimfileline = "";
                    String eclaimfilecontent = "";
                    while ((eclaimfileline = rd.readLine()) != null) {
                        eclaimfilecontent += eclaimfileline;
                    }
                    //END E-CLAIMS XML PARSING AREA
                    if ((arrayfatalerror.isEmpty()) && (arrayerror.isEmpty())) {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document docs = dBuilder.parse(new InputSource(new StringReader(eclaimfilecontent)));
                        docs.getDocumentElement().normalize();

                        ArrayList<String> idlist = new ArrayList<>();
                        //-----------------------------------------------
                        NodeList eclaimspHospitalCode = docs.getElementsByTagName("eCLAIMS");
                        //-----------------------------------------------
                        ArrayList<NClaimsData> nclaimsdatalist = new ArrayList<>();
                        NodeList nList = docs.getElementsByTagName("CLAIM");
                        //------------------------------------------------
                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            NClaimsData nclaimsdata = new NClaimsData();

                            //GET THE HOSPITAL CODE
                            Node nNodess = eclaimspHospitalCode.item(0);
                            if (nNodess.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElements = (Element) nNodess;
                                nclaimsdata.setHospitalcode(eElements.getAttribute("pHospitalCode"));
                            }

                            //GET THE pClaimNumber
                            Node nNodes = nList.item(temp);
                            if (nNodes.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElements = (Element) nNodes;
                                nclaimsdata.setPclaimnumber(eElements.getAttribute("pClaimNumber"));
                                idlist.add(eElements.getAttribute("pClaimNumber"));
                            }
                            //GET DATA FROM CF1
                            NodeList cf1 = docs.getElementsByTagName("CF1");
                            Node nNodecf1 = cf1.item(temp);
                            if (nNodecf1.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElementcf1 = (Element) nNodecf1;
                                nclaimsdata.setDateofBirth(eElementcf1.getAttribute("pPatientBirthDate"));
                                nclaimsdata.setGender(eElementcf1.getAttribute("pPatientSex"));
                            }
                            //GET DATA FROM CF2
                            NodeList cf2 = docs.getElementsByTagName("CF2");
                            Node nNodecf2 = cf2.item(temp);
                            if (nNodecf2.getNodeType() == Node.ELEMENT_NODE) {
                                Element eElementcf2 = (Element) nNodecf2;
                                nclaimsdata.setAdmissionDate(eElementcf2.getAttribute("pAdmissionDate"));
                                nclaimsdata.setTimeAdmission(eElementcf2.getAttribute("pAdmissionTime"));
                                nclaimsdata.setDischargeDate(eElementcf2.getAttribute("pDischargeDate"));
                                nclaimsdata.setTimeDischarge(eElementcf2.getAttribute("pDischargeTime"));
                                nclaimsdata.setDischargeType(eElementcf2.getAttribute("pDisposition"));
                                nclaimsdata.setExpiredDate(eElementcf2.getAttribute("pExpiredDate"));
                                nclaimsdata.setExpiredTime(eElementcf2.getAttribute("pExpiredTime"));
                            }
                            nclaimsdatalist.add(nclaimsdata);
                        }

                        //DATA VALIDATION METHOD
                        DRGWSResult pedResult = ped.ParseEClaimsDrgXML(datasource, drg, nclaimsdatalist, idlist);
                        result.setResult(pedResult.getResult());
                        result.setMessage(pedResult.getMessage());
                        result.setSuccess(pedResult.isSuccess());
                        //END DATA VALIDATION METHOD
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
                        result.setMessage("XML have errors");
                        result.setResult(utility.objectMapper().writeValueAsString(xmlerrors));
                    }
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(DRGClaims.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //THIS AREA IS TO VALIDATE VALUE FOR DRG DATA BEFORE SUBMISSION
//    @POST
//    @Path("GetSeeker")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public DRGWSResult GetSeeker(final GrouperParameter grouperparam) throws JAXBException, SQLException {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        DRGWSResult resulst = dataarrange.DataArrangement(datasource, grouperparam);
//        result.setMessage(resulst.getMessage());
//        result.setResult(resulst.getResult());
//        result.setSuccess(resulst.isSuccess());
//        return result;
//    }
//    @POST
//    @Path("GetCF5Data")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public SeekerResult GetCF5Data(final Series series) throws JAXBException, SQLException {
//        SeekerResult result = utility.SeekerResult();
//        SeekerResult resulst = gp.CombinedResult(datasource, series.getSeries());
//        result.setDxdiag(resulst.getDxdiag());
//        result.setInfo(resulst.getInfo());
//        result.setMessage(resulst.getMessage());
//        result.setProcedure(resulst.getProcedure());
//        result.setSuccess(resulst.isSuccess());
//        result.setWarning(resulst.getWarning());
//        return result;
//    }
}
