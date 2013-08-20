package net.aufdemrand.denizen.utilities.javaluator;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

/** An evaluator that is able to evaluate arithmetic expressions on real numbers.
 * <br>Built-in operators:<ul>
 * <li>+: Addition</li>
 * <li>-: Subtraction</li>
 * <li>-: Unary minus</li>
 * <li>*: Multiplication</li>
 * <li>/: Division</li>
 * <li>^: Exponentiation</li>
 * <li>%: Modulo</li>
 * </ul>
 * Built-in functions:<ul>
 * <li>abs: absolute value</li>
 * <li>acos: arc cosine</li>
 * <li>asin: arc sine</li>
 * <li>atan: arc tangent</li>
 * <li>average: average of arguments</li>
 * <li>ceil: nearest upper integer</li>
 * <li>cos: cosine</li>
 * <li>cosh: hyperbolic cosine</li>
 * <li>floor: nearest lower integer</li>
 * <li>ln: natural logarithm (base e)</li>
 * <li>log: base 10 logarithm</li>
 * <li>max: maximum of arguments</li>
 * <li>min: minimum of arguments</li>
 * <li>round: nearest integer</li>
 * <li>sin: sine</li>
 * <li>sinh: hyperbolic sine</li>
 * <li>sum: sum of arguments</li>
 * <li>tan: tangent</li>
 * <li>tanh: hyperbolic tangent</li>
 * <li>random: pseudo-random number (between 0 and 1)</li>
 * </ul>
 * Built-in constants:<ul>
 * <li>e: Base of natural algorithms</li>
 * <li>pi: Ratio of the circumference of a circle to its diameter</li>
 * </ul>
 * @author Jean-Marc Astesana
 * @see <a href="../../../license.html">License information</a>
 */
public class DoubleEvaluator extends AbstractEvaluator<Double> {
    /** The order or operations (operator precedence) is not clearly defined, especially between the unary minus operator and exponentiation
     * operator (see <a href="http://en.wikipedia.org/wiki/Order_of_operations#Exceptions_to_the_standard">http://en.wikipedia.org/wiki/Order_of_operations</a>).
     * These constants define the operator precedence styles.
     */
    public static enum Style {
        /** The most commonly operator precedence, where the unary minus as a lower precedence than the exponentiation.
         * <br>With this style, used by Google, Wolfram alpha, and many others, -2^2=-4.
         */
        STANDARD, 
        /** The operator precedence used by Excel, or bash shell script language, where the unary minus as a higher precedence than the exponentiation.
         * <br>With this style, -2^2=4.
         */
        EXCEL 
    }
    
    /** A constant that represents pi (3.14159...) */
    public final static Constant PI = new Constant("pi");
    /** A constant that represents e (2.718281...) */
    public final static Constant E = new Constant("e");
    
    /** Returns the smallest integer >= argument */
    public final static Function CEIL = new Function("ceil", 1);
    /** Returns the largest integer <= argument */
    public final static Function FLOOR = new Function("floor", 1);
    /** Returns the closest integer of a number */
    public final static Function ROUND = new Function("round", 1);
    /** Returns the absolute value of a number */
    public final static Function ABS = new Function("abs", 1);

    /** Returns the trigonometric sine of an angle. The angle is expressed in radian.*/
    public final static Function SINE = new Function("sin", 1);
    /** Returns the trigonometric cosine of an angle. The angle is expressed in radian.*/
    public final static Function COSINE = new Function("cos", 1);
    /** Returns the trigonometric tangent of an angle. The angle is expressed in radian.*/
    public final static Function TANGENT = new Function("tan", 1);
    /** Returns the trigonometric arc-cosine of an angle. The angle is expressed in radian.*/
    public final static Function ACOSINE = new Function("acos", 1);
    /** Returns the trigonometric arc-sine of an angle. The angle is expressed in radian.*/
    public final static Function ASINE = new Function("asin", 1);
    /** Returns the trigonometric arc-tangent of an angle. The angle is expressed in radian.*/
    public final static Function ATAN = new Function("atan", 1);

    /** Returns the hyperbolic sine of a number.*/
    public final static Function SINEH = new Function("sinh", 1);
    /** Returns the hyperbolic cosine of a number.*/
    public final static Function COSINEH = new Function("cosh", 1);
    /** Returns the hyperbolic tangent of a number.*/
    public final static Function TANGENTH = new Function("tanh", 1);

