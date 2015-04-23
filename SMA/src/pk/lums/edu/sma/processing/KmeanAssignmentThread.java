package pk.lums.edu.sma.processing;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;

import pk.lums.edu.sma.utils.IOUtils;

public class KmeanAssignmentThread extends Thread {

    private Thread t;
    private LinkedHashMap<Integer, double[]> vecSpaceMap;
    private Map<double[], SortedSet<Integer>> clusters;

    public KmeanAssignmentThread(int threadName,
	    LinkedHashMap<Integer, double[]> vecSpace,
	    Map<double[], SortedSet<Integer>> cluster) {
	this.vecSpaceMap = vecSpace;
	this.clusters = cluster;
	this.t = new Thread(this, Integer.toString(threadName));
    }

    @Override
    public void run() {
	// TODO Auto-generated method stub
	process();
    }

    private void process() {
	// TODO Auto-generated method stub
	int i = 0;
	for (Map.Entry<Integer, double[]> entry : vecSpaceMap.entrySet()) {
	    i++;
	    if (i % 100 == 0)
		IOUtils.log(this.getName() + " : " + i);
	    double[] cent = null;
	    double sim = 0;
	    for (double[] c : clusters.keySet()) {
		double csim = cosSim(entry.getValue(), c);
		if (csim > sim) {
		    sim = csim;
		    cent = c;
		}
	    }
	    if (cent != null && entry.getKey() != null) {
		clusters.get(cent).add(entry.getKey());
	    }
	}
    }

    static double cosSim(double[] a, double[] b) {
	double dotp = 0, maga = 0, magb = 0;
	for (int i = 0; i < a.length; i++) {
	    dotp += a[i] * b[i];
	    maga += Math.pow(a[i], 2);
	    magb += Math.pow(b[i], 2);
	}
	maga = Math.sqrt(maga);
	magb = Math.sqrt(magb);
	double d = dotp / (maga * magb);
	return d == Double.NaN ? 0 : d;
    }
}
