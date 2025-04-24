package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.TestCaseStep;
import ch.qa.testautomation.tas.core.component.TestStepResult;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomAttachmentListDeserializer;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomResultLabelDeserializer;
import ch.qa.testautomation.tas.core.media.ImageHandler;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.*;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static java.util.Objects.isNull;

@Getter
public class JSONStepResult extends JSONContainer {
    @Setter
    private String name;
    @Setter
    private String status;
    @Setter
    private long start;
    @Setter
    private long stop;
    private List<JSONAttachment> attachments;
    private Map<String, Object> statusDetails;
    private List<JSONResultLabel> parameters = new LinkedList<>();
    private final String logFilePath;

    public JSONStepResult(TestCaseStep testCaseStep, String logFilePath) {
        TestStepResult testStepResult = testCaseStep.getTestStepResult();
        this.name = testStepResult.getName();
        this.status = testStepResult.getStatus().text();
        this.start = testStepResult.getStart();
        this.stop = testStepResult.getStop();
        this.logFilePath = logFilePath;
        attachments = new LinkedList<>();
        addParameters(testCaseStep);
        addAttachments(testCaseStep);
    }

    @JsonDeserialize(using = CustomAttachmentListDeserializer.class)
    public void setAttachments(List<JSONAttachment> attachments) {
        this.attachments = attachments;
    }

    @JsonAnySetter
    public void setStatusDetails(String key, Object value) {
        if (isNull(statusDetails)) {
            statusDetails = new LinkedHashMap<>();
        }
        statusDetails.put(key, value);
    }

    public void addParameter(String name, String value) {
        parameters.add(new JSONResultLabel(name, value));
    }

    @JsonDeserialize(using = CustomResultLabelDeserializer.class)
    public void setParameters(List<JSONResultLabel> parameters) {
        this.parameters = parameters;
    }

    @JsonIgnore
    public void setStatusDetailsMap(Map<String, Object> detailsMap) {
        statusDetails = detailsMap;
    }

    private void addParameters(TestCaseStep testCaseStep) {
        testCaseStep.getParameters().forEach((key, value) -> addParameter(key, String.valueOf(value)));
    }

    private void addAttachments(TestCaseStep testCaseStep) {
        addComment(testCaseStep.getComment());
        addLogs(testCaseStep.getTestStepResult());
        addScreenshot(testCaseStep.getTestStepResult());
        addScreenDiff(testCaseStep.getTestStepResult());
    }

    private void addComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            attachments.add(new JSONAttachment("Comment: " + comment, "text/json", ""));
        }
    }

    private void addLogs(TestStepResult stepResult) {
        String location = new File(logFilePath).getParentFile().getAbsolutePath();
        String filePath = location + "/" + stepResult.getName().replace("/", "-") + "_stepLog.txt";
        FileOperation.writeStringToFile(stepResult.getStepLogs(), new File(filePath));
        attachments.add(new JSONAttachment("Step Log", "text/plain", filePath));
        //add stack trace as attachment
        if (stepResult.getStatus().equals(TestStatus.FAIL)) {
            filePath = location + "/" + stepResult.getName().replace("/", "-") + "_failure.txt";
            FileOperation.writeStringToFile(stepResult.getTestFailure().getTrace(), new File(filePath));
            attachments.add(new JSONAttachment("Failure", "text/plain", filePath));
        }
    }

    private void addScreenshot(TestStepResult stepResult) {
        String attachType = "image/" + PropertyResolver.getScreenshotFormat().toLowerCase();
        File screenshot = stepResult.getFullScreen();
        if (Objects.nonNull(screenshot)) {
            attachments.add(new JSONAttachment("Screenshot", attachType, screenshot.getAbsolutePath()));
        }
    }

    private void addScreenDiff(TestStepResult stepResult) {
        if (isNull(stepResult.getExpectedScreen())) {
            debug("No expected screen found!");
            return;
        } else if (isNull(stepResult.getActualScreen())) {
            debug("No actual screen found!");
            return;
        }
        byte[] expected = FileOperation.readFileToByteArray(stepResult.getExpectedScreen());
        byte[] actual = FileOperation.readFileToByteArray(stepResult.getActualScreen());
        File diffImage = ImageHandler.comparePixel(stepResult.getExpectedScreen(), stepResult.getActualScreen(), stepResult.getIgnoredScreen());
        byte[] diff = FileOperation.readFileToByteArray(diffImage);
        String diffContent = new ObjectMapper().createObjectNode()
                .put("expected", "data:image/png;base64," + Base64.getEncoder().encodeToString(expected))
                .put("actual", "data:image/png;base64," + Base64.getEncoder().encodeToString(actual))
                .put("diff", "data:image/png;base64," + Base64.getEncoder().encodeToString(diff))
                .toString();
        File content = new File(diffImage.getParent() + "/diffContent.txt");
        FileOperation.writeStringToFile(diffContent, content);
        attachments.add(new JSONAttachment("Screen diff", "application/vnd.allure.image.diff", content.getAbsolutePath()));
    }
}