package nz.net.cdonald.rosters.services

import ch.qos.logback.classic.Logger
import com.avaje.ebean.EbeanServer
import nz.net.cdonald.rosters.domain.Operator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OperatorService {

	static final Logger logger = LoggerFactory.getLogger(OperatorService.class)

	@Autowired
	EbeanServer server

	public List<Operator> getOperators() {
		def list = server.find(Operator.class).orderBy("UPPER(lastName)").findList()
		logger.debug("Found ${list.size()} operators")
		return list
	}

	public Operator getOperator(long operatorId) {
		logger.debug("Searching for operator $operatorId")
		def op = server.find(Operator.class).where().eq("id", operatorId).findUnique();
		if (op) logger.info("Found operator for ${op?.id} - ${op?.email}")
		else logger.info("Operator $operatorId not found")
		return op
	}

	public Operator createOperator(Operator operator) {
		if (!validateEmail(operator.email)) throw new IllegalArgumentException("Email invalid")

		//provide a nicer exception than would otherwise be thrown (and also case insensitive)
		if (findByEmail(operator.email)) throw new IllegalArgumentException("Email already in use")

		operator.id = null
		operator.email = operator.email.trim().toLowerCase()

		server.save(operator)
		logger.info("Created operator ${operator?.id} - ${operator?.email}")
		return operator
	}

	public Operator updateOperator(Operator operator) {
		if (!validateEmail(operator.email)) throw new IllegalArgumentException("Email invalid")

		operator.email = operator.email.trim().toLowerCase()

		def exists = findByEmail(operator.email)

		//provide a nicer exception than would otherwise be thrown
		if (exists && exists.id != operator.id) throw new IllegalArgumentException("Email already in use")

		server.update(operator)
		logger.info("Updated operator ${operator?.id} - ${operator?.email}")
		return getOperator(operator.id)
	}

	def Operator findByEmail(String email) {
		return server.find(Operator.class).where().ieq("email", email).findUnique()
	}

	def validateEmail(String email) {
		email != null && email ==~ /.+\@.+\..+/
	}

}

