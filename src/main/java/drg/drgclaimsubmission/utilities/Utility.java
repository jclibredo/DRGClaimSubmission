/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities;

import drg.drgclaimsubmission.methods.CF5Method;
import drg.drgclaimsubmission.seekermethod.SeekerResult;
import drg.drgclaimsubmission.structures.DRGOutput;
import drg.drgclaimsubmission.structures.DRGWSResult;
import drg.drgclaimsubmission.structures.EncryptedXML;
import drg.drgclaimsubmission.structures.GrouperParameter;
import drg.drgclaimsubmission.structures.KeyPerValueError;
import drg.drgclaimsubmission.structures.WarningErrorList;
import drg.drgclaimsubmission.structures.XMLReport;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import okhttp3.OkHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author MINOSUN
 */
@ApplicationScoped
@Singleton
public class Utility {

    public Utility() {
    }

    public DRGWSResult DRGWSResult() {
        return new DRGWSResult();
    }

    public XMLReport XMLReport() {
        return new XMLReport();
    }

    public SeekerResult SeekerResult() {
        return new SeekerResult();
    }

    public KeyPerValueError KeyPerValueError() {
        return new KeyPerValueError();
    }

    public GrouperParameter GrouperParameter() {
        return new GrouperParameter();
    }

    public DRGOutput DRGOutput() {
        return new DRGOutput();
    }

    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    public EncryptedXML EncryptedXML() {
        return new EncryptedXML();
    }

    public WarningErrorList WarningErrorList() {
        return new WarningErrorList();
    }

    public OkHttpClient OkHttpClient() {
        return new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
    }

    public SimpleDateFormat SimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    public String GetString(String name) {
        String result = "";
        try {
            Context context = new InitialContext();
            Context environment = (Context) context.lookup("java:comp/env");
            result = (String) environment.lookup(name);
        } catch (NamingException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        }
        return result;
    }

    public String CleanCode(String data) {
        return data.trim().replaceAll("\\.", "").toUpperCase();
    }

