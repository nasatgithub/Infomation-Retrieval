package assignments.ir.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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

class StartElasticSearchThread implements Runnable{
	public Node node;
	public Client client;
	public MyActionListener listener;
	String[] docArray;
	Thread t;
	TermVectorRequestBuilder builder;
	List<String> data;
	List<String> docs;
	HashMap<String, String> queries;
	HashMap<String,String> docLens;
	HashMap<String,Long> ttfs;
	double total_docLen=0;
	double avg_docLen=0;
    int tfTermQuery;
	TreeMap<String,Double> okapiTMap;
	TreeMap<String,Double> tfidfTMap;
	TreeMap<String, Double> bm25TMap;
	TreeMap<String,Double> laplaceTMap;
	TreeMap<String,Double> jmTMap;
	public StartElasticSearchThread(Node node,Client client,List<String> docs,HashMap<String,String> queries,HashMap<String,String> docLens,HashMap<String,Long> ttfs){
		this.node=node;
		this.client=client;
		t=new Thread(this);
		data=new ArrayList<String>();
		//this.docs=docs;
		this.docs=docs;
		this.queries=queries;
		this.docLens=docLens;
		this.ttfs=ttfs;
		total_docLen=Double.parseDouble(docLens.get("Total_Doc_Len"));
		avg_docLen=total_docLen/84678;
		listener=new MyActionListener();
		builder=new TermVectorRequestBuilder(client);
		okapiTMap=new TreeMap<String, Double>();
		tfidfTMap=new TreeMap<String,Double>();
		bm25TMap=new TreeMap<String,Double>();
		laplaceTMap=new TreeMap<String,Double>();
		jmTMap=new TreeMap<String,Double>();
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
				   int docLength=Integer.parseInt(docLens.get(docId));
				   int df,tf;
				   long ttf=0;  
				   double okapi_tf_w_d=0;
				   double okapi_tf_d_q=0;
				   double tfidf_tf_d_q=0;
				   double laplace_w_d=0;
				   double laplace_d_q;
				   double bm25_w_d;
				   double bm25_d_q=0;
				   double jm_w_d;
				   double jm_d_q=0;
				   long ttfsvalue;
				   TermVectorResponse resp=builder.get();
				   Fields fieldS=resp.getFields();
				   Terms allterms=fieldS.terms("text");
				   PorterStemmer stemmer=new PorterStemmer();
				   int found=0;
				   if(allterms!=null){
					   TermsEnum t=allterms.iterator(null);
					  for(String qKey: queries.keySet()){
						  String query=queries.get(qKey);
						  String words[]=query.split(" ");
						  okapi_tf_d_q=0;
						  tfidf_tf_d_q=0;
						  bm25_d_q=0.0;
						  laplace_d_q=0.0;  
						  jm_d_q=0.0;
						  
						  for(String w: words){   // each word from query
							      if(w.equals(""))
							    	  System.out.println("Query number = "+qKey);
							      t=allterms.iterator(null);
							      found=0;
							      tfTermQuery=PrepareQueries.countWordOccurance(query, w);

								   while(t.next()!=null){
									   
									   term=t.term().utf8ToString();
									   ttf=t.totalTermFreq();
									   if(term.equals(stemmer.stem(w.toLowerCase()))){
										  
										 found=1;
										 docEnum=t.docs(null, null, DocsEnum.FLAG_FREQS);
										 df=t.docFreq();
										 tf=docEnum.freq();
										 
										 
										 // Code for Debug
											/* if(docId.equals("AP890320-0177") && qKey.equals("100.")){
											     System.out.println("<< Word : "+w+" : found from Query "+qKey+" >>");
											     System.out.println("tf ="+tf);
											     System.out.println("okapi w d = "+okapi_tf_w_d);
											     System.out.println("till now okapi d q = "+okapi_tf_d_q);
											   }*/
										 
										 // Okapi TF Computation
										 okapi_tf_w_d=computeOkapi_W_D(tf,docLength);
										 okapi_tf_d_q=okapi_tf_d_q+okapi_tf_w_d;
										 
										 // TF-IDF Computation
										 tfidf_tf_d_q=tfidf_tf_d_q+(okapi_tf_w_d*(Math.log(84678.0/df)));
										 
										 // Okapi BM-25 Computation
										 bm25_w_d=computeBM25(tf,df,tfTermQuery,docLength);
										 bm25_d_q=bm25_d_q+bm25_w_d;
										 
										 // LaPLace Computation 
										 laplace_w_d=computeLaplace_W_D(tf,docLength);
										 laplace_d_q=laplace_d_q+Math.log(laplace_w_d);
						                 
										 // JM Smoothing 
										 jm_w_d=computeJM_W_D(tf,docLength,ttf);
										 jm_d_q=jm_d_q+Math.log(jm_w_d);
										
										 break; 
										} // end of if terms.equal(w)
									   
									   //LaPlace Summation for a Query
									  
									   
									 
								 } // end of while(t.next())
								   if(found==0){
									   laplace_d_q=laplace_d_q+Math.log(computeLaplace_W_D(0,docLength));
									   String stemmedW=stemmer.stem(w.toLowerCase());
									   jm_d_q=jm_d_q+Math.log(computeJM_W_D(0, docLength, ttfs.get(stemmedW)));
									   
									   // Code for Debug
									   /*if(docId.equals("AP890717-0106") && qKey.equals("94.")){
										     System.out.println("found value = "+found);
										     System.out.println("Doc Len ="+docLength); 
											  System.out.println("jm till now "+jm_d_q);}*/
								   }
								
						  } // end of for(w:words)
						  if(okapi_tf_d_q!=0.0)
							  okapiTMap.put(qKey+"#"+docId, okapi_tf_d_q);    
						  if(tfidf_tf_d_q!=0.0)
							  tfidfTMap.put(qKey+"#"+docId, tfidf_tf_d_q);
						  if(bm25_d_q!=0.0)
							  bm25TMap.put(qKey+"#"+docId, bm25_d_q);
						  
							  laplaceTMap.put(qKey+"#"+docId, laplace_d_q);
							  
							  jmTMap.put(qKey+"#"+docId, jm_d_q);
							  
						  
				     } // end of for(q:queries)
				   } // end of if (allterms!=null)  
			    } // end of for(docId: docs)
	    } // end of try
			    
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void closeAllResources(){
		node.close();
		client.close();
	}
	public double computeOkapi_W_D(int tf,int docLength){
		double result=tf/(tf+0.5+1.5*(docLength/avg_docLen));
		return result;
	}
	public double computeBM25(int tf,int df,int tfTermQuery,int docLength){
		double result,k1,k2,b;
		double D=84678.0;
		k1=1.2;
		k2=1000;
		b=0.75;
		result= Math.log((D+0.5)/(df+0.5))*((tf+(k1*tf)/(tf+(k1*((1-b)+b*(docLength/avg_docLen))))))*((tfTermQuery+(k2*tfTermQuery))/(tfTermQuery+k2));
		return result;
	}
	public double computeLaplace_W_D(int tf,int docLength){
		
		double result= (tf+1.0)/(docLength+178081.0);
		return result;
	}
	public double computeJM_W_D(int tf,int docLength,long ttf){
		double result,l=0.8;
		result=(l*((double)tf/docLength))+((1-l)*(((ttf)/178081.0)));
		return result;
	}
	
}
