package pushrocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;

import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.ObstacleBlock;
import pushrocks.model.TeleporterBlock;
import static java.util.Map.entry;  

public class TeleporterBlockTest {
    private char[] validTypes = new char[] {'t'};
    private String[] validDirections = new String[] {null};

    //Tests of constructor and methods inherited by BlockAbstract
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
        BlockAbstract neutralValueCoordinates = new TeleporterBlock(x, y, validTypes[0], validDirections[0], null);
        assertEquals(x, neutralValueCoordinates.getX());
        assertEquals(x, neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(y, neutralValueCoordinates.getY());
        assertEquals(y, neutralValueCoordinates.getCoordinatesXY()[1]);
    }

    @Test
    public void testConstructorValidTypes() {
        for (char validType : this.validTypes) {
            BlockAbstract constructedValidType = new TeleporterBlock(0, 0, validType, validDirections[0], null);
            assertEquals(validType, constructedValidType.getType());
        }      
    }
    @Test
    public void testConstructorInvalidTypes() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TeleporterBlock(0, 0, 'p', validDirections[0], null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid type.");
    }
    @Test
    @DisplayName("Test that blocks are constructed with the correct state value according to their type.")
    public void testConstructorState() {
        for (char validType : this.validTypes) {
            BlockAbstract contructedValidTypeWithoutConnection = new TeleporterBlock(0, 0, validType, validDirections[0], null);
            assertFalse(contructedValidTypeWithoutConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
            ObstacleBlock connection = new TeleporterBlock(0, 0, 't', validDirections[0], null);
            BlockAbstract contructedValidTypeWithConnection = new TeleporterBlock(0, 0, validType, validDirections[0], connection);
            assertTrue(contructedValidTypeWithConnection.getState(), "All teleporter blocks should have their state set to false once constructed without a connection.");
        }
    }

    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract teleporterBlock = new TeleporterBlock(0, 0, validTypes[0], validDirections[0], null);
        assertTrue(teleporterBlock.hasCollision(), "Directed blocks, and thus by extension moveable blocks, do have collision, thus hasCollision() should always return true");
    }
    

    //Tests of constructor and methods inherited by DirectedBlock
    @Test
    public void testConstructorValidDirections() {
        for (String direction : validDirections) {
            DirectedBlock constructedValidDirection = new TeleporterBlock(0, 0, validTypes[0], direction, null);
            assertEquals(direction, constructedValidDirection.getDirection());
            assertEquals(0, constructedValidDirection.getDirectionXY()[0]);
            assertEquals(0, constructedValidDirection.getDirectionXY()[1]);
        }
    }

    private void testConstructorInvalidDirections(String invalidDirection) {
        assertThrows(
            IllegalArgumentException.class,
            () -> new TeleporterBlock(0, 0, 't', invalidDirection, null),
            "IllegalArgument should be thrown if the constuctor is provided with an invalid direction. Direction was: " + invalidDirection);
    }
    @Test
    public void testConstructorInvalidDirections() {
        String[] invalidDirections = new String[]{"NorthToVabbi", "up", "down", "right", "left", ""};
        for (String invalidDirection : invalidDirections) {
            testConstructorInvalidDirections(invalidDirection);
        }

    }
    // @Test
    // @DisplayName("Ensure that the setType() method properly sets the correct direction when provided with a valid direction.")
    // public void testSetValidDirection() {
    //     TeleporterBlock constructedValidDirection = new TeleporterBlock(0, 0, validTypes[1], validDirections[1]);
    //     for (String direction : validDirections) {
    //         constructedValidDirection.setDirection(direction);
    //         assertEquals(direction, constructedValidDirection.getDirection());
    //     }
    //     constructedValidDirection.setDirection("dOWn");
    //     assertEquals("down", constructedValidDirection.getDirection());
    // }
    // @Test
    // @DisplayName("Ensure that the setType() throws illegal argument exception when provided with an invalid direction.")
    // public void testSetInvalidDirection() {
    //     TeleporterBlock constructedValidDirection = new TeleporterBlock(0, 0, 'p', "right");
    //     assertThrows(
    //         IllegalArgumentException.class,
    //         () -> constructedValidDirection.setDirection("abcd"),
    //         "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    //     assertThrows(
    //         IllegalArgumentException.class,
    //         () -> constructedValidDirection.setDirection(null),
    //         "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    //     assertThrows(
    //         IllegalArgumentException.class,
    //         () -> constructedValidDirection.setDirection(""),
    //         "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    // }

