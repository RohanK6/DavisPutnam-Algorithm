package BackEnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        /* Initialization */
        URL url = BackEnd.Main.class.getResource("input.txt");
        Path path = Paths.get(url.toURI());
        File file = path.toFile();
        File outputFile = new File("output.txt");
        PrintWriter out = new PrintWriter(outputFile);

        Scanner in = new Scanner(file);

        while (in.hasNextLine()) {
            String line = in.nextLine();
            // Handle the easy case, where the first line is a 0 (i.e. no solution)
            if (line.equals("0")) {
                System.out.println("NO SOLUTION");
                out.println("NO SOLUTION");
                out.close();
                return;
            }   
        }

    }

}
