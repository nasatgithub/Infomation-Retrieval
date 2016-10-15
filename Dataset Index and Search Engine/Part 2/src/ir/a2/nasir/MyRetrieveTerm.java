package ir.a2.nasir;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class MyRetrieveTerm {
	public String jsonResponse;
	List<String> okapiFinalResult;
	List<String> tfidfFinalResult;
	List<String> laplaceFinalResult;
	List<String> bm25FinalResult;
	List<String> jmFinalResult;
	List<String> proxFinalResult;
	HashMap<String,String> filteredQ;
	TreeMap<Integer, String> docTreeMap;
	long totaldocLen;
	HashSet<Integer> docList;
	public MyRetrieveTerm() {

	 okapiFinalResult=new ArrayList<String>();
	 tfidfFinalResult=new ArrayList<String>();
	 laplaceFinalResult=new ArrayList<String>();
	 bm25FinalResult=new ArrayList<String>();
	 jmFinalResult=new ArrayList<String>();
	 proxFinalResult=new ArrayList<String>();
	 docList=new HashSet<Integer>();
	 totaldocLen=0;
	}
public static void main(String a[]) throws IOException, InterruptedException{
   int sum_doc_freq,doc_count,sum_ttf,df,ttf,tf,docLength;
   long stime,etime;
  
   MyRetrieveTerm rv=new MyRetrieveTerm();
   PrepareQueries prepQ=new PrepareQueries();

   rv.filteredQ= prepQ.getFilteredQueries();
   rv.docTreeMap=rv.readDocMap(); 
   HashMap<String,Long> termMap=rv.getTermMap();
   HashMap<Long,Integer> ttfMap=rv.getTtfMap();
   System.out.println("Processing Search.... Please wait..");
   stime=System.currentTimeMillis();
   MyStartQuickSearch start=new MyStartQuickSearch(rv.docTreeMap,termMap,ttfMap,rv.filteredQ,rv.docList,rv.totaldocLen);
   start.processSearch();
   System.out.println("Searching Done Successfully..\nRanking Pages Now.. Please wait for some moment");
   
   String groupR1="#1";
   String groupR2="#84678";
   // BLOCK TO PREPARE OKAPI-TF FINAL DOCUMENT
   TreeMap<String, Double> okapiTMapCombined=start.okapiTMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sOkapi=okapiTMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sOkapi = SortByValue(sOkapi); 
	   rv.addToOkapiFinal(okapiTMapCombined,sOkapi,queryKey);
	   
   }
   rv.writeModelsFinal("okapiThreaded.txt",rv.okapiFinalResult);
   // END OF BLOCK 
   System.out.println("Done with Okapi- Tf Scoring");
   // BLOCK TO PREPARE TF-IDF FINAL DOCUMENT
   TreeMap<String, Double> tfidfTMapCombined=start.tfidfTMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sTfidf=tfidfTMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sTfidf = SortByValue(sTfidf); 
	   rv.addToTfidfFinal(tfidfTMapCombined,sTfidf,queryKey);
	   
   }
   rv.writeModelsFinal("tfidfThreaded.txt",rv.tfidfFinalResult);
   // END OF BLOCK 
   System.out.println("Done with Okapi- Tf Idf Scoring");
   // BLOCK TO PREPARE BM25 FINAL DOCUMENT
   TreeMap<String, Double> bm25TMapCombined=start.bm25TMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sBM25=bm25TMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sBM25 = SortByValue(sBM25); 
	   rv.addToBM25Final(bm25TMapCombined,sBM25,queryKey);
	   
   }
   rv.writeModelsFinal("bm25Threaded.txt",rv.bm25FinalResult);
   // END OF BLOCK 
   System.out.println("Done with Bm25 Scoring");
   
   
   // BLOCK TO PREPARE Laplace FINAL DOCUMENT
   /*TreeMap<String, Double> laplaceTMapCombined=start.laplaceTMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sLaplace=laplaceTMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sLaplace = SortByValue(sLaplace); 
	   rv.addToLaplaceFinal(laplaceTMapCombined,sLaplace,queryKey);
	   
   }
   rv.writeModelsFinal("laplaceThreaded.txt",rv.laplaceFinalResult);*/
   // END OF BLOCK 
   
   // BLOCK TO PREPARE JM Smoothing FINAL DOCUMENT
   TreeMap<String, Double> jmTMapCombined=start.jmTMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sJM=jmTMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sJM = SortByValue(sJM); 
	   rv.addToJMFinal(jmTMapCombined,sJM,queryKey);
	   
   }
   rv.writeModelsFinal("jmThreaded.txt",rv.jmFinalResult);
   // END OF BLOCK 
   System.out.println("Done with JM Scoring");
   
   // BLOCK TO PREPARE Proximity FINAL DOCUMENT
   TreeMap<String, Double> ProxTMapCombined=start.proxTMap;
   for(String queryKey: rv.filteredQ.keySet())
   {
	   SortedMap<String,Double> sProx=ProxTMapCombined.subMap(queryKey+groupR1, true, queryKey+groupR2, true);
	   sProx = SortByValue(sProx); 
	   rv.addToProxFinal(ProxTMapCombined,sProx,queryKey);
	   
   }
   rv.writeModelsFinal("proximityThreaded.txt",rv.proxFinalResult);
   // END OF BLOCK 
   System.out.println("Done with Proximity Scoring");
   
   etime=System.currentTimeMillis();
   System.out.println("Total Time(Seconds) taken for Search = "+(etime-stime)/1000);
}



