package ittalents.javaee1.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class CryptWithBCrypt {
	
	private static final int Salt = 13;
	
	public static String hashPassword(String password_plaintext) {
		String salt = BCrypt.gensalt(Salt);
		String hashed_password = BCrypt.hashpw(password_plaintext, salt);
		
		return (hashed_password);
	}
	
	public static boolean checkPassword(String password_plaintext, String stored_hash) {
		return BCrypt.checkpw(password_plaintext, stored_hash);
	}
}
