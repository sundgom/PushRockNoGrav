package pushrocks.fxui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.PushRocks;

public class SaveHandler implements ISaveHandler {


    public List<String> getLevelNames() {
        Path levelsFolderPath = getResourceFoldersPath("levels");
        File[] levelFiles = levelsFolderPath.toFile().listFiles();
        List<String> levelFileNames = new ArrayList<String>();
        for (File levelFile : levelFiles) {
            String fileName = levelFile.getName();
            if (fileName.endsWith(".txt")) {
                levelFileNames.add(fileName.substring(0, fileName.lastIndexOf(".")));
            }
        }
        return levelFileNames;
    }

    public Path getResourceFoldersPath(String folderName) {
        return Path.of(System.getProperty("user.dir"), "src", "main", "resources", "pushrocks", folderName);
    }

    // public Path getFilePath(String fileName) {
        
    // }

    @Override
    public void loadGameLevel(String filename, PushRocks pushrocks) throws FileNotFoundException {
        
    }

    @Override
    public void saveGame(Path filePath, PushRocks pushrocks) throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadGame(Path filePath, PushRocks pushrocks) throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadGameSave(Path filePath, PushRocks pushrocks) throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }


    // public PushRocks(String levelName, String levelMapLayout, String levelDirectionLayout) 
    // public PushRocks(String levelName, String levelMapLayout, String levelDirectionLayout, String saveMapLayout, String saveDirectionLayout, int saveMoveCount) 


    public PushRocks loadGame(InputStream inputStream) {
        PushRocks pushRocks = null;
        try (var scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("#");
            // PushRocks(String levelName, String mapLayout, String directionLayout, int moveCount, boolean isSave) //from save
            // PushRocks(String levelName, String mapLayout, String directionLayout) //from levelLoad
            String fileType = null;      //Save or Level
            String levelName = null;     //Name of the level
            String levelMapLayout = null;
            String levelDirectionLayout = null;
            
            String saveMapLayout = null;
            String saveDirectionLayout = null;
            int saveMoveCount = -1;
            
            while (scanner.hasNext()) {
                String nextScan = scanner.next();
                System.out.println(nextScan);
                String fieldName = nextScan.substring(0, nextScan.indexOf(":"));
                String fieldData = nextScan.substring(fieldName.length()+3).stripTrailing(); //Each fieldName is followed by a colon and a line shift (":\n"), the remainder will then be the data
                System.out.println("fieldName:" + fieldName);
                System.out.println("fieldData:" + fieldData);

                
                if (fieldName.equals("File type")) {
                    fileType = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Level name")) {
                    levelName = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Level map layout")) {
                    levelMapLayout = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Level direction layout")) {
                    levelDirectionLayout = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Save map layout")) {
                    saveMapLayout = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Save direction layout")) {
                    saveDirectionLayout = fieldData.stripTrailing();
                }
                else if (fieldName.equals("Save move count")) {
                    String saveMoveCountString = fieldData.stripTrailing();
                    try {
                        saveMoveCount = Integer.parseInt(saveMoveCountString);
                    } catch (NumberFormatException e) {
                        //TODO: handle exception
                    }
                }
            }
            System.out.println("fileType:" + fileType + ", levelName:" + levelName);
            System.out.println("levelMapLayout:" + levelMapLayout + ", levelDirectionLayout:" + levelDirectionLayout);
            System.out.println("saveMapLayout:" + saveMapLayout + ", saveDirectionLayout:" + saveDirectionLayout + ", saveMoveCount:" + saveMoveCount);
            
            if (fileType == null) {
                throw new IllegalArgumentException("Could not find file type");
            }
            if (levelName == null) {
                throw new IllegalArgumentException("Could not find level name");
            }
            if (levelMapLayout == null) {
                throw new IllegalArgumentException("Could not find level map layout");
            }
            if (levelDirectionLayout == null) {
                throw new IllegalArgumentException("Could not find level direction layout");
            }

            if (fileType.equals("Level")) {
                pushRocks = new PushRocks(levelName, levelMapLayout, levelDirectionLayout);
            }
            else if (fileType.equals("Save")) {
                if (saveMapLayout == null) {
                    throw new IllegalArgumentException("Could not find save direction layout");
                }
                if (saveDirectionLayout == null) {
                    throw new IllegalArgumentException("Could not find save direction layout");
                }
                if (saveMoveCount == -1) {
                    throw new IllegalArgumentException("Could not find save move count");
                }
                pushRocks = new PushRocks(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, saveMoveCount);
            }
            else {
                throw new IllegalArgumentException("fileType must be either 'Level' or 'Save', but was: " + fileType);
            }

            // pushRocks = new PushRocks(levelMapLayout, levelDirectionLayout);
            // pushRocks = new PushRocks(levelName, levelMapLayout, levelDirectionLayout);
            // pushRocks = new PushRocks(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, saveMoveCount);
            
            // pushRocks.setlevelName(levelName);              //only allowed if levelName is empty
            // pushRocks.setLevelMapLayout(levelMapLayout);    //only allowed if levelMapLayout is empty
            // pushRocks.setMoveCount(saveMoveCount);          //only allowed if moveCount is 0
            
        }
        return pushRocks;
    }

    public PushRocks loadGame(String fileName, boolean isSave) throws FileNotFoundException, IOException {
        Path folderPath = null;
        if (isSave) {
            folderPath = this.getResourceFoldersPath("saves");
        }
        else {
            folderPath = this.getResourceFoldersPath("levels");
        }
        Path filePath = folderPath.resolve(fileName + ".txt");
        try (var inputStream = new FileInputStream(filePath.toFile())) {
            return loadGame(inputStream);
        }
    }
    public PushRocks loadGame(Path filePath) throws FileNotFoundException, IOException {
        try (var inputStream = new FileInputStream(filePath.toFile())) {
            return loadGame(inputStream);
        }
    }

    public PushRocks loadGame(String filePath) {
        // System.out.println(filePath);
        // Path path = Paths.get(filePath);

        // try (var input = new FileInputStream(path.toFile())) {
        //     // return readGameFile();
        //     return null;
        // }
        return null;
    }

    public void saveGame(PushRocks pushRocks, OutputStream outputStream) {
        try (var printWriter = new PrintWriter(outputStream)) {
            printWriter.println("#File type:");
            printWriter.println("Save");
            printWriter.println();
            printWriter.println("#Level name:");
            printWriter.println(pushRocks.getLevelName());
            printWriter.println();
            printWriter.println("#Level map layout:");
            printWriter.println(pushRocks.getLevelMapLayout());
            printWriter.println();
            printWriter.println("#Level direction layout:");
            printWriter.println(pushRocks.getLevelDirectionLayout());
            printWriter.println();
     
            List<String> gameLayout = this.gameLayoutToSaveFormat(pushRocks);
            String mapLayout = gameLayout.get(0);
            String directionLayout = gameLayout.get(1);
            printWriter.println("#Save map layout:");
            printWriter.println(mapLayout);
            printWriter.println("#Save direction layout:");
            printWriter.println(directionLayout);
            printWriter.println();
            printWriter.println("#Save move count:");
            printWriter.println(pushRocks.getMoveCount());
        }
    }

    public void saveGame(PushRocks pushRocks, Path savePath) throws IOException {
        // Path savePath = Paths.get(savePathString);
        try (var outputStream = new FileOutputStream(savePath.toFile())) {
            saveGame(pushRocks, outputStream);
        }
    }


    public List<String> gameLayoutToSaveFormat(PushRocks pushRocks) {
        if (pushRocks.isGameOver()) {
            throw new IllegalArgumentException("Can not save a completed game.");
        }
        System.out.println("Save format start.");
        String mapLayoutSave = "";
        String directionLayoutSave = "";
        int height = pushRocks.getHeight();
        int width = pushRocks.getWidth();


        for (int y = 0; y > height*(-1); y--) {
            for (int x = 0; x < width; x++) {
                char blockCopyType = '?';
                // char blockType = pushRocks.getTopBlockType(x, y);
                // String blockClass = pushRocks.getTopBlockClass(x, y);
                // boolean blockIsBirdView = pushRocks.getTopBlockBirdView(x, y);
                // pushRocks.getTopBlockDirection(x, y);
                // pushRocks.getTopBlockState(x, y);
                
                BlockAbstract blockCopy = pushRocks.getTopBlockCopy(x, y);
                blockCopyType = blockCopy.getType();

                //At most one directed block can occupy a given coordinate in the level, and this block must have a type, 
                // thus let this block's type represent this coordinate in the level layout string.
                if (blockCopy instanceof DirectedBlock) {
                    //If the directed block is a player, rock or portal, then it must have a specified direction. 
                    String direction = ((DirectedBlock) blockCopy).getDirection();
                    if (direction != null) {
                        directionLayoutSave += direction.charAt(0);
                    }
                }
                //Exactly one traversable block will occupy every coordinate in the level, thus when there are no directed blocks
                // placed ontop of it, the traversable block must itself represent the given coordinate in the level layout string.

                //At coordinates where the underlying traversable block has bird view disabled, the type representation to be
                // saved should be set to upper case.

                if (!pushRocks.getTraversableBlockCopy(x, y).isBirdView()) {
                    blockCopyType = Character.toUpperCase(blockCopyType);
                    //Since the type representation for traversable blocks is ' ', which can't be changed to uppercase, then 
                    // change it to '-' instead.
                    if (blockCopyType == ' ') {
                        blockCopyType = '-';
                    }
                }
                if(blockCopyType == '?') {
                    throw new IllegalArgumentException("There must be at least one block occupying the (" + x + ", " + y + ") coordinates, and they must have a type in order to be saved.");
                }
                mapLayoutSave += blockCopyType;
            }
            mapLayoutSave += "@\r\n";
        } 
        //At the very end of the direction layout string the letter 'g' should be added, lower case indicates that gravity was not inverted when the game was saved, wheras
        // uppercase indicates that it was inverted.
        if (!pushRocks.isGravityInverted()) {
            directionLayoutSave += 'g';
        }
        else {
            directionLayoutSave += 'G';
        }
        System.out.println("Save format end");
        List<String> layoutList = new ArrayList<String>();
        layoutList.add(mapLayoutSave);
        layoutList.add(directionLayoutSave);
        return layoutList;
    }
    public static void main(String[] args) {
        SaveHandler save = new SaveHandler();
        try {
            save.loadGame("Level 2", false);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // C:\Users\magnu\Documents\LocalUNI\2022V\GIT\TDT4100_prosjekt_magnsu\src\main\resources\pushrocks\levels\Level 1.txt
}
