package com.example.maximilienpluchard.inkarma;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dubois
 * 
 * This class defines a simple  scripting language, with string, integer algebra.
 * It provides a lightweight parser to execute commands and evaluate expression, and direct access to variable store.
 * It is designed to be dropped and used into any Java project without any dependency or configuration (it only requires java.util.HashMap).
 * 
 *  
 * Variables names consists of letters and numbers, and may only begins with letters. Supported types are integer, boolean and strings, with
 * automatic type casting when applicable. Strings are " delimited, and may contains expression to be evaluated inside $$. 
 * Variable reference preceded with $ is assumed to reference a String expression, which is to be evaluated. 
 * 
 * It supports the following operators, in reverse priority order :  
 * 
 * =
 * ? :
 * |
 * &
 * == != < > <= >=
 * + -
 * * /
 * - ! (unary)
 * 
 * EBNF-like grammar :
 * 
 * program = statement [';' program] .
 * statement = [ident '=' ] expression ['?' expression ':' expression] .
 * expression = bool { '|' bool } .
 * bool = test { '&' test } .
 * test = sum { ( '==' | '!=' | '<' | '>' | '<=' | '>=' sum } .   
 * sum = term {('+' | '-' ) term } .
 * term = factor { ( '*' | '/' ) factor } .
 * factor = [ '-' | '!' ]
 *  (  [$] ident
 *    | number
 *    | '(' statement ')'
 *    | string   ) .    
 *  string =   
 *        ( '"' {^"$} {'$' program '$' {^"$} } '"' )
 *     | ''' {^'} '''  .
 * 
 */
public class Script {

	// character type used
	public static enum TYPE {DIGIT,LETTER,SPACE,
		OP_PLUS,OP_MINUS,OP_TIME,OP_DIVIDE,
		OP_AND,OP_OR,OP_NEG,OP_TEST,OP_SELECT,
		L_PAR,R_PAR,EVALUATE,
		QUOTE,SIMPLE_QUOTE,SEMI_COLON,
		EGAL,LT,GT,
		OTHER,EOF};	

		/**
		 * tests character type
		 * @param c
		 * @return type found
		 */
		public static TYPE getType(char c){			
			if (Character.isDigit(c)) return TYPE.DIGIT;
			if (Character.isLetter(c)) return TYPE.LETTER;
			if (Character.isWhitespace(c) ) return TYPE.SPACE;
			if (c=='+') return TYPE.OP_PLUS;
			if (c=='-') return TYPE.OP_MINUS;
			if (c=='*') return TYPE.OP_TIME;
			if (c=='/') return TYPE.OP_DIVIDE;

			if (c=='&') return TYPE.OP_AND;
			if (c=='|') return TYPE.OP_OR;
			if (c=='!') return TYPE.OP_NEG;

			if (c=='?') return TYPE.OP_TEST;
			if (c==':') return TYPE.OP_SELECT;		

			if (c=='(') return TYPE.L_PAR;
			if (c==')') return TYPE.R_PAR;
			if (c=='$') return TYPE.EVALUATE;
			if (c=='"') return TYPE.QUOTE;
			if (c=='\'') return TYPE.SIMPLE_QUOTE;
			if (c==';') return TYPE.SEMI_COLON;

			if (c=='=') return TYPE.EGAL;			
			if (c=='<') return TYPE.LT;			
			if (c=='>') return TYPE.GT;			

			return TYPE.OTHER;
		}

		/**
		 * Convert object to int.
		 * 
		 * null value is 0.
		 * boolean value is O or 1 (resp. false or true).
		 * string value is parsed to int.
		 * 
		 * @param o
		 * @return
		 */
		public static int asInt(Object o){
			if (o == null) return 0;
			if (o instanceof Integer) return (Integer) o;
			if (o instanceof Boolean) return ((Boolean) o) ? 1 :0;
			return Integer.parseInt(o.toString());
		}

		/**
		 * Convert object to boolean
		 * 
		 * null value is false.
		 * integer value is false if and only if it is 0
		 * string value is always true.
		 * 
		 * @param o
		 * @return
		 */
		public static boolean asBool(Object o){
			if (o == null) return false;
			if (o instanceof Boolean) return ((Boolean) o);
			if (o instanceof Integer) return ((Integer) o) != 0;
			return true;
		}


		/**
		 * Compare 2 string, int or boolean.
		 * 
		 * null is less than everything else.
		 * If one parameter is a string, the other is promoted to string.
		 * Else, if one parameter is an int the other is promoted to int.
		 * false is less than true
		 * 
		 * @param a
		 * @param b
		 * @return
		 */
		public static int compare(Object a, Object b){
			if (a == null && b == null) {
				return 0;
			} else if (a == null){
				return +1;
			} else if (b == null){
				return -1;
			} else if ((a instanceof String) || (b instanceof String) ){
				return a.toString().compareTo(b.toString());
			} else if ((a instanceof Integer) || (b instanceof Integer) ){
				int ia = asInt(a),ib = asInt(b);
				return  ia == ib ? 0 : (ia < ib ? -1 : +1);
			} else {
				boolean ba = asBool(a), bb = asBool(b);
				return  (ba == bb)? 0 : (bb ? -1 : +1);
			}		
		}


