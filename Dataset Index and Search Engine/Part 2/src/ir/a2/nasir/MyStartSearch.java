package ir.a2.nasir;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;



class MyStartSearch{

	String[] docArray;
	List<String> data;
	List<String> docs;
	TreeMap<Integer,String> docMap;
	HashMap<String,Long> termMap;
	HashMap<Long,Integer> ttfMap;
	HashMap<String, String> queries;
	double total_docLen=0;
	double avg_docLen=0;
    int tfTermQuery;
	TreeMap<String,Double> okapiTMap;
	TreeMap<String,Double> tfidfTMap;
	TreeMap<String, Double> bm25TMap;
	TreeMap<String,Double> laplaceTMap;
	TreeMap<String,Double> jmTMap;
	HashMap<Long,Long> indexCatalog;
	public MyStartSearch(TreeMap<Integer,String> docMap,HashMap<String,Long> termMap,HashMap<Long,Integer> ttfMap,HashMap<String,String> queries,int total_docLen){
		data=new ArrayList<String>();
		//this.docs=docs;
		this.docs=docs;
		this.queries=queries;
		this.docMap=docMap;
		this.termMap=termMap;
		this.ttfMap=ttfMap;
		this.total_docLen=total_docLen;
		avg_docLen=total_docLen/84678;
		okapiTMap=new TreeMap<String, Double>();
		tfidfTMap=new TreeMap<String,Double>();
		bm25TMap=new TreeMap<String,Double>();
		laplaceTMap=new TreeMap<String,Double>();
		jmTMap=new TreeMap<String,Double>();
		
	}
	
	public void processSearch()  {
		
		try{
			    
			    indexCatalog=getIndexCatMap();
			    for(int docId:docMap.keySet()){
				   String[] docLineSplits=docMap.get(docId).split("#");
				   String docName=docLineSplits[0];
				   int docLength=Integer.parseInt(docLineSplits[1]);
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
				   PorterStemmer stemmer=new PorterStemmer();
				   int found=0;
				   long termId;
				   for(String qKey: queries.keySet()){
						  String query=queries.get(qKey);
						  String words[]=query.split(" ");
						  okapi_tf_d_q=0;
						  tfidf_tf_d_q=0;
						  bm25_d_q=0.0;
						  laplace_d_q=0.0;  
						  jm_d_q=0.0;
						  termId=0;
						  System.out.println(query);
						  for(String w: words){   // each word from query
							      if(w.equals(""))
							    	  System.out.println("Query number = "+qKey);
							      found=0;
							      tfTermQuery=PrepareQueries.countWordOccurance(query, w);
							      w=stemmer.stem(w);
								  if(termMap.containsKey(w)){
									     List<Integer> tf_df;
                                         termId=termMap.get(w);
                                         System.out.println(termId);
                                         tf=getTfDf(termId,docId).get(0);
                                         df=getTfDf(termId, docId).get(1);
                                         System.out.println("came now only");
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
										
										
										} // end of if terms.equal(w)
									   
									   //LaPlace Summation for a Query
								/*   if(found==0){
									   laplace_d_q=laplace_d_q+Math.log(computeLaplace_W_D(0,docLength));
									   jm_d_q=jm_d_q+Math.log(computeJM_W_D(0, docLength, ttfMap.get(termId)));
									   
									   // Code for Debug
									   if(docId.equals("AP890717-0106") && qKey.equals("94.")){
										     System.out.println("found value = "+found);
										     System.out.println("Doc Len ="+docLength); 
											  System.out.println("jm till now "+jm_d_q);}
								   }*/
								
						  } // end of for(w:words)
						  if(okapi_tf_d_q!=0.0)
							  okapiTMap.put(qKey+"#"+docName, okapi_tf_d_q);    
						  if(tfidf_tf_d_q!=0.0)
							  tfidfTMap.put(qKey+"#"+docName, tfidf_tf_d_q);
						  if(bm25_d_q!=0.0)
							  bm25TMap.put(qKey+"#"+docName, bm25_d_q);
						  
							  laplaceTMap.put(qKey+"#"+docName, laplace_d_q);
							  
							  jmTMap.put(qKey+"#"+docName, jm_d_q);
							  
						  
				     } // end of for(q:queries)
				   System.out.println("Processed : "+docId);
			    } // end of for(docId: docs)
	    } // end of try
			    
		catch (Exception e) {
			e.printStackTrace();
		}
		
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
	public HashMap<Long, Long> getIndexCatMap() throws IOException{
		Path path = Paths.get("./OutputFiles/indexCatalog.txt");
	    List<String> catalog=Files.readAllLines(path, StandardCharsets.UTF_8);
	    HashMap<Long, Long> IndexCatMap=new HashMap<Long, Long>();
	    for(String line:catalog)
	    {
	    	String[] splits=line.split("#");
	    	IndexCatMap.put(Long.parseLong(splits[0]),Long.parseLong(splits[1]));
	    }
	    
	    return IndexCatMap;
	}
	public List<Integer> getTfDf(Long termId,int docId) throws IOException{
		List<Integer> tf_df=new ArrayList<Integer>();
		int tf=0;
		int df=0;
		long offset=indexCatalog.get(termId);
		RandomAccessFile raf=new RandomAccessFile("C:/Users/NasirAhmed/invertedIndex.txt", "r");
		raf.seek(offset);
		String indexLine=raf.readLine();
		String[] indexLineSplits=indexLine.split("\\$");
		String[] dblocks=indexLineSplits[1].split("#");
		df=indexLineSplits.length;
		for(String dblock: dblocks){
			String[] dblkSplits=dblock.split(":");
			if(Integer.parseInt(dblkSplits[0])==docId){
			 tf=Integer.parseInt(dblkSplits[2]);
			 System.out.println("tf of term "+termId+" = "+tf);
			 break;
			}
		}
		tf_df.add(tf);
		tf_df.add(df);
		raf.close();
		return tf_df;
	}
	
}
