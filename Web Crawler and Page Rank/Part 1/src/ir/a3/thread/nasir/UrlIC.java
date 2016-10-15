package ir.a3.thread.nasir;

import java.util.ArrayList;
import java.util.List;

public class UrlIC {
private String url;
private int level;
private int ic;
private long time;
private List<String> inlinks;
private List<String> outlinks;
private int indexed;

public UrlIC(){
	ic=0;
	indexed=0;
	inlinks=new ArrayList<String>();
	outlinks=new ArrayList<String>();
}

public int getLevel() {
	return level;
}
public void setLevel(int level) {
	this.level = level;
}
public String getUrl() {
	return url;
}
public void setUrl(String url) {
	this.url = url;
}
public int getIc() {
	return ic;
}
public void setIc(int ic) {
	this.ic = ic;
}
public void incIc() {
	this.ic++;
}
public long getTime() {
	return time;
}
public void setTime(long time) {
	this.time = time;
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
	//inlinks.clear();
	outlinks.clear();
}

public int getIndexed() {
	return indexed;
}

public void setIndexed(int indexed) {
	this.indexed = indexed;
}

}
