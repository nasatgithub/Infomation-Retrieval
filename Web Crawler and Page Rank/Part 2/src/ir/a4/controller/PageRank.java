package ir.a4.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PageRank {
private static HashMap<String,NodeBean> pageData;
private static HashMap<String,Double> simplePageData;
private static HashSet<String> sinkNodes;
private static 	int NumberOfPages;
private static List<String> finalPageRanks;
public static void main(String args[])throws Exception{
	pageData=new HashMap<String, NodeBean>();
	sinkNodes=new HashSet<String>();
	finalPageRanks=new ArrayList<String>();
	simplePageData=new HashMap<String, Double>();
	readAdjacency();
	computePageRank();
	displayPageRanked();
}
public static void readAdjacency() throws IOException{
	FileReader f=new FileReader("Files/testInlinks.txt");
	BufferedReader br=new BufferedReader(f);
	String line;
	System.out.println("Computing Page Ranks .. Please Wait...");
	while((line=br.readLine())!=null){
		//System.out.println(line);
		String[] splits=line.split(" ");
		String page=splits[0];
		if(!pageData.containsKey(page))
			pageData.put(page, new NodeBean());
		NodeBean n=pageData.get(page);
		List<String> inlinks=new ArrayList<String>();
		for(int i=1;i<splits.length;i++){
			String pageInner=splits[i];
			inlinks.add(pageInner);
			
			if(!pageData.containsKey(pageInner))
				pageData.put(pageInner, new NodeBean());
			NodeBean nInner=pageData.get(pageInner);
			nInner.addOutlink(page);
		}
		if(!inlinks.isEmpty())
			n.addInlinks(inlinks);
	}
	NumberOfPages=pageData.size();
	for(String page: pageData.keySet()){
		NodeBean n=pageData.get(page);
		n.setPageRank((double)1/NumberOfPages);
		if(n.getOutlinks().isEmpty())
		 sinkNodes.add(page);
	}
	System.out.println("Num of Pages = "+pageData.size());
	System.out.println("Sink Nodes = "+ sinkNodes.size());
	
}
public static void computePageRank(){
	double sinkPR;
	double d=0.15;
	boolean converged=false;
	int iter=0;
	while(true){
		iter++;
		sinkPR=0;
		for(String page: sinkNodes){
			sinkPR=sinkPR+pageData.get(page).getPageRank();
		}
		
		double newPr=0;
		HashSet<String> pageInlinks;
		NodeBean n;
		for(String page: pageData.keySet()){
			
			n=pageData.get(page);
			double currentPr=n.getPageRank();
			pageInlinks=n.getInlinks();
			newPr=(double)(1-d)/NumberOfPages;
			newPr = newPr + (d*sinkPR/NumberOfPages);
			for(String pageInlink: pageInlinks){
				NodeBean nInlink=pageData.get(pageInlink);
				newPr = newPr + (d * ( nInlink.getPageRank() / nInlink.getOutlinks().size()));
			}
			//System.out.println("newPR "+page+"   "+newPr );
			if(currentPr==newPr){
		       converged=true;		
			}
			else{
				n.setPageRank(newPr);
				converged=false;
			}
		}
		if(converged)
			break;
	}
	System.out.println("iter :"+iter);
	createSimplePageDataMap();
}
public static void displayPageRanked() throws Exception{
	//System.out.println("PAGE RANKS : ");
	
	Comparator<String> vc = new ValueComparator(simplePageData);
	TreeMap<String,Double> sortedPageRank=new TreeMap<String, Double>(vc);
	sortedPageRank.putAll(simplePageData);
	
	for(String page: sortedPageRank.keySet()){
		//System.out.println(page+"\t"+sortedPageRank.get(page).getPageRank());
		finalPageRanks.add(page.trim()+" "+pageData.get(page).getPageRank());
	}
	
	if(!finalPageRanks.isEmpty())
	 writePRToFile();
}
public static void writePRToFile() throws IOException{
	  System.out.println("Writing Page Ranks To File.. Please wait");
	  Path path = Paths.get("Files/PageRanks.txt");
	  Files.write(path, finalPageRanks, StandardCharsets.UTF_8);
	  System.out.println("FILE ( Files/PageRanks.txt ) WRITE SUCCESSFUL !!! ");
}
public static void createSimplePageDataMap(){
	for(String page: pageData.keySet()){
		simplePageData.put(page,pageData.get(page).getPageRank());
	}
}
}

class ValueComparator implements Comparator<String>
{
	HashMap<String,Double> sPageData;
	public ValueComparator(HashMap<String,Double> sPageData) {
		this.sPageData=sPageData;
	}
    @Override
    public int compare(String x, String y)
    {    //System.out.println(pageData.get(x).getPageRank()+ "  "+pageData.get(y).getPageRank());
    	 if(sPageData.get(x)>= sPageData.get(y))
     	   return -1;
     	 else 
      	   return 1;	 
    }
}

