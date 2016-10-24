package examples;

import java.util.ArrayList;
import java.util.List;

import examples.algo.LinkedListImpl;

public class TestEx {
	
	
	 public static void parseList(List<? extends Object> list) { } 
	 
	 List<Number>[] arrayOfLists = new List<Number>[2]; 
	
	public static void main(String[] args) {
		
		
		 List<String> stringList = new ArrayList<String>(); 
		 parseList(stringList); 
		LinkedListImpl impl= new LinkedListImpl();
		LinkedListImpl implDiff = new LinkedListImpl();
		LinkedListImpl implSame = impl;
		
		System.out.println("impl== implSame "+ (impl== implSame));
		System.out.println("impl== implDiff "+ (impl== implDiff));
		
		System.out.println("impl equals implSame "+ (impl.equals( implSame)));
		System.out.println("impl equals implDiff "+ (impl.equals(implDiff)));
		
		int a =8;
		int b=8;
		int c =a;
		System.out.println("a== b "+ (a== b));
		System.out.println("a== c "+ (a== c));
	
		String str1 ="Test";
		String str2 ="Test";
		String str3 =str1;
		
		System.out.println("impl== implSame "+ (str1== str2));
		System.out.println("impl== implDiff "+ (str1== str3));
		
		System.out.println("impl equals implSame "+ (str1.equals( str2)));
		System.out.println("impl equals implDiff "+ (str1.equals(str3)));
		
	}
}
