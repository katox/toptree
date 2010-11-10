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
 * This class represents physical structure of one cluster node and provides
 * tools for managing it.
 * <p>
 * It is important to understand the relation between <code>ClusterNode</code> and
 * {@link toptree.impl.ClusterInfo} class. <code>ClusterNode</code> class presents
 * only physical structure of one cluster node - type of the cluster, some flags,
 * references to descendants and ancestor, information about boundary vertices and
 * reference to information in <code>ClusterInfo</code> object. All these things are
 * necessary to managing top tree structure. 
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar (documentation)
 * @version 1.0
 * @since	1.0
 * @see toptree.impl.ClusterInfo
 */
class ClusterNode implements Cloneable {

	/**
	 * This mask marks bits that hold information about type of this cluster.
	 * These bits are 00000011.
	 * It is necessary for work with <code>BASE_CLUSTER</code>, <code>COMPRESS_CLUSTER</code>,
	 * <code>RAKE_CLUSTER</code> and <code>HARD_RAKE_CLUSTER</code> fields.
	 */
	protected static final byte TYPE_MASK = 0x3;

	/**
	 * This mask marks bits that hold information about need of this cluster
	 * to be normalized.
	 * These bits are 00001100. 
	 * It is necessary for work with <code>OP_NONE</code> and
	 * <code>OP_NORMALIZE</code> fields.
	 */
	protected static final byte OP_MASK = 0xc;

	/**
	 * This mask marks bits that hold information about state of this cluster.
	 * These bits are 01110000. 
	 * It is necessary for work with these fields:
	 * <ul>
	 *   <li><code>CLEAN</code></li>
	 *   <li><code>NEW</code></li>
	 *   <li><code>DIRTY</code></li>
	 *   <li><code>OBSOLETE</code></li>
	 *   <li><code>SELECT_AUXILIARY</code></li>
	 *   <li><code>SELECT_MODIFIED</code></li>
	 * </ul>
	 */
	protected static final byte STATE_MASK = 0x70;

	/**
	 * This kind of type of the cluster marks a base cluster.
	 * It's bits are 00000000. 
	 */
	protected static final byte BASE_CLUSTER = 0x0;

	/**
	 * This kind of type of the cluster marks a compress cluster.
	 * It's bits are 00000001. 
	 */
	protected static final byte COMPRESS_CLUSTER = 0x1;

	/**
	 * This kind of type of the cluster marks a rake cluster that must have just one boundary vertex.
	 * It's bits are 00000010. 
	 */
	protected static final byte RAKE_CLUSTER = 0x2;

	/**
	 * This kind of type of the cluster marks a rake cluster that must have just two boundary vertices.
	 * It's bits are 00000011. 
	 */
	protected static final byte HARD_RAKE_CLUSTER = 0x3;

	/**
	 * This kind of normalization flag marks a cluster node that is
	 * normalized so don't need it.
	 * It's bits are 00000000. 
	 */
	protected static final byte OP_NONE = 0x00;

	/**
	 * This kind of normalization flag marks a compress cluster node 
	 * that needs to be normalized.
	 * It's bits are 00001000. 
	 * 
	 * @see toptree.impl.CompressClusterNode#normalize()
	 */
	protected static final byte OP_NORMALIZE = 0x8;

	/**
	 * This kind of state marks a cluster node where everything is set as
	 * it should be. 
	 * It's bits are 00000000. 
	 */
	protected static final byte CLEAN = 0x00;

	/**
	 * This kind of state marks a new cluster node which could need some
	 * initialization to be all-right. 
	 * It's bits are 00010000. 
	 */
	protected static final byte NEW = 0x10;

	/**
	 * This kind of state marks an old cluster node where some modification
	 * is happening. This cluster node needs to be transfered to clean or to obsolete
	 * state.
	 * It's bits are 00100000.
	 */
	protected static final byte DIRTY = 0x20;

	/**
	 * This kind of state marks an old cluster node which is unnecessary.
	 * This nodes are the food for garbage collector.
	 * Only new and dirty cluster nodes may be transfered to the obsolete state. 
	 * It's bits are 00110000. 
	 */
	protected static final byte OBSOLETE = 0x30;

