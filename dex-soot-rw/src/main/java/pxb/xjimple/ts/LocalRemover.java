package pxb.xjimple.ts;

import java.util.Iterator;

import pxb.xjimple.JimpleMethod;
import pxb.xjimple.Local;
import pxb.xjimple.Value.VT;
import pxb.xjimple.stmt.AssignStmt;
import pxb.xjimple.stmt.Stmt;

public class LocalRemover implements Transformer {

    @Override
    public void transform(JimpleMethod je) {
        for (Iterator<Stmt> it = je.stmts.iterator(); it.hasNext();) {
            Stmt st = it.next();
            switch (st.st) {
            case ASSIGN:
                AssignStmt as = (AssignStmt) st;
                if (as.left.value.vt == VT.LOCAL && as.right.value.vt == VT.LOCAL) {
                    Local a = (Local) as.left.value;
                    Local b = (Local) as.right.value;
                    if (a._ls_write_count == 1) {
                        b._ls_read_count += a._ls_read_count;
                        je.locals.remove(a);
                        a._ls_vb.value = b;
                        it.remove();
                        continue;
                    }
                }
            }
        }
    }
}
