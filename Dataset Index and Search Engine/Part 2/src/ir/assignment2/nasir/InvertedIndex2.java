package ir.assignment2.nasir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvertedIndex2 {
	private List<String> stopWords;
	private int posCounter;
	private int tfCounter;
	List<String> invFormat;
	StringBuffer termDetails;
	TreeMap<Integer,String> partialTermMap;
	TreeMap<Integer,String> secondaryTermMap;
	public InvertedIndex2(){
		posCounter=0;
		tfCounter=0;
		invFormat=new ArrayList<String>();
		termDetails=new StringBuffer("");
		partialTermMap=new TreeMap<Integer,String>();
		secondaryTermMap=new TreeMap<Integer,String>();
	}
	public static void main(String a[]) throws Exception{
		long stime,etime;
		stime=System.currentTimeMillis();
		String s;
		int inDoc=0;
		int inDocNo=0;
		int inText=0;
		String docStartTag="<DOC>";
		String docEndTag="</DOC>";
		String docNoStartTag="<DOCNO>";
		String docNoEndTag="</DOCNO>";
		String textStartTag="<TEXT>";
		String textEndTag="</TEXT>";
		int docCount=0;
		StringBuffer docNoContent=new StringBuffer("");
		StringBuffer textContent=new StringBuffer("");
		List<String> docnos=new ArrayList<String>();
		List<String> docContents=new ArrayList<String>();
		FileReader fr=null;
		BufferedReader br=null;
		String foldername="Files";
		File folder=new File(foldername);
		File[] files=folder.listFiles();
		ReadRawTerms rawT=new ReadRawTerms();
		TreeMap<Integer,String> allTerms=rawT.readTermsRange();
		HashMap<String,Integer> allTermsInverted=rawT.readTermsRangeInverted();
		System.out.println("Processing Index... Please wait for about 2.5 min");
	    InvertedIndex2 invInd=new InvertedIndex2();
		invInd.stopWords=invInd.readStopWords();
		String[] wSplits;
		File f0=new File("./OutputFiles/pInvertedIndex.txt");
		if(f0.delete())
			System.out.println("partial index file deleted successfully");
		else 
			System.out.println("partial index file delete UNSUCCESSFUL");
	
		int docLimit=0;
		try{
		for(File f:files){
					fr=new FileReader(f);
					br=new BufferedReader(fr);	
					while((s=br.readLine())!=null){
						if(s.indexOf(docStartTag)!=-1){
							 inDoc=1;
							 docLimit++;
							}
						else if(s.indexOf(docEndTag)!=-1){
						    inDoc=0;
						    docCount++;
						    invInd.searchTermInDoc(allTerms,allTermsInverted, new String(textContent),new String(docNoContent));
						    docNoContent=new StringBuffer("");
						    textContent=new StringBuffer(""); 
						    if(docLimit>1000){
						    	System.out.println("Processed 1000");
						    	docLimit=0;
						    	   /*for(int termKey:invInd.partialTermMap.keySet())
						           	System.out.println(termKey+"#"+invInd.partialTermMap.get(termKey));*/
						    	invInd.writePartialTermMap(invInd.partialTermMap);
						    	invInd.partialTermMap=new TreeMap<Integer,String>();
						    }
						}
						else if(inDoc==1){
							
							// Block to extract the content between <DOCNO> .. </DOCNO> in a <DOC>
						    if(s.indexOf(docNoStartTag)!=-1){
						     inDocNo=1;
						     String docNo;
							     if(s.indexOf(docNoEndTag)!=-1){
							    	 docNo=s.substring(docNoStartTag.length(), s.indexOf(docNoEndTag)).trim();
							    	 inDocNo=0;
							     }
							     else{
							         docNo=s.substring(docNoStartTag.length(), s.length()).trim();
							     }
							 docNoContent.append(docNo);  
							 docnos.add(docNo);
						    }
						    else if(s.indexOf(docNoEndTag)!=-1)
						     {
						    	System.out.println("Coming hrer");
						    	inDocNo=0;
						     }
						    else if(inDocNo==1){
						    	
						    	docNoContent.append(s.trim());
						    	docnos.add(s.trim());
						    }
						    
						    // Block to extract the content between <TEXT> .. </TEXT> in a <DOC>
						    if(s.indexOf(textStartTag)!=-1){
						     inText=1;
						     if(s.indexOf(textEndTag)!=-1){
						    	 textContent.append(s.substring(textStartTag.length(), s.indexOf(textEndTag)).trim()+" "); 
						    	 inText=0;
						     }
						     else{
						    	 textContent.append(s.substring(textStartTag.length(), s.length()).trim()+" ");
						     }
						    }
						    else if(s.indexOf(textEndTag)!=-1)
							    inText=0;
						    else if(inText==1){  
						    	textContent.append(s.trim()+" ");
						    }
						}	
					}	
			} // end of for(File f:files)
		}
		finally{
			br.close();
			fr.close();		
		}
		System.out.println(invInd.termDetails);
		etime=System.currentTimeMillis();
		System.out.println("Time Taken for Indexing in seconds: "+(etime-stime)/1000);
		invInd.writeSecondaryPTemp();
		System.out.println("This is done !!!");
		//invInd.mergePartialList();
		//uncomment the following line to write all docnos into a file
		 //ir.writeTermsToFile(allterms);
     	
	}

public void writeTermsToFile(List<String> docNos) throws IOException{
	Path path=Paths.get("./OutputFiles/allTerms.txt");
	Files.write(path, docNos, StandardCharsets.UTF_8);
}
public List<String> readStopWords() throws IOException{
	Path path = Paths.get("./Stopwords/stoplist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}
public static TreeMap<String, Integer> SortByValue 
(HashMap<String, Integer> map) {
	ValueComparator2 vc =  new ValueComparator2(map);
	TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(vc);
	sortedMap.putAll(map);
	return sortedMap;
}
public void searchTermInDoc(TreeMap<Integer,String> terms,HashMap<String,Integer> termsInv,String sText,String docNo){
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(sText);
	List<String> sArray=new ArrayList<String>();
	PorterStemmer stemmer = new PorterStemmer();
	String stemmedS;
	String[] termsSplits;
	String term;
	StringBuffer positionBuffer=new StringBuffer("");
	posCounter=0;
	
	while(mat.find()){
		String w=mat.group();
		if(w.matches("[A-Z][\\.][A-Z]"))
	    	w=w+".";
		w=w.toLowerCase();
		sArray.add(w);
	}
	
	
/*	for(int termKey:terms.keySet()){
		termsSplits=terms.get(termKey).split("#");
		term=termsSplits[0];
		positionBuffer=new StringBuffer("");
		posCounter=0;
		tfCounter=0;
		System.out.println("in func : "+term);
		if(sText.toLowerCase().indexOf(term.toLowerCase())!=-1){
			for(String s:sArray){
				if(s.matches("[A-Z][\\.][A-Z]"))
			    	s=s+".";
				s=s.toLowerCase();
				stemmedS=stemmer.stem(s);
				if(s.length()!=1 && !stopWords.contains(s)){
					//System.out.println(stemmedS);
					posCounter++;
				}
				if(stemmedS.equals(term)){
					tfCounter++;
					positionBuffer.append(posCounter+",");
				  
				}
			}
			if(tfCounter!=0){
				positionBuffer.replace(positionBuffer.length()-1,positionBuffer.length(), ":");
			    if(!partialTermMap.containsKey(termKey)){
					partialTermMap.put(termKey,term+"#"+termsSplits[1]+"$"+docNo+":"+positionBuffer+tfCounter);
			    }
			    else
			    	partialTermMap.put(termKey, partialTermMap.get(termKey)+"#"+docNo+":"+positionBuffer+tfCounter);
			    }
		}
	} // end of for(int termKey:terms.keySet())
	*/
	
	HashMap<Integer,String> posCounterHash=new HashMap<Integer,String>();
	TreeMap<Integer,Integer> tfCounterHash=new TreeMap<Integer,Integer>();
	
	int termId=0;
	
	sArray.removeAll(stopWords);

	for(String s:sArray){
		stemmedS=stemmer.stem(s);
		if(s.length()!=1){
			//System.out.println(stemmedS);
			posCounter++;
		}
/*		if(terms.containsValue(stemmedS)){
			if(!tempTermList.containsKey(stemmedS)){
				 for(int termKey:terms.keySet()){
				  if(terms.get(termKey).equals(stemmedS)){
					  termId=termKey;
					  tempTermList.put(terms.get(termKey),termKey );
					  break;
				  }
				 }
			}
			else{
				
				termId=tempTermList.get(stemmedS);
			}*/
		if(termsInv.containsKey(stemmedS)){
			termId=termsInv.get(stemmedS);
		 if(!posCounterHash.containsKey(termId))
			 posCounterHash.put(termId, posCounter+",");
		 else 
			 posCounterHash.put(termId, posCounterHash.get(termId)+posCounter+",");
		 if(!tfCounterHash.containsKey(termId))
			 tfCounterHash.put(termId, 1);
		 else 
		     tfCounterHash.put(termId, tfCounterHash.get(termId)+1);
		}
	}
	//System.out.println("<<<<<<<<<< New Doc Done >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	for(int termKey:tfCounterHash.keySet()){
		term=terms.get(termKey);
		positionBuffer=new StringBuffer(posCounterHash.get(termKey));
		positionBuffer.replace(positionBuffer.length()-1,positionBuffer.length(), ":");
		tfCounter=tfCounterHash.get(termKey);
		if(!partialTermMap.containsKey(termKey)){
			partialTermMap.put(termKey,term+"$"+docNo+":"+positionBuffer+tfCounter);
	    }
	    else
	    	partialTermMap.put(termKey, partialTermMap.get(termKey)+"#"+docNo+":"+positionBuffer+tfCounter);
	}	
 }
public void writePartialTermMap(TreeMap<Integer,String> pTMap) throws IOException{
	RandomAccessFile raf=new RandomAccessFile("./OutputFiles/pInvertedIndex.txt", "rw");
	long currentFilePointer;
	raf.seek(raf.length());      // going to the end of file to write
	for(int termKey:pTMap.keySet())
	{
		currentFilePointer=raf.getFilePointer();
		if(!secondaryTermMap.containsKey(termKey))
			secondaryTermMap.put(termKey, Long.toString(currentFilePointer));
		else
			secondaryTermMap.put(termKey, 
					             secondaryTermMap.get(termKey)+"#"+Long.toString(currentFilePointer));
		
		raf.writeBytes(termKey+"#"+pTMap.get(termKey)+"\n");		
	}	
}
public void writeSecondaryPTemp() throws IOException{
	List<String> secPTempList=new ArrayList<String>();
	for(int termKey:secondaryTermMap.keySet())
		secPTempList.add(termKey+"#"+secondaryTermMap.get(termKey));
	Path path=Paths.get("./OutputFiles/secondaryTermMap.txt");
	Files.write(path, secPTempList, StandardCharsets.UTF_8);	
	
}

public void mergePartialList()throws IOException{
	RandomAccessFile rafSec=new RandomAccessFile("./OutputFiles/secondaryTermMap.txt", "r");
    RandomAccessFile rafPartial=new RandomAccessFile("./OutputFiles/pInvertedIndex.txt", "r");
	RandomAccessFile rafFinal=new RandomAccessFile("C:/Users/NasirAhmed/invertedIndex.txt","rw");

	String line,pLine;
	String[] termMapSplits;
	String[] pIndexSplits;
	String[] pIndexContSplits;
	int tFound=0;
	while((line=rafSec.readLine())!=null){
		termMapSplits=line.split("#");	
		String termId=termMapSplits[0];
		tFound=0;
	/*	rafPartial.seek(0);
		while((pLine=rafPartial.readLine())!=null){
			pIndexSplits=pLine.split("#");
			if(pIndexSplits[0].equals(termId)){
				if(tFound==0){
					tFound=1;
					rafFinal.writeBytes(pLine);
				}
				else{
				  pIndexContSplits=pLine.split("\\$");
				  rafFinal.writeBytes("#"+pIndexContSplits[1]);
				}
			}		
		}*/
		for(int i=1;i<termMapSplits.length;i++)
		{
			rafPartial.seek(Long.parseLong(termMapSplits[i]));
			pLine=rafPartial.readLine();
			if(i==1)
			rafFinal.writeBytes(pLine);
			else{
			 pIndexContSplits=pLine.split("\\$");
			 rafFinal.writeBytes("#"+pIndexContSplits[1]);
			}
		}
		rafFinal.writeBytes("\n");
	}
	
		
}
}
