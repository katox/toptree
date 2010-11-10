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

/**
 * This error is thrown during syntactic analysis of TQL command when any problem occurs.
 * <p>
 * It is necessary to use the error instead an exception because syntax is checked by JFlex and Cup.
 * These libraries are generated and it is not possible to teach them using our special exception. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class TQLError extends Error {

	/**
	 * A version number for error serialization.
	 */
	private static final long serialVersionUID = -1295848209032833429L;

	/**
	 * An error constructor creates the error with given message.
	 * 
	 * @param reason A message of the error.
	 */
	protected TQLError(String reason) {
		super(reason);
	}

}
