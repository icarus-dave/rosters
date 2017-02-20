package nz.net.cdonald.rosters.auth

import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import com.auth0.spring.security.api.authentication.JwtAuthentication
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.UserService
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
class AuthenticiationProviderTest extends Assert {

	@Value('${jwt.audience}')
	String audience

	@Value('${jwt.issuer}')
	String issuer

	@Value('${jwt.secret}')
	String secret

	@Autowired
	AuthenticationProvider inviteAuthnComponent

	@Autowired
	UserService userService

	@Autowired
	OperatorService operatorService

	@Test
	public void testOperatorSignedUp() {
		String jwt = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,secret.bytes)
			.setIssuer(issuer).setAudience(audience).claim("app_metadata",["signup_complete":true, "operator_id":"123"]).compact()

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
				.claim("app_metadata",["permissions":["operator:unbound"],"operator_id":"123","signup_complete":true]).compact()

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
	public void testNoAppMetadata() {
		Exception e = null
		try {
			String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience).compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt));
		} catch (BadCredentialsException ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testNoOperatorFoundForEmail() {
		Exception e1 = null
		try {
			//uses a verified account w/out an operator
			String jwt = Jwts.builder().setSubject("123:123").signWith(SignatureAlgorithm.HS256,secret.bytes)
					.setIssuer(issuer).setAudience(audience)
					.claim("app_metadata",["operator_id":"123"])
					.compact()
			inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt))
		} catch (BadCredentialsException ex) {
			e1 = ex
		}

		assertNotNull(e1)
	}

	@Test
	public void testKnownOperator() {
		def uuid = UUID.randomUUID().toString()

		def o1 = new Operator()
		o1.firstName = "foo"
		o1.lastName = "baz"
		o1.email = "$uuid@cdonald.nz"
		operatorService.createOperator(o1)
		def shellUser = operatorService.inviteOperator(o1.id)

		def newEmail = "${UUID.randomUUID().toString()}@cdonald.nz"

		def newUser = userService.createShellUser(newEmail)

		Thread.sleep(5000)

		String jwt = Jwts.builder().setSubject(newUser.user_id).signWith(SignatureAlgorithm.HS256,secret.bytes)
				.setIssuer(issuer).setAudience(audience)
				.claim("app_metadata",["shell_user":shellUser.user_id ,"operator_id":o1.id,"registration_token":"123",token_valid_until:123])
				.compact()

		inviteAuthnComponent.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(jwt))

		Thread.sleep(5000)

		def updatedOperator = operatorService.getOperator(o1.id).orElseThrow { throw new Exception() }

		assertNotNull(updatedOperator.authUserId)
		assertEquals(newUser.user_id,updatedOperator.authUserId)

		def updatedUser = userService.getProfile(newUser.user_id).orElseThrow { throw new Exception () }
		assertTrue(updatedUser.app_metadata.signup_complete)
		assertNull(updatedUser.app_metadata.shell_user)
		assertNull(updatedUser.app_metadata.registration_token)
		assertNull(updatedUser.app_metadata.token_valid_until)
		assertEquals(updatedUser.email,newEmail as String)
		assertEquals(o1.id,updatedUser.app_metadata.operator_id)

		def updatedShellUser = userService.getProfile(shellUser.user_id).orElse(null)

		assertNull(updatedShellUser)

		userService.deleteUser(newUser.user_id)
	}

}
