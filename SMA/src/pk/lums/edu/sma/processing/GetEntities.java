package pk.lums.edu.sma.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
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

    public GetEntities(String[] tweet, String tNumber) {
	tweets = tweet;
	t = new Thread(this, tNumber);
	t.start();
    }

    public void run() {
	process();
    }

    private void process() {
	// Make properties object to hold properties for Stanford NLP
	Properties props = new Properties();
	// props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	InputStream inStream = null;
	HashMap<String, Integer> entities = new HashMap<String, Integer>();
	int count = 0;
	// Read properties
	try {
	    inStream = new FileInputStream(new File("stanford_nlp.properties"));
	    props.load(inStream);
	} catch (Exception ex) {
	    IOUtils.log("Properties not found");
	}
	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	// run annotation on all documents
	for (String tweet : tweets) {
	    if (tweet == null) {
		continue;
	    }
	    Annotation document = new Annotation(tweet);
	    if (pipeline == null) {
		continue;
	    }
	    pipeline.annotate(document);

	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    for (CoreMap sentence : sentences) {
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
		    String word = token.get(TextAnnotation.class);
		    String pos = token.get(PartOfSpeechAnnotation.class);
		    String ner = token.get(NamedEntityTagAnnotation.class);
		}
	    }
	}
    }

    /**
     * Remove hash '#' before a string
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
