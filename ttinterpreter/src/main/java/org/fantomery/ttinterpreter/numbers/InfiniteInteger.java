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
 * This class represents integer numbers including infinity.
 * <p>
 * The number is internally saved through the use of two class fields - <code>type</code> and 
 * <code>finite_value</code>. Information about finiteness is hold in <code>type</code>. 
 * If the number is finite then its value is saved into <code>finite_value</code>. If the <code>type</code>
 * is not finite then the value of <code>finite_value</code> is not defined.
 * <p>
 * Upward the described representation there are built methods required 
 * by {@link numbers.InfiniteNumber} abstract ancestor. The class provides also methods
 * for doing basic arithmetic.
 * 
 * @author  Michal Vajbar
 * @version 1.0
 * @since	1.0
 */
public class InfiniteInteger extends InfiniteNumber<Integer> {
	

	/**
	 * This field holds an information about finiteness of represented number.
	 */
	private Finiteness type;
	
	
	/**
	 * This field holds a value of the represented number if the number is finite. If it is not finite,
	 * then the value of this field is not defined.
	 */
	private int finite_value;
	
	
	/**
	 * An empty constructor.
	 */
	public InfiniteInteger() {
	}
	
	
	/**
	 * This constructor creates instance of {@link numbers.InfiniteInteger} that represents
	 * non-finite number (infinity or NaN).
     * <p>
	 * It just sets value of given parameter into <code>type</code> field. The method doesn't set value of
	 * <code>finite_value</code> field. So the <code>type</code> should not be <code>FINITE</code> because
	 * <code>finite_value</code> stays undefined.
	 * 
	 * @param type A type of finiteness of created number. It should not be <code>FINITE</code>.
	 */
	public InfiniteInteger(Finiteness type) {
		assert type != Finiteness.FINITE;
		this.type = type;
	}
	
	
	/**
	 * This constructor creates instance of {@link numbers.InfiniteInteger} that represents
	 * finite number.
     * <p>
	 * The method sets value of given parameter into <code>finite_value</code> field and value 
	 * <code>FINITE</code> into <code>type</code>.
	 * 
	 * @param finite_value A finite value of the integer number.
	 */
	public InfiniteInteger(Integer finite_value) {
		this.type = Finiteness.FINITE;
		this.finite_value = finite_value;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isFinite() {
		return this.type == Finiteness.FINITE;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNegativeInfinity() {
		return this.type == Finiteness.NEGATIVE_INFINITY;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isPositiveInfinity() {
		return this.type == Finiteness.POSITIVE_INFINITY;
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNaN() {
		return this.type == Finiteness.NOT_A_NUMBER;
	}

	
	/**
	 * This method computes and returns a sum of given possibly infinite integer numbers.
     * <p>
	 * It takes numbers step-by-step and adds their value to the sum. The method proceeds according to these
	 * rules:
	 * <ul>
	 *   <li>the sum of two finite numbers is finite number</li>
	 *   <li>the sum of finite and infinite number is appropriate infinity</li>
	 *   <li>the sum of two positive (negative) infinities is positive infinity</li>
	 *   <li>the sum of positive and negative infinity is not a number (NaN)</li>
	 *   <li>if any number is NaN then the sum is NaN</li>
	 * </ul> 
	 * 
	 * @param summands An array of possibly infinite integer numbers to sum.
	 * @return A sum of given numbers.
	 */
	public static InfiniteInteger sum(InfiniteInteger... summands) {
		// initialization: sum is first number
		InfiniteInteger sum = new InfiniteInteger();
		sum.type = summands[0].type;
		sum.finite_value = summands[0].finite_value;
		
		// add step-by-step numbers to sum
		for(int i = 1; i < summands.length; i++) {
			switch (sum.type) {
			case FINITE:
				if (summands[i].isFinite()) {
					sum.finite_value = sum.finite_value + summands[i].finite_value;
				}
				else {
					sum.type = summands[i].type;
				}
				break;
			case NOT_A_NUMBER:
				return sum;
			case POSITIVE_INFINITY:
				if (summands[i].isNaN() || summands[i].isNegativeInfinity()) {
					sum.type = Finiteness.NOT_A_NUMBER;
				}
				break;
			case NEGATIVE_INFINITY:
				if (summands[i].isNaN() || summands[i].isPositiveInfinity()) {
					sum.type = Finiteness.NOT_A_NUMBER;
				}
				break;
			default:
				assert false;
			}
		}
		
		assert sum.type != null;
		return sum;
	}


	/**
	 * This method computes and returns a difference of two given numbers.
     * <p>
	 * The difference is computed according to these rules:
	 * <ul>
	 *   <li>the difference of two finite numbers is finite number</li>
	 *   <li>the difference of finite number and infinity is opposite infinity</li>
	 *   <li>the difference of infinity and finite number is the same infinity</li>
	 *   <li>the difference of two same infinities is not a number (NaN)</code></li>
	 *   <li>the difference of two opposite infinities is the minuend</code></li>
	 *   <li>if any number is NaN then the difference is NaN</code></li>
	 * </ul> 
	 * 
	 * @param minuend A possibly infinite integer number that is a minuend of the difference.
	 * @param subtrahend A possibly infinite integer number that is a subtrahend of the difference.
	 * @return A difference of two given numbers.
	 */
	public static InfiniteInteger difference(InfiniteInteger minuend, InfiniteInteger subtrahend) {
		
		// initialization:
		InfiniteInteger result = new InfiniteInteger();

		// look at type of minuend and subtrahend to compute difference
		switch (minuend.type) {
		case FINITE:
			switch (subtrahend.type) {
			case FINITE:
				result.type = Finiteness.FINITE;
				result.finite_value = minuend.finite_value - subtrahend.finite_value;
				break;
			case NOT_A_NUMBER:
				result.type = Finiteness.NOT_A_NUMBER;
				break;
			case POSITIVE_INFINITY:
				result.type = Finiteness.NEGATIVE_INFINITY;
				break;
			case NEGATIVE_INFINITY:
				result.type = Finiteness.POSITIVE_INFINITY;
				break;
			default:
				assert false;
			}
			break;
		case NOT_A_NUMBER:
			result.type = Finiteness.NOT_A_NUMBER;
			break;
		case POSITIVE_INFINITY:
			if (subtrahend.isNaN() || subtrahend.isPositiveInfinity()) {
				result.type = Finiteness.NOT_A_NUMBER;
			}
			else {
				result.type = Finiteness.POSITIVE_INFINITY;
			}
			break;
		case NEGATIVE_INFINITY:
			if (subtrahend.isNaN() || subtrahend.isNegativeInfinity()) {
				result.type = Finiteness.NOT_A_NUMBER;
			}
			else {
				result.type = Finiteness.NEGATIVE_INFINITY;
			}
			break;
		default:
			assert false;
		}
		
		assert result.type != null;
		return result;
	}
	
	
	/**
	 * This method computes and returns a product of given possibly infinite integer numbers.
     * <p>
	 * It takes numbers step-by-step and adds their value to the product. The method proceeds according 
	 * to these rules:
	 * <ul>
	 *   <li>the product of two finite numbers is finite number</li>
	 *   <li>the product of finite and infinite number is appropriate infinity</li>
	 *   <li>the product of two positive (negative) infinities is positive infinity</li>
	 *   <li>the product of positive and negative infinity is negative infinity</li>
	 *   <li>the product of infinity and 0 is not a number (NaN)</li>
	 *   <li>if any number is NaN then the product is NaN</li>
	 * </ul> 
	 * 
	 * @param factors An array of possibly infinite integer numbers to product.
	 * @return A product of given numbers.
	 */
	public static InfiniteInteger product(InfiniteInteger... factors) {
		// initialization: product is first number
		InfiniteInteger result = new InfiniteInteger();
		result.type = factors[0].type;
		result.finite_value = factors[0].finite_value;
		
		// add step-by-step numbers to product
		for(int i = 1; i < factors.length; i++) {
			switch (result.type) {
			case FINITE:
				if (factors[i].isFinite()) {
					result.finite_value = result.finite_value * factors[i].finite_value;
				}
				else {
					int comparison = ((Integer) result.finite_value).compareTo(0);
					if (comparison == 0) {
						result.type = Finiteness.NOT_A_NUMBER;
					}
					else if (comparison < 0) {
						switch (factors[i].type) {
						case NOT_A_NUMBER:
							result.type = Finiteness.NOT_A_NUMBER;
							break;
						case NEGATIVE_INFINITY:
							result.type = Finiteness.POSITIVE_INFINITY;
							break;
						case POSITIVE_INFINITY:
							result.type = Finiteness.NEGATIVE_INFINITY;
							break;
						default:
							assert false;
						}
					}
					else {
						assert comparison > 0;
						result.type = factors[i].type;
					}
				}
				break;
			case NOT_A_NUMBER:
				return result;
			case POSITIVE_INFINITY:
				switch (factors[i].type) {
				case FINITE:
					int comparison = ((Integer) factors[i].finite_value).compareTo(0);
					if (comparison == 0) {
						result.type = Finiteness.NOT_A_NUMBER;
					}
					else if (comparison < 0) {
						result.type = Finiteness.NEGATIVE_INFINITY;
					}
					else {
						assert comparison > 0;
						result.type = Finiteness.POSITIVE_INFINITY;
					}
					break;
				case NOT_A_NUMBER:
					result.type = Finiteness.NOT_A_NUMBER;
					break;
				case POSITIVE_INFINITY:
					result.type = Finiteness.POSITIVE_INFINITY;
					break;
				case NEGATIVE_INFINITY:
					result.type = Finiteness.NEGATIVE_INFINITY;
					break;
				default:
					assert false;
				}
				break;
			case NEGATIVE_INFINITY:
				switch (factors[i].type) {
				case FINITE:
					int comparison = ((Integer) factors[i].finite_value).compareTo(0);
					if (comparison == 0) {
						result.type = Finiteness.NOT_A_NUMBER;
					}
					else if (comparison < 0) {
						result.type = Finiteness.POSITIVE_INFINITY;
					}
					else {
						assert comparison > 0;
						result.type = Finiteness.NEGATIVE_INFINITY;
					}
					break;
				case NOT_A_NUMBER:
					result.type = Finiteness.NOT_A_NUMBER;
					break;
				case POSITIVE_INFINITY:
					result.type = Finiteness.NEGATIVE_INFINITY;
					break;
				case NEGATIVE_INFINITY:
					result.type = Finiteness.POSITIVE_INFINITY;
					break;
				default:
					assert false;
				}
				break;
			default:
				assert false;
			}
		}
		
		assert result.type != null;
		return result;
	}


	/**
	 * This method computes and returns a result of an integral division of two given numbers.
     * <p>
	 * The result (quotient) is computed according to these rules:
	 * <ul>
	 *   <li>if the divisor is 0 then {@link java.lang.ArithmeticException} is thrown</li>
	 *   <li>the quotient of two finite numbers is finite</li>
	 *   <li>the quotient of finite and infinite number is 0</li>
	 *   <li>the quotient of infinite and finite number is appropriate infinity</li>
	 *   <li>the quotient of two infinities is not a number (NaN)</li>
	 *   <li>if any number is NaN then the result is NaN</li>
	 * </ul> 
	 * 
	 * @param dividend A possibly infinite integer number that is a dividend of the quotient.
	 * @param divisor A possibly infinite integer number that is a divisor of the quotient.
	 * @return A result of an integral division of two given numbers.
	 */
	public static InfiniteInteger quotient(InfiniteInteger dividend, InfiniteInteger divisor) {

		// initialization:
		InfiniteInteger result = new InfiniteInteger();

		// if divisor is 0 then throw exception
		if (divisor.isFinite() && ((Integer) divisor.finite_value).compareTo(0) == 0) {
			throw new ArithmeticException("/ by zero");
		}
		
		// look at type of dividend and divisor to compute difference
		switch (dividend.type) {
		case FINITE:
			if (divisor.isFinite()) {
				result.type = Finiteness.FINITE;
				result.finite_value = dividend.finite_value / divisor.finite_value;
			}
			else {
				switch (divisor.type) {
				case NOT_A_NUMBER:
					result.type = Finiteness.NOT_A_NUMBER;
					break;
				case NEGATIVE_INFINITY:
					result.type = Finiteness.FINITE;
					result.finite_value = 0;
					break;
				case POSITIVE_INFINITY:
					result.type = Finiteness.FINITE;
					result.finite_value = 0;
					break;
				default:
					assert false;
				}
			}
			break;
		case NOT_A_NUMBER:
			result.type = Finiteness.NOT_A_NUMBER;
			break;
		case POSITIVE_INFINITY:
			if (divisor.isFinite()) {
				int comparison = ((Integer) divisor.finite_value).compareTo(0);
				if (comparison < 0) {
					result.type = Finiteness.NEGATIVE_INFINITY;
				}
				else {
					assert comparison > 0;
					result.type = Finiteness.POSITIVE_INFINITY;
				}
			}
			else {
				result.type = Finiteness.NOT_A_NUMBER;
			}
			break;
		case NEGATIVE_INFINITY:
			if (divisor.isFinite()) {
				int comparison = ((Integer) divisor.finite_value).compareTo(0);
				if (comparison < 0) {
					result.type = Finiteness.POSITIVE_INFINITY;
				}
				else {
					assert comparison > 0;
					result.type = Finiteness.NEGATIVE_INFINITY;
				}
			}
			else {
				result.type = Finiteness.NOT_A_NUMBER;
			}
			break;
		default:
			assert false;
		}
		
		assert result.type != null;
		return result;
	}
	

	/**
	 * This method adds a <code>summand</code> to this instance of possibly infinite number, saves
	 * a result into this instance and returns it.
	 * <p>
	 * The method uses {@link numbers.InfiniteInteger#sum(InfiniteInteger...)} to
	 * compute new value.
	 * 
	 * @param summand A number that will be added.
	 * @return A result - this instance with new value.
	 */
	public InfiniteInteger plus(InfiniteInteger summand) {
		// compute new value for this and save it
		InfiniteInteger sum = InfiniteInteger.sum(this, summand);
		this.type = sum.type;
		this.finite_value = sum.finite_value;
		// return the result
		return this;
	}

	
	/**
	 * This method computes a difference of this instance of possibly infinite number and 
	 * <code>subtrahend</code>, saves a result into this instance and returns it.
	 * <p>
	 * The method uses
	 * {@link numbers.InfiniteInteger#difference(InfiniteInteger, InfiniteInteger)} to
	 * compute new value.
	 * 
	 * @param subtrahend A number that is subtrahend of computed difference.
	 * @return A result - this instance with new value.
	 */
	public InfiniteInteger minus(InfiniteInteger subtrahend) {
		// compute new value for this and save it
		InfiniteInteger difference = InfiniteInteger.difference(this, subtrahend);
		this.type = difference.type;
		this.finite_value = difference.finite_value;
		// return the result
		return this;
	}

	
	/**
	 * This method multiplies this instance of possibly infinite number by a <code>summand</code>,
	 * saves a result into this instance and returns it.
	 * <p>
	 * The method uses {@link numbers.InfiniteInteger#product(InfiniteInteger...)} to
	 * compute new value.
	 * 
	 * @param factor A number that will multiply a value of this instance.
	 * @return A result - this instance with new value.
	 */
	public InfiniteInteger times(InfiniteInteger factor) {
		// compute new value for this and save it
		InfiniteInteger product = InfiniteInteger.product(this, factor);
		this.type = product.type;
		this.finite_value = product.finite_value;
		// return the result
		return this;
	}

	
	/**
	 * This method computes a quotient of this instance of possibly infinite number and 
	 * <code>divisor</code>, saves a result into this instance and returns it.
	 * <p>
	 * The method uses
	 * {@link numbers.InfiniteInteger#quotient(InfiniteInteger, InfiniteInteger)} to
	 * compute new value.
	 * 
	 * @param divisor A number that is divisor of computed division.
	 * @return A result - this instance with new value.
	 */
	public InfiniteInteger divide(InfiniteInteger divisor) {
		// compute new value for this and save it
		InfiniteInteger quotient = InfiniteInteger.quotient(this, divisor);
		this.type = quotient.type;
		this.finite_value = quotient.finite_value;
		// return the result
		return this;
	}

	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isGreaterThen(InfiniteNumber<Integer> a) {
	
		if (this.isNaN() || a.isNaN()) {
			return false;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value > ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return !a.isPositiveInfinity() ? true : false;
		case NEGATIVE_INFINITY:
			return false;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return a.isNegativeInfinity() ? true : false;
		}
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isGreaterOrEqualTo(InfiniteNumber<Integer> a) {

		if (this.isNaN() || a.isNaN()) {
			return false;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value >= ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return true;
		case NEGATIVE_INFINITY:
			return a.isNegativeInfinity() ? true : false;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return a.isNegativeInfinity() ? true : false;
		}
	}

	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isLessThen(InfiniteNumber<Integer> a) {
		
		if (this.isNaN() || a.isNaN()) {
			return false;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value < ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return false;
		case NEGATIVE_INFINITY:
			return !a.isNegativeInfinity() ? true : false;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return a.isPositiveInfinity() ? true : false;
		}
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isLessOrEqualTo(InfiniteNumber<Integer> a) {

		if (this.isNaN() || a.isNaN()) {
			return false;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value <= ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return a.isPositiveInfinity() ? true : false;
		case NEGATIVE_INFINITY:
			return true;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return a.isPositiveInfinity() ? true : false;
		}
	}

	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isEqualTo(InfiniteNumber<Integer> a) {
		
		if (this.isNaN() || a.isNaN()) {
			return false;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value == ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return a.isPositiveInfinity() ? true : false;
		case NEGATIVE_INFINITY:
			return a.isNegativeInfinity() ? true : false;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return false;
		}
	}
	
	
	/*
	 * Same javadoc as in the interface. 
	 */
	public boolean isNotEqualTo(InfiniteNumber<Integer> a) {
	
		if (this.isNaN() || a.isNaN()) {
			return true;
		}
		
		if (this.isFinite() && a.isFinite()) {
			return this.finite_value != ((InfiniteInteger) a).finite_value;
		}
		
		switch (this.type) {
		case POSITIVE_INFINITY:
			return !a.isPositiveInfinity() ? true : false;
		case NEGATIVE_INFINITY:
			return !a.isNegativeInfinity() ? true : false;
		default:
			assert this.isFinite();
			assert a.isPositiveInfinity() || a.isNegativeInfinity();
			return true;
		}
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
	public static InfiniteInteger maximum(InfiniteInteger... inf_numbers) {
		
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
	public static InfiniteInteger minimum(InfiniteInteger... inf_numbers) {
		
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
	 * This method returns a value saved in <code>finite_value</code> field. It should be called only if 
	 * this instance is finite.
	 * 
	 * @return A value saved in <code>value</code>.
	 */
	public int getFiniteValue() {
		assert isFinite();
		return this.finite_value;
	}
	
	
	/**
	 * This method returns a value of the integer number as a string.
	 */
	public String toString() {
	
		String s = null;
		
		switch (this.type) {
		case FINITE:
			s = String.valueOf(this.finite_value);
			break;
		case NOT_A_NUMBER:
			s =  "NaN";
			break;
		case NEGATIVE_INFINITY:
			s =  "-infinity";
			break;
		case POSITIVE_INFINITY:
			s =  "infinity";
			break;
		default:
			assert false;
		}

		return s;

	}

}
