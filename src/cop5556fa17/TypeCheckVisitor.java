package cop5556fa17;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
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
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
		
		private SymbolTable symbolTable; 

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}	
		
		public TypeCheckVisitor(){
			symbolTable = SymbolTable.getSymbolTableInstance();
		}
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		
		if(declaration_Variable != null){
			Declaration node = this.symbolTable.lookup(declaration_Variable.name);
			if(node != null){
				throw new SemanticException(declaration_Variable.firstToken, "Identifier " + node.firstToken.getText() + "has already been declared");
			}
						
			declaration_Variable.setType(TypeUtils.getType(declaration_Variable.type));
			
			if(declaration_Variable.e != null){
				declaration_Variable.e.visit(this, null);
				if(declaration_Variable.e.getType() != declaration_Variable.getType()){
					throw new SemanticException(declaration_Variable.firstToken, "Expected type " + declaration_Variable.getType() + " but found " + declaration_Variable.e.getType());
				}
			}
			
			this.symbolTable.insert(declaration_Variable.name, declaration_Variable);
		}
		
		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		
		if(expression_Binary != null){
			
			if(expression_Binary.e0 != null){
				expression_Binary.e0.visit(this, null);
			}
			if(expression_Binary.e1 != null){
				expression_Binary.e1.visit(this, null);
			}			
			Kind operator = expression_Binary.op;
			
			if(operator == Kind.OP_EQ || operator == Kind.OP_NEQ){
				expression_Binary.setType(Type.BOOLEAN);
			}else if((operator == Kind.OP_GE || operator == Kind.OP_GT || operator == Kind.OP_LT || operator == Kind.OP_LE) && expression_Binary.e0 != null && expression_Binary.e0.getType() == Type.INTEGER){
				expression_Binary.setType(Type.BOOLEAN);
			}else if((operator == Kind.OP_AND || operator == Kind.OP_OR) && expression_Binary.e0 != null && (expression_Binary.e0.getType() == Type.INTEGER || expression_Binary.e0.getType() == Type.BOOLEAN)){
				expression_Binary.setType(expression_Binary.e0.getType());				
			}else if((operator == Kind.OP_DIV || operator == Kind.OP_MINUS || operator == Kind.OP_MOD || operator == Kind.OP_PLUS || operator == Kind.OP_POWER || operator == Kind.OP_TIMES) &&
					expression_Binary.e0 != null && expression_Binary.e0.getType() == Type.INTEGER){
				expression_Binary.setType(Type.INTEGER);
			}else{
				expression_Binary.setType(null);
			}
						
			if(!(expression_Binary.e0.getType() == expression_Binary.e1.getType() && expression_Binary.getType() != null)){
				throw new SemanticException(expression_Binary.firstToken, "Type check failed in expression_binary");
			}
		}
		
		return expression_Binary;
		
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		if(expression_Unary != null){
			Type expType = null;
			if(expression_Unary.e != null){
				expression_Unary.e.visit(this, null);
				expType = expression_Unary.e.getType();
			}
						
			Kind op = expression_Unary.op;
			
			if(op == Kind.OP_EXCL && (expType == Type.BOOLEAN || expType == Type.INTEGER)){
				expression_Unary.setType(expType);
			}else if((op == Kind.OP_PLUS || op == Kind.OP_MINUS) && expType == Type.INTEGER){
				expression_Unary.setType(Type.INTEGER);
			}else{
				expression_Unary.setType(null);
			}
			
			if(expression_Unary.getType() == null){
				throw new SemanticException(expression_Unary.firstToken, "Expression Unary has a null type");
			}			
		}
		
		return expression_Unary;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if(index != null){
			if(index.e0 != null){
				index.e0.visit(this, null);
			}
			if(index.e1 != null){
				index.e1.visit(this, null);
			}
			
			if(!(index.e0.getType() == Type.INTEGER && index.e1.getType() == Type.INTEGER)){
				throw new SemanticException(index.firstToken, MessageFormat.format("Expression0 type: {0} mismatch Expression1 Type: {1}",index.e0.getType(), index.e1.getType()));				
			}						
			
			Kind exp0Type = index.e0.firstToken.kind;			
			Kind exp1Type = index.e1.firstToken.kind;
			
			index.setCartesian(!(exp0Type == Kind.KW_r && exp1Type == Kind.KW_a));	
		}
		
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		
		if(expression_PixelSelector != null){
			Declaration node = this.symbolTable.lookup(expression_PixelSelector.name);
			if(node != null){
				if(expression_PixelSelector.index != null){
					expression_PixelSelector.index.visit(this, null);
				}
				if(node.getType() == Type.IMAGE){
					expression_PixelSelector.setType(Type.INTEGER);
				}else if(expression_PixelSelector.index == null){
					expression_PixelSelector.setType(node.getType());
				}else{
					expression_PixelSelector.setType(null);
				}
				
				if(expression_PixelSelector.getType() == null){
					throw new SemanticException(expression_PixelSelector.firstToken, "Expression Pixel selector type is null");
				}
			}else{
				throw new SemanticException(expression_PixelSelector.firstToken, "Variable " + expression_PixelSelector.name +" must be declared before use");
			}
		}
		
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		if(expression_Conditional != null && expression_Conditional.condition != null && expression_Conditional.trueExpression != null && expression_Conditional.falseExpression != null){
			expression_Conditional.condition.visit(this, null);
			expression_Conditional.trueExpression.visit(this, null);
			expression_Conditional.falseExpression.visit(this, null);
			
			if(!(expression_Conditional.condition.getType() == Type.BOOLEAN && expression_Conditional.trueExpression.getType() == expression_Conditional.falseExpression.getType())){
				throw new SemanticException(expression_Conditional.firstToken, "Expression condition type mismatch");
			}						
			
			expression_Conditional.setType(expression_Conditional.trueExpression.getType());
		}
		
		return expression_Conditional;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(declaration_Image != null){
			Declaration node = this.symbolTable.lookup(declaration_Image.name);
			if(node != null){
				throw new SemanticException(declaration_Image.firstToken, "Identifier has already been declared");
			}
			
			if(declaration_Image.source != null){
				declaration_Image.source.visit(this, null);
			}			
			
			if(declaration_Image.xSize != null){
				declaration_Image.xSize.visit(this, null);
			}
			if(declaration_Image.ySize != null){
				declaration_Image.ySize.visit(this, null);
			}			
			
			declaration_Image.setType(Type.IMAGE);
			this.symbolTable.insert(declaration_Image.name, declaration_Image);
			
			if(declaration_Image.xSize != null){
				if(declaration_Image.ySize == null){
					throw new SemanticException(declaration_Image.firstToken, "Ysize is null where as XSize contains values");
				}
				
				if(!(declaration_Image.xSize.getType() == Type.INTEGER && declaration_Image.ySize.getType() == Type.INTEGER)){
					throw new SemanticException(declaration_Image.firstToken, MessageFormat.format("XSize type: {0} when expected INTEGER; Ysize type :{1} when expected INTEGER", declaration_Image.xSize.getType(), declaration_Image.ySize.getType()));
				}
			}
		}			
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		
		if(source_StringLiteral != null){
			String fileOrUrl = source_StringLiteral.fileOrUrl;
			try {
				new URL(fileOrUrl);
				source_StringLiteral.setType(Type.URL);
			} catch (MalformedURLException e) {
				source_StringLiteral.setType(Type.FILE);
			}
		}	
		
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
	
		if(source_CommandLineParam != null){
			Expression paramNum = source_CommandLineParam.paramNum;
			if(paramNum != null){
				paramNum.visit(this, null);
				if(!paramNum.isType(Type.INTEGER)){
					throw new SemanticException(source_CommandLineParam.firstToken, "Source CommandLineParam Type:  expected INTEGER found " + source_CommandLineParam.getType());
				}
			}		
								
			source_CommandLineParam.setType(Type.NONE);
		}		
		
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		if(source_Ident != null){
			String sourceIdentName = source_Ident.name;
			Declaration node = this.symbolTable.lookup(sourceIdentName);
			if(node != null){
				source_Ident.setType(node.getType());
				if(!(source_Ident.isType(Type.FILE) || source_Ident.isType(Type.URL))){
					throw new SemanticException(source_Ident.firstToken, "Type of \"" + sourceIdentName + "\" should be FILE or URL");
				}	
			}else{
				throw new SemanticException(source_Ident.firstToken, "Identifier \"" + sourceIdentName + "\" must be declared before use");
			}		
		}		
		return source_Ident; 
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		
		if(declaration_SourceSink != null){
			Declaration node = this.symbolTable.lookup(declaration_SourceSink.name);
			if(node != null){
				throw new SemanticException(declaration_SourceSink.firstToken, "Identifier has already been declared");
			}
			
			if(declaration_SourceSink.source != null){
				declaration_SourceSink.source.visit(this, null);
			}					
						
			declaration_SourceSink.setType(TypeUtils.getType(declaration_SourceSink.firstToken));
			
			if((declaration_SourceSink.source.getType() == declaration_SourceSink.getType()) || (declaration_SourceSink.source.getType() == Type.NONE)){
				this.symbolTable.insert(declaration_SourceSink.name, declaration_SourceSink);
			}
			else {
				throw new SemanticException(declaration_SourceSink.firstToken, MessageFormat.format("Source type :{0} does not match with declaration sourceSink type {1}", declaration_SourceSink.source.getType(),declaration_SourceSink.getType()));
			}						
		}
		
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		if(expression_IntLit != null){
			expression_IntLit.setType(Type.INTEGER);
		}
		
		return expression_IntLit;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		
		if(expression_FunctionAppWithExprArg != null){
			
			if(expression_FunctionAppWithExprArg.arg != null){
				expression_FunctionAppWithExprArg.arg.visit(this, null);
				
				if(expression_FunctionAppWithExprArg.arg.getType() != Type.INTEGER){
					throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Function arguments is not integer");
				}				
			}else{
				throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Function arguments is not integer");
			}
			
			expression_FunctionAppWithExprArg.setType(Type.INTEGER);
		}
		
		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		if(expression_FunctionAppWithIndexArg != null){
			if(expression_FunctionAppWithIndexArg.arg != null){
				expression_FunctionAppWithIndexArg.arg.visit(this, null);
			}
			expression_FunctionAppWithIndexArg.setType(Type.INTEGER);
		}
		
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		if(expression_PredefinedName != null){
			expression_PredefinedName.setType(Type.INTEGER);
		}
		
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		if(statement_Out != null){
			Declaration node = this.symbolTable.lookup(statement_Out.name);
			if(node == null){
				throw new SemanticException(statement_Out.firstToken, "Identifier " + statement_Out.name + " should be declared before use");								
			}
			
			if(statement_Out.sink != null){
				statement_Out.sink.visit(this, null);
			}			
			statement_Out.setDec(node);
			
			if(((node.getType() == Type.INTEGER || node.getType() == Type.BOOLEAN) && statement_Out.sink.getType() == Type.SCREEN)
					|| (node.getType() == Type.IMAGE && (statement_Out.sink.getType() == Type.FILE || statement_Out.sink.getType() == Type.SCREEN))){
				
			}else{
				throw new SemanticException(statement_Out.firstToken, "Type does not satisfy constraints");
			}
		}
		
		return statement_Out;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		
		if(statement_In != null){
			Declaration node = this.symbolTable.lookup(statement_In.name);
			if(node == null){
				throw new SemanticException(statement_In.firstToken, "Identifier " + statement_In.name + " should be declared before use");								
			}
			
			if(statement_In.source != null){
				statement_In.source.visit(this, null);
			}			
			statement_In.setDec(node);
			
			/*if(!(node != null && (node.getType() == statement_In.source.getType()))){
				throw new SemanticException(statement_In.firstToken, "Source type " + statement_In.source.getType() + " mismatches with statement type " + node.getType());
			}	*/				
		}
		
		return statement_In;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		if(statement_Assign != null){
			if(statement_Assign.lhs != null && statement_Assign.e != null){
				statement_Assign.lhs.visit(this, null);
				statement_Assign.e.visit(this, null);
				
				if((statement_Assign.lhs.getType() == statement_Assign.e.getType()) || (statement_Assign.lhs.isType(Type.IMAGE) && statement_Assign.e.isType(Type.INTEGER))){
					statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);					
				}else {
					throw new SemanticException(statement_Assign.firstToken, "Type of Lhs and expression does not match");
				}				
			}else{
				throw new SemanticException(statement_Assign.firstToken, "Type of Lhs and expression does not match");
			}
		}
		
		return statement_Assign;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {	
		
		if(lhs != null){
			
			Declaration node = this.symbolTable.lookup(lhs.name);
			lhs.declaration = node;
			if(node != null){
				lhs.setType(node.getType());
			}else{
				throw new SemanticException(lhs.firstToken, "Variable " + lhs.name +" must be declared before use");
			}
			
			if(lhs.index != null){
				lhs.index.visit(this, null);
				lhs.isCartesian = lhs.index.isCartesian();
			}								
		}
		
		return lhs;			
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {		
		if(sink_SCREEN != null){
			sink_SCREEN.setType(Type.SCREEN);
		}		
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		
		if(sink_Ident != null){
			Declaration node = this.symbolTable.lookup(sink_Ident.name);
			if(node != null){
				sink_Ident.setType(node.getType());
			}else{
				throw new SemanticException(sink_Ident.firstToken, "Variable should be declared before use");
			}
			
			if(!sink_Ident.isType(Type.FILE)){
				return new SemanticException(sink_Ident.firstToken, "File Type was expected");
			}
		}		
		
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		if(expression_BooleanLit != null){
			expression_BooleanLit.setType(Type.BOOLEAN);
		}
		
		return expression_BooleanLit;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		
		if(expression_Ident != null){			
			Declaration node = this.symbolTable.lookup(expression_Ident.name);
			if(node != null){
				expression_Ident.setType(node.getType());
			}else{
				throw new SemanticException(expression_Ident.firstToken, "Variable \"" + expression_Ident.name + "\" must be declared before use");
			}			
		}		
				
		return expression_Ident;
	}

}
