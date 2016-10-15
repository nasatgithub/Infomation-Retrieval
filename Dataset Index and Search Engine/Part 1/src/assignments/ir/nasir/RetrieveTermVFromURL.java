package assignments.ir.nasir;

import groovy.json.JsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.spec.EncodedKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

public class RetrieveTermVFromURL {
	public  String httpGet(String urlStr) throws IOException {
		  URL url = new URL(urlStr);
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();
          //conn.setRequestMethod("GET");
          
          System.out.println("json/text ");
		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }
          System.out.println("here");
		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
			
		    sb.append(line);
		    
		  }
		  rd.close();

		  conn.disconnect();
		  
		  return sb.toString();
		}
    public int getDocFreq(String searchterm,JSONObject terms) throws JSONException{
    	JSONObject termJsonObj=terms.getJSONObject(searchterm);
    	System.out.println(termJsonObj.toString());
    	return termJsonObj.getInt("doc_freq");
    }
	public static void main(String args[]) throws Exception{
		RetrieveTermVFromURL r=new RetrieveTermVFromURL();
		String url="http://localhost:9200/apdataset/document/AP890101-0001/_termvector?";
        String urlQ="{"+
                   "\"fields\" : [\"text\"],"+
				   "\"offsets\" : true,"+
				   "\"payloads\" : true,"+
				   "\"positions\" : true,"+
				   "\"term_statistics\" : true,"+
				   "\"field_statistics\" : true"+
				   "}";
        System.out.println(urlQ.toString());
		String encodedURLQ= URLEncoder.encode(urlQ, "UTF-8");
		
		String jsonResponse=r.httpGet(url+encodedURLQ);
		System.out.println(jsonResponse);
        JSONObject jobj=new JSONObject(jsonResponse);
        JSONObject fieldObj=jobj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("field_statistics");
        System.out.println(fieldObj.length());
        int f=fieldObj.getInt("sum_doc_freq");
        System.out.println(f);
        JSONObject terms=jobj.getJSONObject("term_vectors").getJSONObject("text").getJSONObject("terms");
    
        String allterms[]=JSONObject.getNames(terms);
//        for(String s:allterms){
//        	System.out.println(s);
//        }
//        
        System.out.println("term freq "+r.getDocFreq("celluloid", terms));
        
	    
	}
}
