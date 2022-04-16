package pushrocks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import pushrocks.model.BlockAbstract;
import pushrocks.model.PortalWallBlock;

public class PortalWallBlockTest {
    // private char[] validTypes = new char[] {'w', 'v', 'u'};
    // private String[] validDirectionsWall = new String[] {null};
    private String[] validDirectionsPortal = new String[] {"up", "down", "right", "left"};

    //Tests for constructor and methods inherited from BlockAbstract
    private void testConstructorCoordinates(int x, int y) {
        BlockAbstract neutralValueCoordinates = new PortalWallBlock(x, y);
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
        PortalWallBlock constructedValidType = new PortalWallBlock(0, 0);
        assertEquals('w', constructedValidType.getType(), "Portal-wall block type should be 'w' after construction.");
        assertFalse(constructedValidType.getState(), "Portal-wall block state should be false after construction.");
        //Direction properties inherited by DirectedBlock.
        assertNull(constructedValidType.getDirection(), "Portal-wall block direction should be null after construction.");
        assertEquals(0, constructedValidType.getDirectionXY()[0], "Portal-wall block x-coordinate direction should be 0 after construction.");
        assertEquals(0, constructedValidType.getDirectionXY()[1], "Portal-wall block y-coordinate direction should be 0 after construction.");
        //Connection properties inherited by ObstacleBlock.
        assertNull(constructedValidType.getConnection(), "Portal-wall block connection should be null after construction.");
    }

    @Test
    @DisplayName("Test that hasCollision() returns the correct truth value for the blocks of this class.")
    public void testHasCollision() {
        BlockAbstract PortalWallBlock = new PortalWallBlock(0, 0);
        assertTrue(PortalWallBlock.hasCollision(), "Directed blocks, and thus by extension portal-wall blocks, have collision, thus hasCollision() should always return true");
    }

    @Nested
    //The first few of the following connection tests are inspired by the partner excercise.
    class TestNestConnectionMethods {
        private PortalWallBlock portalWall1;
        private PortalWallBlock portalWall2;

        @BeforeEach
        private void setup() {
            portalWall1 = new PortalWallBlock(0, 0);
            portalWall2 = new PortalWallBlock(5, 5);
        }
        
        @Test
        @DisplayName("Check that the correct type is assigned when setting the portal to be either portal one (v) or portal two (u).")
        public void testSetPortalTypes() {
            portalWall1.setPortal(true, validDirectionsPortal[0], null);
            assertEquals('v', portalWall1.getType(), "A portal set to be portal one should have its type set to 'v");
            portalWall2.setPortal(false, validDirectionsPortal[0], null);
            assertEquals('u', portalWall2.getType(), "A portal set to be portal one should have its type set to 'u");
        }
        @Test
        @DisplayName("Check that the correct direction is assigned when setting the portal with a given valid direction.")
        public void testSetPortalValidDirections() {
            for (String direction : validDirectionsPortal) {
                PortalWallBlock portal = new PortalWallBlock(0, 0);
                assertNull(portal.getDirection(), "portal-walls should have their direction set to null after construction.");
                portal.setPortal(true, direction, null);
                assertEquals(direction, portal.getDirection(), "portal wall should have been set to a specific direction after being set to be a portal. Direction should have been: " + direction);
            }
            PortalWallBlock portal = new PortalWallBlock(0, 0);
            assertNull(portal.getDirection(), "portal-walls should have their direction set to null after construction.");
            portal.setPortal(true, "RIGht", null);
            assertEquals("right", portal.getDirection(), "portal wall should have been set to a specific direction after being set to be a portal. Direction should have been: " + "RIGht");
        }
        @Test
        @DisplayName("Check that an IllegalArgumentException is thrown when attempting to set a portal with an invalid direction.")
        public void testSetPortalInvalidDirections() {
            String[] invalidDirectionsPortal = new String[] {"NorthToVabbi", "", null};
            for (String direction : invalidDirectionsPortal) {
                PortalWallBlock portal = new PortalWallBlock(0, 0);
                assertNull(portal.getDirection(), "portal-walls should have their direction set to null after construction.");
                assertThrows(IllegalArgumentException.class, 
                () -> portal.setPortal(true, direction, null),
                "IllegalArgument should be thrown when setting a portal with an invalid direction. Direction was: " + direction);
            }
        }

