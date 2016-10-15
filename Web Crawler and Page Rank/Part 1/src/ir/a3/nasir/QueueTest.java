package ir.a3.nasir;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueTest {
public static void main(String a[]){
	Queue<String> q=new PriorityQueue<String>(); 
	q.add("b");
	q.add("a");
	q.add("c");
	System.out.println(q.remove());
	System.out.println(q.remove());
	System.out.println(q.remove());
	
	
	LinkedList<String> l=new LinkedList<String>();
	l.add("b");
	l.add("a");
	l.add("c");
	System.out.println(l.remove());
	System.out.println(l.remove());
	System.out.println(l.remove());
	

}
}
