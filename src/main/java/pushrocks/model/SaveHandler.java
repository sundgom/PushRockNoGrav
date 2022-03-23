package pushrocks.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class SaveHandler implements SaveHandlerInterface {

    @Override
    public void save(String fileName, PushRocks pushRocks) throws FileNotFoundException {
        
        // try (PrintWriter printWriter = new PrintWriter(new File(getFilePath(fileName))) {
        try {
            PrintWriter printWriter = new PrintWriter(new File(getFilePath(fileName, true)));
            printWriter.println(pushRocks.toGameToSaveFormat());

            printWriter.flush(); //ensures that the printwriter is done writing to the file
            printWriter.close(); //closes the file
            

        }
        catch (Exception e) {
            
        }
        
    }

    @Override
    public void load(String fileName) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] lineInfo = line.split(",");
        }
        
    }
    
    private static String getFilePath(String fileName, boolean isSave) {
        if (isSave) {
            return SaveHandler.class.getResource("saves/").getFile() + fileName + ".txt";
        }
        else {
            return SaveHandler.class.getResource("levels/").getFile() + fileName + ".txt";
        }
    }
}
