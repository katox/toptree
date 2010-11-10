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

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteNumber;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;
import org.fantomery.ttinterpreter.ttlangs.tql.TQLValue.Key;
import org.fantomery.toptree.TopTree.ExposeTwoResults;


/**
 * This class encapsulates all semantic exceptions thrown during execution of TQL commands.
 * <p>
 * All methods just checks if some condition holds or not and then it throws exception or not.
 * This approach is used to have all texts of exceptions well-arranged on one place.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLChecker {

	
	/**
	 * An empty constructor.
	 */
	private TQLChecker() {
	}
	
	
	/**
	 * This method checks differentness of both arguments and if they are equal then the exception is thrown.
	 * 
	 * @param name A name of checked field.
	 * @param unique_vertex_field A name of unique identifier.
	 * @throws TQLException If both names are equal.
	 */
	protected static void checkFieldNotUnique(String name, String unique_vertex_field) throws TQLException {
		// checks that given name is different from name of the node id
		if (name.matches(unique_vertex_field)) {
			throw new TQLException("A field '" + name + "' is unique vertex identifier. "
					+ "It can't be used here.");
		}
	}


	/**
	 * This method checks value of <code>exists</code> and if it is <code>false</code> then it
	 * throws an exception.
	 * 
	 * @param name A name of checked field.
	 * @param exists A switcher that says if the name of field exists or not.
	 * @throws TQLException If value of <code>exists</code> is <code>false</code>.
	 */
	protected static void checkFieldExistence(String name, boolean exists) throws TQLException {
		// if value of exists if false then throw exception
		if (!exists) {
			throw new TQLException("A field '" + name + "' doesn't exist.");
		}
	}

	
	/**
	 * This method checks differentness of both arguments and if they are equal then the exception is thrown.
	 * 
	 * @param name A name of checked field.
	 * @param used_name A name of field that is already used.
	 * @throws TQLException If both names are equal.
	 */
	protected static void checkDifferentNames(String name, String used_name) throws TQLException {
		// if both strings are same then throw exception
		if (name.matches(used_name)) {
			throw new TQLException("A field '" + name + "' is used more than once.");
		}
	}

	
	/**
	 * This method checks if the field is in the array of initialized fields and if it is then the exception
	 * is thrown.
	 * 
	 * @param name A name of checked field.
	 * @param res_init_vals An array of initialized fields.
	 * @throws TQLException If the field is in the array of initialized fields.
	 */
	protected static void checkFieldUsageInDeclarXorInit(String name, ArrayList<TQLValue> res_init_vals)
	throws TQLException {
		// if value list contains an value with given name then throw exception
		for (TQLValue value : res_init_vals) {
			if (name.matches(value.getName())) {
				throw new TQLException("A field '" + name
						+ "' can be used either in declaration or in initialization - but not in both.");
			}
		}
		
	}

	
	/**
	 * This method checks if the type of checked field is equal to the expected type and if it isn't then 
	 * the exception is thrown.
	 * 
	 * @param name A name of checked field.
	 * @param value A value of checked field.
	 * @param type A type of checked field.
	 * @param field_type An expected type.
	 * @throws TQLException If both types are different.
	 */
	protected static void checkFieldType(String name, String value, Class<?> type, Class<?> field_type)
	throws TQLException {
		// both given classes must be same
		if (type != field_type) {
			throw new TQLException("A type of a field '" + name + "'(" + value + ") is '"
					+ type.getSimpleName() + "', but type '" + field_type.getSimpleName()
					+ "' is expected.");
		}
	}


	/**
	 * This method checks if both keys are different and they are not then the exception is thrown.
	 * 
	 * @param name A name of checked array field.
	 * @param key A key of the field.
	 * @param used_key A key that was used already.
	 * @throws TQLException If both keys are equal.
	 */
	protected static void checkUsageOfArray(String name, Key key, Key used_key)
	throws TQLException {
		/* It is not necessary to check the count of usage. We have only two kinds of key, so it is
		 * enough to check that each kind of the key is used at most once. */
		assert key != null && used_key != null;
		if (key == used_key) {
			throw new TQLException("An array field '" + name + "' is used twice with same key '"
					+ key + "'.");
		}
	}


	/**
	 * This method checks if the fields should be an array or not according to the cluster field
	 * information. If it is the array and if should not be or it isn't and it should be then the exception
	 * is thrown. 
	 * 
	 * @param name A name of checked field.
	 * @param is_array A switcher that says if the field is array or not.
	 * @param cluster_field Information about the cluster field.
	 * @throws TQLException If it is the array and if should not be or it isn't and it should be.
	 */
	protected static void checkArray(String name, Boolean is_array,	TQLClusterField cluster_field)
	throws TQLException {
		// if field in cluster is an array then the variable must be field too
		if (cluster_field.isArray() && !is_array) {
			throw new TQLException("A field '" + name + "' is an array.");
		}
		// if field in cluster isn't an array then the variable can't be field
		if (!cluster_field.isArray() && is_array) {
			throw new TQLException("A field '" + name + "' is not an array.");
		}
	}


	/**
	 * This method checks an existence of the vertex identifier and if it already exists then the exception
	 * is thrown.
	 * 
	 * @param unique_vertex_field A name of unique vertex identifier.
	 * @param value A value of vertex identifier.
	 * @param vertices An array of all vertices.
	 * @param hash A hash table from vertex identifiers into the array of all vertices.
	 * @throws TQLException If the identifier already exists.
	 */
	protected static void checkNodeNonExistence(String unique_vertex_field, TQLValue value,
			ArrayList<?> vertices, TQLHash hash)
	throws TQLException {
		// just look if id that represents new node is contained in hash table
		if (hash.contains(value.getValue())) {
			throw new TQLException("A node '" + value.toString() + "' already exists.");
		}
	}


	/**
	 * This method checks for numerical value of vertex identifier if it is finite. It throws the exception
	 * if it is not finite.
	 * 
	 * @param unique_vertex_field A name of unique vertex identifier.
	 * @param value A value of vertex identifier.
	 * @throws TQLException If the value is numerical and it is not finite.
	 */
	protected static void checkAllowedValueOfNodeId(String unique_vertex_field, TQLValue value)
	throws TQLException {
		// take class of checked value
		Class<?> type = value.getType();
		assert type != null;
		
		// if node id is InfiniteInteger or InfiniteReal then it has to have finite value
		if (type == InfiniteInteger.class || type == InfiniteReal.class) {
			if (!((InfiniteNumber<?>)value.getValue()).isFinite()) {
				throw new TQLException("A value of the unique node field '" + unique_vertex_field + "' is '"
						+ value.toString() + "', but only finite values are accepted here.");
			}
		}
	}

	
	/**
	 * This method compares a count of declared variables and a count of values and if they are different
	 * if throws the exception.
	 * 
	 * @param variables_count A count of declared variables.
	 * @param values_count A count of values.
	 * @throws TQLException A both counts are different.
	 */
	protected static void checkSameSizeOfLists(int variables_count, int values_count) throws TQLException {
		// compare both values and if they are different then throw exception
		if (variables_count != values_count) {
			throw new TQLException("A count of values is different from the count of declared variables.");
		}
	}


	/**
	 * This method checks the result of expose called on the two vertices and if they are in the same
	 * component then the exception is thrown. 
	 * 
	 * @param vertex1 A vertex.
	 * @param vertex2 A vertex.
	 * @param expose_result A result of expose over received two vertices.
	 * @throws TQLException If vertices are in the same component.
	 */
	protected static void checkNonExistenceOfEdge(String vertex1, String vertex2,
			ExposeTwoResults expose_result) throws TQLException {
		// if both vertices are in one component then throw exception
		if (expose_result == ExposeTwoResults.COMMON_COMPONENT) {
			throw new TQLException("There already exists a path between nodes '" + vertex1
					+ "' and '" + vertex2 + "'.");
		}
	}


	/**
	 * This method checks differentness of both vertices and if they are not different then the exception
	 * is thrown.
	 * 
	 * @param vertex1 A vertex.
	 * @param vertex2 A vertex.
	 * @throws TQLException If the vertices are one vertex.
	 */
	protected static void checkDifferentNodes(TQLValue vertex1, TQLValue vertex2) throws TQLException {
		// the types of both values are same
		assert vertex1.getType() == vertex2.getType();
		// so we can easily compare toString values independently of types
		if (vertex1.toString().matches(vertex2.toString())) {
			throw new TQLException("The edge can exist only between two different nodes.");
		}
	}


	/**
	 * This method checks the result of expose called on the two vertices and if they are not in the same
	 * component then the exception is thrown. 
	 * 
	 * @param vertex1 A vertex name.
	 * @param vertex2 A vertex name.
	 * @param expose_result A result of expose over received two vertices.
	 * @throws TQLException If vertices are not in the same component.
	 */
	protected static void checkExistenceOfPath(String vertex1, String vertex2,
			ExposeTwoResults expose_result) throws TQLException {
		// if both vertices are not in one component then throw exception
		if (expose_result != ExposeTwoResults.COMMON_COMPONENT) {
			throw new TQLException("There is no edge between nodes '" + vertex1	+ "' and '"
					+ vertex2 + "'.");
		}
	}


	/**
	 * This method checks an existence of the vertex in the hash table and if it doesn't exist then
	 * the exception is thrown.
	 * 
	 * @param vertex A vertex.
	 * @param hash A hash table from vertex identifiers into the array of all vertices.
	 * @throws TQLException If the vertex doesn't exists in the hash table.
	 */
	protected static void checkExistenceOfNode(TQLValue vertex, TQLHash hash) throws TQLException {
		// just look if value of vertex is used as key in hash
		if (!hash.contains(vertex.getValue())) {
			throw new TQLException("The node '" + vertex.toString()	+ "' doesn't exist.");
		}
		
	}


	/**
	 * This method check existence of the function and if it doesn't exists then the exception is thrown.
	 * 
	 * @param func_name A name of the function.
	 * @param exists A switcher that says if the function exists or not.
	 * @throws TQLException If the function doesn't exist.
	 */
	protected static void checkFunctionExistence(String func_name, boolean exists) throws TQLException {
		// if value of exists if false then throw exception
		if (!exists) {
			throw new TQLException("Function '" + func_name + "' doesn't exist.");
		}
	}


	/**
	 * This method checks if types of parameters received from the command are same as expected types and 
	 * if they are not then the exception is thrown.
	 * 
	 * @param func_params An array of parameters received from the command.
	 * @param met_params An array of expected parameter types.
	 * @throws TQLException If parameter types are different from expected types.
	 */
	protected static void checkFunctionParameterCount(ArrayList<TQLValue> func_params,
			Class<?>[] met_params)
	throws TQLException {
		// compare both values and if they are different then throw exception
		int func_params_size = func_params.size() + 2;
		int met_params_size = met_params.length;
		
		if (func_params_size < met_params_size) {
			throw new TQLException("A count of parameters is lower then the function expects." );
		}
		else if (func_params_size > met_params_size 
				&& met_params[met_params_size-1] != Object[].class
				&& met_params[met_params_size-1] != InfiniteNumber[].class
				&& met_params[met_params_size-1] != InfiniteInteger[].class
				&& met_params[met_params_size-1] != InfiniteReal[].class
				&& met_params[met_params_size-1] != String[].class
				&& met_params[met_params_size-1] != Boolean[].class) {
			throw new TQLException("A count of parameters is greater then the function expects." );
		}
	}


	/**
	 * This method checks if the parameter type is the same as the expected type and if it is not then 
	 * the exception is thrown.
	 * 
	 * @param param_order A position of parameter from the command.
	 * @param value A value of the parameter.
	 * @param param_type A type of the parameter.
	 * @param met_type An expected type of the parameter.
	 * @throws TQLException If the parameter type is different from the expected type.
	 */
	protected static void checkFunctionParameterType(int param_order, String value,	Class<?> param_type,
			Class<?> met_type) throws TQLException {
		// if type of parameter is different from type declared by method then throw the exception
		if (met_type == InfiniteNumber.class && param_type != InfiniteInteger.class
				&& param_type != InfiniteReal.class) {
			throw new TQLException("Parameter '" + value + "' on position " + param_order
					+ " has type '" + param_type.getSimpleName() + "', but numerical type is expected.");
		}
		else if (met_type != Object.class && param_type != met_type) {
			throw new TQLException("Parameter '" + value + "' on position " + param_order
					+ " has type '" + param_type.getSimpleName() + "', but type '"
					+ met_type.getSimpleName() + "' is expected.");
		}
		
	}

	
	/**
	 * This method checks if the field is used and it throws the exception if it isn't.
	 * 
	 * @param field_name A name of the field.
	 * @param used A switcher that says if the field is used or not.
	 * @throws TQLException If the field is not used.
	 */
	protected static void checkClassFieldsUsed(String field_name, boolean used) throws TQLException {
		// if not used then throw exception
		if (!used) {
			throw new TQLException("A field '" + field_name + "' is not used.");
		}
	}

	
	/**
	 * This method checks usage of both keys and if any is not used then the exception is thrown.
	 * 
	 * @param field_name A name of an array field.
	 * @param left_used A switcher that says if the left key of the field is used or not.
	 * @param right_used A switcher that says if the right key of the field is used or not.
	 * @throws TQLException If any of keys is not used.
	 */
	protected static void checkArrayClassFieldsUsed(String field_name, boolean left_used,
			boolean right_used) throws TQLException {
		// if any of array keys not used then throw exception
		if (!left_used) {
			throw new TQLException("A field '" + field_name + "[L]' is not used.");
		}
		if (!right_used) {
			throw new TQLException("A field '" + field_name + "[R]' is not used.");
		}
	}

}