    // //Tests of constructor-parameters and class methods specific to TeleporterBlock
    // @Test
    // @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    // public void testTypeChecks() {
    //     TeleporterBlock constructedPlayer = new TeleporterBlock(0, 0, 'p', validDirections[0]);
    //     assertTrue(constructedPlayer.isPlayer());
    //     assertFalse(constructedPlayer.isRock());
    //     TeleporterBlock constructedRock = new TeleporterBlock(0, 0, 'r', validDirections[0]);
    //     assertTrue(constructedRock.isRock());
    //     assertFalse(constructedRock.isPlayer());
    // }
    
    // @Nested
    // class TestNestMovementMethods {
    //     private TeleporterBlock constructedValidType;
    //     @BeforeEach
    //     public void setup() {
    //         constructedValidType = new TeleporterBlock(0, 0, validTypes[0], validDirections[0]);
    //     }
    //     @Test
    //     public void testUp() {
    //         constructedValidType.up();
    //         assertEquals(0, constructedValidType.getX());
    //         assertEquals(1, constructedValidType.getY());
    //         assertEquals("up", constructedValidType.getDirection());
    //     }
    //     @Test
    //     public void testDown() {
    //         constructedValidType.down();
    //         assertEquals(0, constructedValidType.getX());
    //         assertEquals(-1, constructedValidType.getY());
    //         assertEquals("down", constructedValidType.getDirection());
    //     }
    //     @Test
    //     public void testRight() {
    //         constructedValidType.right();
    //         assertEquals(1, constructedValidType.getX());
    //         assertEquals(0, constructedValidType.getY());
    //         assertEquals("right", constructedValidType.getDirection());
    //     }
    //     @Test
    //     public void testLeft() {
    //         constructedValidType.left();
    //         assertEquals(-1, constructedValidType.getX());
    //         assertEquals(0, constructedValidType.getY());
    //         assertEquals("left", constructedValidType.getDirection());
    //     }
    //     @Test 
    //     public void testMoveDirectionValidDirections() {
    //         for (String direction : validDirections) {
    //             this.constructedValidType = new TeleporterBlock(0, 0, validTypes[0], direction);
    //             constructedValidType.moveInDirection(direction);
    //             assertEquals(direction, constructedValidType.getDirection());
    //             assertEquals(0 + directionToXYChange.get(direction)[0], constructedValidType.getX());
    //             assertEquals(0 + directionToXYChange.get(direction)[1], constructedValidType.getY());
    //         }
    //         this.constructedValidType = new TeleporterBlock(0, 0, validTypes[0], "right");
    //         constructedValidType.moveInDirection("DOwn");
    //         assertEquals("down", constructedValidType.getDirection());
    //         assertEquals(0 + directionToXYChange.get("down")[0], constructedValidType.getX());
    //         assertEquals(0 + directionToXYChange.get("down")[1], constructedValidType.getY());
    //     }
    //     @Test 
    //     public void testMoveDirectionInvalidDirections() {
    //         TeleporterBlock constructedValidType = new TeleporterBlock(0, 0, validTypes[0], validDirections[0]);
    //         assertThrows(
    //             IllegalArgumentException.class,
    //             () -> constructedValidType.moveInDirection("NorthToVabbi"),
    //             "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    //         assertThrows(
    //             IllegalArgumentException.class,
    //             () -> constructedValidType.moveInDirection(null),
    //             "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    //         assertThrows(
    //             IllegalArgumentException.class,
    //             () -> constructedValidType.moveInDirection(""),
    //             "IllegalArgument should be thrown if the constuctor is provided with an invalid direction.");
    //     }
    // }
}
