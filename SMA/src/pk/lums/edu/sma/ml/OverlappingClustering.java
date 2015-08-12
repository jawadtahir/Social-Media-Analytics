package pk.lums.edu.sma.ml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pk.lums.edu.sma.utils.IOUtils;

public class OverlappingClustering {
    private static ArrayList<String> topEntList = new ArrayList<String>();

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Processing entities...");
	String[] entityLine = IOUtils.readFile("Entities.txt");
	String csvEntities = entityLine[0].substring(1);
	csvEntities = csvEntities.substring(0, csvEntities.length() - 1);
	String[] entityArr = csvEntities.split(", ");
	Map<String, Double> entityMap = new HashMap<String, Double>();
	StringBuilder sb = new StringBuilder();
	int count = 0;
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
	// IOUtils.writeFile("EntLine.csv", sb.toString(), false);
	entityMap = IOUtils.sortByValues(entityMap);
	// String[] topEntities = IOUtils.getTopNEntities(entityMap,
	// entityMap.size() / FRACTION_OF_TWEETS_TO_PROCESS);
	for (Map.Entry<String, Double> entity : entityMap.entrySet()) {
	    topEntList.add(entity.getKey().trim().toLowerCase());
	}

	IOUtils.log(Calendar.getInstance().getTime().toString());
    }

}
