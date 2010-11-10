/*
 *  Infinite Numbers Implementation
 * 
 *  The package numbers implements infinity numbers that supports basic arithmetic,
 *  comparison and others.   
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
package org.fantomery.ttinterpreter.numbers;


/**
 * An abstract class for representing and numbers including infinity and working with this possibly
 * infinite numbers.
 * <p>
 * A number can be finite, positive infinity, negative infinity or not a finite number. The class contains
 * methods for finding out the finiteness of the number and for comparing numbers.
 *
 * @param <N> A descendant of {@link java.lang.Number} which represents a type how finite numbers are saved
 * 				in concrete descendant of this abstract class. 
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public abstract class InfiniteNumber<N extends Number> {
	
	/**
	 * A list of all possible types of finiteness of a number. There are four types: number is finite,
	 * positive or negative infinity or it is not a number.
	 * 
	 * @author  Michal Vajbar
	 * @version 1.0
	 * @since	1.0
	 */
	public enum Finiteness {
		
		/**
		 * This value represents negative infinity.
		 */
		NEGATIVE_INFINITY,
		
		/**
		 * This value represents positive infinity.
		 */
		POSITIVE_INFINITY,
		
		/**
		 * This value represents finite number.
		 */
		FINITE,
		
		/**
		 * A value for representing of NaN (not a number).
		 */
		NOT_A_NUMBER
	};

	
	/**
	 * A method that finds out if this number is finite or it is not.
	 * 
	 * @return <code>True</code> if the number is finite, <code>False</code> if it is not.
	 */
	public abstract boolean isFinite();

	
	/**
	 * A method that finds out if this number is negative infinity or it is not.
	 * 
	 * @return <code>True</code> if the number is negative infinity, <code>False</code> if it is not.
	 */
	public abstract boolean isNegativeInfinity();
	
	
	/**
	 * A method that finds out if this number is positive infinity or it is not.
	 * 
	 * @return <code>True</code> if the number is positive infinity, <code>False</code> if it is not.
	 */
	public abstract boolean isPositiveInfinity();
	
	
	/**
	 * A method that finds out if this number is "not a number" or it is not.
	 * 
	 * @return <code>True</code> if the number is "not a number", <code>False</code> if it is not.
	 */
	public abstract boolean isNaN();


	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number is greater then <code>a</code> or <code>false</code> if it is not. 
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number is greater then <code>a</code> and <code>false</code> if 
	 * 			it is not.
	 */
	public abstract boolean isGreaterThen(InfiniteNumber<N> a);
	
	
	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number is greater then or equal to <code>a</code> or <code>false</code>
	 * if it is not.
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number is greater then or equal to <code>a</code> and
	 * 			 <code>false</code> if it is not.
	 */
	public abstract boolean isGreaterOrEqualTo(InfiniteNumber<N> a);

	
	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number is smaller then <code>a</code> or <code>false</code> if it is not.
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number is smaller then <code>a</code> and <code>false</code> if 
	 * 			it is not.
	 */
	public abstract boolean isLessThen(InfiniteNumber<N> a);
	
	
	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number is smaller then or equal to <code>a</code> or <code>false</code>
	 * if it is not.
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number is smaller then or equal to <code>a</code> and
	 * 			 <code>false</code> if it is not.
	 */
	public abstract boolean isLessOrEqualTo(InfiniteNumber<N> a);

	
	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number is equal to <code>a</code> or <code>false</code>
	 * if it is not.
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number is equal to <code>a</code> and <code>false</code>
	 * 			if it is not.
	 */
	public abstract boolean isEqualTo(InfiniteNumber<N> a);
	
	
	/**
	 * This method compares this instance of number and given number <code>a</code> and returns
	 * <code>true</code> if this number isn't equal to <code>a</code> or <code>false</code>
	 * if it is.
     * <p>
	 * The comparison works according to these rules:
	 * <ul>
	 *   <li>if any number is NaN then <code>false</code> is returned</li>
	 *   <li>positive (negative) infinities are equal themselves</li>
	 *   <li>finite numbers are compared in normal way</li>
	 *   <li>-infinity < finite number < infinity</li>
	 *   <li>NaN is not equal NaN</li>
	 * </ul>
	 * 
	 * @param a A possibly infinite number to compare.
	 * @return <code>True</code> if this number isn't equal to <code>a</code> and <code>false</code>
	 * 			if it is.
	 */
	public abstract boolean isNotEqualTo(InfiniteNumber<N> a);

}
