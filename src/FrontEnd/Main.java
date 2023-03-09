package FrontEnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static ArrayList<Node> nodes = new ArrayList<>();
    public static ArrayList<String> treasures = new ArrayList<>();
    public static int ALLOWED_STEPS = -1;
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        /* Initialization */
        URL url = FrontEnd.Main.class.getResource("input.txt");
        Path path = Paths.get(url.toURI());
        File file = path.toFile();
        Scanner in = new Scanner(file);
        String line;

        // First line:
        line = in.nextLine();
        String[] inputNodes = line.split(" ");
        for (String node : inputNodes) {
            if (node.isEmpty()) continue;
            Node inputNode = new Node(node);
            nodes.add(inputNode);
        }

        // Second line:
        line = in.nextLine();
        String[] inputTreasures = line.split(" ");
        for (String treasure : inputTreasures) {
            treasures.add(treasure);
        }

        // Third line:
        line = in.nextLine();
        ALLOWED_STEPS = Integer.parseInt(line);

        // Remaining lines:
        while (in.hasNextLine()) {
            line = in.nextLine();

            // H TREASURES GOLD WAND NEXT G
            String[] inputLine = line.split(" ");

            String nodeName = inputLine[0];
            // Find the node within the nodes list
            Node currentNode = null;
            for (Node node : nodes) {
                if (node.name.equals(nodeName)) {
                    currentNode = node;
                    break;
                }
            }

            int treasuresStartingIndex = 2;
            while (!inputLine[treasuresStartingIndex].equals("NEXT")) {
                String treasure = inputLine[treasuresStartingIndex];
                currentNode.addTreasure(treasure);
                treasuresStartingIndex++;
            }

            int neighborsStartingIndex = treasuresStartingIndex + 1;
            while (neighborsStartingIndex < inputLine.length) {
                String neighborName = inputLine[neighborsStartingIndex];
                currentNode.addNeighbor(neighborName);
                neighborsStartingIndex++;
            }
        }

        // We have two atoms:
        // At(N,I) means that the player is on node N at time I
        // Has(T,I) means that the player has treasure T at time I

        LinkedHashMap<String, String> atoms = new LinkedHashMap<>();
        int atomNumber = 1;
        for (int i = 0; i <= ALLOWED_STEPS; i++) {
            for (Node node : nodes) {
                atoms.put("At(" + node.name + "," + i + ")", atomNumber+"");
                atomNumber++;
            }
        }
        for (int i = 0; i <= ALLOWED_STEPS; i++) {
            for (String treasure : treasures) {
                atoms.put("Has(" + treasure + "," + i + ")", atomNumber+"");
                atomNumber++;
            }
        }

        ArrayList<String> clauses = new ArrayList<String>();
        HashMap<String, String> alreadyCompared = new HashMap<>();
         // Category 1
         for (int i = 0; i <= ALLOWED_STEPS; i++) {
             for (Node node : nodes) {
                 for (Node otherNode : nodes) {
                     // Make sure we don't compare the same node twice/ I.e. if we've already compared A to B, don't compare B to A
                     if (node != otherNode && !alreadyCompared.containsKey(otherNode.name + node.name)) {
                         clauses.add("~At(" + node.name + "," + i + ") V ~At(" + otherNode.name + "," + i + ")");
                     }
                     alreadyCompared.put(node.name + otherNode.name, "true");
                 }
             }
             alreadyCompared.clear();
         }

         // Category 2
         for (int i = 0; i < ALLOWED_STEPS; i++) {
             for (Node node : nodes) {
                 String clause = "~At(" + node.name + "," + i + ")";
                 for (String otherNode : node.neighbors) {
                     clause += " V At(" + otherNode + "," + (i+1) + ")";
                 }
                 clauses.add(clause);
             }
         }

         // Category 3
        for (int i = 0; i <= ALLOWED_STEPS; i++) {
            for (Node node : nodes) {
                for (String treasure : node.treasures) {
                    clauses.add("~At(" + node.name + "," + i + ") V Has(" + treasure + "," + i + ")");
                }
            }
        }

        // Category 4
        for (int i = 0; i < ALLOWED_STEPS; i++) {
            for (String treasure : treasures) {
                clauses.add("~Has(" + treasure + "," + i + ") V Has(" + treasure + "," + (i+1) + ")");
            }
        }

        // Category 5
        for (int i = 0; i < ALLOWED_STEPS; i++) {
            for (String treasure : treasures) {
                String clause = "Has(" + treasure + "," + i + ") V ~Has(" + treasure + "," + (i+1) + ")";
                for (Node node : nodes) {
                    if (node.treasures.contains(treasure)) {
                        clause += " V At(" + node.name + "," + (i+1) + ")";
                    }
                }
                clauses.add(clause);
            }
        }

         // Category 6
         clauses.add("At(START,0)");

         // Category 7
         for (String treasure : treasures) {
             clauses.add("~Has(" + treasure + ",0)");
         }

         // Category 8
         for (String treasure : treasures) {
             clauses.add("Has(" + treasure + "," + ALLOWED_STEPS + ")");
         }

        ArrayList<ArrayList<String>> translatedClausesToNumbers = new ArrayList<>();
        for (String clause : clauses) {
            ArrayList<String> arrayListForClause = new ArrayList<>();
            String[] splitAtoms = clause.split("V");
            String translatedClause = "";
            for (String atom : splitAtoms) {
                String formattedAtom = atom.trim();
                boolean isNegated = formattedAtom.charAt(0) == '~';
                String atomLookup;
                if (isNegated) {
                    translatedClause = "-" + atoms.get(formattedAtom.substring(1));
                } else {
                    translatedClause = atoms.get(formattedAtom);
                }
                arrayListForClause.add(translatedClause);
            }
            translatedClausesToNumbers.add(arrayListForClause);
        }

        File outputFile = new File("FrontEndOutput.txt");
        PrintWriter out = new PrintWriter(outputFile);

        // Print translated clauses, each ArrayList on its own line
        for (ArrayList<String> clause : translatedClausesToNumbers) {
            for (String atom : clause) {
                System.out.print(atom + " ");
                out.print(atom + " ");
            }
            System.out.println();
            out.println();
        }

        // Print "0"
        System.out.println(0);
        out.println(0);

        // Print atoms
        for (String atom : atoms.keySet()) {
            System.out.println(atoms.get(atom) + " " + atom);
            out.println(atoms.get(atom) + " " + atom);
        }

        out.close();

        // Call DavisPutnam.Main with the output file as input
        DavisPutnam.Main.main(new String[]{outputFile.getAbsolutePath()});
    }
}
