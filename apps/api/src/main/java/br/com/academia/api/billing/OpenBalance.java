package br.com.academia.api.billing;

import java.time.LocalDate;
import java.util.UUID;

/** Faturas em aberto de um aluno: {@code overdueSince} nulo quando nada venceu. */
public record OpenBalance(UUID studentId, LocalDate overdueSince, long overdueCents, LocalDate upcomingDue) {
}
