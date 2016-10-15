package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.HashMap;

import groovy.json.JsonBuilder;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContentGenerator;
import org.elasticsearch.node.Node;



public class PrepareSearchTest {
public static void main(String a[]) throws Exception{
	PrepareSearchTest p=new PrepareSearchTest();
	p.search();
}
public void search() throws Exception{
	Node node =nodeBuilder().clusterName("irnasir").client(true).node();
	Client client =node.client();
	//XContentFactory jsonBuilder=new XContentFactory();
	XContentBuilder jsonbuilder=XContentFactory.jsonBuilder();
//	XContentBuilder builder=jsonbuilder
//			                .startObject()
//			                 .field("fields",jsonbuilder.startArray().array("text").endArray())
//			                 .field("term_statistics",true)
//			                 .field("field_statistics",true)
//			                .endObject();
	XContentBuilder b2=jsonbuilder
			           .startObject()
			            .field("query")
			             .startObject()
			              .field("match_all")
			                .startObject()
			                .endObject()
			             .endObject()
			           .endObject();  
			             
	SearchResponse res=client.prepareSearch("crawltest")
			                 .setTypes("document")
			                 .setSource(b2)
			                 .execute()
			                 .actionGet();
	
	System.out.println(res.toString());
	
	
	
}
}
