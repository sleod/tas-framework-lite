package testFramework.RegressionTests.TestSettings;

/**
 * @author UEX13996
 *
 * Constants which will be used in the regression test suite of the Framework
 *
 */

public class RegresssionTestConstants {

    public RegresssionTestConstants(){
        //empty constructor to avoid instances of this constant class
    }

    //Database constants
    public final static String DATABASE_TYPE = "mssql";
    public final static String DATABASE_HOST = "mssql-pfc-mada-p.service.raiffeisen.ch";
    public final static String DATABASE_USER = "UT_PFCP02_P";
    public final static String DATABASE_PORT = "14331";
    public final static String DATABASE_NAME = "pj_ebanking";
    public final static String DATABASE_PASSWORD = "rS9(ze)0ZPjx2!V?=Dn";
    public final static String TEST_SQL_FILE_PATH = "src/test/RegressionTestResources/testSQL.sql";

    //WebDriverTest constants
    public final static String TEST_URL = "https://www.raiffeisen.ch";
    public final static String BROWSER_INITIALIZATION_TEST_SELECTOR = "//*[@id='searchButton']";





}
