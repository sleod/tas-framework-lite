package testFramework.RegressionTests;

import ch.sleod.testautomation.framework.common.IOUtils.FileOperation;
import ch.sleod.testautomation.framework.common.utils.DBConnector;
import ch.sleod.testautomation.framework.common.utils.QrCodeUtils;
import ch.sleod.testautomation.framework.common.utils.WebOperationUtils;
import ch.sleod.testautomation.framework.web.ChromeDriverProvider;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import testFramework.RegressionTests.TestSettings.RegresssionTestConstants;

import java.io.*;
import java.util.*;


/**
 * @author UEX13996
 * <p>
 * Regression Testsuite of the basic framework functionalities
 */

public class RegressionTest {

    @Before
    public void initializeTestsuite() {

        //TODO any init processes needed for the test execution

    }

    /**
     * Connect and Execute SQL Statement which is defined in a .sql file.
     * Important!
     * The file must be in the TestResource folder /RegressionTestResources
     * We are using constants because of t
     */

    @Test
    public void dbConnectAndExecuteStatementFromFile() {
        log("INFO", "Start DB Test of the following DB: " + RegresssionTestConstants.DATABASE_TYPE + " - " + RegresssionTestConstants.DATABASE_NAME);

        List<Map<String, Object>> resultSet = null;
        try {
            resultSet = DBConnector.connectAndExcute(RegresssionTestConstants.DATABASE_TYPE, RegresssionTestConstants.DATABASE_HOST, RegresssionTestConstants.DATABASE_USER,
                    RegresssionTestConstants.DATABASE_PORT, RegresssionTestConstants.DATABASE_NAME, RegresssionTestConstants.DATABASE_PASSWORD, FileOperation.readFileToLinedString(RegresssionTestConstants.TEST_SQL_FILE_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("INFO", "ResultSetSize: " + resultSet.size());
        Assert.assertTrue(resultSet.size() > 0);
    }

    //DriverInitializationTests

    /**
     * Test the initialization of the Chrome WebDriver
     */

    @Test
    public void chromeDriverInitialization() {
        log("INFO", "Driver initialization Chrome");
        ChromeDriverProvider chromeDriverProvider = new ChromeDriverProvider();
        chromeDriverProvider.initialize();
        chromeDriverProvider.getDriver().get(RegresssionTestConstants.TEST_URL);
        try {
            navigateToTestpage(chromeDriverProvider.getDriver());

        } catch (AssertionError | NoSuchElementException e) {

            Assert.fail("Chromedriver initialization failed because the Assertion of an Element on " + RegresssionTestConstants.TEST_URL + " Failed");

        }finally{
            if (chromeDriverProvider != null) {
                chromeDriverProvider.close();
            }
        }
    }

    /**
     * Test the initialization of the IE WebDriver --> not possible on Pipeline!
     * Execution of this test only local poossible! If the test fails please check that
     * your IDE is running as admin
     */

//    @Test
//    public void ieDriverInitialization() {
//        log("INFO", "Driver initialization IE");
//
//        IEDriverProvider ieDriverProvider = new IEDriverProvider();
//        ieDriverProvider.initialize();
//        ieDriverProvider.getDriver().get(RegresssionTestConstants.TEST_URL);
//        try {
//            navigateToTestpage(ieDriverProvider.getDriver());
//
//        } catch (AssertionError | NoSuchElementException e) {
//
//            Assert.fail("Chromedriver initialization failed because the Assertion of an Element on " + RegresssionTestConstants.TEST_URL + " Failed");
//
//        }finally{
//            if (ieDriverProvider != null) {
//                ieDriverProvider.close();
//            }
//        }
//    }

    /**
     * Test if the QR Code utils are still working fine.
     * Create jpg from encoded base64 String + Read Qr Code from picture
     */
    @Test
    public void  QrCodeTest(){

        dbConnectAndExecuteStatementFromFile();

        String qrCodeEncoded = "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";
        QrCodeUtils.createQrCodeImageFromEncodedString("./testQR.jpg",qrCodeEncoded);
        String finalQrCode = QrCodeUtils.readQrCodeFromImage("./testQR.jpg");
        log("INFO", "QR Code: "+finalQrCode);
        Assert.assertNotNull(finalQrCode);
    }

    /**
     * Test if the JSON Object / Array Convert to String are working fine.
     */
    @Test
    public void  jsonObjectArrayToStringTest() throws IOException, ParseException {
        String json = "";
        JSONObject obj = new JSONObject();
        obj.put("name", "RCH Framework Java");
        obj.put("codelines", new Integer(28));

        JSONArray list = new JSONArray();
        list.add("test 1");
        list.add("test 2");
        list.add("test 3");

        obj.put("messages", list);

        try (FileWriter file = new FileWriter("src/test/RegressionTestResources/test.json")) {
            file.write(obj.toJSONString());
            file.flush();
        } catch (IOException e) {e.printStackTrace();}

        //Use JSONObject for simple JSON and JSONArray for array of JSON.
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader("src/test/RegressionTestResources/test.json"));//path to the JSON file.
            json = data.toJSONString();
        } catch (IOException | ParseException e) {e.printStackTrace();}
        log("INFO", "JSON Value: "+ json);
        Assert.assertEquals("{\"codelines\":28,\"name\":\"RCH Framework Java\",\"messages\":[\"test 1\",\"test 2\",\"test 3\"]}", obj.toJSONString());
        Assert.assertEquals("{\"codelines\":28,\"name\":\"RCH Framework Java\",\"messages\":[\"test 1\",\"test 2\",\"test 3\"]}", json);
    }

    //Testhelper methods
    public void navigateToTestpage(WebDriver driver) throws AssertionError, NoSuchElementException {
        WebOperationUtils.waitForPageLoad(driver);

        WebOperationUtils.waitUntilVisible(driver, driver.findElement(By.xpath(RegresssionTestConstants.BROWSER_INITIALIZATION_TEST_SELECTOR)));

        Assert.assertEquals(true, driver.findElement(By.xpath(RegresssionTestConstants.BROWSER_INITIALIZATION_TEST_SELECTOR)).isDisplayed());
    }


}
