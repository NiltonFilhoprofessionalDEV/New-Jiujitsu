package br.com.academia.api.portal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.common.CurrentUser;
import br.com.academia.api.student.Household;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentRepository;
import org.springframework.stereotype.Component;

/**
 * Quem a conta logada pode ver: o próprio cadastro e, se for o responsável da família, todos os membros dela.
 * Qualquer outro aluno responde 404, sem revelar que existe.
 */
@Component
public class PortalAccess {

	private final StudentRepository students;

	PortalAccess(StudentRepository students) {
		this.students = students;
	}

	/** O primeiro é sempre o próprio cadastro. */
	public List<Student> profiles(CurrentUser me) {
		Map<UUID, Student> visible = new LinkedHashMap<>();
		for (Student self : students.findLinkedToUser(me.academyId(), me.userId())) {
			visible.put(self.getId(), self);
			Household household = self.getHousehold();
			if (household != null && self.getId().equals(household.getResponsibleStudentId())) {
				students.findMembers(household.getId()).forEach(m -> visible.putIfAbsent(m.getId(), m));
			}
		}
		return List.copyOf(visible.values());
	}

	Student require(CurrentUser me, UUID studentId) {
		return profiles(me).stream()
				.filter(s -> s.getId().equals(studentId))
				.findFirst()
				.orElseThrow(() -> ApiException.notFound("STUDENT_NOT_FOUND", "Aluno não encontrado"));
	}

	static boolean isSelf(Student student, CurrentUser me) {
		return me.userId().equals(student.getUserId());
	}
}
