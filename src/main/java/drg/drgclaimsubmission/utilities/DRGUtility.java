/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities;

import drg.drgclaimsubmission.structures.DRGWSResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@ApplicationScoped
@Singleton
public class DRGUtility {

    private java.sql.Date date;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
    SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    private final Utility utility = new Utility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGUtility() {
    }

    String[] pkgcode = {"", "", "", ""};

    String[] OPA = {"5051", "5059"};
    String[] OPB = {"3350", "3351", "3352", "336", "3751"};
    String[] OPD = {"4100", "4101", "4102", "4103", "4104", "4105", "4106", "4107", "4108", "4109"};

    //AX 99BX Radiotherapy
    String[] BX99 = {"Z510"};

    //AX 99CX Chemotherapy
    String[] CX99 = {"Z511"};

    //AX 99PBX Blood transfusion
    String[] PBX99 = {"9903", "9905", "9904"};

    //AX 99PCX Cont mech ventilation 96 consecutive hours or more
    String[] PCX99 = {"9672"};

    //AX 2PDX Cataract Frag/Asp
    String[] PDX2 = {"1342"};

    //AX 99PDX Tracheostomy
    String[] PDX99 = {"311", "3121", "3129"};

    //AX 99PEX Radiotherapeutic procedure
    String[] PEX99 = {"9223", "9224", "9225", "9227", "9228", "9229", "9230", "9231", "9232"};

    //AX 99PFX Parenteral cancer chemotherapy
    String[] PFX99 = {"9925"};
    String[] PBX12 = {"5731", "5732", "5821", "5822", "5823", "5824", "5831", "6011",
        "6013", "6111", "6211", "6301", "8773", "8776",
        "8791", "8793", "8801", "8879", "8895", "8897", "9202", "9214", "9218", "240"};
    //AX 2BX Parenteral cancer chemotherapy

    //AX 3BX Upper airway obstruction diagnoses
    String[] BX3 = {"C000", "C069", "C001", "C002", "C07", "C003", "C080", "C004", "C005", "C081",
        "C006", "C008", "C088", "C009", "C01", "C020", "C089", "C021", "C090", "C091", "C0210",
        "C0211", "C098", "C0219", "C099", "C022", "C100", "C023", "C102", "C024",
        "C103", "C028", "C104", "C029", "C108", "C030", "C109", "C031", "C110", "C039", "C040", "C111",
        "C041", "C112", "C048", "C113", "C049", "C118", "C050", "C051", "C119", "C052", "C12", "C058", "C130",
        "C131", "C059", "C060", "C132", "C061", "C138", "C0610", "C139", "C0611", "C140", "C0612", "C0613", "C141", "C0614",
        "C0615", "C142", "C0619", "C148", "C062", "C300", "C0620",
        "C0621", "C301", "C0629", "C310", "C068", "C311", "C312", "C313", "C318", "C319", "C320", "C321", "C322",
        "C323", "C328", "C329", "C390", "C462", "C760", "D000", "D020", "D370", "D380", "J042", "J051", "J390", "K112", "K113",
        "K122", "S015", "S0150", "S0151", "S0157", "S024", "S0240", "S02400", "S02401", "S02402", "S02407", "S0241", "S02410",
        "S02411", "S02412", "S02417", "S025", "S0250", "S02500", "S02501", "S02502", "S02503", "S02504", "S02507", "S02509", "S0251",
        "S02510", "S02511", "S02512", "S02513", "S02514", "S02517", "S02519", "S026", "S0260", "S02600", "S02601", "S02602", "S02603",
        "S02604", "S02605", "S02606", "S02607", "S02609", "S0261", "S02610", "S02611", "S02612", "S02613", "S02614", "S02615", "S02616",
        "S02617", "S02619", "T171", "T172", "T280", "T285"};