		/**
		 * @author dubois
		 * This were the parsing is done. An instance is created for each string to parse,
		 * in order to make the parsing thread/reentry safe (if no variable is used in different threads).  
		 */
		private class Parser {

			private final String string; // String to parse
			private int current; // current index in the string


			/**
			 * Generate a parser
			 * @param string String to parse
			 */
			private Parser(String string) {
				this.string = string;
				current = 0;
			}


			/**
			 * Skip one character.
			 */
			private void consume() {
				++current;
			}

			/**
			 * Find type of character at given position in string.
			 * @param position
			 * @return character type, EOF if no character available 
			 */
			private TYPE getTypeAt(int position){
				if (position>= string.length()) return TYPE.EOF;
				return getType(string.charAt(position));
			}

			/**
			 * Check current character type, and consumes it if it matches. 
			 * @param t expected type
			 * @return true if type matches. If true, advance to next character
			 */
			private boolean accept(TYPE t){
				if (!check(t)) return false;
				consume();
				return true;
			}

			/**
			 * Check current and next character types, and consumes both if they match. 
			 * @param t expected type
			 * @return true if types match. If true, advance two characters
			 */
			private boolean accept(TYPE t1, TYPE t2){
				if (!check(t1)) return false;
				if (!checkNext(t2)) return false;
				consume();consume();
				return true;
			}

			/**
			 * Check current character type, without moving.
			 * @param t expected type
			 * @return true if type matches
			 */
			private boolean check(TYPE t) {
				return getTypeAt(current) == t;
			}

			/**
			 * Check next character type, without moving.
			 * @param t expected type
			 * @return true if type matches
			 */
			private boolean checkNext(TYPE t) {
				return getTypeAt(current+1) == t;
			}

			private void globbleSpace(){
				while (accept(TYPE.SPACE));
			}

			/**
			 * Read the next variable name.
			 * 
			 * Name stars with a letter, and contains only letters and digit.
			 * Advance to the first character after the name.
			 * @return variable name
			 */
			private String readIdent(){
				globbleSpace();
				int start = current;
				if (!accept(TYPE.LETTER)) return null;
				while(accept(TYPE.LETTER) || accept(TYPE.DIGIT));
				int end = current;
				globbleSpace();
				return string.substring(start,end);
			}

			/**
			 * Read an integer
			 * 
			 * Read and consume a relative integer literal. The integer may begin with
			 * a sign character.
			 * @return parsed integer value
			 */
			private int readNumber(){
				globbleSpace();
				int start = current;
				if (accept(TYPE.OP_MINUS) || accept(TYPE.OP_PLUS));
				while(accept(TYPE.DIGIT));
				int end = current;
				globbleSpace();
				return Integer.parseInt(string.substring(start,end));			
			}


			/**
			 * Read one integer variable, value or expression.
			 * 
			 * factor = [ '-' | '!' ]
			 *  (  ['$'] ident
			 *    | number
			 *    | '(' statement ')'
			 *    | string   ).
			 *    
			 * @return Computed value
			 */
			private Object readFactor(){

				globbleSpace();

				boolean int_negation = (accept(TYPE.OP_MINUS));
				boolean bool_negation = !int_negation && (accept(TYPE.OP_NEG));

				Object result = null;				

				globbleSpace();
				if (accept(TYPE.EVALUATE)){ // interprets variable as a program
					result = evaluate(String.valueOf((get(readIdent()))));					
				} else if (check(TYPE.LETTER)) { // get variable value
					result =  get(readIdent());
				} else if (accept(TYPE.L_PAR)) { // handle parenthesis 
					result = readStatement();
					globbleSpace();
					accept(TYPE.R_PAR);
					globbleSpace();
				} else if(check(TYPE.QUOTE)|| check(TYPE.SIMPLE_QUOTE)){ // handle string litteral 
					result = readString();										
				} else {
					result = readNumber();
				}

				if (int_negation) return -asInt(result);
				if (bool_negation) return !asBool(result);
				return result;
			}

