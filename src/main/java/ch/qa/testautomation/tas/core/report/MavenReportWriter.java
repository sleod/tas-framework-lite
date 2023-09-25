package ch.qa.testautomation.tas.core.report;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.common.utils.XMLUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.TestCaseObject;
import ch.qa.testautomation.tas.core.component.TestStepResult;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
//xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance xsi:noNamespaceSchemaLocation=https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report-3.0.xsd
*/
public class MavenReportWriter {

    public static void generateMavenTestXML(List<TestCaseObject> testCaseObjects) {
        String folder = PropertyResolver.getTestCaseReportLocation();
        String fileName = folder + "/" + "MavenXMLReport-" + DateTimeUtils.getFormattedLocalTimestamp() + ".xml";
        String backupFile = folder + "/" + "MavenXMLReport-latest.xml";
        LinkedHashMap<String, Integer> testCaseNames = new LinkedHashMap<>();
        Document document = XMLUtils.createNewDoc("testsuite");
        Element root = document.getRootElement();
        int failure = 0;
        int tests = testCaseObjects.size();
        root.setAttribute("name", "WebAppTestCases");
        root.setAttribute("tests", String.valueOf(tests));
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String name = testCaseObject.getName();
            if (testCaseNames.containsKey(name)) {
                int number = testCaseNames.get(name);
                testCaseNames.put(name, number + 1);
            } else {
                testCaseNames.put(name, 1);
            }
            Element testcaseNode = new Element("testcase")
                    .setAttribute("classname", "TestCaseObject")
                    .setAttribute("name", name)
                    .setAttribute("number", String.valueOf(testCaseNames.get(name)))
                    .setAttribute("time", String.valueOf(testCaseObject.getTestRunResult().getRunDuration()));
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
        FileOperation.writeStringToFile(xmlString, fileName);
        FileOperation.writeStringToFile(xmlString, backupFile);
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

    public static void generateSurefireXMLReport(List<TestCaseObject> testCaseObjects) {
        Document document = XMLUtils.createNewDoc("testsuite");
        Element testsuite = document.getRootElement();//testsuite
        AtomicInteger failure = new AtomicInteger();
        AtomicInteger tests = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        testCaseObjects.forEach(testCaseObject -> testCaseObject.getTestRunResult().getStepResults().forEach(testStepResult -> {
            tests.getAndIncrement();
            Element testCase = buildTestStep2Testcase(testStepResult)
                    .setAttribute("classname", testCaseObject.getTestRunResult().getName())
                    .setAttribute("name", testStepResult.getName())
                    .setAttribute("group", testCaseObject.getSuiteName())
                    .setAttribute("time", String.valueOf(testCaseObject.getTestRunResult().getRunDuration()));
            if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
                failure.getAndIncrement();
            } else if (testStepResult.getStatus().equals(TestStatus.SKIPPED)) {
                skipped.getAndIncrement();
            }
            testsuite.addContent(testCase);
        }));
        testsuite.setAttribute("name", "Automated Testcases")
                .setAttribute("tests", String.valueOf(tests.get()))
                .setAttribute("failures", String.valueOf(failure.get()))
                .setAttribute("errors", "0")
                .setAttribute("skipped", "0");
        document.setContent(testsuite);
        XMLOutputter outPutter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outPutter.outputString(document);
        FileOperation.writeStringToFile(xmlString, PropertyResolver.getTestCaseReportLocation() + "/" + "TEST-XMLReport-latest.xml");
    }

    //build test step as testcase
    private static Element buildTestStep2Testcase(TestStepResult testStepResult) {
        Element testCase = new Element("testcase");
        Element sysout = new Element("system-out");
        sysout.addContent(new CDATA(testStepResult.getInfo()));
        testCase.addContent(sysout);
        if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
            Element failure = new Element("failure")
                    .setAttribute("message", testStepResult.getTestFailure().getMessage())
                    .setAttribute("type", testStepResult.getTestFailure().getException().getClass().getName())
                    .addContent(new CDATA(testStepResult.getTestFailure().getTrace()));
            testCase.addContent(failure);
        }
        return testCase;
    }

}
