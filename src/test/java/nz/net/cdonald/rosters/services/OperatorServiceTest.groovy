package nz.net.cdonald.rosters.services

import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.net.cdonald.rosters.domain.Operator
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import javax.persistence.OptimisticLockException
import javax.persistence.PersistenceException

@RunWith(SpringRunner.class)
@SpringBootTest
class OperatorServiceTest extends Assert {

	@Autowired
	EbeanServer server;

	@Autowired
	OperatorService operatorService

	@Autowired
	UserService userService

	@Value('${mailtrap.inbox}')
	String inbox

	@Value('${mailtrap.key}')
	String key


	@After
	public void clear() {
		server.deleteAll(server.find(Operator.class).findList())
	}

	@Test
	public void testList() throws Exception {
		assertNotNull(server)

		def o1 = new Operator()
		o1.firstName = "Foo"
		o1.lastName = "ZBaz"
		o1.email = "foo@baz.com"
		server.save(o1)

		def o2 = new Operator()
		o2.firstName = "Baz"
		o2.lastName = "aFoo"
		o2.email = "foo@foo.com"
		o2.active = false
		server.save(o2)

		def list = operatorService.getOperators()
		assertEquals(2, list.size())

		assertTrue(list.get(0).firstName == "Baz")
		assertTrue(list.get(1).firstName == "Foo")
		assertTrue(o1.active)
		assertFalse(o2.active)
	}

	@Test
	public void testRetrieveOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		server.save(o1)

