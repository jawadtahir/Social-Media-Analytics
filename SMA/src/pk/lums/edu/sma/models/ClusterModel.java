package pk.lums.edu.sma.models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClusterModel implements Comparable<ClusterModel> {

    private String text = null;
    private Date date = null;
    private String cluster = null;
    private int id = 0;
    private String location = null;

    private SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public ClusterModel(String text, Date date, String cluster, int id,
	    String location) {
	super();
	this.text = text;
	this.date = date;
	this.cluster = cluster;
	this.id = id;
	this.setLocation(location);
    }

    public ClusterModel(String text, Date date, String cluster, int id) {
	super();
	this.text = text;
	this.date = date;
	this.cluster = cluster;
	this.id = id;
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }

    public String getCluster() {
	return cluster;
    }

    public void setCluster(String cluster) {
	this.cluster = cluster;
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public String getLocation() {
	return location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    @Override
    public int compareTo(ClusterModel o) {
	return this.getDate().compareTo(o.getDate());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.cluster + " , " + this.text + " , "
		+ df.format(this.date) + " , " + this.id + " , "
		+ this.getLocation() + " ");
	return sb.toString();
    }

}
