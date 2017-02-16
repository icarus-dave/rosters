package nz.net.cdonald.rosters.services

import groovyx.net.http.HttpResponseException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/*
These are effectively integration tests
*/
@RunWith(SpringRunner.class)
@SpringBootTest
class UserServiceTest extends Assert {

	@Autowired
	UserService userService

	@Test
	public void testGetProfile() {
		def profile = userService.getProfile("google-apps|davidm@cdonald.net.nz")
		assertEquals("davidm@cdonald.net.nz",profile.email)
		assertTrue(profile.email_verified)
		assertEquals("baz",profile.app_metadata.foo)
	}

	@Test
	public void testGetUnknownProfile() {
		def e = null

		try {
			def profile = userService.getProfile("123|123")
		} catch (HttpResponseException ex) {
			e = ex
		}

		assertNotNull(e);
		assertEquals(404,e.response.getStatus())
	}

	@Test
	public void testUpdateMetadata() {
		def uuid = UUID.randomUUID().toString().toUpperCase();
		//set the value, retrieve it, and check it's what we expect
		userService.updateAppMetadata("google-apps|davidm@cdonald.net.nz",["test":uuid])

		def profile = userService.getProfile("google-apps|davidm@cdonald.net.nz")
		assertEquals(uuid,profile.app_metadata.test)
	}

	@Test
	public void updateAppMetadataJwtNoExistingMetadata() {
		String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,userService.clientSecret.bytes)
				.setIssuer(userService.clientId).setAudience(userService.audience).compact()

		def newJwt = userService.updateAppMetadataJwt(jwt,userService.clientSecret,["foo":"baz", "moo":[1, 2]])
		def newParsedJwt = Jwts.parser().setSigningKey(userService.clientSecret.bytes).parseClaimsJws(newJwt)

		assertEquals("baz",newParsedJwt.getBody().get("app_metadata").foo)
		assertEquals(2,newParsedJwt.getBody().get("app_metadata").moo.size())
	}

	@Test
	public void updateAppMetadataJwtExistingMetadata() {
		String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,userService.clientSecret.bytes)
				.setIssuer(userService.clientId).setAudience(userService.audience).claim("app_metadata",["foo":"foo", "abc":"def"]).compact()

		def newJwt = userService.updateAppMetadataJwt(jwt,userService.clientSecret,["foo":"baz", "moo":[1, 2]])
		def newParsedJwt = Jwts.parser().setSigningKey(userService.clientSecret.bytes).parseClaimsJws(newJwt)

		assertEquals("baz",newParsedJwt.getBody().get("app_metadata").foo)
		assertEquals(2,newParsedJwt.getBody().get("app_metadata").moo.size())
		assertEquals("def",newParsedJwt.getBody().get("app_metadata").abc)
	}

	@Test
	public void testCreateReadDeleteUser() {
		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString().toUpperCase()

		def auth0User = userService.createUser(email,password)

		assertNotNull(auth0User.user_id)
		assertEquals(email,auth0User.email)
		assertEquals(true,auth0User.email_verified);

		def auth0UserGot = userService.getProfile(auth0User.user_id)

		assertNotNull(auth0UserGot)
		assertEquals(email,auth0UserGot.email)
		assertEquals(true,auth0UserGot.email_verified);

		userService.deleteUser(auth0User.user_id)

		HttpResponseException e
		try {
			userService.getProfile(auth0User.user_id)
		} catch(HttpResponseException ex) {
			e = ex
		}

		assertNotNull(e)
		assertEquals(404,e.getStatusCode())
	}

	@Test
	public void testCreateUser() {


	}

	@Test
	public void testUpdateUser() {

	}

}