		def o = operatorService.getOperator(o1.id).orElse(null)
		assertEquals("abc", o.firstName)
		assertEquals("def", o.lastName)
		assertEquals("foo@baz.com", o.email)
	}

	@Test
	public void testCreateOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		o1.active = true

		def o2 = operatorService.createOperator(o1)
		assert o2.id != 0
		assertEquals(o2.firstName, "abc")
		assertTrue(o2.active)
	}

	@Test
	public void testUpdateOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		operatorService.createOperator(o1)

		def o2 = new Operator()
		o2.id = o1.id
		o2.firstName = "zyx"

		def o3 = operatorService.updateOperator(o2)

		assertEquals("zyx", o3.firstName)
		assertEquals("foo@baz.com", o3.email)
		assertEquals(1, o3.version)
	}

	@Test
	public void testUniqueEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.COM"

		operatorService.createOperator(o1)
		assertEquals("foo@baz.com", o1.email)

		def o2 = new Operator()
		o2.firstName = "abc"
		o2.lastName = "def"
		o2.email = "foo@BAZ.com"
		def ex
		try {
			operatorService.createOperator(o2)
		} catch (IllegalArgumentException e) {
			ex = e
		}
		assertNotNull(ex)

		o2.firstName = "abc"
		o2.lastName = "def"
		o2.email = "baz@FOO.com"
		operatorService.createOperator(o2)
		assertEquals("baz@foo.com", o2.email)

		o2.email = "foo@BAZ.com"
		def ex2
		try {
			operatorService.updateOperator(o2)
		} catch (IllegalArgumentException e) {
			ex2 = e
		}
		assertNotNull(ex2)
	}

	@Test
	public void testCreateSpaceyMail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "     foo@baz.com    "
		operatorService.createOperator(o1)
		assertEquals("abc", server.find(Operator.class).where().eq("email", "foo@baz.com").findUnique().firstName)
	}

	@Test
	public void testUpdateSpaceyMail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.org"
		operatorService.createOperator(o1)

		o1.email = "     baz@foo.org    "
		operatorService.updateOperator(o1)

		assertEquals("abc", server.find(Operator.class).where().eq("email", "baz@foo.org").findUnique().firstName)
	}

	@Test
	public void testUpdatedVersionNumber() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		operatorService.createOperator(o1)

		def o2 = new Operator()
		o2.id = o1.id
		o2.firstName = "def"
		o2.lastName = "abc"
		o2.version = 55
		o2.email = "foo@baz.com"

		def ex
		try {
			operatorService.updateOperator(o2)
		} catch (OptimisticLockException e) {
			ex = e
		}

		assertNotNull(ex)
	}

	@Test
	public void invalidEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "invalid"

		def ex
		try {
			operatorService.createOperator(o1)
		} catch (IllegalArgumentException e) {
			ex = e
		}

		assertNotNull(ex)

		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		o1.email = "invalid"
		def e2
		try {
			operatorService.updateOperator(o1)
		} catch (IllegalArgumentException e) {
			e2 = e
		}

		assertNotNull(e2)

	}

	@Test
	public void invalidNoEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"

		def ex
		try {
			operatorService.createOperator(o1)
		} catch (IllegalArgumentException e) {
			ex = e
		}

		assertNotNull(ex)

		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		o1.email = null
		def e2
		try {
			operatorService.updateOperator(o1)
		} catch (PersistenceException e) {
			e2 = e
		}

		assertNotNull(e2)
	}

	@Test
	public void testFindByEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		def o2 = operatorService.findByEmail("foo@baz.com")

		assertEquals(o1.id, o2.id)

		def o3 = operatorService.findByEmail("baz@foo.com")
		assertNull(o3)
	}

	@Test
	public void testFindNullOperator() throws Exception {
		def o = operatorService.findByEmail(null)
		assertNull(o)
	}

	@Test
	public void authzOperatorMatch() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		String operatorToken = Jwts.builder().setSubject("123")
				.claim("app_metadata", ["operator_id": o1.id]).compact()

		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)

		assertTrue(operatorService.authzOperatorUpdate(o1.id, authn))
		assertFalse(operatorService.authzOperatorUpdate(456, authn))
	}

	@Test
	public void authzOperatorMatchAsString() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		String operatorToken = Jwts.builder().setSubject("123")
				.claim("app_metadata", ["operator_id": "${o1.id}" as String]).compact()

		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)

		assertTrue(operatorService.authzOperatorUpdate(o1.id, authn))
		assertFalse(operatorService.authzOperatorUpdate(456, authn))
	}

	@Test
	public void authzNoMetadata() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		String operatorToken = Jwts.builder().setSubject("123").compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(operatorService.authzOperatorUpdate(123, authn))
	}

	@Test
	public void authzNoOperatorId() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		String operatorToken = Jwts.builder().setSubject("123").claim("app_metadata", [:]).compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(operatorService.authzOperatorUpdate(123, authn))
	}

	@Test
	public void testInvite() throws Exception {
		def uuid = UUID.randomUUID().toString()

		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "$uuid@cdonald.nz"
		operatorService.createOperator(o1)

		def inviteProfile = operatorService.inviteOperator(o1.id)

		def profile = userService.getOperatorProfile(o1.id).orElseThrow { new Exception() }

		assertEquals(o1.id,profile.app_metadata.operator_id)
		assertNotNull(profile.app_metadata.registration_token)
		assertNotNull(profile.app_metadata.token_valid_until)

		def mailtrapClient = new RESTClient("https://mailtrap.io/api/v1/inboxes/")

		def resp = mailtrapClient.get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		def mail = (resp.data as List).find { it.to_email == o1.email }
		assertNotNull(mail)
		assertTrue(mail.html_body.contains(o1.firstName))
		assertTrue(mail.text_body.contains(o1.firstName))
		assertTrue(mail.html_body.contains(profile.app_metadata.registration_token))
		assertTrue(mail.text_body.contains(profile.app_metadata.registration_token))

		Exception e = null
		try {
			operatorService.inviteOperator(o1.id)
		} catch (Exception ex) {
			e = ex
		}
		assertNotNull(e)

		userService.deleteUser(profile.user_id)

		//test re-send invitation
		mailtrapClient.patch(
				path: "$inbox/clean",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)
	}

	@Test
	public void testInviteUpdate() {
		operatorService.tokenPurgatory = 0

		def uuid = UUID.randomUUID().toString()

		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "$uuid@cdonald.nz"
		operatorService.createOperator(o1)

		def inviteProfile = operatorService.inviteOperator(o1.id)

		def profile = userService.getOperatorProfile(o1.id).orElseThrow { new Exception() }

		assertEquals(o1.id,profile.app_metadata.operator_id)
		assertNotNull(profile.app_metadata.registration_token)
		assertNotNull(profile.app_metadata.token_valid_until)

		Thread.sleep(2000) //just to be sure

		def profile2 = operatorService.inviteOperator(o1.id)

		assertTrue(profile2.app_metadata.registration_token != profile.app_metadata.registration_token)
		assertTrue(profile2.app_metadata.token_valid_until > profile.app_metadata.token_valid_until)
		assertTrue(profile2.app_metadata.operator_id == profile.app_metadata.operator_id)

		userService.deleteUser(profile.user_id)

		def resp = new RESTClient("https://mailtrap.io/api/v1/inboxes/").get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		def mail = (resp.data as List).findAll { it.to_email == o1.email }.sort { it.created_at }
		assertNotNull(mail)
		assertEquals(2,mail.size())
		assertTrue(mail[0].html_body.contains(o1.firstName))
		assertTrue(mail[0].text_body.contains(o1.firstName))
		assertTrue(mail[0].html_body.contains(profile.app_metadata.registration_token))
		assertTrue(mail[0].text_body.contains(profile.app_metadata.registration_token))

		assertTrue(mail[1].html_body.contains(o1.firstName))
		assertTrue(mail[1].text_body.contains(o1.firstName))
		assertTrue(mail[1].html_body.contains(profile2.app_metadata.registration_token))
		assertTrue(mail[1].text_body.contains(profile2.app_metadata.registration_token))

		new RESTClient("https://mailtrap.io/api/v1/inboxes/").patch(
				path: "$inbox/clean",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)
	}

	@Test
	public void testInviteUnknownOperator() {
		Exception e = null

		try {
			operatorService.inviteOperator(123)
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)

		def profile = userService.getOperatorProfile(123).orElse(null)

		assertNull(profile)

		Thread.sleep(5000)

		def resp = new RESTClient("https://mailtrap.io/api/v1/inboxes/").get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		assertEquals(0,resp.data.size())
	}

	@Test
	public void testInviteSignedUpOperator() {

		def uuid = UUID.randomUUID().toString()

		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "$uuid@cdonald.nz"
		operatorService.createOperator(o1)

		def op = operatorService.inviteOperator(o1.id)

		Thread.sleep(5000)

		new RESTClient("https://mailtrap.io/api/v1/inboxes/").patch(
				path: "$inbox/clean",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		userService.updateAppMetadata(op.user_id,["signup_complete":true])

		Thread.sleep(5000) //auth0 is eventually consistent

		def newProfile = operatorService.inviteOperator(o1.id)

		assertNull(newProfile)

		def resp = new RESTClient("https://mailtrap.io/api/v1/inboxes/").get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		assertEquals(0,resp.data.size())

		new RESTClient("https://mailtrap.io/api/v1/inboxes/").patch(
				path: "$inbox/clean",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		userService.deleteUser(op.user_id)
	}

}


