package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
@CompileStatic
public class Operator extends BaseModel {

	boolean active = true;

	@Column(nullable = false)
	String firstName;

	@Column(nullable = false)
	String lastName;

	@Column(nullable = false, unique = true)
	String email;

	@OneToMany(mappedBy = "operator")
	List<TeamMembers> teams;
}
