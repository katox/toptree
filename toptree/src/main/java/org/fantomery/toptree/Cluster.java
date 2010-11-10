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


/**
 * An interface for holding information about one cluster and managing them.
 * It manages left and right boundary vertices of this cluster and user defined
 * information about this cluster in a user defined object.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 *
 * @param <C> A user defined object with information about the cluster.
 * @param <V> A user defined object with information about the vertex.
 */
public interface Cluster<C,V> {

	
	/**
	 * A list of all possible types of clusters. There are only two types. A point cluster that has one
	 * boundary vertex and a path cluster that has two boundary vertices.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since   1.0
	 */
	public enum ClusterType {
		
		/** A point cluster with one boundary vertex. */
		TYPE_POINT_CLUSTER,
		
		/** A path cluster with two boundary vertices. */
		TYPE_PATH_CLUSTER
	};
	
	/**
	 * A list of all possible connection of two clusters into one parental cluster. Cases correspond to
	 * division (including symmetric variant) as specified a figure 1 in article 
	 * <a href="doc-files/p243-alstrup.pdf">Maintaining Information in Fully Dynamic Trees with Top Trees</a>
	 * from Alstrup, Holm, de Lichtenberg, Thorup.
	 * 
 	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since   1.0
	 */
	public enum ConnectionType {
		
		/**
		 * A parental cluster is path cluster and both children are path clusters with both boundary 
		 * vertices - fig 1: (1).<br />
		 * A common vertex: right boundary of the left child and left boundary of the right child.<br />
		 * Boundaries of parental cluster: left is left boundary of the left child and right is right 
		 * boundary of the right child.
		 */
		TYPE_PATH_AND_PATH,
		
		/**
		 * A parental cluster is path cluster, a left child is a path with both boundary vertices 
		 * and a right child is a point cluster with only right boundary vertex - fig 1: (2).<br />
		 * A common vertex: right boundary of the left child and right (the only one) boundary of 
		 * the right child.<br />
		 * Boundaries of parental cluster: same as boundary vertices of the left child.
		 */
		TYPE_PATH_AND_POINT,
		
		/**
		 * A parental cluster is path cluster, a left child is a point cluster with only right 
		 * boundary vertex and a right child is a path with both boundary 
		 * vertices - fig 1: (2) (symmetric variant).<br />
		 * A common vertex: left boundary of the right child and right (the only one) boundary of 
		 * the left child.<br />
		 * Boundaries of parental cluster: same as boundary vertices of the right child.
		 */
		TYPE_POINT_AND_PATH,

		/**
		 * A parental cluster is a point cluster and both children are path clusters with both 
		 * boundary vertices - fig 1: (3).<br />
		 * A common vertex: right boundary of the left child and left boundary of the right child.<br />
		 * Boundary of parental cluster: only one vertex - left boundary of the left child.
		 */
		TYPE_LPOINT_AND_RPOINT,

		/**
		 * A parental cluster is a point cluster and both children are path clusters with both 
		 * boundary vertices - fig 1: (4).<br />
		 * A common vertex: right boundary of the left child and left boundary of the right child.<br />
		 * Boundary of parental cluster: only one vertex - right boundary of the right child.
		 */
		TYPE_LPOINT_OVER_RPOINT,
		
		/**
		 * A parental cluster is a point cluster and both children are path clusters with both 
		 * boundary vertices - fig 1: (4) (symmetric variant).<br />
		 * A common vertex: right boundary of the left child and left boundary of the right child.<br />
		 * Boundary of parental cluster: only one vertex - the common vertex.
		 */
		TYPE_RPOINT_OVER_LPOINT,
		
		/**
		 * A parental cluster is a point cluster and both children are point clusters with only right
		 * boundary vertices - fig 1: (5).<br />
		 * A common vertex: right (the only one) vertices of both children clusters.<br />
		 * Boundary of parental cluster: the same only one vertex as boundary vertex of both children.
		 */
		TYPE_POINT_AND_POINT
	};

	
	/**
	 * A method for getting user defined object with information about this cluster.
	 * 
	 * @return User defined object with information about this cluster.
	 */
	public abstract C getInfo();


	/**
	 * A method for setting user defined object with new information about this cluster.
	 * 
	 * @param info User defined object with new information about this cluster.
	 */
	public abstract void setInfo(C info);


	/**
	 * A method returns information about left boundary vertex which are hold in user
	 * defined object in {@link toptree.Vertex}.
	 * 
	 * @return User defined object with information about left boundary vertex of this cluster.
	 */
	public abstract V getBu();


	/**
	 * A method for setting new information to left boundary vertex whose are hold in user
	 * defined object in {@link toptree.Vertex}.
	 * 
	 * @param info User defined object with new information about left boundary vertex of this cluster.
	 */
	public abstract void setBu(V info);


	/**
	 * A method returns information about right boundary vertex which are hold in user
	 * defined object in {@link toptree.Vertex}.
	 * 
	 * @return User defined object with information about right boundary vertex of this cluster.
	 */
	public abstract V getBv();


	/**
	 * A method for setting new information to right boundary vertex whose are hold in user
	 * defined object in {@link toptree.Vertex}.
	 * 
	 * @param info User defined object with new information about right boundary vertex of this cluster.
	 */
	public abstract void setBv(V info);

}
