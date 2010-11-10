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

import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.DataType;


/**
 * This class represents one expression used during compiling Top Tree Functional Language source file into
 * Java source file.
 * <p>
 * The class encapsulates a source code of generated expression and its data type. It contains tools for
 * creating the expression and for obtaining informations about it. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
class TFLExpression {

	
	/**
	 * A field for saving a code during generating Java code from TFL.
	 */
	private ArrayList<String> source;
	
	
	/**
	 * A data type of this expression
	 */
	private DataType data_type;
	
	
	/**
	 * A constructor that creates new instance of this class from data type and one string.
	 * 
	 * @param data_type A data type of created expression.
	 * @param source A string that contains code of created expression.
	 */
	protected TFLExpression(DataType data_type, String source) {
		this.data_type	= data_type;
		this.source		= new ArrayList<String>();
		this.source.add(source);
	}


	/**
	 * A constructor that creates new instance of this class from data type and a list of strings.
	 * 
	 * @param data_type A data type of created expression.
	 * @param source A list of strings that contains code of created expression.
	 */
	protected TFLExpression(DataType data_type, ArrayList<String> source) {
		this.data_type	= data_type;
		this.source		= source;
	}

	
	/**
	 * This method just returns a value of the field <code>source</code>.
	 * 
	 * @return A value of the field <code>source</code>.
	 */
	protected ArrayList<String> getSource() {
		return source;
	}


	/**
	 * This method just returns a type of this expression.
	 * 
	 * @return A value of the field <code>data_type</code>.
	 */
	protected DataType getDataType() {
		return data_type;
	}
	
}
