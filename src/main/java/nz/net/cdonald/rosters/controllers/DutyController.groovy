package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Duty
import nz.net.cdonald.rosters.domain.Team
import nz.net.cdonald.rosters.services.DutyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/duty")
class DutyController {

	@Autowired
	DutyService dutyService

	@RequestMapping
	public List<Duty> list() {

	}

	@RequestMapping("/{id}")
	public Duty getDuty(@PathVariable String id) {

	}

	@RequestMapping(method = RequestMethod.POST)
	public Duty createDuty(@RequestBody Duty duty) {

	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public Duty updateDuty(@PathVariable String id, @RequestBody Duty duty) {

	}

	@RequestMapping(value = "/{id}/standdown", method = RequestMethod.POST)
	public void standDown(@PathVariable String id, @RequestParam long operatorId) {

	}

	@RequestMapping(value = "/{id}/unstanddown", method = RequestMethod.POST)
	public void unstandDown(@PathVariable String id, @RequestParam long operatorId) {

	}

}

