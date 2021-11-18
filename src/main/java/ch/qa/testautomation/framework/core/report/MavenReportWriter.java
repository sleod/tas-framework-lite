package ch.qa.testautomation.framework.core.report;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.utils.XMLUtils;
import ch.qa.testautomation.framework.core.component.TestCaseObject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class MavenReportWriter {

    public static void generateMavenTestXML(List<TestCaseObject> testCaseObjects, String absolutefilename, String backupFileName) throws IOException {
        LinkedHashMap<String, Integer> testCaseNames = new LinkedHashMap<>();
        Document document = XMLUtils.createNewDoc("testsuite");
        Element root = document.getRootElement();
        int failure = 0;
        int tests = testCaseObjects.size();
        root.setAttribute("name", "WebAppTestCases");
        root.setAttribute("tests", String.valueOf(tests));
        for (TestCaseObject testCaseObject : testCaseObjects) {
            Element testcaseNode = new Element("testcase");
            testcaseNode.setAttribute("classname", "TestCaseObject");
            String name = testCaseObject.getName();
            if (testCaseNames.containsKey(name)) {
                int number = testCaseNames.get(name);
                testCaseNames.put(name, number + 1);
            } else {
                testCaseNames.put(name, 1);
            }
            testcaseNode.setAttribute("name", name);
            testcaseNode.setAttribute("number", String.valueOf(testCaseNames.get(name)));
            float duration = testCaseObject.getTestRunResult().getRunDuration();
            testcaseNode.setAttribute("time", String.valueOf(duration));
            if (testCaseObject.getTestRunResult().getStatus().equals(TestStatus.FAIL)) {
                failure++;
            }
            addTestStepResult(testcaseNode, testCaseObject);
            root.addContent(testcaseNode);
        }
        root.setAttribute("failure", String.valueOf(failure));
        document.setContent(root);
        XMLOutputter outPutter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outPutter.outputString(document);
        FileOperation.writeBytesToFile(xmlString.getBytes(), new File(absolutefilename));
        FileOperation.writeBytesToFile(xmlString.getBytes(), new File(backupFileName));
    }

    private static void addTestStepResult(Element testcaseNode, TestCaseObject testCaseObject) {
        testCaseObject.getTestRunResult().getStepResults().forEach(testStepResult -> {
            Element sysout = new Element("system-out");
            sysout.setText(testStepResult.getInfo());
            testcaseNode.addContent(sysout);
            if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
                Element fail = new Element("failure");
                fail.setAttribute("classname", "TestCaseStep");
                fail.setAttribute("message", testStepResult.getTestFailure().getMessage());
                fail.setAttribute("step", testStepResult.getName());
                fail.setText(testStepResult.getTestFailure().getTrace());
                testcaseNode.addContent(fail);
            } else if (testStepResult.getStatus().equals(TestStatus.SKIPPED)) {
                Element skipped = new Element("skipped");
                skipped.setAttribute("classname", "TestCaseStep");
                skipped.setAttribute("step", testStepResult.getName());
                testcaseNode.addContent(skipped);
            } else if (testStepResult.getStatus().equals(TestStatus.BROKEN)) {
                Element broken = new Element("broken");
                broken.setAttribute("classname", "TestCaseStep");
                broken.setAttribute("step", testStepResult.getName());
                broken.setAttribute("message", testStepResult.getTestFailure().getMessage());
                broken.setText(testStepResult.getTestFailure().getTrace());
                testcaseNode.addContent(broken);
            } else {
                Element success = new Element("success");
                success.setAttribute("classname", "TestCaseStep");
                success.setAttribute("step", testStepResult.getName());
                testcaseNode.addContent(success);
            }
        });
    }

}
