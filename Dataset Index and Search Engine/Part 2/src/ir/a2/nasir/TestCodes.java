package ir.a2.nasir;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

	System.out.println("2D Array Test");
	int matrix[][]=new int[2][2];
	
	for(int i=0;i<matrix.length;i++){
		int col[]=matrix[i];
		for(int j=0;j<col.length;j++)
			System.out.print(matrix[i][j]+"\t");
		System.out.println();
	}
	
   System.out.println(Integer.MAX_VALUE);

	
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
