package pk.lums.edu.sma.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntityDateModel {

    private String entity = "";
    private Date date = null;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public EntityDateModel(String entity, Date date) {
	super();
	this.entity = entity;
	this.date = date;
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

    @Override
    public boolean equals(Object obj) {

	if (obj != null && obj instanceof EntityDateModel
		&& ((EntityDateModel) obj).getDate().equals(this.date)
		&& ((EntityDateModel) obj).getEntity().equals(this.entity)) {
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
	sb.append("{ Entity: " + this.entity + ", Date: " + date.toString()
		+ " }");

	return sb.toString();
    }
}
