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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteNumber;
import org.fantomery.ttinterpreter.numbers.InfiniteReal;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator.MethodTypes;
import org.fantomery.ttinterpreter.ttlangs.tfl.lexer;
import org.fantomery.toptree.Cluster;
import org.fantomery.toptree.TopTreeListener;
import de.hunsicker.jalopy.Jalopy;


/**
 * This class represents compiler from Top Tree Friendly Language (TFL) to Java language.
 * <p>
 * TFL was developed specially for easy designing of algorithms over Top Trees. This class contains
 * one public method for starting the compilation and non-public methods for making hard work.
 * <p>
 * The only public method is called {@link TFLCompiler#compileTFL(String, String, String)}.
 * It makes a lexical analysis of input TFL file. During the analysis all information for compiling
 * are prepared from the input and saved inside the instance of the class. When the lexical analysis
 * is finished then three Java class are generated and saved into required folder. First generated class
 * describes information stored in vertices, second informations stored in cluster and third 
 * describes an algorithm that will be running over the Top Tree. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class TFLCompiler {
	
	/**
	 * This constant defines one character that is used as a separator between cluster and
	 * vertex shortcuts and field name in class fields.
	 */
	private static final String FIELD_SEPARATOR = "#";

	/**
	 * This constant defines one character that is used as a separator between cluster and
	 * vertex shortcuts and field name in auxiliary fields.
	 */
	private static final String LOCAL_SEPARATOR = "@";
	
	/**
	 * This constant is used as replacement for
	 * {@link ttlangs.tfl.TFLCompiler#LOCAL_SEPARATOR} in resulting Java-code.
	 */
	private static final String LOCAL_SEPARATOR_REPLACEMENT = "_";
	
	/**
	 * This constant is used for indicating the beginning of assignment statement.
	 */
	private static final String OPEN_ASSIGNMENT = "%";

	/**
	 * This constant is used for indicating the end of assignment statement.
	 */
	private static final String CLOSE_ASSIGNMENT = "%%";
	
	
	/**
	 * This enumeration type represents all allowed data types used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum DataType {
		
		/** InfiniteInteger */
		INTEGER,

		/** InfiniteReal */
		REAL,
		
		/** String */
		STRING,
		
		/** Boolean */
		BOOLEAN,
		
		/** Array whose key is resulting vertex info class and value is InfiniteInteger */
		ARRAY_INTEGER,
		
		/** Array whose key is resulting vertex info class and value is InfiniteReal */
		ARRAY_REAL,
		
		/** Array whose key is resulting vertex info class and value is String */
		ARRAY_STRING,
		
		/** Array whose key is resulting vertex info class and value is Boolean */
		ARRAY_BOOLEAN
	}

	
	/**
	 * This enumeration type represents all types of cluster shortcuts used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum ClusterShort {
		
		/** A left cluster child of the current cluster. */
		A,
		
		/** A right cluster child of the current cluster. */
		B,
		
		/** The current cluster. */
		C,
		
		/** Any cluster child of the current cluster. */
		CHILD
	}

	
	/**
	 * This enumeration type represents all types of vertex shortcuts used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum VertexShort {

		/** A left boundary vertex of the cluster. */
		LEFT,

		/** A right boundary vertex of the cluster. */
		RIGHT,

		/** A vertex that is common for both cluster children of the current cluster. */
		COMMON,

		/** An only boundary vertex of the current point cluster. */
		BORDER
	}

	
	/**
	 * This enumeration type represents all types of assignment statement used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum AssignmentOperetor {
		
		/** Assignment = ... can be used with any data types */
		ASSIGN,
		
		/** Assignment += ... can be used only with numerical data types */
		PLUS_ASSIGN,
		
		/** Assignment -= ... can be used only with numerical data types */
		MINUS_ASSIGN,
		
		/** Assignment *= ... can be used only with numerical data types */
		TIMES_ASSIGN,
		
		/** Assignment /= ... can be used only with numerical data types */
		DIVIDE_ASSIGN,
		
		/** Assignment &= (concatenation) ... can be used only with strings */
		AMPERSAND_ASSIGN
	}

	
	/**
	 * This enumeration type represents all types of boolean operators used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum BooleanOperetor {
		
		/** Logical AND (&&) */
		AND,

		/** Logical OR (||) */
		OR
    }

	
	/**
	 * This enumeration type represents all types of comparison operator used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum ComparisonOperetor {

		/** Equal to == */
		EQUAL,
		
		/** Not equal to != */
		NOT_EQUAL,
		
		/** Less than < */
		LESS,
		
		/** Greater than > */
		GREATER,
		
		/** Less than or equal to <= */
		LESS_EQUAL,
		
		/** Greater than or equal to >= */
		GREATER_EQUAL
	}

	
	/**
	 * This enumeration type represents all types of arithmetic operator used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum ArithmeticOperetor {
		
		/** Additive operator + */
		PLUS,
		
		/** Subtraction operator - */
		MINUS,
		
		/** Multiplication operator * */
		TIMES,

		/** Division operator / */
		DIVIDE
	}
	
	
	/**
	 * This enumeration type represents all types of local blocks used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum BuildingBlock {
		
		/** The current cluster is path cluster (create, destroy). */
		PATH,
		
		/** The current cluster is point cluster (create, destroy). */
		POINT,
		
		/** A child of the current cluster is path child (join, split). */
		PATH_CHILD,
		
		/** A child of the current cluster is point child (join, split). */
		POINT_CHILD,
		
		/** The current cluster is path cluster (join, split). */
		PATH_PARENT,
		
		/** The current cluster is point cluster (join, split). */
		POINT_PARENT,
		
		/** The current cluster represents connection of type path and path (join, split). */
		PATH_AND_PATH,
		
		/** The current cluster represents connection of type path and point (join, split). */
		PATH_AND_POINT,
		
		/** The current cluster represents connection of type point and path (join, split). */
		POINT_AND_PATH,
		
		/** The current cluster represents connection of type point and point (join, split). */
		POINT_AND_POINT,
		
		/** The current cluster represents connection of type lpoint over rpoint (join, split). */
		LPOINT_OVER_RPOINT,
		
		/** The current cluster represents connection of type rpoint over lpoint (join, split). */
		RPOINT_OVER_LPOINT,
		
		/** The current cluster represents connection of type lpoint and rpoint (join, split). */
		LPOINT_AND_RPOINT 
	}
	
	
	/**
	 * This enumeration type represents all types of root blocks used in TFL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum RootBlock {
		
		/** Represent create method. */
		CREATE,
		
		/** Represent destroy method. */
		DESTROY,
		
		/** Represent join method. */
		JOIN,
		
		/** Represent split method. */
		SPLIT,
		
		/** Represent selectQustion method. */
		SELECT
	}
  
  
	/** A name of algorithm. Is used for generating vertex, cluster and algorithm class name. */
	private String algorithm_name;
	
	/** A name of package whose members generated Java classes are. */
	private String package_name;
	
	/** A name of generated Java vertex info class. */
	private String vertex_class_name;
	
	/** A name of generated Java cluster info class. */
	private String cluster_class_name;
	
	/** A map of vertex class fields. Names are keys and data types are values. */
	private HashMap<String, DataType> vertex_fields;
	
	/** A map of cluster class fields. Names are keys and data types are values. */
	private HashMap<String, DataType> cluster_fields;

	/** A map of algorithm class fields. Names are keys and data types are values. */
	private HashMap<String, DataType> var_fields;

	/** A map of local auxiliary fields. Names are keys and data types are values. */
	private HashMap<String, DataType> local_fields;

	/** A map of resulting Java code of algorithm class methods.
	 *  Root blocks are keys and appropriate Java sources are values. */
	private HashMap<RootBlock, String> class_parts;
	
	/** A map of the code rising during compilation of TFL code into Java code in local blocks.
	 *  Local blocks are keys and rising code are values. */
	private HashMap<BuildingBlock, ArrayList<String>> method_parts;
	
	/** A list of data types that must be imported into generated algorithm Java class. */
	private ArrayList<DataType> imported_types;
	

	/**
	 * This constructor initializes all hash-map and array-list class fields to new empty maps and lists.
	 */
	protected TFLCompiler() {
		this.vertex_fields	= new HashMap<String, DataType>();
		this.cluster_fields	= new HashMap<String, DataType>();
		this.var_fields		= new HashMap<String, DataType>();
		this.local_fields	= new HashMap<String, DataType>();
		this.class_parts	= new HashMap<RootBlock, String>();
		this.method_parts	= new HashMap<BuildingBlock, ArrayList<String>>();
		this.imported_types	= new ArrayList<DataType>();
	}
	
	
	/**
	 * This method inserts new vertex class fields into the instance. It pushes all names 
	 * from <code>arr_vs</code> into {@link ttlangs.tfl.TFLCompiler#vertex_fields}
	 * with data type <code>d_t</code>.
	 * <p>
	 * Before inserting it checks uniqueness of field names. If any is not unique then it throws
	 * an exception. 
	 * 
	 * @param d_t A data type of inserted fields.
	 * @param arr_vs A list of names of inserted fields.
	 * @throws TFLException If vertex field name is not unique.
	 */
	protected void pushVertexFields(DataType d_t, ArrayList<String> arr_vs) throws TFLException {
		// check uniqueness of all names and insert it into vertex_field
		for (String name : arr_vs) {
			TFLChecker.checkFieldUniqueness(name, this.vertex_fields.containsKey(name), "vertex");
			this.vertex_fields.put(name, d_t);
		}
	}


	/**
	 * This method inserts new cluster class fields into the instance. It pushes all names 
	 * from <code>arr_vs</code> into {@link ttlangs.tfl.TFLCompiler#cluster_fields}
	 * with data type <code>d_t</code>.
	 * <p>
	 * Before inserting it checks uniqueness of field names. If any is not unique then it throws
	 * an exception. 
	 * 
	 * @param d_t A data type of inserted fields.
	 * @param arr_vs A list of names of inserted fields.
	 * @throws TFLException If cluster field name is not unique.
	 */
	protected void pushClusterFields(DataType d_t, ArrayList<String> arr_vs) throws TFLException {
		// check uniqueness of all names and insert it into vertex_field
		for (String name : arr_vs) {
			TFLChecker.checkFieldUniqueness(name, this.cluster_fields.containsKey(name), "cluster");
			this.cluster_fields.put(name, d_t);
		}
	}
	
	
	/**
	 * This method inserts new algorithm class fields into the instance. It pushes all names 
	 * from <code>arr_vs</code> into {@link ttlangs.tfl.TFLCompiler#var_fields}
	 * with data type <code>d_t</code>.
	 * <p>
	 * Before inserting it checks uniqueness of field names. If any is not unique then it throws
	 * an exception. 
	 * 
	 * @param d_t A data type of inserted fields.
	 * @param arr_vs A list of names of inserted fields.
	 * @throws TFLException If algorithm field name is not unique.
	 */
	protected void pushVarFields(DataType d_t, ArrayList<String> arr_vs) throws TFLException {
		// check uniqueness of all names and insert it into vertex_field
		for (String name : arr_vs) {
			TFLChecker.checkFieldUniqueness(name, this.var_fields.containsKey(name), "var");
			this.var_fields.put(name, d_t);
		}
	}
	
	
	/**
	 * This method converts a data type that is not an array type into appropriate data type
	 * that can be an array type.
	 * <p>
	 * It decides according to the switcher <code>is_array</code>. If it is <code>false</code>
	 * then the method returns the same type as it received. Otherwise it returns appropriate
	 * array data type.
	 * 
	 * @param type A data type that is not an array type.
	 * @param is_array A switcher that shows if the given type is array or not.
	 * @return An actual data type.
	 */
	protected static DataType getCommonType(DataType type, Boolean is_array) {
		switch (type) {
		case INTEGER:	return is_array ? DataType.ARRAY_INTEGER 	: DataType.INTEGER;
		case REAL:		return is_array ? DataType.ARRAY_REAL 		: DataType.REAL;
		case STRING:	return is_array ? DataType.ARRAY_STRING 	: DataType.STRING;
		default:		assert type == DataType.BOOLEAN;
						return is_array ? DataType.ARRAY_BOOLEAN	: DataType.BOOLEAN;
		}
	}
	

	/**
	 * This method receives a name of the algorithm and creates and saves vertex, cluster and algorithm
	 * class name.
	 * <p> 
	 * It sets these fields:
	 * <ul>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#algorithm_name}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#vertex_class_name}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#cluster_class_name}</li>
	 * </ul>
	 * 
	 * @param alg_name A name of algorithm.
	 */
	protected void setNames(String alg_name) {
		// change first letter of the name to upper case 
		alg_name = alg_name.substring(0, 1).toUpperCase() + alg_name.substring(1);
		this.algorithm_name 	= alg_name;
		this.vertex_class_name 	= alg_name + "VertexInfo";
		this.cluster_class_name	= alg_name + "ClusterInfo";
	}
	
	
	/**
	 * This method saves package name into this instance.
	 * <p>
	 * It sets field {@link ttlangs.tfl.TFLCompiler#package_name}.
	 * 
	 * @param package_name A name of package whose members generated Java classes will be.
	 */
	private void setPackageName(String package_name) {
		this.package_name = package_name;
	}
	
	
	/**
	 * This method returns a name of the algorithm.
	 * <p>
	 * It returns value of field {@link ttlangs.tfl.TFLCompiler#algorithm_name}.
	 * 
	 * @return A name of the algorithm.
	 */
	private String getAlgorithmName() {
		return this.algorithm_name;
	}
	
	
	/**
	 * This method returns a name of the package whose members generated Java classes will be.
	 * <p>
	 * It returns value of field {@link ttlangs.tfl.TFLCompiler#package_name}.
	 * 
	 * @return A name of the package whose members generated Java classes will be.
	 */
	private String getPackageName() {
		return this.package_name;
	}
	
	
	/**
	 * This method returns a name of the generated vertex class.
	 * <p>
	 * It returns value of field {@link ttlangs.tfl.TFLCompiler#vertex_class_name}.
	 * 
	 * @return A name of the generated vertex class.
	 */
	private String getVertexClassName() {
		return this.vertex_class_name;
	}
	
	
	/**
	 * This method returns a name of the generated cluster class.
	 * <p>
	 * It returns value of field {@link ttlangs.tfl.TFLCompiler#cluster_class_name}.
	 * 
	 * @return A name of the generated cluster class.
	 */
	private String getClusterClassName() {
		return this.cluster_class_name;
	}
	
	
	/**
	 * This method generates and returns Java source code of imports that corresponds to data types
	 * used in <code>fields</code>.
	 * <p>
	 * If arrays are used in TFL so there must be also imported packages for manipulation with them.
	 * <p>
	 * This method is used during generating vertex and cluster info classes.
	 * 
	 * @param fields Class fields for that imports will be generated. 
	 * @return A string containing Java code of imports.
	 */
	private String generateImports(HashMap<String, DataType> fields) {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// if InfiniteIntegers are used, we must import it
		if (fields.containsValue(DataType.INTEGER) || fields.containsValue(DataType.ARRAY_INTEGER)) {
			result.append("import ").append(InfiniteInteger.class.getName()).append(";\n");
		}
		// if InfiniteReals are used, we must import it
		if (fields.containsValue(DataType.REAL)	|| fields.containsValue(DataType.ARRAY_REAL)) {
			result.append("import ").append(InfiniteReal.class.getName()).append(";\n");
		}
		/* If arrays(=HashMaps) are used, we must import tools for work with them inside getters and
		 * setters and also inside toString method. */
		if (fields.containsValue(DataType.ARRAY_INTEGER) || fields.containsValue(DataType.ARRAY_REAL)
				|| fields.containsValue(DataType.ARRAY_STRING)
				|| fields.containsValue(DataType.ARRAY_BOOLEAN)) {
			result.append("import java.util.ArrayList;\n");
			result.append("import java.util.HashMap;\n");
			result.append("import java.util.Map;\n");
			result.append("import java.util.Set;\n");
			result.append("import java.util.Map.Entry;\n");
			result.append("import ").append(getPackageName()).append(".")
			.append(getVertexClassName()).append(";\n");
		}
		result.append("\n");
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of class fields declaration for all fields
	 * according to <code>fields</code>.
	 * <p>
	 * Array from TFL are represented in Java through hash-maps where the key is vertex info object.
	 * <p>
	 * This method is used during generating vertex and cluster info classes.
	 * 
	 * @param fields Class fields for that declaration will be generated. 
	 * @return A string containing Java code of class fields declaration.
	 */
	private String generateFields(HashMap<String, DataType> fields) {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// for each field generate its private Java declaration according the data type
		Set<Map.Entry<String, DataType>> all_entries = fields.entrySet();
		for (Entry<String, DataType> entry: all_entries) {
			switch (entry.getValue()){
			case INTEGER:
				result.append("private InfiniteInteger ").append(entry.getKey()).append(";\n\n");
				break;
			case REAL:
				result.append("private InfiniteReal ").append(entry.getKey()).append(";\n\n");
				break;
			case STRING:
				result.append("private String ").append(entry.getKey()).append(";\n\n");
				break;
			case BOOLEAN:
				result.append("private Boolean ").append(entry.getKey()).append(";\n\n");
				break;
			case ARRAY_INTEGER:
				result.append("private HashMap<").append(getVertexClassName()).append(", InfiniteInteger> ")
				.append(entry.getKey()).append(";\n\n");
				break;
			case ARRAY_REAL:
				result.append("private HashMap<").append(getVertexClassName()).append(", InfiniteReal> ")
				.append(entry.getKey()).append(";\n\n");
				break;
			case ARRAY_STRING:
				result.append("private HashMap<").append(getVertexClassName()).append(", String> ")
				.append(entry.getKey()).append(";\n\n");
				break;
			default:
				assert entry.getValue() == DataType.ARRAY_BOOLEAN;
				result.append("private HashMap<").append(getVertexClassName()).append(", Boolean> ")
				.append(entry.getKey()).append(";\n\n");
			}
		}
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of constructor that initializes all
	 * class fields to implicit values.
	 * <p>
	 * Implicit values are defined in class {@link ttlangs.tfl.TFLImplicitFieldValues}.
	 * <p>
	 * This method is used during generating vertex and cluster info classes.
	 * 
	 * @param class_name A name of the class whose constructor is generated.
	 * @param fields Class fields. 
	 * @return A string containing Java code of the constructor.
	 */
	private String generateConstructor(String class_name, HashMap<String, DataType> fields) {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// public header of constructor
		result.append("public ").append(class_name).append("() {\n");
		// implicit initialization of all class fields
		Set<Map.Entry<String, DataType>> all_fields = fields.entrySet();
		for (Entry<String, DataType> field: all_fields) {
			result.append("this.").append(field.getKey()).append(" = ")
			.append(TFLImplicitFieldValues.getImplicitValue(field.getValue(), getVertexClassName()))
			.append(";\n");
		}
		// close the body
		result.append("}\n\n");
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of all class methods for received field.
	 * <p>
	 * There is generated GET and SET method and if the type of the field is different from Boolean
	 * then ADD method. Generated methods are public.
	 * <p>
	 * This method is used during generating vertex and cluster info classes.
	 * 
	 * @param d_t A data type of the class field.
	 * @param name A name of the class field.
	 * @return A string containing Java code of all generated class methods for received field.
	 */
	private String generateFieldMethods(DataType d_t, String name) {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// prepare name of field's data type
		String type_name = null;
		switch (d_t){
		case INTEGER:
			type_name = "InfiniteInteger";
			break;
		case REAL:
			type_name = "InfiniteReal";
			break;
		case STRING:
			type_name = "String";
			break;
		case BOOLEAN:
			type_name = "Boolean";
			break;
		case ARRAY_INTEGER:
			type_name = "InfiniteInteger";
			break;
		case ARRAY_REAL:
			type_name = "InfiniteReal";
			break;
		case ARRAY_STRING:
			type_name = "String";
			break;
		default:
			assert d_t == DataType.ARRAY_BOOLEAN;
			type_name = "Boolean";
		}

		// getter
		result.append("public ").append(type_name).append(" ")
		.append(TFLMethodNameCreator.createMethodName(MethodTypes.GET, name));
		if (d_t == DataType.INTEGER || d_t == DataType.REAL || d_t == DataType.STRING
				|| d_t == DataType.BOOLEAN) {
			result.append("() {\nreturn this.").append(name).append(";\n}\n\n");
		}
		else {
			// hash-map
			result.append("(").append(getVertexClassName()).append(" vertex) {\nreturn this.")
			.append(name).append(".get(vertex);\n}\n\n");
		}
		
		// setter
		result.append("public void ").append(TFLMethodNameCreator.createMethodName(MethodTypes.SET, name));
		if (d_t == DataType.INTEGER || d_t == DataType.REAL || d_t == DataType.STRING
				|| d_t == DataType.BOOLEAN) {
			result.append("(").append(type_name).append(" ").append(name).append(") {\nthis.")
			.append(name).append(" = ").append(name).append(";\n}\n\n");
		}
		else {
			// hash-map
			result.append("(").append(getVertexClassName()).append(" vertex, ").append(type_name)
			.append(" ").append(name).append(") {\nthis.").append(name).append(".put(vertex, ")
			.append(name).append(");\n}\n\n");
		}
		
		// no adder for Boolean
		if (d_t == DataType.BOOLEAN || d_t == DataType.ARRAY_BOOLEAN)
			return result.toString();
		
		// adder - according to the type
		result.append("public void ").append(TFLMethodNameCreator.createMethodName(MethodTypes.ADD, name));
		if (d_t == DataType.INTEGER || d_t == DataType.REAL) {
			result.append("(").append(type_name).append(" ").append(name).append(") {\nthis.")
			.append(name).append(".plus(").append(name).append(");\n");
		}
		else if (d_t == DataType.ARRAY_INTEGER || d_t == DataType.ARRAY_REAL) {
			result.append("(").append(getVertexClassName()).append(" vertex, ").append(type_name)
			.append(" ").append(name).append(") {\nthis.").append(name).append(".get(vertex).plus(")
			.append(name).append(");\n");
		}
		else if (d_t == DataType.STRING) {
			result.append("(").append(type_name).append(" ").append(name).append(") {\nthis.")
			.append(name).append(" += ").append(name).append(";\n");
		}
		else {
			assert d_t == DataType.ARRAY_STRING;
			result.append("(").append(getVertexClassName()).append(" vertex, ").append(type_name)
			.append(" ").append(name).append(") {\nthis.").append(name).append(".put(vertex, this.")
			.append(name).append(".get(vertex).concat(").append(name).append("));\n");
			
		}
		// close the body
		result.append("}\n\n");
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of all class methods according to
	 * <code>fields</code>.
	 * <p>
	 * For each field is called
	 * {@link ttlangs.tfl.TFLCompiler#generateFieldMethods(ttlangs.tfl.TFLCompiler.DataType, String)} that generates class methods for this field. Received codes are pushed into
	 * storage and in the end the complete result is returned. 
	 * <p>
	 * This method is used during generating vertex and cluster info classes.
	 * 
	 * @param fields Class fields. 
	 * @return A string containing Java code of all class methods.
	 */
	private String generateMethods(HashMap<String, DataType> fields) {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// for all fields generate their methods
		Set<Map.Entry<String, DataType>> all_entries = fields.entrySet();
		for (Entry<String, DataType> entry: all_entries) {
			result.append(generateFieldMethods(entry.getValue(), entry.getKey()));
		}
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of <code>toString()</code> method for 
	 * vertex info class.
	 * <p>
	 * For each vertex info class field generate code that writes information about the field.
	 * Information about all fields will be collected in one <code>StringBuilder</code> and 
	 * in the end returned.
	 * 
	 * @return A string containing Java code of toString() method of vertex info class.
	 */
	private String generateVertexToStringMethod() {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// header and local auxiliary variable
		result.append("public String toString() {\n");
		result.append("StringBuilder result_fields = new StringBuilder();\n");
		// for each field generate its to string writing
		Set<Map.Entry<String, DataType>> all_entries = this.vertex_fields.entrySet();
		for (Entry<String, DataType> entry: all_entries) {
			DataType type = entry.getValue();
			String name = entry.getKey();
			if (type == DataType.INTEGER || type == DataType.REAL || type == DataType.BOOLEAN){
				result.append("if (").append(name).append(" == null) {\n");
				result.append("result_fields.append(\"").append(name).append(" = null;\\t\");\n");
				result.append("}\nelse {\n");
				result.append("result_fields.append(\"").append(name).append(" = \" + ")
				.append(name).append(".toString() + \";\\t\");\n");
			}
			else {
				assert type == DataType.STRING;
				result.append("if (").append(name).append(" == null) {\n");
				result.append("result_fields.append(\"").append(name).append(" = null;\\t\");\n");
				result.append("}\nelse {\n");
				result.append("result_fields.append(\"").append(name).append(" = \\\"\" + ")
				.append(name).append(".toString() + \"\\\";\\t\");\n");
			}
			result.append("}\n");
		}
		// insert returning and close the method
		result.append("return result_fields.toString();\n}\n");
		// return String
		return result.toString();
	}


	/**
	 * This method generates and returns Java source code of <code>toString()</code> method for 
	 * cluster info class.
	 * <p>
	 * For each cluster info class field generate code that writes information about the field.
	 * Information about all fields will be collected in one <code>StringBuilder</code> and 
	 * in the end returned.
	 * <p>
	 * For generating information about hash-map it is necessary to collect information boundary
	 * vertices.
	 * 
	 * @return A string containing Java code of toString() method of cluster info class.
	 */
	private String generateClusterToStringMethod() {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		// header and local auxiliary variable
		result.append("public String toString() {\n");
		result.append("StringBuilder result_fields = new StringBuilder();\n");
		result.append("StringBuilder result_arrays = new StringBuilder();\n");
		// before generating code for first hash-map, we must write info about keys (vertices)
		boolean first_time = true;
		// for each field generate its to string writing
		Set<Map.Entry<String, DataType>> all_entries = this.cluster_fields.entrySet();
		for (Entry<String, DataType> entry: all_entries) {
			DataType type = entry.getValue();
			String name = entry.getKey();
			// for not hash-map is it easy - just distinguish the data type
			if (type == DataType.INTEGER || type == DataType.REAL || type == DataType.BOOLEAN){
				result.append("if (").append(name).append(" == null) {\n");
				result.append("result_fields.append(\"").append(name).append(": null\");\n");
				result.append("}\nelse {\n");
				result.append("result_fields.append(\"").append(name).append(": \" + ")
				.append(name).append(".toString() + \"\\n\");\n");
			}
			else if (type == DataType.STRING){
				result.append("if (").append(name).append(" == null) {\n");
				result.append("result_fields.append(\"").append(name).append(": null\");\n");
				result.append("}\nelse {\n");
				result.append("result_fields.append(\"").append(name).append(": \\\"\" + ")
				.append(name).append(".toString() + \"\\\"\\n\");\n");
			}
			else {
				// for hash-map it is a little complicated
				if (first_time) {
					// before first hash-map we must write info about keys (boundary vertices)
					first_time = false;
					// data type is needed for generating for-cycle over hash-map via set entry-set
					String type_name;
					switch (type) {
					case ARRAY_INTEGER:	type_name = "InfiniteInteger";
										break;
					case ARRAY_REAL:	type_name = "InfiniteReal";
										break;
					case ARRAY_STRING:	type_name = "String";
										break;
					default: 			assert type == DataType.ARRAY_BOOLEAN;
										type_name = "Boolean";
					}
					// generate for-cycle where information is collected
					result.append("ArrayList<").append(getVertexClassName())
					.append("> $boundaries = new ArrayList<").append(getVertexClassName()).append(">();\n");
					result.append("Set<Map.Entry<").append(getVertexClassName()).append(", ")
					.append(type_name).append(">> $all_entries = ").append(name).append(".entrySet();\n");
					result.append("int j = 1;\n");
					result.append("for (Entry<").append(getVertexClassName()).append(", ")
					.append(type_name).append("> $entry: $all_entries) {\n");
					result.append("$boundaries.add($entry.getKey());\n");
					result.append("result_arrays.append(\"Boundary\" + j++ + \": \" + ")
					.append("$entry.getKey().toString() + \"\\n\");\n");
					result.append("}\n");
				}
				// now it's easy - just generate code for writing info about the hash-map
				result.append("for (int i = 0; i < $boundaries.size(); i++) {\n");
				result.append("if ($boundaries.get(i) == null) {\n");
				result.append("result_fields.append(\"\\t").append(name)
				.append("[Boundary\" + (i+1) + \"]: null\\n\");\n");
				result.append("}\nelse {\n");
				result.append("result_arrays.append(\"\\t")
				.append(name).append("[Boundary\" + (i+1) + \"]: \" + ")
				.append(name).append(".get($boundaries.get(i)) + \"\\n\");\n");
				result.append("}\n");
			}
			result.append("}\n");
		}
		// join info about hash-map after info about ordinary fields
		result.append("result_fields.append(result_arrays.toString());\n");
		result.append("return result_fields.toString();\n}\n");
		// return String
		return result.toString();
	}


	/**
	 * This method writes received string into required file. If a file doesn't exist then it is created.
	 * 
	 * @param file_name A name of the created file.
	 * @param string A string to write.
	 * @throws IOException If an I/O error occurs.
	 */
	private void writeIntoFile(String file_name, String string) throws IOException {
		// just create file writer -> write -> close file and finish
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(file_name));
		out.write(string);
		out.close();
	}


	/**
	 * This method compose all parts of vertex info class and writes it into the java-file.
	 * It returns the name of the file.
	 * <p>
	 * For generation separate part of the class it step by step calls following methods:
	 * <ul>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateImports(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateFields(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateConstructor(String, HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateMethods(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateVertexToStringMethod()}</li>
	 * </ul>
	 * Complete Java source code is then written into the file.
	 * 
	 * @param output_path A path to the folder where the java-file will be saved.
	 * @return A name of the file.
	 * @throws IOException If an I/O error occurs.
	 */
	private String generateVertexInfoFile(String output_path) throws IOException {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		/* Compose source of the java-file: */
		// package
		result.append("package " + getPackageName() + ";\n\n");
		// imports
		result.append(generateImports(this.vertex_fields));
		// class header
		result.append("public class " + getVertexClassName() + " {\n\n");
		// class fields
		result.append(generateFields(this.vertex_fields));
		// class constructor
		result.append(generateConstructor(getVertexClassName(), this.vertex_fields));
		// class methods
		result.append(generateMethods(this.vertex_fields));
		// toString method
		result.append(generateVertexToStringMethod());
		// class footer
		result.append("}\n");

		// compose file name
		String file_name = output_path + getVertexClassName() + ".java";
		// write source into file
		writeIntoFile(file_name, result.toString());
		
		// return name of the file
		return file_name;
	}
	
	
	/**
	 * This method compose all parts of cluster info class and writes it into the java-file.
	 * It returns the name of the file.
	 * <p>
	 * For generation separate part of the class it step by step calls following methods:
	 * <ul>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateImports(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateFields(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateConstructor(String, HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateMethods(HashMap)}</li>
	 *   <li>{@link ttlangs.tfl.TFLCompiler#generateClusterToStringMethod()}</li>
	 * </ul>
	 * Complete Java source code is then written into the file.
	 * 
	 * @param output_path A path to the folder where the java-file will be saved.
	 * @return A name of the file.
	 * @throws IOException If an I/O error occurs.
	 */
	private String generateClusterInfoFile(String output_path) throws IOException {
		// prepare storage for generated Java code
		StringBuilder result = new StringBuilder();
		/* Compose source of the java-file: */
		// package
		result.append("package " + getPackageName() + ";\n\n");
		// class imports
		result.append(generateImports(this.cluster_fields));
		// class header
		result.append("public class " + getClusterClassName() + " {\n\n");
		// class fields
		result.append(generateFields(this.cluster_fields));
		// class constructor
		result.append(generateConstructor(getClusterClassName(), this.cluster_fields));
		// class methods
		result.append(generateMethods(this.cluster_fields));
		// toString method
		result.append(generateClusterToStringMethod());
		// class footer
		result.append("}\n");

		// compose file name
		String file_name = output_path + getClusterClassName() + ".java";
		// write source into file
		writeIntoFile(file_name, result.toString());
		
		// return name of the file
		return file_name;
	}
	
	
	/**
	 * This method creates and returns object that represents received variable.
	 * <p>
	 * First, the method checks if the variable is declared in var block - then it reads its data type.
	 * Next, if the variable is not contained in list of local variables then it is inserted into it.
	 * Else its data type is read from the list and checked. Finally, the object representing the variable
	 * is created and returned.
	 * 
	 * @param name A name of a variable
	 * @return An object representing the variable.
	 * @throws TFLException If the variable is used before it is initialized.
	 */
	protected TFLExpression prepareExpressionFromVariable(String name) throws TFLException {
		// prepare field for storing a type
		DataType type = null;
		// if the variable is declared in var then read its type
		if (this.var_fields.containsKey(name)) {
			type = this.var_fields.get(name);
		}
		// if the variable is saved in local list then put it in
		if (!this.local_fields.containsKey(name)) {
			this.local_fields.put(name, type);
		}
		else {
			// if it isn't saved: read type, check initialization and check or set variable type
			DataType local_type = this.local_fields.get(name);
			TFLChecker.checkInitializationOfVariable(name, local_type != null);
			if (type == null) {
				type = local_type;
			} else {
				assert local_type == type;
			}
		}
		// create and return new variable
		return new TFLExpression(type, name);
	}
	
	
	/**
	 * This method creates and returns object that represents received variable
	 * <code>cl_short</code>.<code>name</code>.
	 * <p>
	 * The method creates a code for the variable. First, it replaces cluster short by appropriate 
	 * character (a, b, c). If the type is CHILD then it creates two variables - one for a and second
	 * for b and works with both. Next, the separator is inserted into the code after the cluster character.
	 * If name of variable is contained in declaration of cluster block then
	 * {@link TFLCompiler#FIELD_SEPARATOR} is used, else {@link TFLCompiler#LOCAL_SEPARATOR}. After,
	 * if the variable is not contained in list of local variables then it is inserted into it. Else its 
	 * data type is read from the list and  checked. Finally, the object representing the variable
	 *  is created and returned.
	 * 
	 * @param cl_short A type of cluster short.
	 * @param name A name of variable.
	 * @return An object representing the variable.
	 * @throws TFLException If the variable is used before it is initialized.
	 */
	protected TFLExpression prepareExpressionFromVariable(ClusterShort cl_short, String name)
	throws TFLException {
		// an auxiliary array for cluster type(s)
		ArrayList<String> cluster_names = new ArrayList<String>();
		// a storage for result source
		StringBuilder source = new StringBuilder();
		// save cluster type - for child type save both - a and b
		switch (cl_short) {
		case A:		cluster_names.add("a");
					break;
		case B: 	cluster_names.add("b");
					break;
		case C:		cluster_names.add("c");
					break;
		default: 	assert cl_short == ClusterShort.CHILD;
					cluster_names.add("a");
					cluster_names.add("b");
		}
		// if variable is declared in cluster block then use field separator else local
		DataType type = null;
		if (this.cluster_fields.containsKey(name)) {
			type = this.cluster_fields.get(name);
			source.append(FIELD_SEPARATOR + name);
		}
		else {
			source.append(LOCAL_SEPARATOR + name);
		}
		// for each cluster (one or two - a, b)
		String full_source;
		for (String cl_name : cluster_names) {
			full_source = cl_name + source.toString();
			// if the variable isn't saved in local list then put it in
			if (!this.local_fields.containsKey(full_source)) {
				this.local_fields.put(full_source, type);
			}
			else {
				// if it is saved: read type, check initialization and check or set variable type
				DataType local_type = this.local_fields.get(full_source);
				TFLChecker.checkInitializationOfVariable(cl_name + "." + name, local_type != null);
				if (type == null) {
					type = local_type;
				} else {
					assert local_type == type;
				}
			}
		}
		// create and return new variable
		if (cluster_names.size() == 1) {
			return new TFLExpression(type, cluster_names.get(0) + source.toString());
		}
		else {
			assert cluster_names.size() == 2;
			// we need to remember the short
			return new TFLExpression(type, "child" + source.toString());
		}
	}
	
	
	/**
	 * This method creates and returns object that represents received variable
	 * <code>cl_short</code>.<code>name</code>[<code>ver_short</code>].
	 * <p>
	 * Arrays are allowed only in cluster declaration so the method checks the existence an a type of
	 * the variable.
	 * <p>
	 * The method creates a code for the variable. First, it replaces cluster short by appropriate 
	 * character (a, b, c). If the type is CHILD then it creates two variables - one for a and second
	 * for b and works with both. Next, the separator {@link TFLCompiler#FIELD_SEPARATOR} is inserted
	 * into the code after the cluster character. The name and key is inserted too. After, if the variable
	 * is not contained in list of local variables then it is inserted into it. Else its data type is read
	 * from the list and checked. Finally, the object representing the variable is created and returned.
	 * 
	 * @param cl_short A type of cluster short.
	 * @param name A name of the array variable.
	 * @param ver_short A type of vertex short that is a key of the array.
	 * @return An object representing the array variable.
	 * @throws TFLException If variable is not cluster field or if it's not an array.
	 */
	protected TFLExpression prepareExpressionFromVariable(ClusterShort cl_short, String name,
			VertexShort ver_short)
	throws TFLException {
		// the variable must be an cluster array so it must exist: read data type
		TFLChecker.checkFieldExistence(name, this.cluster_fields.containsKey(name), "cluster");
		DataType data_type = this.cluster_fields.get(name);
		TFLChecker.checkIfClusterFieldIsArray(name, data_type);
		// an auxiliary array for cluster type(s)
		ArrayList<String> cluster_names = new ArrayList<String>();
		// a storage for result source
		StringBuilder source = new StringBuilder();
		// save cluster type - for child type save both - a and b
		switch (cl_short) {
		case A:		cluster_names.add("a");
					break;
		case B: 	cluster_names.add("b");
					break;
		case C:		cluster_names.add("c");
					break;
		default: 	assert cl_short == ClusterShort.CHILD;
					cluster_names.add("a");
					cluster_names.add("b");
		}
		// append field separator, name and left bracket
		source.append(FIELD_SEPARATOR + name + "[");
		// append the key - vertex short
		switch (ver_short) {
		case LEFT:		source.append("left");
						break;
		case RIGHT:		source.append("right");
						break;
		case COMMON:	source.append("common");
						break;
		default:		assert ver_short == VertexShort.BORDER;
						source.append("border");
		}
		// close key
		source.append("]");
		// prepare data type for stored value
		DataType return_type;
		switch (data_type) {
		case ARRAY_INTEGER:	return_type = DataType.INTEGER;
							break;
		case ARRAY_REAL:	return_type = DataType.REAL;
							break;
		case ARRAY_STRING:	return_type = DataType.STRING;
							break;
		default:			assert data_type == DataType.ARRAY_BOOLEAN;
							return_type = DataType.BOOLEAN;
		}
		// for each cluster (one or two - a, b)
		String full_source;
		for (String cl_name : cluster_names) {
			full_source = cl_name + source.toString();
			// if the variable isn't saved in local list then put it in
			if (!this.local_fields.containsKey(full_source)) {
				this.local_fields.put(full_source, return_type);
			}
			else {
				// if it is saved: read type, check variable type
				DataType local_type = this.local_fields.get(full_source);
				assert local_type != null;
				assert local_type == return_type;
			}
		}
		// create and return new variable
		if (cluster_names.size() == 1) {
			return new TFLExpression(return_type, cluster_names.get(0) + source.toString());
		}
		else {
			assert cluster_names.size() == 2;
			// we need to remember the short
			return new TFLExpression(return_type, "child" + source.toString());
		}
	}


	/**
	 * This method creates and returns object that represents received variable
	 * <code>cl_short</code>.<code>ver_short</code>.<code>name</code>.
	 * <p>
	 * The method creates a code for the variable. First, it replaces cluster short by appropriate 
	 * character (a, b, c). If the type is CHILD then it creates two variables - one for a and second
	 * for b and works with both. Next, it remembers the type of separator that will be used. If name
	 * of variable is contained in declaration of cluster block then {@link TFLCompiler#FIELD_SEPARATOR}
	 * is used, else {@link TFLCompiler#LOCAL_SEPARATOR}. After, the method compose the code by adding
	 * the separator, the vertex short, the separator and the variable name after the cluster character.
	 * Next, if the variable is not contained in list of local variables then it is inserted into it.
	 * Else its data type is read from the list and checked. Finally, the object representing 
	 * the variable is created and returned.
	 * 
	 * @param cl_short A type of cluster short.
	 * @param ver_short A type of vertex short.
	 * @param name A name of the variable.
	 * @return An object representing the variable.
	 * @throws TFLException If the variable is used before it is initialized.
	 */
	protected TFLExpression prepareExpressionFromVariable(ClusterShort cl_short, VertexShort ver_short,
			String name) throws TFLException {
		// border and common can be used only with cluster C
		assert ver_short == VertexShort.BORDER ? cl_short == ClusterShort.C : true;
		assert ver_short == VertexShort.COMMON ? cl_short == ClusterShort.C : true;
		// remember the type of separator we are using
		String separator = null;
		DataType type = null;
		if (this.vertex_fields.containsKey(name)) {
			// remember type
			type = this.vertex_fields.get(name);
			separator = FIELD_SEPARATOR;
		}
		else {
			separator = LOCAL_SEPARATOR;
		}
		// an auxiliary array for cluster type(s)
		ArrayList<String> cluster_names = new ArrayList<String>();
		// a storage for result source
		StringBuilder source = new StringBuilder();
		// save cluster type - for child type save both - a and b
		switch (cl_short) {
		case A:		cluster_names.add("a");
					break;
		case B: 	cluster_names.add("b");
					break;
		case C:		cluster_names.add("c");
					break;
		default: 	assert cl_short == ClusterShort.CHILD;
					cluster_names.add("a");
					cluster_names.add("b");
		}
		source.append(separator);		
		// append the key - vertex short
		switch (ver_short) {
		case LEFT:		source.append("left" + separator + name);
						break;
		case RIGHT:		source.append("right" + separator + name);
						break;
		case COMMON:	source.append("common" + separator + name);
						break;
		default:		assert ver_short == VertexShort.BORDER;
						source.append("border" + separator + name);
		}
		// for each cluster (one or two - a, b)
		String full_source;
		for (String cl_name : cluster_names) {
			full_source = cl_name + source.toString();
			// if the variable isn't saved in local list then put it in
			if (!this.local_fields.containsKey(full_source)) {
				this.local_fields.put(full_source, type);
			}
			else if (separator.matches(FIELD_SEPARATOR)) {
				// if it is saved and variable is declared in cluster: read type
				DataType local_type = this.local_fields.get(full_source);
				assert local_type != null;
				assert local_type == type;
			}
			else {
				// read type, check variable type
				assert separator.matches(LOCAL_SEPARATOR);
				DataType local_type = this.local_fields.get(full_source);
				TFLChecker.checkInitializationOfVariable(cl_name + "." + ver_short.toString().toLowerCase()
						+ "." + name, local_type != null);
				if (type == null) {
					type = local_type;
				} else {
					assert local_type == type;
				}
			}
		}
		// create and return new variable
		if (cluster_names.size() == 1) {
			return new TFLExpression(type, cluster_names.get(0) + source.toString());
		}
		else {
			assert cluster_names.size() == 2;
			// we need to remember the short
			return new TFLExpression(type, "child" + source.toString());
		}
	}
	
	
	/**
	 * This method creates and return object that represents numerical variable.
	 * <p>
	 * It checks that the variable is initialized and its type is numerical.
	 * 
	 * @param var_name A name of the variable
	 * @return An object representing the variable.
	 * @throws TFLException If the variable is not initialized or it is not number.
	 */
	protected TFLExpression numericExpressionFromVariable(String var_name) throws TFLException {
		// variable must be initialized
		TFLChecker.checkInitializationOfVariable(var_name, this.local_fields.containsKey(var_name));
		// variable must be numerical
		DataType data_type = this.local_fields.get(var_name);
		TFLChecker.checkNumericTypeOfVariable(var_name, data_type);
		// create and return numerical variable
		return new TFLExpression(data_type, var_name);
	}


	/**
	 * This method connects both string expressions and returns the result.
	 * 
	 * @param expr1 First string expression.
	 * @param expr2 Second string expression.
	 * @return A string expression.
	 * @throws TFLException If any of expressions is not string.
	 */
	protected TFLExpression connectStringExpressions(TFLExpression expr1, TFLExpression expr2)
	throws TFLException {
		// both expressions must be string
		TFLChecker.checkDataTypeOfExpression(expr1, DataType.STRING);
		TFLChecker.checkDataTypeOfExpression(expr2, DataType.STRING);
		// just connect the expressions
		ArrayList<String> source = new ArrayList<String>();
		source.addAll(expr1.getSource());
		source.add(" + ");
		source.addAll(expr2.getSource());
		// return the result string expression
		return new TFLExpression(DataType.STRING, source);
	}
	
	
	/**
	 * This method returns a numerical expression that is composed from received numerical expressions by
	 * applying the arithmetic operator.
	 * 
	 * @param op A type of arithmetic operator.
	 * @param expr1 First numeric expression.
	 * @param expr2 Second numeric expression.
	 * @return A numeric expression that was composed from received type by applying the operator.
	 * @throws TFLException If any of received expressions is not number or their types are different.
	 */
	protected TFLExpression connectByArithmeticOperator(ArithmeticOperetor op, TFLExpression expr1,
			TFLExpression expr2)
	throws TFLException {
		// both expressions must be numbers and their type must be the same
		TFLChecker.checkNumericExpression(expr1);
		TFLChecker.checkNumericExpression(expr2);
		TFLChecker.checkSameTypeOfTwoExpressions(expr1, expr2);
		// prepare storage for the code of connected numeric expressions
		StringBuilder source_buff = new StringBuilder();
		// first compose the name of called static method - start with class
		switch (expr1.getDataType()) {
		case INTEGER:	source_buff.append("InfiniteInteger.");
						break;
		case REAL:		source_buff.append("InfiniteReal.");
						break;
		default:		assert false;
		}
		// append the method name
		switch (op) {
		case PLUS:		source_buff.append("sum(");
						break;
		case MINUS:		source_buff.append("difference(");
						break;
		case TIMES:		source_buff.append("product(");
						break;
		case DIVIDE:	source_buff.append("quotient(");
						break;
		default:		assert false;
		}
		// append codes of both expressions
		ArrayList<String> source = new ArrayList<String>();
		source.add(source_buff.toString());
		source.addAll(expr1.getSource());
		source.add(", ");
		source.addAll(expr2.getSource());
		source.add(")");
		// return the result expression of numerical type
		return new TFLExpression(expr1.getDataType(), source);
	}
	
	
	/**
	 * This method just enwraps received expression by parenthesis and returns it.
	 * 
	 * @param expr An expression.
	 * @return The expression enwraped by parenthesis.
	 */
	protected TFLExpression enwrapExpressionByParenthesis(TFLExpression expr) {
		// just enwrap the expression by parenthesis
		ArrayList<String> source = new ArrayList<String>();
		source.add("(");
		source.addAll(expr.getSource());
		source.add(")");
		// return enwraped expression
		return new TFLExpression(expr.getDataType(), source);
	}
	
	
	/**
	 * This method just connects both numerical expressions by comma and returns the result expression.
	 * The data type of the result is same as data type of both expressions.
	 * 
	 * @param expr1 First numerical expression.
	 * @param expr2 Second numerical expression.
	 * @return An expression that is composed from both received expressions by comma connection.
	 * @throws TFLException
	 */
	protected TFLExpression connectNumericExpressionsByComma(TFLExpression expr1, TFLExpression expr2)
	throws TFLException {
		// both expressions must be numbers and their type must be the same
		TFLChecker.checkNumericExpression(expr1);
		TFLChecker.checkNumericExpression(expr2);
		TFLChecker.checkSameTypeOfTwoExpressions(expr1, expr2);
		// connect expressions by comma
		ArrayList<String> source = new ArrayList<String>();
		source.addAll(expr1.getSource());
		source.add(", ");
		source.addAll(expr2.getSource());
		// return the result
		return new TFLExpression(expr1.getDataType(), source);
	}
	
	
	/**
	 * This method generates and returns an boolean expression that represents exists function.
	 * It generates a java code according to type of the cluster whose left boundary is tested to 
	 * existence.
	 * 
	 * @param cl_s A type of the cluster (A or B).
	 * @return A boolean expression that responds to the exists test.
	 */
	protected TFLExpression enwrapVertexByExistsFunction(ClusterShort cl_s) {
		// prepare storage for result
		String str = null;
		// according to vertex type generate java code
		switch (cl_s) {
		case A:		str = "(a.getBu() != null)";
					break;
		default:	assert cl_s == ClusterShort.B;
					str = "(b.getBu() != null)";
		}
		// return the result
		return new TFLExpression(DataType.BOOLEAN, str);
	}
	
	
	/**
	 * This method just checks if the type of received expression is numeric. If it is not then
	 * the exception is thrown.
	 * 
	 * @param expr A numeric expression to check.
	 * @throws TFLException If the expression is not numeric.
	 */
	protected void checkNumericExpression(TFLExpression expr) throws TFLException {
		// just check it
		TFLChecker.checkNumericExpression(expr);
	}
	
	
	/**
	 * This method generates and returns code of extreme function with arguments from received 
	 * numerical expression.
	 * <p>
	 * The method check the type of the expression and then it enwraps the expression code by the calling
	 * of extreme function.
	 * 
	 * @param expr A numeric expression
	 * @param func_name A name of extreme function: "maximum" or "minimum"
	 * @return The new numerical expression
	 * @throws TFLException If the expression is not number.
	 */
	protected TFLExpression enwrapExpressionByExtremFunction(TFLExpression expr, String func_name)
	throws TFLException {
		// only allowed extreme is maximum and minimum
		assert func_name.matches("maximum") || func_name.matches("minimum");
		// expression must be a number
		TFLChecker.checkNumericExpression(expr);
		// remember the type of expression
		DataType type = expr.getDataType();
		// compose the result: insert name of the function and arguments
		StringBuilder str_buff = new StringBuilder();
		str_buff.append(type == DataType.INTEGER ? "InfiniteInteger." : "InfiniteReal.");
		str_buff.append(func_name + "(");
		ArrayList<String> source = new ArrayList<String>();
		source.add(str_buff.toString());
		source.addAll(expr.getSource());
		source.add(")");
		// return new numeric expression
		return new TFLExpression(type, source);
	}
	
	
	/**
	 * This method generates and returns code of two numbers comparison.
	 * <p>
	 * Both expressions must have the same numerical type. The method composes the java code where on
	 * the first number there is called comparison method with second number as an argument.
	 * 
	 * @param op A type of comparison operator.
	 * @param expr1 First numerical expression.
	 * @param expr2 Second numerical expression.
	 * @return A composed boolean expression.
	 * @throws TFLException
	 */
	protected TFLExpression connectByCopmarisonOperator(ComparisonOperetor op, TFLExpression expr1,
			TFLExpression  expr2)
	throws TFLException {
		// both expressions must be numbers and their type must be the same
		TFLChecker.checkNumericExpression(expr1);
		TFLChecker.checkNumericExpression(expr2);
		TFLChecker.checkSameTypeOfTwoExpressions(expr1, expr2);
		// compose the calling of comparison method
		String operator = null;
		switch (op) {
		case EQUAL:			operator = ".isEqualTo(";
							break;
		case NOT_EQUAL:		operator = ".isNotEqualTo(";
							break;
		case GREATER:		operator = ".isGreaterThen(";
							break;
		case GREATER_EQUAL:	operator = ".isGreaterOrEqualTo(";
							break;
		case LESS:			operator = ".isLessThen(";
							break;
		default:			assert op == ComparisonOperetor.LESS_EQUAL;
							operator = ".isLessOrEqualTo(";
		}
		// compose all code
		ArrayList<String> source = new ArrayList<String>();
		source.addAll(expr1.getSource());
		source.add(operator);
		source.addAll(expr2.getSource());
		source.add(")");
		// return new boolean expression
		return new TFLExpression(DataType.BOOLEAN, source);
	}
	
	
	/**
	 * This method generates and returns boolean expression of logical condition.
	 * <p>
	 * Both expressions must have boolean type. The method inserts boolean operator between expressions.
	 * 
	 * @param op A type of boolean operator.
	 * @param expr1 First boolean expression.
	 * @param expr2 Second boolean expression.
	 * @return A composed boolean expression.
	 * @throws TFLException If any of expressions is not boolean.
	 */
	protected TFLExpression connectByBooleanOperator(BooleanOperetor op, TFLExpression expr1,
			TFLExpression expr2)
	throws TFLException {
		// both expression must be booleans
		TFLChecker.checkDataTypeOfExpression(expr1, DataType.BOOLEAN);
		TFLChecker.checkDataTypeOfExpression(expr2, DataType.BOOLEAN);
		// prepare code for operator
		String operator = null;
		switch (op) {
		case AND:	operator = " && ";
					break;
		default:	assert op == BooleanOperetor.OR;
					operator = " || ";
		}
		// compose result expression
		ArrayList<String> source = new ArrayList<String>();
		source.addAll(expr1.getSource());
		source.add(operator);
		source.addAll(expr2.getSource());
		// return new boolean expression
		return new TFLExpression(DataType.BOOLEAN, source);
	}
	
	
	/**
	 * This method generates and returns source code of assignment operation. Source code of
	 * second expression is assigned (according to operator type) to the first variable expression.
	 * <p>
	 * First expression must be a variable and if the operator is not $=$ then it must be already
	 * initialized. The method composes the code of assignment - this code starts with special character 
	 * {@link TFLCompiler#OPEN_ASSIGNMENT} on the first position of created array and ends with
	 * {@link TFLCompiler#CLOSE_ASSIGNMENT} on the last position of created array. On the second
	 * position there is the code of the variable following by the code of assigning composed
	 * with the code of the right side expression. The code of assigning differs according to the
	 * type of assignment operator.
	 * 
	 * @param op A type of assignment operator.
	 * @param expr1 An expression of variable on the left side of the assignment.
	 * @param expr2 An expression that is assigned into the variable.
	 * @return A source code of the operation.
	 * @throws TFLException If data type checking doesn't pass or the variable is used before
	 *  the initialization. 
	 */
	protected ArrayList<String> connectByAssignmentOperator(AssignmentOperetor op, TFLExpression expr1,
			TFLExpression expr2)
	throws TFLException {
		// first expression must be a variable - take it and its type
		assert expr1.getSource().size() == 1;
		String var_name = expr1.getSource().get(0);
		DataType type = expr1.getDataType();
		// check operator
		if (op != AssignmentOperetor.ASSIGN) {
			TFLChecker.checkInitializationOfVariable(
					var_name.replaceAll("[" + FIELD_SEPARATOR + LOCAL_SEPARATOR + "]", "."),
					type != null);
		}
		// if cluster short is CHILD then we must create two real names - a and b short
		ArrayList<String> real_name = new ArrayList<String>();
		Pattern pattern = Pattern.compile("child[" + FIELD_SEPARATOR + LOCAL_SEPARATOR + "].+");
        Matcher matcher = pattern.matcher(var_name);
		if (matcher.matches()) {
			real_name.add("a" + var_name.substring(5));
			real_name.add("b" + var_name.substring(5));
		}
		else {
			real_name.add(var_name);
		}
		// if the variable is contained in local list then types are equal, else the type is null
		assert real_name.size() == 2 ?
				((this.local_fields.containsKey(real_name.get(0))
						? this.local_fields.get(real_name.get(0)) == type : type == null)
				&& (this.local_fields.containsKey(real_name.get(1))
						? this.local_fields.get(real_name.get(1)) == type : type == null))
				:
				(this.local_fields.containsKey(real_name.get(0))
						? this.local_fields.get(real_name.get(0)) == type : type == null);
		// if type of variable is null then saves it type from right side of assignment
		if (type == null) {
			type = expr2.getDataType();
			for (String name : real_name) {
				this.local_fields.put(name, type);
			}
		}
		else {
			// else check equality of types
			TFLChecker.checkDataTypeOfExpression(expr2, type);
		}
		// prepare code for future replacing of variable shorts:
		ArrayList<String> source = new ArrayList<String>();
		source.add(OPEN_ASSIGNMENT);	// insert special character that determines begining of assingment
		source.add(var_name);
		// compose assignment according to the type of the operator
		switch (op) {
		case PLUS_ASSIGN:
			TFLChecker.checkNumericExpression(expr2);
			source.add( (type == DataType.INTEGER)
					? "InfiniteInteger.sum(" : "InfiniteReal.sum(" );
			source.add(var_name);
			source.add(", ");
			source.addAll(expr2.getSource());
			source.add(")");
			break;
		case MINUS_ASSIGN:
			TFLChecker.checkNumericExpression(expr2);
			source.add( (type == DataType.INTEGER)
					? "InfiniteInteger.difference("	: "InfiniteReal.difference(" );
			source.add(var_name);
			source.add(", ");
			source.addAll(expr2.getSource());
			source.add(")");
			break;
		case TIMES_ASSIGN:
			TFLChecker.checkNumericExpression(expr2);
			source.add( (type == DataType.INTEGER)
					? "InfiniteInteger.product(" : "InfiniteReal.product(" );
			source.add(var_name);
			source.add(", ");
			source.addAll(expr2.getSource());
			source.add(")");
			break;
		case DIVIDE_ASSIGN:
			TFLChecker.checkNumericExpression(expr2);
			source.add( (type == DataType.INTEGER)
					? "InfiniteInteger.quotient(" : "InfiniteReal.quotient(" );
			source.add(var_name);
			source.add(", ");
			source.addAll(expr2.getSource());
			source.add(")");
			break;
		case AMPERSAND_ASSIGN:
			TFLChecker.checkDataTypeOfExpression(expr2, DataType.STRING);
			source.add(var_name);
			source.add(" + ");
			source.addAll(expr2.getSource());
			break;
		default:
			assert op == AssignmentOperetor.ASSIGN;
			/* any type can be used with = */
			source.addAll(expr2.getSource());
		}
		// insert special char to the end
		source.add(CLOSE_ASSIGNMENT);
		// return only source code - all data types checks were already done
		return source;
	}
	
	
	/**
	 * This method composes and returns complete if-elseif-...-else block. The condition must be boolean
	 * expression.
	 * <p>
	 * The method just sticks the parts of the block together.
	 * 
	 * @param condition A boolean condition of the if-block.
	 * @param any_code A code of the if-block.
	 * @param else_ifs A prepared code of else-if-blocks.
	 * @param else_block A prepared code of else-block.
	 * @return A resulting source code of completed block.
	 * @throws TFLException If the condition expression doesn't have boolean type.
	 */
	protected ArrayList<String> createIfBlock(TFLExpression condition, ArrayList<String> any_code,
			ArrayList<String>  else_ifs, ArrayList<String> else_block)
	throws TFLException {
		// condition must have boolean type
		TFLChecker.checkDataTypeOfExpression(condition, DataType.BOOLEAN);
		// compose java code of the if-block
		ArrayList<String> source = new ArrayList<String>();
		source.add("if (");
		source.addAll(condition.getSource());
		source.add(") {\n");
		source.addAll(any_code);
		source.add("}\n");
		// append else-if parts
		source.addAll(else_ifs);
		// append else part
		source.addAll(else_block);
		// return source code of the result
		return source;
	}
	
	
	/**
	 * This method composes a code of one else-if-block, appends it after prepared else-if-blocks
	 * and returns the result. The condition must be boolean expression.
	 * <p>
	 * The method just sticks the parts of the block together and appends it after the other blocks.
	 * 
	 * @param else_ifs A code of prepared else-if-blocks.
	 * @param condition A boolean condition of the else-if-block.
	 * @param any_code A code of the else-if-block.
	 * @return A resulting source code of completed else-if-blocks.
	 * @throws TFLException If the condition expression doesn't have boolean type.
	 */
	protected ArrayList<String> createElseIfBlock(ArrayList<String> else_ifs, TFLExpression condition,
			ArrayList<String> any_code)
	throws TFLException {
		// condition must have boolean type
		TFLChecker.checkDataTypeOfExpression(condition, DataType.BOOLEAN);
		// compose java code of the elseif-block and append it after prepared blocks
		ArrayList<String> source = new ArrayList<String>();
		source.addAll(else_ifs);
		source.add("elseif (");
		source.addAll(condition.getSource());
		source.add(") {\n");
		source.addAll(any_code);
		source.add("}\n");
		// return source code of the result
		return source;
	}
	
	
	/**
	 * This method composes a code of else-block and returns the result.
	 * <p>
	 * The method just sticks the parts of the block together.
	 * 
	 * @param any_code A code of the else-block.
	 * @return A resulting source code of completed else-block.
	 */
	protected ArrayList<String> createElseBlock(ArrayList<String> any_code) {
		// compose java code of the else-block
		ArrayList<String> source = new ArrayList<String>();
		source.add("else {\n");
		source.addAll(any_code);
		source.add("}\n");
		// return source code of the result
		return source;
	}
	
	
	/**
	 * This method composes a code of select-if-else-block and returns the result. The condition must
	 * be boolean expression.
	 * <p>
	 * The method just sticks the parts of the block together like ordinary if-else-block.
	 * 
	 * @param condition A boolean condition of the if-block.
	 * @param any_code1 A code of the if-block.
	 * @param any_code2 A code of the else-block.
	 * @return A resulting source code of completed selectIf-else-block.
	 * @throws TFLException If the condition expression doesn't have boolean type.
	 */
	protected ArrayList<String> connectSelectIf(TFLExpression condition, ArrayList<String> any_code1,
			ArrayList<String> any_code2) throws TFLException {
		// condition must have boolean type
		TFLChecker.checkDataTypeOfExpression(condition, DataType.BOOLEAN);
		// compose java code of the selectIf-blocks
		ArrayList<String> source = new ArrayList<String>();
		source.add("if (");
		source.addAll(condition.getSource());
		source.add(") {\n");
		source.addAll(any_code1);
		source.add("return a;\n}\nelse {\n");
		source.addAll(any_code2);
		source.add("return b;\n}\n");
		// return source code of the result
		return source;
	}
	
	
	/**
	 * This method puts the code into {@link TFLCompiler#method_parts} on position of the received block.
	 * If the position is already used then the exception is thrown.
	 * 
	 * @param name A type of the block
	 * @param any_code A source code.
	 * @throws TFLException If the block is already initialized.
	 */
	protected void registerBuildingBlock(BuildingBlock name, ArrayList<String> any_code)
	throws TFLException {
		// check if the block is free and then insert the code into the array
		TFLChecker.checkIfBuildingBlockIsFree(name, !this.method_parts.containsKey(name));
		this.method_parts.put(name, any_code);
	}
	
	
	/**
	 * This method puts the code into {@link TFLCompiler#method_parts} on position of each block
	 * contained in received array. If the position is already used then the exception is thrown.
	 * 
	 * @param block_names An array of block names.
	 * @param any_code A source code.
	 * @throws TFLException If any block is already initialized.
	 */
	protected void registerBuildingBlocks(ArrayList<BuildingBlock> block_names,
			ArrayList<String> any_code)
	throws TFLException {
		// for each block insert the code into the array
		for (BuildingBlock name : block_names) {
			TFLChecker.checkIfBuildingBlockIsFree(name, !this.method_parts.containsKey(name));
			this.method_parts.put(name, any_code);
		}
	}
	
	
	/**
	 * This method concatenates the codes and inserts the result into all building blocks.
	 * <p>
	 * If the codes are empty then nothing is done.
	 * 
	 * @param any_code1 First code.
	 * @param any_code2 Second code.
	 */
	protected void registerBlocksForSelect(ArrayList<String> any_code1, ArrayList<String> any_code2) {
		// connect both blocks
		ArrayList<String> block = new ArrayList<String>();
		if (any_code1 != null)
			block.addAll(any_code1);
		if (any_code2 != null)
			block.addAll(any_code2);
		// insert the code into all blocks
		if (!block.isEmpty()) {
			this.method_parts.put(BuildingBlock.PATH_AND_PATH, block);
			this.method_parts.put(BuildingBlock.PATH_AND_POINT, block);
			this.method_parts.put(BuildingBlock.POINT_AND_PATH, block);
			this.method_parts.put(BuildingBlock.POINT_AND_POINT, block);
			this.method_parts.put(BuildingBlock.LPOINT_OVER_RPOINT, block);
			this.method_parts.put(BuildingBlock.RPOINT_OVER_LPOINT, block);
			this.method_parts.put(BuildingBlock.LPOINT_AND_RPOINT, block);
		}
	}
	
	
	/**
	 * This method generates all necessary imports for TopTreeListener implementing class.
	 * <p>
	 * Classes {@link toptree.Cluster} and {@link toptree.TopTreeListener} are imported always.
	 * If the class uses numbers then appropriate class from {@link numbers} is imported.
	 * 
	 * @return The source code that contains all necessary imports.
	 */
	private String generateAlgorithmImports() {
		// compose source of imports
		StringBuilder result = new StringBuilder();
		// imports for Cluster and TopTreeListener
		result.append("import ").append(Cluster.class.getName()).append(";\n");
		result.append("import ").append(TopTreeListener.class.getName()).append(";\n");
		// if integer is used then generate import for InfiniteInteger
		if (this.imported_types.contains(DataType.INTEGER)) {
			result.append("import ").append(InfiniteInteger.class.getName()).append(";\n");
		}
		// if real is used then generate import for InfiniteReal
		if (this.imported_types.contains(DataType.REAL)) {
			result.append("import ").append(InfiniteReal.class.getName()).append(";\n");
		}
		// if numerical type is used then generate import for InfiniteNumber.Finiteness
		if (this.imported_types.contains(DataType.INTEGER)
				|| this.imported_types.contains(DataType.REAL)) {
			result.append("import ").append(InfiniteNumber.class.getName()).append(".Finiteness")
			.append(";\n");
		}
		result.append("\n");
		// return the result code
		return result.toString();

	}


	/**
	 * This method inserts important types that are used into {@link TFLCompiler#imported_types}. Important
	 * are only integer and real.
	 */
	private void rememberNecessaryImports() {
		// important are only integer and real
		if (this.local_fields.containsValue(DataType.INTEGER) 
				&& !this.imported_types.contains(DataType.INTEGER)) {
			this.imported_types.add(DataType.INTEGER);
		}
		if (this.local_fields.containsValue(DataType.REAL) 
				&& !this.imported_types.contains(DataType.REAL)) {
			this.imported_types.add(DataType.REAL);
		}
	}


	/**
	 * This method generates and returns a source code fo a header of the method according the type
	 * of the method.
	 * 
	 * @param method_type A type of the method in algorithm class
	 * @return The result source code of the method header.
	 */
	private String generateMethodHeader(RootBlock method_type) {
		// compose header according to method type
		StringBuilder result = new StringBuilder();
		// remember the cluster type source code - will be needed moretimes
		String cluster_type = "Cluster<" + getClusterClassName() + "," + getVertexClassName() + ">";
		// according to the method_type generate the header
		switch (method_type) {
		case CREATE:
			result.append("public void create(" + cluster_type + " c, " + "Cluster.ClusterType type) {\n");
			break;
		case DESTROY:
			result.append("public void destroy(" + cluster_type + " c, " + "Cluster.ClusterType type) {\n");
			break;
		case JOIN:
			result.append(
				"public void join(" + cluster_type + " c, " + cluster_type + " a, " + cluster_type
				+ " b, Cluster.ConnectionType type) {\n");
			break;
		case SPLIT:
			result.append(
					"public void split(" + cluster_type + " a, " + cluster_type + " b, " + cluster_type
					+ " c, Cluster.ConnectionType type) {\n");
			break;
		default:
			assert method_type == RootBlock.SELECT;
			result.append(
					"public " + cluster_type + " selectQuestion(" + cluster_type + " a, "
					+ cluster_type + " b, Cluster.ConnectionType type) {\n");
		}
		// return the source code of the method header
		return result.toString();
	}


	/**
	 * The method just returns predefined string of the method footer.
	 * 
	 * @return A source code of the method footer.
	 */
	private String generateMethodFooter() {
		// just return footer
		return "}\n\n";
	}


	/**
	 * This method generates and returns a source code of declarations of all local variables.
	 * <p>
	 * For JOIN, SPLIT and SELECT the short common is generated. And for JOIN and SPLIT also border.
	 * Then the method generates appropriate java declaration for each local variable from
	 * {@link TFLCompiler#local_fields} that is not declared in vertex, cluster or var block.
	 * 
	 * @param method_name A type of the root block.
	 * @return A source code all local declarations.
	 */
	private String generateLocalFields(RootBlock method_name) {
		// compose declarations of local variables
		StringBuilder result = new StringBuilder();
		// prepare variables for name and type of the field
		String name = null;
		DataType type = null;
		// generate border and common shorts
		if (method_name == RootBlock.JOIN || method_name == RootBlock.SPLIT
				|| method_name == RootBlock.SELECT) {
			if (method_name != RootBlock.SELECT) {
				result.append(getVertexClassName() + " border = null;\n");
			}
			result.append(getVertexClassName() + " common = null;\n");
		}
		// for each local field generate one java variable
		Set<Map.Entry<String, DataType>> all_fields = this.local_fields.entrySet();
		for (Entry<String, DataType> field: all_fields) {
			name = field.getKey();
			type = field.getValue();
			if (this.var_fields.containsKey(name) || name.contains(FIELD_SEPARATOR)) {
				// declared variable in vertex, cluster or var block
				continue;
			}
			if (name.contains(LOCAL_SEPARATOR)) {
				// replace the separator
				name = name.replace(LOCAL_SEPARATOR, LOCAL_SEPARATOR_REPLACEMENT);
			}
			// prepare a source code of the declaration
			switch (type) {
			case INTEGER:	result.append("InfiniteInteger " + name + " = null;\n");
							break;
			case REAL:		result.append("InfiniteReal " + name + " = null;\n");
							break;
			case STRING:	result.append("String " + name + " = null;\n");
							break;
			case BOOLEAN:	result.append("Boolean " + name + " = null;\n");
							break;
			default:		assert false;
			}
		}
		// return the results
		return result.toString();
	}


	/**
	 * This method checks the form of all variables with shorts in the code. If any variable hasn't allowed
	 * form then the exception is thrown.
	 * <p>
	 * The method first prepares regular expressions that the variable with a short has to match.
	 * Next, all variables are checked. If the variable doesn't match any of allowed regular expression
	 * of the block then the exception is thrown.
	 * 
	 * @param method_name A type of the root block.
	 * @param block_name A type of the building block.
	 * @param any_code A source code with variables that must be checked.
	 * @throws TFLException If the form of the variable is not allowed in the block.
	 */
	private void checkFormOfVariables(RootBlock method_name, BuildingBlock block_name,
			ArrayList<String> any_code)
	throws TFLException {
		
		// prepare the array of allowed variable forms for each block
		ArrayList<String> regexps = new ArrayList<String>();
		ArrayList<String> separators = new ArrayList<String>();
		separators.add(FIELD_SEPARATOR);
		separators.add(LOCAL_SEPARATOR);
		// regular expressions shorts ordinary
		String  s	= "[" + FIELD_SEPARATOR + LOCAL_SEPARATOR + "]";
		String  v	= "[a-zA-Z][a-zA-Z0-9_]*";
		// regular expressions shorts for cluster shorts
		String  c	= "c";
		String ab	= "(a|b)";
		String abc	= "(a|b|c)";
		String chc	= "(child|c)";
		// regular expressions shorts for vertex shorts
		String  LR	= "(left|right)";
		String  LRM	= "(left|right|common)";
		String  MD	= "(common|border)";
		String aLR	= "\\[(left|right)\\]";
		String aLRM	= "\\[(left|right|common)\\]";
		String aLRMD= "\\[(left|right|common|border)\\]";
		String  M	= "common";
		String aM	= "\\[common\\]";
		String aMD	= "\\[(common|border)\\]";
		// define allowed forms of variable shorts for each variable and check the variable
		for (String source : any_code) {
			if (source.contains(FIELD_SEPARATOR) || source.contains(LOCAL_SEPARATOR)) {
				// it contains some separator => it is variable with shorts => check it
				if (block_name == BuildingBlock.PATH || block_name == BuildingBlock.POINT) {
					regexps.add(c + s + v);
					regexps.add(c + s + v  + aLR);
					regexps.add(c + s + LR + s + v);
				}
				else if (block_name == BuildingBlock.PATH_CHILD) {
					regexps.add(chc + s + v);
					regexps.add(chc + s + v + aLRM);
				 	regexps.add( c  + s + M  + s + v);
					regexps.add(chc + s + LR + s + v);
				}
				else if (block_name == BuildingBlock.POINT_CHILD) {
					regexps.add(chc + s + v);
					regexps.add(chc + s + v + aM);
					regexps.add( c  + s + M + s + v);
				}
				else if (block_name == BuildingBlock.PATH_PARENT) {
					regexps.add(abc + s + v);
					regexps.add(abc + s + v + aM);
					regexps.add( c  + s + v + aLR);
					regexps.add( c  + s + LRM + s + v);
				}
				else if (block_name == BuildingBlock.POINT_PARENT) {
					regexps.add(abc + s + v);
					regexps.add(abc + s + v  + aMD);
					regexps.add( c  + s + MD + s + v);
				}
				else if (block_name == BuildingBlock.PATH_AND_PATH
						|| block_name == BuildingBlock.PATH_AND_POINT
						|| block_name == BuildingBlock.POINT_AND_PATH) {
					regexps.add(abc + s + v);
					regexps.add(abc + s + v + aLRM);
					regexps.add( c  + s + M  + s + v);
					regexps.add(abc + s + LR + s + v);
				}
				else if (block_name == BuildingBlock.POINT_AND_POINT) {
					regexps.add(abc + s + v);
					regexps.add(abc + s + v + aLRMD);
					regexps.add( c  + s + MD + s + v);
					regexps.add(ab  + s + LR + s + v);
				}
				else if (block_name == BuildingBlock.LPOINT_OVER_RPOINT
						|| block_name == BuildingBlock.RPOINT_OVER_LPOINT
						|| block_name == BuildingBlock.LPOINT_AND_RPOINT) {
					regexps.add( abc + s + v);
					regexps.add( abc + s + v + aLRMD);
					regexps.add(  c  + s + MD + s + v);
					regexps.add( abc + s + LR + s + v);
				}
				else {
					assert false;
				}
				TFLChecker.checkVariableForm(source, regexps, method_name, block_name, separators);
			}
			else if (source.matches("\\([a|b]\\.(getBu\\(\\)|getBv\\(\\)) != null\\)")) {
				// exists function condition
				TFLChecker.checkExistsInBlock(method_name, block_name);
			}

		}
	}


	/**
	 * This method replaces all occurrences of cluster short CHILD by the real cluster name (a or b) and 
	 * returns the result.
	 * 
	 * @param child_name A real name of the child - the replacement for CHILD.
	 * @param orig_any_code A code where the replacing will by applied.
	 * @return A source code with finished replacements.
	 */
	private ArrayList<String> replaceChildShort(String child_name, ArrayList<String> orig_any_code) {
		// prepare storage for the result
		ArrayList<String> new_any_code = new ArrayList<String>();
		// replace each CHILD by child_name
		if (orig_any_code != null) {
			for (String code_part : orig_any_code) {
				if (code_part.length() > 6
						&& code_part.substring(0, 6).matches("child[" + FIELD_SEPARATOR + LOCAL_SEPARATOR
								+ "]")) {
					new_any_code.add(code_part.replaceFirst("child", child_name));
				}
				else {
					new_any_code.add(code_part);
				}
			}
		}
		// return the result
		return new_any_code;
	}


	/**
	 * This method composes a java source code of the boundary vertex according to the cluster name 
	 * and vertex short and returns the result.
	 * 
	 * @param cluster A string with cluster name.
	 * @param vertex A string with vertex short.
	 * @return A source code of the boundary.
	 */
	private String generateClusterBoundaryCode(String cluster, String vertex) {
		// replace boundary short by appropriate java code and return the result
		if (vertex.matches("left")) {
			return cluster + "." + "getBu()";
		}
		else if (vertex.matches("right")) {
			return cluster + "." + "getBv()";
		}
		else if (vertex.matches("common")) {
			return "common";
		}
		else {
			assert vertex.matches("border");
			return "border";
		}
	}


	/**
	 * This method generates and returns java source code of calling getter of setter from received
	 * variable.
	 * <p>
	 * The method must remember for cluster c in JOIN block if the field has been initialized yet. Then
	 * it can check the initialization of these field before the first reading from them.
	 * <p>
	 * There must be distinguished three types of variables: a field of the boundary vertex, a field
	 * of the cluster (not array), an array field of the cluster. According to the type the
	 * method generates calling of appropriate class method (getter or setter) calling.
	 * 
	 * @param variable A code of a variable.
	 * @param c_init_checker An array containing all initialized fields of cluster c in JOIN block.
	 * @param method_name A type of the root block.
	 * @param is_getter A switch if getter or setter has to be generating.
	 * @return A java source code of calling getter or setter method.
	 * @throws TFLException If field of cluster c is not initialized in JOIN and a getter is going 
	 * 						to be create.
	 */
	private String generateFieldGetterOrSetter(String variable,	ArrayList<String> c_init_checker,
			RootBlock method_name, Boolean is_getter)
	throws TFLException {
		// regexps for next if: first has to match and second has not to
		String regexp_match 	= "c"+FIELD_SEPARATOR+".+";
		String regexp_not_match = "c"+FIELD_SEPARATOR+"(left|right|border|common)"+FIELD_SEPARATOR+".+";
		// initialization of field in c cluster
		if (method_name == RootBlock.JOIN 
				&& variable.matches(regexp_match) && !variable.matches(regexp_not_match)
				&& !c_init_checker.contains(variable)) {
			if (is_getter) {
				// check initialization of field in c cluster when a cluster field getter is created
				TFLChecker.checkInitializationOfVariable(
						variable.replaceAll("[" + FIELD_SEPARATOR + LOCAL_SEPARATOR + "]", "."), false);
			} else {
				// remember initialization of field in c cluster if is just created
				c_init_checker.add(variable);
			}
		}
		// prepare storage for result
		StringBuilder result = new StringBuilder();
		// divide variable by field separator
		String[] parts = variable.split(FIELD_SEPARATOR);
		// compose the result
		if (parts.length == 3) {
			// a field in vertex
			result.append(generateClusterBoundaryCode(parts[0], parts[1]) + ".");
			if (is_getter) {
				result.append(TFLMethodNameCreator.createMethodName(MethodTypes.GET, parts[2]) + "()");
			}
			else {
				result.append(TFLMethodNameCreator.createMethodName(MethodTypes.SET, parts[2]) + "(");
			}
		}
		else {
			// a field in cluster
			assert parts.length == 2;
			if (parts[1].contains("[")) {
				// an array: split by [ and ] to take a key
				String[] sec_parts = parts[1].split("[\\[\\]]");
				result.append(parts[0] + ".getInfo().");
				// generate getting or setting
				if (is_getter) {
					result.append(
							TFLMethodNameCreator.createMethodName(MethodTypes.GET, sec_parts[0] + "("));
					result.append(generateClusterBoundaryCode(parts[0], sec_parts[1]) + ")");
				}
				else {
					result.append(
							TFLMethodNameCreator.createMethodName(MethodTypes.SET, sec_parts[0] + "("));
					result.append(generateClusterBoundaryCode(parts[0], sec_parts[1]) + ", ");
				}
			}
			else {
				// not array - just generate getting or setting
				result.append(parts[0] + ".getInfo().");
				if (is_getter) {
					result.append(TFLMethodNameCreator.createMethodName(MethodTypes.GET, parts[1]) + "()");
				}
				else {
					result.append(TFLMethodNameCreator.createMethodName(MethodTypes.SET, parts[1]) + "(");
				}
			}

		}
		// return a source code of the result
		return result.toString();
	}


	/**
	 * This method generates a java source code for the switch case specified by building and root block
	 * and returns the result.
	 * <p>
	 * First, the method composes all received codes together in right order according to the root block.
	 * Next, it generates local variable common for JOIN, SPLIT and SELECT blocks and border for JOIN
	 * and SPLIT. After, the method takes parts from array of codes and if it is variable then the 
	 * method replaces the shorts and generates java code. If it is not a variable, the code is just 
	 * append to the result. In the end the result java source code is returned.
	 * 
	 * @param method_name A type of the root block.
	 * @param block_name A type of the building block.
	 * @param a_code A special code for cluster a (from *_child block).
	 * @param b_code A special code for cluster b (from *_child block).
	 * @param c_code A special code for cluster c (from *_parent block).
	 * @param block_code A code common for all clusters.
	 * @return A java source code generated for the case specified by building block.
	 * @throws TFLException If field of cluster c is not initialized in JOIN and a getter is going 
	 * 						to be create.
	 */
	private String generateCaseInMethod(RootBlock method_name, BuildingBlock block_name,
			ArrayList<String> a_code, ArrayList<String> b_code,
			ArrayList<String> c_code, ArrayList<String> block_code)
	throws TFLException {
		// compose all codes together in correct order
		ArrayList<String> glued_any_codes = new ArrayList<String>();
		if (method_name == RootBlock.JOIN) {
			if (a_code != null)		glued_any_codes.addAll(a_code);
			if (b_code != null)		glued_any_codes.addAll(b_code);
			if (c_code != null)		glued_any_codes.addAll(c_code);
			if (block_code != null)	glued_any_codes.addAll(block_code);
		}
		if (method_name == RootBlock.SPLIT) {
			if (c_code != null)		glued_any_codes.addAll(c_code);
			if (a_code != null)		glued_any_codes.addAll(a_code);
			if (b_code != null)		glued_any_codes.addAll(b_code);
			if (block_code != null)	glued_any_codes.addAll(block_code);
		}
		if (method_name == RootBlock.SELECT || method_name == RootBlock.CREATE ||
				method_name == RootBlock.DESTROY) {
			if (block_code != null)	glued_any_codes.addAll(block_code);
		}
		// prepare storage for result
		StringBuilder result = new StringBuilder();
		
		// generate variable for common and border short
		if (method_name == RootBlock.JOIN || method_name == RootBlock.SPLIT
				|| method_name == RootBlock.SELECT) {
			result.append("common = a.getBv();\n");
		}
		if (method_name == RootBlock.JOIN || method_name == RootBlock.SPLIT) {
			if (block_name == BuildingBlock.POINT_AND_POINT) {
				result.append("border = a.getBv();\n");
			}
			if (block_name == BuildingBlock.LPOINT_OVER_RPOINT) {
				result.append("border = a.getBu();\n");
			}
			if (block_name == BuildingBlock.RPOINT_OVER_LPOINT) {
				result.append("border = b.getBv();\n");
			}
			if (block_name == BuildingBlock.LPOINT_AND_RPOINT) {
				result.append("border = a.getBv();\n");
				
			}
		}
		// a switch to remember that we works with left side of assignment
		boolean left_side_of_assignment = false;
		// a switch to remember that we must close the setter calling after preparing the arguments
		boolean close_setter = false;
		ArrayList<String> c_init_checker = new ArrayList<String>();
		
		// generate java source code from glued_any_codes
		for (String code : glued_any_codes) {
			if (code.matches(OPEN_ASSIGNMENT)) {
				// open assignment signs left side
				left_side_of_assignment = true;
			}
			else if (code.matches(CLOSE_ASSIGNMENT)) {
				// close command
				if (close_setter) {
					result.append(");\n");
					close_setter = false;
				}
				else {
					result.append(";\n");
				}
			}
			else if (left_side_of_assignment && code.contains(FIELD_SEPARATOR)) {
				// we must generate setter for declared variable
				close_setter = true;
				result.append(generateFieldGetterOrSetter(code, c_init_checker, method_name, false));
				left_side_of_assignment = false;
			}
			else if (left_side_of_assignment && code.contains(LOCAL_SEPARATOR)) {
				// we must generate assigning into local variable
				result.append(code.replaceAll(LOCAL_SEPARATOR, LOCAL_SEPARATOR_REPLACEMENT) + " = ");
				left_side_of_assignment = false;
			}
			else if (left_side_of_assignment) {
				// just assigning
				result.append(code + " = ");
				left_side_of_assignment = false;
			}
			else if (code.contains(FIELD_SEPARATOR)) {
				// generate getter for declared field
				result.append(generateFieldGetterOrSetter(code, c_init_checker, method_name, true));
			}
			else if (code.contains(LOCAL_SEPARATOR)) {
				// just replace local separator
				result.append(code.replaceAll(LOCAL_SEPARATOR, LOCAL_SEPARATOR_REPLACEMENT));
			}
			else {
				// no variable => append to result
				result.append(code);
			}
		}
		// return composed java code
		return result.toString();
	}


	/**
	 * This method generates and returns java source code of whole switch according to the recieved 
	 * root block.
	 * <p>
	 * The method first checks a form of used variables by calling
	 * {@link TFLCompiler#checkFormOfVariables(ttlangs.tfl.TFLCompiler.RootBlock, ttlangs.tfl.TFLCompiler.BuildingBlock, ArrayList)}
	 * on prepared code of each building block.
	 * <p>
	 * Next, the method creates two codes (for cluster a and cluster b) from common code that uses short
	 * CHILD. The short is just replaced by a and b.
	 * <p>
	 * Finally the whole code of the switch is generated - the header of switch, all cases and the footer
	 * of the switch. Cases are generated by calling
	 * {@link TFLCompiler#generateCaseInMethod(ttlangs.tfl.TFLCompiler.RootBlock, ttlangs.tfl.TFLCompiler.BuildingBlock, ArrayList, ArrayList, ArrayList, ArrayList)}. 
	 * 
	 * @param method_name A type of the root block.
	 * @return A composed java source code of whole switch.
	 * @throws TFLException If a form of variable is not allowed or if any field of cluster c is not
	 * 						initialized in JOIN and a getter is going to be create. 
	 */
	private String generateSwitchInMethod(RootBlock method_name) throws TFLException {
		// first we check form of variables
		Set<Map.Entry<BuildingBlock, ArrayList<String>>> all_blocks = this.method_parts.entrySet();
		for (Entry<BuildingBlock, ArrayList<String>> field: all_blocks) {
			checkFormOfVariables(method_name, field.getKey(), field.getValue());
		}
		// compose source of the switch in appropriate method
		StringBuilder result = new StringBuilder();
		ArrayList<String> path_a = replaceChildShort("a",
				this.method_parts.get(BuildingBlock.PATH_CHILD));
		ArrayList<String> path_b = replaceChildShort("b",
				this.method_parts.get(BuildingBlock.PATH_CHILD));
		ArrayList<String> point_a = replaceChildShort("a",
				this.method_parts.get(BuildingBlock.POINT_CHILD));
		ArrayList<String> point_b = replaceChildShort("b",
				this.method_parts.get(BuildingBlock.POINT_CHILD));

		/* Compose whole switch: for each case generate the header and then a body. For generation
		 * of the body used prepared parts of code. If root block is different from SELECT
		 * then close the case by break (Select's case is finished by return (a|b)). */
		result.append("switch (type) {\ncase TYPE_PATH_AND_PATH:\n");
		
		result.append(generateCaseInMethod(method_name, BuildingBlock.PATH_AND_PATH,
				path_a,
				path_b,
				this.method_parts.get(BuildingBlock.PATH_PARENT),
				this.method_parts.get(BuildingBlock.PATH_AND_PATH)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");
		
		result.append("case TYPE_PATH_AND_POINT:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.PATH_AND_POINT,
				path_a,
				point_b,
				this.method_parts.get(BuildingBlock.PATH_PARENT),
				this.method_parts.get(BuildingBlock.PATH_AND_POINT)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");
		
		result.append("case TYPE_POINT_AND_PATH:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.POINT_AND_PATH,
				point_a,
				path_b,
				this.method_parts.get(BuildingBlock.PATH_PARENT),
				this.method_parts.get(BuildingBlock.POINT_AND_PATH)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");
			
		result.append("case TYPE_POINT_AND_POINT:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.POINT_AND_POINT,
				point_a,
				point_b,
				this.method_parts.get(BuildingBlock.POINT_PARENT),
				this.method_parts.get(BuildingBlock.POINT_AND_POINT)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");

		result.append("case TYPE_LPOINT_OVER_RPOINT:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.LPOINT_OVER_RPOINT,
				path_a,
				path_b,
				this.method_parts.get(BuildingBlock.POINT_PARENT),
				this.method_parts.get(BuildingBlock.LPOINT_OVER_RPOINT)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");

		result.append("case TYPE_RPOINT_OVER_LPOINT:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.RPOINT_OVER_LPOINT,
				path_a,
				path_b,
				this.method_parts.get(BuildingBlock.POINT_PARENT),
				this.method_parts.get(BuildingBlock.RPOINT_OVER_LPOINT)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");

		result.append("case TYPE_LPOINT_AND_RPOINT:\n");
		result.append(generateCaseInMethod(method_name, BuildingBlock.LPOINT_AND_RPOINT,
				path_a,
				path_b,
				this.method_parts.get(BuildingBlock.POINT_PARENT),
				this.method_parts.get(BuildingBlock.LPOINT_AND_RPOINT)));
		if (method_name != RootBlock.SELECT) result.append("break;\n");
		// close switch
		result.append("}\n");
		// return composed java source code of whole switch
		return result.toString();
	}


	/**
	 * This method generates and saves java source code of algorithm method with switch inside. It is
	 * used for generating methods join, split and selectQuestion.
	 * <p>
	 * First, the method remembers data types for imports generating. It is done by calling of method
	 * {@link TFLCompiler#rememberNecessaryImports()}. Next, if there is any non-empty building block
	 * then the method generates a non-empty body. It calls methods:
	 * <ul>
	 *   <li>{@link TFLCompiler#generateLocalFields(ttlangs.tfl.TFLCompiler.RootBlock)}</li>
	 *   <li>{@link TFLCompiler#generateSwitchInMethod(ttlangs.tfl.TFLCompiler.RootBlock)}</li>
	 * </ul>
	 * Else the body is empty. Finally, the method
	 * insert the result into {@link TFLCompiler#class_parts} and cleans auxiliary fields
	 * {@link TFLCompiler#local_fields} and {@link TFLCompiler#method_parts}.
	 * 
	 * @param method_name A type of the root block.
	 * @throws TFLException If a form of any variable is not allowed or if any field of cluster c is not
	 * 						initialized in JOIN and a getter is going to be create. 
	 */
	protected void generateMethodWithSwitch(RootBlock method_name) throws TFLException {
		// first remember data types for imports
		rememberNecessaryImports();
		// compose source of the java-file
		StringBuilder result = new StringBuilder();
		// method header
		result.append(generateMethodHeader(method_name));
		
		// remember if there is any non-empty block
		boolean not_empty = false;
		Set<Map.Entry<BuildingBlock, ArrayList<String>>> all_lists = this.method_parts.entrySet();
		for (Entry<BuildingBlock, ArrayList<String>> arr_list: all_lists) {
			if (arr_list.getValue() != null && !arr_list.getValue().isEmpty()) {
				not_empty = true;
				break;
			}
		}
		// if there is at least one non-empty block then generate the body of the method
		if (not_empty) {
			result.append(generateLocalFields(method_name));
			if (method_name == RootBlock.JOIN) {
				// add construction of c info cluster
				result.append("c.setInfo(new " + getClusterClassName() + "());\n");
			}
			result.append(generateSwitchInMethod(method_name));
		}
		
		if (method_name == RootBlock.SELECT) {
			result.append("return null;\n");
		}
		result.append(generateMethodFooter());
		// insert java source of the method into class_parts array
		this.class_parts.put(method_name, result.toString());
		// clean local_fields and method_parts
		this.local_fields = new HashMap<String, DataType>();
		this.method_parts = new HashMap<BuildingBlock, ArrayList<String>>();
	}
	
	
	/**
	 * This method generates and returns a java source code of the body of method without switch.
	 * <p>
	 * The method first checks a form of used variables by calling
	 * {@link TFLCompiler#checkFormOfVariables(ttlangs.tfl.TFLCompiler.RootBlock, ttlangs.tfl.TFLCompiler.BuildingBlock, ArrayList)}
	 * on prepared code of each building block.
	 * <p>
	 * The the whole code of the body is generated - a condition and the code for path and point cluster.
	 * Codes in conditions are generated by method
	 * {@link TFLCompiler#generateCaseInMethod(ttlangs.tfl.TFLCompiler.RootBlock, ttlangs.tfl.TFLCompiler.BuildingBlock, ArrayList, ArrayList, ArrayList, ArrayList)}. 
	 * 
	 * @param method_name A type of the root block.
	 * @return The generated java source code.
	 * @throws TFLException If a form of any variable is not allowed or if any field of cluster c is not
	 * 						initialized in JOIN and a getter is going to be create. 
	 */
	private String generateBodyWithoutSwitch(RootBlock method_name) throws TFLException {
		// first we check form of variables
		Set<Map.Entry<BuildingBlock, ArrayList<String>>> all_blocks = this.method_parts.entrySet();
		for (Entry<BuildingBlock, ArrayList<String>> field: all_blocks) {
			checkFormOfVariables(method_name, field.getKey(), field.getValue());
		}
		// compose source of the switch in appropriate method
		StringBuilder result = new StringBuilder();
		// generate code for path cluster
		ArrayList<String> block = this.method_parts.get(BuildingBlock.PATH);
		if (block != null && !block.isEmpty()) {
			result.append("if (type == Cluster.ClusterType.TYPE_PATH_CLUSTER) {\n");
			result.append(generateCaseInMethod(method_name, BuildingBlock.PATH,	null, null, null, block));
			result.append("}\n");
		}
		// generate code for point cluster
		block = this.method_parts.get(BuildingBlock.POINT);
		if (block != null && !block.isEmpty()) {
			result.append("if (type == Cluster.ClusterType.TYPE_POINT_CLUSTER) {\n");
			result.append(generateCaseInMethod(method_name, BuildingBlock.POINT, null, null, null, block));
			result.append("}\n");
		}
		// return generated java code
		return result.toString();
	}


	/**
	 * This method generates and saves java source code of algorithm method without switch inside. It is
	 * used for generating methods create and destroy.
	 * <p>
	 * First, the method remembers data types for imports generating. It is done by calling of method
	 * {@link TFLCompiler#rememberNecessaryImports()}.
	 * <p>
	 * Next, whole body is generated. It calls methods:
	 * <ul>
	 *   <li>{@link TFLCompiler#generateMethodHeader(ttlangs.tfl.TFLCompiler.RootBlock)}</li>
	 *   <li>{@link TFLCompiler#generateLocalFields(ttlangs.tfl.TFLCompiler.RootBlock)}</li>
	 *   <li>{@link TFLCompiler#generateBodyWithoutSwitch(ttlangs.tfl.TFLCompiler.RootBlock)}</li>
	 *   <li>{@link TFLCompiler#generateMethodFooter()}</li>
	 * </ul>
	 * <p>
	 * Finally, the method insert the result into {@link TFLCompiler#class_parts} and cleans auxiliary 
	 * fields {@link TFLCompiler#local_fields} and {@link TFLCompiler#method_parts}.
	 * 
	 * @param method_name A type of the root block.
	 * @throws TFLException If a form of any variable is not allowed or if any field of cluster c is not
	 * 						initialized in JOIN and a getter is going to be create. 
	 */
	protected void generateMethodWithoutSwitch(RootBlock method_name) throws TFLException {
		// first remember data types for imports
		rememberNecessaryImports();
		// compose source of the java-file
		StringBuilder result = new StringBuilder();
		result.append(generateMethodHeader(method_name));
		result.append(generateLocalFields(method_name));
		result.append(generateBodyWithoutSwitch(method_name));
		result.append(generateMethodFooter());
		// insert java source of the method into class_parts array
		this.class_parts.put(method_name, result.toString());
		// clean local_fields and method_parts
		this.local_fields = new HashMap<String, DataType>();
		this.method_parts = new HashMap<BuildingBlock, ArrayList<String>>();
	}


	/**
	 * This method composes whole java code of the algorithm class and saves it into the received folder.
	 * It return a name of the generated file.
	 * <p>
	 * The method just compose whole code. It starts with package declaration, imports and a header of the
	 * algorithm class. For local variables generating it calls {@link TFLCompiler#generateFields(HashMap)}.
	 * Next, the method generates all methods by calling {@link TFLCompiler#generateMethods(HashMap)}
	 * and appends generated codes to the result.
	 * <p>
	 * Finally, the method generates a name of the files and saves the class into the file
	 * with method {@link TFLCompiler#writeIntoFile(String, String)}. The name of the 
	 * file is returned. 
	 * 
	 * @param output_path A folder for saving a file with the class.
	 * @return Generated name of the saved file.
	 * @throws IOException If an I/O error occurs during files saving.
	 */
	private String generateAlgorithmFile(String output_path) throws IOException {
		
		// compose source of the java-file
		StringBuilder result = new StringBuilder();
		result.append("package " + getPackageName() + ";\n\n");
		result.append(generateAlgorithmImports());
		result.append("public class " + getAlgorithmName() + " implements TopTreeListener<"
				+ getClusterClassName() + ", " + getVertexClassName() + "> {\n\n");
		result.append(generateFields(this.var_fields));
		result.append(generateMethods(this.var_fields));
		result.append(this.class_parts.get(RootBlock.CREATE));
		result.append(this.class_parts.get(RootBlock.DESTROY));
		result.append(this.class_parts.get(RootBlock.JOIN));
		result.append(this.class_parts.get(RootBlock.SPLIT));
		result.append(this.class_parts.get(RootBlock.SELECT));
		result.append("}\n");

		// compose file name
		String file_name = output_path + getAlgorithmName() + ".java";
		
		// write source into file
		writeIntoFile(file_name, result.toString());
		
		return file_name;
	}
	
	
	/**
	 * This method formats received file to be more readable and saves the changes.
	 * <p>
	 * The method uses library Jalopy for the formatting. The file is overwritten. Information
	 * about formating is printed to system output.
	 * 
	 * @param file_name A name of the file to format.
	 * @throws FileNotFoundException If the file doesn't exists.
	 */
	private static void formatJavaSourceFile(String file_name) throws FileNotFoundException {
		// inform user that class was successfully generated
		System.out.print(file_name + ": successfully created");
		// prepare instance of the formatter and the file
		Jalopy jalopy = new Jalopy();
		File file = new File(file_name);
		// specify input and output target
		jalopy.setInput(file);
		jalopy.setOutput(file);
		// format and overwrite the given input file
		jalopy.format();
		// show result of the formatting
		if (jalopy.getState() == Jalopy.State.OK)
			System.out.println(" and formatted");
		else if (jalopy.getState() == Jalopy.State.WARN)
			System.out.println(file + " and formatted with warnings");
		else if (jalopy.getState() == Jalopy.State.ERROR)
			System.out.println(file + ", but could not be formatted");
	}

	
	/**
	 * This method compiles input TFL code into three java classes and saves these classes into
	 * received folder. The method returns the name of algorithm class. Names of other classes
	 * can be generated be adding <code>VertexInfo</code> and <code>ClusterInfo</code> to the end
	 * of the algorithm class name.
	 * <p>
	 * First, the method does lexical analysis of the input. Classes generated by JFlex and Cup are used
	 * for that.
	 * <p>
	 * Next, the method generates vertex info, cluster info and algorithm java class:
	 * <ul>
	 *   <li>{@link TFLCompiler#generateVertexInfoFile(String)}</li>
	 *   <li>{@link TFLCompiler#generateClusterInfoFile(String)}</li>
	 *   <li>{@link TFLCompiler#generateAlgorithmFile(String)}</li>
	 * </ul>
	 * They are saved into files to the folder <code>output_path</code> and formatted by method
	 * {@link TFLCompiler#formatJavaSourceFile(String)}.
	 * Finally, the generated name of the algorithm class is returned.
	 * 
	 * @param input A name of the input TFL file.
	 * @param output_path A folder where output java file is saved in.
	 * @param package_name A name of package whose part generated java classes have to be. 
	 * @return A name of the algorithm class.
	 * @throws Exception If any problem occurs during generation and saving files.
	 */
	public static String compileTFL(String input, String output_path, String package_name)
	throws Exception {

		// parse the input file info info classes generator
		parser p = new parser(new lexer(new FileReader(input)));
        TFLCompiler result = (TFLCompiler) p.parse().value;
        // set name of package of generates classes
        result.setPackageName(package_name);
        
        // generate and format vertex info java file
        formatJavaSourceFile(	result.generateVertexInfoFile(output_path)	);
        // generate and format cluster info java file
        formatJavaSourceFile(	result.generateClusterInfoFile(output_path)	);
        // generate and format algorithm java file
        formatJavaSourceFile(	result.generateAlgorithmFile(output_path)	);
        
        // return name of algorithm class
        return result.getAlgorithmName();
	}
	
	

	
}
