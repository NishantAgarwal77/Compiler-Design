package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;

import static cop5556fa17.Scanner.Kind.*;

public class TypeCheckTest {

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
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}
	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	 @Test
	 public void testDec1() throws Exception {
	 String input = "prog int k = 42;";
	 typeCheck(input);
	 }
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec2() throws Exception {
	 String input = "prog boolean k = true;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec3() throws Exception {
	 String input = "prog int v = 6; image[4, 6] nish;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec4() throws Exception {
	 String input = "prog int v = 6; int b = 3; int c = 7; image[c + b, 6] nish;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec5() throws Exception {
	 String input = "prog int v = 6; int b = 3;  image[c + b, 6] nish;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec6() throws Exception {
	 String input = "prog int v = 6; int b = 3;  image[b, 6] nish <- @3;";	 
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec44() throws Exception {
	 String input = "prog int v = 6; int b = 3;  image[b, false] nish <- @3;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec7() throws Exception {
	 String input = "prog int v = 6; int b = 3; int n = 4; image[b, 6] nish <- @5*6-7/2%9+2+n;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec8() throws Exception {
	 String input = "prog int v = 6; int b = 3; int n = 4; image[b, 6] nish <- \"nishant\";";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec9() throws Exception {
	 String input = "prog int v = 5 > 3 ? 3 + 5 : true;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec10() throws Exception {
	 String input = "prog int v = 5 > 3 ? 3 + 5 : 7;";	 
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec11() throws Exception {
	 String input = "prog boolean v = 5 > 3 ? 3 + 5 : 7;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }

	 @Test
	 public void testDec12() throws Exception {
	 String input = "prog boolean v = 5 > 3 ? true : false;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec13() throws Exception {
	 String input = "prog url abc = \"http://www.google.com\";";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec14() throws Exception {
	 String input = "prog file abc = \"book.pdf\";";
	 typeCheck(input);
	 }	 
	 
	 @Test
	 public void testDec16() throws Exception {
	 String input = "prog file def = \"book.pdf\"; file abc = def;";
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec17() throws Exception {
	 String input = "prog int def = 4; file abc = def;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 @Test
	 public void testDec18() throws Exception {
	 String input = "prog int k = false;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
	 
	 //////////////////Statement/////////////////////////////
	 
	 // StatementAssignment
	 @Test
	 public void testDec19() throws Exception {
	 String input = "prog abc[[x,y]] = 5;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	 	 
	 
	 @Test
	 public void testDec20() throws Exception {
	 String input = "prog boolean abc = true; abc[[x,y]] = 5;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	 	 
	 
	 @Test
	 public void testDec21() throws Exception {
	 String input = "prog int abc = 7; abc[[x,y]] = 5;";	 
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec22() throws Exception {
	 String input = "prog boolean abc = true; abc[[x,y]] = false;";	
	 typeCheck(input);
	 }	 	
	 
	 /// Statement_IN ///
	 
	 @Test
	 public void testDec23() throws Exception {
	 String input = "prog boolean def = true; def <- @45;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec24() throws Exception {
	 String input = "prog int def = 6; def <- @45;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec25() throws Exception {
	 String input = "prog int def = 6; def <- @true;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	 	
	 
	 //// statement Out ////////////////
	 
	 @Test
	 public void testDec26() throws Exception {
	 String input = "prog int def = 5; def -> SCREEN;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec27() throws Exception {
	 String input = "prog boolean def = true; def -> SCREEN;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec28() throws Exception {
	 String input = "prog image[2,3]def; def -> SCREEN;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec29() throws Exception {
	 String input = "prog image[2,3]def; file abc = \"book.pdf\"; def -> abc;";	
	 typeCheck(input);
	 }	 	
	 
	 /// Expression Binary////////////
	 
	 @Test
	 public void testDec30() throws Exception {
	 String input = "prog boolean abc = 2 + 5 == 4+7;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec31() throws Exception {
	 String input = "prog boolean abc = 2 + 5 >= 4+7;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec32() throws Exception {
	 String input = "prog boolean abc = 2 + 5 < 4+7;";	
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec33() throws Exception {
	 String input = "prog int abc = 8 & 4+7;";	 
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec34() throws Exception {
	 String input = "prog boolean abc = 8 > 5 | 4 < 7;";	 
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec35() throws Exception {
	 String input = "prog boolean abc = 8 > 5 & 4 < 7;";	 
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec36() throws Exception {
	 String input = "prog int abc = 8%5*2-7+6/2;";	 
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec37() throws Exception {
	 String input = "prog boolean abc = false;";	 
	 typeCheck(input);
	 }	 	
	 
	 ////Expression_FunctionAppWithExprArg ////
	 
	 @Test
	 public void testDec38() throws Exception {
	 String input = "prog int abc = sin(90);";	 
	 typeCheck(input);
	 String input2 = "prog int abc = cos(90);";	 
	 typeCheck(input2);
	 String input3 = "prog int abc = atan(90);";	 
	 typeCheck(input3);
	 String input4 = "prog int abc = abs(-90);";	 
	 typeCheck(input4);
	 String input5 = "prog int abc = cart_x(90);";	 
	 typeCheck(input5);
	 String input6 = "prog int abc = polar_a(90);";	 
	 typeCheck(input6);
	 }
	 
	 @Test
	 public void testDec39() throws Exception {
	 String input = "prog int abc = polar_a(true);";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }	 	
	 
	 @Test
	 public void testDec40() throws Exception {
	 String input = "prog int abc = polar_a[a > 2 ? 3 : 4,x];";	 
	 typeCheck(input);
	 
	 String input2 = "prog int abc = polar_a[!r,x];";	 
	 typeCheck(input2);
	 
	 String input3 = "prog boolean v = 6; int abc = polar_a[!v,x];";
	 thrown.expect(SemanticException.class);
	 typeCheck(input3);
	 }	 	
	 
	 @Test
	 public void testDec41() throws Exception {
		 
	 String input2 = "prog int abc = false;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input2);	 
	 	
	 }	 		 
	 
	 @Test
	 public void testDec42() throws Exception {		 
		 String input4 = "prog boolean v = true; boolean abc = v;";
		 typeCheck(input4);
	 }
	 
	 @Test
	 public void testDec43() throws Exception {
		 String input3 = "prog boolean v = true; int abc = v;";	
		 thrown.expect(SemanticException.class);
		 typeCheck(input3);	  
	 }
	 
	 @Test
	 public void testDec45() throws Exception {
		 String input3 = "prog image[4, 6] nish; int abc = nish[3,5];";			 
		 typeCheck(input3);	  
	 }
	 
	 @Test
	 public void testDec46() throws Exception {
		 String input3 = "prog int nish = 4; int def = nish[3,5];";
		 thrown.expect(SemanticException.class);
		 typeCheck(input3);	  
	 }
	 
	 @Test
	 public void testDec47() throws Exception {
		 String input3 = "prog int nish = !+-6;";		 
		 typeCheck(input3);	 
		 
		 String input4 = "prog int nish = !+----!x;";		 
		 typeCheck(input4);	 
		 
		 String input5 = "prog int nish = !+----!(6783+ 7382);";		 
		 typeCheck(input5);	
		 
		 String input6 = "prog int nish = !sin(2);";		 
		 typeCheck(input6);	
		 
		 String input7 = "prog boolean nish = !true;";		 
		 typeCheck(input7);	
		 
		 String input8 = "prog int nish = !true;";
		 thrown.expect(SemanticException.class);
		 typeCheck(input8);	
	 }		
	 
	 @Test
	 public void testDec48() throws Exception {
		 String input5 = "prog int nish = !+----!(6783+ 7382);";		 
		 typeCheck(input5);	 
	 }		
	 
	 @Test
	 public void testDec49() throws Exception {
		 String input5 = "prog int abc; abc[[x,y]] = 3;";		 
		 typeCheck(input5);	 
	 }		
}
