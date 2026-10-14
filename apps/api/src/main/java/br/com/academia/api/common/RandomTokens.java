package br.com.academia.api.common;

import java.security.SecureRandom;
import java.util.Base64;

/** Tokens opacos, aleatórios e seguros para URL (refresh, convites, link de matrícula). */
public final class RandomTokens {

	private static final int TOKEN_BYTES = 32;
	private static final SecureRandom RANDOM = new SecureRandom();

	private RandomTokens() {
	}

	/** 256 bits em base64url sem padding: 43 caracteres. */
	public static String urlSafe() {
		byte[] bytes = new byte[TOKEN_BYTES];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
