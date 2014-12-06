package pk.lums.edu.sma.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pk.lums.edu.sma.utils.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class GetEntities extends Thread {

    private Thread t;
    private String[] tweets;
    private Map<String, Integer> entities;

    public GetEntities(String[] tweet, String tNumber,
	    Map<String, Integer> entityMap) {
	this.tweets = tweet;
	this.entities = entityMap;
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
	// Read properties
	try {
	    inStream = new FileInputStream(new File("stanford_nlp.properties"));
	    props.load(inStream);
	} catch (Exception ex) {
	    IOUtils.log("Properties not found");
	}
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	for (String tweet : tweets) {
	    if (tweet == null) {
		continue;
	    }
	    if (pipeline == null) {
		continue;
	    }
	    count++;
	    IOUtils.log("Thread " + this.getName() + " : Count : " + count
		    + " Tweet : " + tweet);
	    // run annotation on all documents
	    Annotation document = new Annotation(tweet);
	    pipeline.annotate(document);

	    // Get all sentences
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    for (CoreMap sentence : sentences) {
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
		    String word = token.get(TextAnnotation.class);
		    String pos = token.get(PartOfSpeechAnnotation.class);
		    String ne = token.get(NamedEntityTagAnnotation.class);
		    if (pos.equals("HT")) {
			String ht = removeHash(word.toLowerCase().trim());
			try {
			    if (entities.containsKey(ht)) {
				entities.put(ht, entities.get(ht) + 1);
			    } else {
				entities.put(ht, 1);
			    }
			} catch (Exception ex) {
			    IOUtils.log(ex.getMessage());
			    continue;
			}
		    }
		    if (!ne.equals("O")) {
			if (isEntity(ne)) {
			    try {
				if (entities.containsKey(word.toLowerCase())) {
				    entities.put(
					    word.toLowerCase().trim(),
					    entities.get(word.toLowerCase()) + 1);
				} else {
				    entities.put(word.toLowerCase(), 1);
				}
			    } catch (Exception ex) {
				IOUtils.log(ex.getMessage());
			    }

			}
		    }
		}
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
