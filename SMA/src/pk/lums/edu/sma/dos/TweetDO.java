package pk.lums.edu.sma.dos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import pk.lums.edu.sma.utils.IOUtils;

public class TweetDO {

    public static final String INSERT_QUERY = "INSERT INTO TWEETDATA.TWEETDTA (jsonTweet, textTweet, dateTextTweet, locationTweet, tweetIDTweet) VALUES (?, ?, ?, ?, ?)";
    public static final String SELECT_ALL_TEXT_QUERY = "SELECT textTweet FROM TWEETDATA.TWEETDTA limit 1000";
    public static final String SELECT_ALL_ID_TEXT_QUERY = "SELECT idTWEETDTA, textTweet FROM TWEETDATA.TWEETDTA limit 1000";
    public static final String SELECT_TEXT_LIKE = "SELECT idTWEETDTA, textTweet, dateTextTweet, locationTweet FROM TWEETDATA.TWEETDTA where textTweet like ? limit 1000";
    public static final String SELECT_ALL_FROM_ID = "select * from TWEETDATA.TWEETDTA where idTWEETDTA = ? and limit 1000";
    public static final String SELECT_ALL_QUERY = "SELECT * FROM TWEETDATA.TWEETDTA limit 1000";

    public static final String INSERT_QUERY_US = "INSERT INTO TWEETDATA.TWEETDTAUS (jsonTweet, textTweet, dateTextTweet, locationTweet, tweetIDTweet) VALUES (?, ?, ?, ?, ?)";
    public static final String SELECT_ALL_TEXT_QUERY_US = "SELECT textTweet FROM TWEETDATA.TWEETDTAUS limit 200000";
    public static final String SELECT_ALL_ID_TEXT_QUERY_US = "SELECT idTWEETDTA, textTweet FROM TWEETDATA.TWEETDTAUS limit 200000";
    public static final String SELECT_TEXT_LIKE_US = "SELECT idTWEETDTA, textTweet, dateTextTweet, locationTweet FROM TWEETDATA.TWEETDTAUS where textTweet like ? limit 200000";
    public static final String SELECT_ALL_FROM_ID_US = "select * from TWEETDATA.TWEETDTAUS where idTWEETDTA = ? limit 200000";
    public static final String SELECT_ALL_QUERY_US = "SELECT * FROM TWEETDATA.TWEETDTAUS limit 200000";

    public static final String INSERT_QUERY_NEW = "INSERT INTO TWEETDATA.TWEETDTANEW (jsonTweet, textTweet, dateTextTweet, locationTweet, tweetIDTweet) VALUES (?, ?, ?, ?, ?)";
    public static final String SELECT_ALL_TEXT_QUERY_NEW = "SELECT textTweet FROM TWEETDATA.TWEETDTANEW";
    public static final String SELECT_ALL_ID_TEXT_QUERY_NEW = "SELECT idTWEETDTA, textTweet FROM TWEETDATA.TWEETDTANEW";
    public static final String SELECT_TEXT_LIKE_NEW = "SELECT idTWEETDTA, textTweet, dateTextTweet, locationTweet FROM TWEETDATA.TWEETDTANEW where textTweet like ?";
    public static final String SELECT_ALL_FROM_ID_NEW = "select * from TWEETDATA.TWEETDTANEW where idTWEETDTA = ?";
    public static final String SELECT_ALL_QUERYNEW = "SELECT * FROM TWEETDATA.TWEETDTANEW";

    private long id = 0;
    private String jsonTweet = "";
    private String textTweet = "";
    private String dateTextTweet = "";
    private String locTweet = "";
    private long tweetIDTweet = 0;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public String getJsonTweet() {
	return jsonTweet;
    }

    public void setJsonTweet(String jsonTweet) {
	this.jsonTweet = jsonTweet;
    }

    public String getTextTweet() {
	return textTweet;
    }

    public void setTextTweet(String textTweet) {
	this.textTweet = textTweet;
    }

    public String getDateTextTweet() {
	return dateTextTweet;
    }

    public void setDateTextTweet(String dateTextTweet) {
	this.dateTextTweet = dateTextTweet;
    }

    public String getLocTweet() {
	return locTweet;
    }

    public void setLocTweet(String locTweet) {
	this.locTweet = locTweet;
    }

    public long getTweetIDTweet() {
	return tweetIDTweet;
    }

    public void setTweetIDTweet(long tweetIDTweet) {
	this.tweetIDTweet = tweetIDTweet;
    }

    public static ArrayList<TweetDO> translateAllTweetDO(ResultSet res) {
	ArrayList<TweetDO> tweetArr = new ArrayList<TweetDO>();
	if (res != null) {
	    try {
		while (res.next()) {
		    TweetDO tdo = new TweetDO();
		    tdo.setId(res.getLong("idTWEETDTA"));
		    tdo.setJsonTweet(res.getString("jsonTweet"));
		    tdo.setTextTweet(res.getString("textTweet"));
		    tdo.setDateTextTweet(res.getString("dateTextTweet"));
		    tdo.setLocTweet(res.getString("locationTweet"));
		    tdo.setTweetIDTweet(res.getLong("tweetIDTweet"));
		    tweetArr.add(tdo);
		}
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return tweetArr;
    }

    public static ArrayList<TweetDO> translateTextIdDateLocTweetDO(ResultSet res) {
	ArrayList<TweetDO> tweetArr = new ArrayList<TweetDO>();
	if (res != null) {
	    try {
		while (res.next()) {
		    TweetDO tdo = new TweetDO();
		    tdo.setId(res.getLong("idTWEETDTA"));
		    tdo.setTextTweet(res.getString("textTweet"));
		    tdo.setDateTextTweet(res.getString("dateTextTweet"));
		    tdo.setLocTweet(res.getString("locationTweet"));
		    tweetArr.add(tdo);
		}
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return tweetArr;
    }

    public static ArrayList<String> getTextArrayOfColumn(ResultSet res,
	    String colomnName) {
	ArrayList<String> textList = new ArrayList<String>();
	try {
	    while (res.next()) {
		String text = res.getString(colomnName).replaceAll(
			"[^\\u0000-\\uFFFF]", "");
		textList.add(text);
	    }
	} catch (SQLException e) {
	    IOUtils.log(e.getMessage());
	}
	return textList;
    }

    public static ArrayList<TweetDO> translateTextIdTweetDO(ResultSet res) {
	ArrayList<TweetDO> tweetArr = new ArrayList<TweetDO>();
	if (res != null) {
	    try {
		while (res.next()) {
		    TweetDO tdo = new TweetDO();
		    tdo.setId(res.getLong("idTWEETDTA"));
		    tdo.setTextTweet(res.getString("textTweet"));
		    tweetArr.add(tdo);
		}
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return tweetArr;
    }

}