    public String RandomAlphaNumeric(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(("abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase() + "0123456789").length());
            char rndChar = ("abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase() + "0123456789").charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomAlpha(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(("abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase()).length());
            char rndChar = ("abcdefghijklmnopqrstuvwxyz" + "abcdefghijklmnopqrstuvwxyz".toUpperCase()).charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomNumeric(int length) {
        //reset daily random number series 
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt("0123456789".length());
            char rndChar = "0123456789".charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public boolean IsValidNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            e.getLocalizedMessage();
            return false;
        }
    }

    public boolean isStringFromatVAlid(String claimnumber) {
        boolean isValid = claimnumber.matches("\\d{6}-\\d{8}-\\d{1}-\\d{1}");
        return isValid;
    }

    public boolean isValidPhoneNumber(String phone_number) {
        boolean isValid = phone_number.matches("\\d{11}");
        return isValid;
    }

    public boolean IsValidDate(String string) {
        this.SimpleDateFormat("MM-dd-yyyy").setLenient(false);
        try {
            this.SimpleDateFormat("MM-dd-yyyy").parse(string);
            return true;
        } catch (ParseException e) {
            e.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    public boolean isValidNumeric(String strNum) {
        boolean result = false;
        if (strNum == null) {
            result = false;
        }
        try {
            double d = Double.parseDouble(strNum);
            result = true;
        } catch (NumberFormatException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            result = false;
        }
        return result;
    }

    public boolean IsSURGEValidTime(String time) {
        boolean isValid = time.matches("(1[0-2]|0?[1-9]):[0-5][0-9](\\s)(?i)(am|pm)");
        return isValid;
    }

    public boolean IsAITMDValidTime(String time) {
        boolean isValid = time.matches("(1[0-2]|0?[1-9]):[0-5][0-9]:[0-5][0-9](?i)(am|pm)");
        return isValid;
    }

    public boolean IsBITMDValidTime(String time) {
        boolean isValid = time.matches("(1[0-2]|0?[1-9]):[0-5][0-9]:[0-5][0-9](\\s)(?i)(am|pm)");
        return isValid;
    }

    public boolean IsValidPIN(String string) {
        boolean result = false;
        int[] weights = {6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 1};
        int sum = 0;
        try {
            String PIN = string.replaceAll("[-,]", "");
            Double.parseDouble(PIN);
            if (PIN.length() == 12) {
                for (int i = 0; i < 12; i++) {
                    int intpin = Integer.parseInt(PIN.substring(i, i + 1));
                    sum = sum + (intpin * (weights[i]));
                }
                if ((sum % 11) == 0) {
                    result = true;
                }
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            e.getLocalizedMessage();
            return result;
        }
        return result;
    }

    public boolean IsValidDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            e.getLocalizedMessage();
            return false;
        }
    }

    public boolean ValidDate(String stringdate, String pattern) {
        this.SimpleDateFormat(pattern).setLenient(false);
        try {
            this.SimpleDateFormat(pattern).parse(stringdate);
            return true;
        } catch (ParseException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            e.getLocalizedMessage();
            return false;
        }
    }

    public boolean ValidDateRange(String startdate, String enddate, String pattern) {
        boolean result = true;
        try {
            Date datestart = this.SimpleDateFormat(pattern).parse(startdate);
            Date dateend = this.SimpleDateFormat(pattern).parse(enddate);
            if (datestart.compareTo(dateend) > 0) {
                result = false;
            }
        } catch (ParseException e) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
            e.getLocalizedMessage();
            result = false;
        }
        return result;
    }

    //==============================================
    public boolean MaxAge(String DOB, String AD) {
        boolean result = false;
        try {
//            Date DateOfBirth = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(DOB);
//            Date AdmissioDate = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(AD);//PARAM
            long difference_In_Time = Math.abs(new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(AD).getTime() - new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(DOB).getTime());
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = AgeY > 124;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    public int ComputeSURGETime(String DateIn, String TimeIn, String DateOut, String TimeOut) {
        int result = 0;
        try {
//            Date AdmissionTime = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((DateIn + "" + TimeIn).replaceAll("\\s", "")); //PARAM
//            Date DischargeTime = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((DateOut + "" + TimeOut).replaceAll("\\s", ""));//PARAM
            long Time_difference = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((DateOut + "" + TimeOut).replaceAll("\\s", "")).getTime() - this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((DateIn + "" + TimeIn).replaceAll("\\s", "")).getTime();
            long Hours_difference = (Time_difference / (1000 * 60 * 60)) % 24;
            result = (int) Hours_difference;
            return result;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeITMDTime(String DateIn, String TimeIn, String DateOut, String TimeOut) {
        int result = 0;
        try {
//            Date AdmissionTime = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((DateIn + "" + TimeIn).replaceAll("\\s", "")); //PARAM
//            Date DischargeTime = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((DateOut + "" + TimeOut).replaceAll("\\s", ""));//PARAM
            long Time_difference = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((DateOut + "" + TimeOut).replaceAll("\\s", "")).getTime() - this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((DateIn + "" + TimeIn).replaceAll("\\s", "")).getTime();
            long Hours_difference = (Time_difference / (1000 * 60 * 60)) % 24;
            result = (int) Hours_difference;
            return result;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int MinutesSURGECompute(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
//            Date AdmissionDateTime = this.SimpleDateFormat("MM-dd-yyyyhh:mm aa").parse((datein + "" + timein).replaceAll("\\s", "")); //PARAM
//            Date DischargeDateTime = this.SimpleDateFormat("MM-dd-yyyyhh:mm aa").parse((dateout + "" + timeout).replaceAll("\\s", ""));//PARAM
            long difference_In_Time = this.SimpleDateFormat("MM-dd-yyyyhh:mm aa").parse((dateout + "" + timeout).replaceAll("\\s", "")).getTime() - this.SimpleDateFormat("MM-dd-yyyyhh:mm aa").parse((datein + "" + timein).replaceAll("\\s", "")).getTime();
            long Minutes = (difference_In_Time / (1000 * 60)) % 60;
            result = (int) Minutes;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public int MinutesITMDCompute(String datein, String timein, String dateout, String timeout) {
//        int result = 0;
//        try {
//            String IN = datein + timein;
//            String OUT = dateout + timeout;
//            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa");
//            Date AdmissionDateTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
//            Date DischargeDateTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
//            long difference_In_Time = DischargeDateTime.getTime() - AdmissionDateTime.getTime();
//            long Minutes = (difference_In_Time / (1000 * 60)) % 60;
//            result = (int) Minutes;
//        } catch (ParseException ex) {
//            ex.getLocalizedMessage();
//            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    public boolean TimeDif(String timein, String timeout) {
        boolean result = false;
        try {
            if (this.IsSURGEValidTime(timein) && this.IsSURGEValidTime(timeout)) {
                result = this.SimpleDateFormat("hh:mm a").parse(timein).after(this.SimpleDateFormat("hh:mm a").parse(timeout));
            } else if (this.IsAITMDValidTime(timein) && this.IsAITMDValidTime(timeout)) {
                result = this.SimpleDateFormat("hh:mm:ssaa").parse(timein).after(this.SimpleDateFormat("hh:mm:ssaa").parse(timeout));
            } else if (this.IsBITMDValidTime(timein) && this.IsBITMDValidTime(timeout)) {
                result = this.SimpleDateFormat("hh:mm:ss aa").parse(timein).after(this.SimpleDateFormat("hh:mm:ss aa").parse(timeout));
            }
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    public int ComputeYear(String DOB, String AD) {
        int result = 0;
        try {
            long difference_In_Time = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(AD).getTime() - new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(DOB).getTime();
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = (int) AgeY;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String Convert12to24(String times) {
        String result = "";
        try {
            result = this.SimpleDateFormat("HH:mm").format(this.SimpleDateFormat("hh:mm a").parse(times));
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String Convert24to12(String times) {
        String result = "";
        try {
            result = this.SimpleDateFormat("hh:mma").format(new SimpleDateFormat("HH:mm").parse(times));
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeDay(String DOB, String AD) {
        int result = 0;
        try {
            long difference_In_Time = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(AD).getTime() - new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH).parse(DOB).getTime();
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) difference_In_Days;
            return result;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeSURGELOS(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
//            Date AdmissioDate = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((datein + "" + timein).replaceAll("\\s", "")); //PARAM
//            Date DischargeDate = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((dateout + "" + timeout).replaceAll("\\s", ""));//PARAM
            long difference_In_Time = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((dateout + "" + timeout).replaceAll("\\s", "")).getTime() - this.SimpleDateFormat("MM-dd-yyyyhh:mmaa").parse((datein + "" + timein).replaceAll("\\s", "")).getTime();
            long CalLOS = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) CalLOS;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeITMDLOS(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
            Date AdmissioDate = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((datein + "" + timein).replaceAll("\\s", "")); //PARAM
            Date DischargeDate = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa").parse((dateout + "" + timeout).replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDate.getTime() - AdmissioDate.getTime();
            long CalLOS = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) CalLOS;
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String CodeConverter(DataSource datasouce, String rvs) {
        String result = "";
        for (int m = 0; m < Arrays.asList(rvs.split(",")).size(); m++) {
            DRGWSResult finalResult = new CF5Method().GetICD9cm(datasouce, Arrays.asList(rvs.split(",")).get(m).trim());
            if (finalResult.isSuccess()) {
                result = finalResult.getResult();
            }
        }
        return result;
    }

    public String DTDFilePath() {
        String DTDFile = "<!ELEMENT CF5 (DRGCLAIM)>\n"
                + "<!ATTLIST CF5 \n"
                + "     pHospitalCode       CDATA #REQUIRED\n"
                + "   >\n"
                + "<!ELEMENT DRGCLAIM (SECONDARYDIAGS,PROCEDURES)>\n "
                + "<!ATTLIST DRGCLAIM \n"
                + "     PrimaryCode         CDATA #REQUIRED\n"
                + "     NewBornAdmWeight    CDATA #REQUIRED\n"
                + "     Remarks             CDATA #REQUIRED\n"
                + "     ClaimNumber         CDATA #REQUIRED\n"
                + ">\n"
                + "<!ELEMENT SECONDARYDIAGS (SECONDARYDIAG)*>\n"
                + "<!ELEMENT SECONDARYDIAG EMPTY>\n"
                + "<!ATTLIST SECONDARYDIAG\n"
                + "     SecondaryCode       CDATA #REQUIRED\n"
                + "     Remarks             CDATA #REQUIRED\n"
                + "   >\n"
                + "<!ELEMENT PROCEDURES (PROCEDURE)*>\n"
                + "<!ELEMENT PROCEDURE EMPTY>\n"
                + "<!ATTLIST PROCEDURE\n"
                + "  RvsCode 		CDATA #REQUIRED\n"
                + "  Laterality         CDATA #REQUIRED\n"
                + "  Ext1 		CDATA #REQUIRED\n"
                + "  Ext2 		CDATA #REQUIRED\n"
                + "  Remarks 		CDATA #REQUIRED\n"
                + "   >";
        return DTDFile;
    }

    public boolean isParsableDate(String dateString, String dateFormat) {
        boolean result = false;
        this.SimpleDateFormat(dateFormat).setLenient(false);
        try {
            this.SimpleDateFormat(dateFormat).parse(dateString);
            result = true;
        } catch (ParseException e) {
        }
        return result;
    }

}
