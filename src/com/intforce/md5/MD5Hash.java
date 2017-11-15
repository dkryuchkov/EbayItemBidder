/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intforce.md5;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5Hash {

    public static String md5Java(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            //converting byte array to Hexadecimal String 
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    } /* * Spring framework also provides overloaded md5 methods. You can pass input * as String or byte array and Spring can return hash or digest either as byte * array or Hex String. Here we are passing String as input and getting * MD5 hash as hex String. */ 
    public static String md5Spring(String text) {
        return DigestUtils.md5Hex(text);
    } /* * Apache commons code provides many overloaded methods to generate md5 hash. It contains * md5 method which can accept String, byte[] or InputStream and can return hash as 16 element byte * array or 32 character hex String. */ 
    public static String md5ApacheCommonsCodec(String content) {
        return DigestUtils.md5Hex(content);
    }
}
