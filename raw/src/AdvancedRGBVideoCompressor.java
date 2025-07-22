import java.io.*;
import java.util.Arrays;

public class AdvancedRGBVideoCompressor {

    static int width = 640;
    static int height = 480;
    static int fps = 30;
    static int keyframeInterval = 30;

    public static void main(String[] args) throws IOException {
        File input = new File("/home/haldun-altuntop/raw-frames/pavane.rgb");
        File output = new File("/home/haldun-altuntop/raw-frames/pavane.video");

        int frameSize = width * height * 3;
        int totalFrames = (int) (input.length() / frameSize);

        try (RandomAccessFile in = new RandomAccessFile(input, "r");
             DataOutputStream out = new DataOutputStream(new FileOutputStream(output))) {

            // Header
            out.writeInt(width);
            out.writeInt(height);
            out.writeInt(fps);
            out.writeInt(totalFrames);
            out.writeInt(keyframeInterval);

            byte[] previous = new byte[frameSize];
            byte[] current = new byte[frameSize];

            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                in.readFully(current);

                boolean isKeyframe = (frameIndex % keyframeInterval == 0);
                out.writeBoolean(isKeyframe);

                if (isKeyframe) {
                    out.write(current); // write full frame
                } else {
                    int runCount = 0;
                    ByteArrayOutputStream runBuffer = new ByteArrayOutputStream();
                    DataOutputStream runOut = new DataOutputStream(runBuffer);

                    int i = 0;
                    while (i < current.length) {
                        if (current[i] == previous[i] && current[i + 1] == previous[i + 1] && current[i + 2] == previous[i + 2]) {
                            i += 3;
                            continue;
                        }

                        int startPixel = i / 3;
                        int runLength = 1;
                        byte r = current[i], g = current[i + 1], b = current[i + 2];
                        i += 3;

                        while (i < current.length && runLength < 65535) {
                            if (current[i] == r && current[i + 1] == g && current[i + 2] == b) {
                                runLength++;
                                i += 3;
                            } else break;
                        }

                        runOut.writeInt(startPixel);
                        runOut.writeShort(runLength);
                        runOut.writeByte(r);
                        runOut.writeByte(g);
                        runOut.writeByte(b);
                        runCount++;
                    }

                    out.writeInt(runCount);
                    out.write(runBuffer.toByteArray());
                }

                previous = Arrays.copyOf(current, frameSize);
            }
        }

        System.out.println("Sıkıştırma tamamlandı.");
    }
}
