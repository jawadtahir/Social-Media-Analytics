package pk.lums.edu.sma.processing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class ClusterWordCountThread extends Thread {
    private Thread t;
    private File fileName;
    private Map<String, Integer> wordCount;

    public ClusterWordCountThread(int threadCount, File fileName,
	    Map<String, Integer> map) {
	this.fileName = fileName;
	this.t = new Thread(this, Integer.toString(threadCount));
    }

    public void run() {
	process();
    }

    private void process() {
	// TODO Auto-generated method stub
	wordCount = new HashMap<String, Integer>();
	String[] tweetEnts = IOUtils.readFile(this.fileName.getAbsolutePath());
	for (String tweetEnt : tweetEnts) {
	    TweetDO tdo = stringToDO(tweetEnt);
	    String[] subStrs = tdo.getTextTweet().split(" ");
	    for (String string : subStrs) {
		if (wordCount.containsKey(string)) {
		    wordCount.put(string, wordCount.get(string) + 1);
		} else {
		    wordCount.put(string, 1);
		}
	    }
	}
	IOUtils.writeFile(fileName.getName(), wordCount.toString(), true);

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
