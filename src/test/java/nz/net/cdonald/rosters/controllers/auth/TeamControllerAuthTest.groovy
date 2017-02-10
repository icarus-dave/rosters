package nz.net.cdonald.rosters.controllers.auth

import com.avaje.ebean.EbeanServer
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.services.Auth0Service
import nz.net.cdonald.rosters.services.OperatorService
import nz.net.cdonald.rosters.services.TeamService
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.jsonpath.JsonPathExpression
import org.junit.AfterClass
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class TeamControllerAuthTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {
		def app = new SpringApplication(Application.class)
		context = app.run()

		def server = context.getBean(EbeanServer.class)
		def auth0Service = context.getBean(Auth0Service.class)
		def teamService = context.getBean(TeamService.class)
		def operatorService = context.getBean(OperatorService.class)

		def teamEndpoint = "http://localhost:8080/api/team"

		def email = UUID.randomUUID().toString().toLowerCase() + "@cdonald.nz"
		def password = UUID.randomUUID().toString().toUpperCase()

		def auth0User = auth0Service.createUser(email,password)

		Operator o = generateOperator("zyx", "abc", "baz@foo.com")
		server.save(o)

		Team t = new Team()
		t.name = "foo"
		t.teamLead = o
		teamService.createTeam(t)

		auth0Service.updateAppMetadata(auth0User.user_id,["operator_id":o.id])

		String token = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("scope","operator:unbound team:modify").compact()

		String unauthz = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("scope","operator:unbound").compact()

		String tokenTeamLead = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("app_metadata",["operator_id":o.id]).compact()

		String tokenRandomOp = Jwts.builder().setSubject("123").signWith(SignatureAlgorithm.HS256,context.environment.getProperty("jwt.secret").bytes)
				.setIssuer(context.environment.getProperty("jwt.issuer")).setAudience(context.environment.getProperty("jwt.audience"))
				.claim("app_metadata",["operator_id":123]).compact()



		syncTest("Test of secured list", teamEndpoint)
				.request(headers(header("Authorization","Bearer $token")))
				.expectation(predicate { new JsonPathExpression("\$.data.length()").evaluate(it) == 1 });

		syncTest("Test of secured list failure", teamEndpoint)
				.expectsException()
				.expectation(httpStatusCode(401));

		syncTest("Test of secured list", "$teamEndpoint/$t.id")
				.request(headers(header("Authorization","Bearer $token")))
				.expectation(jsonpath(".[?(@.id == " + t.id + ")]"));

		syncTest("Test of unsecured list", "$teamEndpoint/$t.id")
				.expectsException()
				.expectation(httpStatusCode(401));

		syncTest("Secure update team","$teamEndpoint/${t.id}")
				.request(headers(header("Authorization","Bearer $token")),
				headers(header(Exchange.HTTP_METHOD, PUT())), json('{"name":"fooey"}'))
				.expectation(httpStatusCode(200))

		syncTest("Secure update team no permissions","$teamEndpoint/${t.id}")
				.request(headers(header("Authorization","Bearer $unauthz"),header(Exchange.HTTP_METHOD, PUT())),json('{"name":"fooey"}'))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Test of unsecured update", "$teamEndpoint/$t.id")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"name":"fooey"}'))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured test create",teamEndpoint)
				.request(json('{"name":"abc"}'),headers(header(Exchange.HTTP_METHOD, POST()),header("Authorization","Bearer $token")))
				.expectation(httpStatusCode(200))

		syncTest("Secured test create no permissions",teamEndpoint)
				.request(json('{"name":"abc"}'),headers(header(Exchange.HTTP_METHOD, POST()),header("Authorization","Bearer $unauthz")))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured test create",teamEndpoint)
				.request(json('{"name":"abc"}'),headers(header(Exchange.HTTP_METHOD, POST())))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured Add team member","$teamEndpoint/${t.id}/members")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $token")),json('[{"operator_id":' + o.id + '}]'))
				.expectation(httpStatusCode(200))

		syncTest("Secured add team member no permissions","$teamEndpoint/${t.id}/members")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $unauthz")),json('[{"operator_id":' + o.id + '}]'))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured add team member","$teamEndpoint/${t.id}/members")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('[{"operator_id":' + o.id + '}]'))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured update team member","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $token")),json('{"roster_weighting":999}'))
				.expectation(httpStatusCode(200))

		//team lead can do this
		syncTest("Secured update team member","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $tokenTeamLead")),json('{"roster_weighting":999}'))
				.expectation(httpStatusCode(200))

		syncTest("Secured update team member random operator","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $tokenRandomOp")),json('{"roster_weighting":999}'))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured update team member","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"roster_weighting":999}'))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured Set team lead","$teamEndpoint/${t.id}/lead/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $token")))
				.expectation(httpStatusCode(200))

		syncTest("Secured Set team lead no permissions","$teamEndpoint/${t.id}/lead/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT()),header("Authorization","Bearer $unauthz")))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured Set team lead","$teamEndpoint/${t.id}/lead/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured Remove team member","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE()),header("Authorization","Bearer $token")))
				.expectation(httpStatusCode(200))

		syncTest("Secured Remove team member no permissions","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE()),header("Authorization","Bearer $unauthz")))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured Remove team member no permissions","$teamEndpoint/${t.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
				.expectsException()
				.expectation(httpStatusCode(401))

		syncTest("Secured remove team lead","$teamEndpoint/${t.id}/lead")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE()),header("Authorization","Bearer $token")))
				.expectation(httpStatusCode(200))

		syncTest("Secured remove team lead no permissions","$teamEndpoint/${t.id}/lead")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE()),header("Authorization","Bearer $unauthz")))
				.expectsException()
				.expectation(httpStatusCode(403))

		syncTest("Unsecured remove team lead no permissions","$teamEndpoint/${t.id}/lead")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
				.expectsException()
				.expectation(httpStatusCode(401))
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

