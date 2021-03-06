package pk.lums.edu.sma.test;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class WekaTest {

    private static List<String> topEntList = new ArrayList<String>();

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	String entss = IOUtils.readFile("Entities.txt")[0];
	entss = entss.substring(1, entss.length() - 1);
	for (String temp : entss.split(",")) {
	    if (temp.length() > 2) {
		attributes.add(new Attribute(temp));
		topEntList.add(temp);
	    }
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
	    double[] temp = getVecSpace(tdo.getTextTweet());
	    if (temp != null) {
		instances.add((int) tdo.getId(), new DenseInstance(1.0, temp));
	    } else {
		it.remove();
	    }
	}

	IOUtils.log(Calendar.getInstance().getTime().toString());
	IOUtils.log("Going for main course...");
	ArffSaver saver = new ArffSaver();
	try {
	    saver.setFile(new File("testWeka.arff"));
	    saver.setInstances(instances);
	    saver.writeBatch();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static double[] getVecSpace(String tweet) {
	double[] vecSpace = new double[topEntList.size()];
	int i = 0;
	int nonZeroEnt = 0;
	for (String ent : topEntList) {
	    String[] ents = ent.split("><");
	    for (String entss : ents) {
		vecSpace[i] += countSubstring(tweet.toLowerCase(), entss);
	    }
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

}
