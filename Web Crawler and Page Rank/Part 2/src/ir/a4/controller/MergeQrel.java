package ir.a4.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public class MergeQrel {
private static TreeMap<String,Integer> finalUrls;
private static List<String> finalMergedContent;
private static HashSet<String> urlSet;
private static List<String> combinedUrlMap;
public MergeQrel(){
	
}
public static void main(String args[]) throws Exception{
	//MergeQrel mq=new MergeQrel();
	//mergeQrel();
	urlSet=new HashSet<String>();
	mergeUrlMap("Files/urlMap.txt");
	mergeUrlMap("Files/esPages.txt");
	//mergeUrlMap("Files/inlinks_parallel_6.txt");
	combinedUrlMap=new ArrayList<String>(urlSet);
	writContentToFile();
	
}
public static void mergeQrel()throws Exception{
	finalUrls=new TreeMap<String, Integer>();
	List<String> filenames=new ArrayList<String>();
    finalMergedContent=new ArrayList<String>();
	filenames.add("Files/qrel-web-crawled.txt");
	filenames.add("Files/qrel_amod.txt");
	FileReader f=null;
	BufferedReader br=null;
	for(String filename: filenames){
		f=new FileReader(filename);
		br=new BufferedReader(f);
		String line;
	    while((line=br.readLine())!=null){
	    	String[] splits=line.split("\t");
	    	String qid=splits[0].trim();
	    	String url=splits[2].trim();
	    	int grade=Integer.parseInt(splits[3]);
	    	//System.out.println(qid+"#"+grade);
	    	if(!finalUrls.containsKey(qid+"#"+url))
	    		finalUrls.put(qid+"#"+url, grade);
	    	else{
	    		int prev_grade=finalUrls.get(qid+"#"+url);
	    		int new_grade=(int)Math.ceil(((double)grade+prev_grade)/2);
	    		finalUrls.put(qid+"#"+url, new_grade);	
	    	}
	    		
	    }
	}
	for(String key: finalUrls.keySet()){
		String[] splits=key.split("#");
		String qid=splits[0];
		String url=splits[1];
		int grade=finalUrls.get(key);
		finalMergedContent.add(qid+"\tNas007\t"+url+"\t"+grade);
		System.out.println(qid+"\tNas007\t"+url+"\t"+grade);
	}
   
	writContentToFile();	
	br.close();
	f.close();
}
public static void mergeUrlMap(String filename) throws Exception{
	
	FileReader f=new FileReader(filename);
	BufferedReader br=new BufferedReader(f);
	String line;
	while((line=br.readLine())!=null){
		String[] splits=line.split(" ");
		urlSet.add(splits[0]);
	}	
	br.close();
	f.close();
}
public static void writContentToFile() throws IOException{
	  System.out.println("Writing Merged Content To File.. Please wait");
	  Path path = Paths.get("Files/MergedUrlMap.txt");
	  Files.write(path, combinedUrlMap, StandardCharsets.UTF_8);
	  System.out.println("FILE (Files/MergedUrlMap.txt) WRITE SUCCESSFUL !!! ");
}
}
