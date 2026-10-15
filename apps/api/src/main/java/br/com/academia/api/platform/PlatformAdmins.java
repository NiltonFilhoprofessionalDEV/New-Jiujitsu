package br.com.academia.api.platform;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.config.AppProperties;
import br.com.academia.api.user.UserRepository;
import org.springframework.stereotype.Component;

/** Administradores da plataforma, definidos por e-mail na configuração ({@code APP_PLATFORM_ADMIN_EMAILS}). */
@Component
public class PlatformAdmins {

	private final Set<String> emails;
	private final UserRepository users;

	public PlatformAdmins(AppProperties props, UserRepository users) {
		this.emails = Set.copyOf(props.platform().adminEmails());
		this.users = users;
	}

	public boolean isAdmin(UUID userId) {
		if (emails.isEmpty()) {
			return false;
		}
		return users.findById(userId)
				.map(u -> emails.contains(u.getEmail().toLowerCase(Locale.ROOT)))
				.orElse(false);
	}

	public void requireAdmin(UUID userId) {
		if (!isAdmin(userId)) {
			throw ApiException.forbidden("PLATFORM_ADMIN_ONLY", "Área restrita à administração da plataforma");
		}
	}
}
