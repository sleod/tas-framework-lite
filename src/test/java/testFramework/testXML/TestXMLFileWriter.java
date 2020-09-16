//package testFramework.testXML;

//import ch.raiffeisen.testautomation.framework.core.report.MavenReportRewriter;

//import org.junit.Test;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.support.FindBy;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//
//public class TestXMLFileWriter {
    //    @Test
//    public void tryXML() {
//        MavenReportRewriter.rewriteTESTXML(new File("C:\\Users\\uex15227\\Downloads\\TEST-ch.raiffeisen.testautomation.crowdtesting.cttool.WebAppTestCases.xml"));
//    }
//
//    @Test
//    public void trySortMap() {
////        MavenReportRewriter.rewriteTESTXML(new File("C:\\Users\\uex15227\\Downloads\\TEST-ch.raiffeisen.testautomation.crowdtesting.cttool.WebAppTestCases.xml"));
//        HashMap<Float, String> sortedMap = new HashMap<>(5);
//        sortedMap.put((float) 0.59, "0.59");
//        sortedMap.put((float) 0.508, "0.508");
//        sortedMap.put((float) 7.093, "7.093");
//        sortedMap.put((float) 0.291, "0.291");
//        sortedMap.put((float) 1.144, "1.144");
//        ArrayList<Float> keyset = new ArrayList<>(sortedMap.keySet());
//        Collections.sort(keyset);
//        keyset.forEach(k -> System.out.println(sortedMap.get(k)));
//    }
//
//
//    @Test
//    public void tryMatcher() {
//        final String regex = "<response>(.*)<\\/response>";
//        String content = "<response>eb1iq4</response>";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(content);
//        while (matcher.find()) {
//            System.out.println("Full match: " + matcher.group(0));
//            for (int i = 1; i <= matcher.groupCount(); i++) {
//                System.out.println("Group " + i + ": " + matcher.group(i));
//            }
//        }
//    }

//    @FindBy(xpath = "//response")
//    private WebElement reponse;
//
//    public String getResponse() {
//        return reponse.getText();
//    }
//}
