package pkg.demo.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JasEncryptUtils {
	
	@Autowired
	private org.jasypt.encryption.StringEncryptor jasyptStringEncryptor;

	private static final String pwdENCStart = "ENC(";
	private static final String pwdENCEnd = ")";

	private String cmsGSBEncrypt(String plainText) {
		return jasyptStringEncryptor.encrypt(plainText);
	}

	private String _decrypt(String cipherText) {
		return jasyptStringEncryptor.decrypt(cipherText);
	}

	public String _encrypt(String plainText) {
		if (plainText != null) {
			if (plainText.startsWith(pwdENCStart) & plainText.endsWith(pwdENCEnd)) {
				return plainText;
			} else {
				return pwdENCStart + cmsGSBEncrypt(plainText) + pwdENCEnd;
			}
		}
		return null;
	}

	public String decrypt(String cipherText) {
		if (cipherText != null) {
			String pwd = cipherText;

			if (cipherText.startsWith(pwdENCStart) & cipherText.endsWith(pwdENCEnd)) {
				pwd = cipherText.substring(pwdENCStart.length(), cipherText.lastIndexOf(pwdENCEnd));
				if (pwd != null && !pwd.equals("")) {
					_decrypt(pwd);
				}
			}
			return pwd;
		}
		return null;
	}

	public static String getPwdencstart() {
		return pwdENCStart;
	}

	public static String getPwdencend() {
		return pwdENCEnd;
	}
}
