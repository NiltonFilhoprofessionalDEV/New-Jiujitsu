package br.com.academia.api.billing;

import java.util.UUID;

/** Mensalidades ativas por plano. */
public record PlanCount(UUID planId, long count) {
}
