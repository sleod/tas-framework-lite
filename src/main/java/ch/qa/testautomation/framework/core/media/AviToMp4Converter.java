package ch.qa.testautomation.framework.core.media;

import java.io.File;
import java.util.Date;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;

public class AviToMp4Converter {
    public void AviToMp4(String oldPath, String newPath) {
        File source = new File(oldPath);
        File target = new File(newPath);
        SystemLogger.info("pre-conversion path:" + oldPath);
        SystemLogger.info("converted path:" + newPath);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame"); // audio coding format
        audio.setBitRate(800000);
        audio.setChannels(1);
        audio.setSamplingRate(22050);
        VideoAttributes video = new VideoAttributes();
        video.setCodec("libx264"); // video encoding format
        video.setBitRate(3200000);
        video.setFrameRate(15); // small digital set, the video will Caton
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setInputFormat("avi");
        attrs.setOutputFormat("mp4");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder();
        MultimediaObject multimediaObject = new MultimediaObject(source);
        try {
            SystemLogger.info("avi conversion start switch MP4 ---:" + new Date());
            encoder.encode(multimediaObject, target, attrs);
            SystemLogger.info("avi switch MP4 --- End Conversion:" + new Date());
        } catch (IllegalArgumentException | EncoderException e) {
            e.printStackTrace();
        }
    }
}
