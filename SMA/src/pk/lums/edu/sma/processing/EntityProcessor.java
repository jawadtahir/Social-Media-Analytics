package pk.lums.edu.sma.processing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class EntityProcessor {

    private static final int NO_OF_THREADS = 10;
    private static final int FRACTION_OF_TWEETS_TO_PROCESS = 5;
    private static ArrayList<ProcessEntities> threadList = new ArrayList<ProcessEntities>();
    private static ArrayList<String> topEntList = new ArrayList<String>();

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.log(Calendar.getInstance().getTime().toString());
	String[] entityLine = IOUtils.readFile("Entities.txt");
	String csvEntities = entityLine[0].substring(1);
	csvEntities = csvEntities.substring(0, csvEntities.length() - 1);
	String[] entityArr = csvEntities.split(",");
	Map<String, Integer> entityMap = new HashMap<String, Integer>();
	for (String entity : entityArr) {
	    if (entity.length() > 3) {
		System.out.println(entity);
		String[] keyVal = entity.split("=");
		if (keyVal.length == 2 && keyVal[0].trim().length() > 4) {
		    String key = keyVal[0].trim();
		    int val = Integer.parseInt(keyVal[1]);
		    entityMap.put(key, val);
		}
	    }
	}
	entityMap = IOUtils.sortByValues(entityMap);
	String[] topEntities = IOUtils.getTopNEntities(entityMap,
		entityMap.size() / FRACTION_OF_TWEETS_TO_PROCESS);
	for (String entity : topEntities) {
	    topEntList.add(entity.trim());
	}

	try {
	    ResultSet res = IOUtils.getConnection()
		    .prepareStatement(TweetDO.SELECT_ALL_TEXT_QUERY)
		    .executeQuery();

	} catch (SQLException e) {
	}

	IOUtils.log("Creating threads....");
	int noOfEntityPerThread = (int) topEntities.length / NO_OF_THREADS;
	for (int i = 0; i < NO_OF_THREADS; i++) {
	    String[] entitiesForThread = getNextMelements(i
		    * noOfEntityPerThread, noOfEntityPerThread);
	    ProcessEntities entityThread = new ProcessEntities(
		    entitiesForThread, i);
	    threadList.add(entityThread);
	}

	IOUtils.log("Starting Threads");
	for (ProcessEntities pThread : threadList) {
	    pThread.start();
	}

	IOUtils.log("Waiting for threads to die....");
	for (ProcessEntities pThread : threadList) {
	    try {
		pThread.join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	IOUtils.log("All threads completed their work!");
    }

    /**
     * This function get the next offset element from topEntList starting from
     * an index
     * 
     * @param strtIndex
     *            starting point
     * @param offset
     *            number of elements to get
     * @return string array ranging from [strtIndex : strtIndex + offset
     */
    private static String[] getNextMelements(int strtIndex, int offset) {
	ArrayList<String> retList = new ArrayList<String>();
	for (int i = 0; i < offset; i++) {
	    String tweet = topEntList.get(strtIndex + i);
	    retList.add(tweet);
	}
	return retList.toArray(new String[retList.size()]);
    }

    private static int[] getVecSpace(String tweet) {
	int[] vecSpace = new int[topEntList.size()];
	int i = 0;
	for (String ent : topEntList) {
	    vecSpace[i] = org.apache.commons.lang3.StringUtils.countMatches(
		    tweet, ent);
	    i++;
	}
	return vecSpace;
    }

    private static double cosSim(double[] a, double[] b) {
	double dotp = 0, maga = 0, magb = 0;
	for (int i = 0; i < a.length; i++) {
	    dotp += a[i] * b[i];
	    maga += Math.pow(a[i], 2);
	    magb += Math.pow(b[i], 2);
	}
	maga = Math.sqrt(maga);
	magb = Math.sqrt(magb);
	double d = dotp / (maga * magb);
	return d == Double.NaN ? 0 : d;
    }
}
