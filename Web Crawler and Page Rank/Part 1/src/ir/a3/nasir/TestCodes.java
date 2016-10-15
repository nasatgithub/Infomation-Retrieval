package ir.a3.nasir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestCodes {
public static void main(String a[]) throws Exception{
   //parseFile();
	//sortTest2();
    // extractRelevantUrl();
   //checkTextInContent();
   //validExtension();
	//readURLS();
	/*List<String> lines=WebCrawler2.myRead2("./Files/inlinks_parallel_6.txt");
	for(String line:lines)
		System.out.println(line);*/
	/*String u="obama barack";
	System.out.println(u.hashCode());
	HashMap<String,Integer> h=new HashMap<String, Integer>();
	System.out.println(h.containsKey("a"));*/
    List<Integer> l=new ArrayList<Integer>();
    l.add(2);
    l.add(3);
    l.add(1);
    Collections.sort(l);
    Collections.reverse(l);
    System.out.println(l);
    
}
public static void parseFile() throws Exception{
	File f=new File("./Files/test2.html");
	Document doc=Jsoup.connect("").get();
	Element content=doc.getElementById("content");
	System.out.println(doc.outerHtml());
/*	Elements ahrefs=doc.select("a[href]");
	for(Element ahref:ahrefs){
		System.out.println(ahref.attr("href"));
	}*/
}
public static void sortTest(){
	UrlIC u1=new UrlIC();
	u1.setUrl("b");
	u1.setIc(4);
	u1.setTime(1);
	UrlIC u2=new UrlIC();
	u2.setUrl("a");
	u2.setIc(2);
	u2.setTime(3);
	TreeMap<String,UrlIC> t=new TreeMap<String,UrlIC>();
	t.put("b", u1);
	t.put("a", u2);
	for(String k:t.keySet())
		System.out.println(t.get(k).getUrl());
}

public static void sortTest2(){
	Comparator<UrlIC> comparator = new UrlComparator1();
	HashMap<String,UrlIC> h=new HashMap<String,UrlIC>();
	PriorityQueue<UrlIC> pq=new PriorityQueue<UrlIC>(2, comparator);
	UrlIC u1=new UrlIC();
	u1.setUrl("b");
	u1.setLevel(2);
	u1.setIc(5);
	u1.setTime(5);
	pq.add(u1);
	h.put(u1.getUrl(), u1);
	UrlIC u2=new UrlIC();
	u2.setUrl("a");
	u2.setLevel(1);
	u2.setIc(4);
	u2.setTime(3);
	pq.add(u2);
	h.put(u2.getUrl(), u2);
	
    
	/*UrlIC uu=h.get("a");
	uu.setIc(6);
	pq.remove(uu);
	pq.add(uu); */
	System.out.println(pq);
	System.out.println(pq.remove().getUrl());
	
	
}
public static void extractRelevantUrl() throws Exception{
	String url="http://en.wikipedia.org/wiki/Barack_Obama";
	URL myUrl=new URL(url);
	Document doc=Jsoup.connect(url).get();
	System.out.println(doc.html());
	
}
public static void checkTextInContent(){
	String text="Hi Hello Welcome to Boston. This is such a wonderful place. it is 9.25 pm now.";
	System.out.println(text.contains("Welcome"));
}
public static void validExtension(){
	String url="http://en.wikipedia.org/wiki/Barack_Obama.vob";
	System.out.println(url.matches("[\\w].*\\.[a|jp|i|d|m|p|pp|o|s|c|pd|PD|D|S|t|b|x|f|n|g|v|r|y|(0-9)]+$"));
}
public static List<String> readURLS() throws Exception{
	Path path = Paths.get("./OutputFiles/urlMap.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}
public static List<String> myRead(String filename) throws Exception{
    
	FileReader f=new FileReader(filename);
	BufferedReader br=new BufferedReader(f);
	String s;
	List<String> lines=new ArrayList<String>();
	s=br.readLine();
	int c=0;
	while(s!=null){
	 try{
	   c++;
	   lines.add(s);
	   s=br.readLine();
	 }
	 catch(Exception e){
		 System.out.println("Error on line : "+s);
	 }
	}
	System.out.println(c+" lines read from file : "+filename);
	br.close();
	return lines;	
}

}
class UrlComparator1 implements Comparator<UrlIC>
{
    @Override
    public int compare(UrlIC x, UrlIC y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if(x.getLevel() < y.getLevel())
    	   return -1;
    	else if(x.getLevel() > y.getLevel())
     	   return 1;
    	else{
	    	if (x.getIc() > y.getIc())
	        {
	            return -1;
	        }
	        else if (x.getIc() < y.getIc())
	        {
	            return 1;
	        }
	        else{
	        	if(x.getTime() < y.getTime())
	        		return -1;
	        	else if(x.getTime() > y.getTime())
	        		return 1;
	        }
    	}
        
        return 0;
    }
}
