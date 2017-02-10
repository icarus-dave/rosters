package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import com.auth0.jwt.impl.NullClaim
import com.auth0.jwt.interfaces.Claim
import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import com.avaje.ebean.annotation.Transactional
import com.fasterxml.jackson.databind.ObjectMapper
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.domain.TeamMember
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class TeamService {

	static final Logger logger = LoggerFactory.getLogger(TeamService.class)

	@Autowired
	EbeanServer server

	@Autowired
	OperatorService operatorService

	@Transactional
	def createTeam(Team t) {
		if (!t.name) throw new IllegalArgumentException("Team name must be specified")
		server.save(t)
		logger.info("Created team {}:{}", t.id, t.name)
		t.member_ids.asList().each {
			assignTeamMember(t, operatorService.getOperator(it).orElseThrow {
				new IllegalArgumentException("Operator for id " + it + " is unknown")
			})
		};

		return t
	}

	def List<Team> getTeams() {
		return server.find(Team.class).findList()
	}

	def getTeam(long id) {
		return Optional.ofNullable(server.find(Team.class).where().eq("id", id).findUnique());
	}

	def getTeam(String name) {
		return Optional.ofNullable(server.find(Team.class).where().ieq("name", name).findUnique());
	}

	def updateTeam(Team team) {
		server.update(team)
		server.refreshMany(team,"members")
		logger.info("Updated team {}:{}",team.id,team.name)
		return team
	}

	//TeamMember?
	def assignTeamMember(Team team, Operator operator) {
		if (team.members.find { it.operator.id == operator.id }) {
			logger.info("Operator: {}:{} is already a member of Team {}:{}, skipping",operator.id,operator.email,team.id,team.name)
			return
		}

		TeamMember t = new TeamMember()
		t.operator = operator
		t.team = team

		server.save(t)
		server.refreshMany(team,"members")
		server.refreshMany(operator,"teams")

		logger.info("Assigned operator {}:{} to team {}:{}",operator.id,operator.email,team.id,team.name)
		return t
	}

	def removeTeamMember(Team team, Operator operator) {
		if (!team.members.find { it.operator.id == operator.id }) {
			logger.info("Operator {}:{} does not belong to Team {}:{} and cannot be deleted, skipping",operator.id,operator.email,team.id,team.name)
			return
		}
		server.delete(team.members.find { it.operator.id == operator.id })
		server.refreshMany(team,"members")
		server.refreshMany(operator,"teams")

		logger.info("Removed operator {}:{} from team {}:{}",operator.id,operator.email,team.id,team.name)
	}

	def updateTeamMemberForOperator(TeamMember tm) {
		if (!tm.operator) tm.setOperator(operatorService.getOperator(tm.operator_id).orElseThrow { new IllegalArgumentException("Operator ID " + tm.operator_id + " is unknown") })
		if (!tm.team) tm.setTeam(getTeam(tm.team_id).orElseThrow { new IllegalArgumentException("Team ID " + tm.team_id + " is unknown") })

		if (!operatorInTeam(tm.team,tm.operator))
			throw new IllegalArgumentException("Operator ${tm.operator.id} is not a member of Team ${tm.team.id}")

		server.update(tm)

		return tm
	}

	@Transactional
	def addTeamMembers(List<TeamMember> tm) {
		tm.each { member ->
			member.team = member.team != null ? member.team : getTeam(member.team_id).orElseThrow { new IllegalArgumentException("Team ID + " + member.team_id + " is unknown") }
			member.operator = operatorService.getOperator(member.operator_id)
					.orElseThrow { new IllegalArgumentException("Operator ID " + member.operator_id + " is unknown")}

			server.save(member)

			server.refreshMany(member.team,"members")
			server.refreshMany(member.operator,"teams")
		}

		return tm
	}

	def setTeamLead(long teamId, long operatorId) {
		def team = getTeam(teamId).orElseThrow { new IllegalArgumentException("Team not found for ID " + teamId) }
		def operator = operatorService.getOperator(operatorId)
				.orElseThrow { new IllegalArgumentException("Operator not found for ID " + operatorId ) }

		team.teamLead = operator

		server.save(team)
	}

	def removeTeamLead(long teamId) {
		def team = getTeam(teamId).orElseThrow { new IllegalArgumentException("Team not found for ID " + teamId) }

		team.teamLead = null

		server.save(team)
	}

	def boolean operatorInTeam(Team t, Operator o) {
		return t.members.find { it.operator.id == o.id } != null
	}

	def boolean authzTeamMemberUpdate(long id, Authentication authn) {
		//we just need to check that the ID matches operator_id in the token
		def jwtAuthn = authn as AuthenticationJsonWebToken
		def appMetadata = jwtAuthn.getDetails().getClaim("app_metadata") as Claim
		if (appMetadata instanceof NullClaim) return false

		def objectMapper = new ObjectMapper()
		def jwtAppMetadata = objectMapper.convertValue(appMetadata.data, Map.class)

		return getTeam(id).map( { it.teamLead?.id == jwtAppMetadata.get("operator_id")}).orElse(false)
	}

}
