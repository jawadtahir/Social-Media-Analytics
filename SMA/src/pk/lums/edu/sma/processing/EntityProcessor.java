package pk.lums.edu.sma.processing;

import java.sql.ResultSet;
import java.sql.SQLException;
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

	List<TweetDO> tweets = null;
	try {
	    ResultSet res = IOUtils.getConnection()
		    .prepareStatement(TweetDO.SELECT_ALL_ID_TEXT_QUERY)
		    .executeQuery();
	    tweets = TweetDO.translateTextIdTweetDO(res);

	} catch (SQLException e) {
	}

	tweets = tweets.subList(0, 100);

	LinkedHashMap<Long, double[]> vecSpaceList = new LinkedHashMap<Long, double[]>();

	for (Iterator<TweetDO> it = tweets.iterator(); it.hasNext();) {
	    TweetDO tdo = it.next();
	    double[] temp = getVecSpace(tdo.getTextTweet());
	    if (temp != null) {
		vecSpaceList.put(tdo.getId(), temp);
	    } else {
		it.remove();
	    }
	}

	kmean(tweets, vecSpaceList, 5);

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
    private static LinkedHashMap<Long, double[]> getNextMelements(
	    int strtIndex, int offset, LinkedHashMap<Long, double[]> list) {
	list.LinkedHashMap<Long, double[]> retList = new LinkedHashMap<Long, double[]>();
	for (int i = 0; i < offset; i++) {
	    double[] vecSpace = list.get(strtIndex + i);
	    retList.add(vecSpace);
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

    private static void kmean(List<TweetDO> tweets,
	    LinkedHashMap<Long, double[]> vecspace, int k) {
	Map<double[], SortedSet<Integer>> clusters = new HashMap<double[], SortedSet<Integer>>();
	HashMap<double[], TreeSet<Integer>> step = new HashMap<double[], TreeSet<Integer>>();
	HashSet<Integer> rand = new HashSet<Integer>();
	TreeMap<Double, HashMap<double[], SortedSet<Integer>>> errorsums = new TreeMap<Double, HashMap<double[], SortedSet<Integer>>>();
	int maxiter = 10;
	for (int init = 0; init < 3; init++) {
	    clusters.clear();
	    step.clear();
	    rand.clear();
	    // randomly initialize cluster centers
	    while (rand.size() < k)
		rand.add((int) (Math.random() * vecspace.size()));
	    for (int r : rand) {
		double[] temp = new double[vecspace.get(r).length];
		System.arraycopy(vecspace.get(r), 0, temp, 0, temp.length);
		step.put(temp, new TreeSet<Integer>());
	    }
	    boolean go = true;
	    int iter = 0;
	    while (go) {
		clusters = new HashMap<double[], SortedSet<Integer>>(step);
		List<KmeanAssignmentThread> assThrdLst = new ArrayList<KmeanAssignmentThread>();
		// cluster assignment step
		int noOfVecSpPrThread = vecspace.size() / NO_OF_THREADS;
		for (int i = 0; i < NO_OF_THREADS; i++) {
		    List<double[]> VecSpace = getNextMelements(i
			    * noOfVecSpPrThread, noOfVecSpPrThread, vecspace);
		    assThrdLst.add(new KmeanAssignmentThread(i, VecSpace,
			    clusters));
		    if (i == NO_OF_THREADS - 1) {
			if ((i + 1) * noOfVecSpPrThread < vecspace.size()) {
			    List<double[]> VecSpacen = getNextMelements((i + 1)
				    * noOfVecSpPrThread, vecspace.size()
				    - ((i + 1) * noOfVecSpPrThread), vecspace);
			    assThrdLst.add(new KmeanAssignmentThread(i + 1,
				    VecSpacen, clusters));
			}
		    }
		}

		for (KmeanAssignmentThread thread : assThrdLst) {
		    thread.start();
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
		    double[] updatec = new double[cent.length];
		    for (int d : clusters.get(cent)) {
			double[] doc = vecspace.get(d);
			for (int i = 0; i < updatec.length; i++)
			    updatec[i] += doc[i];
		    }
		    for (int i = 0; i < updatec.length; i++)
			updatec[i] /= clusters.get(cent).size();
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
		if (++iter >= maxiter)
		    go = false;
	    }
	    System.out.println(clusters.toString()
		    .replaceAll("\\[[\\w@]+=", ""));
	    if (iter < maxiter)
		System.out.println("Converged in " + iter + " steps.");
	    else
		System.out.println("Stopped after " + maxiter + " iterations.");
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

	}
	// pick the clustering with the maximum similarity sum and print the
	// filenames and indices
	System.out.println("Best Convergence:");
	System.out.println(errorsums.get(errorsums.lastKey()).toString()
		.replaceAll("\\[[\\w@]+=", ""));
	System.out.print("{");
	for (double[] cent : errorsums.get(errorsums.lastKey()).keySet()) {
	    System.out.print("[");
	    for (int pts : errorsums.get(errorsums.lastKey()).get(cent)) {
		System.out.print(tweets.get(pts).getId() + ", ");
	    }
	    System.out.print("\b\b], ");
	}
	System.out.println("\b\b}");
    }
}
