/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.utilities;


import drg.drgclaimsubmission.structures.EncryptedXML;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author MinoSun
 */
public class Cryptor {

    public Cryptor() {
    }

    static int CIPHER_KEY1_LEN = 16;
    static int CIPHER_KEY2_LEN = 16;
    static int CIPHER_KEY_LEN = CIPHER_KEY1_LEN + CIPHER_KEY2_LEN;
    static int CIPHER_IV_LEN = 16;

    public EncryptedXML EncryptXmlPayload(String xml, String key) {
        EncryptedXML result = new EncryptedXML();
        byte[] data;
        try {
            //data = xml.getBytes("8859_1");
            data = xml.getBytes("UTF-8");
            //data = xml.getBytes();
            if ((data.length % 16) != 0) {
                byte paddingbyte = (byte) 0x00;
                int paddinglength = 16 - (data.length % 16);
                byte[] paddingdata = new byte[paddinglength];
                for (int a = 0; a < paddinglength; a++) {
                    paddingdata[a] = paddingbyte;
                }
                byte[] paddedata = new byte[data.length + paddingdata.length];
                System.arraycopy(data, 0, paddedata, 0, data.length);
                System.arraycopy(paddingdata, 0, paddedata, data.length, paddingdata.length);
                data = paddedata;
            }

            byte[] keybyte = new byte[CIPHER_KEY_LEN];
            keybyte = KeyHash(key);
            byte[] iv = GetRandomBytes(CIPHER_IV_LEN);
            byte[] encrypteddata = EncryptUsingAES(data, keybyte, iv);
            if (encrypteddata.length > 0) {
                byte[] encryptedkey1 = new byte[0];
                byte[] encryptedkey2 = new byte[0];
                byte[] encryptediv = iv;
                String key1base64 = Base64.getEncoder().encodeToString(encryptedkey1);
                String key2base64 = Base64.getEncoder().encodeToString(encryptedkey2);
                String ivbase64 = Base64.getEncoder().encodeToString(encryptediv);
                String sha256hash = SHA256HashString(data);
                String database64 = Base64.getEncoder().encodeToString(encrypteddata);

                result.setDocMimeType("text/xml");
                result.setHash(sha256hash);
                result.setKey1(key1base64);
                result.setKey2(key2base64);
                result.setIv(ivbase64);
                result.setDoc(database64);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Cryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String DecryptXMLPayload(EncryptedXML encryptedxml, String key) {
        String result = "";
        byte[] iv = Base64.getDecoder().decode(encryptedxml.getIv());
        byte[] doc = Base64.getDecoder().decode(encryptedxml.getDoc());
        byte[] keybytes = KeyHash(key);
        byte[] decryptedstringbytes = DecryptUsingAES(doc, keybytes, iv);
        try {
            result = new String(decryptedstringbytes, "UTF-8");
            //result = new String(decryptedstringbytes, "8859_1");
            //result = new String(decryptedstringbytes);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Cryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static byte[] KeyHash(String key) {
        byte[] keybyte = new byte[CIPHER_KEY_LEN];
        for (int i = 0; i < CIPHER_KEY_LEN; i++) {
            keybyte[i] = 0;
        }
        byte[] keybytes = key.getBytes();
        byte[] keyhashbytes = SHA256HashBytes(keybytes);
        System.arraycopy(keyhashbytes, 0, keybyte, 0, Math.min(keyhashbytes.length, CIPHER_KEY_LEN));
        return keybyte;
    }

    private static byte[] SHA256HashBytes(byte[] key) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("SHA-256").digest(key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private byte[] GetRandomBytes(int count) {
        Random random = new Random();
        byte[] data = new byte[count];
        random.nextBytes(data);
        return data;
    }

    private static byte[] EncryptUsingAES(byte[] data, byte[] key, byte[] iv) {
        byte[] encrypteddoc = null;
        try {
            IvParameterSpec ips = new IvParameterSpec(iv);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sks, ips);
            encrypteddoc = cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Cryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encrypteddoc;
    }

    public static byte[] DecryptUsingAES(byte[] stringbytes, byte[] keybytes, byte[] ivbytes) {
        byte[] decryptedstringbytes = null;
        try {
            IvParameterSpec ips = new IvParameterSpec(ivbytes);
            SecretKeySpec sks = new SecretKeySpec(keybytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
            cipher.init(Cipher.DECRYPT_MODE, sks, ips);
            decryptedstringbytes = cipher.doFinal(stringbytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(Cryptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decryptedstringbytes;
    }

    private String SHA256HashString(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashInBytes = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Cryptor.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }

    }

//    public String DecryptXMLPayload(RequestBody requestbody, String key) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
