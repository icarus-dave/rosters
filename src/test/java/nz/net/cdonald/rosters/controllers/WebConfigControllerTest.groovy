package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import org.apache.camel.Exchange
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class WebConfigControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {

		context = SpringApplication.run(Application.class);

		def configEndpoint = "http://localhost:8080/api/webconfig"

		syncTest("Test of Get List", configEndpoint)
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				jsonpath(".[?(@.foo == 'baz')]"),
				jsonpath(".[?(@.baz == 'foo')]"))

	}


}
