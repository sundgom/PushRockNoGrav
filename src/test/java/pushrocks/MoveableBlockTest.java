package pushrocks;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.MoveableBlock;



public class MoveableBlockTest {
    // private MoveableBlock constructedPlayer;
    // private MoveableBlock constructedRock;
    // private String[] validDirections = new String[] {"up", "down", "left", "right"};

    //Tests of constructor and methods inherited by BlockAbstract
    @Test
    @DisplayName("Test ")
    public void testConstructorCoordinates() {
        BlockAbstract neutralValueCoordinates = new MoveableBlock(0, 0, 'p', "right");
        assertEquals(0, neutralValueCoordinates.getX());
        assertEquals(0, neutralValueCoordinates.getY());
        assertEquals(new int[]{0,0}[0], neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{0,0}[1], neutralValueCoordinates.getCoordinatesXY()[1]);
        BlockAbstract positiveValueCoordinates = new MoveableBlock(11, 12, 'p', "right");
        assertEquals(11, positiveValueCoordinates.getX());
        assertEquals(12, positiveValueCoordinates.getY());
        assertEquals(new int[]{11,12}[0], positiveValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{11,12}[1], positiveValueCoordinates.getCoordinatesXY()[1]);
        BlockAbstract negativeValueCoordinates = new MoveableBlock(-11, -12, 'p', "right");
        assertEquals(-11, negativeValueCoordinates.getX());
        assertEquals(-12, negativeValueCoordinates.getY());
        assertEquals(new int[]{-11,-12}[0], negativeValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(new int[]{-11,-12}[1], negativeValueCoordinates.getCoordinatesXY()[1]);
    }

    @Test
    @DisplayName("Test setX()/setY() after construction (vis") 
    public void testSetCoordinates() {
        MoveableBlock neutralValueCoordinates = new MoveableBlock(0, 0, 'p', "right");
        neutralValueCoordinates.setX(11);
        assertEquals(11, neutralValueCoordinates.getX());
        neutralValueCoordinates.setY(12);
        assertEquals(12, neutralValueCoordinates.getY());
        neutralValueCoordinates.setX(0);
        assertEquals(0, neutralValueCoordinates.getX());
        neutralValueCoordinates.setY(0);
        assertEquals(0, neutralValueCoordinates.getY());
        neutralValueCoordinates.setX(-11);
        assertEquals(-11, neutralValueCoordinates.getX());
        neutralValueCoordinates.setY(-12);
        assertEquals(-12, neutralValueCoordinates.getY());

    }

    @Test
    public void testConstructorValidTypes() {
        BlockAbstract constructedPlayer = new MoveableBlock(0, 0, 'p', "right");
        assertEquals('p', constructedPlayer.getType());
        BlockAbstract constructedRock = new MoveableBlock(0, 0, 'r', "right");
        assertEquals('r', constructedRock.getType());
    }
    @Test
    public void testConstructorInvalidTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new MoveableBlock(0, 0, ' ', "right"),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorStateStartValues() {
        BlockAbstract contructedPlayer = new MoveableBlock(0, 0, 'p', "right");
        assertFalse(contructedPlayer.getState(), "Player blocks should have their state set to false once constructed.");
        BlockAbstract constructedRock = new MoveableBlock(0, 0, 'r', "right");
        assertFalse(constructedRock.getState(), "Rock blocks should have their state set to false once constructed.");
    }
    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract MoveableBlock = new MoveableBlock(0, 0, 'p', "right");
        assertTrue(MoveableBlock.hasCollision(), "Directed blocks, and thus by extension moveable blocks, do have collision, thus hasCollision() should always return true");
    }
    

