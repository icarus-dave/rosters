package nz.net.cdonald.rosters.services

import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import io.jsonwebtoken.Jwts
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
		Team t = new Team()
		t.name = "foo"

		teamService.createTeam(t)

		assertTrue(t.id != 0)
		assertEquals("foo",t.name)
		assertEquals(0,t.members.size())
		assertNull(t.teamLead)
	}

	@Test
	public void testCreateTeamWithMembers() {
		Operator o = new Operator()
		o.firstName = "foo"
		o.lastName = "baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Operator o2 = new Operator()
		o2.firstName = "foo"
		o2.lastName = "baz"
		o2.email = "foo@baz.com1"
		operatorService.createOperator(o2)

		Team t = new Team()
		t.name = "foo"
		t.member_ids = [o.id,o2.id]
		t.teamLead = o2

		teamService.createTeam(t)

		assertEquals(2,t.members.size())
		assertNotNull(t.members.find { it.operator.email == "foo@baz.com"})
		assertNotNull(t.members.find { it.operator.email == "foo@baz.com1"})
		assertEquals(o2.id,t.teamLead.id)
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
	public void testCreateNoSuchOperator() {
		Operator o = new Operator()
		o.firstName = "foo"
		o.lastName = "baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		t.member_ids = [o.id,999l]
		Exception e
		try {
			teamService.createTeam(t)
		} catch (IllegalArgumentException ex) {
			e = ex
		}
		assertNotNull(e)
	}

	@Test
	public void testGetTeam() {
		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		Team t2 = new Team()
		t2.name = "baz"
		teamService.createTeam(t2)

		Operator o = new Operator()
		o.firstName = "foo"
		o.lastName = "baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		t.member_ids = [o.id]
		t.teamLead = o

		teamService.createTeam(t)

		def t3 = teamService.getTeam(t.id).orElseThrow { new Exception() }
		assertEquals("foo",t3.name)
		assertEquals(o.id,t3.teamLead.id)

		def t4 = teamService.getTeam("baz").orElseThrow { new Exception() }
		assertEquals("baz",t4.name)
		assertNull(t4.teamLead)

		assertEquals(2,teamService.getTeams().size())
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
		assertEquals(42,operatorService.getOperator(o.id).orElse(null).teams.getAt(0).rosterWeighting)
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

	@Test
	public void testUpdateTeamMemberForOperator() {
		def t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		def tm = new TeamMember()
		tm.operator = o
		tm.team = t
		server.save(tm)

		tm.rosterWeighting = 42

		teamService.updateTeamMemberForOperator(tm)

		def tm1 = server.find(TeamMember.class).where().eq("id",tm.id).findUnique()
		assertEquals(42,tm1.rosterWeighting)
	}

	@Test
	public void testUpdateTeamMemberForOperatorIDSpecified() {
		def t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		def tm = new TeamMember()
		tm.operator = o
		tm.team = t
		server.save(tm)

		def tm2 = new TeamMember()
		tm2.operator_id = o.id
		tm2.team_id = t.id
		tm2.rosterWeighting = 42

		teamService.updateTeamMemberForOperator(tm2)

		def tm3 = server.find(TeamMember.class).where().eq("id",tm.id).findUnique()
		assertEquals(42,tm3.rosterWeighting)
	}

	@Test
	public void testUpdateTeamMemberForOpNoIdsSpecified() {
		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def tmx = new TeamMember()
		tmx.team = t
		tmx.operator = o
		server.save(tmx)

		def tm = new TeamMember()
		tm.operator_id = -1
		tm.team_id = -1
		tm.rosterWeighting = 42

		IllegalArgumentException e = null
		try {
			teamService.updateTeamMemberForOperator(tm)
		} catch (IllegalArgumentException ex) {
			e = ex
		}

		assertNotNull(e)

		tm.operator_id = o.id

		IllegalArgumentException e2 = null
		try {
			teamService.updateTeamMemberForOperator(tm)
		} catch (IllegalArgumentException ex) {
			e2 = ex
		}

		assertNotNull(e2)

		tm.team_id = t.id
		tm.rosterWeighting = 42

		teamService.updateTeamMemberForOperator(tm)

		def tm1 = server.find(TeamMember.class).where().eq("id",tm.id).findUnique()
		assertEquals(t.id,tm1.team.id)
		assertEquals(o.id,tm1.operator.id)
		assertEquals(42,tm1.rosterWeighting)
	}

	@Test
	public void testAddTeamMembers() {
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

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def tm = new TeamMember()
		tm.team_id = t.id
		tm.operator_id = o.id

		def tm2 = new TeamMember()
		tm2.team_id = t.id
		tm2.operator_id = o2.id

		teamService.addTeamMembers([tm,tm2])

		def t1 = server.find(Team.class).where().eq("id",t.id).findUnique()
		assertEquals(2,t1.members.size())
	}

	@Test
	public void testAddTeamMemberMissing() {
		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def tm = new TeamMember()
		tm.team_id = t.id
		tm.operator_id = o.id

		def tm2 = new TeamMember()
		tm2.team_id = t.id
		tm2.operator_id = -11

		IllegalArgumentException e = null
		try {
			teamService.addTeamMembers([tm, tm2])
		} catch (IllegalArgumentException ex) {
			e = ex
		}

		assertNotNull(e)

		assertEquals(0,server.find(TeamMember.class).findList().size())
	}

	@Test
	public void testAddTeamMembersNoSuchTeam() {
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

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		def tm = new TeamMember()
		tm.team_id = t.id
		tm.operator_id = o.id

		def tm2 = new TeamMember()
		tm2.team_id = -11
		tm2.operator_id = o2.id

		IllegalArgumentException e = null
		try {
			teamService.addTeamMembers([tm, tm2])
		} catch (IllegalArgumentException ex) {
			e = ex
		}

		assertNotNull(e)

		assertEquals(0,server.find(TeamMember.class).findList().size())
	}

	@Test
	public void testSetTeamLead() {
		Team t = new Team()
		t.name = "foo"

		teamService.createTeam(t)

		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"

		operatorService.createOperator(o)

		teamService.setTeamLead(t.id,o.id)

		assertEquals(o.id,server.find(Team.class).where().eq("id",t.id).findUnique().teamLead.id)
	}

	@Test
	public void testSetTeamLeadNoOperatorOrTeam() {
		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		IllegalArgumentException e = null

		try {
			teamService.setTeamLead(-11, o.id)
		} catch (IllegalArgumentException ex) {
			e = ex
		}

		assertNotNull(e)

		IllegalArgumentException e2 = null

		try {
			teamService.setTeamLead(t.id, -11)
		} catch (IllegalArgumentException ex) {
			e2 = ex
		}

		assertNotNull(e2)
	}


	@Test
	public void testRemoveTeamLead() {
		def o = new Operator()
		o.firstName = "Foo"
		o.lastName = "Baz"
		o.email = "foo@baz.com"
		operatorService.createOperator(o)

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		teamService.setTeamLead(t.id,o.id)

		teamService.removeTeamLead(t.id)

		assertNull(server.find(Team.class).where().eq("id",t.id).findUnique().teamLead)
	}

	@Test
	public void testRemoveTeamLeadNoTeam() {
		Exception e = null
		try {
			teamService.removeTeamLead(-11)
		} catch (IllegalArgumentException ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testAuthzNoMetadata() {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		Team t = new Team()
		t.name = "foo"
		t.teamLead = o1
		teamService.createTeam(t)

		String operatorToken = Jwts.builder().setSubject("123").compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(teamService.authzTeamMemberUpdate(123,authn))
	}

	@Test
	public void testAuthzNoOperatorId() {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		Team t = new Team()
		t.name = "foo"
		t.teamLead = o1
		teamService.createTeam(t)

		String operatorToken = Jwts.builder().setSubject("123").claim("app_metadata",[:]).compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(teamService.authzTeamMemberUpdate(123,authn))
	}

	@Test
	public void testAuthzOperatorIdMatch() {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		Team t = new Team()
		t.name = "foo"
		t.teamLead = o1
		teamService.createTeam(t)

		String operatorToken = Jwts.builder().setSubject("123").claim("app_metadata",["operator_id":o1.id]).compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertTrue(teamService.authzTeamMemberUpdate(t.id,authn))
	}

	@Test
	public void testAuthzOperatorIdNoTeamLead() {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		Team t = new Team()
		t.name = "foo"
		teamService.createTeam(t)

		String operatorToken = Jwts.builder().setSubject("123").claim("app_metadata",["operator_id":o1.id]).compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(teamService.authzTeamMemberUpdate(t.id,authn))
	}

	@Test
	public void testAuthzOperatorIdMismatch() {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		Team t = new Team()
		t.name = "foo"
		t.teamLead = o1
		teamService.createTeam(t)

		String operatorToken = Jwts.builder().setSubject("123").claim("app_metadata",["operator_id":123]).compact()
		def authn = PreAuthenticatedAuthenticationJsonWebToken.usingToken(operatorToken).verify(null)
		assertFalse(teamService.authzTeamMemberUpdate(t.id,authn))
	}
}

