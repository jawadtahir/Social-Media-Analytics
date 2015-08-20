package pk.lums.edu.sma.ml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.utils.IOUtils;

public class OverlappingClustering {
    private static final int NO_OF_THREADS = 4;

    private static List<String> topEntList = new ArrayList<String>();
    private static List<File> listOfOverLapRegions = new ArrayList<File>();
    private static List<OverlappingClusteringThread> threadList = new ArrayList<OverlappingClusteringThread>();

    /**
     * Finds clusters in overlapped regions
     * 
     * @param args
     *            first argument would be the name of file where attributes are
     *            stored. Second argument would be the directory where
     *            overlapped regions are Third argument would be the name of
     *            cluster to exclude processing
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.log("Reading entities...");
	// Read attributes
	String[] entityLine = IOUtils.readFile(args[0]);
	// Remove braces from the start and end
	String csvEntities = entityLine[0].substring(1,
		entityLine[0].length() - 1);
	// Split the string so we can get an array of attributes
	String[] entityArr = csvEntities.split(", ");
	Map<String, Double> entityMap = new HashMap<String, Double>();
	StringBuilder sb = new StringBuilder();
	int count = 0;
	// Create a hash map where key is attribute and value is its count in
	// data set
	for (String entity : entityArr) {
	    count++;
	    if (count <= 600) {
		// System.out.println(entity);
		String[] keyVal = entity.split("=");
		if (keyVal.length == 2 && keyVal[0].trim().length() > 3) {
		    String key = keyVal[0].trim();
		    int val = Integer.parseInt(keyVal[1]);
		    if (entityMap.containsKey(key)) {
			entityMap.put(key, entityMap.get(key) + val);
		    } else {
			entityMap.put(key, (double) val);
		    }
		    sb.append(key + ", " + val + "\n");
		}
	    }
	}
	entityMap = IOUtils.sortByValues(entityMap);
	// Create a list of attributes
	for (Map.Entry<String, Double> entity : entityMap.entrySet()) {
	    topEntList.add(entity.getKey().trim().toLowerCase());
	}

	IOUtils.log("Creating threads...");

	File dir = new File(args[1]);
	// Create a list of overlapped regions in the given directory
	for (File cluster : dir.listFiles()) {
	    String clusterName = cluster.getName();
	    if (cluster.isDirectory()) {
		IOUtils.deleteDir(cluster);
		continue;
	    }
	    // removing extension from the name
	    clusterName = clusterName.replace(".txt", "");
	    if (!clusterName.equals(args[2])) {
		listOfOverLapRegions.add(new File(dir.getAbsoluteFile() + "/"
			+ clusterName));
	    }
	}
	// finding the number of files we should give to each thread
	int noOfFilesPerThread = (int) Math.floor(listOfOverLapRegions.size()
		/ NO_OF_THREADS);
	int previousIndex = 0;
	for (int i = 0; i < NO_OF_THREADS; i++) {
	    List<File> fileList = null;
	    if (i != NO_OF_THREADS - 1) {
		fileList = listOfOverLapRegions.subList(previousIndex,
			previousIndex + noOfFilesPerThread);
		previousIndex += noOfFilesPerThread;
	    } else {
		// Get all remaining files
		fileList = listOfOverLapRegions.subList(previousIndex,
			listOfOverLapRegions.size());
	    }

	    // Populate thread list
	    threadList.add(new OverlappingClusteringThread(fileList,
		    topEntList, Integer.toString(i)));
	}

	// Start all threads

	IOUtils.log("Starting threads...");

	for (OverlappingClusteringThread thread : threadList) {
	    thread.start();
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	// Wait for all the threads to close

	IOUtils.log("Waiting for threads to die...");

	for (OverlappingClusteringThread thread : threadList) {
	    try {
		thread.join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	IOUtils.log("Process finished.");

    }
}
