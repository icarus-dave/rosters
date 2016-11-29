package nz.net.cdonald.rosters.services

import com.avaje.ebean.EbeanServer
import nz.net.cdonald.rosters.domain.Operator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OperatorService {

	@Autowired
	EbeanServer server

	public List<Operator> getOperators() {
		return server.find(Operator.class).orderBy("UPPER(lastName)").findList()
	}

	public Operator getOperator(long operatorId) {
		return server.find(Operator.class).where().eq("id", operatorId).findUnique();
	}

	public Operator createOperator(Operator operator) {
		server.save(operator)
		return operator
	}

	public Operator updateOperator(Operator operator) {
		server.update(operator)
		return operator
	}

}

