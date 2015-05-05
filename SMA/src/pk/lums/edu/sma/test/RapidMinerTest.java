package pk.lums.edu.sma.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.XMLException;

public class RapidMinerTest {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);

	RapidMiner.init();

	// loads the process from the repository
	Process location = null;
	try {
	    location = new Process(
		    new File(
			    "/Users/jawadtahir/.RapidMiner5/repositories/Local Repository/abc.rmp"));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (XMLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
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
	IOObject result = ioResult.getElementAt(0);
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

}
