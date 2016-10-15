package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.termvector.TermVectorRequestBuilder;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

class StartElasticSearchDocLenThread implements Runnable{
	public Node node;
	public Client client;
	public MyActionListener listener;
	String docId;
	Thread t;
	TermVectorRequestBuilder builder;
	List<String> data;
	List<String> docs;
	HashMap<String,Long> ttfdata;
	public int totalDocLen;
	public StartElasticSearchDocLenThread(Node node,Client client,List<String> docs){
		this.node=node;
		this.client=client;
		t=new Thread(this);
		data=new ArrayList<String>();
		this.docs=docs;
		listener=new MyActionListener();
		builder=new TermVectorRequestBuilder(client);
		totalDocLen=0;
		ttfdata=new HashMap<String, Long>();
		t.start();
	}
	@Override
	public void run()  {
		try{
			    for(String docId:docs){
					builder.setSelectedFields("text")
					   .setTermStatistics(true)
				       .setFieldStatistics(true)
				       .setIndex("ap_dataset")
				       .setType("document")
				       .setId(docId)
				       .setPositions(false)
				       .setOffsets(false);
				       
				   String term;
				   DocsEnum docEnum;
				   int docLength=0;
				   int df,tf,docL;
				   long ttf;  
				   docL=0;
				   TermVectorResponse resp=builder.get();
				   Fields fieldS=resp.getFields();
				   Terms allterms=fieldS.terms("text");
				   if(allterms!=null){
					   TermsEnum t=allterms.iterator(null);
					   tf=df=0;
					   ttf=0;
					   while(t.next()!=null){
						   term=t.term().utf8ToString();
						   docEnum=t.docs(null, null, DocsEnum.FLAG_FREQS);
						   tf=docEnum.freq();
						   docL=docL+tf;
						   ttf=t.totalTermFreq();
						   if(!ttfdata.containsKey(term))
							   ttfdata.put(term, ttf);
					   }
				   }
				   //System.out.println("Doc Id: "+ docId + " | Doc Length = "+docL);
				   data.add(docId+"#"+docL);
				   totalDocLen=totalDocLen+docL;
			    }
			    
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void closeAllResources(){
		node.close();
		client.close();
	}
	
}
