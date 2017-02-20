package nz.net.cdonald.rosters.services

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class MailServiceTest extends Assert {

	@Autowired
	MailService mailService

	@Value('${mailtrap.inbox}')
	String inbox

	@Value('${mailtrap.key}')
	String key

	def mailtrapClient = new RESTClient("https://mailtrap.io/api/v1/inboxes/")

	@Test
	public void sendMail() {
		def subject = UUID.randomUUID().toString()

		mailService.sendMessage("foo@cdonald.nz", "baz@cdonald.nz", subject, "<b>hello</b>","hello")

		Thread.sleep(5000)

		def resp = mailtrapClient.get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		def mail = (resp.data as List).find { it.subject == subject }

		assertNotNull(mail)
		assertEquals("foo@cdonald.nz",mail.to_email)
		assertEquals("baz@cdonald.nz",mail.from_email)
		assertEquals("<b>hello</b>",mail.html_body)
		assertEquals("hello",mail.text_body)
	}

	@Test
	public void sendTemplate() {
		def email = "${UUID.randomUUID().toString()}@cdonald.nz"

		mailService.sendTemplate(email,"invite",["registration_url":"foo","first_name":"abc"])

		Thread.sleep(5000)

		def resp = mailtrapClient.get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		def mail = (resp.data as List).find { it.to_email == email }

		assertNotNull(mail)
		assertTrue(mail.html_body.contains("foo"))
		assertTrue(mail.html_body.contains("abc"))

		assertTrue(mail.text_body.contains("foo"))
		assertTrue(mail.html_body.contains("abc"))
	}

	@Test
	public void sendMissingTemplate() {

		Exception e = null

		def email = "${UUID.randomUUID().toString()}@cdonald.nz"

		try {
			mailService.sendTemplate(email, "invite1", ["registration_url": "foo", "first_name": "abc"])
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)

		def resp = mailtrapClient.get(
				path: "$inbox/messages",
				headers: ["Authorization": "Token token=$key"],
				requestContentType: ContentType.JSON
		)

		def mail = (resp.data as List).find { it.to_email == email }

		assertNull(mail)
	}

	@After
	public void emptyInbox() {
		def resp = mailtrapClient.patch(
				path: '179796/clean',
				headers: ["Authorization": "Token token=f298016ebbb1612a728ee139eac421e8"],
				requestContentType: ContentType.JSON
		)
	}


}
