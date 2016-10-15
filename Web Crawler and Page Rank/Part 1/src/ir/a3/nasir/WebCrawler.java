package ir.a3.nasir;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.ObjectInputStream.GetField;
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

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
public PriorityQueue<UrlIC> frontier;
public HashMap<String,UrlIC> frontierDetailMap;
public HashMap<String,UrlBean> urlDataMap;
public HashMap<String,PageData> pageDataMap;
public HashSet<String> visitedUrlSet;
public HashMap<String,Long> domainTimeAccessMap;
private HashMap<String, Object> json;
public HashSet<String> searchTerms;
public HashMap<Integer,Long> levelCountMap;
public List<Integer> blockLevel;
public static int fileCounter;
public static int urlCount;
Node node;
Client client;

public WebCrawler(){
	frontier=new PriorityQueue<UrlIC>();
	frontierDetailMap=new HashMap<String,UrlIC>();
	urlDataMap=new HashMap<String, UrlBean>();
	pageDataMap=new HashMap<String,PageData>();
	visitedUrlSet=new HashSet<String>();
	domainTimeAccessMap=new HashMap<String,Long>();
	searchTerms=new HashSet<String>();
	levelCountMap=new HashMap<Integer,Long>();
	json=new HashMap<String,Object>();
	blockLevel=new ArrayList<Integer>();
	fileCounter=0;
	urlCount=0;
	node=nodeBuilder().clusterName("ir3").client(true).node();
	client=node.client();
}
public static void main(String a[]) throws Exception{
	long startTime,endTime;
	startTime=System.currentTimeMillis();
	WebCrawler wc=new WebCrawler();
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
	urls.add(seed1);
	urls.add(seed2);
	urls.add(seed3);
	String canonUrl;
	Comparator<UrlIC> comparator = new UrlComparator();
	frontier=new PriorityQueue<UrlIC>(10,comparator);
	for(String url: urls){
		UrlIC u=new UrlIC();
		canonUrl=Canonicalization.canonicalize(url);
		u.setUrl(canonUrl);
	    u.setTime(System.currentTimeMillis());
	    u.setLevel(0);
		frontier.add(u);
		frontierDetailMap.put(url, u);
		canonUrls.add(canonUrl);
	}
	searchTerms=Canonicalization.getUrlQueries(canonUrls);
	URL myUrl;
	String domain;
	long td;
	String url="";
	while((!frontier.isEmpty()) && urlCount<12000){
			try{
				System.out.println("Frontier Size = "+frontier.size());
				UrlIC fData=frontier.remove(); // Enqueue
				url=fData.getUrl();
				frontierDetailMap.remove(url);
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
				//UrlIC uc=frontierDetailMap.get(url);
				
			/*	System.out.println("inlink count = "+uc.getIc());
				System.out.println("level = "+uc.getLevel());
				System.out.println("time in queue "+ uc.getTime());*/
				//process enqueued url
				if(RobotParser.parse(url)){
				processURL(url,urlLevel);	
				}
				else
					continue;
				System.out.println("Page Size = "+pageDataMap.size());
				if(pageDataMap.size()>100){
					//writePageDataToFile(pageDataMap);
					//writePageDataToES(pageDataMap,urlDataMap);	
					fileCounter++;
					//new WriteToESThreaded(node, client, pageDataMap, urlDataMap);
					pageDataMap=new HashMap<String,PageData>();
				}
		 }
		catch(Exception e){
			System.out.print("Exception on : "+url);
			e.printStackTrace();
		}		
	}
	if(pageDataMap.size()>0)
		writePageDataToES(pageDataMap,urlDataMap);	
	node.close();
	client.close();
	
	
}
public void processURL(String url,int urlLevel) throws Exception{
	URL myUrl=new URL(url);
	UrlBean urlData;
	boolean foundInMap=false;
	if(!urlDataMap.containsKey(url))
	 urlData=new UrlBean();
	else
	 {
		urlData=urlDataMap.get(url);	
		foundInMap=true;
	 }
	 
	PageData pData;
	HashSet<String> seenPerPage=new HashSet<String>();
    domainTimeAccessMap.put(myUrl.getHost(), System.currentTimeMillis());
    if(!foundInMap)
     urlDataMap.put(url,urlData);
	Document doc=Jsoup.connect(url).get();
	String text;
	int relevantPageFlag=0;
	pData=new PageData();
	String title=getRelevantText(doc.title()).trim();
	pData.setTitle(title);
	text=getRelevantText(doc.text()).trim();
	pData.setText(text);
	pageDataMap.put(url, pData);
	System.out.println("Url No: "+ (++urlCount));
	System.out.println("Crawling : "+url+"\t level = "+urlLevel+"\n-------------------------------------------");
	for(String w: searchTerms){
		if(text.contains(w))
		{
			relevantPageFlag=1;
			break;
		}
	}
	//code to check if the extracted content from the page has words relevant to url query terms
	if(relevantPageFlag==1){
	/* Following is the code block to determine all the outlinks for the current processed url. All the links 
	 * found from the current url page is checked whether it has been already seen in urlDataMap. If it is not found 
	 * a new urlData is created for it and the current url is stored as one of it's inlinks, else it is fetched
	 * from the urlDataMap and the inlink is added. The inlinks found are also added to the frontier for future 
	 * processing if it is not already visited.*/
		Elements outlinks=doc.select("a[href]");
		for(Element outlink: outlinks){
			String urlOutlink=Canonicalization.canonicalize(outlink.absUrl("href"));
			
			// add the newly found outlinks into the frontier queue iff it is not visited and 
			// not already present in frontier and has a valid extension
			if(!urlDataMap.containsKey(urlOutlink) && !frontierDetailMap.containsKey(urlOutlink) && validExt(urlOutlink) && frontier.size()<=100000){
				UrlIC u=new UrlIC();
				u.setUrl(urlOutlink);
				u.setTime(System.currentTimeMillis());
				u.setLevel(urlLevel+1);
				frontier.add(u);
				frontierDetailMap.put(urlOutlink, u);
				//System.out.println(urlOutlink + "  level = "+u.getLevel());
			}
			
			 
			 
			if(!seenPerPage.contains(urlOutlink)){
				
				/*if(urlOutlink.equals("http://en.wikipedia.org/wiki/Wikipedia:Protection_policy"))
					System.out.println("http://en.wikipedia.org/wiki/Wikipedia:Protection_policy from : "+url);*/
				seenPerPage.add(urlOutlink);
				//store outlinks into current urlData
				urlData.addOutlinks(urlOutlink);
				urlDataMap.put(url, urlData);
				// create new urlData for the discovered outlink and store the inlinks
				UrlBean newUrl;
				if(!urlDataMap.containsKey(urlOutlink))
					newUrl=new UrlBean();
				else
					newUrl=urlDataMap.get(urlOutlink);
				newUrl.addInlinks(url);	
				urlDataMap.put(urlOutlink, newUrl);
				
				// Also update frontierDetailMap so that the PriorityQueue is sorted accordingly
				if(frontierDetailMap.containsKey(urlOutlink) ){
				 UrlIC modU=frontierDetailMap.get(urlOutlink);
				 modU.incIc();
				/* if(modU.getIc()==2){
					 System.out.println("Current URL : "+url +" Seeing: "+urlOutlink);
				 }*/
				 frontier.remove(modU);
				 frontier.add(modU);
				}
			} // end of seen per page 
		} // end of for loop
	} //end of if  relevant page
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
  Path path = Paths.get("./OutputFiles/doc"+WebCrawler.fileCounter+".txt");
  Files.write(path, data, StandardCharsets.UTF_8);
}
public void writePageDataToES(HashMap<String,PageData> pageDataMap,HashMap<String,UrlBean> urlDataMap){
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
public boolean validExtension(String url){
	String[] invalidExt={".jpg",".jpeg",".pdf",".ogg",".ogv",".doc",".docx",".xls",
			             ".xlsx",".ppt",".pptx",".mp3",".mp4",".avi",".mov",".flv"};
	//System.out.println("url in valid ex "+url);
	for(String s:invalidExt){
		if(url.contains(s)){
			//System.out.println("url contains "+s);
			return false;
		}
	}
	
	return true;
}
public boolean validExt(String url){
	return (!url.matches("[\\w].*\\.[a|jp|i|d|m|p|pp|o|s|c|pd|PD|D|S|t|b|x|f|n|g|v|r|y|(0-9)]+$"));
}
public HashMap<String, Object> createJson(StringBuffer docNo,StringBuffer text){
	json=new HashMap<String,Object>();
	json.put("docno", docNo);
	json.put("text", text);
	return json;
}
}

class UrlComparator implements Comparator<UrlIC>
{
    @Override
    public int compare(UrlIC x, UrlIC y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
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
