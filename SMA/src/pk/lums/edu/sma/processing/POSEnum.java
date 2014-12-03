package pk.lums.edu.sma.processing;

public enum POSEnum {
	CC("Conjunction"),
	CD("Numeral"),
	DT("Determiner"),
	EX("Existential"),
	FW("Foriegn Word"),
	IN("Preposition"),
	JJ("Adjective, ordinal"),
	JJR("Adjective, comparitive"),
	JJS("Adjective, superlative"),
	LS("List item"),
	MD("Modal auxiliary"),
	NN("Noun"),
	NNP("Proper noun"),
	NNPS("Proper noun plural"),
	NNS("Noun plural"),
	PDT("Pre determiner"),
	POS("Genitive marker"),
	PRP("Pronoun"),
	PRP$("Pronoun possesive"),
	RB("Adverb"),
	RBR("Adverb comparative"),
	RBS("Adverb Superlative"),
	RP("Particle"),
	RT("Retweet"),
	HT("Hashtag"),
	URL("URL"),
	USR("Username"),
	SYM("Symbol"),
	TO("To"),
	UH("Interjaction"),
	VB("Verb"),
	VBD("Verb past"),
	VBG("Verb gerund"),
	VBN("Verb past participle"),
	VBP("Verb"),
	VBZ("Verb"),
	WDT("WH determiner"),
	WP("WH pronoun"),
	WP$("WH pronoun possesive"),
	WRB("WH adverb"),
	SP("Special character"),
	O("O")
	;
	
	public final String name;
	private POSEnum (String name){
		this.name = name;
	}
	

}
