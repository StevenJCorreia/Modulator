package com.stevenjcorreia.modulator.Utils;

import android.util.Log;
import java.util.Locale;

/*
* This module converts characters to binary using UTF-8.
* */
// TODO - Add delimiters & parity bit to Manchester
public class Modem {
    private static final String TAG = "Modem";

    public static final int NRZ_I = 0;
    public static final int MANCHESTER_IEEE = 1;

    private static final float LOW_VOLTAGE = .2f;
    private static final float HIGH_VOLTAGE = 5f;

    public static String execute(String text, int mode) {
        // Text -> ASCII -> binary
        int[] bytes = AsciiStringToBinaryArray(text);

        // Modulate voltages
        float[][] voltages = Modulate(bytes, mode);

        // Demodulate voltages
        bytes = demodulate(voltages);

        // Binary to ASCII -> text
        return binaryArrayToAsciiString(bytes);
    }

    private static float[][] Modulate(int[] bytes, int mode) {
        float[][] output = new float[bytes.length][];
        float startingVoltage;

        // Initialize sub-arrays
        for (int i = 0; i < bytes.length; i++) {
            output[i] = new float[String.valueOf(bytes[i]).length()];
        }

        switch (mode) {
            case NRZ_I:
                for (int i = 0; i < output.length; i++) { // Each character's binary string
                    for (int j = 0; j < String.valueOf(bytes[i]).length(); j++) { // Each character's binary string char-element
                        if (j == 0) {
                            // If first bit, set starting voltage to appropriate voltage (0 = high, 1 = low)
                            output[i][j] = startingVoltage = String.valueOf(bytes[i]).charAt(j) == '0' ? HIGH_VOLTAGE : LOW_VOLTAGE;
                            Log.d(TAG, String.format(Locale.US, "bit %s receives voltage of %f.", String.valueOf(bytes[i]).charAt(j), startingVoltage));
                            continue;
                        }

                        // Swapping voltages if bit is 1
                        output[i][j] = String.valueOf(bytes[i]).charAt(j) == '0' ? output[i][j - 1] : swapVoltage(output[i][j - 1]);
                        Log.d(TAG, String.format(Locale.US, "bit %s receives voltage of %f.", String.valueOf(bytes[i]).charAt(j), output[i][j]));
                    }
                }
                break;
            case MANCHESTER_IEEE:
                // ...
                break;
        }

        return output;
    }

    private static int[] demodulate(float[][] voltages) {
        int[] output = new int[voltages.length];
        int startingBit;

        // For every voltage in every sub-array, append 0 or 1 to string, convert to int, add to respective index
        for (int i = 0; i < voltages.length; i++) {
            StringBuilder byteValue = new StringBuilder();
            for (int j = 0; j < voltages[i].length; j++) {
                if (j == 0) {
                    // If first voltage, set starting bit to appropriate voltage (high = 0, low = 1)
                    startingBit = Float.compare(voltages[i][j], HIGH_VOLTAGE) == 0 ? 0 : 1;
                    byteValue.append(startingBit);
                    Log.d(TAG, String.format(Locale.US, "voltage of %f receives bit %s.", voltages[i][j], startingBit));
                    continue;
                }

                // Setting bit to 1 if current and previous voltages are different
                byteValue.append(Float.compare(voltages[i][j], voltages[i][j - 1]) == 0 ? 0 : 1);
                Log.d(TAG, String.format(Locale.US, "voltage of %f receives bit %s.", voltages[i][j], output[i]));
            }

            output[i] = Integer.parseInt(byteValue.toString());
        }

        return output;
    }

    private static int[] AsciiStringToBinaryArray(String text) {
        int[] output = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            output[i] = text.charAt(i);
        }

        for (int i = 0; i < output.length; i++) {
            output[i] = Integer.parseInt(Integer.toString(output[i], 2));
        }

        return output;
    }

    private static String binaryArrayToAsciiString(int[] bytes) {
        char[] ASCII = new char[bytes.length];
        for (int i = 0; i < ASCII.length; i++) {
            ASCII[i] = (char) Integer.parseInt(String.valueOf(bytes[i]), 2);
        }

        return String.valueOf(ASCII);
    }

    private static float swapVoltage(float voltage) {
        return Float.compare(voltage, LOW_VOLTAGE) == 0 ? HIGH_VOLTAGE : LOW_VOLTAGE;
    }
}
