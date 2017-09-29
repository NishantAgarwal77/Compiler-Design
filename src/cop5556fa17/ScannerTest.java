/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static org.junit.Assert.*;
import jdk.nashorn.internal.objects.annotations.Where;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sun.rmi.rmid.ExecOptionPermission;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import static cop5556fa17.Scanner.Kind.*;
import static cop5556fa17.Scanner.EOFchar;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	
	@Test
	public void integerLiteralOutOfRange() throws LexicalException {
		String input = "&2839428347198237483032840";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(1,e.getPos());
			throw e;
		}
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
		
	@Test
	public void testIdentifier4() throws LexicalException {
		String input = "a+b";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_a, 0, 1, 1, 1);
		checkNext(scanner, OP_PLUS, 1, 1, 1, 2);
		checkNext(scanner, IDENTIFIER, 2, 1, 1, 3);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComments3() throws LexicalException {
		String input = "abc // asjdkf\njlsfd";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);	
		checkNext(scanner, IDENTIFIER, 14, 5, 2, 1);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSeperators() throws LexicalException {
		String input = ")([];,";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, RPAREN, 0, 1, 1, 1);
		checkNext(scanner, LPAREN, 1, 1, 1, 2);
		checkNext(scanner, LSQUARE, 2, 1, 1, 3);
		checkNext(scanner, RSQUARE, 3, 1, 1, 4);
		checkNext(scanner, SEMI, 4, 1, 1, 5);
		checkNext(scanner, COMMA, 5, 1, 1, 6);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral8() throws LexicalException {
		String input = "\"ab\\\"def";		
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(8,e.getPos());
			throw e;
		}
	}
		
	@Test
	public void test_Int_Literal() throws LexicalException {
		String input = ")9037902*6\n0";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, RPAREN, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 7, 1, 2);
		checkNext(scanner, OP_TIMES, 8, 1, 1, 9);
		checkNext(scanner, INTEGER_LITERAL, 9, 1, 1, 10);
		checkNext(scanner, INTEGER_LITERAL, 11, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOperators() throws LexicalException {
		String input = ">=<!?:==!=&<=-><-@";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_GE, 0, 2, 1, 1);
		checkNext(scanner, OP_LT, 2, 1, 1, 3);
		checkNext(scanner, OP_EXCL, 3, 1, 1, 4);
		checkNext(scanner, OP_Q, 4, 1, 1, 5);
		checkNext(scanner, OP_COLON, 5, 1, 1, 6);
		checkNext(scanner, OP_EQ, 6, 2, 1, 7);
		checkNext(scanner, OP_NEQ, 8, 2, 1, 9);
		checkNext(scanner, OP_AND, 10, 1, 1, 11);
		checkNext(scanner, OP_LE, 11, 2, 1, 12);
		checkNext(scanner, OP_RARROW, 13, 2, 1, 14);
		checkNext(scanner, OP_LARROW, 15, 2, 1, 16);
		checkNext(scanner, OP_AT, 17, 1, 1, 18);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testMiscellaneous1() throws LexicalException {
		String input = ">==";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_GE, 0, 2, 1, 1);
		checkNext(scanner, OP_ASSIGN, 2, 1, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testMiscellaneous2() throws LexicalException {
		String input = "abc\r\ndef";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);		
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, IDENTIFIER, 5, 3, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testPowerOperators() throws LexicalException {
		String input = ")**[*";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, RPAREN, 0, 1, 1, 1);
		checkNext(scanner, OP_POWER, 1, 2, 1, 2);
		checkNext(scanner, LSQUARE, 3, 1, 1, 4);			
		checkNext(scanner, OP_TIMES, 4, 1, 1, 5);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifiers() throws LexicalException {
		String input = "abc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);		
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void booleanLiteral() throws LexicalException {
		String input = "&true%false";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_AND, 0, 1, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 1, 4, 1, 2);
		checkNext(scanner, OP_MOD, 5, 1, 1, 6);
		checkNext(scanner, BOOLEAN_LITERAL, 6, 5, 1, 7);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testCommentsWithLineTerminators() throws LexicalException {
		String input = "// abc \ndef";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 8, 3, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void test_Identifier1() throws LexicalException {
		String input = "xx";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);		
		checkNextIsEOF(scanner);
	}	
	
	@Test
	public void test_Reserve_Words() throws LexicalException {
		String input = "x= a + _1    ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_x, 0, 1, 1, 1);
		checkNext(scanner, OP_ASSIGN, 1, 1, 1, 2);
		checkNext(scanner, KW_a, 3, 1, 1, 4);
		checkNext(scanner, OP_PLUS, 5, 1, 1, 6);
		checkNext(scanner, IDENTIFIER, 7, 2, 1, 8);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testAssignment() throws LexicalException {
		String input = "==;;\n=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNext(scanner, SEMI, 2, 1, 1, 3);
		checkNext(scanner, SEMI, 3, 1, 1, 4);
		checkNext(scanner, OP_ASSIGN, 5, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testJustNegative() throws LexicalException {
		String input = ";=!\n=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);		
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, OP_ASSIGN, 1, 1, 1, 2);
		checkNext(scanner, OP_EXCL, 2, 1, 1, 3);
		checkNext(scanner, OP_ASSIGN, 4, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier5() throws LexicalException {
		String input = "12$_abcde";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);				
		checkNext(scanner, INTEGER_LITERAL, 0, 2, 1, 1);
		checkNext(scanner, IDENTIFIER, 2, 7, 1, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testNegative() throws LexicalException {
		String input = ";!=\n=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);		
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, OP_NEQ, 1, 2, 1, 2);
		checkNext(scanner, OP_ASSIGN, 4, 1, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSeperatorsWithLineBreak() throws LexicalException {
		String input = ")([]\n;,";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, RPAREN, 0, 1, 1, 1);
		checkNext(scanner, LPAREN, 1, 1, 1, 2);
		checkNext(scanner, LSQUARE, 2, 1, 1, 3);
		checkNext(scanner, RSQUARE, 3, 1, 1, 4);
		checkNext(scanner, SEMI, 5, 1, 2, 1);
		checkNext(scanner, COMMA, 6, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testDivision() throws LexicalException {
		String input = "/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_DIV, 0, 1, 1, 1);		
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOnlyComments() throws LexicalException {
		String input = "//abc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOnlyCommentsWithEOL() throws LexicalException {
		String input = "//abc\n";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOnlyCommentsWithEOL1() throws LexicalException {
		String input = "//abc\n*";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_TIMES, 6, 1, 2, 1);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testBothLineTerminatorUsed() throws LexicalException {
		String input = "abc \r\n )";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		checkNext(scanner, RPAREN, 7, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral() throws LexicalException {
		String input = "\"abc\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 5, 1, 1);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral1() throws LexicalException {
		String input = "\"\\nabc\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 7, 1, 1);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral2() throws LexicalException {
		String input = "\"\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 2, 1, 1);	
		checkNextIsEOF(scanner);
	}
		
	@Test
	public void testStringLiteral3() throws LexicalException {
		String input = "\"\\\\\"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 4, 1, 1);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral4() throws LexicalException {
		String input = "x=\"\b\";";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_x, 0, 1, 1, 1);
		checkNext(scanner, OP_ASSIGN, 1, 1, 1, 2);
		checkNext(scanner, STRING_LITERAL, 2, 3, 1, 3);
		checkNext(scanner, SEMI, 5, 1, 1, 6);	
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral5() throws LexicalException {
		String input = "\"abc def ; \"";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 12, 1, 1);	
		checkNextIsEOF(scanner);
	}
		
	@Test
	public void testStringLiteral9() throws LexicalException {
		String input = "//\n";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);		
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral10() throws LexicalException {
		String input = "\"abc\\\bd\"";		
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(5,e.getPos());
			throw e;
		}
	}

	@Test
	public void testStringLiteral11() throws LexicalException {
		String input = "ssd//qwu\r\nsx";		
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);	
		checkNext(scanner, IDENTIFIER, 10, 2, 2, 1);	
		checkNextIsEOF(scanner);	
	}
	
	@Test
	public void testWhiteSpace1() throws LexicalException {
		String input = "  abcd ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 2, 4, 1, 3);		
		checkNextIsEOF(scanner);
	}

	@Test
	public void testStringLiteral6() throws LexicalException {
		String input = "\"abc \r def ; \"";		
		show(input);		
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(5,e.getPos());
			throw e;
		}
	}	
	
	@Test
	public void testStringLiteral7() throws LexicalException {
		String input = "\"abc\\g\"";		
		show(input);		
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(5,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void test_Identifier_2() throws LexicalException {
		String input = "Test\rabc";		
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 4, 1, 1);
		checkNext(scanner, IDENTIFIER, 5, 3, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testInValidInput1() throws LexicalException {
		String input = "asjk \b";		
		show(input);		
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(5,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testInValidInput2() throws LexicalException {
		String input = "asjk \\b";		
		show(input);		
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(5,e.getPos());
			throw e;
		}
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral4() throws LexicalException {
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}
}
