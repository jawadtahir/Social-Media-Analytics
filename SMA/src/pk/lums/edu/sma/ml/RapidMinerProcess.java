package pk.lums.edu.sma.ml;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.dos.TweetDO;
import pk.lums.edu.sma.utils.IOUtils;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExitMode;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.Cluster;
import com.rapidminer.operator.clustering.ClusterModel;
import com.rapidminer.tools.XMLException;

public class RapidMinerProcess {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);

	RapidMiner.init();

	// loads the process from the repository
	Process location = null;
	try {
	    location = new Process(
		    new File(
			    "/home/jawad/.RapidMiner5/repositories/Local Repository/abc.rmp"));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (XMLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	location.getRootOperator()
		.getSubprocess(0)
		.getOperatorByName("Read ARFF")
		.setParameter("data_file",
			"/home/jawad/git/Social-Media-Analytics/SMA/testWekaID1.arff");
	// Entry entry = location.locateEntry();
	// if (entry instanceof ProcessEntry) {
	// Process process = new RepositoryProcessLocation(location).load(null);

	// Operator op = location.getOperator("Read Excel");
	// op.setParameter(ExcelExampleSource.PARAMETER_EXCEL_FILE,
	// "E:\\Amit_Agrawal\\thesis\\paper\\anomaly in text\\blog\\data\\component.xls");

	// Operator outp = location.getOperator("Write Excel");
	// outp.setParameter(ExcelExampleSource.PARAMETER_EXCEL_FILE,
	// "E:\\Amit_Agrawal\\thesis\\paper\\anomaly in text\\blog\\data\\abcd.xls");

	IOContainer ioResult = null;
	try {
	    ioResult = location.run();
	} catch (OperatorException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	Map<Integer, List<Double>> clustMap = new HashMap<Integer, List<Double>>();
	IOObject result = ioResult.getElementAt(0);
	ClusterModel cmodel = (ClusterModel) result;
	for (Cluster clust : cmodel.getClusters()) {
	    clustMap.put(clust.getClusterId(), (List) clust.getExampleIds());
	}

	printClusters(clustMap, 8);
	RapidMiner.quit(ExitMode.NORMAL);
	// use the result(s) as needed, for example if your process just returns
	// one ExampleSet, use this:
	if (ioResult.getElementAt(0) instanceof ExampleSet) {
	    ExampleSet resultSet = (ExampleSet) ioResult.getElementAt(0);
	    System.out.println(result);
	    int i = 0;
	    for (Example example : resultSet) {
		Iterator<Attribute> allAtts = example.getAttributes()
			.allAttributes();

		while (allAtts.hasNext()) {
		    Attribute a = allAtts.next();
		    if (i <= 3)
			System.out.print(a.getName() + "  ");

		    i++;
		}
	    }
	    System.out.println("\n");
	    for (Example example : resultSet) {
		Iterator<Attribute> allAtts = example.getAttributes()
			.allAttributes();

		while (allAtts.hasNext()) {
		    Attribute a = allAtts.next();

		    if (a.isNumerical()) {
			double value = example.getValue(a);
			System.out.print(value + " ");
			// System.out.println("\n");

		    } else {
			String value = example.getValueAsString(a);
			System.out.print(value + " ");
			// System.out.println("\n");
		    }
		}
		System.out.println("\n");

	    }

	}
    }

    private static void printClusters(Map<Integer, List<Double>> clusters,
	    int iteration) {
	// TODO Auto-generated method stub
	IOUtils.clearClusterFolder("clusters" + iteration);
	Iterator<Integer> itr = clusters.keySet().iterator();
	Connection con = null;
	PreparedStatement pst = null;
	try {
	    con = IOUtils.getConnection();
	    pst = con.prepareStatement(TweetDO.SELECT_ALL_FROM_ID_US);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	int i = 0;
	while (itr.hasNext()) {
	    Integer key = itr.next();
	    List<Double> tweets = clusters.get(key);
	    Iterator<Double> setItr = tweets.iterator();
	    List<TweetDO> tdoList = new ArrayList<TweetDO>();
	    i++;
	    while (setItr.hasNext()) {
		// Double did = (Double.parseDouble(setItr.next().toString()));
		int id = setItr.next().intValue();
		try {
		    pst.setInt(1, id);
		    List<TweetDO> tempList = TweetDO.translateAllTweetDO(pst
			    .executeQuery());
		    tdoList.add(tempList.get(0));
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	    printCluster(tdoList, i, iteration);
	}

    }

    private static void printCluster(List<TweetDO> tdoList, int i, int iteration) {
	// TODO Auto-generated method stub
	File file = new File("clusters" + iteration);
	if (!file.exists()) {
	    file.mkdir();
	}
	StringBuilder sb = new StringBuilder();
	for (int j = 0; j < tdoList.size(); j++) {
	    TweetDO tdo = tdoList.get(j);
	    sb.append(tdo.getTextTweet().trim() + " , "
		    + tdo.getDateTextTweet().trim() + " , " + tdo.getId()
		    + " , " + tdo.getLocTweet().trim() + "\n");
	}
	IOUtils.writeFile(file.getAbsolutePath() + "/cluster-" + i + ".txt", sb
		.toString().trim());

    }

}
