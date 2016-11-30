package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.OperatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/operator")
class OperatorController {

	@Autowired
	OperatorService operatorService

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Operator> list() {
		return operatorService.getOperators()
	}

	@RequestMapping("/{id}")
	public ResponseEntity getOperator(@PathVariable long id) {
		def op = operatorService.getOperator(id)
		if (op == null) return new ResponseEntity(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Operator not found"), HttpStatus.NOT_FOUND)

		return new ResponseEntity<Operator>(op, HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public Operator createOperator(@RequestBody Operator operator) {
		return operatorService.createOperator(operator)
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity updateOperator(@RequestBody Operator operator, @PathVariable long id) {
		if (operator.id == null || id != operator.id) return new ResponseEntity(
				new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Operator id and path id parameter mismatch or not specified"), HttpStatus.BAD_REQUEST)

		return new ResponseEntity(operatorService.updateOperator(operator), HttpStatus.OK)
	}

}

