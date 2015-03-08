package pk.lums.edu.sma.utils;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class IOUtils {
    private static final String url = "jdbc:mysql://localhost:3306/TWEETDATA";
    private static final String user = "jawad2";
    private static final String password = "abc123";

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

    /**
     * Append given text to given file path
     * 
     * @param filePath
     *            File path to write
     * @param text
     *            text to write
     */
    public static void writeFile(String filePath, String text) {
	writeFile(filePath, text, true);
    }

    /**
     * Write given text to given file
     * 
     * @param filePath
     *            path to write
     * @param text
     *            text to write
     * @param isAppend
     *            true or false
     */
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

    /**
     * Java method to sort Map in Java by value e.g. HashMap or Hashtable throw
     * NullPointerException if Map contains null values It also sort values even
     * if they are duplicates
     * 
     * @param map
     *            HashMap to sort
     * @return Sorted HashMap
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValues(
	    Map<K, V> map) {
	try {

	} catch (Exception ex) {

	}
	List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(
		map.entrySet());

	Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {

	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o2.getValue().compareTo(o1.getValue());
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

    /**
     * get top n number of entries from map
     * 
     * @param map
     *            map from which we have to chose entries
     * @param n
     *            number of entries to chose
     * @return top n number of entries from map, in map format
     */

    public static String[] getTopNEntities(Map<String, Double> map, int n) {
	Iterator<String> itr = map.keySet().iterator();
	int i = 0;
	ArrayList<String> entites = new ArrayList<String>();
	while (itr.hasNext()) {
	    if (i == n) {
		break;
	    }
	    String eNmae = (String) itr.next();
	    entites.add(eNmae);
	    i++;
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

    /**
     * Clears clusters folder
     */
    public static void clearClusterFolder() {
	File dir = new File("clusters");
	if (!dir.exists()) {
	    return;
	}
	for (File file : dir.listFiles()) {
	    file.delete();
	}
    }

    /**
     * Get connection for tweet DB
     * 
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
	return DriverManager.getConnection(url, user, password);
    }

    public static void deleteDir(File file) {
	Path dir = Paths.get(file.getAbsolutePath());
	try {
	    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

		@Override
		public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {

		    System.out.println("Deleting file: " + file);
		    Files.delete(file);
		    return CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir,
			IOException exc) throws IOException {

		    System.out.println("Deleting dir: " + dir);
		    if (exc == null) {
			Files.delete(dir);
			return CONTINUE;
		    } else {
			throw exc;
		    }
		}

	    });
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}