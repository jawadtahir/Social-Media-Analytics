package pk.lums.edu.sma.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.plyjy.factory.JythonObjectFactory;
import org.python.core.PyArray;
import org.python.core.PyArray.array_fromlist_exposer;
import org.python.core.PyInstance;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import pk.lums.edu.sma.utils.IOUtils;

public class EventDetection {
	private static String basAdr = "/home/jawad/Documents/twitter_nlp";
	private static String procStr = "java -Xmx256m -Xms256m -XX:+UseSerialGC -cp "
			+ basAdr
			+ "/mallet-2.0.6/lib/mallet-deps.jar:"
			+ basAdr
			+ "/mallet-2.0.6/class cc.mallet.fst.SimpleTaggerStdin --weights sparse --model-file "
			+ basAdr + "/models/event/event.model";

	public  void detect(String[] words, String[] pos) throws IOException {
		// TODO Auto-generated method stub
		Process evePro = Runtime.getRuntime().exec(procStr);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				evePro.getOutputStream()));
		BufferedReader br = new BufferedReader(new InputStreamReader(
				evePro.getInputStream()));
		PythonInterpreter pyInt = new PythonInterpreter();
		pyInt.exec("import os");
		pyInt.exec("import sys");
		pyInt.exec("BASE_DIR = os.environ['TWITTER_NLP']");
		pyInt.exec("sys.path.append('%s/python' % (BASE_DIR))");
		//pyInt.exec("import event_tagger_stdin");
		pyInt.execfile("/home/jawad/Documents/twitter_nlp/python/event_tagger_stdin2.py");
		PyInstance tag = (PyInstance) pyInt.eval("EventTagger()");
		// String[] words = { "RT", "@Arsenal", ":", "Arsene", "Wenger", "has",
		// "given", "his", "take", "on", "the", "defeat", "to",
		// "Borussia", "Dortmund" };
		// String[] pos = { "RT", "USR", ":", "NNP", "NNP", "VBZ", "VBN",
		// "PRP$",
		// "NN", "IN", "DT", "NN", "TO", "NNP", "NNP" };

		// pyInt.exec("obj = event_tagger_stdin.EventTagger()");
		// PyObject obj = pyInt.get("TagSentence");

		PyArray pyWords = new PyArray(PyString.class, words);
		PyArray pyPos = new PyArray(PyString.class, pos);
		for (String string : words) {
			pyWords.__add__(new PyString(string).__unicode__());
		}
		for (String string : pos) {
			pyPos.__add__(new PyString(string).__unicode__());
		}

		PyString a = new PyString(words.toString());
		PyString b = new PyString(pos.toString());
		PyObject res = tag.invoke("TagSentence", pyWords.array_tolist(),
				pyPos.array_tolist());
		String resStr = res.asString();
		bw.write(resStr);
		bw.flush();
		String eventss = "";

		for (int i = 0; i < pos.length; i++) {
			eventss += br.readLine();
		}

		evePro.destroy();
		pyInt.close();

		System.out.println(eventss);

	}

}