	/**
	 * This kind of state is used during operation <code>select</code> for marking cluster nodes that
	 * are only auxiliary for select and after it they will be deleted. 
	 * It's bits are 01000000. 
	 */
	protected static final byte SELECT_AUXILIARY = 0x40;

	/**
	 * This kind of state is used during operation <code>select</code> for marking cluster nodes that
	 * were clean in original tree and in select they change. While rebuilding the original tree after
	 * select the changes are annul and these cluster nodes become clean again. 
	 * It's bits are 01010000. 
	 */
	protected static final byte SELECT_MODIFIED = 0x50;

	/**
	 * This field shows if the left and right children and so left and right boundary vertices 
	 * are swapped. If they are in correct order, this field is set to value 0. If they are swapped,
	 * then the value is 1.
	 */
	protected int reversed;

	/**
	 * A reference to information about the cluster which handler current cluster
	 * node is.
	 */
	@SuppressWarnings("unchecked")
	ClusterInfo clusterInfo;

	/**
	 * A byte that is used for holding information about type, state and need to be
	 * normalized.
	 * It has four parts - 0 000 00 00. First bit from left side is not currently used.
	 * Second part with three bits is for state. Third part from left with two bits is
	 * for normalization operation. And the last forth part with two bits is for type of
	 * this cluster node.
	 */
	private byte flags;

	/**
	 * This field is used for hold reference to left and right boundary 
	 * vertices of current cluster.
	 */
	@SuppressWarnings("unchecked")
	protected VertexInfo[] boundaryVertices;

	/**
	 * The left proper child of this cluster node.
	 */
	ClusterNode left;

	/**
	 * The right proper child of this cluster node.
	 */
	ClusterNode right;

	/**
	 * The parent of this cluster node.
	 * The left and right proper children use parent references to hold parental
	 * compress node. And rake clusters use this parent reference to hold parental
	 * rake node.
	 */
	ClusterNode parent;

	/**
	 * This reference is used for holding direct predecessor which is not a parent
	 * of this cluster node in the sense of <code>parent</code> field.
	 * The left and right foster children use link references to hold compress node 
	 * which is their direct predecessor. Base and rake clusters use this link reference
	 * to hold rake node which is their direct predecessor.
	 */
	ClusterNode link;


	/**
	 * Creates a new instance of <code>ClusterNode</code> from only two vertices which are used 
	 * as boundary vertices. New cluster node is not reversed. Created new cluster node is holder of
	 * new cluster, so new <code>ClusterInfo</code> object is created and double-side linked with 
	 * this cluster node.
	 * 
	 * @param type	It is the type of new cluster: <code>BASE_CLUSTER</code>, <code>COMPRESS_CLUSTER</code>,
	 * 				<code>RAKE_CLUSTER</code> or <code>HARD_RAKE_CLUSTER</code> value.
	 * @param bu	A left boundary vertex of new cluster.
	 * @param bv	A right boundary vertex of new cluster.
	 */
	@SuppressWarnings("unchecked")
	ClusterNode(byte type, VertexInfo bu, VertexInfo bv) {
		/* Only type of a cluster node */
		assert type==BASE_CLUSTER || type==COMPRESS_CLUSTER || type==RAKE_CLUSTER || type==HARD_RAKE_CLUSTER;
		/* Setting of flags - the state is new a the type is given in parameter type. */
		this.flags = (byte) ((ClusterNode.NEW & ClusterNode.STATE_MASK) | (type & ClusterNode.TYPE_MASK));
		/* Setting left (bu) and right (bv) boundary vertices. */
		this.boundaryVertices = new VertexInfo[2];
		this.boundaryVertices[0] = bu;
		this.boundaryVertices[1] = bv;
		/* New cluster node is not reversed. */
		this.reversed = 0;
		/* New cluster node is handler of new cluster. */
		this.clusterInfo = new ClusterInfo(this);
	}


