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

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;

/**
 * This class represents values used in TQL commands.
 * <p>
 * Each value stores information about type of the value and the value itself. If a vertex/cluster field
 * whose value is represented is known then there is stored a name of the field and a key if the field
 * is an array.
 * <p>
 * The class provides method for getting and setting stored information. It is not possible to change
 * the value and the data type. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLValue {

	/**
	 * This enumeration type represents keys of arrays used in TQL. There are only two keys - a left
	 * boundary vertex and a right boundary vertex.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since 1.0
	 */
	protected enum Key {
		
		/** Represents left boundary vertex. */
		L,
		
		/** Represents right boundary vertex. */
		R
	}
	
	
	/** A name of a field whose value is represented. */
	private String name;
	
	/** A key of an array field whose value is represented. */
	private Key key;
	
	/** A data type of represented value: InfiniteInteger, InfiniteReal, String or Boolean. */
	private Class<?> type;
	
	/** Represented value - allowed type: InfiniteInteger, InfiniteReal, String and Boolean */
	private Object value;
	
	
	/**
	 * The constructor sets data type of received value and value of new instance, name and key are null.
	 * <p>
	 * It is expected that data type of the value is InfiniteInteger, InfiniteReal, String or Boolean.
	 * 
	 * @param value Represented value - allowed type: InfiniteInteger, InfiniteReal, String and Boolean.
	 */
	protected TQLValue(Object value) {
		this.name = null;
		this.type = value.getClass();
		this.value = value;
		this.key = null;
		assert this.type == InfiniteInteger.class 	|| this.type == InfiniteReal.class
			|| this.type == String.class 			|| this.type == Boolean.class;
	}
	
	
	/**
	 * This method returns a data type of represented value
	 * 
	 * @return A data type of the value.
	 */
	protected Class<?> getType() {
		return this.type;
	}
	
	
	/**
	 * This method returns a key. It is null if the value responds to non-array field. 
	 * 
	 * @return A key of the field value. It is null if the value responds to non-array field. 
	 */
	protected Key getKey() {
		return this.key;
	}
	
	
	/**
	 * This method returns <code>true</code> if a field whose value is represented here is an array. 
	 * Else it returns <code>false</code>.
	 * <p>
	 * The method checks value of <code>key</code>. If it is null then the field is not array. 
	 * 
	 * @return If field is array then <code>true</code> else <code>false</code>. 
	 */
	protected boolean isArray() {
		return this.key != null;
	}
	
	
	/**
	 * This method sets not null key in this instance of the class.
	 * 
	 * @param key Not null key of the array field.
	 */
	protected void setKey(Key key) {
		assert key != null;
		this.key = key;
	}
	
	
	/**
	 * This method returns a name of the field whose value is represented. It can be null if the instance
	 * stores only some value without belonging to some vertex/cluster field.
	 * 
	 * @return A name of the field whose value is represented.
	 */
	protected String getName() {
		return this.name;
	}
	
	
	/**
	 * This method sets new name of the field whose value is represented.
	 * 
	 * @param name A name of the field.
	 */
	protected void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * This method returns a value represented in the instance.
	 * 
	 * @return A value represented in the instance.
	 */
	protected Object getValue() {
		return this.value;
	}

	
	/**
	 * This method returns string that represents <code>value</code>.
	 */
	public String toString() {
		return this.value.toString();
	}

}
