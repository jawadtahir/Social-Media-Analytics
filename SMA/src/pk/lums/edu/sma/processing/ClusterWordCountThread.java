package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class ClusterWordCountThread extends Thread {
    private Thread t;
    private File fileName;
    private Map<String, Double> entMap;

    public ClusterWordCountThread(int threadCount, File fileName,
	    Map<String, Double> map) {
	this.fileName = fileName;
	this.entMap = map;
	this.t = new Thread(this, Integer.toString(threadCount));
    }

    public void run() {
	process();
    }

    private void process() {
	// TODO Auto-generated method stub
	Map<String, Double> wordCount = null;
	wordCount = new HashMap<String, Double>();
	String[] tweetEnts = IOUtils.readFile(this.fileName.getAbsolutePath());
	for (String tweetEnt : tweetEnts) {
	    TweetDO tdo = stringToDO(tweetEnt);
	    if (tdo != null) {
		String[] subStrs = tdo.getTextTweet().split(" ");
		for (String string : subStrs) {
		    string = string.toLowerCase().trim();
		    if (entMap.containsKey(string.toLowerCase().trim())) {
			if (wordCount.containsKey(string)) {
			    wordCount.put(string, wordCount.get(string) + 1);
			} else {
			    wordCount.put(string, (double) 1);
			}
		    }
		}
	    }
	}
	wordCount = IOUtils.sortByValues(wordCount);
	// IOUtils.writeFile(fileName.getName(), wordCount.toString(), true);
	wordCount = getTopNEntities(wordCount, 100);

	IOUtils.log(wordCount.toString());
	ChartCreator cc = new ChartCreator(wordCount, fileName.getName()
		.substring(0, fileName.getName().length() - 4));
	cc.create();

    }

    private TweetDO stringToDO(String text) {
	TweetDO tdo = new TweetDO();

	StringBuilder sb = new StringBuilder(text);
	sb.append("  ");
	String[] tweetEntArr = sb.toString().split(" , ");
	if (tweetEntArr.length >= 4) {
	    sb = new StringBuilder();
	    for (int i = 0; i <= tweetEntArr.length - 4; i++) {
		sb.append(tweetEntArr[i]);
	    }
	    tdo.setTextTweet(sb.toString());
	} else {
	    return null;
	}
	return tdo;
    }

    public static Map<String, Double> getTopNEntities(Map<String, Double> map,
	    int n) {
	Iterator<Entry<String, Double>> itr = map.entrySet().iterator();
	int i = 0;
	Map<String, Double> entites = new LinkedHashMap<String, Double>();
	while (itr.hasNext()) {
	    if (i == n) {
		break;
	    }
	    Entry<String, Double> ent = itr.next();
	    entites.put(ent.getKey(), ent.getValue());
	    i++;
	}

	return entites;
    }

}
