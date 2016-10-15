package ir.a4.controller;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class GenerateInLinks {
	private static Node node;
	private static Client client;
	private static List<String> resultFileList;
	public GenerateInLinks(){
		node=nodeBuilder().clusterName("ana").node();
		client=node.client();
		resultFileList=new ArrayList<String>();
	}
public static void main(String args[]){
	GenerateInLinks gi=new GenerateInLinks();
	gi.generateInlinks();
	
}
public void generateInlinks(){
	
}
}
