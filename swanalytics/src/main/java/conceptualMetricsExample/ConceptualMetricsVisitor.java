package conceptualMetricsExample;

import beans.ClassBean;
import beans.PackageBean;
import metrics.CKMetrics;
import metrics.ConceptualMetrics;
import metrics.FileUtility;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;
import parser.ClassParser;
import parser.CodeParser;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class ConceptualMetricsVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        // We will need a parser to mine the structural properties of a class.
        CodeParser codeParser = new CodeParser();

        // We want to compute metrics for all classes of the repository.
        List<RepositoryFile> files = scmRepository.getScm().files();

        for (RepositoryFile file: files) { // So, for each file available in the repository,
            if (file.getFullName().contains(".java")) { // we check if the file is a java file, i.e., we do not want to consider configuration files and others.

                // Afterwards, we start parsing the file.
                CompilationUnit parsed;
                try {
                    // We exploit the parser capabilities to read a file and transform it in something usable for analysis.
                    parsed = codeParser.createParser(FileUtility.readFile(file.getFile().getAbsolutePath()));
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
                    packageBean.setName(parsed.getPackage().getName().getFullyQualifiedName());

                    ClassBean classBean = ClassParser.parse(typeDeclaration, packageBean.getName(), imports);
                    classBean.setPathToClass(file.getFile().getAbsolutePath());

                    // Once converted the files read in our beans, we can compute metrics in a super-easy way.
                    // NB: The class 'ConceptualMetrics' contains another metric, which allows you to compute the
                    // conceptual coupling between classes!
                    double C3  = ConceptualMetrics.getC3(classBean);

                    // Now, let's pretty-print our results.
                    writer.write(
                            commit.getHash(),
                            classBean.getBelongingPackage() + "." + classBean.getName(),
                            C3
                    );

                    // That's all folks!

                } catch (IOException e) {
                    // In this example, we do not handle the exception. NB: In a real case, you should handle it!
                }
            }
        }
    }
}
