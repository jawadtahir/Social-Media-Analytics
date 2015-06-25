package pk.lums.edu.sma.processing;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pk.lums.edu.sma.utils.IOUtils;

public class ProcessClusterThread extends Thread {
    // private List<ClusterModel> clustModList = null;
    private List<File> allFileArr = null;
    private String name = null;
    private ArrayList<File> fileArr = null;
    private DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    Thread t = null;

    public ProcessClusterThread(List<File> filesToProc, String name,
	    ArrayList<File> files) {
	this.allFileArr = filesToProc;
	this.name = name;
	this.fileArr = files;
	t = new Thread(this, name);
    }

    @Override
    public void run() {
	// TODO Auto-generated method stub
	process();
    }

    private void process() {
	// TODO Auto-generated method stub
	File relationDir = new File("Relationship1");
	if (!relationDir.exists()) {
	    relationDir.mkdir();
	}
	int count = 0;
	// clearDir(relationDir.getAbsolutePath());
	for (File cFile : fileArr) {
	    IOUtils.log("Thread - " + this.getName() + " Count = " + count
		    + " Cluster: " + cFile.getName());
	    String[] tweetsEnt = IOUtils.readFile(cFile.getAbsolutePath());
	    StringBuilder sbTemp = new StringBuilder();
	    String cClusterName = cFile.getName().substring(0,
		    cFile.getName().length() - 4);
	    File tempFile = new File(relationDir.getAbsoluteFile() + "/"
		    + cClusterName);
	    if (!tempFile.exists()) {
		tempFile.mkdir();
	    }

	    List<ClusterModel> cClustMod = makeClusterModelList(tweetsEnt,
		    cClusterName);
	    for (ClusterModel cmodel : cClustMod) {
		sbTemp.append(cmodel.toString() + "\n");
	    }
	    IOUtils.writeFile(tempFile.getAbsolutePath() + "/" + cClusterName
		    + ".txt", sbTemp.toString(), false);
	    Date[] dateRange = getRange(tweetsEnt);
	    for (File rFile : allFileArr) {
		StringBuilder sb = new StringBuilder();
		if (!rFile.getName().equals(cFile.getName())) {
		    String[] tweetsEntTemp = IOUtils.readFile(rFile
			    .getAbsolutePath());
		    String rClusterName = rFile.getName().substring(0,
			    rFile.getName().length() - 4);
		    List<ClusterModel> rClustMod = makeClusterModelList(
			    tweetsEntTemp, rClusterName, dateRange);

		    for (ClusterModel cModel : rClustMod) {
			sb.append(cModel.toString() + "\n");
		    }
		    double simNum = (double) (rClustMod.size())
			    / (double) (tweetsEntTemp.length);
		    sb.append(simNum);

		    IOUtils.writeFile(tempFile.getAbsolutePath() + "/"
			    + cClusterName + "_" + rClusterName + ".txt",
			    sb.toString(), false);
		}
		count++;

	    }

	}

    }

    private boolean isInRange(Date date, Date[] range) {
	boolean retVal = false;
	retVal = ((date.after(range[0]) || date.equals(range[0])) && (date
		.before(range[1]) || date.equals(range[1]))) ? true : false;
	return retVal;
    }

    // private int getindex(Date date) {
    // int index = 0;
    // for (ClusterModel cModel : clustModList) {
    // Date cDate = cModel.getDate();
    // if (cDate.equals(date)) {
    // return index;
    // }
    // index++;
    // }
    // return -1;
    // }

    private Date[] getRange(String[] tweets) {
	Map<Date, Integer> dateMap = new LinkedHashMap<Date, Integer>();
	int tweetCount = 0;
	// Date maxDate = null;
	// Date minDate = null;
	for (String tweet : tweets) {
	    StringBuilder sb = new StringBuilder(tweet);
	    sb.append("  ");
	    String[] tweetEntry = sb.toString().split(" , ");
	    if (tweetEntry.length >= 4) {
		Date time = null;
		String date = tweetEntry[tweetEntry.length - 3].trim();
		date = date.replace(",", "");
		try {
		    time = df.parse(date);
		    IOUtils.insertInMap(dateMap, time);
		    // if (tweetCount != 0) {
		    // if (time.after(maxDate)) {
		    // maxDate = time;
		    // } else if (time.before(minDate)) {
		    // minDate = time;
		    // }
		    // } else {
		    // maxDate = time;
		    // minDate = time;
		    // }
		    tweetCount++;
		} catch (ParseException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}

	    }
	}
	dateMap = IOUtils.sortByValues(dateMap);
	System.out.println(dateMap);
	Date range[] = getTopNDateRange(dateMap, findN(dateMap));

	return range;
    }

