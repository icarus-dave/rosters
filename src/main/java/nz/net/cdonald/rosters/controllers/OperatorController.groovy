package nz.net.cdonald.rosters.controllers

import com.fasterxml.jackson.annotation.JsonView
import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.RelationshipView
import nz.net.cdonald.rosters.domain.WrappedList
import nz.net.cdonald.rosters.services.OperatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/operator")
class OperatorController {

	@Autowired
	OperatorService operatorService

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@JsonView(RelationshipView.Operator)
	public WrappedList<Operator> list() {
		//wrapped to avoid the security issue around unwrapped arrays
		return new WrappedList<Operator>(operatorService.getOperators())
	}

	@RequestMapping("/{id}")
	@JsonView(RelationshipView.Operator)
	public ResponseEntity getOperator(@PathVariable long id) {
		return operatorService.getOperator(id).map({ operator -> return new ResponseEntity(operator, HttpStatus.OK)})
				.orElse(new ResponseEntity(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Operator not found"), HttpStatus.NOT_FOUND))
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@PreAuthorize("hasAuthority('operator:modify')")
	@JsonView(RelationshipView.Operator)
	public Operator createOperator(@RequestBody Operator operator) {
		return operatorService.createOperator(operator)
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('operator:modify') or @operatorService.authzOperatorUpdate(#id,authentication)")
	@JsonView(RelationshipView.Operator)
	public ResponseEntity updateOperator(@RequestBody Operator operator, @PathVariable long id) {
		operator.id = id
		return new ResponseEntity(operatorService.updateOperator(operator), HttpStatus.OK)
	}

}