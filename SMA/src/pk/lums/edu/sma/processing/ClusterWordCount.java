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
	String[] entityArr = csvEntities.split(",");
	Map<String, Integer> entityMap = new HashMap<String, Integer>();
	for (String entity : entityArr) {
	    if (entity.length() > 3) {
		// System.out.println(entity);
		String[] keyVal = entity.split("=");
		if (keyVal.length == 2 && keyVal[0].trim().length() > 4) {
		    String key = keyVal[0].trim();
		    int val = Integer.parseInt(keyVal[1]);
		    entityMap.put(key, val);
		}
	    }
	}
	entityMap = IOUtils.sortByValues(entityMap);
	entityMap = getTopNEntities(entityMap, entityMap.size() / 5);

	File file = new File("clusters");
	File[] clusters = file.listFiles();
	List<ClusterWordCountThread> threadList = new ArrayList<ClusterWordCountThread>();
	int i = 0;
	for (File cluster : clusters) {
	    i++;
	    threadList.add(new ClusterWordCountThread(i, cluster, entityMap));
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

    public static Map<String, Integer> getTopNEntities(
	    Map<String, Integer> map, int n) {
	Iterator<Entry<String, Integer>> itr = map.entrySet().iterator();
	int i = 0;
	Map<String, Integer> entites = new HashMap<String, Integer>();
	while (itr.hasNext()) {
	    if (i == n) {
		break;
	    }
	    Entry<String, Integer> ent = itr.next();
	    entites.put(ent.getKey(), ent.getValue());
	    i++;
	}

	return entites;
    }
}
