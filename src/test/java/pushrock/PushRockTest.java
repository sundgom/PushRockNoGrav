package pushrock;

import static org.junit.jupiter.api.Assertions.*;

import java.text.Normalizer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pushrock.model.AbstractObservablePushRock;
import pushrock.model.BlockAbstract;
import pushrock.model.IObserverPushRock;
import pushrock.model.IntervalNotifier;
import pushrock.model.MoveableBlock;
import pushrock.model.PortalWallBlock;
import pushrock.model.PushRock;
import pushrock.model.TraversableBlock;

public class PushRockTest {

    //Replaces all line separators with the line separator of the current system and removes excess trailings before
    //asserting wether the expected and actual strings are equal.
    private void assertEqualsNoLineSeparator(String expected, String actual) {
        expected = expected.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        actual = actual.replaceAll("\\n|\\r\\n", System.lineSeparator()).stripTrailing();
        assertEquals(expected, actual);
    }

    private void checkConstructorExpectedStartValues(PushRock pushRock, String levelName, String levelMapLayout, String levelDirectionLayout, int width, int height, boolean isGravityInverted, int moveCount) {
        //Check that pushRock's attributes are set correctly according to the input contructor parameters after construction
        assertEquals(levelName, pushRock.getLevelName());
        assertEqualsNoLineSeparator(levelMapLayout, pushRock.getLevelMapLayout());
        assertEquals(levelDirectionLayout, pushRock.getLevelDirectionLayout());
        assertEquals(width, pushRock.getWidth());
        assertEquals(height, pushRock.getHeight());
        assertEquals(moveCount, pushRock.getMoveCount(), "");
        assertFalse(pushRock.isGameOver(), "The game should not be over right after the game has been constructed.");
        assertSame(isGravityInverted, pushRock.isGravityInverted(), "Gravity should only be inverted if the direction layout ends with an upper case 'G', and not inverted when it ends with lower case 'g';");
    }

    private void checkConstructorMapResult(PushRock pushRock, String expectedMapLayout, String expectedDirectionLayout, int expectedWidth, int expectedHeight) {
        //Check that every type character in the mapLayout constructed an object of correct class and attribute values
        String typeSequence = expectedMapLayout.replaceAll("\\n|\\r\\n|@", "").stripTrailing();
        for (int y = 0; y < expectedHeight; y++) {
            for (int x = 0; x < expectedWidth; x++) {
                char expectedType = typeSequence.charAt(y * expectedWidth + x);
                char expectedTraversableType = ' ';
                BlockAbstract actualTopBlock = pushRock.getTopBlockCopy(x, -y);
                TraversableBlock actualTraversableBlock = pushRock.getTraversableBlockCopy(x, -y);
                //Check that a given coordinate holds a traversable blocks.
                assertNotNull(actualTraversableBlock, "Every coordinate should hold a traversable block.");
                //check that the block objects were initialized with coordinates matching their type-representation's position in the mapLayout
                assertEquals(x, actualTopBlock.getX());
                assertEquals(-y, actualTopBlock.getY());
                assertEquals(x, actualTraversableBlock.getX());
                assertEquals(-y, actualTraversableBlock.getY());

                if (Character.isUpperCase(expectedType)) {
                    assertFalse(actualTraversableBlock.isBirdView());
                    expectedType = Character.toLowerCase(expectedType);
                } else if (expectedType == '-') {
                    assertFalse(actualTraversableBlock.isBirdView());
                    expectedType = ' ';
                } else {
                    assertTrue(actualTraversableBlock.isBirdView());
                }
                if (expectedType == 'o') {
                    expectedType = 'r';
                    expectedTraversableType = 'd';
                } else if (expectedType == 'q') {
                    expectedType = 'p';
                    expectedTraversableType = 'd';
                } else if (expectedType == 'd') {
                    expectedTraversableType = 'd';
                }
                assertEquals(expectedType, actualTopBlock.getType());
                assertEquals(expectedTraversableType, actualTraversableBlock.getType());
            }
        }
    }

    private void checkStringRepresentation(PushRock pushRock, String expectedMap, String directionLayout, int expectedWidth, int expectedHeight) {
        String actualMap = "";
        for (int y = 0; y < expectedHeight; y++) {
            for (int x = 0; x < expectedWidth; x++) {   
                String blockToString = pushRock.getTopBlockCopy(x, -y).toString();
                if (!pushRock.getTraversableBlockCopy(x, -y).isBirdView()) {
                    blockToString = blockToString.toUpperCase();
                }
                actualMap += blockToString;
            }
            actualMap += "@\n";
        }
        //Check that the actual map formed from the blocks' string toString() representations matches the actual map
        assertEqualsNoLineSeparator(expectedMap, actualMap);
        //Check that pushRock's own toString() matches the expected map 
        assertEqualsNoLineSeparator(expectedMap, pushRock.toString());
    }   

