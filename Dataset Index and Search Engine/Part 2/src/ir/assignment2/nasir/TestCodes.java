package ir.assignment2.nasir;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCodes {
public static void main(String a[]) throws IOException{
	//String test="1960S";
	//Pattern p=Pattern.compile("^[0-9]+[A-Za-z]+[A-Za-z]*$");
	
	/*String[] splits=test.split("[0-9]+[a-z]+");
	for(String s:splits)
		System.out.println(s);*/
	/*Matcher m=p.matcher(test);
	if(m.find())
		System.out.println(test.replaceAll("[A-Za-z]", ""));*/
	
	String ex="Naomi Foner, who wrote ``Running on Empty'' and also served as"
              +" the film's 192...168.1.1 executive producer, grew up in Brooklyn, N.Y., the"
              +" daughter of sociologists. Her own experiences made Foner well";
	/*Pattern p=Pattern.compile("\\w+(\\.?\\w+)*");
	Matcher m=p.matcher(ex);
	while(m.find()){
		System.out.print("Start index: " + m.start());
	      System.out.print(" End index: " + m.end() + " ");
	      System.out.println(m.group());
	}*/
	
	PorterStemmer stemmer=new PorterStemmer();
	System.out.println(stemmer.stem("u.s."));
	String s="hello friendship";
	String term="friends";
	System.out.println(s.matches(".*\\b"+term+"\\b.*"));
	
	System.out.println(stemmer.stem(term));
	
	System.out.println("hi new".indexOf("new"));
	
	HashMap<Integer,String> h=new HashMap<Integer,String>();
	h.put(1, "a");
	System.out.println(h.containsKey(1));
	
	Pattern p=Pattern.compile("\\[[0-9]+\\]");
	Matcher m=p.matcher("1#new#2#(AP890101-0001):4,11:[245]#(AP890101-0002):5:[1]");
	int hashC=0;
	while(m.find())
		{   String tf=m.group().replaceAll("[\\[\\]]", "");
		    System.out.println(tf);
		    hashC=hashC+Integer.parseInt(tf);
		}
	System.out.println(hashC);
	
	Pattern p2=Pattern.compile("\\([\\w-]+\\)");
	Matcher m2=p2.matcher("1#new#2#(AP890101-0001):4,11:[245]#(AP890101-0002):5:[1]");
	int df=0;
	while(m2.find())
		{   
		    //System.out.println(m2.group());
		    df++;
		}
    String e="1#car#2#";
    String[] splits=e.split("#");
    System.out.println("length="+splits.length);
    for(String s1:splits)
    	System.out.println(s1);
    long stime,etime;
	stime=System.currentTimeMillis();
    InvertedIndex2 inv=new InvertedIndex2();
    inv.mergePartialList();
    etime=System.currentTimeMillis();
    System.out.println("Time taken to merge = "+(etime-stime)/1000);
	
	
}

}

class ValueComparatorTest implements Comparator<String> {
	 
    Map<String, Integer> map;
 
    public ValueComparatorTest(Map<String, Integer> base) {
        this.map = base;
    }
 
    public int compare(String a, String b) {
    	System.out.println(map.get(b));
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }

}
