package pk.lums.edu.sma.processing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class TweetProcessingSigRank {

    private final static int NO_OF_THREADS = 8;
    private static Map<String, Double> entityMap = new HashMap<String, Double>();
    private static Map<Date, Integer> dateMap = new HashMap<Date, Integer>();
    private static Map<EntityPhraseDateModel, Double> entityDateMap = new HashMap<EntityPhraseDateModel, Double>();
    private static ArrayList<TweetDO> twtList = new ArrayList<TweetDO>();
    private static String[] topEntities = null;

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.log("***************************************************************Program start*****************************************************************************");
	IOUtils.log(Calendar.getInstance().getTime().toString());
	Connection con = null;
	PreparedStatement pst = null;
	ResultSet res = null;
	IOUtils.log("Reading Tweets....");
	// Reading tweets
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.SELECT_ALL_QUERY_US);
	    res = pst.executeQuery();
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Converting Tweets into array....");
	    // getting tweets from result set
	    twtList = TweetDO.translateAllTweetDO(res);
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    con.close();
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	// Creating a synchronized hashed map so that all threads can share data
	entityMap = Collections.synchronizedMap(entityMap);
	dateMap = Collections.synchronizedMap(dateMap);
	entityDateMap = Collections.synchronizedMap(entityDateMap);
	// Empty out cluster folder
	IOUtils.clearClusterFolder();
	// IOUtils.log("Populating tweet array list....");
	// // populate strTwtList
	// for (String tweet : tweets) {
	// strTwtList.add(tweet);
	// }

	// This number of tweets will be assigned to each thread
	int noOfTweetsPerThread = twtList.size() / NO_OF_THREADS;
	List<NerUsageExampleWithSigRank> threadList = new ArrayList<NerUsageExampleWithSigRank>();
	IOUtils.log("Creating threads....");

	// Creating threads
	for (int i = 0; i < NO_OF_THREADS; i++) {
	    List<TweetDO> tweetsForThread = getNextMelements(i
		    * noOfTweetsPerThread, noOfTweetsPerThread);
	    NerUsageExampleWithSigRank thread = new NerUsageExampleWithSigRank(
		    tweetsForThread, Integer.toString(i), entityMap, dateMap,
		    entityDateMap);
	    threadList.add(thread);
	}

	IOUtils.log("Strating threads....");

	// Starting threads...
	for (NerUsageExampleWithSigRank thread : threadList) {
	    thread.start();
	}

	IOUtils.log("Waiting for threads to close");
	// Waiting for threads to close....
	for (NerUsageExampleWithSigRank thread : threadList) {
	    try {
		thread.join();
	    } catch (InterruptedException e) {

	    }
	}
	System.out.println("All threads completed their work.......");
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("All threads completed their work.......");
	// Sorting entities map
	entityMap = IOUtils.sortByValues(entityMap);
	IOUtils.log("Writing Entities on file.... Here are they");
	// Writing entities on file so in case of any mishap we can start
	// over.....
	IOUtils.writeFile("Entities.txt", entityMap.toString(), false);
	IOUtils.log(entityMap.toString());
	topEntities = IOUtils.getTopNEntities(entityMap, entityMap.size() / 4);
	IOUtils.log("Going to process entities....");

	// temporary copy entityDateMap
	Map<EntityPhraseDateModel, Double> tempMap = new HashMap<EntityPhraseDateModel, Double>(
		entityDateMap);

	for (Map.Entry<EntityPhraseDateModel, Double> ent : entityDateMap
		.entrySet()) {
	    double g2 = 0f;
	    // x = e, y = d
	    double oxy = (double) ent.getValue().intValue()
		    / (double) twtList.size();
	    double px = (double) entityMap.get(ent.getKey().getEntity())
		    .intValue() / (double) twtList.size();
	    double py = (double) dateMap.get(ent.getKey().getDate()).intValue()
		    / (double) twtList.size();
	    double exy = px * py;
	    double lnoxyexy = Math.log(oxy / exy);
	    g2 += oxy * lnoxyexy;
	    // x = e, y = !d
	    oxy = 0f;
	    py = 0f;
	    for (Map.Entry<EntityPhraseDateModel, Double> tempEnt : tempMap
		    .entrySet()) {
		if (tempEnt.getKey().getEntity()
			.equals(ent.getKey().getEntity())
			&& !tempEnt.getKey().getDate()
				.equals(ent.getKey().getDate())) {
		    oxy += (double) tempEnt.getValue().intValue();
		}
	    }
	    oxy = oxy / (double) twtList.size();
	    for (Map.Entry<Date, Integer> tempEnt : dateMap.entrySet()) {
		if (!tempEnt.getKey().equals(ent.getKey().getDate())) {
		    py += (double) tempEnt.getValue().intValue();
		}
	    }
	    py /= (double) twtList.size();
	    exy = px * py;
	    lnoxyexy = Math.log(oxy / exy);
	    g2 += oxy * lnoxyexy;
	    // x = !e, y = !d
	    oxy = 0f;
	    px = 0f;

	    for (Map.Entry<EntityPhraseDateModel, Double> tempEnt : entityDateMap
		    .entrySet()) {
		if (!tempEnt.getKey().getDate().equals(ent.getKey().getDate())
			&& !tempEnt.getKey().getEntity()
				.equals(ent.getKey().getEntity())) {
		    oxy += (double) tempEnt.getValue().intValue();
		}
	    }

	    oxy /= (double) twtList.size();

	    for (Map.Entry<String, Double> tempEnt : entityMap.entrySet()) {
		if (!tempEnt.getKey().equals(ent.getKey().getEntity())) {
		    px += (double) tempEnt.getValue().intValue();
		}
	    }

	    px /= (double) twtList.size();
	    exy = px * py;
	    lnoxyexy = Math.log(oxy / exy);
	    g2 += oxy * lnoxyexy;

	    // x = !e, y = d
	    oxy = 0f;
	    py = 0f;

	    for (Map.Entry<EntityPhraseDateModel, Double> tempEnt : entityDateMap
		    .entrySet()) {
		if (!tempEnt.getKey().getEntity()
			.equals(ent.getKey().getEntity())
			&& tempEnt.getKey().getDate()
				.equals(ent.getKey().getDate())) {
		    oxy += (double) tempEnt.getValue().intValue();
		}
	    }
	    oxy /= (double) twtList.size();

	    for (Map.Entry<Date, Integer> tempEnt : dateMap.entrySet()) {
		if (tempEnt.getKey().equals(ent.getKey().getDate())) {
		    py += (double) tempEnt.getValue().intValue();
		}
	    }
	    py /= (double) twtList.size();
	    exy = px * py;
	    lnoxyexy = Math.log(oxy / exy);
	    g2 += oxy * lnoxyexy;

	    ent.setValue(g2);

	}
	entityDateMap = IOUtils.sortByValues(entityDateMap);
	String text = entityDateMap.toString();
	IOUtils.writeFile("Events.txt", entityDateMap.toString(), false);

	// EntityProcessor ep = new EntityProcessor();
	// ep.process(entityMap);
	// ProcessEntities proEnt = new ProcessEntities(topEntities, 1);
	// boolean completed = false;
	// Creating clusters
	// while (completed == false) {
	// try {
	// proEnt.start();
	// completed = true;
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// }
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("***************************************************************Program end*****************************************************************************");

    }

    /**
     * This function get the next offset element from strTwtList starting from
     * an index
     * 
     * @param strtIndex
     *            starting point
     * @param offset
     *            number of elements to get
     * @return string array ranging from [strtIndex : strtIndex + offset
     */
    private static List<TweetDO> getNextMelements(int strtIndex, int offset) {
	List<TweetDO> retList = new ArrayList<TweetDO>();
	for (int i = 0; i < offset; i++) {
	    TweetDO tweet = twtList.get(strtIndex + i);
	    retList.add(tweet);
	}
	return retList;
    }
}
