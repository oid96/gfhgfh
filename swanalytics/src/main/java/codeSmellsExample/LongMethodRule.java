package codeSmellsExample;

import beans.MethodBean;

public class LongMethodRule {

	public boolean isLongMethod(MethodBean pMethod) {
		String[] tokenizedTextualContent = pMethod.getTextContent().split("\n");
		
		if( (tokenizedTextualContent.length > 100) && (pMethod.getParameters().size() >= 2) )
			return true;
		
		return false;
	}
}