    //Tests of constructor and methods inherited by DirectedBlock
    @Test
    public void testConstructorValidDirections() {
        DirectedBlock constructedUp = new MoveableBlock(0, 0, 'p', "up");
        assertEquals("up", constructedUp.getDirection());
        assertEquals(new int[]{0,1}[0], constructedUp.getDirectionXY()[0]);
        assertEquals(new int[]{0,1}[1], constructedUp.getDirectionXY()[1]);
        DirectedBlock constructedDown = new MoveableBlock(0, 0, 'p', "down");
        assertEquals("down", constructedDown.getDirection());
        assertEquals(new int[]{0,-1}[0], constructedDown.getDirectionXY()[0]);
        assertEquals(new int[]{0,-1}[1], constructedDown.getDirectionXY()[1]);
        DirectedBlock constructedRight = new MoveableBlock(0, 0, 'p', "right");
        assertEquals("right", constructedRight.getDirection());
        assertEquals(new int[]{1,0}[0], constructedRight.getDirectionXY()[0]);
        assertEquals(new int[]{1,0}[1], constructedRight.getDirectionXY()[1]);
        DirectedBlock constructedLeft = new MoveableBlock(0, 0, 'p', "left");
        assertEquals("left", constructedLeft.getDirection());
        assertEquals(new int[]{-1,0}[0], constructedLeft.getDirectionXY()[0]);
        assertEquals(new int[]{-1,0}[1], constructedLeft.getDirectionXY()[1]);

        // for (String direction : validDirections) {
        //     DirectedBlock constructedValidDirection = new MoveableBlock(0, 0, 'p', direction);
        //     assertEquals(direction, constructedValidDirection.getDirection());
        //     // assertEquals(new int[]{1,0}[0], constructedRight.getDirectionXY()[0]);
        //     // assertEquals(new int[]{1,0}[1], constructedRight.getDirectionXY()[1]);
        // }
    }
    @Test
    public void testConstructorInvalidDirections() {
        // MoveableBlock move = new MoveableBlock(0, 0, 'p', "abcd");
        assertThrows(
            IllegalArgumentException.class,
            () -> new MoveableBlock(0, 0, 'p', "abcd"),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
        assertThrows(
            IllegalArgumentException.class,
            () -> new MoveableBlock(0, 0, 'p', null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
        assertThrows(
            IllegalArgumentException.class,
            () -> new MoveableBlock(0, 0, 'p', ""),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    }

    @Test
    @DisplayName("Ensure that the setType() method properly sets the correct direction when provided with a valid direction.")
    public void testSetValidDirection() {
        MoveableBlock constructedValidDirection = new MoveableBlock(0, 0, 'p', "right");
        constructedValidDirection.setDirection("up");
        assertEquals("up", constructedValidDirection.getDirection());
        constructedValidDirection.setDirection("down");
        assertEquals("down", constructedValidDirection.getDirection());
        constructedValidDirection.setDirection("right");
        assertEquals("right", constructedValidDirection.getDirection());
        constructedValidDirection.setDirection("left");
        assertEquals("left", constructedValidDirection.getDirection());
        constructedValidDirection.setDirection("DowN");
        assertEquals("down", constructedValidDirection.getDirection());
    }
    @Test
    @DisplayName("Ensure that the setType() throws illegal argument exception when provided with an invalid direction.")
    public void testSetInvalidDirection() {
        MoveableBlock constructedValidDirection = new MoveableBlock(0, 0, 'p', "right");
        assertThrows(
            IllegalArgumentException.class,
            () -> constructedValidDirection.setDirection("abcd"),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
        assertThrows(
            IllegalArgumentException.class,
            () -> constructedValidDirection.setDirection(null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
        assertThrows(
            IllegalArgumentException.class,
            () -> constructedValidDirection.setDirection(""),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    }

    //Tests of constructor-parameters and class methods specific to MoveableBlock
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        MoveableBlock constructedPlayer = new MoveableBlock(0, 0, 'p', "right");
        assertTrue(constructedPlayer.isPlayer());
        assertFalse(constructedPlayer.isRock());
        MoveableBlock constructedRock = new MoveableBlock(0, 0, 'r', "right");
        assertTrue(constructedRock.isRock());
        assertFalse(constructedRock.isPlayer());
    }


    
}
