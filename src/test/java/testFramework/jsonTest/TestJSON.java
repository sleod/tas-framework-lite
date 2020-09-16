//package testFramework.jsonTest;
//
//import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
//import ch.raiffeisen.testautomation.framework.core.json.container.JSONTestCase;
//import ch.raiffeisen.testautomation.framework.core.json.container.JSONTestResult;
//import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import net.sf.json.JSONObject;
//import org.junit.Test;
//
//import java.io.IOException;
//import java.util.List;
//
//public class TestJSON {
//    //    @Test
//    public void convertJson() {
//        String path = TestJSON.class.getClassLoader().getResource("testCases/Demo1.json").getPath();
//        JSONObject jsonObject = JSONObject.fromObject(FileOperation.readFileToLinedString(path));
//        String json = jsonObject.toString();
//        try {
//            JSONTestCase jsonTestCase = new ObjectMapper().readValue(json, JSONTestCase.class);
//            System.out.println(jsonTestCase.getDescription());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //    @Test
//    public void testResults() {
//        String jsonString = FileOperation.readFileToLinedString(FileOperation.getFilePathFromResource("ch.raiffeisen.testautomation.example-result1.json"));
//        JSONObject jsonObject = JSONObject.fromObject(jsonString);
//        try {
//            JSONTestResult jsonTestResult = new ObjectMapper().readValue(jsonObject.toString(), JSONTestResult.class);
//            jsonTestResult.getLabelsMap();
//
//            String serialized = new ObjectMapper().writeValueAsString(jsonTestResult);
//            System.out.println(serialized);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testload() throws IOException {
//        List<JSONTestResult> list = JSONContainerFactory.loadJSONAllureTestResults();
//        System.out.println(list.size());
//    }
//}