	/**
	 * Creates a new instance of <code>ClusterNode</code> from two cluster nodes and two vertices
	 * which are used as boundary vertices. New cluster node is not reversed. Created new cluster 
	 * node is holder of new cluster, so new <code>ClusterInfo</code> object is created and 
	 * double-side linked with this cluster node.
	 * 
	 * @param type	It is the type of new cluster: <code>BASE_CLUSTER</code>, <code>COMPRESS_CLUSTER</code>,
	 * 				<code>RAKE_CLUSTER</code> or <code>HARD_RAKE_CLUSTER</code> value.
	 * @param left	A left proper child of new cluster node.
	 * @param right	A right proper child of new cluster node.
	 * @param bu	A left boundary vertex of new cluster.
	 * @param bv	A right boundary vertex of new cluster.
	 */
	@SuppressWarnings("unchecked")
	ClusterNode(byte type, ClusterNode left, ClusterNode right, VertexInfo bu, VertexInfo bv) {
		/* Only type of a cluster node */
		assert type==BASE_CLUSTER || type==COMPRESS_CLUSTER || type==RAKE_CLUSTER || type==HARD_RAKE_CLUSTER;
		/* Setting of flags - the state is new a the type is given in parameter type. */
		this.flags = (byte) ((ClusterNode.NEW & ClusterNode.STATE_MASK) | (type & ClusterNode.TYPE_MASK));
		/* References to left and right children. */
		this.left = left;
		this.right = right;
		/* Setting left (bu) and right (bv) boundary vertices. */
		this.boundaryVertices = new VertexInfo[2];
		this.boundaryVertices[0] = bu;
		this.boundaryVertices[1] = bv;
		/* New cluster node is not reversed. */
		this.reversed = 0;
		/* New cluster node is handler of new cluster. */
		this.clusterInfo = new ClusterInfo(this);
	}


	/**
	 * This method makes a copy of this cluster node. First it calls 
	 * predecessor's method clone() and then it sets boundary vertices,
	 * state <code>NEW</code> and appropriate <code>ClusterInfo</code> object. 
	 * 
	 * @return A copy of current cluster node.
	 */
	@SuppressWarnings("unchecked")
	ClusterNode cloneNew() {
		ClusterNode res = null;

		try {
			/* Predecessor's clone() method. */
			res = (ClusterNode) super.clone();
			/* Setting boundary vertices. */
			res.boundaryVertices = new VertexInfo[2];
			res.boundaryVertices[0] = boundaryVertices[0];
			res.boundaryVertices[1] = boundaryVertices[1];
			/* Setting NEW state. */
			res.setState(ClusterNode.NEW);
			/* Reference to new ClusterInfo object. */
			res.clusterInfo = new ClusterInfo(res);
		} catch (CloneNotSupportedException e) {
			/* This should not ever happen. */
			assert false;
		}

		return res;
	}


	/**
	 * This method change the <code>reverse</code> field of current cluster node
	 * to opposite value. Values are 0 or 1.
	 */
	final void reverse() {
		this.reversed ^= 1;
	}


	/**
	 * A test if the order of left and right children and boundary vertices is swapped.
	 * @return True if it is swapped. False the order is correct.
	 */
	final boolean isReversed() {
		return this.reversed == 1;
	}


	/**
	 * This method returns left boundary vertex. It looks if vertices are swapped.
	 * @return The left boundary vertex.
	 */
	@SuppressWarnings("unchecked")
	final VertexInfo getBu() {
		return boundaryVertices[reversed];
	}


	/**
	 * This method returns right boundary vertex. It looks if vertices are swapped.
	 * @return The right boundary vertex.
	 */
	@SuppressWarnings("unchecked")
	final VertexInfo getBv() {
		return boundaryVertices[reversed ^ 1];
	}


