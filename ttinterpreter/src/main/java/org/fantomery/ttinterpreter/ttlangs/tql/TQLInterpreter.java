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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import java_cup.runtime.Symbol;
import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteNumber;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator.MethodTypes;
import org.fantomery.ttinterpreter.ttlangs.tql.TQLValue.Key;
import org.fantomery.toptree.TopTreeException;
import org.fantomery.toptree.Vertex;
import org.fantomery.toptree.impl.TopTreeImpl;


/**
 * This class contains tools for execution of Top Tree Query Language (TQL) commands over
 * Top Trees. It holds the data structure of Top Trees and provides method that represents TQL commands.
 * <p>
 * After an instance of this class is created, it has to be created the Top Tree and chosen
 * vertex identifier before using the object that is responsibility of the method
 * {@link ttlangs.tql.TQLInterpreter#prepareTopTree(String, String, String, String, String)}.
 * <p>
 * Single TQL commands are executed by method {@link ttlangs.tql.TQLInterpreter#execute(String)}. It
 * does a lexical analysis of the command and then it try to execute it. For the execution it
 * uses private method of this class.
 *  
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class TQLInterpreter {
	
	
	/**
	 * This enumeration type represent a object type of the top tree. There are only two type
	 * that TQL can work with - vertices and clusters.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	private enum TypeOfObject {
		
		/** Represents vertex. */
		VERTEX,
		
		/** Represents cluster. */
		CLUSTER
	}
	
	/** A name of the class that implements interface {@link ttlangs.tql.TopTreeHolder}. */
	private static final String TOPTREE_NAME = "$TopTree";
	
	/** An object of the class implementing interface {@link ttlangs.tql.TopTreeHolder}. */
	@SuppressWarnings("unchecked")
	private TopTreeHolder tree;
	
	/** 
	 * An object of the class {@link ttlangs.tql.TQLHash} that is used for two level mapping from
	 * unique vertex identifiers to the objects of vertices. */
	private TQLHash hash;

	/** A type of the class that represents vertex information. */
	private Class<?> vertex_class;

	/** A type of the class that represents cluster information. */
	private Class<?> cluster_class;

	/** A name of the vertex class field that will be used as unique vertex identifier. */
	private String unique_vertex_field;

	/** A name of the package where will be generated class representing the top tree placed in. */
	private String toptree_package;

	/** A conversion from names of vertex class fields into their data types. */
	private HashMap<String, Class<?>> vertex_fields;

	/** A conversion from names of cluster class fields into their data types. */
	private HashMap<String, TQLClusterField> cluster_fields;
	
	/** A list of declared vertex class fields. */
	private ArrayList<TQLVariable> node_variables;
	
	/** A list of implicit values of vertex class fields. */
	private ArrayList<TQLValue> node_init_values;

	/** A list of declared cluster class fields. */
	private ArrayList<TQLVariable> cluster_variables;
	
	/** A list of implicit values of vertex class fields. */
	private ArrayList<TQLValue> cluster_init_values;

	/** A conversion from names of supported TQL functions into their Method types. */
	private HashMap<String, Method> functions;


	/**
	 * The constructor sets all received information into appropriate internal fields and creates
	 * empty lists for declared and implicit values of vertices and edges.
	 * 
	 * @param vertex_fields Data types of vertex class fields.
	 * @param cluster_fields Data types of cluster class fields.
	 * @param vertex_class Full package name of a vertex class.
	 * @param cluster_class Full package name of a cluster class.
	 * @param functions A conversion from supported functions names into their Method types.
	 * @param toptree_package A name of top tree package.
	 */
	protected TQLInterpreter(HashMap<String, Class<?>> vertex_fields,
			HashMap<String, TQLClusterField> cluster_fields,
			Class<?> vertex_class, Class<?> cluster_class, HashMap<String, Method> functions,
			String toptree_package) {
		this.vertex_fields = vertex_fields;
		this.cluster_fields = cluster_fields;
		this.vertex_class = vertex_class;
		this.cluster_class = cluster_class;
		this.node_variables = new ArrayList<TQLVariable>();
		this.node_init_values = new ArrayList<TQLValue>();
		this.cluster_variables = new ArrayList<TQLVariable>();
		this.cluster_init_values = new ArrayList<TQLValue>();
		this.functions = functions;
		this.toptree_package = toptree_package;
	}


	/**
	 * This method creates and compiles a class that implements {@link ttlangs.tql.TopTreeHolder}.
	 * <p>
	 * First it generates Java source code of the class and writes it into the file in required folder.
	 * Then the method calls method {@link ttlangs.tql.Launcher#CompileFiles(String...)}. In the end it
	 * creates an instance of compiled class saves the instance into 
	 * {@link ttlangs.tql.TQLInterpreter#tree}.
	 * 
	 * @param vertex_class A name of vertex info class.
	 * @param cluster_class A name of cluster info class.
	 * @param algortithm_class A name of algorithm class.
	 * @param package_name A name of package that will used as root package.
	 * @throws Exception If any exception occurs (I/O, Java Compiler, Reflection API)
	 */
	private void generateTopTree(String vertex_class, String cluster_class,
			String algortithm_class, String package_name)
	throws Exception {
		// a storage for generated top tree holder
		StringBuilder result = new StringBuilder();
		// just generate Java code for the class implementing TopTreeHolder
		result.append("package ").append(package_name).append(".").append(toptree_package).append(";\n\n");
		result.append("import java.util.ArrayList;\n");
		result.append("import ").append(Vertex.class.getName()).append(";\n");
		result.append("import ").append(TopTreeImpl.class.getName()).append(";\n");
		result.append("import ").append(TopTreeHolder.class.getName()).append(";\n");
		result.append("import ").append(package_name).append(".").append(algortithm_class).append(";\n");
		result.append("import ").append(package_name).append(".").append(vertex_class).append(";\n");
		result.append("import ").append(package_name).append(".").append(cluster_class).append(";\n\n");
		result.append("public class ").append(TOPTREE_NAME).append(" extends TopTreeHolder<")
		.append(cluster_class).append(", ").append(vertex_class).append("> {\n");
		result.append("\tpublic ").append(TOPTREE_NAME).append("() {\n");
		result.append("\t\tthis.handler = new ").append(algortithm_class).append("();\n");
		result.append("\t\tthis.top = new TopTreeImpl<").append(cluster_class).append(", ")
		.append(vertex_class).append(">(this.handler);\n");
		result.append("\t\tthis.vertices = new ArrayList<Vertex<").append(vertex_class).append(">>();\n");
		result.append("\t}\n");
		result.append("}");
		
		// make auxiliary folder
		File f = new File(package_name + "/" + toptree_package);
		f.mkdir();
		// compose file name
		String file_name = package_name + "/" + toptree_package + "/" + TOPTREE_NAME + ".java";
		// write source into file
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(file_name));
		out.write(result.toString());
		out.close();
		// try to compile generated class
		Launcher.CompileFiles(file_name);

		// make instance of the class
		Constructor<?> con = Class.forName(package_name + "." + toptree_package + "." + TOPTREE_NAME)
							.getConstructor((Class<?>[]) null);
		this.tree = (TopTreeHolder<?, ?>) con.newInstance((Object[]) null);
	}
    

	/**
	 * This method checks received candidate for unique vertex identifier and if it passes then the method
	 * generates top tree and returns <code>true</code>. Otherwise it returns <code>false</code>.
	 * <p>
	 * The candidate must be a vertex class field of type String, InfiniteInteger or InfiniteReal.
	 * <p>
	 * Top Tree is created by method
	 * {@link ttlangs.tql.TQLInterpreter#generateTopTree(String, String, String, String)}.
	 * 
	 * @param name A name of vertex field - candidate to unique identifier.
	 * @param vertex_class A name of vertex info class.
	 * @param cluster_class A name of cluster info class.
	 * @param algortithm_class A name of algorithm class.
	 * @param package_name A name of package that will used as root package.
	 * @return If unique identifier accepted then <code>true</code>, else <code>false</code>.
	 * @throws Exception If any exception occurs (I/O, Java Compiler, Reflection API)
	 */
	public boolean prepareTopTree(String name, String vertex_class, String cluster_class,
			String algortithm_class, String package_name)
	throws Exception {
		// check if given name is one of fields in vertex class
		if (this.vertex_fields.containsKey(name)) {
			// check type
			Class<?> type = this.vertex_fields.get(name);
			if (type != InfiniteInteger.class && type != InfiniteReal.class && type != String.class) {
		        System.out.print("The field '" + name + "' has type '" + type.getSimpleName()
		        		+ "', but only types '" + InfiniteInteger.class.getSimpleName() + "', '"
		        		+ InfiniteReal.class.getSimpleName() + "' and '" + String.class.getSimpleName()
		        		+ "' are allowed.");
				return false;
			}
			// remember the name
			this.unique_vertex_field = name;
			//
			this.hash = new TQLHash(type);
			// create top tree
			generateTopTree(vertex_class, cluster_class, algortithm_class, package_name);
			return true;
		}
		else {
			// if is not then return false
	        System.out.print("The field '" + name + "' doesn't exist - try it again: ");
			return false;
		}
	}


	/**
	 * This method checks a number of usages of the variable.
	 * <p>
	 * If the variable represents an array then there are allowed two occurrences - with different keys.
	 * If the variable is not an array then it can be used just once.
	 * 
	 * @param vars A list of all used variables.
	 * @param var Checked variable.
	 * @throws TQLException If variable is used more than allowed.
	 */
	private void checkIfContainsVariableName(ArrayList<TQLVariable> vars, TQLVariable var)
	throws TQLException {
		// take name of checked field - it will be needed often
		String name = var.getName();
		
		// distinguish if field is array or not
		if (var.isArray()) {
			// if array then there are allowed at most two usages - but with different keys (L/R) 
			for (TQLVariable variable : vars) {
				if (variable.getName().matches(name)) {
					// if match then check usage of key
					TQLChecker.checkUsageOfArray(name, var.getKey(), variable.getKey());
				}
			}
		}
		else {
			// if not array then just check that the name was not used before
			for (TQLVariable variable : vars) {
				TQLChecker.checkDifferentNames(name, variable.getName());
			}
		}
	}

	
	/**
	 * This method checks a number of usages of the initialized field with value.
	 * <p>
	 * If the field represents an array then there are allowed two occurrences - with different keys.
	 * If the field is not an array then it can be used just once.
	 * 
	 * @param init_vals A list of used initialized fields.
	 * @param value Checked field with value.
	 * @throws TQLException If field is used more than allowed.
	 */
	private void checkIfContainsInitVariableName(ArrayList<TQLValue> init_vals, TQLValue value)
	throws TQLException {
		// take name of checked field - it will be needed often
		String name = value.getName();
		
		// distinguish if field is array or not
		if (value.isArray()) {
			// if array then there are allowed at most two usages - but with different keys (L/R) 
			for (TQLValue init_val : init_vals) {
				if (init_val.getName().matches(name)) {
					// if match then check usage of key
					TQLChecker.checkUsageOfArray(name, value.getKey(), init_val.getKey());
				}
			}
		}
		else {
			// if not array then just check that the name was not used before
			for (TQLValue init_val : init_vals) {
				TQLChecker.checkDifferentNames(name, init_val.getName());
			}
		}
	}


	/**
	 * This method checks that all cluster class fields are used during defining of new declaration
	 * and implicit initialization for edges.
	 * <p>
	 * If the field is not an array it must be used just once. If it is an array so it must 
	 * be used twice - with left and right boundaries as keys.
	 * 
	 * @param res_vars A list of declared cluster fields.
	 * @param res_init_vals A list of implicitly initialized cluster class fields.
	 * @throws TQLException If any cluster class field is not used.
	 */
	private void checkAllFieldsAreUsed(ArrayList<TQLVariable> res_vars, ArrayList<TQLValue> res_init_vals)
	throws TQLException {
		// for each class cluster field check it it is used 
		for (String field_name: this.cluster_fields.keySet()) {
			if (!this.cluster_fields.get(field_name).isArray()) {
				// for non-array cluster class field just check occurrence
				boolean used = false;
				for (TQLVariable variable : res_vars) {
					if (variable.getName().matches(field_name)) {
						used = true;
						break;
					}
				}
				if (!used) {
					for (TQLValue value : res_init_vals) {
						if (value.getName().matches(field_name)) {
							used = true;
							break;
						}
					}
				}
				TQLChecker.checkClassFieldsUsed(field_name, used);
			}
			else {
				// for array cluster class field check occurrence of both keys
				boolean left_used = false;
				boolean right_used = false;
				for (TQLVariable variable : res_vars) {
					if (variable.getName().matches(field_name)) {
						if (variable.getKey() == Key.L) left_used = true;
						if (variable.getKey() == Key.R) right_used = true;
						if (left_used && right_used)	break;
					}
				}
				if (!left_used && !right_used) {
					for (TQLValue value : res_init_vals) {
						if (value.getName().matches(field_name)) {
							if (value.getKey() == Key.L) left_used = true;
							if (value.getKey() == Key.R) right_used = true;
							if (left_used && right_used)	break;
						}
					}
				}
				TQLChecker.checkArrayClassFieldsUsed(field_name, left_used, right_used);
			}
		}
	}


	/**
	 * This method checks that all cluster class fields are used during creation of new edge.
	 * <p>
	 * If the field is not an array it must be used just once. If it is an array so it must 
	 * be used twice - with left and right boundaries as keys.
	 * 
	 * @param dec_result A list of initialized cluster class fields.
	 * @throws TQLException If any cluster class field is not used.
	 */
	private void checkAllFieldsAreUsed(ArrayList<TQLValue> dec_result) throws TQLException {
		// for cluster class field distinguish between array and normal type
		for (String field_name: this.cluster_fields.keySet()) {
			if (!this.cluster_fields.get(field_name).isArray()) {
				// for non-array cluster class field just check occurrence
				boolean used = false;
				for (TQLValue value : dec_result) {
					if (value.getName().matches(field_name)) {
						used = true;
						break;
					}
				}
				TQLChecker.checkClassFieldsUsed(field_name, used);
			}
			else {
				// for array cluster class field check occurrence of both keys
				boolean left_used = false;
				boolean right_used = false;
				for (TQLValue value : dec_result) {
					if (value.getName().matches(field_name)) {
						if (value.getKey() == Key.L) left_used = true;
						if (value.getKey() == Key.R) right_used = true;
						if (left_used && right_used)	break;
					}
				}
				TQLChecker.checkArrayClassFieldsUsed(field_name, left_used, right_used);
			}
		}
	}


	/**
	 * This method checks parameters and saves declaration and implicit initialization of 
	 * nodes and edges.
	 * <p>
	 * The method checks for each parameter existence of fields, exclusivity of field usage (either
	 * in declaration or in initialization), data types and count of usage. After checking
	 * it saves gained information.  
	 * 
	 * @param params Information of declaration and initialization.
	 * @param type Vertex/Cluster.
	 * @throws TQLException If any parameter doesn't pass the checking.
	 */
	private void setDeclaration(TQLCommand params, TypeOfObject type) throws TQLException {
		
		// take variables for declaration and values for initialization
		ArrayList<TQLVariable> vars = params.getVariables();
		ArrayList<TQLValue> init_vals = params.getInitValues();
		// prepare empty storages for saving declaration and implicit initialization
		ArrayList<TQLVariable> res_vars = new ArrayList<TQLVariable>();
		ArrayList<TQLValue> res_init_vals = new ArrayList<TQLValue>();
		
		// Declaration: for each field check its existence, exclusive usage and number of usage.
		for (TQLVariable var : vars) {
			String name = var.getName();
			if (type == TypeOfObject.VERTEX) {
				assert !var.isArray();
				// can't be unique identifier
				TQLChecker.checkFieldNotUnique(name, this.unique_vertex_field);
				TQLChecker.checkFieldExistence(name, this.vertex_fields.containsKey(name));
				TQLChecker.checkFieldUsageInDeclarXorInit(name, init_vals);
				checkIfContainsVariableName(res_vars, var);
			}
			else {
				assert type == TypeOfObject.CLUSTER;
				TQLChecker.checkFieldExistence(name, this.cluster_fields.containsKey(name));
				TQLChecker.checkFieldUsageInDeclarXorInit(name, init_vals);
				TQLChecker.checkArray(name, var.isArray(), this.cluster_fields.get(name));
				checkIfContainsVariableName(res_vars, var);
			}
			// insert into results
			res_vars.add(var);
		}
		
		// Initialization: for each value check field existence, data type and number of usage
		for (TQLValue val : init_vals) {
			String name = val.getName();
			if (type == TypeOfObject.VERTEX) {
				assert !val.isArray();
				// can't be unique identifier
				TQLChecker.checkFieldNotUnique(name, this.unique_vertex_field);
				TQLChecker.checkFieldExistence(name, this.vertex_fields.containsKey(name));
				TQLChecker.checkFieldType(name, val.toString(), val.getType(), this.vertex_fields.get(name));
				// don't to check if it is in res_vars, because it was checked in first for
				checkIfContainsInitVariableName(res_init_vals, val);
			}
			else {
				assert type == TypeOfObject.CLUSTER;
				TQLChecker.checkFieldExistence(name, this.cluster_fields.containsKey(name));
				TQLChecker.checkFieldType(name, val.toString(), val.getType(),
						this.cluster_fields.get(name).getType());
				TQLChecker.checkArray(name, val.isArray(), this.cluster_fields.get(name));
				// don't to check if it is in res_vars, because it was checked in first for
				checkIfContainsInitVariableName(res_init_vals, val);
			}
			// insert into results
			res_init_vals.add(val);
		}
		// for cluster type check if each class field is declared or initialized
		if (type == TypeOfObject.CLUSTER) checkAllFieldsAreUsed(res_vars, res_init_vals);
		// save declaration and implicit initialization
		if (type == TypeOfObject.VERTEX) {
			this.node_variables = res_vars;
			this.node_init_values = res_init_vals;
		}
		else {
			assert type == TypeOfObject.CLUSTER;
			this.cluster_variables = res_vars;
			this.cluster_init_values = res_init_vals;
		}
	}


	/**
	 * This method prints information about declaration and implicit initialization of nodes or edges.
	 * <p>
	 * It is easy - just load declaration and initialization information from appropriate lists,
	 * compose it into one string and return it.
	 * 
	 * @param type Switch between node and edge information printing.
	 */
	private void showDeclarInfo(TypeOfObject type) {
		// prepare empty storage
		StringBuilder result = new StringBuilder();
		// load declaration and implicit field values according to the object type
		ArrayList<TQLVariable> variables = null;
		ArrayList<TQLValue> values = null;
		if (type == TypeOfObject.VERTEX) {
			result.append(this.unique_vertex_field).append(" (");
			variables = node_variables;
			values = node_init_values;
		}
		else {
			assert type == TypeOfObject.CLUSTER;
			result.append("(");
			variables = cluster_variables;
			values = cluster_init_values;
		}
		// we are going to print list of declared fields
		for (int i = 0; i < variables.size(); i++) {
			TQLVariable var = variables.get(i);
			result.append(var.getName());
			if (var.isArray()) result.append("[" + var.getKey() + "]");
			if (i < variables.size()-1 ) result.append(", ");
		}
		result.append(") (");
		// we are going to print list of fields and their implicit values
		for (int i = 0; i < values.size(); i++) {
			TQLValue value = values.get(i);
			result.append(value.getName());
			if (value.isArray()) result.append("[" + value.getKey().toString() + "]");
			result.append("=").append(value.toString());
			if (i < values.size()-1 ) result.append(", ");
		}
		result.append(")");
		// print information
		System.out.println(result.toString());
	}


	/**
	 * This method prepares parameters for creation of new node or new edge.
	 * <p>
	 * The preparation has two parts. Each field can be used only in one of these parts. Declaration
	 * is the first part. The method checks values for declared fields. Second part is initialization.
	 * It checks exclusivity of field usage, existence of the fields and data types.
	 * <p>
	 * Prepared initializations are merged together with implicit initialization and the returned.
	 * 
	 * @param declar_values Values that corresponds to implicit declared fields.
	 * @param init_values Initialization of fields.
	 * @param object_type Vertex/Cluster
	 * @return Prepared field initialization.
	 * @throws TQLException If any parameter doesn't pass the checking.
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<TQLValue> prepareParameters(ArrayList<TQLValue> declar_values,
			ArrayList<TQLValue> init_values, TypeOfObject object_type) throws TQLException {

		// prepare empty storages for declared and initializes fields
		ArrayList<TQLValue> dec_result = new ArrayList<TQLValue>();
		ArrayList<TQLValue> init_result = new ArrayList<TQLValue>();
		
		// load actual declared fields and implicit initialization values with fields
		ArrayList<TQLVariable> saved_variables = null;	// declared
		ArrayList<TQLValue> saved_init_values = null;	// initialized
		// loading is done according the type of object
		if (object_type == TypeOfObject.VERTEX) {
			saved_variables = this.node_variables;
			saved_init_values = this.node_init_values;
		}
		else {
			assert object_type == TypeOfObject.CLUSTER;
			saved_variables = this.cluster_variables;
			saved_init_values = this.cluster_init_values;
		}
	
		// prepare values for declared fields
		if (declar_values != null && !declar_values.isEmpty()) {
			TQLChecker.checkSameSizeOfLists(saved_variables.size(), declar_values.size());
			/* For each value check usage only in declaration or in initialization and data type.
			 * Then push all information about the field and value into storage of declaration. */ 
			for (int i = 0; i < declar_values.size(); i++) {
				TQLValue dec_val = declar_values.get(i);
				String dec_name = saved_variables.get(i).getName();
				// usage only in declaration or in initialization
				if (init_values != null && !init_values.isEmpty()) {
					TQLChecker.checkFieldUsageInDeclarXorInit(dec_name, init_values);
				}
				// data type
				if (object_type == TypeOfObject.VERTEX) {
					TQLChecker.checkFieldType(dec_name, dec_val.toString(), dec_val.getType(),
							this.vertex_fields.get(dec_name));
				}
				else {
					assert object_type == TypeOfObject.CLUSTER;
					TQLChecker.checkArray(dec_name, saved_variables.get(i).isArray(),
							this.cluster_fields.get(dec_name));
					TQLChecker.checkFieldType(dec_name, dec_val.toString(), dec_val.getType(),
							this.cluster_fields.get(dec_name).getType());
				}
				// preparing of information: name, type, value, key (if array)
				TQLValue dec_res = new TQLValue(dec_val.getValue());
				dec_res.setName(dec_name);
				if (saved_variables.get(i).isArray()) {
					dec_res.setKey(saved_variables.get(i).getKey());
				}
				// add value into storage of declaration values
				dec_result.add(dec_res);
			}
		}
		
		// prepare values for initialization fields
		if (init_values != null && !init_values.isEmpty()) {
			/* For each value check existence of the field, unique of usage count and type.
			 * Insert initialization into appropriate storage. */
			for (TQLValue init_val : init_values) {
				String name = init_val.getName();
				if (object_type == TypeOfObject.VERTEX) {
					TQLChecker.checkFieldExistence(name, this.vertex_fields.containsKey(name));
					TQLChecker.checkFieldNotUnique(name, this.unique_vertex_field);
					TQLChecker.checkFieldType(name, init_val.toString(), init_val.getType(),
							this.vertex_fields.get(name));
				}
				else {
					assert object_type == TypeOfObject.CLUSTER;
					TQLChecker.checkFieldExistence(name, this.cluster_fields.containsKey(name));
					TQLChecker.checkArray(name, init_val.isArray(), this.cluster_fields.get(name));
					TQLChecker.checkFieldType(name, init_val.toString(), init_val.getType(),
							this.cluster_fields.get(name).getType());
				}
				// check if number of usage is allowed
				checkIfContainsInitVariableName(init_result, init_val);
				// add into initialization storage
				init_result.add(init_val);
			}
		}

		// merge prepared field values with implicit initialization values
		ArrayList<TQLValue> node_init = (ArrayList<TQLValue>) saved_init_values.clone();
		for (TQLValue res_val : init_result) {
			for (TQLValue val : saved_init_values) {
				if (res_val.getName().matches(val.getName()) && res_val.getKey() == val.getKey()) {
					assert res_val.getType() == val.getType();
					node_init.remove(val);
					break;
				}
			}
		}
		// concatenate results from declaration and initialization
		dec_result.addAll(init_result);
		dec_result.addAll(node_init);
		// for cluster type check if all class fields are initialized
		if (object_type == TypeOfObject.CLUSTER) checkAllFieldsAreUsed(dec_result);
		// return the result
		return dec_result;
	}
	

	/**
	 * This method creates new vertex and insert it into top tree and into the list of vertices.
	 * All the essentials must be checked before calling this method!
	 * <p>
	 * The method takes vertex info constructor and create the vertex object. Afterwards it set all
	 * vertex fields as defined in <code>params</code> list. Finally it inserts the vertex into 
	 * top tree and into hash-map where all vertices are kept.
	 * 
	 * @param unique_field A name (unique identifier) of a vertex.
	 * @param params Parameters of inserted vertes.
	 * @throws Exception If any exception of Reflection API occurs.
	 */
	@SuppressWarnings("unchecked")
	private void insertVertexInfo(TQLValue unique_field, ArrayList<TQLValue> params)
	throws Exception {
		
		// take constructor of vertex info class and create an instance of this class
		Constructor<?> construstor = this.vertex_class.getConstructor((Class<?>[]) null);
		Object vertex_info = construstor.newInstance((Object[]) null);
		
		// insert value of unique vertex identifier into other parameters
		params.add(unique_field);
		// set values of field in vertex info object according to params
		for (TQLValue value : params) {
			assert !value.isArray();
			String name = value.getName();
			Method method = this.vertex_class.getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.SET, name), value.getType());
			method.invoke(vertex_info, value.getValue());
		}
		
		// finally insert new vertex into top tree and into hash-map where all vertices are kept
		this.tree.vertices.add(this.hash.put(unique_field.getValue()), 
				this.tree.top.createNewVertex(vertex_info));
	}


	/**
	 * This method checks all parameters of node creation command and if the parameters pass then
	 * if inserts the node into the top tree and into hash-map where all vertices are kept.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>Allowed values of the vertex identifier - if it is numerical type, it must be finite.</li>
	 *   <li>A type of the vertex identifier.</li>
	 *   <li>The vertex identifier has not to be already used.</li>
	 * </ul>
	 * The parameters of vertex are prepared by method
	 * {@link ttlangs.tql.TQLInterpreter#prepareParameters(ArrayList, ArrayList, ttlangs.tql.TQLInterpreter.TypeOfObject)}.
	 * Adding is made by method 
	 * {@link ttlangs.tql.TQLInterpreter#insertVertexInfo(TQLValue, ArrayList)}.
	 * 
	 * @param result All information needed for insertion.
	 * @throws Exception If any exception during parameter checking or in Reflection API occurs.
	 */
	private void addNode(TQLCommand result) throws Exception {

		// take value of unique node identifier and set the name of this field
		TQLValue unique_field = result.getValue1();
		unique_field.setName(this.unique_vertex_field);
		
		// if node identifier is number then there are allowed only finite values
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, unique_field);

		// check the type of unique node identifier
		TQLChecker.checkFieldType(this.unique_vertex_field, unique_field.toString(),
				unique_field.getType(), this.vertex_fields.get(this.unique_vertex_field));
		
		// check that the node with given id doesn't exist
		TQLChecker.checkNodeNonExistence(this.unique_vertex_field, unique_field, this.tree.vertices,
				this.hash);
		
		// prepare parameters for creating vertex info class
		ArrayList<TQLValue> params = prepareParameters(result.getValues(), result.getInitValues(),
				TypeOfObject.VERTEX);
		
		// create vertex info class and appropriate node
		insertVertexInfo(unique_field, params);
	}


	/**
	 * This method creates a new edge and inserts into its basic cluster received information.
	 * If any problem occurs during the creation it throws the exception.
	 * <p>
	 * First, the method creates object for storing user defined information. Next, it saves all received
	 * parameters with values into this object. Finally, the method creates the edge by calling
	 * one of <code>link</code> methods from {@link toptree.TopTree} according to received vertices.
	 * Values of <code>vertex3</code> and <code>vertex4</code> can be <code>null</code>. It sign that 
	 * there are no requirements to position.
	 * 
	 * @param vertex1 The first end-point of new edge.
	 * @param vertex2 The second end-point of new edge.
	 * @param vertex3 A vertex that specify a position of new edge in the first end-point.
	 * @param vertex4 A vertex that specify a position of new edge in the second end-point.
	 * @param params An array of information that will be saved into edge cluster.
	 * @throws Exception If any exception during parameter checking or in Reflection API occurs.
	 */
	@SuppressWarnings("unchecked")
	private void createEdge(TQLValue vertex1, TQLValue vertex2, TQLValue vertex3, TQLValue vertex4,
			ArrayList<TQLValue> params)
	throws Exception {

		// take constructor of cluster info class and create an instance of this class
		Constructor<?> construstor = this.cluster_class.getConstructor((Class<?>[]) null);
		Object cluster_info = construstor.newInstance((Object[]) null);
		
		// take objects that represent vertices and neighbors in top tree
		Vertex<?> vertex1_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex1.getValue()));
		Vertex<?> vertex2_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex2.getValue()));
		Vertex<?> vertex3_obj = null;
		if (vertex3 != null) {
			vertex3_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex3.getValue()));
		}
		Vertex<?> vertex4_obj = null;
		if (vertex4 != null) {
			vertex4_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex4.getValue()));
		}
		
		// set values of field in cluster info object according to params
		for (TQLValue value : params) {
			String name = value.getName();
			if (value.isArray()) {
				// catch method
				Method method = this.cluster_class.getMethod(
						TFLMethodNameCreator.createMethodName(MethodTypes.SET, name),
							this.vertex_class, value.getType());
				// distinguish between left and right key
				if (value.getKey() == Key.L) {
					method.invoke(cluster_info, (Object) vertex1_obj.getInfo(), value.getValue());
				} else {
					assert value.getKey() == Key.R;
					method.invoke(cluster_info, (Object) vertex2_obj.getInfo(), value.getValue());
				}
			}
			else {
				// just take the method and call it
				Method method = this.cluster_class.getMethod(
						TFLMethodNameCreator.createMethodName(MethodTypes.SET, name), value.getType());
				method.invoke(cluster_info, value.getValue());
			}
		}
		
		// finally create new edge with prepared cluster info in top tree
		if (vertex3_obj == null && vertex4_obj == null)
			this.tree.top.link(vertex1_obj, vertex2_obj, cluster_info);
		else if (vertex3_obj != null && vertex4_obj == null)
			this.tree.top.link(vertex2_obj, vertex1_obj, vertex3_obj, cluster_info);
		else if (vertex3_obj == null && vertex4_obj != null)
			this.tree.top.link(vertex1_obj, vertex2_obj, vertex4_obj, cluster_info);
		else if (vertex3_obj != null && vertex4_obj != null)
			this.tree.top.link(vertex1_obj, vertex3_obj, vertex2_obj, vertex4_obj, cluster_info);
	}


	/**
	 * This method prepares parameters for creation of new edge and creates it.
	 * If any problem occurs during the parameters checking or the linking then the exception is thrown.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>Data types of vertex identifiers and allowed values for numerical types.</li>
	 *   <li>Differentness of all vertices.</li>
	 *   <li>Existence of vertex identifier - if it doesn't exists and there are no position 
	 *       requirement then the vertex is created.</li>
	 *   <li>The requirements of the position of new edge in its end-points.</li>
	 * </ul>
	 * The parameters of new edge are prepared by method
	 * {@link ttlangs.tql.TQLInterpreter#prepareParameters(ArrayList, ArrayList, ttlangs.tql.TQLInterpreter.TypeOfObject)}.
	 * The edge creation is made by method 
	 * {@link ttlangs.tql.TQLInterpreter#createEdge(TQLValue, TQLValue, TQLValue, TQLValue, ArrayList)}.
	 * 
	 * @param result All information needed for edge creation prepared during lexical analysis.
	 * @throws Exception If any exception during parameter checking or in Reflection API occurs.
	 */
	@SuppressWarnings("unchecked")
	private void link(TQLCommand result) throws Exception {
		
		/* Take values of unique node identifiers that represents linked vertices and set 
		 * names of these fields. */
		TQLValue vertex1 = result.getValue1();
		TQLValue vertex2 = result.getValue2();
		TQLValue vertex3 = result.getValue3();
		TQLValue vertex4 = result.getValue4();
		vertex1.setName(this.unique_vertex_field);
		vertex2.setName(this.unique_vertex_field);
		if (vertex3 != null) vertex3.setName(this.unique_vertex_field);
		if (vertex4 != null) vertex4.setName(this.unique_vertex_field);

		// check the type of node identifier for both vertices and their neighbors
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex1.toString(),
				vertex1.getType(), this.vertex_fields.get(this.unique_vertex_field));
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex2.toString(),
				vertex2.getType(), this.vertex_fields.get(this.unique_vertex_field));
		if (vertex3 != null) TQLChecker.checkFieldType(this.unique_vertex_field, vertex3.toString(),
				vertex3.getType(), this.vertex_fields.get(this.unique_vertex_field));
		if (vertex4 != null) TQLChecker.checkFieldType(this.unique_vertex_field, vertex4.toString(),
				vertex4.getType(), this.vertex_fields.get(this.unique_vertex_field));
		
		// if node identifier is number then there are allowed only finite values
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex1);
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex2);
		if (vertex3 != null) TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex3); 
		if (vertex4 != null) TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex4); 
		
		// check if the nodes are different
		TQLChecker.checkDifferentNodes(vertex1, vertex2);
		if (vertex3 != null) TQLChecker.checkDifferentNodes(vertex1, vertex3);
		if (vertex4 != null) TQLChecker.checkDifferentNodes(vertex1, vertex4);
		if (vertex3 != null) TQLChecker.checkDifferentNodes(vertex2, vertex3);
		if (vertex4 != null) TQLChecker.checkDifferentNodes(vertex2, vertex4);
		if (vertex3 != null && vertex4 != null) TQLChecker.checkDifferentNodes(vertex3, vertex4);
		
		// check and prepare parameters for creating cluster info class
		ArrayList<TQLValue> params = prepareParameters(result.getValues(), result.getInitValues(),
				TypeOfObject.CLUSTER);
		
		// after params checking: if linked nodes doesn't exist and there is no its neighbor then create it
		if (!this.hash.contains(vertex1.getValue()) && vertex3 == null) {
			insertVertexInfo(vertex1, this.node_init_values);
		} else {
			TQLChecker.checkExistenceOfNode(vertex1, this.hash);
		}
		if (!this.hash.contains(vertex2.getValue()) && vertex4 == null) {
			insertVertexInfo(vertex2, this.node_init_values);
		} else {
			TQLChecker.checkExistenceOfNode(vertex2, this.hash);
		}
		if (vertex3 != null) TQLChecker.checkExistenceOfNode(vertex3, this.hash);
		if (vertex4 != null) TQLChecker.checkExistenceOfNode(vertex4, this.hash);
		
		// check if exists edge 1--3
		if (vertex3 != null)
			TQLChecker.checkExistenceOfPath(
				vertex1.toString(),
				vertex3.toString(),
				this.tree.top.expose(
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex1.getValue())),
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex3.getValue()))));

		// check if exists edge 2--4
		if (vertex4 != null)
			TQLChecker.checkExistenceOfPath(
				vertex2.toString(),
				vertex4.toString(),
				this.tree.top.expose(
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex2.getValue())),
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex4.getValue()))));

		// check if vertices are in different components
		TQLChecker.checkNonExistenceOfEdge(
				vertex1.toString(),
				vertex2.toString(),
				this.tree.top.expose(
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex1.getValue())),
						(Vertex<?>) this.tree.vertices.get(this.hash.get(vertex2.getValue()))));

		// create cluster info class and appropriate edge
		createEdge(vertex1, vertex2, vertex3, vertex4, params);
	}


	/**
	 * This method deletes an edge from the tree. If any problem occurs during parameter checking or
	 * the deletion then the exception is thrown.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>Data types of vertex identifiers and allowed values for numerical types.</li>
	 *   <li>Existence of vertex identifiers.</li>
	 *   <li>Differentness of all vertices.</li>
	 * </ul>
	 * The edge deletion is made by method {@link toptree.TopTree#cut(Vertex, Vertex)}.
	 * 
	 * @param result All information needed for edge deletion prepared during lexical analysis.
	 * @throws TQLException If any problem during parameter checking occurs.
	 * @throws TopTreeException If any problem during the edge deletion occurs.
	 */
	@SuppressWarnings("unchecked")
	private void cut(TQLCommand result) throws TQLException, TopTreeException {

		/* Take values of unique node identifiers that represents vertices and set 
		 * names of these fields. */
		TQLValue vertex1 = result.getValue1();
		TQLValue vertex2 = result.getValue2();
		vertex1.setName(this.unique_vertex_field);
		vertex2.setName(this.unique_vertex_field);
		
		// if node identifier is number then there are allowed only finite values
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex1);
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex2);
		
		// check the type of node identifier for both vertices
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex1.toString(),
				vertex1.getType(), this.vertex_fields.get(this.unique_vertex_field));
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex2.toString(),
				vertex2.getType(), this.vertex_fields.get(this.unique_vertex_field));

		// check existence of both vertices
		TQLChecker.checkExistenceOfNode(vertex1, this.hash);
		TQLChecker.checkExistenceOfNode(vertex2, this.hash);
		
		// check if the nodes are different
		TQLChecker.checkDifferentNodes(vertex1, vertex2);
		
		// take objects that represent vertices in top tree
		Vertex<?> vertex1_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex1.getValue()));
		Vertex<?> vertex2_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex2.getValue()));
		
		// check if vertices are in different components
		TQLChecker.checkExistenceOfPath(
				vertex1.toString(),
				vertex2.toString(),
				this.tree.top.expose(vertex1_obj,vertex2_obj));

		// finally delete the edge from the top tree
		this.tree.top.cut(vertex1_obj, vertex2_obj);
	}


	/**
	 * This method reads information stored in the vertex and print it into system output.
	 * If any problem occurs during parameter checking then the exception is thrown.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>A data type of vertex identifier and allowed value if it has numerical type.</li>
	 *   <li>Existence of vertex identifier.</li>
	 * </ul>
	 * After parameter checking, the method calls {@link toptree.TopTree#expose(Vertex)} to make the 
	 * vertex boundary of the root cluster. Then it just reads the information from vertex and prints it.
	 * 
	 * @param result All information of the command prepared during lexical analysis.
	 * @throws TQLException If any problem during parameter checking occurs.
	 */
	@SuppressWarnings("unchecked")
	private void showNodeInfo(TQLCommand result) throws TQLException {

		/* Take value of unique node identifier that represents vertex and set 
		 * name of this field. */
		TQLValue vertex = result.getValue1();
		vertex.setName(this.unique_vertex_field);
		
		// if node identifier is number then there are allowed only finite values
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex);
		
		// check the type of node identifier for the vertex
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex.toString(),
				vertex.getType(), this.vertex_fields.get(this.unique_vertex_field));

		// check existence of vertex
		TQLChecker.checkExistenceOfNode(vertex, this.hash);

		// take object that represents vertices in top tree
		Vertex<?> vertex_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex.getValue()));

		// expose vertex into the root cluster
		this.tree.top.expose(vertex_obj);

		// show informations that are in vertex info object in top tree
		System.out.println(vertex_obj.getInfo().toString());
	}


	/**
	 * This method reads information stored in the cluster with specified boundary vertices and print
	 * it into system output. If any problem occurs during parameter checking then the exception is thrown.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>A data type of vertex identifiers and allowed values if they have numerical type.</li>
	 *   <li>Existence of vertex identifiers.</li>
	 *   <li>Differentness of both vertices.</li>
	 *   <li>Existence of the path between the vertices.</li>
	 * </ul>
	 * After parameter checking, the method calls {@link toptree.TopTree#expose(Vertex, Vertex)}
	 * to make vertices boundaries of the root cluster. Then it just reads information from the cluster
	 * and prints it.
	 * 
	 * @param result All information of the command prepared during lexical analysis.
	 * @throws TQLException If any problem during parameter checking occurs.
	 */
	@SuppressWarnings("unchecked")
	private void showLinkInfo(TQLCommand result) throws TQLException {

		/* Take values of unique node identifiers that represents vertices and set 
		 * names of these fields. */
		TQLValue vertex1 = result.getValue1();
		TQLValue vertex2 = result.getValue2();
		vertex1.setName(this.unique_vertex_field);
		vertex2.setName(this.unique_vertex_field);
		
		// if node identifier is number then there are allowed only finite values
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex1);
		TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, vertex2);
		
		// check the type of node identifier for both vertices
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex1.toString(),
				vertex1.getType(), this.vertex_fields.get(this.unique_vertex_field));
		TQLChecker.checkFieldType(this.unique_vertex_field, vertex2.toString(),
				vertex2.getType(), this.vertex_fields.get(this.unique_vertex_field));

		// check existence of both vertices
		TQLChecker.checkExistenceOfNode(vertex1, this.hash);
		TQLChecker.checkExistenceOfNode(vertex2, this.hash);
		
		// check if the nodes are different
		TQLChecker.checkDifferentNodes(vertex1, vertex2);

		// take objects that represent vertices in top tree
		Vertex<?> vertex1_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex1.getValue()));
		Vertex<?> vertex2_obj = (Vertex<?>) this.tree.vertices.get(this.hash.get(vertex2.getValue()));

		// check if vertices are in different components
		TQLChecker.checkExistenceOfPath(
				vertex1.toString(),
				vertex2.toString(),
				this.tree.top.expose(vertex1_obj,vertex2_obj));

		// show informations that are in vertex info object in top tree
		System.out.print(this.tree.top.getTopComponent(vertex1_obj).getInfo().toString());
	}


	/**
	 * This method executes received function and prints the returned result into system output.
	 * If any problem occurs then the exception is thrown.
	 * <p>
	 * Method checks:
	 * <ul>
	 *   <li>Existence of the function.</li>
	 *   <li>Data types of all parameters of the function.</li>
	 *   <li>Existence of used vertex identifiers.</li>
	 * </ul>
	 * The method checks all parameters and prepare them for function. Finally the function is invoked
	 * and the string result is printed into output.
	 * 
	 * @param result All information needed for function execution prepared during lexical analysis.
	 * @throws Exception If any exception during parameter checking or in Reflection API occurs.
	 */
	private void executeFunction(TQLCommand result)
	throws Exception {

		// take name of the function a it's parameters
		String func_name = result.getName();
		ArrayList<TQLValue> func_params = result.getValues();
		
		// check existence of function name
		TQLChecker.checkFunctionExistence(func_name, this.functions.containsKey(func_name));
		
		// catch appropriate method and its parameters
		Method method = this.functions.get(func_name);
		Class<?>[] met_params = method.getParameterTypes();
		
		/* Check number of parameters: it hasn't to be lower than method's count of parameters.
		 * If it is greater then last argument must be an array type. */
		TQLChecker.checkFunctionParameterCount(func_params, met_params);
		
		// create array of Objects for arguments needed to invoke 'method'
		Object[] arguments = new Object[met_params.length];
		// fill top tree and handler into first and second position
		arguments[0] = this.tree.top;
		arguments[1] = this.tree.handler;
		
		// check types of parameters
		for (int i = 2; i < met_params.length-1; i++) {
			TQLValue value = func_params.get(i-2);
			Object argument = null;
			
			if (met_params[i] == Vertex.class) {
				// if node identifier is number then there are allowed only finite values
				TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, value);
				// check the type of node identifier for the vertex
				TQLChecker.checkFieldType(this.unique_vertex_field, value.toString(),
						value.getType(), this.vertex_fields.get(this.unique_vertex_field));
				// check existence of vertex
				TQLChecker.checkExistenceOfNode(value, this.hash);
				// take object that represents vertices in top tree
				argument = (Vertex<?>) this.tree.vertices.get(this.hash.get(value.getValue()));
			}
			else if (met_params[i] == Object.class) {
				argument = value.getValue();
			}
			else {
				TQLChecker.checkFunctionParameterType(
								i-1, value.toString(), value.getType(), met_params[i]);
				argument = value.getValue();
			}
			arguments[i] = argument;
		}
		
		// remember counts of values and arguments
		int func_size = func_params.size();
		int met_size = met_params.length;
		// take type of last argument
		Class<?> met_last_type = met_params[met_size-1];

		// distinguish between simple type and array
		if (met_last_type == String.class
				|| met_last_type == InfiniteInteger.class
				|| met_last_type == InfiniteReal.class
				|| met_last_type == InfiniteNumber.class
				|| met_last_type == Boolean.class
				|| met_last_type == Object.class
				|| met_last_type == Vertex.class) {

			assert func_size+2 == met_size;
			// simple type - just check type of value and push the value into the last argument position
			TQLValue value = func_params.get(func_size-1);
			if (met_last_type == Vertex.class) {
				// if node identifier is number then there are allowed only finite values
				TQLChecker.checkAllowedValueOfNodeId(this.unique_vertex_field, value);
				// check the type of node identifier for the vertex
				TQLChecker.checkFieldType(this.unique_vertex_field, value.toString(),
						value.getType(), this.vertex_fields.get(this.unique_vertex_field));
				// check existence of vertex
				TQLChecker.checkExistenceOfNode(value, this.hash);
				// take object that represents vertices in top tree
				arguments[met_size-1] = (Vertex<?>) this.tree.vertices.get(this.hash.get(value.getValue()));
			}
			else {
				TQLChecker.checkFunctionParameterType(func_size, value.toString(), value.getType(),
						met_params[met_size-1]);
				arguments[met_size-1] = value.getValue();
			}
		}
		else {
			// last argument is array - remember the type of array
			if (met_params[met_size-1] == Object[].class) {
				met_last_type = Object.class;
			} else if (met_params[met_size-1] == InfiniteNumber[].class) {
				met_last_type = InfiniteNumber.class;
			} else if (met_params[met_size-1] == InfiniteInteger[].class) {
				met_last_type = InfiniteInteger.class;
			} else if (met_params[met_size-1] == InfiniteReal[].class) {
				met_last_type = InfiniteReal.class;
			} else if (met_params[met_size-1] == String[].class) {
				met_last_type = String.class;
			} else if (met_params[met_size-1] == Boolean[].class) {
				met_last_type = Boolean.class;
			} else {
				assert false;
			}
			// all last values must be placed into the array then will be the last argument
			int last_values_count = func_size + 2 - met_size +1;
			// create an array with size right size and the type of the last argument
			Object[] last_arguments = (Object[]) Array.newInstance(met_last_type, last_values_count);
			/* Check last values: for-cycle goes according to func_params so we must decrease start
			 * index by -3. */
			for (int i = 0; i < last_values_count; i++) {
				int func_cursor = i + met_size - 3;
				TQLValue value = func_params.get(func_cursor);
				TQLChecker.checkFunctionParameterType(func_cursor+1, value.toString(), value.getType(),
						met_last_type);
				// don't remember add +2 to index (toptree and handler)
				last_arguments[i] = value.getValue();
			}
			// push created array into the last argument position
			arguments[met_size-1] = last_arguments;
		}
		// try to execute the method
		String message = (String) method.invoke(this.functions, arguments);
		System.out.print(message);
	}


	/**
	 * This method receives a line with TQL command string, analyzes the command and executes the command.
	 * If the command is exit then it returns <code>true</code> else <code>false</code>.
	 * <p>
	 * The method first does syntactic analysis of the command by calling
	 * {@link ttlangs.tql.lexer} and {@link ttlangs.tql.parser}. During the analysis the object
	 * {@link ttlangs.tql.TQLCommand} is prepared. It stores all information about the command.
	 * According to a type of the command the appropriate private class method is called to execute
	 * the command.
	 * 
	 * @param line A line with one TQL command.
	 * @return If the command is exit then <code>true</code> else <code>false</code>.
	 * @throws Exception If any exception occurs during the command execution (TQLException,
	 * 					Reflection API or TopTreeException). 
	 */
	public boolean execute(String line) throws Exception {

		// empty line
		if (line.matches("")) {
			return false;
		}
		
		// create parser for actual command from the file
		parser p = new parser(new lexer(new StringReader(line)));
		
		/* Try to parse the command. "java_cup.runtime.Symbol" is Cup's class that represents
		 * last matched state. */
		Symbol symbol = p.parse();
		
		/* If symbol is null then no grammar rule was used during parsing. If some rule was used
		 * for creating any non-terminal then symbol is not null. But the jFlex gives to the
		 * parser symbol sym.EOF that represents end of file - if some rule from grammar matches
		 * input. */
		if (symbol == null || symbol.sym != sym.EOF) {
			return false;
		}

		// now we can safely retype because parser has finished without failure
		TQLCommand result = (TQLCommand) symbol.value;
		assert result != null; 
		
		// According to the type of command make semantic analysis and then execute the command
		switch (result.getType()) {
		case EXIT:
			// we will finish now
			return true;
		case NODE_DECLARATION:
			// try to set node declaration
			setDeclaration(result, TypeOfObject.VERTEX);
			System.out.println("OK");
			break;
		case LINK_DECLARATION:
			// try to set link declaration
			setDeclaration(result, TypeOfObject.CLUSTER);
			System.out.println("OK");
			break;
		case NODE_DECLAR_INFO:
			// show info about node declaration
			showDeclarInfo(TypeOfObject.VERTEX);
			break;
		case LINK_DECLAR_INFO:
			// show info about link declaration
			showDeclarInfo(TypeOfObject.CLUSTER);
			break;
		case NODE_INFO:
			// show info about node from top tree
			showNodeInfo(result);
			break;
		case LINK_INFO:
			// show info about link from top tree
			showLinkInfo(result);
			break;
		case NODE:
			// try to add new vertex into the top tree
			addNode(result);
			System.out.println("OK");
			break;
		case LINK:
			// try to create an edge between two vertices of the top tree
			link(result);
			System.out.println("OK");
			break;
		case CUT:
			// try to create an edge between two vertices of the top tree
			cut(result);
			System.out.println("OK");
			break;
		case FUNCTION:
			// try to execute the function
			executeFunction(result);
			break;
		default:
			assert false;
		}

		// read next command
		return false;
	}
	
}
