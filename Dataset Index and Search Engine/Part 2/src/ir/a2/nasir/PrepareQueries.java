package ir.a2.nasir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrepareQueries {
	
public static void main(String a[]) throws IOException{
	PrepareQueries pq=new PrepareQueries();
	HashMap<String,String> filteredQueries=pq.getFilteredQueries();
	for(String key:filteredQueries.keySet())
		 System.out.println(key+"#"+filteredQueries.get(key));
	
	
}
public HashMap<String,String> getFilteredQueries() throws IOException{
	List<String> orgQueries=readQueries();
	List<String> stopWordsList=readStopWords();
	String qwords[];
	StringBuffer filteredQwords=new StringBuffer("");
	int wcount=0;
	int stopWordFlag=0;
	String queryKey;
	HashMap<String, String> filtered=new HashMap<String,String>();
	for(String qw: orgQueries){
		
		filteredQwords=new StringBuffer("");
		qw=formatQuery(qw);
		qwords=qw.split(" ");
		queryKey=qwords[0].replace(".", "");
		for(int i=4;i<qwords.length;i++){
		   stopWordFlag=0;
		   if(i==4)
		   {
			   if(qwords[i].equals("or")){
				   i++;
				   continue;
			   }     
		   }
		   String qword=qwords[i];		  
		   if(!stopWordsList.contains(qword)&&qword.length()!=1){			 
			    filteredQwords.append(qword.trim().toLowerCase()+" ");
			   
		   }
		   	  
	  }
	  filtered.put(queryKey,new String(filteredQwords));
	 	
	}

     return filtered;	
}

public String formatQuery(String query){
	
		query=query.replaceAll("-", " ");
		query=query.replaceAll("\"", "");
		query=query.replaceAll(",", "");
		query=query.replaceAll("  +", " ");
		query=query.replaceAll("[()]", "");
		query=query.replaceAll("'", " ");
		
		if(query.substring(5).lastIndexOf(".")!=-1)
			query=query.substring(0,query.lastIndexOf("."));
		
		return query;
}

public List<String> readQueries() throws IOException{
	Path path = Paths.get("./Queries/query_desc.51-100.short.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

public List<String> readStopWords() throws IOException{
	Path path = Paths.get("./Stopwords/stoplist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

public static int countWordOccurance(String original, String term){
	original=original.toLowerCase();
	term=term.toLowerCase();
	String splits[]=original.split(" ");
	HashMap<String, Integer> countMap=new HashMap<String,Integer>();
	for(String s:splits){
		if(countMap.containsKey(s))
			countMap.put(s, countMap.get(s)+1);
		else
			countMap.put(s, 1);
	}
	return countMap.get(term);
}
}
