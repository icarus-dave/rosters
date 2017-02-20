package nz.net.cdonald.rosters.services

import groovyx.net.http.HttpResponseException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.net.cdonald.rosters.domain.Operator
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

	@Autowired
	OperatorService operatorService


	@Test
	public void testGetProfile() {
		def profile = userService.getProfile("google-oauth2|102641659505100008616").orElseThrow { new Exception() }
		assertEquals("david.a.macdonald@createdigital.com.au",profile.email)
		assertTrue(profile.email_verified)
		assertEquals("baz",profile.app_metadata.foo)
	}

	@Test
	public void testGetUnknownProfile() {
		def profile = userService.getProfile("123|123").orElse(null)

		assertNull(profile)
	}

	@Test
	public void testGetProfileBadAuthnz() {
		userService.clientSecret = "123"

		Exception e = null
		try {
			def profile = userService.getProfile("google-oauth2|102641659505100008616").orElseThrow { new Exception() }
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
		assertTrue(e.message.contains("401"))
	}

	@Test
	public void testGetOperatorProfile() {
		def profile = userService.getOperatorProfile(-1).orElseThrow { new Exception() }

		assertEquals("google-oauth2|102641659505100008616",profile["user_id"])
		assertEquals("baz",profile["app_metadata"]["foo"])
	}

	@Test
	public void testGetOperatorProfileNotFound() {
		def profile = userService.getOperatorProfile(0).orElse(null)
		assertNull(profile)
	}

	@Test
	public void testGetOperatorProfileMultiple() {
		Exception e = null
		try {
			userService.getOperatorProfile(-2).orElse(null)
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testGetOperatorProfileAuthnError() {
		userService.clientSecret = "123"
		Exception e = null
		try {
			userService.getOperatorProfile(1).orElse(null)
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testUpdateMetadataMissingUser() {
		Exception e = null
		try {
			def uuid = UUID.randomUUID().toString().toUpperCase();
			//set the value, retrieve it, and check it's what we expect
			userService.updateAppMetadata("123", ["test": uuid])
		} catch (Exception ex) {
			e = ex
		}
		assertNotNull(e)
	}

	@Test
	public void testUpdateMetadata() {
		def uuid = UUID.randomUUID().toString().toUpperCase();
		//set the value, retrieve it, and check it's what we expect
		userService.updateAppMetadata("google-oauth2|102641659505100008616",["test":uuid])

		def profile = userService.getProfile("google-oauth2|102641659505100008616").orElseThrow { new Exception() }
		assertEquals(uuid,profile.app_metadata.test)
	}

	@Test
	public void testCreateShellUserAndDelete() {
		def uuid = UUID.randomUUID().toString().toUpperCase();
		def user = userService.createShellUser("$uuid@cdonald.nz",["abc":"123"])

		assertEquals("123",user["app_metadata"]["abc"])
		assertTrue(user["email_verified"])

		userService.deleteUser(user.user_id)

		def otherProfile = userService.getProfile(user.user_id).orElse(null)
		assertNull(otherProfile)
	}

	@Test
	public void testCreateShellUserExistingEmail() {
		Exception e = null
		try {
			def user = userService.createShellUser("foo@cdonald.nz", ["abc": "123"])
		} catch (Exception ex) {
			e = ex
		}
		assertNotNull(e)
	}

	@Test
	public void testDeleteUserMissing() {
		Exception e = null
		try {
			def user = userService.deleteUser("123")
		} catch (Exception ex) {
			e = ex
		}
		assertNotNull(e)
	}

}
