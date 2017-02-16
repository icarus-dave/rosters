package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.context.MapValueResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Service
class MailService {

	static final Logger logger = LoggerFactory.getLogger(MailService.class)

	@Autowired
	ContentService contentService

	@Value('${mail.smtp.host}')
	String host

	@Value('${mail.smtp.port}')
	String port

	@Value('${mail.smtp.user}')
	String user

	@Value('${mail.smtp.pass}')
	String password

	def sendTemplate(String address, String templateId, data = [:]) {
		def emailContent = contentService.getEntityForName("email",templateId)

		Handlebars handleBars = new Handlebars()

		def htmlTemplate = handleBars.compileInline(emailContent["htmlContent"])
		def textTemplate = handleBars.compileInline(emailContent["textContent"])

		def html = htmlTemplate.apply(Context.newBuilder(data).resolver(MapValueResolver.INSTANCE).build())
		def text = textTemplate.apply(Context.newBuilder(data).resolver(MapValueResolver.INSTANCE).build())

		def from = emailContent["from"]
		def subject = emailContent["subject"]

		logger.info("Sending template {} to {}",emailContent["name"],address)
		sendMessage(address,from,subject,html,text)
	}

	def sendMessage(String to, String from, String subject, String html, String text) {
		Properties properties = System.getProperties()
		properties.setProperty("mail.smtp.host",host)
		properties.setProperty("mail.smtp.port",port)
		properties.put("mail.smtp.starttls.enable","true")
		properties.put("mail.smtp.auth", "true")
		properties.put("mail.smtps.ssl.trust", "*")

		Session session = Session.getDefaultInstance(properties)

		MimeMessage message = new MimeMessage(session)
		message.setFrom(new InternetAddress(from))
		message.addRecipient(Message.RecipientType.TO,new InternetAddress(to))
		message.setSubject(subject)

		MimeBodyPart htmlPart = new MimeBodyPart()
		htmlPart.setContent(html,"text/html")
		MimeBodyPart textPart = new MimeBodyPart()
		textPart.setContent(text,"text/plain")

		def mp = new MimeMultipart("alternative")
		mp.addBodyPart(textPart)
		mp.addBodyPart(htmlPart)

		message.setContent(mp)

		Transport.send(message,user,password)
		logger.info("Sent message {} to email address {} from {}",subject,to,from)
	}

}

