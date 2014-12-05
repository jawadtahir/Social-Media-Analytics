package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pk.lums.edu.sma.utils.IOUtils;

public class EntityCluster {

    private HashMap<String, ArrayList<String>> clusters;

    public EntityCluster(String[] entities) {
	super();
	clusters = new HashMap<String, ArrayList<String>>();
	for (String entity : entities) {
	    clusters.put(entity.trim(), new ArrayList<String>());
	}
    }

    public void putInCluster(String cluster, String entry) {
	ArrayList<String> entries = clusters.get(cluster);
	entries.add(entry);
    }

    public void printCluster() {
	Iterator<String> mapItr = clusters.keySet().iterator();
	while (mapItr.hasNext()) {
	    String clstrName = (String) mapItr.next();
	    System.out.println("***************************************");
	    System.out.println("*****************" + clstrName
		    + "***************");
	    System.out.println("***************************************");
	    ArrayList<String> tweets = clusters.get(clstrName);
	    for (String tweet : tweets) {
		System.out.println(tweet);
	    }
	    System.out.println("********************************");
	}
    }

    public void writeCluster() {
	File dir = new File("clusters");
	if (!dir.exists()) {
	    dir.mkdir();
	}
	// for (File file : dir.listFiles()) {
	// file.delete();
	// }
	Iterator<String> mapItr = clusters.keySet().iterator();
	while (mapItr.hasNext()) {
	    String clstrName = (String) mapItr.next();
	    ArrayList<String> tweets = clusters.get(clstrName);
	    StringBuilder txtToWrite = new StringBuilder();
	    for (String tweet : tweets) {
		txtToWrite.append(tweet + "\n");
	    }
	    IOUtils.writeFile(dir.getAbsolutePath() + "/" + clstrName + ".txt",
		    txtToWrite.toString(), true);
	}
    }
}
