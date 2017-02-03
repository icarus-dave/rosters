package nz.net.cdonald.rosters.services

import com.avaje.ebean.EbeanServer
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.domain.TeamMember
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
class TeamServiceTest extends Assert {

	@Autowired
	EbeanServer server;

	@Autowired
	OperatorService operatorService

	@Autowired
	TeamService teamService

	@After
	public void clear() {
		server.deleteAll(server.find(TeamMember.class).findList())
		server.deleteAll(server.find(Team.class).findList())
		server.deleteAll(server.find(Operator.class).findList())
	}

	@Test
	public void testCreateTeam() {
		Operator o = new Operator()
		o.firstName = "abc"
		o.lastName = "def"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		t.member_ids.add(o.id)

		teamService.createTeam(t)
		assertNotNull(t)
		assertNotNull(t.id)

		def tDb = server.find(Team.class).where().eq("id", t.id).findUnique()
		assertNotNull(tDb)
		assertEquals("foo",tDb.name)

		assertEquals(1,tDb.members.size())
		assertEquals(o.id,tDb.members.getAt(0).operator.id)

		tDb.members.getAt(0).rosterWeighting = 42;
		teamService.updateTeam(tDb)

		def tDb2 = server.find(Team.class).where().eq("id", t.id).findUnique()

		assertEquals(42,tDb.members.getAt(0).rosterWeighting)
		assertEquals(42,tDb2.members.getAt(0).rosterWeighting)
	}

	@Test
	public void testNullName() {
		Exception e
		try {
			teamService.createTeam(new Team())
		} catch (IllegalArgumentException ex) {
			e = ex
		}
		assertNotNull(e)
	}

	@Test
	public void testGetTeam() {
		throw new Exception("todo");
	}

	@Test
	public void testCreateTeamUnknownOperatorId() {
		throw new Exception("todo");
	}

	@Test
	public void testCreateTeamExistingName() {
		Team t = new Team()
		t.name = "foo"

		teamService.createTeam(t)

		Team t2 = new Team()
		t2.name = "FOO"

		Exception e
		try {
			teamService.createTeam(t2)
		} catch (Exception ex) {
			e = ex
		}
		assertNotNull(e)
	}

	@Test
	public void assignAndRemoveTeamMembers() {
		/*
			Test we can assign multiple members to multiple teams, and then remove them
		 */
		def t = new Team()
		t.name = "a"

		def t2 = new Team()
		t2.name = "b"

		teamService.createTeam(t)
		teamService.createTeam(t2)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		def o2 = new Operator()
		o2.firstName = "Baz"
		o2.lastName = "Baz"
		o2.email = "foo1@baz.com"

		operatorService.createOperator(o)
		operatorService.createOperator(o2)
		assertEquals(0,o.teams.size())

		teamService.assignTeamMember(t,o)

		//check we can assign to one team
		assertEquals(1,o.teams.size())
		assertEquals(1,t.members.size())

		assertEquals("a",o.teams.getAt(0).team.name)
		assertEquals("Foo",t.members.getAt(0).operator.firstName)

		assertEquals(1,server.find(TeamMember).findList().size())

		//assign to another team
		teamService.assignTeamMember(t2,o)
		assertEquals(2,o.teams.size())
		assertEquals(1,t2.members.size())

		assertEquals("b",o.teams.getAt(1).team.name)
		assertEquals("Foo",t2.members.getAt(0).operator.firstName)

		assertEquals(2,server.find(TeamMember).findList().size())

		//now assign another team member
		teamService.assignTeamMember(t2,o2)
		teamService.assignTeamMember(t,o2)
		assertEquals(2,o2.teams.size())
		assertEquals(2,t.members.size())
		assertEquals(2,t2.members.size())

		//remove the team member
		teamService.removeTeamMember(t,o)
		assertEquals(1,t.members.size())

		teamService.removeTeamMember(t2,o)
		assertEquals(1,t2.members.size())

		teamService.removeTeamMember(t,o2)
		teamService.removeTeamMember(t2,o2)
		assertEquals(0,t.members.size())
		assertEquals(0,t2.members.size())
	}

	@Test
	public void testAssignSameOperatorMultipleTimes() {
		def t = new Team()
		t.name = "a"

		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		operatorService.createOperator(o)

		def tm = teamService.assignTeamMember(t,o)
		t.members.getAt(0).rosterWeighting = 42

		teamService.updateTeam(t)

		def tDb = server.find(Team.class).where().eq("id", t.id).findUnique()

		assertEquals(42,tDb.members.getAt(0).rosterWeighting)

		teamService.assignTeamMember(tDb,o)

		assertEquals(1,tDb.members.size())
		assertEquals(1,o.teams.size())
		assertEquals(42,tDb.members.getAt(0).rosterWeighting)
		assertEquals(42,operatorService.getOperator(o.id).teams.getAt(0).rosterWeighting)
	}

	@Test
	public void testUpdateMultipleValues() {
		def t = new Team()
		t.name = "a"

		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		def o2 = new Operator()
		o2.firstName = "Baz"
		o2.lastName = "Foo"
		o2.email = "foo1@baz.com"

		operatorService.createOperator(o)
		operatorService.createOperator(o2)

		teamService.assignTeamMember(t,o)
		teamService.assignTeamMember(t,o2)

		t.members.each { it.rosterWeighting = 123 }

		teamService.updateTeam(t)

		def tDb = server.find(Team.class).where().eq("id", t.id).findUnique()

		assertEquals(123,tDb.members.getAt(0).rosterWeighting)
		assertEquals(123,tDb.members.getAt(1).rosterWeighting)
	}

	@Test
	public void testRemoveUnmatchedTeamOperator() {
		def t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		teamService.removeTeamMember(t,o)
	}

	@Test
	public void testUpdateOperatorValues() {
		def t = new Team()
		t.name = "a"

		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		operatorService.createOperator(o)
		teamService.assignTeamMember(t,o)

		o.teams.getAt(0).rosterWeighting = 42

		operatorService.updateOperator(o)

		def oDb = server.find(Operator.class).where().eq("id", o.id).findUnique()

		assertEquals(42,oDb.teams.getAt(0).rosterWeighting)
	}

	@Test
	public void updateOperatorWithMembers() {
		def t = new Team()
		t.name = "a"
		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		TeamMember tm = new TeamMember()
		tm.operator = o
		tm.team = t
		o.teams.add(tm)

		operatorService.updateOperator(o)

		def oDb = server.find(Operator.class).where().eq("id", o.id).findUnique()
		def tDb = server.find(Team.class).where().eq("id", t.id).findUnique()

		assertEquals(1,oDb.teams.size())
		assertEquals(1,tDb.members.size())
	}

	@Test
	public void testGetTeams() {
		assertEquals(0,teamService.getTeams().size())

		def t = new Team()
		t.name = "a"
		def t2 = new Team()
		t2.name = "b"
		teamService.createTeam(t)
		teamService.createTeam(t2)

		assertEquals(2,teamService.getTeams().size())
		assertNotNull(teamService.getTeams().find { it.name == "a" })
		assertNotNull(teamService.getTeams().find { it.name == "b" })
	}
}

