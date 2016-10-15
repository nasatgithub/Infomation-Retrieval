package ir.assignment2.nasir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTokenizer {
	public HashMap<String,Integer> vocTtf;
	public MyTokenizer(){
		vocTtf=new HashMap<String,Integer>();
	}
public HashMap<String,Integer> tokenize(String text,List<String> stopWords){
	//text=text.replaceAll("  +", "");
	//text=text.trim();
	//String[] splits=text.split("[^A-Za-z0-9.]+");
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(text);
	Pattern p=Pattern.compile("^[0-9]+[A-Za-z]+[A-Za-z]*$");
	Matcher m;
	HashSet<String> voc=new HashSet<String>();
	int pos;
	List<String> sArray=new ArrayList<String>();
	PorterStemmer stemmer = new PorterStemmer();
	while(mat.find()){
		sArray.add(mat.group());
		//System.out.println(mat.group());
	}
	//System.out.println(text);
	for(String s:sArray){
		    if(s.matches("[A-Z][\\.][A-Z]"))
		    	s=s+".";
		    s=s.toLowerCase();
		   /* if((s.indexOf(".")==(pos=s.length()-1)) && (s.lastIndexOf(".")==s.length()-1) && pos!=-1){   // to remove last full stop
		        //System.out.println("<<"+s+">>");
		    	s=removeLastStop(s);
		    }*/
		  /*  m=p.matcher(s);          this is to remove extra letters after a number 1960s
		    if(m.find())
		    	s=s.replaceAll("[A-Za-z]", "");*/
		    if(s.length()!=1 && !(stopWords.contains((String)s))){
		    	s=stemmer.stem(s);
		    	//voc.add(s);
		    	if(vocTtf.containsKey(s))
		    		vocTtf.put(s, vocTtf.get(s)+1);
		    	else
		    		vocTtf.put(s, 1);
		    		
		    }
			 
	}
	return vocTtf;
}
public String removeLastStop(String word){
	return (word.substring(0, word.length()-1).trim());
}
/*public String formatNumeric(String word){
	
	
}*/
}
