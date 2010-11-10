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

import java.util.*;

import org.fantomery.toptree.Cluster;
import org.fantomery.toptree.TopTree;
import org.fantomery.toptree.TopTreeException;
import org.fantomery.toptree.impl.TopTreeIllegalAccessException;
import org.fantomery.toptree.TopTreeListener;
import org.fantomery.toptree.Vertex;


/**
 * This class implements the TopTree interface to represent and manage whole Top Tree.
 * There are held information about vertices, edges, clusters, cluster nodes and about
 * user defined operations to manage user defined information. The class provides 
 * a lot of methods that enable the managing of the tree. 
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version	1.0
 * @since	1.0
 *
 * @param <C> A user defined object with information about the cluster.
 * @param <V> A user defined object with information about the vertex.
 */
@SuppressWarnings("unchecked")
public class TopTreeImpl<C,V> implements TopTree<C,V> {
	
	/**
	 * An object which tells how to deal with user defined information during
	 * managing the Top Tree.
	 */
	TopTreeListener event;

	/**
	 * A list of all vertices in Top Tree.
	 */
	List<VertexInfo> vertices;

	/**
	 * Number of edges in this Top Tree.
	 */
	int numOfEdges;

	/**
	 * This field is used as holder for sequence of dirty nodes when some operation rebuilds the tree.
	 * It holds a top node of dirty nodes and these nodes hold references down to their children.
	 */
	private ClusterNode origTop;

	/**
	 * This field is used during hardExpose to remember position of a left end of root path in rake tree
	 * where this end is moved. If the Top Tree is not hard-exposed then its value must be <code>-1<code>.
	 */
	private int hardExposeLeftTail;
	
	/**
	 * This field is used during hardExpose to remember a position of a right end of root path in rake tree
	 * where this end is moved. If the Top Tree is not hard-exposed then its value must be <code>-1<code>.
	 */
	private int hardExposeRightTail;
	
	/**
	 * This field is used during hardExpose to remember a number of point clusters that are raked on left
	 * boundary vertex v. If the Top Tree is not hard-exposed then its value must be <code>0<code>.
	 */
	private int hardExposeLeftCount;

	/**
	 * This field is used during hardExpose to remember a number of point clusters that are raked on right
	 * boundary vertex u. If the Top Tree is not hard-exposed then its value must be <code>0<code>.
	 */
	private int hardExposeRightCount;
	
	/**
	 * This field is used to hold a root node of hard-exposed Top Tree. If the Top Tree is not hard-exposed
	 * then its value must be <code>null<code>.
	 */
	private ClusterNode hardExposeTop;
	
	/**
	 * This field is used to remember that the Top Tree was exposed on one vertex. It references the vertex
	 * on which a method {@link toptree.impl.TopTreeImpl#expose(Vertex)} was called. If the Top Tree is
	 * not after this type of expose the value of this field must be null.
	 */
	private Vertex exposeOneVertex = null;


	/**
	 * This constructor creates a new instance of <code>TopTreeImpl</code> .
	 * It prepares empty array for vertices, assigns given object implementing
	 * <code>TopTreeListener</code> interface, sets <code>origTop</code> to null and
	 * number of edges to zero. 
	 * 
	 * @param event Object which implements <code>TopTreeListener</code> interface.
	 * @see toptree.TopTreeListener
	 */
	public TopTreeImpl(TopTreeListener<C,V> event) {
		assert event != null;

		vertices = new ArrayList<VertexInfo>();
		this.event = event;
		this.origTop = null;
		this.numOfEdges = 0;
		this.hardExposeLeftTail = -1;
		this.hardExposeRightTail = -1;
		this.hardExposeLeftCount = 0;
		this.hardExposeRightCount= 0;
		this.hardExposeTop = null;
		this.exposeOneVertex = null;
	}


	/**
	 * This method creates new vertex with given user defined information and 
	 * returns this new vertex. It creates new instance of <code>VertexInfo</code>
	 * class and inserts it into the array of vertices of this Top Tree.
	 */
	public Vertex<V> createNewVertex(V info) {
		VertexInfo v = new VertexInfo(info);
		vertices.add(v);
		return (Vertex<V>) v;
	}


	/**
	 * A method for obtaining quantity of vertices in this Top Tree.
	 * It returns the size of the array with vertices.
	 * 
	 * @return A number of vertices in this Top Tree.
	 */
	public int getNumOfVertices() {
		return vertices.size();
	}


	/**
	 * A method for obtaining quantity of edges in this Top Tree.
	 * It returns value of the field <code>numOfEdges</code>.
	 * 
	 * @return A number of edges in this Top Tree.
	 */
	public int getNumOfEdges() {
		return numOfEdges;
	}


	/**
	 * A method for obtaining number of component in this Top Tree.
	 * It is counted as the difference between number of vertices minus
	 * number of edges.
	 * 
	 * @return A number of number in this Top Tree.
	 */
	public int getNumOfComponents() {
		return getNumOfVertices() - getNumOfEdges();
	}


	/**
	 * This method creates new <code>ClusterNode</code> of type base from given two vertices and user
	 * defined information and returns this created node.
	 * First it creates the node and then it sets the information. At the end if any of given
	 * vertices has degree zero, it sets it property <code>cluster</code> of relevant vertex to
	 * this base cluster node. The method returns new created base <code>ClusterNode</code>.
	 * 
	 * @param u A vertex which will be left child of the created base <code>ClusterNode</code>.
	 * @param v A vertex which will be right child of the created base <code>ClusterNode</code>.
	 * @param info User defined information that will be assign to the new node.
	 * @return The created base <code>ClusterNode</code>.
	 */
	private ClusterNode createBaseClusterNode(VertexInfo u, VertexInfo v, Object info) {
		// Vertices cannot be null.
		assert u != null && v != null;

		// Make a new cluster of base type and its end-points make given vertices its boundary vertices.
		ClusterNode baseCluster = new ClusterNode(ClusterNode.BASE_CLUSTER, u, v);
		
		// Assign given information.
		try {
			baseCluster.clusterInfo.setInfo(info);
		} catch (TopTreeIllegalAccessException e) {
			// this should never happen
			e.printStackTrace();
			assert false;
		}

		// Bind only orphaned vertices.
		if (u.getDegree() == 0)
			u.setClusterNode(baseCluster);
		if (v.getDegree() == 0)
			v.setClusterNode(baseCluster);

		return baseCluster;
	}


	/**
	 * This method creates new compress cluster node from given parameters and returns it.
	 * None given parameters can be null excepting foster children.
	 * <p>
	 * According to boundary vertices may be proper children reversed. Foster children 
	 * may be reversed too so that their right boundary vertex is <code>compressedVertex</code>
	 * of new compress node.
	 * <p>
	 * When children are well oriented then the compress cluster node is created. After it the
	 * references from children to their new father are repaired.
	 * 
	 * @param v A vertex whose handler will created node be.
	 * @param cu Left proper child of created node.
	 * @param fosterCu Left foster child of created node.
	 * @param cv Right proper child of created node.
	 * @param fosterCv Right foster child of created node.
	 * @return New created compress cluster node.
	 */
	private ClusterNode createCompressClusterNode(VertexInfo v, ClusterNode cu,
			ClusterNode fosterCu, ClusterNode cv, ClusterNode fosterCv) {
		

		assert cu != null && cv != null && v != null;	// Proper children and handler's vertex cannot be null.
		assert !cu.isRake() && !cv.isRake();			// Proper children cannot be rake.

		// force the same edge orientation in both components.
		if (cu.getBv() != v)	// Right boundary of left proper son must be compressed vertex.
			cu.reverse();
		if (cv.getBu() != v)	// Left boundary of right proper son must be compressed vertex.
			cv.reverse();
		assert cu.getBv() == cv.getBu();	// So now bv of left proper a bu of right proper are the same.

		// The left boundary is the original left boundary of left proper son.
		VertexInfo bu = cu.getBu();
		// The right boundary is the original right boundary of right son.
		VertexInfo bv = cv.getBv();
		// The compress cluster always has two boundary vertices.
		assert bu != null && bv != null;

		/* The right boundary of the left child and the left boundary of the
		 * right child is a single vertex (to be compressed). */
		VertexInfo compressedVertex = v;

		// Force the edge orientation towards compressed vertex in both foster components.
		if (fosterCu != null && fosterCu.getBv() != compressedVertex) {
			fosterCu.reverse();
			assert fosterCu.getBv() == compressedVertex;	// bv of foster must be compressed vertex.
		}
		if (fosterCv != null && fosterCv.getBv() != compressedVertex) {
			fosterCv.reverse();
			assert fosterCv.getBv() == compressedVertex;	// bv of foster must be compressed vertex.
		}

		// The topmost non-rake node for the vertex to be compressed, bu and vu is the newly created cluster.
		ClusterNode compressCluster = new CompressClusterNode(v, cu, fosterCu, cv, fosterCv, bu, bv);
		compressedVertex.setClusterNode(compressCluster);
		bu.setClusterNode(compressCluster);
		bv.setClusterNode(compressCluster);

		// Update parent pointers for all children.
		cu.parent = compressCluster;
		cu.link = null;
		cv.parent = compressCluster;
		cv.link = null;
		if (fosterCu != null) {
			fosterCu.parent = null;
			fosterCu.link = compressCluster;
		}
		if (fosterCv != null) {
			fosterCv.parent = null;
			fosterCv.link = compressCluster;
		}

		return compressCluster;
	}


	/**
	 * This method creates new rake cluster node from two given cluster nodes and
	 * returns it. None of given parameters can be null.
	 * <p>
	 * The method first prepares orientation of given nodes so that their right boundary 
	 * vertex will be given vertex. This vertex is compress vertex of compress cluster
	 * node which will be a father of created rake node. 
	 * After it the new rake node is created and in the end the references from two
	 * given nodes to their new father are repaired.
	 * 
	 * @param v A vertex from path on which will new rake cluster connected.
	 * @param cu One of two cluster nodes which will create new rake cluster node. 
	 * @param cv One of two cluster nodes which will create new rake cluster node.
	 * @return New created rake cluster node.
	 */
	private ClusterNode createRakeClusterNode(Vertex v, ClusterNode cu, ClusterNode cv) {
		// None of given parameters can be null.
		assert cu != null && cv != null && v != null;

		// bv of both nodes must be given vertex.
		if (cu.getBv() != v)
			cu.reverse();
		if (cv.getBv() != v)
			cv.reverse();
		assert cu.getBv() == v && cv.getBv() == v;

		// Create new rake: bu is null and bv is bv of both cluster nodes.
		ClusterNode rakeCluster = new ClusterNode(ClusterNode.RAKE_CLUSTER, cu, cv, null, cv
				.getBv());

		// Update pointers from sons to their new father.
		if (cu.isRake()) {
			cu.parent = rakeCluster;	// From rake to rake => parent
			cu.link = null;
		} else {
			cu.parent = null;
			cu.link = rakeCluster;		// From compress to rake => link
		}
		// Same as for cu
		if (cv.isRake()) {
			cv.parent = rakeCluster;
			cv.link = null;
		} else {
			cv.parent = null;
			cv.link = rakeCluster;
		}

		return rakeCluster;
	}


	/**
	 * This method creates new hard rake cluster node from two given cluster nodes and
	 * returns it. Hard rake cluster node represents connection of path cluster node and
	 * point cluster node. None of given parameters can be null.
	 * <p>
	 * The method first very hardly checks types of cluster nodes and their boundaries.
	 * Correct orientation of cluster node must be prepared before calling of this method.
	 * Then it creates new hard rake cluster node and sets new cluster node in boundary vertices
	 * of this new cluster node. It sets references from sons to their new father too. The son who
	 * is path cluster uses parent-reference and the son who is point cluster uses link-reference.
	 * New cluster node is returned.
	 *  
	 * @param v	A vertex from path on which will be left boundary vertex of new hard rake cluster node.
	 * @param u	A vertex from path on which will be right boundary vertex of new hard rake cluster node.
	 * @param cu	A cluster nodes which will will be a left son of created hard rake cluster node. 
	 * @param cv	A cluster nodes which will will be a right son of created hard rake cluster node.
	 * @return	New created hard rake cluster node.
	 */
	private ClusterNode createHardRakeClusterNode(VertexInfo v, VertexInfo u,
			ClusterNode cu, ClusterNode cv) {
		// None of given parameters can be null.
		assert cu != null && cv != null && u != null && v != null;
		
		// let's look who is path cluster and check types and boundaries of nodes
		if (cu.getBu() == v && cu.getBv() == u) {
			// left is path cluster, right is point cluster
			assert cu.getBv() == cv.getBv();
		}
		else {
			// left is point cluster, right is path cluster
			assert cv.getBu() == v && cv.getBv() == u;
			assert cu.getBv() == cv.getBu();
		}

		// Create new hard rake.
		ClusterNode hardRakeCluster = new ClusterNode(ClusterNode.HARD_RAKE_CLUSTER, cu, cv, v, u);
		
		// set new cluster node for boundary vertices and check it
		hardRakeCluster.bindVertices();
		assert hardRakeCluster.getBu().getClusterNode() == hardRakeCluster;
		assert hardRakeCluster.getBv().getClusterNode() == hardRakeCluster;		
		
		/* Update pointers from sons to their new father. Parent-reference is used for path child
		 * and link-reference for point child */
		if (cu.getBu() == v && cu.getBv() == u) {
			cu.parent = hardRakeCluster;
			cu.link = null;
		} else {
			cu.parent = null;
			cu.link = hardRakeCluster;
		}
		if (cv.getBu() == v && cv.getBv() == u) {
			cv.parent = hardRakeCluster;
			cv.link = null;
		} else {
			cv.parent = null;
			cv.link = hardRakeCluster;
		}
		
		return hardRakeCluster;
	}


