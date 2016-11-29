package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic

import javax.persistence.*

@Entity
@CompileStatic
public class Shift extends BaseModel {

	@ManyToOne(fetch = FetchType.LAZY)
	@Column(nullable = false)
	Duty duty;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	Date start;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	Date finish;

	Operator operator;

}
