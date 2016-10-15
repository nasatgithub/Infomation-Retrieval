package ir.a4.controller;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class GenerateESResult {
	private static Node node;
	private static Client client;
	private static List<String> resultFileList;
	public GenerateESResult(){
		node=nodeBuilder().clusterName("ana").node();
		client=node.client();
		resultFileList=new ArrayList<String>();
	}
public static void main(String a[]) throws Exception{
	GenerateESResult mg=new GenerateESResult();
	mg.generateResultFile();
}

public static void generateResultFile() throws Exception{
		SearchResponse myResponse,mergeResponse;
		SearchHit[] mySearchHits;
		int i=0;	
			try{
				XContentBuilder jsonBuilder=XContentFactory.jsonBuilder();	
				XContentBuilder InlinksES;
				TreeMap<Integer,String> queryIdMap=new TreeMap<Integer, String>();
				queryIdMap.put(151501, "Obama family");
				queryIdMap.put(151502, "Obama election 2008");
				queryIdMap.put(151503, "ObamaCare");
				for(Integer qKey: queryIdMap.keySet()){
					System.out.println("Fetching results for query : "+queryIdMap.get(qKey)+" ..........");
						myResponse=client.prepareSearch("anair3") 
								.setTypes("document")
								.setSize(200)
								.setQuery(QueryBuilders.matchQuery("text", queryIdMap.get(qKey)))
								.execute()
								.actionGet();
						if(myResponse.getHits().getTotalHits()==0)
							continue;
						mySearchHits=myResponse.getHits().getHits();
						int rankCount=0;
						for(SearchHit mySearchHit: mySearchHits){
							//System.out.println(mySearchHit.getId() +"\t"+ mySearchHit.getScore());
							rankCount++;
							resultFileList.add(qKey+" Q0 "+mySearchHit.getId()+" "+rankCount+" "+mySearchHit.getScore()+" Exp");
							
						}
						
						//List<String> myInlinks=(ArrayList<String>)mySearchHit.getSource().get("inlinks");		
				}
			}
	
			catch(ElasticsearchException ese){
				ese.printStackTrace();
			}
			catch(Exception e){
				//System.out.println("Exception on : "+url);
				e.printStackTrace();
			}
			finally{
			node.close();
			client.close();
	}
	
		writeToFile();
}
public static void writeToFile() throws IOException{
	  System.out.println("Writing Results Data To File.. Please wait");
	  Path path = Paths.get("Files/crawledResultFile.txt");
	  Files.write(path, resultFileList, StandardCharsets.UTF_8);
	  System.out.println("FILE ( Files/crawledResultFile.txt ) WRITE SUCCESSFUL !!! ");
}
}
