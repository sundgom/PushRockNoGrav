package pushrock.fxui;

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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pushrock.model.BlockAbstract;
import pushrock.model.DirectedBlock;
import pushrock.model.PushRock;

public class SaveHandler implements ISaveHandler {

    @Override
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

    @Override
    public Path getResourceFoldersPath(String folderName) {
        return Path.of(System.getProperty("user.dir"), "src", "main", "resources", "pushrock", folderName);
    }

    @Override
    public PushRock loadGame(InputStream inputStream) throws IllegalArgumentException, NumberFormatException {
        PushRock pushRock = null;
        try (var scanner = new Scanner(inputStream)) {
            scanner.useDelimiter("#");
            String fileType = null;      //Save or Level
            String levelName = null;     //Name of the level
            String levelMapLayout = null;
            String levelDirectionLayout = null;
            
            String saveMapLayout = null;
            String saveDirectionLayout = null;
            int saveMoveCount = -1;
            
            while (scanner.hasNext()) {
                String nextScan = scanner.next();
                if (!nextScan.contains(":")) {
                    throw new IllegalArgumentException("Save format is invalid, missing ':' in title.");
                }
                String fieldName = nextScan.substring(0, nextScan.indexOf(":"));
                String fieldData = nextScan.substring(fieldName.length()+3).stripTrailing(); 

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
                    saveMoveCount = Integer.parseInt(saveMoveCountString);
                }
            }
            
            if (fileType == null) {
                throw new IllegalArgumentException("File is not formated correctly: could not find file type");
            }
            if (levelName == null) {
                throw new IllegalArgumentException("File is not formated correctly: could not find level name");
            }
            if (levelMapLayout == null) {
                throw new IllegalArgumentException("File is not formated correctly: could not find level map layout");
            }
            if (levelDirectionLayout == null) {
                throw new IllegalArgumentException("File is not formated correctly: could not find level direction layout");
            }

            if (fileType.equals("Level")) {
                if (saveMapLayout != null) {
                    throw new IllegalArgumentException("File is not formated correctly: a level-file should not contain a save map layout");
                }
                if (saveDirectionLayout != null) {
                    throw new IllegalArgumentException("File is not formated correctly: a level-file should not contain a save direction layout");
                }
                if (saveMoveCount != -1) {
                    throw new IllegalArgumentException("File is not formated correctly: a level-file should not contain a save move count");
                }
                pushRock = new PushRock(levelName, levelMapLayout, levelDirectionLayout);
            }
            else if (fileType.equals("Save")) {
                if (saveMapLayout == null) {
                    throw new IllegalArgumentException("File is not formated correctly: could not find save map layout");
                }
                if (saveDirectionLayout == null) {
                    throw new IllegalArgumentException("File is not formated correctly: could not find save direction layout");
                }
                if (saveMoveCount == -1) {
                    throw new IllegalArgumentException("File is not formated correctly: could not find save move count");
                }
                pushRock = new PushRock(levelName, levelMapLayout, levelDirectionLayout, saveMapLayout, saveDirectionLayout, saveMoveCount);
            }
            else {
                throw new IllegalArgumentException("fileType must be either 'Level' or 'Save', but was: " + fileType);
            }
        }
        return pushRock;
    }

    @Override
    public PushRock loadGame(String fileName, boolean isSave) throws FileNotFoundException, IOException {
        Path folderPath = null;
        if (isSave) {
            folderPath = this.getResourceFoldersPath("saves");
        }
        else {
            folderPath = this.getResourceFoldersPath("levels");
        }
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        Path filePath = folderPath.resolve(fileName);

        try (var inputStream = new FileInputStream(filePath.toFile())) {
            return loadGame(inputStream);
        }
    }
    @Override
    public PushRock loadGame(Path filePath) throws FileNotFoundException, IOException {
        if (filePath.toString().length() < 1) {
            throw new IllegalArgumentException("A file path or a file name of at least one character is needed to save.");
        }
        if (!filePath.toString().contains("\\")) {
            Path savePathOld = filePath;
            if (!savePathOld.endsWith(".txt")) {
                savePathOld = Paths.get(filePath.toString() + ".txt");
            }
            filePath = this.getResourceFoldersPath("saves").resolve(savePathOld);
        }
        try (var inputStream = new FileInputStream(filePath.toFile())) {
            return loadGame(inputStream);
        }
    }

    @Override
    public void saveGame(PushRock pushRock, OutputStream outputStream) {
        try (var printWriter = new PrintWriter(outputStream)) {
            printWriter.println("#File type:");
            printWriter.println("Save");
            printWriter.println();
            printWriter.println("#Level name:");
            printWriter.println(pushRock.getLevelName());
            printWriter.println();
            printWriter.println("#Level map layout:");
            printWriter.println(pushRock.getLevelMapLayout());
            printWriter.println();
            printWriter.println("#Level direction layout:");
            printWriter.println(pushRock.getLevelDirectionLayout());
            printWriter.println();
     
            List<String> gameLayout = this.gameLayoutToSaveFormat(pushRock);
            String mapLayout = gameLayout.get(0);
            String directionLayout = gameLayout.get(1);
            printWriter.println("#Save map layout:");
            printWriter.println(mapLayout);
            printWriter.println("#Save direction layout:");
            printWriter.println(directionLayout);
            printWriter.println();
            printWriter.println("#Save move count:");
            printWriter.println(pushRock.getMoveCount());
        }
    }
    @Override
    public void saveGame(PushRock pushRock, Path savePath) throws IOException {
        if (savePath.toString().length() < 1) {
            throw new IllegalArgumentException("A file path or a file name of at least one character is needed to save.");
        }
        if (!savePath.toString().contains("\\")) {
            Path savePathOld = savePath;
            savePath = this.getResourceFoldersPath("saves").resolve(savePathOld + ".txt");
        }
        try (var outputStream = new FileOutputStream(savePath.toFile())) {
            saveGame(pushRock, outputStream);
        }
    }

    private List<String> gameLayoutToSaveFormat(PushRock pushRock) {
        if (pushRock.isGameOver()) {
            throw new IllegalArgumentException("Can not save a completed game.");
        }
        String mapLayoutSave = pushRock.toString();
        //Accents will be removed, as they are only used as to visualize connected transporters, and connected
        //transporters will be connected automatically when the game map is built, as such it is redunant for the format.
        mapLayoutSave = Normalizer.normalize(mapLayoutSave, Normalizer.Form.NFD);
        mapLayoutSave = mapLayoutSave.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String directionLayoutSave = "";
        int width = pushRock.getWidth();

        String typeSequence = mapLayoutSave.toLowerCase().replaceAll("\\n|\\r\\n|@","").stripTrailing();
        for (int i = 0; i < typeSequence.length(); i++) {
            String type = typeSequence.toLowerCase().charAt(i) + "";
            //these types are directed types, and as such should have their directions saved.
            if ("pqrovu".contains(type)) {
                int x = Math.floorMod(i, width);
                int y = - i / width;
                BlockAbstract directedBlock = pushRock.getTopBlockCopy(x, y);
                String direction = ((DirectedBlock) directedBlock).getDirection();
                directionLayoutSave += direction.charAt(0);
            }
        }
        List<String> layoutList = new ArrayList<String>();
        layoutList.add(mapLayoutSave);
        layoutList.add(directionLayoutSave);
        return layoutList;
    }
}