        @Test
        @DisplayName("portalWall1 should be connected to portalWall2 and vice versa after a portalWall1 has been set to portal with portalWall2 as a connection when both are portals of opposing types.")
        public void testSetPortalConnectionOpposingType() {
            portalWall1.setPortal(true, "right", null);
            portalWall2.setPortal(false, "right", portalWall1);
            assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
            assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
            assertTrue(portalWall1.getState(), "portalWall1 should have its state set to true once it is connected to another portal");
            assertTrue(portalWall2.getState(), "portalWall2 should have its state set to true once it is connected to another portal");
        }
        @Test
        @DisplayName("Attempting to set a portal with a connection of identical portal-type to the portal should throw an IllegalArgumentException, and the portalWall objects should remain unchanged.")
        public void testSetPortalConnectionIdenticalType() {
            portalWall1.setPortal(true, "right", null);
            assertThrows(IllegalArgumentException.class, 
            () -> portalWall2.setPortal(true, "right", portalWall1),
            "IllegalArgument should be thrown when setting a portal with an invalid direction. Direction was: ");
            //The portal walls' connection should remain unchanged
            assertNull(portalWall1.getConnection(), "portalWall1 should not be connected to portalWall2, after it failed to be set to a portal.");
            assertNull(portalWall2.getConnection(), "portalWall2 should not have a connection after it failed to be set to a portal.");
            //The portal walls' type should remain unchanged
            assertEquals('w', portalWall2.getType(), "portalWall2' type should remain as a wall ('w') after failing to be set to a portal.");
            assertEquals('v', portalWall1.getType(), "portalWall1' type should remain as portalOne ('v') after portalWall2 failed to be set to a portal.");
            //The portal walls' direction should remain unchanged
            assertNull(portalWall2.getDirection(), "portalWall2's direction should remain null after failing to be set to a portal.");
            assertEquals("right", portalWall1.getDirection(), "portalWall2's direction should remain 'right' after portalWall2 failed to be set to a portal.");
            //The portal walls' state should remain false
            assertFalse(portalWall2.getState(), "portalWall2's state should remain false after failing to be set to a portal.");
            assertFalse(portalWall1.getState(), "portalWall2's state should remain false after failing to be set to a portal.");
        }
        @Test
        @DisplayName("Attempting to set a portal with itself as a connection should set the connection to null, but otherwise change properties accordingly")
        public void testSetPortalConnectionToSelf() {
            portalWall1.setPortal(true, "right", null);
            assertNull(portalWall1.getConnection());
            portalWall1.setPortal(false, "left", portalWall1);
            //The portal's connection should remain null;
            assertNull(portalWall1.getConnection(), "portalWall1's connection should remain null, as it can not have itself as a connection.");
            //The portal's type should be changed to the opposing type, according to the input.
            assertEquals('u', portalWall1.getType(), "portalWall1' should have been changed to 'u'.");
            //The portal's direction should change to 'left', according to the input.
            assertEquals("left", portalWall1.getDirection(), "portalWall2's direction should have been changed to 'left', as according to the input.");
            //The portal's state should remain false
            assertFalse(portalWall1.getState(), "portalWall1's state should remain false after connecting to itself, as it should not be connected to itself, and thus has null as connection.");
        }

