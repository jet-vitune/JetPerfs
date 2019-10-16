package com.jetsynthesys.jetanalytics;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by siddhartho.gosh on 08-11-2017.
 */

class JetxCrypt {

    public static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + Integer.toHexString(data[i] & 0xFF);
            else
                str = str + Integer.toHexString(data[i] & 0xFF);
        }
        return str;
    }


    public static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    private static IvParameterSpec generateAESIV() {
        // build the initialization vector (randomly).
        SecureRandom random = new SecureRandom();
        byte iv[] = new byte[16];//generate random 16 byte long
        random.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        return ivspec;
    }


    public static String _decrypt(String _key, String _ivToDecrypt, String _encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(_key.getBytes("UTF-8"), "AES"), new IvParameterSpec(hexToBytes(_ivToDecrypt)));
            return new String(cipher.doFinal(android.util.Base64.decode(_encryptedData, android.util.Base64.DEFAULT)));
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////
    public static String _encrypt(String _key, String _data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec _iv = generateAESIV();
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(_key.getBytes("UTF-8"), "AES"), _iv);
            String str = new String(android.util.Base64.encode(cipher.doFinal(_data.getBytes()), android.util.Base64.DEFAULT));
            //return bytesToHex(_iv.getIV()) + new String(android.util.Base64.encode(cipher.doFinal(_data.getBytes()), android.util.Base64.DEFAULT));
            Utils.LogDebug(str);
            return bytesToHex(_iv.getIV()) + str;

        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
}




