/*
 * File:           CF5HandlerImpl.java
 * Date:           July 22, 2024  2:53 PM
 *
 * @author  DRG_SHADOWBILLING
 * @version generated by NetBeans XML module
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities.dtd.files;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 *
 * @author DRG_SHADOWBILLING
 */
public class CF5HandlerImpl implements CF5Handler {

    public static final boolean DEBUG = false;

    @Override
    public void start_PROCEDURES(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("start_PROCEDURES: " + meta);
        }
    }

    @Override
    public void end_PROCEDURES() throws SAXException {
        if (DEBUG) {
            System.err.println("end_PROCEDURES()");
        }
    }

    @Override
    public void start_CF5(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("start_CF5: " + meta);
        }
    }

    @Override
    public void end_CF5() throws SAXException {
        if (DEBUG) {
            System.err.println("end_CF5()");
        }
    }

    @Override
    public void handle_PROCEDURE(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("handle_PROCEDURE: " + meta);
        }
    }

    @Override
    public void start_SECONDARYDIAGS(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("start_SECONDARYDIAGS: " + meta);
        }
    }

    @Override
    public void end_SECONDARYDIAGS() throws SAXException {
        if (DEBUG) {
            System.err.println("end_SECONDARYDIAGS()");
        }
    }

    @Override
    public void handle_DRGCLAIM(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("handle_DRGCLAIM: " + meta);
        }
    }

    @Override
    public void handle_SECONDARYDIAG(final AttributeList meta) throws SAXException {
        if (DEBUG) {
            System.err.println("handle_SECONDARYDIAG: " + meta);
        }
    }
    
}
