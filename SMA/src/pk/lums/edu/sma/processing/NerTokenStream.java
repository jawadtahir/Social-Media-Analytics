package pk.lums.edu.sma.processing;

import java.util.ArrayList;
import java.util.List;

import com.twitter.common.text.token.TokenStream;

public class NerTokenStream extends TokenStream {
    private final NamedEntityTypeAttribute neAttribute;
    private final POSAttribute posAttribute;
    private final EventTypeAttribute eventAttribute;

    private String[] tokens;
    private NamedEntityType[] nerTags;
    private POSEnum[] posTags;
    private EventType[] eventTags;
    private int currIndex;
    private NamedEntityWrapper tagger;
    private List<String> specialChrctrArr = createSpecialArray();

    public NerTokenStream() {
	this.neAttribute = addAttribute(NamedEntityTypeAttribute.class);
	this.tagger = new NamedEntityWrapper();
	this.posAttribute = addAttribute(POSAttribute.class);
	this.eventAttribute = addAttribute(EventTypeAttribute.class);
    }

    private List<String> createSpecialArray() {
	ArrayList<String> specialChrctrArr = new ArrayList<String>();
	specialChrctrArr.add("$");
	specialChrctrArr.add("``");
	specialChrctrArr.add("''");
	specialChrctrArr.add("(");
	specialChrctrArr.add(")");
	specialChrctrArr.add(",");
	specialChrctrArr.add("--");
	specialChrctrArr.add(".");
	specialChrctrArr.add(":");
	return specialChrctrArr;
    }

    @Override
    public boolean incrementToken() {
	if (this.currIndex == this.tokens.length) {
	    return false;
	}
	this.neAttribute.setType(this.nerTags[this.currIndex]);
	this.neAttribute.setToken(this.tokens[this.currIndex]);
	this.posAttribute.setToken(this.tokens[this.currIndex]);
	this.posAttribute.setType(this.posTags[this.currIndex]);
	this.eventAttribute.setType(this.eventTags[this.currIndex]);
	this.eventAttribute.setToken(this.tokens[this.currIndex]);

	this.currIndex++;
	return true;
    }

    @Override
    public void reset(CharSequence input) {
	String[] tagged = this.tagger.tagTweet(input.toString()).split(" ");
	this.tokens = new String[tagged.length];
	this.nerTags = new NamedEntityType[tagged.length];
	this.posTags = new POSEnum[tagged.length];
	this.eventTags = new EventType[tagged.length];

	for (int i = 0; i < tagged.length; i++) {
	    String[] tokenTag = tagged[i].split("/");
	    if (tokenTag.length > 4) {
		String[] tempTokenTag = new String[4];
		String tempToken = "";
		for (int a = 0; a < (tokenTag.length - 3); a++) {
		    tempToken += tokenTag[a];
		}
		tempTokenTag[0] = tempToken;
		int b = 1;
		for (int a = 3; a >= 1; a--) {
		    tempTokenTag[b] = tokenTag[tokenTag.length - a];
		    b++;
		}
		tokenTag = tempTokenTag;
	    }
	    String token = tokenTag[0];
	    String nertag = tokenTag[1];
	    String pos = tokenTag[2].trim();
	    if (specialChrctrArr.contains(pos)) {
		pos = "SP";
	    }
	    String eventTag = tokenTag[3].trim();
	    this.tokens[i] = token;
	    this.nerTags[i] = NamedEntityType.valueOf(nertag.replace("-", ""));
	    this.posTags[i] = POSEnum.valueOf(pos.replace("-", ""));
	    this.eventTags[i] = EventType.valueOf(eventTag.replace("-", ""));
	}
	this.currIndex = 0;
    }
}
