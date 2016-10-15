package ir.a4.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;



public class RelevancyHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private HashMap<String,Integer> urlRel;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RandomAccessFile raf;
    /**
     * Default constructor. 
     */
    public RelevancyHandler() {
        // TODO Auto-generated constructor stub
    	urlRel=new HashMap<String,Integer>();
    	raf=null;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.request=request;
		this.response=response;
		System.out.println("yo came here");
		String method=request.getParameter("method");
	
		if(method.equals("readRelevancy")){
			String urlId=request.getParameter("urlid");
			int urlVal=Integer.parseInt(request.getParameter("urlval"));
			readRelevancy(urlId,urlVal);
		}	
		else if(method.equals("clearMap"))
			clearMap();	
		else if(method.equals("createQREL")){
			String aid=request.getParameter("aid");
			String qid=request.getParameter("qid");
			createQREL(aid,qid);
		}
	}
	public void readRelevancy(String urlId,int urlVal) throws IOException{
		PrintWriter out=response.getWriter();
		JSONObject json=new JSONObject();
		System.out.println("URLID : "+ urlId);
		System.out.println("URLVALUE : "+ urlVal);
		if(urlVal!=-1)
		 urlRel.put(urlId, urlVal);
		else{
			if(urlRel.containsKey(urlId))
				urlRel.remove(urlId);
		}
			
		for(String key:urlRel.keySet())
			System.out.println(key+"\t"+ urlRel.get(key));
		System.out.println("------------------------------------------------");
		json.put("ratedCount", urlRel.size());
		out.println(json);
	}
	public void clearMap() throws IOException{
		System.out.println("Clearing...");
		if(raf!=null)
			raf.close();
		urlRel.clear();
	}
	public void createQREL(String aid,String qid){
		PrintWriter out=null;
		JSONObject json=new JSONObject();
		System.out.println("Came here");
		try{
		out=response.getWriter();
		System.out.println(request.getContextPath());
		String path=getServletContext().getInitParameter("fsroot");
		raf=new RandomAccessFile(path+"/qrel-web-"+qid+".txt","rw");
		raf.seek(raf.length());
		System.out.println(urlRel);
		for(String key: urlRel.keySet()){
			System.out.println(qid+"\t"+aid+"\t"+key+"\t"+urlRel.get(key)+"\n");
			raf.writeBytes(qid+"\t"+aid+"\t"+key+"\t"+urlRel.get(key)+"\n");
		}
		json.put("status", "success");
		
		}
		catch(Exception e){
			e.printStackTrace();
			json.put("status", "failure");
		}
		out.println(json);
		
		
	}

}
