package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic

import javax.persistence.*

@Entity
@CompileStatic
public class Duty extends BaseModel {

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	Date date;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	DutyState state;

	@OneToMany(cascade = [CascadeType.ALL])
	List<Shift> roster;

	@Column(nullable = false)
	Team team;

	@ManyToMany
	@JoinTable(name = "stand_downs")
	List<Operator> standDowns;

	@ManyToMany
	@JoinTable(name = "replacements")
	List<Operator> replacements;

}
