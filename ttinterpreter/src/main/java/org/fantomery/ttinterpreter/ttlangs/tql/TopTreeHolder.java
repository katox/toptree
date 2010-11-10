/*
 *  Top Tree Query Language Implementation
 * 
 *  The package ttlangs.tql contains implementation of Top Tree Query Language. This query language
 *  was developed specially for Top Tree administration. It contains some basic commands and it is
 *  possible to extend abilities of TQL by function adding.
 *  
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
 *  Developed by:	Michal Vajbar
 *  				Charles University in Prague
 *  				Faculty of Mathematics and Physics
 *					michal.vajbar@tiscali.cz
 */
package org.fantomery.ttinterpreter.ttlangs.tql;

import java.util.ArrayList;

import org.fantomery.toptree.TopTree;
import org.fantomery.toptree.TopTreeListener;
import org.fantomery.toptree.Vertex;


/**
 * This class represents top tree over that TQL runs. A descendant of this abstract class is generated
 * when TQL obtains information about vertex and cluster info classes and about algorithm class. Then the
 * class of the descendant is compiled in runtime and an instance is created.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 *
 * @param <C> A type of the cluster info object.
 * @param <V> A type of the vertex info object
 */
public abstract class TopTreeHolder<C, V> {
	
	/** This field is a handler of an algorithm class instance. */
	protected TopTreeListener<C, V> handler;
	
	/** A link to the top tree. */
	protected TopTree<C, V> top;
	
	/** A list of all vertices used in top tree. */
	protected ArrayList<Vertex<V>> vertices;
}
