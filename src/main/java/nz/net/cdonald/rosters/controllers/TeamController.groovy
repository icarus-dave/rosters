package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.services.TeamService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

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
