package pushrocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import pushrocks.model.BlockAbstract;
import pushrocks.model.ObstacleBlock;
import pushrocks.model.PortalWallBlock;
import pushrocks.model.TeleporterBlock;

public class TeleporterBlockTest {
    // private char validType = 't';
    // private String validDirection = null;

    //Tests for constructor and methods inherited from BlockAbstract
    private void testConstructorCoordinates(int x, int y) {
        BlockAbstract neutralValueCoordinates = new TeleporterBlock(x, y);
        assertEquals(x, neutralValueCoordinates.getX());
        assertEquals(x, neutralValueCoordinates.getCoordinatesXY()[0]);
        assertEquals(y, neutralValueCoordinates.getY());
        assertEquals(y, neutralValueCoordinates.getCoordinatesXY()[1]);
    }
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
    //Tests of constructor and it's inherited properties from BlockAbstract, DirectedBlock, and ObstacleBlock
    @Test
    @DisplayName("Check that portal wall blocks have their type set to 'w' after being constructed.")
    public void testConstructorCorrectStartValues() {
        //Coordinate and type properties inherited by BlockAbstract.
        TeleporterBlock teleporter = new TeleporterBlock(0, 0);
        assertEquals('t', teleporter.getType(), "Teleporter block type should be 't' after construction.");
        assertFalse(teleporter.getState(), "Teleporter block state should be false after construction.");
        //Direction properties inherited by DirectedBlock.
        assertNull(teleporter.getDirection(), "Teleporter block direction should be null after construction.");
        assertEquals(0, teleporter.getDirectionXY()[0], "Teleporter block x-coordinate direction should be 0 after construction.");
        assertEquals(0, teleporter.getDirectionXY()[1], "Teleporter block y-coordinate direction should be 0 after construction.");
        //Connection properties inherited by ObstacleBlock.
        assertNull(teleporter.getConnection(), "Teleporter block connection should be null after construction.");
    }

    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract teleporterBlock = new TeleporterBlock(0, 0);
        assertTrue(teleporterBlock.hasCollision(), "Directed blocks, and thus by extension teleporter blocks, have collision, thus hasCollision() should always return true");
    }

    @Nested
    //The first few of the following connection tests are inspired by the partner excercise.
    class TestNestConnectionMethods {
        private TeleporterBlock teleporter1;
        private TeleporterBlock teleporter2;

        @BeforeEach
        private void setup() {
            teleporter1 = new TeleporterBlock(0, 0);
            teleporter2 = new TeleporterBlock(5, 5);
        }
        @Test
        @DisplayName("teleporter1 should be connected to teleporter2 and vice versa after a teleporter1.setConnection(teleporter2) method call, and their state should be set to true.")
        public void testSetConnectionOtherTeleporter() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            assertTrue(teleporter1.getState(), "teleporter1 should have it's state set to true once it is connected to another teleporter");
            assertTrue(teleporter2.getState(), "teleporter2 should have it's state set to true once it is connected to another teleporter");
        }
        @Test
        @DisplayName("Attempting to connect a teleporter to a non-teleporter object should throw IllegalArgumentException")
        public void testSetConnectionNonTeleporterObjects() {
            ObstacleBlock wall = new PortalWallBlock(0, 0);
            PortalWallBlock portal = new PortalWallBlock(0, 0);
            portal.setPortal(true, "right", null);
            assertThrows(IllegalArgumentException.class, 
            () -> teleporter1.setConnection(wall),
            "IllegalArgument should be thrown when attempting to connect a teleporter with a non-teleporter object, in this case a wall");
            assertThrows(IllegalArgumentException.class, 
            () -> teleporter2.setConnection(portal),
            "IllegalArgument should be thrown when attempting to connect a teleporter with a non-teleporter object, in this case a portal");
        }
        @Test
        @DisplayName("Setting a connected teleporter's connection to null should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testSetConnectionNull() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            teleporter1.setConnection(null);
            assertNull(teleporter1.getConnection(), "teleporter1 should no longer have a connection after the setConnection(null) was called on it.");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection after the setConnection(null) was called on its connection.");
            assertFalse(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been set to null.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection set to null.");
        }
        @Test
        @DisplayName("Removing a connected teleporter's connection should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testRemoveConnection() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            teleporter1.removeConnection();
            assertNull(teleporter1.getConnection(), "teleporter1 should no longer have a connection after the removeConnection() was called on it.");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection after the removeConnection() was called on its connection.");
            assertFalse(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been removed.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
        }
        @Test
        @DisplayName("Removing a connected teleporter's connection should remove the connection of both that teleporter and its connection, and their state should be set to false.")
        public void testReplaceConnectionWithNewConnection() {
            teleporter1.setConnection(teleporter2);
            assertEquals(teleporter2, teleporter1.getConnection(), "teleporter1 should be connected to teleporter2");
            assertEquals(teleporter1, teleporter2.getConnection(), "teleporter2 should be connected to teleporter1");
            TeleporterBlock teleporter3 = new TeleporterBlock(0, 0);
            //teleporter3 replaces teleporter2 as teleporter1's connection
            teleporter1.setConnection(teleporter3);
            assertEquals(teleporter3, teleporter1.getConnection(), "teleporter1 should be connected to teleporter3");
            assertEquals(teleporter1, teleporter3.getConnection(), "teleporter3 should be connected to teleporter1");
            assertNull(teleporter2.getConnection(), "teleporter2 should no longer have a connection once it's been replaced by teleporter3 as teleporter1's connection.");
            assertTrue(teleporter1.getState(), "teleporter1 should have its state set to false once its connection has been removed.");
            assertTrue(teleporter3.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
            assertFalse(teleporter2.getState(), "teleporter2 should have its state set to false once its connection's connection has been removed.");
        }
        @Test
        @DisplayName("Attempting to connect a teleporter to itself should set the connection to null.")
        public void testSetConnectionToSelf() {
            teleporter1.setConnection(teleporter1);
            assertNull(teleporter1.getConnection(), "teleporter connection should stay null after attempting to connect to itself.");
            teleporter1.setConnection(teleporter2);
            assertNotNull(teleporter1.getConnection(), "teleporter connection should no longer be null after connecting to another teleporter.");
            teleporter1.setConnection(teleporter1);
            assertNull(teleporter1.getConnection(), "teleporter connection should be back to null after attempting to connect to itself again.");
            assertNull(teleporter2.getConnection(), "the previously connected teleporter should no longer be connected to the teleporter that attempted to connect to itself.");
        }

        @Test
        @DisplayName("Check that a connected teleporter returns a list containing correct entry point coordinates")
        public void testGetEntryPointsConnectedTeleporter() {
            teleporter1.setConnection(teleporter2);
            int tpX = teleporter1.getX();
            int tpY = teleporter1.getY();
            //Entry points are always placed one step away from the teleporter itself, either horizontal or vertical, not both.
            int[][] entryPoints = teleporter1.getEntryPointsXY();
            for (int[] entry : entryPoints) {
                //If the provided entry point is one right step away from the teleporter
                if (entry[0] == tpX + 1) {
                    //then the y coordinate must be the same for both the teleporter and the entry point.
                    assertEquals(tpY, entry[1]);
                }
                //If the provided entry point is one left step away from the teleporter
                else if (entry[0] == tpX - 1) {
                    //then the y coordinate must be the same for both the teleporter and the entry point.
                    assertEquals(tpY, entry[1]);
                }
                //If the provided entry point is one upward step away from the teleporter
                else if (entry[1] == tpY + 1) {
                    //then the x coordinate must be the same for both the teleporter and the entry point.
                    assertEquals(tpX, entry[0]);
                }
                //If the provided entry point is one downward step away from the teleporter
                else if (entry[1] == tpY - 1) {
                    //then the x coordinate must be the same for both the teleporter and the entry point.
                    assertEquals(tpX, entry[0]);
                }
                else {
                    //In all other cases the coordinate is not a correctly placed entry point.
                    assertFalse(true, "The coordinate (x:" + entry[0] + ", y:" + entry[1] + ") is not valid coordinate for a teleporter with coordinates: (x:" + tpX + ", y:" + tpY + ").");
                }
            }
        }
        @Test
        @DisplayName("Check that a disconnected teleporter does not return any entry point coordinates when getEntryPointsXY() is called.")
        public void testGetEntryPointsXYDisconnectedTeleporter() {
            assertNull(teleporter1.getConnection());
            assertNull(teleporter1.getEntryPointsXY(), "a teleporter without a connection should not return an entry point.");
        }
        @Test
        @DisplayName("Check that a connected teleporter returns a correct exit point coordinate when it is connected and the entering block is standing at an entry point.")
        public void testGetExitPointXYConnectedTeleporterWhileAtEntryPoint() {
            teleporter1.setConnection(teleporter2);
            int tp1X = teleporter1.getX();
            int tp1Y = teleporter1.getY();
            BlockAbstract blockAtUpperEntry = new TeleporterBlock(tp1X, tp1Y+1);
            BlockAbstract blockAtLowerEntry = new TeleporterBlock(tp1X, tp1Y-1);
            BlockAbstract blockAtRightEntry = new TeleporterBlock(tp1X+1, tp1Y);
            BlockAbstract blockAtLeftEntry = new TeleporterBlock(tp1X-1, tp1Y);
            int tp2X = teleporter2.getX();
            int tp2Y = teleporter2.getY();
            //When entered from above the exit point should be one step below the connected teleporter.
            assertEquals(teleporter1.getExitPointXY(blockAtUpperEntry)[0], tp2X, "The exit point should have an x-coordinate value equal to the connected teleporter");
            assertEquals(teleporter1.getExitPointXY(blockAtUpperEntry)[1], tp2Y-1, "The exit point should have a y-coordinate value one less than the connected teleporter");
            //When entered from below the exit point should be one step above connected teleporter.      
            assertEquals(teleporter1.getExitPointXY(blockAtLowerEntry)[0], tp2X, "The exit point should have an x-coordinate value equal to the connected teleporter");
            assertEquals(teleporter1.getExitPointXY(blockAtLowerEntry)[1], tp2Y+1, "The exit point should have a y-coordinate value one greater than the connected teleporter");
            //When entered from the right the exit point should be one step left of the connected teleporter.
            assertEquals(teleporter1.getExitPointXY(blockAtRightEntry)[0], tp2X-1, "The exit point should have a x-coordinate value one less than the connected teleporter");
            assertEquals(teleporter1.getExitPointXY(blockAtRightEntry)[1], tp2Y, "The exit point should have a y-coordinate value equal to the connected teleporter");
            //When entered from the left the exit point should be one step right of the connected teleporter.
            assertEquals(teleporter1.getExitPointXY(blockAtLeftEntry)[0], tp2X+1, "The exit point should have a x-coordinate value one greater than the connected teleporter");
            assertEquals(teleporter1.getExitPointXY(blockAtLeftEntry)[1], tp2Y, "The exit point should have a y-coordinate value equal to the connected teleporter");
        }
        @Test
        @DisplayName("Check that a connected teleporter does not return any exit point coordinates when the entering block is not standing at an entry point.")
        public void testGetExitPointConnectedTeleporterWhileNotAtEntryPoint() {
            BlockAbstract enteringBlock = new TeleporterBlock(1, 1);
            teleporter1.setConnection(teleporter2);
            assertNull(teleporter1.getExitPointXY(enteringBlock), "a teleporter with a connection should not return an entry point if the entring block does not stand at one of the teleporter's entry points.");
        }
        @Test
        @DisplayName("Check that a disconnected teleporter does not return any exit point coordinates.")
        public void testGetExitPointDisconnectedTeleporter() {
            BlockAbstract enteringBlock = new TeleporterBlock(1, 0);
            assertNull(teleporter1.getConnection());
            assertNull(teleporter1.getExitPointXY(enteringBlock), "a teleporter without a connection should not return an entry point.");
        }

        @Test
        @DisplayName("Check that a connected teleporter returns the correct exit direction for getExitDirectionXY()")
        public void testGetExitPointDirectionXYConnectedWithBlockAtEntryPoint() {
            teleporter1.setConnection(teleporter2);
            int tpX = teleporter1.getX();
            int tpY = teleporter1.getY();
            BlockAbstract blockAtUpperEntry = new TeleporterBlock(tpX, tpY+1);
            BlockAbstract blockAtLowerEntry = new TeleporterBlock(tpX, tpY-1);
            BlockAbstract blockAtRightEntry = new TeleporterBlock(tpX+1, tpY);
            BlockAbstract blockAtLeftEntry = new TeleporterBlock(tpX-1, tpY);
            //A block entering a teleporter from above should be headed down, thus the direction is represented by a -1 y-coordinate
            assertEquals(teleporter1.getExitDirectionXY(blockAtUpperEntry)[0], 0);
            assertEquals(teleporter1.getExitDirectionXY(blockAtUpperEntry)[1], -1);
            //A block entering a teleporter from below should be headed up, thus the direction is represented by a +1 y-coordinate
            assertEquals(teleporter1.getExitDirectionXY(blockAtLowerEntry)[0], 0);
            assertEquals(teleporter1.getExitDirectionXY(blockAtLowerEntry)[1], +1);
            //A block entering a teleporter from the right should be headed left, thus the direction is represented by a -1 x-coordinate
            assertEquals(teleporter1.getExitDirectionXY(blockAtRightEntry)[0], -1);
            assertEquals(teleporter1.getExitDirectionXY(blockAtRightEntry)[1], 0);
            //A block entering a teleporter from the left should be headed right, thus the direction is represented by a +1 x-coordinate
            assertEquals(teleporter1.getExitDirectionXY(blockAtLeftEntry)[0], +1);
            assertEquals(teleporter1.getExitDirectionXY(blockAtLeftEntry)[1], 0);
        }
        @Test
        @DisplayName("Check that a teleporter returns direction null when disconnected")
        public void testGetExitPointDirectionXYDisconnected() {
            int tpX = teleporter1.getX();
            int tpY = teleporter1.getY();
            BlockAbstract blockAtRightEntry = new TeleporterBlock(tpX+1, tpY);
            assertNull(teleporter1.getExitDirectionXY(blockAtRightEntry));
        }
        @Test
        @DisplayName("Check that a connected teleporter returns null when the entering block is not standing at an entry point")
        public void testGetExitPointDirectionXYConnectedWithBlockNotAtEntryPoint() {
            teleporter1.setConnection(teleporter2);
            int tpX = teleporter1.getX();
            int tpY = teleporter1.getY();
            BlockAbstract blockNotAtEntryPoint = new TeleporterBlock(tpX+1, tpY+1);
            assertNull(teleporter1.getExitDirectionXY(blockNotAtEntryPoint));
           
        }

        @Test
        @DisplayName("Check that a connected teleporter CAN be entered by a block that is standing at one of the teleporter's entry points.")
        public void testCanBlockAtEntryPointEnterConnectedTeleporter() {
            teleporter1.setConnection(teleporter2);
            int tpX = teleporter1.getX();
            int tpY = teleporter1.getY();
            BlockAbstract blockAtUpperEntry = new TeleporterBlock(tpX, tpY+1);
            BlockAbstract blockAtLowerEntry = new TeleporterBlock(tpX, tpY-1);
            BlockAbstract blockAtRightEntry = new TeleporterBlock(tpX+1, tpY);
            BlockAbstract blockAtLeftEntry = new TeleporterBlock(tpX-1, tpY);
            assertTrue(teleporter1.canBlockEnter(blockAtUpperEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtLowerEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtRightEntry));
            assertTrue(teleporter1.canBlockEnter(blockAtLeftEntry));
        }
        @Test
        @DisplayName("Check that a connected teleporter CAN NOT be entered by a block that is NOT standing at one of the teleporter's entry points.")
        public void testCanBlockNotAtEntryPointEnterConnectedTeleporter() {
            teleporter1.setConnection(teleporter2);
            BlockAbstract blockNotAtEntry = new TeleporterBlock(0, 0);
            assertFalse(teleporter1.canBlockEnter(blockNotAtEntry));
            BlockAbstract blockAtTeleporterCoordinates = new TeleporterBlock(0, 0);
            assertFalse(teleporter1.canBlockEnter(blockAtTeleporterCoordinates));
            BlockAbstract blockNearEntry = new TeleporterBlock(1, 1);
            assertFalse(teleporter1.canBlockEnter(blockNearEntry ));
        }
        @Test
        @DisplayName("Check that disconnected teleporters can not be entered.")
        public void testCanBlockEnterDisconnectedTeleporter() {
            BlockAbstract block = new TeleporterBlock(1, 0);
            assertFalse(teleporter1.canBlockEnter(block), "A block should not be able to enter a disconnected teleporter, even while standing at an entry point.");
        }
    }

    //Tests constructor and class methods specific to TeleporterBlock
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        TeleporterBlock teleporter = new TeleporterBlock(0, 0);
        assertTrue(teleporter.isTeleporter());
    }
}
