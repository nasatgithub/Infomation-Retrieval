package ir.a4.controller;

import java.util.HashSet;
import java.util.List;

public class NodeBean {
private double pageRank;
private HashSet<String> inlinks;
private HashSet<String> outlinks;
public NodeBean() {
 pageRank=0;
 inlinks=new HashSet<String>();
 outlinks=new HashSet<String>();
}
public double getPageRank() {
	return pageRank;
}
public void setPageRank(double pageRank) {
	this.pageRank = pageRank;
}
public HashSet<String> getInlinks() {
	return inlinks;
}
public void setInlinks(HashSet<String> inlinks) {
	this.inlinks = inlinks;
}
public void addInlinks(List<String> inlinks) {
	this.inlinks.addAll(inlinks);
}
public HashSet<String> getOutlinks() {
	return outlinks;
}
public void setOutlinks(HashSet<String> outlinks) {
	this.outlinks = outlinks;
}
public void addOutlinks(List<String> outlinks) {
	this.outlinks.addAll(outlinks);
}
public void addOutlink(String outlink) {
	this.outlinks.add(outlink);
}
}
