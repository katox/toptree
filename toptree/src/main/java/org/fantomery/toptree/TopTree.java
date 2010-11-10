/*
 *  Top Tree interface implementation
 * 
 *  The package toptree implements a dynamic collection of trees known
 *  as Top Trees (S. Alstrup et al.). This is a direct implementation 
 *  of the Top Tree interface utilizing vertex disjoint paths with an extra 
 *  maintained map of vertex information. For more information see the paper 
 *  "Self-Adjusting Top Trees" by Renato F. Werneck and Robert E. Tarjan.   
 *  
 *  Copyright (C) 2005  Kamil Toman
 *  Copyright (C) 2008  Michal Vajbar
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *  
 *  Developed by:	Kamil Toman, Michal Vajbar
 *  				Charles University in Prague
 *  				Faculty of Mathematics and Physics
 *					kamil.toman@gmail.com, michal.vajbar@tiscali.cz
 */
package org.fantomery.toptree;

import java.util.ArrayList;


/**
 * An interface for representing and managing the TopTree data structure.
 * <p>
 * It contains methods for obtaining information about this Top Tree (number of vertices or edges),
 * a method for creating vertices and others methods.
 * <p>
 * Crucial part of this interface are methods for providing expose, link and cut operations (described
 * in section 4 in <a href="doc-files/TW05.pdf">R. E. Tarjan, R. F. Werneck: Self-Adjusting Top Trees</a>). 
 *  
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since   1.0
 *
 * @param <C> A user defined object with information about the cluster.
 * @param <V> A user defined object with information about the vertex.
 */
public interface TopTree<C,V> {

	/**
	 * A list of possible returned values of method {@link toptree.TopTree#expose(Vertex)}.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since   1.0
	 */
	public enum ExposeOneResults {
		
		/**
		 * The given vertex is a single vertex.
		 */
		SINGLE,
		
		/**
		 * The given vertex is not single and it is a part of some component. 
		 */
		COMPONENT 
	}
	
	/**
	 * A list of possible returned values of method {@link toptree.TopTree#expose(Vertex, Vertex)}.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since   1.0
	 */
	public enum ExposeTwoResults {
		
		/**
		 * The left of given vertices is a single vertex and the right is a part of some component.
		 */
		LEFT_SINGLE,
		
		/**
		 * The left of given vertices is a part of some component and the right is a single vertex.
		 */
		RIGHT_SINGLE,
		
		/**
		 * Both of given vertices are single. It is not explored if the vertices are the same vertex.
		 */
		BOTH_SINGLE,
		
		/**
		 * Given vertices are one vertex. This vertex is not single.
		 */
		ONE_VERTEX,
		
		/**
		 * None of given vertices is single and both vertices are connected in some component.
		 */
		COMMON_COMPONENT,
		
		/**
		 * None of given vertices is single and vertices are parts of two different components.
		 */
		DIFFERENT_COMPONENTS
	}
	
	/**
	 * This method creates new vertex with given user defined information and 
	 * returns this new vertex. 
	 * 
	 * @param info User defined information for new vertex.
	 * @return New created vertex.
	 */
	public abstract Vertex<V> createNewVertex(V info);


	/**
	 * A method for obtaining quantity of vertices in this Top Tree.
	 * 
	 * @return A number of vertices in this Top Tree.
	 */
	public abstract int getNumOfVertices();


	/**
	 * A method for obtaining quantity of edges in this Top Tree.
	 * 
	 * @return A number of edges in this Top Tree.
	 */
	public abstract int getNumOfEdges();


	/**
	 * A method for obtaining number of component in this Top Tree.
	 * 
	 * @return A number of number in this Top Tree.
	 */
	public abstract int getNumOfComponents();


	/**
	 * This method returns for given vertex a top cluster (component) whose
	 * part the vertex is.
	 * 
	 * @param uf A vertex whose top cluster is finding.
	 * @return A top cluster of given vertex.
	 */
	public abstract Cluster<C,V> getTopComponent(Vertex<V> uf);


	/**
	 * This method rebuilds the Top Tree so that a given vertex will be 
	 * represented on the root path and so a handler of this vertex 
	 * will be a root of the Top Tree. 
	 * 
	 * @param u	A vertex to expose.
	 * @return	A value of enumeration type <code>ExposeOneResults</code> defined in this interface.
	 */
	public abstract ExposeOneResults expose(Vertex<V> u);


	/**
	 * This method rebuilds the Top Tree so that given vertices will be 
	 * the end-points on the root path and so a handler of these vertices 
	 * will be a root of the Top Tree. 
	 * 
	 * @param u	A vertex to expose.
	 * @param v	A vertex to expose.
	 * @return	A value of enumeration type <code>ExposeTwoResults</code> defined in this interface.
	 */
	public abstract ExposeTwoResults expose(Vertex<V> u, Vertex<V> v);


	/**
	 * A method creates an edge with given user defined information between given two vertices 
	 * where position of the edge is not important. It creates the edge somewhere around the vertices. 
	 * If any problem occurs during linking an exception is thrown.
	 * 
	 * @param uf A vertex that will be the end-point of created edge.
	 * @param vf A vertex that will be the end-point of created edge.
	 * @param info An information to be assign to the new edge.
	 * @throws TopTreeException If any problem occurs while creating the new edge.
	 */
	public abstract void link(Vertex<V> uf, Vertex<V> vf, C info) throws TopTreeException;


