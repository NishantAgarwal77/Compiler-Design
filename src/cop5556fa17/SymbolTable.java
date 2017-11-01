package cop5556fa17;

import java.util.HashMap;
import java.util.Map;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.Declaration;

public class SymbolTable {
	
	private static SymbolTable instance;
	private static Map<String, Declaration> symbolTable;

	protected static Map<String, Declaration> getSymbolTable() {
		return symbolTable;
	}

	private SymbolTable() {
		symbolTable = new HashMap<String, Declaration>();
	}

	public static SymbolTable getSymbolTableInstance() {
		if(instance == null){
			return new SymbolTable();
		}
		return instance;		
	}
	
	public Declaration lookup(String nodeKey){
		return symbolTable.get(nodeKey);
	}
	
	public Declaration insert(String key, Declaration node){
		return symbolTable.put(key, node);
	}
}
