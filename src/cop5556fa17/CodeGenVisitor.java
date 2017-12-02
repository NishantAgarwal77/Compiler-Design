package cop5556fa17;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeCheckVisitor.SemanticException;
import cop5556fa17.AST.Statement_Assign;
//import cop5556fa17.image.ImageFrame;
//import cop5556fa17.image.ImageSupport;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
					
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		// and instructions to main method, respectively
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, "DEF_X", "I", null, null);
		fv.visitEnd();
		
		fv = cw.visitField(ACC_STATIC, "DEF_Y", "I", null, null);
		fv.visitEnd();
		
		fv = cw.visitField(ACC_STATIC, "Z", "I", null, null);
		fv.visitEnd();
				
		mv.visitLdcInsn(new Integer(256));		
		mv.visitFieldInsn(PUTSTATIC, className, "DEF_X", "I");
		mv.visitLdcInsn(new Integer(256));
		mv.visitFieldInsn(PUTSTATIC, className, "DEF_Y", "I");		
		mv.visitLdcInsn(new Integer(16777215));	
		mv.visitFieldInsn(PUTSTATIC, className, "Z", "I");
		
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		// generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);		

		// handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 8);		
				

		// Sets max stack size and number of local vars.
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily set the parameter in the ClassWriter constructor to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		// TODO
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, declaration_Variable.name, declaration_Variable.getType().getASMType(), null,
				null);
		fv.visitEnd();

		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name,
					declaration_Variable.e.getType().getASMType());
		}

		return declaration_Variable;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		expression_Binary.e0.visit(this, arg);
		expression_Binary.e1.visit(this, arg);
		Label expStart = new Label();
		Label expEnd = new Label();

		switch (expression_Binary.op) {
		case OP_AND:
			mv.visitInsn(IAND);
			break;
		case OP_MINUS:
			mv.visitInsn(ISUB);
			break;

		case OP_PLUS:
			mv.visitInsn(IADD);
			break;
		case OP_TIMES:
			mv.visitInsn(IMUL);
			break;
		case OP_DIV:
			mv.visitInsn(IDIV);
			break;
		case OP_OR:
			mv.visitInsn(IOR);
			break;
		case OP_MOD:
			mv.visitInsn(IREM);
			break;
		case OP_LE: {
			mv.visitJumpInsn(IF_ICMPLE, expStart);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;
		case OP_LT: {
			mv.visitJumpInsn(IF_ICMPLT, expStart);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;

		case OP_GT: {
			mv.visitJumpInsn(IF_ICMPGT, expStart);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;
		case OP_GE: {
			mv.visitJumpInsn(IF_ICMPGE, expStart);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;
		case OP_NEQ: {
			mv.visitJumpInsn(IF_ICMPNE, expStart);
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;
		case OP_EQ: {
			if (expression_Binary.e0.isType(Type.INTEGER) || expression_Binary.e0.isType(Type.BOOLEAN)) {
				mv.visitJumpInsn(IF_ICMPEQ, expStart);
			} else {
				mv.visitJumpInsn(IF_ACMPEQ, expStart);
			}
			mv.visitLdcInsn(false);
			mv.visitJumpInsn(GOTO, expEnd);
			mv.visitLabel(expStart);
			mv.visitLdcInsn(true);
			mv.visitLabel(expEnd);
		}
			break;
		default:
			break;
		}

		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		return null;		
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO

		if (expression_Unary.e != null) {
			expression_Unary.e.visit(this, arg);
		}

		switch (expression_Unary.op) {
		case OP_EXCL: {
			if (expression_Unary.e.isType(Type.INTEGER)) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
			} else if (expression_Unary.e.isType(Type.BOOLEAN)) {
				Label expStart = new Label();
				Label expEnd = new Label();
				mv.visitJumpInsn(IFEQ, expStart);
				mv.visitLdcInsn(new Integer(0));
				mv.visitJumpInsn(GOTO, expEnd);
				mv.visitLabel(expStart);
				mv.visitLdcInsn(new Integer(1));
				mv.visitLabel(expEnd);
			}
		}
			break;
		case OP_MINUS: {
			mv.visitInsn(INEG);
		}
			break;
			
		case OP_PLUS: {			
		}
			break;
		default: {
			throw new UnsupportedOperationException();
		}
		}

		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		
		index.e0.visit(this, null);
		index.e1.visit(this, null);
		
		if(!index.isCartesian()){			
			mv.visitInsn(Opcodes.DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,false);
			mv.visitInsn(Opcodes.DUP_X2);
			mv.visitInsn(Opcodes.POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,false);					
		}
				
		return index;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		if(expression_PixelSelector.index != null) {
			expression_PixelSelector.index.visit(this, arg);
		}
		
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig,false);
		return expression_PixelSelector;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
		expression_Conditional.condition.visit(this, arg);
		mv.visitLdcInsn(true);
		Label expStart = new Label();
		Label expEnd = new Label();

		mv.visitJumpInsn(IF_ICMPEQ, expStart);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, expEnd);
		mv.visitLabel(expStart);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(expEnd);
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {		
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null,null);
		fv.visitEnd();				
		
		if(declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
			if(declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}else {
				mv.visitInsn(ACONST_NULL);
			}
			
			if(declaration_Image.ySize != null) {
				declaration_Image.ySize.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			}else {
				mv.visitInsn(ACONST_NULL);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,false);
		}else {
			
			if(declaration_Image.xSize != null) {
				declaration_Image.xSize.visit(this, arg);
			}else {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
			}
			
			if(declaration_Image.ySize != null) {
				declaration_Image.ySize.visit(this, arg);
			}else {
				mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
			}		
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig,false);
		}		
		
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		return declaration_Image;
	}

	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return source_StringLiteral;
	}

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {

		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return source_CommandLineParam;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return source_Ident;
	}

	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		
		FieldVisitor fv;
		fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name, "Ljava/lang/String;", null,null);
		fv.visitEnd();
		
		if(declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, ImageSupport.StringDesc);
		}
		
		return declaration_SourceSink;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
		mv.visitLdcInsn(new Integer(expression_IntLit.value));
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6

		expression_FunctionAppWithExprArg.arg.visit(this, null);
		switch (expression_FunctionAppWithExprArg.function) {
		case KW_log: {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
			break;
		case KW_abs: {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
			break;
		default: {
			throw new UnsupportedOperationException();
		}
		}

		return expression_FunctionAppWithExprArg;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
				
		switch(expression_FunctionAppWithIndexArg.function) {
		case KW_cart_x : {
			expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
			expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig,false);
		}
		break;
		case KW_cart_y : {
			expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
			expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig,false);
		}
		break;
		case KW_polar_a :{
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);
		}
		break;
		case KW_polar_r : {
			expression_FunctionAppWithIndexArg.arg.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);
		}
		break;
		default : {
			throw new UnsupportedOperationException();
		}
		}
		
		return expression_FunctionAppWithIndexArg;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		
		switch (expression_PredefinedName.kind) {
		case KW_x : {			
			mv.visitVarInsn(ILOAD, 1);			
		}
		break;
		case KW_y : {
			mv.visitVarInsn(ILOAD, 2);
		}
		break;
		case KW_X :{
			mv.visitVarInsn(ILOAD, 3);
		}
		break;
		case KW_Y : {
			mv.visitVarInsn(ILOAD, 4);
		}
		break;
		case KW_r : {
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig,false);			
			
		}
		break;
		case KW_a : {
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig,false);
		}
		break;
		case KW_A :{
			mv.visitVarInsn(ILOAD, 7);
		}
		break;
		case KW_R : {
			mv.visitVarInsn(ILOAD, 8);
		}
		break;
		case KW_Z : {
			mv.visitFieldInsn(GETSTATIC, className, "Z", "I");	
		}
		break;
		case KW_DEF_X : {
			mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
		}
		break;
		case KW_DEF_Y : {
			mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
		}
		break;
		default : {
			throw new UnsupportedOperationException();
		}		
		}
		
		return expression_PredefinedName;
	}

	/**
	 * For Integers and booleans, the only "sink"is the screen, so generate code to
	 * print to console. For Images, load the Image onto the stack and visit the
	 * Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5: only INTEGER and BOOLEAN
		
		switch (statement_Out.getDec().getType()) {
		case INTEGER: {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(I)V", false);
		}
			break;
		case BOOLEAN: {
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
			CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
			break;
			
		case IMAGE : {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeLog", "globalLogAddImage", "("+ImageSupport.ImageDesc + ")V", false);
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			statement_Out.sink.visit(this, arg);
		}
		break;
		default: {
			throw new UnsupportedOperationException();
		}
		}
		// TODO HW6 remaining cases
		return statement_Out;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 * In HW5, you only need to handle INTEGER and BOOLEAN Use
	 * java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean to convert
	 * String to actual type.
	 * 
	 * TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {

		if (statement_In.source != null) {
			statement_In.source.visit(this, arg);
		}

		Declaration dec = statement_In.getDec();
		switch (dec.getType()) {
		case INTEGER: {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}
			break;
		case BOOLEAN: {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}
			break;
			
		case IMAGE : {	
			if(dec instanceof Declaration_Image) {
				Declaration_Image dImage = (Declaration_Image)dec;
				if(dImage.xSize != null) {
					dImage.xSize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}else {
					mv.visitInsn(ACONST_NULL);
				}
				
				if(dImage.ySize != null) {
					dImage.ySize.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}else {
					mv.visitInsn(ACONST_NULL);
				}				
			}else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig,false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, ImageSupport.ImageDesc);			
		}
		break;
		default: {
			throw new UnsupportedOperationException();
		}
		}

		return statement_In;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {

		if(statement_Assign.lhs.declaration.isType(Type.BOOLEAN) || statement_Assign.lhs.declaration.isType(Type.INTEGER)) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		
		if(statement_Assign.lhs.declaration.isType(Type.IMAGE)) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig,false);
			mv.visitVarInsn(ISTORE, 3);
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig,false);
			mv.visitVarInsn(ISTORE, 4);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			
			Label l4 = new Label();
			mv.visitJumpInsn(GOTO, l4);
			Label l5 = new Label();
			mv.visitLabel(l5);						
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);			
			Label l7 = new Label();
			mv.visitJumpInsn(GOTO, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);			
			
			statement_Assign.e.visit(this, null);
			statement_Assign.lhs.visit(this, arg);
							
			mv.visitIincInsn(2, 1);
			mv.visitLabel(l7);
								
			mv.visitVarInsn(ILOAD, 2);
			mv.visitVarInsn(ILOAD, 4);
			mv.visitJumpInsn(IF_ICMPLT, l8);		
			mv.visitIincInsn(1, 1);
			mv.visitLabel(l4);
					
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 3);
			mv.visitJumpInsn(IF_ICMPLT, l5);	
		}
		
		return statement_Assign;
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {

		if (lhs.isType(Type.INTEGER) || lhs.isType(Type.BOOLEAN)) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, lhs.getType().getASMType());
		}
		
		if(lhs.isType(Type.IMAGE)) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);						
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig,false);		
		}

		return lhs;
	}
	
	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		// TODO HW6		
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeFrame", ImageSupport.makeFrameSig,false);
		mv.visitInsn(Opcodes.POP);
		return sink_SCREEN;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		// TODO HW6
		
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig,false);
		return sink_Ident;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		// TODO
		mv.visitLdcInsn(new Boolean(expression_BooleanLit.value));
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident, Object arg) throws Exception {
		// TODO
		if (expression_Ident.isType(Type.BOOLEAN) || expression_Ident.isType(Type.INTEGER)) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, expression_Ident.getType().getASMType());
		} else {
			throw new UnsupportedOperationException();
		}

		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

}
