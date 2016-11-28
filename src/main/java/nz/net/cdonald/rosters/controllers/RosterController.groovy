package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.Shift
import nz.net.cdonald.rosters.services.DutyService
import nz.net.cdonald.rosters.services.ShiftService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
