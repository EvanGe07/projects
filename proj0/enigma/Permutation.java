package enigma;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author Zixi Li
 */
public class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the form
     * "(cccc) (cc) ..." where the c's are characters in ALPHABET, which is
     * interpreted as a permutation in cycle notation. Characters in the alphabet
     * that are not included in any cycle map to themselves. Whitespace is ignored.
     */

    /** The Pattern for this permutation */
    private String _cycle;

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    public Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        this._cycle = cycles;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    public int size() {
        return _alphabet.size();
    }

    /**
     * Return the index result of applying this permutation to the character at
     * index P in ALPHABET.
     */
    public int permute(int p) {
        // NOTE: it might be beneficial to have one permute() method always call the
        // other
        char pChar = _alphabet.toChar(p);
        int indexP = _cycle.indexOf(pChar);
        if (indexP == -1) {
            // This character is not found in cycles, which means it's not defined.
            // Just return the original char.
            return p;
        } else {
            // This character is found in cycles. Find its next.
            if (_cycle.charAt(indexP + 1) == ')') {
                String pattern = String.format("\\(([A-Z])\\w*%c\\)", pChar);
                Pattern finder = Pattern.compile(pattern);
                Matcher pmatch = finder.matcher(_cycle);
                if (pmatch.find()) {
                    String rep = pmatch.group(1);
                    return _alphabet.toInt(rep.charAt(0));
                } else {
                    // The letter is in a standalone group.
                    return p;
                }
            } else {
                int temp = _alphabet.toInt(_cycle.charAt(indexP + 1));
                return temp;
            }
        }
    }

    /**
     * Return the index result of applying the inverse of this permutation to the
     * character at index C in ALPHABET.
     */
    public int invert(int c) {
        // NOTE: it might be beneficial to have one invert() method always call the
        // other
        char cChar = _alphabet.toChar(c);
        int indexC = _cycle.indexOf(cChar);
        if (indexC == -1) {
            // This character is not found in cycles, which means it's not defined.
            // Just return the original char.
            return c;
        } else {
            // This character is found in cycles. Find its next.
            if (_cycle.charAt(indexC - 1) == '(') {
                String pattern = String.format("\\(%c\\w*([A-Z])\\)", cChar);
                Pattern finder = Pattern.compile(pattern);
                Matcher pmatch = finder.matcher(_cycle);
                if (pmatch.find()) {
                    String rep = pmatch.group(1);
                    return _alphabet.toInt(rep.charAt(0));
                } else {
                    // The letter is in a standalone group.
                    return c;
                }
            } else {
                return _alphabet.toInt(_cycle.charAt(indexC - 1));
            }
        }
    }

    /**
     * Return the character result of applying this permutation to the index of
     * character P in ALPHABET.
     */
    public char permute(char p) {
        // NOTE: it might be beneficial to have one permute() method always call the
        // other
        return _alphabet.toChar(permute(_alphabet.toInt(p)));
    }

    /**
     * Return the character result of applying the inverse of this permutation to
     * the index of character P in ALPHABET.
     */
    public char invert(char c) {
        // NOTE: it might be beneficial to have one invert() method always call the
        // other
        return _alphabet.toChar(invert(_alphabet.toInt(c)));
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    public Alphabet alphabet() {
        return _alphabet;
    }

    // Some starter code for unit tests. Feel free to change these up!
    // To run this through command line, from the proj0 directory, run the following:
    // javac enigma/Permutation.java enigma/Alphabet.java enigma/CharacterRange.java enigma/EnigmaException.java
    // java enigma/Permutation
    public static void main(String[] args) {
        Permutation perm = new Permutation("(ABCDEFGHIJKLMNOPQRSTUVWXYZ)", new CharacterRange('A', 'Z'));
        System.out.println(perm.size() == 26);
        System.out.println(perm.permute('A') == 'B');
        System.out.println(perm.invert('B') == 'A');
        System.out.println(perm.permute(0) == 1);
        System.out.println(perm.invert(1) == 0);
    }
}
