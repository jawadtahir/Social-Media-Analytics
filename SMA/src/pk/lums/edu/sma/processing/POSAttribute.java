package pk.lums.edu.sma.processing;

import org.apache.lucene.util.Attribute;

public interface POSAttribute extends Attribute, Cloneable {
	 /**
	   * Sets the type of the current token.
	   *
	   * @param type type of the current token.
	   */
	  void setType(POSEnum type);
	  
	  void setToken(String token);

	  /**
	   * Returns the type of the current token.
	   *
	   * @return type of the current token.
	   */
	  POSEnum getType();
	  
	  String getToken();

	  Object clone();

}
