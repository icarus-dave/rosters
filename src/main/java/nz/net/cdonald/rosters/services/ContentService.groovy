package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDAHttpException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ContentService {

	static final Logger logger = LoggerFactory.getLogger(ContentService.class)

	CDAClient client

	@Value('${contentful.locale:en-NZ}')
	String locale = "en-NZ"

	public ContentService(@Value('${contentful.space}') String space, @Value('${contentful.token}') String token) {
		client = CDAClient.builder()
				.setSpace(space)
				.setToken(token)
				.build()
	}

	def getEntityForName(String contentType, String name) {
		try {
			def result = client.fetch(CDAEntry.class)
					.where("content_type", contentType)
					.where("fields.name", name)
					.all().entries().values()[0]
			if (result == null) throw new Exception("Unable to find content $name for type $contentType")
			return result.rawFields().collectEntries { key, value -> [key,value[locale]]}
		} catch (CDAHttpException ex) {
			throw new Exception("Error retrieving Contentful entity $name - ${ex.responseCode()}:${ex.responseMessage()}",ex)
		}
	}
}
