package nz.net.cdonald.rosters.domain

import groovy.transform.CompileStatic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@CompileStatic
public class Team extends BaseModel {

    @Column(nullable = false)
    String name;

    @OneToMany(mappedBy="team")
    List<TeamMembers> members;

}
