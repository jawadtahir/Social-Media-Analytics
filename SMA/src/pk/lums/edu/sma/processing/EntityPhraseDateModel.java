package pk.lums.edu.sma.processing;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityPhraseDateModel {

    private String entity = "";
    private Date date = null;
    private Map<String, Double> phrases = new HashMap<String, Double>();

    public EntityPhraseDateModel(String entity, List<String> listphrases,
	    Date date) {
	super();
	this.entity = entity;
	this.date = date;
	for (String phrase : listphrases) {
	    if (phrases.containsKey(phrase)) {
		phrases.put(phrase, phrases.get(phrase) + 1);
	    } else {
		phrases.put(phrase, (double) 1);
	    }
	}
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

    public void addPhrases(Map<String, Double> phrases) {
	for (Map.Entry<String, Double> ent : phrases.entrySet()) {
	    if (this.phrases.containsKey(ent.getKey())) {
		this.phrases.put(ent.getKey(), this.phrases.get(ent.getKey())
			+ ent.getValue());
	    } else {
		this.phrases.put(ent.getKey(), ent.getValue());
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
	sb.append("{ Entity: " + this.entity + " Event: " + phrases.toString()
		+ ", Date: " + date.toString() + " }");

	return sb.toString();
    }
}
