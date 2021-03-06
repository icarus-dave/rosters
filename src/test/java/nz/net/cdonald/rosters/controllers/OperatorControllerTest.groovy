package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import org.apache.camel.Exchange
import org.apache.camel.ExpressionEvaluationException
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.jsonpath.JsonPathExpression
import org.junit.AfterClass
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class OperatorControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {
		def app = new SpringApplication(Application.class)
		app.setAdditionalProfiles("noauth")
		context = app.run()

		def server = context.getBean(EbeanServer.class)

		def operatorEndpoint = "http://localhost:8080/api/operator"

		syncTest("Test of Empty List", operatorEndpoint)
				.expectation(json('{"data":[]}'), headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")))

		Operator o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "zyx"
		o1.email = "foo@baz.com"

		syncTest("Test of Populated List", operatorEndpoint)
				.request(process { server.save(generateOperator()) })
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
					jsonpath(".data.[?(@.first_name == 'abc')]"),
					jsonpath(".data.[?(@.last_name == 'zyx')]"), jsonpath(".data.[?(@.email == 'foo@baz.com')]"))
			.addPart(operatorEndpoint)
				.request(process { server.save(generateOperator("zyx", "abc", "baz@foo.com")) })
				.expectation(predicate { new JsonPathExpression("\$.data.length()").evaluate(it) == 2 },
					jsonpath(".data.[?(@.first_name == 'abc')]"), jsonpath(".data.[?(@.last_name == 'zyx')]"), jsonpath(".data.[?(@.email == 'foo@baz.com')]"),
					jsonpath(".data.[?(@.first_name == 'zyx')]"), jsonpath(".data.[?(@.last_name == 'abc')]"), jsonpath(".data.[?(@.email == 'baz@foo.com')]"))

		def o = generateOperator("1", "2", "1@2.com")


		syncTest("Test of GET", "$operatorEndpoint/123456")
				.expectsException()
				.expectation(httpStatusCode(404), jsonpath(".[?(@.code == '404')]"), jsonpath(".[?(@.message == 'Operator not found')]"))
				.request(process { server.save(o) })
			.addPart(operatorEndpoint)
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${o.id}") })
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")), jsonpath(".[?(@.first_name == '1')]"),
					jsonpath(".[?(@.last_name == '2')]"), jsonpath(".[?(@.email == '1@2.com')]"))

		syncTest("Test of GET Invalid URL", "$operatorEndpoint/abc")
				.expectsException()
				.expectation(httpStatusCode(400), headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				jsonpath(".[?(@.code == '400')]"))

		def i

		syncTest("Create and Update Operator", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{ "first_name":"rrr","last_name":"nnn","email":"abde@asdd.com" }'))
				.expectation(predicate { i = new JsonPathExpression("@.id").evaluate(it); i instanceof Integer },
				jsonpath(".[?(@.version == 1)]"),headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")))
			.addPart(operatorEndpoint)
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${i}") })
				.expectation(jsonpath(".[?(@.last_name == 'nnn')]"), jsonpath(".[?(@.active == true)]"),jsonpath(".[?(@.version == 1)]"))
			.addPart(operatorEndpoint)
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${i}") },
				headers(header(Exchange.HTTP_METHOD, PUT())), process { json('{ "id": ' + i + ', "first_name":"nnn","last_name":"rrr","email":"abde@asdd.com","active":false,"version":1 }').process(it) })
				.expectation(jsonpath(".[?(@.first_name == 'nnn')]"), jsonpath(".[?(@.last_name == 'rrr')]"),
				jsonpath(".[?(@.version == 2)]"),jsonpath(".[?(@.email == 'abde@asdd.com')]"), jsonpath(".[?(@.active == false)]"))


		syncTest("Create operator ID specified", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{ "id": 45678, "first_name":"rrr","last_name":"nnn","email":"asdf@asdf.com","version":555}'))
				.expectation(predicate { def j = new JsonPathExpression("@.id").evaluate(it); j instanceof Integer && j != 45678 },
				jsonpath(".[?(@.version == 1)]"),
				//hacky way to check non-existence of element
				predicate { try { new JsonPathExpression("@.whenCreated").evaluate(it) } catch (ExpressionEvaluationException e) { return true }; return false },
				headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")))


		syncTest("Update operator not known", "$operatorEndpoint/12345")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())))
				.expectsException()
				.expectation(httpStatusCode(400))

		syncTest("Update operator not all fields specified", operatorEndpoint)
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${i}") },
				headers(header(Exchange.HTTP_METHOD, PUT())), json('{ "id":"123","first_name":"nnn1","email":"abde@asdd.com"}'))
				.expectation(jsonpath(".[?(@.first_name == 'nnn1')]"), jsonpath(".[?(@.last_name == 'rrr')]")
				,jsonpath(".[?(@.email == 'abde@asdd.com')]"), jsonpath(".[?(@.active == false)]"))

		syncTest("Existing email create fail", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{ "first_name":"rrr","last_name":"nnn","email":"abde@asdd.com" }'))
				.expectsException()
				.expectation(httpStatusCode(400))


		syncTest("Existing email create fail", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
				.expectsException()
				.expectation(httpStatusCode(405))


		syncTest("Create operator with invalid email", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{ "first_name":"rrr","last_name":"nnn","email":"invalid" }'))
				.expectsException()
				.expectation(httpStatusCode(400))

		def j

		syncTest("Update operator with dumb version number", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{ "first_name":"rrr","last_name":"nnn","email":"test@test.com" }'))
				.expectation(jsonpath(".[?(@.version == 1)]"),predicate { j = new JsonPathExpression("@.id").evaluate(it); j instanceof Integer })
			.addPart(operatorEndpoint)
				.request(process { it.getIn().setHeader(Exchange.HTTP_PATH, "/${j}") }, headers(header(Exchange.HTTP_METHOD, PUT())),
				process { json('{ "id": ' + j + ', "first_name":"nnn","last_name":"rrr","email":"test@test.com","active":false,"version":9999 }').process(it) })
				.expectsException()
				.expectation(httpStatusCode(409))


		//invite operator
		//invite operator missing
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

