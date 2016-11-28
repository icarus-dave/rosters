package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import org.apache.camel.jsonpath.JsonPathExpression
import nz.ac.auckland.morc.MorcTestBuilder
import nz.ac.auckland.morc.specification.SyncOrchestratedTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.Predicate
import org.junit.AfterClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Service

class OperatorControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {

		context = SpringApplication.run(Application.class);
		def server = context.getBean(EbeanServer.class)

		/*syncTest("Test of Empty List","http://localhost:8080/operator")
				.expectation(regex("\\[\\]"))

		Operator o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "zyx"
		o1.email = "foo@baz.com"

		syncTest("Test of Populated List", "http://localhost:8080/operator")
				.request (process { server.save(generateOperator()) })
				.expectation(jsonpath(".[?(@.firstName == 'abc')]"),
				jsonpath(".[?(@.lastName == 'zyx')]"), jsonpath(".[?(@.email == 'foo@baz.com')]"))
			.addPart("http://localhost:8080/operator")
				.request (process { server.save(generateOperator("zyx","abc","baz@foo.com")) } )
				.expectation(predicate { new JsonPathExpression("\$.length()").evaluate(it) == 2 },
				jsonpath(".[?(@.firstName == 'abc')]"), jsonpath(".[?(@.lastName == 'zyx')]"), jsonpath(".[?(@.email == 'foo@baz.com')]"),
				jsonpath(".[?(@.firstName == 'zyx')]"),jsonpath(".[?(@.lastName == 'abc')]"), jsonpath(".[?(@.email == 'baz@foo.com')]"))
		*/

		syncTest("Test of GET", "http://localhost:8080/operator/123456")
				.expectsException()
				.expectation(httpStatusCode(404),bodyAs(null))
			/*.addPart("http://localhost:8080/operator")
				.request (process { server.save(generateOperator("zyx","abc","baz@foo.com")) } )
				.expectation(predicate { new JsonPathExpression("\$.length()").evaluate(it) == 2 },
				jsonpath(".[?(@.firstName == 'abc')]"), jsonpath(".[?(@.lastName == 'zyx')]"), jsonpath(".[?(@.email == 'foo@baz.com')]"),
				jsonpath(".[?(@.firstName == 'zyx')]"),jsonpath(".[?(@.lastName == 'abc')]"), jsonpath(".[?(@.email == 'baz@foo.com')]"))

			*/


		/*
			list: 0 in list, lots in list
			get: not found, found, invalid id (string etc)
			create: normal, provide ID and other stuff (teams?)
			update: not found, found
			invalid operation
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
				it.call();
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

