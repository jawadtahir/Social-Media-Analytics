package pk.lums.edu.sma.processing;

import java.util.Date;

public class ClusterModel implements Comparable<ClusterModel> {

    private String text = null;
    private Date date = null;
    private String cluster = null;
    private int id = 0;

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

    @Override
    public int compareTo(ClusterModel o) {
	// TODO Auto-generated method stub
	return this.getDate().compareTo(o.getDate());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(this.cluster + " , " + this.text + " , "
		+ this.date.toString());
	return sb.toString();
    }

}