    //Procedures for upper airway obstruction
    String[] PDX3 = {"0403", "0404", "0405", "0406", "0407", "0412", "0419", "0441", "0442", "0449", "0471", "0472", "0473",
        "0474", "0475", "0476", "0492", "0493", "0499", "0521", "0522", "066", "0943", "1652", "1665", "1666", "1698", "2106", "2172", "2183",
        "244", "245", "252", "253", "254", "270", "2732", "2757", "290", "292", "2931", "2932", "2933", "2939", "294", "2951", "2952", "2953", "2954",
        "2959", "2992", "2999", "3001", "3009", "3021", "3022", "313", "3145", "315", "3161", "3162", "3163", "3164", "3169", "3171", "3172", "3174", "3175", "3179", "3192", "3199", "3422",
        "3800", "3802", "3812", "3832", "3842", "3862", "3882", "3998", "3999", "4011", "4019", "4021", "4023", "4029", "403", "4040", "4041", "4042", "4050", "4059", "409", "4201",
        "4209", "4210", "4211", "4212", "4219", "4221", "4225", "4231", "4232", "4239", "4240", "4241", "4242", "4251", "4252", "4253", "4254", "4255", "4256", "4258", "4259",
        "4261", "4262", "4263", "4264", "4265", "4266", "4268", "4269", "427", "4282", "4283", "4284", "4286", "4287", "4289", "4319", "7601", "7609", "7611", "7619", "762", "7631",
        "7639", "7641", "7642", "7643", "7644", "7645", "7646", "765", "7661", "7662", "7663", "7664", "7665", "7666", "7667", "7668", "7669", "7670", "7672", "7674", "7676", "7677", "7679",
        "7691", "7692", "7694", "7697", "7699", "7719", "7730", "7740", "7749", "7769", "7779", "7789", "7799", "7929", "7939", "7969", "8302", "8339", "8349", "8622", "864", "8663", "8666",
        "8667", "8669", "8670", "8671", "8672", "8674", "8675", "8681", "8682", "8684", "8689", "8691", "8693", "9227", "4282", "4283", "4284", "4286", "4287", "4289", "4319", "7601", "7609",
        "7611", "7619", "762", "7631", "7639", "7641", "7642", "7643", "7644", "7645", "7646", "765", "7661", "7662", "7663", "7664", "7665", "7666", "7667", "7668", "7669", "7670", "7672", "7674",
        "7676", "7677", "7679", "7691", "7692", "7694", "7697", "7699", "7719", "7730", "7740", "7749", "7769", "7779", "7789", "7799", "7929", "7939", "7969", "8302", "8339", "8349", "8622", "864",
        "8663", "8666", "8667", "8669", "8670", "8671", "8672", "8674", "8675", "8681", "8682", "8684", "8689", "8691", "8693", "9227"};

    String[] PEX3 = {"2755", "2756", "2757", "2759", "294", "7641", "7643", "7644", "7646", "8670", "8671", "8672", "8673", "8674", "8675"};
    String[] PCX3 = {"2122", "2129", "2211", "2411", "2412", "2501", "3143", "3144", "3148", "4223", "2611", "2723", "2912", "2919", "3141", "3142", "4224",
        "8703", "8891", "8897"};
    String[] PBX3 = {"2301", "2309", "2311", "2319", "232", "233", "2341", "2342", "2343", "2349", "235", "236", "2370", "2371", "2372", "2373", "247"};
    String[] PCX4 = {"3322", "3323", "3324", "3326", "3372", "8703", "8741", "8801", "9214"};
    String[] BX4 = {"Z430", "Z930"};
    String[] PBX5 = {"3721", "3722", "3723", "3726", "8852", "8853", "8854", "8855", "8856", "8857", "8858"};
    String[] PCX5 = {"0066", "3601", "3602", "3605"};
    String[] PDX5 = {"3606", "3607"};
    String[] PEX5 = {"3553", "3562", "3610", "3611", "3612", "3613", "3614", "3615", "3616", "3617", "3619"};
    String[] PFX5 = {"0041", "0042", "0043", "3605"};
    String[] PGX5 = {"0040", "3601", "3602"};
    String[] PJX5 = {"0055", "0063", "0061", "0065", "3990"};
    String[] PHX5 = {"9910", "9920"};
    String[] PGX14 = {"6621", "6622", "6629", "6631", "6632", "6639"};
    String[] EX14 = {"Z390"};
    String[] CX6 = {"C181", "K352", "K383"};
    String[] PFX8 = {"8721", "8838", "8848", "8893", "8894", "8895", "9202", "9214", "9218"};
    String[] PCX8 = {"0070", "0071", "0072", "0073", "0085", "0086", "0087", "8151", "8152", "8153"};
    String[] PDX8 = {"0080", "0081", "0082", "0083", "0084", "8154", "8155"};
    String[] PEX8 = {"8156"};
    String[] PBX9 = {"86221>1"};
    String[] PCX9 = {"8511", "8591", "8735", "8736", "8737", "8741", "8834", "8873", "8892", "9202", "9214", "9216", "9218"};
    String[] PBX10 = {"3995"};
    String[] PBX11 = {"9851"};
    String[] PBX13 = {"7021", "7022", "8773", "8777", "8801", "8879", "8895", "8897", "9202", "9214", "9218"};
    String[] KX14 = {"O083"};
    String[] PFX14 = {"6831", "6839", "684", "7491"};
    String[] DX14 = {"O678", "O679", "O702", "O703", "O721", "O744", "O745", "O746", "O747", "O748", "O749", "O754"};
    String[] PJX14 = {"4939", "4944", "4945", "6711", "6712", "6719", "6731", "6732", "6739", "6902", "6909", "6952", "6995",
        "7013", "7014", "7024", "7029", "7031", "7033", "7050", "7053", "7071", "7076", "7091",
        "7101", "7109", "7111", "7119", "7122", "7123", "7124", "7129", "713", "7171", "7179", "718", "719", "7599"};