    //     @Test
    //     @DisplayName("Setting a connected portalWall's connection to null should remove the connection of both that portalWall and its connection, and their state should be set to false.")
    //     public void testSetConnectionNull() {
    //         portalWall1.setConnection(portalWall2);
    //         assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
    //         assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
    //         portalWall1.setConnection(null);
    //         assertNull(portalWall1.getConnection(), "portalWall1 should no longer have a connection after the setConnection(null) was called on it.");
    //         assertNull(portalWall2.getConnection(), "portalWall2 should no longer have a connection after the setConnection(null) was called on its connection.");
    //         assertFalse(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been set to null.");
    //         assertFalse(portalWall2.getState(), "portalWall2 should have its state set to false once its connection's connection set to null.");
    //     }
    //     @Test
    //     @DisplayName("Removing a connected portalWall's connection should remove the connection of both that portalWall and its connection, and their state should be set to false.")
    //     public void testRemoveConnection() {
    //         portalWall1.setConnection(portalWall2);
    //         assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
    //         assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
    //         portalWall1.removeConnection();
    //         assertNull(portalWall1.getConnection(), "portalWall1 should no longer have a connection after the removeConnection() was called on it.");
    //         assertNull(portalWall2.getConnection(), "portalWall2 should no longer have a connection after the removeConnection() was called on its connection.");
    //         assertFalse(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been removed.");
    //         assertFalse(portalWall2.getState(), "portalWall2 should have its state set to false once its connection's connection has been removed.");
    //     }
    //     @Test
    //     @DisplayName("Removing a connected portalWall's connection should remove the connection of both that portalWall and its connection, and their state should be set to false.")
    //     public void testReplaceConnectionWithNewConnection() {
    //         portalWall1.setConnection(portalWall2);
    //         assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
    //         assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
    //         PortalWallBlock portalWall3 = new PortalWallBlock(0, 0);
    //         //portalWall3 replaces portalWall2 as portalWall1's connection
    //         portalWall1.setConnection(portalWall3);
    //         assertEquals(portalWall3, portalWall1.getConnection(), "portalWall1 should be connected to portalWall3");
    //         assertEquals(portalWall1, portalWall3.getConnection(), "portalWall3 should be connected to portalWall1");
    //         assertNull(portalWall2.getConnection(), "portalWall2 should no longer have a connection once it's been replaced by portalWall3 as portalWall1's connection.");
    //         assertTrue(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been removed.");
    //         assertTrue(portalWall3.getState(), "portalWall2 should have its state set to false once its connection's connection has been removed.");
    //         assertFalse(portalWall2.getState(), "portalWall2 should have its state set to false once its connection's connection has been removed.");
    //     }
    //     @Test
    //     @DisplayName("Attempting to connect a portalWall to itself should set the connection to null.")
    //     public void testSetConnectionToSelf() {
    //         portalWall1.setConnection(portalWall1);
    //         assertNull(portalWall1.getConnection(), "portalWall connection should stay null after attempting to connect to itself.");
    //         portalWall1.setConnection(portalWall2);
    //         assertNotNull(portalWall1.getConnection(), "portalWall connection should no longer be null after connecting to another portalWall.");
    //         portalWall1.setConnection(portalWall1);
    //         assertNull(portalWall1.getConnection(), "portalWall connection should be back to null after attempting to connect to itself again.");
    //         assertNull(portalWall2.getConnection(), "the previously connected portalWall should no longer be connected to the portalWall that attempted to connect to itself.");
    //     }

