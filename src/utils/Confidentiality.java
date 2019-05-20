/**
 * 
 */
package utils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class Confidentiality {
	private static final String AES_ECB_PKCS5_PADDING = "AES/ECB/PKCS5Padding";
	private static final String AES = "AES";

	private byte[] key;

	public Confidentiality() {
		KeyGenerator keygen = getKeyGeneratorInstance();
		if (keygen == null)
			return;

		this.key = keygen.generateKey().getEncoded();
	}

	private KeyGenerator getKeyGeneratorInstance() {
		try {
			return KeyGenerator.getInstance(AES);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Confidentiality(String encryptKey) {
		this.key = encryptKey.getBytes(StandardCharsets.ISO_8859_1);
	}

	public byte[] encript(byte[] cleartext) {
		try {
			Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, AES));
			return cipher.doFinal(cleartext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] decrypt(byte[] ciphertext) {
		try {
			Cipher aesCipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
			aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES));
			return aesCipher.doFinal(ciphertext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the key
	 */
	public byte[] getKey() {
		return key;
	}

}