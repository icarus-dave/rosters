package nz.net.cdonald.rosters.controllers

import nz.net.cdonald.rosters.domain.Operator
import nz.net.cdonald.rosters.domain.WrappedList
import nz.net.cdonald.rosters.services.OperatorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.util.SystemPropertyUtils
import org.springframework.web.bind.annotation.*

import javax.management.relation.Role
import java.security.Principal


@RestController
@RequestMapping("/api/operator")
class OperatorController {

	@Autowired
	OperatorService operatorService

	//@PreAuthorize("@currentUserServiceImpl.canAccessUser(principal, #id)")
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public WrappedList<Operator> list() {
		//wrapped to avoid the security issue around unwrapped arrays
		return new WrappedList<Operator>(operatorService.getOperators())
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
		def o = operatorService.updateOperator(operator)
		return new ResponseEntity(o, HttpStatus.OK)
	}

}

public interface CurrentUserService {
	boolean canAccessUser(Authentication a, Long userId);
}

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

	@Override
	public boolean canAccessUser(Authentication a, Long userId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		println "HERE";
		true;
		//return currentUser != null
		//&& (currentUser.getRole() == Role.ADMIN || currentUser.getId().equals(userId));
	}

}
