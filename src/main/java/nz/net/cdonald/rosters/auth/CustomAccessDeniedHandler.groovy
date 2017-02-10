package nz.net.cdonald.rosters.auth

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class CustomAccessDeniedHandler implements AccessDeniedHandler {
	void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
		response.setContentType("application/json")
		response.setStatus(HttpServletResponse.SC_FORBIDDEN)
		response.getOutputStream().println("{\"code\":\"$HttpServletResponse.SC_FORBIDDEN\",\"message\":\"${e.message}\"}")
	}
}
