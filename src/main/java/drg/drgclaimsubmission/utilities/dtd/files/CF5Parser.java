/*
 * File:           CF5Parser.java
 * Date:           July 22, 2024  2:53 PM
 *
 * @author  MinoSun
 * @version generated by NetBeans XML module
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities.dtd.files;

import java.io.IOException;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.*;

/**
 *
 * @author MinoSun
 */
public class CF5Parser implements DocumentHandler {

    private final CF5Handler handler;
    private final Stack context;
    private final StringBuffer buffer;
    private final EntityResolver resolver;

    /**
     *
     * Creates a parser instance.
     * @param handler handler interface implementation (never <code>null</code>
     * @param resolver SAX entity resolver implementation or <code>null</code>.
     * It is recommended that it could be able to resolve at least the DTD.
     */
    public CF5Parser(final CF5Handler handler, final EntityResolver resolver) {
        this.handler = handler;
        this.resolver = resolver;
        buffer = new StringBuffer(111);
        context = new java.util.Stack();
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     */
    @Override
    public final void setDocumentLocator(Locator locator) {
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     */
    @Override
    public final void startDocument() throws SAXException {
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     */
    @Override
    public final void endDocument() throws SAXException {
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     * @param attrs
     */
    @Override
    public final void startElement(java.lang.String name, org.xml.sax.AttributeList attrs) throws org.xml.sax.SAXException {
        dispatch(true);
        context.push(new Object[]{name, new org.xml.sax.helpers.AttributeListImpl(attrs)});
        if (null != name) switch (name) {
            case "PROCEDURES":
                handler.start_PROCEDURES(attrs);
                break;
            case "CF5":
                handler.start_CF5(attrs);
                break;
            case "PROCEDURE":
                handler.handle_PROCEDURE(attrs);
                break;
            case "SECONDARYDIAGS":
                handler.start_SECONDARYDIAGS(attrs);
                break;
            case "DRGCLAIM":
                handler.handle_DRGCLAIM(attrs);
                break;
            case "SECONDARYDIAG":
                handler.handle_SECONDARYDIAG(attrs);
                break;
            default:
                break;
        }
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     */
    @Override
    public final void endElement(java.lang.String name) throws org.xml.sax.SAXException {
        dispatch(false);
        context.pop();
        if (null != name) switch (name) {
            case "PROCEDURES":
                handler.end_PROCEDURES();
                break;
            case "CF5":
                handler.end_CF5();
                break;
            case "SECONDARYDIAGS":
                handler.end_SECONDARYDIAGS();
                break;
            default:
                break;
        }
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     * @param chars
     * @param len
     */
    @Override
    public final void characters(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     * @param chars
     * @param len
     */
    @Override
    public final void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
    }

    /**
     *
     * This SAX interface method is implemented by the parser.
     */
    @Override
    public final void processingInstruction(String target, String data) throws SAXException {
    }

    private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) {
            return; //skip it
        }
        Object[] ctx = (Object[]) context.peek();
        String here = (String) ctx[0];
        org.xml.sax.AttributeList attrs = (org.xml.sax.AttributeList) ctx[1];
        buffer.delete(0, buffer.length());
    }

    /**
     *
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying the requested configuration cannot be created
     */
    public void parse(final org.xml.sax.InputSource input) throws SAXException, ParserConfigurationException, IOException {
        parse(input, this);
    }

    /**
     *
     * The recognizer entry method taking a URL.
     * @param url URL Source to be parsed.
     * @throws java.io.IOException on I/O error
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying the requested configuration cannot be created
     */
    public void parse(final java.net.URL url) throws SAXException, ParserConfigurationException, IOException {
        parse(new org.xml.sax.InputSource(url.toExternalForm()), this);
    }

    /**
     *
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @param handler
     * @throws java.io.IOException on I/O error
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying the requested configuration cannot be created
     */
    public static void parse(final org.xml.sax.InputSource input, final CF5Handler handler) throws SAXException, ParserConfigurationException, IOException {
        parse(input, new CF5Parser(handler, null));
    }

    /**
     *
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @param handler
     * @throws java.io.IOException on I/O error
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfying the requested configuration cannot be created
     */
    public static void parse(final java.net.URL url, final CF5Handler handler) throws SAXException, ParserConfigurationException, IOException {
        parse(new org.xml.sax.InputSource(url.toExternalForm()), handler);
    }

    private static void parse(final org.xml.sax.InputSource input, final CF5Parser recognizer) throws SAXException, ParserConfigurationException, IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(true); //the code was generated according DTD
        factory.setNamespaceAware(false); //the code was generated according DTD
        Parser parser = factory.newSAXParser().getParser();
        parser.setDocumentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());
        if (recognizer.resolver != null) {
            parser.setEntityResolver(recognizer.resolver);
        }
        parser.parse(input);
    }

    /**
     *
     * Creates default error handler used by this parser.
     * @return org.xml.sax.ErrorHandler implementation
     */
    protected ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void error(SAXParseException ex) throws SAXException {
                if (context.isEmpty()) {
                    System.err.println("Missing DOCTYPE.");
                }
                throw ex;
            }

            @Override
            public void fatalError(SAXParseException ex) throws SAXException {
                throw ex;
            }

            @Override
            public void warning(SAXParseException ex) throws SAXException {
                // ignore
            }
        };
    }
    
}
