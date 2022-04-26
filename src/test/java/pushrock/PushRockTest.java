package pushrock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pushrock.model.BlockAbstract;
import pushrock.model.MoveableBlock;
import pushrock.model.PortalWallBlock;
import pushrock.model.PushRock;

public class PushRockTest {
    @Test
    @DisplayName("Help me.")
    public void testConstructor() {

    }
    
    PushRock pushRock;

    @BeforeEach
    public void setup() {
        this.pushRock = new PushRock("Test", " dprd@ ", "rrg");
    }


    @Test
    @DisplayName("Check that interacting with a block retrieved from the getTopBlockCopy(..) method does not alter the state of the game.")
    public void testGetTopBlockCopy() {
        PushRock pushRock = new PushRock("Test", "prdwt@", "rrg"); 
        BlockAbstract playerCopy1 = pushRock.getTopBlockCopy(0, 0);
        assertTrue(playerCopy1 instanceof MoveableBlock && ((MoveableBlock) playerCopy1).isPlayer());
        ((MoveableBlock) playerCopy1).setState(true);
        ((MoveableBlock) playerCopy1).moveInDirection("right");
        BlockAbstract playerCopy2 = pushRock.getTopBlockCopy(0, 0);
        assertTrue(playerCopy2 instanceof MoveableBlock && ((MoveableBlock) playerCopy1).isPlayer());

        assertNotEquals(playerCopy1.getX(), playerCopy2.getX());
        assertNotEquals(playerCopy1.getState(), playerCopy2.getState());
        
        this.assertEqualsNoLineSeparator("prdwt@", pushRock.toString());
    }

    //Replaces all line separators with the line separator of the current system and removes excess trailings before
    //asserting wether the expected and actual strings are equal.
    private void assertEqualsNoLineSeparator(String expected, String actual) {
        expected = expected.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        actual = actual.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        assertEquals(expected, actual);
    }

    private void assertDirectionAfterMovement(String moveDirection, PushRock pushRock, int startX, int startY, boolean expectedToMove) {
        //If the block was expected to move, then adjust the coordinates according to the movement direction.
        if (expectedToMove) {
            switch (moveDirection) {
                case "up":
                    startY++;
                    break;
                case "down":
                    startY--;
                    break;
                case "right":
                    startX++;
                    break;
                case "left":
                    startX--;
                    break;
            }
        }
        pushRock.movePlayer(moveDirection);
        BlockAbstract movedBlockCopy = pushRock.getTopBlockCopy(startX, startY);
        assertTrue(movedBlockCopy instanceof MoveableBlock);
        assertEquals(moveDirection, ((MoveableBlock) movedBlockCopy).getDirection());
    }

    @Test
    @DisplayName("Check that the player can place portal one at a wall.")
    public void testPlacingPortalsWall() {
        String[] portalTypes = new String[] {"v", "u"};
        for (String portalType : portalTypes) {
            String expected = "pr dw@";
            PushRock pushRock = new PushRock("Test", expected, "rrg");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            pushRock.placePortal(portalType.equals("v"));
            expected = "pr dw@".replaceAll("w", portalType);
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            BlockAbstract portal = pushRock.getTopBlockCopy(4, 0);
            assertTrue(portal instanceof PortalWallBlock);
            assertEquals("left", ((PortalWallBlock) portal).getDirection());
        }
    }

