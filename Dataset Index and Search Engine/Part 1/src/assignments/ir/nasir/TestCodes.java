package assignments.ir.nasir;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCodes {
public static void main(String a[]) throws IOException{
PorterStemmer stemmer=new PorterStemmer();
String word="use";
System.out.println(stemmer.stem(word.toLowerCase()));
HashMap<String,Integer> test=new HashMap<String,Integer>();
test.put("85.#AP1", 5);
test.put("77.#AP1", 7);
test.put("85.#AP2",8);
test.put("77.#AP3", 6);

String q[]={"77.","85."};
//System.out.println(test);

/*for(String s: q)
{
  SortedMap<String,Integer> t=test.subMap(s+"#AP1", true, s+"#AP3", true);
  t = SortByValue(t); 
  //System.out.println(t);
  for(String k:t.keySet())
	  System.out.println(k+" "+test.get(k));

}*/

/*for(String key:test.descendingKeySet())
System.out.println(key+" "+test.get(key));*/


/*TreeMap<String, Integer> sortedMap = SortByValue(test);  
System.out.println(sortedMap);*/

/*RetrieveDocLen rd=new RetrieveDocLen();
List<String> list=rd.readDocLenList();
int doclen=0;
int sum=0;
for(String s:list){
	String splits[]=s.split("#");
	if(splits[0].equals("Total_Doc_Len"))
		break;
	sum=sum+Integer.parseInt(splits[1]);
}
System.out.println("Sum ="+sum);*/

String c="\"dual-use\"";
System.out.println(c.replaceAll("-"," "));

System.out.println(1.0 + 3);

System.out.println(Math.log(4/(1+178081.0)));

String m="This is a big day The Day happiest day in the biggest life.";

System.out.println("counts="+PrepareQueries.countWordOccurance(m, "the"));

HashMap<String,Integer> t1=new HashMap<String, Integer>();
t1.put("a", 1);
t1.put("b", 4);

HashMap<String, Integer> t2=new HashMap<String, Integer>();
t2.put("a", 1);
t2.put("b", 4);
HashMap<String, Integer> tfinal=new HashMap<String, Integer>(t1);
tfinal.putAll(t2);

System.out.println(tfinal.get("b"));

String testw="(hi)";
System.out.println(testw.replaceAll("[()]", ""));

System.out.println(stemmer.stem("d'etat"));


}

public static TreeMap<String, Integer> SortByValue 
(SortedMap<String, Integer> map) {
ValueComparator vc =  new ValueComparator(map);
TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(vc);
sortedMap.putAll(map);
return sortedMap;
}

public static int countWordOccurance(String original, String term){
	original=original.toLowerCase();
	term=term.toLowerCase();
	String splits[]=original.split(" ");
	HashMap<String, Integer> countMap=new HashMap<String,Integer>();
	
	for(String s:splits){
		if(countMap.containsKey(s))
			countMap.put(s, countMap.get(s)+1);
		else
			countMap.put(s, 1);
	}
	
	return countMap.get(term);
}
}

class ValueComparator implements Comparator<String> {
	 
    Map<String, Integer> map;
 
    public ValueComparator(Map<String, Integer> base) {
        this.map = base;
    }
 
    public int compare(String a, String b) {
        if (map.get(a) >= map.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys 
    }
    


}