    //     @Test
    //     @DisplayName("Check that a connected portalWall returns a list containing correct entry point coordinates")
    //     public void testGetEntryPointsConnectedportalWall() {
    //         portalWall1.setConnection(portalWall2);
    //         int tpX = portalWall1.getX();
    //         int tpY = portalWall1.getY();
    //         //Entry points are always placed one step away from the portalWall itself, either horizontal or vertical, not both.
    //         int[][] entryPoints = portalWall1.getEntryPointsXY();
    //         for (int[] entry : entryPoints) {
    //             //If the provided entry point is one right step away from the portalWall
    //             if (entry[0] == tpX + 1) {
    //                 //then the y coordinate must be the same for both the portalWall and the entry point.
    //                 assertEquals(tpY, entry[1]);
    //             }
    //             //If the provided entry point is one left step away from the portalWall
    //             else if (entry[0] == tpX - 1) {
    //                 //then the y coordinate must be the same for both the portalWall and the entry point.
    //                 assertEquals(tpY, entry[1]);
    //             }
    //             //If the provided entry point is one upward step away from the portalWall
    //             else if (entry[1] == tpY + 1) {
    //                 //then the x coordinate must be the same for both the portalWall and the entry point.
    //                 assertEquals(tpX, entry[0]);
    //             }
    //             //If the provided entry point is one downward step away from the portalWall
    //             else if (entry[1] == tpY - 1) {
    //                 //then the x coordinate must be the same for both the portalWall and the entry point.
    //                 assertEquals(tpX, entry[0]);
    //             }
    //             else {
    //                 //In all other cases the coordinate is not a correctly placed entry point.
    //                 assertFalse(true, "The coordinate (x:" + entry[0] + ", y:" + entry[1] + ") is not valid coordinate for a portalWall with coordinates: (x:" + tpX + ", y:" + tpY + ").");
    //             }
    //         }
    //     }
    //     @Test
    //     @DisplayName("Check that a disconnected portalWall does not return any entry point coordinates when getEntryPointsXY() is called.")
    //     public void testGetEntryPointsXYDisconnectedportalWall() {
    //         assertNull(portalWall1.getConnection());
    //         assertNull(portalWall1.getEntryPointsXY(), "a portalWall without a connection should not return an entry point.");
    //     }
    //     @Test
    //     @DisplayName("Check that a connected portalWall returns a correct exit point coordinate when it is connected and the entering block is standing at an entry point.")
    //     public void testGetExitPointXYConnectedportalWallWhileAtEntryPoint() {
    //         portalWall1.setConnection(portalWall2);
    //         int tp1X = portalWall1.getX();
    //         int tp1Y = portalWall1.getY();
    //         BlockAbstract blockAtUpperEntry = new PortalWallBlock(tp1X, tp1Y+1);
    //         BlockAbstract blockAtLowerEntry = new PortalWallBlock(tp1X, tp1Y-1);
    //         BlockAbstract blockAtRightEntry = new PortalWallBlock(tp1X+1, tp1Y);
    //         BlockAbstract blockAtLeftEntry = new PortalWallBlock(tp1X-1, tp1Y);
    //         int tp2X = portalWall2.getX();
    //         int tp2Y = portalWall2.getY();
    //         //When entered from above the exit point should be one step below the connected portalWall.
    //         assertEquals(portalWall1.getExitPointXY(blockAtUpperEntry)[0], tp2X, "The exit point should have an x-coordinate value equal to the connected portalWall");
    //         assertEquals(portalWall1.getExitPointXY(blockAtUpperEntry)[1], tp2Y-1, "The exit point should have a y-coordinate value one less than the connected portalWall");
    //         //When entered from below the exit point should be one step above connected portalWall.      
    //         assertEquals(portalWall1.getExitPointXY(blockAtLowerEntry)[0], tp2X, "The exit point should have an x-coordinate value equal to the connected portalWall");
    //         assertEquals(portalWall1.getExitPointXY(blockAtLowerEntry)[1], tp2Y+1, "The exit point should have a y-coordinate value one greater than the connected portalWall");
    //         //When entered from the right the exit point should be one step left of the connected portalWall.
    //         assertEquals(portalWall1.getExitPointXY(blockAtRightEntry)[0], tp2X-1, "The exit point should have a x-coordinate value one less than the connected portalWall");
    //         assertEquals(portalWall1.getExitPointXY(blockAtRightEntry)[1], tp2Y, "The exit point should have a y-coordinate value equal to the connected portalWall");
    //         //When entered from the left the exit point should be one step right of the connected portalWall.
    //         assertEquals(portalWall1.getExitPointXY(blockAtLeftEntry)[0], tp2X+1, "The exit point should have a x-coordinate value one greater than the connected portalWall");
    //         assertEquals(portalWall1.getExitPointXY(blockAtLeftEntry)[1], tp2Y, "The exit point should have a y-coordinate value equal to the connected portalWall");
    //     }
    //     @Test
    //     @DisplayName("Check that a connected portalWall does not return any entry point coordinates when the entering block is not standing at an entry point.")
    //     public void testGetExitPointConnectedportalWallWhileNotAtEntryPoint() {
    //         BlockAbstract enteringBlock = new PortalWallBlock(1, 1);
    //         portalWall1.setConnection(portalWall2);
    //         assertNull(portalWall1.getExitPointXY(enteringBlock), "a portalWall with a connection should not return an entry point if the entring block does not stand at one of the portalWall's entry points.");
    //     }
    //     @Test
    //     @DisplayName("Check that a disconnected portalWall does not return any entry point coordinates.")
    //     public void testGetExitPointDisconnectedportalWall() {
    //         BlockAbstract enteringBlock = new PortalWallBlock(1, 0);
    //         assertNull(portalWall1.getConnection());
    //         assertNull(portalWall1.getExitPointXY(enteringBlock), "a portalWall without a connection should not return an entry point.");
    //     }

