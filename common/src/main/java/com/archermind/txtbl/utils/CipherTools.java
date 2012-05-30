package com.archermind.txtbl.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.logging.Logger;


public class CipherTools {

    private static final Logger logger = Logger.getLogger(CipherTools.class);

    /**
     * The message digest size (in bits)
     */
    public static final int DIGESTBITS = 512;

	/**
	 * The message digest size (in bytes)
	 */
	public static final int DIGESTBYTES = DIGESTBITS >>> 3;

    /**
     * The SHA-256 Algorithm
     */
    public static final String SHA_256 = "SHA-256";


	/**
	 * decrypt email password
	 * 
	 * @param s
	 *            encryped password
	 * @param keyString
	 *            use email name as its key for example (test123@gmail, test123
	 *            will be used as its key)
	 * @return String if return null, decrypt failed
	 */
	public static String RC4Decrype(byte[] in, String keyString) {
		// byte[] key = genKey(keyString);
		byte[] key = getHashCode(keyString);
		if (key == null)
			try {
				return new String(in, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return null;
			}
		else {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "ARCFOUR");
			try {
				Cipher cipher = Cipher.getInstance("ARCFOUR");
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
				return new String(cipher.doFinal(in), "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				try {
					return new String(in, "utf-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					return null;
				}
			}
		}
	}

	/**
	 * encrypt email password
	 * 
	 * @param s
	 *            password
	 * @param keyString
	 *            use email name as its key for example (test123@gmail, test123
	 *            will be used as its key)
	 * @return byte[] if return null, encrypt failed
	 */
	public static byte[] RC4Encrypt(String s, String keyString) {
		byte[] key = getHashCode(keyString);
		if (key == null)
			try {
				return s.getBytes("utf-8");
			} catch (UnsupportedEncodingException e1) {
				return null;
			}
		else {
			SecretKeySpec skeySpec = new SecretKeySpec(key, "ARCFOUR");
			try {
				Cipher cipher = Cipher.getInstance("ARCFOUR");
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
				return cipher.doFinal(s.getBytes("utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e);
				try {
					return s.getBytes("utf-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
					return null;
				}
			}
		}
	}

    /**
     * Returns a hash of the given plain text data using the algorithm specified
     * @param data The data to hash
     * @param algorithm The algorithm to employ. For example, CipherTools.SHA_256.
     * @return The hex hash of data
     * @throws NoSuchAlgorithmException If algorithm is not supported
     * @throws UnsupportedEncodingException If UTF-8 is not supported
     */
    public static String getHash(String data, String algorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        long start = System.currentTimeMillis();

        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(data.getBytes("UTF-8"));

        byte raw[] = md.digest();

        StringBuffer hexString = new StringBuffer();
        for (byte aRaw : raw) {
            hexString.append(Integer.toHexString(0xFF & aRaw));
        }

        logger.info("Completed " + algorithm + " hash generation in " + (System.currentTimeMillis() - start) + " ms.");

        return hexString.toString().toUpperCase();
    }

	private static byte[] getHashCode(String keyString) {

		Whirlpool w = new Whirlpool();
		byte[] digest = new byte[DIGESTBYTES];
		byte[] data = new byte[1000000];
		Arrays.fill(data, (byte) 0);
		w.NESSIEinit();
		w.NESSIEadd(keyString);
		w.NESSIEfinalize(digest);
		
		byte[] result = new byte[16];
		System.arraycopy(digest, 0, result, 0, 16);

		return result;
	}
}
