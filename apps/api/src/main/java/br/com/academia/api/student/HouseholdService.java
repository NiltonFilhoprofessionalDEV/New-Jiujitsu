package br.com.academia.api.student;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.student.HouseholdDtos.CreateHouseholdRequest;
import br.com.academia.api.student.HouseholdDtos.HouseholdDetail;
import br.com.academia.api.student.HouseholdDtos.HouseholdSummary;
import br.com.academia.api.student.HouseholdDtos.StudentRef;
import br.com.academia.api.student.StudentDtos.StudentDetail;
import br.com.academia.api.student.StudentDtos.StudentListItem;
import br.com.academia.api.student.StudentDtos.StudentRequest;
import br.com.academia.api.student.StudentEnums.Kind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseholdService {

	private final HouseholdRepository households;
	private final StudentRepository students;
	private final StudentService studentService;

	public HouseholdService(HouseholdRepository households, StudentRepository students,
			StudentService studentService) {
		this.households = households;
		this.students = students;
		this.studentService = studentService;
	}

	@Transactional(readOnly = true)
	public List<HouseholdSummary> list(UUID academyId) {
		List<Household> all = households.findAllInAcademy(academyId);
		Map<UUID, Long> counts = students.countMembersByHousehold(academyId).stream()
				.collect(Collectors.toMap(HouseholdMemberCount::householdId, HouseholdMemberCount::members));
		List<UUID> responsibleIds = all.stream().map(Household::getResponsibleStudentId).filter(Objects::nonNull)
				.toList();
		Map<UUID, Student> responsibles = students.findAllInAcademy(academyId, responsibleIds).stream()
				.collect(Collectors.toMap(Student::getId, Function.identity()));
		return all.stream().map(h -> {
			Student r = responsibles.get(h.getResponsibleStudentId());
			StudentRef ref = r == null ? null : new StudentRef(r.getId(), r.getName());
			return new HouseholdSummary(h.getId(), h.getName(), ref, counts.getOrDefault(h.getId(), 0L));
		}).toList();
	}

	@Transactional(readOnly = true)
	public HouseholdDetail get(UUID academyId, UUID id) {
		return detail(require(academyId, id));
	}

	@Transactional
	public HouseholdDetail create(UUID academyId, CreateHouseholdRequest request) {
		Set<UUID> memberIds = new LinkedHashSet<>(request.memberIds() == null ? List.of() : request.memberIds());
		if (request.responsibleStudentId() != null) {
			memberIds.add(request.responsibleStudentId());
		}
		if (memberIds.isEmpty()) {
			throw ApiException.badRequest("HOUSEHOLD_EMPTY", "A família precisa de ao menos um membro");
		}
		List<Student> members = students.findAllInAcademy(academyId, new ArrayList<>(memberIds));
		if (members.size() != memberIds.size()) {
			throw ApiException.notFound("STUDENT_NOT_FOUND", "Aluno não encontrado");
		}
		members.forEach(HouseholdService::requireNoHousehold);

		Household household = households.save(new Household(academyId, request.name()));
		household.setResponsibleStudentId(request.responsibleStudentId());
		members.forEach(s -> s.setHousehold(household));
		return detail(household);
	}

	@Transactional
	public HouseholdDetail rename(UUID academyId, UUID id, String name) {
		Household household = require(academyId, id);
		household.rename(name);
		return detail(household);
	}

	@Transactional
	public HouseholdDetail setResponsible(UUID academyId, UUID id, UUID studentId) {
		Household household = require(academyId, id);
		requireMember(household, studentService.require(academyId, studentId));
		household.setResponsibleStudentId(studentId);
		return detail(household);
	}

	@Transactional
	public HouseholdDetail addMember(UUID academyId, UUID id, UUID studentId) {
		Household household = require(academyId, id);
		Student student = studentService.require(academyId, studentId);
		requireNoHousehold(student);
		student.setHousehold(household);
		return detail(household);
	}

	@Transactional
	public HouseholdDetail removeMember(UUID academyId, UUID id, UUID studentId) {
		Household household = require(academyId, id);
		Student student = studentService.require(academyId, studentId);
		requireMember(household, student);
		student.setHousehold(null);
		if (studentId.equals(household.getResponsibleStudentId())) {
			household.setResponsibleStudentId(null);
		}
		return detail(household);
	}

	/** Dependente treina e fica sob o responsável; por isso a família precisa ter um responsável definido. */
	@Transactional
	public StudentDetail addDependent(UUID academyId, UUID id, StudentRequest request) {
		Household household = require(academyId, id);
		if (household.getResponsibleStudentId() == null) {
			throw ApiException.badRequest("HOUSEHOLD_WITHOUT_RESPONSIBLE",
					"Defina o responsável da família antes de cadastrar um dependente");
		}
		if (request.kind() != Kind.TRAINING) {
			throw ApiException.badRequest("DEPENDENT_MUST_TRAIN", "Dependente precisa ser aluno que treina");
		}
		return studentService.create(academyId, request, household);
	}

	@Transactional
	public void delete(UUID academyId, UUID id) {
		Household household = require(academyId, id);
		households.detachMembers(household.getId());
		households.delete(household);
	}

	private Household require(UUID academyId, UUID id) {
		return households.findByIdAndAcademyId(id, academyId)
				.orElseThrow(() -> ApiException.notFound("HOUSEHOLD_NOT_FOUND", "Família não encontrada"));
	}

	private HouseholdDetail detail(Household h) {
		List<StudentListItem> members = students.findMembers(h.getId()).stream().map(StudentListItem::of).toList();
		return new HouseholdDetail(h.getId(), h.getName(), h.getResponsibleStudentId(), members);
	}

	private static void requireNoHousehold(Student student) {
		if (student.getHousehold() != null) {
			throw ApiException.conflict("STUDENT_IN_OTHER_HOUSEHOLD",
					student.getName() + " já faz parte de outra família");
		}
	}

	private static void requireMember(Household household, Student student) {
		if (student.getHousehold() == null || !student.getHousehold().getId().equals(household.getId())) {
			throw ApiException.badRequest("NOT_A_MEMBER", "O aluno não faz parte desta família");
		}
	}
}
