package encryptdecrypt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Context {

    private Algorithm algorithm;
    void setAlgorithm(Algorithm alg) {
        this.algorithm = alg;
    }

    void runAlgorithm() throws IOException {
        System.out.println(this.algorithm.operateData());
    }

}

interface Algorithm {
    String operateData() throws IOException;

    String encrypt();

    String decrypt();
}

class ShiftingAlgorithm implements Algorithm {

    private final static String ALPHABET_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private final static String ALPHABET_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String mode, data, output;
    private int key;
    boolean toFile;

    public ShiftingAlgorithm(Config config) throws IOException {
        this.mode = config.mode;
        this.key = config.key;
        this.data = config.fromFile ? readFileAsString(config.in) : config.input;
        this.output = config.out;
        this.toFile = config.toFile;
    }

    @Override
    public String operateData() {
        System.out.println("Starting operate data, source - " + data);
        if (toFile) {
            saveToFile(output, mode.equals("enc") ? encrypt() : decrypt());
            return "Saved to file " + output;
        } else{
            return mode.equals("enc") ? encrypt() : decrypt();
        }
    }

    public String encrypt() {
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < data.length(); i++) {
            if (ALPHABET_LOWER.contains(data.substring(i, i + 1))) {
                int index = (ALPHABET_LOWER.indexOf(data.substring(i, i + 1)) + key) % 26;
                encrypted.append(ALPHABET_LOWER.charAt(index));
            } else if (ALPHABET_UPPER.contains(data.substring(i, i + 1))) {
                int index = (ALPHABET_UPPER.indexOf(data.substring(i, i + 1)) + key) % 26;
                encrypted.append(ALPHABET_UPPER.charAt(index));
            } else {
                encrypted.append(data.charAt(i));
            }
        }

        return new String(encrypted);
    }

    @Override
    public String decrypt() {
        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < data.length(); i++) {
            if (ALPHABET_LOWER.contains(data.substring(i, i + 1))) {
                int index = (ALPHABET_LOWER.indexOf(data.substring(i, i + 1)) - key + 26) % 26;
                decrypted.append(ALPHABET_LOWER.charAt(index));
            } else if (ALPHABET_UPPER.contains(data.substring(i, i + 1))) {
                int index = (ALPHABET_UPPER.indexOf(data.substring(i, i + 1)) - key + 26) % 26;
                decrypted.append(ALPHABET_UPPER.charAt(index));
            } else {
                decrypted.append(data.charAt(i));
            }
        }

        return new String(decrypted);
    }

    private String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private void saveToFile(String fileName, String data) {
        File file = new File(fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: can't write to file");
        }
    }
}

class UnicodeAlgorithm implements Algorithm {

    private String mode,data, output;
    private int key;
    boolean toFile;

    public UnicodeAlgorithm(Config config) throws IOException {
        this.mode = config.mode;
        this.key = config.key;
        this.output = config.out;
        this.data = config.fromFile ? readFileAsString(config.in) : config.input;
        this.toFile = config.toFile;
    }

    @Override
    public  String operateData() {
        if (toFile) {
            saveToFile(output, mode.equals("enc") ? encrypt() : decrypt());
            return "Saved to file " + output;
        } else{
            return mode.equals("enc") ? encrypt() : decrypt();
        }
    }

    @Override
    public String encrypt() {
        char[] encrypted = new char[data.length()];

        for (int i = 0; i < encrypted.length; i++) {
            encrypted[i] = (char) ((int) data.charAt(i) + key);
        }

        return new String(encrypted);
    }

    @Override
    public String decrypt() {
        char[] decrypted = new char[data.length()];

        for (int i = 0; i < decrypted.length; i++) {
            decrypted[i] = (char) ((int) data.charAt(i) - key);
        }

        return new String(decrypted);
    }

    private String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private void saveToFile(String fileName, String data) {
        File file = new File(fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: can't write to file");
        }
    }
}

class Config {
    String mode, alg, input, in, out;
    int key;
    boolean fromFile, toFile;

    Config(String[] args) {
        mode = getArg(args, "-mode", "enc");
        alg = getArg(args, "-alg", "shift");
        input = getArg(args, "-data", "");
        in = getArg(args, "-in", "");
        key = Integer.parseInt(getArg(args, "-key", "0"));
        out = getArg(args, "-out", "");
        fromFile = input.isEmpty();
        toFile = !out.isEmpty();
    }

    private String getArg(String[] args,String option, String defaultValue) {
        String arg = defaultValue;

        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) {
                arg = args[i + 1];
            }
        }

        return arg;
    }
}

public class Main {

    public static void main(String[] args) throws IOException {

        Config config = new Config(args);
        Algorithm alg = create(config);
        Context ctx = new Context();
        ctx.setAlgorithm(alg);
        ctx.runAlgorithm();

    }

    public static Algorithm create(Config config) throws IOException {
        switch (config.alg) {
            case "shift":
                return new ShiftingAlgorithm(config);
            case "unicode":
                return new UnicodeAlgorithm(config);
            default:
                throw new IllegalArgumentException("Unknown algorithm type " + config.alg);
        }
    }
}