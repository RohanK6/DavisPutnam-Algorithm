package BackEnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        /* Initialization */
        // URL url = BackEnd.Main.class.getResource("input.txt");
        System.out.println("================ BackEnd ================");
        Path path = Paths.get(args[0]);
        File file = path.toFile();
        File outputFile = new File("BackEndOutput.txt");
        PrintWriter out = new PrintWriter(outputFile);
        boolean checkedFirstLineNonZero = false;

        Scanner in = new Scanner(file);

        LinkedHashSet<String> trueAtomsFromInput = new LinkedHashSet<>();

        while (in.hasNextLine()) {
            String line = in.nextLine();
            // Handle the easy case, where the first line is a 0 (i.e. no solution)
            if (line.equals("0")) {
                if (!checkedFirstLineNonZero) {
                    System.out.println("NO SOLUTION");
                    out.println("NO SOLUTION");
                    out.close();
                    return;
                }
                break;
            }
            checkedFirstLineNonZero = true;

            String[] inputLine = line.split(" ");
            String atom = inputLine[0];
            boolean isTrue = inputLine[1].equals("T");
            if (isTrue) {
                trueAtomsFromInput.add(atom);
            }
        }

        // Now we get the corresponding Stringified atoms from the input
        HashMap<String, String> stringifiedAtomsFromInput = new HashMap<>();
        while (in.hasNextLine()) {
            String line = in.nextLine();
            String[] inputLine = line.split(" ");
            String atomNumber = inputLine[0];
            String stringifiedAtom = inputLine[1];
            stringifiedAtomsFromInput.put(atomNumber, stringifiedAtom);
        }

        // Now we have the true atoms from the input, and the corresponding stringified atoms from the input.
        // Only print the stringified atoms that are true and are "At" atoms, not "Has" atoms.
        for (String atomNumber : trueAtomsFromInput) {
            String stringifiedAtom = stringifiedAtomsFromInput.get(atomNumber);
            if (stringifiedAtom.startsWith("At")) {
                String node = stringifiedAtom.split(",")[0].substring(3);
                System.out.print(node + " ");
                out.print(node + " ");
            }
        }

        out.close();
    }

}
