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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.BuildingBlock;
import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.DataType;
import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.RootBlock;

/**
 * This class encapsulates all semantic exceptions thrown during compiling of TFL into Java.
 * <p>
 * All methods just checks if some condition holds or not and then it throws exception or not.
 * This approach is used to have all texts of exceptions well-arranged on one place.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TFLChecker {
	
	
	/**
	 * An empty constructor.
	 */
	private TFLChecker() {
	}
	
	/**
	 * This method checks value of <code>if_exists</code> and if it is <code>true</code> then it
	 * throws an exception.
	 * 
	 * @param name A name of checked variable.
	 * @param if_exists	A switcher that says if the name of variable exists or not.
	 * @param field_type A data type of the variable.
	 * @throws TFLException If the name exists.
	 */
	protected static void checkFieldUniqueness(String name, boolean if_exists, String field_type)
	throws TFLException {

		if (if_exists) {
			throw new TFLException("A name '" + name + "' of " + field_type
					+ " field isn't unique.");
		}
	}

	
	/**
	 * This method checks value of <code>is_initialized</code> and if it is <code>false</code> then it
	 * throws an exception.
	 * 
	 * @param var_name A name of checked variable.
	 * @param is_initialized A switcher that say if the variable is initialized or not.
	 * @throws TFLException If the variable is not initialized.
	 */
	protected static void checkInitializationOfVariable(String var_name, Boolean is_initialized)
	throws TFLException {

		if (!is_initialized) {
			throw new TFLException("A variable '" + var_name
					+ "' must be initialized before first usage.");
		}
	}

	
	/**
	 * This method checks if a data type of given variable is numerical. If not then an exception is thrown.
	 * 
	 * @param var_name A name of checked variable.
	 * @param type A data type of the variable.
	 * @throws TFLException If the variable is not numeric type.
	 */
	protected static void checkNumericTypeOfVariable(String var_name, DataType type)
	throws TFLException {

		if (type != DataType.INTEGER && type != DataType.REAL) {
			throw new TFLException("A data type of '" + var_name + "' is '" + type
					+ "', but numerical type is expected.");
		}
	}

    
	/**
	 * This method checks value of <code>array_exists</code> and if it is <code>false</code> then it
	 * throws an exception.
	 * 
	 * @param name A name of checked variable.
	 * @param array_exists A switcher that say if an array exists or not.
	 * @param field_type A data type of the variable.
	 * @throws TFLException If the variable doesn't exists.
	 */
	protected static void checkFieldExistence(String name, boolean array_exists, String field_type)
	throws TFLException {

		if (!array_exists) {
			throw new TFLException("A variable '" + name + "' of '" + field_type
					+ "' field doesn't exist.");
		}
	}

    
	/**
	 * This method checks if a data type of a variable is an array. If not then exception is thrown.
	 * 
	 * @param name A name of checked variable.
	 * @param type A data type of the variable.
	 * @throws TFLException If data type of variable is not an array.
	 */
	protected static void checkIfClusterFieldIsArray(String name, DataType type)
	throws TFLException {

		if (type != DataType.ARRAY_INTEGER && type != DataType.ARRAY_REAL
				&& type != DataType.ARRAY_STRING && type != DataType.ARRAY_BOOLEAN) {
			throw new TFLException("A variable '" + name
					+ "' of cluster field isn't declared as array.");
		}
	}
	
	
	/**
	 * This method checks if a data type of an expression is the same as <code>type</code>.
	 * If not then exception is thrown.
	 * 
	 * @param expr A checked expression.
	 * @param type A data type that is expected.
	 * @throws TFLException If a data type of the expression is different from given type.
	 */
	protected static void checkDataTypeOfExpression(TFLExpression expr, DataType type)
	throws TFLException {

		if (expr.getDataType() != type) {
			throw new TFLException("A data type of '" + expr.getSource() + "' is '"
					+ expr.getDataType() + "', but '" + type + "' is expected.");
		}
	}
	
	
	/**
	 * This method checks if a data type of an expression is numerical. If not then exception is thrown.
	 * 
	 * @param expr A checked expression.
	 * @throws TFLException If a data type of the expression is different from given type.
	 */
	protected static void checkNumericExpression(TFLExpression expr) throws TFLException {

		if (expr.getDataType() != DataType.INTEGER && expr.getDataType() != DataType.REAL) {
			throw new TFLException("A data type of '" + expr.getSource() + "' is '"
					+ expr.getDataType() + "', but numerical type is expected.");
		}
	}
	
	
	/**
	 * The method checks it the types of both expressions are same. If not then the exception is thrown.
	 * 
	 * @param expr1 A checked expression.
	 * @param expr2 A checked expression.
	 * @throws TFLException If data types of given expressions are different.
	 */
	protected static void checkSameTypeOfTwoExpressions(TFLExpression expr1, TFLExpression expr2)
	throws TFLException {

		if (expr1.getDataType() != expr2.getDataType() ) {
			throw new TFLException("A data type of '" + expr1.getSource() + "' is '"
					+ expr1.getDataType() + "' and a data type of '" + expr2.getSource() + "' is '"
					+ expr2.getDataType() + "'. It is expected both types are same.");
		}
	}


	/**
	 * This method checks if a block is free. The exception is thrown if a value of <code>is_free</code> is
	 * <code>false</code>.
	 * 
	 * @param name A name of block.
	 * @param is_free A switcher that says if the block is used or it is free.
	 * @throws TFLException If the block is not free.
	 */
	protected static void checkIfBuildingBlockIsFree(BuildingBlock name, boolean is_free)
	throws TFLException {

		if (!is_free) {
			throw new TFLException("A block '" + name + "' can be used in every of "
					+ "join and split block at most once.");
		}
	}


	/**
	 * This method checks according to regular expressions if given name of variable is allowed. If it
	 * doesn't match any of them, then the exception is thrown. 
	 * 
	 * @param var_name A name of checked variable.
	 * @param all_regexps Regular expressions that determines forms of variables that are allowed in given 
	 * 						block.
	 * @param method_name A name of root block.
	 * @param block_name A name of internal block of given root block.
	 * @param separators A list of characters that are used as separators in variables.
	 * @throws TFLException If the variable doesn't match any regular expression.
	 */
	protected static void checkVariableForm(String var_name, ArrayList<String> all_regexps,
			RootBlock method_name, BuildingBlock block_name, ArrayList<String> separators)
	throws TFLException {
		
		Pattern pattern = null;
		Matcher matcher = null;
		// if name matches same regexp then it is ok
		for (String regexp : all_regexps) {
			pattern = Pattern.compile(regexp);
	        matcher = pattern.matcher(var_name);
			if (matcher.matches()) {
				return;
			}
		}
		// No match => replace separator by dot and throw exception
		for (String sep : separators) {
			var_name = var_name.replace(sep, ".");
		}
		throw new TFLException("Method " + method_name + ", block " + block_name
				+ ": this form '" + var_name + "' of variable is not allowed here.");
	}

	
	/**
	 * This method checks where function exists is used in. If it is used in block PATH, POINT,
	 * PATH_CHILD or POINT_CHILD then the exception is thrown. 
	 * 
	 * @param method_name A name of root block.
	 * @param block_name A name of internal block of given root block.
	 * @throws TFLException If function EXISTS is used in denied block.
	 */
	protected static void checkExistsInBlock(RootBlock method_name,	BuildingBlock block_name)
	throws TFLException {
		// if exists is used in denied block then throw exception
		if (block_name == BuildingBlock.PATH
				|| block_name == BuildingBlock.POINT
				|| block_name == BuildingBlock.PATH_CHILD
				|| block_name == BuildingBlock.POINT_CHILD) {
			throw new TFLException("Method " + method_name + ", block " + block_name
					+ ": using of function 'EXISTS' is not allowed here.");
		}
	}

}
