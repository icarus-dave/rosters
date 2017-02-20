package nz.net.cdonald.rosters.auth

import ch.qos.logback.classic.Logger
import com.auth0.jwt.JWT
import com.auth0.spring.security.api.JwtAuthenticationProvider
import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import com.fasterxml.jackson.databind.ObjectMapper
import nz.net.cdonald.rosters.services.UserService
import nz.net.cdonald.rosters.services.OperatorService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

/*
	Simple authentication hook to ensure the user registration process completes
 */

@Component
class AuthenticationProvider extends JwtAuthenticationProvider {

	static final Logger logger = LoggerFactory.getLogger(AuthenticationProvider.class)

	@Autowired
	OperatorService operatorService

	@Autowired
	UserService userService

	@Autowired
	public AuthenticationProvider(@Value('${jwt.secret}') String secret,
								  @Value('${jwt.issuer}') String issuer,
								  @Value('${jwt.audience}') String audience) {
		super(secret.bytes, issuer, audience);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		def jwtAuthnToken = super.authenticate(authentication) as AuthenticationJsonWebToken
		def jwt = JWT.decode(jwtAuthnToken.token)

		//OK if they don't have an operator ID
		if (jwtAuthnToken.getAuthorities().find { it.getAuthority() == "operator:unbound" }) return jwtAuthnToken

		def appMetadataClaim = jwt.getClaim("app_metadata")
		if (appMetadataClaim == null) throw new BadCredentialsException("No app_metadata provided")
		def jwtAppMetadata = new ObjectMapper().convertValue(appMetadataClaim.data, Map.class)
		def operatorId = jwtAppMetadata.get("operator_id") as long
		if (operatorId == null) throw new BadCredentialsException("Operator identifier not provided")

		//We've been here before
		if (jwtAppMetadata.get("signup_complete") as boolean) return jwtAuthnToken

		//Lets finish the sign-up/registration process
		def operator = operatorService.getOperator(operatorId).orElseThrow { new BadCredentialsException("Unknown operator") }

		operator.authUserId = jwt.getClaim("sub").asString()
		operatorService.updateOperator(operator)
		userService.updateAppMetadata(operator.authUserId,
				jwtAppMetadata + [
				"signup_complete":true,
				"shell_user":null,
				"registration_token":null,
				"token_valid_until":null])

		//delete shell user
		userService.deleteUser(jwtAppMetadata.get("shell_user") as String)

		return jwtAuthnToken
	}
}

