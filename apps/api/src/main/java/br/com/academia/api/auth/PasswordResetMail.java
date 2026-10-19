package br.com.academia.api.auth;

/** Envio do link de redefinição. A implementação real fala com o Resend. */
public interface PasswordResetMail {

	boolean enabled();

	void sendResetLink(String email, String link);
}
