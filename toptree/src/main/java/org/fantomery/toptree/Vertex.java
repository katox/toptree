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
 * An interface for representing one vertex of the TopTree and for working 
 * with it's properties.
 * <p>
 * In this Vertex object there are hold user defined information for this vertex in one user defined
 * object (for example weight of vertex) and degree of vertex. This object also provides methods
 * for manage these information.
 * 
 * @author  Kamil Toman
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 * 
 * @param <V> A user defined object with information about the vertex.
 */
public interface Vertex<V> {

	/**
	 * A method for obtaining user defined information about this vertex.
	 * <p>
	 * Information is returned in user defined object.
	 * 
	 * @return User defined object with information about this vertex.
	 */
	public abstract V getInfo();
	
	
    /**
     * A method for setting new user defined information to this vertex.
     * <p>
     * Information is taken over in user defined object.
     * 
     * @param info User defined object with new information.
     */
	public abstract void setInfo(V info);


	/**
	 * A method for getting the degree of this vertex.
	 * 
	 * @return Value of degree of this vertex.
	 */
	public abstract int getDegree();

}
