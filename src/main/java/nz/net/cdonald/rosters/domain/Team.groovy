package nz.net.cdonald.rosters.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import groovy.transform.CompileStatic

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Transient

@Entity
@CompileStatic
@JsonIgnoreProperties(value=["members","teamLead"], allowGetters=true)
public class Team extends BaseModel {

	@Column(nullable = false)
	String name

	@OneToMany(cascade=CascadeType.PERSIST, mappedBy = "team")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	Set<TeamMember> members = new HashSet<>()

	@ManyToOne(fetch=FetchType.EAGER)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY,value="team_lead")
	Operator teamLead

	@Transient
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JsonView(RelationshipView.TeamCreate)
	long team_lead_id

	@Transient
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@JsonView(RelationshipView.TeamCreate)
	Set<Long> member_ids = new HashSet<>()

}
