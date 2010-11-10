/*
 *  Top Tree Languages Implementation
 * 
 *  The package ttlangs is a root of packages ttlangs.tfl and ttlangs.tql that represent
 *  Top Tree Friendly Language (TFL) and Top Tree Query Language (TQL). There is only one class in this
 *  package. It provides tools for method names composing in TFL and TQL.
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
package org.fantomery.ttinterpreter.ttlangs;


/**
 * This class represents types of method names that are used for dynamically created methods in VertexInfo
 * and ClusterInfo classes. Methods are generated for class fields during compilation of Top Tree Functional
 * Language to Java language.
 * <p>
 * The class contains one enumeration data type that represents all types of method names. There is also
 * a method for putting the name together.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class TFLMethodNameCreator {

	
	/**
	 * A list of types of generated methods for fields. There are three types of methods - for getting,
	 * or setting a value and for adding a value to the field current value.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	public enum MethodTypes {
		
		/**
		 * A field for methods that returns a field value.
		 */
		GET,
		
		/**
		 * A field for methods that sets new value into a field.
		 */
		SET,
		
		/**
		 * A field for methods that adds a number to a numeric field.
		 */
		ADD
	}
	
	
	/**
	 * An empty constructor.
	 */
	private TFLMethodNameCreator() {
	}
	
	
	/**
	 * This method creates and returns a name of method from <code>field_name</code> according
	 * to <code>type</code> field.
	 * 
	 * @param type A type of method {@link ttlangs.TFLMethodNameCreator.MethodTypes}.
	 * @param field_name A name of a field.
	 * @return A name of method created according to <code>type</code> field.
	 */
	public static String createMethodName(MethodTypes type, String field_name) {
		
		String method_name = "";
		// create a beginning of string according to type
		switch (type) { 
		case GET:
			method_name = "get";
			break;
		case SET:
			method_name = "set";
			break;
		case ADD:
			method_name = "addTo";
			break;
		default:
			assert false;
		}
		// create second part of name and return it
		return method_name + field_name.substring(0, 1).toUpperCase() + field_name.substring(1);
	}

}
