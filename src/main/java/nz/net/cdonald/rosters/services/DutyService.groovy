package nz.net.cdonald.rosters.services

import com.avaje.ebean.EbeanServer
import nz.net.cdonald.rosters.domain.Duty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DutyService {

	@Autowired
	EbeanServer server

	public List<Duty> getDuties() {

		//order by date desc
	}

	public Duty getDuty(long dutyId) {

	}

	public Duty scheduleDuty(Date date) {

	}

	public void updateDuty(Duty duty) {

	}


}
