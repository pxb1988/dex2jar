/* Soot - a J*va Optimization Framework
 * Copyright (C) 2006 Eric Bodden
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
package pxb.xjimple.graph;

import java.util.List;

/**
 * An orderer builds an order on a directed, not necessarily acyclic, graph.
 * 
 * @author Eric Bodden
 */
public interface Orderer<N> {

    /**
     * Builds an order for a directed graph. The order is represented by the returned list, i.e. is a node was assigned
     * number <i>i</i> in the order, it will be in the <i>i</i>th position of the returned list.
     * 
     * @param g
     *            a DirectedGraph instance whose nodes we wish to order
     * @param reverse
     *            <code>true</code> to compute the reverse order
     * @return a somehow ordered list of the graph's nodes
     */
    public abstract List<N> newList(DirectedGraph<N> g, boolean reverse);
}