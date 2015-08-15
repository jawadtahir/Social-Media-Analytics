package pk.lums.edu.sma.models;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.utils.IOUtils;

public class EntityPhraseDateModel {

    private String entity = "";
    private Date date = null;
    private Map<String, Double> phrases = new LinkedHashMap<String, Double>();

    public EntityPhraseDateModel(String entity, Map<String, Double> mapphrases,
	    Date date) {
	super();
	this.entity = entity;
	this.date = date;
	this.phrases = mapphrases;
    }

    public String getEntity() {
	return entity;
    }

    public void setEntity(String entity) {
	this.entity = entity;
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public Map<String, Double> getPhrases() {
	return phrases;
    }

    public void setPhrases(Map<String, Double> phrases) {
	this.phrases = phrases;
    }

    public void sortAndRemove() {
	Map<String, Double> newPhrase = new LinkedHashMap<String, Double>();
	for (Map.Entry<String, Double> ent : IOUtils.sortByValues(this.phrases)
		.entrySet()) {
	    newPhrase.put(ent.getKey(), ent.getValue());
	    break;
	}
	this.phrases = newPhrase;
    }

    public void sortAndRemove(int count) {
	Map<String, Double> newPhrase = new LinkedHashMap<String, Double>();
	int i = 0;
	for (Map.Entry<String, Double> ent : IOUtils.sortByValues(this.phrases)
		.entrySet()) {
	    i++;
	    if (i <= count) {
		newPhrase.put(ent.getKey(), ent.getValue());
	    } else {
		break;
	    }
	}
	this.phrases = newPhrase;
    }

    public void addPhrases(List<String> phrases) {
	for (String phrase : phrases) {
	    if (this.phrases.containsKey(phrase)) {
		this.phrases.put(phrase, this.phrases.get(phrase) + 1);
	    } else {
		this.phrases.put(phrase, (double) 1);
	    }
	}
    }

    @Override
    public boolean equals(Object obj) {

	if (obj != null
		&& obj instanceof EntityPhraseDateModel
		&& ((EntityPhraseDateModel) obj).getDate().equals(this.date)
		&& ((EntityPhraseDateModel) obj).getEntity().equalsIgnoreCase(
			this.entity)) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public int hashCode() {
	return (this.entity + this.date.toString()).hashCode();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append("{ Entity: " + this.entity + ", Event: " + phrases.toString()
		+ ", Date: " + date.toString() + " }");

	return sb.toString();
    }
}