    @Test
    @DisplayName("Check that the level contructor initializeses objects with correct values according to the input layouts.")
    public void testLevelConstructorValidInput() {
        String levelName = "Test";
        String levelMapLayout = """
                 protwud@
                --ROTWVD@
                """;
        String levelDirectionLayout = "lurdddlg";
        int expectedWidth = 8;
        int expectedHeight = 2;
        PushRock pushRock = new PushRock(levelName, levelMapLayout, levelDirectionLayout);
        checkConstructorExpectedStartValues(pushRock, levelName, levelMapLayout, levelDirectionLayout, expectedWidth, expectedHeight, false, 0);
        checkConstructorMapResult(pushRock, levelMapLayout, levelDirectionLayout, expectedWidth, expectedHeight);
        String expectedMap = """
                 proṭwụd@
                --ROṬWṾD@
                """;

        checkStringRepresentation(pushRock, expectedMap, levelDirectionLayout, expectedWidth, expectedHeight); 
    }
    @Test
    @DisplayName("Check that the save contructor initializeses objects with correct values according to the input layouts.")
    public void testSaveConstructorValidInput() {
        String levelName = "Test";
        String levelMapLayout = """
                 protwud@
                --ROTWVD@
                """;
        String levelDirectionLayout = "lurdddlg";
        String saveMapLayout = """
                  rotwud@
                -PROTWVD@
                """;
        String saveDirectionLayout = "lurdddlg";
        int saveMoveCount = 17;
        int expectedWidth = 8;
        int expectedHeight = 2;
        PushRock pushRock = new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, saveMoveCount);
        checkConstructorExpectedStartValues(pushRock, levelName, levelMapLayout, levelDirectionLayout, expectedWidth, expectedHeight, false, saveMoveCount);
        checkConstructorMapResult(pushRock, saveMapLayout, saveDirectionLayout, expectedWidth, expectedHeight);
    }
    @Test
    @DisplayName("Check that using null for String parameters throws IllegalArgumentException.")
    public void testConstructorNullParameters() {
        String levelName = "Test";
        String levelMapLayout = """
                 protwud@
                --ROTWVD@
                """;
        String levelDirectionLayout = "lurdddlg";
        String saveMapLayout = """
                  rotwud@
                -PROTWVD@
                """;
        String saveDirectionLayout = "lurdddlg";
        Assertions.assertDoesNotThrow(
            () -> new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, 0));
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(null, levelMapLayout, levelDirectionLayout));
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(levelName, null, levelDirectionLayout));
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(levelName, levelDirectionLayout, null));
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(levelName, levelMapLayout, levelDirectionLayout, null, saveDirectionLayout, 0));
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, null, 0));
    }
    @Test
    @DisplayName("Check that using negative values for saveMoveCount parameter throws IllegalArgumentException.")
    public void testConstructorSaveMoveCountNegative() {
        String levelName = "Test";
        String levelMapLayout = """
                 protwud@
                --ROTWVD@
                """;
        String levelDirectionLayout = "lurdddlg";
        String saveMapLayout = """
                  rotwud@
                -PROTWVD@
                """;
        String saveDirectionLayout = "lurdddlg";
        assertThrows(
            IllegalArgumentException.class, 
            () -> new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, -1));
    }

    @Test
    @DisplayName("Check that attempting to construct with map layout that has inconistent width results in IllegalArgumentExpcetion to be thrown.")
    public void testConstructorMapLayoutInconsistentMapWidth() {
        String middleWidthGain = """
                pdwwww@
                wwwwwww@
                wwwwww@
                """;
        String  middleWidthLoss = """
                pdwwww@
                wwwww@
                wwwwww@
                """;
        String  endWidthGain = """
                pdwwww@
                wwwwww@
                wwwwwww@
                """;
        String  endWidthLoss = """
                pdwwww@
                wwwwww@
                wwwww@
                """;
        String[] layoutsWithInconsistentWidth = new String[] {middleWidthGain, middleWidthLoss, endWidthGain, endWidthLoss};
        for (String mapLayout : layoutsWithInconsistentWidth) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new PushRock("Test", mapLayout, "rg"), 
                "Should not be able to construct PushRock with inconsistent width");
        }
    }

    @Test
    @DisplayName("Check that a too short map layout results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutTooShort() {
        String tooShort = "p@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", tooShort, "rg"), 
            "IllegalArgumentException should be thrown when the map layout is too short.");
    }
    @Test
    @DisplayName("Check that a map layout without a player results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutNoPlayer() {
        String noPlayer = "wd@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", noPlayer, "g"), 
            "IllegalArgumentException should be thrown when the map layout has no player.");
    }
    @Test
    @DisplayName("Check that a map layout with too many players results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutTooManyPlayers() {
        String tooManyPlayers = "ppd@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", tooManyPlayers, "rrg"), 
            "IllegalArgumentException should be thrown when the map layout has too many players.");
    }

    @Test
    @DisplayName("Check that a map layout with too many of any portal type results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutTooManyPortals() {
        String tooManyPortalOne = "puud@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", tooManyPortalOne, "rrrg"), 
            "IllegalArgumentException should be thrown when the map layout has too many portal one.");
        String tooManyPortalTwo = "pvvd@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", tooManyPortalTwo, "rrrg"), 
            "IllegalArgumentException should be thrown when the map layout has too many portal two.");
    }

    @Test
    @DisplayName("Check that a map layout without an unoccupied pressure plate results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutNoUnoccupiedPressurePlate() {
        String noUnoccupiedPressurePlate = "-q@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", noUnoccupiedPressurePlate, "rg"), 
            "IllegalArgumentException should be thrown when the map layout has no unoccupied pressure plate.");
    }
    @Test
    @DisplayName("Check that a map layout without a width marker '@' results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutNoWidthMarker() {
        String noWidthMarker = "pd";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", noWidthMarker, "rg"), 
            "IllegalArgumentException should be thrown when the map layout has no unoccupied pressure plate.");
    }

    @Test
    @DisplayName("Check that invalid characters in the map layout results in IllegalArgumentException being thrown.")
    public void testConstructorMapLayoutInvalidCharacters() {
        String mapLayout = "pdX@";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", mapLayout, "rg"), 
            "IllegalArgumentException should be thrown when the map layout contains invalid characters.");
    }
    @Test
    @DisplayName("Check that a too short direction layout results in IllegalArgumentException being thrown.")
    public void testTooShortDirectionLayout() {
        String directionLayout = "g";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", "pd@", directionLayout), 
            "IllegalArgumentException should be thrown when the direction layout contains too few characters.");
    }
    @Test
    @DisplayName("Check that a direction layout without a gravity direction 'g'/'G' results in IllegalArgumentException being thrown.")
    public void testConstructorDirectionLayoutNoGravityDirection() {
        String directionLayout = "rr";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", "prd@", directionLayout), 
            "IllegalArgumentException should be thrown when the direction layout has no gravity direction.");
    }
    @Test
    @DisplayName("Check that invalid characters in the direction layout results in IllegalArgumentException being thrown.")
    public void testConstructorDirectionLayoutInvalidCharacters() {
        String directionLayout = "rXg";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", "pd@", directionLayout), 
            "IllegalArgumentException should be thrown when the direction layout contains invalid characters.");
    }
    @Test
    @DisplayName("Check that a direction layout containing too few or too many directions compared to the map layout results in IllegalArgumentException being thrown.")
    public void testConstructorWrongDirectionCount() {
        String mapLayout = "prd@";
        String tooMany = "rrrg";
        String tooFew =  "rg";
        String correct = "rrg";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", mapLayout, tooMany), 
            "IllegalArgumentException should be thrown when the direction layout contains invalid characters.");
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", mapLayout, tooFew), 
            "IllegalArgumentException should be thrown when the direction layout contains invalid characters.");
        Assertions.assertDoesNotThrow(
            () -> new PushRock("Test", mapLayout,  correct),
            "No exceptions should be thrown when the map and direction layouts are compatible.");
    }

    @Test
    @DisplayName("Check that no exceptions are thrown when constructing with level and save layouts that are equal.")
    public void testLevelAndSaveLayoutsAreEqual() {
        String levelMapLayout = "pd@";
        String levelDirectionLayout = "rg";
        Assertions.assertDoesNotThrow(
            () -> new PushRock("Test", levelMapLayout,  levelDirectionLayout, levelMapLayout, levelDirectionLayout, 0),
            "No exceptions should be thrown when the level and save layouts are equal .");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts of inequal length results in illegal argument exception being thrown.")
    public void testLevelAndSaveMapLayoutsAreOfInequalLength() {
        String levelMapLayout = "pd@";
        String saveMapLayout = "pdw@";
        String levelDirectionLayout = "rg";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", levelMapLayout,  levelDirectionLayout, saveMapLayout, levelDirectionLayout, 0),
            "IllegalArgumentException should be thrown when level and map layouts are of inequal length.");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts of inequal width results in illegal argument exception being thrown.")
    public void testLevelAndSaveMapLayoutsAreOfInequalWidth() {
        String levelMapLayout = """
                pd@ww@
                """;
        String saveMapLayout = """
                pdww@
                """;
        String levelDirectionLayout = "rg";
        assertThrows(
            IllegalArgumentException.class,
            () -> new PushRock("Test", levelMapLayout,  levelDirectionLayout, saveMapLayout, levelDirectionLayout, 0),
            "IllegalArgumentException should be thrown when level and map layouts are of inequal width.");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts that have bird-view enabled at differentl positions from one another results in IllegalArgumentException being thrown.")
    public void testLevelAndSaveMapLayoutHaveDifferentBirdViewPositions() {
    String levelMapLayoutWall = """
            pdwW@
            """;
    String saveMapLayoutWall = """
            pdWw@
            """;
    String levelDirectionLayout = "rg";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelMapLayoutWall,  levelDirectionLayout, saveMapLayoutWall, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when level and map layouts have bird-view enabled at different positions.");
    String levelMapLayoutAir = """
            pd- @
            """;
    String saveMapLayoutAir = """
            pd -@
            """;
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelMapLayoutAir,  levelDirectionLayout, saveMapLayoutAir, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when level and map layouts have bird-view enabled at different positions.");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts that have walls placed differently from one another results in IllegalArgumentException being thrown.")
    public void testLevelAndSaveMapLayoutHaveDifferentWallPositions() {
    String levelMapLayout = """
            pd ww@
            """;
    String saveMapLayout = """
            pdw w@
            """;
    String levelDirectionLayout = "rg";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelMapLayout,  levelDirectionLayout, saveMapLayout, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when level and save map layouts have walls placed differently.");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts that have teleporters placed differently from one another results in IllegalArgumentException being thrown.")
    public void testLevelAndSaveMapLayoutHaveDifferentTeleporterPositions() {
        String levelMapLayout = """
            pd T@
            """;
    String saveMapLayout = """
            pdT @
            """;
    String levelDirectionLayout = "rg";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelMapLayout,  levelDirectionLayout, saveMapLayout, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when level and save map layouts have teleporters placed differently.");
    }
    @Test
    @DisplayName("Check that constructing with level and save map layouts that do not have pressure plates at the same spots results in IllegalArgumentException being thrown, regardless of wether or not players/rocks are standing on top of them.")
    public void testLevelAndSaveMapLayoutHaveDifferentPressurePlatePositions() {
    String levelDirectionLayout = "rrg";
    String levelNothingOnTop = "pr d@";
    String saveNothingOnTop =  "prd @";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelNothingOnTop,  levelDirectionLayout, saveNothingOnTop, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
    String levelNotSameSpotRockOnTop = "pd  o@";
    String saveNotSameSpotRockOnTop =  "pd o @";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelNotSameSpotRockOnTop,  levelDirectionLayout, saveNotSameSpotRockOnTop, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
    String levelNotSameSpotRockNotOnTop =  "pd rd@";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelNotSameSpotRockNotOnTop,  levelDirectionLayout, saveNotSameSpotRockOnTop, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
    String levelNotSameSpotPlayerNotOnTop = "rd pd@";
    String saveNotSameSpotPlayerOnTop =     "rd q @";
    assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelNotSameSpotPlayerNotOnTop,  levelDirectionLayout, saveNotSameSpotPlayerOnTop, levelDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
    }
    @Test
    @DisplayName("Check that level and save map layouts with pressure plates at the same spots do not throw exceptions, even if there are players/rocks standing on top of them.")
    public void testLevelAndSaveMapLayoutHaveIdenticalPressurePlatePositions() {
        String levelDirectionLayout = "rrg";
        String levelSameSpotPlayerNotOntop = "dr pd@";
        String saveSameSpotPlayerOntop =     "dr  q@";

        Assertions.assertDoesNotThrow(
            () -> new PushRock("Test", levelSameSpotPlayerNotOntop,  levelDirectionLayout, saveSameSpotPlayerOntop, levelDirectionLayout, 0),
            "No exceptions should be thrown when the pressure plates are placed at the same spots between the level and save map layouts.");
        String levelSameSpotRockNotOntop = "dp rd@";
        String saveSameSpotRockOntop =     "dp  o@";
        Assertions.assertDoesNotThrow(
            () -> new PushRock("Test", levelSameSpotRockNotOntop,  levelDirectionLayout, saveSameSpotRockOntop, levelDirectionLayout, 0),
            "No exceptions should be thrown when the pressure plates are placed at the same spots between the level and save map layouts.");
    }
    @Test
    @DisplayName("Check that level and save map layouts with differing counts of rocks results in IllegalArgumentException being thrown.")
    public void testLevelAndSaveMapLayoutsWithDifferentRockCounts() {
        String levelDirectionLayout = "rrg";
        String saveDirectionLayout = "rrrg";
        String levelOneRock =  "pd r @";
        String saveTwoRocks  = "pd rr@";
        assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelOneRock,  levelDirectionLayout, saveTwoRocks, saveDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
        String levelOneRockWithNoneAtAPressurePlate =  "pd rd@";
        String saveTwoRocksWithOneAtAPressurePlate  =  "pd ro@";
        assertThrows(
        IllegalArgumentException.class,
        () -> new PushRock("Test", levelOneRockWithNoneAtAPressurePlate,  levelDirectionLayout, saveTwoRocksWithOneAtAPressurePlate, saveDirectionLayout, 0),
        "IllegalArgumentException should be thrown when pressure plates are placed differently between the level and save map layouts.");
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
    @DisplayName("Check that successfully moving a player through move input increments the move count.")
    public void testIncrementMoveCountSuccessfulMovement() {
        String expected = "wp  d@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertEquals(0, pushRock.getMoveCount());
        pushRock.movePlayer("right");
        assertEquals(1, pushRock.getMoveCount());
    }
    @Test
    @DisplayName("Check that only changing direction through move input does not increment the move count.")
    public void testIncrementMoveCountUnSuccessfulMovement() {
        String expected = "wp  d@";
        PushRock pushRock = new PushRock("Test", expected, "rg");
        assertEquals(0, pushRock.getMoveCount());
        pushRock.movePlayer("left");
        assertEquals(0, pushRock.getMoveCount());
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
    @DisplayName("Check that the player is placed at correct coordinates and with the correct direction after they have entered a connected portal for any possible entry and exit direction.")
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
    @DisplayName("Check that a rock can be pushed through a connected portal while player pushing it is placed at its exit point in a straight loop.")
    public void testPushRockThroughAConnectedPortalStraightLoop() {
        String map = " vpru d@";
        PushRock pushRock = new PushRock("test", map, "ruulg");
        String expected = " ṿprụ d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = " ṿrpụ d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = " ṿprụ d@";
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }
    @Test
    @DisplayName("Check that a rock can be pushed through a connected portal while player pushing it is placed at its exit point in a corner.")
    public void testPushRockThroughAConnectedPortalCorner() {
        String map = """
                 prv@
                duww@
                """;
        PushRock pushRock = new PushRock("test", map, "rrlug");
        String expected = """
                 prṿ@
                dụww@
                """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = """
                 rpṿ@
                dụww@
                """;
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
    @DisplayName("Check that a player standing on a falling moveable rock can move in all directions except down.")
    public void testMainProtagonistPhysics() {
        String map = """
            ------D@
            --P----@
            --RR---@
            -------@
            """;
        PushRock pushRock = new PushRock("Test", map, "rrrg");
        pushRock.setGravityApplicationManual();
        assertEqualsNoLineSeparator(map, pushRock.toString());
        pushRock.movePlayer("down");
        map = """
            ------D@
            --P----@
            --RR---@
            -------@
            """;
        assertEqualsNoLineSeparator(map, pushRock.toString());
        pushRock.movePlayer("right");
        map = """
            ------D@
            ---P---@
            --RR---@
            -------@
            """;
        assertEqualsNoLineSeparator(map, pushRock.toString());
        pushRock.movePlayer("left");
        map = """
            ------D@
            --P----@
            --RR---@
            -------@
            """;
        assertEqualsNoLineSeparator(map, pushRock.toString());
        pushRock.movePlayer("up");
        map = """
            --P---D@
            -------@
            --RR---@
            -------@
            """;
        assertEqualsNoLineSeparator(map, pushRock.toString());
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
    @DisplayName("Check that a player can jump one step up while carrying a single block that is stacked ontop of them.") 
    public void testPlayerCanJumpWhileCarryingOneBlock() {
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
    @DisplayName("Check that a player's carried block stack gets left behind the player enters a connected transporter.") 
    public void testPlayerCollidingWithConnectedTransporterWhileCarryingBlocks() {
        String mapLevelLayout = """
            T-----oD@
            ---R----@
            ---R----@
            ---R----@
            ---R----@
            ---PT---@
            wwwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "rrrrrrg");
        pushRock.setGravityApplicationManual();
        String expected = """
            Ṭ-----oD@
            ---R----@
            ---R----@
            ---R----@
            ---R----@
            ---PṬ---@
            wwwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.movePlayer("right");
        expected = """
            ṬP----oD@
            ---R----@
            ---R----@
            ---R----@
            ---R----@
            ----Ṭ---@
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
    @DisplayName("Check that moveable block chains stacked ontop of portals both facing the opposite direction of gravity results in the falling blocks attempting to equal out the weight balance of both sides.")
    public void testMoveableStackBalancePortalDirectionsOpposingGravity() {
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

    @Test
    @DisplayName("Check that a block chain falling into a portal whose exit is horizontal falls only for as long as the block chain at the exit porter is not longer than the falling block chain's weight, and that the movement and collision that might occur is resolved in the expected pattern.")
    public void testMoveableStackBalancePortalOneHorizontalOtherOpposingGravity() {
        String mapLevelLayout = """
            w-R-----D@
            w-R------@
            w-R------@
            w-R------@
            w-R------@
            U-R------@
            w-P------@
            wwVwwwwww@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "dddddrddug");
        assertFalse(pushRock.isGravityInverted());
        String expected = """
            w-R-----D@
            w-R------@
            w-R------@
            w-R------@
            w-R------@
            Ụ-R------@
            w-P------@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w-R------@
            w-R------@
            w-R------@
            w-R------@
            ỤPR------@
            w-R------@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w-R------@
            w-R------@
            w-R------@
            w-R------@
            ỤRP------@
            w--R-----@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w--------@
            w-R------@
            w-R------@
            w-R------@
            Ụ-R------@
            wRPR-----@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w--------@
            w--------@
            w-R------@
            w-R------@
            ỤPR------@
            wRRR-----@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w--------@
            w--------@
            w-R------@
            w-R------@
            ỤRPR-----@
            wR-R-----@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            w-------D@
            w--------@
            w--------@
            w--------@
            w-R------@
            ỤRRR-----@
            wRPR-----@
            wwṾwwwwww@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        //Now that horizontal chain's length is equal to the falling chains weight, the falling chain should no longer be heavy enough to move the horizontal chain, 
        //applying gravity should result in no change from the previous expected map outcome.
        pushRock.gravityStep();
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    @Test
    @DisplayName("Check that a moveable block can fall into a transporter even if it places it at the entrance of transporter of another transporter-pairing.")
    public void testFallIntoTransporterAndLandAtAnotherTransporterPairingEntranceLoop() {
        String mapLevelLayout = """
            -V--@
            -P--@
            -T--@
            ----@
            -T--@
            ---d@
            -U-o@
            """;
        PushRock pushRock = new PushRock("Test", mapLevelLayout, "drurg");
        assertFalse(pushRock.isGravityInverted());
        String expected = """
            -Ṿ--@
            -P--@
            -Ṭ--@
            ----@
            -Ṭ--@
            ---d@
            -Ụ-o@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            -Ṿ--@
            ----@
            -Ṭ--@
            ----@
            -Ṭ--@
            -P-d@
            -Ụ-o@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        pushRock.gravityStep();
        expected = """
            -Ṿ--@
            -P--@
            -Ṭ--@
            ----@
            -Ṭ--@
            ---d@
            -Ụ-o@
            """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
    }

    //About 'testFallIntoTransporterAndLandAtAnotherTransporterPairingEntranceChainLoop()'
    //The following test revealed an edge-case that initially caused a stack overflow error because of an infinte loop one of the methods entered
    //when tackling two chained transporter pairs. While I have made necessary changes to prevent that error from occuring, I have not yet achieved
    //the behaviour that I would have liked for this particular scenario. 

    //As illustrated in the test by the expected map: I would have wanted the rock and player to continuously swap positions as to simulate a continuous fall through the transporters.
    //For now however they will instead stand still until one of them makes room for the other to fall.

    // @Test
    // @DisplayName("Check that two moveable blocks loops/swaps positions when stuck in a falling loop between two transporter pairs.")
    // public void testFallIntoTransporterAndLandAtAnotherTransporterPairingEntranceChainLoop() {
    //     String mapLevelLayout = """
    //         -V--@
    //         -P--@
    //         -T--@
    //         ----@
    //         -T--@
    //         -R-d@
    //         -U-o@
    //         """;
    //     PushRock pushRock = new PushRock("Test", mapLevelLayout, "drrurg");
    //     assertFalse(pushRock.isGravityInverted());
    //     String expected = """
    //         -Ṿ--@
    //         -P--@
    //         -Ṭ--@
    //         ----@
    //         -Ṭ--@
    //         -R-d@
    //         -Ụ-o@
    //         """;
    //     assertEqualsNoLineSeparator(expected, pushRock.toString());
    //     pushRock.gravityStep();
    //     expected = """
    //         -Ṿ--@
    //         -R--@
    //         -Ṭ--@
    //         ----@
    //         -Ṭ--@
    //         -P-d@
    //         -Ụ-o@
    //         """;
    //     assertEqualsNoLineSeparator(expected, pushRock.toString());
    //     pushRock.gravityStep();
    //     expected = """
    //         -Ṿ--@
    //         -P--@
    //         -Ṭ--@
    //         ----@
    //         -Ṭ--@
    //         -R-d@
    //         -Ụ-o@
    //         """;
    //     assertEqualsNoLineSeparator(expected, pushRock.toString());
    // }


    //TestObserverPushRock is a simple class implementing IObserverPushRock only as a tool to test that the methods PushRock
    //inherits from AbstractObservablePushRock behave as expected.
    class TestObserverPushRock implements IObserverPushRock {
        private int updateCount;
        public TestObserverPushRock() {
            this.updateCount = 0;
        }
        public int getUpdateCount() {
            return this.updateCount;
        }
        @Override
        public void update(AbstractObservablePushRock observable) {
            this.updateCount++;
        }
    }

    @Test
    @DisplayName("Check that observerers that have been added get notified.")
    public void testAddObservers() {
        PushRock pushRock = new PushRock("test", "dp   @", "rg");
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.notifyObservers();
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.addObserver(testObserverPushRock);
        pushRock.notifyObservers();
        assertEquals(1, testObserverPushRock.getUpdateCount());
        pushRock.notifyObservers();
        assertEquals(2, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observerers that have been removed no longer get notified.")
    public void testRemoveObservers() {
        PushRock pushRock = new PushRock("test", "dp   @", "rg");
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.notifyObservers();
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.addObserver(testObserverPushRock);
        pushRock.notifyObservers();
        assertEquals(1, testObserverPushRock.getUpdateCount());
        pushRock.removeObserver(testObserverPushRock);
        pushRock.notifyObservers();
        assertEquals(1, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are notified after the player was moved successfully through move input.")
    public void testObserversMoveInputCoordinateChange() {
        PushRock pushRock = new PushRock("test", "dp   @", "rg");
        assertEqualsNoLineSeparator("dp   @", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("d p  @", pushRock.toString());
        assertEquals(0, testObserverPushRock.getUpdateCount());
        pushRock.addObserver(testObserverPushRock);
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("d  p @", pushRock.toString());
        assertEquals(1, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are notified if the player's direction was changed after move input.")
    public void testObserversMoveInputPlayerDirectionChange() {
        PushRock pushRock = new PushRock("test", "dpw@", "lg");
        assertEqualsNoLineSeparator("dpw@", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("dpw@", pushRock.toString());
        assertEquals(1, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are not notified if the player did not move or change direction after move input.")
    public void testObserversMoveInputPlayerNoChange() {
        PushRock pushRock = new PushRock("test", "dpw@", "rg");
        assertEqualsNoLineSeparator("dpw@", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.movePlayer("right");
        assertEqualsNoLineSeparator("dpw@", pushRock.toString());
        assertEquals(0, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are notified if gravityStep moved any moveable block.") 
    public void testObserversGravityStepWithChange() {
        String map = """
                oRd@
                --P@
                ---@
                """;
        PushRock pushRock = new PushRock("test", map, "rrrg");
        assertEqualsNoLineSeparator(map, pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.gravityStep();
        String expected = """
                o-d@
                -R-@
                --P@
                """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertEquals(1, testObserverPushRock.getUpdateCount());
        pushRock.gravityStep();
        expected = """
                o-d@
                ---@
                -RP@
                """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertEquals(2, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are not notified if gravityStep did not move any moveable blocks.") 
    public void testObserversGravityStepWithNoChange() {
        String map = """
                o-d@
                -RP@
                """;
        PushRock pushRock = new PushRock("test", map, "rrrg");
        assertEqualsNoLineSeparator(map, pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.gravityStep();
        String expected = """
                o-d@
                -RP@
                """;
        assertEqualsNoLineSeparator(expected, pushRock.toString());
        assertEquals(0, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are notified once a portal placement changes the type representation of a block.")
    public void testObserversPortalPlacementChangedType() {
        PushRock pushRock = new PushRock("test", "dpw@", "rg");
        assertEqualsNoLineSeparator("dpw@", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        //Place a portal one
        pushRock.placePortal(true);
        assertEqualsNoLineSeparator("dpv@", pushRock.toString());
        assertEquals(1, testObserverPushRock.getUpdateCount());
        //Place a portal two to replace the old portal one.
        pushRock.placePortal(false);
        assertEqualsNoLineSeparator("dpu@", pushRock.toString());
        //Observers should also be notified if a portal is placed by another portal type
        assertEquals(2, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are notified once portal placement results only in a change of the portal's direction.")
    public void testObserversPortalPlacementChangedDirection() {
        PushRock pushRock = new PushRock("test", "dpv@", "rug");
        assertEqualsNoLineSeparator("dpv@", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.placePortal(true);
        assertEqualsNoLineSeparator("dpv@", pushRock.toString());
        assertEquals(1, testObserverPushRock.getUpdateCount());
    }
    @Test
    @DisplayName("Check that observers are not notified once a portal placement results in no change.")
    public void testObserversPortalPlacementNoChange() {
        PushRock pushRock = new PushRock("test", "dpv@", "rlg");
        assertEqualsNoLineSeparator("dpv@", pushRock.toString());
        TestObserverPushRock testObserverPushRock = new TestObserverPushRock();
        pushRock.addObserver(testObserverPushRock);
        pushRock.placePortal(true);
        assertEqualsNoLineSeparator("dpv@", pushRock.toString());
        assertEquals(0, testObserverPushRock.getUpdateCount());
    }


    @Nested
    class TestObserverIntervalNotifier {

        private IntervalNotifier intervalNotifier;
        private PushRock pushRock;
        private String levelMapLayout;

        @BeforeEach
        private void setup() {
            this.levelMapLayout = """
                -----@
                --P-R@
                -----@
                d----@
                """;
            this.pushRock = new PushRock("test", levelMapLayout, "rrg");
            this.intervalNotifier = new IntervalNotifier(1000);
        }
    
        @Test
        @DisplayName("Check that pushRock is only notified, and updates accordingly, once it has been added as an observer of the interval notifier.")
        public void testUpdateAfterAddedObserverPushRock() {
            assertEqualsNoLineSeparator(levelMapLayout, this.pushRock.toString());
            //issue the interval notifier to notify its observers
            intervalNotifier.notifyObservers();
            //pushRock is not yet added as an observer of the interval notifier, and should thus not be issued to update once the interval notifier notifies its observers
            assertEqualsNoLineSeparator(levelMapLayout, this.pushRock.toString());
            //add pushRock as an observer of the interval notifier
            this.intervalNotifier.addObserver(this.pushRock);
            //issue the interval notifier to notify its observers
            intervalNotifier.notifyObservers();
            String expected = """
                -----@
                -----@
                --P-R@
                d----@
                """;
            //pushRock should now have been notified and thus been issued to update, which should call the gravityStep method which should update the game's state accordingly.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //pushRock should then keep recieving notifications for as long as it remains an observer of the interval notifier
            intervalNotifier.notifyObservers();
            expected = """
                -----@
                -----@
                -----@
                d-P-R@
                """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
        @Test
        @DisplayName("Check that pushRock is no longer notified, and thus no longer udpates, once it has been removed as an observer from the interval notifier.")
        public void testRemoveObserverPushRock() {
            assertEqualsNoLineSeparator(levelMapLayout, this.pushRock.toString());
            this.intervalNotifier.addObserver(this.pushRock);
            intervalNotifier.notifyObservers();
            String expected = """
                -----@
                -----@
                --P-R@
                d----@
                """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            this.intervalNotifier.removeObserver(this.pushRock);
            expected = """
                -----@
                -----@
                --P-R@
                d----@
                """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
        @Test
        @DisplayName("Check that pushRock no longer applies gravity after 'pauseIntervalGravity(true)' has been called.")
        public void testIgnoreIntervalNotifier() {
            assertEqualsNoLineSeparator(levelMapLayout, this.pushRock.toString());
            this.intervalNotifier.addObserver(this.pushRock);
            //pause interval gravity to temporarily stop applying gravity once notified.
            this.pushRock.pauseIntervalGravity(true);
            intervalNotifier.notifyObservers();
            String expected = """
                -----@
                -----@
                --P-R@
                d----@
                """;
            assertEqualsNoLineSeparator(expected, pushRock.toString());
            //Unpause interval gravtiy to start applying gravity once notified again.
            this.pushRock.pauseIntervalGravity(false);
            intervalNotifier.notifyObservers();
            expected = """
                -----@
                -----@
                -----@
                d-P-R@
                """;
            //pushRock should now continue to apply gravity once it is notified.
            assertEqualsNoLineSeparator(expected, pushRock.toString());
        }
    } 
}
