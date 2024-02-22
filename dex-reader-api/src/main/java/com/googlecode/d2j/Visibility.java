package com.googlecode.d2j;

/**
 * @author bob
 */
public enum Visibility {

    BUILD(0), RUNTIME(1), SYSTEM(2);

    public final int value;

    // int VISIBILITY_BUILD = 0;
    // int VISIBILITY_RUNTIME = 1;
    // int VISIBILITY_SYSTEM = 2;
    Visibility(int v) {
        this.value = v;
    }

    public String displayName() {
        return name().toLowerCase();
    }

}
