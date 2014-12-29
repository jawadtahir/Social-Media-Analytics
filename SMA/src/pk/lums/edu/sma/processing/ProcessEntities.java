package pk.lums.edu.sma.processing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class ProcessEntities extends Thread {

    private String[] entities = null;
    private EntityCluster cluster = null;
    private Connection con = null;
    private PreparedStatement pst = null;
    private ResultSet res = null;
    private ArrayList<TweetDO> tweetArr = null;
    private Thread t = null;

    public ProcessEntities(String[] entities, int threadCount) {
	this.entities = entities;
	t = new Thread(this, Integer.toString(threadCount));
    }

    @Override
    public void run() {
	try {
	    process();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    void process() throws SQLException {
	// Getting DB connection
	con = IOUtils.getConnection();
	pst = con.prepareStatement(TweetDO.SELECT_TEXT_LIKE);
	// cluster = new EntityCluster(entities);
	IOUtils.log("Processing entities....");
	int count = 0;
	// Processing entities....
	for (String entity : entities) {
	    cluster = new EntityCluster(new String[] { entity });
	    count++;
	    if (!(entity.equals("") || entity == null || entity.length() == 1)) {
		IOUtils.log("Entity: " + entity + "\t\tCount: " + count
			+ "\t\tThread: " + this.getName());
		pst.setString(1, "%" + entity + "%");
		res = pst.executeQuery();
		tweetArr = TweetDO.translateTextIdDateLocTweetDO(res);
		for (TweetDO tdo : tweetArr) {
		    String text = tdo.getTextTweet().trim() + " , "
			    + tdo.getDateTextTweet().trim() + " , "
			    + tdo.getId() + " , " + tdo.getLocTweet().trim();
		    cluster.putInCluster(entity.trim(), text);
		}
	    }
	    cluster.writeCluster();
	}
    }
}
