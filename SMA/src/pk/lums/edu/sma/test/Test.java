package pk.lums.edu.sma.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import pk.lums.edu.sma.utils.IOUtils;

public class Test {
    public static void main(String[] args) {
	StringBuilder str = new StringBuilder();
	str.append("SELECT textTweet FROM TWEETDATA.TWEETDTAUS where ");
	String filepath = "/home/jawad/git/Social-Media-Analytics/SMA/EntLine.csv";
	List<String> lines = Arrays.asList(IOUtils.readFile(filepath));
	lines = lines.subList(0, 100);
	for (String line : lines) {
	    String tok = line.split(", ")[0].trim().toLowerCase();
	    str.append("textTweet like '%").append(tok).append("%' or ");
	}
	str.delete(str.length() - 4, str.length());
	System.out.println(str);
	Connection con = null;
	int count = 0;
	try {
	    PrintWriter pw = new PrintWriter("jawadtt100.txt");
	    con = IOUtils.getConnection();
	    ResultSet rs = con.prepareStatement(str.toString()).executeQuery();

	    while (rs.next()) {
		count++;
		pw.println(rs.getString("textTweet"));
	    }
	    pw.println(count);
	    pw.close();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
