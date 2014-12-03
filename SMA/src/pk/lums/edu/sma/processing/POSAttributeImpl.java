package pk.lums.edu.sma.processing;

import org.apache.lucene.util.AttributeImpl;

public class POSAttributeImpl extends AttributeImpl implements POSAttribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5090281621679538420L;
	private POSEnum pos;
	private String token;
	
	@Override
	public void setType(POSEnum type) {
		// TODO Auto-generated method stub
		this.pos = type;
	}

	@Override
	public void setToken(String token) {
		// TODO Auto-generated method stub
		this.token = token;
	}

	@Override
	public POSEnum getType() {
		// TODO Auto-generated method stub
		return this.pos;
	}

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return this.token;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		this.pos = POSEnum.POS.O;
	}

	@Override
	public void copyTo(AttributeImpl arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof POSAttributeImpl){
			((POSAttributeImpl) arg0).setType(getType());
		}
	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return arg0 != null
		        && arg0 instanceof POSAttributeImpl
		        && ((POSAttributeImpl) arg0).pos == this.pos;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.pos.hashCode();
	}
	
	@Override
	public Object clone(){
		POSAttribute result = (POSAttribute) super.clone();
		return result;
	}

}
