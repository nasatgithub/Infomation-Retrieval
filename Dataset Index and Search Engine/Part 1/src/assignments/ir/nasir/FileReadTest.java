package assignments.ir.nasir;

import groovy.json.JsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.XContentFactory.*;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import org.elasticsearch.client.Client;

import static org.elasticsearch.node.NodeBuilder.*;

public class FileReadTest {
public static void main(String a[]) throws Exception{
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
	StringBuffer docNoContent=new StringBuffer("");
	StringBuffer textContent=new StringBuffer("");
	List<String> docnos=new ArrayList<String>();
	FileReader fr=null;
	BufferedReader br=null;
	String foldername="Files";
	File folder=new File(foldername);
	File[] files=folder.listFiles();
	for(File f:files){
		//System.out.println("<<Processing File..>>");
		try{
				fr=new FileReader(f);
				br=new BufferedReader(fr);	
				while((s=br.readLine())!=null){
					if(s.indexOf(docStartTag)!=-1){
						 //System.out.println("<<Processing Doc..>>");
						 inDoc=1;

						}
					else if(s.indexOf(docEndTag)!=-1){
					    inDoc=0;
					    //System.out.println("* DOC NUMBER:"+ docNoContent);
					    //System.out.println("* TEXT CONTENT"+textContent+"\n --------------------------------------");
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
					    	docNoContent.append("\n");
					    	docNoContent.append(s.trim());
					    	docnos.add(s.trim());
					    }
					    
					    // Block to extract the content between <TEXT> .. </TEXT> in a <DOC>
					    if(s.indexOf(textStartTag)!=-1){
					     inText=1;
					     if(s.indexOf(textEndTag)!=-1){
					    	 textContent.append(s.substring(textStartTag.length(), s.indexOf(textEndTag)).trim()); 
					    	 inText=0;
					     }
					     textContent.append(s.substring(textStartTag.length(), s.length()).trim());
					    }
					    else if(s.indexOf(textEndTag)!=-1)
						    inText=0;
					    else if(inText==1){
					    	textContent.append("\n");
					    	textContent.append(s.trim());
					    }
					}	
				}	
		  
		}
		finally{
			br.close();
			fr.close();
		}
	//System.out.println("## END OF FILE ##");
	}
	for(String doc: docnos){
	System.out.println(doc);	
	}
	
	
}
}
