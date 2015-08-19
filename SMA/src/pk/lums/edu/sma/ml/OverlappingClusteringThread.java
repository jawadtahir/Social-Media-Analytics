package pk.lums.edu.sma.ml;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import pk.lums.edu.sma.utils.IOUtils;

public class OverlappingClusteringThread extends Thread {

    private Thread t = null;
    private List<File> listOfFiles = null;
    private List<String> listOfAttr = null;
    private File currFile = null;
    private Map<Integer, ClusterModel> tweetMap = null;
    private static final int MAX_ITER = 70;
    private static final int INIT = 3;
    private static final int K = 5;

    private static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static DecimalFormat decif = new DecimalFormat("#.####");

    public OverlappingClusteringThread(List<File> listOfFiles,
	    List<String> listOfAttr, String name) {
	this.listOfAttr = listOfAttr;
	this.listOfFiles = listOfFiles;
	this.t = new Thread(this, name);
    }

    @Override
    public void run() {
	process();
    }

    private void process() {
	for (File overLap : listOfFiles) {
	    // make directory to store clusters
	    if (!overLap.exists()) {
		overLap.mkdir();
	    }
	    currFile = overLap;
	    File tempFile = new File(overLap.getAbsoluteFile() + ".txt");
	    List<ClusterModel> listTweets = readFileAndCreateModel(tempFile,
		    overLap.getName());
	    tweetMap = makeMap(listTweets);
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Creating vector space of all tweets...");
	    LinkedHashMap<Integer, double[]> vecSpaceList = new LinkedHashMap<Integer, double[]>();

	    int count = 0;
	    for (Iterator<ClusterModel> it = listTweets.iterator(); it
		    .hasNext();) {
		count++;
		if (count % 1000 == 0)
		    IOUtils.log(Integer.toString(count));
		ClusterModel tdo = it.next();
		double[] temp = getVecSpace(tdo.getText());
		if (temp != null) {
		    vecSpaceList.put((int) tdo.getId(), temp);
		} else {
		    it.remove();
		}
	    }

	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Going for main course...");
	    HashMap<double[], SortedSet<Integer>> cluster = kmean(listTweets,
		    vecSpaceList, K);
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Printing clusters...");
	    printClusters(cluster, 0);
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	}
    }

    private Map<Integer, ClusterModel> makeMap(List<ClusterModel> listTweets) {
	Map<Integer, ClusterModel> map = new LinkedHashMap<Integer, ClusterModel>();
	for (ClusterModel cmodel : listTweets) {
	    map.put(cmodel.getId(), cmodel);
	}
	return map;
    }

    private List<ClusterModel> readFileAndCreateModel(File tempFile,
	    String clusterName) {
	List<ClusterModel> list = new ArrayList<ClusterModel>();
	String[] tweetEnts = IOUtils.readFile(tempFile.getAbsolutePath());
	int counter = 0;
	for (String tweetEnt : tweetEnts) {
	    // if (counter % 1000 == 0) {
	    // try {
	    // // Thread.sleep(50);
	    // } catch (InterruptedException e) {
	    // // TODO Auto-generated catch block
	    // e.printStackTrace();
	    // }
	    // }
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
	}
	return list;
    }

