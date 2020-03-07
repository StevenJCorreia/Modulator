package com.stevenjcorreia.modulator.Utils;

import android.util.Log;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Modem {
    private static final String TAG = "Modem";
    private static int mode = -1;

    public static final int NRZ_I = 0;
    public static final int MANCHESTER_IEEE = 1;

    private static final float LOW_VOLTAGE = .2f;
    private static final float HIGH_VOLTAGE = 5f;

    public static String execute(String message, int mode) {
        Modem.mode = mode;

        final int mid = message.length() / 2;
        final String[] parts = {message.substring(0, mid), message.substring(mid)};
        final Vector<String> decodedParts = new Vector<>();

        for (final String part: parts) {
            Thread modemThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Text -> ASCII -> binary
                    int[] bytes = AsciiStringToBinaryArray(part);

                    // Modulate voltages
                    float[][] voltages = modulate(bytes);

                    // Demodulate voltages
                    bytes = demodulate(voltages);

                    // Binary -> ASCII -> text
                    decodedParts.add(binaryArrayToAsciiString(bytes));
                }
            });

            modemThread.start();
            try { modemThread.join(); } catch (InterruptedException e) { Log.e(TAG, e.toString()); }
        }

        // Re-append elements together
        StringBuilder output = new StringBuilder();
        for (String part: decodedParts) {
            output.append(part);
        }

        return output.toString();
    }

    private static float[][] modulate(int[] bytes) {
        float[][] output = new float[bytes.length][];

        // Initialize sub-arrays
        for (int i = 0; i < bytes.length; i++) {
            output[i] = new float[String.valueOf(bytes[i]).length()];
        }

        switch (mode) {
            case NRZ_I:
                for (int i = 0; i < output.length; i++) { // Each character as binary string
                    for (int j = 0; j < String.valueOf(bytes[i]).length(); j++) { // Each character's binary string char-element
                        if (j == 0) {
                            // If first bit, set starting voltage to appropriate voltage (0 = high, 1 = low)
                            output[i][j] = String.valueOf(bytes[i]).charAt(j) == '0' ? HIGH_VOLTAGE : LOW_VOLTAGE;
                            Log.d(TAG, String.format(Locale.US, "bit %s receives voltage of %f.", String.valueOf(bytes[i]).charAt(j), output[i][j]));
                            continue;
                        }

                        // Swapping voltages if bit is 1
                        output[i][j] = String.valueOf(bytes[i]).charAt(j) == '0' ? output[i][j - 1] : swapVoltage(output[i][j - 1]);
                        Log.d(TAG, String.format(Locale.US, "bit %s receives voltage of %f.", String.valueOf(bytes[i]).charAt(j), output[i][j]));
                    }
                }
                break;
            case MANCHESTER_IEEE:
                for (int i = 0; i < output.length; i++) { // Each character as binary string
                    int expectedBits = String.valueOf(bytes[i]).length();
                    int receivedBits = 0;
                    while (receivedBits < expectedBits) {
                        output[i][receivedBits] = String.valueOf(bytes[i]).charAt(receivedBits) == '1' ? HIGH_VOLTAGE : LOW_VOLTAGE;
                        Log.d(TAG, String.format(Locale.US, "bit %s receives voltage of %f.", String.valueOf(bytes[i]).charAt(receivedBits), output[i][receivedBits]));

                        receivedBits++;
                    }
                }
                break;
        }

        return output;
    }

    private static int[] demodulate(float[][] voltages) {
        int[] output = new int[voltages.length];

        switch (mode) {
            case NRZ_I:
                // For every voltage in every sub-array, append 0 or 1 to string, convert to int, add to respective index
                for (int i = 0; i < voltages.length; i++) {
                    StringBuilder byteValue = new StringBuilder();
                    for (int j = 0; j < voltages[i].length; j++) {
                        if (j == 0) {
                            // If first voltage, set starting bit to appropriate voltage (high = 0, low = 1)
                            byteValue.append(Float.compare(voltages[i][j], HIGH_VOLTAGE) == 0 ? 0 : 1);
                            Log.d(TAG, String.format(Locale.US, "voltage of %f receives bit %s.", voltages[i][j], byteValue));
                            continue;
                        }

                        // Setting bit to 1 if current and previous voltages are different
                        byteValue.append(Float.compare(voltages[i][j], voltages[i][j - 1]) == 0 ? 0 : 1);
                        Log.d(TAG, String.format(Locale.US, "voltage of %f receives bit %s.", voltages[i][j], byteValue.charAt(j)));
                    }

                    output[i] = Integer.parseInt(byteValue.toString());
                }
                break;
            case MANCHESTER_IEEE:
                for (int i = 0; i < output.length; i++) { // Each character as binary string
                    StringBuilder byteValue = new StringBuilder();
                    int expectedBits = voltages[i].length;
                    int receivedBits = 0;
                    while (receivedBits < expectedBits) {
                        byteValue.append(Float.compare(voltages[i][receivedBits], HIGH_VOLTAGE) == 0 ? 1 : 0);
                        Log.d(TAG, String.format(Locale.US, "voltage of %f receives bit %s.", voltages[i][receivedBits], byteValue.charAt(receivedBits)));

                        receivedBits++;
                    }

                    output[i] = Integer.parseInt(byteValue.toString());
                }
                break;
        }

        return output;
    }

    private static int[] AsciiStringToBinaryArray(String text) {
        int[] output = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            output[i] = text.charAt(i);
        }

        for (int i = 0; i < output.length; i++) {
            output[i] = Integer.parseInt(setParityBit(Integer.toString(output[i], 2)));
        }

        return output;
    }

    private static String binaryArrayToAsciiString(int[] bytes) {
        char[] ASCII = new char[bytes.length];

        for (int i = 0; i < ASCII.length; i++) {
            ASCII[i] = (char) Integer.parseInt(stripParityBit(String.valueOf(bytes[i])), 2);
        }

        return String.valueOf(ASCII);
    }

    private static String setParityBit(String byteValue) {
        int onBitCount = 0;
        for (int i = 0; i < byteValue.length(); i++) {
            if (byteValue.charAt(i) == '1') {
                onBitCount++;
            }
        }

        return byteValue.concat((onBitCount & 1) == 0 ? "0" : "1");
    }

    private static String stripParityBit(String byteValue) {
        return byteValue.substring(0, byteValue.length() - 1);
    }

    private static float swapVoltage(float voltage) {
        return Float.compare(voltage, LOW_VOLTAGE) == 0 ? HIGH_VOLTAGE : LOW_VOLTAGE;
    }
}
