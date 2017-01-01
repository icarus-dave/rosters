package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.jsonpath.JsonPathExpression
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class WebConfigControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {

		def app = new SpringApplication(Application.class)
		app.setAdditionalProfiles("noauth")
		context = app.run()

		def configEndpoint = "http://localhost:8080/api/webconfig"

		syncTest("Test of Get List", configEndpoint)
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				predicate { new JsonPathExpression("\$.length()").evaluate(it) == 2 },
				jsonpath(".[?(@.foo == 'baz')]"),
				jsonpath(".[?(@.baz == 'foo')]"))

	}

	def predicate = {
		return new Predicate() {
			public boolean matches(Exchange e) {
				it.call(e);
			}
		}
	}

}
