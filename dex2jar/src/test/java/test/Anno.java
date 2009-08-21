/**
 * 
 */
package test;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
@Entity(name = "a_entity")
@Table
public class Anno {
	@Id
	@Column(name = "a_field_id")
	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE })
	private String id;

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "a_method_id")
	public String getId() {
		return id;
	}

	@Autowired
	public void methodABC(@Inject @Qualifier("asdf") String a, @Qualifier("asdfxxx") String b) {
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
