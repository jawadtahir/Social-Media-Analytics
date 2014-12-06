package pk.lums.edu.sma.processing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import pk.lums.edu.sma.dos.TweetDO;
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

public class StanfordNER {
    static String url = "jdbc:mysql://localhost:3306/TWEETDATA";
    static String user = "root";
    static String password = "abc";
    static Connection con = null;
    static PreparedStatement pstTweetAll = null;
    static ResultSet res = null;

    public StanfordNER() {
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (Exception e) {
	    IOUtils.log(e.getMessage());
	}
    }

    public static void main(String[] args) {
	IOUtils.log("*********************************************************************************");
	IOUtils.log("************************************Program Start********************************");
	IOUtils.log("*********************************************************************************");

	try {
	    con = DriverManager.getConnection(url, user, password);
	} catch (Exception e) {
	    IOUtils.log(e.getMessage());
	}
	try {
	    pstTweetAll = con.prepareStatement(TweetDO.SELECT_ALL_TEXT_QUERY);
	    res = pstTweetAll.executeQuery();

	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    IOUtils.log(e.getMessage());
	}

	IOUtils.log(Calendar.getInstance().getTime().toString());
	// TODO Auto-generated method stub
	// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
	// NER, parsing, and coreference resolution
	Properties props = new Properties();
	// props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	InputStream inStream = null;
	HashMap<String, Integer> entities = new HashMap<String, Integer>();
	int count = 0;

	try {
	    inStream = new FileInputStream(new File("stanford_nlp.properties"));
	    props.load(inStream);
	} catch (Exception ex) {
	    IOUtils.log("Properties not found");
	}

	StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	String[] tweets = pk.lums.edu.sma.utils.IOUtils.readFile("tweets.txt");
	// String[] tweets = TweetDO.getTextArrayOfColumn(res, "textTweet");

	// Add your text here!
	// props.put("annotators",
	// "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

	IOUtils.log("*********************************************************************************");
	IOUtils.log("************************************Start processing*****************************");
	IOUtils.log("*********************************************************************************");
	IOUtils.log(Calendar.getInstance().getTime().toString());
	for (String tweet : tweets) {
	    try {
		count += 1;
		IOUtils.log(Integer.toString(count));
		System.out.println(count);
		// create an empty Annotation just with the given text
		if (tweet == null) {
		    IOUtils.log("ERROR: Tweet is null");
		    continue;
		}
		Annotation document = new Annotation(tweet);
		if (pipeline == null) {
		    IOUtils.log("ERROR: pipeline is null");
		    continue;
		}

		// document.set (DocDateAnnotation.class, "2014-09-22");
		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as
		// keys
		// and
		// has values with custom types
		List<CoreMap> sentences = document
			.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

		    IOUtils.log(sentence.toString());
		    // traversing the words in the current sentence
		    // a CoreLabel is a CoreMap with additional token-specific
		    // methods
		    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			// this is the text of the token
			String word = token.get(TextAnnotation.class);
			System.out.print(word + ": ");
			// this is the POS tag of the token

			String pos = token.get(PartOfSpeechAnnotation.class);
			if (pos.equals("HT")) {
			    String ht = removeHash(word.toLowerCase());
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

			// System.out.print(pos + ": ");
			// this is the NER label of the token
			String ne = token.get(NamedEntityTagAnnotation.class);
			System.out.print(ne + " : ");
			// IOUtils.log(ne);
			if (!ne.equals("O")) {
			    if (isEntity(ne)) {
				try {
				    if (entities
					    .containsKey(word.toLowerCase())) {
					entities.put(
						word.toLowerCase(),
						entities.get(word.toLowerCase()) + 1);
				    } else {
					entities.put(word.toLowerCase(), 1);
				    }
				} catch (Exception ex) {
				    IOUtils.log(ex.getMessage());
				}

			    }
			}

			// String date = token
			// .get(NormalizedNamedEntityTagAnnotation.class);
			// if (date != null)
			// System.out.println(date.toString() + " : ");
		    }
		    System.out.println();

		    // //////////////
		    // EventDetection eveDet = new EventDetection();
		    // try {
		    // eveDet.detect(listWords.toArray(new
		    // String[listWords.size()]),
		    // listPos.toArray(new String[listPos.size()]));
		    // } catch (IOException e) {
		    // // TODO Auto-generated catch block
		    // e.printStackTrace();
		    // }
		    // /////////////////////
		    // sentence.get(Timex)

		    // ///////////////////////////////
		    // List<CoreMap> timeAnns = sentence.get
		    // (TimexAnnotations.class);
		    // if (timeAnns != null)
		    // {
		    // for (CoreMap timeAnn : timeAnns)
		    // {
		    // if (timeAnn != null)
		    // {
		    // TimeExpression te = timeAnn.get
		    // (TimeExpression.Annotation.class);
		    // if (te != null)
		    // {
		    // System.out.println (te.getTemporal ());
		    // }
		    // }
		    //
		    // }
		    // }
		    // ///////////////////////////

		    // this is the parse tree of the current sentence
		    // Tree tree = sentence.get(TreeAnnotation.class);

		    // this is the Stanford dependency graph of the current
		    // sentence
		    // SemanticGraph dependencies =
		    // sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		}
	    } catch (Exception ex) {
		IOUtils.log(ex.getMessage());
	    }
	}

	IOUtils.log("*********************************************************************************");
	IOUtils.log("************************************NER Done*************************************");
	IOUtils.log("*********************************************************************************");
	IOUtils.log(Calendar.getInstance().getTime().toString());
	// System.out.println(Calendar.getInstance().getTime());
	HashMap<String, Integer> sortedEntities = (HashMap<String, Integer>) pk.lums.edu.sma.utils.IOUtils
		.sortByValues(entities);
	// System.out.println (sortedEntities.toString ());
	IOUtils.log(sortedEntities.toString());

	String[] topEntities = pk.lums.edu.sma.utils.IOUtils.getTopNEntities(
		sortedEntities, (int) sortedEntities.size() / 10);
	EntityCluster cluster = new EntityCluster(topEntities);
	for (String tweet : tweets) {
	    for (String entity : topEntities) {
		if (tweet.toLowerCase().contains(entity)) {
		    try {
			cluster.putInCluster(entity, tweet);
		    } catch (Exception ex) {
			IOUtils.log(ex.getMessage());
		    }
		}
	    }
	}

	// pipeline.addAnnotator (new TimeAnnotator ("sutime", props));

	cluster.writeCluster();

	// System.out.println(Calendar.getInstance().getTime());
	IOUtils.log(Calendar.getInstance().getTime().toString());

    }

    public static boolean isEntity(String entity) {
	boolean isEntity = true;
	for (UnrelatedEntities ent : UnrelatedEntities.values()) {
	    if (ent.name().equals(entity)) {
		return false;
	    }
	}
	return isEntity;
    }

    public static String removeHash(String hashTag) {
	String ht;
	ht = hashTag.replace("#", "");
	return ht;
    }
}