    String[] CX14 = {"F000", "F0000", "F0001", "F0002", "F0003", "F0004", "F0104", "F011", "F0110", "F0111", "F0112", "F0113", "F0114", "F012",
        "F0120", "F0121", "F0122", "F0123", "F0124", "F013", "F0130", "F0131", "F0132", "F0133", "F0134", "F018", "F0180", "F0181", "F0182", "F0183",
        "F0184", "F019", "F0190", "F0191", "F0192", "F002", "F0020", "F0021", "F0022", "F0023", "F0024", "F009", "F0090", "F0091", "F0092", "F0093", "F0094",
        "F010", "F0100", "F0101", "F0102", "F0103", "F0193", "F0194", "F020", "F033", "F034", "F04", "F050", "F051", "F058", "F059", "F0200", "F0201", "F0202",
        "F0203", "F0204", "F060", "F061", "F062", "F063", "F0630", "F0631", "F0632", "F0633", "F018", "F0180", "F0181", "F0182", "F0183", "F0184", "F019", "F0190",
        "F0191", "F0192", "F1056", "F106", "F107", "F1070", "F1071", "F1072", "F1073", "F1074", "F1075", "F108", "F109", "F110", "F1100", "F1101", "F1102", "F1103",
        "F1104", "F1105", "F1106", "F1107", "F111", "F112", "F1120", "F1121", "F1122", "F1123", "F1124", "F1125", "F1126", "F113", "F1130", "F1131", "F114", "F1140",
        "F1141", "F115", "F1150", "F1151", "F1152", "F1153", "F1154", "F1155", "F1156", "F116", "F117", "F1170", "F1171", "F1172", "F1173", "F1174", "F1175", "F118",
        "F119", "F123", "F1230", "F1231", "F124", "F1240", "F1241", "F125", "F1250", "F1251", "F1252", "F1253", "F1254", "F1255", "F1256", "F126", "F127", "F1270", "F1271",
        "F1272", "F1273", "F1274", "F1275", "F128", "F129", "F130", "F1300", "F1301", "F1302", "F1303", "F1304", "F1305", "F1306", "F1307", "F131", "F132", "F1320", "F1321",
        "F1322", "F1323", "F1324", "F1325", "F1326", "F133", "F1330", "F1331", "F134", "F1340", "F1341", "F135", "F1350", "F1351", "F1352", "F1353", "F1354", "F1355", "F1356",
        "F136", "F137", "F1370", "F1371", "F1372", "F1373", "F1374", "F1375", "F138", "F139", "F140", "F1400", "F1401", "F1402", "F1403", "F1404", "F1405", "F1406", "F1407", "F141",
        "F142", "F1420", "F1421", "F1422", "F1423", "F1424", "F1425", "F1426", "F143", "F1430", "F1431", "F144", "F1440", "F1441", "F145", "F1450", "F1451", "F1452", "F1453", "F1454",
        "F1455", "F1456", "F146", "F147", "F1470", "F1471", "F1472", "F1473", "F1474", "F1475", "F148", "F149", "F150", "F1500", "F1501", "F1502", "F1503", "F1504", "F1505", "F1506",
        "F1507", "F151", "F152", "F1520", "F1521", "F1522", "F1523", "F1524", "F1525", "F1526", "F153", "F1530", "F1531", "F154", "F1540", "F1541", "F155", "F1550", "F1551", "F1552", "F1553",
        "F1554", "F1555", "F1556", "F156", "F157", "F1570", "F1571", "F1572", "F1573", "F1574", "F1575", "F158", "F159", "F160", "F1600", "F1601", "F1602", "F1603", "F1604", "F1605", "F1606", "F1607",
        "F161", "F162", "F1620", "F1621", "F1622", "F1623", "F1624", "F1625", "F1626", "F163", "F1630", "F1631", "F164", "F1640", "F1641", "F165", "F1650", "F1651", "F1652", "F1653", "F1654", "F1655",
        "F1656", "F166", "F167", "F1670", "F1671", "F1672", "F1673", "F1674", "F1675", "F168", "F169", "F172", "F1720", "F1721", "F1722", "F1723", "F1724", "F1725", "F1726", "F173", "F1730",
        "F1731", "F174", "F1740", "F1741", "F1807", "F181", "F182", "F1820", "F1821", "F1822", "F1823", "F1824", "F1825", "F1826", "F183", "F1830", "F1831", "F184", "F1840", "F1841", "F185", "F1850", "F1851",
        "F1852", "F1853", "F1854", "F1855", "F1856", "F186", "F187", "F1870", "F1871", "F1872", "F1873", "F1874", "F1875", "F188", "F189", "F190", "F1900", "F1901", "F1902", "F1903", "F1904", "F1905", "F1906", "F1907", "F191", "F192", "F1920", "F1921",
        "F1922", "F1923", "F1924", "F1925", "F1926", "F193", "F1930", "F1931", "F194", "F1940", "F1941", "F195", "F1950", "F1951", "F1952", "F1953", "F1954", "F1955", "F1956", "F196", "F197", "F1970", "F1971", "F1972", "F1973", "F1974", "F1975",
        "F198", "F199", "F200", "F2000", "F2001", "F2002", "F2003", "F2004", "F2005", "F2008", "F2009", "F201", "F2010", "F2011", "F2012", "F2013", "F2014", "F2015", "F2018", "F2019", "F202", "F2020", "F2021", "F2022", "F2023", "F2024", "F2025",
        "F2028", "F2029", "F203", "F2030", "F205", "F2050", "F2051", "F2052", "F2053", "F2054", "F2055", "F2058", "F2059", "F318", "F319", "F322", "F206", "F333", "F2060", "F3330", "F2061", "F2062", "F2063", "F2064", "F3331", "F2065",
        "F2068", "F2069", "F338", "F339", "F208", "F500", "F2080", "F501", "F2081", "F502", "F2082", "F503", "F2083", "F504", "F2084", "F2085", "F508", "F2088", "F2089", "F840", "F841",
        "F209", "F842", "F2090", "F843", "F2091", "F844", "F2092", "F845", "F2093", "F848", "F2094", "F849", "F2095", "F2098", "F2099", "F230", "O121", "F231", "O140", "F232", "O141", "F233",
        "O142", "F238", "O149", "F239", "O150", "F250", "O159", "F251", "O212", "F252", "O218", "F258", "O219", "F259", "O223", "F29", "O225", "F310", "O228", "O240", "O343", "O241",
        "O365", "O242", "O40", "O243", "O410", "O244", "O421", "O2440", "O2441", "O440", "O2449", "O441", "O249", "O450", "O290", "O460", "O291", "O670", "O292", "O740", "O300", "O741",
        "O301", "O302", "O742", "O308", "O309", "O743", "O311", "O312", "O751", "O756", "O318", "O325", "O993"};

