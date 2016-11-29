package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.services.OperatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

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
		if (op == null) return new ResponseEntity(new ErrorResponse(HttpStatus.NOT_FOUND.value(),"Operator not found"),HttpStatus.NOT_FOUND)

		return new ResponseEntity<Operator>(op, HttpStatus.OK)
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public Operator createOperator(@RequestBody Operator operator) {
		return operatorService.createOperator(operator)
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Operator updateOperator(@RequestBody Operator operator) {
		return operatorService.updateOperator(operator)
	}

	@ExceptionHandler([IllegalArgumentException.class, MethodArgumentTypeMismatchException.class])
	public ResponseEntity handleBadRequests() throws IOException {
		def res = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"Invalid parameter provided")
		return new ResponseEntity(res, HttpStatus.BAD_REQUEST)
	}

}

