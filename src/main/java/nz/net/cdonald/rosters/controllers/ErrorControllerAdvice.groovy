package nz.net.cdonald.rosters.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

import javax.persistence.OptimisticLockException
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ErrorControllerAdvice {
	@ExceptionHandler([IllegalArgumentException.class, MethodArgumentTypeMismatchException.class])
	public ResponseEntity handleBadRequests(Exception e) throws IOException {
		def res = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid parameter provided: ${e.message}")
		return new ResponseEntity(res, HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity handleUnsupportedMethod(HttpServletRequest request) throws IOException {
		def res = new ErrorResponse(HttpStatus.METHOD_NOT_ALLOWED.value(), "Request method ${request.method} not supported for path ${request.servletPath}")
		return new ResponseEntity(res, HttpStatus.METHOD_NOT_ALLOWED)
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity handleOptimisticLockException(HttpServletRequest request) throws IOException {
		def res = new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict in updating object. Try GETing again and then updating")
		return new ResponseEntity(res, HttpStatus.CONFLICT)
	}
}
