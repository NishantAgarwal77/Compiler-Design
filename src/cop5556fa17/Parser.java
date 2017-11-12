package cop5556fa17;

import static cop5556fa17.Scanner.Kind.EOF;
import static cop5556fa17.Scanner.Kind.IDENTIFIER;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionApp;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();				
	}

	/**
	 * Main method called by compiler to parser input. Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/**
	 * Program ::= IDENTIFIER ( Declaration SEMI | Statement SEMI )*
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			while (getFirstStatement().contains(t.kind)
					|| getFirstDeclaration().contains(t.kind)) {
				if (getFirstStatement().contains(t.kind)) {
					decsAndStatements.add(statement());
					match(Kind.SEMI);
				} else {
					decsAndStatements.add(declaration());
					match(Kind.SEMI);
				}
			}
			
			if(!t.isKind(Kind.EOF) && !(getFirstStatement().contains(t.kind)
					|| getFirstDeclaration().contains(t.kind))){
				throw new SyntaxException(t, MessageFormat.format(
						"The token {0} is invalid at line number {1} , pos {2}",
						t.kind, t.line, t.pos_in_line));
			}
			
			return new Program(firstToken, firstToken, decsAndStatements);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	Declaration declaration() throws SyntaxException {		
		Declaration dec = null;
		if (getFirstVariableDeclaration().contains(t.kind)) {
			dec = variableDeclaration();
		} else if (getFirstImageDeclaration().contains(t.kind)) {
			dec = imageDeclaration();
		} else if (getFirstSourceSinkDeclaration().contains(t.kind)) {
			dec = sourceSinkDeclaration();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return dec;
	}

	Declaration_Variable variableDeclaration() throws SyntaxException {
		Token firstToken = t;
		if (getFirstVarType().contains(t.kind)) {
			Token type = varType();
			Token name = match(Kind.IDENTIFIER);
			Expression e = null;
			if (t.isKind(Kind.OP_ASSIGN)) {
				consume();
				e = expression();
			}
			
			return new Declaration_Variable(firstToken, type, name, e);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	Token varType() throws SyntaxException {
		Token firstToken = t;
		if (getFirstVarType().contains(t.kind)) {			
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return firstToken;
	}

	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Token firstToken = t;
		if (getFirstSourceSinkType().contains(t.kind)) {
			Token type = sourceSinkType();
			Token name = match(Kind.IDENTIFIER);
			match(Kind.OP_ASSIGN);
			Source source = source();
			return new Declaration_SourceSink(firstToken, type, name, source);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
	}

	Token sourceSinkType() throws SyntaxException {
		Token firstToken = t;
		if (getFirstSourceSinkType().contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return firstToken;
	}

	Declaration_Image imageDeclaration() throws SyntaxException {
		Token firstToken = t;
		Expression xSize = null; Expression ySize = null; Source source = null; Token identName = null;		
		if (t.isKind(Kind.KW_image)) {
			consume();
			if (t.isKind(Kind.LSQUARE)) {
				consume();
				xSize = expression();
				match(Kind.COMMA);
				ySize = expression();
				match(Kind.RSQUARE);
			}
			identName = match(Kind.IDENTIFIER);
			if (t.isKind(Kind.OP_LARROW)) {
				consume();
				source = source();
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return new Declaration_Image(firstToken, xSize, ySize, identName, source);
	}

	Statement statement() throws SyntaxException {
		Token firstToken = t;
		Statement statement = null;
		if (t.isKind(Kind.IDENTIFIER)) {
			consume();
			if (t.isKind(Kind.LSQUARE)) {
				statement = restAssignmentStatement(firstToken);
			} else if (t.isKind(Kind.OP_LARROW)) {
				statement = restImageInStatement(firstToken);
			} else if (t.isKind(Kind.OP_RARROW)) {
				statement = restImageOutStatement(firstToken);
			}else if(t.isKind(Kind.OP_ASSIGN)){
				//consume();
				//expression();
				statement = restAssignmentStatement(firstToken);
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
		
		return statement;
	}

	Statement_Out imageOutStatement() throws SyntaxException {
		Statement_Out statementOut = null;
		if (t.isKind(Kind.IDENTIFIER)) {
			Token identifier = t;
			consume();
			statementOut = restImageOutStatement(identifier);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return statementOut;
	}

	Statement_Out restImageOutStatement(Token identifier) throws SyntaxException{
		match(Kind.OP_RARROW);
		Sink sink = sink();
		return new Statement_Out(identifier, identifier, sink);
	}

	Sink sink() throws SyntaxException {
		Sink sink = null;
		if (t.isKind(Kind.IDENTIFIER)) {
			sink = new Sink_Ident(t, t);
			consume();
		}else if(t.isKind(Kind.KW_SCREEN)) {
			sink = new Sink_SCREEN(t);
			consume();
		}
		else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return sink;
	}

	Source source() throws SyntaxException {
		Source src = null;
		Token firstToken = t;
		if (t.isKind(Kind.STRING_LITERAL)) {
			src = new Source_StringLiteral(firstToken, t.getText());
			consume();
		} else if (t.isKind(Kind.OP_AT)) {
			consume();
			Expression paramNum = expression();
			src = new Source_CommandLineParam(firstToken, paramNum);
		}else if(t.isKind(Kind.IDENTIFIER)){
			src = new Source_Ident(firstToken, t);
			consume();
		}else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return src;
	}

	Statement_In ImageInStatement() throws SyntaxException {
		Statement_In statementIn = null;
		if (t.isKind(Kind.IDENTIFIER)) {
			Token identifier = t;
			consume();
			statementIn = restImageInStatement(identifier);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return statementIn;
	}

	Statement_In restImageInStatement(Token identifier) throws SyntaxException{
		match(Kind.OP_LARROW);
		Source source = source();
		return new Statement_In(identifier, identifier, source);
	}

	Statement_Assign assignmentStatement() throws SyntaxException {	
		Statement_Assign stAssign = null;
		if (t.isKind(Kind.IDENTIFIER)) {
			Token identifier = t;
			consume();
			stAssign = restAssignmentStatement(identifier);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		
		return stAssign;
	}

	Statement_Assign restAssignmentStatement(Token firstToken) throws SyntaxException{
		LHS lhsObj = restLhs(firstToken);
		match(Kind.OP_ASSIGN);
		Expression exp = expression();
		return new Statement_Assign(firstToken, lhsObj, exp);
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
	public Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = null;
		if (getFirstOrExpression().contains(t.kind)) {
			e0 = orExpression();
			if (t.isKind(Kind.OP_Q)) {
				consume();
				Expression trueExpression = expression();
				match(Kind.OP_COLON);
				Expression falseExpression = expression();
				e0 = new Expression_Conditional(firstToken, e0, trueExpression, falseExpression);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		return e0;
	}

	Expression orExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;

		if (getFirstAddExpression().contains(t.kind)) {
			e0 = andExpression();
			while (t.isKind(Kind.OP_OR)) {
				Token currentOperator = t;
				consume();
				e1 = andExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression andExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;

		if (getFirstEqExpression().contains(t.kind)) {
			e0 = eqExpression();
			while (t.isKind(Kind.OP_AND)) {
				Token currentOperator = t;
				consume();
				e1 = eqExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression eqExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;

		if (getFirstRelExpression().contains(t.kind)) {
			e0 = relExpression();
			while (t.isKind(Kind.OP_EQ) || t.isKind(Kind.OP_NEQ)) {
				Token currentOperator = t;
				consume();
				e1 = relExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression relExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;

		if (getFirstAddExpression().contains(t.kind)) {
			e0 = addExpression();
			while (t.isKind(Kind.OP_LT) || t.isKind(Kind.OP_GT)
					|| t.isKind(Kind.OP_LE) || t.isKind(Kind.OP_GE)) {
				Token currentOperator = t;
				consume();
				e1 = addExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression addExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;

		if (getFirstMultExpression().contains(t.kind)) {
			e0 = multExpression();
			while (t.isKind(Kind.OP_PLUS) || t.isKind(Kind.OP_MINUS)) {
				Token currentOperator = t;
				consume();
				e1 = multExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression multExpression() throws SyntaxException {
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		if (getFirstUnaryExpression().contains(t.kind)) {
			e0 = unaryExpression();
			while (t.isKind(Kind.OP_TIMES) || t.isKind(Kind.OP_DIV)
					|| t.isKind(Kind.OP_MOD)) {
				Token currentOperator = t;
				consume();
				e1 = unaryExpression();
				e0 = new Expression_Binary(firstToken, e0, currentOperator, e1);
			}
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return e0;
	}

	Expression unaryExpression() throws SyntaxException {	
		Token firstToken = t;
		Expression expressionObj = null;
		if (t.isKind(Kind.OP_PLUS) || t.isKind(Kind.OP_MINUS)) {
			consume();
			Expression unaryExpObj = unaryExpression();
			expressionObj = new Expression_Unary(firstToken, firstToken, unaryExpObj);
		} else if (getFirstUnaryExpressionNotPlusMinus().contains(t.kind)) {
			expressionObj = unaryExpressionNotPlusMinus();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return expressionObj;
	}

	Expression unaryExpressionNotPlusMinus() throws SyntaxException {	
		Token firstToken = t;
		Expression expressionObj = null;		
		if (t.isKind(Kind.OP_EXCL)) {
			consume();
			Expression unaryExpObj = unaryExpression();
			expressionObj = new Expression_Unary(firstToken, firstToken, unaryExpObj);
		} else if (getFirstPrimary().contains(t.kind)) {
			expressionObj = primary();
		} else if (t.isKind(IDENTIFIER)) {
			expressionObj = identOrPixelSelectorExpression();
		} else if (getListKeyWordsForUnaryExpNotplusMinus().contains(t.kind)) {
			expressionObj = new Expression_PredefinedName(firstToken, t.kind);
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return expressionObj;
	}

	Expression primary() throws SyntaxException {
		Token firstToken = t;
		Expression expressionObj = null;
		if (t.isKind(Kind.INTEGER_LITERAL)) {
			expressionObj = new Expression_IntLit(firstToken, t.intVal());
			consume();
		} else if (t.isKind(Kind.LPAREN)) {
			consume();
			expressionObj = expression();
			match(Kind.RPAREN);
		} else if (getFirstFunctionApplication().contains(t.kind)) {
			expressionObj = functionApplication();
		}else if(t.isKind(Kind.BOOLEAN_LITERAL)){
			expressionObj = new Expression_BooleanLit(firstToken, Boolean.valueOf(t.getText()));
			consume();
		}
		else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return expressionObj;
	}

	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token firstToken = match(Kind.IDENTIFIER);
		Expression expression = null;
		if (t.isKind(Kind.LSQUARE)) {
			consume();
			Index selIndex = selector();
			match(Kind.RSQUARE);
			expression = new Expression_PixelSelector(firstToken, firstToken, selIndex); 
		}

		if(expression == null){
			expression = new Expression_Ident(firstToken, firstToken);
		}

		return expression;
	}

	LHS lhs() throws SyntaxException {
		Token currentToken = match(Kind.IDENTIFIER);
		return restLhs(currentToken);
	}

	LHS restLhs(Token firstToken) throws SyntaxException{		
		Index lhsSelectorIndex = null;
		if (t.isKind(Kind.LSQUARE)) {
			consume();
			lhsSelectorIndex = lhsSelector();
			match(Kind.RSQUARE);
		}

		return new LHS(firstToken, firstToken, lhsSelectorIndex);
	}

	Expression_FunctionApp functionApplication() throws SyntaxException {
		Token currentToken = t;
		Expression_FunctionApp expression_FunctionApp = null;
		Kind function = functionName();
		if (t.isKind(Kind.LPAREN)) {
			consume();
			Expression e0 = expression();
			expression_FunctionApp = new Expression_FunctionAppWithExprArg(currentToken, function, e0);
			match(Kind.RPAREN);
		} else if (t.isKind(Kind.LSQUARE)) {
			consume();
			Index selIndex = selector();
			expression_FunctionApp = new Expression_FunctionAppWithIndexArg(currentToken, function, selIndex);
			match(Kind.RSQUARE);
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}	

		return expression_FunctionApp;
	}

	Index raSelector() throws SyntaxException {
		Token firstToken = match(Kind.KW_r);
		Expression e0 = new Expression_PredefinedName(firstToken, Kind.KW_r);
		match(Kind.COMMA);
		Token secondToken = match(Kind.KW_a);
		Expression e1 = new Expression_PredefinedName(secondToken, Kind.KW_a);
		return new Index(firstToken, e0, e1);
	}

	Index xySelector() throws SyntaxException {
		Token firstToken = match(Kind.KW_x);
		Expression e0 = new Expression_PredefinedName(firstToken, Kind.KW_x);
		match(Kind.COMMA);
		Token secondToken = match(Kind.KW_y);
		Expression e1 = new Expression_PredefinedName(secondToken, Kind.KW_y);
		return new Index(firstToken, e0, e1);
	}

	Kind functionName() throws SyntaxException {
		Kind tokenKind = null;
		if (getFirstFunctionName().contains(t.kind)) {
			tokenKind = t.kind;
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		return tokenKind;
	}

	Index lhsSelector() throws SyntaxException {
		Index lhsSelector = null;
		match(Kind.LSQUARE);
		if (t.isKind(Kind.KW_x)) {
			lhsSelector = xySelector();
		} else if (t.isKind(Kind.KW_r)) {
			lhsSelector = raSelector();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}
		match(Kind.RSQUARE);
		return lhsSelector;
	}

	Index selector() throws SyntaxException {
		Token currentFirstToken = t;		
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		return new Index(currentFirstToken, e0, e1);
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

	private Token match(Kind kind) throws SyntaxException {
		Token currentToken = t;
		if (t.kind == kind) {
			consume();
		} else {
			throw new SyntaxException(t, MessageFormat.format(
					"The token {0} is invalid at line number {1} , pos {2}",
					t.kind, t.line, t.pos_in_line));
		}

		return currentToken;
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
