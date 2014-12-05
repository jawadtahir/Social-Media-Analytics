package pk.lums.edu.sma.processing;

import java.util.ArrayList;

import pk.lums.edu.sma.utils.IOUtils;

public class TweetProcessing {

    private final static int NO_OF_THREADS = 10;

    private static ArrayList<String> strTwtList = new ArrayList<String>();

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	IOUtils.clearClusterFolder();
	String[] tweets = IOUtils.readFile("tweetsSmall.txt");
	// populate strTwtList
	for (String tweet : tweets) {
	    strTwtList.add(tweet);
	}
	int noOfTweetsPerThread = tweets.length / NO_OF_THREADS;
	ArrayList<GetEntities> threadList = new ArrayList<GetEntities>();
	for (int i = 0; i < NO_OF_THREADS; i++) {
	    String[] tweetsForThread = getNextMelements(
		    i * noOfTweetsPerThread, noOfTweetsPerThread);
	    GetEntities thread = new GetEntities(tweetsForThread,
		    Integer.toString(i));
	    threadList.add(thread);
	}

	for (GetEntities thread : threadList) {
	    thread.start();
	}
	for (GetEntities thread : threadList) {
	    try {
		thread.join();
	    } catch (InterruptedException e) {

	    }
	}
	System.out.println("All threads completed their work.......");
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
}
