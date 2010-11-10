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


import org.fantomery.toptree.Cluster;


/**
 * This class represents one cluster, hold information about it and provides
 * tools for managing the cluster.
 * It is important to understand the relation between <code>ClusterInfo</code> and
 * {@link toptree.impl.ClusterNode} class. <code>ClusterInfo</code> class presents
 * one cluster - informatio about it and the tools for managing. Each cluster has
 * one cluster node which is represent by <code>ClusterNode</code> class.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 *
 * @param <C> A user defined object with information about the cluster.
 * @param <V> A user defined object with information about the vertex.
 */
class ClusterInfo<C,V> implements Cluster<C,V> {

	/**
	 * A switch if it is allowed to manage property <cone>info</code> when this cluster
	 * is not the top cluster. 
	 */
	private boolean allowedLocalAccess;

	/**
	 * A reference to the node which is cluster node of this this cluster.
	 */
	private ClusterNode cn;

	/**
	 * Holds user defined information about this cluster.
	 */
	private Object info;


	/**
	 * This constructor creates a new instance of <code>ClusterInfo</code> which 
	 * references to a given cluster. The cluster node can't be null. The property 
	 * <code>allowedLocalAccess</code> is set to <code>false</code>.
	 * 
	 * @param cn A cluster node of this new cluster.
	 */
	ClusterInfo(ClusterNode cn) {
		assert cn != null;
		this.cn = cn;
		this.allowedLocalAccess = false;
	}


