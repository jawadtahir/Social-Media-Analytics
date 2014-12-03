package pk.lums.edu.sma.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class IOUtils {
    public static String[] readFile(String filePath) {
	String[] ret = null;
	List<String> lines = null;
	try {
	    Scanner sc = new Scanner(new File(filePath));
	    lines = new ArrayList<String>();
	    while (sc.hasNextLine()) {
		lines.add(sc.nextLine());
	    }
	    sc.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
	ret = lines.toArray(new String[lines.size()]);
	return ret;
    }

    public static void writeFile(String filePath, String text) {
	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
		    filePath, true));
	    bufferedWriter.write(text.trim());
	    bufferedWriter.newLine();
	    bufferedWriter.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public static void writeFile(String filePath, String text, boolean isAppend) {
	try {
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
		    filePath, isAppend));
	    bufferedWriter.write(text.trim());
	    bufferedWriter.newLine();
	    bufferedWriter.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /*
     * Java method to sort Map in Java by value e.g. HashMap or Hashtable throw
     * NullPointerException if Map contains null values It also sort values even
     * if they are duplicates
     */
    public static <K extends Comparable<String>, V extends Comparable<Integer>> Map<K, V> sortByValues(
	    Map<K, V> map) {
	try {

	} catch (Exception ex) {

	}
	List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(
		map.entrySet());

	Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o2.getValue().compareTo((Integer) o1.getValue());
	    }
	});

	// LinkedHashMap will keep the keys in the order they are inserted
	// which is currently sorted on natural ordering
	Map<K, V> sortedMap = new LinkedHashMap<K, V>();

	for (Map.Entry<K, V> entry : entries) {
	    sortedMap.put(entry.getKey(), entry.getValue());
	}

	return sortedMap;
    }

    public static String[] getTopNEntities(HashMap<String, Integer> map, int n) {
	Iterator<String> itr = map.keySet().iterator();
	int i = 0;
	ArrayList<String> entites = new ArrayList<String>();
	while (itr.hasNext()) {
	    String eNmae = (String) itr.next();
	    entites.add(eNmae);
	    i++;
	    if (i == n) {
		break;
	    }
	}

	return entites.toArray(new String[entites.size()]);
    }

    public static String getPatternString(String[] entities) {
	int i = 0;
	StringBuilder sb = new StringBuilder();
	for (String string : entities) {
	    sb.append(string);
	    i++;
	    if (i < entities.length) {
		sb.append("|");
	    } else {
		break;
	    }
	}

	return sb.toString();
    }

    public static void log(String text) {
	IOUtils.writeFile("Log.txt", text, true);
    }

}