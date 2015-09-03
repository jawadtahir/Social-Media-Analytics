package pk.lums.edu.sma.ml;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

import pk.lums.edu.sma.models.ClusterModel;
import pk.lums.edu.sma.processing.GetEntities;
import pk.lums.edu.sma.utils.IOUtils;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

public class OverlappingClusteringAll {
    /**
     * This script will find clustering from all overlap data of given cluster
     * 
     * @param args
     *            1 param in the path of directory where overlap region are
     *            placed. 2 param is flag for dynamic feature set
     */

    private static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static DecimalFormat decif = new DecimalFormat("#.####");
    private static Map<Integer, ClusterModel> tweetMap = null;
    private static List<String> listOfAttr = new ArrayList<String>();
    private static final int K = 5;
    private static final int MAX_ITER = 50;
    private static final int INIT = 3;
    private static final int NO_OF_THREADS = 4;
    private static File dir = null;
    private static List<ClusterModel> clusterModelList = null;
    private static Map<String, Integer> entityMap = new LinkedHashMap<String, Integer>();

    /**
     * This script finds the clustering in all overlap regions.
     * 
     * @param args
     *            First parameter would be the name of directory where all
     *            overlap regions are stored Second parameter would be the flag
     *            Y or N to get dynamic feature set Third parameter is number of
     *            feature set. Fourth parameter is optional if previous
     *            parameter is Y. else it would be the name of CSV file where
     *            feature set is stored.
     */
    public static void main(String[] args) {
	dir = new File(args[0]);
	if (dir.isDirectory()) {
	    // Traverse each file to create data set
	    IOUtils.log("Process Start with " + dir.listFiles().length
		    + " files");
	    // Container to store all data
	    clusterModelList = new ArrayList<ClusterModel>();
	    for (File clusterFile : dir.listFiles()) {
		if (!clusterFile.isDirectory()
			&& clusterFile.getName().contains("cluster")) {
		    String[] input = IOUtils.readFile(clusterFile
			    .getAbsolutePath());
		    String clusterName = clusterFile.getName().replace(".txt",
			    "");
		    clusterModelList.addAll(getClusterModelList(input,
			    clusterName));

		} else {
		    // is not a text file
		    IOUtils.deleteDir(clusterFile);
		}
	    }
	    entityMap = Collections.synchronizedMap(entityMap);
	    listOfAttr = getAttributes(args[1], Integer.parseInt(args[2]),
		    args[3]);

	    IOUtils.log(" Creating map <id, tweets>");
	    // Creating map so that we can print quickly instead of calling SQL
	    // query
	    tweetMap = makeMap(clusterModelList);

	    IOUtils.log(" Creating vector space of all tweets in region...");

	    LinkedHashMap<Integer, double[]> vecSpaceList = new LinkedHashMap<Integer, double[]>();

	    int count = 0;
	    for (Iterator<ClusterModel> it = clusterModelList.iterator(); it
		    .hasNext();) {
		count++;
		if (count % 1000 == 0)
		    IOUtils.log(Integer.toString(count)
			    + " vector spaces created");
		ClusterModel tdo = it.next();
		double[] temp = getVecSpace(tdo.getText());
		if (temp != null) {
		    vecSpaceList.put((int) tdo.getId(), temp);
		} else {
		    it.remove();
		}
	    }

	    IOUtils.log(" Going for main course...");
	    HashMap<double[], SortedSet<Integer>> cluster = kmean(
		    clusterModelList, vecSpaceList, K);

	    IOUtils.log(" Printing clusters...");
	    printClusters(cluster, 0);

	}
    }

    private static List<String> getAttributes(String flag, int featureSetSize,
	    String fileName) {
	// TODO Auto-generated method stub
	List<String> retList = new ArrayList<String>();

	if (flag.equalsIgnoreCase("Y")) {
	    getHashTags();
	    List<String> tweetList = new ArrayList<String>();
	    for (ClusterModel cmodel : clusterModelList) {
		tweetList.add(cmodel.getText());
	    }
	    GetEntities thread = new GetEntities(
		    tweetList.toArray(new String[tweetList.size()]), "1",
		    entityMap);
	    thread.start();
	    try {
		thread.join();
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    IOUtils.sortByValues(entityMap);
	    int count = 0;
	    for (Map.Entry<String, Integer> ent : entityMap.entrySet()) {
		if (count < featureSetSize) {
		    retList.add(ent.getKey().trim().toLowerCase());
		}
		count++;
	    }
	    return retList;

	} else if (flag.equalsIgnoreCase("N")) {
	    IOUtils.log("Reading entities...");
	    // Read attributes
	    String[] entityLine = IOUtils.readFile(fileName);
	    // Remove braces from the start and end
	    String csvEntities = entityLine[0].substring(1,
		    entityLine[0].length() - 1);
	    // Split the string so we can get an array of attributes
	    String[] entityArr = csvEntities.split(", ");
	    Map<String, Double> entityMap = new HashMap<String, Double>();
	    int count = 0;
	    // Create a hash map where key is attribute and value is its count
	    // in data set
	    for (String entity : entityArr) {
		count++;
		if (count <= featureSetSize) {
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
		    }
		}
	    }
	    entityMap = IOUtils.sortByValues(entityMap);
	    // Create a list of attributes
	    for (Map.Entry<String, Double> entity : entityMap.entrySet()) {
		retList.add(entity.getKey().trim().toLowerCase());
	    }
	}
	return retList;
    }

