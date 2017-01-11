package nz.net.cdonald.rosters.controllers

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

import javax.persistence.OptimisticLockException
import javax.persistence.PersistenceException
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ErrorControllerAdvice {

	static final Logger logger = LoggerFactory.getLogger(ErrorControllerAdvice.class)

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

	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity handlePersistenceException(Exception e) throws IOException {
		def res = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error working with database")
		logger.error("Returning 500 to user", e)
		return new ResponseEntity(res, HttpStatus.INTERNAL_SERVER_ERROR)
	}

	@ExceptionHandler(InsufficientAuthenticationException.class)
	public ResponseEntity InsufficientAuthenticationException(Exception e) throws IOException {
		def res = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server error working with database")
		logger.error("Returning 500 to user", e)
		return new ResponseEntity(res, HttpStatus.INTERNAL_SERVER_ERROR)
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity HttpMessageNotReadableException(Exception e) throws IOException {
		def res = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request made, please check and try again")
		logger.warn("Invalid request made by user",e)
		return new ResponseEntity(res, HttpStatus.BAD_REQUEST)
	}
}
