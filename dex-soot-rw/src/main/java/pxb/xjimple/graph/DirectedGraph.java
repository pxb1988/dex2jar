/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrice Pominville, Raja Vallee-Rai
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Defines the notion of a directed graph.
 * 
 * @param N
 *            node type
 */
public interface DirectedGraph<N> extends Iterable<N> {

    /**
     * Returns a list of entry points for this graph.
     */
    public Set<N> getHeads();

    /** Returns a list of exit points for this graph. */
    public Set<N> getTails();

    /**
     * Returns a list of predecessors for the given node in the graph.
     */
    public List<N> getPredsOf(N s);

    /**
     * Returns a list of successors for the given node in the graph.
     */
    public List<N> getSuccsOf(N s);

    /**
     * Returns the node count for this graph.
     */
    public int size();

    /**
     * Returns an iterator for the nodes in this graph. No specific ordering of the nodes is guaranteed.
     */
    public Iterator<N> iterator();
}
