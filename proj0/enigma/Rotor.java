package enigma;

import static enigma.EnigmaException.*;

/**
 * Superclass that represents a rotor in the enigma machine.
 * 
 * @author Zixi Li (zixili2@illinois.edu)
 */
public class Rotor {

    /** Current Position, in numeric format. */
    protected int iPos;
    
    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** A rotor named NAME whose permutation is given by PERM. */
    public Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        iPos = 0;
    }

    /** Return my name. */
    public String name() {
        return _name;
    }

    /** Return my alphabet. */
    public Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    public Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    public int size() {
        return _permutation.size();
    }

    /** Return true if and only if I have a ratchet and can move.
     * @return Where this rotor can rotate
     */
    public boolean rotates() {
        return false;
    }

    /** Return true if and only if I reflect. */
    public boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    public int setting() {
        return iPos;
    }

    /**
     * Set setting() to POSN.
     */
    public void set(int posn) {
        this.iPos = wrapsize(posn);
    }

    /** Set setting() to character CPOSN. */
    public void set(char cposn) {
        this.iPos = wrapsize(alphabet().toInt(cposn));
    }

    public int wrapsize(int k){
        return (k + size()) % size();
    }

    /**
     * Return the conversion of P (an integer in the range 0..size()-1) according to
     * my permutation.
     */
    public int convertForward(int p) {
        // return 0;
        int newPos = wrapsize(p + iPos);
        return wrapsize(_permutation.permute(newPos) + size() - iPos);
    }

    /**
     * Return the conversion of C (an integer in the range 0..size()-1) according to
     * the inverse of my permutation.
     */
    public int convertBackward(int c) {
        int newPos = wrapsize(c + iPos);
        return wrapsize(_permutation.invert(newPos) - iPos + size());
    }

    /**
     * Returns true if and only if I am positioned to allow the rotor to my left to
     * advance.
     */
    public boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    public void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }
}
