package br.com.academia.api.student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.common.TextNormalizer;
import br.com.academia.api.student.StudentEnums.Kind;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.student.StudentFilter.AgeFilter;
import br.com.academia.api.student.StudentFilter.HouseholdFilter;
import br.com.academia.api.student.StudentFilter.KindFilter;
import br.com.academia.api.student.StudentFilter.StatusFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

final class StudentSpecs {

	private static final int KIDS_MAX_AGE = 15;
	private static final int LEGAL_AGE = 18;
	private static final char LIKE_ESCAPE = '\\';

	private StudentSpecs() {
	}

	/** Sempre restrito à academia do token; na consulta de conteúdo já traz a família e ordena por nome. */
	static Specification<Student> matching(UUID academyId, StudentFilter filter, LocalDate today) {
		return (root, query, cb) -> {
			boolean isCount = Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType());
			From<Student, Household> household = householdJoin(root, !isCount);
			if (!isCount) {
				query.orderBy(cb.asc(root.get("searchName")));
			}
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("academyId"), academyId));
			addStatus(predicates, root, cb, filter.status());
			addKind(predicates, root, cb, filter.kind());
			addAge(predicates, root, cb, filter.age(), today);
			if (filter.household() == HouseholdFilter.NONE) {
				predicates.add(cb.isNull(root.get("household")));
			}
			addSearch(predicates, root, household, cb, filter.q());
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	/** Na contagem não pode haver fetch; no conteúdo o fetch evita N+1 ao montar o nome da família. */
	private static From<Student, Household> householdJoin(Root<Student> root, boolean fetch) {
		if (!fetch) {
			return root.join("household", JoinType.LEFT);
		}
		Fetch<Student, Household> fetched = root.fetch("household", JoinType.LEFT);
		return (Join<Student, Household>) fetched;
	}

	private static void addStatus(List<Predicate> out, Root<Student> root, CriteriaBuilder cb, StatusFilter status) {
		switch (status) {
			case ACTIVE -> out.add(cb.equal(root.get("status"), Status.ACTIVE));
			case INACTIVE -> out.add(cb.equal(root.get("status"), Status.INACTIVE));
			case ALL -> { }
		}
	}

	private static void addKind(List<Predicate> out, Root<Student> root, CriteriaBuilder cb, KindFilter kind) {
		switch (kind) {
			case TRAINING -> out.add(cb.equal(root.get("kind"), Kind.TRAINING));
			case RESPONSIBLE -> out.add(cb.equal(root.get("kind"), Kind.RESPONSIBLE));
			case ALL -> { }
		}
	}

	/** Sem data de nascimento conta como adulto na régua de faixa, mas fica fora dos filtros de idade legal. */
	private static void addAge(List<Predicate> out, Root<Student> root, CriteriaBuilder cb, AgeFilter age,
			LocalDate today) {
		var birth = root.<LocalDate>get("birthDate");
		LocalDate kidsCutoff = today.minusYears(KIDS_MAX_AGE + 1L);
		LocalDate legalCutoff = today.minusYears(LEGAL_AGE);
		switch (age) {
			case KIDS -> out.add(cb.greaterThan(birth, kidsCutoff));
			case ADULT -> out.add(cb.or(cb.isNull(birth), cb.lessThanOrEqualTo(birth, kidsCutoff)));
			case UNDER18 -> out.add(cb.greaterThan(birth, legalCutoff));
			case ADULT18 -> out.add(cb.lessThanOrEqualTo(birth, legalCutoff));
			case ALL -> { }
		}
	}

	private static void addSearch(List<Predicate> out, Root<Student> root, From<Student, Household> household,
			CriteriaBuilder cb, String q) {
		String folded = TextNormalizer.fold(q);
		if (folded.isEmpty()) {
			return;
		}
		String pattern = TextNormalizer.likeContains(folded);
		List<Predicate> any = new ArrayList<>();
		any.add(cb.like(root.get("searchName"), pattern, LIKE_ESCAPE));
		any.add(cb.like(root.get("email"), pattern, LIKE_ESCAPE));
		any.add(cb.like(household.get("searchName"), pattern, LIKE_ESCAPE));
		String digits = TextNormalizer.digits(q);
		if (digits.length() >= 3) {
			any.add(cb.like(root.get("cpf"), "%" + digits + "%"));
		}
		out.add(cb.or(any.toArray(Predicate[]::new)));
	}
}
