package assignments.ir.nasir;

import java.io.IOException;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

public class MyActionListener implements ActionListener<TermVectorResponse>{
	private String jsonResponse;
	public MyActionListener() {
		// TODO Auto-generated constructor stub
		jsonResponse=null;
	}
	public void onResponse(TermVectorResponse response) {
		// TODO Auto-generated method stub
		try {
			XContentBuilder xbuilder= XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
			response.toXContent(xbuilder, ToXContent.EMPTY_PARAMS);

			jsonResponse=new String(xbuilder.string());
			System.out.println("Terms Vector in MyActionListener: "+jsonResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	public void onFailure(Throwable e) {
		// TODO Auto-generated method stub
		e.printStackTrace();
	}
	
	public String getJsonResponse(){
		return jsonResponse;
	}

}