void writeData1(List<String> strLines) throws IOException {
    Path path = Paths.get("./OutputFiles/data1.txt");
    Files.write(path, strLines, StandardCharsets.UTF_8);
}

public static TreeMap<String, Double> SortByValue 
(SortedMap<String, Double> map) {
ValueComparator1 vc =  new ValueComparator1(map);
TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
sortedMap.putAll(map);
return sortedMap;
}

public void addToOkapiFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=1000)
		okapiFinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}


public void addToTfidfFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=100)
	      tfidfFinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

public void addToBM25Final(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=100)
	      bm25FinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

public void addToLaplaceFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=100)
	      laplaceFinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}
	
public void addToJMFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=100)
		 jmFinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

public void addToProxFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	String docName;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		String[] docMapSplits=docTreeMap.get(Integer.parseInt(splits[1])).split("#");
		docName=docMapSplits[0];
		if(i<=1000)
	      proxFinalResult.add(splits[0]+" Q0 "+docName+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}
	
void writeModelsFinal(String filename,List<String> strLines) throws IOException {
    Path path = Paths.get("./OutputFiles/"+filename);
    Files.write(path, strLines, StandardCharsets.UTF_8);
}

public TreeMap<Integer,String> readDocMap() throws IOException {
    Path path = Paths.get("./OutputFiles/docMap.txt");
    List<String> docMapList=Files.readAllLines(path, StandardCharsets.UTF_8);
    HashMap<Integer,String> docHashMap=new HashMap<Integer,String>();
    for(String doc:docMapList)
    {
    	String[] splits=doc.split("#");
    	docHashMap.put(Integer.parseInt(splits[0]), splits[1]+"#"+splits[2]);
    	docList.add(Integer.parseInt(splits[0]));
    	totaldocLen=totaldocLen+Integer.parseInt(splits[2]);
    	
    }
    TreeMap<Integer, String> docTreeMap=new TreeMap<Integer,String>(docHashMap);
    return docTreeMap;
}
public HashMap<String, Long> getTermMap() throws IOException{
	Path path = Paths.get("./OutputFiles/termMap.txt");
    List<String> termMapList=Files.readAllLines(path, StandardCharsets.UTF_8);
    HashMap<String, Long> termHashMap=new HashMap<String, Long>();
    for(String t:termMapList)
    {
    	String[] splits=t.split("#");
    	termHashMap.put(splits[1],Long.parseLong(splits[0]));
    }
    
    return termHashMap;
}
public HashMap<Long, Integer> getTtfMap() throws IOException{
	Path path = Paths.get("./OutputFiles/termMap.txt");
    List<String> termMapList=Files.readAllLines(path, StandardCharsets.UTF_8);
    HashMap<Long, Integer> ttfHashMap=new HashMap<Long, Integer>();
    for(String t:termMapList)
    {
    	String[] splits=t.split("#");
    	//System.out.println(Long.parseLong(splits[0])+Integer.parseInt(splits[2]));
    	ttfHashMap.put(Long.parseLong(splits[0]),Integer.parseInt(splits[2]));
    }
    return ttfHashMap;
}
}

class ValueComparator1 implements Comparator<String> {
	 
    Map<String, Double> map;
 
    public ValueComparator1(Map<String, Double> base) {
        this.map = base;
    }
 
    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }

}




