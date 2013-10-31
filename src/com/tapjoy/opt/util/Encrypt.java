package com.tapjoy.opt.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.util.zip.CRC32;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class Encrypt {

	private final static String encryptionForm = "DES/ECB/PKCS5Padding";
	private final static String defaultKey = "4835793562AA23F3";
	private static Logger logger = Logger.getLogger(Encrypt.class);
	
	public static class EncryptException extends Exception {
		static final long serialVersionUID = 0L;
		
		EncryptException(String message) {
			super(message);
		}
	}
	
	private SecretKey secretKey = null;

	private static Encrypt defaultInstance = null;

	private Cipher encryptCipher;
	private Cipher decryptCipher;
	
	private Encrypt() throws EncryptException {
			init(defaultKey);
		}

	public Encrypt(String key) throws EncryptException {
		init(key);
	}

	public static Encrypt getDefault() throws EncryptException {
		if (defaultInstance == null) {
			synchronized (Encrypt.class) {
				if (defaultInstance == null) {
					defaultInstance = new Encrypt();
				}
		}
	}

		return defaultInstance;
	}

	public void init(String key) throws EncryptException {
		init(key.getBytes());		
	}

	private void init(byte[] thekey) throws EncryptException {
		try {
		KeySpec ks = new DESKeySpec(thekey);
		SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
		secretKey = kf.generateSecret(ks);
	
			encryptCipher = Cipher.getInstance(encryptionForm);
			encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			
			decryptCipher = Cipher.getInstance(encryptionForm);
			decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
		}
		catch(Exception ex) {
			logger.fatal(ex);
			throw new EncryptException(ex.getMessage());
		}
	}

	public byte[] encrypt(byte[] input) throws EncryptException {
		byte[] encrypted = null;

		try {
		if (secretKey != null) {
				encrypted = encryptCipher.doFinal(input);
			}
		}
		catch(Exception ex) {
			logger.fatal(ex);
			throw new EncryptException(ex.getMessage());
		}

		return encrypted;
	}

	public byte[] decrypt(byte[] input) throws EncryptException {
		byte[] decrypted = null;

		try {
		if (secretKey != null) {
				decrypted = decryptCipher.doFinal(input);
			}
		}
		catch(Exception ex) {
			logger.fatal(ex);
			throw new EncryptException(ex.getMessage());
		}

		return decrypted;
	}

	public static byte[] hexToBytes(String str) 
		throws DecoderException {
		
		return Hex.decodeHex(str.toCharArray());
	}

	public static String bytesToHex(byte[] data) {
		
		return new String(Hex.encodeHex(data)).toUpperCase();
	}

	public static String encodeBase64(byte[] binaryData) {
		byte[] encoded = Base64.encodeBase64(binaryData);
		
		String encodedStr = new String(encoded);
		
		// replace + to (
		// replace / to )
		// replace = to #		 
		return encodedStr.replace('+', '(').replace('/', ')').replace('=', '$');
	}
	
	public static byte[] decodeBase64(String encoded) {
	
		// replace ( to +
		// replace ) to /
		// replace # to =		 
		byte[] decoded = Base64.decodeBase64(encoded.replace('(', '+').replace(')', '/').replace('$', '=').getBytes());
		
		return decoded;
	}
	
	/*
	 * calculate CRC32 based on incoming String
	 */	
	public static long CRC32(String str) {
		long CRC32 = 0;

		if (str != null) {
			byte[] bytes = str.getBytes();
			CRC32 crc = new CRC32();
			crc.update(bytes);

			CRC32 = crc.getValue();			
		}
		
		return CRC32;
	}

	public static void main(String[] args) {
		try {			              
			logger.debug("encrypted string is::" + args[0]);

			byte[] decoded = Encrypt.decodeBase64(args[0]);				
			byte[] decrypted = Encrypt.getDefault().decrypt(decoded);
			
			logger.debug("decrypted string is::" + new String(decrypted));
		}	
		catch(Exception ex) {
			logger.fatal(ex);
		}

	}
	
	/* input: @str - string to sign
	 * output: hexadecimal representation of MD5 signature
	 */
	public static String signMD5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
	  	byte[] hash = null;
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		digest.update(str.getBytes("UTF-8"));
		hash = digest.digest();

		/* converting byte array to hexadecimal string representation */
		return bytesToHex(hash);
	}

	/* input: @str - string to sign
	 *        @key - signature key
	 * output: concatenated string @str + @key
	 */
	public static String getKeyedMD5StringToSign(String str, String key) {
		return str + key;
//		return new String((str + key).getBytes("UTF-8"), "UTF-8");
	}
	
	public static boolean validMD5Signature(String stringToSign, String signature) 
	throws UnsupportedEncodingException, NoSuchAlgorithmException {
		if(stringToSign == null || signature == null) { return false; }
		return signature.equalsIgnoreCase(signMD5(stringToSign));
	}
}
