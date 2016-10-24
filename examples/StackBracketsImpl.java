package examples;

import java.util.Stack;

public class StackBracketsImpl {
	
	/**
	 * testcases
	 * ((
	 * ()
	 * ({})
	 * }}
	 * ({)}
	 * null
	 * @param s
	 * @return
	 */
	public boolean balanced(String s)
	{	
		Stack<Character> stack = new Stack() ;

		if(s== null || s.length()==0)
			return false;
		char[] c = s.toCharArray();
		for(int i=0;i<c.length ;i++)
		{
			if(c[i] == '{' || c[i] == '(' || c[i] == '[')
			{
				stack.push(c[i]);
			}
			else if(stack.isEmpty() && 
					(c[i] == '}' || c[i] == ']' || c[i] == ')' ))
			{
				return false;
			}
			else if(!stack.isEmpty() && 
					((c[i] == '}' && stack.peek() == '{' ) || (c[i] == ']' && stack.peek() == '[' ) || (c[i] == ')' && stack.peek() == '(' )))
			{
				stack.pop();
			}			
			
		}
		if(stack.isEmpty() )
			return true;
		
		return false;
	}
	
	public boolean check(String s)
	{
		Stack<Character> stack = new Stack() ;

		if(s== null || s.length()==0)
			return false;
		char[] c = s.toCharArray();
		
		for(int i=0;i<c.length ;i++)
		{
			if(c[i] == '{' || c[i] == '(' || c[i] == '[')
			{
				stack.push(c[i]);
			}
			else
			{
				if((c[i] == '}' && stack.peek() == '{') || ( c[i] == ')' && stack.peek() == '(')|| (c[i] == ']'&& stack.peek() == '['))
				{
					if(!stack.isEmpty())
						stack.pop();
				}

			}
		}
		if(stack.isEmpty())
			return true;
		return false;
		
	}
	
	public static void main(String[] args) {
		StackBracketsImpl impl = new StackBracketsImpl();
		System.out.println("s == null "+impl.check(null));
		System.out.println("s ==   "+impl.check(" "));
		System.out.println("s == (( "+impl.check("(("));
		System.out.println("s == ({}) "+impl.check("({})"));
		System.out.println("s == )) "+impl.check("))"));
		System.out.println("s == ({)} "+impl.check("({)}"));
		System.out.println("s == null "+impl.check(null));

		
	}

}
