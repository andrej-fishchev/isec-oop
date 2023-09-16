import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

// var 11
public class Main {

    public static final int KeyBitLength = 512;

    public static final RSA.Decoder DecoderInstance = RSA.getDecoder();

    public static final RSA.Encoder EncoderInstance = RSA.getEncoder();

    public static final RSAKeyPairGenerator.RSAKeyPair Keys = genKeys(KeyBitLength);

    public static final String ToFilePath = "/data/some.txt";

    public static void main(String[] args) {

        byte[] decoded = new byte[0];

        int iBlockInBytes = getBlockSizeInBytes(Keys.getModulus().bitLength());

        try(FileInputStream inputStream = new FileInputStream(getAbsolutePathToFile())) {

            int iBytes;
            int iOffset = 0;

            byte[] encoded;
            byte[] buffer = new byte[iBlockInBytes];

            while((iBytes = inputStream.readNBytes(buffer, iOffset, Math.min(iBlockInBytes, inputStream.available()))) > 0) {

                if(iBytes != iBlockInBytes)
                    buffer = Arrays.copyOf(buffer, iBlockInBytes);

                encoded = encode(buffer, EncoderInstance, Keys.getPublicKey());

                decoded = copyWithResizeCustom(decoded, decode(encoded, DecoderInstance, Keys.getPrivateKey()));

                buffer = new byte[iBlockInBytes];
            }

            System.out.println();

        } catch (IOException ignored) {        }

        System.out.println("Decoded: \n" + new String(decoded, StandardCharsets.UTF_8).replaceAll("\\x00", ""));
    }

    static byte[] copyWithResizeCustom(byte[] source, byte[] value) {

        source = Arrays.copyOf(source, source.length + value.length);

        System.arraycopy(value, 0, source, source.length - value.length, value.length);

        return source;
    }

    static String getAbsolutePathToFile() {
        return System.getProperty("user.dir") +  ToFilePath;
    }

    static int getBlockSizeInBytes(int realKeyBits) {
        return realKeyBits / Byte.SIZE;
    }

    static int getTotalBlocks(byte[] buffer, int blockSize) {
        return buffer.length / blockSize + 1;
    }

    public static byte[] encode(byte[] value, RSA.Encoder encoder, RSAPublicKey key) {

        byte[] encoded = encoder.encode(value, key);

        System.out.printf("Source: %s \nSourceInBytes: %s (total: %d) \nEncodedInBytes: %s (total: %d); \nEncoded: %s\n\n",
                new String(value, StandardCharsets.UTF_8),
                Arrays.toString(value), value.length,
                Arrays.toString(encoded), encoded.length,
                new String(encoded, StandardCharsets.UTF_8));

        return encoded;
    }

    public static byte[] decode(byte[] value, RSA.Decoder decoder, RSAPrivateKey key) {

        byte[] decoded = decoder.decode(value, key);

        System.out.printf("Source: %s \nSourceInBytes: %s (total: %d) \nDecodedInBytes: %s (total: %d); \nDecoded: %s\n\n",
                new String(value, StandardCharsets.UTF_8),
                Arrays.toString(value), value.length,
                Arrays.toString(decoded), decoded.length,
                new String(decoded, StandardCharsets.UTF_8));

        return decoded;
    }

    public static RSAKeyPairGenerator.RSAKeyPair genKeys(int keyBitLength) {
        Optional<RSAKeyPairGenerator.RSAKeyPair> buffer;
        Random rnd = new SecureRandom();

        do buffer = RSA.getRSAKeyPair(keyBitLength, 1, rnd);
        while(buffer.isEmpty());

        return buffer.get();
    }
}