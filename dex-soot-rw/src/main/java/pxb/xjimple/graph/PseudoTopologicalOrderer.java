/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai, Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */
package pxb.xjimple.graph;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Orders in pseudo-topological order, the nodes of a DirectedGraph instance.
 */

/* Updated By Marc Berndl May 13 */
public class PseudoTopologicalOrderer<N> implements Orderer<N> {

    public static final boolean REVERSE = true;

    public PseudoTopologicalOrderer() {
    }
    private Map<Object, Object> stmtToColor;
    private static final Object GRAY = new Object();
    private LinkedList<N> order;
    private boolean mIsReversed = false;
    private DirectedGraph<N> graph;
    private int[] indexStack;
    private N[] stmtStack;
    private int last;

    /**
     * {@inheritDoc}
     */
    public List<N> newList(DirectedGraph<N> g, boolean reverse) {
        this.mIsReversed = reverse;
        return computeOrder(g);
    }

    /**
     * Orders in pseudo-topological order.
     * 
     * @param g
     *            a DirectedGraph instance we want to order the nodes for.
     * @return an ordered list of the graph's nodes.
     */
    @SuppressWarnings("unchecked")
    protected List<N> computeOrder(DirectedGraph<N> g) {
        stmtToColor = new IdentityHashMap<Object, Object>((3 * g.size()) / 2);// new HashMap((3 * g.size()) / 2, 0.7f);
        indexStack = new int[g.size()];
        stmtStack = (N[]) new Object[g.size()];
        order = new LinkedList<N>();
        graph = g;

        // Visit each node
        for (N s : g) {
            if (stmtToColor.get(s) == null) {
                visitNode(s);
            }
        }
        indexStack = null;
        stmtStack = null;
        stmtToColor = null;
        return order;
    }

    // Unfortunately, the nice recursive solution fails
    // because of stack overflows
    // Fill in the 'order' list with a pseudo topological order (possibly
    // reversed)
    // list of statements starting at s. Simulates recursion with a stack.
    protected void visitNode(N startStmt) {
        last = 0;

        stmtToColor.put(startStmt, GRAY);

        stmtStack[last] = startStmt;
        indexStack[last++] = -1;
        while (last > 0) {
            int toVisitIndex = ++indexStack[last - 1];
            N toVisitNode = stmtStack[last - 1];

            List<N> succs = graph.getSuccsOf(toVisitNode);
            if (toVisitIndex >= succs.size()) {
                // Visit this node now that we ran out of children
                if (mIsReversed) {
                    order.addLast(toVisitNode);
                } else {
                    order.addFirst(toVisitNode);
                }

                last--;
            } else {
                N childNode = succs.get(toVisitIndex);

                if (stmtToColor.get(childNode) == null) {
                    stmtToColor.put(childNode, GRAY);
                    stmtStack[last] = childNode;
                    indexStack[last++] = -1;
                }
            }
        }
    }
}
