package pushrock;

import static org.junit.jupiter.api.Assertions.*;

import java.text.Normalizer;

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
        assertEqualsNoLineSeparator("prdwt@", pushRock.toString());
        //Obtain a copy of the block at coordinate (0,0), which should be the player block 'p'
        BlockAbstract playerCopy1 = pushRock.getTopBlockCopy(0, 0);
        assertTrue(playerCopy1 instanceof MoveableBlock && ((MoveableBlock) playerCopy1).isPlayer());
        //Attempt to change the state of the copy by calling various methods that are available.
        ((MoveableBlock) playerCopy1).setState(true);
        ((MoveableBlock) playerCopy1).moveInDirection("right");
        //Check that yet another copy can be aquired, and that this copy does not have the same state as the previous copy that had its attributes changed through method calls.
        BlockAbstract playerCopy2 = pushRock.getTopBlockCopy(0, 0);
        assertTrue(playerCopy2 instanceof MoveableBlock && ((MoveableBlock) playerCopy1).isPlayer());
        assertNotEquals(playerCopy1.getX(), playerCopy2.getX());
        assertNotEquals(playerCopy1.getState(), playerCopy2.getState());
        //The game's state should be unchanged from what it was in the beginning, as the top block copy, which in this case would be the player block, should not be the actual player block that is in the game.
        this.assertEqualsNoLineSeparator("prdwt@", pushRock.toString());
    }
    @Test
    @DisplayName("Check that interacting with a block retrieved from the getPlayerCopy(..) method does not alter the state of the game.")
    public void testGetPlayerCopy() {
        PushRock pushRock = new PushRock("Test", "prdwt@", "rrg"); 
        assertEqualsNoLineSeparator("prdwt@", pushRock.toString());
        //Obtain a copy of the player block 'p'
        MoveableBlock playerCopy1 = pushRock.getPlayerCopy();
        assertTrue(playerCopy1.isPlayer());
        //Attempt to change the state of the copy by calling various methods that are available.
        playerCopy1.setState(true);
        playerCopy1.moveInDirection("right");
        //Check that yet another copy can be aquired, and that this copy does not have the same state as the previous copy that had its attributes changed through method calls.
        MoveableBlock playerCopy2 = pushRock.getPlayerCopy();
        assertTrue(playerCopy2.isPlayer());
        assertNotEquals(playerCopy1.getX(), playerCopy2.getX());
        assertNotEquals(playerCopy1.getState(), playerCopy2.getState());
        //The game's state should be unchanged from what it was in the beginning, as the player copies should not be the actual player block that is in the game.
        assertEqualsNoLineSeparator("prdwt@", pushRock.toString());
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
    @DisplayName("Check that placing a portal results in accurate outcomes depending on the given scenario.")
    public void testPlacingPortal() {
        String[] portalTypes = new String[] {"v", "u"};
        for (String portalType : portalTypes) {
            testPlacingPortalsAtWall(portalType);
            testPlacingPortalThroughRocks(portalType);
            testPlacingPortalOutOfBounds(portalType);
            testPlacingPortalThroughTeleporter(portalType);
            testPortalPlacementAndDirection(portalType);
            testPlacingPortalWhilePortalOfTheSameTypeExists(portalType);
        }
        testPlacingPortalAtPortalOfSameTypeButDifferentDirection(portalTypes);
        testPlacingPortalAtPortalOfOpposingType(portalTypes);
        testPlacingPortalWhilePortalOfOpposingTypeExists(portalTypes);
        testPlacingPortalAtAConnectedPortalOfOpposingType(portalTypes);
    }

    @DisplayName("Check that the player can place a portal at a wall.")
    private void testPlacingPortalsAtWall(String portalType) {
        String expected = "p wd@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.placePortal(portalType.equals("v"));
        expected = "p wd@".replaceAll("w", portalType);
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        BlockAbstract portal = pushRock.getTopBlockCopy(2, 0);
        assertTrue(portal instanceof PortalWallBlock);
        assertEquals("left", ((PortalWallBlock) portal).getDirection());
    }

    @DisplayName("Check that placing a portal when there are rocks that intercept the line of sight between the player and a wall still results in a correctly placed portal at that wall.")
    private void testPlacingPortalThroughRocks(String portalType) {
        String expected = "p rwd@";
        PushRock pushRock = new PushRock("Test", expected, "rrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.placePortal(portalType.equals("v"));
        expected = "p rwd@".replaceAll("w", portalType);
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @DisplayName("Check that attempting to place a portal out of bounds throws IllegalStateException.")
    private void testPlacingPortalOutOfBounds(String portalType) {
        String expected = "p d@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertThrows(
            IllegalStateException.class,
            () -> pushRock.placePortal(portalType.equals("v")),
                "Attempting to place a portal out of bounds should throw IllegalStateException.");
    }

    @DisplayName("Check that attempting to place a portal when there is a teleporter that intercept the line of sight between the player and a wall results in an IllegalStateException beeing thrown.")
    private void testPlacingPortalThroughTeleporter(String portalType) {
        String expected = "p twd@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertThrows(
            IllegalStateException.class,
            () -> pushRock.placePortal(portalType.equals("v")),
                "Attempting to place a portal through a teleporter should throw IllegalStateException.");
    }

    @DisplayName("Check that both portal types get placed at correct coordinates and with correct directions when placed at walls.")
    private void testPortalPlacementAndDirection(String portalType) {
        String map = """
                www@
                wpw@
                wwd@
                """;
        String directionLayout = "pg";
        String[] playerDirections = new String[] {"u","r","l","d"};
        for (String playerDirection : playerDirections) {
            PushRock pushRock = new PushRock("test", map, directionLayout.replaceAll("p", playerDirection));
            assertEqualsNoLineSeparator(map, pushRock.toString());
            pushRock.placePortal(portalType.equals("v"));
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

    @DisplayName("Check that placing a new portal while an old portal of the same type exists removes the old portal and then places the new one correctly.")
    private void testPlacingPortalWhilePortalOfTheSameTypeExists(String portalType) {
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

    @DisplayName("Check that placing a portal of one type at wall already inhabited by a portal of the same type but different direction replaces that old portal with the new one.")
    private void testPlacingPortalAtPortalOfSameTypeButDifferentDirection(String[] portalTypes) {
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

    @DisplayName("Check that placing a new portal ontop of an old portal of the opposing type replaces that old portal with the new one.")
    private void testPlacingPortalAtPortalOfOpposingType(String[] portalTypes) {
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

    @DisplayName("Check that placing a portal while a portal of the opposing type connects the two portals together, which should alter their character representation to one that reflects a connected state.")
    private void testPlacingPortalWhilePortalOfOpposingTypeExists(String[] portalTypes) {
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

    @DisplayName("Check that placing a portal at a portal of opposing type, while that old portal is connected to another portal, removes both that old portal and its connection and then correctly places the new portal.")
    private void testPlacingPortalAtAConnectedPortalOfOpposingType(String[] portalTypes) {
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
            MoveableBlock playerCopy = pushRock.getPlayerCopy();
            assertEquals("right", playerCopy.getDirection());

            assertDirectionAfterMovement(direction, pushRock, 1, -1, true);
        }
    }
    @Test
    @DisplayName("Check that moving the player into a wall changes the direction they face, but not their coordinate.")
    public void testMovePlayerIntoWall() {
        String expected = "wpd@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        //Check that all blocks are placed correctly after game construction
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        //Obtain a copy of the player and check that they were initiallized with "right" as their direction.
        MoveableBlock playerCopyBeforeMove = pushRock.getPlayerCopy();
        assertEquals("right", playerCopyBeforeMove.getDirection());
        //Now we attempt to move the player to the left, which would move them into the wall
        pushRock.movePlayer("left");
        //The wall should have prevented the coordinate change from the movement, and thus the player should remain at the original coordinates..
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        MoveableBlock playerCopyAfterMove = pushRock.getPlayerCopy();
        //..but their direction should have changed to match that of the movement direction.
        assertEquals("left", playerCopyAfterMove.getDirection(), "The player's direction should have been set to match the direction of the movement, even though the wall prevented a coordinate change.");
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
    @DisplayName("Check that the player can not move into a disconnected transporter, even if standing at its entry point.")
    public void testMovePlayerIntoDisconnectedTransporter() {
        String[] transporterTypes = new String[] {"t", "v", "u"};
        for (String transporterType : transporterTypes) {
            String expected = " px d@".replaceAll("x", transporterType);
            String directionLayout = "lxg";
            if (transporterType.equals("t")) {
                directionLayout = directionLayout.replaceAll("x", "");
            } else {
                directionLayout = directionLayout.replaceAll("x", "l");
            }
            PushRock pushRock = new PushRock("Test", expected, directionLayout);
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //attempt to move the player to the right, into the disconnected transporter
            pushRock.movePlayer("right");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }
    @Test
    @DisplayName("Check that the player can not move into a connected portal when they are not standing at the portal's entry point.")
    public void testMovePlayerIntoConnecteddPortalWhileNotAtEntryPoint() {
        String map = """
                     d@
                 pv u @
                      @
                """;
        String[] portalDirections = new String[] {"u", "d","r"}; 
        //The portal's entry point is placed one step from where they are placed in the direction they are facing, thus test every portal direction that 
        //results in the player not being placed at its entry point
        for (String direction : portalDirections) {
            PushRock pushRock = new PushRock("Test", map, "lxrg".replaceAll("x", direction));
            String expected = map.replaceAll("v", "ṿ").replaceAll("u", "ụ");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //Attempt to move the player into the connected portal from a point that is not its entry point.
            pushRock.movePlayer("right");
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    }

    private void checkGameStateIsAsExpectedAfterMovingPlayerInDirection(String mapBefore, String directionLayout, String moveDirection, String expectedMapResult, String expectedPlayerDirection) {
        String mapLayout = mapBefore;
        //remove accents from the before-map, as the constructor does not accept the accented characters that are used in the toString() representation for activated transporters
        mapLayout = Normalizer.normalize(mapLayout, Normalizer.Form.NFD);
        mapLayout = mapLayout.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        PushRock pushRock = new PushRock("test", mapLayout, directionLayout);
        assertEqualsNoLineSeparator(mapBefore, pushRock.toString());
        pushRock.movePlayer(moveDirection);
        //check that the game's map state is as expected after the move was made
        assertEqualsNoLineSeparator(expectedMapResult, pushRock.toString());
        //check that the player ended up with correct direction after the movement
        assertEquals(expectedPlayerDirection, pushRock.getPlayerCopy().getDirection());
    }
    @Test
    @DisplayName("Check that the player is placed at correct coordinates and with the correct direction after they entered a connected teleporter.")
    public void testMovePlayerIntoConnectedTeleporterWhileAtEntryPoint() {
        String entryLeft = """
            d        o@
                pṭ  ṭ @
                      @
            """;
        String exitRight = """
            d        o@
                 ṭ  ṭp@
                      @
            """;
        checkGameStateIsAsExpectedAfterMovingPlayerInDirection(entryLeft, "rug", "right", exitRight, "right");

        String entryRight = """
            d        o@
                 ṭp ṭ @
                      @
            """;
        String exitLeft = """
            d        o@
                 ṭ pṭ @
                      @
            """;
        checkGameStateIsAsExpectedAfterMovingPlayerInDirection(entryRight, "rug", "left", exitLeft, "left");
        
        String entryBelow = """
            d        o@
                 ṭ  ṭ @
                 p    @
            """;
        String exitAbove = """
            d       po@
                 ṭ  ṭ @
                      @
            """;
        checkGameStateIsAsExpectedAfterMovingPlayerInDirection(entryBelow, "rdg", "up", exitAbove, "up");
        
        String entryAbove = """
            d    p   o@
                 ṭ  ṭ @
                      @
            """;
        String exitBelow = """
            d        o@
                 ṭ  ṭ @
                    p @
            """;
        checkGameStateIsAsExpectedAfterMovingPlayerInDirection(entryAbove, "rug", "down", exitBelow, "down");
    }
    @Test
    @DisplayName("Check that the player is placed at correct coordinates and with the correct direction after they have entered a connected portal.")
    public void testMovePlayerIntoConnectedPortalWhileAtEntryPoint() {
        String entryLeft = """
            o        d@
                px  y @
                      @
            """;
        String exitRight = """
            o        d@
                 x  yp@
                      @
            """;

        String entryRight = """
            o        d@
                 xp y @
                      @
            """;
        String exitLeft = """
            o        d@
                 x py @
                      @
            """;
        
        String entryBelow = """
            o        d@
                 x  y @
                 p    @
            """;
        String exitAbove = """
            o       pd@
                 x  y @
                      @
            """;
        
        String entryAbove = """
            o    p   d@
                 x  y @
                      @
            """;
        String exitBelow = """
            o        d@
                 x  y @
                    p @
            """;
        String[] connectedPortalTypes = new String[] {"ṿ", "ụ"};
        String[] portalDirections = new String[] {"u", "d", "l", "r"};
        for (int i = 0; i < connectedPortalTypes.length; i++) {
            for (String entryPortalDirection : portalDirections) {
                String mapStart = "";
                String playerMoveDirection = "";
                String directionLayoutFormat = "";
                switch (entryPortalDirection) {
                    case "u":
                        mapStart = entryAbove;
                        playerMoveDirection = "down";
                        directionLayoutFormat = "rl" + entryPortalDirection + "yg";
                        break;
                    case "d":
                        mapStart = entryBelow;
                        playerMoveDirection = "up";
                        directionLayoutFormat = "r" + entryPortalDirection + "ylg";
                        break;
                    case "r":
                        mapStart = entryRight;
                        playerMoveDirection = "left";
                        directionLayoutFormat = "r" + entryPortalDirection + "lyg";
                        break;
                    case "l":
                        mapStart = entryLeft;
                        playerMoveDirection = "right";
                        directionLayoutFormat = "rl" + entryPortalDirection + "yg";
                        break;
                }
                mapStart = mapStart.replaceAll("x", connectedPortalTypes[i]);
                mapStart = mapStart.replaceAll("y", connectedPortalTypes[1-i]);
                for (String exitPortalDirection : portalDirections) {
                    String mapAfter = "";
                    String playerDirectionAfter = "";
                    switch(exitPortalDirection) {
                        case "u":
                            mapAfter = exitAbove;
                            playerDirectionAfter = "up";
                            break;
                        case "d":
                            mapAfter = exitBelow;
                            playerDirectionAfter = "down";
                            break;
                        case "r":
                            mapAfter = exitRight;
                            playerDirectionAfter = "right";
                            break;
                        case "l":
                            mapAfter = exitLeft;
                            playerDirectionAfter = "left";
                            break;
                    }
                    mapAfter = mapAfter.replaceAll("x", connectedPortalTypes[i]);
                    mapAfter = mapAfter.replaceAll("y", connectedPortalTypes[1-i]);
                    String directionLayout = directionLayoutFormat.replaceAll("y", exitPortalDirection);
                    checkGameStateIsAsExpectedAfterMovingPlayerInDirection(mapStart, directionLayout, playerMoveDirection, mapAfter, playerDirectionAfter);
                }
            }
        }
    }

    @Test
    @DisplayName("Check that a player can not enter a transporter whose entry point is at a wall.") 
    public void testEnterConnectedTransporterWithWallAtExitPoint() {
        String map = "od pt tw@";
        PushRock pushRock = new PushRock("test", map, "rlg");
        String expected = "od pṭ ṭw@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        //The player's coordinate should remain the same, but direction should have changed to that of the movement.
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertEquals("right", pushRock.getPlayerCopy().getDirection());
    }

    @Test
    @DisplayName("Check that a player attempting to enter a teleporter that would place them out of bounds leads to IllegalStateException being thrown.") 
    public void testMovePlayerThroughConnectedTeleporterLeadingOutOfBounds() {
        String map = "od pt t@";
        PushRock pushRock = new PushRock("test", map, "rlg");
        String expected = "od pṭ ṭ@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertThrows(
            IllegalStateException.class,
            () -> pushRock.movePlayer("right"),
            "Attempting to take a teleporter that would lead out of bounds should throw IllegalStateException.");
    }
    @Test
    @DisplayName("Check that a player attempting to enter a portal that would place them out of bounds leads to IllegalStateException being thrown.") 
    public void testMovePlayerThroughConnectedPortalrLeadingOutOfBounds() {
        String map = "od pu v@";
        PushRock pushRock = new PushRock("test", map, "rllrg");
        String expected = "od pụ ṿ@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertThrows(
            IllegalStateException.class,
            () -> pushRock.movePlayer("right"),
            "Attempting to take a teleporter that would lead out of bounds should throw IllegalStateException.");
    }

    @Test
    @DisplayName("Check that a player can not push a rock into a transporter whose exit point is out of bounds.") 
    public void testPushRockThroughAConnectedTransporterOutOfBounds() {
        String map = "od prt t@";
        PushRock pushRock = new PushRock("test", map, "rllg");
        String expected = "od prṭ ṭ@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertThrows(
            IllegalStateException.class,
            () -> pushRock.movePlayer("right"),
            "Attempting to push a rock through a teleporter leading out of bounds should throw IllegalStateException.");
    }

    @Test
    @DisplayName("Check that a rock can be pushed through a connected teleporter.") 
    public void testPushRockThroughAConnectedTeleporter() {
        String map = "o prt   t  d@";
        PushRock pushRock = new PushRock("test", map, "rllg");
        String expected = "o prṭ   ṭ  d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = "o  pṭ   ṭr d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a rock can be pushed through a connected portal.") 
    public void testPushRockThroughAConnectedPortal() {
        String map = "o pru   v  d@";
        PushRock pushRock = new PushRock("test", map, "rllllg");
        String expected = "o prụ   ṿ  d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = "o  pụ  rṿ  d@";
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
    @DisplayName("Check that placing weight at pressure plates connects the correct teleporters.")
    public void testPressurePlateActivationTeleporters() {
        String expected = "ttttprdd   d@";
        PushRock pushRock = new PushRock("Test", expected, "rrg");
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        //Once one pressure plate is weighed down, then the first two teleporters should get connected.
        assertEqualsNoLineSeparator("ṭṭtt pod   d@", pushRock.toString());
        pushRock.movePlayer("right");
        //Once two pressure plates are weighed down, then the first and second teleporter should get connected.
        assertEqualsNoLineSeparator("ṭtṭt  qo   d@", pushRock.toString());
        //Then make sure that removing weight from the pressure plates updates the teleporter connections accordingly.
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("ṭṭtt  dqr  d@", pushRock.toString());
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("tttt  ddpr d@", pushRock.toString());
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
    @DisplayName("Check that calling methods that could change the game state throw IllegalStateException while the game is over.")
    public void testGameIsOverInputs() {
        PushRock pushRock = new PushRock("Test", "pd@", "rg");
        pushRock.movePlayer("right");
        assertTrue(pushRock.isGameOver(), "Game should be over once the only pressure plate in the game is weighed down by the player.");
        assertEqualsNoLineSeparator(" q@", pushRock.toString());

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
    @Test
    @DisplayName("Check that the game can be correctly reset while the game is over.")
    public void testResetWhileGameIsOver() {
        String mapLevelLayout = "pd@";
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rg");
        //Move the player to end the game.
        pushRock.movePlayer("right");
        assertTrue(pushRock.isGameOver(), "Game should be over once the only pressure plate in the game is weighed down by the player.");
        assertEqualsNoLineSeparator(" q@", pushRock.toString());
        //reset the level
        pushRock.resetLevel();
        //the game's state should now be reset to what it was at the beginning.
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
    }
    
    
    @Test
    @DisplayName("Check that gravity direction can be inverted.")
    public void testGravityInverter() {
        String mapLevelLayout = "pd@";
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rg");
        assertFalse(pushRock.isGravityInverted(), "Gravity should not be inverted after construction when the direction layout specifies a non-inverted direction by lower case 'g'");
        pushRock.gravityInverter();
        assertTrue(pushRock.isGravityInverted(), "Gravity should be inverted after gravityInverter() was called when the game's gravity direction was not inverted already.");
        pushRock.gravityInverter();
        assertFalse(pushRock.isGravityInverted(), "Gravity should no longer be inverted after another gravityInverter() method call.");
        }

    @Test
    @DisplayName("Check that gravity is only applied to airborne moveable blocks according to the current gravity direction.")
    public void testGravityAppliedToAllMoveableBlocks() {
        String mapLevelLayout = """
            R-----D@
            -R-----@
            --P----@
                r  @
                 r @
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrrrg");
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        pushRock.gravityStep();
        String expected = """
            ------D@
            R------@
            -R-----@
              p r  @
                 r @
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ------D@
            -------@
            R------@
             rp r  @
                 r @
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that gravity is only applied to airborne moveable blocks according to the current gravity direction before and after gravity has been inverted.")
    public void testGravityAppliedToAllMoveableBlocksGravityInversion() {
        String mapLevelLayout = """
            R-----D@
            -R-----@
            --P----@
                r  @
                 r @
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrrrg");
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        pushRock.gravityStep();
        String expected = """
            ------D@
            R------@
            -R-----@
              p r  @
                 r @
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        //Invert gravity right before applying gravity
        pushRock.gravityInverter();
        pushRock.gravityStep();
        expected = """
            R-----D@
            -R-----@
            -------@
              p r  @
                 r @
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that gravity application can be set to manual.")
    public void testSetGravityApplicationManual() {
        PushRock pushRock = new PushRock("test", "pd@", "rg");
        pushRock.setGravityApplicationManual();
        assertTrue(pushRock.isGravityApplicationManual());
        assertFalse(pushRock.isGravityApplicationMoveInput());
        assertFalse(pushRock.isGravityApplicationInterval());
    }
    @Test
    @DisplayName("Check that gravity application can be set to move input.")
    public void testSetGravityApplicationMoveInput() {
        PushRock pushRock = new PushRock("test", "pd@", "rg");
        pushRock.setGravityApplicationMoveInput();
        assertFalse(pushRock.isGravityApplicationManual());
        assertTrue(pushRock.isGravityApplicationMoveInput());
        assertFalse(pushRock.isGravityApplicationInterval());
    }
    @Test
    @DisplayName("Check that gravity application can be set to interval.")
    public void testSetGravityApplicationInterval() {
        PushRock pushRock = new PushRock("test", "pd@", "rg");
        pushRock.setGravityApplicationInterval();
        assertFalse(pushRock.isGravityApplicationManual());
        assertFalse(pushRock.isGravityApplicationMoveInput());
        assertTrue(pushRock.isGravityApplicationInterval());
    }


    @Test
    @DisplayName("Check that while gravity application is set to manual, an airborne player attempting to move will remain in place, and is then only moved in direction of gravity by manual gravityStep() calls.")
    public void testMoveAirbornePlayerWhileGravityApplicationIsManual() {
        String mapLevelLayout = """
            ----R-D@
            --P----@
            -------@
            -------@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrg");
        pushRock.setGravityApplicationManual();
        assertTrue(pushRock.isGravityApplicationManual());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        //Attempt to move the player
        pushRock.movePlayer("left");
        //Player coordinates should remain unchanged.
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        //Apply gravity once with gravityStep()
        pushRock.gravityStep();
        String expected = """
            ------D@
            ----R--@
            --P----@
            -------@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());

    }
    @Test
    @DisplayName("Check that while gravity application is set to move input, a grounded player attempting to move will correctly change coordinates and will then also automatically apply gravity making all airborne moveable blocks fall one step in gravity's direction.")
    public void testMoveGroundedPlayerGravityApplicationIsMoveInput() {
        String mapLevelLayout = """
            ----R-D@
            -------@
            -P-----@
            wwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrg");
        pushRock.setGravityApplicationMoveInput();
        assertTrue(pushRock.isGravityApplicationMoveInput());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        //Attempt to move the player
        pushRock.movePlayer("left");
        //Player coordinates should not be changed according to the input movement
        String expected = """
            ------D@
            ----R--@
            P------@
            wwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that while gravity application is set to move input, an airborne player attempting to move will fail to change coordinates, but will then automatically apply gravity making all airborne moveable blocks fall one step in gravity's direction.")
    public void testMoveAirbornePlayerWhileGravityApplicationIsMoveInput() {
        String mapLevelLayout = """
            ----R-D@
            --P----@
            -------@
            -------@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrg");
        pushRock.setGravityApplicationMoveInput();
        assertTrue(pushRock.isGravityApplicationMoveInput());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        //Attempt to move the player
        pushRock.movePlayer("left");
        //Player coordinates should change according to the movement, and then gravity should be applied to all other moveable blocks currently airborne.
        String expected = """
            ------D@
            ----R--@
            --P----@
            -------@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that although a player can not move while airborne, they can still change their direction to reorient themselves.")
    public void testAirbornePlayerReorientDirection() {
        String mapLevelLayout = """
            ------D@
            --P----@
            -------@
            -------@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rg");
        pushRock.setGravityApplicationManual();
        assertTrue(pushRock.isGravityApplicationManual());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        assertEquals(pushRock.getPlayerCopy().getDirection(), "right", "The player should have been initialized with direction 'right'");
        //Attempt to move the player as to reorient them
        pushRock.movePlayer("left");
        //Coordinates should remain the same, but the player should now be directed to the left
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        assertEquals(pushRock.getPlayerCopy().getDirection(), "left", "The player direction should match that of the issued movement 'left'");
    }

    @Test
    @DisplayName("Check that a player can carry around a block that is stacked ontop of them.") 
    public void testPlayerCanCarryABlock() {
        String mapLevelLayout = """
            ------D@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrg");
            assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
            pushRock.movePlayer("right");
        String expected = """
            ------D@
            --R----@
            --P----@
            wwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a player can jump one step up while carrying a block that is stacked ontop of them.") 
    public void testPlayerCanJumpWhileCarryingABlock() {
        String mapLevelLayout = """
            ------D@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrg");
            pushRock.setGravityApplicationManual();
            assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
            pushRock.movePlayer("up");
        String expected = """
            -R----D@
            -P-----@
            -------@
            wwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a player can carry around multiple blocks that are stacked ontop of them.") 
    public void testPlayerCanCarryMultipleBlocks() {
        String mapLevelLayout = """
            ------D@
            -R-----@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            PushRock pushRock = new PushRock("Test", mapLevelLayout, "rlrg");
            assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
            pushRock.movePlayer("right");
        String expected = """
            ------D@
            --R----@
            --R----@
            --P----@
            wwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a player can not jump one step up while carrying more than one block that is stacked ontop of them.") 
    public void testPlayerCanJumpWhileCarryingMoreThanOneBlock() {
        String mapLevelLayout = """
            ------D@
            -R-----@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            PushRock pushRock = new PushRock("Test", mapLevelLayout, "rlrg");
            pushRock.setGravityApplicationManual();
            assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
            pushRock.movePlayer("up");
        String expected = """
            ------D@
            -R-----@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that a player's carried block stack gets it's top split off when colliding with a wall, right at the position of that wall.") 
    public void testPlayerCarriedBlockCollidingWithWall() {
        String mapLevelLayout = """
            ------D@
            -R-----@
            -R-----@
            -RW----@
            -R-----@
            -P-----@
            wwwwwww@
            """;
            PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrrrg");
            pushRock.setGravityApplicationManual();
            assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
            pushRock.movePlayer("right");
        String expected = """
            ------D@
            -R-----@
            -R-----@
            -RW----@
            --R----@
            --P----@
            wwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a player's carried block stack gets it's top split off when hitting a connected transporter, right at the position of said transporter, with the block that collided with the transporter getting moved to its exit point.") 
    public void testPlayerCarriedBlockCollidingWithConnectedTransporter() {
        String mapLevelLayout = """
            T-----oD@
            ---R----@
            ---R----@
            ---RT---@
            ---R----@
            ---P----@
            wwwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrrrrg");
        pushRock.setGravityApplicationManual();
        String expected = """
            Ṭ-----oD@
            ---R----@
            ---R----@
            ---RṬ---@
            ---R----@
            ---P----@
            wwwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = """
            ṬR----oD@
            ---R----@
            ---R----@
            ----Ṭ---@
            ----R---@
            ----P---@
            wwwwwwww@
            """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together while gravity is not inverted.")
    public void testMoveableBlocksFallTogetherGravityNotInverted() {
        String mapLevelLayout = """
            -------D@
            ---R----@
            ---P----@
            ---R----@
            --------@
            --------@
            wwwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrg");
        assertFalse(pushRock.isGravityInverted());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        pushRock.gravityStep();
        String expected = """
            -------D@
            --------@
            ---R----@
            ---P----@
            ---R----@
            --------@
            wwwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together while gravity IS inverted.")
    public void testMoveableBlocksFallTogetherGravityInverted() {
        String mapLevelLayout = """
            -------D@
            --------@
            ---R----@
            ---P----@
            ---R----@
            --------@
            wwwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrg");
        pushRock.gravityInverter();
        assertTrue(pushRock.isGravityInverted());
        assertEqualsNoLineSeparator(mapLevelLayout, pushRock.toString());
        pushRock.gravityStep();
        String expected = """
            -------D@
            ---R----@
            ---P----@
            ---R----@
            --------@
            --------@
            wwwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    
    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together into a transporter while gravity is not inverted.")
    public void testMoveableBlocksFallTogetherIntoTransporterGravityNotInverted() {
        String mapLevelLayout = """
            ---U---D@
            --------@
            --------@
            ---R----@
            ---P----@
            ---R----@
            wwwVwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrrug");
        assertFalse(pushRock.isGravityInverted());
        String expected = """
            ---Ụ---D@
            --------@
            --------@
            ---R----@
            ---P----@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---R----@
            --------@
            --------@
            ---R----@
            ---P----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---P----@
            ---R----@
            --------@
            --------@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together into a transporter while gravity IS inverted.")
    public void testMoveableBlocksFallTogetherIntoTransporterGravityInverted() {
        String mapLevelLayout = """
            ---U---D@
            ---R----@
            ---P----@
            ---R----@
            --------@
            --------@
            wwwVwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrrug");
        pushRock.gravityInverter();
        assertTrue(pushRock.isGravityInverted());
        String expected = """
            ---Ụ---D@
            ---R----@
            ---P----@
            ---R----@
            --------@
            --------@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---P----@
            ---R----@
            --------@
            --------@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---R----@
            --------@
            --------@
            ---R----@
            ---P----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together in an infinte loop when the block chain is uninterrupted between exit and entry transporter and gravity is not inverted.")
    public void testMoveableBlocksFallingInInfinteLoopGravityNotInverted() {
        String mapLevelLayout = """
            ---U---D@
            ---R----@
            ---R----@
            ---P----@
            ---R----@
            wwwVwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrrrug");
        assertFalse(pushRock.isGravityInverted());
        String expected = """
            ---Ụ---D@
            ---R----@
            ---R----@
            ---P----@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---R----@
            ---R----@
            ---R----@
            ---P----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---P----@
            ---R----@
            ---R----@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that moveable blocks stacked ontop of eachother fall together in an infinte loop when the block chain is uninterrupted between exit and entry transporter and gravity is inverted.")
    public void testMoveableBlocksFallingInInfinteLoopGravityInverted() {
        String mapLevelLayout = """
            ---U---D@
            ---R----@
            ---P----@
            ---R----@
            ---R----@
            wwwVwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrrrug");
        pushRock.gravityInverter();
        assertTrue(pushRock.isGravityInverted());
        String expected = """
            ---Ụ---D@
            ---R----@
            ---P----@
            ---R----@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---P----@
            ---R----@
            ---R----@
            ---R----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            ---Ụ---D@
            ---R----@
            ---R----@
            ---R----@
            ---P----@
            wwwṾwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that moveable block chains stacked ontop of portals facing the ")
    public void testMoveableStackPortalBalance() {
        String mapLevelLayout = """
            -------D@
            --R-----@
            --R-----@
            --R-----@
            --P--R--@
            wwUwwVww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrrruug");
        assertFalse(pushRock.isGravityInverted());
        String expected = """
            -------D@
            --R-----@
            --R-----@
            --R-----@
            --P--R--@
            wwỤwwṾww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            -------D@
            --------@
            --R-----@
            --R--R--@
            --R--P--@
            wwỤwwṾww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            -------D@
            --------@
            -----R--@
            --R--P--@
            --R--R--@
            wwỤwwṾww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            -------D@
            --------@
            --R-----@
            --R--R--@
            --R--P--@
            wwỤwwṾww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }


}
