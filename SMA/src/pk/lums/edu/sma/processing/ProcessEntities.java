package pk.lums.edu.sma.processing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

public class ProcessEntities {

    private String[] entities = null;
    private EntityCluster cluster = null;
    private Connection con = null;
    private PreparedStatement pst = null;
    private ResultSet res = null;
    private ArrayList<TweetDO> tweetArr = null;

    public ProcessEntities(String[] entities) {
	this.entities = entities;
    }

    void process() throws SQLException {
	// Getting DB connection
	con = IOUtils.getConnection();
	pst = con.prepareStatement(TweetDO.SELECT_TEXT_LIKE);
	cluster = new EntityCluster(entities);
	IOUtils.log("Processing entities....");
	// Processing entities....
	for (String entity : entities) {
	    IOUtils.log("Entity: " + entity);
	    pst.setString(1, "%" + entity + "%");
	    res = pst.executeQuery();
	    tweetArr = TweetDO.translateTextIdDateLocTweetDO(res);
	    for (TweetDO tdo : tweetArr) {
		String text = tdo.getTextTweet() + " , "
			+ tdo.getDateTextTweet() + " , " + tdo.getId() + " , "
			+ tdo.getLocTweet();
		cluster.putInCluster(entity, text);
	    }
	}
	cluster.writeCluster();
    }
}
