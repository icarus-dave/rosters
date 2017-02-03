package nz.net.cdonald.rosters.controllers

import com.fasterxml.jackson.annotation.JsonView
import nz.net.cdonald.rosters.domain.RelationshipView
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.domain.TeamMember
import nz.net.cdonald.rosters.domain.WrappedList
import nz.net.cdonald.rosters.services.OperatorService
import nz.net.cdonald.rosters.services.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/team")
class TeamController {

	@Autowired
	TeamService teamService

	@Autowired
	OperatorService operatorService

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@JsonView(RelationshipView.Team)
	public WrappedList<Team> list() {
		//wrapped to avoid the security issue around unwrapped arrays
		return new WrappedList<Team>(teamService.getTeams())
	}

	@RequestMapping("/{id}")
	@JsonView(RelationshipView.Team)
	public ResponseEntity getTeam(@PathVariable long id) {
		return teamService.getTeam(id).map({ team -> return new ResponseEntity(team, HttpStatus.OK)})
				.orElse(new ResponseEntity(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Team not found for id: " + id), HttpStatus.NOT_FOUND))
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@JsonView(RelationshipView.Team)
	@PreAuthorize("hasAuthority('team:modify')")
	public ResponseEntity updateTeam(@PathVariable long id, @JsonView(RelationshipView.Team) @RequestBody Team team) {
		if (team.id == null || id != team.id)
			return new ResponseEntity(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"Team id and path id parameter mismatch or not specified"), HttpStatus.BAD_REQUEST)

		return new ResponseEntity(teamService.updateTeam(team), HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@JsonView(RelationshipView.Team)
	@PreAuthorize("hasAuthority('team:modify')")
	public Team createTeam(@JsonView(RelationshipView.TeamCreate) @RequestBody Team team) {
		team.teamLead = operatorService.getOperator(team.team_lead_id).orElse(null)

		return teamService.createTeam(team)
	}

	@RequestMapping(value = "/{id}/members", method = RequestMethod.POST)
	@JsonView(RelationshipView.TeamMember)
	@PreAuthorize("hasAuthority('team:modify') or @teamService.authzTeamMemberUpdate(#id,authentication)")
	public WrappedList<TeamMember> addTeamMembers(@PathVariable long id, @RequestBody List<TeamMember> members) {
		return new WrappedList<TeamMember>(teamService.createTeamMembers(id,members))
	}

	@RequestMapping(value = "/{id}/members/{operatorId}", method = RequestMethod.PUT)
	@JsonView(RelationshipView.TeamMember)
	@PreAuthorize("hasAuthority('team:modify') or @teamService.authzTeamMemberUpdate(#id,authentication)")
	public ResponseEntity updateTeamMember(@PathVariable long id, @PathVariable long operatorId, @RequestBody TeamMember member) {
		if (member.operator_id == 0 || operatorId != member.operator_id)
			return new ResponseEntity(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"Operator id and team member id parameter mismatch or not specified"), HttpStatus.BAD_REQUEST)

		return new ResponseEntity(teamService.updateTeamMemberFromOperator(id,member), HttpStatus.OK)
	}

	@RequestMapping(value = "/{id}/members/{operatorId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('team:modify') or @teamService.authzTeamMemberUpdate(#id,authentication)")
	public void removeMember(@PathVariable long id, @PathVariable long operatorId) {
		teamService.removeTeamMember(teamService.getTeam(id).orElseThrow { throw new IllegalArgumentException("Unknown team id: " + id) },
				operatorService.getOperator(operatorId).orElseThrow { throw new IllegalArgumentException("Unknown operator id: " + operatorId)})
	}

	@RequestMapping(value = "/{id}/lead/{operatorId}", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('team:modify')")
	public void setTeamLead(@PathVariable long id, @PathVariable long operatorId) {
		teamService.setTeamLead(id,operatorId)
	}

	@RequestMapping(value = "/{id}/lead", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('team:modify')")
	public void removeTeamLead(@PathVariable long id) {
		teamService.removeTeamLead(id)
	}
}
