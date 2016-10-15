package ir.a2.nasir;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;



public class MyStartQuickSearch{
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
	TreeMap<String,Double> proxTMap;
	HashMap<Long,Long> indexCatalog;
	HashSet<Integer> docList;
	HashMap<String,List<List<Integer>>> proximityData;
	HashMap<String,Integer> qTermCount;
	public MyStartQuickSearch(TreeMap<Integer,String> docMap,HashMap<String,Long> termMap,HashMap<Long,Integer> ttfMap,HashMap<String,String> queries,HashSet<Integer> docList,long total_docLen){
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
		proxTMap=new TreeMap<String,Double>();
		this.docList=docList;
	    proximityData=new HashMap<String,List<List<Integer>>>();
	    qTermCount=new HashMap<String,Integer>(); 
	}
	public void processSearch()  {
		System.out.println("total doc length = "+total_docLen);
		try{  
			    indexCatalog=getIndexCatMap();
		           List<String> debug=new ArrayList<String>();
		           debug.add("industrialized");
		           debug.add("states");
		           debug.add("transfer");
		           debug.add("high");
		           debug.add("tech");
		           debug.add("states");
		           debug.add("technologies");
		           debug.add("nations");
				   int df,tf;
				   long ttf=0;  
				   long ttfsvalue;
				   PorterStemmer stemmer=new PorterStemmer();
				   long termId;
				   for(String qKey: queries.keySet()){
						  String query=queries.get(qKey);
						  String words[]=query.split(" ");
						  termId=0;
						  System.out.println("Query No:"+qKey);
						  qTermCount.put(qKey, words.length);
						  for(String w: words){   // each word from query
							      
							      if(w.equals(""))
							    	  System.out.println("Query number = "+qKey);
							      
							      tfTermQuery=PrepareQueries.countWordOccurance(query, w);
							     // System.out.println(w);
							      String dw=w;
							      w=stemmer.stem(w);
								  if(termMap.containsKey(w)){
                                         termId=termMap.get(w);
                                         processScoring(qKey,termId,dw,tfTermQuery);
								   }		
						  } // end of for(w:words)	  	
				     } // end of for(q:queries)
				   System.out.println("Processing Proximity Searching Now.... ");
				   processProximitySearch(); 
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
		double exp1=Math.log((D+0.5)/(df+0.5));
	    double exp2=(tf+(k1*tf))/(tf+(k1*((1-b)+b*(docLength/avg_docLen))));
	    double exp3=(tfTermQuery+(k2*tfTermQuery))/(tfTermQuery+k2);
		result= exp1*exp2*exp3;
		return result;
	}
	public double computeLaplace_W_D(int tf,int docLength){
		
		double result= (tf+1.0)/(docLength+157835.0);
		return result;
	}
	public double computeJM_W_D(int tf,int docLength,long ttf){
		double result,l=0.8;
		result=(l*((double)tf/docLength))+((1-l)*(((ttf)/157835.0)));
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
	public void processScoring(String qKey,Long termId,String dw,int tfTermQuery) throws IOException{
	int tf=0;
	int df=0;
    double okapi_tf_w_d=0;
    double tfidf_w_d=0;
    double bm25_w_d=0;
    double laplace_w_d=0;
    double jm_w_d=0;
	long offset=indexCatalog.get(termId);
	RandomAccessFile raf=new RandomAccessFile("C:/Users/NasirAhmed/invertedIndex.txt", "r");
	raf.seek(offset);
	String indexLine=raf.readLine();
	String[] indexLineSplits=indexLine.split("\\$");
	String[] dblocks=indexLineSplits[1].split("#");
	df=dblocks.length;

	System.out.println(dw);
	//System.out.println("term id & offset :"+termId+" "+offset);
	int docId;
	int docLength;
	//System.out.println("term: "+dw+"  tfTermQuery:"+tfTermQuery);
	HashSet<Integer> foundDocIds=new HashSet<Integer>();
	for(String dblock: dblocks){ // processing score for each document that is found in the Inverted Index for the search term
		String[] dblkSplits=dblock.split(":");
		 docId=Integer.parseInt(dblkSplits[0]);
		 String posList=dblkSplits[1];
		 String[] posListSplits=posList.split(",");
		 tf=Integer.parseInt(dblkSplits[2]);	
		 String[] docMapSplits=docMap.get(docId).split("#");
		 docLength=Integer.parseInt(docMapSplits[1]);
		 if(dw.equals("encryption"))
			 System.out.println("Found Encrypt in : "+docMapSplits[0]+"  Dblock = "+dblock);
		 foundDocIds.add(docId);
		 okapi_tf_w_d=computeOkapi_W_D(tf, docLength);
		 tfidf_w_d=okapi_tf_w_d*(Math.log(84678.0/df));
		 bm25_w_d=computeBM25(tf, df, tfTermQuery, docLength);
		 
		 //Method to compute Laplace
		 //laplace_w_d=computeLaplace_W_D(tf, docLength);
		 
		 jm_w_d=computeJM_W_D(tf, docLength, ttfMap.get(termId));
		 if(!okapiTMap.containsKey(qKey+"#"+docId))
			 okapiTMap.put(qKey+"#"+docId, okapi_tf_w_d);
		 else
		     okapiTMap.put(qKey+"#"+docId, okapiTMap.get(qKey+"#"+docId)+okapi_tf_w_d); 
		 if(!tfidfTMap.containsKey(qKey+"#"+docId))
			 tfidfTMap.put(qKey+"#"+docId, tfidf_w_d);
		 else
		     tfidfTMap.put(qKey+"#"+docId, tfidfTMap.get(qKey+"#"+docId)+tfidf_w_d);
		 if(!bm25TMap.containsKey(qKey+"#"+docId))
			 bm25TMap.put(qKey+"#"+docId, bm25_w_d);
		 else
			 bm25TMap.put(qKey+"#"+docId, bm25TMap.get(qKey+"#"+docId)+bm25_w_d);
		 
		 // CODE TO COMPUTE LAPLACE
		 /*if(!laplaceTMap.containsKey(qKey+"#"+docId))
			 laplaceTMap.put(qKey+"#"+docId, Math.log(laplace_w_d));
		 else
			 laplaceTMap.put(qKey+"#"+docId, laplaceTMap.get(qKey+"#"+docId)+Math.log(laplace_w_d));*/
		 
		 if(!jmTMap.containsKey(qKey+"#"+docId))
				jmTMap.put(qKey+"#"+docId, Math.log(jm_w_d));
			else
				jmTMap.put(qKey+"#"+docId, jmTMap.get(qKey+"#"+docId)+Math.log(jm_w_d));
		 
		 List<Integer> termPosList=new ArrayList<Integer>();
		 for(String pos:posListSplits){
		  termPosList.add(Integer.parseInt(pos));
		 }
		 
		 
		 if(!proximityData.containsKey(qKey+"#"+docId))
		 {
			 
			 List<List<Integer>> perDocProximityTerms=new ArrayList<List<Integer>>();
			 perDocProximityTerms.add(termPosList);
			 proximityData.put(qKey+"#"+docId, perDocProximityTerms);
		 }
		 else
		 {
			 List<List<Integer>> perDocProximityTerms=proximityData.get(qKey+"#"+docId);
			 perDocProximityTerms.add(termPosList);
			 proximityData.put(qKey+"#"+docId, perDocProximityTerms);
			 
		 }
	}
	
	HashSet<Integer> NotFoundDocList=new HashSet<Integer>(docList);
	NotFoundDocList.removeAll(foundDocIds);
	for(int docid:NotFoundDocList){
		
		String[] splits=docMap.get(docid).split("#");
		int docLen=Integer.parseInt(splits[1]);
		
		//Score Computation for Laplace
		/*laplace_w_d=computeLaplace_W_D(0, docLen);
		if(!laplaceTMap.containsKey(qKey+"#"+docid))
			laplaceTMap.put(qKey+"#"+docid, Math.log(laplace_w_d));
		else
			laplaceTMap.put(qKey+"#"+docid, laplaceTMap.get(qKey+"#"+docid)+Math.log(laplace_w_d));*/
		
		jm_w_d=computeJM_W_D(0, docLen, ttfMap.get(termId));
		if(!jmTMap.containsKey(qKey+"#"+docid))
			jmTMap.put(qKey+"#"+docid, Math.log(jm_w_d));
		else
			jmTMap.put(qKey+"#"+docid, jmTMap.get(qKey+"#"+docid)+Math.log(jm_w_d));
	}
	raf.close();
 }

 public void processProximitySearch(){
	 double proximity_q_d=0;
	 //Converting proximity data to matrix form for computations 
	 int matrix[][]=null;
	 int docId;
	 String qKey="";
	 for(String qKeydocId:proximityData.keySet()){
		 String[] qKeydocIdSplits=qKeydocId.split("#");
		 qKey=qKeydocIdSplits[0];
		 docId=Integer.parseInt(qKeydocIdSplits[1]);
		 List<List<Integer>> perDocTerms=proximityData.get(qKeydocId);
		 int k=perDocTerms.size();
		 int noQT=qTermCount.get(qKey);
		 System.out.println("no: of terms in query : "+qKey +" = "+ noQT);
		//if(k>=noQT/1.5){
			 matrix=new int[k][];
			 int i=0;
			 int maxCols[]=new int[k];
			 for(List<Integer> termsPosList:perDocTerms){
				 matrix[i]=new int[termsPosList.size()];
				 int j=0;
				 for(int pos:termsPosList)
					 {matrix[i][j]=pos;
					  maxCols[i]=j;
					  j++;		 
					 } 
				 i++;
			 }
		
			 int min,max;
			 int span;
			 int m,n;
			 m=n=0;
	
			 TreeMap<Integer,Integer[]> currentWindow_pos_Col=new TreeMap<Integer,Integer[]>();
			 for(m=0;m<matrix.length;m++){
		        Integer r_c[]=new Integer[2];
		        r_c[0]=m;
		        r_c[1]=n;
		        currentWindow_pos_Col.put(matrix[m][n],r_c);
			 }
			 min=currentWindow_pos_Col.firstKey();
			 max=currentWindow_pos_Col.lastKey();
			 span=max-min;
			 //System.out.println(currentWindow_pos_Col);
			 TreeMap<Integer,Integer[]> OldCurrentWindow_pos_Col;
			 while(span!=k-1){
				 min=currentWindow_pos_Col.firstKey();
				 max=currentWindow_pos_Col.lastKey();
				 span=Math.min(max-min,span);	
				 //change the current window now
				 OldCurrentWindow_pos_Col=new TreeMap<Integer,Integer[]>(currentWindow_pos_Col);
				 Integer r_c[]=currentWindow_pos_Col.get(min);
				 if(r_c[1]+1<=maxCols[r_c[0]])
				 {   r_c[1]=r_c[1]+1;
				     currentWindow_pos_Col.remove(min);
					 currentWindow_pos_Col.put(matrix[r_c[0]][r_c[1]],r_c); 
				 }
				 if(OldCurrentWindow_pos_Col.equals(currentWindow_pos_Col))
					 break;
			} 
			 //System.out.println("min span for doc:"+docId+"  for query:"+qKey+"  is "+span);
			 
			 
				 String[] docString=docMap.get(docId).split("#");
				 int docL=Integer.parseInt(docString[1]);
				 double exp1=(1500-span)*k;
				 double exp2=docL+157835.0;
				 //double lmb=0.8;
				 proximity_q_d=exp1/exp2;
				 if(!proxTMap.containsKey(qKey+"#"+docId))
					  proxTMap.put(qKey+"#"+docId, proximity_q_d);
			 
		// }// end of if k
	
	 }// end of for(int docId:docList)
 }
}

