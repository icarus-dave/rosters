package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.OperatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/operator")
class OperatorController {

	@Autowired
	OperatorService operatorService

	@RequestMapping
	public List<Operator> list() {
		return operatorService.getOperators()
	}

	@RequestMapping("/{id}")
	public ResponseEntity<Operator> getOperator(@PathVariable long id) {
		def op = operatorService.getOperator(id)
		if (op == null) return new ResponseEntity<Operator>(HttpStatus.NOT_FOUND)

		return new ResponseEntity<Operator>(op, HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.POST)
	public Operator createOperator(@RequestBody Operator operator) {
		return operatorService.createOperator(operator)
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Operator updateOperator(@RequestBody Operator operator) {
		return operatorService.updateOperator(operator)
	}

}
