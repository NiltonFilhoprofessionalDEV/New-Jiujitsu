package br.com.academia.api.graduation;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.academia.api.common.ApiException;
import br.com.academia.api.graduation.BeltRuleDtos.RuleInput;
import br.com.academia.api.graduation.BeltRuleDtos.RuleView;
import br.com.academia.api.student.Belt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeltRuleService {

	private final BeltRuleRepository rules;

	public BeltRuleService(BeltRuleRepository rules) {
		this.rules = rules;
	}

	@Transactional(readOnly = true)
	public List<RuleView> list(UUID academyId) {
		Map<Belt, BeltRule> saved = saved(academyId);
		return BeltRuleDefaults.EDITABLE.stream().map(belt -> {
			BeltRule row = saved.get(belt);
			RuleValues v = row == null ? BeltRuleDefaults.of(belt) : row.values();
			return new RuleView(belt.slug(), v.minMonths(), v.minTrainings(), v.trainingsPerDegree(), row != null);
		}).toList();
	}

	/** Regra em vigor por faixa: a salva pela academia ou, na falta dela, o padrão. */
	@Transactional(readOnly = true)
	Map<Belt, RuleValues> effective(UUID academyId) {
		Map<Belt, BeltRule> saved = saved(academyId);
		Map<Belt, RuleValues> result = new EnumMap<>(Belt.class);
		BeltRuleDefaults.EDITABLE.forEach(belt -> {
			BeltRule row = saved.get(belt);
			result.put(belt, row == null ? BeltRuleDefaults.of(belt) : row.values());
		});
		return result;
	}

	@Transactional
	public List<RuleView> save(UUID academyId, List<RuleInput> inputs) {
		Map<Belt, BeltRule> saved = saved(academyId);
		Set<Belt> seen = new HashSet<>();
		for (RuleInput input : inputs) {
			Belt belt = Belt.fromSlug(input.belt());
			if (!BeltRuleDefaults.EDITABLE.contains(belt) || !seen.add(belt)) {
				throw ApiException.badRequest("INVALID_BELT_RULE", "Regra inválida para a faixa " + input.belt());
			}
			BeltRule row = saved.computeIfAbsent(belt, b -> new BeltRule(academyId, b));
			row.apply(new RuleValues(input.minMonths(), input.minTrainings(), input.trainingsPerDegree()));
			rules.save(row);
		}
		return list(academyId);
	}

	private Map<Belt, BeltRule> saved(UUID academyId) {
		return rules.findByAcademyId(academyId).stream()
				.collect(Collectors.toMap(BeltRule::getBelt, Function.identity(), (a, b) -> a,
						() -> new EnumMap<>(Belt.class)));
	}
}
