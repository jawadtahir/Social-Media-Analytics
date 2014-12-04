package pk.lums.edu.sma.processing;

public class GetEntities extends Thread {

    private Thread t;
    private String[] tweets;

    public GetEntities(String[] tweet, String tNumber) {
	tweets = tweet;
	t = new Thread(this, tNumber);
	t.start();
    }

    public void run() {
	process();
    }

    private void process() {
	// TODO Auto-generated method stub

    }

}
