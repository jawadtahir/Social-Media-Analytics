package pk.lums.edu.sma.test;

import pk.lums.edu.sma.utils.IOUtils;
import twitter4j.JSONObject;
import twitter4j.JSONTokener;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

public class JsonTest {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	String[] jsonTweets = IOUtils
		.readFile("/Users/jawadtahir/Desktop/jsonTweetsCopy.txt");
	for (String json : jsonTweets) {
	    json = json.replace("", "");
	    Status status;

	    try {
		JSONObject obj = new JSONObject(new JSONTokener(json));
		status = TwitterObjectFactory.createStatus(json);
		System.out.println(status.getText());
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

    }

}
