package nz.net.cdonald.rosters.controllers.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.avaje.ebean.EbeanServer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.Auth0Service
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.jsonpath.JsonPathExpression
import org.junit.AfterClass
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class OperatorControllerAuthTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {
		def app = new SpringApplication(Application.class)
		context = app.run()

		def server = context.getBean(EbeanServer.class)
		def auth0Service = context.getBean(Auth0Service.class)

		def operatorEndpoint = "http://localhost:8080/api/operator"

		String token = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("app_metadata",["permissions":["unbound_operator"]]).compact()

		server.save(generateOperator("zyx", "abc", "baz@foo.com"))

		syncTest("Test of secured GET", operatorEndpoint)
				.request(headers(header("Authorization","Bearer $token")))
				.expectation(predicate { new JsonPathExpression("\$.data.length()").evaluate(it) == 1 });

		syncTest("Test of secured GET failure", operatorEndpoint)
				.expectsException()
				.expectation(httpStatusCode(401));

		syncTest("Test of secured POST", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST()),header("Authorization","Bearer $token"))
					,json('{ "firstName":"rrr","lastName":"nnn","email":"abde@asdd.com" }'))
				.expectation(jsonpath(".[?(@.version == 1)]"))

		syncTest("Test of secured POST failure", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST()))
					,json('{ "firstName":"rrr","lastName":"nnn","email":"abde@asdd.com" }'))
				.expectsException()
				//check AuthenticationExceptionEntryPoint is loading correctly
				.expectation(httpStatusCode(401),jsonpath(".[?(@.code == 401)]"))

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