	/** 
	 * This method repairs order of left and right children, boundary vertices and if the order is reversed
	 * only for hard rake cluster nodes.
	 * It is important to set reverse bit only for proper children (father is referenced through 
	 * parent-reference), because foster child is always raked by the right boundary vertex.  
	 * <p>
	 * If the order is not reversed or cluster node is not hard rake then nothing is done.
	 */
	@SuppressWarnings("unchecked")
	void normalize() {
		// only hard rake nodes can be normalized - compress nodes have their own method that overrides this
		if (isHardRake()) {
			if (reversed != 0) {
				reversed = 0;
				/* Setting reverse bit only to proper children */
				if (left.parent == this) {
					left.reverse();
				}
				if (right.parent == this) {
					right.reverse();
				}

				/* Swapping proper children. */
				ClusterNode tmp = left;
				left = right;
				right = tmp;

				/* Swapping boundary vertices. */
				VertexInfo tmpv = boundaryVertices[0];
				boundaryVertices[0] = boundaryVertices[1];
				boundaryVertices[1] = tmpv;
			}
		}
	}

	
	/**
	 * This method sets for boundary vertices in non-rake clusters their property
	 * <code>cluster</code> to the current cluster node.
	 * 
	 * @see toptree.impl.VertexInfo#setClusterNode(ClusterNode)
	 */
	void bindVertices() {
		if (!isRake()) {
			getBu().setClusterNode(this);
			getBv().setClusterNode(this);
		}
	}


	/**
	 * The method returns a value of type of the current cluster node.
	 * 
	 * @return A value of type - <code>BASE_CLUSTER</code>,
	 *         <code>COMPRESS_CLUSTER</code> or <code>RAKE_CLUSTER</code>.
	 */
	final protected int getType() {
		return flags & ClusterNode.TYPE_MASK;
	}


	/**
	 * The method returns a value of normalization operation of the current cluster node.
	 * 
	 * @return A value of normalization operation - neither <code>OP_NONE</code> or
	 *         <code>OP_NORMALIZE</code>.
	 */
	final protected int getOp() {
		return flags & ClusterNode.OP_MASK;
	}


	/**
	 * The method returns a value of state of the current cluster node.
	 * 
	 * @return A value of state - <code>CLEAN</code>, <code>NEW</code>,
	 *         <code>DIRTY</code> OR <code>OBSOLETE</code>.
	 */
	final protected int getState() {
		return flags & ClusterNode.STATE_MASK;
	}


	/**
	 * This method sets the state of the current cluster node to given value.
	 * 
	 * @param state New value of state.
	 */
	final protected void setState(byte state) {
		flags = (byte) (state | (flags & (~ClusterNode.STATE_MASK)));
	}


	/**
	 * This method sets the normalization operation of the current cluster node to given value.
	 * 
	 * @param operation New value of normalization operation.
	 */
	final protected void setOp(byte operation) {
		flags = (byte) (operation | (flags & (~ClusterNode.OP_MASK)));
	}


	/**
	 * A test if the state of the current cluster node has <code>DIRTY</code> value. It returns 
	 * true if the node is dirty, else false.
	 * 
	 * @return True if this cluster node is dirty. Else it returns false.
	 */
	final boolean isDirty() {
		return getState() == ClusterNode.DIRTY;
	}


	/**
	 * A test if the state of the current cluster node has <code>NEW</code> value. It returns 
	 * true if the node is new, else false.
	 * 
	 * @return True if this cluster node is new. Else it returns false.
	 */
	final boolean isNew() {
		return getState() == ClusterNode.NEW;
	}


	/**
	 * A test if the state of the current cluster node has <code>CLEAN</code> value. It returns 
	 * true if the node is clean, else false.
	 * 
	 * @return True if this cluster node is clean. Else it returns false.
	 */
	final boolean isClean() {
		return getState() == ClusterNode.CLEAN;
	}


	/**
	 * A test if the state of the current cluster node has <code>OBSOLETE</code> value. It returns 
	 * true if the node is obsolete, else false.
	 * 
	 * @return True if this cluster node is obsolete. Else it returns false.
	 */
	final boolean isObsolete() {
		return getState() == ClusterNode.OBSOLETE;
	}


	/**
	 * A test if the state of the current cluster node has <code>SELECT_AUXILIARY</code> value. It returns 
	 * true if the node is auxiliary for select, else false.
	 * 
	 * @return True if this cluster node is auxiliary for select. Else it returns false.
	 */
	final boolean isSelectAuxiliary() {
		return getState() == ClusterNode.SELECT_AUXILIARY;
	}


