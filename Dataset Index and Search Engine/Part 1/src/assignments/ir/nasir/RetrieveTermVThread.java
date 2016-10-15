package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.node.Node;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.termvector.TermVectorRequestBuilder;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.json.JSONException;
import org.json.JSONObject;


public class RetrieveTermVThread {
	public String jsonResponse;
	public MyActionListener listener;
	public Node node;
	public Client client;
	List<String> okapiFinalResult;
	List<String> tfidfFinalResult;
	List<String> laplaceFinalResult;
	List<String> bm25FinalResult;
	List<String> jmFinalResult;
	public RetrieveTermVThread() {
	 listener=new MyActionListener();
	 node=nodeBuilder().clusterName("irnasir").client(true).node();
	 client=node.client();
	 okapiFinalResult=new ArrayList<String>();
	 tfidfFinalResult=new ArrayList<String>();
	 laplaceFinalResult=new ArrayList<String>();
	 bm25FinalResult=new ArrayList<String>();
	 jmFinalResult=new ArrayList<String>();
	}
public static void main(String a[]) throws IOException, JSONException, InterruptedException{
   int sum_doc_freq,doc_count,sum_ttf,df,ttf,tf,docLength;
   long stime,etime;
  
   PrepareQueries prepQ=new PrepareQueries();
   HashMap<String,String> filteredQ= prepQ.getFilteredQueries();
   RetrieveDocLen rdocLen=new RetrieveDocLen();
   HashMap<String,String> docLens=rdocLen.getDocLenDetails(); 
   HashMap<String,Long> ttfs=rdocLen.getTTFDetails();
   
   RetrieveTermVThread rv=new RetrieveTermVThread();
   stime=System.currentTimeMillis();
   //String docsArray[]={"AP890101-0001","AP890101-0002"};
   int parts=5;
   List<String> docs=rv.readDocList();
   List<String> docs1=docs.subList(0, docs.size()/parts);
   List<String> docs2=docs.subList(docs.size()/parts, (2*docs.size())/parts);
   List<String> docs3=docs.subList((2*docs.size())/parts,(3*docs.size())/parts);
   List<String> docs4=docs.subList((3*docs.size())/parts,(4*docs.size())/parts);
   List<String> docs5=docs.subList((4*docs.size())/parts,(5*docs.size())/parts);
   
   // Creating Threads for different groups of documents
   StartElasticSearchThread startThread1=new StartElasticSearchThread(rv.node,rv.client,docs1,filteredQ,docLens,ttfs);
   StartElasticSearchThread startThread2=new StartElasticSearchThread(rv.node,rv.client,docs2,filteredQ,docLens,ttfs);
   StartElasticSearchThread startThread3=new StartElasticSearchThread(rv.node,rv.client,docs3,filteredQ,docLens,ttfs);
   StartElasticSearchThread startThread4=new StartElasticSearchThread(rv.node,rv.client,docs4,filteredQ,docLens,ttfs);
   StartElasticSearchThread startThread5=new StartElasticSearchThread(rv.node,rv.client,docs5,filteredQ,docLens,ttfs);

   // To loop between all the documents in the apdataset
   System.out.println("Processing Search.... Please wait for about 7.5 min");
   startThread1.t.join();
   startThread2.t.join();
   startThread3.t.join();
   startThread4.t.join();
   startThread5.t.join();
  
   // BLOCK TO PREPARE OKAPI-TF FINAL DOCUMENT
   TreeMap<String, Double> okapiTMapCombined=new TreeMap<String,Double>(startThread1.okapiTMap);
   okapiTMapCombined.putAll(startThread2.okapiTMap);
   okapiTMapCombined.putAll(startThread3.okapiTMap);
   okapiTMapCombined.putAll(startThread4.okapiTMap);
   okapiTMapCombined.putAll(startThread5.okapiTMap);
   
   for(String queryKey: filteredQ.keySet())
   {
	   SortedMap<String,Double> sOkapi=okapiTMapCombined.subMap(queryKey+"#AP890101-0001", true, queryKey+"#AP891231-0048", true);
	   sOkapi = SortByValue(sOkapi); 
	   rv.addToOkapiFinal(okapiTMapCombined,sOkapi,queryKey);
	   
   }
   rv.writeModelsFinal("okapiThreaded.txt",rv.okapiFinalResult);
   // END OF BLOCK 
   
   // BLOCK TO PREPARE TF-IDF FINAL DOCUMENT
   TreeMap<String, Double> tfidfTMapCombined=new TreeMap<String,Double>(startThread1.tfidfTMap);
   tfidfTMapCombined.putAll(startThread2.tfidfTMap);
   tfidfTMapCombined.putAll(startThread3.tfidfTMap);
   tfidfTMapCombined.putAll(startThread4.tfidfTMap);
   tfidfTMapCombined.putAll(startThread5.tfidfTMap);
   
   for(String queryKey: filteredQ.keySet())
   {
	   SortedMap<String,Double> sTfidf=tfidfTMapCombined.subMap(queryKey+"#AP890101-0001", true, queryKey+"#AP891231-0048", true);
	   sTfidf = SortByValue(sTfidf); 
	   rv.addToTfidfFinal(tfidfTMapCombined,sTfidf,queryKey);
	   
   }
   rv.writeModelsFinal("tfidfThreaded.txt",rv.tfidfFinalResult);
   // END OF BLOCK 
   
   // BLOCK TO PREPARE BM25 FINAL DOCUMENT
   TreeMap<String, Double> bm25TMapCombined=new TreeMap<String,Double>(startThread1.bm25TMap);
   bm25TMapCombined.putAll(startThread2.bm25TMap);
   bm25TMapCombined.putAll(startThread3.bm25TMap);
   bm25TMapCombined.putAll(startThread4.bm25TMap);
   bm25TMapCombined.putAll(startThread5.bm25TMap);
   
   for(String queryKey: filteredQ.keySet())
   {
	   SortedMap<String,Double> sBM25=bm25TMapCombined.subMap(queryKey+"#AP890101-0001", true, queryKey+"#AP891231-0048", true);
	   sBM25 = SortByValue(sBM25); 
	   rv.addToBM25Final(bm25TMapCombined,sBM25,queryKey);
	   
   }
   rv.writeModelsFinal("bm25Threaded.txt",rv.bm25FinalResult);
   // END OF BLOCK 
   

   // BLOCK TO PREPARE Laplace FINAL DOCUMENT
   TreeMap<String, Double> laplaceTMapCombined=new TreeMap<String,Double>(startThread1.laplaceTMap);
   laplaceTMapCombined.putAll(startThread2.laplaceTMap);
   laplaceTMapCombined.putAll(startThread3.laplaceTMap);
   laplaceTMapCombined.putAll(startThread4.laplaceTMap);
   laplaceTMapCombined.putAll(startThread5.laplaceTMap);
   
   for(String queryKey: filteredQ.keySet())
   {
	   SortedMap<String,Double> sLaplace=laplaceTMapCombined.subMap(queryKey+"#AP890101-0001", true, queryKey+"#AP891231-0048", true);
	   sLaplace = SortByValue(sLaplace); 
	   rv.addToLaplaceFinal(laplaceTMapCombined,sLaplace,queryKey);
	   
   }
   rv.writeModelsFinal("laplaceThreaded.txt",rv.laplaceFinalResult);
   // END OF BLOCK 
   
   // BLOCK TO PREPARE JM Smoothing FINAL DOCUMENT
   TreeMap<String, Double> jmTMapCombined=new TreeMap<String,Double>(startThread1.jmTMap);
   jmTMapCombined.putAll(startThread2.jmTMap);
   jmTMapCombined.putAll(startThread3.jmTMap);
   jmTMapCombined.putAll(startThread4.jmTMap);
   jmTMapCombined.putAll(startThread5.jmTMap);
   
   for(String queryKey: filteredQ.keySet())
   {
	   SortedMap<String,Double> sJM=jmTMapCombined.subMap(queryKey+"#AP890101-0001", true, queryKey+"#AP891231-0048", true);
	   sJM = SortByValue(sJM); 
	   rv.addToJMFinal(jmTMapCombined,sJM,queryKey);
	   
   }
   rv.writeModelsFinal("jmThreaded.txt",rv.jmFinalResult);
   // END OF BLOCK 
   
   etime=System.currentTimeMillis();
   System.out.println("Total Time(Seconds) taken for Search = "+(etime-stime)/1000);
   startThread1.closeAllResources();
   startThread2.closeAllResources();
   startThread3.closeAllResources();
   startThread4.closeAllResources();
   startThread5.closeAllResources();
}

List<String> readDocList() throws IOException {
    Path path = Paths.get("./OutputFiles/docnoslist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
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
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		if(i<=100)
		okapiFinalResult.add(splits[0].substring(0, splits[0].lastIndexOf("."))+" Q0 "+splits[1]+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}


public void addToTfidfFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		if(i<=100)
	      tfidfFinalResult.add(splits[0].substring(0, splits[0].lastIndexOf("."))+" Q0 "+splits[1]+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

public void addToBM25Final(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		if(i<=100)
	      bm25FinalResult.add(splits[0].substring(0, splits[0].lastIndexOf("."))+" Q0 "+splits[1]+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

public void addToLaplaceFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		if(i<=100)
	      laplaceFinalResult.add(splits[0].substring(0, splits[0].lastIndexOf("."))+" Q0 "+splits[1]+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}
	
public void addToJMFinal(TreeMap<String,Double> orgData,SortedMap<String,Double> sortedData,String queryKey){
	int i=1;
	for(String key: sortedData.keySet()){
		String splits[]=key.split("#");
		if(i<=100)
		 jmFinalResult.add(splits[0].substring(0, splits[0].lastIndexOf("."))+" Q0 "+splits[1]+" "+i+" "+orgData.get(key)+" Exp");
		i++;
	}
}

void writeModelsFinal(String filename,List<String> strLines) throws IOException {
    Path path = Paths.get("./OutputFiles/"+filename);
    Files.write(path, strLines, StandardCharsets.UTF_8);
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




