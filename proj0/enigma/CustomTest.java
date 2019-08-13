package enigma;

import org.junit.Test;

import static enigma.TestUtils.NAVALA;
import static org.junit.Assert.assertTrue;

public class CustomTest {
    @Test
    public void testSetNotch(){
        Rotor newRotor = new MovingRotor("I",new Permutation(NAVALA.get("I"),new CharacterRange('A','Z')),"AM");
        assertTrue(((MovingRotor) newRotor).haveNotch('A'));
        assertTrue(((MovingRotor) newRotor).haveNotch('M'));
    }
}
