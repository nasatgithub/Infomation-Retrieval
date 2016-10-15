package assignments.ir.nasir;

import java.util.ArrayList;
import java.util.List;

public class ListToStringTest {
public static void main(String a[]){
	List<String> l=new ArrayList<String>();
	l.add("hello");
	l.add("bye");
	l.add("good morning");
	System.out.println(l.size()/2);
	List<String> s=l.subList(0, 3);
	for(String str:s)
		System.out.println(str);
}
}
