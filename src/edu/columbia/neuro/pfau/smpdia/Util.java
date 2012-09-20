/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author davidpfau
 */
public class Util {
    private static final int NEWLINE = -1;

    public static int[][] loadText(String path, HashMap<Integer,Integer> alphabet) throws FileNotFoundException, IOException {
        File in = new File(path);
        alphabet.put((int)'\n', NEWLINE); // assign newline a special value
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(in));
        int[] symbols = new int[(int) in.length() + 1]; // +1 so we can add a newline at the end

        int ind = 0;
        int b;
        int len = 0;
        int numLines = 1;
        while ((b = bis.read()) > -1) {
            Integer c = alphabet.get(b);
            if (c != null) {
                symbols[(ind++)] = c;
                if (c == NEWLINE) len++;
            } else {
                symbols[(ind++)] = alphabet.size() - 1;
                alphabet.put(b, alphabet.size() - 1);
            }
            if (b == '\n') {
            	numLines++;
            }
        }

        symbols[symbols.length - 1] = NEWLINE;
        len++;

        assert len == numLines;

        int[][] data = new int[numLines][];
        int i = 0;
        int line = 0;
        for (int j = 0; j < symbols.length; j++) {
            if (symbols[j] == NEWLINE) {
                data[line] = new int[j - i];
                System.arraycopy(symbols, i, data[line], 0, j - i);
                i = j + 1;
                line++;
            }
        }
        if (bis != null) bis.close();
        return data;
    }

    public static int[][] loadText( String path ) throws FileNotFoundException, IOException {
        HashMap<Integer,Integer> alphabet = new HashMap<Integer,Integer>();
        return loadText( path, alphabet );
    }
}
