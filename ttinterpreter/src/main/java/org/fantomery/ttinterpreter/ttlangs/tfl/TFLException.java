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

/**
 * This exception is thrown during semantic analysis of TFL when any problem occurs.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class TFLException extends Exception {

	/**
	 * A version number for exception serialization.
	 */
	private static final long serialVersionUID = 1110317412837618709L;

	/**
	 * An exception constructor creates the exception with given message.
	 * 
	 * @param reason A message of the exception.
	 */
	protected TFLException(String reason) {
		super(reason);
	}

}
