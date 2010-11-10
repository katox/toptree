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
package org.fantomery.toptree.impl;

import org.fantomery.toptree.Vertex;


/**
 * This class implements the Vertex interface to represent and manage one vertex of the TopTree. 
 * There are held user defined information about this vertex in user defined object, a degree 
 * and a reference to a parental cluster of this vertex.   
 *  
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 *
 * @param <V> A user defined object with information about the vertex.
 */
class VertexInfo<V> implements Vertex<V> {

	/**
	 * User defined object to hold user defined information about this vertex.
	 */
	private Object info;

	/**
	 * A field for degree of this vertex.
	 */
	private int degree;

	/**
	 * A handle of parental cluster to which this vertex belongs to.
	 */
	private ClusterNode cluster; // handle


	/**
 	 * A constructor creates a new instance of VertexInfo class.
 	 * The instance gets user defined information about vertex in parameter 
 	 * <code>info</code>. A degree of new vertex is <code>0</code>. 
 	 *  
	 * @param info User defined object with information about new vertex.
	 */
	VertexInfo(Object info) {
		this.info = info;
		this.degree = 0;
	}


	/**
	 * A method for obtaining user defined information about this vertex.
	 * <p>
	 * The parental cluster of this vertex must be the top cluster of the top tree. 
	 * information are returned in user defined object.
	 * 
	 * @return User defined object with information about this vertex.
	 * @throws TopTreeIllegalAccessException If vertex is not a part of the top cluster.
	 * @see toptree.Vertex#getInfo()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 */
	@SuppressWarnings("unchecked")
	final public V getInfo() throws TopTreeIllegalAccessException {

		/* cluster must be the top cluster of whole top tree */
		if (cluster != null && !cluster.isTopCluster())
			throw new TopTreeIllegalAccessException(
					"Vertex access violation. Only information from exposed vertices can be retrieved.");

		return (V) info;
	}


    /**
     * A method for setting new user defined information to this vertex.
     * <p>
     * The parental cluster of this vertex must be the top cluster of the top tree.
     * information are taken over in user defined object.
     * 
     * @param info User defined object with new information.
     * @throws TopTreeIllegalAccessException If vertex is not a part of the top cluster.
     */
	final public void setInfo(V info) throws TopTreeIllegalAccessException {

		/* cluster must be the top cluster of whole top tree */
		if (cluster != null && !cluster.isTopCluster())
			throw new TopTreeIllegalAccessException(
					"Vertex access violation. Only information from exposed vertices can be retrieved.");

		this.info = info;
	}


	/**
	 * A method returns user defined object with information about this vertex. The method is similar like
	 * {@link toptree.impl.VertexInfo#getInfo()}, but it don't check the cluster to be top cluster. It can
	 * be used only for getting information of boundary vertices where the check is ensured by the cluster.
	 * 
	 * @return User defined object with information about this vertex.
	 */
	final Object getVertexInfo() {
		return info;
	}


	/*
	 * (non-Javadoc)
	 * Same documentation as:
	 * 
	 * @see toptree.impl.Vertex#getDegree()
	 */
	final public int getDegree() {
		return this.degree;
	}


	/**
	 * A method for incrementing degree of this vertex.
	 * The degree before increment must be greater then or equal to <code>0</code>. 
	 */
	final void incDegree() {
		/* Degree must be greater then or equal to 0 */
		assert degree >= 0;
		degree++;
	}


	/**
	 * A method for decrementing degree of this vertex.
	 * The degree before decrement must be greater then <code>0</code>. 
	 */
	final void decDegree() {
		/* Degree must be greater then 0 */
		assert degree > 0;
		degree--;
	}

	/**
	 * A method returns a reference to the parental cluster of this vertex.
	 * @return A paternal cluster node reference of this vertex.
	 */
	final ClusterNode getClusterNode() {
		return cluster;
	}

	/**
	 * A method for setting new parental cluster node of this vertex. 
	 * @param cluster New parental cluster node reference.
	 */
	final void setClusterNode(ClusterNode cluster) {
		this.cluster = cluster;
	}


	/**
     * This method converts and return information about this vertex from user defined object 
     * to String type. It is used for printing pictures of a tree.
     * 
     * @return User defined information about vertex in String type. 
	 */
	public String toString() {
		return ((info != null) ? info.toString() : "null");
	}

}
