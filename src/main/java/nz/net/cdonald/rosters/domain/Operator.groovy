package nz.net.cdonald.rosters.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import groovy.transform.CompileStatic

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
@CompileStatic
@JsonIgnoreProperties(value=["teams"], allowGetters=true)
public class Operator extends BaseModel {

	boolean active = true;

	@Column(nullable = false)
	@JsonProperty("first_name")
	String firstName;

	@Column(nullable = false)
	@JsonProperty("last_name")
	String lastName;

	@Column(nullable = false, unique = true)
	String email;

	@Column
	@JsonIgnore
	String authUserId;

	@OneToMany(cascade=CascadeType.PERSIST, mappedBy = "operator")
	@JsonView(RelationshipView.Operator)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	Set<TeamMember> teams = new HashSet()

}
