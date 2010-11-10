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
 * An interface for describing how to compute user defined information for clusters 
 * when they are created, joined, destroyed or split.
 * <p>
 * Every cluster holds some user defined information. When the cluster changes, 
 * then the information can change too. There are four types of change that must
 * be differentiated. These are creating or destroying new cluster and joining or splitting 
 * existing clusters.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since   1.0
 * @see     toptree.Cluster
 *
 * @param <C> A user defined object with information about the cluster.
 * @param <V> A user defined object with information about the vertex.
 */
public interface TopTreeListener<C,V> {

	/**
	 * A method which describes the way how to count user defined information for
	 * a new given cluster. 
	 * 
	 * @param c A new cluster for which will be counted information.
	 * @param type	A type of cluster <code>c</code> - a point or a path cluster.
	 */
	public void create(Cluster<C,V> c, Cluster.ClusterType type);


	/**
	 * A method which describes the way how to count user defined information
	 * a given cluster if destroying. 
	 * 
	 * @param c A destroying cluster for which will be counted information.
	 * @param type	A type of cluster <code>c</code> - a point or a path cluster.
	 */
	public void destroy(Cluster<C,V> c, Cluster.ClusterType type);


	/**
	 * A method which describes the way how to count user defined information when
	 * clusters <code>a</code> and <code>b</code> are joining into a cluster <code>c</code>.
	 * A parameter <code>type</code> tells how are clusters <code>a</code> and <code>b</code> connected
	 * in cluster <code>c</code>. For more information about type of connection look into interface
	 * {@link toptree.Cluster} on enumeration type <code>ConnectionType</code>.
	 * 
	 * @param c	A cluster which are other two clusters joining into.
	 * @param a	A cluster which is joining.
	 * @param b	A cluster which is joining.
	 * @param type A type of connection clusters <code>a</code> and <code>b</code> in cluster <code>c</code>.
	 */
	public void join(Cluster<C,V> c, Cluster<C,V> a, Cluster<C,V> b, Cluster.ConnectionType type);


	/**
	 * A method which describes the way how to count user defined information when
	 * a cluster <code>c</code> is splitting into clusters <code>a</code> <code>b</code>.
	 * A parameter <code>type</code> tells how are clusters <code>a</code> and <code>b</code> connected
	 * in cluster <code>c</code>. For more information about type of connection look into interface
	 * {@link toptree.Cluster} on enumeration type <code>ConnectionType</code>.
	 * 
	 * @param a A cluster for first part of splitting cluster.
	 * @param b A cluster for second part of splitting cluster.
	 * @param c A cluster which is splitting.
	 * @param type A type of connection clusters <code>a</code> and <code>b</code> in cluster <code>c</code>.
	 */
	public void split(Cluster<C,V> a, Cluster<C,V> b, Cluster<C,V> c, Cluster.ConnectionType type);
	
	
	/**
	 * This method choose one from given clusters and returns it. Clusters <code>a</code> and 
	 * <code>b</code> must be children of one parental cluster.
	 * A parameter <code>type</code> tells how are clusters <code>a</code> and <code>b</code> connected
	 * in parental cluster. For more information about type of connection look into interface
	 * {@link toptree.Cluster} on enumeration type <code>ConnectionType</code>.
	 * 
	 * @param a	A cluster that is a child of the same cluster as a cluster <code>b</code>.
	 * @param b	A cluster that is a child of the same cluster as a cluster <code>a</code>.
	 * @param type	A type of connection clusters <code>a</code> and <code>b</code> in parental cluster.
	 * @return	A child cluster of given cluster.
	 */
	public Cluster<C,V> selectQuestion(Cluster<C,V> a, Cluster<C,V> b, Cluster.ConnectionType type);

}
