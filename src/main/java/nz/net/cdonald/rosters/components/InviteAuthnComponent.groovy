package nz.net.cdonald.rosters.components

import ch.qos.logback.classic.Logger
import com.auth0.jwt.JWT
import com.auth0.jwt.impl.NullClaim
import com.auth0.spring.security.api.JwtAuthenticationProvider
import com.auth0.spring.security.api.authentication.JwtAuthentication
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import nz.net.cdonald.rosters.services.Auth0Service
import nz.net.cdonald.rosters.services.OperatorService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component

/*
We need to verify users logging in should actually have access (i.e they have an email address for an operator)
This will also link an operator record with the Auth0 profile.

Because the first time the user logs in with a JWT without the linkage in app_metadata we'll need to inject
it each time. This will resolve itself next time they login.

We might consider making this a rule in Auth0 at some point
 */
@Component
class InviteAuthnComponent extends JwtAuthenticationProvider {

	static final Logger logger = LoggerFactory.getLogger(InviteAuthnComponent.class)

	@Autowired
	OperatorService operatorService

	@Autowired
	Auth0Service auth0Service

	@Autowired
	EbeanServer ebeanServer

	@Value('${jwt.secret}')
	String jwtSecret

	@Autowired
	public InviteAuthnComponent(@Value('${jwt.secret}') String secret,
								@Value('${jwt.issuer}') String issuer,
								@Value('${jwt.audience}') String audience) {
		super(secret.bytes,issuer,audience);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		def jwtAuthnToken = super.authenticate(authentication) as JwtAuthentication
		def jwt = JWT.decode(jwtAuthnToken.token)

		def objectMapper = new ObjectMapper()

		def appMetadataClaim = jwt.getClaim("app_metadata")

		if (!(appMetadataClaim instanceof NullClaim)) {
			def jwtAppMetadata = objectMapper.convertValue(appMetadataClaim.data, Map.class)
			def permissions = objectMapper.convertValue(jwtAppMetadata.get("permissions"),List.class)
			//if everything is kosher return as quickly as possible
			if (jwtAppMetadata.get("operator_id") != null || permissions?.contains("unbound_operator")) return jwtAuthnToken
		}

		//now check if we can link a profile
		def sub = jwt.getClaim("sub").asString()

		def profile
		try {
			profile = auth0Service.getProfile(sub)
		} catch(HttpResponseException ex) {
			logger.error("Auth0 returned an error for subject {}", sub, ex)
			if (ex.getStatusCode() == 404) throw new BadCredentialsException("Unknown subject")
			else throw ex
		}

		if (profile.email_verified == false) {
			logger.info("User {} has an unverified email address and will not be able to login", sub)
			throw new InsufficientAuthenticationException("Email must be verified first")
		}
		if (profile.email == null) {
			logger.info("User {} has no email address and will not be able to login", sub)
			throw new InsufficientAuthenticationException("Email required in authentication profile")
		}

		//quick and dirty method to ensure we don't bother with a database check for subsequent calls
		//with an operator updated but no new JWT token yet generated (i.e. first-ever login)...
		//in the future we should do some kind of cache to reduce the 2 calls to auth0
		if (profile?.app_metadata?.operator_id != null) {
			String newToken = auth0Service.updateAppMetadataJwt(jwtAuthnToken.token,
					jwtSecret,["operator_id":profile.app_metadata.operator_id])
			return super.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(newToken))
		}

		def operatorId

		//now lets transactionally update the operator and user to bind them together
		ebeanServer.beginTransaction()
		try {
			def operator = operatorService.findByEmail(profile.email)

			if (operator == null) {
				logger.info("User {} has tried to login with email {} but no matching operator found: access denied", sub, profile.email)
				throw new BadCredentialsException("Email address unknown for any operator - unable to bind user to operator")
			}

			operatorId = operator.id
			operator.authUserId = sub

			operatorService.updateOperator(operator)

			//if this fails we'll want to roll back (though in reality it's effectively idempotent)
			auth0Service.updateAppMetadata(sub, ["operator_id": operator.id])

			ebeanServer.commitTransaction()

			logger.info("Completed binding of user {} to operator {}", sub, operator.id)
		} finally {
			ebeanServer.endTransaction()
		}

		//JWT token needs to be updated (this will be reflected on the client the next time they login
		String newToken = auth0Service.updateAppMetadataJwt(jwtAuthnToken.token,jwtSecret,["operator_id":operatorId])

		//now it'll be the updated token
		return super.authenticate(PreAuthenticatedAuthenticationJsonWebToken.usingToken(newToken))
	}
}