    private int findN(Map<Date, Integer> dateMap) {
	// TODO Auto-generated method stub
	int sum = 0;
	int N = 0;
	for (Map.Entry<Date, Integer> ent : dateMap.entrySet()) {
	    sum += ent.getValue();
	}
	int instSum = 0;
	int previousVal = 0;
	for (Map.Entry<Date, Integer> ent : dateMap.entrySet()) {
	    N++;
	    instSum += ent.getValue();
	    if (instSum > (sum / 2) && previousVal <= (ent.getValue() / 2)) {
		break;
	    }
	    previousVal = ent.getValue();
	}
	return N;
    }

    private Date[] getTopNDateRange(Map<Date, Integer> dateMap, int i) {
	// TODO Auto-generated method stub
	Date minDate = null, maxDate = null;
	int count = 0;
	for (Map.Entry<Date, Integer> ent : dateMap.entrySet()) {
	    if (count == 0) {
		minDate = ent.getKey();
		maxDate = ent.getKey();
	    } else {
		if (count >= i) {
		    break;
		} else {
		    if (ent.getKey().after(maxDate)) {
			maxDate = ent.getKey();
		    } else if (ent.getKey().before(minDate)) {
			minDate = ent.getKey();
		    }
		}
	    }
	    count++;
	}
	Calendar cal = Calendar.getInstance();
	cal.setTime(maxDate);
	cal.add(Calendar.DAY_OF_YEAR, 1);
	maxDate = cal.getTime();

	return new Date[] { minDate, maxDate };
    }

    private void clearDir(String relationDir) {
	File relatDir = new File(relationDir);
	for (String strRelat : relatDir.list()) {
	    File relate = new File(relationDir, strRelat);
	    deleteDir(relate);
	}
    }

    private void deleteDir(File file) {
	Path dir = Paths.get(file.getAbsolutePath());
	try {
	    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

		@Override
		public FileVisitResult visitFile(Path file,
			BasicFileAttributes attrs) throws IOException {

		    System.out.println("Deleting file: " + file);
		    Files.delete(file);
		    return CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir,
			IOException exc) throws IOException {

		    System.out.println("Deleting dir: " + dir);
		    if (exc == null) {
			Files.delete(dir);
			return CONTINUE;
		    } else {
			throw exc;
		    }
		}

	    });
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    // private ArrayList<ClusterModel> getNtoMelements(int start, int end) {
    // ArrayList<ClusterModel> clusterList = new ArrayList<ClusterModel>();
    // for (int i = start; i <= end; i++) {
    // ClusterModel cModel = clustModList.get(i);
    // clusterList.add(cModel);
    // }
    // return clusterList;
    // }

    private List<ClusterModel> makeClusterModelList(String[] tweetsEnt,
	    String clusterName, Date[] dateRange) {
	List<ClusterModel> clustModList = new ArrayList<ClusterModel>();
	for (String tweetEnt : tweetsEnt) {
	    StringBuilder sb = new StringBuilder(tweetEnt);
	    sb.append("  ");
	    String[] tweetEntArr = sb.toString().split(" , ");
	    if (tweetEntArr.length >= 4) {
		String loc = tweetEntArr[tweetEntArr.length - 1].trim();
		String id = tweetEntArr[tweetEntArr.length - 2].trim();
		String time = tweetEntArr[tweetEntArr.length - 3].trim();
		time = time.replace(",", "");
		Date date = null;
		try {
		    date = df.parse(time);
		    sb = new StringBuilder();
		    for (int i = 0; i <= tweetEntArr.length - 4; i++) {
			sb.append(tweetEntArr[i]);
		    }
		    if (isInRange(date, dateRange)) {
			ClusterModel cModel = new ClusterModel(sb.toString(),
				date, clusterName, Integer.parseInt(id));
			clustModList.add(cModel);
		    }
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }
	}
	Collections.sort(clustModList);
	return clustModList;
    }

    private List<ClusterModel> makeClusterModelList(String[] tweetsEnt,
	    String clusterName) {
	List<ClusterModel> clustModList = new ArrayList<ClusterModel>();
	for (String tweetEnt : tweetsEnt) {
	    StringBuilder sb = new StringBuilder(tweetEnt);
	    sb.append("  ");
	    String[] tweetEntArr = sb.toString().split(" , ");
	    if (tweetEntArr.length >= 4) {
		String loc = tweetEntArr[tweetEntArr.length - 1].trim();
		String id = tweetEntArr[tweetEntArr.length - 2].trim();
		String time = tweetEntArr[tweetEntArr.length - 3].trim();
		time = time.replace(",", "");
		Date date = null;
		try {
		    date = df.parse(time);
		    sb = new StringBuilder();
		    for (int i = 0; i <= tweetEntArr.length - 4; i++) {
			sb.append(tweetEntArr[i]);
		    }
		    ClusterModel cModel = new ClusterModel(sb.toString(), date,
			    clusterName, Integer.parseInt(id));
		    clustModList.add(cModel);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	    }
	}
	Collections.sort(clustModList);
	return clustModList;
    }
}
