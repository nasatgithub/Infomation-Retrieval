package assignments.ir.nasir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.client.Client;
import static org.elasticsearch.node.NodeBuilder.*;
import org.elasticsearch.client.transport.TransportClient;

public class IRIndexing {
	private HashMap<String, Object> json;
	public IRIndexing() {
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
        
		// Starting Elastic Search's Node and Client
		Node node=nodeBuilder().clusterName("irnasir").client(true).node();
		Client client=node.client();
		System.out.println("Processing Index... Please wait for about 2.5 min");
		IRIndexing ir=new IRIndexing();
		IndexResponse response=null;
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
						    response=client.prepareIndex("ap_dataset","document",new String(docNoContent))
						    		       .setSource(ir.createJson(docNoContent, textContent))
						    		       .execute()
						    		       .actionGet();
						   
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
			}
		}
		finally{
			br.close();
			fr.close();
			node.close();
			client.close();
			
			
		}
		
		 //uncomment the following line to write all docnos into a file
		 ir.writeDocNosToFile(docnos);
		
		etime=System.currentTimeMillis();
		System.out.println("INDEXING COMPLETED SUCCESSFULLY");
		System.out.println("Number of Documents processed and Indexed = "+docCount);
		System.out.println("Time Taken for Indexing in seconds: "+(etime-stime)/1000);
		
		// Call to compute Doc Length and  TTFs
		String empty[]=new String[1];
		RetrieveDocLen.main(empty);
	}
public HashMap<String, Object> createJson(StringBuffer docNo,StringBuffer text){
	json=new HashMap<String,Object>();
	json.put("docno", docNo);
	json.put("text", text);
	return json;
}
public void writeDocNosToFile(List<String> docNos) throws IOException{
	Path path=Paths.get("./OutputFiles/docnoslist.txt");
	Files.write(path, docNos, StandardCharsets.UTF_8);
}
}
