package nz.net.cdonald.rosters.services

import groovyx.net.http.HttpResponseException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

/*
These are effectively integration tests
*/
@RunWith(SpringRunner.class)
@SpringBootTest
class Auth0ServiceTest extends Assert {

	@Autowired
	Auth0Service auth0Service

	@Test
	public void testGetProfile() {
		def profile = auth0Service.getProfile("google-apps|davidm@cdonald.net.nz")
		assertEquals("davidm@cdonald.net.nz",profile.email)
		assertTrue(profile.email_verified)
		assertEquals("baz",profile.app_metadata.foo)
	}

	@Test
	public void testGetUnknownProfile() {
		def e = null

		try {
			def profile = auth0Service.getProfile("123|123")
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
		auth0Service.updateAppMetadata("google-apps|davidm@cdonald.net.nz",["test":uuid])

		def profile = auth0Service.getProfile("google-apps|davidm@cdonald.net.nz")
		assertEquals(uuid,profile.app_metadata.test)
	}

	@Test
	public void updateAppMetadataJwtNoExistingMetadata() {
		String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,auth0Service.clientSecret.bytes)
				.setIssuer(auth0Service.clientId).setAudience(auth0Service.audience).compact()

		def newJwt = auth0Service.updateAppMetadataJwt(jwt,auth0Service.clientSecret,["foo":"baz","moo":[1,2]])
		def newParsedJwt = Jwts.parser().setSigningKey(auth0Service.clientSecret.bytes).parseClaimsJws(newJwt)

		assertEquals("baz",newParsedJwt.getBody().get("app_metadata").foo)
		assertEquals(2,newParsedJwt.getBody().get("app_metadata").moo.size())
	}

	@Test
	public void updateAppMetadataJwtExistingMetadata() {
		String jwt = Jwts.builder().setSubject("123|123").signWith(SignatureAlgorithm.HS256,auth0Service.clientSecret.bytes)
				.setIssuer(auth0Service.clientId).setAudience(auth0Service.audience).claim("app_metadata",["foo":"foo","abc":"def"]).compact()

		def newJwt = auth0Service.updateAppMetadataJwt(jwt,auth0Service.clientSecret,["foo":"baz","moo":[1,2]])
		def newParsedJwt = Jwts.parser().setSigningKey(auth0Service.clientSecret.bytes).parseClaimsJws(newJwt)

		assertEquals("baz",newParsedJwt.getBody().get("app_metadata").foo)
		assertEquals(2,newParsedJwt.getBody().get("app_metadata").moo.size())
		assertEquals("def",newParsedJwt.getBody().get("app_metadata").abc)
	}

	@Test
	public void testCreateReadDeleteUser() {
		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString().toUpperCase()

		def auth0User = auth0Service.createUser(email,password)

		assertNotNull(auth0User.user_id)
		assertEquals(email,auth0User.email)
		assertEquals(true,auth0User.email_verified);

		def auth0UserGot = auth0Service.getProfile(auth0User.user_id)

		assertNotNull(auth0UserGot)
		assertEquals(email,auth0UserGot.email)
		assertEquals(true,auth0UserGot.email_verified);

		auth0Service.deleteUser(auth0User.user_id)

		HttpResponseException e
		try {
			auth0Service.getProfile(auth0User.user_id)
		} catch(HttpResponseException ex) {
			e = ex
		}

		assertNotNull(e)
		assertEquals(404,e.getStatusCode())
	}

}
