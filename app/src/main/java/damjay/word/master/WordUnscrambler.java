package damjay.word.master;

import android.content.res.AssetManager;
import damjay.utils.ZipUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;

public class WordUnscrambler {
    public static final String WORDS = "Words";
    
    public static final int SMALL_DB = 2;
    public static final int MEDIUM_DB = 1;
    public static final int LARGE_DB = 0;
        
    public static String text;
    public static int limit = -1;
    public static HashSet<String> dict = new HashSet<>();
    private static boolean initDict = false;
    public static ArrayList<String>[] scramblerStorage;
    private static File wordDir;
    
    private static final String[] wordDatabases = {"WordDB", "WordsDB", "WordsDatabase"};
    private static int databaseSelector = 2;

    public static boolean unzipData(File fileDir, AssetManager assets, String wordsArchive) {
        File wordArchive = new File(fileDir, wordsArchive);
        if (wordArchive.exists()) {
            wordDir = new File(fileDir, WORDS);
            return true;
        }
    	try {
            InputStream wordArchiveStream = assets.open(wordsArchive);
            if (!ZipUtils.copyStream(wordArchiveStream, new FileOutputStream(wordArchive))) return false;
            System.out.println("Extracting");
            ZipUtils.extractZip(wordArchive, (wordDir = new File(fileDir, WORDS)));
            System.out.println("Extracted Successfully");
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }

    public static HashSet<String> getResults(String text, int limit, int database) {
        if (!checkArguments(text, limit, database)) return null;
        
        long startTime = System.currentTimeMillis();
        text = text.toLowerCase();
        WordUnscrambler.text = text;
        WordUnscrambler.limit = limit;
        initDict = true;

        initScramblerStorage(text, limit);
        int size = 0;
        
        ArrayList<String> wordCombination = recursiveLimitScramble(text, limit);
            
        HashSet<String> correctWords = new HashSet<>();
        for (String combination : wordCombination) {
            ArrayList<String> result = recursiveScramble(combination);
            size += result.size();
            getValidWords(result, correctWords);
        }
        return correctWords;
    }
    
    private static boolean checkArguments(String text, int limit, int database) {
        if (text.length() == 0) {
            System.out.println("The input must not be empty");
            return false;
        } else if (!text.matches("[a-z]*[A-Z]*")) {
            System.out.println("The input must be purely letters");
            return false;
        }
        if (limit > 2 && limit < text.length()) {
            WordUnscrambler.limit = limit;
        }
        databaseSelector = database;
        
        return true;
    }
    
    private static HashSet<String> getValidWords(ArrayList<String> result, HashSet<String> correctWords) {
        for (String word : result) {
            try {
                if (isValidWord(word)) {
                    if (correctWords.add(word)) {
                        System.out.print("--> ");
                        System.out.println(word);
                    }
                }
            } catch (Throwable t) {
                System.out.println("Error: " + t.getMessage());
                t.printStackTrace();
                break;
            }
        }
        return correctWords;
    }

    private static void initScramblerStorage(String text, int limit) {
        scramblerStorage = new ArrayList[limit == -1 ? text.length() - 1 : limit];
        for (int i = 0; i < scramblerStorage.length; i++) {
            scramblerStorage[i] = new ArrayList<>();
        }
    }

    private static ArrayList<String> recursiveScramble(String text) {
        if (text.length() == 2) {
            ArrayList<String> returnedList = scramblerStorage[0];
            returnedList.clear();
            returnedList.add(text.charAt(0) + "" + text.charAt(1));
            if (text.charAt(0) != text.charAt(1))
                returnedList.add(text.charAt(1) + "" + text.charAt(0));
            return returnedList;
        } else {
            ArrayList<String> list = scramblerStorage[text.length() - 2];
            list.clear();
            HashSet<Character> gottenChars = new HashSet<>();
            for (int i = 0; i < text.length(); i++) {
                if (!gottenChars.add(text.charAt(i))) continue;
                ArrayList<String> subList = recursiveScramble(text.substring(0, i) + text.substring(i + 1));
                for (String element : subList) {
                    list.add(text.charAt(i) + element);
                }
            }
            return list;
        }

    }
    
    private static ArrayList<String> recursiveLimitScramble(String text, int limit) {
        ArrayList<String> results = new ArrayList<>();
        
        if (limit < 1) {
            return results;
        } else if (limit == 1) {
            for (char c : text.toCharArray()) {
                results.add(c + "");
            }
        } else {
            for (int i = 0; i <= text.length() - limit; i++) {
                String substring = text.substring(i + 1);
                ArrayList<String> subResult = recursiveLimitScramble(substring, limit - 1);
                for (String subResults : subResult) {
                    results.add(text.charAt(i) + subResults);
                }
            }
        }
        return results;
    }

    private static boolean isValidWord(String text) throws IOException {
        if (initDict) {
            dict.clear();
            initDict = false;
        }
        if (dict.size() == 0) {
            for (char character : WordUnscrambler.text.toCharArray()) {
                Scanner scanner = new Scanner(new FileInputStream(new File(wordDir, wordDatabases[databaseSelector] + "/Length" + text.length() + "/" + character + ".txt")));
                while (scanner.hasNextLine()) {
                    String word = scanner.nextLine();
                    if (word.length() != 0) {
                        dict.add(word);
                    }
                }
            }
        }

        if (dict.size() == 0) {
            throw new IOException("Dictionary is empty");
        }

        return dict.contains(text);
    }
}