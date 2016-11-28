package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Duty
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.services.DutyService
import nz.net.cdonald.rosters.services.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/team")
class TeamController {

	@Autowired
	TeamService teamService

	@RequestMapping
	public List<Team> list() {

	}

	@RequestMapping("/{id}")
	public Team getTeam(@PathVariable String id) {

	}

	@RequestMapping(method = RequestMethod.POST)
	public Team createTeam(@RequestBody Team team) {

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
	public void addMember(@PathVariable String id, @RequestParam long operatorId) {

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public void removeMember(@PathVariable String id, @RequestParam long operatorId) {

	}
}
