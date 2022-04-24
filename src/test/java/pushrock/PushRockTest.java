package pushrock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pushrock.model.BlockAbstract;
import pushrock.model.MoveableBlock;
import pushrock.model.PushRock;
import pushrock.model.TraversableBlock;

public class PushRockTest {
    @Test
    @DisplayName("Help me.")
    public void testConstructor() {

    }


    @BeforeEach
    public void setup() {
        PushRock pushRock = new PushRock("Test", " dprd@ ", "rrg");
    }
    
    @Test 
    @DisplayName("Check that player-blocks have their state set to true only when standing ontop of a pressure plate.")
    public void testPressurePlateActivationPlayer() {
        PushRock pushRock = new PushRock("Test", " dprd @", "rrg");
        BlockAbstract player = pushRock.getTopBlockCopy(2, 0);
        assertTrue(player instanceof MoveableBlock && ((MoveableBlock) player).isPlayer());
        assertFalse(player.getState());
        pushRock.movePlayer("left");
        player = pushRock.getTopBlockCopy(1, 0);
        assertTrue(player instanceof MoveableBlock && ((MoveableBlock) player).isPlayer());
        assertTrue(player.getState());
        pushRock.movePlayer("left");
        player = pushRock.getTopBlockCopy(0, 0);
        assertTrue(player instanceof MoveableBlock && ((MoveableBlock) player).isPlayer());
        assertFalse(player.getState());
        System.out.println(pushRock);
    }

    //Replaces all line separators with the line separator of the current system and removes excess trailings before
    //asserting wether the expected and actual strings are equal.
    private void assertEqualsNoLineSeparator(String expected, String actual) {
        expected = expected.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        actual = actual.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        System.out.println(expected);
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test 
    @DisplayName("Check that player-blocks have their state set to true only when standing ontop of a pressure plate.")
    public void testPressurePlateActivationPlayer2() {
        //a player is represented by 'p' while state is false, and 'q' while true
        String expected = " dprd @";
        PushRock pushRock = new PushRock("Test", expected, "rrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("left");
        expected = " q rd @";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("left");
        expected = "pd rd @";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test 
    @DisplayName("Check that rock-blocks have their state set to true only when standing ontop of a pressure plate.")
    public void testPressurePlateActivationRock() {
        //a rock is represented by 'r' while state is false, and 'o' while true
        String expected = " dprd @";
        PushRock pushRock = new PushRock("Test", expected, "rrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = " d po @";
        //rock's state should return to true once it is pushed on top of the pressure plate
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = " d  qr@";
        //rock's state should return to false once it is pushed off the pressure plate
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        System.out.println("ṛ");
        System.out.println("ṛ".toUpperCase());
    }
    @Test
    @DisplayName("Check that placing weight on all pressure plates results in ending the game by completion.") 
    public void testEndingGamePlayerAndRock() {
        PushRock pushRock = new PushRock("Test", "dprd@", "rrg");
        assertFalse(pushRock.isGameOver());
        //push rock ontop of one of two pressure plate
        pushRock.movePlayer("right");
        assertFalse(pushRock.isGameOver());
        //move player two steps left to step on the last of two pressure plates
        pushRock.movePlayer("left");
        pushRock.movePlayer("left");
        //the game should now be complete
        assertTrue(pushRock.isGameOver(), "The game should have ended once all of the two pressure plates had a moveable block ontop of them.");
    }
}
