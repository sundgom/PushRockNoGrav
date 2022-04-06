package pushrocks.fxui;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pushrocks.model.BlockAbstract;
import pushrocks.model.DirectedBlock;
import pushrocks.model.PushRocks;

public class SaveHandler implements ISaveHandler {
   
    // @Override
    // public void save(String fileName, PushRocks pushRocks) throws FileNotFoundException {
        
    //     // try (PrintWriter printWriter = new PrintWriter(new File(getFilePath(fileName))) {
    //     try {
    //         PrintWriter printWriter = new PrintWriter(new File(getFilePath(fileName, true)));
    //         printWriter.println(pushRocks.toGameToSaveFormat());

    //         printWriter.flush(); //ensures that the printwriter is done writing to the file
    //         printWriter.close(); //closes the file
            

    //     }
    //     catch (Exception e) {
            
    //     }
        
    // }

    // @Override
    // public void load(String fileName) throws FileNotFoundException {
    //     Scanner scanner = new Scanner(new File(fileName));

    //     while(scanner.hasNextLine()) {
    //         String line = scanner.nextLine();
    //         String[] lineInfo = line.split(",");
    //     }
        
    // }

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
        Path levelPath = this.getResourceFoldersPath("levels");
        
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


    public String toGameToSaveFormat(PushRocks pushRocks) {
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
}