	/**
	 * A method creates an edge with given user defined information between given two vertices 
	 * where position of the edge around second vertex is specified by third parameter.
	 * <p>
	 * Position is specified by an edge <code>(b,v)</code> in cyclic order of edges around
	 * vertex <code>vf</code>, so new edge will be successor of <code>(b,v)</code>.
	 * If any problem occurs during linking an exception is thrown.
	 * 
	 * @param uf A vertex that will be the end-point of created edge.
	 * @param vf A vertex that will be the end-point of created edge.
	 * @param bf A vertex that signs edge <code>(b,v)</code> which will be a predecessor
	 * 			 of new edge in the sense of cyclic order around vertex <code>vf</code>.
	 * @param info An information to be assign to the new edge.
	 * @throws TopTreeException If any problem occurs while creating the new edge.
	 */
	public abstract void link(Vertex<V> uf, Vertex<V> vf, Vertex<V> bf, C info) throws TopTreeException;


	/**
	 * A method creates an edge with given user defined information between given two vertices 
	 * where position of the edge around given vertices is specified by third and forth parameter.
	 * <p>
	 * Position is specified by edges <code>(a,u)</code> and <code>(b,v)</code> in cyclic order 
	 * of edges around vertices <code>uf</code> and <code>vf</code>, so new edge will be successor
	 * of <code>(a,u)</code> in vertex <code>uf</code> and successor of <code>(b,v)</code> 
	 * in vertex <code>vf</code>.
	 * If any problem occurs during linking an exception is thrown.
	 * 
	 * @param uf A vertex that will be the end-point of created edge.
	 * @param af A vertex that signs edge <code>(a,u)</code> which will be a predecessor
	 * 			 of new edge in the sense of cyclic order around vertex <code>uf</code>.
	 * @param vf A vertex that will be the end-point of created edge.
	 * @param bf A vertex that signs edge <code>(b,v)</code> which will be a predecessor
	 * 			 of new edge in the sense of cyclic order around vertex <code>vf</code>.
	 * @param info An information to be assign to the new edge.
	 * @throws TopTreeException If any problem occurs while creating the new edge.
	 */
	public abstract void link(Vertex<V> uf, Vertex<V> af, Vertex<V> vf, Vertex<V> bf, C info)
			throws TopTreeException;


	/**
	 * This method removes an edge between two given vertices.
	 * It throws an exception if there any problem occurs while deleting the edge.
	 * 
	 * @param uf A vertex which is one end-point of removing edge.
	 * @param vf A vertex which is second end-point of removing edge.
	 * @throws TopTreeException If any problem occurs while deleting the edge.
	 */
	public abstract void cut(Vertex<V> uf, Vertex<V> vf) throws TopTreeException;
	
	
	/**
	 * This method provides finding out a base cluster that contains the result of given non-local 
	 * algorithm. The algorithm is specified by user defined decision in method 
	 * {@link toptree.TopTreeListener#selectQuestion(Cluster, Cluster, toptree.Cluster.ConnectionType)}
	 * that chooses one of given child clusters.
	 * <p>
	 * A selection starts in a root cluster of appropriate tree and works recursively. If given vertex is
	 * single, then there is nothing to do and method returns <code>null</code> value else the finding 
	 * starts. In every step it decides if to take a left or a right son of current cluster for next 
	 * iteration. When it takes a base cluster, the finding ends. Result is in this base cluster whose two
	 * boundary vertices are returned.
	 * 
	 * @param v	A vertex that determines a component of Top Tree for selecting.
	 * @return	Vertices that creates a base cluster that is result of selecting or <code>null</code>
	 * 			if the given vertex is single.
	 */
	public abstract ArrayList<Vertex<V>> select(Vertex<V> v);
	
	
	/**
	 * This method provides finding out a base cluster that contains the result of given non-local 
	 * algorithm on a way specified by vertices <code>u</code> and <code>v</code>.
	 * The algorithm is specified by user defined decision in method 
	 * {@link toptree.TopTreeListener#selectQuestion(Cluster, Cluster, toptree.Cluster.ConnectionType)}
	 * that chooses one of given child clusters.
	 * <p>
	 * A selection starts in a root cluster of appropriate tree and works recursively. If given vertices
	 * <code>u</code> and <code>v</code> aren't in the same component, then there is nothing to do and
	 * the method returns <code>null</code> value else the finding starts. It works similarly as
	 * {@link toptree.TopTree#select(Vertex)}. Only difference is that user defined function
	 * <code>selectQuestion</code> is called only if both sons are path children of paternal path cluster.
	 * If one of them is point cluster, then it automatically takes the path child for next iteration.
	 * 
	 * @param u	A vertex that is one of end-points of required way for selecting.
	 * @param v	A vertex that is second end-points of required way for selecting.
	 * @return	Vertices that creates a base cluster that is result of selecting or <code>null</code>
	 * 			if the given vertices are not in the same component.
	 */
	public abstract ArrayList<Vertex<V>> select(Vertex<V> u, Vertex<V> v);
	
}
