package pushrocks.fxui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public PushRocks loadGame(InputStream inputStream) {
        PushRocks pushRocks = null;
        try (var scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("#");
            // PushRocks(String levelName, String mapLayout, String directionLayout, int moveCount, boolean isSave) //from save
            // PushRocks(String levelName, String mapLayout, String directionLayout) //from levelLoad
            String fileType = null;      //Save or Level
            String levelName = null;     //Name of the level
            String levelLayout = null;   
            
            
            String mapLayout = null;
            String directionLayout = null;
            String moveCount = null;

            while (scanner.hasNext()) {
                String nextScan = scanner.next();
                System.out.println(nextScan);
                String fieldName = nextScan.substring(0, nextScan.indexOf(":"));
                String fieldData = nextScan.substring(fieldName.length()+3).stripTrailing(); //Each fieldName is followed by a colon and a line shift (":\n"), the remainder will then be the data
                System.out.println("fieldName:" + fieldName);
                System.out.println("fieldData:" + fieldData);


                if (fieldName.contains("Map layout")) {
                    String garbage = fieldData.substring(fieldData.stripTrailing().length(), fieldData.length());
                    System.out.println(garbage + "length:" + garbage.length());
                    System.out.println(fieldData.replace("cr", "HELLLO"));
                    mapLayout = fieldData;
                }
                else if (fieldName.contains("Direction layout")) {
                    directionLayout = fieldData;
                }
                // if (fieldName == "Map layout") {

                // }
                // if (fieldName == "Map layout") {

                // }
                // if (fieldName == "Map layout") {

                // }
                
            }
            System.out.println("map:" + mapLayout + "|direction:" + directionLayout);
            pushRocks = new PushRocks(mapLayout, directionLayout);
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
    public PushRocks loadGame(Path filePath) {
        return null;
        
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

    }

    // public void saveGame(PushRocks pushRocks) throws IOException {
        
    // }
    public void saveGame(PushRocks pushRocks, String savePathString) throws IOException {
        Path savePath = Paths.get(savePathString);
        try (var outputStream = new FileOutputStream(savePath.toFile())) {
            String layout = gameToSaveFormat(pushRocks);

        }
    }


    public String gameToSaveFormat(PushRocks pushRocks) {
        if (pushRocks.isGameOver()) {
            throw new IllegalArgumentException("Can not save a completed game.");
        }
        System.out.println("Save format start.");
        String mapLayoutSave = "";
        String directionLayoutSave = ">";
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
            mapLayoutSave += "\n";
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
        return mapLayoutSave + directionLayoutSave;
    }
    public static void main(String[] args) {
        SaveHandler save = new SaveHandler();
        try {
            save.loadGame("Level 1", false);
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
