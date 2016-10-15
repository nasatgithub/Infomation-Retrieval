package ir.a3.nasir;

import java.util.ArrayList;
import java.util.List;

public class UrlBean {
private String id;
private int inlinkC;
private List<String> inlinks;
private List<String> outlinks;
public UrlBean(){
	inlinkC=0;
	inlinks=new ArrayList<String>();
	outlinks=new ArrayList<String>();
}
public String getId() {
	return id;
}

public void setId(String id) {
	this.id = id;
}

public int getInlinkC() {
	return inlinkC;
}

public void incrementInlinkC() {
	inlinkC++;
}

public List<String> getInlinks() {
	return inlinks;
}
public void addInlinks(String url) {
	if(!inlinks.contains(url))
	inlinks.add(url);
}
public List<String> getOutlinks() {
	return outlinks;
}
public void addOutlinks(String url) {
	if(!outlinks.contains(url))
		outlinks.add(url);
}
public void clearLinks(){
	inlinks.clear();
	outlinks.clear();
}
}
