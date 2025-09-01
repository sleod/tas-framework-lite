package io.github.sleod.tas.rest.connection.hpqc;


public final class QCConstants {

    //Constance of Container Path
    public static final String TEST_CASE_CONTAINER = "tests";
    public static final String TEST_CASE_CONFIG_CONTAINER = "test-configs";
    public static final String TESTPLAN_FOLDER_CONTAINER = "test-folders";
    public static final String TESTLAB_RUN_CONTAINER = "runs";
    public static final String TESTLAB_RUN_STEPS_CONTAINER = "run-steps";
    public static final String TESTLAB_TESTSET_CONTAINER = "test-sets";
    public static final String TESTLAB_FOLDER_CONTAINER = "test-set-folders";
    public static final String TESTLAB_INSTANCE_CONTAINER = "test-instances";
    public static final String REQUIREDMENTS_CONTAINER = "requirements";
    public static final String DEFECTS_CONTAINER = "defects";
    public static final String DEFECTS_LINK_CONTAINER = "defect-links";
    public static final String ATTACHMENT_CONTAINER = "attachments";
    public static final String DESIGN_STEPS_CONTAINER = "design-steps";
    public static final String REQUIREMENT_COVERAGES_CONTAINER = "requirement-coverages";

    //Enities
    public static final String ENTITY_TEST_CASE = "test";
    public static final String ENTITY_TESTPLAN_FOLDER = "test-folder";
    public static final String ENTITY_RUN = "run";
    public static final String ENTITY_RUN_STEP = "run-step";
    public static final String ENTITY_TESTSET = "test-set";
    public static final String ENTITY_TESTLAB_FOLDER = "test-set-folder";
    public static final String ENTITY_INSTANCE = "test-instance";
    public static final String ENTITY_REQUIREDMENT = "requirement";
    public static final String ENTITY_DEFECT = "defect";
    public static final String ENTITY_DEFECTS_LINK = "defect-link";
    public static final String ENTITY_ATTACHMENT = "attachment";
    public static final String ENTITY_DESIGN_STEP = "design-step";
    public static final String ENTITY_COVERAGE = "requirement-coverage";

    //Enity Type
    public static final int ENTITY_TYPE_Entities = 0;
    public static final int ENTITY_TYPE_DEFECT = 1;
    public static final int ENTITY_TYPE_TEST_CASE = 2;
    public static final int ENTITY_TYPE_TESTPLAN_FOLDER = 3;
    public static final int ENTITY_TYPE_RUN = 4;
    public static final int ENTITY_TYPE_TESTSET = 5;
    public static final int ENTITY_TYPE_TESTLAB_FOLDER = 6;
    public static final int ENTITY_TYPE_INSTANCE = 7;
    public static final int ENTITY_TYPE_REQUIREMENT = 8;
    public static final int ENTITY_TYPE_ATTACHMENT = 9;
    public static final int ENTITY_TYPE_COVERAGE = 10;
    public static final int ENTITY_TYPE_DESIGN_STEP = 11;
    public static final int ENTITY_TYPE_RUN_STEP = 12;

    //Constance of REST Return Code and Cause
    public static final int SUCCESSFUL_OPERATIONS = 200;
    public static final int SUCCESSFUL_POST_OPERATIONS = 201;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHENTICATED_REQUEST = 401;
    public static final int UNAUTHORIZED_OPERATIONS = 403;
    public static final int RESOURCE_NOT_FOUND = 404;
    public static final int METHOD_NOT_SUPPORTED_BY_RESOURCE = 405;
    public static final int UNSUPPORTED_ACCEPT_TYPE = 406;
    public static final int UNSUPPORTED_REQUEST_CONTENT_TYPE = 415;
    public static final int INTERNAL_SERVER_ERROR = 500;

    //Constance of Entity Template
    public static final String ENTITY_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Entity></Entity>";

    //Actions
    public static final String CHECKIN = "check-in";
    public static final String CHECKOUT = "check-out";
    public static final String LOCK = "lock";
    public static final String UNLOCK = "unlock";

    //Reference Keys
    public static final String PARAM_TEST_CASE_ID = "test-id";
    public static final String PARAM_INSTANCE_ID_IN_TEST_CASE = "cycle-id";
    public static final String PARAM_LAST_MODIFIED = "last-modified";
    public static final String PARAM_INSTANCE_ORDER = "order-id";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_TEST_CASE_STATUS = "exec-status";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PARENT_ID = "parent-id";
    public static final String PARAM_INSTANCE_TESTER = "actual-tester";
    public static final String TAG_ENTITY = "Entity";
    public static final String TAG_ENTITIES = "Entities";
    public static final String TAG_TOTALRESULTS = "TotalResults";
    public static final String TAG_FIELD = "Field";
    public static final String TAG_NAME = "Name";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_EXPECTED = "expected";
    public static final String PARAM_STEP_ORDER = "step-order";
    public static final String PARAM_INSTANCE_ID_IN_RUN = "testcycl-id";
    public static final String PARAM_DESIGNSTEP_ID_IN_RUNSTEP = "desstep-id";
    public static final String PARAM_ACTUAL = "actual";


