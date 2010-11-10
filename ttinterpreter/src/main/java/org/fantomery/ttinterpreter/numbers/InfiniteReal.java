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
 * This class represents real numbers including infinity. The number is internally saved as
 * a primitive type <code>double</code>. Upward the <code>double</code> there are built methods required 
 * by {@link numbers.InfiniteNumber} abstract ancestor. The class provides also methods
 * for doing
 * basic arithmetic.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class InfiniteReal extends InfiniteNumber<Double> {
	

	/**
	 * A field for representing the possibly infinity number.
	 */
	private double value;
	
	
	/**
	 * An empty constructor.
	 */
	public InfiniteReal() {
	}
	
	
	/**
	 * This constructor creates new instance of {@link numbers.InfiniteReal} that represents
	 * non-finite number. If the type of number is <code>FINITE</code> then it does nothing.
	 *  
	 * @param type A value of enumeration type {@link numbers.InfiniteNumber.Finiteness}.
	 */
	public InfiniteReal(Finiteness type) {
		// assign Double value according to the given type
		switch (type) {
		case NOT_A_NUMBER:
			this.value = Double.NaN;
			break;
		case NEGATIVE_INFINITY:
			this.value = Double.NEGATIVE_INFINITY;
			break;
		case POSITIVE_INFINITY:
			this.value = Double.POSITIVE_INFINITY;
			break;
		default:
			// for FINITE value nothing to do
			assert false;
		}
	}
	
	
	/**
	 * This constructor creates an instance of {@link numbers.InfiniteReal} that represents
	 * any possibly infinite number.
     * <p>
	 * It just assigns given <code>Double</code> into the appropriate field in the class.
	 * 
	 * @param value A value of created number.
	 */
	public InfiniteReal(Double value) {
		this.value = value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isFinite() {
		return !Double.isInfinite(this.value) && !Double.isNaN(this.value);
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNegativeInfinity() {
		return this.value == Double.NEGATIVE_INFINITY;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isPositiveInfinity() {
		return this.value == Double.POSITIVE_INFINITY;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNaN() {
		return Double.isNaN(this.value);
	}

	
	/**
	 * This method computes a sum of real numbers that are given thru array <code>reals</code> and
	 * returns it.
     * <p>
	 * It just takes internal <code>double</code> values of the given numbers and computes the sum. 
	 * 
	 * @param summands An array of real numbers from which the sum is computed.
	 * @return Instance of {@link numbers.InfiniteReal} that contains the product.
	 */
	public static InfiniteReal sum(InfiniteReal... summands) {
		
		InfiniteReal sum = new InfiniteReal(summands[0].value);
		
		for(int i = 1; i < summands.length; i++) {
			sum.value += summands[i].value;
		}
		
		return sum;
	}
	

	/**
	 * This method computes a difference of two given real numbers and returns it.
     * <p>
	 * The difference is computed from Double values of <code>minuend</code> and <code>subtrahend</code>.
	 * 
	 * @param minuend A real number that is minuend of the difference.
	 * @param subtrahend A real number that is subtrahend of the difference.
	 * @return Instance of {@link numbers.InfiniteReal} that contains the result.
	 */
	public static InfiniteReal difference(InfiniteReal minuend, InfiniteReal subtrahend) {
		
		return new InfiniteReal(minuend.value - subtrahend.value);
		
	}
	
	
	/**
	 * This method computes a product of real numbers that are given thru array <code>reals</code> and
	 * returns it.
     * <p>
	 * It just takes internal Double values of the given numbers and computes the product.
	 * 
	 * @param factors An array of real numbers from which the product is computed.
	 * @return Instance of {@link numbers.InfiniteReal} that contains the product.
	 */
	public static InfiniteReal product(InfiniteReal... factors) {
		
		InfiniteReal result = new InfiniteReal(factors[0].value);
		
		for(int i = 1; i < factors.length; i++) {
			result.value *= factors[i].value;
		}
		
		return result;
	}


	/**
	 * This method computes a quotient of two given real numbers and returns it.
     * <p>
	 * The quotient is computed from Double values of <code>dividend</code> and <code>divisor</code>.
	 * 
	 * @param dividend A real number that is dividend of the difference.
	 * @param divisor A real number that is divisor of the difference.
	 * @return Instance of {@link numbers.InfiniteReal} that contains the result.
	 */
	public static InfiniteReal quotient(InfiniteReal dividend, InfiniteReal divisor) {
		
		return new InfiniteReal(dividend.value / divisor.value);
		
	}
	

	/**
	 * This method adds a <code>summand</code> to this instance of possibly infinite number,
	 * saves a result into this instance and returns it.
	 * <p>
	 * The result is computed from Double values of this instance and the argument.
	 * 
	 * @param summand A number that will be added.
	 * @return A result - this instance with new value.
	 */
	public InfiniteReal plus(InfiniteReal summand) {
		// compute new value for this and save it
		this.value += summand.value;
		// return the result
		return this;
	}

	
	/**
	 * This method computes a difference of this instance of possibly infinite number and 
	 * <code>subtrahend</code>, saves a result into this instance and returns it.
	 * <p>
	 * The result is computed from Double values of this instance and the argument.
	 * 
	 * @param subtrahend A number that is subtrahend of computed difference.
	 * @return A result - this instance with new value.
	 */
	public InfiniteReal minus(InfiniteReal subtrahend) {
		// compute new value for this and save it
		this.value -= subtrahend.value;
		// return the result
		return this;
	}

	
	/**
	 * This method multiplies this instance of possibly infinite number by a <code>summand</code>,
	 * saves a result into this instance and returns it.
	 * <p>
	 * The result is computed from Double values of this instance and the argument.
	 * 
	 * @param factor A number that will multiply a value of this instance.
	 * @return A result - this instance with new value.
	 */
	public InfiniteReal times(InfiniteReal factor) {
		// compute new value for this and save it
		this.value *= factor.value;
		// return the result
		return this;
	}

	
	/**
	 * This method computes a quotient of this instance of possibly infinite number and 
	 * <code>divisor</code>, saves a result into this instance and returns it.
	 * <p>
	 * The result is computed from Double values of this instance and the argument.
	 * 
	 * @param divisor A number that is divisor of computed division.
	 * @return A result - this instance with new value.
	 */
	public InfiniteReal divide(InfiniteReal divisor) {
		// compute new value for this and save it
		this.value /= divisor.value;
		// return the result
		return this;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isGreaterThen(InfiniteNumber<Double> a) {
		
		return this.value > ((InfiniteReal) a).value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isGreaterOrEqualTo(InfiniteNumber<Double> a) {
		
		return this.value >= ((InfiniteReal) a).value;
	}

	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isLessThen(InfiniteNumber<Double> a) {
		
		return this.value < ((InfiniteReal) a).value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isLessOrEqualTo(InfiniteNumber<Double> a) {
		
		return this.value <= ((InfiniteReal) a).value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isEqualTo(InfiniteNumber<Double> a) {
		
		return this.value == ((InfiniteReal) a).value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNotEqualTo(InfiniteNumber<Double> a) {
		
		return this.value != ((InfiniteReal) a).value;
	}
	
	
	/**
	 * This method chooses and returns a maximum of given numbers.
    * <p>
	 * If any number is NaN then the result is NaN. The method compares numbers step-by-step and the greatest
	 * of them is the result.
	 * 
	 * @param inf_numbers Possibly infinity numbers.
	 * @return A maximum from given numbers.
	 */
	public static InfiniteReal maximum(InfiniteReal... inf_numbers) {
		
		// if first number is NaN then it is the result
		if (inf_numbers[0].isNaN()) {
			return inf_numbers[0];
		} 
		
		// current maximum is on position 0 
		int max = 0;

		// find maximum step-by-step
		for (int i = 1; i < inf_numbers.length; i++) {
			// NaN => result is NaN
			if (inf_numbers[i].isNaN()) {
				return inf_numbers[i];
			}
			// comparison
			if (inf_numbers[max].isLessThen(inf_numbers[i])) {
				max = i;
			}
		}
		
		// return result
		return inf_numbers[max];
	}
	
	
	/**
	 * This method chooses and returns a minimum of given numbers.
    * <p>
	 * If any number is NaN then the result is NaN. The method compares numbers step-by-step and the smallest
	 * of them is the result.
	 * 
	 * @param inf_numbers Possibly infinity numbers.
	 * @return A minimum of given numbers.
	 */
	public static InfiniteReal minimum(InfiniteReal... inf_numbers) {
		
		// if first number is NaN then it is the result
		if (inf_numbers[0].isNaN()) {
			return inf_numbers[0];
		} 
		
		// current maximum is on position 0 
		int max = 0;

		// find minimum step-by-step
		for (int i = 1; i < inf_numbers.length; i++) {
			// NaN => result is NaN
			if (inf_numbers[i].isNaN()) {
				return inf_numbers[i];
			}
			// comparison
			if (inf_numbers[max].isGreaterThen(inf_numbers[i])) {
				max = i;
			}
		}
		
		// return result
		return inf_numbers[max];
	}
	
	
	/**
	 * This method returns a value saved in <code>value</code> field. It should be called only if 
	 * this instance is finite.
	 * 
	 * @return A value saved in <code>value</code>.
	 */
	public double getFiniteValue() {
		assert isFinite();
		return this.value;
	}
	
	
	/**
	 * This method returns a value of the real number as a string.
	 */
	public String toString() {
		// just use toString of Double
		return String.valueOf(this.value);

	}
	
}
