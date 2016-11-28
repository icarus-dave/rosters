package nz.net.cdonald.rosters.domain;

import java.io.Serializable;

public class TeamMembersId implements Serializable {
    long teamId;
    long operatorId;

    public int hashCode() {
        return (int)(teamId + operatorId);
    }

    public boolean equals(Object object) {
        if (object instanceof TeamMembersId) {
            TeamMembersId otherId = (TeamMembersId)object;
            return (otherId.teamId == this.teamId && otherId.operatorId == this.operatorId);
        }
        return false;
    }
}
