package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pk.lums.edu.sma.utils.IOUtils;

public class ClusterWordCount {

    private static final int NO_OF_THREADS = 10;

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Processing entities...");
	String[] entityLine = IOUtils.readFile("Entities.txt");
	String csvEntities = entityLine[0].substring(1);
	csvEntities = csvEntities.substring(0, csvEntities.length() - 1);
	String[] entityArr = csvEntities.split(", ");
	Map<String, Double> entityMap = new HashMap<String, Double>();
	List<String> entList = new ArrayList<String>();

	int count = 0;
	for (String entity : entityArr) {
	    count++;
	    if (count <= 600) {
		// System.out.println(entity);
		entList.add(entity.split("=")[0]);
	    }
	}
	// entityMap = IOUtils.sortByValues(entityMap);
	// entityMap = getTopNEntities(entityMap, entityMap.size() / 5);

	File file = new File("600cluster/clusters0");
	File[] clusters = file.listFiles();
	List<ClusterWordCountThread> threadList = new ArrayList<ClusterWordCountThread>();
	int i = 0;
	for (File cluster : clusters) {
	    if (cluster.getName().contains("cluster")) {
		i++;
		threadList.add(new ClusterWordCountThread(i, cluster, entList));
	    }
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

    public static Map<String, Double> getTopNEntities(Map<String, Double> map,
	    int n) {
	Iterator<Entry<String, Double>> itr = map.entrySet().iterator();
	int i = 0;
	Map<String, Double> entites = new HashMap<String, Double>();
	while (itr.hasNext()) {
	    if (i == n) {
		break;
	    }
	    Entry<String, Double> ent = itr.next();
	    entites.put(ent.getKey(), ent.getValue());
	    i++;
	}

	return entites;
    }
}
