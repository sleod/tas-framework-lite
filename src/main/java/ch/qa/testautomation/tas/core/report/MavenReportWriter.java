package ch.qa.testautomation.tas.core.report;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.utils.XMLUtils;
import ch.qa.testautomation.tas.core.component.TestCaseObject;
import ch.qa.testautomation.tas.core.component.TestStepResult;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/*
//xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report-3.0.xsd"
 */
public class MavenReportWriter {

    public static void generateMavenTestXML(List<TestCaseObject> testCaseObjects, String absolutfilename, String backupFileName) {
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
        FileOperation.writeStringToFile(xmlString, absolutfilename);
        FileOperation.writeStringToFile(xmlString, backupFileName);
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
