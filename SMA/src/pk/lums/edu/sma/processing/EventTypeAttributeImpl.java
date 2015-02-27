package pk.lums.edu.sma.processing;

import org.apache.lucene.util.AttributeImpl;

public class EventTypeAttributeImpl extends AttributeImpl implements
	EventTypeAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = -26697609293965910L;

    private EventType type = EventType.O;
    private String token;

    @Override
    public void setType(EventType type) {
	// TODO Auto-generated method stub
	this.type = type;
    }

    @Override
    public void setToken(String token) {
	// TODO Auto-generated method stub
	this.token = token;
    }

    @Override
    public EventType getType() {
	// TODO Auto-generated method stub
	return this.type;
    }

    @Override
    public String getToken() {
	// TODO Auto-generated method stub
	return this.token;
    }

    @Override
    public void clear() {
	// TODO Auto-generated method stub
	this.type = EventType.O;
    }

    @Override
    public void copyTo(AttributeImpl arg0) {
	// TODO Auto-generated method stub
	if (arg0 instanceof EventTypeAttributeImpl) {
	    ((EventTypeAttributeImpl) arg0).setType(getType());
	}
    }

    @Override
    public boolean equals(Object arg0) {
	// TODO Auto-generated method stub
	return arg0 != null && arg0 instanceof EventTypeAttributeImpl
		&& ((EventTypeAttributeImpl) arg0).type == this.type;
    }

    @Override
    public int hashCode() {
	// TODO Auto-generated method stub
	return this.type.hashCode();
    }

    @Override
    public Object clone() {
	EventTypeAttribute result = (EventTypeAttribute) super.clone();
	return result;
    }

}
