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

public class InvertIndex {
	private List<String> stopWords;
	private int posCounter;
	private int tfCounter;
	List<String> invFormat;
	StringBuffer termDetails;
	public InvertIndex(){
		posCounter=0;
		tfCounter=0;
		invFormat=new ArrayList<String>();
		termDetails=new StringBuffer("");
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
		String textContent=new String();
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
	    InvertIndex invInd=new InvertIndex();
		invInd.stopWords=invInd.readStopWords();
		String[] wSplits;
		TreeMap<Integer,String> sampleT=new TreeMap<Integer,String>();
		sampleT.put(1, "gener#2");
		sampleT.put(2, "new#2");
		System.out.println(sampleT);
		try{
		invInd.termDetails.append("1"+"#"+"gener");
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
						    System.out.println("--- End of Doc " + docNoContent+" ---");
						    docNoContent=new StringBuffer("");
						    if(invInd.tfCounter!=0){
						    invInd.termDetails.replace(invInd.termDetails.length()-1,invInd.termDetails.length(), ":");
						    invInd.termDetails.append(invInd.tfCounter);
						    }
						    else 
						     invInd.termDetails.replace(invInd.termDetails.lastIndexOf("#"),
						    		                     invInd.termDetails.length(), ""); 
						    invInd.posCounter=0;
						    invInd.tfCounter=0;
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
							 invInd.termDetails.append("#"+docNoContent+":");
						    }
						    else if(s.indexOf(docNoEndTag)!=-1)
						     {
						    	System.out.println("Coming hrer");
						    	inDocNo=0;
						    	invInd.termDetails.append("#"+docNoContent+":"); // first element of DBLOCK
						     }
						    else if(inDocNo==1){
						    	
						    	docNoContent.append(s.trim());
						    	docnos.add(s.trim());
						    }
						    
						    // Block to extract the content between <TEXT> .. </TEXT> in a <DOC>
						    if(s.indexOf(textStartTag)!=-1){
						     inText=1;
						     if(s.indexOf(textEndTag)!=-1){
						    	 textContent=(s.substring(textStartTag.length(), s.indexOf(textEndTag)).trim()+" ");
						    	 invInd.searchTermInDoc("gener",textContent);
						    	 inText=0;
						     }
						     /*if(textStartTag.length()!=s.length())
						    	 System.out.println("Check Doc : "+docNoContent);*/
						     else{
						     textContent=(s.substring(textStartTag.length(), s.length()).trim()+" ");
						     invInd.searchTermInDoc("gener",textContent);
						     }
						    }
						    else if(s.indexOf(textEndTag)!=-1)
							    inText=0;
						    else if(inText==1){  
						    	textContent=(s.trim()+" ");
						    	invInd.searchTermInDoc("gener",textContent);
						    }
						}	
					}	
			} // end of for(File f:files)
		}
		finally{
			br.close();
			fr.close();		
		}
		invInd.termDetails.append("#");
		System.out.println(invInd.termDetails);
		etime=System.currentTimeMillis();
		System.out.println("Time Taken for Indexing in seconds: "+(etime-stime)/1000);
		
		//uncomment the following line to write all docnos into a file
		 //ir.writeTermsToFile(allterms);
        /*for(String words:vocabulary)
        	System.out.println(words);
        System.out.println("number of unique words = "+vocabulary.size());*/
		
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
public void searchTermInDoc(String sTerm,String sText){
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(sText);
	List<String> sArray=new ArrayList<String>();
	PorterStemmer stemmer = new PorterStemmer();
	String stemmedS;
	while(mat.find()){
		sArray.add(mat.group());
		//System.out.println(mat.group());
	}
	for(String s:sArray){
		if(s.matches("[A-Z][\\.][A-Z]"))
	    	s=s+".";
		s=s.toLowerCase();
		stemmedS=stemmer.stem(s);
		if(s.length()!=1 && !stopWords.contains(s)){
			System.out.println(stemmedS);
			posCounter++;
		}
		if(stemmedS.equals(sTerm))
			{
			  System.out.println(posCounter);
			  termDetails.append(posCounter+",");
			  tfCounter++;
			  
			}
		
	}
	
}

}
