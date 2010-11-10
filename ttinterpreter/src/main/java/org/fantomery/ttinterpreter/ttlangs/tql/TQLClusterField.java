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

/**
 * This class represents cluster field in declaration of edge creation in TQL.
 * <p>
 * It only holds and gets information about a data type of the field and if the field is an array.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLClusterField {

	/** A data type of the field. */
	private Class<?> type;

	/** An option if the field is array. */
	private boolean is_array;
	
	
	/**
	 * This constructor initializes the type and the array option of new class instance. 
	 * 
	 * @param type A data type of the field.
	 * @param is_array An option if the field is array.
	 */
	protected TQLClusterField(Class<?> type, boolean is_array) {
		this.type = type;
		this.is_array = is_array;
	}
	
	
	/**
	 * This method returns <code>true</code> if the field is array and <code>false</code> if it is not.
	 * 
	 * @return If the field is array then <code>true</code> else <code>false</code>.
	 */
	protected boolean isArray() {
		return this.is_array;
	}
	
	
	/**
	 * This method returns a data type of the field.
	 * 
	 * @return A data type of the field.
	 */
	protected Class<?> getType() {
		return this.type;
	}
	
}
