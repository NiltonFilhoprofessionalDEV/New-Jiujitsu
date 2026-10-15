package br.com.academia.api.access;

import java.time.Instant;

/**
 * Acesso calculado de uma academia. {@code daysLeft} conta atÃ© o prÃ³ximo marco: fim do teste ou do
 * perÃ­odo pago (TRIAL/ACTIVE), ou o bloqueio (GRACE).
 */
public record Access(AccessStatus status, Instant accessUntil, Instant suspendsAt, long daysLeft) {
}
