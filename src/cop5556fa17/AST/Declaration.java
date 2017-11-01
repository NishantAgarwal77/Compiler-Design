package cop5556fa17.AST;

import cop5556fa17.TypeUtils;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.TypeUtils.Type;

public abstract class Declaration extends ASTNode {	
	
	public Declaration(Token firstToken) {
		super(firstToken);
	}
}
