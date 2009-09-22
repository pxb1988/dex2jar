/**
 * 
 */
package test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.opensymphony.xwork2.inject.Inject;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
@Entity(name = "a_entity")
@Table
@Action(params = { "13", "1f" })
public class Anno {
	@Id
	@Column(name = "a_field_id")
	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REMOVE })
	private String id;

	// {
	// fv = cw.visitField(ACC_PRIVATE, "id", "Ljava/lang/String;", null, null);
	// {
	// av0 = fv.visitAnnotation("Ljavax/persistence/Id;", true);
	// av0.visitEnd();
	// }
	// {
	// av0 = fv.visitAnnotation("Ljavax/persistence/Column;", true);
	// av0.visit("name", "a_field_id");
	// av0.visitEnd();
	// }
	// {
	// av0 = fv.visitAnnotation("Ljavax/persistence/OneToMany;", true);
	// {
	// AnnotationVisitor av1 = av0.visitArray("cascade");
	// av1.visitEnum(null, "Ljavax/persistence/CascadeType;", "MERGE");
	// av1.visitEnum(null, "Ljavax/persistence/CascadeType;", "REMOVE");
	// av1.visitEnd();
	// }
	// av0.visitEnd();
	// }
	// fv.visitEnd();
	// }
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "a_method_id")
	public String getId() {
		return id;
	}

	@Actions(@Action(""))
	@Actionsx(value = @Action, t = Anno.class, ts = Anno.class)
	@Autowired
	public void methodABC(@Inject @Qualifier("asdf") String a, @Qualifier("asdfxxx") String b) {
	}

	@Target( { java.lang.annotation.ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Actionsx {
		public abstract Action value();

		public Class<?> t();

		public Class<?>[] ts();
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
