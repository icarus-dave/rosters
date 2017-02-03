package nz.net.cdonald.rosters.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import groovy.transform.CompileStatic

import javax.persistence.*

@Entity
@CompileStatic
@JsonIgnoreProperties(value=["operator","team"], allowGetters=true)
public class TeamMember {

	@EmbeddedId
	@JsonIgnore
	TeamMembersId id = new TeamMembersId()

	@ManyToOne
	@JoinColumn(name = "operator_id", insertable = false, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonView([RelationshipView.Team,RelationshipView.TeamMember])
	Operator operator

	@ManyToOne
	@JoinColumn(name = "team_id", insertable = false, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonView(RelationshipView.Operator)
	Team team

	@Column
	@JsonProperty("roster_weighting")
	long rosterWeighting

	//use this for setting operator IDs as part of the API
	@Transient
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JsonView(RelationshipView.TeamMember)
	long operator_id

	public void setOperator(Operator operator) {
		this.operator = operator
		id.operator_id = operator.id
	}

	public void setTeam(Team team) {
		this.team = team;
		id.team_id = team.id
	}

}

class RelationshipView {
	public static class Team {}
	public static class Operator {}
	public static class TeamMember {}
	public static class TeamCreate {}
}
