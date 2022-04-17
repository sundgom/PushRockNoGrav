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
        public void testSetPortalConnectionIsOpposingType() {
            portalWall1.setPortal(true, "right", null);
            portalWall2.setPortal(false, "right", portalWall1);
            assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
            assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
            assertTrue(portalWall1.getState(), "portalWall1 should have its state set to true once it is connected to another portal");
            assertTrue(portalWall2.getState(), "portalWall2 should have its state set to true once it is connected to another portal");
        }
        @Test
        @DisplayName("Attempting to set a portal with a connection of identical portal-type to the portal should throw an IllegalArgumentException, and the portalWall objects should remain unchanged.")
        public void testSetPortalConnectionIsIdenticalType() {
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
        public void testSetPortalConnectionIsSelf() {
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

        @Test
        @DisplayName("Setting a portal-wall to be a portal with the connection-parameter set to null should remove the connection of both that portalWall and its connection, and their state should be set to false.")
        public void testSetPortalConnectionIsNull() {
            portalWall1.setPortal(true, "right", null);
            portalWall2.setPortal(false, "right", portalWall1);
            assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
            assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
            portalWall1.setPortal(true, "right", null);
            assertNull(portalWall1.getConnection(), "portalWall1 should no longer have a connection after the setConnection(null) was called on it.");
            assertNull(portalWall2.getConnection(), "portalWall2 should no longer have a connection after the setConnection(null) was called on its connection.");
            assertFalse(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been set to null.");
            assertFalse(portalWall2.getState(), "portalWall2 should have its state set to false once its connection's connection set to null.");
        }
        @Test
        @DisplayName("Clearing a portal that is connected should: remove connection for both portals, set type to wall ('w') for the cleared portal while keeping the type unchanged for " 
                    + "its connection, set direction to null for the cleared portal while keeping the direction unchanged for its connection, and change the state of both from true to false.")
        public void testClearPortalConnectedPortal() {
            portalWall1.setPortal(true, "right", null);
            portalWall2.setPortal(false, "right", portalWall1);
            assertEquals(portalWall2, portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
            assertEquals(portalWall1, portalWall2.getConnection(), "portalWall2 should be connected to portalWall1");
            assertEquals('v', portalWall1.getType(), "portalWall1's type should be 'v' for portal one.");
            assertEquals('u', portalWall2.getType(), "portalWall2's type should be 'u' for portal two");
            assertEquals("right", portalWall1.getDirection(), "portalWall1's direction should be set to 'right'.");
            assertEquals("right", portalWall2.getDirection(), "portalWall2's direction should be set to 'right'.");
            assertTrue(portalWall1.getState(), "portalWall1 should have its state set to true once its been connected to portalWall2 through portalWall2.setPortal(...) call.");
            assertTrue(portalWall2.getState(), "portalWall2 should have its state set to true once its been connected to portalWall1 through portalWall2.setPortal(...) call.");
            //Clear the portal held by portalWall1
            portalWall1.clearPortal();
            assertNull(portalWall1.getConnection(), "portalWall1 should no longer have a connection after the clearPortal() was called on it.");
            assertNull(portalWall2.getConnection(), "portalWall2 should no longer have a connection after the clearPortal() was called on its connection.");
            assertEquals('w', portalWall1.getType(), "portalWall1's type should be 'w' for walll.");
            assertEquals('u', portalWall2.getType(), "portalWall2's type should be 'u' for portal two");
            assertNull(portalWall1.getDirection(), "portalWall1's direction should be set to null.");
            assertEquals("right", portalWall2.getDirection(), "portalWall2's direction should remain 'right'.");
            assertFalse(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been removed.");
            assertFalse(portalWall2.getState(), "portalWall2 should have its state set to false once its connection's connection has been removed.");
        }
        @Test
        @DisplayName("Clearing a portal that is disconnected should: keep connection null, change its type to wall 'w' as it no longer holds a portal, set direction to null, keep state false.")
        public void testClearPortalDisconnectedPortal() {
            portalWall1.setPortal(true, "right", null);
            assertNull(portalWall1.getConnection(), "portalWall1 should be connected to portalWall2");
            assertEquals('v', portalWall1.getType(), "portalWall1's type should be 'v' for portal one.");
            assertEquals("right", portalWall1.getDirection(), "portalWall1's direction should be set to 'right'.");
            assertFalse(portalWall1.getState(), "portalWall1 should have its state set to true once its been connected to portalWall2 through portalWall2.setPortal(...) call.");
            //Clear the portal held by portalWall1
            portalWall1.clearPortal();
            assertNull(portalWall1.getConnection(), "portalWall1 should no longer have a connection after the clearPortal() was called on it.");
            assertEquals('w', portalWall1.getType(), "portalWall1's type should be 'w' for walll.");
            assertNull(portalWall1.getDirection(), "portalWall1's direction should be set to null.");
            assertFalse(portalWall1.getState(), "portalWall1 should have its state set to false once its connection has been removed.");
        }
        @Test
        @DisplayName("Calling 'clearPortal(..)' on a portal-wall whose type is wall 'w' should change nothing as there is no portal to clear.")
        public void testClearPortalWall() {
            assertNull(portalWall1.getConnection(), "portalWall1's connection should be null as walls can not be connected");
            assertEquals('w', portalWall1.getType(), "portalWall1's type should be 'w' for wall.");
            assertNull(portalWall1.getDirection(), "portalWall1's direction should be null, as it is a wall.");
            assertFalse(portalWall1.getState(), "portalWall1's state should be false as walls can not be connected.");
            //Clear the portal held by portalWall1
            portalWall1.clearPortal();
            assertNull(portalWall1.getConnection(), "portalWall1's connection should remain null.");
            assertEquals('w', portalWall1.getType(), "portalWall1's type should remain 'w' for wall.");
            assertNull(portalWall1.getDirection(), "portalWall1's direction should remain null.");
            assertFalse(portalWall1.getState(), "portalWall1's state should remain false.");
        }

        @Test
        @DisplayName("Check that a connected portalWall returns a list containing correct entry point coordinates")
        public void testGetEntryPointsConnectedPortal() {
            for (String direction : validDirectionsPortal) {
                portalWall1.setPortal(true, direction, null);
                portalWall2.setPortal(false, "left", portalWall1);
                int tpX = portalWall1.getX();
                int tpY = portalWall1.getY();
                //Portals have a single entry point, and it is always placed one step away in the direction of the portal from the portal itself.
                int[][] entryPoints = portalWall1.getEntryPointsXY();
                //the method getEntryPointsXY() is inherited from ObstacleBlock and should return every entry point of the given ObstacleBlock.
                for (int[] entry : entryPoints) {
                    //for the case of portals there is only a single entry point, thus this for loop should only run once.
                    switch (direction) {
                        //check that the direction the portal was set with results in a direction that is correct for that direction.
                        case "up":
                            assertEquals(tpX, entry[0], "when the entry portal's direction is up, then the entry point should have an x-coordinate equal to the portal itself.");
                            assertEquals(tpY+1, entry[1], "when the entry portal's direction is up, then the entry point should have a y-coordinate one greater than the portal itself.");
                            break;
                        case "down":
                            assertEquals(tpX, entry[0], "when the entry portal's direction is down, then the entry point should have an x-coordinate equal to the portal itself.");
                            assertEquals(tpY-1, entry[1], "when the entry portal's direction is down, then the entry point should have a y-coordinate one less than the portal itself.");
                            break;
                        case "right":
                            assertEquals(tpX+1, entry[0], "when the entry portal's direction is right, then the entry point should have an x-coordinate one greater than the portal itself.");
                            assertEquals(tpY, entry[1], "when the entry portal's direction is right, then the entry point should have a y-coordinate equal to the the portal itself.");
                            break;
                        case "left":
                            assertEquals(tpX-1, entry[0], "when the entry portal's direction is left, then the entry point should have an x-coordinate one less than the portal itself.");
                            assertEquals(tpY, entry[1], "when the entry portal's direction is left, then the entry point should have a y-coordinate equal to the the portal itself.");
                            break;
                        default:
                            assertTrue(false, "portals should never have a direction other than up, down, right or left.");
                    }
                }
            }
        }
        @Test
        @DisplayName("Check that a disconnected portal does not return any entry point coordinates when getEntryPointsXY() is called.")
        public void testGetEntryPointsXYWall() {
            assertNull(portalWall1.getConnection());
            assertNull(portalWall1.getEntryPointsXY(), "a portalWall without a connection should not return entry points.");
        }
        @Test
        @DisplayName("Check that a disconnected portal does not return any entry point coordinates when getEntryPointsXY() is called.")
        public void testGetEntryPointsXYDisconnectedPortal() {
            portalWall1.setPortal(true, "right", null);
            assertNull(portalWall1.getConnection());
            assertNull(portalWall1.getEntryPointsXY(), "a portalWall without a connection should not return entry points.");
        }
        @Test
        @DisplayName("Check that a connected portal returns the correct exit point coordinate and direction when the entering block is standing at an entry point.")
        public void testGetExitPointXYConnectedPortalWhileAtEntryPoint() {
            for (String direction1 : validDirectionsPortal) {
                portalWall1.setPortal(true, direction1, null);
                int tp1X = portalWall1.getX();
                int tp1Y = portalWall1.getY();
                int entryX = tp1X;
                int entryY = tp1Y;
                switch (direction1) {
                    case "up":
                        entryY++;
                        break;
                    case "down":
                        entryY--;
                        break;
                    case "right":
                        entryX++;
                        break;
                    case "left":
                        entryX--;
                        break;
                }
                BlockAbstract entryBlock = new PortalWallBlock(entryX, entryY);
                for (String direction2 : validDirectionsPortal) {
                    portalWall2.setPortal(false, direction2, portalWall1);
                    int tp2X = portalWall2.getX();
                    int tp2Y = portalWall2.getY();
                    int exitX = tp2X;
                    int exitY = tp2Y;
                    switch (direction2) {
                        case "up":
                            exitY++;
                            break;
                        case "down":
                            exitY--;
                            break;
                        case "right":
                            exitX++;
                            break;
                        case "left":
                            exitX--;
                            break;
                    }
                    assertEquals(exitX, portalWall1.getExitPointXY(entryBlock)[0]);
                    assertEquals(exitY, portalWall1.getExitPointXY(entryBlock)[1]);
                    assertEquals(exitX-tp2X, portalWall1.getExitDirectionXY(entryBlock)[0]);
                    assertEquals(exitY-tp2Y, portalWall1.getExitDirectionXY(entryBlock)[1]);
                }
            }
        }
        @Test
        @DisplayName("Check that a connected portal does not return any entry point coordinates when the entering block is not standing at an entry point.")
        public void testGetExitPointConnectedportalWallWhileNotAtEntryPoint() {
            portalWall1.setPortal(true, "up", null);
            portalWall2.setPortal(false, "right", portalWall1);
            assertNotNull(portalWall1.getConnection());
            int x = portalWall1.getX();
            int y = portalWall1.getY();
            //create blocks that should enter the portal from various positions in relation to that portal's coordinate.
            BlockAbstract abovePortal = new PortalWallBlock(x, y+1);
            BlockAbstract wayAbovePortal = new PortalWallBlock(x, y+2);
            BlockAbstract underPortal = new PortalWallBlock(x, y-1);
            BlockAbstract rightOfPortal = new PortalWallBlock(x+1, y);
            BlockAbstract leftOfPortal = new PortalWallBlock(x-1, y);
            //check that getExitPointXY returns null when the entering block is not standing the portal's entry point.
            assertNotNull(portalWall1.getExitPointXY(abovePortal), "exit point should not be null the entry block is at the portal's entry point.");
            assertNull(portalWall1.getExitPointXY(wayAbovePortal), "exit point should be null when the entry block is not at the portal's entry point.");
            assertNull(portalWall1.getExitPointXY(underPortal), "exit point should be null when the entry block is not at the portal's entry point.");
            assertNull(portalWall1.getExitPointXY(rightOfPortal), "exit point should be null when the entry block is not at the portal's entry point.");
            assertNull(portalWall1.getExitPointXY(leftOfPortal), "exit point should be null when the entry block is not at the portal's entry point.");
        }
        @Test
        @DisplayName("Check that a disconnected portal does not return any exit point coordinates.")
        public void testGetExitPointDisconnectedportalWall() {
            portalWall1.setPortal(true, "right", null);
            //create a block that is standing ontop of the portal's entry point (one step away from the portal's coordinates in the direction the portal is facing: right)
            BlockAbstract enteringBlock = new PortalWallBlock(portalWall1.getX()+1, portalWall1.getY());
            assertNull(portalWall1.getConnection(), "the portal should not have a connection.");
            assertNull(portalWall1.getExitPointXY(enteringBlock), "a disconnected portal should not return an entry point, even if the entry block is at the portal's entry point.");
        }

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
    }
    //Tests constructor and class methods specific to PortalWallBlock
    @Test
    @DisplayName("Test that type checks specific to this class returns the correct truth value according to their current type.")
    public void testTypeChecks() {
        PortalWallBlock wall = new PortalWallBlock(0, 0);
        assertTrue(wall.isWall());
        PortalWallBlock portalOne = new PortalWallBlock(0, 0);
        portalOne.setPortal(true, "right", null);
        assertTrue(portalOne.isPortal());
        assertTrue(portalOne.isPortalOne());
        PortalWallBlock portalTwo = new PortalWallBlock(0, 0);
        portalTwo.setPortal(false, "right", null);
        assertTrue(portalTwo.isPortal());
        assertTrue(portalTwo.isPortalTwo());
    }

}
