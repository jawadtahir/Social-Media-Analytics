package pk.lums.edu.sma.ml;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.processing.KmeanAssignmentThread;
import pk.lums.edu.sma.processing.ProcessEntities;
import pk.lums.edu.sma.utils.IOUtils;

public class EntityProcessor {

    private static final int NO_OF_THREADS = 4;
    private static final int K = 50;
    private static final int MAX_ITER = 20;
    private static final int INIT = 5;
    private static final int FRACTION_OF_TWEETS_TO_PROCESS = 4;
    private static ArrayList<ProcessEntities> threadList = new ArrayList<ProcessEntities>();
    private static ArrayList<String> topEntList = new ArrayList<String>();
    private static DecimalFormat df = new DecimalFormat("#.####");

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
	    if (count <= 200) {
		// System.out.println(entity);
		String[] keyVal = entity.split("=");
		// if (keyVal.length == 2 && keyVal[0].trim().length() > 4) {
		String key = keyVal[0].trim();
		int val = Integer.parseInt(keyVal[1]);
		entityMap.put(key, (double) val);
		sb.append(key + ", " + val + "\n");
		// }
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
	IOUtils.log("Selecting all tweets...");
	List<TweetDO> tweets = null;
	try {
	    ResultSet res = IOUtils.getConnection()
		    .prepareStatement(TweetDO.SELECT_ALL_ID_TEXT_QUERY_US)
		    .executeQuery();
	    tweets = TweetDO.translateTextIdTweetDO(res);

	} catch (SQLException e) {
	}

	// tweets = tweets.subList(0, 100);

	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Creating vector space of all tweets...");
	LinkedHashMap<Integer, double[]> vecSpaceList = new LinkedHashMap<Integer, double[]>();

	count = 0;
	for (Iterator<TweetDO> it = tweets.iterator(); it.hasNext();) {
	    count++;
	    if (count % 1000 == 0)
		IOUtils.log(Integer.toString(count));
	    TweetDO tdo = it.next();
	    double[] temp = getVecSpace(tdo.getTextTweet());
	    if (temp != null) {
		vecSpaceList.put((int) tdo.getId(), temp);
	    } else {
		it.remove();
	    }
	}

	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Going for main course...");
	HashMap<double[], SortedSet<Integer>> cluster = kmean(tweets,
		vecSpaceList, K);
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Printing clusters...");
	printClusters(cluster);
	IOUtils.log(Calendar.getInstance().getTime().toString());

	// IOUtils.log("Creating threads....");
	// int noOfEntityPerThread = (int) topEntities.length / NO_OF_THREADS;
	// for (int i = 0; i < NO_OF_THREADS; i++) {
	// String[] entitiesForThread = getNextMelements(i
	// * noOfEntityPerThread, noOfEntityPerThread);
	// ProcessEntities entityThread = new ProcessEntities(
	// entitiesForThread, i);
	// threadList.add(entityThread);
	// }
	//
	// IOUtils.log("Starting Threads");
	// for (ProcessEntities pThread : threadList) {
	// pThread.start();
	// }
	//
	// IOUtils.log("Waiting for threads to die....");
	// for (ProcessEntities pThread : threadList) {
	// try {
	// pThread.join();
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// IOUtils.log("All threads completed their work!");
    }

    private static void printClusters(
	    HashMap<double[], SortedSet<Integer>> cluster) {
	// TODO Auto-generated method stub
	// IOUtils.clearClusterFolder();
	// Iterator<double[]> itr = cluster.keySet().iterator();
	// Connection con = null;
	// PreparedStatement pst = null;
	// try {
	// con = IOUtils.getConnection();
	// pst = con.prepareStatement(TweetDO.SELECT_ALL_FROM_ID_US);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// int i = 0;
	// while (itr.hasNext()) {
	// double[] key = itr.next();
	// SortedSet<Integer> tweets = cluster.get(key);
	// Iterator<Integer> setItr = tweets.iterator();
	// List<TweetDO> tdoList = new ArrayList<TweetDO>();
	// i++;
	// while (setItr.hasNext()) {
	// int id = setItr.next();
	// try {
	// pst.setInt(1, id);
	// List<TweetDO> tempList = TweetDO.translateAllTweetDO(pst
	// .executeQuery());
	// tdoList.add(tempList.get(0));
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// printCluster(tdoList, i);
	// }
	printClusters(cluster, 0);

    }

    private static void printCluster(List<TweetDO> tdoList, int i) {
	// TODO Auto-generated method stub
	File file = new File("clusters");
	if (!file.exists()) {
	    file.mkdir();
	}
	StringBuilder sb = new StringBuilder();
	for (int j = 0; j < tdoList.size(); j++) {
	    TweetDO tdo = tdoList.get(j);
	    sb.append(tdo.getTextTweet().trim() + " , "
		    + tdo.getDateTextTweet().trim() + " , " + tdo.getId()
		    + " , " + tdo.getLocTweet().trim() + "\n");
	}
	IOUtils.writeFile(file.getAbsolutePath() + "/cluster-" + i + ".txt", sb
		.toString().trim(), false);

    }

