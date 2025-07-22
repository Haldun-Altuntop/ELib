public class BilinearUpscale {
    public static byte[] upscale(byte[] src, int width, int height, int scale) {
        int newWidth = width * scale;
        int newHeight = height * scale;
        byte[] dst = new byte[newWidth * newHeight * 3];

        for (int y = 0; y < newHeight; y++) {
            float gy = (float)(y) / scale;
            int y0 = (int)Math.floor(gy);
            int y1 = Math.min(y0 + 1, height - 1);
            float dy = gy - y0;

            for (int x = 0; x < newWidth; x++) {
                float gx = (float)(x) / scale;
                int x0 = (int)Math.floor(gx);
                int x1 = Math.min(x0 + 1, width - 1);
                float dx = gx - x0;

                int i00 = (y0 * width + x0) * 3;
                int i10 = (y0 * width + x1) * 3;
                int i01 = (y1 * width + x0) * 3;
                int i11 = (y1 * width + x1) * 3;

                for (int c = 0; c < 3; c++) { // R, G, B
                    int p00 = src[i00 + c] & 0xFF;
                    int p10 = src[i10 + c] & 0xFF;
                    int p01 = src[i01 + c] & 0xFF;
                    int p11 = src[i11 + c] & 0xFF;

                    float val =
                            (1 - dx) * (1 - dy) * p00 +
                                    dx * (1 - dy) * p10 +
                                    (1 - dx) * dy * p01 +
                                    dx * dy * p11;

                    dst[(y * newWidth + x) * 3 + c] = (byte)(int)(val + 0.5f);
                }
            }
        }

        return dst;
    }
}
