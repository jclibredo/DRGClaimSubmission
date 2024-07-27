/*
 * File:           CF5Handler.java
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

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;

/**
 *
 * @author MinoSun
 */
public interface CF5Handler {

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_PROCEDURES(final AttributeList meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_PROCEDURES() throws SAXException;

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_CF5(final AttributeList meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_CF5() throws SAXException;

    /**
     *
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_PROCEDURE(final AttributeList meta) throws SAXException;

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_SECONDARYDIAGS(final AttributeList meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_SECONDARYDIAGS() throws SAXException;

    /**
     *
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_DRGCLAIM(final AttributeList meta) throws SAXException;

    /**
     *
     * An empty element event handling method.
     * @param data value or null
     */
    public void handle_SECONDARYDIAG(final AttributeList meta) throws SAXException;
    
}