/*
 *  Enhancing of TQL skills by functions adding
 * 
 *  The package func contains one class Functions.java that allows extend skills of TQL by adding
 *  new functions.
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
package func;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.fantomery.ttinterpreter.numbers.InfiniteInteger;
import org.fantomery.ttinterpreter.numbers.InfiniteNumber.Finiteness;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator;
import org.fantomery.ttinterpreter.ttlangs.TFLMethodNameCreator.MethodTypes;
import org.fantomery.toptree.Cluster;
import org.fantomery.toptree.TopTree;
import org.fantomery.toptree.TopTreeListener;
import org.fantomery.toptree.Vertex;


/**
 * This class encapsulates all methods prepared for using in Top Tree Query Language.
 * <p>
 * For methods adding there are rules that must hold:
 * <ul>
 *   <li>Method must be public and static. Other methods are ignored.</li>
 *   <li>Names of all public static methods must be unique and must satisfied regular expression
 *   <code>[a-zA-Z][a-zA-Z0-9_]*</code>.</li>
 *   <li>Returned data type must be String. It is recommended to insert line-break "\n" to the end of
 *   returned strings.</li>
 *   <li>First argument of the method is Top Tree implementing {@link toptree.TopTree} 
 *   interface.</li>
 *   <li>Second argument is algorithm description implementing {@link toptree.TopTreeListener}
 *   interface.</li>
 *   <li>Behind the second argument there must be non-empty row of all vertices that the method
 *   works with. They must implement interface {@link toptree.Vertex}.</li>
 *   <li>Behind the row of vertices there can be all other arguments. Their data types have to be 
 *   only {@link java.lang.String}, {@link java.lang.Boolean}, {@link numbers.InfiniteNumber},
 *   {@link numbers.InfiniteInteger} or {@link numbers.InfiniteReal}. For generalization of 
 *   data type it can be used {@link java.lang.Object} or generic types. Last type can
 *   have flexible number of arguments.</li>
 * </ul>
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since 1.0
 */
public class Functions {

	
	/**
	 * This method creates and returns new array of a type <code>T</code> and saves into it all members
	 * of given array <code>results</code>.
	 * 
	 * @param <T> A type of returned array.
	 * @param results An array that will be retyped to a type <code>T</code>.
	 * @return	New array of a type <code>T</code> with retyped instances of given array
	 * 			<code>results</code>.
	 */
	@SuppressWarnings("unchecked")
	private static<T> T[] retypeArrayOfObjects(Object[] results) {
		// first create array of type of elements in results, if has to have same length
		T[] arr = (T[]) Array.newInstance(results[0].getClass(), results.length);
		// now copy elements of results into new array and retype them
		for (int i = 0; i < results.length; i++) {
			arr[i] = (T) results[i];
		}
		return arr;
	}


