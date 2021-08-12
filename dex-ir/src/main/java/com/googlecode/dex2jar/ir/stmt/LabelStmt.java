package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a Label statement
 *
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 * @see ST#LABEL
 */
public class LabelStmt extends E0Stmt {

    public String displayName;

    public int lineNumber = -1;

    public List<AssignStmt> phis;

    public Object tag;

    public LabelStmt() {
        super(ST.LABEL);
    }

    @Override
    public LabelStmt clone(LabelAndLocalMapper mapper) {
        LabelStmt labelStmt = mapper.map(this);
        if (phis != null && labelStmt.phis == null) {
            labelStmt.phis = new ArrayList<>(phis.size());
            for (AssignStmt phi : phis) {
                labelStmt.phis.add((AssignStmt) phi.clone(mapper));
            }
        }
        return labelStmt;
    }

    public String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        int x = hashCode();
        return String.format("L%08x", x);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayName()).append(":");

        if (phis != null && phis.size() > 0) {
            sb.append(" // ").append(phis);
        }
        if (lineNumber >= 0) {
            sb.append(" // line ").append(lineNumber);
        }
        return sb.toString();
    }

}
