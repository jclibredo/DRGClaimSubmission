/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drg.drgclaimsubmission.structures;

import lombok.Data;

/**
 *
 * @author MINOSUN
 */
@Data
public class EncryptedXML {
    
    public EncryptedXML() {
    }
    private String docMimeType;
    private String hash;
    private String key1;
    private String key2;
    private String iv;
    private String doc;
}



//text = "Hello";
//   text = kalsdjfoq3u4pufgpqohdlksldkhf


//  CF5.xml

