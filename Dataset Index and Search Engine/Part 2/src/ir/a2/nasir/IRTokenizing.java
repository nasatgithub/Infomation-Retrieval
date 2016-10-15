package ir.a2.nasir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class IRTokenizing {
	private HashMap<String, Object> json;
	public IRTokenizing() {
		json=new HashMap<String,Object>();

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
	    HashSet<String> vocabulary=new HashSet<String>();
        MyTokenizer tok=new MyTokenizer();
		
		System.out.println("Processing Tokenizing... Please wait for about 2.5 min");
		IRTokenizing ir=new IRTokenizing();
		List<String> stopWords=ir.readStopWords();
		int docLimit=0;
		int fileC=0;
		try{
		for(File f:files){
					fr=new FileReader(f);
					br=new BufferedReader(fr);	
					while((s=br.readLine())!=null){
						if(s.indexOf(docStartTag)!=-1){
							 inDoc=1;
							 docLimit++;
							 docCount++;
							}
						else if(s.indexOf(docEndTag)!=-1){
						    inDoc=0;
						    
						    //System.out.println(textContent);
						    String text=new String(textContent);
						    if(!text.trim().equals(""))
						      {
						    	tok.tokenize(text,stopWords,new String(docNoContent));
						    	
						      }
						    else
						    	tok.docNameMap.put(++tok.docIdCounter, docNoContent+"#"+0);
						    if(docLimit==1000)
						    {   fileC++;
						        ir.prepareDocTuples(tok.docTuples,"./TupleFiles/Tuple"+fileC);
						    	tok.docTuples=new TreeMap<Integer,String>();
						    	docLimit=0;
						    	System.out.println("Processed : "+docCount);
						    }
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
						     inDocNo=0;
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
						     textContent.append(s.substring(textStartTag.length(), s.length()).trim()+" ");
						    }
						    else if(s.indexOf(textEndTag)!=-1)
							    inText=0;
						    else if(inText==1){
						  
						    	textContent.append(s.trim()+" ");
						    
						    }
						}	
					}	
			}// end of for(f:Files)
		// writing remaining document tuple details
		fileC++;
		if(!tok.docTuples.isEmpty())
        ir.prepareDocTuples(tok.docTuples,"./TupleFiles/Tuple"+fileC);
		}
		finally{
			br.close();
			fr.close();		
		}
		etime=System.currentTimeMillis();
		/*System.out.println("INDEXING COMPLETED SUCCESSFULLY");
		System.out.println("Number of Documents processed and Indexed = "+docCount);*/
		System.out.println("Time Taken for Tokenizing in seconds: "+(etime-stime)/1000);
		

	    ir.prepareTermMapFile(tok.uniqueWords,tok.vocTtf);
	    ir.prepareDocMapFile(tok.docNameMap);
		//uncomment the following line to write all docnos into a file
		 

		
	}

public void writeListToFile(List<String> list,String filename) throws IOException{
	Path path=Paths.get(filename);
	Files.write(path, list, StandardCharsets.UTF_8);
}
public List<String> readStopWords() throws IOException{
	Path path = Paths.get("./Stopwords/stoplist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}
public void prepareTermMapFile(HashMap<String,Long> uniqueWords,HashMap<Long,Integer> vocTtf) throws IOException{
	
	List<String> termMapList=new ArrayList<String>();
	for(String term:uniqueWords.keySet()){
		long termId=uniqueWords.get(term);
		termMapList.add(termId+"#"+term+"#"+vocTtf.get(termId));
	}
    writeListToFile(termMapList,"./OutputFiles/termMap.txt");
}
public void prepareDocTuples(TreeMap<Integer,String> docTuple,String filename) throws IOException{
	List<String> docTupleList=new ArrayList<String>();
	for(int docId:docTuple.keySet()){
		docTupleList.add(docTuple.get(docId));
	}
	writeListToFile(docTupleList,filename);
}
public void prepareDocMapFile(HashMap<Integer,String> docNameMap) throws IOException{
	TreeMap<Integer,String> sDocNameMap=new TreeMap<Integer,String>(docNameMap);
	List<String> docMapList=new ArrayList<String>();
	for(int docId:sDocNameMap.keySet()){
		docMapList.add(docId+"#"+sDocNameMap.get(docId));
	}
    writeListToFile(docMapList,"./OutputFiles/docMap.txt");
}
}

