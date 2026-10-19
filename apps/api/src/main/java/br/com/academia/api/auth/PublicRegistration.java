package br.com.academia.api.auth;

import br.com.academia.api.common.ApiException;

/** O cadastro aberto de academia fica fechado. Quem cria academia entra por convite do admin. */
public final class PublicRegistration {

	private PublicRegistration() {
	}

	public static void requireOpen(boolean open) {
		if (!open) {
			throw ApiException.forbidden("REGISTRATION_CLOSED",
					"O cadastro de academia é só por convite do administrador da plataforma");
		}
	}
}
