package pk.lums.edu.sma.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

import com.twitter.common.text.DefaultTextTokenizer;

import edu.stanford.nlp.ling.CoreAnnotations.DocDateAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

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
	Properties props = new Properties();
	InputStream inStream = null;
	int count = 0;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	UwNamedEntityTagger tokenizer = new UwNamedEntityTagger.Builder()
		.setKeepPunctuation(true).build();
	NerTokenStream stream = (NerTokenStream) tokenizer
		.getDefaultTokenStream();
	try {
	    inStream = new FileInputStream(new File("stanford_nlp.properties"));
	    props.load(inStream);
	} catch (Exception ex) {
	    IOUtils.log("Properties not found");
	}
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	// We're now going to iterate through a few tweets and tokenize each in
	// turn.
	int tokenCnt = 0;
	for (TweetDO tweet : tweets) {

	    // We're first going to demonstrate the "token-by-token" method of
	    // consuming tweets.

	    IOUtils.log("Thread " + this.getName() + " : Count : " + tokenCnt
		    + " Tweet : " + tweet.getTextTweet());
	    List<String> tempEntityList = new ArrayList<String>();
	    List<Date> tempDateList = new ArrayList<Date>();
	    List<String> tempEventList = new ArrayList<String>();
	    try {
		Status status = TwitterObjectFactory.createStatus(tweet
			.getJsonTweet());
		Date creationDate = status.getCreatedAt();
		HashtagEntity htEnts[] = status.getHashtagEntities();
		for (HashtagEntity htEnt : htEnts) {
		    String ht = removeHash(htEnt.getText().toLowerCase().trim());
		    if (!tempEntityList.contains(ht)) {
			tempEntityList.add(ht);
		    }
		}

		Annotation document = new Annotation(tweet.getTextTweet());
		document.set(DocDateAnnotation.class, df.format(creationDate));
		pipeline.annotate(document);

		// Get all sentences
		List<CoreMap> sentences = document
			.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
		    List<CoreMap> timeAnns = sentence
			    .get(TimexAnnotations.class);
		    if (timeAnns != null) {
			for (CoreMap timeAnn : timeAnns) {
			    if (timeAnn != null) {
				TimeExpression te = timeAnn
					.get(TimeExpression.Annotation.class);
				if (te != null) {
				    try {
					Date tempDate = df.parse(te
						.getTemporal().toString());
					if (!tempDateList.contains(tempDate)) {
					    tempDateList.add(tempDate);
					}
				    } catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				    }
				}
			    }
			}
		    }
		}

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
		    IOUtils.log("\t TWEET: " + tweet.getTextTweet());
		}

		insertInEntities(tempEntityList);
		insertInDates(tempDateList);

		for (String ent : tempEntityList) {
		    for (Date dte : tempDateList) {
			Map<String, Double> mapEvent = new LinkedHashMap<String, Double>();

			EntityPhraseDateModel edm = new EntityPhraseDateModel(
				ent, mapEvent, dte);
			for (EntityPhraseDateModel epd : entityDateModel
				.keySet()) {
			    if (epd.equals(edm)) {
				mapEvent = epd.getPhrases();
				break;
			    }
			}
			for (String ep : tempEventList) {
			    if (mapEvent.containsKey(ep)) {
				mapEvent.put(ep, mapEvent.get(ep) + 1);
			    } else {
				mapEvent.put(ep, (double) 1);
			    }
			}
			edm.setPhrases(mapEvent);
			if (entityDateModel.containsKey(edm)) {
			    entityDateModel.put(edm,
				    entityDateModel.get(edm) + 1);
			} else {
			    entityDateModel.put(edm, (double) 1);
			}
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
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
