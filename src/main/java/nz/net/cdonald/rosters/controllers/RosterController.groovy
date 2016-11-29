package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Shift
import nz.net.cdonald.rosters.services.ShiftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roster/{dutyId}")
class RosterController {

	@Autowired
	ShiftService shiftService

	@RequestMapping
	public List<Shift> list(@PathVariable long dutyId) {

	}

	@RequestMapping(method = RequestMethod.POST)
	public Shift createShift(@PathVariable long dutyId, @RequestBody Shift shift) {

	}

	@RequestMapping(value = "/{shiftId}", method = RequestMethod.DELETE)
	public void deleteShift(@PathVariable long dutyId, @PathVariable long shiftId) {

	}

	@RequestMapping(value = "/{shiftId}/assignOperator", method = RequestMethod.POST)
	public void assignShift(@PathVariable long dutyId, @PathVariable long shiftId, @RequestParam long operatorId) {

	}
}
