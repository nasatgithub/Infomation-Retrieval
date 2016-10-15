package ir.a3.thread.nasir;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.jsoup.nodes.Document;

public class PageFetchThread implements Runnable{
	Thread t;
	HashMap<String,PageData> pageDataMap;
	String title;
	String text;
	String rawHtml;
	Document doc;
    public PageFetchThread(Document doc){
    	this.doc=doc;
    	t=new Thread(this);
    	t.start();
    }
    public void run() {
    	 title=doc.title();
    	 //text=doc.text();
    	 rawHtml=doc.outerHtml();   
   }
    public void writeFile(List<String> urls) throws Exception{
    	Path path = Paths.get("./OutputFiles2/urlMap"+WebCrawlerController.fileCounter+".txt");
    	Files.write(path, urls, StandardCharsets.UTF_8);
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
