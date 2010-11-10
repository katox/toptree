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

import org.fantomery.ttinterpreter.ttlangs.tql.TQLValue.Key;


/**
 * This class represents variables used in commands of TQL.
 * <p>
 * It stores a name of the variable and a key if the variable in an array. When an instance of the class
 * is created then it is not to change stored information. The class allows only to get information.
 * It is used typically for collection information about a variable during parsing the TQL command so
 * the changing is not required. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLVariable {
	
	/** This field represents not null key if the variable represent an array. Else it must be null.  */
	private Key key;

	/** A name of the variable. */
	private String name;
	
	
	/**
	 * This constructor sets a name of the variable that is not an array. Value of <code>key</code>
	 * is <code>null</code>.
	 * 
	 * @param name A name of the variable.
	 */
	protected TQLVariable(String name) {
		this.name = name;
		this.key = null;
	}

	
	/**
	 * This constructor sets a name of the variable that is an array and its <code>key</code>.
	 * 
	 * @param name A name of the variable.
	 * @param key A key of the array.
	 */
	protected TQLVariable(String name, Key key) {
		this.name = name;
		this.key = key;
	}

	
	/**
	 * This method looks if the value of <code>key</code> field is not <code>null</code>, then it returns
	 * <code>true</code> else <code>false</code>.
	 * 
	 * @return If the variable is an array then <code>true</code> else <code>false</code>.
	 */
	protected boolean isArray() {
		return this.key != null;
	}
	
	
	/**
	 * This method returns a name of the variable.
	 * 
	 * @return A name of the variable.
	 */
	protected String getName() {
		return this.name;
	}
	
	
	/**
	 * This method returns a key of the array variable. Before using this method it should be checked that
	 * the variable is an array ({@link ttlangs.tql.TQLVariable#isArray()}); 
	 * 
	 * @return A key of the array variable.
	 */
	protected Key getKey() {
		assert key != null;
		return this.key;
	}
	
}