    private static void printClusters(
	    Map<double[], SortedSet<Integer>> clusters, int iteration) {
	// TODO Auto-generated method stub
	IOUtils.clearClusterFolder("clusters" + iteration);
	Iterator<double[]> itr = clusters.keySet().iterator();
	Connection con = null;
	PreparedStatement pst = null;
	try {
	    con = IOUtils.getConnection();
	    // pst = con.prepareStatement(TweetDO.SELECT_ALL_FROM_ID_US);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	int i = 0;
	while (itr.hasNext()) {
	    StringBuilder sb = new StringBuilder();
	    double[] key = itr.next();
	    SortedSet<Integer> tweets = clusters.get(key);
	    Iterator<Integer> setItr = tweets.iterator();
	    List<TweetDO> tdoList = new ArrayList<TweetDO>();
	    i++;
	    int count = 0;
	    sb.append("SELECT idTWEETDTA, textTweet, dateTextTweet, locationTweet FROM TWEETDATA.TWEETDTAUS where idTWEETDTA IN ( ");
	    while (setItr.hasNext()) {
		int id = setItr.next();
		sb.append(id + " ,");
		count++;
		// try {
		// pst.setInt(1, id);
		// List<TweetDO> tempList = TweetDO.translateAllTweetDO(pst
		// .executeQuery());
		// tdoList.add(tempList.get(0));
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	    }
	    sb.deleteCharAt(sb.length() - 1);
	    sb.append(");");
	    // IOUtils.log(sb.toString());
	    if (count != 0) {
		// System.out.println(sb.toString());
		try {
		    pst = con.prepareStatement(sb.toString());
		    ResultSet res = pst.executeQuery();
		    List<TweetDO> templist = TweetDO
			    .translateTextIdDateLocTweetDO(res);
		    for (TweetDO tweetDO : templist) {
			tdoList.add(tweetDO);
		    }
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

		printCluster(tdoList, i, iteration);

	    }
	}

    }

    private static void printCluster(List<TweetDO> tdoList, int i, int iteration) {
	// TODO Auto-generated method stub
	File file = new File("clusters" + iteration);
	if (!file.exists()) {
	    file.mkdir();
	}
	StringBuilder sb = new StringBuilder();
	for (int j = 0; j < tdoList.size(); j++) {
	    TweetDO tdo = tdoList.get(j);
	    sb.append(tdo.getTextTweet().trim() + " , "
		    + tdo.getDateTextTweet().trim() + " , " + tdo.getId()
		    + " , " + tdo.getLocTweet().trim() + "\n");
	}
	IOUtils.writeFile(file.getAbsolutePath() + "/cluster-" + i + ".txt", sb
		.toString().trim());

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
    private static LinkedHashMap<Integer, double[]> getNextMelements(
	    int strtIndex, int offset, LinkedHashMap<Integer, double[]> list) {
	LinkedHashMap<Integer, double[]> retList = new LinkedHashMap<Integer, double[]>();
	long i = 0;
	for (Map.Entry<Integer, double[]> entry : list.entrySet()) {
	    if (i < (strtIndex + offset) && i >= (strtIndex)) {
		retList.put(entry.getKey(), entry.getValue());
	    }
	    i++;
	}
	return retList;
    }

    private static List<TweetDO> getNextMelements(int strtIndex, int offset,
	    List<TweetDO> tweets) {
	List<TweetDO> retList = new ArrayList<TweetDO>();
	for (int i = strtIndex; i < (strtIndex + offset); i++) {
	    TweetDO tdo = tweets.get(i);
	    retList.add(tdo);
	}
	return retList;
    }

    private static double[] getVecSpace(String tweet) {
	double[] vecSpace = new double[topEntList.size()];
	int i = 0;
	int nonZeroEnt = 0;
	for (String ent : topEntList) {
	    vecSpace[i] = countSubstring(tweet.toLowerCase(), ent);
	    if (vecSpace[i] > 0f) {
		nonZeroEnt++;
	    }
	    i++;
	}
	if (nonZeroEnt == 0) {
	    return null;
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

    public static int countSubstring(String str, String subStr) {
	// the result of split() will contain one more element than the
	// delimiter
	// the "-1" second argument makes it not discard trailing empty strings
	return str.split(Pattern.quote(subStr), -1).length - 1;
    }

    private static HashMap<double[], SortedSet<Integer>> kmean(
	    List<TweetDO> tweets, LinkedHashMap<Integer, double[]> vecspace,
	    int k) {
	Map<double[], SortedSet<Integer>> clusters = new HashMap<double[], SortedSet<Integer>>();
	HashMap<double[], TreeSet<Integer>> step = new HashMap<double[], TreeSet<Integer>>();
	HashSet<Integer> rand = new HashSet<Integer>();
	TreeMap<Double, HashMap<double[], SortedSet<Integer>>> errorsums = new TreeMap<Double, HashMap<double[], SortedSet<Integer>>>();
	for (int init = 0; init < INIT; init++) {
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Round : " + init);
	    clusters.clear();
	    step.clear();
	    rand.clear();
	    // randomly initialize cluster centers
	    while (rand.size() < k) {
		int randNo = (int) ((Math.random() * vecspace.size()));
		if (vecspace.get(randNo) != null)
		    rand.add(randNo);
	    }
	    for (int r : rand) {
		double[] temp = new double[vecspace.get(r).length];
		System.arraycopy(vecspace.get(r), 0, temp, 0, temp.length);
		step.put(temp, new TreeSet<Integer>());
	    }
	    boolean go = true;
	    int iter = 0;
	    while (go) {
		IOUtils.log(Calendar.getInstance().getTime().toString());
		IOUtils.log("Iter : " + iter);
		clusters = new HashMap<double[], SortedSet<Integer>>(step);
		List<KmeanAssignmentThread> assThrdLst = new ArrayList<KmeanAssignmentThread>();
		// cluster assignment step
		int noOfVecSpPrThread = vecspace.size() / NO_OF_THREADS;
		for (int i = 0; i < NO_OF_THREADS; i++) {
		    LinkedHashMap<Integer, double[]> VecSpace = getNextMelements(
			    i * noOfVecSpPrThread, noOfVecSpPrThread, vecspace);
		    assThrdLst.add(new KmeanAssignmentThread(i, VecSpace,
			    clusters));
		    if (i == NO_OF_THREADS - 1) {
			if ((i + 1) * noOfVecSpPrThread < vecspace.size()) {
			    LinkedHashMap<Integer, double[]> VecSpacen = getNextMelements(
				    (i + 1) * noOfVecSpPrThread,
				    vecspace.size()
					    - ((i + 1) * noOfVecSpPrThread),
				    vecspace);
			    assThrdLst.add(new KmeanAssignmentThread(i + 1,
				    VecSpacen, clusters));
			}
		    }
		}

		for (KmeanAssignmentThread thread : assThrdLst) {
		    thread.start();
		    try {
			thread.sleep(500);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

		for (KmeanAssignmentThread thread : assThrdLst) {
		    try {
			thread.join();
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

		// centroid update step
		step.clear();
		for (double[] cent : clusters.keySet()) {
		    IOUtils.log("Updating centroids");
		    double[] updatec = new double[cent.length];
		    for (int d : clusters.get(cent)) {
			double[] doc = vecspace.get(d);
			for (int i = 0; i < updatec.length; i++)
			    updatec[i] += doc[i];
		    }
		    for (int i = 0; i < updatec.length; i++) {
			double temp = 0;
			String a = "";
			try {
			    a = df.format(updatec[i]
				    / clusters.get(cent).size());
			    temp = Double.parseDouble(a);

			} catch (NumberFormatException ex) {
			    System.out.println(ex.getMessage());
			    System.out.println(ex.getCause());
			    System.out.println(a);
			    System.out.println(updatec[i]);
			    System.out.println(clusters.get(cent).size());
			} finally {
			    updatec[i] = temp;
			}

		    }
		    step.put(updatec, new TreeSet<Integer>());
		}
		// check break conditions
		String oldcent = "", newcent = "";
		for (double[] x : clusters.keySet())
		    oldcent += Arrays.toString(x);
		for (double[] x : step.keySet())
		    newcent += Arrays.toString(x);
		if (oldcent.equals(newcent))
		    go = false;
		if (++iter >= MAX_ITER)
		    go = false;
	    }
	    // System.out.println(clusters.toString()
	    // .replaceAll("\\[[\\w@]+=", ""));
	    if (iter < MAX_ITER)
		System.out.println("Converged in " + iter + " steps.");
	    else
		System.out
			.println("Stopped after " + MAX_ITER + " iterations.");
	    System.out.println("");

	    // calculate similarity sum and map it to the clustering
	    double sumsim = 0;
	    for (double[] c : clusters.keySet()) {
		SortedSet<Integer> cl = clusters.get(c);
		for (int vi : cl) {
		    sumsim += cosSim(c, vecspace.get(vi));
		}
	    }
	    errorsums.put(sumsim, new HashMap<double[], SortedSet<Integer>>(
		    clusters));
	    try {
		IOUtils.log("printing cluster");
		printClusters(clusters, init + 1);
	    } catch (Exception ex) {
		IOUtils.log(ex.getMessage());
		System.out.println(ex.getMessage());
	    }

	}
	// pick the clustering with the maximum similarity sum and print the
	// filenames and indices
	System.out.println("Best Convergence:");
	System.out.println(errorsums.get(errorsums.lastKey()).toString()
		.replaceAll("\\[[\\w@]+=", ""));
	HashMap<double[], SortedSet<Integer>> con = errorsums.get(errorsums
		.lastKey());
	return con;

    }

}
