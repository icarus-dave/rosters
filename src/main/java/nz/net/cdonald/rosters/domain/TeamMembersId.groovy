package nz.net.cdonald.rosters.domain

import javax.persistence.Embeddable

@Embeddable
public class TeamMembersId implements Serializable {
	public Long team_id = 0;
	public Long operator_id = 0;

	public int hashCode() {
		return (int) (team_id + operator_id);
	}

	public boolean equals(Object object) {
		if (object instanceof TeamMembersId) {
			TeamMembersId otherId = (TeamMembersId) object;
			return (otherId.team_id == this.team_id && otherId.operator_id == this.operator_id);
		}
		return false;
	}
}
