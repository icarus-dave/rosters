package nz.net.cdonald.rosters.controllers.auth

import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import org.apache.camel.Exchange
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class WebConfigControllerAuthTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {
		def app = new SpringApplication(Application.class)
		context = app.run()

		def configEndpoint = "http://localhost:8080/api/webconfig"

		syncTest("Test of Get List no authn", configEndpoint)
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
					httpStatusCode(200))
	}


}