			/**
			 * Read one string litteral.
			 * 
			 * Programs inside $$ are evaluated and replaced par resulting value			 * 
			 * 
			 *  string =    '"' {^"$} {'$' program '$' {^"$} } '"'
			 */
			private String readString() {
				// TODO Auto-generated method stub
				String result = "";				
				globbleSpace();
				int start;
				if (accept(TYPE.QUOTE)){ // double quote allows inner $expressions$ evaluation.
					start = current;
					while(!accept(TYPE.QUOTE)) {
						if(accept(TYPE.EVALUATE)){ 
							result += string.substring(start,current-1); //ends and adds current string
							result += String.valueOf(readProgram()); //evaluate program
							globbleSpace();
							accept(TYPE.EVALUATE);
							start = current;		                 // resume string literal				
						} else {
							consume();						
						}
					}
				} else { // simple quote allows no evaluation
					accept(TYPE.SIMPLE_QUOTE);
					start = current;
					while(!accept(TYPE.SIMPLE_QUOTE)) consume();						
				}
				result += string.substring(start,current-1); //litteral ends
				globbleSpace();

				return result;
			}


			/**
			 * Read a product of factors.
			 * 
			 * term = factor { ( '*' | '/' ) factor } .
			 * 
			 * @return Computed value
			 */
			private Object readTerm(){
				globbleSpace();
				Object result = readFactor();
				while(true){
					globbleSpace();
					if (accept(TYPE.OP_TIME)){
						result = asInt(result) * asInt(readFactor());
					} else if (accept(TYPE.OP_DIVIDE)){
						result = asInt(result) /  asInt(readFactor());
					} 			
					else {
						return result;
					}
				}
			}

			/**
			 * Performs or operation on boolean expressions.
			 * 
			 * expression = bool { '|' bool } .
			 * 
			 * @return Computed value
			 */
			private Object readExpression() {
				globbleSpace();
				Object result = readBool();

				while(true){
					globbleSpace();
					if (accept(TYPE.OP_OR)){
						result = asBool(result) || asBool(readBool());
					} else {
						return result;
					}				
				}			
			}

			/**
			 * Performs logical AND on operands
			 * 
			 * bool = test { '&' test } .
			 * 
			 * @return
			 */
			private Object readBool() {
				globbleSpace();
				Object result = readTest();

				while(true){
					globbleSpace();
					if (accept(TYPE.OP_AND)){
						result = asBool(result) || asBool(readTest());
					} else {
						return result;
					}				
				}			
			}


			/**
			 * Evaluate test
			 * 
			 * test = sum { ( '==' | '!=' | '<' | '>' | '<=' | '>=' sum } .   
			 * @return
			 */

			private Object readTest() {
				globbleSpace();
				Object result = readSum();
				while(true){
					globbleSpace();
					if (accept(TYPE.EGAL,TYPE.EGAL)){
						result = compare(result,readSum())==0;
					} else if (accept(TYPE.OP_NEG,TYPE.EGAL)){
						result = compare(result,readSum())!=0;					
					} else if (accept(TYPE.LT,TYPE.EGAL)){
						result = compare(result,readSum())<=0;					
					} else if (accept(TYPE.GT,TYPE.EGAL)){
						result = compare(result,readSum())>=0;					
					} else if (accept(TYPE.LT)){
						result = compare(result,readSum())<0;					
					} else if (accept(TYPE.GT)){
						result = compare(result,readSum())>0;					
					} 			
					else {
						return result;
					}
				}
			}

			/**
			 * Read a sum of terms
			 * 
			 * sum = term {('+' | '-' ) term } .
			 * @return
			 */
			private Object readSum() {

				globbleSpace();
				Object result = readTerm();
				while(true){
					globbleSpace();
					if (accept(TYPE.OP_PLUS)){
						Object term = readTerm();
						if ((term instanceof String) || (result instanceof String) )
							result = String.valueOf(result)+ String.valueOf(term);	
						else 
							result = asInt(result)+asInt(term);					
					} else if (accept(TYPE.OP_MINUS)){
						result = asInt(result) - asInt(readTerm());	
					}
					else {
						return result;
					}
				}			
			}


			/**
			 * Read and execute a statement.
			 * The statement is either an affectation, or an expression and may use
			 * the ternary test operator
			 * 
			 * statement = [ident '=' ] expression ['?' expression ':' expression] .
			 *  
			 * @return Computed value.
			 */		
			private Object readStatement() {
				globbleSpace();
				int start = current; // Save position for backtracking purpose
				String ident = null;
				Object value = null;

				// Looking for affectation.
				// if successful, ident contains the target variable name ;
				// if not, ident revert to null and we backtrack
				if (check(TYPE.LETTER)){
					ident = readIdent();				
					if ( accept(TYPE.EGAL,TYPE.EGAL) || !accept(TYPE.EGAL)){ // == or not = at all						
						// -> this is not an affectation
						ident = null;
						current = start; //backtracking...
					}				
				}						

				value = readExpression();

				if (accept(TYPE.OP_TEST)){ //  ? : 
					Object expression1 = readExpression();
					accept(TYPE.OP_SELECT);
					Object expression2 = readExpression();
					value =  asBool(value) ? expression1 : expression2;
				}			

				if (ident != null) put(ident,value); // performs affectation
				globbleSpace();
				return value;
			}

