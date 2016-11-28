package nz.net.cdonald.rosters.domain;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.WhenCreated;
import com.avaje.ebean.annotation.WhenModified;
import com.avaje.ebean.annotation.WhoCreated;
import com.avaje.ebean.annotation.WhoModified
import groovy.transform.CompileStatic;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;

@MappedSuperclass
@CompileStatic
public class BaseModel extends Model {

    @Id
    Long id;

    @Version
    Long version;

    @WhenCreated
    Timestamp whenCreated;

    @WhenModified
    Timestamp whenModified;

}