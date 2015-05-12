package pk.lums.edu.sma.test;

import java.io.File;
import java.util.List;

public class TestObject implements Comparable<TestObject> {
    File cluster1 = null;
    File cluster2 = null;
    List<Integer> listIds = null;
    Double score = (double) 0f;

    public TestObject(File cluster1, File cluster2, List<Integer> listIds,
	    Double score) {
	super();
	this.cluster1 = cluster1;
	this.cluster2 = cluster2;
	this.listIds = listIds;
	this.score = score;
    }

    @Override
    public int compareTo(TestObject o) {
	// TODO Auto-generated method stub
	this.score.compareTo(o.score);
	return 0;
    }
}
