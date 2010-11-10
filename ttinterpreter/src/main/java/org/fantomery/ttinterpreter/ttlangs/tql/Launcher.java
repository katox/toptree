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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteNumber;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator.MethodTypes;
import org.fantomery.toptree.TopTree;
import org.fantomery.toptree.TopTreeListener;
import org.fantomery.toptree.Vertex;


/**
 * This class provides static tools for making initial routines on launching TQL Interpreter.
 * <p>
 * There are only static methods. It supports runtime compiling of Java classes, preparing of information
 * about vertex and cluster class fields for initializing of the interpreter, preparing and checking
 * of user functions used in TQL.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class Launcher {

	
	/**
	 * An empty constructor.
	 */
	private Launcher() {
	}
	
	
	/**
	 * This method compiles all Java files that it receives as argument and print information
	 * about the process to the standard output.
	 * <p>
	 * For compilation it uses method <code>com.sun.tools.javac.Main.compile(String[], PrintWriter)</code>
	 * from Java JDK 1.6.0_02 (tools.jar).
	 * 
	 * @param files Names of all Java files to compile.
	 * @throws TQLException If compilation fails.
	 */
	public static void CompileFiles(String... files) throws TQLException {
		// for each received path try to compile it
		for (String file_name : files) {
			System.out.print(file_name +": ");
			final String[] commandLine = new String[] { file_name };
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final PrintWriter pw = new PrintWriter(baos);
			// compilation
			int errorCode = com.sun.tools.javac.Main.compile(commandLine, pw);
			// check if compilation completed successfully
			pw.close();
			if(errorCode != 0) {
				throw new TQLException("Compilation errors: code=" + errorCode + ", Message="
						+ baos.toString());
			}
			else {
				System.out.println("Compilation successful");
			}
		}
	}
	
	
	/**
	 * This method loads data types of fields form vertex and cluster class and then creates new
	 * instance of TQLInterpreter.
	 * <p>
	 * It creates conversion hash-maps from field names to their data types and then just
	 * calls only constructor of {@link ttlangs.tql.TQLInterpreter}.
	 * 
	 * @param vertex_name Full package name of a vertex class.
	 * @param cluster_name Full package name of a cluster class.
	 * @param functions A conversion from supported functions names into their Method types.
	 * @param toptree_package A name of top tree package.
	 * @return New instance of {@link ttlangs.tql.TQLInterpreter}.
	 * @throws ClassNotFoundException If vertex or cluster class doesn't exist.
	 * @throws SecurityException 	If security manager throws it during loading fields of vertex and
	 * 								cluster classes.
	 * @throws NoSuchMethodException If doesn't exist get-method of any array class field.
	 */
	public static TQLInterpreter createInterpreter(String vertex_name, String cluster_name,
			HashMap<String, Method> functions, String toptree_package)
	throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		
		// read all fields of vertex class and save it with data type 
		Class<?> vertex_class = Class.forName(vertex_name);
		Field[] vertex_fields = vertex_class.getDeclaredFields();
		HashMap<String, Class<?>> vertex_hm = new HashMap<String, Class<?>>();
		for (Field field : vertex_fields) {
			vertex_hm.put(field.getName(), field.getType());
		}

		// read all fields of vertex class and save it with data type 
		Class<?> cluster_class = Class.forName(cluster_name);
		Field[] cluster_fields = cluster_class.getDeclaredFields();
		HashMap<String, TQLClusterField> cluster_hm = new HashMap<String, TQLClusterField>();
		for (Field field : cluster_fields) {
			String name = field.getName();
			Class<?> type = field.getType();
			boolean is_array = false;
			// if type is hash-map (=array in TFL) then we must find value type of this array
			if (type.getName().matches("java.util.HashMap")) {
				// read value type of the array from its getter
				Method getter = Class.forName(cluster_name).getMethod(
						TFLMethodNameCreator.createMethodName(MethodTypes.GET, name),
						Class.forName(vertex_name));
				// save new type and remember that this was array
				type = getter.getReturnType();
				is_array = true;
			}
			// save field name, data type and information if it is an array
			cluster_hm.put(name, new TQLClusterField(type, is_array));
		}
		
		// create and return new TQLInterpreter
		return new TQLInterpreter(vertex_hm, cluster_hm, vertex_class, cluster_class, functions,
				toptree_package);
	}


	/**
	 * This method checks format of user defined functions for TQL and creates a conversion from
	 * supported functions names into their Method types.
	 * <p>
	 * Each method must satisfy:
	 * <ul>
	 *   <li>name uniqueness</li>
	 *   <li>method returns String and is static - else it is ignored</li>
	 *   <li>first argument's type is <code>toptree.TopTree</code></li>
	 *   <li>second argument must implement <code>toptree.TopTreeListener</code> interface</li>
	 *   <li>third argument's type is <code>toptree.Vertex</code></li>
	 *   <li>all other <code>toptree.Vertex</code> argument must be in row behind third argument</li>
	 *   <li>after the row of <code>toptree.Vertex</code> arguments there can be a row of arguments
	 *       with these types: Object, InfiniteNumber, InfiniteInteger, InfiniteReal, String, Boolean</li>
	 *   <li>last argument can have arbitrary number of arguments</li>
	 * </ul>
	 * Non-public methods are ignored.
	 * 
	 * @param functions_package A name of functions's package.
	 * @param functions_name A name of functions's class.
	 * @return A conversion from supported functions names into their Method types.
	 * @throws ClassNotFoundException If class with functions doesn't exist.
	 * @throws TQLException If any method doesn't pass checking.
	 */
	public static HashMap<String, Method> CheckFunctions(String functions_package, String functions_name)
	throws ClassNotFoundException, TQLException {
		
		// read all declared methods from class where functions are
		Class<?> type = Class.forName(functions_package + "." + functions_name);
		Method[] methods = type.getDeclaredMethods();
		
		// storage of checked public declared methods
		HashMap<String, Method> functions = new HashMap<String, Method>();
		
		
		// check all methods and then insert it into storage
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			
			// if any method is not public then jump it over
			if (!Modifier.isPublic(method.getModifiers()))
				continue;
			
			// if any method is not static then jump it over
			if (!Modifier.isStatic(method.getModifiers()))
				continue;
			
			// names of public methods must be unique
			String name = method.getName();
			for (int j = i + 1; j < methods.length; j++) {
				if (!Modifier.isPublic(methods[j].getModifiers()))
					continue;
				if (name.matches(methods[j].getName())) {
					throw new TQLException("Checking of '" + functions_name	+ "': "
						+ "there are two methods using name '" + name + "'.");
				}
			}
			
			// only allowed return type is String
			if (method.getReturnType() != String.class) {
				throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
						+ " return type is '" + method.getReturnType().getName()
						+ "', but only '" + String.class.getName() + "' allowed.");
			}
			
			// check parameters of method
			Class<?>[] params = method.getParameterTypes();
			// there must be at least 3 parameters
			if (params.length < 3) {
				throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
						+ " has less then 3 parameters.");
			}
			// first parameter is an instance of TopTree interface
			if (params[0] != TopTree.class) {
				throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
						+ " type of parameter 1 is '" + params[0].getName()
						+ "', but only '" + TopTree.class.getName() + "' allowed.");
			}
			// second parameter must be an instance of TopTreeListener class
			if (params[1] != TopTreeListener.class) {
				throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
						+ " type of parametr 2 is '" + method.getReturnType().getName()
						+ "', but only '" + Object.class.getName() + "' allowed.");
			}
			// third parameter is an instance of Vertex interface
			if (params[2] != Vertex.class) {
				throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
						+ " type of parametr 3 is '" + method.getReturnType().getName()
						+ "', but only '" + Vertex.class.getName() + "' allowed.");
			}
			int cursor = 3;
			// jump over parameters with type Vertex
			while (cursor < params.length && params[cursor] == Vertex.class) {
				cursor++;
			}
			/* All parameters behind Vertices can be only same type (see next if). So check it. */
			if (cursor < params.length) {
				for (int j = cursor; j < params.length; j++) {
					Class<?> param_class = params[j];
					// if type is not one of follows then it is possible that exception occures
					boolean throw_exception = false;
					if (param_class != Object.class
							&& param_class != InfiniteNumber.class
							&& param_class != InfiniteInteger.class
							&& param_class != InfiniteReal.class
							&& param_class != String.class
							&& param_class != Boolean.class) {
						throw_exception = true;
					}
					/* If there is possibility of exception and we have last parameter, it can be one
					 * of following types */
					if (throw_exception
							&& j == params.length-1 /* last element  */
							&&
							(param_class == Object[].class
							|| param_class == InfiniteNumber[].class
							|| param_class == InfiniteInteger[].class
							|| param_class == InfiniteReal[].class
							|| param_class == String[].class
							|| param_class == Boolean[].class)) {
						throw_exception = false;
					}
					// if true then throw
					if (throw_exception) {
						throw new TQLException("Checking of '" + functions_name	+ "', '" + name + "': "
								+ " type of parametr " + (j+1) + " is '" + param_class.getName()
								+ "', but it is not allowed.");
					}
				}
			}
			// the method pass through
			functions.put(name, method);
		}
		
		// return checked functions
		return functions;
	}
	
}
