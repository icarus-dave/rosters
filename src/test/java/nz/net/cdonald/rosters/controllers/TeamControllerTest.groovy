package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.junit.AfterClass
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

class TeamControllerTest extends MorcTestBuilder {

	private static ConfigurableApplicationContext context;

	public void configure() {
		def app = new SpringApplication(Application.class)
		app.setAdditionalProfiles("noauth")
		context = app.run()

		def server = context.getBean(EbeanServer.class)

		def teamEndpoint = "http://localhost:8080/api/team"

		def i

		Team t = new Team()

		syncTest("Create Team", teamEndpoint)
				.request(process { server.save(generateOperator()); server.save(generateOperator("foo","baz","asdf@asdf.com")) },
					headers(header(Exchange.HTTP_METHOD, POST())), json('{ "name":"abc","team_lead_id":1,"member_ids":[1] }'))
				.expectation(//predicate { i = new JsonPathExpression("@.id").evaluate(it); i instanceof Integer },
				predicate {
					return true
				})
				//jsonpath(".[?(@.version == 1)]"),headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")))
				.addPart(teamEndpoint + "/1/members").request(headers(header(Exchange.HTTP_METHOD, POST())),
					json('[{ "operator_id":"2","roster_weighting":42 }]'))
				.expectation(predicate {
					return true
				})
				.addPart(teamEndpoint + "/1/members/1").request(headers(header(Exchange.HTTP_METHOD, PUT())),
						json('{ "operator_id":"1","roster_weighting":42 }'))
						.expectation(predicate {
					def foo = server.find(Team.class).where().eq("id",1).findUnique()
					println foo.members
					println foo
					return true
				})
				.addPart(teamEndpoint + "/1").expectation(predicate {
					return true
				})
				.addPart(teamEndpoint + "/1/members/1").request(headers(header(Exchange.HTTP_METHOD, DELETE())))
						.expectation(predicate {
					return true
				})
				.addPart(teamEndpoint + "/1/lead").request(headers(header(Exchange.HTTP_METHOD, DELETE())))
							.expectation(predicate {
						return true
					})
				.addPart(teamEndpoint + "/1/lead/2").request(headers(header(Exchange.HTTP_METHOD, POST())))
				.expectation(predicate {
			return true
		})
				.addPart(teamEndpoint + "/1").request(headers(header(Exchange.HTTP_METHOD, PUT())),
						json('{ "id":1, "name":"foo","members":[{"operator_id":1}],"member_ids":[1] }'))
						.expectation(predicate {
					return true
				})
				.addPart(teamEndpoint + "/1").expectation(predicate {
			return true
		})


	}

	@AfterClass
	public static void finish() {
		context.close();
	}

	def generateOperator(firstName = "abc", lastName = "zyx", email = "foo@baz.com") {
		Operator o1 = new Operator()
		o1.firstName = firstName
		o1.lastName = lastName
		o1.email = email

		return o1
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