	/**
	 * A test if the state of the current cluster node has <code>SELECT_MODIFIED</code> value. It returns 
	 * true if the node is modified by select, else false.
	 * 
	 * @return True if this cluster node is modified by select. Else it returns false.
	 */
	final boolean isSelectModified() {
		return getState() == ClusterNode.SELECT_MODIFIED;
	}




	/**
	 * This method sets <code>DIRTY</code> value to the state of the current cluster node.
	 * The cluster node may not be base and not have obsolete state. It must have clean state.
	 */
	final void setDirty() {
		if (!isBase() && !isObsolete()) {
			assert getState() == ClusterNode.CLEAN;
			setState(ClusterNode.DIRTY);
		}
	}


	/**
	 * This method sets <code>OBSOLETE</code> value to the state of the current cluster node.
	 * The cluster node may not have obsolete state.
	 */
	final void setObsolete() {
		assert getState() == ClusterNode.CLEAN || getState() == ClusterNode.NEW || getState() == ClusterNode.OBSOLETE;
		setState(ClusterNode.OBSOLETE);
	}


	/**
	 * This method sets <code>CLEAN</code> value to the state of the current cluster node.
	 * The cluster node must have new state.
	 */
	final void setClean() {
		assert getState() == ClusterNode.NEW;

		setState(ClusterNode.CLEAN);
	}


	/**
	 * A test if the current cluster node is base cluster.
	 * It returns true if the type is set to <code>BASE_CLUSTER</code>. Else it
	 * returns false.
	 * 
	 * @return True if the current cluster node is base, else false.
	 */
	final boolean isBase() {
		return getType() == ClusterNode.BASE_CLUSTER;
	}


	/**
	 * A test if the current cluster node is compress cluster.
	 * It returns true if the type is set to <code>COMPRESS_CLUSTER</code>. Else it
	 * returns false.
	 * 
	 * @return True if the current cluster node is compress, else false.
	 */
	final boolean isCompress() {
		return getType() == ClusterNode.COMPRESS_CLUSTER;
	}


	/**
	 * A test if the current cluster node is rake cluster (rake with one boundary vertex).
	 * It returns true if the type is set to <code>RAKE_CLUSTER</code>. Else it
	 * returns false.
	 * 
	 * @return True if the current cluster node is rake, else false.
	 */
	final boolean isRake() {
		return getType() == ClusterNode.RAKE_CLUSTER;
	}


	/**
	 * A test if the current cluster node is hard rake cluster (rake with two boundary vertices).
	 * It returns true if the type is set to <code>HARD_RAKE_CLUSTER</code>. Else it
	 * returns false.
	 * 
	 * @return True if the current cluster node is rake, else false.
	 */
	final boolean isHardRake() {
		return getType() == ClusterNode.HARD_RAKE_CLUSTER;
	}


	/**
	 * A test if the current cluster node is the top cluster of whole top tree.
	 * The top cluster has no parent and link predecessor.
	 * 
	 * @return True if the current cluster is the top cluster, else false.
	 */
	final boolean isTopCluster() {
		return parent == null && link == null;
	}


	/**
	 * A test if the normalization operation of the current cluster node
	 * is set to <code>OP_NORMALIZE</code> value. It returns true if so, 
	 * else it returns false. 
	 * 
	 * @return True if the current cluster node needs normalization, else false.
	 */
	final boolean needsNormalization() {
		return getOp() == ClusterNode.OP_NORMALIZE;
	}


	/**
	 * This method sets normalization operation of the current cluster node to
	 * <code>OP_NORMALIZE</code> value.
	 */
	final void setNormalizationFlag() {
		setOp(ClusterNode.OP_NORMALIZE);
	}


	/**
	 * This method sets normalization operation of the current cluster node to
	 * <code>OP_NONE</code> value.
	 */
	final void clearNormalizationFlag() {
		setOp(ClusterNode.OP_NONE);
	}

}
