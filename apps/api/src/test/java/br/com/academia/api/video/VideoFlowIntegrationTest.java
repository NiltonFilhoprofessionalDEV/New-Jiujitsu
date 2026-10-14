package br.com.academia.api.video;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.PASSWORD;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.authJson;
import static br.com.academia.api.ApiTestSupport.json;
import static br.com.academia.api.ApiTestSupport.read;
import static br.com.academia.api.ApiTestSupport.registerOwner;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import br.com.academia.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class VideoFlowIntegrationTest {

	static final String VIDEOS = CTX + "/videos";

	@Autowired
	MockMvc mvc;

	String owner;
	String student;

	@BeforeEach
	void setUp() throws Exception {
		owner = registerOwner(mvc);
		student = acceptStudentInvite("azul");
	}

	@Test
	void studentInteractsWithAVideoAndOnlyAuthorOrManagerDeletesComments() throws Exception {
		String videoId = createVideo("https://youtu.be/aaaaaaaaaaa", "Raspagem", "\"audience\":\"ALL\"");
		String base = VIDEOS + "/" + videoId;

		mvc.perform(auth(get(VIDEOS), student))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].youtubeId").value("aaaaaaaaaaa"))
				.andExpect(jsonPath("$[0].watched").value(false));
		mvc.perform(authJson(put(base + "/reaction"), student, "{\"reaction\":\"FIRE\"}"))
				.andExpect(jsonPath("$.reactions.FIRE").value(1))
				.andExpect(jsonPath("$.myReaction").value("FIRE"));
		mvc.perform(authJson(put(base + "/reaction"), owner, "{\"reaction\":\"FIRE\"}"))
				.andExpect(jsonPath("$.reactions.FIRE").value(2));
		mvc.perform(authJson(put(base + "/reaction"), student, "{\"reaction\":null}"))
				.andExpect(jsonPath("$.reactions.FIRE").value(1))
				.andExpect(jsonPath("$.myReaction").doesNotExist());
		mvc.perform(auth(put(base + "/favorite"), student)).andExpect(jsonPath("$.favorite").value(true));
		mvc.perform(auth(put(base + "/favorite"), student)).andExpect(jsonPath("$.favorite").value(true));
		mvc.perform(auth(put(base + "/watched"), student)).andExpect(jsonPath("$.watched").value(true));
		mvc.perform(auth(get(base), owner))
				.andExpect(jsonPath("$.watched").value(false))
				.andExpect(jsonPath("$.favorite").value(false));

		String studentComment = comment(base, student, "Muito bom 🔥");
		String ownerComment = comment(base, owner, "Valeu!");
		mvc.perform(auth(get(base + "/comments"), student))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].body").value("Muito bom 🔥"))
				.andExpect(jsonPath("$[0].mine").value(true))
				.andExpect(jsonPath("$[1].canDelete").value(false))
				.andExpect(jsonPath("$[1].authorRole").value("OWNER"));
		mvc.perform(auth(delete(base + "/comments/" + ownerComment), student))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("COMMENT_NOT_YOURS"));
		mvc.perform(auth(delete(base + "/comments/" + studentComment), owner))
				.andExpect(jsonPath("$", hasSize(1)));
		mvc.perform(auth(get(base), student)).andExpect(jsonPath("$.commentCount").value(1));
	}

	@Test
	void audienceByBeltOrClassHidesTheVideoFromEveryoneElse() throws Exception {
		String forPurple = createVideo("https://www.youtube.com/watch?v=bbbbbbbbbbb", "Só roxas",
				"\"audience\":\"BELTS\",\"belts\":[\"roxa\"]");
		String classId = read(mvc.perform(authJson(post(CTX + "/classes"), owner,
				"{\"title\":\"Competição\",\"weekdays\":[2],\"startTime\":\"19:00\"}"))
				.andExpect(status().isCreated()).andReturn(), "$.id");
		String forClass = createVideo("https://youtube.com/shorts/ccccccccccc", "Da turma",
				"\"audience\":\"CLASSES\",\"classIds\":[\"" + classId + "\"]");

		mvc.perform(auth(get(VIDEOS), student)).andExpect(jsonPath("$", hasSize(0)));
		mvc.perform(auth(get(VIDEOS + "/" + forPurple), student)).andExpect(status().isNotFound());
		mvc.perform(authJson(put(VIDEOS + "/" + forPurple + "/reaction"), student, "{\"reaction\":\"LIKE\"}"))
				.andExpect(status().isNotFound());
		mvc.perform(json(post(VIDEOS + "/" + forPurple + "/comments"), "{\"body\":\"oi\"}")
				.header("Authorization", "Bearer " + student)).andExpect(status().isNotFound());

		String studentId = read(mvc.perform(auth(get(CTX + "/portal/profiles"), student)).andReturn(), "$[0].id");
		mvc.perform(authJson(post(CTX + "/classes/" + classId + "/students"), owner,
				"{\"studentId\":\"" + studentId + "\"}")).andExpect(status().isOk());
		mvc.perform(auth(get(VIDEOS), student))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(forClass))
				.andExpect(jsonPath("$[0].vertical").value(true));
		mvc.perform(auth(get(VIDEOS), owner)).andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void onlyManagersEditTheLibraryAndLinksAreValidated() throws Exception {
		String body = videoBody("https://youtu.be/ddddddddddd", "Passagem", "\"audience\":\"ALL\"");
		mvc.perform(authJson(post(VIDEOS), student, body)).andExpect(status().isForbidden());
		mvc.perform(authJson(post(VIDEOS), owner, videoBody("https://vimeo.com/1", "X", "\"audience\":\"ALL\"")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_YOUTUBE_URL"));
		mvc.perform(authJson(post(VIDEOS), owner, videoBody("https://youtu.be/ddddddddddd", "X",
				"\"audience\":\"BELTS\",\"belts\":[]")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VIDEO_AUDIENCE_EMPTY"));
		mvc.perform(authJson(post(VIDEOS), owner, body)).andExpect(status().isCreated());
		mvc.perform(authJson(post(VIDEOS), owner, body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("VIDEO_ALREADY_ADDED"));
		mvc.perform(authJson(post(CTX + "/playlists"), student, "{\"title\":\"P\"}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void playlistsKeepTheManagerOrderAndShowOnlyVisibleVideos() throws Exception {
		String first = createVideo("https://youtu.be/eeeeeeeeeee", "Um", "\"audience\":\"ALL\"");
		String hidden = createVideo("https://youtu.be/fffffffffff", "Dois",
				"\"audience\":\"BELTS\",\"belts\":[\"preta\"]");
		String third = createVideo("https://youtu.be/ggggggggggg", "Três", "\"audience\":\"ALL\"");
		String playlistId = read(mvc.perform(authJson(post(CTX + "/playlists"), owner, """
				{"title":"Fundamentos","videoIds":["%s","%s","%s"]}""".formatted(third, hidden, first)))
				.andExpect(status().isCreated()).andReturn(), "$.id");

		mvc.perform(auth(get(CTX + "/playlists"), student))
				.andExpect(jsonPath("$[0].videoIds", contains(third, first)));
		mvc.perform(authJson(put(CTX + "/playlists/" + playlistId), owner, """
				{"title":"Fundamentos","videoIds":["%s","%s","%s"]}""".formatted(first, third, hidden)))
				.andExpect(jsonPath("$.videoIds", contains(first, third, hidden)));

		mvc.perform(auth(delete(VIDEOS + "/" + first), owner)).andExpect(status().isNoContent());
		mvc.perform(auth(get(CTX + "/playlists"), owner))
				.andExpect(jsonPath("$[0].videoIds", contains(third, hidden)));

		String fourth = createVideo("https://youtu.be/hhhhhhhhhhh", "Quatro",
				"\"audience\":\"ALL\",\"playlistIds\":[\"" + playlistId + "\"]");
		mvc.perform(auth(get(CTX + "/playlists"), owner))
				.andExpect(jsonPath("$[0].videoIds", contains(third, hidden, fourth)));
	}

	private String createVideo(String url, String title, String audience) throws Exception {
		return read(mvc.perform(authJson(post(VIDEOS), owner, videoBody(url, title, audience)))
				.andExpect(status().isCreated()).andReturn(), "$.id");
	}

	private static String videoBody(String url, String title, String audience) {
		return "{\"url\":\"%s\",\"title\":\"%s\",%s}".formatted(url, title, audience);
	}

	private String comment(String base, String token, String body) throws Exception {
		return read(mvc.perform(authJson(post(base + "/comments"), token, "{\"body\":\"" + body + "\"}"))
				.andExpect(status().isCreated()).andReturn(), "$[-1].id");
	}

	private String acceptStudentInvite(String belt) throws Exception {
		String token = read(mvc.perform(authJson(post(CTX + "/invites"), owner, "{\"kind\":\"TRAINING\"}"))
				.andExpect(status().isCreated()).andReturn(), "$.token");
		return read(mvc.perform(json(post(CTX + "/public/invites/" + token + "/accept"), """
				{"name":"Aluno Vídeo","email":"aluno-%s@teste.dev","password":"%s","belt":"%s","degrees":0}
				""".formatted(UUID.randomUUID(), PASSWORD, belt)))
				.andExpect(status().isCreated()).andReturn(), "$.accessToken");
	}
}
