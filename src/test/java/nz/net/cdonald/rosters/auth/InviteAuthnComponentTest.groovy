package nz.net.cdonald.rosters.auth

import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import com.auth0.spring.security.api.authentication.JwtAuthentication
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.Auth0Service
import nz.net.cdonald.rosters.services.OperatorService
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class InviteAuthnComponentTest extends Assert {

	@Value('${jwt.audience}')
	String audience

	@Value('${jwt.issuer}')
	String issuer

	@Value('${jwt.secret}')
	String secret

	@Autowired
	InviteAuthnComponent inviteAuthnComponent

	@Autowired
	Auth0Service auth0Service

	@Autowired
	OperatorService operatorService

	@Test
	public void testOperatorExists() {
		String jwt = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,secret.bytes)
			.setIssuer(issuer).setAudience(audience).claim("app_metadata",["operator_id":"123"]).compact()

		PreAuthenticatedAuthenticationJsonWebToken token = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

		def authn = inviteAuthnComponent.authenticate(token);
		assertNotNull(authn);
		assertEquals(AuthenticationJsonWebToken,authn.class)
	}

	@Test
	public void testUnboundPermission() {
		String jwt = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,secret.bytes)
				.setIssuer(issuer).setAudience(audience).claim("scope","operator:unbound").compact()

		PreAuthenticatedAuthenticationJsonWebToken token = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

		def authn = inviteAuthnComponent.authenticate(token);
		assertNotNull(authn);
	}

	@Test
	public void testUnboundPermissionAndOperator() {
		String jwt = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,secret.bytes)
				.setIssuer(issuer).setAudience(audience).claim("scope","operator:unbound")
				.claim("app_metadata",["permissions":["operator:unbound"],"operator_id":"123"]).compact()

		PreAuthenticatedAuthenticationJsonWebToken token = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);

		def authn = inviteAuthnComponent.authenticate(token);
		assertNotNull(authn);
	}

	@Test
	public void testEmptyAppMetadata() {
		Exception e = null
		try {
			String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience).claim("app_metadata",[:]).compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt));
		} catch (BadCredentialsException ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testNoEmailVerified() {
		Exception e = null
		try {
			String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience).compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt));
		} catch (BadCredentialsException ex) {
			e = ex
		}

		assertNotNull(e)

		Exception e1 = null
		try {
			//uses an unverified account
			String jwt = Jwts.builder().setSubject("auth0|58741f430b097f12dd78efa2").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience).compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt))
		} catch (InsufficientAuthenticationException ex) {
			e1 = ex
		}

		assertNotNull(e1)
	}

	@Test
	public void testNoOperatorFoundForEmail() {
		Exception e1 = null
		try {
			//uses a verified account w/out an operator
			String jwt = Jwts.builder().setSubject("auth0|5874225be8e62d2d9e212f3a").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience).compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt))
		} catch (BadCredentialsException ex) {
			e1 = ex
		}

		assertNotNull(e1)
	}

	@Test
	public void testOperatorTableUpdate() {
		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString()

		def user = auth0Service.createUser(email, password, true)

		def o = new Operator()
		o.email = email
		o.firstName = "abc"
		o.lastName = "def"

		operatorService.createOperator(o)

		String jwt = Jwts.builder().setSubject(user.user_id).signWith(SignatureAlgorithm.HS256,secret.bytes)
				.setIssuer(issuer).setAudience(audience).compact()

		PreAuthenticatedAuthenticationJsonWebToken token = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
		def authn = inviteAuthnComponent.authenticate(token);

		def o1 = operatorService.getOperator(o.id)
		assertEquals(user.user_id, o1.authUserId)

		def user1 = auth0Service.getProfile(user.user_id)
		assertEquals(o.id,user1.app_metadata.operator_id)

		//check the returned metadata has added the key as needed
		def claims = Jwts.parser().setSigningKey(secret.bytes).parseClaimsJws((authn as JwtAuthentication).token)
		assertEquals(o.id, claims.getBody().get("app_metadata").operator_id)

		auth0Service.deleteUser(user.user_id)
	}

	@Test
	public void testSubsequentAPICallsUpdate() {
		//test the subsequent calls aren't messing with the database...
		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString()

		//create user
		def user = auth0Service.createUser(email, password, true)

		//pretend we've already gone through the authn process
		//while the operator doesn't exist in the database (and not in the JWT below), it shouldn't matter
		//as it'll come from the profile
		auth0Service.updateAppMetadata(user.user_id,["operator_id":123])

		//there's no operator_id so it'll skip to checking the profile
		String jwt = Jwts.builder().setSubject(user.user_id).signWith(SignatureAlgorithm.HS256,secret.bytes)
				.setIssuer(issuer).setAudience(audience).compact()

		PreAuthenticatedAuthenticationJsonWebToken token = PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt);
		def authn = inviteAuthnComponent.authenticate(token);

		//and the returned value should have the metadata included now
		def claims = Jwts.parser().setSigningKey(secret.bytes).parseClaimsJws((authn as JwtAuthentication).token)
		assertEquals(123, claims.getBody().get("app_metadata").operator_id)

		auth0Service.deleteUser(user.user_id)
	}


}
