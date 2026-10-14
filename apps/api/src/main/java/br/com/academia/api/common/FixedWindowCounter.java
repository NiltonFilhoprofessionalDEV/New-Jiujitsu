package br.com.academia.api.common;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conta requisições por chave em janelas fixas, em memória e com custo constante por chave
 * (ao contrário de {@link SlidingWindowLimiter}, que guarda cada evento). Vale por instância.
 */
public class FixedWindowCounter {

	private static final int MAX_TRACKED_KEYS = 50_000;

	private record Window(long index, AtomicInteger count) {
	}

	private final Map<String, Window> windows = new ConcurrentHashMap<>();
	private final int limit;
	private final long windowMillis;
	private final Clock clock;

	public FixedWindowCounter(int limit, Duration window, Clock clock) {
		this.limit = limit;
		this.windowMillis = window.toMillis();
		this.clock = clock;
	}

	/** Conta a requisição e diz se ela ainda cabe no limite da janela atual. */
	public boolean tryAcquire(String key) {
		long index = clock.millis() / windowMillis;
		if (windows.size() > MAX_TRACKED_KEYS) {
			windows.clear();
		}
		Window current = windows.compute(key,
				(k, w) -> w == null || w.index() != index ? new Window(index, new AtomicInteger()) : w);
		return current.count().incrementAndGet() <= limit;
	}

	/** Segundos (arredondados para cima) até a próxima janela, para o cabeçalho {@code Retry-After}. */
	public long secondsUntilReset() {
		long remaining = windowMillis - clock.millis() % windowMillis;
		return Math.max(1, (remaining + 999) / 1000);
	}
}
