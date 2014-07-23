package com.starfish.kol.model.models.login;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash implements Serializable
{
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -3913668500864943263L;
	
	private final String hash;
	
	public PasswordHash(String password, boolean prehashed) {
		if(prehashed)
			hash = password;
		else
			this.hash = getHash(password);
	}
	
	protected String completeChallenge(String challenge) {
		return getHash(hash + ":" + challenge);
	}
	
	public String getBaseHash() {
		return hash;
	}
	
	private static String getHash(String value) {
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			return getHexString(digester.digest(value.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static String getHexString(final byte[] bytes) {
		byte[] output = new byte[bytes.length + 1];
		for (int i = 0; i < bytes.length; ++i) {
			output[i + 1] = bytes[i];
		}

		StringBuffer result = new StringBuffer(
				(new BigInteger(output)).toString(16));
		int desiredLength = bytes.length * 2;

		while (result.length() < desiredLength) {
			result.insert(0, '0');
		}

		if (result.length() > desiredLength) {
			result.delete(0, result.length() - desiredLength);
		}

		return result.toString();
	}
}