    String[] CX15 = {"A020", "A038", "A022", "A039", "A030", "A040", "A031", "A041", "A032", "A042", "A033", "A043", "A044", "B956", "A048", "B958", "A080",
        "A083", "B964", "A084", "A09", "B978", "D180", "D1800", "A099", "D1801", "D1802", "A419", "D1803", "A500", "D1804", "A501", "D1807", "A502", "D1808",
        "A509", "D1809", "A543", "D181", "A740", "H13.1*", "A90", "A91", "D551", "A911", "A919", "B019", "D589", "B09", "D598", "B169", "D62", "D638", "B24", "D695",
        "D696", "B349", "D728", "B354", "D751", "B356", "D759", "B359", "D821", "E031", "B3700", "E038", "B3701", "E039", "B3702", "E161", "B3703", "E162", "B3704",
        "E250", "B3705", "B3706", "E259", "B3708", "E46", "B3709", "E730", "B372", "E731", "B373", "N77.1*", "E739", "B374", "E785", "B379", "E806", "B500", "E835", "E841", "B508", "E86",
        "B509", "E871", "B510", "E872", "B518", "E875", "B519", "E876", "B520", "E877", "B528", "E878", "B529", "B530", "E880", "B531", "H010", "H045", "B955", "H050", "H100", "H103", "H108",
        "H113", "H131", "H309", "H351", "H601", "H603", "H660", "I500", "I517", "I519", "I809", "I959", "J00", "J029", "J069", "J159", "J160", "J180", "J181", "J188", "J189", "J209", "J219", "J348", "J398",
        "J459", "J690", "J981", "J988", "J989", "K006", "K0060", "K0061", "K0062", "K0063", "K0064", "K0065", "K0068", "K0069", "K070", "K090", "K121", "K123", "K219", "K296", "K400", "K401", "K402",
        "K404", "K409", "K429", "K522", "K529", "K600", "K904", "K922", "L00", "L010", "L020", "L021", "L022", "L023", "L024", "L028", "L029", "L030", "L031", "L032", "L033", "L038", "L039", "L059",
        "L080", "L088", "L089", "L208", "L209", "L210", "L211", "L218", "L219", "L22", "L239", "L249", "L259", "L303", "L304", "L403", "L509", "L510", "L511", "L739", "L89", "L890", "L891", "L892", "L893",
        "L899", "L928", "L929", "M726", "M7260", "M7261", "M7262", "M7263", "M7264", "M7265", "M7266", "M7267", "M7268", "M7269", "N179", "N19", "N390", "N433", "N47", "N61", "P002", "P011",
        "P012", "P013", "P027", "P100", "P101", "P102", "P103", "P104", "P108", "P109", "P110", "P112", "P113", "P114", "P122", "P123", "P128", "P129", "P130", "P131", "P132", "P133", "P134", "P138",
        "P139", "P140", "P141", "P142", "P143", "P150", "P151", "P153", "P154", "P155", "P209", "P211", "P219", "P221", "P228", "P229", "P230", "P231", "P233", "P236", "P238", "P239", "P240", "P241",
        "P242", "P243", "P248", "P249", "P258", "P280", "P281", "P282", "P283", "P284", "P290", "P291", "P292", "P294", "P299", "P350", "P351", "P352", "P353", "P354", "P355", "P356", "P357", "P358",
        "P359", "P360", "P361", "P362", "P363", "P364", "P365", "P366", "P367", "P368", "P369", "P370", "P371", "P372", "P373", "P374", "P375", "P378", "P379", "P381", "P382", "P383", "P384", "P385",
        "P386", "P387", "P388", "P389", "P390", "P391", "P392", "P393", "P394", "P504", "P509", "P510", "P511", "P512", "P513", "P514", "P515", "P516", "P517", "P518", "P519", "P520", "P521", "P523",
        "P524", "P525", "P526", "P528", "P529", "P53", "P540", "P541", "P543", "P544", "P549", "P550", "P551", "P558", "P559", "P560", "P569", "P570", "P578", "P579", "P581", "P582", "P584", "P585", "P591",
        "P592", "P610", "P611", "P612", "P613", "P614", "P616", "P618", "P619", "P702", "P703", "P704", "P708", "P709", "P711", "P712", "P713", "P714", "P718", "P719", "P720", "P721", "P722", "P728", "P729", "P740", "P741",
        "P742", "P743", "P744", "P745", "P748", "P749", "P75", "P760", "P761", "P768", "P769", "P781", "P782", "P783", "P800", "P830", "P832", "P90", "P911", "P912", "P913", "P922", "P923", "P940", "P942", "P948", "P95", "P97",
        "Q000", "Q001", "Q002", "Q003", "Q004", "Q010", "Q011", "Q012", "Q018", "Q019", "Q020", "Q030", "Q031", "Q038", "Q039", "Q040", "Q041", "Q042", "Q043", "Q044", "Q045", "Q046", "Q048", "Q049", "Q050", "Q051", "Q052",
        "Q053", "Q054", "Q055", "Q056", "Q057", "Q058", "Q059", "Q060", "Q061", "Q062", "Q063", "Q064", "Q068", "Q069", "Q070", "Q078", "Q079", "Q100", "Q107", "Q111", "Q112", "Q113", "Q120", "Q133", "Q150", "Q219", "Q246",
        "Q248", "Q249", "Q250", "Q251", "Q300", "Q301", "Q302", "Q303", "Q308", "Q309", "Q310", "Q311", "Q312", "Q313", "Q314", "Q315", "Q318", "Q319", "Q320", "Q322", "Q351", "Q353", "Q355", "Q359", "Q360", "Q361",
        "Q369", "Q370", "Q371", "Q373", "Q374", "Q375", "Q378", "Q379", "Q380", "Q382", "Q383", "Q3830", "Q3831", "Q3832", "Q3834", "Q3838", "Q3839", "Q390", "Q400", "Q4100", "Q4108", "Q4109", "Q411", "Q412",
        "Q418", "Q419", "Q420", "Q421", "Q422", "Q423", "Q428", "Q429", "Q431", "Q433", "Q434", "Q436", "Q437", "Q438", "Q439", "Q451", "Q540", "Q541", "Q542", "Q543", "Q544", "Q548", "Q549", "Q560", "Q561", "Q562",
        "Q563", "Q620", "Q640", "Q650", "Q651", "Q658", "Q676", "Q677", "Q680", "Q681", "Q790", "Q792", "Q793", "Q794", "Q795", "Q798", "Q802", "Q803", "Q804", "Q808", "Q809", "Q860", "Q861", "Q870", "Q894",
        "Q897", "Q913", "Q917", "Q999", "R001", "R010", "R011", "R060", "R068", "R162", "R220", "R221", "R230", "R31", "R34", "R508", "R571", "R572", "R578", "R579", "R599", "R599", "R628", "T803", "T813", "R633",
        "T814", "R634", "T850", "R739", "R75", "T857", "S000", "S143", "S4200", "Z201", "T211", "T212", "T360", "Z205", "T740", "Z384", "T741", "Z386", "T781", "Z387", "T793", "Z433", "T801", "Z991"};

