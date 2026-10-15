package br.com.academia.api;

import static br.com.academia.api.ApiTestSupport.CTX;
import static br.com.academia.api.ApiTestSupport.auth;
import static br.com.academia.api.ApiTestSupport.read;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.UUID;

import br.com.academia.api.academy.AcademyRepository;
import br.com.academia.api.auth.TokenService;
import br.com.academia.api.membership.Membership;
import br.com.academia.api.membership.MembershipRepository;
import br.com.academia.api.membership.MembershipRole;
import br.com.academia.api.user.User;
import br.com.academia.api.user.UserRepository;
import org.springframework.test.web.servlet.MockMvc;

/** Cria professores na academia de um gestor. Use com {@code @Import(StaffFixture.class)}. */
public class StaffFixture {

	public static final String INSTRUCTOR_NAME = "Professor";

	private final UserRepository users;
	private final AcademyRepository academies;
	private final MembershipRepository memberships;
	private final TokenService tokens;

	public StaffFixture(UserRepository users, AcademyRepository academies, MembershipRepository memberships,
			TokenService tokens) {
		this.users = users;
		this.academies = academies;
		this.memberships = memberships;
		this.tokens = tokens;
	}

	/** Vincula um professor novo à academia do dono e devolve o access token dele. */
	public String instructorTokenFor(MockMvc mvc, String ownerToken) throws Exception {
		return memberTokenFor(mvc, ownerToken, MembershipRole.INSTRUCTOR);
	}

	/** Vincula uma conta nova com o papel dado à academia do dono e devolve o access token dela. */
	public String memberTokenFor(MockMvc mvc, String ownerToken, MembershipRole role) throws Exception {
		String academyId = read(mvc.perform(auth(get(CTX + "/me"), ownerToken)).andReturn(), "$.academyId");
		User user = users.save(new User("membro-" + UUID.randomUUID() + "@teste.dev", "x", INSTRUCTOR_NAME));
		var academy = academies.findById(UUID.fromString(academyId)).orElseThrow();
		Membership membership = memberships.save(new Membership(user, academy, role));
		return tokens.issueAccessToken(membership);
	}
}
