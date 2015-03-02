package pk.lums.edu.sma.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import edu.stanford.nlp.ling.CoreAnnotations.DocDateAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;

public class GetEntitiesWithSigRank extends Thread {

    private Thread t;
    private List<TweetDO> tweets;
    private Map<String, Integer> entities;
    private Map<Date, Integer> dates;
    private Map<EntityDateModel, Double> entityDateModel;

    public GetEntitiesWithSigRank(List<TweetDO> tweet, String tNumber,
	    Map<String, Integer> entityMap, Map<Date, Integer> dateMap,
	    Map<EntityDateModel, Double> edMap) {
	this.tweets = tweet;
	this.entities = entityMap;
	this.dates = dateMap;
	this.entityDateModel = edMap;
	this.t = new Thread(this, tNumber);
    }

    public void run() {
	process();
    }

    private void process() {
	// Make properties object to hold properties for Stanford NLP
	Properties props = new Properties();
	InputStream inStream = null;
	int count = 0;
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	// Read properties
	try {
	    inStream = new FileInputStream(new File("stanford_nlp.properties"));
	    props.load(inStream);
	} catch (Exception ex) {
	    IOUtils.log("Properties not found");
	}
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	pipeline.addAnnotator(new TimeAnnotator("sutime", props));
	for (TweetDO tweet : tweets) {

	    try {
		Status status = TwitterObjectFactory.createStatus(tweet
			.getJsonTweet());
		if (pipeline == null) {
		    continue;
		}
		List<String> tempEntList = new ArrayList<String>();
		List<Date> tempDateList = new ArrayList<Date>();
		Date creationDate = status.getCreatedAt();
		count++;
		IOUtils.log("Thread " + this.getName() + " : Count : " + count
			+ " Tweet : " + tweet.getTextTweet());
		// run annotation on all documents
		Annotation document = new Annotation(tweet.getTextTweet());
		document.set(DocDateAnnotation.class, df.format(creationDate));
		pipeline.annotate(document);

		// Get all sentences
		List<CoreMap> sentences = document
			.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {

		    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			String ne = token.get(NamedEntityTagAnnotation.class);
			word = word.toLowerCase().trim();
			if (pos.equals("HT")) {
			    String ht = removeHash(word);
			    if (ht.length() > 1) {
				try {
				    if (!tempEntList.contains(ht)) {
					tempEntList.add(ht);
				    }
				} catch (Exception ex) {
				    IOUtils.log(ex.getMessage());
				    continue;
				}
			    }

			}
			if (!ne.equals("O")) {
			    if (isEntity(ne) && word.length() > 1) {
				try {
				    if (!tempEntList.contains(word)) {
					tempEntList.add(word);
				    }
				} catch (Exception ex) {
				    IOUtils.log(ex.getMessage());
				}

			    }
			}
		    }

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
		insertInEntities(tempEntList);
		insertInDates(tempDateList);

		for (String ent : tempEntList) {
		    for (Date dte : tempDateList) {
			EntityDateModel edm = new EntityDateModel(ent, dte);
			if (entityDateModel.containsKey(edm)) {
			    entityDateModel.put(edm,
				    entityDateModel.get(edm) + 1);
			} else {
			    entityDateModel.put(edm, (double) 1);
			}
		    }
		}

	    } catch (TwitterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
	// HashMap<String, Integer> sortedEntities = (HashMap<String, Integer>)
	// pk.lums.edu.sma.utils.IOUtils
	// .sortByValues(entities);
	// // System.out.println (sortedEntities.toString ());
	// IOUtils.log(sortedEntities.toString());
	//
	// String[] topEntities = pk.lums.edu.sma.utils.IOUtils.getTopNEntities(
	// sortedEntities, (int) sortedEntities.size() / 2);
	// EntityCluster cluster = new EntityCluster(topEntities);
	// for (String tweet : tweets) {
	// for (String entity : topEntities) {
	// if (tweet.toLowerCase().contains(entity)) {
	// try {
	// cluster.putInCluster(entity, tweet);
	// } catch (Exception ex) {
	// IOUtils.log(ex.getMessage());
	// }
	// }
	// }
	// }
	// cluster.writeCluster();
	IOUtils.log("Thread " + this.getName() + " is completed");
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
		entities.put(ent, 1);
	    }
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

    /**
     * Identifies either the string is valid entitiy or not...
     * 
     * @param entity
     *            String entity
     * @return true or false
     */

    public static boolean isEntity(String entity) {
	boolean isEntity = true;
	for (UnrelatedEntities ent : UnrelatedEntities.values()) {
	    if (ent.name().equals(entity)) {
		return false;
	    }
	}
	return isEntity;
    }
}
