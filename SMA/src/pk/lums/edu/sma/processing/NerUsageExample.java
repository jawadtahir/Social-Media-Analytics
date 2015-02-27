package pk.lums.edu.sma.processing;

import java.util.Map;

import pk.lums.edu.sma.utils.IOUtils;

import com.twitter.common.text.DefaultTextTokenizer;

/**
 * Annotated example illustrating major features of {@link DefaultTextTokenizer}
 * .
 */
public class NerUsageExample extends Thread {

    private Thread t;
    private String[] tweets;
    private Map<String, Integer> entities;

    public NerUsageExample(String[] tweets, String tNumber,
	    Map<String, Integer> entities) {
	// TODO Constructor
	this.tweets = tweets;
	this.entities = entities;
	this.t = new Thread(this, tNumber);
    }

    public void run() {
	process();
    }

    public void process() {
	// String[] tweets = IOUtils.readFile("tweets.txt");
	// This is the canonical way to create a token stream.
	UwNamedEntityTagger tokenizer = new UwNamedEntityTagger.Builder()
		.setKeepPunctuation(true).build();
	NerTokenStream stream = (NerTokenStream) tokenizer
		.getDefaultTokenStream();

	// We're going to ask the token stream what type of attributes it makes
	// available. "Attributes"
	// can be understood as "annotations" on the original text.
	// System.out.println("Attributes available:");
	// Iterator<Class<? extends Attribute>> iter = stream
	// .getAttributeClassesIterator();
	// while (iter.hasNext()) {
	// Class<? extends Attribute> c = iter.next();
	// System.out.println(" - " + c.getCanonicalName());
	// }
	// System.out.println("");

	// We're now going to iterate through a few tweets and tokenize each in
	// turn.
	int tokenCnt = 0;
	for (String tweet : tweets) {
	    // We're first going to demonstrate the "token-by-token" method of
	    // consuming tweets.
	    // System.out.println("Processing: " + tweet);
	    // Reset the token stream to process new input.
	    IOUtils.log("Thread " + this.getName() + " : Count : " + tokenCnt
		    + " Tweet : " + tweet);
	    try {
		stream.reset(tweet);
		tokenCnt++;
		// Now we're going to consume tokens from the stream.
		while (stream.incrementToken()) {
		    NamedEntityTypeAttribute nerAttribute = stream
			    .getAttribute(NamedEntityTypeAttribute.class);
		    POSAttribute posAttribute = stream
			    .getAttribute(POSAttribute.class);
		    EventTypeAttribute eventAttribute = stream
			    .getAttribute(EventTypeAttribute.class);
		    String token = removeHash(nerAttribute.getToken());
		    String ner = nerAttribute.getType().toString();
		    String pos = posAttribute.getType().toString();
		    String event = eventAttribute.getType().toString();
		    if (token.length() > 2) {
			// if (pos.equals("HT")) {
			// insertInEntityMap(token);
			// }
			// if (!ner.equals("O")) {
			// insertInEntityMap(token);
			// }
			if (!event.equals("O")) {
			    insertInEntityMap(token);
			}
		    }

		}
	    } catch (Exception e) {
		e.printStackTrace();
		IOUtils.log("ERROR: " + e.getMessage().toString());
		IOUtils.log("\t TWEET: " + tweet);
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
	    entities.put(token, 1);
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

}
