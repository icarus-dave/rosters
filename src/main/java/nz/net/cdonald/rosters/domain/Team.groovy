package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
@CompileStatic
public class Team extends BaseModel {

	@Column(nullable = false)
	String name;

	@OneToMany(mappedBy = "team")
	List<TeamMembers> members;

}
