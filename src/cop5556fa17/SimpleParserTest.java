package cop5556fa17;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Parser.SyntaxException;

public class SimpleParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	/**
	 * Simple test case with an empty program. This test expects an
	 * SyntaxException because all legal programs must have at least an
	 * identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. This is not legal
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		thrown.expect(SyntaxException.class);
		try {
			parser.parse(); // Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Another example. This is a legal program and should pass when your parser
	 * is implemented.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "pay oi <- @A|abg[true, false] ;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); //
		parser.parse();
	}
	
	@Test
	public void testDec45() throws LexicalException, SyntaxException {
		String input = "3*a/x%-Z";
		show(input);
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); //
		parser.expression();
	}
	
	@Test
	public void testDec45sd() throws LexicalException, SyntaxException {
		String input = "polar_r((5) | true))";
		show(input);
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); //
		parser.expression();
	}
	
	@Test
	public void testDec2() throws LexicalException, SyntaxException {
		String input = "\"jsdkfhk\"123\"456\"";
		show(input);
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); //
		thrown.expect(SyntaxException.class);
		try {
			parser.parse(); // Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testDec34() throws LexicalException, SyntaxException {
		String input = "abdz = something;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); //
		thrown.expect(SyntaxException.class);
		try {
			parser.parse(); // Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void source() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("abcdf", "\"ksdfjhk\"", "@(a+v)");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.source(); // Call expression directly.
		}		
	}
	
	@Test
	public void program() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("int nishant", "int nishant = skdjs", "url sbdbsn = \"sdhs\"", "image abcdf","image [x,(hg / hhg)]abcdf", "image [x,(hg / hhg)]abcdf <-@d", "abcsd -> screen");
		for(String name : names){
			name = "abdcd " + name + ";";
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.program(); // Call expression directly.
		}		
	}
	
	@Test
	public void declaration() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("int nishant", "int nishant = skdjs", "url sbdbsn = \"sdhs\"", "image abcdf","image [x,(hg / hhg)]abcdf", "image [x,(hg / hhg)]abcdf <-@d");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.declaration(); // Call expression directly.
		}		
	}
	
	@Test
	public void variableDeclaration() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("int nishant", "int nishant = skdjs");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.variableDeclaration(); // Call expression directly.
		}		
	}
	
	@Test
	public void sourceSinkDeclaration() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("url sbdbsn = \"sdhs\"");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.sourceSinkDeclaration(); // Call expression directly.
		}		
	}
	
	@Test
	public void image_declaration() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("image abcdf","image [x,(hg / hhg)]abcdf", "image [x,(hg / hhg)]abcdf <-@d");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.imageDeclaration(); // Call expression directly.
		}		
	}

	/**
	 * This example invokes the method for expression directly. Effectively, we
	 * are viewing Expression as the start symbol of a sub-language.
	 * 
	 * Although a compiler will always call the parse() method, invoking others
	 * is useful to support incremental development. We will only invoke
	 * expression directly, but following this example with others is
	 * recommended.
	 * 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void expression1() throws SyntaxException, LexicalException {
		String input = "++++x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression(); // Call expression directly.
	}
	
	@Test
	public void statement() throws SyntaxException, LexicalException {
		String input = "abcsd -> screen";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.statement();
	}
	
	@Test
	public void statement2() throws SyntaxException, LexicalException {
		String input = "abc <- xy";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.statement();
	}
	
	@Test
	public void statement3() throws SyntaxException, LexicalException {
		String input = "abc = 3";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.statement();
	}
	
	@Test
	public void ImageOutStatement() throws SyntaxException, LexicalException {
		String input = "abcsd -> screen";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.imageOutStatement();
	}
	
	@Test
	public void sink() throws SyntaxException, LexicalException {
		String input = "screen";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.sink(); // Call expression directly.
	}
	
	@Test
	public void ImageInStatement() throws SyntaxException, LexicalException {
		String input = "abc <- xy";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.ImageInStatement(); // Call expression directly.
	}
	
	
	@Test
	public void assignmentStatement() throws SyntaxException, LexicalException {
		String input = "abc = 3";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.assignmentStatement(); // Call expression directly.
	}
	
	@Test
	public void assignmentStatement2() throws SyntaxException, LexicalException {
		String input = "abc[[x,y]] = 3";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.assignmentStatement(); // Call expression directly.
	}
	
	
	@Test
	public void IdentOrPixelSelectorExpression() throws SyntaxException, LexicalException {
		String input = "jksd";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.identOrPixelSelectorExpression(); // Call expression directly.
	}
	
	@Test
	public void unaryExpressionNotPlusMinus1() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("x", "y", "r", "a", "X", "Y", "Z", "A", "R", "DEF_X", "DEF_Y");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.unaryExpressionNotPlusMinus(); // Call expression directly.
		}		
	}
	
	@Test
	public void unaryExpressionNotPlusMinus2() throws SyntaxException, LexicalException {
		String input = "jksd [2,2]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpressionNotPlusMinus(); // Call expression directly.
	}
	
	@Test
	public void unaryExpressionNotPlusMinus3() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("true", "false", "2", "3", "(skad)", "sin(x)", "!R", "!+A", "!- jksd [2,2]");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.unaryExpressionNotPlusMinus(); // Call expression directly.
		}		
	}
	
	@Test
	public void unaryExpressionNotPlusMinus4() throws SyntaxException, LexicalException {
		String input = "!x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpressionNotPlusMinus(); // Call expression directly.
	}
	
	@Test
	public void primary1() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("true", "false", "2", "3", "(skad)", "sin(x)");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.primary(); // Call expression directly.
		}		
	}
	
	@Test
	public void expression234() throws SyntaxException, LexicalException {
		String input =  "x*X + y*Y < x == R & x*X + y*Y < x == R | x*X + y*Y < x == R & x*X + y*Y < x == R";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression(); // Call expression directly.
	}
	
	@Test
	public void expression2344() throws SyntaxException, LexicalException {
		String input =  "1 + 2";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression(); // Call expression directly.
	}
	
	
	@Test
	public void expression3we() throws SyntaxException, LexicalException {
		String input =  "polar_a(s + f)";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression(); // Call expression directly.
	}
	
	@Test
	public void expression43() throws SyntaxException, LexicalException {
		String input =  "polar_a(s + f) ? (a+b) : (c+ d)";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.expression(); // Call expression directly.
	}
	
	@Test
	public void selector() throws SyntaxException, LexicalException {
		String input =  "cart_y(d),(s + d)";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.selector(); // Call expression directly.
	}
	
	@Test
	public void orExpression() throws SyntaxException, LexicalException {
		String input =  "x*X + y*Y < x == R & x*X + y*Y < x == R | x*X + y*Y < x == R & x*X + y*Y < x == R";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.orExpression(); // Call expression directly.
	}
		
	@Test
	public void andExpression() throws SyntaxException, LexicalException {
		String input =  "x*X + y*Y < x == R & x*X + y*Y < x == R";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.andExpression(); // Call expression directly.
	}
	
	@Test
	public void eqExpression() throws SyntaxException, LexicalException {
		String input =  "x*X + y*Y < x == R";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.eqExpression(); // Call expression directly.
	}
	
	@Test
	public void relExpression() throws SyntaxException, LexicalException {
		String input = "+Z*+true/ 2%sin(a + b)++Z*+true/ 2%sin(a + b)>+Z*+true/ 2%sin(a + b)++Z*+true/ 2%sin(a + b)<=+Z*+true/ 2%sin(a + b)++Z*+true/ 2%sin(a + b)";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.relExpression(); // Call expression directly.
	}
	
	@Test
	public void addExpression() throws SyntaxException, LexicalException {
		String input = "+Z*+true/ 267%sin(a + b)++Z*+true*2%sin(a + b)";
		//String input = "+Z*+true++Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.addExpression(); // Call expression directly.
	}
	
	@Test
	public void multExpression() throws SyntaxException, LexicalException {
		String input = "+Z*+true";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression(); // Call expression directly.
	}
	
	@Test
	public void multExpression2() throws SyntaxException, LexicalException {
		String input = "+Z*+true/2%sin(a + b)";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression(); // Call expression directly.
	}
		
	@Test
	public void unaryExpression() throws SyntaxException, LexicalException {
		String input = "- jksd [2,2]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression(); // Call expression directly.
	}
	
	@Test
	public void unaryExpression2() throws SyntaxException, LexicalException {
		String input = "--+-+-+!x";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.unaryExpression(); // Call expression directly.
	}
	
	@Test
	public void IdentOrPixelSelectorExpression2() throws SyntaxException, LexicalException {
		String input = "jksd [2,2]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.identOrPixelSelectorExpression(); // Call expression directly.
	}
		
	@Test
	public void lhsSelector23() throws SyntaxException, LexicalException {
		String input = "[x , y]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.lhsSelector(); // Call expression directly.
	}
	
	@Test
	public void lhsSelector2() throws SyntaxException, LexicalException {
		String input = "[r , A]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.lhsSelector(); // Call expression directly.
	}
	
	@Test
	public void lhs() throws SyntaxException, LexicalException {
		String input = "abcd";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.lhs(); // Call expression directly.
	}
	
	@Test
	public void lhs2() throws SyntaxException, LexicalException {
		String input = "abcd  [[r , A]]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.lhs(); // Call expression directly.
	}
	
	@Test
	public void lhs_fail() throws SyntaxException, LexicalException {
		String input = "abcd  [[r ,]]";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try{
			parser.lhs();
		}catch(SyntaxException se){
			throw se;
		}	
	}
	
	@Test
	public void lhsSelector_fail() throws SyntaxException, LexicalException {
		String input = "[r , A";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try{
			parser.lhsSelector();
		}catch(SyntaxException se){
			throw se;
		}	
	}
	
	@Test
	public void functionName234234() throws SyntaxException, LexicalException {
		List<String> names = Arrays.asList("sin", "cos", "atan", "abs", "cart_x", "cart_y", "polar_a", "polar_r");
		for(String name : names){
			show(name);
			Scanner scanner = new Scanner(name).scan();
			show(scanner);
			Parser parser = new Parser(scanner);
			parser.functionName(); // Call expression directly.
		}		
	}
	
	@Test
	public void xySelector24323() throws SyntaxException, LexicalException {
		String input = "x , y";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.xySelector(); // Call expression directly.
	}
	
	@Test
	public void xySelector_fail() throws SyntaxException, LexicalException {
		String input = "x ,";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);		
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try{
			parser.xySelector();
		}catch(SyntaxException se){
			throw se;
		}	
	}
	
	@Test
	public void raSelector() throws SyntaxException, LexicalException {
		String input = "r , A";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		Parser parser = new Parser(scanner);
		parser.raSelector(); // Call expression directly.
	}
	
	@Test
	public void raSelector_fail() throws SyntaxException, LexicalException {
		String input = ", A";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);		
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try{
			parser.raSelector();
		}catch(SyntaxException se){
			throw se;
		}	
	}
}