	/**
	 * This method repairs user defined information in children of dirty and obsolete 
	 * cluster nodes in cluster whose root the given <code>ClusterNode</code> is.
	 * <p>
	 * The method should be called after operations that rebuild the Top Tree are finished.
	 * During rebuilding new nodes are created as copy of some nodes. Original node became dirty
	 * or obsolete. So it is necessary to recompute user define information from these fathers
	 * to their sons. And it is exactly done here. After it the method 
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} should be called for
	 * recomputing user define information from sons to their new fathers in post-order manner.
	 * <p>
	 * The computing of values for sons is done in pre-order manner on top-down way. So it starts
	 * in given node. If current node is rake or compress then first information 
	 * for sons of the current node are computed by method
	 * {@link toptree.TopTreeListener#split(Cluster, Cluster, Cluster, toptree.Cluster.ConnectionType)}
	 * and then this cleaning method is recursively called on these sons. Before splitting a method
	 * {@link toptree.impl.TopTreeImpl#typeOfJoin(ClusterInfo, ClusterInfo, ClusterInfo)} is called 
	 * for finding out the type of connection of children clusters into <code>c</code>. 
	 * If current node is base then method 
	 * {@link toptree.TopTreeListener#destroy(Cluster, toptree.Cluster.ClusterType)} is 
	 * called and recursion is finished.
	 * <p>
	 * When the method for user define splitting is called, it is important to give it parameters of 
	 * children clusters in correct order.<br />
	 * With rake cluster nodes there is no problem. Both sons must have right boundary vertex 
	 * which is same for both. So left son is parameter <code>a</code> and right 
	 * son is <code>b</code>.<br />
	 * Hard rake cluster nodes are little different, but there is no problem still. If left son is 
	 * path then its right boundary vertex is the same as only boundary vertex (right boundary vertex)
	 * of right son. Symmetrical if right son is path then its left boundary is the same as only boundary
	 * vertex (right boundary vertex) of left son. In both cases left son is parameter <code>a</code>
	 * and right son is <code>b</code>.<br />
	 * A problem can appear in compress cluster node that represents three splits if it has both not null
	 * foster sons. First split represents a division of complete cluster into 
	 * <code>leftComposedClusterInfo</code> as parameter <code>a</code> and 
	 * <code>rightComposedClusterInfo</code> as parameter <code>b</code>. Second split represents a 
	 * division of <code>leftComposedClusterInfo</code> info left proper (parameter <code>a</code>)
	 * and foster (parameter <code>b</code>) sons. And third split represents a division of 
	 * <code>rightComposedClusterInfo</code> into right proper (parameter <code>b</code>) and foster
	 * (parameter <code>a</code>) sons. To know correct order how to give disconnected clusters to user
	 * define split it is enough to remember one rule: 
	 * If left boundary vertex of path cluster is common for both clusters, then this path cluster must
	 * be given as parameter <code>b</code>, and if the common is right boundary then this path cluster
	 * must be given as parameter <code>a</code>.
	 * 
	 * @param c A cluster node which holds the cluster that will be cleaned.
	 */
	private void cleanDirtyNodes(ClusterNode c) {
		// Node must be not null and dirty or obsolete.
		if (c != null && (c.isDirty() || c.isObsolete())) {
			if (c.isBase()) {
				// Base node has no children.
				assert c.left == null && c.right == null;
				// Report destroying of this base cluster to an application.
				c.clusterInfo.setAllowedLocalAccess(true);
				// Find out the type of cluster and give it to destroy method.
				Cluster.ClusterType cluster_type = typeOfBaseCluster(c.clusterInfo);
				event.destroy(c.clusterInfo, cluster_type);
				c.clusterInfo.setAllowedLocalAccess(false);

			} else if (c.isRake()) {
				// Rake or hard rake node has both sons.
				assert c.left != null && c.right != null;

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// Report splitting of this rake cluster to an application.
				c.clusterInfo.setAllowedLocalAccess(true);
				c.left.clusterInfo.setAllowedLocalAccess(true);
				c.right.clusterInfo.setAllowedLocalAccess(true);

				// Find out the type of connection and give it to split method.
				type = typeOfJoin(c.left.clusterInfo, c.right.clusterInfo, c.clusterInfo);
				event.split(c.left.clusterInfo, c.right.clusterInfo, c.clusterInfo, type);

				c.left.clusterInfo.setAllowedLocalAccess(false);
				c.right.clusterInfo.setAllowedLocalAccess(false);
				c.clusterInfo.setAllowedLocalAccess(false);

				// Clean both left and right Top Subtrees.
				cleanDirtyNodes(c.left);
				cleanDirtyNodes(c.right);
				c.left = c.right = null;

			} else if (c.isHardRake()) {
				// Rake or hard rake node has both sons.
				assert c.left != null && c.right != null;

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// Report splitting of this rake cluster to an application.
				c.clusterInfo.setAllowedLocalAccess(true);
				c.left.clusterInfo.setAllowedLocalAccess(true);
				c.right.clusterInfo.setAllowedLocalAccess(true);

				// Find out the type of connection and give it to split method.
				type = typeOfJoin(c.left.clusterInfo, c.right.clusterInfo, c.clusterInfo);
				event.split(c.left.clusterInfo, c.right.clusterInfo, c.clusterInfo, type);

				c.left.clusterInfo.setAllowedLocalAccess(false);
				c.right.clusterInfo.setAllowedLocalAccess(false);
				c.clusterInfo.setAllowedLocalAccess(false);

				// Clean both left and right Top Subtrees.
				cleanDirtyNodes(c.left);
				cleanDirtyNodes(c.right);
				c.left = c.right = null;

			} else {
				// Compress cluster node has both proper sons.
				CompressClusterNode cc = (CompressClusterNode) c;
				assert c.left != null && c.right != null;

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// It must be distinguished which foster children it has.
				if (cc.leftFoster != null && cc.rightFoster != null) {
					// It has both fosters.

					// Report splitting of this compress to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
							cc.clusterInfo);
					event.split(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
							cc.clusterInfo, type);
					type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo);
					event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo, type);
					type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo);
					event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo, type);

					cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

					// Clean all children and kill them.
					cleanDirtyNodes(cc.leftFoster);
					cleanDirtyNodes(cc.left);
					cleanDirtyNodes(cc.rightFoster);
					cleanDirtyNodes(cc.right);
					cc.leftFoster = cc.rightFoster = cc.left = cc.right = null;

				} else if (cc.leftFoster != null) {
					// It has only left foster.

					// Report splitting of this compress to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.leftComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo);
					event.split(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);
					type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo);
					event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo, type);

					cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

					// Clean all children and kill them.
					cleanDirtyNodes(cc.leftFoster);
					cleanDirtyNodes(cc.left);
					cleanDirtyNodes(cc.right);
					cc.leftFoster = cc.left = cc.right = null;

				} else if (cc.rightFoster != null) {
					// It has only right foster.

					// Report splitting of this compress to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo);
					event.split(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo, type);
					type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo);
					event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo, type);

					cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

					// Clean all children and kill them.
					cleanDirtyNodes(cc.left);
					cleanDirtyNodes(cc.rightFoster);
					cleanDirtyNodes(cc.right);
					cc.rightFoster = cc.left = cc.right = null;

				} else {
					// No fosters.
					
					// Report splitting of this compress to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo);
					event.split(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);

					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

					// Clean all children and kill them.
					cleanDirtyNodes(cc.left);
					cleanDirtyNodes(cc.right);
					cc.left = cc.right = null;

				}
			}
		}
	}


	/**
	 * This method computes user define information for new cluster nodes from their
	 * children in a cluster whose root is given <code>ClusterNode</code>.
	 * <p>
	 * The method should be called after calling method
	 * {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} which is called after 
	 * operations that rebuild the Top Tree. During rebuilding new nodes are created as 
	 * copy of some nodes. Original node became dirty or obsolete. So it is necessary to 
	 * recompute user define information from these fathers to their sons.
	 * It is exactly done by cleaning method. After cleaning there are computed 
	 * correct information in sons of old nodes, but not in their new fathers. And that 
	 * is exactly done by this method.
	 * <p>
	 * The method computes the information in post-order manner on top-down way.
	 * If given node is rake or compress cluster node then the method call themselves
	 * recursively for sons of current node. After it new information are computed by method
	 * {@link toptree.TopTreeListener#join(Cluster, Cluster, Cluster, toptree.Cluster.ConnectionType)}
	 * and node is set to type clean. Before splitting a method
	 * {@link toptree.impl.TopTreeImpl#typeOfJoin(ClusterInfo, ClusterInfo, ClusterInfo)} is called 
	 * for finding out the type of connection of children clusters into <code>c</code>.
	 * If given node is base then its new information is computed by
	 * method {@link toptree.TopTreeListener#create(Cluster, toptree.Cluster.ClusterType)} and node is
	 * set to type clean.
	 * <p>
	 * When the method for user define joining is called, it is important to give it parameters of 
	 * children clusters in correct order.<br />
	 * With rake cluster nodes there is no problem. Both sons must have right boundary vertex 
	 * which is same for both. So left son is parameter <code>a</code> and right 
	 * son is <code>b</code>.<br />
	 * Hard rake cluster nodes are little different, but there is no problem still. If left son is 
	 * path then its right boundary vertex is the same as only boundary vertex (right boundary vertex)
	 * of right son. Symmetrical if right son is path then its left boundary is the same as only boundary
	 * vertex (right boundary vertex) of left son. In both cases left son is parameter <code>a</code>
	 * and right son is <code>b</code>.<br />
	 * A problem can appear in compress cluster node that represents three joins if it has both not null
	 * foster sons. First join represents a connection of left proper (parameter <code>a</code>) and
	 * foster sons (parameter <code>b</code>) into <code>leftComposedClusterInfo</code>, second join
	 * represents a connection of right proper (parameter <code>b</code>) and foster (parameter 
	 * <code>a</code>) sons into <code>rightComposedClusterInfo</code> and third join represents 
	 * a connection of two results of previous joins - left part as parameter <code>a</code> and
	 * right part as parameter <code>b</code>. To know correct order how to give connecting clusters
	 * to user define join it is enough to remember one rule: If left boundary vertex of path cluster
	 * is common for both clusters, then this path cluster must be given as parameter <code>b</code>, and
	 * if the common is right boundary then this path cluster must be given as parameter <code>a</code>.
	 *  
	 * @param c A cluster node which holds the cluster that will be fixated.
	 */
	private void fixateNewNodes(ClusterNode c) {
		if (c.isReversed()) {
			// normalize node first for legal work with boundaries in Listener
			c.normalize();
		}
		// Node must be not null and new.
		if (c != null && c.isNew()) {
			
			if (c.isBase()) {
				// Base node has no children.
				assert c.left == null && c.right == null;

				c.setClean();

				// Report creating of this base cluster to an application.
				c.clusterInfo.setAllowedLocalAccess(true);
				// Find out the type of cluster and give it to create method.
				Cluster.ClusterType cluster_type = typeOfBaseCluster(c.clusterInfo);
				event.create(c.clusterInfo, cluster_type);
				c.clusterInfo.setAllowedLocalAccess(false);

			} else if (c.isRake() || c.isHardRake()) {
				// Rake or hard rake node - first fixate both sons.
				fixateNewNodes(c.left);
				fixateNewNodes(c.right);

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				c.setClean();

				// Report joining left and right child into this rake cluster to an application.
				c.clusterInfo.setAllowedLocalAccess(true);
				c.left.clusterInfo.setAllowedLocalAccess(true);
				c.right.clusterInfo.setAllowedLocalAccess(true);

				// Find out the type of connection and give it to split method.
				type = typeOfJoin(c.left.clusterInfo, c.right.clusterInfo, c.clusterInfo);
				event.join(c.clusterInfo, c.left.clusterInfo, c.right.clusterInfo, type);

				c.left.clusterInfo.setAllowedLocalAccess(false);
				c.right.clusterInfo.setAllowedLocalAccess(false);
				c.clusterInfo.setAllowedLocalAccess(false);

			} else {
				// Compress cluster node
				CompressClusterNode cc = (CompressClusterNode) c;

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// It must be distinguish which foster children the node has.
				if (cc.leftFoster != null && cc.rightFoster != null) {
					// It has both foster children
					
					// First fixate all sons
					fixateNewNodes(cc.leftFoster);
					fixateNewNodes(cc.left);
					fixateNewNodes(cc.rightFoster);
					fixateNewNodes(cc.right);

					c.setClean();

					// Report creating this compress cluster to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo);
					event.join(cc.leftComposedClusterInfo, cc.left.clusterInfo,
							cc.leftFoster.clusterInfo, type);
					type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo);
					event.join(cc.rightComposedClusterInfo, cc.rightFoster.clusterInfo,
							cc.right.clusterInfo, type);
					type = typeOfJoin(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
							cc.clusterInfo);
					event.join(cc.clusterInfo, cc.leftComposedClusterInfo,
							cc.rightComposedClusterInfo, type);

					cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);
				} else if (cc.leftFoster != null) {
					// It has only left foster child
					
					// First fixate all sons
					fixateNewNodes(cc.leftFoster);
					fixateNewNodes(cc.left);
					fixateNewNodes(cc.right);

					c.setClean();

					// Report creating this compress cluster to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.leftComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
							cc.leftComposedClusterInfo);
					event.join(cc.leftComposedClusterInfo, cc.left.clusterInfo,
							cc.leftFoster.clusterInfo, type);
					type = typeOfJoin(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo);
					event.join(cc.clusterInfo, cc.leftComposedClusterInfo, cc.right.clusterInfo, type);

					cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
					cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

				} else if (cc.rightFoster != null) {
					// It has only right foster child
					
					// First fixate all sons
					fixateNewNodes(cc.left);
					fixateNewNodes(cc.rightFoster);
					fixateNewNodes(cc.right);

					c.setClean();

					// Report creating this compress cluster to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					cc.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
							cc.rightComposedClusterInfo);
					event.join(cc.rightComposedClusterInfo, cc.rightFoster.clusterInfo,
							cc.right.clusterInfo, type);
					type = typeOfJoin(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo);
					event.join(cc.clusterInfo, cc.left.clusterInfo, cc.rightComposedClusterInfo, type);

					cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
					cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);

				} else {
					// It has no foster children
					
					// First fixate proper sons
					fixateNewNodes(cc.left);
					fixateNewNodes(cc.right);

					c.setClean();

					// Report creating this compress cluster to an application.
					c.clusterInfo.setAllowedLocalAccess(true);
					c.left.clusterInfo.setAllowedLocalAccess(true);
					c.right.clusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo);
					event.join(cc.clusterInfo, cc.left.clusterInfo, cc.right.clusterInfo, type);

					c.left.clusterInfo.setAllowedLocalAccess(false);
					c.right.clusterInfo.setAllowedLocalAccess(false);
					c.clusterInfo.setAllowedLocalAccess(false);
				}
			}
		}
	}


	/**
	 * This method returns a top cluster for given vertex. The vertex cannot be null.
	 * <p>
	 * The method goes on direct predecessors through link or parent reference. When it
	 * finds a node with no link and parent reference, it is top cluster node. Then 
	 * appropriate cluster is returned.
	 * <p>
	 * If given vertex is whole tree, it returns null.
	 * 
 	 * @return A top cluster of given vertex, null for single vertex.
	 */
	public Cluster<C,V> getTopComponent(Vertex<V> uf) {
		
		assert uf != null;
		VertexInfo u = (VertexInfo) uf;

		ClusterNode c = u.getClusterNode();
		if (c == null)
			return null;	// Single vertex.

		// Go up while any predecessor exists.
		while (c.parent != null || c.link != null) {
			c = (c.parent != null) ? c.parent : c.link;
		}

		return (Cluster<C,V>) c.clusterInfo;
	}


	/**
	 * This method repairs an order of sons in nodes on a way from a root down to
	 * a given cluster node <code>x</code>. If second parameter <code>v</code> is not null
	 * and the way is represented in some proper son then the way is moved to a left 
	 * proper son. The cluster node <code>x</code> cannot be null.
	 * <p>
	 * The method first marks the way from the vertex <code>x</code> up to the root.
	 * If the root is compress cluster node, the vertex <code>v</code> is not null (it signs
	 * that soft-expose has a guard) and the way is represented in the proper child of the root
	 * then the method ensures that the way is represented in the left proper child. Next it 
	 * goes down through marked nodes. On compress cluster nodes it calls the method 
	 * {@link toptree.impl.CompressClusterNode#normalize()}.
	 *  
	 * @param x	A cluster node which is on the down end of the way from a root.
	 * @param v	A vertex which is compressed in the root.
	 */
	private void rectify(ClusterNode x, Vertex v) {
		assert x != null;

		// set normalization flag on all nodes from cNx to the top node
		ClusterNode bottom = x;
		ClusterNode c = x;
		while (x != null) {
			c = x;
			x.setNormalizationFlag();
			x = (x.parent != null) ? x.parent : x.link;
		}
		assert c.isTopCluster();

		// check the correct position of uv within a non-trivial tree
		if (c.isCompress() && v != null) {
			// we want to ensure that cluster representing the path u..v
			// is to the right of cNv which is the root of the whole tree

			// v is either the right boundary of the left subtree or it
			// is the left boundary of the right subtree

			// if x (u handle) is in the right subtree of cNv
			// we need to reverse the main path
			if (c.isReversed()) {
				if (c.left.needsNormalization())
					c.reverse();
			} else {
				if (c.right.needsNormalization())
					c.reverse();
			}
		}

		// top-down normalization of marked tree parts
		c.clearNormalizationFlag();
		if (c.isCompress())
			((CompressClusterNode) c).normalize();

		while (c != bottom) {
			if (c.isCompress()) {
				CompressClusterNode cc = (CompressClusterNode) c;
				if (c.left.needsNormalization())
					c = c.left;
				else if (c.right.needsNormalization())
					c = c.right;
				else if (cc.leftFoster != null && cc.leftFoster.needsNormalization())
					c = cc.leftFoster;
				else if (cc.rightFoster != null && cc.rightFoster.needsNormalization())
					c = cc.rightFoster;
			} else if (c.isRake()) {
				if (c.left.needsNormalization())
					c = c.left;
				else if (c.right.needsNormalization())
					c = c.right;
			}

			c.clearNormalizationFlag();
			if (c.isCompress())
				((CompressClusterNode) c).normalize();

		}
	}


	/**
	 * This method make a copy of direct parent-referenced predecessor of given cluster node 
	 * and returns it. Given cluster node cannot be null and in field <code>origTop</code>
	 * must be given node. If parameter <code>guard</code> is not null and the direct
	 * predecessor is not different from the guard, then null is returned.
	 * <p>
	 * The method is used during rebuilding the Top Tree where is necessary to copy father of some node.
	 * If the father has type clean or obsolete, then method {@link toptree.impl.ClusterNode#cloneNew()}
	 * is called and old father is set to be dirty. If the father has type new, then it is not 
	 * necessary to do a copy, because this node holds no information yet.
	 * <p>
	 * If the copy is made (for new node work with original node) then references from sons are
	 * set to their new father.
	 * 
	 * @param c A cluster node whose direct parent-referenced predecessor will be duplicated. 
	 * @param guard A cluster node which is predecessor on the way from <code>c</code> to the
	 *              root of the Top Tree and which signs stop on the way of duplications.
	 * @return The new duplicated node. If <code>c</code> and <code>guard</code> are same, then null.
	 */
	private ClusterNode dupParent(ClusterNode c, ClusterNode guard) {
		assert origTop != null;
		assert c != null;

		ClusterNode op = c.parent;	// Take a father of the current node.

		// If father is not null and same as guard then return null. 
		if (op == null || op == guard)
			return null;

		assert !op.isBase();	// Father cannot be base cluster node.
		ClusterNode newParent;	// Prepare for copy.
		if (op.isClean() || op.isObsolete()) {
			// For clean or obsolete node do really copy.
			newParent = op.cloneNew();
			op.setDirty();
		} else {
			// For new node is not need to do a copy.
			assert c.isNew();
			newParent = op;
		}
		if (newParent.isCompress()) {
			// Made copy is compress node.
			CompressClusterNode cc = (CompressClusterNode) newParent;
			// Set reference from left foster if it exists.
			if (cc.leftFoster != null) {
				assert cc.leftFoster.parent == null;
				cc.leftFoster.link = newParent;
			}
			// Set reference from right foster if it exists.
			if (cc.rightFoster != null) {
				assert cc.rightFoster.parent == null;
				cc.rightFoster.link = newParent;
			}
			// The node c may be a copy so its old father reference to him cannot be used.
			if (op.left == origTop) {
				// c is left.
				newParent.left = c;
				newParent.right.parent = newParent;
			} else {
				// c is right.
				assert op.right == origTop;
				newParent.right = c;
				newParent.left.parent = newParent;
			}
			// Check link of proper children
			assert newParent.left.link == null;
			assert newParent.right.link == null;

		} else {
			// Made copy is rake node.
			assert op.isRake();

			// The node c may be a copy so its old father reference to him cannot be used.
			if (op.left == origTop) {
				// c is left.
				newParent.left = c;
				assert newParent.left.link == null;
				// link or parent reference from right son? 
				if (newParent.right.parent != null) {
					newParent.right.parent = newParent;
					assert newParent.right.link == null;
				} else {
					newParent.right.link = newParent;
					assert newParent.right.parent == null;
				}
			} else {
				// c is right.
				assert op.right == origTop;
				newParent.right = c;
				assert newParent.right.link == null;
				// link or parent reference from left son? 
				if (newParent.left.parent != null) {
					newParent.left.parent = newParent;
					assert newParent.left.link == null;
				} else {
					newParent.left.link = newParent;
					assert newParent.left.parent == null;
				}
			}
		}
		assert c.link == null;
		c.parent = newParent;	// New father for c.
		origTop = op;			// New top of the sequence of dirty nodes (needed for clean dirty nodes).

		return newParent;
	}


	/**
	 * This method rebuilds a subtree of given cluster node <code>x</code> so that this node will
	 * be a root of the subtree if given null <code>guard</code> and so that <code>x</code> will be
	 * a son of <code>guard</code> in the subtree if given some not null <code>guard</code>. The root
	 * of rebuilt subtree is returned. The given node <code>x</code> cannot be null and the 
	 * property <code>origTop</code> of this Top Tree is set to the node <code>x</code>.
	 * <p>
	 * The subtree is meant in the sense of parent references from sons to their fathers. So the root
	 * of the subtree in described sense is a node that has null parent reference. If terms as son, father,
	 * grandfather and so on are used here, it is in the sense of parent reference.
	 * <p>
	 * The node <code>guard</code> should be a predecessor of the node <code>x</code>in the subtree 
	 * for described action. If it will not be then the method works as if there is no <code>guard</code>.  
	 * <p>
	 * If <code>x</code> has no parent-referenced father, then nothing is done 
	 * and <code>x</code> is returned.
	 * <p>
	 * If <code>x</code> has the parent-referenced father which is the <code>guard</code>,
	 * then the method returns the root of this subtree.
	 * <p>
	 * If <code>x</code> has the parent-referenced father which is not the <code>guard</code>,
	 * then the node <code>x</code> is step by step moved up to the root of the subtree or to 
	 * be the son of the <code>guard</code> (if not null). If the node <code>x</code> is not 
	 * new then the copy of it is done. Next the copies of its father and grandfather are done.
	 * While the father is not null and is not the <code>guard</code> then one step of splay
	 * operation is done as described in articles in last paragraph. When it finished and the father
	 * is the <code>guard</code> then the root of this subtree is returned. Else the node
	 * <code>x</code> is the root and it is returned. All nodes on the way up are 
	 * duplicated because after the rebuilding of the tree is finished, the user defined
	 * information must be recomputed for these nodes by method
	 * {@link toptree.impl.TopTreeImpl#cleanUp(TopIntermediate[])} or 
	 * {@link toptree.impl.TopTreeImpl#cleanUp(ClusterNode, ClusterNode)}.
	 * <p>
	 * The operation Spay is in detail described in these two articles:
	 * <ul>
	 *   <li>R. E. Tarjan, R. F. Werneck - 
	 *   <a href="../doc-files/TW05.pdf">Self-Adjusting Top Trees</a>
	 *   (how Splay is used in Top Tree)</li>
	 *   <li>D. D. Sleator, R. E. Tarjan - 
	 *   <a href="../doc-files/p652-sleator-1.pdf">Self-Adjusting Binary Search Trees</a>
	 *   (how Splay works)</li>
	 * </li>
	 *  
	 * @param x A cluster node which will be moved as up as possible in its subtree.
	 * @param guard A cluster node (from the same subtree as <code>x</code>) which is predecessor
	 *              of <code>x</code> and which will become a father of <code>x</code>.
	 * @return A root of the subtree where <code>x</code> was moved up.
	 */
	private ClusterNode splay(ClusterNode x, ClusterNode guard) {
		// Neither x or origTop can be null.
		assert x != null;
		assert origTop != null;

		// Check if x is a local root.
		if (x.parent == null)
			return x; // It is -- we are done.

		// Set compress tree / rake tree flag.
		boolean isRakeTree = x.isRake();

		// Check if x is guarded by its parent in this subtree.
		if (guard != null && x.parent == guard) {
			if (x.isClean()) {
				// Ok, we can just traverse up to the local root.
				while (x.parent != null)
					x = x.parent;
			} else {
				assert x.isNew() || x.isObsolete();
				// We need to duplicate/modify all nodes up to next subtree to fix parent's pointers.
				while (x.parent != null) {
					x = dupParent(x, null);
					if (x.isCompress())
						((CompressClusterNode) x).recomputeVertices();
					else
						x.bindVertices();
				}
			}

			// Return the root of this subtree.
			return x;
		}

		// Compress or rake node.
		assert x.left != null && x.right != null;

		ClusterNode y, z;
		// If x is not new, then a copy of x must be done for recomputing information after rebuilding.
		if (origTop.isClean() || origTop.isObsolete()) {
			assert origTop == x;
			x = x.cloneNew();
			if (x.left.parent != null)
				x.left.parent = x;
			else
				x.left.link = x;
			if (x.right.parent != null)
				x.right.parent = x;
			else
				x.right.link = x;

			if (x.isCompress()) {
				CompressClusterNode cc = (CompressClusterNode) x;
				if (cc.leftFoster != null)
					cc.leftFoster.link = x;
				if (cc.rightFoster != null)
					cc.rightFoster.link = x;
			}
			x.bindVertices();
			origTop.setDirty();
		}

		// Copy all nodes on the path to the global root node.
		y = dupParent(x, guard);
		if (y != null)
			z = dupParent(y, guard);
		else
			z = null;

		// Splay step by step up
		while (y != null && y != guard) {

			// Compress nodes must be oriented in correct order.
			assert (z != null && z.isCompress()) ? !z.isReversed() : true;
			assert (y.isCompress()) ? !y.isReversed() : true;
			assert (x.isCompress()) ? !x.isReversed() : true;

			boolean xLeftOfy = (y.left == x);
			boolean yLeftOfz = (z != null) ? (z.left == y) : false;

			if (z == null) {
				// If no grandfather - splay only x up y.
				x.link = y.link;
				x.parent = y.parent;
				if (xLeftOfy) {
					// fig a1) left ZIG
					ClusterNode tmp = x.right;
					x.right = y;
					y.left = tmp;
					if (tmp.parent != null)
						tmp.parent = y;
					else
						tmp.link = y;
				} else {
					// fig a2) right ZIG
					ClusterNode tmp = x.left;
					x.left = y;
					y.right = tmp;
					if (tmp.parent != null)
						tmp.parent = y;
					else
						tmp.link = y;
				}
				y.link = null;
				y.parent = x;

				// Adjust local top for cluster boundaries.
				if (!isRakeTree) {
					((CompressClusterNode) y).recomputeVertices();
					((CompressClusterNode) x).recomputeVertices();
				}
				break;
			} else {
				// x, y, z are not null.
				x.link = z.link;
				x.parent = z.parent;
				if (yLeftOfz) {
					if (xLeftOfy) {
						// fig b1) left ZIG-ZIG
						ClusterNode tmp = x.right;
						x.right = y;
						y.parent = x;
						y.left = tmp;
						if (tmp.parent != null)
							tmp.parent = y;
						else
							tmp.link = y;
						tmp = y.right;
						y.right = z;
						z.link = null;
						z.parent = y;
						z.left = tmp;
						if (tmp.parent != null)
							tmp.parent = z;
						else
							tmp.link = z;
					} else {
						// fig c1) left ZIG-ZAG
						ClusterNode tmp = x.left;
						x.left = y;
						y.parent = x;
						y.right = tmp;
						if (tmp.parent != null)
							tmp.parent = y;
						else
							tmp.link = y;
						tmp = x.right;
						x.right = z;
						z.link = null;
						z.parent = x;
						z.left = tmp;
						if (tmp.parent != null)
							tmp.parent = z;
						else
							tmp.link = z;
					}
				} else {
					if (xLeftOfy) {
						// fig b2) right ZIG-ZAG
						ClusterNode tmp = x.right;
						x.right = y;
						y.parent = x;
						y.left = tmp;
						if (tmp.parent != null)
							tmp.parent = y;
						else
							tmp.link = y;
						tmp = x.left;
						x.left = z;
						z.link = null;
						z.parent = x;
						z.right = tmp;
						if (tmp.parent != null)
							tmp.parent = z;
						else
							tmp.link = z;
					} else {
						// fig a2) right ZIG-ZIG
						ClusterNode tmp = x.left;
						x.left = y;
						y.parent = x;
						y.right = tmp;
						if (tmp.parent != null)
							tmp.parent = y;
						else
							tmp.link = y;
						tmp = y.left;
						y.left = z;
						z.link = null;
						z.parent = y;
						z.right = tmp;
						if (tmp.parent != null)
							tmp.parent = z;
						else
							tmp.link = z;
					}
				}

				// Adjust local top for cluster boundaries.
				if (!isRakeTree) {
					((CompressClusterNode) z).recomputeVertices();
					((CompressClusterNode) y).recomputeVertices();
					((CompressClusterNode) x).recomputeVertices();
				}

				// Carefully adjust x, y, z pointers.
				// Copy all nodes on the path to the global root node.
				y = dupParent(x, guard);
				if (y == null)
					break;

				z = dupParent(y, guard);
			}
		}

		// Check if x is guarded by its parent in this subtree.
		if (guard != null && x.parent == guard) {
			assert x.isNew() || x.isObsolete();

			// If x is new we need to duplicate/modify all nodes up to next subtree to fix parent's pointers
			while (x.parent != null) {
				x = dupParent(x, null);
				if (x.isCompress())
					((CompressClusterNode) x).recomputeVertices();
				else
					x.bindVertices();
			}
		}

		return x; // Return the new root of this subtree.
	}


	/**
	 * This method make a copy of direct link-referenced predecessor of given cluster node 
	 * and returns it. Given cluster node cannot be null and in field <code>origTop</code>
	 * must be given node. 
	 * <p>
	 * The method is used during rebuilding the Top Tree where is necessary to copy father of some node.
	 * If the father has not type new, then method {@link toptree.impl.ClusterNode#cloneNew()}
	 * is called and old father is set to be dirty. If the father has type new, then it is not 
	 * necessary to do a copy, because this node holds no information yet.
	 * <p>
	 * If the copy is made (for new node work with original node) then references from sons are
	 * set to their new father.
	 * 
	 * @param x A cluster node whose direct link-referenced predecessor will be duplicated.
	 * @return The new duplicated node.
	 */
	private ClusterNode dupLinkedParent(ClusterNode x) {
		// x cannot be null and its has not parent-reference - only link-reference.
		assert x != null;
		assert x.parent == null;

		ClusterNode olp = x.link;

		ClusterNode newParent;	// Prepare new node for duplication.
		if (!olp.isNew()) {
			// For not new (clean or obsolete) do really copy.
			newParent = olp.cloneNew();
			olp.setDirty();
		} else {
			// For new node work with original father.
			newParent = olp;
		}

		if (newParent.isCompress()) {
			// Father is compress cluster node.
			CompressClusterNode cc = (CompressClusterNode) newParent;

			// The node x may be a copy so its old father reference to him cannot be used.
			if (cc.leftFoster == origTop) {
				// x is left foster.
				cc.leftFoster = x;
				if (cc.rightFoster != null)
					cc.rightFoster.link = newParent;
			} else {
				// x is right foster.
				assert cc.rightFoster == origTop;
				cc.rightFoster = x;
				if (cc.leftFoster != null)
					cc.leftFoster.link = newParent;
			}
			// Check link of proper children.
			assert cc.left.link == null;
			assert cc.right.link == null;
			// Set parent of proper children.
			cc.left.parent = newParent;
			cc.right.parent = newParent;
		} else {
			// Father is rake cluster node.
			assert newParent.isRake();

			// The node x may be a copy so its old father reference to him cannot be used.
			if (newParent.left == origTop) {
				// x is left child.
				newParent.left = x;
				assert newParent.left.parent == null;	// Father and son cannot be both rakes. 

				// Look if right son has parent or link reference and set it.
				if (newParent.right.parent != null) {
					newParent.right.parent = newParent;
					assert newParent.right.link == null;
				} else {
					newParent.right.link = newParent;
					assert newParent.right.parent == null;
				}
			} else {
				// x is right child
				assert newParent.right == origTop;
				newParent.right = x;
				assert newParent.right.parent == null;	// Father and son cannot be rakes together.

				// Look if left son has parent or link reference and set it.
				if (newParent.left.parent != null) {
					newParent.left.parent = newParent;
					assert newParent.left.link == null;
				} else {
					newParent.left.link = newParent;
					assert newParent.left.parent == null;
				}
			}
		}

		assert x.parent == null;
		x.link = newParent;			// New father for x.
		newParent.bindVertices();	// Set cluster for boundary vertices.
		origTop = olp;				// New top of the sequence of dirty nodes (needed for clean dirty nodes).

		return newParent;
	}


	/**
	 * This method reverse given cluster node <code>c</code> if its right boundary vertex is not
	 * given vertex <code>v</code> and return the node <code>c</code>.
	 * 
	 * @param v A vertex that will be the right boundary vertex of cluster node <code>c</code>.
	 * @param c	A cluster node that needs to have vertex <code>v</code> as right boundary.
	 * @return Cluster node <code>c</code> whose right boundary vertex is <code>v</code>.
	 */
	private ClusterNode reverseTo(Vertex v, ClusterNode c) {

		if (c.getBv() != v)
			c.reverse();

		return c;
	}


	/**
	 * This method moves given cluster node <code>vy</code> from foster child's subtree 
	 * of a compress cluster node to proper child position of the same compress cluster node
	 * and returns this compress cluster node. The given cluster node must be base or compress.
	 * <p>
	 * Basic principle of the operation Splice is described in the following article 
	 * in the section 4:<br />
	 * R. E. Tarjan, R. F. Werneck - <a href="../doc-files/TW05.pdf">Self-Adjusting Top Trees</a><br />
	 * But the operation is little complicated in detail. It must be distinguished if the father of
	 * the node <code>vy</code> is compress or rake cluster node:
	 * <p>
	 * 1) The compress father situation means that between the given node <code>vy</code> and the 
	 * nearest compress predecessor there is no rake cluster node. So it is easy. According to the 
	 * position (left/right) of <code>vy</code> in the father, this compress node is little 
	 * changed - given node <code>vy</code> becomes proper child and foster subtrees are rebuilt.
	 * And that is all.
	 * <p>
	 * For the rake father it must be distinguished between two next situations:<br />
	 * 2a) If grandfather is compress cluster node, it means that only one rake is between the given
	 * cluster node <code>vy</code> and its nearest compress predecessor. So the father is divided, 
	 * the given node <code>vy</code> becomes the proper child of the grandfather and new foster
	 * children subtrees are built for grandfather.<br />
	 * 2b) If grandfather is rake cluster node, it means that there are two rakes between the given node
	 * <code>vy</code> and its nearest compress predecessor. So both rakes are divided, the given 
	 * cluster node <code>vy</code> becomes the proper child of the great-grandfather and new foster
	 * children subtrees are built for great-grandfather.
	 * <p>
	 * Let's look on these three situations from another corner. Imagine now the root path and take one
	 * vertex on this path. It has some neighbor on the left and on the right and there are some
	 * other ways ended in this vertex (foster children). Consider these ways only on one side (only
	 * one foster child rake subtree). Let there is more then four ways (rake subtree has more then
	 * four leaves). And now it is easy to present meant three situations. If the splice is called on
	 * first or last way in the circular order around the vertex on one side, it is situation 1).
	 * If on second or last but one way, it is situation 2a). And if on other inner ways, it is 
	 * situation 2b). 
	 * 
	 * @param vy A base or compress cluster node which will become proper son of its nearest compress predecessor. 
	 * @return A compress cluster node whose proper child the given cluster node <code>vy</code> became.
	 */
	private ClusterNode spliceStep(ClusterNode vy) {
		// vy is not null.
		assert vy != null;
		assert (vy.isCompress()) ? !vy.isReversed() : true;

		CompressClusterNode cNv;	// here will be the nearest compress predecessor
		ClusterNode vylp = vy.link;	// father
		assert vylp != null;

		// Distinguish three situations:
		if (vylp.isCompress()) {
			// 1) vy is directly linked to another compress subtree
			cNv = (CompressClusterNode) dupLinkedParent(vy);
			Vertex v = cNv.getCompressedVertex();
			assert !cNv.isReversed();

			if (cNv.leftFoster == vy) {
				// direct left variant
				ClusterNode vx = cNv.left;
				ClusterNode cC = cNv.rightFoster;

				// restructure nodes
				ClusterNode cnR = (cC != null) ? createRakeClusterNode(v, vx, cC)
												: reverseTo(v, vx);
				cNv.leftFoster = null;
				cNv.left = vy;
				cNv.rightFoster = cnR;
				cnR.parent = null;
				cnR.link = cNv;
				vy.parent = cNv;
				vy.link = null;

				// Recompute boundaries for compress node or for non-compress node.
				if (vx.isCompress())
					((CompressClusterNode) vx).recomputeVertices();
				else
					vx.bindVertices();	// Only base nodes.

			} else if (cNv.rightFoster == vy) {
				// direct right variant
				ClusterNode vx = cNv.left;
				ClusterNode cA = cNv.leftFoster;

				// restructure nodes
				ClusterNode cnR = (cA != null) ? createRakeClusterNode(v, cA, vx)
												: reverseTo(v, vx);
				cNv.leftFoster = cnR;
				cNv.left = vy;
				cNv.rightFoster = null;
				cnR.parent = null;
				cnR.link = cNv;
				vy.parent = cNv;
				vy.link = null;

				// Recompute boundaries for compress node or for non-compress node.
				if (vx.isCompress())
					((CompressClusterNode) vx).recomputeVertices();
				else
					vx.bindVertices();	// Only base nodes.

			}
		} else {
			// Father is rake
			assert vylp.isRake();

			// vyp is the first rake node above vy
			ClusterNode vyp = dupLinkedParent(vy);

			if (vyp.parent == null) {
				// 2a) one rake between vy and Nv
				assert vyp.link != null && vyp.link.isCompress();

				cNv = (CompressClusterNode) dupLinkedParent(vyp);
				Vertex v = cNv.getCompressedVertex();
				assert !cNv.isReversed();
				ClusterNode cA, cB, cC, cR, vx;

				vx = cNv.left;
				if (cNv.leftFoster == vyp) {
					// left variant

					cC = cNv.rightFoster;
					cR = cNv.leftFoster;
					if (vyp.left == vy) {
						// left ZIG
						cB = cR.right;
						assert vy == cR.left;

						// restructure nodes
						ClusterNode cnR2 = (cC != null) ? createRakeClusterNode(v, vx, cC)
														: reverseTo(v, vx);
						ClusterNode cnR1 = createRakeClusterNode(v, cB, cnR2);
						cNv.leftFoster = null;
						cNv.rightFoster = cnR1;
						cnR1.parent = null;
						cnR1.link = cNv;
					} else {
						// left ZAG
						cA = cR.left;
						assert vy == cR.right;

						// restructure nodes
						ClusterNode cnR = (cC != null) ? createRakeClusterNode(v, vx, cC)
														: reverseTo(v, vx);
						cNv.leftFoster = cA;
						cNv.rightFoster = cnR;
						cA.parent = null;
						cA.link = cNv;
						cnR.parent = null;
						cnR.link = cNv;
					}
				} else {
					// right variant
					assert cNv.rightFoster == vyp;

					cA = cNv.leftFoster;
					cR = cNv.rightFoster;
					if (vyp.right == vy) {
						// right ZIG
						cB = cR.left;
						assert vy == cR.right;

						// restructure nodes
						ClusterNode cnR2 = (cA != null) ? createRakeClusterNode(v, cA, vx)
														: reverseTo(v, vx);
						ClusterNode cnR1 = createRakeClusterNode(v, cnR2, cB);
						cNv.leftFoster = cnR1;
						cNv.rightFoster = null;
						cnR1.parent = null;
						cnR1.link = cNv;
					} else {
						// right ZAG
						cC = cR.right;
						assert vy == cR.left;

						// restructure nodes
						ClusterNode cnR = (cA != null) ? createRakeClusterNode(v, cA, vx)
														: reverseTo(v, vx);
						cNv.leftFoster = cnR;
						cNv.rightFoster = cC;
						cnR.parent = null;
						cnR.link = cNv;
						cC.parent = null;
						cC.link = cNv;
					}
				}
				cNv.left = vy;
				vy.parent = cNv;
				vy.link = null;

				// Recompute boundaries for compress node or for non-compress node.
				if (vx.isCompress())
					((CompressClusterNode) vx).recomputeVertices();
				else
					vx.bindVertices();	// Only base nodes.

			} else {
				// 2b) Two rakes betweeen vy and Nv.

				// vypp is the second rake node above vy
				ClusterNode vypp = dupParent(vyp, null);
				cNv = (CompressClusterNode) dupLinkedParent(vypp);
				Vertex v = cNv.getCompressedVertex();
				assert !cNv.isReversed();

				boolean zLeftOfv = (cNv.leftFoster == vypp);
				boolean yLeftOfz = (vypp.left == vyp);

				ClusterNode cA, cB, cC, cR1, cR2, vx;
				if (zLeftOfv) {
					// full splice, left variant
					cC = cNv.rightFoster;
					vx = cNv.left;
					cR1 = cNv.leftFoster;
					if (yLeftOfz) {
						// left RIGHT-ZIG
						cR2 = cR1.left;
						cB = cR1.right;
						cA = cR2.left;
						assert vy == cR2.right;	// vy is at least third way from the proper child
					} else {
						// left LEFT-ZIG
						cA = cR1.left;
						cR2 = cR1.right;
						cB = cR2.right;
						assert vy == cR2.left;	// vy is at least third way from the proper child
					}

					// restructure nodes
					ClusterNode cnR2 = (cC != null) ? createRakeClusterNode(v, vx, cC) : reverseTo(
						v, vx);
					ClusterNode cnR1 = createRakeClusterNode(v, cB, cnR2);
					cNv.leftFoster = cA;
					cNv.left = vy;
					cNv.rightFoster = cnR1;

					cA.parent = null;
					cA.link = cNv;
					vy.parent = cNv;
					vy.link = null;
					cnR1.parent = null;
					cnR1.link = cNv;
				} else {
					// full splice, right variant

					cA = cNv.leftFoster;
					vx = cNv.left;
					cR1 = cNv.rightFoster;
					if (yLeftOfz) {
						// right RIGHT-ZIG
						cR2 = cR1.left;
						cC = cR1.right;
						cB = cR2.left;
						assert vy == cR2.right;	// vy is at least third way from the proper child
					} else {
						// right LEFT-ZIG
						cB = cR1.left;
						cR2 = cR1.right;
						cC = cR2.right;
						assert vy == cR2.left;	// vy is at least third way from the proper child
					}

					// restructure nodes
					ClusterNode cnR2 = createRakeClusterNode(v, vx, cB);
					ClusterNode cnR1 = (cA != null) ? createRakeClusterNode(v, cA, cnR2) : cnR2;
					cNv.leftFoster = cnR1;
					cNv.left = vy;
					cNv.rightFoster = cC;

					cnR1.parent = null;
					cnR1.link = cNv;
					vy.parent = cNv;
					vy.link = null;
					cC.parent = null;
					cC.link = cNv;
				}

				// Recompute boundaries for compress node or for non-compress node.
				if (vx.isCompress())
					((CompressClusterNode) vx).recomputeVertices();
				else
					vx.bindVertices();	// Only base nodes.

			}
		}
		// move up in the tree
		cNv.recomputeVertices();
		return cNv;
	}


	/**
	 * This method make a copy of given cluster node and returns it. Given cluster node cannot 
	 * be null and in field <code>origTop</code> must be given node. The method supposes that
	 * given cluster node is the top node of some tree. 
	 * <p>
	 * The method is used during rebuilding the Top Tree where is necessary to copy some node.
	 * It make a copy of give cluster only if the node has clean or obsolete type and is not 
	 * base. Else it returns original cluster node.
	 * <p.
	 * The copy is done by method {@link toptree.impl.ClusterNode#cloneNew()} and then
	 * is called and old father is set to be dirty. If the father has type new, then it is not 
	 * necessary to do a copy, because this node holds no information yet.
	 * <p>
	 * If the copy is made (for new node work with original node) then references from sons are
	 * set to their new father.
	 * 
	 * @param x A cluster node that will be duplicated.
	 * @return The new duplicated node.
	 */
	private ClusterNode dupCurrent(ClusterNode x) {
		// Duplicate only clean or obsolete node which is not base.
		if ((origTop.isClean() || origTop.isObsolete()) && !origTop.isBase()) {
			assert origTop == x;	// x is in origTop.
			x = x.cloneNew();		// Make a copy.
			assert x.left != null && x.right != null;	// POKUSNY ASSERT
			if (x.left != null) {	
				// Repair a reference from left son.
				if (x.left.parent != null)
					x.left.parent = x;
				else
					x.left.link = x;
			}
			if (x.right != null) {
				// Repair a reference from right son.
				if (x.right.parent != null)
					x.right.parent = x;
				else
					x.right.link = x;
			}
			if (x.isCompress()) {
				// If x is compress then reprair references from fosters.
				CompressClusterNode cc = (CompressClusterNode) x;
				if (cc.leftFoster != null)
					cc.leftFoster.link = x;
				if (cc.rightFoster != null)
					cc.rightFoster.link = x;
			}
			x.bindVertices();	// Set cluster for boundary vertices. 
			origTop.setDirty();	// The original node become dirty.
		}

		return x;
	}


	/**
	 * This method make the given cluster node <code>x</code> a part of the root path of 
	 * the Top Tree and returns this node. The given cluster node must be base or compress.
	 * <p>
	 * If given node <code>x</code> has parent referenced father then it must be its guard,
	 * so the method only returns given node.
	 * <p>
	 * If given node <code>x</code> has no predecessor, so it is the root of whole Top Tree.
	 * The method returns this node.
	 * <p>
	 * In other cases the operation splice is done as described in the following article
	 * in section 4:<br />
	 * R. E. Tarjan, R. F. Werneck - <a href="../doc-files/TW05.pdf">Self-Adjusting Top Trees</a><br />
	 * Current node is a leaf of some foster subtree. The method 
	 * {@link toptree.impl.TopTreeImpl#spliceStep(ClusterNode)} is used for making the current
	 * node a proper son of the nearest compress predecessor (the part of the higher path).
	 * Then it traverses up on compress cluster nodes under the nearest rake predecessor (the
	 * part of rake subtree closer to the root path). All nodes it works with are duplicated for
	 * computing actual user defined information after rebuilding. Described cycle repeats until
	 * the given node become the part of the root path.  
	 * 
	 * @param x A cluster node which will become a part of the root path.
	 * @return A duplicate of given node <code>x</code> which is on the root path.
	 */
	private ClusterNode splice(ClusterNode x) {
		assert x != null;
		assert !x.isRake();
		assert origTop == x;

		if (x.parent != null) {
			// x.parent must be a guard, we don't need to splice on x
			return x;
		}

		if (x.parent == null && x.link == null) {
			// vy is already the root of the whole tree
			return x;
		}

		x = dupCurrent(x);
		ClusterNode lowestX = x;	// This node will be returned.
		do {
			// While x is not the root of the tree do this:
			x = spliceStep(x);	// Splice x to be part of higher path and take new father of x.
			while (x.parent != null) {
				// While x has compress father then traverse up.
				x = dupParent(x, null);
				assert x.isCompress();
				((CompressClusterNode) x).recomputeVertices();	// Repair boundaries.
			}
		} while (x.link != null);

		return lowestX;	// Return given first x.
	}


	/**
	 * This method rebuilds the Top Tree so that given vertex become the root or a part of the root
	 * path of the Top Tree and returns an instance of {@link toptree.impl.TopIntermediate} class.
	 * The given vertex <code>u</code> cannot be null and must not be single. If second parameter 
	 * <code>guard</code> is not null, then it is the root of the Top Tree and cannot be changed
	 * in this position by any other cluster node. So the handler of the given vertex is moved only
	 * as near as possible to the this <code>guard</code>.
	 * <p>
	 * This method is well described in the following article in section 4:<br />
	 * R. E. Tarjan, R. F. Werneck - <a href="../doc-files/TW05.pdf">Self-Adjusting Top Trees</a>
	 * <p>
	 * A cluster node of the given vertex is taken and the method starts to work with it.
	 * First must be prepared correct orientation in all cluster nodes from taken node to
	 * the root. When it is done, then there are three phases of the operation soft-expose:
	 * <ol>
	 *   <li>Series of the local splays that are made by repeated calling of the method
	 *   {@link toptree.impl.TopTreeImpl#splay(ClusterNode, ClusterNode)}.
	 *   It starts in the cluster node of the given vertex. It is  base or compress cluster node.
	 *   This current node is splayed and becomes a leaf of some rake subtree. Next this node is
	 *   splayed in this subtree of rake nodes. It ends in one of three situations:
	 *     <ol type="a">
	 *       <li>The node become the son of the nearest compress predecessor. The method continues
	 *       by splaying on the same node again as described in first phase.</li>
	 *       <li>Between the node and its nearest compress predecessor there is one rake node. 
	 *       It is not problem of this phase (splice solves it). So the nearest compress 
	 *       predecessor is taken and the first phase runs again on this node.</li>
	 *       <li>Between the node and its nearest compress predecessor there are two rake nodes.
	 *       The method progresses in the same way as in previous situation.</li>
	 *     </ol>
	 *   </li>
	 *   <li>Splice phase. Now the cluster node of the given vertex is moved to the root path.
	 *   It is made by method {@link toptree.impl.TopTreeImpl#splice(ClusterNode)}.</li>
	 *   <li>Global splay. After second phase the cluster node of the given vertex is the 
	 *   part of the root path. But may be it is not the root or if the <code>guard</code> is 
	 *   not null, it could be moved more closely to the <code>guard</code>. And that is exactly
	 *   done here by one calling of the method 
	 *   {@link toptree.impl.TopTreeImpl#splay(ClusterNode, ClusterNode)}.</li>
	 * </ol>
	 * If the <code>guard</code> is not null then it is checked if the given vertex is the right
	 * boundary vertex. If not so it is reversed by {@link toptree.impl.ClusterNode#reverse()}.
	 * In the end the root of old nodes and of the rebuild tree are returned.
	 * 
	 * @param u	A vertex that will be the part of the root path of this Top Tree.
	 * @param guard	A cluster node that if not null value has then it is the root of whole 
	 * 				Top Tree and must stay to be the root. 
	 * @return	The instance of {@link toptree.impl.TopIntermediate} class with the root of 
	 * 			the sequence of old cluster nodes and of the rebuilt Top Tree. 
	 */
	private TopIntermediate softExpose(VertexInfo u, ClusterNode guard) {
		assert u != null;
		// If guard not null, then it is the root and it is compress.
		assert (guard != null) ? (guard.isTopCluster() && guard.isCompress()) : true;
		// Not single vertex
		assert u.getDegree() > 0;

		// Take the cluster node of u.
		ClusterNode x = u.getClusterNode();
		assert x != null;

		// Preparation phase - rectify all nodes from the top to node cNx.
		if (guard == null)
			rectify(x, null);
		else
			rectify(x, ((CompressClusterNode) guard).getCompressedVertex());

		// 1. series of local splays
		origTop = x;
		x = splay(x, guard);
		while (x.link != null) {

			ClusterNode xp = dupLinkedParent(x);
			ClusterNode r;
			if (xp.isRake()) {
				// splay on rake subtree
				r = splay(xp, null);
				if (x.link != xp) {
					ClusterNode lastOrig = origTop;
					origTop = x.link;
					splay(x.link, r);
					origTop = lastOrig;
				}
				r = dupLinkedParent(r);
			} else
				r = xp;

			// splay on compress subtree
			x = splay(r, guard);
		}

		assert x != null && x.isTopCluster();
		assert origTop != null;
		ClusterNode oldTop = this.origTop;	// Remember the root of old nodes for returning.

		/* We have to set the guard to its corresponding new value
		 * in case the original tree was modified. */ 
		if (origTop == guard)
			guard = x; // the guard is always the top cluster in this function

		// 2. splice
		x = u.getClusterNode();
		origTop = x;
		x = splice(x);
		assert x.link == null;

		// 3. global splay
		x = u.getClusterNode();
		rectify(x, null);
		origTop = x;

		// splay on x if it is not already root
		if (x.parent != null) {
			// check if x is the root of a guarded subtree
			if (x.parent != guard) {
				if (x.isBase()) {
					// we can't splay on a base cluster directly
					// we splay on its handle and hard_expose it later
					origTop = x.parent;
					x = splay(x.parent, guard);
				} else {
					origTop = x;
					x = splay(x, guard);
				}
			} else {
				// yes, we are finished
				// just reset x to be the new root of the tree
				x = guard;
			}
		}

		assert x != null && x.link == null;
		assert x.isTopCluster();

		// make the exposed vertex the right boundary of the root cluster
		// do nothing if u is the vertex compressed in x
		if (x.getBu() == u) {
			x.reverse();
			assert x.getBv() == u;
		}

		// return the new root of the top tree
		return new TopIntermediate(oldTop, x);
	}


	/**
	 * This method rebuilds the Top Tree so that both given vertices <code>u</code> and
	 * <code>v</code> will be represented on the root path and returns rebuilt Top Tree in the
	 * array of instances of the class {@link toptree.impl.TopIntermediate}. If the vertices
	 * are in different components, it returns two instances with vertices represented in the
	 * roots of these two Top Trees. If the vertices are in common one component then it returns
	 * one instance with either both vertices as end-points of the root path or one as end-point
	 * and second represented in the root cluster node as <code>compressedVertex</code>.
	 * Both given vertices must be not null and not be single.
	 * <p>
	 * The operation soft-expose is well described in the following article in section 4:<br />
	 * R. E. Tarjan, R. F. Werneck - <a href="../doc-files/TW05.pdf">Self-Adjusting Top Trees</a>
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, ClusterNode)}
	 * on the vertex <code>v</code> and no guard. Next it looks if the cluster node representing
	 * <code>u</code> is the same as the cluster node representing <code>v</code>. Three situations
	 * can happen:
	 * <ul>
	 *   <li>They are the same. It is easier situation. The method just check, if the vertices are
	 *   both end-points or one is end-point and second is represented by the root node. In the
	 *   second case the root node may be reversed if the vertex in end-point is not the right
	 *   boundary vertex.</li>
	 *   <li>The nodes are different and <code>v</code> has degree one. Then the method tries to
	 *   make the cluster node representing <code>u</code> the root and <code>v</code> 
	 *   the end-point by calling {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, ClusterNode)}
	 *   on the vertex <code>u</code> and no guard. If the vertices are then in different
	 *   components it returns these two components that both have one of vertices represented
	 *   in their root. Else it checks only the end-point to be the right boundary vertex and
	 *   returns only one component.</li>
	 *   <li>The nodes are different and <code>v</code> has degree more than one.  Then the method
	 *   calls {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, ClusterNode)}
	 *   on the vertex <code>u</code> and the guard is the root cluster node 
	 *   representing <code>v</code>. The result is that either both vertices are in different
	 *   components or that they are in the same component. If they are in the same component, then
	 *   <code>u</code> is checked to be the right boundary vertex if it has degree one or
	 *   to be represented in right proper son of the root and one component is returned.
	 *   If the vertices are in different components then these components are returned.</li>
	 * </ul>
	 * 
	 * @param u	A vertex that will be a part of the root path in its Top Tree.
	 * @param v	A vertex that will be a part of the root path in its Top Tree.
	 * @return	An array of instances of {@link toptree.impl.TopIntermediate} class - one instance
	 * 			if both given vertices are in one component and two if they are in two different
	 * 			components
	 */
	private TopIntermediate[] softExpose(VertexInfo u, VertexInfo v) {
		assert u != null && v != null;	// both vertices are not null
		assert u.getDegree() > 0 && v.getDegree() > 0;	// no single vertices

		// First make v the root.
		TopIntermediate vComponent = softExpose(v, (ClusterNode) null);
		ClusterNode cNv = vComponent.newRoot;	// The root representing v.
		ClusterNode originalTopCluster = vComponent.origRoot;	// The root of sequence of old nodes.

		if (u.getClusterNode() == cNv) {
			/* Both vertices are represented by the same cluster node cNv:
			 * EITHER:	both vertices are boundaries
			 * NOR:		one is boundary and other one is compressedVertex */

			// prepare correct orientation of boundaries
			if (u.getDegree() >= 1 && v.getDegree() == 1) {
				// only v or both are boundaries - softExpose(v) had to move v to the right boundary position
				assert u.getDegree() == 1 ? cNv.getBu() == u && cNv.getBv() == v
						: ((CompressClusterNode) cNv).getCompressedVertex() == u && cNv.getBv() == v; 
			} else {
				// only u is boundary
				assert u.getDegree() == 1 && v.getDegree() >= 2; 
				// u must be on right boundary position
				if (cNv.getBu() == u) cNv.reverse();
				assert ((CompressClusterNode) cNv).getCompressedVertex() == v && cNv.getBv() == u;
			}

			return new TopIntermediate[] { vComponent };

		} else if (v.getDegree() == 1) {
			// make cNu the root with v right boundary
			TopIntermediate uComponent = softExpose(u, (ClusterNode) null);
			ClusterNode cNu = u.getClusterNode();

			if (u.getClusterNode() != v.getClusterNode()) {
				// u and v are in different components
				assert v.getClusterNode().isTopCluster();
				return new TopIntermediate[] { uComponent, vComponent };
			}

			// prepare correct orientation of boundaries
			if (u.getDegree() == 1) {
				// both are boundaries - softExpose(u) had to move u to the right boundary position
				assert cNu.getBu() == v && cNu.getBv() == u;
				cNu.reverse();
				assert cNu.getBu() == u && cNu.getBv() == v;
			} 
			else {
				/* only v is boundary - softExpose(v) had to move u to the right boundary position
				 * and softExpose(u) hadn't to change it */
				assert ((CompressClusterNode) cNu).getCompressedVertex() == u && cNu.getBv() == v;
			}
			
			// uv is represented by cNu's right child
			uComponent.origRoot = originalTopCluster;
			return new TopIntermediate[] { uComponent };
		} else {
			assert v.getDegree() >= 2;
			assert cNv.isCompress();

			// bring cNu close to the root but do not surpass cNv
			TopIntermediate uComponent = softExpose(u, cNv);
			ClusterNode cNu = u.getClusterNode();
			cNv = v.getClusterNode();

			if (cNu == cNv) {
				// u is a boundary of cNv - softExpose(u) had to move u to the right boundary position
				assert u.getDegree() == 1;
				assert cNv.getBv() == u;

				// uv is represented by cNv's right child
				uComponent.origRoot = originalTopCluster;
				return new TopIntermediate[] { uComponent };
			}
			
			// softExpose(v) even softExpose(u) had to set reverse bit  
			assert !cNv.isReversed();

			if (cNv.left == cNu) {
				// cNu is the left child of cNv
				
				// reverse the main path
				cNv.reverse();
				assert cNu.right.getBv() == v && cNu.right.getBu() == u;

				// vu is represented by cNu's left child
				uComponent.origRoot = originalTopCluster;
				return new TopIntermediate[] { uComponent };
			} else {
				// u and v are in different components
				assert cNu.isTopCluster() && cNv.isTopCluster() && cNu != cNv;
				return new TopIntermediate[] { uComponent, vComponent };
			}
		}

	}


	/**
	 * This method ensures repairing user define information in the Top Tree after the tree 
	 * is rebuild by any operation.
	 * <p>
	 * The method is easy. It only calls other two methods. 
	 * First {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} on the sequence of
	 * old nodes holding by <code>oldTop</code>.
	 * And then {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} on the sequence of
	 * new nodes holding by <code>newTop</code>.
	 * 
	 * @param oldTop A cluster node which is the root of dirty and obsolete cluster nodes.
	 * @param newTop A cluster node which is the root of new cluster nodes.
	 */
	private void cleanUp(ClusterNode oldTop, ClusterNode newTop) {
		// Call split/destroy on modified original clusters.
		cleanDirtyNodes(oldTop);

		// Call create/join on newly created clusters.
		fixateNewNodes(newTop);
	}


	/**
	 * This method computes user define information for new cluster nodes in 
	 * given components of the Top Tree. It first computes information from old 
	 * fathers to their sons in all components by calling the method 
	 * {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} on each component.
	 * And then it computes information from sons to their new fathers by calling
	 * the method {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)}
	 * on each component.
	 * 
	 * @param components 	An array of instances of the class {@link toptree.impl.TopIntermediate}
	 * 						which are the components of the Top Tree and need to compute user
	 * 						defined information in new cluster nodes.
	 */
	private void cleanUp(TopIntermediate[] components) {
		// call split/destroy on modified original clusters in all components
		for (int i = 0; i < components.length; i++)
			cleanDirtyNodes(components[i].origRoot);

		// call create/join on newly created clusters in all components
		for (int i = 0; i < components.length; i++)
			fixateNewNodes(components[i].newRoot);
	}


	/**
	 * This method rebuilds a hard-exposed Top Tree to original form. It is reverse operation to method 
	 * {@link toptree.impl.TopTreeImpl#hardExpose(CompressClusterNode, VertexInfo, VertexInfo)}.
	 * <p>
	 * It first looks if the root was reversed and normalized after <code>hardExpose</code>. If it was
	 * then the tree is restored to state before the reversion and normalization.
	 * <p>
	 * The method checks if all auxiliary fields of this class used special only for hard-expose have
	 * correct values.<br />
	 * The method looks what type of hard-expose it has:<br />
	 * <ul>
	 *   <li>If the left son of hard rake root is not hard rake cluster node, it is easy, because 
	 *   vertex <code>u</code> was end-point and there were no point cluster around it. 
	 *   The left son is rake tree which was built from point clusters around vertex <code>v</code>.
	 *   So with help of auxiliary class field is restored original compress cluster node which
	 *   is the handler of <code>v</code>.</li>
	 *   <li>If the left son of hard rake root is hard rake cluster node too, it is little complicated.
	 *   This left son represents root path cluster with left boundary <code>v</code> and right 
	 *   <code>u</code>. Left son of this hard rake cluster node is a rake tree of point clusters that 
	 *   were originally around <code>v</code> and right son represents cluster v..u without any rakes
	 *   on their boundary vertices. Right son of the hard rake top cluster node is a rake tree that
	 *   represents point clusters around vertex <code>u</code> in original Top Tree. So first the rake
	 *   tree in the left son of the left son of the root is divided into original clusters by the same 
	 *   way as in previous case. The same division is done with the rake tree in right son of the top.
	 *   Next step is to create compress cluster node that is handler of <code>u</code> from 
	 *   <code>u</code>'s point clusters (from right son of the top) and from the right son of the left 
	 *   son of the top (whole way v..u). From this compress cluster node and from <code>v</code>'s
	 *   point clusters the original top compress cluster node is created.</li>
	 * </ul>
	 * <p>
	 * In the end there are recomputed user defined information in the rebuilt tree and all auxiliary
	 * hard-expose special class fields are set to default values.
	 * 
	 */
	private void undoHardExpose() {

		/* If the root was reversed and normalized after hardExpose it must be annul before the undo 
		 * starts. */
		if ((hardExposeTop.left.parent == hardExposeTop && !hardExposeTop.left.isHardRake())
				|| (hardExposeTop.right.parent == hardExposeTop && hardExposeTop.right.isHardRake())) {
			this.hardExposeTop.reverse();
			this.hardExposeTop.normalize();
			this.hardExposeTop.left.normalize();
			this.hardExposeTop.right.normalize();
		}

		assert this.hardExposeTop != null;
		assert this.hardExposeLeftTail >= 0 && this.hardExposeLeftTail <=1;
		assert this.hardExposeTop.left.isHardRake() ? 
				(this.hardExposeRightTail >= 0 && this.hardExposeRightTail <=1)
				: this.hardExposeRightTail == -1;
		assert this.hardExposeLeftCount >= 1 && this.hardExposeLeftCount <= 3;
		assert this.hardExposeTop.left.isHardRake() ?
				(this.hardExposeRightCount >= 1 && this.hardExposeRightCount <= 3)
				: this.hardExposeRightCount ==0;
					
		// catch the root
		ClusterNode top = this.hardExposeTop;
		
		VertexInfo v = top.getBu();	// left boundary vertex of Top Tree before hardExpose
		VertexInfo u = top.getBv();	// right boundary vertex of Top Tree before hardExpose
		
		ClusterNode newtop = null;				// node for original root before hardExpose
		ClusterNode leftFosterOfTop  = null;	// original left foster of original root
		ClusterNode rightFosterOfTop = null;	// original right foster of original root
		ClusterNode leftProperOfTop  = null;	// original left proper of original root

		// catch rake tree where are clusters that were on left boundary v
		ClusterNode fosters =  top.left.isHardRake() ? top.left.left : top.left;
		
		assert fosters.getBv() == v;	// check boundary of point cluster
		
		switch (this.hardExposeLeftCount) {
		case 1:
			// there was only left proper son
			assert fosters.isBase() || fosters.isCompress();
			assert this.hardExposeLeftTail == 0;
			leftProperOfTop = fosters;
			break;
		case 2:
			// there was left proper and one of foster sons
			assert fosters.isRake();
			assert fosters.left.getBv() == v && fosters.right.getBv() == v;
			// let's look who is foster and who proper son
			switch (this.hardExposeLeftTail) {
			case 0:	// proper is the left
				assert fosters.left.isBase() || fosters.left.isCompress();
				leftProperOfTop  = fosters.left;
				rightFosterOfTop = fosters.right;
				break;
			case 1:	// proper is the right
				assert fosters.right.isBase() || fosters.right.isCompress();
				leftFosterOfTop = fosters.left;
				leftProperOfTop = fosters.right;
				break;
			default:
				assert false;
			}
			// set the top of left rake tree to be dirty
			fosters.setDirty();
			break;
		case 3:
			// there were all three sons
			assert fosters.right.isRake();
			assert this.hardExposeLeftTail == 1;
			assert fosters.right.left.isBase() || fosters.right.left.isCompress();
			assert fosters.left.getBv() == v && fosters.right.getBv() == v;
			assert fosters.right.left.getBv() == v && fosters.right.right.getBv() == v;
			leftFosterOfTop  = fosters.left;
			leftProperOfTop  = fosters.right.left;
			rightFosterOfTop = fosters.right.right;
			// set both auxiliary rake nodes to be dirty
			fosters.setDirty();
			fosters.right.setDirty();
			break;
		default:
			assert false;
		}
				
		if (top.left.isHardRake()) {

			assert top.left.getBu() == v && top.right.getBv() == u;
			assert top.right.getBv() == u;
			
			ClusterNode leftFoster  = null;	// original left foster of cNu
			ClusterNode rightFoster = null;	// original right foster of cNu
			ClusterNode rightProper = null;	// original left proper of cNu

			// catch rake tree where are cluster that were on right boundary u
			fosters = top.right;
			
			assert fosters.getBv() == u;	// check boundary of point cluster
			
			switch (this.hardExposeRightCount) {
			case 1:
				// there was only left proper son
				assert fosters.isBase() || fosters.isCompress();
				assert this.hardExposeRightTail == 0;
				rightProper = fosters;
				break;
			case 2:
				// there was right proper and one of foster sons
				assert fosters.isRake();
				assert fosters.left.getBv() == u && fosters.right.getBv() == u;
				// let's look who is foster and who proper son
				switch (this.hardExposeRightTail) {
				case 0:	// proper is the left
					assert fosters.left.isBase() || fosters.left.isCompress();
					rightProper = fosters.left;
					leftFoster  = fosters.right;
					break;
				case 1:	// proper is the right
					assert fosters.right.isBase() || fosters.right.isCompress();
					rightFoster = fosters.left;
					rightProper = fosters.right;
					break;
				default:
					assert false;
				}
				// set the top of left rake tree to be dirty
				fosters.setDirty();
				break;
			case 3:
				// there were all three sons
				assert fosters.right.isRake();
				assert this.hardExposeRightTail == 1;
				assert fosters.right.left.isBase() || fosters.right.left.isCompress();
				assert fosters.left.getBv() == u && fosters.right.getBv() == u;
				assert fosters.right.left.getBv() == u && fosters.right.right.getBv() == u;
				leftFoster  = fosters.right.right;
				rightProper = fosters.right.left;
				rightFoster = fosters.left;
				// set both auxiliary rake nodes to be dirty
				fosters.setDirty();
				fosters.right.setDirty();
				break;
			default:
				assert false;
			}
			
			// u must be left boundary vertex of right proper son
			rightProper.reverse();
			assert rightProper.getBu() == u;
			
			// check a type of left proper son of cNu
			assert top.left.right.isBase() || top.left.right.isCompress();
			
			// set dirty type in hard rake cluster nodes
			top.setDirty();
			top.left.setDirty();
			
			// create cNu - will be right proper of cNv
			newtop = createCompressClusterNode(u, top.left.right, leftFoster, rightProper, rightFoster);
			// check setting of cluster node in boundaries and compress vertex
			assert u.getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBu() == v;
			assert v.getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBv() == rightProper.getBv();
			assert ((CompressClusterNode) newtop).getBv().getClusterNode() == ((CompressClusterNode) newtop);
			
			// create cNv - new top
			newtop = createCompressClusterNode(v, leftProperOfTop, leftFosterOfTop,	newtop,
						rightFosterOfTop);
			// check setting of cluster node in boundaries and compress vertex
			assert v.getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBu() == leftProperOfTop.getBu();
			assert leftProperOfTop.getBu().getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBv() == rightProper.getBv();
			assert ((CompressClusterNode) newtop).getBv().getClusterNode() == ((CompressClusterNode) newtop);
			
		}
		else {
			// u is end-point of root path
			assert top.right.isBase() || top.right.isCompress();
			// set dirty in hard rake cluster top node
			top.setDirty();
			// create cNv - new top
			newtop = createCompressClusterNode(v, leftProperOfTop, leftFosterOfTop,
						top.right, rightFosterOfTop);
			// check setting of cluster node in boundaries and compress vertex
			assert v.getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBu() == leftProperOfTop.getBu();
			assert leftProperOfTop.getBu().getClusterNode() == ((CompressClusterNode) newtop);
			assert ((CompressClusterNode) newtop).getBv() == u;
			assert u.getClusterNode() == ((CompressClusterNode) newtop);
			
		}

		// recompute user define information for rebuilt TopTree;
		cleanUp(this.hardExposeTop, newtop);
		
		// set all auxiliary hardExpose's fields to their default values 
		this.hardExposeTop       = null;
		this.hardExposeLeftTail  = this.hardExposeRightTail  = -1;
		this.hardExposeLeftCount = this.hardExposeRightCount = 0;

		assert this.getTopComponent(v) == newtop.clusterInfo && newtop.clusterInfo != null;
	}	
	
	
	/**
	 * This method builds a rake tree of point clusters from given cluster nodes of some compress cluster
	 * node and return this new tree. The rake tree can consists of one, two ore three point clusters that
	 * are cluster nodes from <code>fosters</code> array. One of them must be proper son of the compress 
	 * cluster node and others are it's fosters if they are not null.
	 * <p>
	 * First the method checks if a right boundary of the proper son is given vertex <code>v</code>.
	 * If it is not, it means that <code>v</code> is a left boundary, so boundaries are swapped by
	 * method {@link toptree.impl.ClusterNode#reverse()}. 
	 * <p>
	 * A compress father of cluster nodes given in <code>fosters</code> array will be deleted. This father
	 * is either a root or the son of the root and both these vertices will be deleted. The father is
	 * cluster node of some boundary vertices of cluster nodes in <code>fosters</code> and cluster nodes
	 * of other their boundaries are these nodes in <code>fosters</code>. So cluster nodes of all 
	 * boundaries of non-rake cluster nodes in <code>fosters</code> must be recomputed by calling of 
	 * a method {@link toptree.impl.ClusterNode#bindVertices()}.
	 * <p>
	 * It is important to build the rake tree in correct order of given nodes. The order in 
	 * <code>fosters</code> array marks the order of nodes in binary rake tree from left.
	 * Three situations are distinguished according to the count of given nodes:
	 * <ol>
	 *   <li>One node. It is the proper son. Nothing is needed to be done. Only return this node.</li>
	 *   <li>Two nodes. A new rake node is created. Left son is from position 0 and right from position 1
	 *   from <code>fosters</code> array.</li>
	 *   <li>Three nodes. Two rake nodes are created. A left son of new top rake node is the node on 
	 *   position 1 and a right is second rake with left son from position 1 and right from position 0.</li>
	 * </ol>
	 * 
	 * @param fosters	An array of at most three cluster nodes that were sons of some compress cluster node.
	 * @param v	A vertex that will be the only boundary vertex in created rake tree. 
	 * @param position	This is an index of the only proper son in the array <code>fosters</code> with
	 * 					values 0, 1, or 2.
	 * @return	New created rake tree of point clusters.
	 */
	private ClusterNode createRakeFromCompressNode(ClusterNode[] fosters, VertexInfo v, int position) {
		
		// we may have 1,2 or 3 clusters 
		assert fosters.length >= 1 && fosters.length <= 3;
		// on "position" there is proper son
		assert fosters[position].isBase() || fosters[position].isCompress();
		
		// for end of the root path the only boundary vertex must be right-handed
		if (fosters[position].getBv() != v)
			fosters[position].reverse();

		/* A father of nodes in fosters will be deleted, so the upper nodes for boundaries 
		 * are nodes in fosters. So set new cluster nodes in these boundary vertices. */
		for(int i = 0; i < fosters.length; i++) {
			fosters[i].bindVertices();
		}
		
		ClusterNode tree = null;
		
		// In fosters there must be 1, 2 or 3 subtrees - so three cases:
		switch (fosters.length) {
		case 1:	// one cluster
			assert position == 0;
			assert fosters[0].getBv() == v;
			tree = fosters[0];
			break;
		case 2: // two clusters
			assert fosters[0].getBv() == v && fosters[1].getBv() == v;
			tree = createRakeClusterNode(v, fosters[0], fosters[1]);
			break;
		default: // three clusters
			assert position == 1;
			assert fosters[1].getBv() == v && fosters[2].getBv() == v;
			tree = createRakeClusterNode(v, fosters[1], fosters[2]);
			tree = createRakeClusterNode(v, fosters[0], tree);
		}
		
		return tree;

	}
	
	
	/**
	 * This method rebuilds given Top Tree so that two given vertices (both are a part of root path)
	 * become end-points of the root path. The resulting tree is Top Tree and holds correct user
	 * defined information, but the construction uses special type of cluster nodes
	 * {@link toptree.impl.ClusterNode#HARD_RAKE_CLUSTER} so it must be undone before any 
	 * operation with Top Tree by calling {@link toptree.impl.TopTreeImpl#undoHardExpose()}.
	 * <p>
	 * The method first checks settings of all fields of this class that are prepared special for
	 * hard-expose operation.
	 * <p>
	 * Description of hard-expose operation:<br />
	 * It must rebuilt Top Tree so that given two vertices become the boundaries of this tree. It takes
	 * all clusters (one, two or three) that are connected to <code>v</code> and build a rake tree from
	 * them. The same is done for <code>u</code>. Then it first connects new root path v..u and rake tree
	 * of <code>v</code> to one hard rake cluster node with boundaries <code>u</code> and <code>v</code>.
	 * If vertex <code>u</code> was a boundary of original Top Tree, then hard-expose is finished, because 
	 * there are no clusters around <code>u</code>. Else there is a rake tree built from these clusters.
	 * So next hard rake cluster node is created from previous hard rake cluster node and from rake tree
	 * of clusters around <code>u</code>.
	 * 
	 * @param cNv	A cluster node which is a root of current Top Tree.
	 * @param v	A vertex which will be a left boundary of the hard-exposed Top Tree.
	 * @param u	A vertex which will be a right boundary of the hard-exposed Top Tree.
	 */
	private void hardExpose(CompressClusterNode cNv, VertexInfo v, VertexInfo u) {
		
		assert this.hardExposeTop == null;
		assert this.hardExposeLeftTail == -1 && this.hardExposeRightTail == -1;
		assert this.hardExposeLeftCount == 0 && this.hardExposeRightCount == 0;
		assert cNv.getCompressedVertex() == v;
		
		/* It doesn't matter which situation we have. The left rake tree must be prepared anyway. */
		// count rakes on v now
		int x = 1;	// left proper always exists
		if (cNv.leftFoster  != null) x++;
		if (cNv.rightFoster != null) x++;
		
		// create array for rakes on v and remember the left count
		ClusterNode[] leftfosters = new ClusterNode[x];
		this.hardExposeLeftCount = x;
		
		// push rakes on v into prepared array
		x = 0;
		if (cNv.leftFoster  != null) leftfosters[x++] = cNv.leftFoster;
		this.hardExposeLeftTail = x;	// remember a position of the left end of the root path
		leftfosters[x++] = cNv.left;
		if (cNv.rightFoster != null) leftfosters[x++] = cNv.rightFoster;
		
		// create left rake tree
		ClusterNode leftRakeTree = createRakeFromCompressNode(leftfosters, v, this.hardExposeLeftTail);
		
		// original top becomes dirty
		if (!cNv.isNew()) { 
			cNv.setDirty();
		}
		
		/* We distinguish between two situations - if u has degree one or more. */
		if (cNv.getBv() == u) {
			/* This is the easiest situation. Vertex u has degree one.
			 * There is only left rake tree, because there are no clusters around u.
			 * Only one hard rake node is created: left son is new left rake tree and right
			 * son is right proper son of root. */
			
			assert cNv.right.getBu() == v && cNv.getBv() == u;
						
			// create new root
			this.hardExposeTop = createHardRakeClusterNode(v, u, leftRakeTree, cNv.right);
		}
		else {
			
			assert cNv.right.isCompress();
			CompressClusterNode cNu = (CompressClusterNode) cNv.right;
			assert cNu.getCompressedVertex() == u;
			assert cNu.left.getBu() == v && cNu.left.getBv() == u;

			/* Left rake is prepared. So prepare right rake now. */
			// count rakes on u now
			x = 1;	// right proper always exists
			if (cNu.leftFoster  != null) x++;
			if (cNu.rightFoster != null) x++;
			
			// create array for rakes on v remember the right count
			ClusterNode[] rightfosters = new ClusterNode[x];
			this.hardExposeRightCount = x;
			
			// push rakes on v into prepared array
			x = 0;
			if (cNu.rightFoster != null) rightfosters[x++] = cNu.rightFoster;
			this.hardExposeRightTail = x;	// remember a position of the right end of the root path
			rightfosters[x++] = cNu.right;
			if (cNu.leftFoster  != null) rightfosters[x++] = cNu.leftFoster;
			
			// create left rake tree
			ClusterNode rightRakeTree = createRakeFromCompressNode(rightfosters, u,
							this.hardExposeRightTail);
			
			// right proper of original top becomes dirty
			if (!cNu.isNew()) {
				cNu.setDirty();				
			}

			// create hard rake on v
			this.hardExposeTop = createHardRakeClusterNode(v, u, leftRakeTree, cNu.left);
			/* It is not necessary to bindVertices, because this node has the same boundaries
			 * as its future father. */ 
			
			// create new root
			this.hardExposeTop = createHardRakeClusterNode(v, u, this.hardExposeTop, rightRakeTree);
			
		}
		
		
		// check setting of cluster node in boundary vertices
		assert this.hardExposeTop.getBu().getClusterNode() == this.hardExposeTop;
		assert this.hardExposeTop.getBv().getClusterNode() == this.hardExposeTop;

	}


	/**
	 * This method recomputes user defined information of a root cluster node from nonstandard form with
	 * two point clusters and one boundary vertex (can be original left or right boundary or compressed 
	 * vertex) to standard form with two path children and two boundary vertices. The method must be 
	 * called just after {@link toptree.impl.TopTreeImpl#expose(Vertex)} and before any other operation.
	 * <p>
	 * It takes the root cluster node, makes a copy of this node, sets old root dirty and calls method
	 * {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)}} for computing information from 
	 * old root to its sons. During this {@link toptree.impl.TopTreeImpl#exposeOneVertex} must be still set 
	 * to appropriate vertex to correct recomputing. After cleaning it is set to null and a method
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} computes standard user info for
	 * the root cluster node.
	 */
	private void undoExposeOneVertex() {
		// method can be called only after expose on one vertex
		assert this.exposeOneVertex != null;
		
		// take root cluster node
		origTop = ((VertexInfo) this.exposeOneVertex).getClusterNode();
		assert origTop.isTopCluster();
		
		// duplicate root
		ClusterNode newTop = dupCurrent(origTop);
		
		// if top cluster was a base cluster it is necessary to make it back a path cluster
		if (newTop.isBase()) {
			// dupCurrent doesn't copy base clusters - it only sets a reference
			newTop.setState(ClusterNode.NEW);
		}
		
		// delete old top, exposeOneVertex is still not null for correct computing info by destroy/split
		cleanDirtyNodes(origTop);

		// set it to null, so fixating of the root will go in standard way with two path children
		this.exposeOneVertex = null;

		// call create/join on new standard root cluster.
		fixateNewNodes(newTop);
	
	}
	
	
	/**
	 * This method rebuilds the Top Tree so that a given vertex will be  represented on the root path and
	 * so a handler of this vertex will be a root of the Top Tree.
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The method returns of values defined in enumeration type <code>ExposeOneResults</code>. It has 
	 * two values:
	 * <dl>
	 *   <dt>SINLGE</dt>	<dd>The given vertex is a single vertex.</dd>
	 *   <dt>COMPONENT</dt>	<dd>The given vertex is not single and it is a part of some component.</dd>
	 * </dl>
	 * <p>
	 * To expose one vertex is very easy. If the vertex is single then nothing is done. Else it is enough
	 * to call a method {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, ClusterNode)}
	 * on given vertex with no guard. User defined information of a root cluster must be computed
	 * in nonstandard way, because the root cluster has only one boundary (can be original left or right
	 * boundary or compress vertex) and two point clusters. If softExpose don't need to rebuild the tree,
	 * it is necessary to recompute root cluster's information to one boundary form. So the root is 
	 * duplicated and on original root calls method 
	 * {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} to recompute information in sons
	 * of dirty nodes in standard way. Before new user defined information are computed, a reference
	 * {@link toptree.impl.TopTreeImpl#exposeOneVertex} must be set to vertex <code>u</code>, because it
	 * is only way how to recognize in a method 
	 * {@link toptree.impl.TopTreeImpl#typeOfJoin(ClusterInfo, ClusterInfo, ClusterInfo)} that root 
	 * cluster must be not path but point cluster. In addition the class remember by this setting that
	 * expose on one vertex was done and before any other operation the join of the root must be recomputed
	 * to standard root cluster with two path children by method <code>undoExposeOneVertex()</code>. 
	 * In the end new information are computed by 
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)}.
	 * The top cluster then represents two point clusters and one boundary vertex. This situation
	 * corresponds to cases 3 a 4 in figure 1 in 
	 * <a href="../doc-files/p243-alstrup.pdf">article</a> from Alstrup, Holm, Lichtenberg adn Thorup. 
	 * 
	 * @param u	A vertex to expose.
	 * @return	A value of enumeration type <code>ExposeOneResults</code> defined in {@link toptree.TopTree}.
	 */
	public TopTree.ExposeOneResults expose(Vertex<V>  u) {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();

		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		// a trivial case first:
		if (u.getDegree() == 0) {
			// single vertex
			return TopTree.ExposeOneResults.SINGLE;
		}
		
		// rebuild the tree, so u will be represented by a root
		TopIntermediate component = softExpose((VertexInfo) u, (ClusterNode) null);

		if (component.newRoot.isBase()) {
			// if u is in base cluster then the cluster must be changed from path to point
			component.newRoot.setState(ClusterNode.NEW);
		} else if (component.newRoot == component.origRoot) {
			// if u was represented in root before soft-expose then recompute the root to be a point cluster
			origTop = component.origRoot;
			component.newRoot = dupCurrent(component.origRoot);
		}
		
		// call destroy/split on old nodes to recompute information in standard way
		cleanDirtyNodes(component.origRoot);

		// we must remember that this is expose on one vertex for method typeOfJoin
		this.exposeOneVertex = u;
		
		// call create/join on new node - the information of root will be computed to nonstandard form
		fixateNewNodes(component.newRoot);
	
		return TopTree.ExposeOneResults.COMPONENT;

	}
	
	
	/**
	 * This method rebuilds the Top Tree so that given vertices will be 
	 * the end-points on the root path and so a handler of these vertices 
	 * will be a root of the Top Tree. 
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The method returns of values defined in enumeration type <code>ExposeOneResults</code>. It has 
	 * two values:
	 * <dl>
	 *   <dt>LEFT_SINGLE</dt>
	 *     <dd>The left of given vertices is a single vertex and the right is a part of some component.</dd>
	 *   <dt>RIGHT_SINGLE</dt>
	 *     <dd>The left of given vertices is a part of some component and the right is a single vertex.</dd>
	 *   <dt>BOTH_SINGLE</dt>
	 *     <dd>Both of given vertices are single. It is not explored if the vertices are the same vertex.</dd>
	 *   <dt>ONE_VERTEX</dt>
	 *     <dd>Given vertices are one vertex. This vertex is not single.</dd>
	 *   <dt>COMMON_COMPONENT</dt>
	 *     <dd>None of given vertices is single and both vertices are connected in some component.</dd>
	 *   <dt>DIFFERENT_COMPONENTS</dt>
	 *     <dd>None of given vertices is single and vertices are parts of two different components.</dd>
	 * </dl>
	 * <p>
	 * There are four trivial cases:
	 * <ul>
	 *   <li>Both vertices are single so nothing is done.</li>
	 *   <li>Only vertex <code>u</code> is single so a method 
	 *   {@link toptree.impl.TopTreeImpl#expose(Vertex)} is called on vertex <code>v</code>.</li>
	 *   <li>Only vertex <code>v</code> is single. The same method as in previous case is called
	 *   on vertex <code>u</code>.</li>
	 *   <li>Vertices coincide. The same method as in previous two cases is called on this
	 *   one vertex.</li>
	 * </ul>
	 * If none of trivial case occurs, a method 
	 * {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} is called on given vertices
	 * to rebuild the tree so that both vertices are moved to root path.
	 * If it returns two components, it means that vertices are in these different components and nothing
	 * more is done. If they are in one component four cases must be distinguished according to a position
	 * of vertices on root path:
	 * <ul>
	 *   <li>Both are end-points of the root path. Nothing is needed to be done.</li>
	 *   <li>Only the vertex <code>v</code> is end-point. A method 
	 *   {@link toptree.impl.TopTreeImpl#hardExpose(CompressClusterNode, VertexInfo, VertexInfo)}
	 *   rebuilds the tree so that both vertices are end-points of the root path.</li>
	 *   <li>Only the vertex <code>u</code> is end-point. The technique is same as in previous
	 *   case, but vertices have swapped roles.</li>
	 *   <li>Vertex <code>v</code> is represented in the root of Top Tree and <code>u</code> is
	 *   somewhere on the right but not end-point. The method hardExpose is called to finish
	 *   the rebuilding.</li>
	 * </ul>
	 * After <code>hardExpose</code> it must be ensured that <code>u</code> is the left and <code>v</code>
	 * is the right boundary vertex of the root. If it is not then a reverse bit of the root is set 
	 * and the root is normalized by method {@link toptree.impl.ClusterNode#normalize()}. If the root has
	 * hard rake node son then it must be normalized to. These normalizations are needed for correct work
	 * of user defined methods with boundaries of clusters.
	 * 
	 * @param u A vertex to expose.
	 * @param v A vertex to expose.
	 */
	public TopTree.ExposeTwoResults expose(Vertex<V> u, Vertex<V> v) {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();
		
		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		// Some trivial cases first:
		if (u.getDegree() == 0 && v.getDegree() == 0) {
			// single vertices
			return TopTree.ExposeTwoResults.BOTH_SINGLE;
		} else if (u.getDegree() == 0) {
			// u is single vertex, so expose only v
			expose(v);
			return TopTree.ExposeTwoResults.LEFT_SINGLE;
		} else if (v.getDegree() == 0) {
			// v is single vertex, so expose only u
			expose(u);
			return TopTree.ExposeTwoResults.RIGHT_SINGLE;
		}
		if (u == v) {
			// u and v coincide
			expose(u);
			return TopTree.ExposeTwoResults.ONE_VERTEX;
		}

		// move u and v to root path
		TopIntermediate[] components = softExpose((VertexInfo) u, (VertexInfo) v);

		if (components.length > 1) {
			// u and v are in different components
			cleanUp(components);
			return TopTree.ExposeTwoResults.DIFFERENT_COMPONENTS;
		}

		// u and v are soft exposed and appear in the same component
		assert components.length == 1;
		assert components[0].newRoot != null;

		// if u and v are boundaries then softExpose had to prepare them to correct positions
		if (components[0].newRoot.getBu() == u && components[0].newRoot.getBv() == v) {
			// u..v is represented by the root path
			cleanUp(components);
			return TopTree.ExposeTwoResults.COMMON_COMPONENT;
		}

		// Non-trivial case - calling hardExpose is needed:
		assert u.getDegree() >= 2 || v.getDegree() >= 2;
		assert components[0].newRoot.isCompress();

		CompressClusterNode top = (CompressClusterNode) components[0].newRoot;
		// normalize the root, because he will be divided
		top.normalize();
		assert !top.isReversed();
		
		if (top.getCompressedVertex() == u && top.right.getBv() == v) {
			// u is the top and v is the right end-point
			assert top.right.getBu() == u && top.right.getBv() == v;
			hardExpose(top, (VertexInfo) u, (VertexInfo) v);
		}
		else if (top.getCompressedVertex() == v && top.right.getBv() == u) {
			// v is the top and u is the right end-point
			assert top.right.getBu() == v && top.right.getBv() == u;
			hardExpose(top, (VertexInfo) v, (VertexInfo) u);
		}
		else {
			// both u and v appear in the middle of the main path
			// cNv is the root and cNu its right child
			assert top.getCompressedVertex() == v;
			assert u.getDegree() >= 2 & v.getDegree() >= 2;
			assert top.right.isCompress();
			// normalize the right son of root, because he will be divided
			((CompressClusterNode) top.right).normalize();
			assert top.right.left.getBu() == v && top.right.left.getBv() == u;

			hardExpose(top, (VertexInfo) v, (VertexInfo) u);
		}

		cleanUp(components[0].origRoot, this.hardExposeTop);
		
		/* Finally it must be ensured that u is left and v is right boundary vertex after hardExpose.
		 * If the order is swapped, then reverse the the hard rake root and normalize it, so its proper
		 * child will be reversed too. It must be ensured that no hard rake node has set reverse bit.
		 * So call normalize method on both sons - it normalizes only hard rake node. */
		if (this.hardExposeTop.getBu() == v && this.hardExposeTop.getBv() == u) {
			this.hardExposeTop.reverse();
			this.hardExposeTop.normalize();
			this.hardExposeTop.left.normalize();
			this.hardExposeTop.right.normalize();
		}
		
		return TopTree.ExposeTwoResults.COMMON_COMPONENT;
	}


	/**
	 * This method creates a new base cluster node as the edge between two vertices with 
	 * degrees zero and computes the user defined information. None of given vertices can be null.
	 * 
	 * @param u	A vertex that will be the end-point of created edge.
	 * @param v A vertex that will be the end-point of created edge.
	 * @param info An information to be assign to the new edge.
	 */
	private void linkD00(VertexInfo u, VertexInfo v, Object info) {
		assert u != null && v != null;
		assert u.getDegree() == 0 && v.getDegree() == 0;
		// fig a)
		// link two trivial underlying trees
		ClusterNode cuv = createBaseClusterNode(u, v, info);
		// report creation of the new base cluster to an application
		fixateNewNodes(cuv);
	}


	/**
	 * This method creates an edge between two vertices with degrees zero and one and recomputes
	 * user define information in the changed Top Tree. The given component and none
	 * of given vertices can be null.
	 * <p>
	 * It creates new base cluster node as the edge between the vertices and then new compress
	 * cluster node from this base cluster node and the given component. The created compress
	 * cluster is the root of new component.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanUp(ClusterNode, ClusterNode)}
	 * is called to compute actual user define information in changed nodes.
	 * 
	 * @param u A vertex that will be the end-point of created edge.
	 * @param v A vertex that will be the end-point of created edge.
	 * @param vComponent A component with the vertex <code>v</code> as the right end-point.
	 * @param info An information to be assign to the new edge.
	 */
	private void linkD01(VertexInfo u, VertexInfo v, TopIntermediate vComponent, Object info) {
		assert u != null && v != null && vComponent != null;
		assert u.getDegree() == 0 && v.getDegree() == 1;
		// fig b)
		// link isolated vertex to boundary vertex
		ClusterNode cNv = vComponent.newRoot;

		// add new parent and base cluster nodes
		ClusterNode cuv = createBaseClusterNode(u, v, info);
		ClusterNode cnNv = createCompressClusterNode(v, cuv, null, cNv, null);

		cleanUp(vComponent.origRoot, cnNv);

	}


	/**
	 * This method creates an edge between two vertices with degrees zero and at least two
	 * and recomputes user define information in the changed Top Tree. The given component
	 * and none of given vertices can be null.
	 * <p>
	 * The method creates new base cluster node as the edge between the vertices and it adds
	 * this base node to the left foster child of the <code>v</code>'s component as the leftmost
	 * leaf.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanUp(ClusterNode, ClusterNode)}
	 * is called to compute actual user define information in changed nodes.
	 * 
	 * @param u	A vertex that will be the end-point of created edge.
	 * @param v	A vertex that will be the end-point of created edge.
	 * @param vComponent	A component whose root is a cluster node with the vertex <code>v</code>
	 * 						as <code>compressedVertex</code>.
	 * @param info	An information to be assign to the new edge.
	 */
	private void linkD02(VertexInfo u, VertexInfo v, TopIntermediate vComponent, Object info) {
		assert u != null && v != null && vComponent != null;
		assert u.getDegree() == 0 && v.getDegree() >= 2;
		// fig c)
		// link an isolated vertex to an internal vertex

		// duplicate the root node
		origTop = vComponent.newRoot;
		CompressClusterNode cNv = (CompressClusterNode) dupCurrent(origTop);
		cNv.normalize();

		// add a new base cluster for (u,v) and reorganize the tree for (u,v)
		// let it be the new first edge of v
		ClusterNode cuv = createBaseClusterNode(u, v, info);
		if (cNv.leftFoster == null) {
			cNv.leftFoster = cuv;
			cuv.link = cNv;
		} else {
			ClusterNode cnR = createRakeClusterNode(v, cuv, cNv.leftFoster);
			cNv.leftFoster = cnR;
			cnR.link = cNv;
		}

		cleanUp(vComponent.origRoot, cNv);
	}


	/**
	 * This method creates an edge between two vertices with degrees one
	 * and recomputes user defined information in the changed Top Tree. None of given 
	 * vertices and components can be null.
	 * <p>
	 * The method first creates new base cluster node as the edge between the vertices. Then
	 * it creates new compress cluster node with the new base node as left proper and 
	 * the <code>v</code>'s component as right proper child. Next it creates a root of the
	 * component that arise by joining given two components. So it creates other compress
	 * cluster node with the <code>u</code>'s component as left proper and the <code>v</code>'s
	 * component as right proper child.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} is
	 * twice called to compute actual user defined information in sons of old nodes of original
	 * <code>u</code>'s and <code>v</code>'s component and the method
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} to compute user defined
	 * information in new fathers in new component.
	 * 
	 * @param u A vertex that will be the end-point of created edge.
	 * @param v A vertex that will be the end-point of created edge.
	 * @param uComponent A component with the vertex <code>u</code> as the right end-point.
	 * @param vComponent A component with the vertex <code>v</code> as the right end-point.
	 * @param info An information to be assign to the new edge.
	 */
	private void linkD11(VertexInfo u, VertexInfo v, TopIntermediate uComponent,
			TopIntermediate vComponent, Object info) {
		assert u != null && v != null && uComponent != null && vComponent != null;
		assert u.getDegree() == 1 && v.getDegree() == 1;

		// fig d)
		// link two end-points of different clusters

		// create cuv, cnNu and cnNv and connect them
		ClusterNode cNx = uComponent.newRoot;
		ClusterNode cNy = vComponent.newRoot;

		ClusterNode cuv = createBaseClusterNode(u, v, info);
		ClusterNode cnNv = createCompressClusterNode(v, cuv, null, cNy, null);	// right proper of root
		ClusterNode cnNu = createCompressClusterNode(u, cNx, null, cnNv, null);	// the root

		// call split/destroy on modified original clusters in both old components
		cleanDirtyNodes(uComponent.origRoot);
		cleanDirtyNodes(vComponent.origRoot);

		// call create/join on newly created clusters
		fixateNewNodes(cnNu);
	}


	/**
	 * This method creates an edge between two vertices with degrees one and two
	 * and recomputes user defined information in the changed Top Tree. None of given 
	 * vertices and components can be null.
	 * <p>
	 * The method first creates new base cluster node as the edge between the vertices. Then
	 * it creates new compress cluster node with the <code>u</code>'s component as left proper
	 * and the new base node as right proper child. Next it joins this new compress cluster
	 * node to the <code>v</code>'s component. If the root of the <code>v</code>'s component
	 * has null left foster child then the created compress cluster node becomes this left
	 * foster child. If the root of the <code>v</code>'s component has not null left foster 
	 * child then new rake cluster node is created from new compress cluster node on the left and original
	 * left foster child on the right. This rake cluster node becomes the left foster child of
	 * the root of the <code>v</code>'s component. The linking is done now.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} is
	 * twice called to compute actual user defined information in sons of old nodes of original
	 * <code>u</code>'s and <code>v</code>'s component and the method
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} to compute user defined
	 * information in new fathers in created component.
	 * 
	 * @param u A vertex that will be the end-point of created edge.
	 * @param v A vertex that will be the end-point of created edge.
	 * @param uComponent A component with the vertex <code>u</code> as the right end-point.
	 * @param vComponent A component whose root is a cluster node with the vertex <code>v</code> as <code>compressedVertex</code>.
	 * @param info An information to be assign to the new edge.
	 */
	private void linkD12(VertexInfo u, VertexInfo v, TopIntermediate uComponent,
			TopIntermediate vComponent, Object info) {
		assert u != null && v != null && uComponent != null && vComponent != null;
		assert u.getDegree() == 1 && v.getDegree() >= 2;
		assert vComponent.newRoot.isCompress();
		// fig e)
		// link an end-point of one cluster to the middle of the main path of the other cluster

		// get handle of u
		ClusterNode cNx = uComponent.newRoot;

		// duplicate the root node
		origTop = vComponent.newRoot;
		CompressClusterNode cNv = (CompressClusterNode) dupCurrent(origTop);
		cNv.normalize();

		// create cuv, cnNu and cnNv and connect them
		ClusterNode cuv = createBaseClusterNode(u, v, info);
		ClusterNode cnNu = createCompressClusterNode(u, cNx, null, cuv, null);

		// let cnNu be the new first edge of v
		if (cNv.leftFoster == null) {
			cNv.leftFoster = reverseTo(v, cnNu);
			cnNu.link = cNv;
		} else {
			ClusterNode cnR = createRakeClusterNode(v, cnNu, cNv.leftFoster);
			cNv.leftFoster = cnR;
			cnR.link = cNv;
		}
		cNv.recomputeVertices();

		// call split/destroy on modified original clusters in both old components
		cleanDirtyNodes(uComponent.origRoot);
		cleanDirtyNodes(vComponent.origRoot);

		// call create/join on newly created clusters
		fixateNewNodes(cNv);
	}


	/**
	 * This method creates an edge between two vertices with degrees two
	 * and recomputes user defined information in the changed Top Tree. None of given 
	 * vertices and components can be null.
	 * <p>
	 * The method first creates new base cluster node as the edge between the vertices. Then
	 * it joins the new base cluster node to <code>v</code>'s component: If the root of 
	 * the <code>v</code>'s component has null left foster child then its left proper child 
	 * becomes the left foster and the created base cluster node becomes the left proper child.
	 * Else the left foster child is replaced by new rake cluster node with original left
	 * foster child on the left and original left proper child on the right. The new base 
	 * cluster node becomes the left proper child of the root.<br />
	 * Now the <code>v</code>'s component must be joined to <code>u</code>'s component.
	 * If the root of the <code>u</code>'s component has null right foster child then original
	 * right proper child becomes the right foster child and the <code>v</code>'s component
	 * becomes the right proper child. Else the right foster child is replaced by new rake cluster
	 * node with the original right foster child on the left and the right proper child of the
	 * root on the right and the <code>v</code>'s component becomes the right proper child.
	 * The linking is done now.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} is
	 * twice called to compute actual user defined information in sons of old nodes of original
	 * <code>u</code>'s and <code>v</code>'s component and the method
	 * {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)} to compute user defined
	 * information in new fathers in created component.
	 * 
	 * @param u A vertex that will be the end-point of created edge.
	 * @param v A vertex that will be the end-point of created edge.
	 * @param uComponent A component whose root is a cluster node with the vertex <code>u</code> as <code>compressedVertex</code>.
	 * @param vComponent A component whose root is a cluster node with the vertex <code>v</code> as <code>compressedVertex</code>.
	 * @param info An information to be assign to the new edge.
	 */
	private void linkD22(VertexInfo u, VertexInfo v, TopIntermediate uComponent,
			TopIntermediate vComponent, Object info) {
		assert u != null && v != null && uComponent != null && vComponent != null;
		assert u.getDegree() >= 2 && v.getDegree() >= 2;
		assert uComponent.newRoot.isCompress();
		assert vComponent.newRoot.isCompress();
		// fig f)
		// link main paths of clusters representing u and v

		// duplicate cNu
		origTop = uComponent.newRoot;
		CompressClusterNode cNu = (CompressClusterNode) dupCurrent(origTop);
		cNu.normalize();

		// duplicate cNv
		origTop = vComponent.newRoot;
		CompressClusterNode cNv = (CompressClusterNode) dupCurrent(origTop);
		cNv.reverse();	// we need to have the (b,v) on the left
		cNv.normalize();

		// create cuv
		ClusterNode cuv = createBaseClusterNode(u, v, info);

		// splice cNv
		ClusterNode cz = cNv.left;
		if (cNv.leftFoster == null) {
			cNv.leftFoster = reverseTo(v, cz);
			cz.link = cNv;
			cz.parent = null;
		} else {
			ClusterNode cnR = createRakeClusterNode(v, cNv.leftFoster, cz);
			cNv.leftFoster = cnR;
			cnR.link = cNv;

		}
		if (cz.isCompress())
			((CompressClusterNode) cz).recomputeVertices();
		else
			cz.bindVertices();
		cNv.left = cuv;
		cuv.parent = cNv;
		cNv.recomputeVertices();

		// splice cNu
		ClusterNode cy = cNu.right;
		if (cNu.rightFoster == null) {
			cNu.rightFoster = reverseTo(u, cy);
			cy.link = cNu;
			cy.parent = null;
		} else {
			ClusterNode cnR = createRakeClusterNode(u, cNu.rightFoster, cy);
			cNu.rightFoster = cnR;
			cnR.link = cNu;
		}
		if (cy.isCompress())
			((CompressClusterNode) cy).recomputeVertices();
		else
			cy.bindVertices();
		cNu.right = cNv;
		cNv.parent = cNu;
		cNu.recomputeVertices();

		// call split/destroy on modified original clusters in both old components
		cleanDirtyNodes(uComponent.origRoot);
		cleanDirtyNodes(vComponent.origRoot);

		// call create/join on newly created clusters
		fixateNewNodes(cNu);
	}


	/**
	 * This method creates an edge with given user defined information between given two vertices 
	 * where position of the edge is not important. It creates the edge somewhere around the vertices. 
	 * If any problem occurs during linking an exception is thrown. None of given vertices cannot
	 * be null.
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The method excepts that the degree of the vertex <code>u</code> is smaller than the degree
	 * of the vertex <code>v</code>. If this does not hold then the method swaps names of vertices.
	 * <p>
	 * The linking distinguishes six different cases - it depends on degrees of vertices:
	 * <ol type="a">
	 *   <li>Degree(u) = Degree(v) = 0:<br />
	 *   Both vertices are single. It is easy. 
	 *   Just call {@link toptree.impl.TopTreeImpl#linkD00(VertexInfo, VertexInfo, Object)}.</li>
	 *   <li>Degree(u) = 0, Degree(v) = 1:<br />
	 *   The vertex <code>v</code> must be represented in the root of its component. This is reached
	 *   by calling {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, ClusterNode)} on 
	 *   <code>v</code> with no guard. At the end new edge is created between given vertices by calling
	 *   {@link toptree.impl.TopTreeImpl#linkD01(VertexInfo, VertexInfo, TopIntermediate, Object)}.</li>
	 *   <li>Degree(u) = 0, Degree(v) >= 2:<br />
	 *   The technique is similar like in the previous case. First soft-expose the vertex 
	 *   <code>v</code> and then new edge is created by method
	 *   {@link toptree.impl.TopTreeImpl#linkD02(VertexInfo, VertexInfo, TopIntermediate, Object)}.</li>
	 *   <li>Degree(u) = 1, Degree(v) = 1:<br />
	 *   Both vertices must be represented in root cluster nodes of their components. This is achieved
	 *   by calling {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} on these two
	 *   vertices. If both are in one component then an exception is thrown, because there exists already
	 *   a way between them. Else the edge is created by method
	 *   {@link toptree.impl.TopTreeImpl#linkD11(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   called on both vertices and their components.</li>
	 *   <li>Degree(u) = 1, Degree(v) >= 2:<br />
	 *   The technique is the same as in previous case. Only difference is that for linking is used method
	 *   {@link toptree.impl.TopTreeImpl#linkD12(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}.</li>
	 *   <li>Degree(u) >= 2, Degree(v) >= 2:<br />
	 *   The technique is the same as in previous two cases. Only difference is that for linking is used method
	 *   {@link toptree.impl.TopTreeImpl#linkD22(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}.</li>
	 * </ol>
	 * Recomputing of user defined information after rebuilding ensure linking methods.
	 * <p>
	 * At the end there are increased degrees of given two vertices and the number of edges in
	 * the Top Tree.
	 * 
	 * @param uf A vertex that will be the end-point of created edge.
	 * @param vf A vertex that will be the end-point of created edge.
	 * @param info An information to be assign to the new edge.
	 * @throws TopTreeException If any problem occurs while creating the new edge.
	 */
	public void link(Vertex<V> uf, Vertex<V> vf, C info) throws TopTreeException {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();
		
		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		assert uf != null && vf != null;

		if (uf == vf)
			throw new TopTreeException("Self loops (u,u) are not allowed.");

		VertexInfo u = (VertexInfo) uf;
		VertexInfo v = (VertexInfo) vf;

		if (v.getDegree() < u.getDegree()) {
			// let u be the vertex with the smaller degree
			VertexInfo tmp = v;
			v = u;
			u = tmp;
		}

		if (u.getDegree() == 0 && v.getDegree() == 0) {
			linkD00(u, v, info);
		} else if (u.getDegree() == 0 && v.getDegree() == 1) {
			// bring v to the root of its top tree component
			TopIntermediate vComponent = softExpose(v, (ClusterNode) null);
			linkD01(u, v, vComponent, info);
		} else if (u.getDegree() == 0 && v.getDegree() >= 2) {
			// bring v to the root of its top tree component
			TopIntermediate vComponent = softExpose(v, (ClusterNode) null);
			linkD02(u, v, vComponent, info);
		} else if (u.getDegree() == 1 && v.getDegree() == 1) {
			// link two end-points of different clusters
			TopIntermediate[] components = softExpose(u, v);
			if (components.length < 2) {
				// u and v are already in the same component, we are done
				cleanUp(components);
				throw new TopTreeException("There is already a path connecting given vertices.");
			}
			assert components.length == 2;
			linkD11(u, v, components[0], components[1], info);
		} else if (u.getDegree() == 1 && v.getDegree() >= 2) {
			// link an end-point of one cluster to the middle of the main path of the other cluster
			TopIntermediate[] components = softExpose(u, v);
			if (components.length < 2) {
				// u and v are already in the same component, we are done
				cleanUp(components);
				throw new TopTreeException("There is already a path connecting given vertices.");
			}
			assert components.length == 2;
			linkD12(u, v, components[0], components[1], info);
		} else {
			assert u.getDegree() >= 2 && v.getDegree() >= 2;
			// link main paths of clusters representing u and v
			TopIntermediate[] components = softExpose(u, v);
			if (components.length < 2) {
				// u and v are already in the same component, we are done
				cleanUp(components);
				throw new TopTreeException("There is already a path connecting given vertices.");
			}
			assert components.length == 2;
			linkD22(u, v, components[0], components[1], info);
		}

		u.incDegree();
		v.incDegree();
		numOfEdges++;
	}


	/**
	 * This method creates an edge with given user defined information between given two vertices 
	 * where position of the edge around second vertex is specified by third parameter.
	 * Position is specified by an edge <code>(b,v)</code> in cyclic order of edges around
	 * vertex <code>vf</code>, so new edge will be successor of <code>(b,v)</code>.
	 * If any problem occurs during linking an exception is thrown. Node of given vertices
	 * can be null.
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The vertices must be represented in roots of their components so the method
	 * {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} is called
	 * on vertices <code>u</code> and <code>v</code>. Also it must be ensured that the edge
	 * will be created as the successor of the edge <code>(b,v)</code>. It is done by calling
	 * {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} on vertices 
	 * <code>b</code> and <code>v</code>. Now everything is prepared for linking.
	 * <p>
	 * The linking distinguishes six different cases - it depends on degrees of vertices:
	 * <ol type="a">
	 *   <li>Degree(u) = 0, Degree(v) = 1:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD01(VertexInfo, VertexInfo, TopIntermediate, Object)}
	 *   on both vertices and the prepared component of the vertex <code>v</code>.</li>
	 *   <li>Degree(u) = 0, Degree(v) >= 2:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD02(VertexInfo, VertexInfo, TopIntermediate, Object)}
	 *   on both vertices and the prepared component of the vertex <code>v</code>.</li>
	 *   <li>Degree(u) = 1, Degree(v) = 1:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD11(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 *   <li>Degree(u) = 1, Degree(v) >= 2:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD12(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 *   <li>Degree(u) >= 2, Degree(v) = 1:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD12(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices. It is the same as in the previous case, but
	 *   the role of the vertices and components are swapped.</li>
	 *   <li>Degree(u) >= 2, Degree(v) >= 2:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD22(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 * </ol>
	 * Recomputing of user defined information after rebuilding ensure linking methods.
	 * <p>
	 * At the end there are increased degrees of given two vertices and the number of edges in
	 * the Top Tree.
	 *  
	 * @param uf A vertex that will be the end-point of created edge.
	 * @param vf A vertex that will be the end-point of created edge.
	 * @param bf A vertex that signs edge <code>(b,v)</code> which will be a predecessor
	 * 			 of new edge in the sense of cyclic order around vertex <code>vf</code>.
	 * @param info An information to be assign to the new edge.
	 * @throws TopTreeException If any problem occurs while creating the new edge.
	 */
	public void link(Vertex<V> uf, Vertex<V> vf, Vertex<V> bf, C info) throws TopTreeException {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();
		
		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		assert uf != null && vf != null && bf != null;

		if (uf == vf)
			throw new TopTreeException("Self loops (u,u) are not allowed.");
		if (vf == bf)	// (b,v) must be an edge
			throw new TopTreeException("Invalid (b,v) vertex pair.");

		VertexInfo u = (VertexInfo) uf;
		VertexInfo v = (VertexInfo) vf;
		VertexInfo b = (VertexInfo) bf;

		if (v.getDegree() == 0) {
			// Invalid call - there must be at least one edge (b,v)
			throw new TopTreeException("There is no path connecting (b,v) pair.");
		}

		TopIntermediate[] components = null;
		if (u.getDegree() > 0) {
			components = softExpose(u, v);
			if (components.length < 2) {
				// u and v are already in the same component, we are done
				cleanUp(components);
				throw new TopTreeException("There is already a path connecting given vertices.");
			}
		}

		TopIntermediate[] bvComponents = softExpose(b, v);
		if (bvComponents.length != 1) {
			// b and v are in different components
			// there might be 2 or 3 different components (u) (b) (v) or (u,b) (v)
			if (components != null) {
				cleanUp(components[0].origRoot, components[0].newRoot);
				bvComponents[1].origRoot = components[1].origRoot;
			}
			cleanUp(bvComponents);
			throw new TopTreeException("There is no path connecting (b,v).");
		}
		if (components != null)
			// Original old nodes of v's component are held by this node:
			bvComponents[0].origRoot = components[1].origRoot;

		// we have at most two intermediate components (u) and (b,v)
		// with proper old and new roots

		if (u.getDegree() == 0 && v.getDegree() == 1) {
			linkD01(u, v, bvComponents[0], info);
		} else if (u.getDegree() == 0 && v.getDegree() >= 2) {
			linkD02(u, v, bvComponents[0], info);
		} else if (u.getDegree() == 1 && v.getDegree() == 1) {
			// link two end-points of different clusters
			assert b.getClusterNode() == v.getClusterNode();
			linkD11(u, v, components[0], bvComponents[0], info);
		} else if (u.getDegree() == 1 && v.getDegree() >= 2) {
			// link an end-point of one cluster to the middle of the main path of the other cluster
			linkD12(u, v, components[0], bvComponents[0], info);
		} else if (v.getDegree() == 1 && u.getDegree() >= 2) {
			// link an end-point of one cluster to the middle of the main path of the other cluster
			linkD12(v, u, bvComponents[0], components[0], info);
		} else {
			assert u.getDegree() >= 2 && v.getDegree() >= 2;
			linkD22(u, v, components[0], bvComponents[0], info);
		}

		u.incDegree();
		v.incDegree();
		numOfEdges++;
	}


	/**
	 * This method creates an edge with given user defined information between given two vertices 
	 * where position of the edge around given vertices is specified by third and forth parameter.
	 * Position is specified by edges <code>(a,u)</code> and <code>(b,v)</code> in cyclic order 
	 * of edges around vertices <code>uf</code> and <code>vf</code>, so new edge will be successor
	 * of <code>(a,u)</code> in vertex <code>uf</code> and successor of <code>(b,v)</code> 
	 * in vertex <code>vf</code>. If any problem occurs during linking an exception is thrown.
	 * Node of given vertices can be null.
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The method excepts that the degree of the vertex <code>u</code> is smaller than the degree
	 * of the vertex <code>v</code>. If this does not hold then the method swaps names of vertices.
	 * <p>
	 * The method first checks if vertices <code>u</code> and <code>v</code> are in different
	 * components by calling the method
	 * {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} on these vertices
	 * and checking number of returned components. Next it is ensured that the edge
	 * will be created as the successor of the edge <code>(a,u)</code> around the vertex
	 * <code>u</code> and of the edge <code>(b,v)</code> around <code>v</code>. This is
	 * done by calling twice same method on appropriate vertices.
	 * Now everything is prepared for linking.
	 * <p>
	 * The linking distinguishes three different cases - it depends on degrees of vertices:
	 * <ol type="a">
	 *   <li>Degree(u) = 1, Degree(v) = 1:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD11(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 *   <li>Degree(u) = 1, Degree(v) >= 2:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD12(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 *   <li>Degree(u) >= 2, Degree(v) >= 2:<br />
	 *   The edge is created by calling the method
	 *   {@link toptree.impl.TopTreeImpl#linkD22(VertexInfo, VertexInfo, TopIntermediate, TopIntermediate, Object)}
	 *   on both vertices and prepared components of both vertices.</li>
	 * </ol>
	 * Recomputing of user defined information after rebuilding ensure linking methods.
	 * <p>
	 * At the end there are increased degrees of given two vertices and the number of edges in
	 * the Top Tree.

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
	public void link(Vertex<V> uf, Vertex<V> af, Vertex<V> vf, Vertex<V> bf, C info) throws TopTreeException {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();
		
		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		assert uf != null && af != null && vf != null && bf != null;

		if (uf == vf)
			throw new TopTreeException("Self loops (u,u) are not allowed.");
		if (uf == af)
			throw new TopTreeException("Invalid (a,u) vertex pair.");
		if (vf == bf)
			throw new TopTreeException("Invalid (b,v) vertex pair.");

		VertexInfo u = (VertexInfo) uf;
		VertexInfo a = (VertexInfo) af;
		VertexInfo v = (VertexInfo) vf;
		VertexInfo b = (VertexInfo) bf;

		if (v.getDegree() < u.getDegree()) {
			// let u be the vertex with the smaller degree
			VertexInfo tmp = v;
			v = u;
			u = tmp;
			tmp = a;
			a = b;
			b = tmp;
		}

		if (u.getDegree() == 0) {
			// Invalid call
			throw new TopTreeException("There is no path connecting (a,u) or (b,v) pair.");
		}

		TopIntermediate[] components = softExpose(u, v);
		if (components.length < 2) {
			// u and v are already in the same component, we are done
			cleanUp(components);
			throw new TopTreeException("There is already a path connecting given vertices.");
		}

		TopIntermediate[] auComponents = softExpose(a, u);
		if (auComponents.length != 1) {
			// a and u are in different components
			// there might be 2 or 3 different components (u) (a) (v) or (u) (a,v)
			auComponents[1].origRoot = components[0].origRoot;
			cleanUp(auComponents);
			cleanUp(components[1].origRoot, components[1].newRoot);
			if (u == uf)
				throw new TopTreeException("There is no path connecting (a,u).");
			else
				throw new TopTreeException("There is no path connecting (b,v).");
		}
		// Original old nodes of u's component are held by this node:
		auComponents[0].origRoot = components[0].origRoot;

		TopIntermediate[] bvComponents = softExpose(b, v);
		if (bvComponents.length != 1) {
			// b and v are in different components
			// there might be 2 or 3 different components (u) (b) (v) or (u,b) (v)
			bvComponents[1].origRoot = components[1].origRoot;
			cleanUp(bvComponents);
			cleanUp(components[0].origRoot, components[0].newRoot);
			if (u == uf)
				throw new TopTreeException("There is no path connecting (b,v).");
			else
				throw new TopTreeException("There is no path connecting (a,u).");

		}
		// Original old nodes of v's component are held by this node:
		bvComponents[0].origRoot = components[1].origRoot;

		// we have only two intermediate components (a,u) and (b,v)
		// with proper old and new roots

		if (u.getDegree() == 1 && v.getDegree() == 1) {
			// link two end-points of different clusters
			assert a.getClusterNode() == u.getClusterNode()
					&& b.getClusterNode() == v.getClusterNode();
			linkD11(u, v, auComponents[0], bvComponents[0], info);
		} else if (u.getDegree() == 1 && v.getDegree() >= 2) {
			// link an end-point of one cluster to the middle of the main path of the other cluster
			assert a.getClusterNode() == u.getClusterNode();
			linkD12(u, v, auComponents[0], bvComponents[0], info);
		} else {
			assert u.getDegree() >= 2 && v.getDegree() >= 2;
			linkD22(u, v, auComponents[0], bvComponents[0], info);
		}

		u.incDegree();
		v.incDegree();
		numOfEdges++;
	}


	/**
	 * This method marks all rake cluster nodes on a way from the given cluster node down to
	 * the rightmost non-rake cluster node and returns this base or compress cluster node.
	 * The given cluster node <code>c</code> cannot be null.
	 * <p>
	 * There is nothing difficult in the method. It is just jumping down through right rake
	 * children and setting them type flag to <code>OBSOLETE</code> until it finds first 
	 * non-rake node. This is base or compress cluster node that is returned.
	 * 
	 * @param c	A cluster node where will be marked the way down through rake cluster
	 * 			nodes to the rightmost non-rake cluster node.
	 * @return	The rightmost non-rake cluster node.
	 */
	private ClusterNode markRightmostPath(ClusterNode c) {
		assert c != null;

		while (c.isRake()) {
			// jump down through rakes
			c.setObsolete();
			c = c.right;
		}
		// the first right non-rake child
		return c;
	}


	/**
	 * This method marks all rake cluster nodes on a way from the given cluster node down to
	 * the leftmost non-rake cluster node and returns this base or compress cluster node.
	 * The given cluster node <code>c</code> cannot be null.
	 * <p>
	 * There is nothing difficult in the method. It is just jumping down through left rake
	 * children and setting them type flag to <code>OBSOLETE</code> until it finds first
	 * non-rake node. This is base or compress cluster node that is returned.
	 * 
	 * @param c	A cluster node where will be marked the way down through rake cluster
	 * 			nodes to the leftmost non-rake cluster node.
	 * @return	The leftmost non-rake cluster node.
	 */
	private ClusterNode markLeftmostPath(ClusterNode c) {
		assert c != null;

		while (c.isRake()) {
			// jump down through rakes
			c.setObsolete();
			c = c.left;
		}
		// the first left non-rake child
		return c;
	}


	/**
	 * This method replaces a right proper child of the root of the given component by
	 * a non-rake cluster node which is the predecessor or successor of the replaced cluster
	 * node in the sense of the circular order around a compress vertex of the root and 
	 * returns the root of the modified component. The given compress cluster node 
	 * cannot be null.
	 * <p>
	 * The method distinguishes three cases - according to foster children of the root
	 * of the given component <code>cNv</code>:
	 * <ol>
	 *   <li>Both are null:<br />
	 *       The easiest situation. The root is removed, the right end-point becomes 
	 *       a single vertex and the left proper child becomes the root of the returned
	 *       component.</li>
	 *   <li>Right is not null:<br />
	 *       First the method finds the rightmost non-rake cluster node in the subtree of
	 *       the right foster child and marks the way to it by calling the method
	 *       {@link toptree.impl.TopTreeImpl#markRightmostPath(ClusterNode)}.
	 *       Then this rightmost non-rake cluster node becomes the right proper child
	 *       of the root and the subtree of the right foster child is rebuilt to a correct
	 *       state.</li>
	 *   <li>Right is null and left is not null:<br />
	 *       This is similar case as the previous. Instead of the rightmost non-rake node
	 *       from the right foster child it uses the leftmost from the left foster child which
	 *       is found by the method {@link toptree.impl.TopTreeImpl#markLeftmostPath(ClusterNode)}.
	 *       The technique is the same as in the previous case.</li>
	 * </ol>
	 * 
	 * 
	 * @param cNv	A cluster node that is the root of the component where will be 
	 * 				a right proper child replaced. 
	 * @return A compress cluster node which is the root of the component with replaced right child.
	 */
	private ClusterNode replaceRightChild(CompressClusterNode cNv) {
		
		cNv.setObsolete();

		if (cNv.leftFoster == null && cNv.rightFoster == null) {
			// it suffices to remove cNv and vu, left child is the new root
			ClusterNode cNx = cNv.left;
			cNx.parent = cNv.parent;
			cNx.link = cNv.link;
			if (cNx.isCompress())
				((CompressClusterNode) cNx).recomputeVertices();
			else
				cNx.bindVertices();

			return cNx;
		}

		ClusterNode cNw, cR = null;
		if (cNv.rightFoster != null) {
			// replace the right child by the rightmost path of the right foster
			cNw = markRightmostPath(cNv.rightFoster);
			
			if (cNw.link != cNv) {
				assert cNw.link.isRake();
				origTop = cNw.link;
				cR = splay(cNw.link, null);
				cR.setObsolete();
				cNv = (CompressClusterNode) dupLinkedParent(cR);
			} else {
				origTop = cNw;
				cNv = (CompressClusterNode) dupLinkedParent(cNw);
			}

			// cNw is at most one rake far from cNv
			if (cR != null) {
				// cR is right foster child of cNv
				// cNw is right child of cR
				ClusterNode nrf = cR.left;
				nrf.link = cNv;
				nrf.parent = null;
				cNv.rightFoster = nrf;
				cR.parent = null;
				cR.link = null;
			} else {
				// cNw is right foster child of cNv
				cNv.rightFoster = null;
			}
		} else {
			// replace the right child by the leftmost path of the left foster
			cNw = markLeftmostPath(cNv.leftFoster);

			if (cNw.link != cNv) {
				assert cNw.link.isRake();
				origTop = cNw.link;
				cR = splay(cNw.link, null);
				cR.setObsolete();
				cNv = (CompressClusterNode) dupLinkedParent(cR);
			} else {
				origTop = cNw;
				cNv = (CompressClusterNode) dupLinkedParent(cNw);
			}

			// cNw is at most one rake far from cNv
			if (cR != null) {
				// cR is left foster child of cNv
				// cNw is left child of cR
				ClusterNode nlf = cR.right;
				nlf.link = cNv;
				nlf.parent = null;
				cNv.leftFoster = nlf;
				cR.parent = null;
				cR.link = null;
			} else {
				// cNw is left foster child of cNv
				cNv.leftFoster = null;
			}
		}

		cNw.reverse();	// bv must be on the left for the right proper
		assert cNw.getBu() == cNv.getCompressedVertex();
		cNv.right = cNw;
		cNw.parent = cNv;
		cNw.link = null;
		cNv.getBv().setClusterNode(null);
		cNv.recomputeVertices();

		return cNv;
	}


	/**
	 * This method destroys the edge between vertices <code>u</code> and <code>v</code> which
	 * is only edge in a given component and computes user defined information for single 
	 * vertices.
	 * <p>
	 * The user define information are computed by the method 
	 * {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} called on the root of
	 * the given component.
	 * 
	 * @param component	A component which is a single edge (base cluster node) with vertices
	 * 					<code>u</code> and <code>v</code> as left and right end-point.
	 */
	private void cutD11(TopIntermediate component) {
		assert component.origRoot == component.newRoot;
		ClusterNode c = component.origRoot;

		c.getBu().setClusterNode(null);
		c.getBv().setClusterNode(null);
		c.setObsolete();
		cleanDirtyNodes(c);
	}


	/**
	 * This method destroys an edge between a vertex <code>v</code> that is compressed in
	 * the root of the given component and a vertex <code>u</code> that is <code>v</code>'s
	 * neighbor and end-point of the root path of the given component. The component cannot
	 * be null.
	 * <p>
	 * The right proper child of the root is a base cluster node which represents the edge (u,v).
	 * The method replaces this base cluster node by a cluster node which is returned by 
	 * the method {@link toptree.impl.TopTreeImpl#replaceRightChild(CompressClusterNode)}
	 * called on the given component. Besides the method create a single vertex <code>u</code>
	 * by deleting the edge (u,v).
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} is
	 * called to compute actual user defined information in sons of old nodes of the original
	 * component and the method {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)}
	 * to compute user defined information in new fathers in the changed component.
 	 * 
	 * @param component	A component whose root is compressed vertex <code>v</code> and
	 * 					the end-point <code>u</code> is its right neighbor.  
	 */
	private void cutD12(TopIntermediate component) {
		assert component.newRoot.isCompress();

		ClusterNode cuv = component.newRoot.right;
		cuv.setObsolete();	// base cluster (u,v) set to obsolete 
		VertexInfo isolatedVertex = cuv.getBv();	// vertex u
		ClusterNode cNv = replaceRightChild((CompressClusterNode) component.newRoot);
		isolatedVertex.setClusterNode(null);
		cleanDirtyNodes(component.origRoot);
		fixateNewNodes(cNv);
	}

	
	/**
	 * This method destroys the edge (u,v) that is represented by a base cluster node which 
	 * is a left proper child of a right proper child of a root of the given component. The root
	 * is compressed vertex <code>v</code> and its right proper child is compressed vertex
	 * <code>u</code>. The given component cannot be null.
	 * <p>
	 * The method first takes the right proper child of the root (<code>u</code>'s handler)
	 * and reverse it so that after calling the method
	 * {@link toptree.impl.TopTreeImpl#replaceRightChild(CompressClusterNode)} on this right
	 * proper child the base cluster representing the edge (u,v) is destroyed.
	 * Next the same method is called on the root to replace the right proper child. When it
	 * is finished, there are two components - one that contains the vertex <code>u</code>
	 * and second that contains the vertex <code>v</code>. Both vertices are 
	 * <code>compressedVertex</code> of the root in appropriate component.
	 * <p>
	 * At the end the method {@link toptree.impl.TopTreeImpl#cleanDirtyNodes(ClusterNode)} is
	 * called to compute actual user defined information in sons of old nodes of the original
	 * component and the method {@link toptree.impl.TopTreeImpl#fixateNewNodes(ClusterNode)}
	 * is called twice to compute user defined information in new fathers in 
	 * <code>u</code>'s and <code>v</code>'s components.
	 *  
	 * @param component	A component whose root is compressed vertex <code>v</code> and the
	 * 					vertex <code>u</code> is compressed in the root's right proper child.
	 */
	private void cutD22(TopIntermediate component) {
		assert component.newRoot.isCompress();
		assert component.newRoot.right.isCompress();
		assert component.newRoot.right.left.isBase();
		
		CompressClusterNode cNv = (CompressClusterNode) component.newRoot;
		CompressClusterNode cNu = (CompressClusterNode) cNv.right;
		ClusterNode cuv = cNu.left;
		cuv.setObsolete();
		
		// temporarily reverse cNu to make uv the right child
		cNu.reverse();
		cNu.normalize();
		ClusterNode cnNu = replaceRightChild(cNu);

		// reverse the subtree back to the original ordering
		cnNu.reverse();
		
		cNv.setObsolete();
		cNv = (CompressClusterNode) dupParent(cnNu, null);
		ClusterNode cnNv = replaceRightChild(cNv);
		cnNu.parent = null;
		cnNu.link = null;
		if (cnNu.isCompress())
			((CompressClusterNode) cnNu).recomputeVertices();
		else
			cnNu.bindVertices();
		
		cleanDirtyNodes(component.origRoot);
		fixateNewNodes(cnNu);
		fixateNewNodes(cnNv);
	}
	

	/**
	 * This method removes an edge between two given vertices.
	 * It throws an exception if there any problem occurs while deleting the edge.
	 * None of given vertices can be null.
	 * <p>
	 * Before anything is done, it must be checked if the Top Tree is hard exposed or if it is after expose
	 * on one vertex. If the tree is hard exposed, then the tree must be rebuilt to original form by calling
	 * the method {@link toptree.impl.TopTreeImpl#undoHardExpose()}. If the tree is after expose on one 
	 * vertex, then user defined information of root cluster must be recomputed to standard form with
	 * two path children by method {@link toptree.impl.TopTreeImpl#undoExposeOneVertex()} 
	 * <p>
	 * The method excepts that the degree of the vertex <code>u</code> is smaller than the degree
	 * of the vertex <code>v</code>. If this does not hold then the method swaps names of vertices.
	 * <p>
	 * First it prepares the vertices to be represented in the root of the Top Tree by calling 
	 * the method {@link toptree.impl.TopTreeImpl#softExpose(VertexInfo, VertexInfo)} on vertices
	 * <code>u</code> and <code>v</code>. 
	 * <p>
	 * The cutting distinguishes three different cases - it depends on degrees of vertices:
	 * <ol type="a">
	 *   <li>Degree(u) = 1, Degree(v) = 1:<br />
	 *   It means that there is only one base cluster node with vertices as end-points. The edge
	 *   is destroyed by calling the method {@link toptree.impl.TopTreeImpl#cutD11(TopIntermediate)}
	 *   on the component prepared by soft-expose.</li>
	 *   <li>Degree(u) = 1, Degree(v) >= 2:<br />
	 *   It means that <code>v</code> is represented in the root but isn't an end-point and
	 *   its neighbor <code>u</code> is the right end-point of the root path. The edge is 
	 *   destroyed by calling the method {@link toptree.impl.TopTreeImpl#cutD12(TopIntermediate)}
	 *   on the component prepared by soft-expose.</li>
	 *   <li>Degree(u) >= 2, Degree(v) >= 2:<br />
	 *   It means that both vertices are on the root path but not end-points. The vertex
	 *   <code>v</code> is represented by the root of the component and the vertex <code>u</code>
	 *   is represented by <code>v</code>'s right proper child (the edge is represented by
	 *   a base cluster node which is the left proper child of the root's right proper child).
	 *   The edge is destroyed by calling the method 
	 *   {@link toptree.impl.TopTreeImpl#cutD22(TopIntermediate)}
	 *   on the component prepared by soft-expose.</li>
	 * </ol>
	 * Recomputing of user defined information after rebuilding ensure cutting methods.
	 * <p>
	 * At the end there are decreased degrees of given two vertices and the number of edges in
	 * the Top Tree.
	 * 
	 * @param uf A vertex which is one end-point of removing edge.
	 * @param vf A vertex which is second end-point of removing edge.
	 * @throws TopTreeException If any problem occurs while deleting the edge.
	 */
	public void cut(Vertex<V> uf, Vertex<V> vf) throws TopTreeException {
		
		// if the Top Tree is hard exposed, it must be rebuilt to original form before any operation
		if (this.hardExposeTop != null) undoHardExpose();
		
		// if the Top Tree was exposed on one vertex, recompute information of root to standard form
		if (this.exposeOneVertex != null) undoExposeOneVertex();

		assert uf != null && vf != null;

		if (uf == vf)
			throw new TopTreeException("Invalid edge (u,u).");

		VertexInfo u = (VertexInfo) uf;
		VertexInfo v = (VertexInfo) vf;

		if (v.getDegree() < u.getDegree()) {
			// let u be the vertex with the smaller degree
			VertexInfo tmp = v;
			v = u;
			u = tmp;
		}

		if (u.getDegree() == 0)
			throw new TopTreeException("There is no such edge (u,v).");

		TopIntermediate[] components = softExpose(u, v);
		if (components.length >= 2) {
			// u and v are already in different components, we are done
			cleanUp(components);
			throw new TopTreeException("There is no edge connecting u and v.");
		}

		if (u.getDegree() == 1 && v.getDegree() == 1) {
			// u and v are both end-points of the main path
			assert components[0].newRoot.getBu() == u && components[0].newRoot.getBv() == v;
			if (!components[0].newRoot.isBase()) {
				cleanUp(components);
				throw new TopTreeException("There is no edge connecting u and v.");
			}
			// uv is represented by the (top) base cluster
			cutD11(components[0]);
		} else if (u.getDegree() == 1 && v.getDegree() >= 2) {
			// u is the right end-point of the main path, cNv is the root of the top tree
			assert components[0].newRoot.isCompress();
			CompressClusterNode cNv = ((CompressClusterNode) components[0].newRoot);
			cNv.normalize();
			assert cNv.getCompressedVertex() == v && components[0].newRoot.getBv() == u;

			if (!cNv.right.isBase()) {
				cleanUp(components);
				throw new TopTreeException("There is no edge connecting u and v.");
			}
			// uv is represented by the right child of the tree root
			cutD12(components[0]);
		} else {
			// cNv is the root of the top tree
			// cNu is its right child
			assert components[0].newRoot.isCompress();
			CompressClusterNode cNv = ((CompressClusterNode) components[0].newRoot);
			cNv.normalize();
			assert cNv.getCompressedVertex() == v;
			assert cNv.right.isCompress();
			CompressClusterNode cNu = ((CompressClusterNode) cNv.right);
			cNu.normalize();

			// uv must be the left proper of the right proper of the root
			if (!cNu.left.isBase()) {
				cleanUp(components);
				throw new TopTreeException("There is no edge connecting u and v.");
			}
			// uv is represented by cNu's left child
			cutD22(components[0]);
		}

		u.decDegree();
		v.decDegree();
		numOfEdges--;
	}
	
	
	/**
	 * This method finds out a type of a given cluster <code>c_i</code> and returns this enumeration type.
	 * <p>
	 * The method should be called only on a base cluster. If the data structure of Top Tree was not 
	 * exposed on one vertex, then each base cluster is a path cluster. The base cluster is a point cluster
	 * only if the tree is exposed on one vertex and a right boundary of the base cluster is this exposed
	 * vertex.
	 * 
	 * @param c_i A cluster whose type will be find out.
	 * @return	A value of enumeration type <code>ClusterType</code> defined in {@link toptree.Cluster}.
	 */
	private Cluster.ClusterType typeOfBaseCluster(ClusterInfo c_i) {
		assert c_i.getCn().isBase();
		if (this.exposeOneVertex == null) {
			return Cluster.ClusterType.TYPE_PATH_CLUSTER;
		}
		else {
			assert c_i.getCn().getBv() == this.exposeOneVertex;
			return Cluster.ClusterType.TYPE_POINT_CLUSTER;
		}
	}
	
	
	/**
	 * This method gets three clusters (both sons and father) and returns a type of connection of children
	 * clusters into parental cluster.
	 * The type of connection is represented by enumeration type <code>ConnectionType</code> defined and
	 * described in detail in {@link toptree.Cluster}.
	 * <p>
	 * The method first checks if parental cluster is root cluster that was exposed on one vertex. If
	 * it is, then the decision goes in different way than for any other type of cluster. This cluster
	 * is represented by compress cluster node. The method checks which of boundary vertices or 
	 * compress vertex is saved in {@link toptree.impl.TopTreeImpl#exposeOneVertex} and according to it
	 * the method decides that the connection represents one of these types:
	 * <ul>
	 *   <li><code>TYPE_LPOINT_OVER_RPOINT</code></li>
	 *   <li><code>TYPE_RPOINT_OVER_LPOINT</code></li>
	 *   <li><code>TYPE_LPOINT_AND_RPOINT</code></li>
	 * </ul>
	 * <p>
	 * For any other parental cluster the method decides in standard way. It looks which type of cluster
	 * node represents the connection and according to this type it distinguishes three different way of
	 * deciding:
	 * <ul>
	 *   <li>Parent is rake node: The easiest case - returns <code>TYPE_POINT_AND_POINT</code>.</li>
	 *   <li>Parent is hard rake node: It decides between <code>TYPE_PATH_AND_POINT</code> and 
	 *   <code>TYPE_POINT_AND_PATH</code> and returns it.</li>
	 *   <li>Parent is compress cluster node: It decides between <code>TYPE_PATH_AND_POINT</code>, 
	 *   <code>TYPE_POINT_AND_PATH</code> and <code>TYPE_PATH_AND_PATH</code> and returns it.</li>
	 * </ul>
	 * 
	 * @param a_i	A cluster that is left child of parental cluster <code>c_i</code>.
	 * @param b_i	A cluster that is right child of parental cluster <code>c_i</code>.
	 * @param c_i	A cluster that is parental cluster of <code>a_i</code> and <code>b_i</code>.
	 * @return	A value of enumeration type <code>ConnectionType</code> defined in {@link toptree.Cluster}.
	 * @throws TopTreeIllegalAccessException	If the method tries to work with not allowed clusters.
	 */
	private Cluster.ConnectionType typeOfJoin(ClusterInfo a_i, ClusterInfo b_i, ClusterInfo c_i)
		throws TopTreeIllegalAccessException {

		// take nodes from ClusterInfo
		ClusterNode a = a_i.getCn();
		ClusterNode b = b_i.getCn();
		ClusterNode c = c_i.getCn();
		
	    // if c is a top cluster node, expose was called only on one vertex and c_i represents whole tree 
		if (c.isTopCluster() && this.exposeOneVertex != null && c.clusterInfo == c_i) {
	    	assert c.isCompress();
	    	/* boundaries must be tested on Cluster type a_i and b_i where is annul possible reversion,
	    	 * !c_i doesn't annul reversion - it is cluster info of c, not composed info! */
	    	if (a_i.getBu() == this.exposeOneVertex.getInfo()) {
	    		return Cluster.ConnectionType.TYPE_LPOINT_OVER_RPOINT;
	    	} else if (b_i.getBv() == this.exposeOneVertex.getInfo()) {
		    	return Cluster.ConnectionType.TYPE_RPOINT_OVER_LPOINT;
		    } else {
		    	assert ((CompressClusterNode) c).getCompressedVertex() == this.exposeOneVertex;
		    	return Cluster.ConnectionType.TYPE_LPOINT_AND_RPOINT;
		    }
	    }
		
		if (c.isHardRake()) {
			assert c.left == a && c.right == b;
			if (!c.isSelectAuxiliary() && !c.isSelectModified()) {
				/* Used in cleaning and fixing during (undo)hardExpose - it is enough to solve one
				 * symmetric variant with hard rake on left. Cannot be merged with variant for select,
				 * because during cleaning sons references to their new fathers. */
				return a.isHardRake() ?
						Cluster.ConnectionType.TYPE_PATH_AND_POINT : Cluster.ConnectionType.TYPE_POINT_AND_PATH;
			} else {
				/* Used for rebuilding after select-operation. 
				 * If c is auxiliary that means we are in cleanAfterSelect.
				 * If c is modified that means we are in fixateAfterSelect - c is set to modified only for this if-block.
				 */
				if (a.parent == c) {
					return Cluster.ConnectionType.TYPE_PATH_AND_POINT;
				} else {
					assert b.parent == c;
					return Cluster.ConnectionType.TYPE_POINT_AND_PATH;
				}				
			}
	    }
	    else if (c.isCompress()) {
	    	CompressClusterNode cc = (CompressClusterNode) c;
	    	if (a != c && b != c) {
	    		// children are not composed clusters of compress cluster node
	    		if (cc.left == a && cc.right == b) {
	    			return Cluster.ConnectionType.TYPE_PATH_AND_PATH;
	    		} else if (cc.left == b && cc.right == a) {
		    		return Cluster.ConnectionType.TYPE_PATH_AND_PATH;
	    		} else if (cc.leftFoster == a && cc.left == b) {
	    			return Cluster.ConnectionType.TYPE_POINT_AND_PATH;
	    		} else if (cc.left == a && cc.leftFoster == b) {
	    			return Cluster.ConnectionType.TYPE_PATH_AND_POINT;
	    		} else if (cc.rightFoster == a && cc.right == b) {
	    			return Cluster.ConnectionType.TYPE_POINT_AND_PATH;
	    		} else {
	    			assert cc.right == a && cc.rightFoster == b;
	    			return Cluster.ConnectionType.TYPE_PATH_AND_POINT;
	    		}
	    	}
	    	else {
	    		// at least one child is composed cluster of compress cluster node
	    		assert (cc.leftComposedClusterInfo == a_i && cc.rightComposedClusterInfo == b_i)
	    			|| (cc.left == a && cc.rightComposedClusterInfo == b_i)
	    			|| (cc.leftComposedClusterInfo == a_i && cc.right == b);
	    		return Cluster.ConnectionType.TYPE_PATH_AND_PATH;
	    	}
	    }
	    else {
	    	assert c.isRake();
	    	return Cluster.ConnectionType.TYPE_POINT_AND_POINT;
	    }

	}


	/**
	 * This method prepares sons of the root of Top Tree for first iteration of select operation a returns
	 * it in {@link toptree.impl.TopTreeImpl.SonNodes} object. The root must be referenced from class
	 * field {@link toptree.impl.TopTreeImpl#origTop}.
	 * <p>
	 * If the root is only an edge then the method returns appropriate object with only left son.
	 * Else if must prepare sons and a type of connection. The root can be a compress cluster node or
	 * a hard rake cluster node. So there are two different techniques.
	 * <p>
	 * The root is a compress cluster node:<br />
	 * It is necessary to normalize this compress cluster node before manipulation with its children.
	 * According to foster sons the method distinguishes four cases:
	 * <ul>
	 *   <li>Left and right foster sons are not null. The method first splits user defined information
	 *   from root cluster and composed clusters into proper and foster children. Then it creates two
	 *   select-auxiliary hard rake nodes that are sons for the first iteration of select. The left 
	 *   consists of original left proper as left son and original left foster as right son. The right 
	 *   hard rake node consists of original right foster son as left son and original right proper son
	 *   as right son. Appropriate user defined information are computed in auxiliary hard rakes.</li>
	 *   <li>Only left foster son is not null. The method first splits user defined information
	 *   from root cluster and left composed cluster into proper and foster children. Then it creates one
	 *   select-auxiliary hard rake node that will be left son for the first iteration of select. The
	 *   hard rake node consists of original left proper as left son and original left foster as right son.
	 *   The right son for first iteration will be the original right proper son of root. Appropriate 
	 *   user defined information are computed in auxiliary hard rake.</li>
	 *   <li>Only right foster son is not null. The method first splits user defined information
	 *   from root cluster and right composed cluster into proper and foster children. Then it creates one
	 *   select-auxiliary hard rake node that will be right son for the first iteration of select. The
	 *   hard rake node consists of original right foster son as left son and original right proper son
	 *   as right son. The left son for first iteration will be the original left proper son of root.
	 *   Appropriate user defined information are computed in auxiliary hard rake.</li>
	 *   <li>Both foster sons are null. The method first splits user defined information
	 *   from root cluster into proper children. The left son for first iteration will be the original
	 *   left proper son and the right will be the original right proper son.</li>
	 * </ul>
	 * The original root is set to be <code>NEW</code>. It still references to its original children, 
	 * but these children references to their auxiliary fathers. Type of connection for the first iteration
	 * is in all four cases <code>TYPE_PATH_AND_PATH</code>.
	 * <p>
	 * The root is a hard rake cluster node:<br />
	 * Recall here first that hard rake node has two children - the proper one (references to the father by
	 * parent-reference) and the foster one (references to the father by link-reference).
	 * The root cannot be reversed so there is not needed any normalization, because it was normalized in
	 * expose operation after hardExpose. This is the reason that there must be distinguishes not only two
	 * but all four symmetrical variants:
	 * <ul>
	 *   <li>Right son is proper and not hard rake. The method first splits user defined information
	 *   from root cluster into children. The left son for first iteration will be the original
	 *   left foster son and the right will be the original right proper son. The type of connection is
	 *   <code>TYPE_POINT_AND_PATH</code>.</li>
	 *   <li>Left son is proper and not hard rake. Symmetrical variant to previous case. The method first
	 *   splits user defined information from root cluster into children. The left son for first iteration
	 *   will be the original left proper son and the right will be the original right foster son. 
	 *   The type of connection for the first iteration is <code>TYPE_PATH_AND_POINT</code>.</li>
	 *   <li>Left son is proper and hard rake. The method first splits user defined information
	 *   from root cluster into children and from the left hard rake son of root into its children.
	 *   Then it creates one select-auxiliary hard rake node that will be left son for the first iteration
	 *   of select. The hard rake node consists of the same children as the original left hard rake son of
	 *   the root. The right son for first iteration will be the original right son of the root. 
	 *   Appropriate user defined information are computed in auxiliary hard rake.
	 *   The type of connection for the first iteration is <code>TYPE_PATH_AND_POINT</code>.</li>
	 *   <li>Right son is proper and hard rake. Symmetrical variant to previous case. The method first
	 *   splits user defined information from root cluster into children and from the right hard rake 
	 *   son of root into its children. Then it creates one select-auxiliary hard rake node that will be
	 *   right son for the first iteration of select. The hard rake node consists of the same children 
	 *   as the original right hard rake son of the root. The left son for first iteration will be 
	 *   the original left son of the root. Appropriate user defined information are computed in 
	 *   auxiliary hard rake. The type of connection for the first iteration is 
	 *   <code>TYPE_POINT_AND_PATH</code>.</li>
	 * </ul>
	 * All original hard rake cluster nodes are set to be <code>NEW</code>. They still references to 
	 * their original children, but these children references to their auxiliary fathers.
	 * 
	 * @return	An object {@link toptree.impl.TopTreeImpl.SonNodes} with prepared sons and type of their
	 * 			connection in the root cluster node.
	 */
	private SonNodes prepareRootForSelect() {
		assert origTop != null && origTop.isTopCluster();	// in origTop must be top cluster node
		
		// trivial case: tree is one edge
		if (origTop.isBase()) {
			return new SonNodes(origTop);
		}
		
		// prepare variable for new sons
		SonNodes sons = new SonNodes();		
		
		// divide root into two children clusters
		if (origTop.isCompress()) {
			CompressClusterNode cc = (CompressClusterNode) origTop;
			
			// normalize the root - we need to know real orientation of sons
			cc.normalize();
			
			// Prepare variable for type of connection of clusters a, b, c
			Cluster.ConnectionType type = null;
			
			if (cc.leftFoster != null && cc.rightFoster != null) {
				// allow access to information into changed clusters
				cc.clusterInfo.setAllowedLocalAccess(true);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(true);
				cc.left.clusterInfo.setAllowedLocalAccess(true);
				cc.right.clusterInfo.setAllowedLocalAccess(true);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
						cc.clusterInfo);
				event.split(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
						cc.clusterInfo, type);
				type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo);
				event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo, type);
				type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo);
				event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo, type);

				// set state for changed vertices
				cc.setState(ClusterNode.NEW);
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.leftFoster.isClean();
				assert cc.rightFoster.isClean();

				// set new sons
				sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
						cc.left, cc.leftFoster);
				sons.left.setState(ClusterNode.SELECT_AUXILIARY);
				sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
						cc.rightFoster, cc.right);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;
				
				// both sons are auxiliary hard rakes, compute their information
				event.join(sons.left.clusterInfo, cc.left.clusterInfo,
						cc.leftFoster.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				event.join(sons.right.clusterInfo, cc.rightFoster.clusterInfo,
						cc.right.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

				// disallow access to information into changed clusters
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.right.clusterInfo.setAllowedLocalAccess(false);
				cc.left.clusterInfo.setAllowedLocalAccess(false);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
				cc.clusterInfo.setAllowedLocalAccess(false);
			}
			else if (cc.leftFoster != null) {
				// allow access to information into changed clusters
				cc.clusterInfo.setAllowedLocalAccess(true);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
				cc.left.clusterInfo.setAllowedLocalAccess(true);
				cc.right.clusterInfo.setAllowedLocalAccess(true);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo);
				event.split(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);
				type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo);
				event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo, type);

				// set state for changed vertices
				cc.setState(ClusterNode.NEW);
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.leftFoster.isClean();

				// set new sons
				sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
						cc.left, cc.leftFoster);
				sons.left.setState(ClusterNode.SELECT_AUXILIARY);
				sons.right = cc.right;
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;

				// left son is auxiliary hard rake, compute its information
				event.join(sons.left.clusterInfo, cc.left.clusterInfo,
						cc.leftFoster.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

				// disallow access to information into changed clusters
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.right.clusterInfo.setAllowedLocalAccess(false);
				cc.left.clusterInfo.setAllowedLocalAccess(false);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
				cc.clusterInfo.setAllowedLocalAccess(false);
			}
			else if (cc.rightFoster != null) {
				// allow access to information into changed clusters
				cc.clusterInfo.setAllowedLocalAccess(true);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(true);
				cc.left.clusterInfo.setAllowedLocalAccess(true);
				cc.right.clusterInfo.setAllowedLocalAccess(true);
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo);
				event.split(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo, type);
				type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo);
				event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo, type);

				// set state for changed vertices
				cc.setState(ClusterNode.NEW);
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.rightFoster.isClean();

				// set new sons
				sons.left = cc.left;
				sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
						cc.rightFoster, cc.right);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;
				
				// right son is auxiliary hard rake, compute its information
				event.join(sons.right.clusterInfo, cc.rightFoster.clusterInfo,
						cc.right.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

				// disallow access to information into changed clusters
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.right.clusterInfo.setAllowedLocalAccess(false);
				cc.left.clusterInfo.setAllowedLocalAccess(false);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
				cc.clusterInfo.setAllowedLocalAccess(false);
			}
			else{
				// allow access to information into changed clusters
				cc.clusterInfo.setAllowedLocalAccess(true);
				cc.left.clusterInfo.setAllowedLocalAccess(true);
				cc.right.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo);
				event.split(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);

				// set state for changed vertices
				cc.setState(ClusterNode.NEW);
				assert cc.left.isClean();
				assert cc.right.isClean();

				// set new sons
				sons.left	= cc.left;
				sons.right	= cc.right;
				sons.type	= Cluster.ConnectionType.TYPE_PATH_AND_PATH;

				// disallow access to information into changed clusters
				cc.right.clusterInfo.setAllowedLocalAccess(false);
				cc.left.clusterInfo.setAllowedLocalAccess(false);
				cc.clusterInfo.setAllowedLocalAccess(false);
			}
		
		} 
		else {
			assert origTop.isHardRake();
			
			if (origTop.right.parent == origTop && !origTop.right.isHardRake()) {
				// allow access to information into changed clusters
				origTop.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.clusterInfo.setAllowedLocalAccess(true);
				
				// split information of root into sons
				event.split(origTop.left.clusterInfo, origTop.right.clusterInfo,
						origTop.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

				// set state for changed vertices
				origTop.setState(ClusterNode.NEW);
				assert origTop.left.isClean();
				assert origTop.right.isClean();
				
				// set new sons
				sons.left	= origTop.left;
				sons.right	= origTop.right;
				sons.type	= Cluster.ConnectionType.TYPE_POINT_AND_PATH;

				// disallow access to information into changed clusters
				origTop.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.clusterInfo.setAllowedLocalAccess(false);
			} 
			else if (origTop.left.parent == origTop && !origTop.left.isHardRake()) {
				// allow access to information into changed clusters
				origTop.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.clusterInfo.setAllowedLocalAccess(true);
				
				// split information of root into sons
				event.split(origTop.left.clusterInfo, origTop.right.clusterInfo,
						origTop.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

				// set state for changed vertices
				origTop.setState(ClusterNode.NEW);
				assert origTop.left.isClean();
				assert origTop.right.isClean();
				
				// set new sons
				sons.left	= origTop.left;
				sons.right	= origTop.right;
				sons.type	= Cluster.ConnectionType.TYPE_PATH_AND_POINT;

				// disallow access to information into changed clusters
				origTop.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.clusterInfo.setAllowedLocalAccess(false);
			} 
			else if (origTop.left.parent == origTop && origTop.left.isHardRake()) {
				// allow access to information into changed clusters
				origTop.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.right.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				event.split(origTop.left.clusterInfo, origTop.right.clusterInfo,
						origTop.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				// split information of root's left son into its sons
				event.split(origTop.left.left.clusterInfo, origTop.left.right.clusterInfo,
						origTop.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

				// set state for changed vertices
				origTop.setState(ClusterNode.NEW);
				origTop.left.setState(ClusterNode.NEW);
				assert origTop.right.isClean();
				assert origTop.left.left.isClean();
				assert origTop.left.right.isClean();
				
				// set new sons
				sons.left = createHardRakeClusterNode(origTop.left.getBu(), origTop.left.getBv(),
						origTop.left.left, origTop.left.right);
				sons.left.setState(ClusterNode.SELECT_AUXILIARY);
				sons.right = origTop.right;
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_POINT;

				// left is auxiliary hard rake, compute its information
				event.join(sons.left.clusterInfo, origTop.left.left.clusterInfo,
						origTop.left.right.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

				// disallow access to information into changed clusters
				origTop.left.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.left.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.clusterInfo.setAllowedLocalAccess(false);
			}
			else {
				assert origTop.right.parent == origTop && origTop.right.isHardRake();
				
				// allow access to information into changed clusters
				origTop.clusterInfo.setAllowedLocalAccess(true);
				origTop.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.left.clusterInfo.setAllowedLocalAccess(true);
				origTop.right.right.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				event.split(origTop.left.clusterInfo, origTop.right.clusterInfo,
						origTop.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
				// split information of root's left son into its sons
				event.split(origTop.right.left.clusterInfo, origTop.right.right.clusterInfo,
						origTop.right.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

				// set state for changed vertices
				origTop.setState(ClusterNode.NEW);
				origTop.right.setState(ClusterNode.NEW);
				assert origTop.left.isClean();
				assert origTop.right.left.isClean();
				assert origTop.right.right.isClean();
				
				// set new sons
				sons.left = origTop.left;
				sons.right = createHardRakeClusterNode(origTop.right.getBu(), origTop.right.getBv(),
						origTop.right.left, origTop.right.right);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				sons.type = Cluster.ConnectionType.TYPE_POINT_AND_PATH;

				// left is auxiliary hard rake, compute its information
				event.join(sons.right.clusterInfo, origTop.right.left.clusterInfo,
						origTop.right.right.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

				// disallow access to information into changed clusters
				origTop.right.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.right.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.right.clusterInfo.setAllowedLocalAccess(false);
				origTop.left.clusterInfo.setAllowedLocalAccess(false);
				origTop.clusterInfo.setAllowedLocalAccess(false);
			}

		}

		return sons;		

	}	
	

	

	
	/**
	 * This internal class is used during select operation for preparing sons of root cluster node for
	 * every iteration of 
	 * {@link toptree.impl.TopTreeImpl#selectStep(toptree.impl.TopTreeImpl.SonNodes, boolean)}.
	 * <p>
	 * It holds left and right son of the root, type of connection of these sons in the root and 
	 * provides method for managing these fields.
	 * 
	 * @author Michal Vajbar
	 * @version %I%, %G%
	 * @since 1.0
	 */
	private final class SonNodes {
		
		/**
		 * A left son of the root node.
		 */
		ClusterNode left;
		
		/**
		 * A right son of the root node.
		 */
		ClusterNode right;
		
		/**
		 * A type of connection of sons in the root node.
		 */
		Cluster.ConnectionType type;
		
		/**
		 * An empty constructor fills all fields of this class by <code>null</code> values.
		 */
		SonNodes() {
			this.left	= null;
			this.right	= null;
			this.type	= null;
		}
		
		/**
		 * This constructor is called on the root that consists of one edge - it puts the root 
		 * base cluster node into <code>left</code> field.
		 * 
		 * @param left	A root that has no children - base cluster node.
		 */
		SonNodes(ClusterNode left) {
			assert left.isBase();
			this.left	= left;
			this.right	= null;
			this.type	= null;
		}
		
		/**
		 * This constructor fills all fields of the class with appropriate given values.
		 * 
		 * @param left	A left son of the root cluster node.
		 * @param right	A right son of the root cluster node.
		 * @param type	A type of connection of sons in the root cluster node.
		 */
		SonNodes(ClusterNode left, ClusterNode right, Cluster.ConnectionType type) {
			this.left	= left;
			this.right	= right;
			this.type	= type;
		}
		
		/**
		 * This method counts the number of sons that are not null in this object and returns this value.
		 * 
		 * @return	If there is no son then 0, if there is only <code>left</code> then 1 and
		 * 			if there are both sons then 2.
		 */
		int size() {
			if (left != null && right != null) {
				return 2;
			} else if (left != null || right != null) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	
	/**
	 * This method checks if given cluster node <code>cn</code> is <code>CLEAN</code> and base cluster 
	 * node or if it has has only one child that is <code>CLEAN</code> and base cluster node then it
	 * returns this clean base cluster node, else it returns <code>null</code> value.
	 * 
	 * @param cn	One of sons that will be the root in next select iteration.
	 * @return	An appropriate base cluster node if <code>cn</code> is clean base cluster node or only
	 * 			one of its sons is clean and base, else it returns <code>null</code> value.
	 */
	private ClusterNode isCleanOnlyBaseInNode(ClusterNode cn) {
		
		// cn is clean 
		if (cn.isClean()) {
			if (cn.isBase()) {
				// cn is base - return it
				return cn;
			}
			// cn is clean, but not base
			return null;
		}
		assert cn.isSelectAuxiliary() && (cn.isHardRake() || cn.isRake());
		
		// cn is select-auxiliary hard rake or rake - look at its children
		if (cn.left.isClean() && cn.right.isClean()) {
			// both children are clean
			return null;
		} else if (cn.left.isClean()) {
			// just one child is clean - look if it is base
			if (cn.left.isBase()) {
				return cn.left;
			}
		} else if (cn.right.isClean()) {
			// just one child is clean - look if it is base
			if (cn.right.isBase()) {
				return cn.right;
			}
		}
		
		return null;
		
	}
	
	
	/**
	 * This method makes given cluster node <code>cn</code> to be a point cluster with right boundary
	 * vertex <code>v</code> and returns this vertex.
	 * <p>
	 * The given cluster node can be only clean or select-auxiliary. If it is clean then the method first
	 * set it to be select-modified. So returned cluster node is select-auxiliary or select-modified.
	 * <p>
	 * If given node is rake cluster node it should be right oriented. If it is not rake node, then 
	 * the orientation is checked and the node is eventually reversed.
	 * 
	 * @param cn	A cluster that will become a point cluster with <code>v</code> as right boundary.
	 * @param v	A vertex that must be right boundary of created point cluster.
	 * @return	A point cluster whose right boundary vertex is <code>v</code>.
	 */
	private ClusterNode createSelectRake(ClusterNode cn, VertexInfo v) {
		
		assert cn.isSelectAuxiliary() || cn.isClean();

		// if cn is clean then set it to select-modified
		if (cn.isClean()) {
			assert !cn.isHardRake();
			cn.setState(ClusterNode.SELECT_MODIFIED);
		}
		assert cn.isSelectAuxiliary() || cn.isSelectModified();
		
		// rake should be right oriented
		assert cn.isRake() ? cn.getBv() == v : true;

		// check orientation
		if (cn.getBv() != v) {
			cn.reverse();
		}
		assert cn.getBv() == v;
		
		return cn;
		
	}
	
	
	/**
	 * This method creates from sons of given cluster node that was picked in current select iteration sons
	 * for next iteration and returns this sons. Given node cannot be base cluster node. If parameter
	 * <code>left_rake</code> is not null then it is raked on the left son of next iteration.
	 * Analogical not null <code>right_rake</code> is raked on the right son of next iteration.
	 * <p>
	 * According to type of cluster node <code>cn</code> there are three different techniques. There is
	 * a few common features of all these techniques. They recompute user defined information from root
	 * and from select-auxiliary nodes into their sons. If the root is clean then it is set to be clean and
	 * the select-auxiliary nodes become dirty. All created nodes are set to be select-auxiliary and
	 * the method computes their user defined information. The methods covers in sum 24 cases 
	 * (some are symmetric pairs). Techniques according to type of cluster node <code>cn</code>:
	 * <ol>
	 *   <li type="1">
	 *     RAKE CLUSTER NODE<br />
	 *     There must be just one of <code>left_rake</code> and <code>right_rake</code> not null. It
	 *     doesn't matter which one is not null, it will be raked as right son of select-auxiliary 
	 *     rake node. There are two types of preparing the sons for next iteration:<br />
	 *     <ol>
	 *       <li type="a">
	 *         <code>cn</code> is clean:<br />
	 *         It means that its both sons are clean. So the left son for next iteration will be original 
	 *         left son and the right will be rake node with original right son at left and given point 
	 *         cluster on right.
	 *       </li>
	 *       <li type="a">
	 *         <code>cn</code> is not clean:<br />
	 *         It means that only its left son is clean. So the method prepares new point cluster node from
	 *         original right son and given point cluster node and call itself recursively on original
	 *         left son and prepared point cluster node.
	 *       </li>
	 *     </ol>
	 *   </li>
	 *   <li type="1">
	 *     HARD RAKE CLUSTER NODE<br />
	 *     There must be just one of <code>left_rake</code> and <code>right_rake</code> not null. According
	 *     to which one it is the left and symmetric right variants exists. There will be description only
	 *     for not null right rake - left is almost the same excepting symmetry. Given root node 
	 *     <code>cn</code> is hard rake so it must be auxiliary.
	 *     Variant are distinguished according to a type of its sons:<br />
	 *     R) <code>right_rake</code> is not null
	 *     <ol>
	 *       <li type="a">
	 *         Neither left or right son of root is clean:<br />
	 *         It means that left son is select-modified or select-auxiliary point cluster and right son
	 *         is hard rake node with both clean children. The left son for next iteration will be auxiliary
	 *         hard rake node with original left son of the root as left foster son and with original clean
	 *         left son of root's right son as proper left son. The right son for next iteration will be
	 *         auxiliary rake cluster node with original right son of root's right son as left son and 
	 *         given <code>right_rake</code> as right son.
	 *       </li>
	 *       <li type="a">
	 *         Both sons are clean and left is proper:<br />
	 *         It means that left son is clean path cluster and right son is clean point cluster.
	 *         The left son for next iteration will be original left son of the root.
	 *         The right son for next iteration will be auxiliary rake cluster node with original
	 *         right son of the root as left son and given <code>right_rake</code> as right son.
	 *       </li>
	 *       <li type="a">
	 *         Both sons are clean and right is proper:<br />
	 *         It means that left son is clean point cluster and right son is clean path cluster.
	 *         The left son for next iteration will be original left son of the root.
	 *         The right son for next iteration will be auxiliary hard rake cluster node with original
	 *         right son of the root as left son and given <code>right_rake</code> as right son.
	 *       </li>
	 *       <li type="a">
	 *         Left son of root is clean:<br />
	 *         It means that left son is clean path cluster and right son is select-modified or 
	 *         select-auxiliary point cluster. So the method prepares new point cluster node from
	 *         original right son and given point cluster <code>right_rake</code> and call itself 
	 *         recursively on original left son and prepared point cluster node.
	 *       </li>
	 *       <li type="a">
	 *         Right son of root is clean:<br />
	 *         It means that left son is select-modified or select-auxiliary point cluster and right
	 *         son is clean path cluster. So the method calls itself recursively on original right son,
	 *         original left son as left rake and given point cluster <code>right_rake</code>
	 *         as right rake.
	 *       </li>
	 *     </ol>
	 *     L) <code>left_rake</code> is not null
	 *     <ol>
	 *       <li type="a">
	 *         Neither left or right son of root is clean:<br />
	 *         Symmetrical to R) a.
	 *       </li>
	 *       <li type="a">
	 *         Both sons are clean and right is proper:<br />
	 *         Symmetrical to R) b.
	 *       </li>
	 *       <li type="a">
	 *         Both sons are clean and left is proper:<br />
	 *         Symmetrical to R) c.
	 *       </li>
	 *       <li type="a">
	 *         Right son of root is clean:<br />
	 *         Symmetrical to R) d.
	 *       </li>
	 *       <li type="a">
	 *         Left son of root is clean:<br />
	 *         Symmetrical to R) e.
	 *       </li>
	 *     </ol>
	 *   </li>
	 *   </li>
	 *   <li type="1">
	 *     COMPRESS CLUSTER NODE<br />
	 *     All sons of the compress cluster node are clean.
	 *     There must be at least one of <code>left_rake</code> and <code>right_rake</code> not null and
	 *     they can be not null both. So with possible null foster sons of the compress cluster it creates
	 *     16 cases how the preparing of sons for next iteration can seem. It is not necessary to describe
	 *     all variants - they are symmetric. Let's describe only variants for left part of compress
	 *     cluster node:<br />
	 *     <ol>
	 *       <li type="a">
	 *         Left foster son and <code>left_rake</code> are not null:<br />
	 *         The left son for next iteration will be auxiliary hard rake cluster node. Its left son is
	 *         foster and it is given point cluster <code>left_rake</code>. Its right son is proper
	 *         auxiliary hard rake cluster node with original left proper son as left proper son and 
	 *         with original left foster son as right foster son.
	 *       </li>
	 *       <li type="a">
	 *         Left foster son is not null and <code>left_rake</code> is null:<br />
	 *         The left son for next iteration will be auxiliary hard rake cluster node. Its left son is
	 *         foster and it is original left proper son. Its right son is proper and it is original left
	 *         foster son.
	 *       </li>
	 *       <li type="a">
	 *         Left foster son is null and <code>left_rake</code> is not null:<br />
	 *         The left son for next iteration will be auxiliary hard rake cluster node. Its left son is
	 *         foster and it is given point cluster <code>left_rake</code>. Its right son is proper
	 *         and it is original left proper son.
	 *       </li>
	 *       <li type="a">
	 *         Left foster son and <code>left_rake</code> are null:<br />
	 *         The left son for next iteration will be original left proper son.
	 *       </li>
	 *     </ol>
	 *     Rules for creating the right son for next iteration are symmetric. All 12 variants originates
	 *     from combining variants of left and right part of compress cluster node. 
	 *   </li>
	 * </ol>
	 * 
	 * @param cn A cluster node that was picked in this select iteration.
	 * @param left_rake A point cluster that will be raked on left son of next iteration.
	 * @param right_rake A point cluster that will be raked on right son of next iteration.
	 * @return	An object {@link toptree.impl.TopTreeImpl.SonNodes} with prepared sons and type of their
	 * 			connection in the root cluster node for next iteration.
	 */
	private SonNodes prepareNextSons(ClusterNode cn, ClusterNode left_rake, ClusterNode right_rake) {
		
		assert !cn.isBase();
		
		SonNodes sons = new SonNodes();
		
		if (cn.isRake()) {
			// just one rake must be not null
			assert left_rake == null || right_rake == null;
			assert left_rake != null || right_rake != null;
			
			// it doesn't matter which one is not null
			ClusterNode rake = (left_rake != null) ? left_rake : right_rake; 
			
			assert cn.getBv() == rake.getBv();
			
			// allow access to information into changed clusters
			cn.clusterInfo.setAllowedLocalAccess(true);
			cn.left.clusterInfo.setAllowedLocalAccess(true);
			cn.right.clusterInfo.setAllowedLocalAccess(true);
			rake.clusterInfo.setAllowedLocalAccess(true);

			// split information of root into sons
			event.split(cn.left.clusterInfo, cn.right.clusterInfo,
					cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

			if (cn.isClean()) {

				// cn is original node - set it new for rebuilding original tree
				cn.setState(ClusterNode.NEW);

				// check state for changed vertices
				assert cn.left.isClean();
				assert cn.right.isClean();
				assert rake.isSelectAuxiliary() || rake.isSelectModified();

				// new left son
				sons.left = cn.left;
				
				// new right son - new rake cluster
				sons.right = createRakeClusterNode(cn.getBv(), cn.right, rake);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				// right son is auxiliary rake, compute its information
				event.join(sons.right.clusterInfo, cn.right.clusterInfo,
						rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

				// set type of connection in new root
				sons.type = Cluster.ConnectionType.TYPE_POINT_AND_POINT;
				
				// disallow access to information into changed clusters
				rake.clusterInfo.setAllowedLocalAccess(false);
				cn.right.clusterInfo.setAllowedLocalAccess(false);
				cn.left.clusterInfo.setAllowedLocalAccess(false);
				cn.clusterInfo.setAllowedLocalAccess(false);
			}
			else {
				assert cn.isSelectAuxiliary();
				assert cn.left.isClean();
				assert !cn.left.isBase();
				assert cn.right.isSelectAuxiliary() || cn.right.isSelectModified();
				
				// cn is not need
				cn.setState(ClusterNode.DIRTY);

				// check state for changed vertices
				assert cn.left.isClean();
				assert cn.right.isSelectAuxiliary() || cn.right.isSelectModified();
				assert rake.isSelectAuxiliary() || rake.isSelectModified();
				
				// create new rake tree for recursion
				ClusterNode rake_tree = createRakeClusterNode(cn.right.getBv(), cn.right, rake);
				rake_tree.setState(ClusterNode.SELECT_AUXILIARY);
				// compute information for new rake tree
				event.join(rake_tree.clusterInfo, cn.right.clusterInfo,
						rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

				// disallow access to information into changed clusters
				rake.clusterInfo.setAllowedLocalAccess(false);
				cn.right.clusterInfo.setAllowedLocalAccess(false);
				cn.left.clusterInfo.setAllowedLocalAccess(false);
				cn.clusterInfo.setAllowedLocalAccess(false);

				// set new sons - recursion
				sons = prepareNextSons(cn.left, null, rake_tree);
			}
			
		}
		else if (cn.isHardRake()) {
			
			assert cn.isSelectAuxiliary();
			assert !cn.isReversed();
			
			// just one rake must be not null
			assert left_rake == null || right_rake == null;
			assert left_rake != null || right_rake != null;
			
			// allow access to information into changed clusters
			cn.clusterInfo.setAllowedLocalAccess(true);
			cn.left.clusterInfo.setAllowedLocalAccess(true);
			cn.right.clusterInfo.setAllowedLocalAccess(true);

			if (right_rake != null) {
				assert cn.getBv() == right_rake.getBv();

				if (!cn.left.isClean() && !cn.right.isClean()) {
					assert cn.left.isSelectAuxiliary() || cn.left.isSelectModified();
					assert cn.right.isHardRake() && cn.right.isSelectAuxiliary();
					assert cn.right.left.isClean() && cn.right.right.isClean();
				
					// allow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(true);
					cn.right.left.clusterInfo.setAllowedLocalAccess(true);
					cn.right.right.clusterInfo.setAllowedLocalAccess(true);
					
					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.setState(ClusterNode.DIRTY);

					// split information of root's right son into sons
					event.split(cn.right.left.clusterInfo, cn.right.right.clusterInfo,
							cn.right.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.right.setState(ClusterNode.DIRTY);

					// new left son - new hard rake cluster
					sons.left = createHardRakeClusterNode(cn.getBu(), cn.getBv(),
							cn.left, cn.right.left);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.left.clusterInfo, cn.left.clusterInfo,
							cn.right.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

					// new right son - new rake cluster
					sons.right = createRakeClusterNode(cn.getBv(), cn.right.right, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.right.clusterInfo, cn.right.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_PATH_AND_POINT;
					
					// disallow access to information into changed clusters
					cn.right.right.clusterInfo.setAllowedLocalAccess(false);
					cn.right.left.clusterInfo.setAllowedLocalAccess(false);
					right_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.left.isClean() && cn.right.isClean() && cn.left.parent == cn) {
					// allow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.setState(ClusterNode.DIRTY);

					// new left son
					sons.left = cn.left;

					// new right son - new rake cluster
					sons.right = createRakeClusterNode(cn.getBv(), cn.right, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.right.clusterInfo, cn.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_PATH_AND_POINT;
					
					// disallow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.left.isClean() && cn.right.isClean() && cn.right.parent == cn) {
					// allow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.setState(ClusterNode.DIRTY);

					// new left son
					sons.left = cn.left;

					// new right son - new hard rake cluster
					sons.right = createHardRakeClusterNode(cn.getBu(), cn.getBv(), cn.right, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.right.clusterInfo, cn.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_POINT_AND_PATH;
					
					// disallow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.left.isClean() && (cn.right.isSelectAuxiliary() || cn.right.isSelectModified())
						&& cn.left.parent == cn) {
					// allow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.setState(ClusterNode.DIRTY);

					// create new rake tree for recursion
					ClusterNode rake_tree = createRakeClusterNode(cn.getBv(), cn.right, right_rake);
					rake_tree.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(rake_tree.clusterInfo, cn.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// disallow access to information into changed clusters
					right_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);

					// set new sons - recursion
					sons = prepareNextSons(cn.left, null, rake_tree);
				}
				else {
					assert (cn.left.isSelectAuxiliary() || cn.left.isSelectModified()) && cn.right.isClean()
					&& cn.right.parent == cn;

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.setState(ClusterNode.DIRTY);

					// disallow access to information into changed clusters
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);

					// set new sons - recursion
					sons = prepareNextSons(cn.right, cn.left, right_rake);
				}
			
			}
			else {
				assert left_rake != null;
				assert cn.getBu() == left_rake.getBv();

				if (!cn.left.isClean() && !cn.right.isClean()) {
					assert cn.right.isSelectAuxiliary() || cn.right.isSelectModified();
					assert cn.left.isHardRake() && cn.left.isSelectAuxiliary();
					assert cn.left.left.isClean() && cn.left.right.isClean();
				
					// allow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(true);
					cn.left.left.clusterInfo.setAllowedLocalAccess(true);
					cn.left.right.clusterInfo.setAllowedLocalAccess(true);
					
					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.setState(ClusterNode.DIRTY);

					// split information of root's left son into sons
					event.split(cn.left.left.clusterInfo, cn.left.right.clusterInfo,
							cn.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.left.setState(ClusterNode.DIRTY);

					// new left son - new rake cluster
					sons.left = createRakeClusterNode(cn.getBu(), cn.left.left, left_rake);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.left.clusterInfo, cn.left.left.clusterInfo,
							left_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// new right son - new hard rake cluster
					sons.right = createHardRakeClusterNode(cn.getBu(), cn.getBv(),
							cn.left.right, cn.right);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.right.clusterInfo, cn.left.right.clusterInfo,
							cn.right.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_POINT_AND_PATH;
					
					// disallow access to information into changed clusters
					cn.left.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.left.clusterInfo.setAllowedLocalAccess(false);
					left_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.left.isClean() && cn.right.isClean() && cn.right.parent == cn) {
					// allow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.setState(ClusterNode.DIRTY);

					// new left son - new rake cluster
					sons.left = createRakeClusterNode(cn.getBu(), cn.left, left_rake);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.left.clusterInfo, cn.left.clusterInfo,
							left_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// new right son
					sons.right = cn.right;

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_POINT_AND_PATH;
					
					// disallow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.left.isClean() && cn.right.isClean() && cn.left.parent == cn) {
					// allow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.setState(ClusterNode.DIRTY);

					// new right son - new hard rake cluster
					sons.left = createHardRakeClusterNode(cn.getBu(), cn.getBv(), left_rake, cn.left);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(sons.left.clusterInfo, left_rake.clusterInfo,
							cn.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);

					// new right son
					sons.right = cn.right;

					// set type of connection in new root
					sons.type = Cluster.ConnectionType.TYPE_PATH_AND_POINT;
					
					// disallow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);
				}
				else if (cn.right.isClean() && (cn.left.isSelectAuxiliary() || cn.left.isSelectModified())
						&& cn.right.parent == cn) {
					// allow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(true);

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					cn.setState(ClusterNode.DIRTY);

					// create new rake tree for recursion
					ClusterNode rake_tree = createRakeClusterNode(cn.getBu(), cn.left, left_rake);
					rake_tree.setState(ClusterNode.SELECT_AUXILIARY);
					// compute information for new rake tree
					event.join(rake_tree.clusterInfo, cn.left.clusterInfo,
							left_rake.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_POINT);

					// disallow access to information into changed clusters
					left_rake.clusterInfo.setAllowedLocalAccess(false);
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);

					// set new sons - recursion
					sons = prepareNextSons(cn.right, rake_tree, null);
				}
				else {
					assert (cn.right.isSelectAuxiliary() || cn.right.isSelectModified()) && cn.left.isClean()
					&& cn.left.parent == cn;

					// split information of root into sons
					event.split(cn.left.clusterInfo, cn.right.clusterInfo,
							cn.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					cn.setState(ClusterNode.DIRTY);

					// disallow access to information into changed clusters
					cn.right.clusterInfo.setAllowedLocalAccess(false);
					cn.left.clusterInfo.setAllowedLocalAccess(false);
					cn.clusterInfo.setAllowedLocalAccess(false);

					// set new sons - recursion
					sons = prepareNextSons(cn.left, left_rake, cn.right);
				}

			}
			
		}
		else {
			assert cn.isCompress();
			assert cn.isClean();
			
			CompressClusterNode cc = (CompressClusterNode) cn;
			
			// normalize the root - we need to know real orientation of sons
			cc.normalize();
			
			// Prepare variable for type of connection of clusters a, b, c
			Cluster.ConnectionType type = null;
			
			// allow access to information into changed clusters
			cc.clusterInfo.setAllowedLocalAccess(true);
			cc.left.clusterInfo.setAllowedLocalAccess(true);
			cc.right.clusterInfo.setAllowedLocalAccess(true);
			if (left_rake != null) left_rake.clusterInfo.setAllowedLocalAccess(true);
			if (right_rake != null) right_rake.clusterInfo.setAllowedLocalAccess(true);

			if (cc.leftFoster != null && cc.rightFoster != null) {
				// check state of sons
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.leftFoster.isClean();
				assert cc.rightFoster.isClean();

				// allow access to information into changed clusters
				cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(true);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
						cc.clusterInfo);
				event.split(cc.leftComposedClusterInfo, cc.rightComposedClusterInfo,
						cc.clusterInfo, type);
				type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo);
				event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo, type);
				type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo);
				event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo, type);

				// set state for changed vertex
				cc.setState(ClusterNode.NEW);
				
				// new left son - create hard rake node
				sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
						cc.left, cc.leftFoster);
				sons.left.setState(ClusterNode.SELECT_AUXILIARY);
				// left son is auxiliary hard rake, compute its information
				event.join(sons.left.clusterInfo, cc.left.clusterInfo,
						cc.leftFoster.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				// if there is left rake, then rake it to new left son
				if (left_rake != null) {
					// temporary node for hard rake
					ClusterNode tmp = sons.left;
					tmp.clusterInfo.setAllowedLocalAccess(true);
					// create new left son
					sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
							left_rake, tmp);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.left.clusterInfo, left_rake.clusterInfo,
							tmp.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					tmp.clusterInfo.setAllowedLocalAccess(false);
				}
				
				// new right son - create hard rake node
				sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
						cc.rightFoster, cc.right);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				// right son is auxiliary hard rake, compute its information
				event.join(sons.right.clusterInfo, cc.rightFoster.clusterInfo,
						cc.right.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
				// if there is right rake, then rake it to new right son
				if (right_rake != null) {
					// temporary node for hard rake
					ClusterNode tmp = sons.right;
					tmp.clusterInfo.setAllowedLocalAccess(true);
					// create new right son
					sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
							tmp, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.right.clusterInfo, tmp.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					tmp.clusterInfo.setAllowedLocalAccess(false);
				}
				
				// set type of connection in new root
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;
				

				// disallow access to information into changed clusters
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
			}
			else if (cc.leftFoster != null) {
				// check state of sons
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.leftFoster.isClean();

				// allow access to information into changed clusters
				cc.leftComposedClusterInfo.setAllowedLocalAccess(true);
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo);
				event.split(cc.leftComposedClusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);
				type = typeOfJoin(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo);
				event.split(cc.left.clusterInfo, cc.leftFoster.clusterInfo,
						cc.leftComposedClusterInfo, type);

				// set state for changed vertex
				cc.setState(ClusterNode.NEW);
				
				// new left son - create hard rake node
				sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
						cc.left, cc.leftFoster);
				sons.left.setState(ClusterNode.SELECT_AUXILIARY);
				// left son is auxiliary hard rake, compute its information
				event.join(sons.left.clusterInfo, cc.left.clusterInfo,
						cc.leftFoster.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				// if there is left rake, then rake it to new left son
				if (left_rake != null) {
					// temporary node for hard rake
					ClusterNode tmp = sons.left;
					tmp.clusterInfo.setAllowedLocalAccess(true);
					// create new left son
					sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
							left_rake, tmp);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.left.clusterInfo, left_rake.clusterInfo,
							tmp.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
					tmp.clusterInfo.setAllowedLocalAccess(false);
				}
				
				// new right son
				if (right_rake != null) {
					// if there is right rake, then rake it to new right son
					sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
							cc.right, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.right.clusterInfo, cc.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				} else {
					sons.right = cc.right;
				}
				
				// set type of connection in new root
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;

				// disallow access to information into changed clusters
				cc.leftFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.leftComposedClusterInfo.setAllowedLocalAccess(false);
			}
			else if (cc.rightFoster != null) {
				// check state of sons
				assert cc.left.isClean();
				assert cc.right.isClean();
				assert cc.rightFoster.isClean();

				// allow access to information into changed clusters
				cc.rightComposedClusterInfo.setAllowedLocalAccess(true);
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(true);

				// split information of root into sons
				type = typeOfJoin(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo);
				event.split(cc.left.clusterInfo, cc.rightComposedClusterInfo, cc.clusterInfo, type);
				type = typeOfJoin(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo);
				event.split(cc.rightFoster.clusterInfo, cc.right.clusterInfo,
						cc.rightComposedClusterInfo, type);

				// set state for changed vertex
				cc.setState(ClusterNode.NEW);
				
				// new left son
				if (left_rake != null) {
					// if there is left rake, then rake it to new left son
					sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
							left_rake, cc.left);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.left.clusterInfo, left_rake.clusterInfo,
							cc.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
				} else {
					sons.left = cc.left;
				}
				
				// new right son - create hard rake node
				sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
						cc.rightFoster, cc.right);
				sons.right.setState(ClusterNode.SELECT_AUXILIARY);
				// right son is auxiliary hard rake, compute its information
				event.join(sons.right.clusterInfo, cc.rightFoster.clusterInfo,
						cc.right.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
				// if there is right rake, then rake it to new right son
				if (right_rake != null) {
					// temporary node for hard rake
					ClusterNode tmp = sons.right;
					tmp.clusterInfo.setAllowedLocalAccess(true);
					// create new right son
					sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
							tmp, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.right.clusterInfo, tmp.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
					tmp.clusterInfo.setAllowedLocalAccess(false);
				}
				
				// set type of connection in new root
				sons.type = Cluster.ConnectionType.TYPE_PATH_AND_PATH;

				// disallow access to information into changed clusters
				cc.rightFoster.clusterInfo.setAllowedLocalAccess(false);
				cc.rightComposedClusterInfo.setAllowedLocalAccess(false);
			}
			else{
				// check state of sons
				assert cc.left.isClean();
				assert cc.right.isClean();

				// split information of root into sons
				type = typeOfJoin(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo);
				event.split(cc.left.clusterInfo, cc.right.clusterInfo, cc.clusterInfo, type);

				// set state for changed vertex
				cc.setState(ClusterNode.NEW);
				
				// new left son
				if (left_rake != null) {
					// if there is left rake, then rake it to new left son
					sons.left = createHardRakeClusterNode(cc.getBu(), cc.getCompressedVertex(),
							left_rake, cc.left);
					sons.left.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.left.clusterInfo, left_rake.clusterInfo,
							cc.left.clusterInfo, Cluster.ConnectionType.TYPE_POINT_AND_PATH);
				} else {
					sons.left = cc.left;
				}
				
				// new right son
				if (right_rake != null) {
					// if there is right rake, then rake it to new right son
					sons.right = createHardRakeClusterNode(cc.getCompressedVertex(), cc.getBv(),
							cc.right, right_rake);
					sons.right.setState(ClusterNode.SELECT_AUXILIARY);
					// compute its information
					event.join(sons.right.clusterInfo, cc.right.clusterInfo,
							right_rake.clusterInfo, Cluster.ConnectionType.TYPE_PATH_AND_POINT);
				} else {
					sons.right = cc.right;
				}
				
				// set type of connection in new root
				sons.type	= Cluster.ConnectionType.TYPE_PATH_AND_PATH;
			}

			// disallow access to information into changed clusters
			if (right_rake != null) right_rake.clusterInfo.setAllowedLocalAccess(false);
			if (left_rake != null) left_rake.clusterInfo.setAllowedLocalAccess(false);
			cc.right.clusterInfo.setAllowedLocalAccess(false);
			cc.left.clusterInfo.setAllowedLocalAccess(false);
			cc.clusterInfo.setAllowedLocalAccess(false);

		}
		
		return sons;
		
	}

	
	/**
	 * This method ensures recursively cleaning Top Tree after operation select from auxiliary cluster
	 * nodes. Given cluster node <code>cn</code> can't be null. The cleaning starts in the auxiliary root
	 * and works recursively.
	 * <p>
	 * When it comes into select-auxiliary cluster node, then it splits user defined information
	 * into sons by method
	 * {@link toptree.TopTreeListener#split(Cluster, Cluster, Cluster, toptree.Cluster.ConnectionType)}
	 * and then recursively cleans both sons.
	 * <p>
	 * When it comes into select-modified cluster node, it only sets the node to be clean. User defined
	 * information of this node were computed during cleaning of its auxiliary father. 
	 * 
	 * @param cn A cluster node whose subtree will be cleaned after select.
	 */
	private void cleanAfterSelect(ClusterNode cn) {
		
		// node must be not null
		assert cn != null;
		
		// for auxiliary node split info into sons and clean sons
		if (cn.isSelectAuxiliary()) {

			// compress may be only top, any other cluster is rake or hard rake
			assert cn.isHardRake() || cn.isRake() || cn.isCompress();

			assert cn.left != null && cn.right != null;
				
			// just recompute information into sons
			cn.clusterInfo.setAllowedLocalAccess(true);
			cn.left.clusterInfo.setAllowedLocalAccess(true);
			cn.right.clusterInfo.setAllowedLocalAccess(true);

			event.split(cn.left.clusterInfo, cn.right.clusterInfo, cn.clusterInfo, 
					Cluster.ConnectionType.TYPE_PATH_AND_PATH);
			
			cn.right.clusterInfo.setAllowedLocalAccess(false);
			cn.left.clusterInfo.setAllowedLocalAccess(false);
			cn.clusterInfo.setAllowedLocalAccess(false);
			
			// Clean all children and kill them.
			cleanAfterSelect(cn.left);
			cleanAfterSelect(cn.right);
			cn.left = cn.right = null;
		} else if (cn.isSelectModified()) {
			// set select-modified cluster nodes back to clean
			cn.setState(ClusterNode.CLEAN);
		} else {
			// if the node was not auxiliary and was nod modified, it must be clean
			assert cn.isClean();
		}
		
	}

	
	/**
	 * This method ensures recursively fixating Top Tree into state before select operation.
	 * The fixating starts in the original root (now set to new) and works recursively.
	 * <p>
	 * If given cluster node <code>cn</code> is null or it is not new, then the method does nothing.
	 * <p>
	 * If <code>cn</code> is new, it means that this node was in original tree, but during select it
	 * becomes root and was rebuilt. So it must be repaired here. Repairing works in similar way for all
	 * types of changed cluster nodes (rake, hard rake and compress cluster node). It must first create
	 * original references from sons to the father (sons refer to auxiliary nodes) and repair boundary
	 * orientation of sons. Next it recursively fixates all sons. When sons are repaired then it binds
	 * its own boundary vertices and eventually compress-vertex. In the end the node is set to be clean
	 * and the method computes its user defined information by calling method
	 * {@link toptree.TopTreeListener#join(Cluster, Cluster, Cluster, toptree.Cluster.ConnectionType)}.
	 * 
	 * @param cn A cluster node whose subtree will be fixated after select.
	 */
	private void fixateAfterSelect(ClusterNode cn) {
		// Node must be not null and new.
		if (cn != null && cn.isNew()) {
			
			if (cn.isRake()) {
				
				// repair left son's pointers to father
				if (cn.left.isRake()) {
					cn.left.parent	= cn;
					cn.left.link	= null;
				} else {
					assert cn.left.isBase() || cn.left.isCompress();
					cn.left.parent	= null;
					cn.left.link	= cn;
				}
				// check left son's right boundary
				if (cn.left.getBv() != cn.getBv()) {
					cn.left.reverse();
					assert cn.left.getBv() == cn.getBv();
				}
				
				// repair right son's pointers to father
				if (cn.right.isRake()) {
					cn.right.parent	= cn;
					cn.right.link	= null;
				} else {
					assert cn.right.isBase() || cn.right.isCompress();
					cn.right.parent	= null;
					cn.right.link	= cn;
				}
				// check right son's right boundary
				if (cn.right.getBv() != cn.getBv()) {
					cn.right.reverse();
					assert cn.right.getBv() == cn.getBv();
				}
				
				// fixate both sons
				fixateAfterSelect(cn.left);
				fixateAfterSelect(cn.right);

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				cn.setClean();

				// Report joining left and right child into this rake cluster to an application.
				cn.clusterInfo.setAllowedLocalAccess(true);
				cn.left.clusterInfo.setAllowedLocalAccess(true);
				cn.right.clusterInfo.setAllowedLocalAccess(true);

				// Find out the type of connection and give it to split method.
				type = typeOfJoin(cn.left.clusterInfo, cn.right.clusterInfo, cn.clusterInfo);
				event.join(cn.clusterInfo, cn.left.clusterInfo, cn.right.clusterInfo, type);

				cn.left.clusterInfo.setAllowedLocalAccess(false);
				cn.right.clusterInfo.setAllowedLocalAccess(false);
				cn.clusterInfo.setAllowedLocalAccess(false);

			} else if (cn.isHardRake()) {

				// repair left son's pointers to father and orientation
				if (cn.left.getBu() == cn.getBu() && cn.left.getBv() == cn.getBv()) {
					cn.left.parent	= cn;
					cn.left.link	= null;
				} else if (cn.left.getBu() == cn.getBv() && cn.left.getBv() == cn.getBu()) {
					assert cn.left.isBase() || cn.left.isCompress();
					cn.left.reverse();
					cn.left.parent	= cn;
					cn.left.link	= null;
				} else if (cn.left.getBu() == cn.getBu()) {
					cn.left.reverse();
					assert cn.left.getBv() == cn.getBu();
					cn.left.parent	= null;
					cn.left.link	= cn;
				} else {
					assert cn.left.getBv() == cn.getBu();
					cn.left.parent	= null;
					cn.left.link	= cn;
				}
				
				// repair right son's pointers to father and orientation
				if (cn.right.getBu() == cn.getBu() && cn.right.getBv() == cn.getBv()) {
					cn.right.parent	= cn;
					cn.right.link	= null;
				} else if (cn.right.getBu() == cn.getBv() && cn.right.getBv() == cn.getBu()) {
					assert cn.right.isBase() || cn.right.isCompress();
					cn.right.reverse();
					cn.right.parent	= cn;
					cn.right.link	= null;
				} else if (cn.right.getBu() == cn.getBv()) {
					cn.right.reverse();
					assert cn.right.getBv() == cn.getBv();
					cn.right.parent	= null;
					cn.right.link	= cn;
				} else {
					assert cn.right.getBv() == cn.getBv();
					cn.right.parent	= null;
					cn.right.link	= cn;
				}
				
				// fixate both sons
				fixateAfterSelect(cn.left);
				fixateAfterSelect(cn.right);

				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// bind vertices before recomputing user defined information
				cn.bindVertices();

				// Report joining left and right child into this rake cluster to an application.
				cn.clusterInfo.setAllowedLocalAccess(true);
				cn.left.clusterInfo.setAllowedLocalAccess(true);
				cn.right.clusterInfo.setAllowedLocalAccess(true);

				// Find out the type of connection and give it to split method.
				// set it to select-modified only for typeOfJoin so it can recognize the type
				cn.setState(ClusterNode.SELECT_MODIFIED);
				type = typeOfJoin(cn.left.clusterInfo, cn.right.clusterInfo, cn.clusterInfo);
				event.join(cn.clusterInfo, cn.left.clusterInfo, cn.right.clusterInfo, type);
				// the node is clean now
				cn.setState(ClusterNode.CLEAN);

				cn.left.clusterInfo.setAllowedLocalAccess(false);
				cn.right.clusterInfo.setAllowedLocalAccess(false);
				cn.clusterInfo.setAllowedLocalAccess(false);

				
			} else {
				assert cn.isCompress();
				// Compress cluster node
				CompressClusterNode ccn = (CompressClusterNode) cn;

				// repair left son's pointers to father and orientation
				if (ccn.left.getBu() == ccn.getCompressedVertex() && ccn.left.getBv() == ccn.getBu()) {
					ccn.left.reverse();
					ccn.left.parent	= ccn;
					ccn.left.link	= null;
				} else {
					assert ccn.left.getBu() == ccn.getBu() && ccn.left.getBv() == ccn.getCompressedVertex();
					ccn.left.parent	= ccn;
					ccn.left.link	= null;
				}
				
				// repair right son's pointers to father and orientation
				if (ccn.right.getBu() == ccn.getBv() && ccn.right.getBv() == ccn.getCompressedVertex()) {
					ccn.right.reverse();
					ccn.right.parent	= ccn;
					ccn.right.link		= null;
				} else {
					assert ccn.right.getBu() == ccn.getCompressedVertex() && ccn.right.getBv() == ccn.getBv();
					ccn.right.parent	= ccn;
					ccn.right.link		= null;
				}
				
				// repair left foster son's pointers and orientation if any exists
				if (ccn.leftFoster != null) {
					ccn.leftFoster.parent	= null;
					ccn.leftFoster.link		= ccn;
					// check right boundary
					if (ccn.leftFoster.getBv() != ccn.getCompressedVertex()) {
						ccn.leftFoster.reverse();
						assert ccn.leftFoster.getBv() == ccn.getCompressedVertex();
					}
				}
				
				// repair right foster son's pointers and orientation if any exists
				if (ccn.rightFoster != null) {
					ccn.rightFoster.parent	= null;
					ccn.rightFoster.link	= ccn;
					// check right boundary
					if (ccn.rightFoster.getBv() != ccn.getCompressedVertex()) {
						ccn.rightFoster.reverse();
						assert ccn.rightFoster.getBv() == ccn.getCompressedVertex();
					}
				}
				
				// Prepare variable for type of connection of clusters a, b, c
				Cluster.ConnectionType type = null;

				// It must be distinguish which foster children the node has.
				if (ccn.leftFoster != null && ccn.rightFoster != null) {
					// It has both foster children
					
					// First fixate all sons
					fixateAfterSelect(ccn.leftFoster);
					fixateAfterSelect(ccn.left);
					fixateAfterSelect(ccn.rightFoster);
					fixateAfterSelect(ccn.right);

					ccn.setClean();

					// bind vertices before recomputing user defined information
					ccn.bindVertices();

					// Report creating this compress cluster to an application.
					ccn.clusterInfo.setAllowedLocalAccess(true);
					ccn.left.clusterInfo.setAllowedLocalAccess(true);
					ccn.right.clusterInfo.setAllowedLocalAccess(true);
					ccn.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					ccn.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					ccn.leftComposedClusterInfo.setAllowedLocalAccess(true);
					ccn.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(ccn.left.clusterInfo, ccn.leftFoster.clusterInfo,
							ccn.leftComposedClusterInfo);
					event.join(ccn.leftComposedClusterInfo, ccn.left.clusterInfo,
							ccn.leftFoster.clusterInfo, type);
					type = typeOfJoin(ccn.rightFoster.clusterInfo, ccn.right.clusterInfo,
							ccn.rightComposedClusterInfo);
					event.join(ccn.rightComposedClusterInfo, ccn.rightFoster.clusterInfo,
							ccn.right.clusterInfo, type);
					type = typeOfJoin(ccn.leftComposedClusterInfo, ccn.rightComposedClusterInfo,
							ccn.clusterInfo);
					event.join(ccn.clusterInfo, ccn.leftComposedClusterInfo,
							ccn.rightComposedClusterInfo, type);

					ccn.leftComposedClusterInfo.setAllowedLocalAccess(false);
					ccn.rightComposedClusterInfo.setAllowedLocalAccess(false);
					ccn.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					ccn.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					ccn.left.clusterInfo.setAllowedLocalAccess(false);
					ccn.right.clusterInfo.setAllowedLocalAccess(false);
					ccn.clusterInfo.setAllowedLocalAccess(false);
				} else if (ccn.leftFoster != null) {
					// It has only left foster child
					
					// First fixate all sons
					fixateAfterSelect(ccn.leftFoster);
					fixateAfterSelect(ccn.left);
					fixateAfterSelect(ccn.right);

					ccn.setClean();

					// bind vertices before recomputing user defined information
					ccn.bindVertices();

					// Report creating this compress cluster to an application.
					ccn.clusterInfo.setAllowedLocalAccess(true);
					ccn.left.clusterInfo.setAllowedLocalAccess(true);
					ccn.right.clusterInfo.setAllowedLocalAccess(true);
					ccn.leftFoster.clusterInfo.setAllowedLocalAccess(true);
					ccn.leftComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(ccn.left.clusterInfo, ccn.leftFoster.clusterInfo,
							ccn.leftComposedClusterInfo);
					event.join(ccn.leftComposedClusterInfo, ccn.left.clusterInfo,
							ccn.leftFoster.clusterInfo, type);
					type = typeOfJoin(ccn.leftComposedClusterInfo, ccn.right.clusterInfo, ccn.clusterInfo);
					event.join(ccn.clusterInfo, ccn.leftComposedClusterInfo, ccn.right.clusterInfo, type);

					ccn.leftComposedClusterInfo.setAllowedLocalAccess(false);
					ccn.leftFoster.clusterInfo.setAllowedLocalAccess(false);
					ccn.left.clusterInfo.setAllowedLocalAccess(false);
					ccn.right.clusterInfo.setAllowedLocalAccess(false);
					ccn.clusterInfo.setAllowedLocalAccess(false);

				} else if (ccn.rightFoster != null) {
					// It has only right foster child
					
					// First fixate all sons
					fixateAfterSelect(ccn.left);
					fixateAfterSelect(ccn.rightFoster);
					fixateAfterSelect(ccn.right);

					ccn.setClean();

					// bind vertices before recomputing user defined information
					ccn.bindVertices();

					// Report creating this compress cluster to an application.
					ccn.clusterInfo.setAllowedLocalAccess(true);
					ccn.left.clusterInfo.setAllowedLocalAccess(true);
					ccn.right.clusterInfo.setAllowedLocalAccess(true);
					ccn.rightFoster.clusterInfo.setAllowedLocalAccess(true);
					ccn.rightComposedClusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(ccn.rightFoster.clusterInfo, ccn.right.clusterInfo,
							ccn.rightComposedClusterInfo);
					event.join(ccn.rightComposedClusterInfo, ccn.rightFoster.clusterInfo,
							ccn.right.clusterInfo, type);
					type = typeOfJoin(ccn.left.clusterInfo, ccn.rightComposedClusterInfo, ccn.clusterInfo);
					event.join(ccn.clusterInfo, ccn.left.clusterInfo, ccn.rightComposedClusterInfo, type);

					ccn.rightComposedClusterInfo.setAllowedLocalAccess(false);
					ccn.rightFoster.clusterInfo.setAllowedLocalAccess(false);
					ccn.left.clusterInfo.setAllowedLocalAccess(false);
					ccn.right.clusterInfo.setAllowedLocalAccess(false);
					ccn.clusterInfo.setAllowedLocalAccess(false);

				} else {
					// It has no foster children
					
					// First fixate proper sons
					fixateAfterSelect(ccn.left);
					fixateAfterSelect(ccn.right);

					ccn.setClean();

					// bind vertices before recomputing user defined information
					ccn.bindVertices();

					// Report creating this compress cluster to an application.
					ccn.clusterInfo.setAllowedLocalAccess(true);
					ccn.left.clusterInfo.setAllowedLocalAccess(true);
					ccn.right.clusterInfo.setAllowedLocalAccess(true);

					// Find out the type of connection and give it to split method.
					type = typeOfJoin(ccn.left.clusterInfo, ccn.right.clusterInfo, ccn.clusterInfo);
					event.join(ccn.clusterInfo, ccn.left.clusterInfo, ccn.right.clusterInfo, type);

					ccn.left.clusterInfo.setAllowedLocalAccess(false);
					ccn.right.clusterInfo.setAllowedLocalAccess(false);
					ccn.clusterInfo.setAllowedLocalAccess(false);
				}
				
			}
		}
	}

	
	/**
	 * This method ensures cleaning the tree after select operation and then restoring the tree into 
	 * state before the select operation.
	 * <p>
	 * For cleaning it calls method {@link toptree.impl.TopTreeImpl#cleanAfterSelect(ClusterNode)}.
	 * <p>
	 * For restoring it calls method {@link toptree.impl.TopTreeImpl#fixateAfterSelect(ClusterNode)}.
	 * 
	 * @param auxiliaryTop	A root of select-auxiliary tree used in select operation.
	 * @param originalTop	A root of original Top Tree before select operation.
	 */
	private void restoreTreeAfterSelect(ClusterNode auxiliaryTop, ClusterNode originalTop) {
		
		cleanAfterSelect(auxiliaryTop);
		fixateAfterSelect(originalTop);

	}
	
	
	/**
	 * This method recursively finds and returns a base cluster node that contains vertex that is 
	 * solution of user defined non-local problem by method
	 * {@link toptree.TopTreeListener#selectQuestion(Cluster, Cluster, toptree.Cluster.ConnectionType)}.
	 * <p>
	 * It starts from root's sons. In every iteration it chooses one of root's sons, rebuilds the
	 * tree so that children of this chosen son represents whole tree and calls itself recursively on
	 * children of chosen son.
	 * <p>
	 * The method first decide which son will be used in next iteration. If the method is called from
	 * {@link toptree.impl.TopTreeImpl#select(Vertex)} then <code>on_path</code> is <code>false</code>.
	 * If it is called from {@link toptree.impl.TopTreeImpl#select(Vertex, Vertex)} then the value
	 * is <code>true</code>. This value tells how the son for next iteration will be picked.
	 * If it is false, it means that the method always asks user defined method 
	 * <code>selectQuestion</code>. If it is true, then picked son must be a path cluster. It mean that
	 * <code>selectQuestion</code> is called only if both children are path clusters. Else it picks the
	 * cluster that is a path.
	 * <p>
	 * When the son for next iteration is picked, the method checks if the solution of user defined problem
	 * is available now by calling method {@link toptree.impl.TopTreeImpl#isCleanOnlyBaseInNode(ClusterNode)}.
	 * If it is then sons are connected into the auxiliary root and a base cluster with solution is 
	 * returned. If it isn't then the method prepares sons for next iteration of <code>selectStep</code>.
	 * <p>
	 * There are two steps for preparing sons for next iteration. First it is to prepare point cluster
	 * from son that was not picked by method
	 * {@link toptree.impl.TopTreeImpl#createSelectRake(ClusterNode, VertexInfo)}.
	 * Second step is to prepare sons for next iteration from picked son and point cluster created in
	 * first step. This is done by method
	 * {@link toptree.impl.TopTreeImpl#prepareNextSons(ClusterNode, ClusterNode, ClusterNode)}.
	 * <p>
	 * When sons are prepared then next iteration is recursively called.
	 * 
	 * @param sons 	An object {@link toptree.impl.TopTreeImpl.SonNodes} with root's sons prepared for this
	 * 				selectStep iteration. 
	 * @param on_path A switcher if this method is called from <code>select</code> on one or two vertices.
	 * @return	A base cluster node that contains solution of user defined problem
	 */
	private ClusterNode selectStep(SonNodes sons, boolean on_path) {
		
		// there must be just two sons
		assert sons.size() == 2;
		
		// prepare variable for next iteration sons
		SonNodes next_sons = null;
		
		// ask which of sons will be used in next iteration
		ClusterNode next_node = null;
		if (!on_path || (on_path && sons.type == Cluster.ConnectionType.TYPE_PATH_AND_PATH)) {
			// ask user defined method for son for next iteration
			sons.left.clusterInfo.setAllowedLocalAccess(true);
			sons.right.clusterInfo.setAllowedLocalAccess(true);
			next_node = ((ClusterInfo) event.selectQuestion(sons.left.clusterInfo,
					sons.right.clusterInfo, sons.type)).getCn();
			sons.right.clusterInfo.setAllowedLocalAccess(false);
			sons.left.clusterInfo.setAllowedLocalAccess(false);
		}
		else if (on_path) {
			// on_path is true and root is not connection of two path children - take path child
			switch (sons.type) {
			case TYPE_PATH_AND_POINT:
				next_node = sons.left;
				break;
			case TYPE_POINT_AND_PATH:
				next_node = sons.right;
				break;
			default:
				assert false;
			}
		}
		
		// look if there is modified only one cluster and it is base
		ClusterNode node = isCleanOnlyBaseInNode(next_node);
		if (node != null) {
			/* We have base cluster and we must connect sons into root for cleaning after select.
			 * Save root into origTop where it will be taken from for cleaning. */
			switch (sons.type) {
			case TYPE_PATH_AND_PATH:
				origTop = createCompressClusterNode(sons.left.getBv(), sons.left, null, sons.right, null);
				break;
			case TYPE_PATH_AND_POINT:
				origTop = createHardRakeClusterNode(sons.left.getBu(), sons.left.getBv(), sons.left, sons.right);
				break;
			case TYPE_POINT_AND_PATH:
				origTop = createHardRakeClusterNode(sons.right.getBu(), sons.right.getBv(), sons.left, sons.right);
				break;
			case TYPE_POINT_AND_POINT:
				origTop = createRakeClusterNode(sons.left.getBv(), sons.left, sons.right);
				break;
			default:
				assert false;
			}
			origTop.setState(ClusterNode.SELECT_AUXILIARY);
			
			
			// compute user define information for auxiliary root
			origTop.clusterInfo.setAllowedLocalAccess(true);
			sons.left.clusterInfo.setAllowedLocalAccess(true);
			sons.right.clusterInfo.setAllowedLocalAccess(true);

			event.join(origTop.clusterInfo, sons.left.clusterInfo, sons.right.clusterInfo, sons.type);
			
			sons.right.clusterInfo.setAllowedLocalAccess(false);
			sons.left.clusterInfo.setAllowedLocalAccess(false);
			origTop.clusterInfo.setAllowedLocalAccess(false);
			
			return node;
		}

		// prepare sons for next iteration
		if (next_node == sons.left) {
			// prepare rake tree from son that was not chosen
			if (	sons.type == Cluster.ConnectionType.TYPE_PATH_AND_PATH	||
					sons.type == Cluster.ConnectionType.TYPE_POINT_AND_PATH	||
					sons.type == Cluster.ConnectionType.TYPE_LPOINT_OVER_RPOINT	||
					sons.type == Cluster.ConnectionType.TYPE_RPOINT_OVER_LPOINT	||
					sons.type == Cluster.ConnectionType.TYPE_LPOINT_AND_RPOINT	) {
				node = createSelectRake(sons.right, sons.right.getBu());
			} else {
				assert	sons.type == Cluster.ConnectionType.TYPE_PATH_AND_POINT	||
						sons.type == Cluster.ConnectionType.TYPE_POINT_AND_POINT;
				node = createSelectRake(sons.right, sons.right.getBv());
			}
			
			// prepare sons for next iteration
			next_sons = prepareNextSons(next_node, null, node);
		}
		else {
			assert next_node == sons.right;

			// prepare rake tree from son that was not chosen
			node = createSelectRake(sons.left, sons.left.getBv());

			// prepare sons for next iteration
			next_sons = prepareNextSons(next_node, node, null);
		}
		
		// next iteration
		node = selectStep(next_sons, on_path);
			
		return node;
	}

	
	
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
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#expose(Vertex)} to annul possible previous
	 * expose on one vertex in any other component. If it recognizes that given vertex is single then
	 * it returns <code>null</code>.
	 * <p>
	 * Next it prepares sons for first iteration of finding. Preparation is ensured by calling method
	 * {@link toptree.impl.TopTreeImpl#prepareRootForSelect()}. The method must remember original root
	 * of the tree before select for restoring the tree after select. If the tree consists from one
	 * edge, so the base contains the solution. Otherwise the finding starts.
	 * <p>
	 * Whole finding is done by method
	 * {@link toptree.impl.TopTreeImpl#selectStep(toptree.impl.TopTreeImpl.SonNodes, boolean)} with
	 * <code>false</code> value for second parameter. It returns the required base cluster.
	 * During rebuilding the original nodes are not deleted. They are set to be new and they refer
	 * to their original sons while the sons refer to their auxiliary parental nodes.
	 * <p>
	 * When base cluster is found then the tree is restored by method
	 * {@link toptree.impl.TopTreeImpl#restoreTreeAfterSelect(ClusterNode, ClusterNode)} and 
	 * both vertices from base cluster are returned in array.
	 * 
	 * @param v	A vertex that determines a component of Top Tree for selecting.
	 * @return	Vertices that creates a base cluster that is result of selecting or <code>null</code>
	 * 			if the given vertex is single.
	 */
	public ArrayList<Vertex<V>> select(Vertex<V> v) {

		// expose v first to annul expose in other component - if any was 
		if (TopTree.ExposeOneResults.COMPONENT != expose(v)) {
			return null;
		} 
		assert getTopComponent(v) != null;

		// remember a top of original top tree for rebuilding back to original state
		origTop = ((ClusterInfo) getTopComponent(v)).getCn();
		
		// prepare root of top tree for select - create two clusters or one base if tree is one edge
		SonNodes prepared_root = prepareRootForSelect();
		
		// remember a top of original top tree in originalTop - into origTop will be saved auxiliary top
		ClusterNode originalTop = origTop;
		
		// remember if exposed on one vertex and set it to null - it would do mess while select-finding
		Vertex exposeOneVertexOriginalValue = exposeOneVertex;
		exposeOneVertex = null;
		
		// find base cluster that is solution of user defined algorithm
		ClusterNode base_cluster = null;
		if (prepared_root.size() == 1) {
			base_cluster = prepared_root.left;
		} else {
			base_cluster = selectStep(prepared_root, false);
			exposeOneVertex = exposeOneVertexOriginalValue;
			restoreTreeAfterSelect(origTop, originalTop);
			origTop = null;
		}

		// take both vertices form base cluster and return them in array
		base_cluster.clusterInfo.setAllowedLocalAccess(true);
		
		assert base_cluster.isBase();
		ArrayList<Vertex<V>> two_vertices = new ArrayList<Vertex<V>>(2);
		Vertex<V> bu = (Vertex<V>) base_cluster.getBu();
		Vertex<V> bv = (Vertex<V>) base_cluster.getBv();
		two_vertices.add(bu);
		two_vertices.add(bv);
		
		base_cluster.clusterInfo.setAllowedLocalAccess(false);

		return two_vertices; 
	}


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
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#expose(Vertex, Vertex)}. So the way from
	 * <code>u</code> to <code>v</code> is represented in top cluster. If it recognizes that given 
	 * vertices are not in common component then it returns <code>null</code>.
	 * <p>
	 * Next it prepares sons for first iteration of finding. Preparation is ensured by calling method
	 * {@link toptree.impl.TopTreeImpl#prepareRootForSelect()}. The method must remember original root
	 * of the tree before select for restoring the tree after select. If the tree consists from one
	 * edge, so the base contains the solution. Otherwise the finding starts.
	 * <p>
	 * Whole finding is done by method
	 * {@link toptree.impl.TopTreeImpl#selectStep(toptree.impl.TopTreeImpl.SonNodes, boolean)} with
	 * <code>true</code> value for second parameter. It returns the required base cluster.
	 * During rebuilding the original nodes are not deleted. They are set to be new and they refer
	 * to their original sons while the sons refer to their auxiliary parental nodes.
	 * <p>
	 * When base cluster is found then the tree is restored by method
	 * {@link toptree.impl.TopTreeImpl#restoreTreeAfterSelect(ClusterNode, ClusterNode)} and 
	 * both vertices from base cluster are returned in array.
	 * 
	 * @param u	A vertex that is one of end-points of required way for selecting.
	 * @param v	A vertex that is second end-points of required way for selecting.
	 * @return	Vertices that creates a base cluster that is result of selecting or <code>null</code>
	 * 			if the given vertices are not in the same component.
	 */
	public ArrayList<Vertex<V>> select(Vertex<V> u, Vertex<V> v) {
	
		// expose v first to annul expose in other component - if any was 
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != expose(u, v)) {
			return null;
		} 
		assert getTopComponent(v) != null && getTopComponent(u) == getTopComponent(v);
	
		// remember a top of original top tree for rebuilding back to original state
		origTop = ((ClusterInfo) getTopComponent(v)).getCn();
		
		// prepare root of top tree for select - create two clusters or one base if tree is one edge
		SonNodes prepared_root = prepareRootForSelect();
		
		// remember a top of original top tree in originalTop - into origTop will be saved auxiliary top
		ClusterNode originalTop = origTop;
		
		// find base cluster that is solution of user defined algorithm
		ClusterNode base_cluster = null;
		if (prepared_root.size() == 1) {
			base_cluster = prepared_root.left;
		} else {
			base_cluster = selectStep(prepared_root, true);
			restoreTreeAfterSelect(origTop, originalTop);
			origTop = null;
		}
	
		// take both vertices form base cluster and return them in array
		base_cluster.clusterInfo.setAllowedLocalAccess(true);
		
		assert base_cluster.isBase();
		ArrayList<Vertex<V>> two_vertices = new ArrayList<Vertex<V>>(2);
		Vertex<V> bu = (Vertex<V>) base_cluster.getBu();
		Vertex<V> bv = (Vertex<V>) base_cluster.getBv();
		two_vertices.add(bu);
		two_vertices.add(bv);
		
		base_cluster.clusterInfo.setAllowedLocalAccess(false);
	
		return two_vertices; 
	}

}
