package nz.net.cdonald.rosters.components

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationExceptionEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		response.setContentType("application/json")
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
		response.getOutputStream().println("{\"code\":\"$HttpServletResponse.SC_UNAUTHORIZED\",\"message\":\"${authException.message}\"}")
	}
}
