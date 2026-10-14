package br.com.academia.api.video;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import br.com.academia.api.common.ApiException;

/**
 * Vídeo do YouTube extraído de um link colado pelo gestor. Só o id é guardado: o player sempre monta a URL
 * de embed, então nenhum endereço digitado chega ao navegador dos alunos.
 */
public record YoutubeLink(String id, boolean vertical) {

	static final int MAX_URL_LENGTH = 300;

	private static final Pattern VIDEO_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
	private static final Set<String> WATCH_HOSTS = Set.of("youtube.com", "m.youtube.com", "music.youtube.com",
			"youtube-nocookie.com");
	private static final String SHORT_HOST = "youtu.be";
	private static final Set<String> ID_PATHS = Set.of("embed", "shorts", "live", "v");
	private static final String SHORTS_PATH = "shorts";

	public static YoutubeLink parse(String url) {
		return tryParse(url).orElseThrow(() -> ApiException.badRequest("INVALID_YOUTUBE_URL",
				"Cole o link de um vídeo do YouTube (ex.: https://youtu.be/...)"));
	}

	static Optional<YoutubeLink> tryParse(String url) {
		if (url == null || url.isBlank() || url.length() > MAX_URL_LENGTH) {
			return Optional.empty();
		}
		String trimmed = url.trim();
		URI uri;
		try {
			uri = new URI(trimmed.contains("://") ? trimmed : "https://" + trimmed);
		} catch (URISyntaxException e) {
			return Optional.empty();
		}
		String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
		if (uri.getHost() == null || !(scheme.equals("https") || scheme.equals("http"))) {
			return Optional.empty();
		}
		String host = uri.getHost().toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
		String[] segments = Arrays.stream(String.valueOf(uri.getPath()).split("/")).filter(s -> !s.isEmpty())
				.toArray(String[]::new);
		if (host.equals(SHORT_HOST)) {
			return segments.length == 0 ? Optional.empty() : of(segments[0], false);
		}
		if (!WATCH_HOSTS.contains(host)) {
			return Optional.empty();
		}
		if (segments.length >= 2 && ID_PATHS.contains(segments[0])) {
			return of(segments[1], segments[0].equals(SHORTS_PATH));
		}
		if (segments.length == 1 && segments[0].equals("watch")) {
			return queryParam(uri.getRawQuery(), "v").flatMap(id -> of(id, false));
		}
		return Optional.empty();
	}

	private static Optional<YoutubeLink> of(String id, boolean vertical) {
		return VIDEO_ID.matcher(id).matches() ? Optional.of(new YoutubeLink(id, vertical)) : Optional.empty();
	}

	private static Optional<String> queryParam(String query, String name) {
		if (query == null) {
			return Optional.empty();
		}
		return Arrays.stream(query.split("&"))
				.map(pair -> pair.split("=", 2))
				.filter(pair -> pair.length == 2 && pair[0].equals(name))
				.map(pair -> pair[1])
				.findFirst();
	}
}
