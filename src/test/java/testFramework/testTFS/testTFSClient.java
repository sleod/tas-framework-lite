//package testFramework.testTFS;
//
//import ch.raiffeisen.testautomation.framework.common.IOUtils.FileLocator;
//import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
//import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
//import ch.raiffeisen.testautomation.framework.core.json.container.JSONRunnerConfig;
//import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
//import ch.raiffeisen.testautomation.framework.rest.TFS.connection.TFSConnector;
//import ch.raiffeisen.testautomation.framework.rest.TFS.connection.TFSRestClient;
//import org.junit.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Scanner;
//
//import org.apache.commons.codec.binary.Base64;
//
//
//public class testTFSClient {
//
//    private JSONRunnerConfig runnerConfig;
//
//    @Test
//    public void testRest() throws IOException {
//        runnerConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getTFSRunnerConfigFile());
//        TFSConnector tfsConnector = new TFSConnector(runnerConfig.getTfsConfig());
//        TFSRestClient tfsRestClient = new TFSRestClient(tfsConnector, runnerConfig.getTfsConfig());
//        File target = new File(FileLocator.findResource(PropertyResolver.getDefaultWebDriverBinLocation()).toString() + "/drivers.zip");
//        SystemLogger.warn("Write to target: "+ target.getAbsolutePath());
//        tfsRestClient.downloadFilesAsZip("Git%20-%20RCH%20Framework%20Solution%20Items/Java/ChromeDriverVersions", target);
////        Response response = tfsRestClient.createTestRun("new Test Run with REST", "436601", "471238", asList("554491", "556461"));
////        System.out.println(response.getStatus());
////        System.out.println(response.getStatusInfo());
////        System.out.println(response.readEntity(String.class));
//    }
//
//    @Test
//    public void testConnect() {
//        try {
////            String payload = "{\"name\":\"new Test Run with REST 1\",\"plan\":{\"id\":\"436601\"},\"pointIds\":[318043,318043]}";
//            String AuthStr = ":" + "vebyjjig3v7txr7eiq3he3aqbyvilqqllmwdf5d56fz64um5axua";
//            Base64 base64 = new Base64();
//            String encodedPAT = new String(base64.encode(AuthStr.getBytes()));
////            URL url = new URL("https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/AP.Testtools/_apis/testplan/Plans/436601/Suites/471238/TestPoint?testCaseId=554491");
////            URL url = new URL("https://tfs-prod.service.raiffeisen.ch:8081//tfs/RCH/AP.Testtools/_apis/test/runs");
//            URL url = new URL("https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/ap.testtools/_apis/tfvc/items?path=Git%20-%20RCH%20Framework%20Solution%20Items/Java/ChromeDriverVersions");
//
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setRequestProperty("Authorization", "Basic " + encodedPAT);
//            con.setRequestProperty("Accept", "application/zip");
//
//            System.out.println("URL - " + url.toString());
//            System.out.println("PAT - " + encodedPAT);
//            con.setRequestMethod("GET");
////            con.setRequestMethod("POST");
//            con.setDoOutput(true);
////            try (OutputStream os = con.getOutputStream()) {
////                byte[] input = payload.getBytes("utf-8");
////                os.write(input, 0, input.length);
////            }
//
//            int status = con.getResponseCode();
//            if (status == 200) {
//                String responseBody;
//                try (Scanner scanner = new Scanner(con.getInputStream())) {
//                    responseBody = scanner.useDelimiter("\\A").next();
//                    System.out.println(responseBody);
//                }
//            }
//            con.disconnect();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
