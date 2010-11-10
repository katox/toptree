/*
 *  Top Tree Friendly Language Implementation
 * 
 *  The package ttlangs.tfl contains implementation of Top Tree Friendly Language. This programming
 *  language was developed specially for easy designing of algorithms over Top Trees.
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
package org.fantomery.ttinterpreter.ttlangs.tfl;

import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.DataType;


/**
 * This class encapsulates string constants that are used for initializing of all kinds of data types used
 * in Java code generated from Top Tree Functional Language.
 * <p>
 * The class contains a few string constants and a static method that returns values of these constants.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TFLImplicitFieldValues {

	
	/**
	 * A field that holds Java code used for initialization of 
	 * {@link numbers.InfiniteInteger} data type.
	 */
	private final static String type_integer = "new InfiniteInteger(0)";

	
	/**
	 * A field that holds Java code used for initialization of 
	 * {@link numbers.InfiniteReal} data type.
	 */
	private final static String type_real = "new InfiniteReal(0.0)";

	
	/**
	 * A field that holds Java code used for initialization of {@link java.lang.String} data type.
	 */
	private final static String type_string	 = "\"\"";

	
	/**
	 * A field that holds Java code used for initialization of {@link java.lang.Boolean} data type.
	 */
	private final static String type_boolean = "false";

	
	/**
	 * A field that holds Java code used for initialization of HashMap with some key (type will by received
	 * in appropriate method) and values of {@link numbers.InfiniteInteger} data type.
	 */
	private final static String[] type_array_integer	= {"new HashMap<", ", InfiniteInteger>()"};

	
	/**
	 * A field that holds Java code used for initialization of HashMap with some key (type will by received
	 * in appropriate method) and values of {@link numbers.InfiniteReal} data type.
	 */
	private final static String[] type_array_real = {"new HashMap<", ", InfiniteReal>()"};

	
	/**
	 * A field that holds Java code used for initialization of HashMap with some key (type will by received
	 * in appropriate method) and values of {@link java.lang.String} data type.
	 */
	private final static String[] type_array_string = {"new HashMap<", ", String>()"};

	
	/**
	 * A field that holds Java code used for initialization of HashMap with some key (type will by received
	 * in appropriate method) and values of {@link java.lang.Boolean} data type.
	 */
	private final static String[] type_array_boolean = {"new HashMap<", ", Boolean>()"};
	
	
	/**
	 * An empty constructor.
	 */
	private TFLImplicitFieldValues() {
	}


	/**
	 * A method that obtains a data type and name of VertexInfo class used in generated Java code and returns
	 * implicit initialization value for given data type.
	 * <p>
	 * The parameter <code>vertex_class_name</code> is used only for data type that generates HashMap
	 * initialization. But it is recommended to give this parameter with not null value always.
	 * 
	 * @param type A data type that determines returned initialization string.
	 * @param vertex_class_name A name of VertexInfo class used in generated Java code.
	 * @return An implicit initialization Java code.
	 */
	protected static String getImplicitValue(DataType type, String vertex_class_name) {
		// according to type return appropriate string - if HashMap that insert name of key type
		switch (type) {
		case INTEGER:
			return type_integer;
		case REAL:
			return type_real;
		case STRING:
			return type_string;
		case BOOLEAN:
			return type_boolean;
		case ARRAY_INTEGER:
			return type_array_integer[0] + vertex_class_name + type_array_integer[1];
		case ARRAY_REAL:
			return type_array_real[0] + vertex_class_name + type_array_real[1];
		case ARRAY_STRING:
			return type_array_string[0] + vertex_class_name + type_array_string[1];
		default:
			assert type == DataType.ARRAY_BOOLEAN;
			return type_array_boolean[0] + vertex_class_name + type_array_boolean[1];
		}
	}
	
}
