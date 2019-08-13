package enigma;

import static enigma.EnigmaException.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that represents a rotating rotor in the enigma machine.
 * 
 * @author Zixi Li (zixili2@illinois.edu)
 */
public class MovingRotor extends Rotor {

    /** The set of notches on this rotor. */
    private Set<Integer> notches = new HashSet<>();

    /**
     * A rotor named NAME whose permutation in its default setting is PERM, and
     * whose notches are at the positions indicated in NOTCHES. The Rotor is
     * initially in its 0 setting (first character of its alphabet).
     */
    public MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        this.iPos = 0;

        // This implementation is not cool enough for Java 12.
        // for (char i : notches.toCharArray()) {
        //     if (i != '\0')
        //         this.notches.add(_permutation.alphabet().toInt(i));
        // }

        this.notches = notches.chars()
            .parallel()
            .filter(item -> alphabet().contains((char)item))
            .mapToObj(item -> alphabet().toInt((char) item))
            .collect(Collectors.toSet());
   }

    @Override
    public boolean rotates() {
        return true;
    }

    @Override
    public boolean atNotch() {
        return this.notches.contains(this.iPos);
    }

    @Override
    public void advance() {
        this.set(this.setting()+1);
    }

    public boolean haveNotch(char k){
        return notches.contains(alphabet().toInt(k));
    }

    // To run this through command line, from the proj0 directory, run the following:
    // javac enigma/Rotor.java enigma/MovingRotor.java enigma/Permutation.java enigma/Alphabet.java enigma/CharacterRange.java enigma/EnigmaException.java
    // java enigma/MovingRotor
    public static void main(String[] args) {
        Permutation perm = new Permutation("(AB) (CDEFGHIJKLMNOPQRSTUVWXYZ)", new CharacterRange('A', 'Z'));
        MovingRotor rotor = new MovingRotor("forward one", perm, "B");

        System.out.println(rotor.name().equals("forward one"));
        System.out.println(rotor.alphabet() == perm.alphabet());
        System.out.println(rotor.permutation() == perm);
        System.out.println(rotor.rotates() == true);
        System.out.println(rotor.reflecting() == false);

        System.out.println(rotor.size() == 26);
        rotor.set(1);
        System.out.println(rotor.setting() == 1);
        System.out.println(rotor.atNotch() == true);
        rotor.set('A');
        System.out.println(rotor.setting() == 0);
        System.out.println(rotor.atNotch() == false);
        System.out.println(rotor.convertForward(0) == 1);
        System.out.println(rotor.convertBackward(1) == 0);
        rotor.advance();
        System.out.println(rotor.setting() == 1);
        System.out.println(rotor.atNotch() == true);
        System.out.println(rotor.convertForward(0) == 25);
        System.out.println(rotor.convertBackward(25) == 0);
    }

}
