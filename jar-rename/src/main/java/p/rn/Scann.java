package p.rn;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

import p.rn.ClassInfo.MemberInfo;
import p.rn.asm.ScanBridgeAdapter;
import p.rn.asm.ScanLibVisitor;
import p.rn.util.FileWalker;
import p.rn.util.FileWalker.StreamHandler;
import p.rn.util.FileWalker.StreamOpener;

public class Scann {
    public static Map<String, MemberInfo> scanBridge(File file) throws IOException {
        final ScanBridgeAdapter slv = new ScanBridgeAdapter(new EmptyVisitor());

        new FileWalker().withStreamHandler(new StreamHandler() {

            @Override
            public void handle(boolean isDir, String name, StreamOpener current, Object nameObject) throws IOException {
                if ((!isDir) && name.endsWith(".class")) {
                    new ClassReader(current.get()).accept(slv, 0);
                }
            }
        }).walk(file);
        return slv.getBridge();
    }

    public static Map<String, ClassInfo> scanLib(File file) throws IOException {
        final ScanLibVisitor slv = new ScanLibVisitor();
        new FileWalker().withStreamHandler(new StreamHandler() {

            @Override
            public void handle(boolean isDir, String name, StreamOpener current, Object nameObject) throws IOException {
                if ((!isDir) && name.endsWith(".class")) {
                    new ClassReader(current.get()).accept(slv, ClassReader.SKIP_CODE);
                }
            }
        }).walk(file);
        return slv.getClassMap();
    }
}
