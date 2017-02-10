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
		return teamService.getTeam(id).map({ team -> return new ResponseEntity(team, HttpStatus.OK) })
				.orElse(new ResponseEntity(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Team not found for id: " + id), HttpStatus.NOT_FOUND))
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@JsonView(RelationshipView.Team)
	@PreAuthorize("hasAuthority('team:modify')")
	public ResponseEntity updateTeam(@PathVariable long id, @RequestBody @JsonView(RelationshipView.Team) Team team) {
		team.id = id
		return new ResponseEntity(teamService.updateTeam(team), HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@JsonView(RelationshipView.Team)
	@PreAuthorize("hasAuthority('team:modify')")
	public Team createTeam(@JsonView(RelationshipView.TeamCreate) @RequestBody Team team) {
		if (team.team_lead_id != null) team.teamLead = operatorService.getOperator(team.team_lead_id)
				.orElseThrow { throw new IllegalArgumentException("Unknown operator: " + team.team_lead_id) }
		return teamService.createTeam(team)
	}

	@RequestMapping(value = "/{id}/members", method = RequestMethod.PUT)
	@JsonView(RelationshipView.TeamMember)
	@PreAuthorize("hasAuthority('team:modify')")
	public void addTeamMembers(@PathVariable long id, @RequestBody List<TeamMember> members) {
		members.each {
			it.team_id = id
		}
		teamService.addTeamMembers(members)
	}

	@RequestMapping(value = "/{id}/members/{operatorId}", method = RequestMethod.PUT)
	@JsonView(RelationshipView.TeamMember)
	@PreAuthorize("hasAuthority('team:modify') or @teamService.authzTeamMemberUpdate(#id,authentication)")
	public ResponseEntity updateTeamMember(
			@PathVariable long id, @PathVariable long operatorId, @RequestBody TeamMember member) {
		member.operator_id = operatorId
		member.team_id = id
		return new ResponseEntity(teamService.updateTeamMemberForOperator(member), HttpStatus.OK)
	}

	@RequestMapping(value = "/{id}/members/{operatorId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('team:modify')")
	public void removeMember(@PathVariable long id, @PathVariable long operatorId) {
		teamService.removeTeamMember(teamService.getTeam(id).orElseThrow {
			throw new IllegalArgumentException("Unknown team id: " + id)
		},
				operatorService.getOperator(operatorId).orElseThrow {
					throw new IllegalArgumentException("Unknown operator id: " + operatorId)
				})
	}

	@RequestMapping(value = "/{id}/lead/{operatorId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('team:modify')")
	public void setTeamLead(@PathVariable long id, @PathVariable long operatorId) {
		teamService.setTeamLead(id, operatorId)
	}

	@RequestMapping(value = "/{id}/lead", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('team:modify')")
	public void removeTeamLead(@PathVariable long id) {
		teamService.removeTeamLead(id)
	}
}
