package nz.net.cdonald.rosters.controllers

import com.avaje.ebean.EbeanServer
import nz.ac.auckland.morc.MorcTestBuilder
import nz.net.cdonald.rosters.Application
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.services.OperatorService
import nz.net.cdonald.rosters.services.TeamService
import org.apache.camel.Exchange
import org.apache.camel.Predicate
import org.apache.camel.Processor
import org.apache.camel.jsonpath.JsonPathExpression
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
		def teamService = context.getBean(TeamService.class)
		def operatorService = context.getBean(OperatorService.class)

		def teamEndpoint = "http://localhost:8080/api/team"
		def operatorEndpoint = "http://localhost:8080/api/operator"

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		Team t2 = new Team()
		t2.name = "baz"
		teamService.createTeam(t2)

		def o = generateOperator()
		operatorService.createOperator(o)

		def o2 = generateOperator("def","jkl","baz@foo.com")
		operatorService.createOperator(o2)
		teamService.assignTeamMember(t2,o)
		teamService.assignTeamMember(t2,o2)
		teamService.setTeamLead(t2.id,o.id)

		Team t3 = new Team()
		t3.name = "3"
		teamService.createTeam(t3)

		def o3 = generateOperator("egg","jkl","baz1@foo.com")
		operatorService.createOperator(o3)

		syncTest("Get team by id",teamEndpoint + "/" + t.id)
			.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				jsonpath(".[?(@.name == 'foo')]"),jsonpath(".[?(@.id == " + t.id + ")]"))

		syncTest("Get unknown team by id","$teamEndpoint/12345")
				.expectsException()
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				httpStatusCode(404), jsonpath(".[?(@.code == '404')]"))

		syncTest("Get teams",teamEndpoint)
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				predicate { new JsonPathExpression("\$.data.length()").evaluate(it) == 3 },
					jsonpath(".data.[?(@.name == 'foo')]"),jsonpath(".data.[?(@.name == 'baz')]"),jsonpath(".data.[?(@.name == '3')]"),
					jsonpath(".data.[?(@.id == " + t.id + ")]"),jsonpath(".data.[?(@.id == " + t2.id + ")]"))

		syncTest("Create team",teamEndpoint)
				.request(json('{"name":"abc"}'),headers(header(Exchange.HTTP_METHOD, POST())))
				.expectation(headers(header(Exchange.CONTENT_TYPE, "application/json;charset=UTF-8")),
				jsonpath(".[?(@.name == 'abc')]"),jsonpath(".[?(@.team_lead == null)]"),
				predicate { new JsonPathExpression("\$.members.length()").evaluate(it) == 0 })

		syncTest("Create team with members and team lead",teamEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())),
					json('{"name":"def","member_ids":[' + o.id + ',' + o2.id + '],"team_lead_id":' + o.id + '}'))
				.expectation(jsonpath(".[?(@.name == 'def')]"),
						//team lead valid
						jsonpath(".[?(@.team_lead.id == ${o.id})]"),
						jsonpath(".[?(@.team_lead.first_name == 'abc')]"),
						//members valid and present
						predicate { new JsonPathExpression("\$.members.length()").evaluate(it) == 2 },
						jsonpath(".members.[?(@.operator.first_name == 'abc')]"),
						jsonpath(".members.[?(@.operator.first_name == 'def')]"),
						jsonpath(".members.[?(@.roster_weighting == 0)]"),
						//other fields from domain model are excluded
						regex("^((?!member_ids).)*\$"),regex("^((?!lcname).)*\$"),regex("^((?!team_lead_id).)*\$"))
				.addPart("$operatorEndpoint/${o.id}")
						.expectation(predicate { new JsonPathExpression("\$.teams.length()").evaluate(it) == 2 },
						jsonpath(".teams.[?(@.team.name == 'def')]"),jsonpath(".teams.[?(@.team.name == 'baz')]"),
						jsonpath(".teams.[?(@.roster_weighting == 0)]"),
						regex("^((?!members).)*\$"))

		syncTest("Create team with unknown team lead",teamEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{"name":"ccc","team_lead_id":12345}'))
				.expectsException()
				.expectation(httpStatusCode(400), jsonpath(".[?(@.code == '400')]"))

		syncTest("Create team with unknown team member",teamEndpoint)
				.request(headers(header(Exchange.HTTP_METHOD, POST())), json('{"name":"fff","team_member_ids":[' + o.id + ',12345]}'))
				.expectsException()
				.expectation(httpStatusCode(400), jsonpath(".[?(@.code == '400')]"))

		syncTest("Update team","$teamEndpoint/${t.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())), json('{"id":12345,"name":"fooey"}'))
				.expectation( predicate { return "fooey" == teamService.getTeam("fooey").get().name } )

		syncTest("Add team member","$teamEndpoint/${t3.id}/members")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('[{"operator_id":' + o.id + ',"roster_weighting":666},{"operator_id":' + o2.id + '}]'))
				.expectation(httpStatusCode(200))
			.addPart("$operatorEndpoint/${o.id}")
				.expectation(predicate { new JsonPathExpression("\$.teams.length()").evaluate(it) == 3 },
				jsonpath(".teams.[?(@.team.name == '3')]"), jsonpath(".teams.[?(@.roster_weighting == 666)]"))
			.addPart("$operatorEndpoint/${o2.id}")
				.expectation(predicate { new JsonPathExpression("\$.teams.length()").evaluate(it) == 3 },
				jsonpath(".teams.[?(@.team.name == '3')]"), jsonpath(".teams.[?(@.roster_weighting == 0)]"))

		syncTest("Add unknown team member","$teamEndpoint/${t3.id}/members")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('[{"operator_id":' + o3.id + '},{"operator_id":12345}]'))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))
			.addPart(teamEndpoint + "/" + t3.id)
				.expectation(predicate { new JsonPathExpression("\$.members.length()").evaluate(it) == 2 })

		syncTest("Remove team member","$teamEndpoint/${t3.id}/members/${o.id}")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
			.addPart(teamEndpoint + "/" + t3.id)
				.expectation(predicate { new JsonPathExpression("\$.members.length()").evaluate(it) == 1 },
				jsonpath(".members.[?(@.operator.first_name == 'def')]"))

		syncTest("Remove unknown team member","$teamEndpoint/${t3.id}/members/12345")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))

		syncTest("Update team member","$teamEndpoint/${t2.id}/members/${o2.id}")
				//should ignore the 123,456 values
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"operator_id":123,"team_id":456, "roster_weighting":999}'))
			.addPart("$operatorEndpoint/${o2.id}")
				.expectation(jsonpath(".teams.[?(@.roster_weighting == 999)]"))

		syncTest("Update team member unknown team","$teamEndpoint/12345/members/${o2.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"operator_id":123,"team_id":456, "roster_weighting":999}'))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))

		syncTest("Update team member unknown operator","$teamEndpoint/${t2.id}/members/12345")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"operator_id":123,"team_id":456, "roster_weighting":999}'))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))

		syncTest("Update team member operator not member","$teamEndpoint/${t2.id}/members/${o3.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())),json('{"operator_id":' + t.id + ',"team_id":' + o3.id + ', "roster_weighting":999}'))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))

		syncTest("Set team lead","$teamEndpoint/${t2.id}/lead/${o3.id}")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())))
			.addPart("$teamEndpoint/${t2.id}")
				.expectation(jsonpath(".[?(@.team_lead.id == ${o3.id})]"),
				jsonpath(".[?(@.team_lead.first_name == 'egg')]"))

		syncTest("Remove team lead","$teamEndpoint/${t2.id}/lead")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
			.addPart("$teamEndpoint/${t2.id}")
				.expectation(jsonpath(".[?(@.team_lead == null)]"))

		syncTest("Remove null team lead","$teamEndpoint/${t2.id}/lead")
				.request(headers(header(Exchange.HTTP_METHOD, DELETE())))
				.addPart("$teamEndpoint/${t2.id}")
				.expectation(jsonpath(".[?(@.team_lead == null)]"))

		syncTest("Set unknown team member","$teamEndpoint/${t2.id}/lead/12345")
				.request(headers(header(Exchange.HTTP_METHOD, PUT())))
				.expectsException()
				.expectation(httpStatusCode(400),jsonpath(".[?(@.code == '400')]"))

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
