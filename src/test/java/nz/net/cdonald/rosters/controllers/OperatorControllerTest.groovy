package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.jsonpath.JsonPathExpression
import org.junit.AfterClass
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class OperatorControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {

		context = SpringApplication.run(Application.class);
		def server = context.getBean(EbeanServer.class)

		syncTest("Test of Empty List", "http://localhost:8080/operator")
				.expectation(regex("\\[\\]"),headers(header(Exchange.CONTENT_TYPE,"application/json;charset=UTF-8")))

		Operator o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "zyx"
		o1.email = "foo@baz.com"

		syncTest("Test of Populated List", "http://localhost:8080/operator")
				.request(process { server.save(generateOperator()) })
				.expectation(headers(header(Exchange.CONTENT_TYPE,"application/json;charset=UTF-8")),
				jsonpath(".[?(@.firstName == 'abc')]"),
				jsonpath(".[?(@.lastName == 'zyx')]"), jsonpath(".[?(@.email == 'foo@baz.com')]"))
			.addPart("http://localhost:8080/operator")
				.request(process { server.save(generateOperator("zyx", "abc", "baz@foo.com")) })
				.expectation(predicate { new JsonPathExpression("\$.length()").evaluate(it) == 2 },
				jsonpath(".[?(@.firstName == 'abc')]"), jsonpath(".[?(@.lastName == 'zyx')]"), jsonpath(".[?(@.email == 'foo@baz.com')]"),
				jsonpath(".[?(@.firstName == 'zyx')]"), jsonpath(".[?(@.lastName == 'abc')]"), jsonpath(".[?(@.email == 'baz@foo.com')]"))

		def o = generateOperator("1", "2", "1@2.com")

		syncTest("Test of GET", "http://localhost:8080/operator/123456")
				.expectsException()
				.expectation(httpStatusCode(404), jsonpath(".[?(@.code == '404')]"), jsonpath(".[?(@.message == 'Operator not found')]"))
				.request(process { server.save(o) })
			.addPart("http://localhost:8080/operator")
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${o.id}") })
				.expectation(headers(header(Exchange.CONTENT_TYPE,"application/json;charset=UTF-8")),jsonpath(".[?(@.firstName == '1')]"),
				jsonpath(".[?(@.lastName == '2')]"), jsonpath(".[?(@.email == '1@2.com')]"))

		syncTest("Test of GET Invalid URL", "http://localhost:8080/operator/abc")
				.expectsException()
				.expectation(httpStatusCode(400), headers(header(Exchange.CONTENT_TYPE,"application/json;charset=UTF-8")),
				jsonpath(".[?(@.code == '400')]"), jsonpath(".[?(@.message == 'Invalid parameter provided')]"))

		syncTest("Create Operator","http://localhost:8080/operator")
			.request(headers(header(Exchange.HTTP_METHOD,POST())),json('{ "firstName":"rrr","lastName":"nnn","email":"abde@asdd" }'))
			.expectation(predicate { new JsonPathExpression("@.id").evaluate(it) instanceof Integer })

		/*
			todo: active, teams
			list: 0 in list, lots in list
			get: not found, found, invalid id (string etc)
			create: normal, provide ID and other stuff (teams?)
			update: not found, found
			invalid operation (what does this mean?)
			invalid http method
			team members
		 */

	}

	def generateOperator(firstName = "abc", lastName = "zyx", email = "foo@baz.com") {
		Operator o1 = new Operator()
		o1.firstName = firstName
		o1.lastName = lastName
		o1.email = email

		return o1
	}


	@AfterClass
	public static void finish() {
		context.close();

	}

	def process = {
		return new Processor() {
			public void process(Exchange e) {
				it.call(e);
			}
		}
	}

	def predicate = {
		return new Predicate() {
			public boolean matches(Exchange e) {
				it.call(e);
			}
		}
	}
}