    String[] PBX16 = {"0096"};
    String[] BX28 = {"I600", "I601", "I602", "I603", "I604", "I605", "I606", "I607", "I608", "I609",
        "I610", "I611", "I612", "I613", "I614", "I615", "I616", "I618", "I619", "I620", "I621", "I629", "I630",
        "I631", "I632", "I633", "I634", "I635", "I636", "I638", "I639", "I64", "I650", "I651", "I652", "I653", "I658",
        "I659", "I660", "I661", "I662", "I663", "I664", "I668", "I669", "I679"};
    String[] CX28 = {"I210", "I211", "I212", "I213", "I214", "I219", "I220", "I221", "I228", "I229"};
    String[] PBX28 = {"9671"};
    String[] DX28 = {"K920", "K922"};
    String[] EX28 = {"C180", "C181", "C182", "C183", "C184", "C185", "C186", "C187", "C188", "C189",
        "C19", "C20", "C221", "C321", "C340", "C341", "C342", "C343", "C348", "C349",
        "C500", "C501", "C502", "C503", "C504", "C505", "C506", "C508", "C509", "C530",
        "C531", "C538", "C539", "C56", "C61", "C679", "C810", "C811", "C812", "C813",
        "C814", "C817", "C819", "C820", "C821", "C822", "C823", "C824", "C825", "C826",
        "C827", "C829", "C830", "C831", "C832", "C833", "C834", "C835", "C836", "C837",
        "C838", "C8380", "C8381", "C8388", "C839", "C840", "C841", "C842", "C843", "C844",
        "C8440", "C8441", "C8442", "C8443", "C8448", "C8449", "C845", "C846", "C847", "C848",
        "C849", "C850", "C851", "C852", "C857", "C859", "C860", "C861", "C862", "C863",
        "C864", "C865", "C866", "C900", "C910", "D392"};
    String[] PBX24 = {"8628>1"};
    String[] dclist = {"0019", "0029", "0049", "0107", "0110", "0203", "0210", "0213", "0214", "0215", "0311", "0318", "0319", "0450", "0503", "0507", "0510", "0525", "0528", "0535",
        "0557", "0615", "0620", "0622", "0625", "0626", "0627", "0629", "0634", "0712", "0713", "0804", "0826", "0827", "0832", "0833", "0834", "0835", "0907", "0912",
        "0913", "1005", "1009", "1011", "1012", "1113", "1114", "1115", "1152", "1161", "1209", "1210", "1253", "1255", "1312", "1313", "1314", "1318", "1319", "1361",
        "1363", "1512", "1604", "1605", "1705", "1706", "1758", "1766", "1808", "1809", "1863", "1901", "1903", "1950", "1951", "1956", "1957", "1961", "2054", "2105",
        "2106", "2203", "2204", "2303", "2305", "2308", "2311", "2312", "2355", "2414", "2416", "2501", "2502", "2506", "2508", "2509", "2650", "2651", "2652", "2653",
        "2654", "2801", "2802", "2803", "2804", "2805", "2806", "2807", "2808", "2809", "2810", "2811", "2812", "2813", "2814", "2815", "2816", "2817", "2818", "2819",
        "2820", "2821", "2822", "2823", "2824", "2825", "2826", "2827", "2828", "2829", "2830", "2831", "2832", "2833", "2834", "2835", "2836", "2837", "2850", "2851",
        "2852", "2853", "2854", "2855", "2856", "2857", "2858", "2859", "2860", "2861", "2862", "2863", "2864", "2865", "2866", "2867", "2868", "2869"};

