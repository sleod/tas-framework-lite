//package testFramework.testQCConnection;
//
//import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
//import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
//import ch.raiffeisen.testautomation.framework.rest.RestfulDriver;
//import org.glassfish.jersey.client.JerseyInvocation;
//import org.junit.Test;
//
//import javax.ws.rs.core.Response;
//
//import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.log;
//
//public class TestQCRest {
//
//    private String qcUser = System.getProperty("user.name");
//    private String qcPassword = "cHc0UmFBMTAwKw==";
//    private String qcHost = "http://vaafist916.ad.raiffeisen.ch:8080/qcbin";
//    //    private String mainPath = "ch/raiffeisen/testautomation/framework/rest/";
//    private String mainPath = "api/";
//    private String qcDomain = "DEFAULT";
//    private String qcProject = "TEST_Automation_S2_2018";
//    private String qcPlanRootFolder = "Subject/Trash";
//    private String qcLabRootFolder = "Root/Automation Framework TEST";
//    private boolean qcVersionControlEnabled = false;
//
//    @Test
//    public void trySignIn() {
//        RestfulDriver driver = new RestfulDriver(qcHost, qcUser, PropertyResolver.decodeBase64(qcPassword));
//        driver.initialize();
//        driver.connect();
//        String encoding = PropertyResolver.encodeBase64(qcUser + ":" + PropertyResolver.decodeBase64(qcPassword));
//        Response response = driver.getRequester().path("api/authentication/sign-in").request().header("Authorization ", "Basic " + encoding).post(null);
//        printResponse(response);
//        response = driver.getRequester().path("api/authentication/sign-out").request().header("Authorization ", "Basic " + encoding).get();
//        printResponse(response);
//
//    }
//
//    public void printResponse(Response response) {
//        log("INFO", "\n============getResponse============");
//        log("INFO", String.valueOf(response.getStatus()));
//        response.getCookies().forEach((key, value) -> {
//            SystemLogger.info("Cookie: " + key + "->" + value);
//        });
//        String entry = response.readEntity(String.class);
//        log("INFO", entry);
//    }
//
//}
