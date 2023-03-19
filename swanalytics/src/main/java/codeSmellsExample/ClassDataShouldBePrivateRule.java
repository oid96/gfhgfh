package codeSmellsExample;

import beans.ClassBean;
import metrics.CKMetrics;

public class ClassDataShouldBePrivateRule {

	public boolean isClassDataShouldBePrivate(ClassBean pClass) {
		
		if(CKMetrics.getNOPA(pClass) > 10)
			return true;
		
		return false;
	}
}
