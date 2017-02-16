package nz.net.cdonald.rosters.controllers.auth

import com.avaje.ebean.EbeanServer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.UserService
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
		def auth0Service = context.getBean(UserService.class)

		def operatorEndpoint = "http://localhost:8080/api/operator"

		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString().toUpperCase()

		def auth0User = auth0Service.createUser(email,password)

		Operator o = generateOperator("zyx", "abc", "baz@foo.com")
		server.save(o)

		auth0Service.updateAppMetadata(auth0User.user_id,["operator_id":o.id])

		String token = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("scope","operator:unbound operator:modify").compact()

		String unauthz = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("scope","operator:unbound").compact()

		String tokenOp = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("app_metadata",["operator_id":o.id]).compact()

		String tokenOp2 = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("app_metadata",["operator_id":123]).compact()



		syncTest("Test of secured GET", operatorEndpoint)
				.request(headers(header("Authorization","Bearer $token")))
				.expectation(predicate { new JsonPathExpression("\$.data.length()").evaluate(it) == 1 });

		syncTest("Test of secured GET failure", operatorEndpoint)
				.expectsException()
				.expectation(httpStatusCode(401));

		syncTest("Test of secured POST", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST()),header("Authorization","Bearer $token"))
					,json('{ "first_name":"rrr","last_name":"nnn","email":"abde@asdd.com" }'))
				.expectation(jsonpath(".[?(@.version == 1)]"))

		syncTest("Test of secured POST failure", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST()))
					,json('{ "first_name":"rrr","last_name":"nnn","email":"abde@asdd.com" }'))
				.expectsException()
				//check AuthenticationExceptionEntryPoint is loading correctly
				.expectation(httpStatusCode(401),jsonpath(".[?(@.code == 401)]"))

		syncTest("Test of secured POST authz failure", operatorEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST()),header("Authorization","Bearer $unauthz"))
				,json('{ "first_name":"rrr","last_name":"nnn","email":"abde@asdd.com" }'))
				.expectsException()
				//no permissions to access this!
				.expectation(httpStatusCode(403),jsonpath(".[?(@.code == 403)]"))

		syncTest("Test of authorized GET", operatorEndpoint + "/" + o.id)
				.request(headers(header("Authorization","Bearer $token")))
				.expectation(jsonpath(".[?(@.first_name == 'zyx')]"));

		syncTest("Test of secured PUT", operatorEndpoint + "/" + o.id)
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $token"))
				,json('{ "id":' + o.id + ', "first_name":"rrr","last_name":"nnn","email":"baz@foo.com" }'))
				//version number starts at 0...
				.expectation(jsonpath(".[?(@.version == 1)]"))

		syncTest("Test of secured PUT authz failure", operatorEndpoint + "/" + o.id)
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $unauthz"))
				,json('{ "id":' + o.id + ', "first_name":"rrr","last_name":"nnn","email":"baz@foo.com" }'))
				.expectsException()
				.expectation(httpStatusCode(403),jsonpath(".[?(@.code == 403)]"))

		syncTest("Test of secured PUT authz with operator id matching", operatorEndpoint + "/" + o.id)
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $tokenOp"))
				,json('{ "id":' + o.id + ', "first_name":"rrr","last_name":"nnn","email":"baz@foo.com" }'))
				.expectation(httpStatusCode(200))

		syncTest("Test of secured PUT authz failure op mismatch", operatorEndpoint + "/" + o.id)
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $tokenOp2"))
					,json('{ "id":' + o.id + ', "first_name":"rrr","last_name":"nnn","email":"baz@foo.com" }'))
				.expectsException()
				.expectation(httpStatusCode(403),jsonpath(".[?(@.code == 403)]"))
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

