package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic

import javax.persistence.*

@Entity
@IdClass(TeamMembersId.class)
@CompileStatic
public class TeamMembers {

	@EmbeddedId
	long operatorId;

	@EmbeddedId
	long teamId;

	@ManyToOne
	Operator operator;

	@ManyToOne
	Team team;

	@Column
	long rosterWeighting;

}
