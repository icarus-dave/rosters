package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Duty
import nz.net.cdonald.rosters.services.DutyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

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

