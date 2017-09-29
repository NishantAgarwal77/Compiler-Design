package cop5556fa17;

import static cop5556fa17.Scanner.Kind.EOF;
import static cop5556fa17.Scanner.Kind.IDENTIFIER;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;	
	
	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();				
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			while (getFirstStatement().contains(t.kind)
					|| getFirstDeclaration().contains(t.kind)) {
				if (getFirstStatement().contains(t.kind)) {
					statement();
					match(Kind.SEMI);
				} else {
					declaration();
					match(Kind.SEMI);
				}
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void declaration() throws SyntaxException {
		if (getFirstVariableDeclaration().contains(t.kind)) {
			variableDeclaration();
		} else if (getFirstImageDeclaration().contains(t.kind)) {
			imageDeclaration();
		} else if (getFirstSourceSinkDeclaration().contains(t.kind)) {
			sourceSinkDeclaration();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void variableDeclaration() throws SyntaxException {
		if (getFirstVarType().contains(t.kind)) {
			varType();
			match(Kind.IDENTIFIER);
			if (t.isKind(Kind.OP_ASSIGN)) {
				consume();
				expression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void varType() throws SyntaxException {
		if (getFirstVarType().contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void sourceSinkDeclaration() throws SyntaxException {
		if (getFirstSourceSinkType().contains(t.kind)) {
			sourceSinkType();
			match(Kind.IDENTIFIER);
			match(Kind.OP_ASSIGN);
			source();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void sourceSinkType() throws SyntaxException {
		if (getFirstSourceSinkType().contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void imageDeclaration() throws SyntaxException {
		if (t.isKind(Kind.KW_image)) {
			consume();
			if (t.isKind(Kind.LSQUARE)) {
				consume();
				expression();
				match(Kind.COMMA);
				expression();
				match(Kind.RSQUARE);
			}
			match(Kind.IDENTIFIER);
			if (t.isKind(Kind.OP_LARROW)) {
				consume();
				source();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void statement() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			if (t.isKind(Kind.LSQUARE)) {
				restAssignmentStatement();
			} else if (t.isKind(Kind.OP_LARROW)) {
				restImageInStatement();
			} else if (t.isKind(Kind.OP_RARROW)) {
				restImageOutStatement();
			}else if(t.isKind(Kind.OP_ASSIGN)){
				consume();
				expression();
			}else {
				throw new SyntaxException(
						t,
						MessageFormat
								.format("The token {0} is invalid at line number {1} , pos {2}",
										t.kind, t.line, t.pos_in_line));
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void imageOutStatement() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			restImageOutStatement();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}
	
	void restImageOutStatement() throws SyntaxException{
		match(Kind.OP_RARROW);
		sink();
	}

	void sink() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER) || t.isKind(Kind.KW_SCREEN)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void source() throws SyntaxException {
		if (t.isKind(Kind.STRING_LITERAL) || t.isKind(Kind.IDENTIFIER)) {
			consume();
		} else if (t.isKind(Kind.OP_AT)) {
			consume();
			expression();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void ImageInStatement() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			restImageInStatement();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}
	
	void restImageInStatement() throws SyntaxException{
		match(Kind.OP_LARROW);
		source();
	}

	void assignmentStatement() throws SyntaxException {
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			restAssignmentStatement();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}
	
	void restAssignmentStatement() throws SyntaxException{
		restLhs();
		match(Kind.OP_ASSIGN);
		expression();
	}

	/**
	 * Expression ::= OrExpression OP_Q Expression OP_COLON Expression |
	 * OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental
	 * development.
	 * 
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {
		if (getFirstOrExpression().contains(t.kind)) {
			orExpression();
			if (t.isKind(Kind.OP_Q)) {
				consume();
				expression();
				match(Kind.OP_COLON);
				expression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void orExpression() throws SyntaxException {
		if (getFirstAddExpression().contains(t.kind)) {
			andExpression();
			while (t.isKind(Kind.OP_OR)) {
				consume();
				andExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void andExpression() throws SyntaxException {
		if (getFirstEqExpression().contains(t.kind)) {
			eqExpression();
			while (t.isKind(Kind.OP_AND)) {
				consume();
				eqExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void eqExpression() throws SyntaxException {
		if (getFirstRelExpression().contains(t.kind)) {
			relExpression();
			while (t.isKind(Kind.OP_EQ) || t.isKind(Kind.OP_NEQ)) {
				consume();
				relExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void relExpression() throws SyntaxException {
		if (getFirstAddExpression().contains(t.kind)) {
			addExpression();
			while (t.isKind(Kind.OP_LT) || t.isKind(Kind.OP_GT)
					|| t.isKind(Kind.OP_LE) || t.isKind(Kind.OP_GE)) {
				consume();
				addExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void addExpression() throws SyntaxException {
		if (getFirstMultExpression().contains(t.kind)) {
			multExpression();
			while (t.isKind(Kind.OP_PLUS) || t.isKind(Kind.OP_MINUS)) {
				consume();
				multExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void multExpression() throws SyntaxException {
		if (getFirstUnaryExpression().contains(t.kind)) {
			unaryExpression();
			while (t.isKind(Kind.OP_TIMES) || t.isKind(Kind.OP_DIV)
					|| t.isKind(Kind.OP_MOD)) {
				consume();
				unaryExpression();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void unaryExpression() throws SyntaxException {
		if (t.isKind(Kind.OP_PLUS) || t.isKind(Kind.OP_MINUS)) {
			consume();
			unaryExpression();
		} else if (getFirstUnaryExpressionNotPlusMinus().contains(t.kind)) {
			unaryExpressionNotPlusMinus();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void unaryExpressionNotPlusMinus() throws SyntaxException {	
		if (t.isKind(Kind.OP_EXCL)) {
			consume();
			unaryExpression();
		} else if (getFirstPrimary().contains(t.kind)) {
			primary();
		} else if (t.isKind(IDENTIFIER)) {
			identOrPixelSelectorExpression();
		} else if (getListKeyWordsForUnaryExpNotplusMinus().contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void primary() throws SyntaxException {
		if (t.isKind(Kind.INTEGER_LITERAL) || t.isKind(Kind.BOOLEAN_LITERAL)) {
			consume();
		} else if (t.isKind(Kind.LPAREN)) {
			consume();
			expression();
			match(Kind.RPAREN);
		} else if (getFirstFunctionApplication().contains(t.kind)) {
			functionApplication();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void identOrPixelSelectorExpression() throws SyntaxException {
		match(Kind.IDENTIFIER);
		if (t.isKind(Kind.LSQUARE)) {
			consume();
			selector();
			match(Kind.RSQUARE);
		}
	}

	void lhs() throws SyntaxException {
		match(Kind.IDENTIFIER);
		restLhs();
	}
	
	void restLhs() throws SyntaxException{
		if (t.isKind(Kind.LSQUARE)) {
			consume();
			lhsSelector();
			match(Kind.RSQUARE);
		}
	}

	void functionApplication() throws SyntaxException {
		functionName();
		if (t.isKind(Kind.LPAREN)) {
			consume();
			expression();
			match(Kind.RPAREN);
		} else if (t.isKind(Kind.LSQUARE)) {
			consume();
			selector();
			match(Kind.RSQUARE);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}	
	}
	
	void raSelector() throws SyntaxException {
		match(Kind.KW_r);
		match(Kind.COMMA);
		match(Kind.KW_A);
	}

	void xySelector() throws SyntaxException {
		match(Kind.KW_x);
		match(Kind.COMMA);
		match(Kind.KW_y);
	}

	void functionName() throws SyntaxException {
		if (getFirstFunctionName().contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	void lhsSelector() throws SyntaxException {
		match(Kind.LSQUARE);
		if (t.isKind(Kind.KW_x)) {
			xySelector();
		} else if (t.isKind(Kind.KW_r)) {
			raSelector();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		match(Kind.RSQUARE);
	}

	void selector() throws SyntaxException {
		expression();
		match(Kind.COMMA);
		expression();
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to
	 * get nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message = "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}

	private void match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	private List<Kind> getFirstUnaryExpression() {
		List<Kind> firstSet = new ArrayList<Scanner.Kind>();
		firstSet.add(Kind.OP_PLUS);
		firstSet.add(Kind.OP_MINUS);
		firstSet.addAll(getFirstUnaryExpressionNotPlusMinus());
		return firstSet;
	}

	private List<Kind> getFirstUnaryExpressionNotPlusMinus() {
		List<Kind> firstSet = new ArrayList<Scanner.Kind>();
		firstSet.add(Kind.OP_EXCL);
		firstSet.add(Kind.IDENTIFIER);
		firstSet.addAll(getFirstPrimary());
		firstSet.addAll(getListKeyWordsForUnaryExpNotplusMinus());
		return firstSet;
	}
	
	private List<Kind> getListKeyWordsForUnaryExpNotplusMinus() {
		return Arrays.asList(Kind.KW_x, Kind.KW_y, Kind.KW_r,
				Kind.KW_a, Kind.KW_X, Kind.KW_Y, Kind.KW_Z, Kind.KW_A,
				Kind.KW_R, Kind.KW_DEF_X, Kind.KW_DEF_Y);
	}

	private List<Kind> getFirstPrimary() {
		List<Kind> firstSet = new ArrayList<Scanner.Kind>();
		firstSet.add(Kind.INTEGER_LITERAL);
		firstSet.add(Kind.BOOLEAN_LITERAL);
		firstSet.add(Kind.LPAREN);
		firstSet.addAll(getFirstFunctionApplication());
		return firstSet;
	}

	private List<Kind> getFirstFunctionName() {
		return Arrays.asList(Kind.KW_sin, Kind.KW_cos, Kind.KW_atan,
				Kind.KW_abs, Kind.KW_cart_x, Kind.KW_cart_y, Kind.KW_polar_a,
				Kind.KW_polar_r);
	}

	private List<Kind> getFirstFunctionApplication() {
		return getFirstFunctionName();
	}

	private List<Kind> getFirstMultExpression() {
		return getFirstUnaryExpression();
	}

	private List<Kind> getFirstAddExpression() {
		return getFirstMultExpression();
	}

	private List<Kind> getFirstRelExpression() {
		return getFirstAddExpression();
	}

	private List<Kind> getFirstEqExpression() {
		return getFirstRelExpression();
	}

	private List<Kind> getFirstAndExpression() {
		return getFirstEqExpression();
	}

	private List<Kind> getFirstOrExpression() {
		return getFirstAndExpression();
	}

	private List<Kind> getFirstSourceSinkType() {
		return Arrays.asList(Kind.KW_url, Kind.KW_file);
	}

	private List<Kind> getFirstSourceSinkDeclaration() {
		return getFirstSourceSinkType();
	}

	private List<Kind> getFirstVarType() {
		return Arrays.asList(Kind.KW_int, Kind.KW_boolean);
	}

	private List<Kind> getFirstVariableDeclaration() {
		return getFirstVarType();
	}

	private List<Kind> getFirstImageDeclaration() {
		return Arrays.asList(Kind.KW_image);
	}

	private List<Kind> getFirstStatement() {
		return Arrays.asList(Kind.IDENTIFIER);
	}
		
	private List<Kind> getFirstDeclaration() {
		List<Kind> firstSet = new ArrayList<Scanner.Kind>();
		firstSet.addAll(getFirstSourceSinkDeclaration());
		firstSet.addAll(getFirstVariableDeclaration());
		firstSet.addAll(getFirstImageDeclaration());
		return firstSet;
	}

	private Token consume() throws SyntaxException {
		Token prev = t;
		t = scanner.nextToken();
		return prev;
	}
}
