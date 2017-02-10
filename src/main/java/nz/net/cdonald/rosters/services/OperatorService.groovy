package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import com.auth0.jwt.impl.NullClaim
import com.auth0.jwt.interfaces.Claim
import com.auth0.spring.security.api.authentication.AuthenticationJsonWebToken
import com.avaje.ebean.EbeanServer
import com.fasterxml.jackson.databind.ObjectMapper
import nz.net.cdonald.rosters.domain.Operator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class OperatorService {

	static final Logger logger = LoggerFactory.getLogger(OperatorService.class)

	@Autowired
	EbeanServer server

	public List<Operator> getOperators() {
		def list = server.find(Operator.class).orderBy("UPPER(lastName)").findList()
		logger.debug("Found {} operators", list.size())
		return list
	}

	public Optional<Operator> getOperator(long operatorId) {
		logger.debug("Searching for operator {}", operatorId)
		def op = server.find(Operator.class).where().eq("id", operatorId).findUnique();
		if (op) logger.debug("Found operator for {}:{}", op.id, op.email)
		else logger.info("Operator {} not found", operatorId)
		return Optional.ofNullable(op)
	}

	public Operator createOperator(Operator operator) {
		if (!validateEmail(operator.email)) throw new IllegalArgumentException("Email invalid")

		//provide a nicer exception than would otherwise be thrown (and also case insensitive)
		if (findByEmail(operator.email)) throw new IllegalArgumentException("Email already in use")

		operator.id = null
		operator.email = operator.email.trim().toLowerCase()

		server.save(operator)
		logger.info("Created operator {}:{}", operator.id, operator.email)
		return operator
	}

	public Operator updateOperator(Operator operator) {
		if (operator.email) {
			if (!validateEmail(operator.email)) throw new IllegalArgumentException("Email invalid")

			operator.email = operator.email.trim().toLowerCase()

			def exists = findByEmail(operator.email)

			//provide a nicer exception than would otherwise be thrown
			if (exists && exists.id != operator.id) throw new IllegalArgumentException("Email already in use")
		}

		server.update(operator)
		logger.info("Updated operator {}:{}", operator.id, operator.email)
		return operator
	}

	def Operator findByEmail(String email) {
		return server.find(Operator.class).where().ieq("email", email).findUnique()
	}

	def validateEmail(String email) {
		email != null && email ==~ /.+\@.+\..+/
	}

	//Match the operator_id in the JWT to the user they're trying to modify
	def boolean authzOperatorUpdate(long id, Authentication authn) {
		//we just need to check that the ID matches operator_id in the token
		def jwtAuthn = authn as AuthenticationJsonWebToken
		def appMetadata = jwtAuthn.getDetails().getClaim("app_metadata") as Claim
		if (appMetadata instanceof NullClaim) return false

		def objectMapper = new ObjectMapper()
		def jwtAppMetadata = objectMapper.convertValue(appMetadata.data, Map.class)

		return jwtAppMetadata.get("operator_id") == id || jwtAppMetadata.get("operator_id").toString().equals("" + id)
	}

}

