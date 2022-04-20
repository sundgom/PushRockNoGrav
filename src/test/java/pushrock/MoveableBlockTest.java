package pushrock;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pushrock.model.BlockAbstract;
import pushrock.model.DirectedBlock;
import pushrock.model.MoveableBlock;

public class MoveableBlockTest {
    private char[] validTypes = new char[] {'p', 'r'};
    private String[] validDirections = new String[] {"up", "down", "left", "right"};
    private Map<String, int[]> directionToXYChange = Map.of("up", new int[]{0,1}, "down", new int[]{0,-1}, "right", new int[]{1,0}, "left", new int[]{-1,0});

    //Tests for constructor and methods inherited by BlockAbstract
    @Test
    @DisplayName("Test positive, zero, and positive integers for constructor coordinates.")
    public void testConstructorCoordinates() {
        int[] coordinateValues = new int[]{2, 0, -3};
        for (int value: coordinateValues) {
            testConstructorCoordinates(value, value);
            testConstructorCoordinates(0, value);
            testConstructorCoordinates(value, 0);
        }
    }
    public void testConstructorCoordinates(int x, int y) {
        BlockAbstract neutralValueCoordinates = new MoveableBlock(x, y, validTypes[0], validDirections[0]);
        assertEquals(x, neutralValueCoordinates.getX());
        assertEquals(x, neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(y, neutralValueCoordinates.getY());
        assertEquals(y, neutralValueCoordinates.getCoordinatesXY()[1]);
    }
    @Test
    @DisplayName("Test positive, zero, and negative integers for setting coordinates through the setX() and setY() coordinates inherited from BlockAbstract, that are made public for this class.") 
    public void testSetCoordinates() {
        int[] coordinateValues = new int[]{11, 0, -12};
        for (int i : coordinateValues) {
            testSetX(i);
            testSetY(i);
        }
    }
    private void testSetX(int x) {
        MoveableBlock moveable = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
        moveable.setX(x);
        assertEquals(x, moveable.getX());
        assertEquals(x, moveable.getCoordinatesXY()[0]);
        assertEquals(0, moveable.getY());
        assertEquals(0, moveable.getCoordinatesXY()[1]);
    }
    private void testSetY(int y) {
        MoveableBlock moveable = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
        moveable.setY(y);
        assertEquals(0, moveable.getX());
        assertEquals(y, moveable.getY());
        assertEquals(0, moveable.getCoordinatesXY()[0]);
        assertEquals(y, moveable.getCoordinatesXY()[1]);
    }

    @Test
    public void testConstructorValidTypes() {
        for (char validType : this.validTypes) {
            BlockAbstract constructedValidType = new MoveableBlock(0, 0, validType, validDirections[0]);
            assertEquals(validType, constructedValidType.getType());
        }      
    }
    @Test
    public void testConstructorInvalidTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new MoveableBlock(0, 0, ' ', validDirections[0]),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorState() {
        for (char validType : this.validTypes) {
            BlockAbstract contructedValidType = new MoveableBlock(0, 0, validType, validDirections[0]);
            assertFalse(contructedValidType.getState(), "All moveable blocks should have their state set to false once constructed.");
        }
    }
    @Test
    @DisplayName("Test setting the state after construction through the setState() method inherited from BlockAbstract that is made public for this class.")
    public void testSetState() {
        MoveableBlock contructedValidType = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
        contructedValidType.setState(true);
        assertTrue(contructedValidType.getState(), "Moveable blocks should have their state set to true once setState(true) is called.");
        contructedValidType.setState(false);
        assertFalse(contructedValidType.getState(), "Moveable blocks should have their state set to false once setState(false) is called.");
        
    }
    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract MoveableBlock = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
        assertTrue(MoveableBlock.hasCollision(), "Directed blocks, and thus by extension moveable blocks, do have collision, thus hasCollision() should always return true");
    }
    

