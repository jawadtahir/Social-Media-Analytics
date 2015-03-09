package pk.lums.edu.sma.processing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.twitter.common.text.DefaultTextTokenizer;

/**
 * Annotated example illustrating major features of {@link DefaultTextTokenizer}
 * .
 */
public class NerUsageExampleWithSigRank extends Thread {

    private Thread t;
    private List<TweetDO> tweets;
    private Map<String, Double> entities;
    private Map<Date, Integer> dates;
    private Map<EntityPhraseDateModel, Double> entityDateModel;

    public NerUsageExampleWithSigRank(List<TweetDO> tweet, String tNumber,
	    Map<String, Double> entityMap, Map<Date, Integer> dateMap,
	    Map<EntityPhraseDateModel, Double> edMap) {
	// TODO Constructor
	this.tweets = tweet;
	this.entities = entityMap;
	this.dates = dateMap;
	this.entityDateModel = edMap;
	this.t = new Thread(this, tNumber);
    }

    public void run() {
	process();
    }

    public void process() {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	UwNamedEntityTagger tokenizer = new UwNamedEntityTagger.Builder()
		.setKeepPunctuation(true).build();
	NerTokenStream stream = (NerTokenStream) tokenizer
		.getDefaultTokenStream();

	// We're now going to iterate through a few tweets and tokenize each in
	// turn.
	int tokenCnt = 0;
	for (TweetDO tweet : tweets) {
	    // We're first going to demonstrate the "token-by-token" method of
	    // consuming tweets.
	    IOUtils.log("Thread " + this.getName() + " : Count : " + tokenCnt
		    + " Tweet : " + tweet);
	    List<String> tempEntityList = new ArrayList<String>();
	    List<Date> tempDateList = new ArrayList<Date>();
	    List<String> tempEventList = new ArrayList<String>();
	    try {
		// Reset the token stream to process new input.
		stream.reset(tweet.getTextTweet());
		tokenCnt++;
		// Now we're going to consume tokens from the stream.
		while (stream.incrementToken()) {
		    NamedEntityTypeAttribute nerAttribute = stream
			    .getAttribute(NamedEntityTypeAttribute.class);
		    POSAttribute posAttribute = stream
			    .getAttribute(POSAttribute.class);
		    EventTypeAttribute eventAttribute = stream
			    .getAttribute(EventTypeAttribute.class);
		    String token = removeHash(nerAttribute.getToken())
			    .toLowerCase().trim();
		    String ner = nerAttribute.getType().toString();
		    String pos = posAttribute.getType().toString();
		    String event = eventAttribute.getType().toString();
		    if (token.length() > 2) {
			if (pos.equals("HT")) {
			    if (!tempEntityList.contains(token)) {
				tempEntityList.add(token);
			    }
			}
			if (!ner.equals("O")) {
			    if (!tempEntityList.contains(token)) {
				tempEntityList.add(token);
			    }
			}
			if (!event.equals("O")) {
			    if (!tempEventList.contains(token)) {
				tempEventList.add(token);
			    }
			}
		    }

		}
	    } catch (Exception e) {
		e.printStackTrace();
		IOUtils.log("ERROR: " + e.getMessage().toString());
		IOUtils.log("\t TWEET: " + tweet);
	    }
	    Parser parser = new Parser();
	    List<DateGroup> dg = parser.parse(tweet.getTextTweet());
	    for (DateGroup d : dg) {
		tempDateList = d.getDates();
	    }
	    for (Date date : tempDateList) {
		try {
		    date = df.parse(date.toString());
		} catch (ParseException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	    insertInEntities(tempEntityList);
	    insertInDates(tempDateList);

	    for (String ent : tempEntityList) {
		for (Date dte : tempDateList) {
		    EntityPhraseDateModel edm = new EntityPhraseDateModel(ent,
			    tempEventList, dte);
		    if (entityDateModel.containsKey(edm)) {
			entityDateModel.put(edm, entityDateModel.get(edm) + 1);
		    } else {
			entityDateModel.put(edm, (double) 1);
		    }
		}
	    }

	    // We're now going to demonstrate the TokenizedCharSequence API.
	    // This should produce exactly the same result as above.
	    // tokenCnt = 0;
	    // System.out.println("Processing: " + tweet);
	    // List<NamedEntityTypeAttribute> tokSeq =
	    // tokenizer.tokenize(tweet);
	    // for (NamedEntityTypeAttribute tok : tokSeq) {
	    // System.out.println(String.format("%s\t%s", tok.getToken(),
	    // tok.getType()));
	    // }
	    // System.out.println("");

	}
	IOUtils.log("Thread " + this.getName() + " is completed");
    }

    private void insertInEntityMap(String token) {
	// TODO Auto-generated method stub
	if (entities.containsKey(token)) {
	    entities.put(token, (entities.get(token) + 1));
	} else {
	    entities.put(token, (double) 1);
	}
    }

    /**
     * Remove hash sign '#' before a string
     * 
     * @param hashTag
     *            string hashtag
     * @return hashtag minus hash sign
     */
    public static String removeHash(String hashTag) {
	String ht;
	ht = hashTag.replace("#", "");
	return ht;
    }

    private void insertInDates(List<Date> tempDateList) {
	// TODO Auto-generated method stub
	for (Date dte : tempDateList) {
	    if (dates.containsKey(dte)) {
		dates.put(dte, dates.get(dte) + 1);
	    } else {
		dates.put(dte, 1);
	    }
	}
    }

    private void insertInEntities(List<String> tempEntList) {
	// TODO Auto-generated method stub
	for (String ent : tempEntList) {
	    if (entities.containsKey(ent)) {
		entities.put(ent, entities.get(ent) + 1);
	    } else {
		entities.put(ent, (double) 1);
	    }
	}

    }

}
