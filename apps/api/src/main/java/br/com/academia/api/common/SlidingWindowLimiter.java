package br.com.academia.api.common;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conta eventos por chave numa janela deslizante, em memória. Vale por instância:
 * com várias réplicas, mover para Redis ou para o banco.
 */
public class SlidingWindowLimiter {

	private static final int MAX_TRACKED_KEYS = 50_000;

	private final Map<String, Deque<Instant>> events = new ConcurrentHashMap<>();
	private final int maxEvents;
	private final Duration window;

	public SlidingWindowLimiter(int maxEvents, Duration window) {
		this.maxEvents = maxEvents;
		this.window = window;
	}

	/** Lança 429 se a chave já atingiu o limite dentro da janela. */
	public void check(String key) {
		Deque<Instant> recent = events.get(key);
		if (recent == null) {
			return;
		}
		synchronized (recent) {
			prune(recent, Instant.now());
			if (recent.size() >= maxEvents) {
				throw ApiException.tooManyRequests("TOO_MANY_ATTEMPTS",
						"Muitas tentativas. Aguarde alguns minutos e tente de novo.");
			}
		}
	}

	public void record(String key) {
		if (events.size() > MAX_TRACKED_KEYS) {
			events.clear();
		}
		Deque<Instant> recent = events.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (recent) {
			recent.addLast(Instant.now());
		}
	}

	public void reset(String key) {
		events.remove(key);
	}

	private void prune(Deque<Instant> recent, Instant now) {
		Instant cutoff = now.minus(window);
		while (!recent.isEmpty() && recent.peekFirst().isBefore(cutoff)) {
			recent.pollFirst();
		}
	}
}
