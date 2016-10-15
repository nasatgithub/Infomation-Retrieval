package ir.a4.controller;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;

public class MergedInlinks {


	static Node node = nodeBuilder().clusterName("ana").node();
	static Client client = node.client();
	
	public static void main(String[] args) throws IOException {
	
		File inlinksMergedOutput = new File("Files/outputInlinksMergedOutput.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(inlinksMergedOutput));
		
		File urlFile = new File("Files/urlMap.txt");
		BufferedReader bufferedReader = new BufferedReader(new FileReader(urlFile));
		String docNo;
		int count=0;
		while((docNo = bufferedReader.readLine()) != null) {
			try {
				SearchResponse response = client.prepareSearch("anair3")
				        .setTypes("document")
				        .setQuery(QueryBuilders.matchQuery("docno", docNo))
				        .setFrom(0).setSize(1)
				        .execute()
				        .actionGet();
				System.out.println(++count);
				SearchHit searchHitMyIndex = response.getHits().getHits()[0];
				
				ArrayList<String> inLinks = (ArrayList<String>) searchHitMyIndex.getSource().get("inlinks");
				
				bufferedWriter.write(docNo);
				
				for(String link:inLinks) {
					bufferedWriter.write(" ");
					bufferedWriter.write(link);
				}
				bufferedWriter.write("\n");
				
			} catch (ElasticsearchException e) {
				System.out.println("Elastic Search Exception inside while");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("Exception on inside");
				e.printStackTrace();
			}
		}
		bufferedReader.close();
		bufferedWriter.close();
	

	}
}
