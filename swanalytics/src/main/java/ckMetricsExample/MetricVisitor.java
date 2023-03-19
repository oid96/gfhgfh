package ckMetricsExample;

import beans.ClassBean;
import beans.PackageBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;
import parser.ClassParser;
import parser.CodeParser;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import metrics.*;

public class MetricVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        // We will need a parser to mine the structural properties of a class.
        CodeParser codeParser = new CodeParser();
        System.out.println(commit.getHash());
        // We want to compute metrics for all classes of the repository.
        scmRepository.getScm().checkout(commit.getHash());
        List<RepositoryFile> files = scmRepository.getScm().files();
        Vector<ClassBean> classes = new Vector<>();
        //header tabella


        for (RepositoryFile file: files) { // So, for each file available in the repository,
            if (file.getFullName().contains(".java")) {

                // Afterwards, we start parsing the file.
                CompilationUnit parsed;
                try {
                    // We exploit the parser capabilities to read a file and transform it in something usable for analysis.
                    parsed = codeParser.createParser(FileUtility.readFile(file.getFile().getAbsolutePath()));

                    if(parsed.types().size()>0) {
                        // Just for your information, the following instruction means that we will
                        // analyze only the main class in a file, i.e., we DO NOT consider inner classes.
                        TypeDeclaration typeDeclaration = (TypeDeclaration) parsed.types().get(0);

                        // While we could already use the TypeDeclaration to compute metrics, for the sake of
                        // understandability we are going to create more easily comprehensible objects, i.e.,
                        // those defined in the package 'beans'
                        Vector<String> imports = new Vector<String>();
                        for (Object importedResource : parsed.imports())
                            imports.add(importedResource.toString());

                        PackageBean packageBean = new PackageBean();
                        //System.out.println("FILE: "+file.getFullName());
                        String pacchetto="";
                        if(parsed.getPackage()!=null){
                            pacchetto=parsed.getPackage().getName().getFullyQualifiedName();
                        }
                        packageBean.setName(pacchetto);

                        ClassBean classBean = ClassParser.parse(typeDeclaration, packageBean.getName(), imports);
                        classBean.setPathToClass(file.getFile().getAbsolutePath());

                        classes.add(classBean);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // In this example, we do not handle the exception. NB: In a real case, you should handle it!
                }
            }
        }

        for (RepositoryFile file: files) { // So, for each file available in the repository,
            if (file.getFullName().contains(".java")) { // we check if the file is a java file, i.e., we do not want to consider configuration files and others.

                // Afterwards, we start parsing the file.
                CompilationUnit parsed;
                try {
                    // We exploit the parser capabilities to read a file and transform it in something usable for analysis.
                    parsed = codeParser.createParser(FileUtility.readFile(file.getFile().getAbsolutePath()));
                    if(parsed.types().size()>0) {
                        // Just for your information, the following instruction means that we will
                        // analyze only the main class in a file, i.e., we DO NOT consider inner classes.
                        TypeDeclaration typeDeclaration = (TypeDeclaration) parsed.types().get(0);

                        // While we could already use the TypeDeclaration to compute metrics, for the sake of
                        // understandability we are going to create more easily comprehensible objects, i.e.,
                        // those defined in the package 'beans'
                        Vector<String> imports = new Vector<String>();
                        for (Object importedResource : parsed.imports())
                            imports.add(importedResource.toString());

                        PackageBean packageBean = new PackageBean();
                        String pacchetto="";
                        if(parsed.getPackage()!=null){
                            pacchetto=parsed.getPackage().getName().getFullyQualifiedName();
                        }
                        packageBean.setName(pacchetto);

                        ClassBean classBean = ClassParser.parse(typeDeclaration, packageBean.getName(), imports);
                        classBean.setPathToClass(file.getFile().getAbsolutePath());

                        // Once converted the files read in our beans, we can compute metrics in a super-easy way.
                        // NB: The class 'CKMetrics' contains a number of other metrics!
                        int LOC = CKMetrics.getLOC(classBean);
                        int WMC = CKMetrics.getWMC(classBean);
                        int RFC = CKMetrics.getRFC(classBean);
                        int LCOM = CKMetrics.getLCOM2(classBean);
                        int CBO = CKMetrics.getCBO(classBean);
                        int DIT = CKMetrics.getDIT(classBean, classes, 0);
                        int NOC = CKMetrics.getNOC(classBean, classes);

                        boolean impIhn = CKMetrics.isImplementationInheritanceImplemented(classBean);
                        boolean intIhn = CKMetrics.isInterfaceInheritanceImplemented(classBean);
                        int delegations = CKMetrics.getDelegation(classBean);

                        /* Gerardo
                        for(Modification m: commit.getModifications()){
                            m.get
                        }
                        modificato=0
                        aggiunte=0
                        rimosse=0
                         */
                        // Now, let's pretty-print our results.
                        writer.write(
                                commit.getHash(),
                                //classBean.getBelongingPackage() + "." + classBean.getName(),
                                classBean.getName(),
                                LOC,
                                WMC,
                                RFC,
                                LCOM,
                                CBO,
                                DIT,
                                NOC,
                                impIhn,
                                intIhn,
                                delegations
                        );

                        // That's all folks!
                    }
                } catch (IOException e) {
                    // In this example, we do not handle the exception. NB: In a real case, you should handle it!
                }
            }
        }
    }
}
