package ir.a2.nasir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTokenizer {
	public HashMap<Long,Integer> vocTtf;
	public HashMap<String,Long> uniqueWords;
	public HashMap<Integer,String> docNameMap;
	public TreeMap<Integer,String> docTuples;
	public long termIdCounter;
	public int  docIdCounter;
	public MyTokenizer(){
		vocTtf=new HashMap<Long,Integer>();
	    termIdCounter=0;
	    docIdCounter=0;
	    uniqueWords=new HashMap<String,Long>();
	    docNameMap=new HashMap<Integer,String>();
	    docTuples=new TreeMap<Integer,String>();
	}
public void tokenize(String text,List<String> stopWords,String docName){

 	docIdCounter++;
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(text);
	int posCounter=0;
	int wordCounter=0;
	List<String> sArray=new ArrayList<String>();
	PorterStemmer stemmer = new PorterStemmer();
	
	StringBuffer tupleBuffer=new StringBuffer("");
	while(mat.find()){
		String s=mat.group();
		if(s.matches("[A-Z][\\.][A-Z]"))
	    	s=s+".";
		s=s.toLowerCase();
		
		// <<<< REMOVE "&& !stopWords.contains(s)" FROM BELOW IF TO INCLUDE STOPWORDS IN INDEX >>>>
	    if(s.length()!=1 && !stopWords.contains(s)){
			posCounter++;
			wordCounter++;
			
			// <<<< COMMENT THE BELOW LINE TO UNSTEM THE TERMS THOSE ARE INDEXED >>>>
	    	s=stemmer.stem(s);  
	    	
		    if(!uniqueWords.containsKey(s)){
			    termIdCounter++;	
			    uniqueWords.put(s,termIdCounter);
			    tupleBuffer.append(termIdCounter+","+docIdCounter+","+posCounter+"#");
			    vocTtf.put(termIdCounter, 1);
		    }
		    else{
		    	    long termId=uniqueWords.get(s);
		    		tupleBuffer.append(termId+","+docIdCounter+","+posCounter+"#");
		    		vocTtf.put(termId, vocTtf.get(termId)+1);
		    		
		    }
		}
   }
	//System.out.println(text);
	//sArray.removeAll(stopWords);
	// creating docMaps
	
	docNameMap.put(docIdCounter, docName+"#"+wordCounter);
	docTuples.put(docIdCounter, new String(tupleBuffer));
	
}
}