    @Test
    @DisplayName("Check that both portal types get placed at correct coordinates and with correct directions when placed at walls.")
    public void testPortalPlacementAndDirection() {
        String map = """
                www@
                wpw@
                wwd@
                """;
        String directionLayout = "pg";
        String[] playerDirections = new String[] {"u","r","l","d"};
        Boolean[] portalTypes = new Boolean[] {true, false};
        for (Boolean isPortalOne : portalTypes) {
            for (String playerDirection : playerDirections) {
                PushRock pushRock = new PushRock("test", map, directionLayout.replaceAll("p", playerDirection));
                assertEqualsNoLineSeparator(map, pushRock.toString());
                pushRock.placePortal(isPortalOne);
                String portalDirection = null;
                int x = 1;
                int y = -1;
                switch(playerDirection) {
                    //the portal's direction should be placed in the direction the player is facing, and have it's direction set to the opposite of the player's direction.
                    case "u":
                        portalDirection = "down";
                        y++;
                        break;
                    case "d":
                        portalDirection = "up";
                        y--;
                        break;
                    case "r":
                        portalDirection = "left";
                        x++;
                        break;
                    case "l":
                        portalDirection = "right";
                        x--;
                        break;
                }
                BlockAbstract portal = pushRock.getTopBlockCopy(x, y);
                assertTrue(portal instanceof PortalWallBlock && ((PortalWallBlock) portal).isPortal(), "portal is not placed at the correct coordinates.");
                assertEquals(portalDirection, ((PortalWallBlock) portal).getDirection(), "portal direction should be opposite to the direction of the player that placed it.");
            }
        }
    }

    @Test
    @DisplayName("Check that placing a portal of one type at wall already inhabited by a portal of the same type but different direction replaces that old portal with the new one.")
    public void testPlacingPortalAtPortalOfSameTypeButDifferentDirection() {
        String[] portalTypes = new String[] {"v", "u"};
        for (int i = 0; i < portalTypes.length; i++) {
            //The map starts out with a portal of a given type placed at coordinate (3,0)
            String expected = "p dx@".replaceAll("x", portalTypes[i]);
            PushRock pushRock = new PushRock("Test", expected, "rug");
            //Ensure that the pre-placed portal has the coordinate (3,0) and the direction "up" after construction
            BlockAbstract portal = pushRock.getTopBlockCopy(3, 0);
            assertTrue(portal instanceof PortalWallBlock && ((PortalWallBlock) portal).isPortal());
            assertEquals("up", ((PortalWallBlock) portal).getDirection());
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //A portal the same type is then placed at the old pre-placed portal, but as the player is facing right, the new portal should have the direction set to "left",
            //wheras the old portal has the direction "up"
            pushRock.placePortal(portalTypes[i].equals("v"));
            expected = "p dx@".replaceAll("x", portalTypes[i]);
            //The new portal of the same type should now be placed where the old one was originally placed, but the direction should now be "left" rather than "up"
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            portal = pushRock.getTopBlockCopy(3, 0);
            assertTrue(portal instanceof PortalWallBlock && ((PortalWallBlock) portal).isPortal());
            assertEquals("left", ((PortalWallBlock) portal).getDirection());
        }
    }

