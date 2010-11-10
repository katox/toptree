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

import java.util.HashMap;

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;


/**
 * This class is a tool for hashing from <code>Integer</code>, <code>String</code> or <code>Double</code>
 * into instances of a vertex class.
 * <p>
 * We hold vertices as values and keys are their unique identifiers. At the moment of compilation 
 * of this Java source the type of keys is not known (it will one of mentioned types). So we can't use
 * hash-map because then the key is Object. But we need to use a value of String, Integer or Double object.
 * This is the reason to create two-level addressing. The first level is this class that represents mapping
 * from any of mentioned three types to Integers. Second level is an array-list: <code>int</code> is primitive
 * key (value in Integers of first level) and type of values is the type of the vertex class.
 * <p>
 * This class contains three prepared types of hash-map. Only difference is a type of the key: String,
 * Integer and Double. When an instance is created it remembers the type of the key and then it always
 * knows which type of hash-map must be used.
 * <p>
 * The class provides methods for mapping to new inserted vertex, getting Integer key for second level
 * according to the unique identifier and method for finding if any mapping exists for requested unique
 * identifier.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLHash {
	
	/** A type of unique vertex identifier. */
	private Class<?> type;
	
	/** A count of used identifiers (size of the hash-map). */
	private int size;
	
	/** A mapping prepared for Integer keys. */
	private HashMap<Integer, Integer> table_integer;
	
	/** A mapping prepared for Double keys. */
	private HashMap<Double,  Integer> table_double;
	
	/** A mapping prepared for String keys. */
	private HashMap<String,  Integer> table_string;
	
	
	/**
	 * This constructor creates empty hash-map of the required type.
	 * 
	 * @param type A type of the unique vertex identifier.
	 */
	protected TQLHash(Class<?> type) {
		// save type and initialize required type of hash-map
		this.type = type;
		this.size = 0;
		if (type == InfiniteInteger.class) {
			table_integer = new HashMap<Integer, Integer>();
		} else if (type == InfiniteReal.class) {
			table_double = new HashMap<Double, Integer>();
		} else {
			assert type == String.class;
			table_string = new HashMap<String, Integer>();
		}
	}
	
	
	/**
	 * This method inserts new unique identifier into hashing table and generated and returns appropriate
	 * key for second level.
	 * 
	 * @param key A key of the first level of mapping.
	 * @return A key for second level of mapping.
	 */
	protected int put(Object key) {
		// insert new hash-mapping according to the type of key
		if (type == InfiniteInteger.class) {
			this.table_integer.put(((InfiniteInteger) key).getFiniteValue(), this.size);
		} else if (type == InfiniteReal.class) {
			this.table_double.put(((InfiniteReal) key).getFiniteValue(), this.size);
		} else {
			assert type == String.class;
			this.table_string.put((String) key, this.size);
		}
		// return the value and increase size
		return this.size++;
	}
	
	
	/**
	 * This method finds and returns second level key according to the first level key.
	 * 
	 * @param key A key of the first level of mapping.
	 * @return A key for second level of mapping.
	 */
	protected int get(Object key) {
		// according to the key type returns appropriate second level key
		if (type == InfiniteInteger.class) {
			return this.table_integer.get(((InfiniteInteger) key).getFiniteValue());
		} else if (type == InfiniteReal.class) {
			return this.table_double.get(((InfiniteReal) key).getFiniteValue());
		} else {
			assert type == String.class;
			return this.table_string.get((String) key);
		}
	}
	
	
	/**
	 * This method check existence of mapping for received unique vertex identifier.
	 * 
	 * @param key A key of the first level of mapping.
	 * @return If the key exists then <code>true</code> else <code>false</code> value.
	 */
	protected boolean contains(Object key) {
		// according to the key type check existence of the key
		if (type == InfiniteInteger.class) {
			return this.table_integer.containsKey(((InfiniteInteger) key).getFiniteValue());
		} else if (type == InfiniteReal.class) {
			return this.table_double.containsKey(((InfiniteReal) key).getFiniteValue());
		} else {
			assert type == String.class;
			return this.table_string.containsKey((String) key);
		}
	}
	
}
