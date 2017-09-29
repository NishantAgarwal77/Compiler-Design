/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Scanner {
	
	public class ReserveWordChecker {
		private Map<String, Scanner.Kind> reserveWords = new HashMap<String, Scanner.Kind>();
		
		public ReserveWordChecker(){			
			addReserveWords();
		}
		
		private void addReserveWords(){
			
			reserveWords.put("x", Scanner.Kind.KW_x);
			reserveWords.put("X", Scanner.Kind.KW_X);
			reserveWords.put("y", Scanner.Kind.KW_y);
			reserveWords.put("Y", Scanner.Kind.KW_Y);
			reserveWords.put("r", Scanner.Kind.KW_r);
			reserveWords.put("R", Scanner.Kind.KW_R);
			reserveWords.put("a", Scanner.Kind.KW_a);
			reserveWords.put("A", Scanner.Kind.KW_A);
			reserveWords.put("Z", Scanner.Kind.KW_Z);
			reserveWords.put("DEF_X", Scanner.Kind.KW_DEF_X);
			reserveWords.put("DEF_Y", Scanner.Kind.KW_DEF_Y);
			reserveWords.put("SCREEN", Scanner.Kind.KW_SCREEN);
			reserveWords.put("cart_x", Scanner.Kind.KW_cart_x);
			reserveWords.put("cart_y", Scanner.Kind.KW_cart_y);
			reserveWords.put("polar_a", Scanner.Kind.KW_polar_a);
			reserveWords.put("polar_r", Scanner.Kind.KW_polar_r);
			reserveWords.put("abs", Scanner.Kind.KW_abs);
			reserveWords.put("sin", Scanner.Kind.KW_sin);
			reserveWords.put("cos", Scanner.Kind.KW_cos);
			reserveWords.put("atan", Scanner.Kind.KW_atan);
			reserveWords.put("log", Scanner.Kind.KW_log);
			reserveWords.put("image", Scanner.Kind.KW_image);
			reserveWords.put("int", Scanner.Kind.KW_int);
			reserveWords.put("boolean", Scanner.Kind.KW_boolean);
			reserveWords.put("url", Scanner.Kind.KW_url);
			reserveWords.put("file", Scanner.Kind.KW_file);			
		}
		
		public boolean isReserveWord(String word){
			return reserveWords.containsKey(word);
		}
		
		public Scanner.Kind getKindForReserveWord(String word){
			return reserveWords.get(word);
		}
	}	
		
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	public static enum State{
		START,
		IN_FORWARD_SLASH,
		IN_COMMENT, 
		IN_EQUAL, 
		IN_EXCLAMATION, 
		IN_TIMES,
		IN_LESS_THAN, 
		IN_GREATER_THAN, 
		IN_MINUS, 
		IN_INTEGER_LITERAL, 
		IN_IDENTIFIER, 
		IN_STRING_LITERAL, 
		IN_ESCAPE_SEQUENCE
	}

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}
		
		public boolean isKind(Kind kind){
			return this.kind == kind;
		}
		

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	public ReserveWordChecker reserveWordChecker;
	
	public String[] escapeSequences = {"\\n","\\t", "\\b", "\\f", "\\r", "\\\"","\\'", "\\\\"};
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  
	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		this.reserveWordChecker = new ReserveWordChecker();
	}

	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		int line = 1;
		int posInLine = 1;		
		int startPos = pos;
		State currentState = State.START;
		while(pos < chars.length){
			char currentChar = chars[pos];
			switch(currentState){
				case START: {
					switch(currentChar){	
					case '0' :{
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}					
					case '(' :{
						tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
					case ')' :{
						tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
					case '[' :{
						tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
					case ']' :{
						tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
					case ';' :{
						tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
					case ',' :{
						tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						break;
					}
						case '+' :{
							tokens.add(new Token(Kind.OP_PLUS, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '-' :{
							currentState = State.IN_MINUS;
							pos++;
							break;
						}
						case ':' :{
							tokens.add(new Token(Kind.OP_COLON, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '@' :{
							tokens.add(new Token(Kind.OP_AT, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '!' :{
							currentState = State.IN_EXCLAMATION;
							pos++;
							break;
						}
						case '>' :{
							currentState = State.IN_GREATER_THAN;
							pos++;
							break;
						}
						case '<' :{
							currentState = State.IN_LESS_THAN;
							pos++;
							break;
						}
						case '?' :{
							tokens.add(new Token(Kind.OP_Q, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '&' :{
							tokens.add(new Token(Kind.OP_AND, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '%' :{
							tokens.add(new Token(Kind.OP_MOD, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '|' :{
							tokens.add(new Token(Kind.OP_OR, startPos, 1, line, posInLine));
							pos++;
							posInLine++;
							startPos = pos;
							break;
						}
						case '*' :{
							currentState = State.IN_TIMES;
							pos++;
							break;
						}
						case '/' :{
							currentState = State.IN_FORWARD_SLASH;
							pos++;
							break;
						}
						case '=' :{
							currentState = State.IN_EQUAL;
							pos++;							
							break;
						}
						case '"' :{
							currentState = State.IN_STRING_LITERAL;
							pos++;
							break;
						}
						case EOFchar : {								
							pos++;
							startPos = pos;
							break;
						}
						default : {		
							int lineTerminatorLength = isLineTerminator(currentChar, pos);
							if(lineTerminatorLength > 0){
								line = line + 1;
								posInLine = 1;
								pos += lineTerminatorLength;
								startPos = pos;								
							}else if(Character.isDigit(currentChar)){
								currentState = State.IN_INTEGER_LITERAL;
								pos++;
							}else if(Character.isLetter(currentChar) || currentChar == '_' || currentChar == '$'){
								currentState = State.IN_IDENTIFIER;
								pos++;
							}else if(Character.isWhitespace(currentChar)){
								pos++;
								startPos = pos;
								posInLine++;
							}else{
								throw new LexicalException(MessageFormat.format("The character {0} is not allowed as input outside string literal", currentChar), pos);
							}
						}
					}	
					break;
				}	
				
				case IN_STRING_LITERAL : {
					
					if (currentChar == '\n' || currentChar == '\r'){
						throw new LexicalException("Line Terminators are not allowed inside String literal", pos);												
					}else if(currentChar == '"'){
						tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos - startPos + 1, line, posInLine));
						pos++;
						posInLine+= pos -startPos;						
						startPos = pos;
						currentState = State.START;						
					}else if(currentChar == '\\'){
						currentState = State.IN_ESCAPE_SEQUENCE;
						pos++;						
					}else if (currentChar == EOFchar){
						throw new LexicalException("The String passed is not valid, Check if string is closed properly " , pos);
					}
					else{
						pos++;
					}
					
					break;
				}
				
				case IN_ESCAPE_SEQUENCE : {
										
					String probableEscapeCharacter = new StringBuilder().append(chars[pos-1]).append(chars[pos]).toString();
					if(Arrays.asList(escapeSequences).contains(probableEscapeCharacter)){						
						pos++;
						currentState = State.IN_STRING_LITERAL;
					}else{
						throw new LexicalException("Single backslash is not allowed in string literal", pos);
					}
					
					break;
				}
				
				case IN_IDENTIFIER : {					
					if(Character.isLetterOrDigit(currentChar)||  currentChar == '_' || currentChar == '$'){
						pos++;
					}else{
						Token idenLiteral = new Token(Kind.IDENTIFIER, startPos, pos - startPos, line, posInLine);
						String textInput = idenLiteral.getText();
						if(textInput.equals("true")|| textInput.equals("false")){							
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos, line, posInLine));							
						}else if (reserveWordChecker.isReserveWord(textInput)){
							tokens.add(new Token(reserveWordChecker.getKindForReserveWord(textInput), startPos, pos - startPos, line, posInLine));							
						}else{
							tokens.add(idenLiteral);
						}
						posInLine += pos - startPos;
						startPos = pos;
						currentState = State.START;
					}
					break;
				}
				
				case IN_INTEGER_LITERAL : {
					if(Character.isDigit(currentChar)){
						pos++;
					}else{
						Token integerLiteral = new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos, line, posInLine);
						try{							
							int intLiteralValue = integerLiteral.intVal();
							tokens.add(integerLiteral);
							currentState = State.START;						
							posInLine += pos - startPos;
							startPos = pos;
						}catch(Exception e){
							System.out.println(e);
							throw new LexicalException("Integer literal out of range at : "  + integerLiteral.toString(),integerLiteral.pos);
						}						
					}
					break;
				}
				case IN_MINUS : {
					if(currentChar == '>'){
						tokens.add(new Token(Kind.OP_RARROW, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_MINUS, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}							
				case IN_LESS_THAN : {
					if(currentChar == '='){
						tokens.add(new Token(Kind.OP_LE, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else if(currentChar == '-'){
						tokens.add(new Token(Kind.OP_LARROW, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_LT, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}		
				case IN_GREATER_THAN : {
					if(currentChar == '='){
						tokens.add(new Token(Kind.OP_GE, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_GT, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}			
				case IN_EXCLAMATION : {
					if(currentChar == '='){
						tokens.add(new Token(Kind.OP_NEQ, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_EXCL, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}			
				case IN_TIMES : {
					if(currentChar == '*'){
						tokens.add(new Token(Kind.OP_POWER, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_TIMES, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}			
				case IN_EQUAL : {
					if(currentChar == '='){
						tokens.add(new Token(Kind.OP_EQ, startPos, 2, line, posInLine));
						pos++;
						posInLine += 2;
					}else{
						tokens.add(new Token(Kind.OP_ASSIGN, startPos, 1, line, posInLine));
						posInLine++;
					}
					startPos = pos;
					currentState = State.START;
					break;
				}
				case IN_FORWARD_SLASH : {
					if (currentChar == '/'){
						currentState = State.IN_COMMENT;
						pos++;
						posInLine++;
					}else{
						tokens.add(new Token(Kind.OP_DIV, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
						startPos = pos;
						currentState = State.START;
					}
					break;
				}
				case IN_COMMENT : {
					if(currentChar != '\n' && currentChar != '\r'){		
						pos++;
						posInLine++;
					}else{												
						currentState = State.START;
					}	
					startPos = pos;
					break;
				}
			}			
		}
		
		tokens.add(new Token(Kind.EOF, pos-1, 0, line, posInLine));
		return this;

	}

	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}
	
	public int isLineTerminator(char currentChar , int pos){		
		if(currentChar == '\n'){
			return 1;			
		}else if (currentChar == '\r' && pos + 1 < chars.length && chars[pos + 1] == '\n'){
			return 2;
		}else if (currentChar == '\r'){
			return 1;
		}else {
			return 0;
		}
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}
}