    //Tests for constructor and methods inherited by DirectedBlock
    @Test
    public void testConstructorValidDirections() {
        for (String direction : validDirections) {
            DirectedBlock constructedValidDirection = new MoveableBlock(0, 0, validTypes[0], direction);
            assertEquals(direction, constructedValidDirection.getDirection());
            assertEquals(0 + directionToXYChange.get(direction)[0], constructedValidDirection .getDirectionXY()[0]);
            assertEquals(0 + directionToXYChange.get(direction)[1], constructedValidDirection .getDirectionXY()[1]);
        }
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
    @DisplayName("Ensure that the setDirection() method inherited from DirectedBlock, which is made public for this class, properly sets the correct direction when provided with a valid direction.")
    public void testSetValidDirection() {
        MoveableBlock constructedValidDirection = new MoveableBlock(0, 0, validTypes[1], validDirections[1]);
        for (String direction : validDirections) {
            constructedValidDirection.setDirection(direction);
            assertEquals(direction, constructedValidDirection.getDirection());
        }
        constructedValidDirection.setDirection("dOWn");
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

    //Tests for constructor and methods specific to MoveableBlock
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        MoveableBlock constructedPlayer = new MoveableBlock(0, 0, 'p', validDirections[0]);
        assertTrue(constructedPlayer.isPlayer());
        assertFalse(constructedPlayer.isRock());
        MoveableBlock constructedRock = new MoveableBlock(0, 0, 'r', validDirections[0]);
        assertTrue(constructedRock.isRock());
        assertFalse(constructedRock.isPlayer());
    }
    
    @Nested
    class TestNestMovementMethods {
        private MoveableBlock constructedValidType;
        @BeforeEach
        public void setup() {
            constructedValidType = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
        }
        @Test
        public void testUp() {
            constructedValidType.up();
            assertEquals(0, constructedValidType.getX());
            assertEquals(1, constructedValidType.getY());
            assertEquals("up", constructedValidType.getDirection());
        }
        @Test
        public void testDown() {
            constructedValidType.down();
            assertEquals(0, constructedValidType.getX());
            assertEquals(-1, constructedValidType.getY());
            assertEquals("down", constructedValidType.getDirection());
        }
        @Test
        public void testRight() {
            constructedValidType.right();
            assertEquals(1, constructedValidType.getX());
            assertEquals(0, constructedValidType.getY());
            assertEquals("right", constructedValidType.getDirection());
        }
        @Test
        public void testLeft() {
            constructedValidType.left();
            assertEquals(-1, constructedValidType.getX());
            assertEquals(0, constructedValidType.getY());
            assertEquals("left", constructedValidType.getDirection());
        }
        @Test 
        public void testMoveDirectionValidDirections() {
            for (String direction : validDirections) {
                this.constructedValidType = new MoveableBlock(0, 0, validTypes[0], direction);
                constructedValidType.moveInDirection(direction);
                assertEquals(direction, constructedValidType.getDirection());
                assertEquals(0 + directionToXYChange.get(direction)[0], constructedValidType.getX());
                assertEquals(0 + directionToXYChange.get(direction)[1], constructedValidType.getY());
            }
            this.constructedValidType = new MoveableBlock(0, 0, validTypes[0], "right");
            constructedValidType.moveInDirection("DOwn");
            assertEquals("down", constructedValidType.getDirection());
            assertEquals(0 + directionToXYChange.get("down")[0], constructedValidType.getX());
            assertEquals(0 + directionToXYChange.get("down")[1], constructedValidType.getY());
        }
        @Test 
        public void testMoveDirectionInvalidDirections() {
            MoveableBlock constructedValidType = new MoveableBlock(0, 0, validTypes[0], validDirections[0]);
            assertThrows(
                IllegalArgumentException.class,
                () -> constructedValidType.moveInDirection("NorthToVabbi"),
                "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
            assertThrows(
                IllegalArgumentException.class,
                () -> constructedValidType.moveInDirection(null),
                "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
            assertThrows(
                IllegalArgumentException.class,
                () -> constructedValidType.moveInDirection(""),
                "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
        }
    }
}
