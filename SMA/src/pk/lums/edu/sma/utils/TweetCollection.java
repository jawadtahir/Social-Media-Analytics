package pk.lums.edu.sma.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import pk.lums.edu.sma.dos.TweetDO;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TweetCollection {
    static String apiKey;
    static String apiSecret;
    static Integer count = 0;
    static Connection con = null;
    static PreparedStatement pst = null;
    static final String newLine = System.getProperty("line.separator");
    static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    static StringBuilder tweets = new StringBuilder();
    static StringBuilder fullTweets = new StringBuilder();
    static StatusListener listener = new StatusListener() {

	@Override
	public void onException(Exception arg0) {
	    // TODO Auto-generated method stub
	    IOUtils.log("onException");
	    IOUtils.log(arg0.getLocalizedMessage());

	}

	@Override
	public void onTrackLimitationNotice(int arg0) {
	    // TODO Auto-generated method stub
	    IOUtils.log("onTrackLimitationNotice");

	}

	@Override
	public void onStatus(Status arg0) {
	    // TODO Auto-generated method stub
	    processStatus(arg0);
	}

	private void processStatus(Status status) {
	    // TODO Auto-generated method stub
	    String jsonTweet = TwitterObjectFactory.getRawJSON(status);
	    ByteBuffer textByte = Charset.forName("UTF-8").encode(
		    status.getText());
	    String text = null;

	    try {
		text = new String(textByte.array(), "UTF-8");
	    } catch (UnsupportedEncodingException e1) {
		// TODO Auto-generated catch block
		IOUtils.log(e1.getMessage());
	    }

	    if (text != null) {
		if (!text.contains("???") && !text.contains("#xxx")
			&& status.getLang().equals("en")
			&& !status.toString().contains(newLine)) {

		    if (count < 100000) {
			// Creating a tweet data object
			TweetDO tdo = new TweetDO();
			tdo.setDateTextTweet(df.format(status.getCreatedAt()));
			tdo.setJsonTweet(jsonTweet);
			// Removing non-UTF8 characters
			tdo.setTextTweet(status.getText().replaceAll(
				"[^\\u0000-\\uFFFF]", ""));
			tdo.setTweetIDTweet(status.getId());
			if (status.getGeoLocation() != null) {
			    tdo.setLocTweet(status.getGeoLocation().toString());
			}
			try {
			    // Inserting in db
			    pst.setString(1, tdo.getJsonTweet());
			    pst.setString(2, tdo.getTextTweet());
			    pst.setString(3, tdo.getDateTextTweet());
			    pst.setString(4, tdo.getLocTweet());
			    pst.setLong(5, tdo.getTweetIDTweet());
			    pst.execute();
			    // logging
			    IOUtils.log(Integer.toString(count));
			    IOUtils.writeFile("log.txt", count.toString(),
				    false);
			    IOUtils.writeFile("cestweets.txt", text);
			    IOUtils.writeFile("cesjason.txt", jsonTweet);
			    count++;
			} catch (SQLException e) {
			    IOUtils.log(e.getMessage());
			}
		    } else {
			System.exit(0);
		    }
		}
	    }

	}

	@Override
	public void onStallWarning(StallWarning arg0) {
	    // TODO Auto-generated method stub
	    IOUtils.log("onStallWarning");

	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
	    // TODO Auto-generated method stub
	    IOUtils.log("onScrubGeo");

	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice arg0) {
	    // TODO Auto-generated method stub
	    IOUtils.log("onDeletionNotice");

	}
    };

    public TweetCollection() {
	Properties prop = new Properties();
	InputStream inStream = null;
	try {
	    inStream = new FileInputStream(new File("twitter4j.properties"));
	    prop.load(inStream);
	    apiKey = prop.getProperty("oauth.consumerKey");
	    apiSecret = prop.getProperty("oauth.consumerSecret");
	    Class.forName("com.mysql.jdbc.Driver");

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    IOUtils.log(e.getMessage());
	}

    }

    public static void main(String[] args) {
	// getting connection URLs
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.INSERT_QUERY_NEW);
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    IOUtils.log(e.getMessage());
	}
	// adding parameters in filter
	FilterQuery fq = createFilterQuery();
	TwitterStream str = new TwitterStreamFactory().getInstance();
	str.addListener(listener);
	str.filter(fq);
    }

    /**
     * This function add all the parameters to add in filter query and returns a
     * filter query
     * 
     * @return FilterQuery
     */
    private static FilterQuery createFilterQuery() {
	// TODO Auto-generated method stub
	FilterQuery fq = new FilterQuery();
	fq.track(createTrack());
	fq.count(createCount());
	fq.follow(createFollow());
	fq.locations(createLocation());
	return fq;
    }

    /**
     * Add LongLat coordinates in array and all public tweets in given rectangle
     * will be captured
     * 
     * @return 2D array of double
     */
    private static double[][] createLocation() {
	// TODO Auto-generated method stub

	// These coordinates are for Lahore (Lower Left, Upper Right )

	// double [][] locArr = {{74.223633d, 31.452234d},{74.437866d,
	// 31.613901d}};

	// Oklohama

	double[][] locArr = { { -97.834536d, 35.332382d },
		{ -97.161624d, 35.666670d } };

	// double[][] locArr = null;
	return locArr;
    }

    private static long[] createFollow() {
	// TODO Auto-generated method stub
	return null;
    }

    private static int createCount() {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * Add words in filter
     * 
     * @return String array of words to track
     */

    private static String[] createTrack() {
	// TODO Auto-generated method stub
	// String[] track = { "pak", "usa", "cricket", "football", "ebola",
	// "google", "comet", "europe", "hockey", "isis", "NBA", "NFL",
	// "stadium", "terrorist", "university", "mit", "stanford",
	// "concert", "protest", "tv show", "asia", "africa", "movie",
	// "album", "match", "microsoft", "sony", "xbox", "playstation",
	// "ps4", "ps3", "apple", "samsung", "disease", "bbc", "computer",
	// "laptop", "smartphone", "galaxy", "tv", "season", "cod",
	// "game", "pakvsnz" };
	// String[] track = { /* "snowmaggeddon2015", "blizzardof2015", */
	// "airport", "road", "rescue", "power", "traffic", "closure", "school",
	// "university" };
	String[] track = { "cwc", "cwc15", "cwc2015", "zimbabwe",
		"nasir jamshed", "swine flu", "pakvuae", "ahmad shehzad",
		"sohail khan", "maxwell", "glenn maxwell", "ausvafg", "misbah",
		"imran khan", "mwc15", "wahab riaz", "unicef", "polio" };
	return track;
    }

}
