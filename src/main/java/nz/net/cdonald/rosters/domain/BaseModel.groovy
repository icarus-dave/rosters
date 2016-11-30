package nz.net.cdonald.rosters.domain

import com.avaje.ebean.Model
import com.avaje.ebean.annotation.JsonIgnore
import com.avaje.ebean.annotation.WhenCreated
import com.avaje.ebean.annotation.WhenModified
import groovy.transform.CompileStatic

import javax.persistence.*
import java.sql.Timestamp

@MappedSuperclass
@CompileStatic
public class BaseModel extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	Long id;

	@Version
	@JsonIgnore
	Long version;

	@WhenCreated
	@JsonIgnore
	Timestamp whenCreated;

	@WhenModified
	@JsonIgnore
	Timestamp whenModified;

}