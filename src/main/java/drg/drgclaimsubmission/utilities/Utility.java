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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
 * @author DRG_SHADOWBILLING
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

    public String RandomAlphaNumeric(int length) {
        String char_lower = "abcdefghijklmnopqrstuvwxyz";
        String char_upper = char_lower.toUpperCase();
        String number = "0123456789";
        String data = char_lower + char_upper + number;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomAlpha(int length) {
        String char_lower = "abcdefghijklmnopqrstuvwxyz";
        String char_upper = char_lower.toUpperCase();
        String data = char_lower + char_upper;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
            builder.append(rndChar);
        }
        return builder.toString();
    }

    public String RandomNumeric(int length) {
        //reset daily random number series 
        String number = "0123456789";
        String data = number;
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(data.length());
            char rndChar = data.charAt(rndCharAt);
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

//    public String getFullUrl(HttpServletRequest request) {
//        if (request.getQueryString() == null) {
//            return request.getRequestURI();
//        }
//        return request.getRequestURI() + "?" + request.getQueryString();
//    }
    public boolean isStringFromatVAlid(String claimnumber) {
        boolean isValid = claimnumber.matches("\\d{6}-\\d{8}-\\d{1}-\\d{1}");
        return isValid;
    }

    public boolean isValidPhoneNumber(String phone_number) {
        boolean isValid = phone_number.matches("\\d{11}");
        return isValid;
    }

    public boolean IsValidDate(String string) {
        SimpleDateFormat newsdf = new SimpleDateFormat("MM-dd-yyyy");
        newsdf.setLenient(false);
        try {
            newsdf.parse(string);
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
            e.getLocalizedMessage();
            result = false;
        }
        return result;
    }

    public boolean IsSURGEValidTime(String time) {
        boolean isValid = time.matches("(1[0-2]|0?[1-9]):[0-5][0-9](\\s)(?i)(am|pm)");
        return isValid;
    }

    public boolean IsITMDValidTime(String time) {
        boolean isValid = time.matches("(1[0-2]|0?[1-9]):[0-5][0-9]:[0-5][0-9](?i)(am|pm)");
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
        SimpleDateFormat newsdf = this.SimpleDateFormat(pattern);
        newsdf.setLenient(false);
        try {
            newsdf.parse(stringdate);
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
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
        try {
            java.util.Date DateOfBirth = sdf.parse(DOB);
            java.util.Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = Math.abs(AdmissioDate.getTime() - DateOfBirth.getTime());
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
            String IN = DateIn + TimeIn;
            String OUT = DateOut + TimeOut;
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            Date AdmissionTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long Time_difference = DischargeTime.getTime() - AdmissionTime.getTime();
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
            String IN = DateIn + TimeIn;
            String OUT = DateOut + TimeOut;
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa");
            Date AdmissionTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long Time_difference = DischargeTime.getTime() - AdmissionTime.getTime();
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
            String IN = datein + timein;
            String OUT = dateout + timeout;
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            Date AdmissionDateTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDateTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDateTime.getTime() - AdmissionDateTime.getTime();
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

    public int ComputeYear(String DOB, String AD) {
        int result = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
        try {
            Date DateOfBirth = sdf.parse(DOB);
            Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
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
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        try {
            Date dates = parseFormat.parse(times);
            result = displayFormat.format(dates);

        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String Convert24to12(String times) {
        String result = "";
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mma");
        try {
            Date time24 = displayFormat.parse(times);
            result = parseFormat.format(time24);
        } catch (ParseException ex) {
            ex.getLocalizedMessage();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeDay(String DOB, String AD) {
        int result = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            Date DateOfBirth = sdf.parse(DOB);
            Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
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
            //   SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssa");
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            String IN = datein + timein;
            String OUT = dateout + timeout;
            Date AdmissioDate = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDate = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDate.getTime() - AdmissioDate.getTime();
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
            //   SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssa");
            SimpleDateFormat times = this.SimpleDateFormat("MM-dd-yyyyhh:mm:ssaa");
            String IN = datein + timein;
            String OUT = dateout + timeout;
            Date AdmissioDate = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDate = times.parse(OUT.replaceAll("\\s", ""));//PARAM
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
        CF5Method gm = new CF5Method();
        String result = "";
        List<String> ProcList = Arrays.asList(rvs.split(","));
        for (int m = 0; m < ProcList.size(); m++) {
            String rvs_code = ProcList.get(m);
            DRGWSResult finalResult = gm.GetICD9cm(datasouce, rvs_code);
            if (String.valueOf(finalResult.isSuccess()).equals(true)) {
                result = finalResult.getResult();
            }
        }
        return result;
    }

    public String SDxSecondary(DataSource datasouce, String icd10) {
        CF5Method gm = new CF5Method();
        String result = "";
        List<String> ProcList = Arrays.asList(icd10.split(","));
        for (int m = 0; m < ProcList.size(); m++) {
            String rvs_code = ProcList.get(m);
            DRGWSResult finalResult = gm.GetICD9cm(datasouce, rvs_code);
            if (String.valueOf(finalResult.isSuccess()).equals(true)) {
                result = finalResult.getResult();
            }
        }
        return result;
    }

    //DTD File in string format
//    public String DTDFilePath() {
//        String DTDFile = "<!ELEMENT CF5 (DRGCLAIM)>\n"
//                + "<!ATTLIST CF5 \n"
//                + "     pHospitalCode       CDATA #REQUIRED\n"
//                + "   >\n"
//                + "<!ELEMENT DRGCLAIM (SECONDARYDIAGS,PROCEDURES)>\n "
//                + "<!ATTLIST DRGCLAIM \n"
//                + "     PrimaryCode         CDATA #REQUIRED\n"
//                + "     NewBornAdmWeight    CDATA #REQUIRED\n"
//                + "     Remarks             CDATA #REQUIRED\n"
//                + "     ClaimNumber         CDATA #REQUIRED\n"
//                + ">\n"
//                + "<!ELEMENT SECONDARYDIAGS (SECONDARYDIAG)+>\n"
//                + "<!ELEMENT SECONDARYDIAG EMPTY>\n"
//                + "<!ATTLIST SECONDARYDIAG\n"
//                + "     SecondaryCode       CDATA #REQUIRED\n"
//                + "     Remarks             CDATA #REQUIRED\n"
//                + "   >\n"
//                + "<!ELEMENT PROCEDURES (PROCEDURE)+>\n"
//                + "<!ELEMENT PROCEDURE EMPTY>\n"
//                + "<!ATTLIST PROCEDURE\n"
//                + "  RvsCode 		CDATA #REQUIRED\n"
//                + "  Laterality         CDATA #REQUIRED\n"
//                + "  Ext1 		CDATA #REQUIRED\n"
//                + "  Ext2 		CDATA #REQUIRED\n"
//                + "  Remarks 		CDATA #REQUIRED\n"
//                + "   >";
//        return DTDFile;
//    }
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
        DateFormat sdf = this.SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateString);
            result = true;
        } catch (ParseException e) {
        }
        return result;
    }

}
