package reuseMetricsExample;

import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import metrics.CKMetrics;
import org.eclipse.core.runtime.CoreException;
import utilities.FolderToJavaProjectConverter;

import java.io.File;
import java.util.Vector;

public class ReuseMetricsComputation {

    public static void main(String args[]) {

        // Path to the directory containing all the projects under analysis
        String pathToDirectory = "path-to-directory";
        File experimentDirectory = new File(pathToDirectory);

        for (File project : experimentDirectory.listFiles()) {
            try {
                // Method to convert a directory into a set of java packages.
                Vector<PackageBean> packages = FolderToJavaProjectConverter.convert(project.getAbsolutePath());

                for (PackageBean packageBean : packages) {
                    for (ClassBean classBean : packageBean.getClasses()) {

                        int delegations = CKMetrics.getDelegation(classBean);
                        boolean isImplementationInheritanceImplemented =
                                CKMetrics.isImplementationInheritanceImplemented(classBean);
                        boolean isInterfaceInheritanceImplemented =
                                CKMetrics.isInterfaceInheritanceImplemented(classBean);

                        System.out.println("Class: " + classBean.getBelongingPackage()
                                + "." + classBean.getName() + "\n"
                                + "		delegations: " + delegations + "\n"
                                + "		implementation inheritance: " + isImplementationInheritanceImplemented + "\n"
                                + "		interface inheritance: " + isInterfaceInheritanceImplemented);

                        for (MethodBean methodBean : classBean.getMethods()) {
                            int methodDelegations = CKMetrics.getDelegation(methodBean);
                        }
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
}
