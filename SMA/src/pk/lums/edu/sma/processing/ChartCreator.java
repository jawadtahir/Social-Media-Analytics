package pk.lums.edu.sma.processing;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class ChartCreator {
    private Map<String, Double> wordCount;
    private File cluster;

    public ChartCreator(Map<String, Double> wordCount, File chartName) {
	this.wordCount = wordCount;
	this.cluster = chartName;
    }

    public void create() {
	// JFrame frame = new JFrame(chartName);
	// frame.setVisible(true);
	// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	// frame.setSize(2560, 1600);
	String chartName = cluster.getName().substring(0,
		cluster.getName().length() - 4);
	DefaultCategoryDataset dataSet = createDataSet(wordCount);
	JFreeChart chart = createChart(dataSet, chartName);
	ChartPanel panel = new ChartPanel(chart);
	panel.setPreferredSize(new Dimension(25600, 16000));
	// frame.getContentPane().add(panel);
	try {
	    ChartUtilities.saveChartAsJPEG(new File(cluster.getParent() + "/"
		    + chartName + ".JPEG"), chart, 25600, 16000);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private DefaultCategoryDataset createDataSet(Map<String, Double> wordCount) {
	// TODO Auto-generated method stub
	DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	Iterator<Entry<String, Double>> itr = wordCount.entrySet().iterator();
	while (itr.hasNext()) {
	    Entry<String, Double> ent = itr.next();
	    dataSet.setValue(ent.getValue(), "Occurence", ent.getKey());
	}
	return dataSet;
    }

    protected JFreeChart createChart(DefaultCategoryDataset dataSet,
	    String clusterName) {
	JFreeChart chart = ChartFactory.createBarChart(clusterName, "Words",
		"Occurence", dataSet, PlotOrientation.VERTICAL, false, true,
		false);
	chart.getTitle().setPaint(Color.BLUE);
	chart.setBackgroundPaint(Color.WHITE);
	CategoryPlot cp = chart.getCategoryPlot();
	cp.setBackgroundPaint(Color.WHITE);
	cp.setRangeGridlinePaint(Color.RED);
	return chart;
    }

}
