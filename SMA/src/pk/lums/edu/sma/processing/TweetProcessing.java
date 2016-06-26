package pk.lums.edu.sma.processing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

public class TweetProcessing {

    private final static int NO_OF_THREADS = 8;
    private static Map<String, Integer> entityMap = new HashMap<String, Integer>();
    private static ArrayList<String> strTwtList = new ArrayList<String>();
    private static String[] topEntities = null;

    public static void main(String[] args) {
	IOUtils.log("***************************************************************Program start*****************************************************************************");
	Connection con = null;
	PreparedStatement pst = null;
	ResultSet res = null;
	// Creating a synchronized hashed map so that all threads can share data
	entityMap = Collections.synchronizedMap(entityMap);
	IOUtils.log("Reading Tweets....");
	// Reading tweets
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.SELECT_JSON_QUERY_NEW);
	    res = pst.executeQuery();
	    IOUtils.log("getting hashtags....");
	    getHashTags(res);
	    // IOUtils.log("Converting Tweets into array....");
	    // // getting tweets from result set
	    // strTwtList = TweetDO.getTextArrayOfColumn(res, "jsonTweet");
	    // IOUtils.log(Calendar.getInstance().getTime().toString());
	    res.close();
	    pst.close();
	    con.close();
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	res = null;
	pst = null;
	con = null;
	try {
	    IOUtils.log("Reading Tweets....");
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.SELECT_ALL_TEXT_QUERY_NEW);
	    res = pst.executeQuery();
	    // IOUtils.log(Calendar.getInstance().getTime().toString());
	    // IOUtils.log("getting hashtags....");
	    // getHashTags(res);
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Converting Tweets into array....");
	    // getting tweets from result set
	    strTwtList = TweetDO.getTextArrayOfColumn(res, "textTweet");
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    res.close();
	    pst.close();
	    con.close();
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}
	// Empty out cluster folder
	// IOUtils.clearClusterFolder();
	// IOUtils.log("Populating tweet array list....");
	// // populate strTwtList
	// for (String tweet : tweets) {
	// strTwtList.add(tweet);
	// }

	res = null;
	pst = null;
	con = null;

	System.gc();
	// This number of tweets will be assigned to each thread
	int noOfTweetsPerThread = strTwtList.size() / NO_OF_THREADS;
	ArrayList<GetEntities> threadList = new ArrayList<GetEntities>();
	IOUtils.log("Creating threads....");

	// Creating threads
	for (int i = 0; i < NO_OF_THREADS; i++) {
	    String[] tweetsForThread = getNextMelements(
		    i * noOfTweetsPerThread, noOfTweetsPerThread);
	    GetEntities thread = new GetEntities(tweetsForThread,
		    Integer.toString(i), entityMap);
	    threadList.add(thread);
	}

	IOUtils.log("Strating threads....");

	// Starting threads...
	for (GetEntities thread : threadList) {
	    thread.start();
	}

	IOUtils.log("Waiting for threads to close");
	// Waiting for threads to close....
	for (GetEntities thread : threadList) {
	    try {
		thread.join();
	    } catch (InterruptedException e) {

	    }
	}
	System.out.println("All threads completed their work.......");
	IOUtils.log("All threads completed their work.......");
	// Sorting entities map
	entityMap = IOUtils.sortByValues(entityMap);
	IOUtils.log("Writing Entities on file.... Here are they");
	// Writing entities on file so in case of any mishap we can start
	// over.....
	IOUtils.writeFile("EntitiesNEW.txt", entityMap.toString(), false);
	IOUtils.log(entityMap.toString());
	topEntities = IOUtils.getTopNEntities(entityMap, 100);
	StringBuilder sb = new StringBuilder();
	for (String tempStr : topEntities) {
	    sb.append(tempStr + " , ");
	}
	IOUtils.writeFile("TopEntNEW.txt", sb.toString(), false);
	IOUtils.log("Going to process entities....");
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
	// e.printStackTrace();
	// }// Reading tweets

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
    private static String[] getNextMelements(int strtIndex, int offset) {
	ArrayList<String> retList = new ArrayList<String>();
	for (int i = 0; i < offset; i++) {
	    String tweet = strTwtList.get(strtIndex + i);
	    retList.add(tweet);
	}
	return retList.toArray(new String[retList.size()]);
    }

    private static void getHashTags(ResultSet res) {
	int count = 0;
	try {
	    while (res.next()) {
		count++;
		if (count % 10000 == 0) {
		    IOUtils.log(Integer.toString(count));
		}
		Status status = TwitterObjectFactory.createStatus(res
			.getString(1));
		HashtagEntity hts[] = status.getHashtagEntities();
		for (HashtagEntity ht : hts) {
		    String htag = ht.getText().trim().toLowerCase();
		    if (entityMap.containsKey(htag)) {
			entityMap.put(htag, entityMap.get(htag) + 1);
		    } else {
			entityMap.put(htag, 1);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
