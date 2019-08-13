package enigma;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Zixi Li
 */
public class Machine {

    /** Rotors in use
     * Index = 0: Leftmost Rotor.
     * Index = slots; Rightmost Rotor.
     */
    List<Rotor> rotorInUse = new ArrayList<>();

    Map<String, Rotor> rotorStor;
    /** Available slots for rotors */
    private int slots;

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of Pawls on this machine */
    public int iPawls;

    /** Setting of the plugboard */
    public Permutation _plugboard;

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls. ALLROTORS contains all the
     *  available rotors. */
    public Machine(Alphabet alpha, int numRotors, int pawls,
            Rotor[] allRotors) {
        _alphabet = alpha;
        slots = numRotors;
        /*
           <Cite> This conversion is from JavaDoc 12, Collectors::toMap.
           https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/stream/Collectors.html#toMap(java.util.function.Function,java.util.function.Function)
         */
        rotorStor = Arrays.stream(allRotors)
                .parallel()
                .filter(item -> item != null)
                .collect(Collectors.toMap(
                        (item) -> item.name().toUpperCase(),
                        Function.identity())
                );

        iPawls = pawls;
    }

    /** Return the number of rotor slots I have. */
    public int numRotors() {
        return slots;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    public int numPawls() {
        return iPawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    public void insertRotors(String[] rotors) {
        String[] rotors_uppercase  = Arrays.stream(rotors)
                .map(item -> item.toUpperCase())
                .collect(Collectors.toList())
                .toArray(String[]::new);

        for(int i=0; i<rotors.length; i++){
            if(rotorStor.get(rotors_uppercase[i])!=null) {
                rotorInUse.add(i, rotorStor.get(rotors_uppercase[i]));
            } else {
                rotorInUse.clear();
                throw error("Specified rotor: |%s|, does not exist.",rotors_uppercase[i]);
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    public void setRotors(String setting) {
        for(int i=1;i<slots; i++){
            rotorInUse.get(i).set(setting.charAt(i-1));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    public void setPlugboard(Permutation plugboard) {
        this._plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    public int convert(int c) {
    	// HINT: This one is tough! Consider using a helper method which advances
    	//			the appropriate Rotors. Then, send the signal into the
    	//			Plugboard, through the Rotors, bouncing off the Reflector,
    	//			back through the Rotors, then out of the Plugboard again.

        //Plugboard
        advance();
        int transmitting = _plugboard.permute(c);

        /** The character enters from the RIGHT. Transmit it leftwards
         * until it hits the reflector. */
        for(int i=slots-1; i>=1;i--){
            transmitting = rotorInUse.get(i).convertForward(transmitting);
        }
        transmitting = rotorInUse.get(0).convertForward(transmitting);
        /**
         * Transmit the character backwards, left to right
         */
        for(int i=1;i<slots;i++){
            transmitting = rotorInUse.get(i).convertBackward(transmitting);
        }

        // Run through the plugboard again.
        return _plugboard.invert(transmitting);
    }

    /** Optional helper method for convert() which rotates the necessary Rotors. */
    private void advance() {
//        rotorInUse.get(slots-1).advance();
//        int pt = slots - 2;
//        while (rotorInUse.get(pt + 1).atNotch() && pt >= 1) {
//            rotorInUse.get(pt).advance();
//            pt--;
//        }
        for (int i = 0; i < slots - 1; i++) {
            if (rotorInUse.get(i).rotates()) {
                if (rotorInUse.get(i + 1).atNotch()) {
                    rotorInUse.get(i).advance();
                }
            }
        }

        rotorInUse.get(slots - 1).advance();
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    public String convert(String msg) {
    	// HINT: Strings are basically just a series of characters
        String msgcpy = msg.toUpperCase();
        return msgcpy.chars()
                .sequential()
                .filter(item -> _alphabet.contains((char)item))
                .map(item -> _alphabet.toInt((char)item))
                .map(this::convert)
                .mapToObj(_alphabet::toChar)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }


    // To run this through command line, from the proj0 directory, run the following:
    // javac enigma/Machine.java enigma/Rotor.java enigma/FixedRotor.java enigma/Reflector.java enigma/MovingRotor.java enigma/Permutation.java enigma/Alphabet.java enigma/CharacterRange.java enigma/EnigmaException.java
    // java enigma/Machine
    public static void main(String[] args) {

        CharacterRange upper = new CharacterRange('A', 'Z');
        MovingRotor rotorI = new MovingRotor("I",
                new Permutation("(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)", upper),
                "Q");
        MovingRotor rotorII = new MovingRotor("II",
                new Permutation("(FIXVYOMW) (CDKLHUP) (ESZ) (BJ) (GR) (NT) (A) (Q)", upper),
                "E");
        MovingRotor rotorIII = new MovingRotor("III",
                new Permutation("(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)", upper),
                "V");
        MovingRotor rotorIV = new MovingRotor("IV",
                new Permutation("(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)", upper),
                "J");
        MovingRotor rotorV = new MovingRotor("V",
                new Permutation("(AVOLDRWFIUQ)(BZKSMNHYC) (EGTJPX)", upper),
                "Z");
        FixedRotor rotorBeta = new FixedRotor("Beta",
                new Permutation("(ALBEVFCYODJWUGNMQTZSKPR) (HIX)", upper));
        FixedRotor rotorGamma = new FixedRotor("Gamma",
                new Permutation("(AFNIRLBSQWVXGUZDKMTPCOYJHE)", upper));
        Reflector rotorB = new Reflector("B",
                new Permutation("(AE) (BN) (CK) (DQ) (FU) (GY) (HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", upper));
        Reflector rotorC = new Reflector("C",
                new Permutation("(AR) (BD) (CO) (EJ) (FN) (GT) (HK) (IV) (LM) (PW) (QZ) (SX) (UY)", upper));

        Rotor[] allRotors = new Rotor[9];
        allRotors[0] = rotorI;
        allRotors[1] = rotorII;
        allRotors[2] = rotorIII;
        allRotors[3] = rotorIV;
        allRotors[4] = rotorV;
        allRotors[5] = rotorBeta;
        allRotors[6] = rotorGamma;
        allRotors[7] = rotorB;
        allRotors[8] = rotorC;

        Machine machine = new Machine(upper, 5, 3, allRotors);
        machine.insertRotors(new String[]{"B", "BETA", "III", "IV", "I"});
        machine.setRotors("AXLE");
        machine.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", upper));

        System.out.println(machine.numRotors() == 5);
        System.out.println(machine.numPawls() == 3);
        System.out.println(machine.convert(5) == 16);
        System.out.println(machine.convert(17) == 21);
        System.out.println(machine.convert("OMHISSHOULDERHIAWATHA").equals("PQSOKOILPUBKJZPISFXDW"));
        System.out.println(machine.convert("TOOK THE CAMERA OF ROSEWOOD").equals("BHCNSCXNUOAATZXSRCFYDGU"));
        System.out.println(machine.convert("Made of sliding folding rosewood").equals("FLPNXGXIXTYJUJRCAUGEUNCFMKUF"));
    }
}
