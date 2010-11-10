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


/**
 * This class represents physical structure of one compressed cluster node and 
 * provides tools for managing it. <code>CompressClusterNode</code> class has
 * the same properties as <code>ClusterNode</code>. Moreover it has left and 
 * right foster children.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar (documentation)
 * @version %I%, %G%
 * @since 1.0
 * @see toptree.impl.ClusterNode
 * @see toptree.impl.ClusterInfo
 */
class CompressClusterNode extends ClusterNode {

	/**
	 * A left foster child.
	 */
	ClusterNode leftFoster;

	/**
	 * A right foster child.
	 */
	ClusterNode rightFoster;

	/**
	 * This field holds information counted from left proper and foster children together.
	 */
	@SuppressWarnings("unchecked")
	ClusterInfo leftComposedClusterInfo;

	/**
	 * This field holds information counted from right proper and foster children together.
	 */
	@SuppressWarnings("unchecked")
	ClusterInfo rightComposedClusterInfo;

	/**
	 * This cluster node is the handle for one vertex which is referenced 
	 * from this field.
	 */
	@SuppressWarnings("unchecked")
	VertexInfo compressedVertex;


	/**
	 * Creates new instance of the <code>CompressClusterNode</code> class from 
	 * four cluster nodes (two proper and two foster children) and two vertices
	 * which are used as boundary vertices. First it calls 
	 * {@link toptree.impl.ClusterNode#ClusterNode(byte, ClusterNode, ClusterNode, VertexInfo, VertexInfo)}
	 * and then it sets foster children, compressed vertex and information about
	 * this cluster node to appropriate properties.
	 * 
	 * @see toptree.impl.ClusterNode#ClusterNode(byte, ClusterNode, ClusterNode, VertexInfo, VertexInfo)
	 * @see toptree.impl.ClusterInfo#ClusterInfo(ClusterNode)
	 */
	@SuppressWarnings("unchecked")
	CompressClusterNode(VertexInfo compressedVertex, ClusterNode left, ClusterNode leftFoster, ClusterNode right,
			ClusterNode rightFoster, VertexInfo bu, VertexInfo bv) {
		/* Calling the constructor of predecessor. */
		super(ClusterNode.COMPRESS_CLUSTER, left, right, bu, bv);
		/* Setting foster children. */
		this.leftFoster = leftFoster;
		this.rightFoster = rightFoster;
		/* Setting compressed vertex. */
		this.compressedVertex = compressedVertex;
		/* Setting information about cluster to this node properties. */
		this.clusterInfo = new ClusterInfo(this);				// info of this node
		this.leftComposedClusterInfo = new ClusterInfo(this);	// info of left part
		this.rightComposedClusterInfo = new ClusterInfo(this);	// info of right part
	}


	/**
	 * This method creates copy of current cluster node. First it calls this method
	 * form predecessor and then it creates two <code>ClusterInfo</code> instances
	 * for left(right)CompossedClusterInfo.
	 * 
	 * @see toptree.impl.ClusterNode#cloneNew()
	 * @see toptree.impl.ClusterInfo#ClusterInfo(ClusterNode)
	 */
	@SuppressWarnings("unchecked")
	public ClusterNode cloneNew() {
		CompressClusterNode res = (CompressClusterNode) super.cloneNew();
		res.leftComposedClusterInfo = new ClusterInfo(res);
		res.rightComposedClusterInfo = new ClusterInfo(res);

		return res;
	}


	/** 
	 * This method repairs order of left and right foster and proper children, boundary vertices
	 * and composed cluster information if the order is reversed. It is important to set reverse
	 * bit to proper children because of boundary vertices. 
	 * <p>
	 * If the order is not reversed it does nothing.
	 */
	@SuppressWarnings("unchecked")
	final void normalize() {
		if (reversed != 0) {
			reversed = 0;
			/* Setting reverse bit to proper children */
			left.reverse();
			right.reverse();

			/* Swapping proper children. */
			ClusterNode tmp = left;
			left = right;
			right = tmp;

			/* Swapping foster children. */
			tmp = leftFoster;
			leftFoster = rightFoster;
			rightFoster = tmp;

			/* Swapping boundary vertices. */
			VertexInfo tmpv = boundaryVertices[0];
			boundaryVertices[0] = boundaryVertices[1];
			boundaryVertices[1] = tmpv;
			
			/* Swapping composed cluster info */
			ClusterInfo tmp_info = leftComposedClusterInfo;
			leftComposedClusterInfo = rightComposedClusterInfo;
			rightComposedClusterInfo = tmp_info;
		}
	}


	/**
	 * This method sets for boundary vertices their property <code>cluster</code>
	 * and for the current compressed cluster node the property 
	 * <code>compressedVertex</code> to the current cluster node.
	 * 
	 * @see toptree.impl.VertexInfo#setClusterNode(ClusterNode)
	 */
	void bindVertices() {
		getBu().setClusterNode(this);
		getBv().setClusterNode(this);
		compressedVertex.setClusterNode(this);
	}


	/**
	 * This method recomputes the boundary vertices and sets their and 
	 * <code>compressedVertex</code>'s property <code>cluster</code> to the current
	 * cluster.
	 *   
	 * @see toptree.impl.VertexInfo#setClusterNode(ClusterNode)
	 */
	final void recomputeVertices() {
		assert left != null && right != null;

		boundaryVertices[0] = left.getBu();
		boundaryVertices[1] = right.getBv();

		boundaryVertices[0].setClusterNode(this);
		boundaryVertices[1].setClusterNode(this);
		compressedVertex.setClusterNode(this);
	}


	/**
	 * This method returns the compressed vertex of this compressed cluster.
	 * 
	 * @return A value of <code>compressedVertex</code> property of this compressed cluster.
	 */
	@SuppressWarnings("unchecked")
	final VertexInfo getCompressedVertex() {
		return compressedVertex;
	}

}
