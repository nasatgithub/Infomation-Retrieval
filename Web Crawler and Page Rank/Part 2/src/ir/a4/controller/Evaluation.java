package ir.a4.controller;

import java.io.RandomAccessFile;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class Evaluation {
private static HashSet<String> seenQid;
private static HashMap<String,HashMap<String,Integer>> qrelMap;
private static HashMap<String,Integer> qrelRelevants;
private static List<Integer> atList;
private static HashMap<Integer,Double> overAllPrecision;
private static HashMap<Integer,Double> overAllRecall;
private static HashMap<Integer,Double> overAllF1;
private static HashMap<Integer,Double> overAllNdcg;
private static TreeMap<Integer,Double> precisionMap;
private static TreeMap<Integer,Double> recallMap;
private static TreeMap<Float,Double> precisonRecallCurve;
private static TreeMap<Float,Double> overallPrecisonRecallCurve;
private static HashMap<String,Integer> rel_retrieved;
private static List<Integer> sortedGradeList;
private static double overall_Avg_P;
private static double overall_Recall_P;
private static List<Integer> gradeList;
private static String choice;
private static int rankSize;
public Evaluation(){
	seenQid=new HashSet<String>();
	qrelMap=new HashMap<String,HashMap<String,Integer>>();
	atList=new ArrayList<Integer>();
	qrelRelevants=new HashMap<String, Integer>();
	overAllPrecision=new HashMap<Integer, Double>();
	overAllRecall=new HashMap<Integer, Double>();
	overAllF1=new HashMap<Integer, Double>();
	overAllNdcg=new HashMap<Integer, Double>();
	gradeList=new ArrayList<Integer>();
	precisionMap =new TreeMap<Integer,Double>();
	recallMap= new TreeMap<Integer, Double>();
	precisonRecallCurve=new TreeMap<Float, Double>();
	overallPrecisonRecallCurve=new TreeMap<Float,Double>();
	rel_retrieved=new HashMap<String, Integer>();
	overall_Avg_P=0.0;
	overall_Recall_P=0.0;
	rankSize=200;
}
public static void main(String args[]){
	Scanner scan=new Scanner(System.in);
	System.out.println("MENU\n----\nEnter\n1 - For Summarized Result\n2 - For -q Option");
	choice=scan.nextLine(); 
	
	Evaluation eval=new Evaluation();
	atList.add(5);atList.add(10);atList.add(20);atList.add(50);atList.add(100);atList.add(200);
	for(int at: atList){
		overAllPrecision.put(at, 0.0);
		overAllRecall.put(at, 0.0);
		overAllF1.put(at, 0.0);	
		overAllNdcg.put(at, 0.0);
	}
	for(double i=0;i<=1;i=i+0.1){
		overallPrecisonRecallCurve.put((float)i, 0.0);
	}
	int newQuery=0;
	try {
		eval.fetchQrelData();
		RandomAccessFile raf=new RandomAccessFile("Files/ScoreList.txt", "rw");
		String line;
		int perQueryLineCount=0;
		double precision=0;
		double recall=0;
		double r_precision=0;
		double f1=0;
		double sum_precision=0;
		double avg_precision=0;
		double ndcg=0;
		int sumRelevancy=0;
		boolean rpFound=false;
		int grade,binaryGrade;
		int beta=1;
		double fexp1,fexp2;
		HashMap<String,Integer> queryDocGradeMap=null;
		int numOfRelevants=0;
		List<String> precisionPrint,recallPrint,f1Print,ndcgPrint;
		precisionPrint=new ArrayList<String>();
		recallPrint=new ArrayList<String>();
		f1Print=new ArrayList<String>();
		ndcgPrint=new ArrayList<String>();
		//System.out.println(recallPrint.isEmpty());
	    while((line=raf.readLine())!=null){
	    	//System.out.println(line);
	    	String[] splits=line.split("\\s");
	    	String qid=splits[0];
	    	String docId=splits[2];
	    	if(!seenQid.contains(qid))
	    		{ 
	    		  if(!precisionPrint.isEmpty() && !recallPrint.isEmpty()){
	    			  if(choice.equals("2")){
		    			  printList(precisionPrint);
		    			  System.out.println();
		    			  printList(recallPrint);
		    			  System.out.println();
		    			  printList(f1Print);
		    			  System.out.println();
		    			  printList(ndcgPrint);
		    			  System.out.println();
		    			  System.out.println("Average precision (non-interpolated) for all rel docs(averaged over queries) : "+sum_precision/numOfRelevants);
		    			  System.out.println();
	    			  }
	    			  overall_Avg_P=overall_Avg_P+(sum_precision/numOfRelevants);
	    			  overall_Recall_P=overall_Recall_P+r_precision;
	    		  }
	    		  if(!precisionMap.isEmpty() && !recallMap.isEmpty()){
	    			  precisonRecallCurve.clear();
	    			  computePlot();
	    		  }
	    		 
	    		  seenQid.add(qid);
	    		  rel_retrieved.put(qid, 0);
	    		  perQueryLineCount=0;
	    		  precision=0;
	    		  recall=0;
	    		  sumRelevancy=0;
	    		  sum_precision=0;
	    		  rpFound=false;
	    		  if(qrelMap.containsKey(qid)){
	    			  queryDocGradeMap = qrelMap.get(qid);
	    			  numOfRelevants=qrelRelevants.get(qid);
	    			  precisionPrint.clear();
	    			  recallPrint.clear();
	    			  f1Print.clear();
	    			  ndcgPrint.clear();
	    			  gradeList.clear();
	    			  precisionMap.clear();
	    			  recallMap.clear();
	    			  if(choice.equals("2"))
	    			   System.out.println("For Query ID : "+qid+"\n------------------");
	    			 // System.out.println(queryDocGradeMap);
	    		  }
	    		  else 
	    			  continue;
	    		}
	    	
	    	if(queryDocGradeMap.containsKey(docId))
                grade=queryDocGradeMap.get(docId);
	    	else 
	    		grade=0;
	    	perQueryLineCount++;
	    	gradeList.add(grade);
	    	if(grade!=0){
	    		binaryGrade=1;
	    		rel_retrieved.put(qid, rel_retrieved.get(qid)+1);
	    	}
	    	else 
	    		binaryGrade=0;
	    	sumRelevancy=sumRelevancy+ binaryGrade;
	    	precision=(double)sumRelevancy/perQueryLineCount;
    		recall=(double)sumRelevancy/numOfRelevants;
    		precisionMap.put(perQueryLineCount, precision);
    		recallMap.put(perQueryLineCount, recall);
    		/*	if(precision==recall && precision!=0){
    			System.out.println("R-Precision = "+precision);
    			System.out.println();
    		}*/
    		if(grade!=0)
    		  sum_precision=sum_precision+precision;
	    	//System.out.println("sum = "+sumRelevancy);
	    	if(atList.contains(perQueryLineCount)){
	    		fexp1=(double)((Math.pow(beta, 2)+1)*precision*recall);
	    		fexp2=(double)((Math.pow(beta, 2)*precision)+recall);
	    		f1=(double)fexp1/fexp2;
	    		ndcg=computeNdcg();
	    		if(Double.isNaN(f1))
	    			f1=0;
	    		if(Double.isNaN(ndcg))
	    			ndcg=0;
	    		precisionPrint.add("Precision at "+perQueryLineCount+" docs : "+ precision);
	    		recallPrint.add("Recall at "+perQueryLineCount+" docs : "+ recall);
	    		f1Print.add("F1-Measure at "+perQueryLineCount+" docs : "+ f1);
	    		ndcgPrint.add("Ndcg at "+perQueryLineCount+" docs : "+ ndcg);
	    		overAllPrecision.put(perQueryLineCount, overAllPrecision.get(perQueryLineCount)+precision);
	    		overAllRecall.put(perQueryLineCount, overAllRecall.get(perQueryLineCount)+recall);
	    		overAllF1.put(perQueryLineCount, overAllF1.get(perQueryLineCount)+f1);
	    		overAllNdcg.put(perQueryLineCount, overAllNdcg.get(perQueryLineCount)+ndcg);
	    		//System.out.println("Precision at "+perQueryLineCount+" : "+ precision);
	    	} 	
	    	if(perQueryLineCount==numOfRelevants){
	    		r_precision=precision;
	    		if(choice.equals("2")){
	    		System.out.println("R-Precision = "+r_precision);
    			System.out.println();
	    		}
    			rpFound=true;
	    	}
	    	if(perQueryLineCount==rankSize && rpFound==false ){
	    		r_precision=((double)sumRelevancy/numOfRelevants);
	    		if(choice.equals("2")){
	    		System.out.println("R-Precision = "+ r_precision);
	    		System.out.println();
	    		}
	    		rpFound=true;
	    	}
	    	
	    }
		  if(!precisionPrint.isEmpty() && !recallPrint.isEmpty()){
			  if(choice.equals("2")){
				  printList(precisionPrint);
				  System.out.println();
				  printList(recallPrint);
				  System.out.println();
				  printList(f1Print);
				  System.out.println();
				  printList(ndcgPrint);
				  System.out.println();
				  System.out.println("Average precision (non-interpolated) for all rel docs(averaged over queries) : "+sum_precision/numOfRelevants);
				  System.out.println();
			  }
			  overall_Avg_P=overall_Avg_P+(sum_precision/numOfRelevants);
			  overall_Recall_P=overall_Recall_P+r_precision;
		  }
		  if(!precisionMap.isEmpty() && !recallMap.isEmpty()){
			  precisonRecallCurve.clear();
			  computePlot();
		  }
		 printOverAllQueriesData();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
public static void fetchQrelData() throws Exception{
	
	RandomAccessFile raf2=new RandomAccessFile("Files/qrels.txt","rw");
	String qrelLine;
	HashMap<String,Integer> docGradeMap=null;
	while((qrelLine=raf2.readLine())!=null){
		String[] splits=qrelLine.split("\\s");
		String qrelQid=splits[0].trim();
		String docId=splits[2].trim();
		int grade=Integer.parseInt(splits[3]);
	    if(!qrelMap.containsKey(qrelQid)){
	       docGradeMap=new HashMap<String, Integer>();
	       qrelMap.put(qrelQid, docGradeMap);
	       qrelRelevants.put(qrelQid, 0);
	    }
	    if(grade!=0)
	    	qrelRelevants.put(qrelQid, qrelRelevants.get(qrelQid)+1);
	    docGradeMap.put(docId, grade);		
	}
}
public static void printList(List<String> list){
	for(String line:list)
		System.out.println(line);
}
public static void printOverAllQueriesData(){
	int qSize=seenQid.size();
	System.out.println("AVERAGED OVER OVERALL "+qSize +" queries\n---------------------------------");
	System.out.println("\nRelevants From Qrel : ");
	for(String qid: qrelRelevants.keySet()){
		System.out.println(qid +":"+qrelRelevants.get(qid));
	}
	System.out.println("Relevants Retrieved : ");
	for(String qid:rel_retrieved.keySet()){
		System.out.println(qid +":"+rel_retrieved.get(qid));
	}
	System.out.println();
	TreeMap<Object,Double> sortedOverAllQueries=new TreeMap<Object, Double>(overAllPrecision);
	for(Object key: sortedOverAllQueries.keySet())
	{
			System.out.println("Precision at "+key+" docs : "+ sortedOverAllQueries.get(key)/qSize);
	}
	System.out.println();
	sortedOverAllQueries=new TreeMap<Object, Double>(overAllRecall);
	for(Object key: sortedOverAllQueries.keySet())
	{
			System.out.println("Recall at "+key+" docs : "+ sortedOverAllQueries.get(key)/qSize);
	}
	System.out.println();
	sortedOverAllQueries=new TreeMap<Object, Double>(overAllF1);
	for(Object key: sortedOverAllQueries.keySet())
	{
			System.out.println("F1 at "+key+" docs : "+ sortedOverAllQueries.get(key)/qSize);
	}
	System.out.println();
	sortedOverAllQueries=new TreeMap<Object, Double>(overAllNdcg);
	for(Object key: sortedOverAllQueries.keySet())
	{
			System.out.println("Ndcg at "+key+" docs : "+ sortedOverAllQueries.get(key)/qSize);
	}
	System.out.println();
	System.out.println("Precision - Recall Curve");
	for(float i: overallPrecisonRecallCurve.keySet()){
		System.out.println("at "+ i+" : "+overallPrecisonRecallCurve.get(i)/qSize);
	}
	 System.out.println("Average R-Precision for all rel docs : "
            +overall_Recall_P/qSize);
	 System.out.println("Average precision (non-interpolated) for all rel docs(averaged over queries) : "
	                   +overall_Avg_P/qSize);
}
public static double computeNdcg(){
	double dcg,sDcg;
	dcg=computeDcg(gradeList);
	sortedGradeList=new ArrayList<Integer>(gradeList);
	Collections.sort(sortedGradeList);
	Collections.reverse(sortedGradeList);
	sDcg=computeDcg(sortedGradeList);
	return (dcg/sDcg);
}
public static double computeDcg(List<Integer> gradeList){
	double result=0;
	int i=1;
	for(int grade: gradeList){
		if(i==1)
			result=result+grade;
		else
			result=result+(grade/(Math.log(i)/Math.log(2)));
		i++;
	}
	return result;
}
public static double getMaxPrecisionFrom(int recallLevelKey){
	double max=0;
	for(int precisionLevelKey: precisionMap.keySet()){
		if(precisionLevelKey>=recallLevelKey){
			max=Math.max(max, precisionMap.get(precisionLevelKey));
		}
	}
	return max;
	
}
public static void computePlot(){
	  for(double i=0;i<=1;i=i+0.10){
		  for(int recallLevelKey: recallMap.keySet()){
			  if(recallMap.get(recallLevelKey)>=i){
				  double maxPrecision=getMaxPrecisionFrom(recallLevelKey);
				  precisonRecallCurve.put((float)i, maxPrecision);
				  overallPrecisonRecallCurve.put((float)i, overallPrecisonRecallCurve.get((float)i)+maxPrecision);
				  break;
			  }
			  else{
				  precisonRecallCurve.put((float)i, 0.0);
			  }
		
		  }
	  }
	  if(choice.equals("2")){
	  System.out.println("Precision - Recall Plot :");
	  System.out.println(precisonRecallCurve);
	  System.out.println();
	  }
}
}
