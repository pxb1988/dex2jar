/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

package pxb.xjimple.scalar;

import java.util.HashMap;
import java.util.Map;

import pxb.xjimple.graph.DirectedGraph;

/**
 * An abstract class providing a metaframework for carrying out dataflow analysis. This class provides common methods
 * and fields required by the BranchedFlowAnalysis and FlowAnalysis abstract classes.
 * 
 * @param <N>
 *            node type of the directed graph
 * @param <A>
 *            abstraction type
 */
public abstract class AbstractFlowAnalysis<N, A> {
    /** Maps graph nodes to IN sets. */
    protected Map<N, A> unitToBeforeFlow;

    /** Filtered: Maps graph nodes to IN sets. */
    protected Map<N, A> filterUnitToBeforeFlow;

    /** The graph being analysed. */
    protected DirectedGraph<N> graph;

    /** Constructs a flow analysis on the given <code>DirectedGraph</code>. */
    public AbstractFlowAnalysis(DirectedGraph<N> graph) {
        unitToBeforeFlow = new HashMap<N, A>(graph.size() * 2 + 1, 0.7f);
        this.graph = graph;
    }

    /**
     * Returns the flow object corresponding to the initial values for each graph node.
     */
    protected abstract A newInitialFlow();

    /**
     * Returns the initial flow value for entry/exit graph nodes.
     */
    protected abstract A entryInitialFlow();

    /**
     * Determines whether <code>entryInitialFlow()</code> is applied to trap handlers.
     */
    protected boolean treatTrapHandlersAsEntries() {
        return false;
    }

    /** Returns true if this analysis is forwards. */
    protected abstract boolean isForward();

    /**
     * Compute the merge of the <code>in1</code> and <code>in2</code> sets, putting the result into <code>out</code>.
     * The behavior of this function depends on the implementation ( it may be necessary to check whether
     * <code>in1</code> and <code>in2</code> are equal or aliased ). Used by the doAnalysis method.
     */
    protected abstract void merge(A in1, A in2, A out);

    /**
     * Merges in1 and in2 into out, just before node succNode. By default, this method just calls merge(A,A,A), ignoring
     * the node.
     */
    protected void merge(N succNode, A in1, A in2, A out) {
        merge(in1, in2, out);
    }

    /** Creates a copy of the <code>source</code> flow object in <code>dest</code>. */
    protected abstract void copy(A source, A dest);

    /**
     * Carries out the actual flow analysis. Typically called from a concrete FlowAnalysis's constructor.
     */
    protected abstract void doAnalysis();

    /** Accessor function returning value of IN set for s. */
    public A getFlowBefore(N s) {
        return unitToBeforeFlow.get(s);
    }

    /**
     * Merges in into inout, just before node succNode.
     */
    protected void mergeInto(N succNode, A inout, A in) {
        A tmp = newInitialFlow();
        merge(succNode, inout, in, tmp);
        copy(tmp, inout);
    }
}
