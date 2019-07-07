import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import org.bytedeco.javacv.*;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class MixAudio {

    private FrameGrabber frameGrabberVideo;
    private FrameGrabber frameGrabberMute;
    private FrameRecorder frameRecorder;

    public void DecodeAudio(){


        frameGrabberVideo = new FFmpegFrameGrabber("video.mp4");
        frameGrabberMute = new FFmpegFrameGrabber("mute.mp3");
        File file = new File("sutu.mp3");

        if (frameGrabberVideo == null){
            System.out.println("Null");
        }
        try {
            frameGrabberVideo.start();
            frameGrabberMute.start();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioInputStream inputStream = null;
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    frameGrabberVideo.getSampleRate(),16,
                    frameGrabberVideo.getAudioChannels(),frameGrabberVideo.getAudioChannels()*2,
                    frameGrabberVideo.getSampleRate(),false);
            inputStream = AudioSystem.getAudioInputStream(audioFormat,audioInputStream);

            String output = "output.mp4";
            frameRecorder = new FFmpegFrameRecorder(output,frameGrabberVideo.getImageWidth(),
                    frameGrabberVideo.getImageHeight(),frameGrabberVideo.getAudioChannels());
            frameRecorder.setAudioBitrate(frameGrabberVideo.getAudioBitrate());
            frameRecorder.setFrameRate(frameGrabberVideo.getFrameRate());
            frameRecorder.setSampleRate(frameGrabberVideo.getSampleRate());
            frameRecorder.setVideoQuality(1);


            frameRecorder.start();
            Frame frame =  frameGrabberVideo.grabFrame();
            System.out.println(frame.samples);
            while (frame != null){
                if (frame.image != null){
                    frame.samples = null;
                    System.out.println("Video");
                    frameRecorder.record(frame);
                }
                frame =  frameGrabberVideo.grabFrame();
            }
            frameGrabberVideo.stop();

            Frame frameMute = frameGrabberMute.grabFrame();

            Integer audioBuffSize = frameGrabberVideo.getAudioChannels()*frameGrabberVideo.getSampleRate();
            byte[] audioBytes = new byte[audioBuffSize];

            Integer nBytesRead = 0;
            while (nBytesRead!=-1){
                nBytesRead = inputStream.read(audioBytes);
                if (nBytesRead != -1){
                    int nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                    Frame frame1 = new Frame();
                    frame1.samples = sBuff;
                }
            }

            frameRecorder.stop();
            frameRecorder.release();
            frameGrabberAudio.stop();

        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }
}

class Main{
    public static void main(String[] args) {
        MixAudio mixAudio = new MixAudio();
        mixAudio.DecodeAudio();
    }
}
