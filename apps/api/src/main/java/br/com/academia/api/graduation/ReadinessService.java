package br.com.academia.api.graduation;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import br.com.academia.api.graduation.Graduation.Kind;
import br.com.academia.api.graduation.GraduationDtos.Progress;
import br.com.academia.api.graduation.GraduationDtos.ReadyStudent;
import br.com.academia.api.graduation.GraduationDtos.RuleTarget;
import br.com.academia.api.student.Belt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadinessService {

	private final ProgressQuery query;
	private final BeltRuleService rules;
	private final Clock clock;

	public ReadinessService(ProgressQuery query, BeltRuleService rules, Clock clock) {
		this.query = query;
		this.rules = rules;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<ReadyStudent> ready(UUID academyId) {
		Map<Belt, RuleValues> effective = rules.effective(academyId);
		LocalDate today = LocalDate.now(clock);
		return query.forAcademy(academyId).stream()
				.map(row -> toReady(row, Readiness.evaluate(row, effective.get(row.belt()), today), today))
				.filter(Objects::nonNull)
				.toList();
	}

	/** {@code null} para aluno inativo ou que não treina: não há progresso a acompanhar. */
	@Transactional(readOnly = true)
	Progress progress(UUID academyId, UUID studentId) {
		List<ProgressRow> rows = query.forStudent(academyId, studentId);
		if (rows.isEmpty()) {
			return null;
		}
		ProgressRow row = rows.getFirst();
		RuleValues rule = rules.effective(academyId).get(row.belt());
		LocalDate today = LocalDate.now(clock);
		return new Progress(row.beltSince(), Readiness.monthsBetween(row.beltSince(), today), row.trainingsSinceBelt(),
				row.lastSince(), row.trainingsSinceLast(), Readiness.evaluate(row, rule, today),
				rule == null ? null : new RuleTarget(rule.minMonths(), rule.minTrainings(), rule.trainingsPerDegree()));
	}

	private static ReadyStudent toReady(ProgressRow row, Kind kind, LocalDate today) {
		if (kind == null) {
			return null;
		}
		boolean belt = kind == Kind.BELT && row.belt() != Belt.PRETA;
		LocalDate since = belt ? row.beltSince() : row.lastSince();
		long trainings = belt ? row.trainingsSinceBelt() : row.trainingsSinceLast();
		return new ReadyStudent(row.studentId(), row.name(), row.nickname(), row.belt().slug(), row.degrees(), kind,
				since, Readiness.monthsBetween(since, today), trainings);
	}
}