	/**
	 * This method reads a value from <code>C</code>'s field <code>field_name</code> and returns it
	 * as string.
	 * <p>
	 * A top tree is rebuilt before computing so that vertices <code>u</code> and <code>v</code> are
	 * boundaries of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex, Vertex)}.
	 * <p>
	 * The method just calls get-method specified by <code>field_name</code> without any arguments.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param u A vertex that is an instance of {@link toptree.Vertex}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param field_name A name of a field of a class <code>C</code> that's value will be returned.
	 * @return	A result that will be read from <code>C</code>'s field
	 * 			<code>field_name</code>.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String getClusterVal2(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> u, Vertex<V> v, String field_name)
	throws Exception {
		
		// first: rebuild the tree so that root cluster represents path u--v
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(u, v)) {
			return "Nodes are in different components.\n";
		}
		
		// call the required method and return an output  
		C cluster_info = top.getTopComponent(u).getInfo();
		// create the method name and take the method
		Method m = cluster_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_name), (Class[]) null);
		// call the method
		return m.invoke(cluster_info, (Object[]) null).toString() + "\n";
	}
	
	
	/**
	 * This method returns a result that is read from <code>C</code>'s field
	 * <code>field_name</code>.
	 * <p>
	 * A top tree is rebuilt before computing so that vertex <code>v</code> is only boundary
	 * of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex)}.
	 * <p>
	 * The procedure is different for ordinary field and for array field. If there is ordinary field then
	 * it just calls get-method specified by <code>field_name</code> without any arguments.
	 * If the field is an array (the value of <code>is_array</code> is <code>true</code>) then object
	 * with information of vertex <code>v</code> must be used as argument of called get-method.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param field_name A name of a field of a class <code>C</code> that's value will be returned.
	 * @param is_array A switcher if the field is an array (<code>true</code>) or not (<code>false</code>).
	 * @return	A result that will be read from <code>C</code>'s field
	 * 			<code>field_name</code>.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String getClusterVal1(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v, String field_name, Boolean is_array)
	throws Exception {
		
		// first: rebuild the tree so that root cluster is handler of v
		if (TopTree.ExposeOneResults.COMPONENT != top.expose(v)) {
			return "Node is single.\n";
		}
		
		// call the required method and return an output  
		C cluster_info = top.getTopComponent(v).getInfo();
		if (is_array) {
			// create the method name and take the method
			Method m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_name),
					v.getInfo().getClass());
			// call the method
			return m.invoke(cluster_info, (Object) v.getInfo()).toString() + "\n";
		}
		else {
			// create the method name and take the method
			Method m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_name), (Class[]) null);
			// call the method
			return m.invoke(cluster_info, (Object[]) null).toString() + "\n";
		}
	}


	/**
	 * This method increases values of numeric fields of a class <code>C</code> specified by 
	 * <code>field_names</code> by value of a number <code>value</code> and returns the result of it.
	 * <p>
	 * A top tree is rebuilt before computing so that vertices <code>u</code> and <code>v</code> are
	 * boundaries of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex, Vertex)}.
	 * <p>
	 * The method step-by-step calls add-methods of numeric fields in the root cluster specified
	 * by <code>field_names</code> with number <code>value</code> as an argument.
	 * All fields must be instances of the same class.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param <T> A type of argument <code>value</code>.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param u A vertex that is an instance of {@link toptree.Vertex}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param value A number that will be added to numeric field specified by <code>field_names</code>.
	 * @param field_names	Names of numeric fields of a class <code>C</code> whose values will be
	 * 						increased.
	 * @return A result of the adding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V,T> String addClusterNum2(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> u, Vertex<V> v, T value, String... field_names)
	throws Exception {
		
		// first: rebuild the tree so that root cluster represents path u--v
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(u, v)) {
			return "Nodes are in different components.\n";
		}
		
		C cluster_info = top.getTopComponent(u).getInfo();
		Method m = null;
		/* Before adding try if all methods exist. If any doesn't exist then reflection API
		 * throws an exception. */
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.ADD, field_names[i]),
					value.getClass());
		}
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.ADD, field_names[i]),
					value.getClass());
			// call the method
			m.invoke(cluster_info, (Object) value);
		}
		return "OK\n";
	}


	/**
	 * This method increases values of numeric fields of a class <code>C</code> specified by 
	 * <code>field_names</code> by value of a number <code>value</code> and returns the result of it.
	 * <p>
	 * A top tree is rebuilt before computing so that vertex <code>v</code> is only boundary
	 * of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex)}.
	 * <p>
	 * The method step-by-step calls add-methods of numeric fields in the root cluster specified
	 * by <code>field_names</code> with number <code>value</code> as an argument.
	 * All fields must be instances of the same class which has implemented add-method.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param <T> A type of argument <code>value</code>.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param value A number that will be added to numeric field specified by <code>field_names</code>.
	 * @param field_names	Names of numeric fields of a class <code>C</code> whose values will be
	 * 						increased.
	 * @return A result of the adding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V,T> String addClusterNum1(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v, T value, String... field_names)
	throws Exception {
		
		// first: rebuild the tree so that root cluster is handler of v
		if (TopTree.ExposeOneResults.COMPONENT != top.expose(v)) {
			return "Node is single.\n";
		}
		
		// call required methods and give them value parameter  
		C cluster_info = top.getTopComponent(v).getInfo();
		Method m = null;
		/* Before adding try if all methods exist. If any doesn't exist then reflection API
		 * throws an exception. */
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.ADD, field_names[i]),
					value.getClass());
		}
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.ADD, field_names[i]),
					value.getClass());
			// call the method
			m.invoke(cluster_info, (Object) value);
		}
		return "OK\n";
	}


	/**
	 * This method returns a string containing the result that is computed by a method specified 
	 * by <code>method_name</code> from fields of a class <code>C</code> specified 
	 * by <code>field_names</code>. 
	 * <p>
	 * A top tree is rebuilt before computing so that vertex <code>v</code> is only boundary
	 * of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex)}.
	 * <p>
	 * The method first reads all field values specified by <code>field_names</code> from the root cluster.
	 * All of them must have the same type. Then it calls method <code>method_name</code> with read
	 * values as arguments. The result is returned.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param <H> A type of handler.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param method_name 	A name of method of a class <code>C</code> that is used for computing a result
	 * 						from objects of type <code>T</code>.
	 * @param field_names	Names of fields of a class <code>C</code> that are used as arguments of method
	 * 						specified by <code>method_name</code>. 
	 * @return 	A string containing the result that is computed by a method specified 
	 * 			by <code>method_name</code> from fields of a class <code>C</code> specified 
	 * 			by <code>field_names</code>.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V,H> String pickClusterNum1(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v, String method_name, String... field_names)
	throws Exception {
		
		// first: rebuild the tree so that root cluster is handler of v
		if (TopTree.ExposeOneResults.COMPONENT != top.expose(v)) {
			return "Node is single.\n";
		}
		
		/* Call all get-methods according to field_names and save results as array of Objects.
		 * Then we must create array of T and save results into it - results are type of T.
		 * In the end invoke the method that chooses the extreme (maximum, minimum) of results. */
		Method m = null;
		C cluster_info = top.getTopComponent(v).getInfo();
		
		/* Obtain results from get-methods - they must be saved as Object, because generic type T cannot
		 * be instantiated. */
		Object[] results = new Object[field_names.length];
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_names[i]),
					(Class[]) null);
			// call the method
			results[i] = m.invoke(cluster_info, (Object[]) null);
		}
		
		/* Retype array of results to T. If this is not done that invoke doesn't recognize the type
		 * of whole array - it would seem to be Object[] instead of T[]. */
		Object[] max_params = retypeArrayOfObjects(results);
		
		// take method to compute extreme (maximum, minimum) and invoke it
		m = results[0].getClass().getMethod(method_name, max_params.getClass());
		Object[] invoke_params = {max_params};
		return m.invoke( null, invoke_params).toString() + "\n";
	}


	/**
	 * This method returns a string containing the result that is computed by a method specified 
	 * by <code>method_name</code> from fields of a class <code>C</code> specified 
	 * by <code>field_names</code>. 
	 * <p>
	 * A top tree is rebuilt before computing so that vertices <code>u</code> and <code>v</code> are
	 * boundaries of a root cluster. It is made by {@link toptree.TopTree#expose(Vertex, Vertex)}.
	 * <p>
	 * The method first reads all field values specified by <code>field_names</code> from the root cluster.
	 * All of them must have the same type. Then it calls method <code>method_name</code> with read
	 * values as arguments. The result is returned.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param u A vertex that is an instance of {@link toptree.Vertex}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param method_name 	A name of method of a class <code>C</code> that is used for computing a result
	 * 						from objects of type <code>T</code>.
	 * @param field_names	Names of fields of a class <code>C</code> that are used as arguments of method
	 * 						specified by <code>method_name</code>. 
	 * @return 	A string containing the result that is computed by a method specified 
	 * 			by <code>method_name</code> from fields of a class <code>C</code> specified 
	 * 			by <code>field_names</code>.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String pickClusterNum2(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> u, Vertex<V> v, String method_name, String... field_names)
	throws Exception {
		
		// first: rebuild the tree so that root cluster represents path u--v
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(u, v)) {
			return "Nodes are in different components.\n";
		}
		
		/* Call all get-methods according to field_names and save results as array of Objects.
		 * Then we must create array of T and save results into it - results are type of T.
		 * In the end invoke the method that chooses the extreme (maximum, minimum) of results. */
		Method m = null;
		C cluster_info = top.getTopComponent(v).getInfo();
		
		/* Obtain results from get-methods - they must be saved as Object, because generic type T cannot
		 * be instantiated. */
		Object[] results = new Object[field_names.length];
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = cluster_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_names[i]),
					(Class[]) null);
			// call the method
			results[i] = m.invoke(cluster_info, (Object[]) null);
		}
		
		/* Retype array of results to T. If this is not done that invoke doesn't recognize the type
		 * of whole array - it would seem to be Object[] instead of T[]. */
		Object[] max_params = retypeArrayOfObjects(results);
		
		// take method to compute extreme (maximum, minimum) and invoke it
		m = results[0].getClass().getMethod(method_name, max_params.getClass());
		Object[] invoke_params = {max_params};
		return m.invoke( null, invoke_params).toString() + "\n";
	}


	/**
	 * This method sets an object <code>value</code> in vertex <code>v</code>
	 * for fields specified by <code>field_names</code>. All these fields have to have the same type
	 * as <code>value</code>.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param <T> A type of object that is saved.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param value An object that is saved into vertex's fields specified by <code>field_names</code>.
	 * @param field_names Names of fields whose value will be set.
	 * @return A result of setting fields.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V,T> String setVertexVal(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v, T value, String... field_names)
	throws Exception {
		
		// first: rebuild the tree so that root cluster is handler of v
		top.expose(v);
		
		// call required methods and give them value parameter  
		// take informations of vertex v
		V vertex_info = v.getInfo();

		Method m = null;
		
		/* Before setting try if all methods exist. If any doesn't exist then reflection API
		 * throws an exception. */
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = vertex_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_names[i]),
					value.getClass());
		}
		
		// call methods to set all required fields
		for (int i = 0; i < field_names.length; i++) {
			// create the method name and take the method
			m = vertex_info.getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_names[i]),
					value.getClass());
			// call the method
			m.invoke(vertex_info, (Object) value);
		}
		// if all fields were successfully set then returns info about it
		return "OK\n";
	}
	
	
	/**
	 * This method reads a value from vertex <code>v</code> saved in field
	 * specified by <code>field_name</code> and returns a string with the result.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param field_name A name of a field in a class <code>V</code> whose value will be returned.
	 * @return 	A result of getting the value.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String getVertexVal(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v, String field_name)
	throws Exception {
		
		// first: rebuild the tree so that root cluster is handler of v
		top.expose(v);
		
		// call required methods and give them value parameter  
		// take informations of vertex v
		V vertex_info = v.getInfo();
		// create the method name and take the method
		Method m = vertex_info.getClass().getMethod(
				TFLMethodNameCreator. createMethodName(MethodTypes.GET, field_name), (Class[]) null);
		// call the method
		return m.invoke(vertex_info, (Object[]) null).toString() + "\n";
	}
	
	
	/**
	 * This method reads a distance to the nearest vertex of given vertex <code>v</code> and returns 
	 * a string with the result.
	 * <p>
	 * The method first takes the tree of given vertex. This is done by calling
	 * {@link toptree.impl.TopTreeImpl#expose(Vertex)}. If the vertex is marked then the distance is 0.
	 * If the vertex is single and not marked then it returns information about it. Else it reads a value
	 * of distance to its nearest vertex. Finally the method returns information about the result.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @return A result of the nearest marked vertex finding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String nearestMarked(TopTree<C,V> top, TopTreeListener<C,V> handler,	Vertex<V> v)
	throws Exception {

		// name of vertex and cluster field
		String field_marked = "marked";
		String field_distance = "mark_dist";
		
		/* A procedure is similar for single and non-single vertex. If v is marked then zero
		 * is returned. If v isn't marked the procedure is different. For single v positive infinity is
		 * returned and for non-single v the distance to the nearest marked vertex. */
		if (TopTree.ExposeOneResults.SINGLE == top.expose(v)) {
			V vertex_info = v.getInfo();
			// create the method name and take the method for read if v is marked
			Method m = vertex_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_marked), (Class[]) null);
			// call the method
			Boolean is_marked = (Boolean) m.invoke(vertex_info, (Object[]) null);
			if (is_marked) {
				return "Distance to the nearest marked vertex: 0\n";
			} else {
				return "Distance to the nearest marked vertex: " +
					(new InfiniteInteger(Finiteness.POSITIVE_INFINITY)).toString() + "\n";
			}
		}
		else {
			V vertex_info = v.getInfo();
			// create the method name and take the method for read if v is marked
			Method m = vertex_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_marked), (Class[]) null);
			// call the method
			Boolean is_marked = (Boolean) m.invoke(vertex_info, (Object[]) null);
			if (is_marked) {
				return "Distance to the nearest marked vertex: 0\n";
			} else {
				// read the distance to the nearest marked vertex and return it
				C cluster_info = top.getTopComponent(v).getInfo();
				// create the method and take the method
				m = cluster_info.getClass().getMethod(
						TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_distance), 
						v.getInfo().getClass());
				// call the method and return the result value
				InfiniteInteger result = (InfiniteInteger) m.invoke(cluster_info, (Object) v.getInfo());
				return "Distance to the nearest marked vertex: " + result.toString() + "\n";
			}
		}
	}


	/**
	 * This method reads names of vertices that are dynamic centers of a tree given by a vertex 
	 * <code>v</code> and returns a string with the result.
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#select(Vertex)}. It returns either
	 * <code>null</code> or two vertices that are possible centers. If the null is returned, it means that
	 * given vertex is single and the method returns information about it. Else it must decide which one
	 * is the center. It is easy. The method rebuilds the tree so a top cluster has these two vertices
	 * as boundaries. Then it just reads maximal distances of both vertices. If one has smaller maximal
	 * distance then another then it is the center. If both maximal distances are equal then both vertices
	 * are returned as two dynamic centers.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @return	A result of dynamic center finding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String center(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> v)
	throws Exception {
		
		// name of cluster field with maximal distances
		String field_max_dist = "max_dist";
		
		// select returns two possible centers (they are an edge) or null if no median exists
		ArrayList<Vertex<V>> possible_centers = top.select(v);
		
		if (possible_centers == null) {
			// no center
			return "No center exists.";
		} 

		assert possible_centers != null; // not single
		assert possible_centers.size() == 2; // two vertices inside
			
		Vertex<V> x = possible_centers.get(0);
		Vertex<V> y = possible_centers.get(1);
		
		// make vertices the boundaries of top cluster
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(x, y)) {
			// x and y are in different components
			return "No center exists.";
		} 
		
		assert top.getTopComponent(x) != null && top.getTopComponent(x) == top.getTopComponent(y); 
		C topcluster = top.getTopComponent(x).getInfo();
		
		// read maximal distance of x: take get-method and call it
		Method m = topcluster.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_max_dist),
				x.getInfo().getClass());
		InfiniteInteger x_value = (InfiniteInteger) m.invoke(topcluster, x.getInfo());

		// read maximal distance of x: take get-method and call it
		m = topcluster.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_max_dist),
				y.getInfo().getClass());
		InfiniteInteger y_value = (InfiniteInteger) m.invoke(topcluster, y.getInfo());

		// return center (centers) and maximal distance of center
		assert x != null && y != null && x != y;
		if (x_value.isLessThen(y_value)) {
			return "Center: " + x.getInfo().toString() + "\nMax. distance of the center: "
					+ x_value.toString() + "\n";
		} else if (x_value.isGreaterThen(y_value)) {
			return "Center: " + y.getInfo().toString() + "\nMax. distance of the center: "
			+ y_value.toString() + "\n";
		} else {
			assert x_value.isEqualTo(y_value);
			return "Centers:\n\t" + x.getInfo().toString() + "\n\t" + y.getInfo().toString()
			+ "\nMax. distance of centers: " + y_value.toString() + "\n";
		}
	}

	
	/**
	 * This method reads names of vertices that are dynamic medians of a tree given by a vertex
	 * <code>v</code> and returns a string with the result.
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#select(Vertex)}. It returns either
	 * <code>null</code> or two vertices that are possible medians. If the null is returned, it means that
	 * given vertex is single and the method returns information about it. Else it must decide which
	 * one is the median. The method deletes an edge that connects returned vertices (it must remember
	 * the length of destroyed edge to recreation). It divides the tree into two trees. Then the method
	 * rebuilds the trees so that their top clusters has possible medians as only boundaries. Afterwards
	 * it reads sums of vertex weights of these two trees. The vertex whose tree is heavier is the median.
	 * If weights are equal then both vertices are medians. In the end the deleted edge is restored.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @return	A result of dynamic median finding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	@SuppressWarnings("unchecked")
	public static<C,V> String median(TopTree<C,V> top, TopTreeListener<C,V> handler, Vertex<V> v)
	throws Exception {

		// vertex name and weight
		String field_name = "name";
		String field_weight = "weight";
		// cluster length and sum
		String field_length = "length";
		String field_int_weight = "sum";
		
		// select returns two possible medians (they are an edge) or null if no median exists
		ArrayList<Vertex<V>> possible_medians = top.select(v);
		
		if (possible_medians == null) {
			// no median exists
			return "No median exists.\n";
		} 

		assert possible_medians.size() == 2; // two vertices inside
			
		// save candidates to median into x and y
		Vertex<V> x = possible_medians.get(0);
		Vertex<V> y = possible_medians.get(1);
		
		// auxiliary variables
		InfiniteInteger x_vertex_weight = new InfiniteInteger(0);
		InfiniteInteger y_vertex_weight = new InfiniteInteger(0);
		InfiniteInteger x_tree_int_weight = new InfiniteInteger(0);
		InfiniteInteger y_tree_int_weight = new InfiniteInteger(0);
		InfiniteInteger x_name = new InfiniteInteger(0);
		InfiniteInteger y_name = new InfiniteInteger(0);

		/* Expose(x,y) so edge (x,y) is represented in root cluster. Then save length of this
		 * edge and cut it. After it compute weights of components of x and of y. Then recreate
		 * edge (x,y) with saved length. */

		// rebuilding tree so (x,y) is represented in root
		top.expose(x,y);
		assert top.getTopComponent(x) == top.getTopComponent(y);
		C cluster_info = top.getTopComponent(x).getInfo();
		// save length of edge
		Method m = cluster_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_length), (Class[]) null);
		InfiniteInteger length = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		// destroy edge - two components are created: x is in one and y in opposite
		top.cut(x, y);

		// compute weight of x-component: a sum of x's weight and internal weight of component
		Cluster<C,V> root_cluster = null;
		// read x's weight
		m = x.getInfo().getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_weight), (Class[]) null);
		x_vertex_weight = (InfiniteInteger) m.invoke(x.getInfo(), (Object[]) null);
		// if x isn't single then add internal weight of x's component
		if (TopTree.ExposeOneResults.COMPONENT == top.expose(x)) {
			root_cluster = top.getTopComponent(x);
			
			m = root_cluster.getInfo().getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_int_weight),
					(Class[]) null);
			x_tree_int_weight = (InfiniteInteger) m.invoke(root_cluster.getInfo(), (Object[]) null);

			x_vertex_weight = InfiniteInteger.sum(x_vertex_weight, x_tree_int_weight);
		}

		// compute weight of y-component: a sum of y's weight and internal weight of component
		// read x's weight
		m = y.getInfo().getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_weight), (Class[]) null);
		y_vertex_weight = (InfiniteInteger) m.invoke(y.getInfo(), (Object[]) null);
		// if x isn't single then add internal weight of x's component
		if (TopTree.ExposeOneResults.COMPONENT == top.expose(y)) {
			root_cluster = top.getTopComponent(y);
			
			m = root_cluster.getInfo().getClass().getMethod(
					TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_int_weight),
					(Class[]) null);
			y_tree_int_weight = (InfiniteInteger) m.invoke(root_cluster.getInfo(), (Object[]) null);

			y_vertex_weight = InfiniteInteger.sum(y_vertex_weight, y_tree_int_weight);
		}
		// read names of vertex x and y to recreate the edge
		m = x.getInfo().getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_name), (Class[]) null);
		x_name = (InfiniteInteger) m.invoke(x.getInfo(), (Object[]) null);
		y_name = (InfiniteInteger) m.invoke(y.getInfo(), (Object[]) null);
		// recreate the edge (x,y)
		Constructor<?> constructor = cluster_info.getClass().getConstructor( (Class<?>[]) null);
		Object info = constructor.newInstance();
		// length
		m = info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_length), length.getClass());
		m.invoke(info, length);
		// sum of internal weights
		m = info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_int_weight), length.getClass());
		m.invoke(info, new InfiniteInteger(0));
		top.link(x, y, (C) info);
		
		// compare weights of x's and y's component - heavier if the median
		if (x_vertex_weight.isGreaterThen(y_vertex_weight)) {
			// x is heavier
			return "Median: " + x_name.toString() + "\n";
		} else if (x_vertex_weight.isLessThen(y_vertex_weight)) {
			// y is heavier
			return "Median: " + y_name.toString() + "\n";
		} else {
			// weights are equal - x and y are both medians
			assert x_vertex_weight.isEqualTo(y_vertex_weight);
			return "Medians: " + x_name.toString() + ", " + y_name.toString() + "\n";
		}
	}

	
	/**
	 * This method finds a vertex that is a level ancestor of a vertex <code>x</code> on a way to a vertex 
	 * <code>y</code> and returns it.
	 * <p>
	 * The method first calls {@link toptree.impl.TopTreeImpl#select(Vertex)}. It returns either
	 * <code>null</code> or two vertices that are possible level ancestors. If the null is returned,
	 * it means that given vertices are not in common tree and the method returns also null. Else it must
	 * decide which one is the level ancestor. For each of possible ancestors the method rebuilds the tree
	 * so that their top clusters has returned vertices with vertex <code>x</code> as boundaries and reads
	 * lengths of cluster paths from <code>x</code> to both of possible ancestors.
	 * The vertex whose length is equal to the <code>level</code> is the level ancestor. If non is equal
	 * then the level ancestor doesn't exist and <code>null</code> is returned.
	 * <p>
	 * If any exception occurs during working with java reflection API then exception is written on output
	 * and <code>null</code> is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param x A vertex that is an instance of {@link toptree.Vertex}.
	 * @param y A vertex that is an instance of {@link toptree.Vertex}.
	 * @param level A number that determines the distance of the ancestor of <code>u</code>.
	 * @param field_length	A name of a field in a class <code>C</code> where a length of the cluster 
	 * 						is saved in.
	 * @return The level ancestor or <code>null</code> value if no ancestor exists.
	 * @throws Exception  If any problem occurs during working with java reflection API.
	 */
	private static<C,V> Vertex<V> jump(TopTree<C,V> top, Vertex<V> x, Vertex<V> y, InfiniteInteger level,
			String field_length)
	throws Exception {

		// find possible ancestors - select returns two vertices that are on one edge or null
		ArrayList<Vertex<V>> possible_level_ancestors = top.select(x,y);
		
		if (possible_level_ancestors == null) {
			// there is no ancestor
			return null;
		} 

		assert possible_level_ancestors.size() == 2; // two vertices inside
		
		/* Computes the lengths of way form x to both possible ancestors returned by select. Then
		 * compare the lengths to level. If any is equal to level then appropriate vertex is ancestor.
		 * If no is equal to level then no ancestor exists. */

		// auxiliary variables for lenghts
		InfiniteInteger component1_length = new InfiniteInteger(0);
		InfiniteInteger component2_length = new InfiniteInteger(0); 
		// take a method for reading the length of the cluster
		C cluster_info = top.getTopComponent(x).getInfo();
		Method m = cluster_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_length), (Class[]) null);
		// read length of the way from x to first possible ancestor
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT == top.expose(x,possible_level_ancestors.get(0))){
			cluster_info = top.getTopComponent(x).getInfo();
			component1_length = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		}
		// read length of the way from x to second possible ancestor
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT == top.expose(x,possible_level_ancestors.get(1))){
			cluster_info = top.getTopComponent(x).getInfo();
			component2_length = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);	
		}
		// compare lengths and level
		if (component1_length.isEqualTo(level)) {
			return possible_level_ancestors.get(0);
		} else if (component2_length.isEqualTo(level)) {
			return possible_level_ancestors.get(1);
		} else {
			return null;
		}
	}

	
	/**
	 * This method finds a vertex that is the nearest common ancestor of given three vertices and returns
	 * its name or <code>null</code> value if no ancestor exists.
	 * <p>
	 * The method first reads the distances between a couples of given vertices by rebuilding
	 * the tree so that boundary vertices of a top cluster are the couple and then reads the length of
	 * the cluster path. Afterward the method computes from lengths how many step from the vertex 
	 * <code>z</code> is the nearest common ancestor on a way to the vertex <code>x</code>. Then it calls
	 * the method
	 * {@link func.Functions#jump(TopTree, Vertex, Vertex, InfiniteInteger, String)}
	 * that returns the nearest common ancestor of given three vertices or <code>null</code> if no ancestor
	 * exists.
	 * <p>
	 * If any exception occurs during working with java reflection API then exception is written on output
	 * and <code>null</code> is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param x A vertex that is an instance of {@link toptree.Vertex}.
	 * @param y A vertex that is an instance of {@link toptree.Vertex}.
	 * @param z A vertex that is an instance of {@link toptree.Vertex}.
	 * @param field_length	A name of a field in a class <code>C</code> where a length of the cluster 
	 * 						is saved in.
	 * @param field_level	A name of a field in a class of handler where a level during the finding 
	 * 						is saved in.
	 * @return The nearest common ancestor or <code>null</code> value if no ancestor exists.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	private static<C,V> Vertex<V> meet(TopTree<C,V> top, TopTreeListener<C,V> handler, Vertex<V> x,
			Vertex<V> y, Vertex<V> z, String field_length, String field_level)
	throws Exception {

		/* Compute value of level to find the nearest common ancestor of x, y and z as level ancestor
		 * of vertex z on a way to vertex x. Then just call jump that finds the ancestor. */
		// take method to read a length of the cluster
		C cluster_info = top.getTopComponent(x).getInfo();
		Method m = cluster_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_length), (Class[]) null);
		// read length of the way x...z
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(x, z)) {
			return null;
		}
		cluster_info = top.getTopComponent(x).getInfo();
		InfiniteInteger dist_x_z = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		// read length of the way y...z
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(y, z)) {
			return null;
		}
		cluster_info = top.getTopComponent(y).getInfo();
		InfiniteInteger dist_y_z = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		// read length of the way x...y
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(x, y)) {
			return null;
		}
		cluster_info = top.getTopComponent(x).getInfo();
		InfiniteInteger dist_x_y = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		
		// compute level
		InfiniteInteger level_value = InfiniteInteger.quotient(
											InfiniteInteger.difference(
													InfiniteInteger.sum(dist_x_z, dist_y_z),
													dist_x_y),
											new InfiniteInteger(2));
		// set level value into appropriate field in the handler 
		m = handler.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_level),
				level_value.getClass());
		m.invoke(handler, (Object) level_value);
		// jump computes the ancestor
		return jump(top, z, x, level_value, field_length);
	}

	
	/**
	 * This method finds a vertex that is a level ancestor of a vertex <code>u</code> on a way to a vertex 
	 * <code>v</code> and returns a string with the result.
	 * <p>
	 * If given vertices are not in the same component or their distance is shorter than the level then
	 * the level ancestor doesn't exist and the method returns information about this.
	 * Otherwise it calls the method
	 * {@link func.Functions#jump(TopTree, Vertex, Vertex, InfiniteInteger, String)}
	 * that returns a vertex that is the level ancestor or <code>null</code> if it doesn't exist. 
	 * Then the method returns information about the result.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param u A vertex that is an instance of {@link toptree.Vertex}.
	 * @param v A vertex that is an instance of {@link toptree.Vertex}.
	 * @param level A number that determines the distance (>=1) of the ancestor of <code>u</code>.
	 * @return A result of finding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String levelAncestor(TopTree<C,V> top,
			TopTreeListener<C,V> handler, Vertex<V> u, Vertex<V> v, InfiniteInteger level)
	throws Exception {

		// names of fields length and level
		String field_length = "length";
		String field_level = "level";
		
		// check: level >= 1
		assert level.isGreaterOrEqualTo(new InfiniteInteger(1));
		
		//	root cluster must represent the way u...v
		if (TopTree.ExposeTwoResults.COMMON_COMPONENT != top.expose(u, v)) {
			return "Nodes are in different components.\n";
		}
		
		/* If level is greater then a length of the way u...v then no ancestor exists. Else save value
		 * of level into handler and call jump to compute the ancestor. */

		// compare length and level
		C cluster_info = top.getTopComponent(v).getInfo();
		Method m = cluster_info.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.GET, field_length), (Class[]) null);
		InfiniteInteger cluster_length = (InfiniteInteger) m.invoke(cluster_info, (Object[]) null);
		if (cluster_length.isLessThen(level)) {
			return "Level ancestor doesn't exist.\n";
		}
		// set level in handler
		m = handler.getClass().getMethod(
				TFLMethodNameCreator.createMethodName(MethodTypes.SET, field_level), level.getClass());
		m.invoke(handler, (Object) level);
		// jump computes the ancestor
		Vertex<V> x = jump(top, u, v, level, field_length);
		// return name of the ancestor (was exposed in jump) or null if no ancestor exists
		if (x != null)
			return "Level ancestor: " + x.getInfo().toString() + "\n";
		else
			return "Level ancestor doesn't exist.\n";
	}

	
	/**
	 * This method finds a vertex that is the nearest common ancestor of given three vertices and returns
	 * a string with result.
	 * <p>
	 * The method just call a method
	 * {@link func.Functions#meet(TopTree, TopTreeListener, Vertex, Vertex, Vertex, String, String)}
	 * that returns a vertex that is the nearest common ancestor or <code>null</code> if it doesn't exist. 
	 * Then the method returns information about the result.
	 * <p>
	 * If any exception occurs during working with java reflection API then the message of the exception
	 * is returned. 
	 * 
	 * @param <C> A type of object with cluster information.
	 * @param <V> A type of object with vertex information.
	 * @param top A top tree that is an instance of {@link toptree.TopTree}.
	 * @param handler An instance of {@link toptree.TopTreeListener}.
	 * @param x A vertex that is an instance of {@link toptree.Vertex}.
	 * @param y A vertex that is an instance of {@link toptree.Vertex}.
	 * @param z A vertex that is an instance of {@link toptree.Vertex}.
	 * @return A result of finding.
	 * @throws Exception If any problem occurs during working with java reflection API.
	 */
	public static<C,V> String commonAncestor(TopTree<C,V> top, TopTreeListener<C,V> handler,
			Vertex<V> x, Vertex<V> y, Vertex<V> z)
	throws Exception {

		// names of fields length and level
		String field_length = "length";
		String field_level = "level";

		/* Just call meet return the result. */
		Vertex<V> a = meet(top, handler, x, y, z, field_length, field_level);
		// return name of the ancestor (was exposed in jump - called from meet) or null if no exists
		if (a != null)
			return "Nearest common ancestor: " + a.getInfo().toString() + "\n";
		else
			return "Nearest common ancestor doesn't exist.\n";
	}
	
	
}