    private static void getHashTags() {
	int count = 0;
	StringBuffer query = new StringBuffer(
		"SELECT jsonTweet FROM TWEETDATA.TWEETDTAUS where idTWEETDTA IN (");
	for (ClusterModel cmodel : clusterModelList) {
	    query.append(cmodel.getId());
	    query.append(" ,");
	}
	query.deleteCharAt(query.length() - 1);
	query.append(");");
	Connection con = null;
	PreparedStatement pst = null;
	ResultSet res = null;
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(query.toString());
	    res = pst.executeQuery();
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
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (TwitterException e) {
	    // TODO: handle exception
	    e.printStackTrace();
	}

    }

    private static List<ClusterModel> getClusterModelList(String[] input,
	    String clusterName) {
	// TODO Auto-generated method stub
	List<ClusterModel> list = new ArrayList<ClusterModel>();
	int counter = 0;
	for (String tweetEnt : input) {
	    counter++;
	    StringBuilder sb = new StringBuilder(tweetEnt);
	    sb.append("  ");
	    String[] tweetEntArr = sb.toString().split(" , ");
	    if (tweetEntArr.length >= 4) {
		String loc = tweetEntArr[tweetEntArr.length - 1].trim();
		String id = tweetEntArr[tweetEntArr.length - 2].trim();
		String time = tweetEntArr[tweetEntArr.length - 3].trim();
		time = time.replace(",", "").trim();
		Date date = null;
		try {
		    date = df.parse(time);
		    sb = new StringBuilder();
		    for (int i = 1; i <= tweetEntArr.length - 4; i++) {
			sb.append(tweetEntArr[i]);
		    }
		    ClusterModel cModel = new ClusterModel(sb.toString(), date,
			    clusterName, Integer.parseInt(id), loc);
		    list.add(cModel);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }

	    if (counter % 1000 == 0) {
		IOUtils.log(" creating list of tweets. Current Count : "
			+ counter + " File : " + clusterName + ".txt");
	    }
	}
	return list;
    }

    private static Map<Integer, ClusterModel> makeMap(
	    List<ClusterModel> listTweets) {
	Map<Integer, ClusterModel> map = new LinkedHashMap<Integer, ClusterModel>();
	int count = 0;
	for (ClusterModel cmodel : listTweets) {
	    count++;
	    map.put(cmodel.getId(), cmodel);
	    if (count % 1000 == 0) {
		IOUtils.log(" creating map... Entries : " + count);
	    }
	}

	IOUtils.log(" map created with " + count + " entries for region "
		+ listTweets.get(0).getCluster());

	return map;
    }

    private static double[] getVecSpace(String tweet) {
	double[] vecSpace = new double[listOfAttr.size()];
	int i = 0;
	int nonZeroEnt = 0;
	for (String ent : listOfAttr) {
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

    public static int countSubstring(String str, String subStr) {
	// the result of split() will contain one more element than the
	// delimiter
	// the "-1" second argument makes it not discard trailing empty strings
	return str.split(Pattern.quote(subStr), -1).length - 1;
    }

    private static List<Integer> getKeySetAsList(
	    LinkedHashMap<Integer, double[]> vecspace) {
	List<Integer> retList = new ArrayList<Integer>();
	for (Iterator<Integer> it = vecspace.keySet().iterator(); it.hasNext();) {
	    retList.add(it.next());
	}

	return retList;
    }

    private static void printClusters(
	    Map<double[], SortedSet<Integer>> clusters, int iteration) {
	int i = 0;

	for (Map.Entry<double[], SortedSet<Integer>> entry : clusters
		.entrySet()) {
	    i++;
	    List<ClusterModel> cModelList = new ArrayList<ClusterModel>();
	    for (Integer id : entry.getValue()) {
		cModelList.add(tweetMap.get(id));
	    }
	    printCluster(cModelList, i, iteration);
	}
    }

    private static void printCluster(List<ClusterModel> tdoList, int i,
	    int iteration) {
	// File file = new File("clusters" + iteration);
	// if (!file.exists()) {
	// file.mkdir();
	// }
	StringBuilder sb = new StringBuilder();
	for (int j = 0; j < tdoList.size(); j++) {
	    ClusterModel tdo = tdoList.get(j);
	    sb.append(tdo.getText().trim() + " , "
		    + df.format(tdo.getDate()).toString() + " , " + tdo.getId()
		    + " , " + tdo.getLocation().trim() + "\n");
	}
	IOUtils.writeFile(dir.getAbsolutePath() + "/OLC" + "/" + iteration
		+ "_" + "cluster-" + i + ".txt", sb.toString().trim());

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

    private static HashMap<double[], SortedSet<Integer>> kmean(
	    List<ClusterModel> listTweets,
	    LinkedHashMap<Integer, double[]> vecspace, int k) {
	Map<double[], SortedSet<Integer>> clusters = new HashMap<double[], SortedSet<Integer>>();
	HashMap<double[], TreeSet<Integer>> step = new HashMap<double[], TreeSet<Integer>>();
	HashSet<Integer> rand = new HashSet<Integer>();
	TreeMap<Double, HashMap<double[], SortedSet<Integer>>> errorsums = new TreeMap<Double, HashMap<double[], SortedSet<Integer>>>();
	// Getting keyset of LINKED hashmap so that we can use indexing
	List<Integer> idList = getKeySetAsList(vecspace);
	for (int init = 0; init < INIT; init++) {

	    IOUtils.log(" Round : " + init);
	    clusters.clear();
	    step.clear();
	    rand.clear();
	    // randomly initialize cluster centers
	    while (rand.size() < k) {
		int randNo = (int) ((Math.random() * vecspace.size()));
		if (vecspace.get(idList.get(randNo)) != null)
		    rand.add(idList.get(randNo));
	    }
	    for (int r : rand) {
		double[] temp = new double[vecspace.get(r).length];
		System.arraycopy(vecspace.get(r), 0, temp, 0, temp.length);
		step.put(temp, new TreeSet<Integer>());
	    }
	    boolean go = true;
	    int iter = 0;
	    while (go) {

		IOUtils.log(" Iter : " + iter);
		clusters = new HashMap<double[], SortedSet<Integer>>(step);
		// ///////////////////////////////////////////////////////////

		// int j = 0;
		// IOUtils.log(" In process");
		// for (Map.Entry<Integer, double[]> entry :
		// vecspace.entrySet()) {
		// j++;
		// if (j % 1000 == 0)
		// IOUtils.log(j + " : " + entry.getKey() + " : ");
		// double[] cent = null;
		// double sim = 0;
		// for (double[] c : clusters.keySet()) {
		// // IOUtils.log("before" + c);
		// double csim = cosSim(entry.getValue(), c);
		// if (csim > sim) {
		// sim = csim;
		// cent = c;
		// }
		// }
		// if (cent != null && entry.getKey() != null) {
		// try {
		// clusters.get(cent).add(entry.getKey());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }

		// //////////////////////////////////////////////////////////
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
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}

		for (KmeanAssignmentThread thread : assThrdLst) {
		    try {
			thread.join();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}

		// centroid update step
		step.clear();
		for (double[] cent : clusters.keySet()) {
		    IOUtils.log(" Updating centroids");
		    double[] updatec = new double[cent.length];
		    for (int d : clusters.get(cent)) {
			double[] doc = vecspace.get(d);
			if (doc != null) {
			    for (int i = 0; i < updatec.length; i++)
				updatec[i] += doc[i];
			}
		    }
		    for (int i = 0; i < updatec.length; i++) {
			double temp = 0;
			String a = "";
			try {
			    a = decif.format(updatec[i]
				    / clusters.get(cent).size());
			    temp = Double.parseDouble(a);

			} catch (NumberFormatException ex) {
			    // System.out.println(ex.getMessage());
			    // System.out.println(ex.getCause());
			    // System.out.println(a);
			    // System.out.println(updatec[i]);
			    // System.out.println(clusters.get(cent).size());
			} finally {
			    updatec[i] = temp;
			}

		    }
		    step.put(updatec, new TreeSet<Integer>());
		}
		// check break conditions
		IOUtils.log(" checking break conditions...");
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
	    IOUtils.log(" calculate similarity sum and map it to the clustering");
	    double sumsim = 0;
	    for (double[] c : clusters.keySet()) {
		SortedSet<Integer> cl = clusters.get(c);
		for (int vi : cl) {
		    sumsim += cosSim(c, vecspace.get(vi));
		}
	    }
	    errorsums.put(sumsim, new HashMap<double[], SortedSet<Integer>>(
		    clusters));
	    IOUtils.log(" printing cluster");
	    try {
		printClusters(clusters, init + 1);
	    } catch (Exception ex) {
		IOUtils.log(ex.getMessage());
		IOUtils.log(" Encountered error \n"
			+ ex.getStackTrace().toString());
		System.out.println(ex.getMessage());
	    }

	}
	// pick the clustering with the maximum similarity sum and print the
	// filenames and indices
	IOUtils.log(" Best Convergence:");
	IOUtils.log(" "
		+ errorsums.get(errorsums.lastKey()).toString()
			.replaceAll("\\[[\\w@]+=", ""));
	HashMap<double[], SortedSet<Integer>> con = errorsums.get(errorsums
		.lastKey());
	return con;

    }

}
