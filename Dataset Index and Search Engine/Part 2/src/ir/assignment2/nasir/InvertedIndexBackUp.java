package ir.assignment2.nasir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

public class InvertedIndexBackUp {
	private List<String> stopWords;
	private int posCounter;
	private int tfCounter;
	List<String> invFormat;
	StringBuffer termDetails;
	TreeMap<Integer,String> partialTermMap;
	public InvertedIndexBackUp(){
		posCounter=0;
		tfCounter=0;
		invFormat=new ArrayList<String>();
		termDetails=new StringBuffer("");
		partialTermMap=new TreeMap<Integer,String>();
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
		String foldername="FilesTest";
		File folder=new File(foldername);
		File[] files=folder.listFiles();
		ReadRawTerms rawT=new ReadRawTerms();
		TreeMap<Integer,String> allTerms=rawT.readTermsRange();
		System.out.println("Processing Index... Please wait for about 2.5 min");
	    InvertedIndexBackUp invInd=new InvertedIndexBackUp();
		invInd.stopWords=invInd.readStopWords();
		String[] wSplits;
		TreeMap<Integer,String> sampleT=new TreeMap<Integer,String>();
		sampleT.put(1, "gener#2");
		sampleT.put(2, "new#2");
		System.out.println(sampleT);
		try{
		for(File f:files){
					fr=new FileReader(f);
					br=new BufferedReader(fr);	
					while((s=br.readLine())!=null){
						if(s.indexOf(docStartTag)!=-1){
							 inDoc=1;
							}
						else if(s.indexOf(docEndTag)!=-1){
						    inDoc=0;
						    docCount++;
						    invInd.searchTermInDoc(sampleT, new String(textContent),new String(docNoContent));
						    docNoContent=new StringBuffer("");
						    textContent=new StringBuffer(""); 
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
						     /*if(textStartTag.length()!=s.length())
						    	 System.out.println("Check Doc : "+docNoContent);*/
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
		
		//uncomment the following line to write all docnos into a file
		 //ir.writeTermsToFile(allterms);
        for(int termKey:invInd.partialTermMap.keySet())
        	System.out.println(termKey+"#"+invInd.partialTermMap.get(termKey));
   
		
		
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
public void searchTermInDoc(TreeMap<Integer,String> terms,String sText,String docNo){
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(sText);
	List<String> sArray=new ArrayList<String>();
	PorterStemmer stemmer = new PorterStemmer();
	String stemmedS;
	String[] termsSplits;
	String term;
	StringBuffer positionBuffer=new StringBuffer("");
	while(mat.find()){
		sArray.add(mat.group());
		//System.out.println(mat.group());
	}
	for(int termKey:terms.keySet()){
		termsSplits=terms.get(termKey).split("#");
		term=termsSplits[0];
		positionBuffer=new StringBuffer("");
		posCounter=0;
		tfCounter=0;
		if(sText.toLowerCase().indexOf(term.toLowerCase())!=-1){
			for(String s:sArray){
				if(s.matches("[A-Z][\\.][A-Z]"))
			    	s=s+".";
				s=s.toLowerCase();
				stemmedS=stemmer.stem(s);
				if(s.length()!=1 && !stopWords.contains(s)){
					System.out.println(stemmedS);
					posCounter++;
				}
				if(stemmedS.equals(term)){
					tfCounter++;
				/*	if(tfCounter==1)
					   	termDetails.append(termKey+"#"+term+"#"+docNo+":");*/
					//System.out.println(posCounter);
					//termDetails.append(posCounter+",");
					positionBuffer.append(posCounter+",");
				  
				}
			}
			if(tfCounter!=0){
			/*    termDetails.replace(termDetails.length()-1,termDetails.length(), ":");
			    termDetails.append(tfCounter);*/
				positionBuffer.replace(positionBuffer.length()-1,positionBuffer.length(), ":");
			    if(!partialTermMap.containsKey(termKey)){
					partialTermMap.put(termKey,term+"#"+docNo+":"+positionBuffer+tfCounter);
			    }
			    else
			    	partialTermMap.put(termKey, partialTermMap.get(termKey)+"#"+docNo+":"+positionBuffer+tfCounter);
			    }
			/*else 
			    termDetails.replace(termDetails.lastIndexOf("#"),
			    		                    termDetails.length(), ""); 
		    termDetails.append("\n");*/
		}
	} // end of for(int termKey:terms.keySet())
 }

}
