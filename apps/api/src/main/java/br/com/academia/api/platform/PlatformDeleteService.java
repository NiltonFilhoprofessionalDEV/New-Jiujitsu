package br.com.academia.api.platform;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.academia.api.academy.Academy;
import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Apaga uma academia criada por engano. Faturas e mensalidades saem antes da academia porque o banco
 * não deixa o aluno ou o plano sumir enquanto elas existem. O login do dono sai junto se ele não
 * tiver outra academia, para o e-mail poder ser usado de novo.
 */
@Service
public class PlatformDeleteService {

	private final PlatformAdmins admins;
	private final AcademyRepository academies;
	private final EntityManager em;

	public PlatformDeleteService(PlatformAdmins admins, AcademyRepository academies, EntityManager em) {
		this.admins = admins;
		this.academies = academies;
		this.em = em;
	}

	@Transactional
	public void delete(CurrentUser me, UUID academyId, String confirmName) {
		admins.requireAdmin(me.userId());
		Academy academy = academies.findById(academyId)
				.orElseThrow(() -> ApiException.notFound("ACADEMY_NOT_FOUND", "Academia não encontrada"));
		if (!sameName(academy.getName(), confirmName)) {
			throw ApiException.badRequest("ACADEMY_NAME_MISMATCH",
					"Digite o nome da academia exatamente como está na lista");
		}
		List<UUID> owners = ownerIds(academyId);
		clearDeleteBlockers(academyId);
		academies.delete(academy);
		em.flush();
		removeOrphanOwners(owners);
	}

	private List<UUID> ownerIds(UUID academyId) {
		return em.createNativeQuery("""
				select user_id from memberships
				where academy_id = ?1 and role = 'OWNER' and platform_support = false
				""", UUID.class).setParameter(1, academyId).getResultList();
	}

	private void clearDeleteBlockers(UUID academyId) {
		execute("update households set responsible_student_id = null where academy_id = ?1", academyId);
		execute("update students set household_id = null where academy_id = ?1", academyId);
		execute("delete from invoices where academy_id = ?1", academyId);
		execute("delete from subscriptions where academy_id = ?1", academyId);
	}

	private void removeOrphanOwners(List<UUID> owners) {
		for (UUID ownerId : owners) {
			execute("""
					delete from users u
					where u.id = ?1
					  and not exists (select 1 from memberships m where m.user_id = u.id)
					  and not exists (select 1 from students s where s.user_id = u.id)
					""", ownerId);
		}
	}

	private void execute(String sql, UUID id) {
		em.createNativeQuery(sql).setParameter(1, id).executeUpdate();
	}

	private static boolean sameName(String academyName, String confirmName) {
		if (confirmName == null) {
			return false;
		}
		return academyName.trim().equalsIgnoreCase(confirmName.trim());
	}
}
