package cop5556fa17;

import cop5556fa17.Scanner.Token;

public class TypeUtils {
	
	public static enum Type {
		INTEGER("I"),
		BOOLEAN("Z"),
		IMAGE(ImageSupport.ImageDesc),
		URL("Ljava/net/URL;"),
		FILE("Ljava/io/File;"),
		SCREEN(ImageSupport.JFrameDesc),
		NONE(null);
		
		String asmType;
		Type(String asmType){
			this.asmType = asmType;
		}
			
		public String getASMType() {
			return asmType;
		}
	}

	public static Type getType(Token token){
		switch (token.kind){
		case KW_int: {return Type.INTEGER;} 
		case KW_boolean: {return Type.BOOLEAN;} 
		case KW_image: {return Type.IMAGE;} 
		case KW_url: {return Type.URL;} 
		case KW_file: {return Type.FILE;}
		case KW_SCREEN: {return Type.SCREEN;}
			default :
				break; 
		}
		assert false;  //should not reach here
		return null;  
	}
}
