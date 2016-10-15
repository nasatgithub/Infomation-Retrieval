package ir.assignment2.nasir;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class ReadRawTerms {
	private int startRange;
	
public ReadRawTerms(){
	startRange=1;
}
public static void main(String a[]) throws IOException{
ReadRawTerms rawT=new ReadRawTerms();
//int vocSize=17;
TreeMap<Integer,String> termMap=rawT.readTermsRange();
for(int id: termMap.keySet())
	System.out.println(id+"#"+termMap.get(id));
System.out.println();

/*while(rawT.startRange<vocSize){
	termMap=rawT.readTermsRange(5);
	System.out.println(termMap);
}*/
}
public TreeMap<Integer,String> readTermsRange()throws IOException{
	FileReader f=new FileReader("./OutputFiles/allTerms.txt");
	BufferedReader br=new BufferedReader(f);
	String line;
	String[] splits=null;
	//int endRange=startRange+range;
	int termId;
	String term;
	int ttf;
	TreeMap<Integer,String> termMap=new TreeMap<Integer,String>();
	while((line=br.readLine())!=null){
		splits=line.split("#");
	    termId=Integer.parseInt(splits[0]);
	    term=splits[1];
	    ttf=Integer.parseInt(splits[2]);
	   // if(termId>=startRange&&termId<endRange) //required Indexing Operation To Be performed
	    termMap.put(termId,term);
	 }
	//System.out.println(startRange+"--------------------------------------------------------------"+(endRange-1));
	//startRange=endRange;
	return termMap;
}
public HashMap<String,Integer> readTermsRangeInverted()throws IOException{
	FileReader f=new FileReader("./OutputFiles/allTerms.txt");
	BufferedReader br=new BufferedReader(f);
	String line;
	String[] splits=null;
	//int endRange=startRange+range;
	int termId;
	String term;
	int ttf;
	HashMap<String,Integer> termMap=new HashMap<String,Integer>();
	while((line=br.readLine())!=null){
		splits=line.split("#");
	    termId=Integer.parseInt(splits[0]);
	    term=splits[1];
	    ttf=Integer.parseInt(splits[2]);
	   // if(termId>=startRange&&termId<endRange) //required Indexing Operation To Be performed
	    termMap.put(term,termId);
	 }
	//System.out.println(startRange+"--------------------------------------------------------------"+(endRange-1));
	//startRange=endRange;
	return termMap;
}
}
