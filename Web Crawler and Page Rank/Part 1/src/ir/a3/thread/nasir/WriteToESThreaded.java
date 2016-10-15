package ir.a3.thread.nasir;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class WriteToESThreaded implements Runnable {
	Thread t;
	HashMap<String,PageData> pageDataMap;
	HashMap<String,UrlIC> urlDataMap;
	Node node;
	Client client;
	public WriteToESThreaded
	(Node node,Client client,HashMap<String,PageData> pageDataMap,HashMap<String,UrlIC> urlDataMap){
	t=new Thread(this);
	this.pageDataMap=pageDataMap;
	this.urlDataMap=urlDataMap;
	this.node=node;
	this.client=client;
	t.start();
	}
    public void run() {
    	  
    	  HashMap<String,Object> jsonData=new HashMap<String,Object>();
    	  List<String> writtenUrls=new ArrayList<String>();
    	  for(String url:pageDataMap.keySet()){
    		  PageData p=pageDataMap.get(url);
    		  jsonData.put("docno", url);
    		  jsonData.put("head",p.getTitle());
    		  if(!p.getText().isEmpty())
    			  jsonData.put("text", p.getText());
    		  if(!p.getRawHtml().isEmpty())
    			  jsonData.put("rawhtml", p.getRawHtml());
    		  UrlIC urlData=urlDataMap.get(url);
    		  List<String> outlinks=urlData.getOutlinks();
    		  if(!outlinks.isEmpty())
    			  jsonData.put("outlinks", outlinks);
    		/*  List<String> inlinks=urlData.getInlinks();
    		  if(!inlinks.isEmpty())
    			  jsonData.put("inlinks", inlinks);	 */ 
    		  client.prepareIndex("crawlnasir","document",url)
    	       .setSource(jsonData)
    	       .execute()
    	       .actionGet();
    		  urlData.setIndexed(1);
    		  urlData.clearLinks();
    		  writtenUrls.add(url);
	   }
       	try {
			writeRandomFile(writtenUrls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
       	pageDataMap.clear();
       	System.out.println("500 urls written to index");
    
   }
    public void writeRandomFile(List<String> urls) throws Exception{
    	RandomAccessFile raf=new RandomAccessFile("./Outputfiles/urlMap.txt", "rw");
    	raf.seek(raf.length());
    	for(String url: urls){
    		raf.writeBytes(url+"\n");
    	}
    	raf.close();
    }
}