    public static String getContainerName(int entityType) {
        String container = "";
        switch (entityType) {
            case ENTITY_TYPE_DEFECT://defect
                container = DEFECTS_CONTAINER;
                break;
            case ENTITY_TYPE_TEST_CASE://testcase
                container = TEST_CASE_CONTAINER;
                break;
            case ENTITY_TYPE_TESTPLAN_FOLDER://testplan folder
                container = TESTPLAN_FOLDER_CONTAINER;
                break;
            case ENTITY_TYPE_RUN://runs
                container = TESTLAB_RUN_CONTAINER;
                break;
            case ENTITY_TYPE_TESTSET://testsets
                container = TESTLAB_TESTSET_CONTAINER;
                break;
            case ENTITY_TYPE_TESTLAB_FOLDER://test lab Folder
                container = TESTLAB_FOLDER_CONTAINER;
                break;
            case ENTITY_TYPE_INSTANCE://test instance
                container = TESTLAB_INSTANCE_CONTAINER;
                break;
            case ENTITY_TYPE_REQUIREMENT://requirement
                container = REQUIREDMENTS_CONTAINER;
                break;
            case ENTITY_TYPE_DESIGN_STEP://design-step
                container = DESIGN_STEPS_CONTAINER;
                break;
            case ENTITY_TYPE_COVERAGE://design-step
                container = REQUIREMENT_COVERAGES_CONTAINER;
                break;
            case ENTITY_TYPE_RUN_STEP://run-step
                container = TESTLAB_RUN_STEPS_CONTAINER;
                break;
            case ENTITY_TYPE_ATTACHMENT://attachment
                container = ATTACHMENT_CONTAINER;
                break;
        }
        return container;
    }

    public static String getEntityName(int entityType) {
        String name = "";
        switch (entityType) {
            case ENTITY_TYPE_DEFECT://defects
                name = ENTITY_DEFECT;
                break;
            case ENTITY_TYPE_TEST_CASE://tests
                name = ENTITY_TEST_CASE;
                break;
            case ENTITY_TYPE_TESTPLAN_FOLDER://test-folder
                name = ENTITY_TESTPLAN_FOLDER;
                break;
            case ENTITY_TYPE_RUN://runs
                name = ENTITY_RUN;
                break;
            case ENTITY_TYPE_TESTSET://testsets
                name = ENTITY_TESTSET;
                break;
            case ENTITY_TYPE_TESTLAB_FOLDER://testset Folder
                name = ENTITY_TESTLAB_FOLDER;
                break;
            case ENTITY_TYPE_INSTANCE://instance
                name = ENTITY_INSTANCE;
                break;
            case ENTITY_TYPE_REQUIREMENT://requirement
                name = ENTITY_REQUIREDMENT;
                break;
            case ENTITY_TYPE_ATTACHMENT://attachment
                name = ENTITY_ATTACHMENT;
                break;
            case ENTITY_TYPE_DESIGN_STEP://design-step
                name = ENTITY_DESIGN_STEP;
                break;
            case ENTITY_TYPE_COVERAGE://design-step
                name = ENTITY_COVERAGE;
                break;
            case ENTITY_TYPE_RUN_STEP://run-step
                name = ENTITY_RUN_STEP;
                break;
        }
        return name;
    }

    public static String getReturnStatus(int code) {
        String cause = "";
        switch (code) {
            case SUCCESSFUL_OPERATIONS://200
                cause = "SUCCESSFUL_OPERATIONS";
                break;
            case SUCCESSFUL_POST_OPERATIONS://201
                cause = "SUCCESSFUL_POST_OPERATIONS";
                break;
            case UNAUTHENTICATED_REQUEST://401
                cause = "UNAUTHENTICATED_REQUEST";
                break;
            case UNAUTHORIZED_OPERATIONS://403
                cause = "UNAUTHORIZED_OPERATIONS";
                break;
            case RESOURCE_NOT_FOUND://404
                cause = "RESOURCE_NOT_FOUND";
                break;
            case METHOD_NOT_SUPPORTED_BY_RESOURCE://405
                cause = "METHOD_NOT_SUPPORTED_BY_RESOURCE";
                break;
            case UNSUPPORTED_ACCEPT_TYPE://406
                cause = "UNSUPPORTED_ACCEPT_TYPE";
                break;
            case UNSUPPORTED_REQUEST_CONTENT_TYPE://415
                cause = "UNSUPPORTED_REQUEST_CONTENT_TYPE";
                break;
            case BAD_REQUEST://400
                cause = "BAD_REQUEST";
                break;
            case INTERNAL_SERVER_ERROR://500
                cause = "INTERNAL_SERVER_ERROR";
                break;
        }
        return code + " : " + cause;
    }

    public static int getEntityType(String name) {
        int type = 0;
        switch (name) {
            case ENTITY_TEST_CASE:
                type = ENTITY_TYPE_TEST_CASE;
                break;
            case ENTITY_TESTPLAN_FOLDER:
                type = ENTITY_TYPE_TESTPLAN_FOLDER;
                break;
            case ENTITY_RUN:
                type = ENTITY_TYPE_RUN;
                break;
            case ENTITY_TESTSET:
                type = ENTITY_TYPE_TESTSET;
                break;
            case ENTITY_TESTLAB_FOLDER:
                type = ENTITY_TYPE_TESTLAB_FOLDER;
                break;
            case ENTITY_INSTANCE:
                type = ENTITY_TYPE_INSTANCE;
                break;
            case ENTITY_REQUIREDMENT:
                type = ENTITY_TYPE_REQUIREMENT;
                break;
            case ENTITY_DEFECT:
                type = ENTITY_TYPE_DEFECT;
                break;
            case ENTITY_ATTACHMENT:
                type = ENTITY_TYPE_ATTACHMENT;
                break;
            case ENTITY_DESIGN_STEP:
                type = ENTITY_TYPE_DESIGN_STEP;
                break;
            case ENTITY_COVERAGE:
                type = ENTITY_TYPE_COVERAGE;
                break;
            case ENTITY_RUN_STEP://run-step
                type = ENTITY_TYPE_RUN_STEP;
                break;
        }
        return type;
    }

    public static String getEntityTypeString(String name) {
        int type = getEntityType(name);
        return String.valueOf(type);
    }
}
