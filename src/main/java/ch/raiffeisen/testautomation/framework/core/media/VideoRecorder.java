package ch.raiffeisen.testautomation.framework.core.media;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder {
    private DesiredScreenRecorder screenRecorder;

//Example:
//    public static void main(String[] args) throws Exception {
//
//        VideoRecorder  videoRecorder = new VideoRecorder();
//        videoRecorder.startRecording(new File("D:\\Videos"), 3440, 1440, "myVideo");
//        System.setProperty("webdriver.chrome.driver", "D:\\IdeaProjects\\AUT_eBanking_Java\\src\\main\\resources\\webDrivers\\ChromeDriverVersions\\chromedriver_880.exe");
//        ChromeDriver driver = new ChromeDriver();
//        driver.get("http://www.google.com");
//
//        WebElement element = driver.findElement(By.name("q"));
//        element.sendKeys("testing");
//        element.submit();
//        System.out.println("Page title is: " + driver.getTitle());
//        driver.quit();
//        videoRecorder.stopRecording();
//    }


    public void startRecording(File file, int width, int height, String name) throws Exception {
        Rectangle captureSize = new Rectangle(0, 0, width, height);

        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        this.screenRecorder = new DesiredScreenRecorder(gc, captureSize,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                        QualityKey, 1.0f,
                        KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                        FrameRateKey, Rational.valueOf(30)),
                null, file, name);
        this.screenRecorder.start();

    }

    public void stopRecording() throws Exception {
        this.screenRecorder.stop();
    }
}

class DesiredScreenRecorder extends ScreenRecorder {
    private String name;

    public DesiredScreenRecorder(GraphicsConfiguration cfg,
                                 Rectangle captureArea, Format fileFormat, Format screenFormat,
                                 Format mouseFormat, Format audioFormat, File movieFolder,
                                 String name) throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat,
                audioFormat, movieFolder);
        this.name = name;
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }
        return new File(movieFolder, name + "." + Registry.getInstance().getExtension(fileFormat));
    }
}
