package pushrock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pushrock.fxui.SaveHandler;
import pushrock.model.BlockAbstract;
import pushrock.model.DirectedBlock;
import pushrock.model.ObstacleBlock;
import pushrock.model.PushRock;
import pushrock.model.TraversableBlock;

public class SaveHandlerTest {
    
    private SaveHandler saveHandler;

    private PushRock getPushRockLevel(String levelName, String levelMapLayout, String levelDirectionLayout) {
        return new PushRock(levelName, levelMapLayout, levelDirectionLayout);
    }
    private PushRock getPushRockSave(String levelName, String levelMapLayout, String levelDirectionLayout, String saveMapLayout, String saveDirectionLayout, int saveMoveCount) {
        return new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, saveMoveCount);
    }

    @BeforeEach 
    public void setup() {
        saveHandler = new SaveHandler();
    }

    @Test
    @DisplayName("Test that the game is saved to the output stream with correct formating.")
    public void testSaveGameToOutputStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String levelName = "Level test";
        String levelMapLayout = """
            prrrrotwuvd -@""";
        String levelDirectionLayout = "uudrlurlg";
        PushRock pushRock = getPushRockLevel(levelName, levelMapLayout, levelDirectionLayout);
        saveHandler.saveGame(pushRock, outputStream);

        String actual = new String(outputStream.toByteArray()).replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));

        String expected = """
        #File type:
        Save
        
        #Level name:
        Level test
        
        #Level map layout:
        prrrrotwuvd -@
        
        #Level direction layout:
        uudrlurlg
        
        #Save map layout:
        prrrrotwuvd -@
        
        #Save direction layout:
        uudrlurlg
        
        #Save move count:
        0
        """.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));

        assertEquals(expected, actual, "Incorrect save format, actual string representation of save does not match the expected one.");
    }

    @Test
    public void testSaveCompletedGame() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String levelName = "Level test";
        String levelMapLayout = """
            pd@""";
        String levelDirectionLayout = "lg";
        PushRock pushRock = getPushRockLevel(levelName, levelMapLayout, levelDirectionLayout);
        pushRock.movePlayer("right");
        assertThrows(
            IllegalArgumentException.class, 
            () -> saveHandler.saveGame(pushRock, outputStream), 
            "Attempting to save a completed game should throw IllegalArgumentException.");
    }
    
    //Check that all PushRock values that should be set based on a given level/save file's information are equal for the expected and actual PushRock.
    private void checkThatExpectedAndActualPushRocksAreEqual(PushRock expectedPushRock, PushRock actualPushRock) {
        //Check that level names match.
        String expectedLevelName = expectedPushRock.getLevelName();
        String actualLevelName = expectedPushRock.getLevelName();
        assertEquals(expectedLevelName, actualLevelName, "Level name should be match that of the loaded");
        //Check that level map layouts match.
        String expectedLevelMapLayout = expectedPushRock.getLevelMapLayout().replaceAll("\\n|\\r\\n", System.getProperty("line.separator")).stripTrailing();
        String actualLevelMapLayout = actualPushRock.getLevelMapLayout().replaceAll("\\n|\\r\\n", System.getProperty("line.separator")).stripTrailing();
        assertEquals(expectedLevelMapLayout, actualLevelMapLayout);
        //Check that move counts match.
        int expectedMoveCount = expectedPushRock.getMoveCount();
        int actualMoveCount = actualPushRock.getMoveCount();
        assertEquals(expectedMoveCount, actualMoveCount);
        //Check that map widths match.
        int expectedWidth = expectedPushRock.getWidth();
        int actualWidth = actualPushRock.getWidth();
        assertEquals(expectedWidth, actualWidth);
        //Check that map heights match.
        int expectedHeight = expectedPushRock.getHeight();
        int actualHeight = actualPushRock.getHeight();
        assertEquals(expectedHeight, actualHeight);
        //Check that gravity directions match.
        boolean expectedIsGravityInverted = expectedPushRock.isGravityInverted();
        boolean actualIsGravityInverted = actualPushRock.isGravityInverted();
        assertEquals(expectedIsGravityInverted, actualIsGravityInverted);
        //Check that game's completion states match.
        boolean expectedIsGameOver = expectedPushRock.isGameOver();
        boolean actualIsGameOver = actualPushRock.isGameOver();
        assertEquals(expectedIsGameOver, actualIsGameOver);

        //For every coordinate, check that the blocks at the given coordiante match.
        for (int x = 0; x < expectedWidth; x++) {
            for (int y = 0; y < expectedWidth; y++) {
                //Check that a given coordinate holds matching traversable blocks.
                TraversableBlock expectedTraversableBlock = expectedPushRock.getTraversableBlockCopy(x, y);
                TraversableBlock actualTraversableBlock = actualPushRock.getTraversableBlockCopy(x, y);
                if (expectedTraversableBlock == null) {
                    assertNull(actualTraversableBlock, "When the expected PushRock has no traversable block at a given coordinate, then the actual one should also have no traversable block at that coordinate.");
                }
                else {
                    assertEquals(expectedTraversableBlock.getType(), actualTraversableBlock.getType(), "The traversable block type for the actual PushRock should be the same as the expected one.");
                    assertEquals(expectedTraversableBlock.getState(), actualTraversableBlock.getState(), "The state of the actual traversable block should be the same as the expected one.");
                }
                //Check that a given coordinate holds matching top-blocks.
                BlockAbstract expectedTopBlock = expectedPushRock.getTopBlockCopy(x, y);
                BlockAbstract actualTopBlock = actualPushRock.getTopBlockCopy(x, y);
                if (expectedTopBlock == null) {
                    assertNull(actualTopBlock, "When the expected PushRock has no top-block at a given coordinate, then the actual one should also have no traversable block at that coordinate.");
                }
                else {
                    assertEquals(expectedTopBlock.getType(), actualTopBlock.getType());
                    assertEquals(expectedTopBlock.getState(), actualTopBlock.getState());
                    if (expectedTopBlock instanceof DirectedBlock) {
                        assertTrue(actualTopBlock instanceof DirectedBlock, "When the expected top-block is a directed block, then the actual top-block should also be a directed top-block");
                        assertEquals(((DirectedBlock) expectedTopBlock).getDirection(), ((DirectedBlock) actualTopBlock).getDirection(), "For two directed top-blocks to be equal, their directions should also be equal.");
                        if (expectedTopBlock instanceof ObstacleBlock) {
                            assertTrue(actualTopBlock instanceof ObstacleBlock, "When the expected top-block is an obstacle block, then the actual top-block should also be an obstacle top-block");
                            ObstacleBlock expectedConnection = ((ObstacleBlock) expectedTopBlock).getConnection();
                            ObstacleBlock actualConnection = ((ObstacleBlock) actualTopBlock).getConnection();
                            if (expectedConnection == null) {
                                assertNull(actualConnection, "When the expected top-block has no connection, then so should the actual top-block.");
                            }
                            else {
                                assertNotNull(actualConnection, "When the expected top-block has a connection, then the so should the actual top-block.");
                                assertEquals(expectedConnection.getX(), actualConnection.getX(), "The actual top-block's connection should have the same x-coordinate as the expected top-block's connection.'");
                                assertEquals(expectedConnection.getY(), actualConnection.getY(), "The actual top-block's connection should have the same y-coordinate as the expected top-block's connection.'");
                                assertEquals(expectedConnection.getType(), actualConnection.getType(), "The actual top-block's connection should have the same type as the expected top-block's connection.'");
                                assertEquals(expectedConnection.getState(), actualConnection.getState(), "The actual top-block's connection should have the same state as the expected top-block's connection.'");
                                assertEquals(expectedConnection.getDirection(), actualConnection.getDirection(), "The actual top-block's connection should have the same direction as the expected top-block's connection.'");
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Test that loading a PushRock level from an input stream results in a PushRock object with correct values.")
    public void testLoadGameLevelFromInputStream() throws UnsupportedEncodingException {
        String levelName = "Level test";
        String levelMapLayout = """
            prrrrotwud @
            -RRRROTWVd-@
            """;
        String levelDirectionLayout = "uurdludurdluug";

        String levelStringRepresentation = """
            #File type:
            Level
            
            #Level name:
            Level test
            
            #Level map layout:
            prrrrotwud @
            -RRRROTWVd-@
            
            #Level direction layout:
            uurdludurdluug
            """.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));
        InputStream inputStream = new ByteArrayInputStream(levelStringRepresentation.getBytes("UTF-8"));

        PushRock expectedPushRock = getPushRockLevel(levelName, levelMapLayout, levelDirectionLayout);
        PushRock actualPushRock = saveHandler.loadGame(inputStream);
        checkThatExpectedAndActualPushRocksAreEqual(expectedPushRock, actualPushRock);
    }

    @Test
    @DisplayName("Test that loading a PushRock save from an input stream results in a PushRock object with correct values.")
    public void testLoadGameSaveFromInputStream() throws UnsupportedEncodingException {
        String levelName = "Level test";
        String levelMapLayout = """
            prrrrotwud @
            -RRRROTWVd-@
            """;
        String levelDirectionLayout = "uurdludurdluug";
        String saveMapLayout = """
             rrrrotwud @
            PRRRROTWVd-@
            """;
        String saveDirectionLayout = "urdlududrdluug";
        int moveCount = 4;

        String saveStringRepresentation = """
            #File type:
            Save
            
            #Level name:
            Level test
            
            #Level map layout:
            prrrrotwud @
            -RRRROTWVd-@
            
            #Level direction layout:
            uurdludurdluug
            
            #Save map layout:
             rrrrotwud @
            PRRRROTWVd-@
            
            #Save direction layout:
            urdlududrdluug
            
            #Save move count:
            4
            """.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));
        InputStream inputStream = new ByteArrayInputStream(saveStringRepresentation.getBytes("UTF-8"));

        PushRock expectedPushRock = getPushRockSave(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, moveCount);
        PushRock actualPushRock = saveHandler.loadGame(inputStream);
        checkThatExpectedAndActualPushRocksAreEqual(expectedPushRock, actualPushRock);
    }

    @Nested
    class TestNestInvalidSaveFormats {
        private String validSaveString = """
            #File type:
            Save
            
            #Level name:
            Level test
            
            #Level map layout:
            prrrrotwud @
            -RRRROTWVd-@
            
            #Level direction layout:
            uurdludurdluug
            
            #Save map layout:
            prrrrotwud @
            -RRRROTWVd-@
            
            #Save direction layout:
            uurdludurdluug
            
            #Save move count:
            0
            """.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));

        @Test
        @DisplayName("Check that loading from an inputstream that has invalid content titles throws IllegalArgumentException.") 
        public void testInvalidTitles() throws UnsupportedEncodingException { 
            String invalidFileTypeTitle = this.validSaveString.replace("#File type:", "#InvalidTitle:");
            String invalidLevelNameTitle = this.validSaveString.replace("#Level name:", "#InvalidTitle:");
            String invalidLevelMapLayoutTitle = this.validSaveString.replace("#Level map layout:", "#InvalidTitle:");
            String invalidLevelDirectionLayoutTitle = this.validSaveString.replace("#Level direction layout:", "#InvalidTitle:");
            String invalidSaveMapLayoutTitle = this.validSaveString.replace("#Save map layout:", "#InvalidTitle:");
            String invalidSaveDirectionLayoutTitle = this.validSaveString.replace("#Save direction layout:", "#InvalidTitle:");
            String invalidMoveCountTitle = this.validSaveString.replace("#Save move count:", "#InvalidTitle:");
            String[] invalidSaveStrings = new String[] {invalidFileTypeTitle, invalidLevelNameTitle, invalidLevelMapLayoutTitle, invalidLevelDirectionLayoutTitle, invalidSaveMapLayoutTitle, invalidSaveDirectionLayoutTitle, invalidMoveCountTitle};
            for (String save : invalidSaveStrings) {
                InputStream inputStream = new ByteArrayInputStream(save.getBytes("UTF-8"));
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> saveHandler.loadGame(inputStream), 
                    "An invalid title should throw IllegalArgumentException.");
            }
        }

        @Test
        @DisplayName("Check that loading from an inputstream that has no content throws IllegalArgumentException.") 
        public void testLoadEmptyInputStream() throws UnsupportedEncodingException {
            InputStream inputStream = new ByteArrayInputStream("".getBytes("UTF-8"));
            assertThrows(
                IllegalArgumentException.class, 
                () -> saveHandler.loadGame(inputStream), 
                "Loading from an empty inputstream should throw IllegalArgumentException.");
        }

        @Test
        @DisplayName("Check that loading from an inputstream that is missing the delimiter throws IllegalArgumentException.")
        public void testLoadNoDelimiterInputStream() throws UnsupportedEncodingException {
            String noDelimiter = this.validSaveString.replaceAll("#", "");
            InputStream inputStream = new ByteArrayInputStream(noDelimiter.getBytes("UTF-8"));
            assertThrows(
                IllegalArgumentException.class, 
                () -> saveHandler.loadGame(inputStream), 
                "Loading from an input stream that is missing the delimiter should throw IllegalArgumentException.");
        }

        @Test
        @DisplayName("Check that loading from an inputstream that is missing the ':' separator throws IllegalArgumentException.")
        public void testLoadNoSeparatorInputStream() throws UnsupportedEncodingException {
            String noDelimiter = this.validSaveString.replaceAll(":", "");
            InputStream inputStream = new ByteArrayInputStream(noDelimiter.getBytes("UTF-8"));
            assertThrows(
                IllegalArgumentException.class, 
                () -> saveHandler.loadGame(inputStream), 
                "Loading from an input stream that is missing the ':' separator should throw IllegalArgumentException.");
        }
        @Test
        @DisplayName("Check that loading from an inputstream that is missing content throws IllegalArgumentException.")
        public void testNoContentInputStream() throws UnsupportedEncodingException {
            String saveNoContent = """
                #File type:
                
                #Level name:
                
                #Level map layout:
                
                #Level direction layout:
                
                #Save map layout:
                
                #Save direction layout:
                
                #Save move count:

                """.replaceAll("\\n|\\r\\n", System.getProperty("line.separator"));

            InputStream inputStream = new ByteArrayInputStream(saveNoContent.getBytes("UTF-8"));
            assertThrows(
                IllegalArgumentException.class, 
                () -> saveHandler.loadGame(inputStream), 
                "Loading from an input stream that is missing content should throw IllegalArgumentException.");
        }
    }

    // @Test
    // @DisplayName("Check that ")





// + Path getResourceFoldersPath(String folder)                                        @O.r
// + PushRock loadGame(InputStream inputStream)                                  @O.r
// + PushRock loadGame(String fileName, boolean isSave)                       @O.r
// + PushRock loadGame(Path filePath)                                                      @O.r
// + void saveGame(PushRock pushRock, OutputStream outputStream)   @O.r
// + void saveGame(PushRock pushRock, Path savePath)                         @O.r
}