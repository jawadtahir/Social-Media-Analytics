package pk.lums.edu.sma.processing;

public enum EventType {

    BEVENT("B-EVENT"), IEVENT("I-EVENT"), O("O");

    public final String name;

    private EventType(String s) {
	this.name = s;
    }
}
