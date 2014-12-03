package pk.lums.edu.sma.processing;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.twitter.common.text.token.TokenStream;
import com.twitter.common.text.token.TokenizedCharSequence;
import com.twitter.common.text.token.TokenizedCharSequence.Builder;
import com.twitter.common.text.token.TokenizedCharSequence.Token;
import com.twitter.common.text.token.attribute.CharSequenceTermAttribute;
import com.twitter.common.text.token.attribute.PartOfSpeechAttribute;
import com.twitter.common.text.token.attribute.TokenType;
import com.twitter.common.text.token.attribute.TokenTypeAttribute;

public class NerTokenStream extends TokenStream {
	private final NamedEntityTypeAttribute neAttribute;
	private final POSAttribute posAttribute;

	private String[] tokens;
	private NamedEntityType[] nerTags;
	private POSEnum[] posTags;
	private int currIndex;
	private NamedEntityWrapper tagger;
	private List<String> specialChrctrArr = createSpecialArray();

	public NerTokenStream() {
		this.neAttribute = addAttribute(NamedEntityTypeAttribute.class);
		this.tagger = new NamedEntityWrapper();
		this.posAttribute = addAttribute(POSAttribute.class);
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
		// System.out.println("token" + Integer.toString(currIndex) + ":" +
		// tokens[currIndex]);
		// System.out.println("tag  " + Integer.toString(currIndex) + ":" +
		// tags[currIndex]);
		// System.out.println(this.neAttribute.getToken());
		// System.out.println(this.neAttribute.getType());
		// System.out.println();
		this.currIndex++;
		return true;
	}

	@Override
	public void reset(CharSequence input) {
		String[] tagged = this.tagger.tagTweet(input.toString()).split(" ");
		this.tokens = new String[tagged.length];
		this.nerTags = new NamedEntityType[tagged.length];
		this.posTags = new POSEnum[tagged.length];

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
					// int b = 1;
					tempTokenTag[b] = tokenTag[tokenTag.length - a];
					b++;
				}
				tokenTag = tempTokenTag;
			}
			String token = tokenTag[0];
			for (int j = 1; j < tokenTag.length; j++) {
				token += " " + tokenTag[j];
			}
			String nertag = tokenTag[1];
			String pos = tokenTag[2].trim();
			if (specialChrctrArr.contains(pos)) {
				pos = "SP";
			}
			//System.out.println(pos);
			this.tokens[i] = token;
			this.nerTags[i] = NamedEntityType.valueOf(nertag.replace("-", ""));
			this.posTags[i] = POSEnum.valueOf(pos.replace("-", ""));
		}
		this.currIndex = 0;
	}
}
