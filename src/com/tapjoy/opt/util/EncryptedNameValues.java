package com.tapjoy.opt.util;

import java.util.HashMap;
import java.util.regex.Pattern;

public class EncryptedNameValues {
	private StringBuffer buffer = new StringBuffer();
	private static String PAIR_DELIMITOR = ";";
	private static String FIELD_DELIMITOR = ",";

	public EncryptedNameValues() {
	}

	public EncryptedNameValues append(String name, String value) {
		if (value != null) {
			if (buffer.length() != 0) {
				buffer.append(PAIR_DELIMITOR);
			}

			buffer.append(name);
			buffer.append(FIELD_DELIMITOR);

			buffer.append(value);
		}

		return this;
	}

	public EncryptedNameValues append(String name, Integer value) {
		if (value != null) {
			if (buffer.length() != 0) {
				buffer.append(PAIR_DELIMITOR);
			}

			buffer.append(name);
			buffer.append(FIELD_DELIMITOR);

			buffer.append(value.toString());
		}

		return this;
	}

	public EncryptedNameValues append(String name, Float value) {
		if (value != null) {
			if (buffer.length() != 0) {
				buffer.append(PAIR_DELIMITOR);
			}

			buffer.append(name);
			buffer.append(FIELD_DELIMITOR);

			buffer.append(value.toString());
		}

		return this;
	}

	public EncryptedNameValues append(String name, Double value) {
		if (value != null) {
			if (buffer.length() != 0) {
				buffer.append(PAIR_DELIMITOR);
			}

			buffer.append(name);
			buffer.append(FIELD_DELIMITOR);

			buffer.append(value.toString());
		}

		return this;
	}

	public String getString() {
		return buffer.toString();
	}

	public String getEncryptedEncodedStr() throws Exception {
		Encrypt encrypt = Encrypt.getDefault();
		byte[] encrypted = encrypt.encrypt(buffer.toString().getBytes("UTF-8"));
		String encodedBase64 = Encrypt.encodeBase64(encrypted);

		return encodedBase64;
	}

	public static HashMap<String, String> getDecryptedStr(
			String encryptedEncodedStr) throws Exception {
		Encrypt encrypt = Encrypt.getDefault();

		byte[] decoded = Encrypt.decodeBase64(encryptedEncodedStr);
		byte[] decrypted = encrypt.decrypt(decoded);

		String message = new String(decrypted, "UTF-8");
		Pattern p = Pattern.compile(PAIR_DELIMITOR);

		String[] namePairs = p.split(message);

		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i <= namePairs.length - 1; i++) {
			Pattern f = Pattern.compile(FIELD_DELIMITOR);

			String[] fieldPairs = f.split(namePairs[i]);
			if (fieldPairs.length == 2) {
				map.put(fieldPairs[0], fieldPairs[1]);
			}
		}

		return map;
	}
}
