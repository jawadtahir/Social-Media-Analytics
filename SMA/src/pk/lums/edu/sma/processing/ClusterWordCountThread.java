package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class ClusterWordCountThread extends Thread {
    private Thread t;
    private File fileName;
    private Map<String, Integer> entMap;

    public ClusterWordCountThread(int threadCount, File fileName,
	    Map<String, Integer> map) {
	this.fileName = fileName;
	this.entMap = map;
	this.t = new Thread(this, Integer.toString(threadCount));
    }

    public void run() {
	process();
    }

    private void process() {
	// TODO Auto-generated method stub
	Map<String, Integer> wordCount = null;
	wordCount = new HashMap<String, Integer>();
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
			    wordCount.put(string, 1);
			}
		    }
		}
	    }
	}
	wordCount = IOUtils.sortByValues(wordCount);
	// IOUtils.writeFile(fileName.getName(), wordCount.toString(), true);

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

}
