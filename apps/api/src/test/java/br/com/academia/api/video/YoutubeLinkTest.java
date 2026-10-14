package br.com.academia.api.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.academia.api.common.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class YoutubeLinkTest {

	static final String ID = "dQw4w9WgXcQ";

	@ParameterizedTest
	@ValueSource(strings = {
			"https://www.youtube.com/watch?v=" + ID,
			"https://youtube.com/watch?feature=share&v=" + ID + "&t=42s",
			"https://m.youtube.com/watch?v=" + ID,
			"https://youtu.be/" + ID + "?si=abc",
			"youtu.be/" + ID,
			"https://www.youtube.com/embed/" + ID,
			"https://www.youtube-nocookie.com/embed/" + ID,
			"https://www.youtube.com/live/" + ID,
			"  https://www.youtube.com/watch?v=" + ID + "  " })
	void extractsTheVideoIdFromCommonLinks(String url) {
		YoutubeLink link = YoutubeLink.parse(url);

		assertThat(link.id()).isEqualTo(ID);
		assertThat(link.vertical()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://www.youtube.com/shorts/" + ID, "https://youtube.com/shorts/" + ID + "?feature=share" })
	void shortsAreVertical(String url) {
		assertThat(YoutubeLink.parse(url)).isEqualTo(new YoutubeLink(ID, true));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"",
			"not a url",
			"https://vimeo.com/123456",
			"https://youtube.com.evil.dev/watch?v=" + ID,
			"https://evil.dev/youtu.be/" + ID,
			"javascript:alert(1)",
			"ftp://youtube.com/watch?v=" + ID,
			"https://www.youtube.com/watch?v=short",
			"https://www.youtube.com/watch?v=" + ID + "x",
			"https://www.youtube.com/channel/UC1234567890",
			"https://youtu.be/" })
	void rejectsAnythingThatIsNotAYoutubeVideo(String url) {
		assertThatThrownBy(() -> YoutubeLink.parse(url))
				.isInstanceOf(ApiException.class)
				.extracting(e -> ((ApiException) e).code())
				.isEqualTo("INVALID_YOUTUBE_URL");
	}

	@Test
	void rejectsOverlongInput() {
		String url = "https://youtu.be/" + ID + "?x=" + "a".repeat(YoutubeLink.MAX_URL_LENGTH);

		assertThat(YoutubeLink.tryParse(url)).isEmpty();
	}
}