			/**
			 * Execute a program.
			 * 
			 * The program is composed of semicolon separated statements.
			 *   
			 * @return  Last statement value. Actual type is either String, Integer or Boolean.
			 */	
			Object readProgram(){
				Object result;

				result = readStatement();
				while(true){
					globbleSpace();
					if (accept(TYPE.SEMI_COLON)){
						result = readStatement();
					} else {
						return result;
					}
				}
			}		
		} 

		//Variables store
		private Map<String,Object> var;


		// Constructors
		
		
		public Script(){
			var = new HashMap();
		}

		public Script(Map<String,Object> varMap){
			var = new HashMap(varMap);
		}


		// Direct access to stored variables

		public Object get(String name) {
			return var.get(name);
		}

		public Object put(String name, Object value) {
			return var.put(name, value);
		}

		public Map<String,Object> getVarMap(){
			return var;
		}
		
		public void clear(){
			var.clear();
		}

		public void putAll(Map<String,Object> map){
			var.putAll(map);
		}

		// Typed access to variables

		public String getString(String name){
			return String.valueOf(var.get(name));
		}

		public int getInt(String name){
			return asInt(get(name));
		}

		public boolean getBool(String name){
			return asBool(get(name));
		}

		// Command evaluation

		public Object evaluate(String command) {
			return new Parser(command).readProgram();
		}	

		public void printAndExecute(String command){
			Object result = evaluate(command);
			System.out.println(command+" -> "+
					((result instanceof Integer) ? "(int) " : 
						(result instanceof Boolean) ? "(bool) " :"(String) ")+result);
		}

		//regression tests
		public static void main(String[] arg){
			Script script = new Script();
			script.printAndExecute("\"hello world\"");
			script.printAndExecute("2+2");
			script.printAndExecute("a=2;a+a");
			script.printAndExecute("test=\"test\";test + test");
			script.printAndExecute("a=(3*(1+1)-1);b=a/2;c=a-b*2");
			script.printAndExecute("b");
			script.printAndExecute("3*1+1-1*2");
			script.printAndExecute("2==2");
			script.printAndExecute("2!=2");
			script.printAndExecute("b==b");
			script.printAndExecute("2<3");
			script.printAndExecute("2<=2");
			script.printAndExecute("3>=5");
			script.printAndExecute("a");
			script.printAndExecute("a==2");
			script.printAndExecute("b==2");
			script.printAndExecute("(a==5)&(b==2)");
			script.printAndExecute("a==5|b==2");
			script.printAndExecute("d=\"tete\";d<test");
			script.printAndExecute("d>test");
			script.printAndExecute("!(2==2)");
			script.printAndExecute("!(2!=2)");
			script.printAndExecute("!(!(2==2))");
			script.printAndExecute("(2==2)==(3==3)");
			script.printAndExecute("true=2==2");
			script.printAndExecute("false=2==3");
			script.printAndExecute("true&false|true&true");
			script.printAndExecute("true&false|false&true");
			script.printAndExecute("!true&false|true&true");
			script.printAndExecute("!true&false");
			script.printAndExecute("!true&false");
			script.printAndExecute("a==5?1:20");
			script.printAndExecute("false?1:20");
			script.printAndExecute(" a = ( 3 * ( 1 + 1 ) - 1 ) \n" +
					"; b = a / 2 ; c = a - b * 2 ");
			script.printAndExecute("HP=20");
			script.printAndExecute("HP = HP - ((a!=5)?1:10)");
			script.printAndExecute("HP = HP -10");
			script.printAndExecute(" HP > 0 ? \"Alive\" : \"Dead\" ");
			script.printAndExecute("e=e+1");
			script.printAndExecute("e=e+1");
			script.printAndExecute("f=f+\".\"");
			script.printAndExecute("f=f+\".\"");
			script.printAndExecute("true?f:1");
			script.printAndExecute("\"2+2=$2+2$\"");
			script.printAndExecute("\"a=$a$\"");
			script.printAndExecute("name = \"bob\";\"your name is $name$\"");
			script.printAndExecute("name + \" has $HP$ HP, and is $HP>0?\"alive\":\"dead\"$.\"");
			script.printAndExecute("status = 'name + \" has $HP$ HP, and is $HP>0?\"alive\":\"dead\"$.\"'");
			script.printAndExecute("\"$name$ drink a potion, and now $HP=HP+5;$status$\"");
			script.printAndExecute("\"value is 5$'$'$\"");
			script.printAndExecute("\"value is 5\"+'$'");
			script.printAndExecute("'value is 5$'");
			script.printAndExecute("'$name$'");

		}
}




