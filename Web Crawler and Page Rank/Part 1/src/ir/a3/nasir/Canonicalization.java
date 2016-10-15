package ir.a3.nasir;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Canonicalization {
public static void main(String a[]) throws Exception{
	Canonicalization c=new Canonicalization();
	String url1="HTTP://www.Example.Com/SomeFile.html";
	String url2="http://www.example.com:80";
	String url3="http://www.example.com/a.html#anything";
	String url4="http://www.example.com//a.html";
	String url5="http://www.example.com/a.html/b.html";
	String url6="http://en.wikipedia.org/w/index.php?title=Special:RecentChanges&feed=atom";
	System.out.println(c.canonicalize("http://articles.boston.com/2007-11-21/news/29233371_1_barack-obama-education-plan-campaign-trail"));
	List<String> urls=new ArrayList<String>();
	urls.add("http://en.wikipedia.org/wiki/Barack_Obama");
	urls.add("http://obamacarefacts.com/obamacare-facts");
	urls.add("http://en.wikipedia.org/wiki/Patient_Protection_and_Affordable_Care_Act");
	urls.add("http://articles.boston.com/2007-11-21/news/29233371_1_barack-obama-education-plan-campaign-trail");
	urls.add(url1);urls.add(url2);urls.add(url3);
	HashSet<String> hs=c.getUrlQueries(urls);
	for(String s: hs)
		System.out.println(s);
	System.out.println("---------");
	for(String s : urls){
		System.out.println(canonicalize(s));
	}
	
}
public static String canonicalize(String url) throws Exception{
 //System.out.println("URL "+url);
 if(!url.isEmpty()){
	 URL myURL=new URL(url);
	 StringBuffer formatURL=new StringBuffer("");
	 formatURL.append(myURL.getProtocol().toLowerCase()+"://");
	 formatURL.append(myURL.getHost().toLowerCase());
	 String path=myURL.getPath();
	 path=path.replaceAll("//", "/");
	 formatURL.append(path);
	 String q;
	 if((q=myURL.getQuery())!=null)
	 formatURL.append("?"+q);
	 //System.out.println(myURL.getHost());
	 return new String(formatURL);
 }
 else
	 return "#INVALID_URL";
}
public static HashSet<String> getUrlQueries(List<String> urls){
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9/]+)*");
	Matcher mat;
	HashSet<String> searchTerms=new HashSet<String>();
	HashSet<String> stopW=new HashSet<String>();
	stopW.add("and");
	stopW.add("facts");
	stopW.add("affordable");
	stopW.add("care");
	for(String url: urls){
	   String queries=url.substring(url.lastIndexOf("/")+1,url.length());
	   mat=pat.matcher(queries);
	   while(mat.find()){
		   String w=mat.group().toLowerCase();
		   if(!stopW.contains(w))
		   searchTerms.add(w);
	   }	   
	}
	return searchTerms;
}
}
