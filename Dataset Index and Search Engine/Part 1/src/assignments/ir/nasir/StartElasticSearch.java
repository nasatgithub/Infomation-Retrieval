package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
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

class StartElasticSearch implements Runnable{
	public Node node;
	public Client client;
	public MyActionListener listener;
	String docId;
	Thread t;
	TermVectorRequestBuilder builder;
	List<String> data;
	public StartElasticSearch(Node node,Client client){
		this.node=node;
		this.client=client;
		t=new Thread(this);
		data=new ArrayList<String>();
		t.start();
	}
	@Override
	public void run() {
		listener=new MyActionListener();
		builder=new TermVectorRequestBuilder(client);
		
	}
	public void startBuild(String docId) throws IOException{
		this.docId=docId;
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
		   while(t.next()!=null){
			   term=t.term().utf8ToString();
			   //System.out.print("\n"+term);
			   docEnum=t.docs(null, null, DocsEnum.FLAG_FREQS);
			   df=t.docFreq();
			   tf=docEnum.freq();
			   ttf=t.totalTermFreq();
			   /*if(term.equals("celluloid")){
				 
				 //System.out.print(" Term Freq = "+docEnum.freq()+" | ");
				 //System.out.print("Ttf= "+t.totalTermFreq());  
				
			   }*/
			   docL=docL+tf;
		   }
	   }
	   //System.out.println("Doc Id: "+ docId + " | Doc Length = "+docL);
	   data.add("Doc Id: "+ docId + " | Doc Length = "+docL+"\n");

	   //System.out.println(term.);
	   //builder.execute(listener);
		
	}
	public void closeAllResources(){
		node.close();
		client.close();
	}
	
}