    private double[] getVecSpace(String tweet) {
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

    public int countSubstring(String str, String subStr) {
	// the result of split() will contain one more element than the
	// delimiter
	// the "-1" second argument makes it not discard trailing empty strings
	return str.split(Pattern.quote(subStr), -1).length - 1;
    }

    private HashMap<double[], SortedSet<Integer>> kmean(
	    List<ClusterModel> listTweets,
	    LinkedHashMap<Integer, double[]> vecspace, int k) {
	Map<double[], SortedSet<Integer>> clusters = new HashMap<double[], SortedSet<Integer>>();
	HashMap<double[], TreeSet<Integer>> step = new HashMap<double[], TreeSet<Integer>>();
	HashSet<Integer> rand = new HashSet<Integer>();
	TreeMap<Double, HashMap<double[], SortedSet<Integer>>> errorsums = new TreeMap<Double, HashMap<double[], SortedSet<Integer>>>();
	// Getting keyset of LINKED hashmap so that we can use indexing
	List<Integer> idList = getKeySetAsList(vecspace);
	for (int init = 0; init < INIT; init++) {
	    IOUtils.log(Calendar.getInstance().getTime().toString());
	    IOUtils.log("Round : " + init);
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
		IOUtils.log(Calendar.getInstance().getTime().toString());
		IOUtils.log("Iter : " + iter);
		clusters = new HashMap<double[], SortedSet<Integer>>(step);
		// ///////////////////////////////////////////////////////////

		int j = 0;
		IOUtils.log("In process");
		for (Map.Entry<Integer, double[]> entry : vecspace.entrySet()) {
		    j++;
		    if (j % 1000 == 0)
			IOUtils.log(Thread.currentThread().getName() + " : "
				+ j + " : " + entry.getKey() + " : "
				+ Thread.currentThread().getId());
		    double[] cent = null;
		    double sim = 0;
		    for (double[] c : clusters.keySet()) {
			// IOUtils.log("before" + c);
			double csim = cosSim(entry.getValue(), c);
			if (csim > sim) {
			    sim = csim;
			    cent = c;
			}
		    }
		    if (cent != null && entry.getKey() != null) {
			try {
			    clusters.get(cent).add(entry.getKey());
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}

		// //////////////////////////////////////////////////////////
		// List<KmeanAssignmentThread> assThrdLst = new
		// ArrayList<KmeanAssignmentThread>();
		// // cluster assignment step
		// int noOfVecSpPrThread = vecspace.size() / NO_OF_THREADS;
		// for (int i = 0; i < NO_OF_THREADS; i++) {
		// LinkedHashMap<Integer, double[]> VecSpace = getNextMelements(
		// i * noOfVecSpPrThread, noOfVecSpPrThread, vecspace);
		// assThrdLst.add(new KmeanAssignmentThread(i, VecSpace,
		// clusters));
		// if (i == NO_OF_THREADS - 1) {
		// if ((i + 1) * noOfVecSpPrThread < vecspace.size()) {
		// LinkedHashMap<Integer, double[]> VecSpacen =
		// getNextMelements(
		// (i + 1) * noOfVecSpPrThread,
		// vecspace.size()
		// - ((i + 1) * noOfVecSpPrThread),
		// vecspace);
		// assThrdLst.add(new KmeanAssignmentThread(i + 1,
		// VecSpacen, clusters));
		// }
		// }
		// }
		//
		// for (KmeanAssignmentThread thread : assThrdLst) {
		// thread.start();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//
		// for (KmeanAssignmentThread thread : assThrdLst) {
		// try {
		// thread.join();
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

		// centroid update step
		step.clear();
		for (double[] cent : clusters.keySet()) {
		    IOUtils.log("Updating centroids");
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
		IOUtils.log("check break conditions");
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
	    IOUtils.log("calculate similarity sum and map it to the clustering");
	    double sumsim = 0;
	    for (double[] c : clusters.keySet()) {
		SortedSet<Integer> cl = clusters.get(c);
		for (int vi : cl) {
		    sumsim += cosSim(c, vecspace.get(vi));
		}
	    }
	    errorsums.put(sumsim, new HashMap<double[], SortedSet<Integer>>(
		    clusters));
	    IOUtils.log("printing cluster");
	    try {
		printClusters(clusters, init + 1);
	    } catch (Exception ex) {
		IOUtils.log(ex.getMessage());
		System.out.println(ex.getMessage());
	    }

	}
	// pick the clustering with the maximum similarity sum and print the
	// filenames and indices
	IOUtils.log("Best Convergence:");
	IOUtils.log(errorsums.get(errorsums.lastKey()).toString()
		.replaceAll("\\[[\\w@]+=", ""));
	HashMap<double[], SortedSet<Integer>> con = errorsums.get(errorsums
		.lastKey());
	return con;

    }

    private List<Integer> getKeySetAsList(
	    LinkedHashMap<Integer, double[]> vecspace) {
	// TODO Auto-generated method stub
	List<Integer> retList = new ArrayList<Integer>();
	for (Iterator<Integer> it = vecspace.keySet().iterator(); it.hasNext();) {
	    retList.add(it.next());
	}

	return retList;
    }

    private void printClusters(Map<double[], SortedSet<Integer>> clusters,
	    int iteration) {
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

    private void printCluster(List<ClusterModel> tdoList, int i, int iteration) {
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
	IOUtils.writeFile(currFile.getAbsolutePath() + "/cluster-" + i + "_"
		+ iteration + ".txt", sb.toString().trim());

    }

    static double cosSim(double[] a, double[] b) {
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
