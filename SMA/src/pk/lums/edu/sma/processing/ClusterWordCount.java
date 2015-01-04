package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterWordCount {

    private static final int NO_OF_THREADS = 10;
    private static Map<String, Integer> map = new HashMap<String, Integer>();

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	File file = new File("clusters");
	File[] clusters = file.listFiles();
	List<ClusterWordCountThread> threadList = new ArrayList<ClusterWordCountThread>();
	int i = 0;
	for (File cluster : clusters) {
	    i++;
	    threadList.add(new ClusterWordCountThread(i, cluster, map));
	}
	for (ClusterWordCountThread thread : threadList) {
	    thread.run();
	}
	for (ClusterWordCountThread thread : threadList) {
	    try {
		thread.join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

}
