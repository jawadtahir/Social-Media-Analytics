package pk.lums.edu.sma.processing.ml.DBSCAN;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class DBSCANTest {
    private static List<String> topEntList = new ArrayList<String>();

    public static void main(String[] args) {

	// TODO Auto-generated method stub

	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	String entss = IOUtils.readFile("Entities2.txt")[0];
	entss = entss.substring(1, entss.length() - 1);
	for (String temp : entss.split(",")) {
	    if (temp.length() > 2) {
		attributes.add(new Attribute(temp.trim()));
		topEntList.add(temp);
	    }
	}
	Attribute idAttr = new Attribute("idTWEETDTA");
	attributes.add(idAttr);
	int idAttrIndex = attributes.size();
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
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Creating vector space of all tweets...");
	Instances instances = new Instances("Weka test", attributes,
		tweets.size());

	int count = 0;
	for (Iterator<TweetDO> it = tweets.iterator(); it.hasNext();) {
	    count++;
	    if (count % 1000 == 0)
		IOUtils.log(Integer.toString(count));
	    TweetDO tdo = it.next();
	    double[] temp = getVecSpace(tdo);
	    if (temp != null) {
		instances.add(new DenseInstance(1.0, temp));
	    } else {
		it.remove();
	    }
	}

	Remove remove = new Remove();
	Instances filteredInstance = null;
	try {
	    remove.setInputFormat(instances);
	    remove.setOptions(new String[] { "-R",
		    new Integer(topEntList.size() + 1).toString() });
	    filteredInstance = Filter.useFilter(instances, remove);
	} catch (Exception e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Going for main course...");
	ArffSaver saver = new ArffSaver();
	try {
	    saver.setFile(new File("testWeka.arff"));
	    saver.setInstances(filteredInstance);
	    saver.writeBatch();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	Map<Integer, ArrayList<Integer>> clusterMap = new LinkedHashMap<Integer, ArrayList<Integer>>();

	DBSCAN clusterer = new DBSCAN();
	clusterer.setEpsilon(2.5);
	clusterer.setMinPoints(10);
	try {
	    clusterer.buildClusterer(filteredInstance);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.out.println("Error cluster");
	}
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Creating Cluster map");
	for (Instance instance : filteredInstance) {
	    try {
		int clustNum = clusterer.clusterInstance(instance);
		int tweetID = (int) ((instances.get(filteredInstance
			.indexOf(instance))).value(idAttr));
		insertClusterMap(clusterMap, clustNum, tweetID);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Printing Clusters");
	System.out.println(clusterMap);
	printClusters(clusterMap, 6);
    }

    private static void insertClusterMap(
	    Map<Integer, ArrayList<Integer>> clustMap, Integer key,
	    Integer Value) {
	if (clustMap.containsKey(key)) {
	    clustMap.get(key).add(Value);
	} else {
	    ArrayList<Integer> tempList = new ArrayList<Integer>();
	    tempList.add(Value);
	    clustMap.put(key, tempList);
	}
    }

    private static double[] getVecSpace(TweetDO tweet) {
	double[] vecSpace = new double[topEntList.size() + 1];
	int i = 0;
	int nonZeroEnt = 0;
	for (String ent : topEntList) {
	    String[] ents = ent.split("><");
	    for (String entss : ents) {
		vecSpace[i] += countSubstring(tweet.getTextTweet()
			.toLowerCase(), entss);
	    }
	    if (vecSpace[i] > 0f) {
		nonZeroEnt++;
	    }
	    i++;
	}
	if (nonZeroEnt == 0) {
	    return null;
	}
	vecSpace[topEntList.size()] = (double) tweet.getId();
	return vecSpace;
    }

    private static void printClusters(
	    Map<Integer, ArrayList<Integer>> clusters, int iteration) {
	// TODO Auto-generated method stub
	IOUtils.clearClusterFolder();
	Iterator<Integer> itr = clusters.keySet().iterator();
	Connection con = null;
	PreparedStatement pst = null;
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.SELECT_ALL_FROM_ID_US);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	int i = 0;
	while (itr.hasNext()) {
	    Integer key = itr.next();
	    ArrayList<Integer> tweets = clusters.get(key);
	    Iterator<Integer> setItr = tweets.iterator();
	    List<TweetDO> tdoList = new ArrayList<TweetDO>();
	    i++;
	    while (setItr.hasNext()) {
		int id = setItr.next();
		try {
		    pst.setInt(1, id);
		    List<TweetDO> tempList = TweetDO.translateAllTweetDO(pst
			    .executeQuery());
		    tdoList.add(tempList.get(0));
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	    printCluster(tdoList, i, iteration);
	}

    }

    private static void printCluster(List<TweetDO> tdoList, int i, int iteration) {
	// TODO Auto-generated method stub
	File file = new File("clusters" + iteration);
	if (!file.exists()) {
	    file.mkdir();
	    IOUtils.clearClusterFolder("clusters" + iteration);
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

    public static int countSubstring(String str, String subStr) {

	// the result of split() will contain one more element than the
	// delimiter
	// the "-1" second argument makes it not discard trailing empty strings
	return str.split(Pattern.quote(subStr), -1).length - 1;
    }
}
