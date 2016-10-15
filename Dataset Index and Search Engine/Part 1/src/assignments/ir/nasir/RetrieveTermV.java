package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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


public class RetrieveTermV {
	public String jsonResponse;
	public MyActionListener listener;
	public Node node;
	public Client client;
	public RetrieveTermV() {
	 listener=new MyActionListener();
	 node=nodeBuilder().clusterName("irnasir").client(true).node();
	 client=node.client();
	}
public static void main(String a[]) throws IOException, JSONException, InterruptedException{
   int sum_doc_freq,doc_count,sum_ttf,df,ttf,tf,docLength;
   long stime,etime;
   
   RetrieveTermV rv=new RetrieveTermV();
   StartElasticSearch startE=new StartElasticSearch(rv.node,rv.client);
   
   
   //String docs[]={"AP890101-0001","AP890101-0002"};
   List<String> docs=rv.readDocList();
   List<String> docs1=docs.subList(0, docs.size()/2);
   List<String> docs2=docs.subList(docs.size()/2, docs.size());
   stime=System.currentTimeMillis();
   
   // To loop between all the documents in the apdataset
   System.out.println("Processing Search.... Please wait");
   for(String d:docs){
	   startE.startBuild(d);
   }
	//   Thread.sleep(20);
	  
	   //rv.buildTermVectors("AP890101-0002");
	   /*System.out.println("Term Vector Details\n----------------");
	   String termVectors=rv.getAllTermVector(startE.listener.getJsonResponse()) ;
	   JSONObject jTermVectors=rv.stringToJson(termVectors);
	 
	   // field_statistics
	   JSONObject jFieldStatistics=rv.getFieldStatistics(jTermVectors);
	   sum_doc_freq=rv.getSum_Doc_Freq(jFieldStatistics);
	   doc_count=rv.getDoc_Count(jFieldStatistics);
	   sum_ttf=rv.getSum_Ttf(jFieldStatistics);
	   System.out.println("sum_doc_freq = "+sum_doc_freq);
	   System.out.println("doc_count = "+doc_count);
	   System.out.println("sum_ttf = "+sum_ttf);
	   // end of field statistics
	   
	   // term statistics
	   JSONObject jTerms=rv.getTerms(jTermVectors);
	   String term;
	   if(d.equals("AP890101-0001"))
		   term="celluloid";
	   else
		   term="factori";
	   df=rv.getTerm_Doc_Freq(term,jTerms);
	   ttf=rv.getTerm_Ttf(term,jTerms);
	   tf=rv.getTerm_Tf(term,jTerms);
	   System.out.println("\nFor Term : "+ term+ "\n");
	   System.out.println("doc_freq (df) = "+df);
	   System.out.println("ttf = "+ttf);
	   System.out.println("term_freq (tf) = "+tf);
	   // end of term statistics
	   
	   // all terms in the document
	   String allTerms[]=JSONObject.getNames(jTerms);
	   for(String s:allTerms)
		   System.out.println(s);
	   // end of all terms in the document
	   
	   // fetching the document length
	   docLength=0;
	   for(String s:allTerms){
		   tf=rv.getTerm_Tf(s, jTerms);
		   docLength=docLength+tf;
	   }
	   
	   System.out.println("Document length (dL)= "+docLength);
	   
   }*/
   etime=System.currentTimeMillis();
   rv.writeData1(startE.data);
   System.out.println("Total Time(Seconds) taken for Search = "+(etime-stime)/1000);
   startE.closeAllResources();
   
}
public String getAllTermVector(String jsonResponse) {
System.out.println("<< JSON Response before formatting >>\n"+jsonResponse);
String formattedJsonResponse=jsonResponse.substring(jsonResponse.indexOf("{"));
System.out.println("<< JSON Response after formatting >>\n"+formattedJsonResponse);
System.out.println(formattedJsonResponse);
return formattedJsonResponse;
}

public JSONObject stringToJson(String s) throws JSONException{
	JSONObject jobj=new JSONObject(s);
	return jobj;
}

public JSONObject getFieldStatistics(JSONObject jTermVectors) throws JSONException{
	JSONObject jFieldStatistics=jTermVectors.getJSONObject("text").getJSONObject("field_statistics");
	return jFieldStatistics;
}

public int getSum_Doc_Freq(JSONObject fieldStats) throws JSONException{
	return fieldStats.getInt("sum_doc_freq");
}

public int getDoc_Count(JSONObject fieldStats) throws JSONException{
	return fieldStats.getInt("doc_count");
}

public int getSum_Ttf(JSONObject fieldStats) throws JSONException{
	return fieldStats.getInt("sum_ttf");
}

public JSONObject getTerms(JSONObject jTermVectors) throws JSONException{
	JSONObject jTerms=jTermVectors.getJSONObject("text").getJSONObject("terms");
	return jTerms;
}

public int getTerm_Doc_Freq(String term, JSONObject jTerms) throws JSONException{
	return jTerms.getJSONObject(term).getInt("doc_freq");
}

public int getTerm_Ttf(String term, JSONObject jTerms) throws JSONException{
	return jTerms.getJSONObject(term).getInt("ttf");
}

public int getTerm_Tf(String term, JSONObject jTerms) throws JSONException{
	return jTerms.getJSONObject(term).getInt("term_freq");
}
List<String> readDocList() throws IOException {
    Path path = Paths.get("./OutputFiles/docnoslist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

void writeData1(List<String> strLines) throws IOException {
    Path path = Paths.get("./OutputFiles/data1.txt");
    Files.write(path, strLines, StandardCharsets.UTF_8);
}

/*public void buildTermVectors(String docId){
	System.out.println("listener value before execute "+listener.getJsonResponse());
	TermVectorRequestBuilder builder=new TermVectorRequestBuilder(StartElasticSearch.client, "apdataset", "document", docId);
	builder.setTermStatistics(true).setFieldStatistics(true);
	builder.execute(listener);	
	System.out.println("listener value after execute "+listener.getJsonResponse());
}*/
}