    @Test
    @DisplayName("Check that placing a new portal while an old portal of the same type exists removes the old portal and then places the new one correctly.")
    public void testPlacingPortalWhilePortalOfTheSameTypeExists() {
        String[] portalTypes = new String[] {"v", "u"};
        for (String portalType : portalTypes) {
            //The map starts out with one portal of a given type already on the map.
            String expected = "wp dx@".replaceAll("x", portalType);
            PushRock pushRock = new PushRock("Test", expected, "lrg");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //A portal of the same type is then placed at a spot other than the already-placed portal of the same type.
            pushRock.placePortal(portalType.equals("v"));
            expected = "xp dw@".replaceAll("x", portalType);
            //The old portal of the given type should now have been removed, and the new portal of that same type should be placed at the new location.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }
    @Test
    @DisplayName("Check that placing a new portal ontop of an old portal of the opposing type replaces that old portal with the new one.")
    public void testPlacingPortalAtPortalOfOpposingType() {
        String[] portalTypes = new String[] {"v", "u"};
        for (int i = 0; i < portalTypes.length; i++) {
            //The map starts out with one of the two portal types already on the map.
            String expected = "p dx@".replaceAll("x", portalTypes[1-i]);
            PushRock pushRock = new PushRock("Test", expected, "rrg");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //A portal of the other type is then placed at that old portal
            pushRock.placePortal(portalTypes[1-i].equals("v"));
            expected = "p dx@".replaceAll("x", portalTypes[1-i]);
            //The new portal of the other type should now be placed where the old one was originally placed.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }

    @Test
    @DisplayName("Check that placing a portal while a portal of the opposing type connects the two portals together, which should alter their character representation to one that reflects a connected state.")
    public void testPlacingPortalWhilePortalOfOpposingTypeExists() {
        String[] portalTypes = new String[] {"v", "u"};
        String[] portalTypesConnected = new String[] {"ṿ", "ụ"};
        for (int i = 0; i < portalTypes.length; i++) {
            //The map starts out with one of the two portal types already on the map.
            String map = "yp dx@".replaceAll("x", portalTypes[i]);
            map = map.replaceAll("y", portalTypes[1-i]);
            PushRock pushRock = new PushRock("Test", map, "rrrg");
            String expected = "yp dx@".replaceAll("x", portalTypesConnected[i]);
            expected = expected.replaceAll("y", portalTypesConnected[1-i]);
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //A portal of the other type is then placed at a location other than that old opposing portal
            pushRock.placePortal(portalTypes[1-i].equals("v"));
            expected = "wp dx@".replaceAll("x", portalTypes[1-i]);
            //The new portal should have been be placed at the other location and the two portals of opposing types should now be connected, 
            //and thus their character representation should have been changed to reflect their connected state.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }
    @Test
    @DisplayName("Check that placing a portal at a portal of opposing type, while that old portal is connected to another portal, removes both that old portal and its connection and then correctly places the new portal.")
    public void testPlacingPortalAtAConnectedPortalOfOpposingType() {
        String[] portalTypes = new String[] {"v", "u"};
        String[] portalTypesConnected = new String[] {"ṿ", "ụ"};
        for (int i = 0; i < portalTypes.length; i++) {
            //The map starts out with one of the two portal types already on the map.
            String expected = "wp dx@".replaceAll("x", portalTypes[i]);
            PushRock pushRock = new PushRock("Test", expected, "lrg");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //A portal of the other type is then placed at a location other than that old opposing portal
            pushRock.placePortal(portalTypes[1-i].equals("v"));
            expected = expected.replaceAll(portalTypes[i], portalTypesConnected[i]);
            expected = expected.replaceAll("w", portalTypesConnected[1-i]);
            //The new portal should have been be placed at the other location and the two portals of opposing types should now be connected, 
            //and thus their character representation should have been changed to reflect their connected state.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }

    @Test
    @DisplayName("Check that placing a portal when there are rocks that intercept the line of sight between the player and a wall still results in a correctly placed portal at that wall.")
    public void testPlacingPortalThroughRocks() {

    }

    @Test
    @DisplayName("Check that attempting to place a portal out of bounds throws IllegalStateException.")
    public void testPlacingPortalOutOfBounds() {
        
    }

    @Test
    @DisplayName("Check that attempting to place a portal at a teleporter throws IllegalStateException.")
    public void testPlacingPortalAtTeleporter() {
        
    }



    @Test
    @DisplayName("Check that moving the player correctly changes their coordinate and direction after moving.")
    public void testMovePlayer() {
        String expected = """
                d   @
                 p  @
                    @
                """;
        String[] moveDirections = new String[] {"up", "down", "left", "right"};
        for (String direction : moveDirections) {
            PushRock pushRock = new PushRock("Test", expected, "rg");
            assertEquals(expected, pushRock.toString());
            BlockAbstract playerCopy = pushRock.getTopBlockCopy(1, -1);
            assertTrue(playerCopy instanceof MoveableBlock && ((MoveableBlock) playerCopy).isPlayer());
            assertEquals("right", ((MoveableBlock) playerCopy).getDirection());

            assertDirectionAfterMovement(direction, pushRock, 1, -1, true);
        }
    }
    @Test
    @DisplayName("Check that moving the player into a wall changes the direction they face, but not their coordinate.")
    public void testMovePlayerIntoWall() {
        String expected = "wpd@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        //the player should be placed at coordinates (1,0) initially.
        BlockAbstract playerCopy = pushRock.getTopBlockCopy(1, 0);
        assertTrue(playerCopy instanceof MoveableBlock && ((MoveableBlock) playerCopy).isPlayer());
        //and should have "right" as their direction
        assertEquals("right", ((MoveableBlock) playerCopy).getDirection());
        //Now we attempt to move the player to the left, which would place it at the wall
        pushRock.movePlayer("left");
        //The wall should have prevented the coordinate change from the movement, and thus the player should remain at the original coordinates
        playerCopy = pushRock.getTopBlockCopy(1, 0);
        assertTrue(playerCopy instanceof MoveableBlock && ((MoveableBlock) playerCopy).isPlayer(), "The player should have remained at their original coordinate (1,0).");
        assertEquals("left", ((MoveableBlock) playerCopy).getDirection(), "The player's direction should have been set to match the direction of the movement, even though the wall prevented a coordinate change.");
    }

    @Test
    @DisplayName("Check that attempting to move player out of bounds throws IllegalStateException")
    public void testMovePlayerOutOfBounds() {
        String expected = "pd@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertThrows(
            IllegalStateException.class, 
            () -> pushRock.movePlayer("left"),
            "Attempting to move out of bounds should throw IllegalStateException.");
    }

    @Test
    @DisplayName("Check that the player can push one rock.")
    public void testPushOneRock() {
        String expected = " pr d@";
        PushRock pushRock = new PushRock("Test", expected, "rlg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = "  prd@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a the player can not push more than one rock.")
    public void testPushMoreThanOneRock() {
        String expected = " prr d@";
        PushRock pushRock = new PushRock("Test", expected, "rlrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        //It should not be possible for a player to push more than one rock, thus the expected map remains the same.
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that the player can not push a rock when there is a wall ahead of the rock.")
    public void testPushOneRockIntoWall() {
        String expected = " prw d@";
        PushRock pushRock = new PushRock("Test", expected, "rlg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        //It should not be possible for a player to push a rock into a wall, thus the expected map remains the same.
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }


    @Test 
    @DisplayName("Check that player-blocks have their state set to true only when standing ontop of a pressure plate.")
    public void testPressurePlateActivationPlayer() {
        //a player is represented by 'p' while state is false, and 'q' while true
        String expected = " dprd @";
        PushRock pushRock = new PushRock("Test", expected, "rrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("left");
        expected = " q rd @";
        //player's state should be set to true once it is moved on top of the pressure plate
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("left");
        expected = "pd rd @";
        //player's state should return to false once it is moved off of the pressure plate
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
        //rock's state should be set to true once it is pushed on top of the pressure plate
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = " d  qr@";
        //rock's state should return to false once it is pushed off the pressure plate
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that placing weight at all pressure plates results in ending the game by completion.") 
    public void testEndingGame() {
        PushRock pushRock = new PushRock("Test", "dprd@", "rrg");
        assertFalse(pushRock.isGameOver());
        //push rock ontop of one of two pressure plates
        pushRock.movePlayer("right");
        assertFalse(pushRock.isGameOver());
        //move player two steps left to step on the last of two pressure plates
        pushRock.movePlayer("left");
        pushRock.movePlayer("left");
        //the game should now be complete
        assertTrue(pushRock.isGameOver(), "The game should have ended once all of the two pressure plates had a moveable block ontop of them.");
    }

    @Test
    @DisplayName("Check that calling methods that could change the game state throw IllegalStateException while game is over.")
    public void testGameIsOverInputs() {
        PushRock pushRock = new PushRock("Test", "pd@", "rg");
        pushRock.movePlayer("right");
        assertTrue(pushRock.isGameOver(), "Game should be over once the only pressure plate in the game is weighed down by the player.");

        assertThrows(
            IllegalStateException.class, 
            () -> pushRock.movePlayer("right"),
            "Attempting to move a player while the game is over should throw IllegalStateException"
        );
        assertThrows(
            IllegalStateException.class, 
            () -> pushRock.placePortal(true),
            "Attempting to place a portal while the game is over should throw IllegalStateException"
        );
        assertThrows(
            IllegalStateException.class, 
            () -> pushRock.gravityInverter(),
            "Attempting to place a portal while the game is over should throw IllegalStateException"
        );
        assertThrows(
            IllegalStateException.class, 
            () -> pushRock.gravityStep(),
            "Attempting to apply gravity while the game is over should throw IllegalStateException"
        );
    }
    
}
