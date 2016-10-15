package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.json.JSONException;

public class RetrieveDocLen {
	public String jsonResponse;
	public MyActionListener listener;
	public Node node;
	public Client client;
	public RetrieveDocLen() {
	;
	}
public static void main(String a[]) throws IOException, JSONException, InterruptedException{
   int sum_doc_freq,doc_count,sum_ttf,df,ttf,tf,docLength;
   long stime,etime;
   System.out.println("Computing Document Lengths and TTFs of all words... Please wait for about 3 min...");
   PrepareQueries prepQ=new PrepareQueries();
   HashMap<String,String> filteredQ= prepQ.getFilteredQueries();
   RetrieveDocLen rv=new RetrieveDocLen();

   rv.node=nodeBuilder().clusterName("irnasir").client(true).node();
   rv.client=rv.node.client();
   stime=System.currentTimeMillis();
   //String docs[]={"AP890101-0001","AP890101-0002"};
   int parts=5;
   List<String> docs=rv.readDocList();
   List<String> docs1=docs.subList(0, docs.size()/parts);
   List<String> docs2=docs.subList(docs.size()/parts, (2*docs.size())/parts);
   List<String> docs3=docs.subList((2*docs.size())/parts,(3*docs.size())/parts);
   List<String> docs4=docs.subList((3*docs.size())/parts,(4*docs.size())/parts);
   List<String> docs5=docs.subList((4*docs.size())/parts,(5*docs.size())/parts);
   //List<String> docs6=docs.subList((5*docs.size())/parts,docs.size());
   /*List<String> docs7=docs.subList((6*docs.size())/parts,(7*docs.size())/parts);
   List<String> docs8=docs.subList((7*docs.size())/parts,(8*docs.size())/parts);
   List<String> docs9=docs.subList((8*docs.size())/parts,(9*docs.size())/parts);
   List<String> docs10=docs.subList((9*docs.size())/parts,docs.size());*/
   StartElasticSearchDocLenThread startThread1=new StartElasticSearchDocLenThread(rv.node,rv.client,docs1);
   StartElasticSearchDocLenThread startThread2=new StartElasticSearchDocLenThread(rv.node,rv.client,docs2);
   StartElasticSearchDocLenThread startThread3=new StartElasticSearchDocLenThread(rv.node,rv.client,docs3);
   StartElasticSearchDocLenThread startThread4=new StartElasticSearchDocLenThread(rv.node,rv.client,docs4);
   StartElasticSearchDocLenThread startThread5=new StartElasticSearchDocLenThread(rv.node,rv.client,docs5);

   /*StartElasticSearchThread startThread7=new StartElasticSearchThread(rv.node,rv.client,docs7);
   StartElasticSearchThread startThread8=new StartElasticSearchThread(rv.node,rv.client,docs8);
   StartElasticSearchThread startThread9=new StartElasticSearchThread(rv.node,rv.client,docs9);
   StartElasticSearchThread startThread10=new StartElasticSearchThread(rv.node,rv.client,docs10);*/
   // To loop between all the documents in the apdataset
   System.out.println("Computing Document Lengths for all documents....This may take about 5 mintues...\nPlease wait....");
   startThread1.t.join();
   startThread2.t.join();
   startThread3.t.join();
   startThread4.t.join();
   startThread5.t.join();


   int finalTotalDocLen=startThread1.totalDocLen+startThread2.totalDocLen+startThread3.totalDocLen+startThread4.totalDocLen
		                +startThread5.totalDocLen;
   List<String> finalData=new ArrayList<String>(startThread1.data);
   finalData.addAll(startThread2.data);
   finalData.addAll(startThread3.data);
   finalData.addAll(startThread4.data);
   finalData.addAll(startThread5.data);
   finalData.add("Total_Doc_Len#"+finalTotalDocLen);
   rv.writeData1(finalData);
   
   HashMap<String, Long> ttfFinal=new HashMap<String,Long>(startThread1.ttfdata);
   ttfFinal.putAll(startThread2.ttfdata);
   ttfFinal.putAll(startThread3.ttfdata);
   ttfFinal.putAll(startThread4.ttfdata);
   ttfFinal.putAll(startThread5.ttfdata);
   rv.writeTTF(ttfFinal);
   
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
    Path path = Paths.get("./OutputFiles/docLenList.txt");
    Files.write(path, strLines, StandardCharsets.UTF_8);
}

void writeTTF(HashMap<String,Long> ttfFinal) throws IOException {
	List<String> ttfFinalList=new ArrayList<String>();
	for(String key:ttfFinal.keySet()){
		ttfFinalList.add(key+"#"+ttfFinal.get(key));
	}
    Path path = Paths.get("./OutputFiles/ttfFinalList.txt");
    Files.write(path, ttfFinalList, StandardCharsets.UTF_8);
}

public HashMap<String,String> getDocLenDetails() throws IOException{
	List<String> rawDocLenFile=readDocLenList();
	/*for(String q:rawDocLenFile)
		System.out.println(q);*/
	HashMap<String,String> docLens=new HashMap<String, String>();
	for(String s:rawDocLenFile){
		String splits[]=s.split("#");
		docLens.put(splits[0], splits[1]);
	}	
	return docLens;
}

List<String> readDocLenList() throws IOException {
    Path path = Paths.get("./OutputFiles/docLenList.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

public HashMap<String,Long> getTTFDetails() throws IOException{
	List<String> rawTTFFile=readTTFList();
	/*for(String q:rawDocLenFile)
		System.out.println(q);*/
	HashMap<String,Long> ttfList=new HashMap<String, Long>();
	for(String s:rawTTFFile){
		String splits[]=s.split("#");
		ttfList.put(splits[0], Long.parseLong(splits[1]));
	}	
	return ttfList;
}

List<String> readTTFList() throws IOException {
    Path path = Paths.get("./OutputFiles/ttfFinalList.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

}
