package ir.a3.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class MergeCrawls2 {
	private static Node node;
	private static Client client;
	public MergeCrawls2(){
		node=nodeBuilder().clusterName("ana").node();
		client=node.client();
	}
public static void main(String a[]) throws Exception{
	MergeCrawls2 mg=new MergeCrawls2();
	mg.merge();
}

public static void merge() throws Exception{
	try{
		
		SearchResponse myResponse,mergeResponse;
		SearchHit mySearchHit,mergeSearchHit;
		List<String> urls=WebCrawler2.myRead("./Files/urlMap.txt");
		int i=0;
		for(String url: urls){		
			try{
			XContentBuilder jsonBuilder=XContentFactory.jsonBuilder();	
			XContentBuilder httpResponseES;
			myResponse=client.prepareSearch("crawlnasir") 
					.setTypes("document")
					.setQuery(QueryBuilders.matchQuery("docno", url))
					.execute()
					.actionGet();
			mySearchHit=myResponse.getHits().getHits()[0];
			List<String> myInlinks=(ArrayList<String>)mySearchHit.getSource().get("inlinks");
			String httpResponse=(String)mySearchHit.getSource().get("rawhtml");
			mergeResponse=client.prepareSearch("anair3") 
						.setTypes("document")
						.setQuery(QueryBuilders.matchQuery("docno", url))
						.execute()
						.actionGet();
			if(mergeResponse.getHits().getTotalHits()>0){
				  System.out.println("Url no: "+(++i)+"   "+url+" found in mergedir3 - UPDATING");
				  mergeSearchHit=mergeResponse.getHits().getHits()[0];
				  httpResponseES = jsonBuilder.startObject() 
										    .field("script","ctx._source.httpResponse = httpResponse") 
											 .startObject("params") 
												.field("httpResponse", httpResponse)
												.endObject()
										  .endObject(); 
				  client.prepareUpdate("anair3", "document", url)
				        .setSource(httpResponseES)
				        .execute()
				        .actionGet();
			}
			else{
				 System.out.println("Url no: "+(++i)+"   "+url+" NOT FOUND in mergedir3 - INSERTING");
				String head=(String)mySearchHit.getSource().get("head");
				String text=(String)mySearchHit.getSource().get("text");
				List<String> myOutlinks=(ArrayList<String>)mySearchHit.getSource().get("outlinks");
				HashMap<String,Object> jsonData=new HashMap<String,Object>();
				jsonData.put("docno", url);
				jsonData.put("head", head);
				jsonData.put("text",text);
				jsonData.put("httpResponse", httpResponse);
				jsonData.put("inlinks", myInlinks);
				jsonData.put("outlinks", myOutlinks);
				
				client.prepareIndex("anair3", "document", url)
					  .setSource(jsonData)
					  .execute() 
					  .actionGet();
			}
			//Thread.sleep(3000);
			}
			catch(Exception e){
				System.out.println("Exception on : "+url);
				e.printStackTrace();
			}
		
		}
	}
	catch(ElasticsearchException ese){
		ese.printStackTrace();
	}
	catch(Exception e){
		e.printStackTrace();
	}
	finally{
	node.close();
	client.close();
	}
}
}
