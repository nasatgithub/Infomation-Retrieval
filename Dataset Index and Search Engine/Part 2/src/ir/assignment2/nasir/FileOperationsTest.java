package ir.assignment2.nasir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FileOperationsTest {
public static void main(String a[]) throws IOException{
	RandomAccessFile raf=new RandomAccessFile("./OutputFiles/pInvertedIndex.txt", "rw");
	String str;
	raf.seek(0);
	/*while((str=raf.readLine())!=null){
		System.out.println(str);
		if(str.indexOf("Everyboody")!=-1)
			System.out.println(raf.getFilePointer());
	}*/
	
	
	//raf.writeBytes("Welcome to the party.\n");
	//System.out.println(raf.readLine());
	HashMap<Long,Long> cat=new HashMap<Long, Long>();
	RandomAccessFile raft=new RandomAccessFile("C:/Users/NasirAhmed/invertedIndex.txt", "rw");
	//raf2.seek(raf2.length());
	
	raft.seek(142658823);
	System.out.println(raft.readLine());
	
  	

	
}
}
