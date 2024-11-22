/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.methods;

import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.XMLErrors;
import drg.drgclaimsubmission.structures.dtd.CF5;
import drg.drgclaimsubmission.utilities.Utility;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ValidateXMLWithDTD {

    public ValidateXMLWithDTD() {
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
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            final ArrayList<String> arraywarning = new ArrayList<>();
            final ArrayList<String> arrayerror = new ArrayList<>();
            final ArrayList<String> arrayfatalerror = new ArrayList<>();
            db.setErrorHandler(new ErrorHandler() {
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
            Document doc = db.parse(new InputSource(new StringReader(stringxml)));
            JAXBContext jaxbcontext = JAXBContext.newInstance(CF5.class);
            Unmarshaller jaxbnmarsaller = jaxbcontext.createUnmarshaller();
            StringReader readers = new StringReader(stringdrgxml);
            CF5 drg = (CF5) jaxbnmarsaller.unmarshal(readers);

            if ((arrayfatalerror.isEmpty()) && (arrayerror.isEmpty())) {
                DRGWSResult keypervalue = new ValidateXMLValues().ValidateXMLValues(datasource, drg, lhio, claimseries, filecontent);
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
                result.setSuccess(false);
            }

        } catch (ParserConfigurationException | IOException | SAXException | JAXBException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateXMLWithDTD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
