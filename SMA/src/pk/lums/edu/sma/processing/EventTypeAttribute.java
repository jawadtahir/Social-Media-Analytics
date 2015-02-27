package pk.lums.edu.sma.processing;

import org.apache.lucene.util.Attribute;

public interface EventTypeAttribute extends Attribute, Cloneable {
    /**
     * Sets the type of the current token.
     *
     * @param type
     *            type of the current token.
     */
    void setType(EventType type);

    void setToken(String token);

    /**
     * Returns the type of the current token.
     *
     * @return type of the current token.
     */
    EventType getType();

    String getToken();

    Object clone();
}
