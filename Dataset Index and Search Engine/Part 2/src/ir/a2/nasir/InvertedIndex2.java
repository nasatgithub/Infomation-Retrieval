package ir.a2.nasir;

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
	TreeMap<Long,String> partialTermMap;
	TreeMap<Long,String> secondaryTermMap;
	public InvertedIndex2(){
		posCounter=0;
		tfCounter=0;
		invFormat=new ArrayList<String>();
		termDetails=new StringBuffer("");
		partialTermMap=new TreeMap<Long,String>();
		secondaryTermMap=new TreeMap<Long,String>();
	}
	public static void main(String a[]) throws Exception{
		long stime,etime;
		stime=System.currentTimeMillis();
		String s;
		int docCount=0;
		FileReader fr=null;
		BufferedReader br=null;
		String foldername="TupleFiles";
		File folder=new File(foldername);
		File[] files=folder.listFiles();
		System.out.println("Processing Index... Please wait for about 2.5 min");
	    InvertedIndex2 invInd=new InvertedIndex2();
		File f0=new File("./OutputFiles/pInvertedIndex.txt");
		File f1=new File("./OutputFiles/indexCatalog.txt");
		File f2=new File("./OutputFiles/secondaryTermMap.txt");
		File f3=new File("C:/Users/NasirAhmed/invertedIndex.txt");
	    System.out.println("File Delete Status:\t"+f0.delete()+"\t"+f1.delete()+"\t"+f2.delete()+"\t"+f3.delete());
		int docLimit=0;
		try{
		for(File f:files){
					fr=new FileReader(f);
					br=new BufferedReader(fr);	
					System.out.println("Processing File: "+f.getName());
					String document;
					invInd.partialTermMap=new TreeMap<Long,String>();	
					while((document=br.readLine())!=null){	
						if(!document.equals(""))
						invInd.IndexHashDoc(document);
					}
				invInd.writePartialTermMap(invInd.partialTermMap);	
			} // end of for(File f:files)
		}
		finally{
			br.close();
			fr.close();		
		}
		//System.out.println(invInd.termDetails);
		invInd.writeSecondaryPTemp();
		invInd.mergePartialList();
		etime=System.currentTimeMillis();
		System.out.println("Time Taken for Indexing in seconds: "+(etime-stime)/1000);
		
		System.out.println("This is done !!!");
		
		//uncomment the following line to write all docnos into a file
		 //ir.writeTermsToFile(allterms);
     	
	}


public List<String> readStopWords() throws IOException{
	Path path = Paths.get("./Stopwords/stoplist.txt");
    return Files.readAllLines(path, StandardCharsets.UTF_8);
}

public void IndexHashDoc(String doc){
	HashMap<Long,String> termIndex=new HashMap<Long,String>();
	HashMap<Long,Integer> tfCounter=new HashMap<Long,Integer>();
	String[] tupleSplits=doc.split("#");
	int docId=0;
	for(String term:tupleSplits){
		
		String[] termSplits=term.split(",");
		if(termSplits[0].equals(""))
			System.out.println("ERROR"+doc);
		long termKey=Integer.parseInt(termSplits[0]);
		int termPos=Integer.parseInt(termSplits[2]);
		if(termPos==1)
			docId=Integer.parseInt(termSplits[1]);
		
		if(!termIndex.containsKey(termKey))
		{
			termIndex.put(termKey, "$"+docId+":"+termPos);
			tfCounter.put(termKey, 1);
		}
		else
		{
			termIndex.put(termKey, termIndex.get(termKey)+","+termPos);
			tfCounter.put(termKey, tfCounter.get(termKey)+1);
		}
	}
	
	for(long termKey: tfCounter.keySet()){
		
		termIndex.put(termKey, termIndex.get(termKey)+":"+tfCounter.get(termKey));
		String termIndexLine=termIndex.get(termKey);
		if(!partialTermMap.containsKey(termKey)){
			partialTermMap.put(termKey, termIndexLine);
			
		}
		else{
		   String[] tIndexSplits=termIndexLine.split("\\$"); 
		   partialTermMap.put(termKey, partialTermMap.get(termKey)+"#"+tIndexSplits[1]);	
		}
	}	
}
public void writePartialTermMap(TreeMap<Long,String> pTMap) throws IOException{
	RandomAccessFile raf=new RandomAccessFile("./OutputFiles/pInvertedIndex.txt", "rw");
	long currentFilePointer;
	raf.seek(raf.length());      // going to the end of file to write
	for(long termKey:pTMap.keySet())
	{
		currentFilePointer=raf.getFilePointer();
		if(!secondaryTermMap.containsKey(termKey))
			secondaryTermMap.put(termKey, Long.toString(currentFilePointer));
		else
			secondaryTermMap.put(termKey, 
					             secondaryTermMap.get(termKey)+"#"+Long.toString(currentFilePointer));
		
		raf.writeBytes(termKey+pTMap.get(termKey)+"\n");		
	}	
}
public void writeSecondaryPTemp() throws IOException{
	List<String> secPTempList=new ArrayList<String>();
	for(long termKey:secondaryTermMap.keySet())
		secPTempList.add(termKey+"#"+secondaryTermMap.get(termKey));
	Path path=Paths.get("./OutputFiles/secondaryTermMap.txt");
	Files.write(path, secPTempList, StandardCharsets.UTF_8);	
	
}

public void mergePartialList()throws IOException{
	RandomAccessFile rafSec=new RandomAccessFile("./OutputFiles/secondaryTermMap.txt", "r");
    RandomAccessFile rafPartial=new RandomAccessFile("./OutputFiles/pInvertedIndex.txt", "r");
    RandomAccessFile rafCatalog=new RandomAccessFile("./OutputFiles/indexCatalog.txt", "rw");
	RandomAccessFile rafFinal=new RandomAccessFile("C:/Users/NasirAhmed/invertedIndex.txt","rw");

	String line,pLine;
	String[] termMapSplits;
	String[] pIndexSplits;
	String[] pIndexContSplits;
	int tFound=0;
	long finalIndexPointer=0;
	while((line=rafSec.readLine())!=null){
		termMapSplits=line.split("#");	
		String termId=termMapSplits[0];
		tFound=0;
		for(int i=1;i<termMapSplits.length;i++)
		{
			rafPartial.seek(Long.parseLong(termMapSplits[i]));
			pLine=rafPartial.readLine();
			if(i==1){
			finalIndexPointer=rafFinal.getFilePointer();
			rafCatalog.seek(rafCatalog.length());
			rafCatalog.writeBytes(termId+"#"+finalIndexPointer);
			rafFinal.writeBytes(pLine);
			}
			else{
			 pIndexContSplits=pLine.split("\\$");
			 rafFinal.writeBytes("#"+pIndexContSplits[1]);
			}
		}
		rafCatalog.writeBytes("\n");
		rafFinal.writeBytes("\n");
	}
	
	
	rafSec.close();
	rafPartial.close();
	rafCatalog.close();
	rafFinal.close();
	
	
		
}
}
