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
 * This class represents a state of the Top Tree between some rebuilding has finished and
 * user defined information were not counted from old cluster nodes to new cluster nodes.
 * <p>
 * It remembers the top cluster node of the sequence of old cluster nodes and the top of
 * the rebuilt Top Tree.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TopIntermediate {

	/**
	 * This field holds the root the sequence of old (that is dirty or obsolete) cluster nodes. 
	 */
	ClusterNode origRoot;
	
	/**
	 * This field only holds new root cluster node of whole Top Tree.
	 */
	ClusterNode newRoot;


	/**
	 * This constructor creates an instance of the class and sets both old and new root fields
	 * to given values. 
	 * 
	 * @param origRoot A root of the sequence of old cluster nodes.
	 * @param newRoot A root of the rebuilt Top Tree.
	 */
	public TopIntermediate(ClusterNode origRoot, ClusterNode newRoot) {
		this.origRoot = origRoot;	// the root of old nodes
		this.newRoot = newRoot;		// the root of the top tree after rebuilding
	}

}