    //     @Test
    //     @DisplayName("Check that a connected portalWall returns the correct exit direction for getExitDirectionXY()")
    //     public void testGetExitPointDirectionXYConnectedWithBlockAtEntryPoint() {
    //         portalWall1.setConnection(portalWall2);
    //         int tpX = portalWall1.getX();
    //         int tpY = portalWall1.getY();
    //         BlockAbstract blockAtUpperEntry = new PortalWallBlock(tpX, tpY+1);
    //         BlockAbstract blockAtLowerEntry = new PortalWallBlock(tpX, tpY-1);
    //         BlockAbstract blockAtRightEntry = new PortalWallBlock(tpX+1, tpY);
    //         BlockAbstract blockAtLeftEntry = new PortalWallBlock(tpX-1, tpY);
    //         //A block entering a portalWall from above should be headed down, thus the direction is represented by a -1 y-coordinate
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtUpperEntry)[0], 0);
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtUpperEntry)[1], -1);
    //         //A block entering a portalWall from below should be headed up, thus the direction is represented by a +1 y-coordinate
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtLowerEntry)[0], 0);
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtLowerEntry)[1], +1);
    //         //A block entering a portalWall from the right should be headed left, thus the direction is represented by a -1 x-coordinate
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtRightEntry)[0], -1);
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtRightEntry)[1], 0);
    //         //A block entering a portalWall from the left should be headed right, thus the direction is represented by a +1 x-coordinate
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtLeftEntry)[0], +1);
    //         assertEquals(portalWall1.getExitDirectionXY(blockAtLeftEntry)[1], 0);
    //     }
    //     @Test
    //     @DisplayName("Check that a portalWall returns null when disconnected")
    //     public void testGetExitPointDirectionXYDisconnected() {
    //         int tpX = portalWall1.getX();
    //         int tpY = portalWall1.getY();
    //         BlockAbstract blockAtRightEntry = new PortalWallBlock(tpX+1, tpY);
    //         assertNull(portalWall1.getExitDirectionXY(blockAtRightEntry));
    //     }
    //     @Test
    //     @DisplayName("Check that a connected portalWall returns null when the entering block is not standing at an entry point")
    //     public void testGetExitPointDirectionXYConnectedWithBlockNotAtEntryPoint() {
    //         portalWall1.setConnection(portalWall2);
    //         int tpX = portalWall1.getX();
    //         int tpY = portalWall1.getY();
    //         BlockAbstract blockNotAtEntryPoint = new PortalWallBlock(tpX+1, tpY+1);
    //         assertNull(portalWall1.getExitDirectionXY(blockNotAtEntryPoint));
        
    //     }

    //     @Test
    //     @DisplayName("Check that a connected portalWall CAN be entered by a block that is standing at one of the portalWall's entry points.")
    //     public void testCanBlockAtEntryPointEnterConnectedportalWall() {
    //         portalWall1.setConnection(portalWall2);
    //         int tpX = portalWall1.getX();
    //         int tpY = portalWall1.getY();
    //         BlockAbstract blockAtUpperEntry = new PortalWallBlock(tpX, tpY+1);
    //         BlockAbstract blockAtLowerEntry = new PortalWallBlock(tpX, tpY-1);
    //         BlockAbstract blockAtRightEntry = new PortalWallBlock(tpX+1, tpY);
    //         BlockAbstract blockAtLeftEntry = new PortalWallBlock(tpX-1, tpY);
    //         assertTrue(portalWall1.canBlockEnter(blockAtUpperEntry));
    //         assertTrue(portalWall1.canBlockEnter(blockAtLowerEntry));
    //         assertTrue(portalWall1.canBlockEnter(blockAtRightEntry));
    //         assertTrue(portalWall1.canBlockEnter(blockAtLeftEntry));
    //     }
    //     @Test
    //     @DisplayName("Check that a connected portalWall CAN NOT be entered by a block that is NOT standing at one of the portalWall's entry points.")
    //     public void testCanBlockNotAtEntryPointEnterConnectedportalWall() {
    //         portalWall1.setConnection(portalWall2);
    //         BlockAbstract blockNotAtEntry = new PortalWallBlock(0, 0);
    //         assertFalse(portalWall1.canBlockEnter(blockNotAtEntry));
    //         BlockAbstract blockAtportalWallCoordinates = new PortalWallBlock(0, 0);
    //         assertFalse(portalWall1.canBlockEnter(blockAtportalWallCoordinates));
    //         BlockAbstract blockNearEntry = new PortalWallBlock(1, 1);
    //         assertFalse(portalWall1.canBlockEnter(blockNearEntry ));
    //     }
    //     @Test
    //     @DisplayName("Check that disconnected portalWalls can not be entered.")
    //     public void testCanBlockEnterDisconnectedportalWall() {
    //         BlockAbstract block = new PortalWallBlock(1, 0);
    //         assertFalse(portalWall1.canBlockEnter(block), "A block should not be able to enter a disconnected portalWall, even while standing at an entry point.");
    //     }
    // }

    // //Tests constructor and class methods specific to PortalWallBlock
    // @Test
    // @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    // public void testTypeChecks() {
    //     PortalWallBlock portalWall = new PortalWallBlock(0, 0);
    //     assertTrue(portalWall.isportalWall());
    }

}