    /** Returns the minimum of n numbers (n>=1) */
    public final static Function MIN = new Function("min", 1, Integer.MAX_VALUE);
    /** Returns the maximum of n numbers (n>=1) */
    public final static Function MAX = new Function("max", 1, Integer.MAX_VALUE);
    /** Returns the sum of n numbers (n>=1) */
    public final static Function SUM = new Function("sum", 1, Integer.MAX_VALUE);
    /** Returns the average of n numbers (n>=1) */
    public final static Function AVERAGE = new Function("avg", 1, Integer.MAX_VALUE);

    /** Returns the natural logarithm of a number */
    public final static Function LN = new Function("ln", 1);
    /** Returns the decimal logarithm of a number */
    public final static Function LOG = new Function("log", 1);
    
    /** Returns a pseudo random number */
    public final static Function RANDOM = new Function("random", 0);

    /** The negate unary operator in the standard operator precedence.*/
    public final static Operator NEGATE = new Operator("-", 1, Operator.Associativity.RIGHT, 3);
    /** The negate unary operator in the Excel like operator precedence.*/
    public final static Operator NEGATE_HIGH = new Operator("-", 1, Operator.Associativity.RIGHT, 5);
    /** The substraction operator.*/
    public final static Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 1);
    /** The addition operator.*/
    public final static Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 1);
    /** The multiplication operator.*/
    public final static Operator MULTIPLY = new Operator("*", 2, Operator.Associativity.LEFT, 2);
    /** The division operator.*/
    public final static Operator DIVIDE = new Operator("/", 2, Operator.Associativity.LEFT, 2);
    /** The exponentiation operator.*/
    public final static Operator EXPONENT = new Operator("^", 2, Operator.Associativity.LEFT, 4);
    /** The <a href="http://en.wikipedia.org/wiki/Modulo_operation">modulo operator</a>.*/
    public final static Operator MODULO = new Operator("%", 2, Operator.Associativity.LEFT, 2);

    /** The standard whole set of predefined operators */
    private static final Operator[] OPERATORS = new Operator[]{NEGATE, MINUS, PLUS, MULTIPLY, DIVIDE, EXPONENT, MODULO};
    /** The excel like whole set of predefined operators */
    private static final Operator[] OPERATORS_EXCEL = new Operator[]{NEGATE_HIGH, MINUS, PLUS, MULTIPLY, DIVIDE, EXPONENT, MODULO};
    /** The whole set of predefined functions */
    private static final Function[] FUNCTIONS = new Function[]{SINE, COSINE, TANGENT, ASINE, ACOSINE, ATAN, SINEH, COSINEH, TANGENTH, MIN, MAX, SUM, AVERAGE, LN, LOG, ROUND, CEIL, FLOOR, ABS, RANDOM};
    /** The whole set of predefined constants */
    private static final Constant[] CONSTANTS = new Constant[]{PI, E};
    
    private static Parameters DEFAULT_PARAMETERS;
    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.US);
    
    /** Gets a copy of DoubleEvaluator standard default parameters.
     * <br>The returned parameters contains all the predefined operators, functions and constants.
     * <br>Each call to this method create a new instance of Parameters. 
     * @return a Paramaters instance
     * @see Style
     */
    public static Parameters getDefaultParameters() {
        return getDefaultParameters(Style.STANDARD);
    }
    
    /** Gets a copy of DoubleEvaluator default parameters.
     * <br>The returned parameters contains all the predefined operators, functions and constants.
     * <br>Each call to this method create a new instance of Parameters. 
     * @return a Paramaters instance
     */
    public static Parameters getDefaultParameters(Style style) {
        Parameters result = new Parameters();
        result.addOperators(style==Style.STANDARD?Arrays.asList(OPERATORS):Arrays.asList(OPERATORS_EXCEL));
        result.addFunctions(Arrays.asList(FUNCTIONS));
        result.addConstants(Arrays.asList(CONSTANTS));
        result.addFunctionBracket(BracketPair.PARENTHESES);
        result.addExpressionBracket(BracketPair.PARENTHESES);
        return result;
    }

    private static Parameters getParameters() {
        if (DEFAULT_PARAMETERS == null) {
            DEFAULT_PARAMETERS = getDefaultParameters();
        }
        return DEFAULT_PARAMETERS;
    }
    
    /** Constructor.
     * <br>This default constructor builds an instance with all predefined operators, functions and constants. 
     */
    public DoubleEvaluator() {
        this(getParameters());
    }

    /** Constructor.
     * <br>This constructor can be used to reduce the set of supported operators, functions or constants,
     * or to localize some function or constant's names.
     * @param parameters The parameters of the evaluator.
     */
    public DoubleEvaluator(Parameters parameters) {
        super(parameters);
    }

    @Override
    protected Double toValue(String literal, Object evaluationContext) {
        ParsePosition p = new ParsePosition(0);
        Number result = FORMATTER.parse(literal, p);
        if (p.getIndex()==0 || p.getIndex()!=literal.length()) throw new IllegalArgumentException(literal+" is not a number");
        return result.doubleValue();
    }
    
    /* (non-Javadoc)
     * @see net.astesana.javaluator.AbstractEvaluator#evaluate(net.astesana.javaluator.Constant)
     */
    @Override
    protected Double evaluate(Constant constant, Object evaluationContext) {
        if (constant==PI) {
            return Math.PI;
        } else if (constant==E) {
            return Math.E;
        } else {
            return super.evaluate(constant, evaluationContext);
        }
    }

    /* (non-Javadoc)
     * @see net.astesana.javaluator.AbstractEvaluator#evaluate(net.astesana.javaluator.Operator, java.util.Iterator)
     */
    @Override
    protected Double evaluate(Operator operator, Iterator<Double> operands, Object evaluationContext) {
        if (operator==NEGATE || operator==NEGATE_HIGH) {
            return -operands.next();
        } else if (operator==MINUS) {
            return operands.next() - operands.next();
        } else if (operator==PLUS) {
            return operands.next() + operands.next();
        } else if (operator==MULTIPLY) {
            return operands.next() * operands.next();
        } else if (operator==DIVIDE) {
            return operands.next() / operands.next();
        } else if (operator==EXPONENT) {
            return Math.pow(operands.next(),operands.next());
        } else if (operator==MODULO) {
            return operands.next() % operands.next();
        } else {
            return super.evaluate(operator, operands, evaluationContext);
        }
    }

    /* (non-Javadoc)
     * @see net.astesana.javaluator.AbstractEvaluator#evaluate(net.astesana.javaluator.Function, java.util.Iterator)
     */
    @Override
    protected Double evaluate(Function function, Iterator<Double> arguments, Object evaluationContext) {
        Double result;
        if (function==ABS) {
            result = Math.abs(arguments.next());
        } else if (function==CEIL) {
            result = Math.ceil(arguments.next());
        } else if (function==FLOOR) {
            result = Math.floor(arguments.next());
        } else if (function==ROUND) {
            Double arg = arguments.next();
            if (arg==Double.NEGATIVE_INFINITY || arg==Double.POSITIVE_INFINITY) {
                result = arg;
            } else {
                result = (double) Math.round(arg);
            }
        } else if (function==SINEH) {
            result = Math.sinh(arguments.next());
        } else if (function==COSINEH) {
            result = Math.cosh(arguments.next());
        } else if (function==TANGENTH) {
            result = Math.tanh(arguments.next());
        } else if (function==SINE) {
            result = Math.sin(arguments.next());
        } else if (function==COSINE) {
            result = Math.cos(arguments.next());
        } else if (function==TANGENT) {
            result = Math.tan(arguments.next());
        } else if (function==ACOSINE) {
            result = Math.acos(arguments.next());
        } else if (function==ASINE) {
            result = Math.asin(arguments.next());
        } else if (function==ATAN) {
            result = Math.atan(arguments.next());
        } else if (function==MIN) {
            result = arguments.next();
            while (arguments.hasNext()) {
                result = Math.min(result, arguments.next());
            }
        } else if (function==MAX) {
            result = arguments.next();
            while (arguments.hasNext()) {
                result = Math.max(result, arguments.next());
            }
        } else if (function==SUM) {
            result = 0.;
            while (arguments.hasNext()) {
                result = result + arguments.next();
            }
        } else if (function==AVERAGE) {
            result = 0.;
            int nb = 0;
            while (arguments.hasNext()) {
                result = result + arguments.next();
                nb++;
            }
            result = result/nb;
        } else if (function==LN) {
            result = Math.log(arguments.next());
        } else if (function==LOG) {
            result = Math.log10(arguments.next());
        } else if (function==RANDOM) {
            result = Math.random();
        } else {
            result = super.evaluate(function, arguments, evaluationContext);
        }
        errIfNaN(result, function);
        return result;
    }
    
    private void errIfNaN(Double result, Function function) {
        if (result.equals(Double.NaN)) throw new IllegalArgumentException("Invalid argument passed to "+function.getName());
    }
}
