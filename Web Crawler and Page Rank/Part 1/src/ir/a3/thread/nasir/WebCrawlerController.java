package ir.a3.thread.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.translog.fs.RafReference;
import org.elasticsearch.node.Node;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawlerController {
public PriorityQueue<UrlIC> frontier;
public HashMap<String,UrlIC> urlDataMap;
public HashMap<String,PageData> pageDataMap;
public HashMap<String,Long> domainTimeAccessMap;
public HashSet<String> searchTerms;
public HashSet<String> irrelevantURLS;
public static int fileCounter;
public static int urlCount;
Node node;
Client client;
public static long startTime,endTime;

public WebCrawlerController(){
	frontier=new PriorityQueue<UrlIC>();
	urlDataMap=new HashMap<String, UrlIC>();
	pageDataMap=new HashMap<String,PageData>();
	domainTimeAccessMap=new HashMap<String,Long>();
	searchTerms=new HashSet<String>();
	irrelevantURLS=new HashSet<String>();
	fileCounter=0;
	urlCount=0;
	node=nodeBuilder().clusterName("ana").client(true).node();
	client=node.client();
}
public static void main(String a[]) throws Exception{
	
	File f1=new File("./OutputFiles/urlMap.txt");
	File f2=new File("./OutputFiles/inlinks.txt");
	System.out.println("UrlMap file delete "+f1.delete());
	System.out.println("Inlinks file delete "+f2.delete());
	WebCrawlerController wc=new WebCrawlerController();
	wc.startCrawler();
	endTime=System.currentTimeMillis();
	System.out.println("<< TOTAL TIME TO CRAWL = "+(endTime-startTime)/1000+" seconds  >>");
	
}
public void startCrawler() throws Exception{
	
	
	String seed1="http://en.wikipedia.org/wiki/Barack_Obama";
	String seed2="http://en.wikipedia.org/wiki/Barack_Obama_presidential_campaign,_2008";
	String seed3="http://en.wikipedia.org/wiki/United_States_presidential_election,_2008";
	List<String> urls=new ArrayList<String>();
	List<String> canonUrls=new ArrayList<String>();
	WriteToESThreaded ws,wsLast; 
	ws=wsLast=null;
	urls.add(seed1);
	urls.add(seed2);
	urls.add(seed3);
	String canonUrl;
	Comparator<UrlIC> comparator = new UrlComparator2();
	frontier=new PriorityQueue<UrlIC>(10,comparator);
	for(String url: urls){
		UrlIC u=new UrlIC();
		canonUrl=Canonicalization.canonicalize(url);
		u.setUrl(canonUrl);
	    u.setTime(System.currentTimeMillis());
	    u.setLevel(0);
		frontier.add(u);
		urlDataMap.put(url, u);
		canonUrls.add(canonUrl);
	}
	searchTerms=Canonicalization.getUrlQueries(canonUrls);
	URL myUrl;
	String domain;
	long td;
	String url="";
	startTime=System.currentTimeMillis();
	while((!frontier.isEmpty()) && urlCount<12000){
			try{
				System.out.println("Frontier Size = "+frontier.size());
				UrlIC fData=frontier.poll();// Dequeue
				url=fData.getUrl();
				//frontierDetailMap.remove(url);
				int urlLevel=fData.getLevel();
				/*if(blockLevel.contains(urlLevel))
					continue;*/
				myUrl=new URL(url);
				domain=myUrl.getHost();
				if(domainTimeAccessMap.containsKey(domain)){
					td=System.currentTimeMillis()-domainTimeAccessMap.get(domain);
					if(td<=1000){
						//System.out.println("Sleeping.. "+(1000-td)+" ms");
						Thread.sleep(1000-td);
					}
				}
				//process enqueued url
				if(RobotParser.parse(url)){
				processURL(url,urlLevel);	
				}
				else
					continue;
				//System.out.println("Page Size = "+pageDataMap.size());
				if(pageDataMap.size()>=500){
					fileCounter++;
					ws=new WriteToESThreaded(node, client, pageDataMap, urlDataMap);
					pageDataMap=new HashMap<String,PageData>();
				}
		 }
		catch(SocketTimeoutException se){
				System.out.print("SocketTimeOutException on : "+url);
			}
		catch(Exception e){
			System.out.print("Exception on : "+url);
		}		
	}
	try{
		ws.t.join();
		if(pageDataMap.size()>0){
			wsLast=new WriteToESThreaded(node, client, pageDataMap, urlDataMap);
			wsLast.t.join();
		}
	}
	catch(Exception e){
		e.printStackTrace();
	}
	writeInlinksToFile();
	writeInlinks();
	node.close();
	client.close();
	
	
	
	
}
public void processURL(String url,int urlLevel) throws Exception{
	PageFetchThread pt=null;
	URL myUrl=new URL(url);
	UrlIC urlData;
	PageData pData;
	HashSet<String> seenPerPage=new HashSet<String>();
    domainTimeAccessMap.put(myUrl.getHost(), System.currentTimeMillis());
	//Document doc=Jsoup.parse(myUrl, 30000);
    Document doc=Jsoup.connect(url).get();
	String text,title,rawHtml;
	int relevantPageFlag=0;
	text=doc.text();
	for(String w: searchTerms){
		if(text.contains(w))
		{
			relevantPageFlag=1;
			break;
		}
	}
	//code to check if the extracted content from the page has words relevant to url query terms
	if(relevantPageFlag==1){
		if(!urlDataMap.containsKey(url))
		 { 
			urlData=new UrlIC();
			urlDataMap.put(url,urlData);
		 }
		else
			urlData=urlDataMap.get(url);	
		urlCount++;
		pt=new PageFetchThread(doc);
		/*title=getRelevantText(doc.title()).trim();
		text=getRelevantText(doc.text()).trim();
		rawHtml=doc.html();*/
		System.out.println("Crawling url "+urlCount+" : "+url+"\t level = "+urlLevel);
	/* Following is the code block to determine all the outlinks for the current processed url. All the links 
	 * found from the current url page is checked whether it has been already seen in urlDataMap. If it is not found 
	 * a new urlData is created for it and the current url is stored as one of it's inlinks, else it is fetched
	 * from the urlDataMap and the inlink is added. The inlinks found are also added to the frontier for future 
	 * processing if it is not already visited.*/
		Elements outlinks=doc.select("a[href]");
		for(Element outlink: outlinks){
			String urlOutlink=Canonicalization.canonicalize(outlink.absUrl("href"));
			if(urlOutlink.equals("#INVALID_URL"))
				continue;
			// add the newly found outlinks into the frontier queue iff it is not visited and 
			// not already present in frontier and has a valid extension
			UrlIC uOutlinkData=null;
			if(!validExt(urlOutlink))
				continue;
			if(!urlDataMap.containsKey(urlOutlink) && !frontier.contains(urlOutlink)
					&& !irrelevantURLS.contains(urlOutlink) && frontier.size()<=300000){
				uOutlinkData=new UrlIC();
				uOutlinkData.setUrl(urlOutlink);
				uOutlinkData.setTime(System.currentTimeMillis());
				uOutlinkData.setLevel(urlLevel+1);
				urlDataMap.put(urlOutlink, uOutlinkData);
				frontier.add(uOutlinkData);
				//frontierDetailMap.put(urlOutlink, uOutlinkData);
			}
			 
			if(!seenPerPage.contains(urlOutlink)){
				
				seenPerPage.add(urlOutlink);
				//store outlinks into current urlData
				urlData.addOutlinks(urlOutlink);
	            if(urlDataMap.containsKey(urlOutlink))
	            	uOutlinkData=urlDataMap.get(urlOutlink);
	            
				if(uOutlinkData!=null){
	            uOutlinkData.addInlinks(url);	
				urlDataMap.put(urlOutlink, uOutlinkData);
				}
				
				// Also update frontierDetailMap so that the PriorityQueue is sorted accordingly
				if(frontier.contains(urlOutlink) ){
				 UrlIC modU=urlDataMap.get(urlOutlink);
				 modU.incIc();
				 frontier.remove(modU);
				 frontier.add(modU);
				}
			} // end of seen per page 
		} // end of for loop
		urlDataMap.put(url, urlData);
		pt.t.join();
		pData=new PageData();
		pData.setTitle(pt.title);
		pData.setText(text);
		pData.setRawHtml(pt.rawHtml);
		pageDataMap.put(url, pData);
	} //end of if  relevant page
	else{
		irrelevantURLS.add(url);
	}
/*	if(levelCountMap.get(urlLevel)>3000)
		blockLevel.add(urlLevel);*/
	/*
	 * Block to store page response in a file 
	 */
      //writePageDataToFile(pageDataMap);
	  //writePageDataToES(pageDataMap,urlDataMap);	
}
/*public PriorityQueue<UrlIC> retainTop(){
	UrlIC urlic;
	while(frontier.size()>100000){
		frontier.remove();
		frontierDetailMap.remove(urlic.getUrl());
	}
}*/

public void writePageDataToFile(HashMap<String,PageData> pageDataMap) throws Exception{
  List<String> data=new ArrayList<String>();
  for(String url:pageDataMap.keySet()){
	  PageData p=pageDataMap.get(url);
	  data.add("<DOC>");
	  data.add("<DOCNO>"+url+"</DOCNO>");
	  data.add("<HEAD>"+p.getTitle()+"</HEAD>");
	  data.add("<TEXT>");
	  if(!p.getText().isEmpty())
		  data.add(p.getText());
	  data.add("</TEXT>");
	  data.add("</DOC>");
  }
  Path path = Paths.get("./OutputFiles/doc"+WebCrawlerController.fileCounter+".txt");
  Files.write(path, data, StandardCharsets.UTF_8);
}
public void writePageDataToES(HashMap<String,PageData> pageDataMap,HashMap<String,UrlIC> urlDataMap){
	  HashMap<String,Object> jsonData=new HashMap<String,Object>();
	  for(String url:pageDataMap.keySet()){
		  PageData p=pageDataMap.get(url);
		  jsonData.put("docno", url);
		  jsonData.put("head",p.getTitle());
		  if(!p.getText().isEmpty())
			  jsonData.put("text", p.getText());
		  List<String> outlinks=urlDataMap.get(url).getOutlinks();
		  if(!outlinks.isEmpty())
			  jsonData.put("outlinks", outlinks);
		  List<String> inlinks=urlDataMap.get(url).getInlinks();
		  if(!inlinks.isEmpty())
			  jsonData.put("inlinks", inlinks);	  
		  client.prepareIndex("crawl","document",url)
	       .setSource(jsonData)
	       .execute()
	       .actionGet();
	  }
}
public String getRelevantText(String docText){
	Pattern pat=Pattern.compile("[A-Za-z0-9]+(\\.?[A-Za-z0-9]+)*");
	Matcher mat=pat.matcher(docText);
	StringBuffer text=new StringBuffer("");
	while(mat.find()){
		String s=mat.group();
		if(s.matches("[A-Z][\\.][A-Z]"))
	    	s=s+".";
		s=s.toLowerCase();
		text.append(s+" ");
	}
	return new String(text);	
}

public boolean validExt(String url){
	return (!url.matches("[\\w].*\\.[a|jp|i|d|m|p|pp|ph|o|s|c|pd|PD|D|S|t|b|x|f|n|g|v|r|y|(0-9)]+$"));
}
public void writeInlinksToFile(){
	RandomAccessFile rafInlinks=null;
	try{
		rafInlinks=new RandomAccessFile("./OutputFiles/inlinks.txt","rw");
		System.out.println("Writing INLINKS TO Inlinks File now.....");	
		List<String> urls=myRead("./OutputFiles/urlMap.txt");
		List<String> inlinks=new ArrayList<String>();
		for(String url: urls){
			rafInlinks.seek(rafInlinks.length());
			UrlIC urlData=urlDataMap.get(url);
			if(urlData!=null && urlData.getInlinks()!=null){
				inlinks=urlData.getInlinks();
				rafInlinks.writeBytes(url+"#");
				for(String inlink: inlinks)
					rafInlinks.writeBytes(inlink+"\t");
				rafInlinks.writeBytes("\n");
			}
		}
		rafInlinks.close();
	}
	catch(ElasticsearchException ese){
		ese.printStackTrace();
	}
	catch(Exception e){
		e.printStackTrace();
	}
}
public void writeInlinks() throws Exception{
	try{
		System.out.println("Writing INLINKS TO ES now.....");	
		List<String> urls=myRead("./OutputFiles/urlMap.txt");
		for(String url: urls){
			UrlIC urlData=urlDataMap.get(url);
			if(urlData!=null && urlData.getInlinks()!=null){
			XContentBuilder jsonbuilder=XContentFactory.jsonBuilder();
			XContentBuilder remInlinksUpdate=jsonbuilder.startObject()
														    .field("script","ctx._source.inlinks = newlinks") 
														      .startObject("params") 
														       .field("newlinks", urlData.getInlinks()) 
														      .endObject()
														    .endObject(); 
			client.prepareUpdate("crawlnasir","document",url)
	 	          .setSource(remInlinksUpdate)
	 	          .execute()
	 	          .actionGet();	
			}
		}
	}
	catch(ElasticsearchException ese){
		ese.printStackTrace();
	}
	catch(Exception e){
		e.printStackTrace();
	}
}
public static List<String> myRead(String filename) throws Exception{
    
	FileReader f=new FileReader(filename);
	BufferedReader br=new BufferedReader(f);
	String s;
	List<String> lines=new ArrayList<String>();
	s=br.readLine();
	int c=0;
	while(s!=null){
	 try{
	   c++;
	   lines.add(s);
	   s=br.readLine();
	 }
	 catch(Exception e){
		 System.out.println("Error on line : "+s);
	 }
	}
	System.out.println(c+" lines read from file : "+filename);
	br.close();
	return lines;	
}
public static List<String> myRead2(String filename) throws Exception{
    
	FileReader f=new FileReader(filename);
	BufferedReader br=new BufferedReader(f);
	String s;
	List<String> lines=new ArrayList<String>();
	s=br.readLine();
	int c=0;
	while(s!=null){
	 try{
	   c++;
	   String[] splits=s.split(" ");
	   s=splits[0];
	   lines.add(s);
	   s=br.readLine();
	 }
	 catch(Exception e){
		 System.out.println("Error on line : "+s);
	 }
	}
	System.out.println(c+" lines read from file : "+filename);
	br.close();
	return lines;	
}
}

class UrlComparator2 implements Comparator<UrlIC>
{
    @Override
    public int compare(UrlIC x, UrlIC y)
    {
    	if(x.getLevel() < y.getLevel())
     	   return -1;
     	else if(x.getLevel() > y.getLevel())
      	   return 1;
     	else{
 	    	if (x.getIc() > y.getIc())
 	        {
 	            return -1;
 	        }
 	        else if (x.getIc() < y.getIc())
 	        {
 	            return 1;
 	        }
 	        else{
 	        	if(x.getTime() < y.getTime())
 	        		return -1;
 	        	else if(x.getTime() > y.getTime())
 	        		return 1;
 	        }
     	}
        
        return 0;
    }
}
