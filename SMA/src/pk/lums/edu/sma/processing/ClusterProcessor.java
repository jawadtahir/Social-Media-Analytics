package pk.lums.edu.sma.processing;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pk.lums.edu.sma.utils.IOUtils;

public class ClusterProcessor {
    private static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    // private static List<ClusterModel> clustModList = null;
    private static List<ProcessClusterThread> clusterThreadList = new ArrayList<ProcessClusterThread>();
    private static List<File> fileList = new ArrayList<File>();
    private static final int NO_OF_THREADS = 10;

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	File relationDir = new File("Relationship");
	if (!relationDir.exists()) {
	    relationDir.mkdir();
	}

	clearDir(relationDir.getAbsolutePath());

	System.out.println(Calendar.getInstance().getTime().toString());
	File clusterDir = new File("clusters1");
	// clustModList = Collections
	// .synchronizedList(new ArrayList<ClusterModel>());
	System.out.println(clusterDir.listFiles().length);
	int count = 0;
	for (File cluster : clusterDir.listFiles()) {

	    if (cluster.getName().length() > 8
		    && cluster.getName().contains("cluster-")) {
		fileList.add(cluster);
		// String[] tweets =
		// IOUtils.readFile(cluster.getAbsolutePath());
		// String clusterName = cluster.getName().substring(0,
		// cluster.getName().length() - 4);
		// int countTweet = 0;
		// fileList.add(cluster);
		// for (String tweetEnt : tweets) {
		// StringBuilder sb = new StringBuilder(tweetEnt);
		// sb.append("  ");
		// countTweet++;
		// String[] tweetEntry = sb.toString().split(" , ");
		// if (tweetEntry.length >= 4) {
		// String loc = tweetEntry[tweetEntry.length - 1].trim();
		// String id = tweetEntry[tweetEntry.length - 2].trim();
		// Date time = null;
		// try {
		// time = df.parse(tweetEntry[tweetEntry.length - 3]);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		// sb = new StringBuilder();
		//
		// for (int i = 0; i <= tweetEntry.length - 4; i++) {
		// sb.append(tweetEntry[i]);
		// }
		//
		// ClusterModel cModel = new ClusterModel(sb.toString(),
		// time, clusterName, Integer.parseInt(id));
		// clustModList.add(cModel);
		// System.out.println(countTweet);
		// System.out.println(clusterName);
		//
		// } else {
		// System.out.println(clusterName);
		// System.out.println(tweetEnt);
		// System.out.println(countTweet);
		// }
		// }

		// count++;
		// System.out.println(count);

		// Date[] dateRange = getRange(tweets);
		// for (File matchCluster : clusterDir.listFiles()) {
		// if (!(matchCluster.getName().equals(cluster.getName()))) {
		//
		// }
		// }
	    }

	}
	System.out.println(Calendar.getInstance().getTime().toString());
	// Collections.sort(clustModList);
	int noOfFilesPerThread = fileList.size() / NO_OF_THREADS;
	for (int j = 0; j < NO_OF_THREADS; j++) {
	    ArrayList<File> clustersForThread = getNextMelements(j
		    * noOfFilesPerThread, noOfFilesPerThread);
	    ProcessClusterThread cThread = new ProcessClusterThread(fileList,
		    Integer.toString(j), clustersForThread);
	    clusterThreadList.add(cThread);
	}

	for (ProcessClusterThread pThread : clusterThreadList) {
	    pThread.start();
	}

	for (ProcessClusterThread cThread : clusterThreadList) {
	    try {
		cThread.join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	System.out.println(Calendar.getInstance().getTime().toString());
	System.out.println("end");

    }

    private static ArrayList<File> getNextMelements(int strtIndex, int offset) {
	ArrayList<File> retList = new ArrayList<File>();
	for (int i = 0; i < offset; i++) {
	    File cluster = fileList.get(strtIndex + i);
	    retList.add(cluster);
	}
	return retList;
    }

    private static void clearDir(String relationDir) {
	File relatDir = new File(relationDir);
	for (String strRelat : relatDir.list()) {
	    File relate = new File(relationDir, strRelat);
	    IOUtils.deleteDir(relate);
	}
    }

}