	/**
	 * A method for getting user defined object with information about this cluster.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is <code>true</code> or 
	 * cluster <code>cn</code> is the top cluster, then property <code>info</code>
	 * is returned, else the TopTreeIllegalAccessException is thrown.
	 * 
	 * @return User defined object with information about this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 */
	@SuppressWarnings("unchecked")
	public C getInfo() throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		return (C) this.info;
	}


	/**
	 * A method for setting user defined object with new information about this cluster.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is <code>true</code> or 
	 * cluster <code>cn</code> is the top cluster, then property <code>info</code>
	 * is set to given value, else the TopTreeIllegalAccessException is thrown.
	 * 
	 * @param info User defined object with new information about this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 */
	public void setInfo(C info) throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		this.info = info;
	}


	/**
	 * A method returns information about left boundary vertex whose are hold in property
	 * <code>info</code> in {@link toptree.impl.VertexInfo}.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is not <code>true</code> or 
	 * cluster <code>cn</code> is not the top cluster, then the TopTreeIllegalAccessException is thrown.
	 * <p>
	 * There are three cases that can happens:
	 * <ul>
	 *   <li>If this cluster is a rake cluster, then null is returned.</li>
	 *   <li>This cluster is left or right composed cluster. If it is <code>leftComposedClusterInfo</code>
	 *   then information of a left boundary vertex of relevant compress cluster is returned. If it is 
	 *   <code>rightComposedClusterInfo</code> then information of a compress vertex of relevant compress
	 *   cluster node is returned. If the cluster is reversed, then boundaries must be swapped, because
	 *   getBu() and getBv() reflect the reversion, but composed cluster information are not swapped.</li>
	 *   <li>Else the value of property <code>info</code> of the left boundary vertex of this cluster
	 *   is returned.</li>
	 * </ul>
	 * 
	 * @return Null if this cluster is a rake cluster. | 
	 *         Value of property <code>info</code> of left boundary vertex of this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 * @see toptree.impl.ClusterNode#getBu()
	 * @see toptree.impl.VertexInfo#getVertexInfo()
	 */
	@SuppressWarnings("unchecked")
	public V getBu() throws TopTreeIllegalAccessException {
		
		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		// there is no left boundary vertex for rake cluster of type rake cluster node
		if (cn.getBu() == null) {
			assert cn.isRake();
			return null;
		}
		
		// special case of rake - foster on proper son (point on path cluster) in compress node
		if (cn.clusterInfo != this) {
			assert cn.isCompress();
			CompressClusterNode ccn = (CompressClusterNode) cn;
			if (ccn.leftComposedClusterInfo == this) {
				// annul swapping of boundaries if cluster is reversed
				return (!ccn.isReversed()) ? 
						(V) ccn.getBu().getVertexInfo() : (V) ccn.getBv().getVertexInfo();
			} else {
				assert ccn.rightComposedClusterInfo == this;
				return (V) ccn.getCompressedVertex().getVertexInfo();
			}
		}

		assert cn.getBu() != null;
		return (V) cn.getBu().getVertexInfo();
	}


	/**
	 * A method for setting new information to left boundary vertex whose are hold in property
	 * <code>info</code> in {@link toptree.impl.VertexInfo}.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is not <code>true</code> or 
	 * cluster <code>cn</code> is not the top cluster, then the TopTreeIllegalAccessException is thrown.
	 * <p>
	 * There are three cases that can happens:
	 * <ul>
	 *   <li>If this cluster is a rake cluster, then exception is thrown, because rake cluster has
	 *   no left boundary vertex.</li>
	 *   <li>This cluster is left or right composed cluster. If it is <code>leftComposedClusterInfo</code>
	 *   then information of a left boundary vertex of relevant compress cluster is set. If it is 
	 *   <code>rightComposedClusterInfo</code> then information of a compress vertex of relevant compress
	 *   cluster node is set. If the cluster is reversed, then boundaries must be swapped, because
	 *   getBu() and getBv() reflect the reversion, but composed cluster information are not swapped.</li>
	 *   <li>Else the value of property <code>info</code> of the left boundary vertex of this cluster
	 *   is set.</li>
	 * </ul>
	 * 
	 * @param info New value of property <code>info</code> of left boundary vertex of this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 * @see toptree.impl.ClusterNode#getBu()
	 * @see toptree.impl.VertexInfo#setInfo(Object)
	 */
	@SuppressWarnings("unchecked")
	public void setBu(V info) throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		// there is no left boundary vertex for rake cluster of type rake cluster node
		if (cn.getBu() == null) {
			assert cn.isRake();
			throw new TopTreeIllegalAccessException(
			"Invalid boundary vertex accessed: no left boundary vertex for this cluster.");
		}
		
		// special case of rake - foster on proper son (point on path cluster) in compress node
		if (cn.clusterInfo != this) {
			assert cn.isCompress();
			CompressClusterNode ccn = (CompressClusterNode) cn;
			if (ccn.leftComposedClusterInfo == this) {
				// annul swapping of boundaries if cluster is reversed
				if (!ccn.isReversed()) {
					ccn.getBu().setInfo(info);
				} else {
					ccn.getBv().setInfo(info);
				}
			} else {
				assert ccn.rightComposedClusterInfo == this;
				ccn.getCompressedVertex().setInfo(info);
			}
		}

		cn.getBu().setInfo(info);
	}


	/**
	 * A method returns information about right boundary vertex whose are hold in property
	 * <code>info</code> in {@link toptree.impl.VertexInfo}.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is not <code>true</code> or 
	 * cluster <code>cn</code> is not the top cluster, then the TopTreeIllegalAccessException is thrown.
	 * <p>
	 * There are cases cases that can happens:
	 * <ul>
	 *   <li>This cluster is left or right composed cluster. If it is <code>leftComposedClusterInfo</code>
	 *   then information of a compress vertex of relevant compress cluster is returned. If it is 
	 *   <code>rightComposedClusterInfo</code> then information of a right boundary vertex of relevant
	 *   compress cluster node is returned. If the cluster is reversed, then boundaries must be swapped,
	 *   because getBu() and getBv() reflect the reversion, but composed cluster information are not 
	 *   swapped.</li>
	 *   <li>Else the value of property <code>info</code> of the left boundary vertex of this cluster
	 *   is returned.</li>
	 * </ul>
	 * 
	 * @return Null if this cluster is a rake cluster. | 
	 *         Value of property <code>info</code> of left boundary vertex of this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 * @see toptree.impl.ClusterNode#getBu()
	 * @see toptree.impl.VertexInfo#getVertexInfo()
	 */
	@SuppressWarnings("unchecked")
	public V getBv() throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		// special case of rake - foster on proper son (point on path cluster) in compress node
		if (cn.clusterInfo != this) {
			assert cn.isCompress();
			CompressClusterNode ccn = (CompressClusterNode) cn;
			if (ccn.leftComposedClusterInfo == this) {
				return (V) ccn.getCompressedVertex().getVertexInfo();
			} else {
				assert ccn.rightComposedClusterInfo == this;
				// annul swapping of boundaries if cluster is reversed
				return (!ccn.isReversed()) ?
						(V) ccn.getBv().getVertexInfo() : (V) ccn.getBu().getVertexInfo();
			}
		}

		assert cn.getBv() != null;
		return (V) cn.getBv().getVertexInfo();
	}


	/**
	 * A method for setting new information to right boundary vertex whose are hold in property
	 * <code>info</code> in {@link toptree.impl.VertexInfo}.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is not <code>true</code> or 
	 * cluster <code>cn</code> is not the top cluster, then the TopTreeIllegalAccessException is thrown.
	 * <p>
	 * There are two cases that can happens:
	 * <ul>
	 *   <li>This cluster is left or right composed cluster. If it is <code>leftComposedClusterInfo</code>
	 *   then information of a compress vertex of relevant compress cluster is set. If it is 
	 *   <code>rightComposedClusterInfo</code> then information of a right boundary vertex of relevant
	 *   compress cluster node is set. If the cluster is reversed, then boundaries must be swapped, because
	 *   getBu() and getBv() reflect the reversion, but composed cluster information are not swapped.</li>
	 *   <li>Else the value of property <code>info</code> of the left boundary vertex of this cluster
	 *   is set.</li>
	 * </ul>
	 * 
	 * @param info New value of property <code>info</code> of right boundary vertex of this cluster.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 * @see toptree.impl.ClusterNode#getBv()
	 * @see toptree.impl.VertexInfo#setInfo(Object)
	 */
	@SuppressWarnings("unchecked")
	public void setBv(V info) throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		// special case of rake - foster on proper son (point on path cluster) in compress node
		if (cn.clusterInfo != this) {
			assert cn.isCompress();
			CompressClusterNode ccn = (CompressClusterNode) cn;
			if (ccn.leftComposedClusterInfo == this) {
				ccn.getCompressedVertex().setInfo(info);
			} else {
				assert ccn.rightComposedClusterInfo == this;
				// annul swapping of boundaries if cluster is reversed
				if (!ccn.isReversed()) {
					ccn.getBv().setInfo(info);
				} else {
					ccn.getBu().setInfo(info);
				}
			}
		}

		assert cn.getBv() != null;
		cn.getBv().setInfo(info);
	}


	/**
	 * This method returns a value of the property <code>cn</code> of this object.
	 * <p>
	 * If the property <code>allowedLocalAccess</code> is not <code>true</code> or 
	 * cluster node <code>cn</code> is not the top cluster, then the TopTreeIllegalAccessException
	 * is thrown.
	 * 
	 * @return Value of the property <code>cn</code>.
	 * @throws TopTreeIllegalAccessException If this cluster is not the top cluster and
	 *         the property <code>allowedLocalAccess</code> is not <code>true</code>.
	 * @see toptree.impl.ClusterInfo#isAllowedLocalAccess()
	 * @see toptree.impl.ClusterNode#isTopCluster()
	 */
	ClusterNode getCn() throws TopTreeIllegalAccessException {

		if (!isAllowedLocalAccess() && !cn.isTopCluster())
			throw new TopTreeIllegalAccessException("Only top clusters can be accessed.");

		return cn;
	}


	/**
	 * This method only return a value of the property <code>allowedLocalAccess</code> of this object.
	 * 
	 * @return Value of the property <code>allowedLocalAccess</code>
	 */
	boolean isAllowedLocalAccess() {
		return allowedLocalAccess;
	}


	/**
	 * A method sets the property <code>allowedLocalAccess</code> of this object on given value.
	 * 
	 * @param allowedLocalAccess New value for the property <code>allowedLocalAccess</code>.
	 */
	void setAllowedLocalAccess(boolean allowedLocalAccess) {
		this.allowedLocalAccess = allowedLocalAccess;
	}

}