    public boolean isValidDCList(String dcs) {
        boolean result = false;
        result = Arrays.asList(dclist).contains(dcs);
        return result;
    }

    public boolean isValid24PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX24).contains(icd10);
        return result;
    }

    public boolean isValid28EX(String icd10) {
        boolean result = false;
        result = Arrays.asList(EX28).contains(icd10);
        return result;
    }

    public boolean isValid28DX(String icd10) {
        boolean result = false;
        result = Arrays.asList(DX28).contains(icd10);
        return result;
    }

    public boolean isValid28PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX28).contains(icd10);
        return result;
    }

    public boolean isValid28CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX28).contains(icd10);
        return result;
    }

    public boolean isValid28BX(String icd10) {
        boolean result = false;
        result = Arrays.asList(BX28).contains(icd10);
        return result;
    }

    public boolean isValid16PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX16).contains(icd10);
        return result;
    }

    public boolean isValid15CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX15).contains(icd10);
        return result;
    }

    public boolean isValid14CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX14).contains(icd10);
        return result;
    }

    public boolean isValid14PJX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PJX14).contains(icd10);
        return result;
    }

    public boolean isValid14DX(String icd10) {
        boolean result = false;
        result = Arrays.asList(DX14).contains(icd10);
        return result;
    }

    public boolean isValid14PFX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PFX14).contains(icd10);
        return result;
    }

    public boolean isValid14KX(String icd10) {
        boolean result = false;
        result = Arrays.asList(KX14).contains(icd10);
        return result;
    }

    public boolean isValid13PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX13).contains(icd10);
        return result;
    }

    public boolean isValid11PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX11).contains(icd10);
        return result;
    }

    public boolean isValid10PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX10).contains(icd10);
        return result;
    }

    public boolean isValid9PCX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PCX9).contains(icd10);
        return result;
    }

    public boolean isValid9PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX9).contains(icd10);
        return result;
    }

    public boolean isValid8PEX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PEX8).contains(icd10);
        return result;
    }

    public boolean isValid8PDX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PDX8).contains(icd10);
        return result;
    }

    public boolean isValid8PCX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PCX8).contains(icd10);
        return result;
    }

    public boolean isValid8PFX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PFX8).contains(icd10);
        return result;
    }

    public boolean isValid6CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX6).contains(icd10);
        return result;
    }

    public boolean isValid14EX(String icd10) {
        boolean result = false;
        result = Arrays.asList(EX14).contains(icd10);
        return result;
    }

    public boolean isValid14PGX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PGX14).contains(icd10);
        return result;
    }

    public boolean isValid5PHX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PHX5).contains(icd10);
        return result;
    }

    public boolean isValid5PJX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PJX5).contains(icd10);
        return result;
    }

    public boolean isValid5PGX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PGX5).contains(icd10);
        return result;
    }

    public boolean isValid5PFX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PFX5).contains(icd10);
        return result;
    }

    public boolean isValid5PEX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PEX5).contains(icd10);
        return result;
    }

    public boolean isValid5PDX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PDX5).contains(icd10);
        return result;
    }

    public boolean isValid5PCX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PCX5).contains(icd10);
        return result;
    }

    public boolean isValid5PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX5).contains(icd10);
        return result;
    }

    public boolean isValid4BX(String icd10) {
        boolean result = false;
        result = Arrays.asList(BX4).contains(icd10);
        return result;
    }

    public boolean isValid4PCX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PCX4).contains(icd10);
        return result;
    }

    public boolean isValid3PBX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PBX3).contains(icd10);
        return result;
    }

    public boolean isValid3PDX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PDX3).contains(icd10);
        return result;
    }

    public boolean isValid3PCX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PCX3).contains(icd10);
        return result;
    }

    public boolean isValid3PEX(String icd10) {
        boolean result = false;
        result = Arrays.asList(PEX3).contains(icd10);
        return result;
    }

    public boolean isValidOPA(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPA).contains(icd10);
        return result;

    }

    public boolean isValid3BX(String icd10) {
        boolean result = false;
        result = Arrays.asList(BX3).contains(icd10);
        return result;

    }

    public boolean isValid2PDX(String procs) {
        boolean result = false;
        result = Arrays.asList(PDX2).contains(procs);
        return result;

    }

    public boolean isValid12PBX(String proce) {
        boolean result = false;
        result = Arrays.asList(PBX12).contains(proce);
        return result;

    }

    public boolean isValidOPB(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPB).contains(icd10);
        return result;
    }

    public boolean isValidOPD(String icd10) {
        boolean result = false;
        result = Arrays.asList(OPD).contains(icd10);
        return result;

    }

    public boolean isValid99BX(String icd10) {
        boolean result = false;
        result = Arrays.asList(BX99).contains(icd10);
        return result;

    }

    public boolean isValid99CX(String icd10) {
        boolean result = false;
        result = Arrays.asList(CX99).contains(icd10);
        return result;

    }

    public boolean isValid99PBX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PBX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PCX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PCX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PDX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PDX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PEX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PEX99).contains(icd9cm);
        return result;
    }

    public boolean isValid99PFX(String icd9cm) {
        boolean result = false;
        result = Arrays.asList(PFX99).contains(icd9cm);
        return result;
    }

    public boolean MaxAge(String DOB, String AD) {
        boolean result = false;
        try {
            java.util.Date DateOfBirth = sdf.parse(DOB);
            java.util.Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = Math.abs(AdmissioDate.getTime() - DateOfBirth.getTime());
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = AgeY > 124;
        } catch (ParseException ex) {
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    public int ComputeTime(String DateIn, String TimeIn, String DateOut, String TimeOut) throws ParseException {
        String IN = DateIn + TimeIn;
        String OUT = DateOut + TimeOut;
        SimpleDateFormat times = utility.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
        Date AdmissionTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
        Date DischargeTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
        long Time_difference = DischargeTime.getTime() - AdmissionTime.getTime();
        long Hours_difference = (Time_difference / (1000 * 60 * 60)) % 24;
        int result = (int) Hours_difference;

        return result;
    }

    public int MinutesCompute(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
            String IN = datein + timein;
            String OUT = dateout + timeout;
            SimpleDateFormat times = utility.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            Date AdmissionDateTime = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDateTime = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDateTime.getTime() - AdmissionDateTime.getTime();
            long Minutes = (difference_In_Time / (1000 * 60)) % 60;

            result = (int) Minutes;
        } catch (ParseException ex) {
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeYear(String DOB, String AD) {
        int result = 0;
        try {
            Date DateOfBirth = sdf.parse(DOB);
            Date AdmissioDate = sdf.parse(AD);//PARAM
            long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
            long AgeY = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));
            result = (int) AgeY;
        } catch (ParseException ex) {
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int ComputeDay(String DOB, String AD) throws ParseException {
        Date DateOfBirth = sdf.parse(DOB);
        Date AdmissioDate = sdf.parse(AD);//PARAM
        long difference_In_Time = AdmissioDate.getTime() - DateOfBirth.getTime();
        long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
        int result = (int) difference_In_Days;
        return result;
    }

    public int ComputeLOS(String datein, String timein, String dateout, String timeout) {
        int result = 0;
        try {
            SimpleDateFormat times = utility.SimpleDateFormat("MM-dd-yyyyhh:mmaa");
            String IN = datein + timein;
            String OUT = dateout + timeout;
            Date AdmissioDate = times.parse(IN.replaceAll("\\s", "")); //PARAM
            Date DischargeDate = times.parse(OUT.replaceAll("\\s", ""));//PARAM
            long difference_In_Time = DischargeDate.getTime() - AdmissioDate.getTime();
            long CalLOS = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
            result = (int) CalLOS;
        } catch (ParseException ex) {
            Logger.getLogger(DRGUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String CodeConverter(DataSource datasouce, String rvs) {
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
                + "<!ELEMENT SECONDARYDIAGS (SECONDARYDIAG)+>\n"
                + "<!ELEMENT SECONDARYDIAG EMPTY>\n"
                + "<!ATTLIST SECONDARYDIAG\n"
                + "     SecondaryCode       CDATA #REQUIRED\n"
                + "     Remarks             CDATA #REQUIRED\n"
                + "   >\n"
                + "<!ELEMENT PROCEDURES (PROCEDURE)+>\n"
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

}
