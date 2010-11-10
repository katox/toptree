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

/**
 * This class represents command of TQL. It stores a type of the command and information (arguments) that
 * the command works with.
 * <p>
 * It provides tools for getting information about the command. Information of created command instance 
 * cannot be changed.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TQLCommand {

	/**
	 * This enumeration type represents all types of commands in TQL.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	protected enum CommandType {
        
		/** Represents termination command. */
		EXIT,
		
		/** Represents command that sets node declaration and implicit initialization. */
        NODE_DECLARATION,
		
		/** Represents command that sets edge declaration and implicit initialization. */
        LINK_DECLARATION,
		
		/** Represents command that shows node declaration and implicit initialization. */
        NODE_DECLAR_INFO,
		
		/** Represents command that shows edge declaration and implicit initialization. */
        LINK_DECLAR_INFO,
		
		/** Represents command that shows information kept in specified node. */
        NODE_INFO,
		
		/** Represents command that shows information kept in specified edge (base cluster). */
        LINK_INFO,
		
		/** Represents command that creates new node. */
        NODE,
		
		/** Represents command that creates new edge. */
        LINK,
		
		/** Represents command that deletes specified edge. */
        CUT,
		
		/** Represents command that executes user defined function. */
        FUNCTION
	}
	
	/** A name of an user defined function if the command represents the function. */
	private String name;

	/** A value of first the unique vertex identifier if the command works with at least on vertex. */
	private TQLValue value1;

	/** A value of the second unique vertex identifier if the command works with two vertices. */
	private TQLValue value2;

	/** A value of the first identifier neighbor if the command is link. */
	private TQLValue value3;

	/** A value of the second identifier neighbor if the command is link. */
	private TQLValue value4;

	/** A type of the command */
	private CommandType type;
	
	/** A list of vertex or cluster class fields if the command works with declaration. */
	private ArrayList<TQLVariable> variables;
	
	/** A list of values for declared vertex or cluster class fields if the command initializes
	 *  the declaration or for arguments of a user defined function if the command is the function. */
	private ArrayList<TQLValue> values;
	
	/** A list of vertex or cluster class fields and their values if the command works with
	 *  initialization. */
	private ArrayList<TQLValue> init_values;
	
	
	/**
	 * This constructor only sets the type of the command.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#EXIT}</li>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#NODE_DECLAR_INFO}</li>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#LINK_DECLAR_INFO}</li>
	 * </ul>
	 * 
	 * @param type A type of the command.
	 */
	protected TQLCommand(CommandType type) {
		this.type = type;
	}
	
	
	/**
	 * This constructor sets the type of the command, the list of declared fields and the list
	 * of initialized fields with their values.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#NODE_DECLARATION}</li>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#LINK_DECLARATION}</li>
	 * </ul>
	 * 
	 * @param type A type of the command.
	 * @param variables A list of declared vertex or cluster class fields.
	 * @param init_values A list of vertex or cluster class fields and their values.
	 */
	protected TQLCommand(CommandType type, ArrayList<TQLVariable> variables, ArrayList<TQLValue> init_values) {
		this.type = type;
		this.variables = variables;
		this.init_values = init_values;
	}
	

	/**
	 * This constructor sets the type of the command, the value of the first (only one) unique vertex
	 * identifier, the list of values of declared fields and the list of initialized fields with 
	 * their values.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#NODE}</li>
	 * </ul>
	 * 
	 * @param type A type of the command.
	 * @param value1 A value of the first unique vertex identifier.
	 * @param values A list of values for declared vertex class fields.
	 * @param init_values A list of vertex or cluster class fields and their values.
	 */
	protected TQLCommand(CommandType type, TQLValue value1, ArrayList<TQLValue> values,
    		ArrayList<TQLValue> init_values) {
		this.type = type;
		this.value1 = value1;
		this.values = values;
		this.init_values = init_values;
	}
	

    /**
	 * This constructor sets the type of the command and values of both unique vertex.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#NODE_INFO}</li>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#LINK_INFO}</li>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#CUT}</li>
	 * </ul>
     * 
	 * @param type A type of the command.
     * @param val1 A value of the first unique vertex identifier.
     * @param val2 A value of the second unique vertex identifier.
     */
	protected TQLCommand(CommandType type, TQLValue val1, TQLValue val2) {
		this.type = type;
    	this.value1 = val1;
    	this.value2 = val2;
    }


    /**
	 * This constructor sets the type of the command, values of both unique vertex
	 * identifiers and their neighbors, the list of values of declared fields and the list of initialized
	 * fields with their values. Neighbors determine where the edge will be created.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#LINK}</li>
	 * </ul>
     * 
	 * @param type A type of the command.
     * @param nodes A list of identifiers of vertices.
     * @param values A list of values for declared vertex or cluster class fields.
	 * @param init_values A list of vertex or cluster class fields and their values.
     */
	protected TQLCommand(CommandType type, TQLValue[] nodes,
			ArrayList<TQLValue> values, ArrayList<TQLValue> init_values) {
		assert nodes.length == 4;
		this.type = type;
    	this.value1 = nodes[0];
    	this.value2 = nodes[1];
    	this.value3 = nodes[2];
    	this.value4 = nodes[3];
    	this.values = values;
    	this.init_values = init_values;
   }

    
    /**
	 * This constructor sets the type of the command, the name of the function and values of
	 * arguments of the function.
	 * <p>
	 * It is typically called for these commands:
	 * <ul>
	 *   <li>{@link ttlangs.tql.TQLCommand.CommandType#FUNCTION}</li>
	 * </ul>
     * 
	 * @param type A type of the command.
     * @param name A name of the function.
     * @param values A list of values for arguments of the function.
     */
	protected TQLCommand(CommandType type, String name, ArrayList<TQLValue> values) {
		this.type = type;
    	this.name = name;
    	this.values = values;
    }

    
	/**
     * This method returns the name of a function. It should be called only when the command
     * represents a function.
     * 
     * @return A name of the function.
     */
	protected String getName() {
		return this.name;
	}


	/**
     * This method returns the value of the first unique vertex identifier.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A value of the first unique vertex identifier.
	 */
	protected TQLValue getValue1() {
		return this.value1;
	}


	/**
     * This method returns the value of the second unique vertex identifier.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A value of the second unique vertex identifier.
	 */
	protected TQLValue getValue2() {
		return this.value2;
	}


	/**
     * This method returns the value of the first identifier neighbor.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A value of the first identifier neighbor.
	 */
	protected TQLValue getValue3() {
		return this.value3;
	}


	/**
     * This method returns the value of the second identifier neighbor.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A value of the second identifier neighbor.
	 */
	protected TQLValue getValue4() {
		return this.value4;
	}


	/**
     * This method returns a type of the command.
	 * 
	 * @return A type of the command
	 */
	protected CommandType getType() {
		return this.type;
	}


	/**
     * This method returns a list of declared vertex or cluster class fields.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A list of declared vertex or cluster class fields.
	 */
	protected ArrayList<TQLVariable> getVariables() {
		return this.variables;
	}


	/**
     * This method returns a list of values.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A list of values.
	 */
	protected ArrayList<TQLValue> getValues() {
		return this.values;
	}


	/**
     * This method returns a list of vertex or cluster class fields and their values.
     * It should be called only for command that uses this informations and so it is set.
	 * 
	 * @return A list of vertex or cluster class fields and their values.
	 */
	protected ArrayList<TQLValue> getInitValues() {
		return this.init_values;
	}
		
}
