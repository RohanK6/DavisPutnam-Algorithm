package DavisPutnam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static TreeSet<String> allAtoms = new TreeSet<String>(new NumericStringComparator());
    private static TreeSet<String> atoms = new TreeSet<String>(new NumericStringComparator());
    private static Stack<State> states = new Stack<State>();

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        /* Initialization */
        // URL url = Main.class.getResource("input.txt");
        System.out.println("================ DavisPutnam ================");
        Path path = Paths.get(args[0]);
        File file = path.toFile();
        Scanner in = new Scanner(file);
        ArrayList<ArrayList<String>> clauses = new ArrayList<ArrayList<String>>();
        LinkedHashMap<String, Boolean> bindings = new LinkedHashMap<String, Boolean>();
        int stateNumber = 0;

        /* Scan the file for the Davis Putnam inputs */
        while (in.hasNextLine()) {
            /* Check if it's 0, so we stop parsing */
            String line = in.nextLine();
            if (line.trim().equals("0")) {
                break;
            }
            /* Split each line into their own clause */
            ArrayList<String> clause = new ArrayList<String>();
            String[] tokens = line.split(" ");
            for (String t : tokens) {
                clause.add(t);
                if (t.charAt(0) == '-') {
                    String atom = t.substring(1);
                    allAtoms.add(atom);
                    atoms.add(atom);
                } else {
                    allAtoms.add(t);
                    atoms.add(t);
                }
            }
            clauses.add(clause);
        }
        
        System.out.println(atoms);

        /* We have the initial stateNumber (0), clauses, and bindings. We want to keep track of States
            in case we have to "pop" and revert back.
         */
        State state = new State(stateNumber, clauses, bindings);
        states.push(state);

        /* Now, we want to run davis putnam */
        LinkedHashMap<String, Boolean> dpResults = davisPutnamAlgorithm();

        /* Print the results */
        System.out.println("------------------------------");

        if (!dpResults.isEmpty()) {
            // Check if atoms is not empty. If it is, insert them into the bindings as true
            for (String atom : allAtoms) {
                if (!dpResults.containsKey(atom)) {
                    System.out.println("Atom " + atom + " is not in the bindings. Adding it as true.");
                    dpResults.put(atom, true);
                }
            }
        }

        /* Make a sorted copy of results */
        TreeMap<String, Boolean> copiedResults = new TreeMap<String, Boolean>(dpResults);
        TreeMap<String, Boolean> sortedResults = new TreeMap<String, Boolean>(new NumericStringComparator());
        sortedResults.putAll(copiedResults);

        System.out.println("Back in Main");
        System.out.println("Results: " + sortedResults);

        // We now want to write the results to a file, with the format of "atom = T/F"
        // After the results, we want to write a newLine with just "0"
        // After that, we want to copy over the contents from the input file after the "0"

        File outputFile = new File("DavisPutnamOutput.txt");
        PrintWriter out = new PrintWriter(outputFile);
        if (sortedResults.size() > 0) {
            for (String atom : sortedResults.keySet()) {
                out.println(atom + " " + ((sortedResults.get(atom) == true) ? "T" : "F"));
            }
        }
        out.println("0");
        while (in.hasNextLine()) {
            out.println(in.nextLine());
        }
        out.close();

        // Call BackEnd.Main with the output file as input
        BackEnd.Main.main(new String[]{outputFile.getAbsolutePath()});
    }

    public static LinkedHashMap<String, Boolean> davisPutnamAlgorithm() {
        State baseState = states.peek();
        int tries = 0;
        while (true) {
            System.out.println("-----------------------------");
            System.out.println("On try: " + tries);
            /* Get the info from the current state */
            State currentState = states.peek();
            tries++;
            int stateNumber = currentState.getStateNumber();
            ArrayList<ArrayList<String>> clauses = currentState.getClauses();
            LinkedHashMap<String, Boolean> bindings = currentState.getBindings();
            LinkedHashMap<String, Boolean> failedBindings = currentState.getFailedBindings();
            System.out.println(currentState);
            System.out.println("Failed: " + failedBindings);
            if (tries == 1000) break;

            // For all atoms that are NOT in bindings, set atoms to a new TreeSet
            TreeSet<String> newAtoms = new TreeSet<String>(new NumericStringComparator());
            for (String atom : allAtoms) {
                if (!bindings.containsKey(atom)) {
                    newAtoms.add(atom);
                }
            }
            atoms = newAtoms;
            System.out.println(atoms);

            /* Attempt to run davis putnam on the current state */
            MixedObjectReturnType results = davisPutnam(clauses, bindings, failedBindings);

            System.out.println(results);
            
            boolean resultingClausesHaveContradictions = false;
            // Check if the resulting clauses have no contradictions
            // i.e. two clauses with just one atom that are opposites (e.g. A and -A))
            for (int i = 0; i < clauses.size(); i++) {
                ArrayList<String> clause = clauses.get(i);
                if (clause.size() == 1) {
                    String atom = clause.get(0);
                    String oppositeAtom = "";
                    if (atom.charAt(0) == '-') {
                        oppositeAtom = atom.substring(1);
                    } else {
                        oppositeAtom = "-" + atom;
                    }

                    for (int j = 0; j < clauses.size(); j++) {
                        if (i == j) continue;
                        ArrayList<String> otherClause = clauses.get(j);
                        if (otherClause.size() == 1) {
                            String otherAtom = otherClause.get(0);
                            if (otherAtom.equals(oppositeAtom)) {
                                resultingClausesHaveContradictions = true;
                                break;
                            }
                        }
                    }
                    if (resultingClausesHaveContradictions) break;
                }
            }

            boolean containsEmptyClause = false;
            // Check if the resulting clauses have an empty clause
            for (ArrayList<String> clause : results.clauses) {
                if (clause.size() == 0) {
                    containsEmptyClause = true;
                    break;
                }
            }

            // System.out.println("Remainaing atoms: ");
            // for (String atom : atoms) {
            //     System.out.println(atom);
            // }


            // We were met with no issues running the algorithm on the current state
            if (results.isPossible && !resultingClausesHaveContradictions && !containsEmptyClause) {
                System.out.println("Success!");
                // We want to consider it a "success" and move on
                int nextStateNumber = stateNumber + 1;
                State successState = new State(nextStateNumber, results.clauses, results.bindings);
                states.push(successState);

                /* Base conditions */
                if (results.clauses.isEmpty()) return results.bindings;
                
            } else {
                System.out.println("Failure!");
                // Clear the failed bindings for the current state since we're reverting and changing previous bindings
                currentState.setFailedBindings(new LinkedHashMap<String, Boolean>());

                // Clearly, the current state is unfeasible, so we have to "revert"
                states.pop();
                // If we have no base states, go back to the initial
                if (states.size() == 0) states.push(baseState);

                // We want to record the failed binding for the state we're reverting to
                State currentStateToRevertTo = states.peek();
                LinkedHashMap<String, Boolean> failedBindingsForState = currentStateToRevertTo.getFailedBindings();
                // We want to add the failed binding to the state (the most recent in bindings):
                String failedBinding = "";
                Boolean failedBindingValue = false;
                for (Map.Entry<String, Boolean> entry : bindings.entrySet()) {
                    failedBinding = entry.getKey();
                    failedBindingValue = entry.getValue();
                }
                // atoms.add(failedBinding);
                System.out.println("Adding " + failedBinding + " back to atoms");

                // check that the failed binding is not already in the failed bindings for the state
                while (failedBindingsForState.containsKey(failedBinding)) {
                    // This means we've exhausted both true and false in our given state, so we need to revert once more.
                    // If we're at the base state (i.e. we've exhausted all possibilities), then we're done.
                     if (states.size() == 1) {
                          break;
                     }

                     // We already failed TRUE for this binding, and now we've failed FALSE, so we need to revert once more
                    State newCurrentState = states.peek();                    // We want to get the bindings for this state and add the last binding to the failed bindings
                    LinkedHashMap<String, Boolean> bindingsForNewCurrentState = newCurrentState.getBindings();
                    for (Map.Entry<String, Boolean> entry : bindingsForNewCurrentState.entrySet()) {
                        failedBinding = entry.getKey();
                        failedBindingValue = entry.getValue();
                    }
                    System.out.println("Adding " + failedBinding + " back to atoms");
                    // atoms.add(failedBinding);
                    states.pop();

                    // We want to add the failed binding to the failed bindings for the state
                    State currentStateToRevertTo2 = states.peek();
                    LinkedHashMap<String, Boolean> failedBindingsForState2 = currentStateToRevertTo2.getFailedBindings();
                    failedBindingsForState2.put(failedBinding, failedBindingValue);

                    currentStateToRevertTo2.setFailedBindings(failedBindingsForState2);

                    for (Map.Entry<String, Boolean> entry : bindings.entrySet()) {
                        failedBinding = entry.getKey();
                        failedBindingValue = entry.getValue();
                    }
                }

                // We want to add the failed binding to the failed bindings for the state
                failedBindingsForState.put(failedBinding, failedBindingValue);

                currentStateToRevertTo.setFailedBindings(failedBindingsForState);
            }
        }
        System.out.println("No solution found.");
        return new LinkedHashMap<String, Boolean>();
    }

    public static MixedObjectReturnType davisPutnam(ArrayList<ArrayList<String>> clauses, LinkedHashMap<String, Boolean> bindings, LinkedHashMap<String, Boolean> failedBindings) {
        MixedObjectReturnType easyCaseResults = tryAndHandleEasyCase(clauses, bindings, failedBindings);
        if (easyCaseResults.isPossible) return easyCaseResults;
        // We have no easy case, so we perform the actual algorithm

        // We have no more atoms to check
        if (atoms.size() == 0) return new MixedObjectReturnType(true, clauses, bindings);

        // Try the "earliest" (first alphabetical atom available) to be TRUE
        String atomToTry = atoms.first();
        boolean atomValueToTry = true;
        if (failedBindings.containsKey(atomToTry)) {
            // We've already tried this atom, so we want to try the opposite value
            atomValueToTry = !failedBindings.get(atomToTry);
        }

        /* Make copies */
        ArrayList<ArrayList<String>> copyClauses = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> clause : clauses) {
            ArrayList<String> copyClause = new ArrayList<String>();
            copyClause.addAll(clause);
            copyClauses.add(copyClause);
        }
        LinkedHashMap<String, Boolean> copyBindings = new LinkedHashMap<String, Boolean>();
        copyBindings.putAll(bindings);

        copyBindings.put(atomToTry, atomValueToTry);

        copyClauses = updateClausesFromBindings(copyClauses, copyBindings, atomToTry);

        return new MixedObjectReturnType(true, copyClauses, copyBindings);
    }

    public static MixedObjectReturnType tryAndHandleEasyCase(ArrayList<ArrayList<String>> clauses, LinkedHashMap<String, Boolean> bindings, LinkedHashMap<String, Boolean> failedBindings) {
        /* Make copies */
        ArrayList<ArrayList<String>> copyClauses = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> clause : clauses) {
            ArrayList<String> copyClause = new ArrayList<String>();
            copyClause.addAll(clause);
            copyClauses.add(copyClause);
        }
        LinkedHashMap<String, Boolean> copyBindings = new LinkedHashMap<String, Boolean>();
        copyBindings.putAll(bindings);

        boolean foundEasyCase = false;
        String easyAtomLetter = "";
        /* Check for an easy case */
        for (ArrayList<String> clause : clauses) {
            if (clause.size() == 1) {
                // Easy clause found!
                String easyAtom = clause.get(0);
                boolean isNegated = (easyAtom.charAt(0) == '-');
                // Get just the letter. I.e. "C" -> "C" and "-C" -> "C"
                String justTheLetter = easyAtom;
                if (isNegated) {
                    justTheLetter = easyAtom.substring(1);
                }

                // Remove it from the list of atoms
                atoms.remove(justTheLetter);
                easyAtomLetter = justTheLetter;

                if (failedBindings.containsKey(justTheLetter)) {
                    copyBindings.put(justTheLetter, !failedBindings.get(justTheLetter));
                } else {

                    if (isNegated) {
                        // We want to bind it to false
                        copyBindings.put(justTheLetter, false);
                    } else {
                        // We want to bind it to true
                        copyBindings.put(justTheLetter, true);
                    }
                }

                foundEasyCase = true;
                break;
            }
        }

        if (!foundEasyCase) {
            // We didn't find an easy case, so we can't do anything
            return new MixedObjectReturnType(false, clauses, bindings);
        }

        // We found an easy case, so we want to remove all clauses that contain the easy atom, based on bindings
        // I.e. if we have a binding of "C" -> true, we want to remove the atom "C" from all clauses
        // and if we have a binding of "C" -> false, we want to remove the atom "-C" from all clauses
        copyClauses = updateClausesFromBindings(copyClauses, copyBindings, easyAtomLetter);

        return new MixedObjectReturnType(true, copyClauses, copyBindings);
    }

    public static ArrayList<ArrayList<String>> updateClausesFromBindings(ArrayList<ArrayList<String>> clauses, LinkedHashMap<String, Boolean> bindings, String atomLetter) {
        ArrayList<ArrayList<String>> newClauses = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> clause : clauses) {
            ArrayList<String> newClause = new ArrayList<String>();
            for (String atom : clause) {
                // If -easyAtomLetter and bindings.get(easyAtomLetter) == true, then we want to remove just the letter
                // If easyAtomLetter and bindings.get(easyAtomLetter) == false, then we want to remove just the letter
                // If -easyAtomLetter and bindings.get(easyAtomLetter) == false, then we want to remove the whole atom
                // If easyAtomLetter and bindings.get(easyAtomLetter) == true, then we want to remove the whole atom
                boolean isNegated = (atom.charAt(0) == '-');
                String justTheLetter = atom;
                if (isNegated) {
                    justTheLetter = atom.substring(1);
                }
                if (justTheLetter.equals(atomLetter)) {
                    if (isNegated && bindings.get(atomLetter)) {
                        // We want to remove just the atom so we do nothing

                    } else if (!isNegated && !bindings.get(atomLetter)) {
                        // We want to remove just the atom so we do nothing

                    } else {
                        // We want to remove the whole clause
                        newClause = null;
                        break;
                    }
                } else {
                    newClause.add(atom);
                }
            }
            if (newClause != null) {
                newClauses.add(newClause);
            }
        }

        // Now, for each atom in atoms, if it is NOT present in any of the clauses, we can remove it from atoms
        ArrayList<String> atomsToRemove = new ArrayList<String>();
        atomsToRemove.add(atomLetter);
        for (String atom : atoms) {
            boolean found = false;
            for (ArrayList<String> clause : newClauses) {
                for (String clauseAtom : clause) {
                    boolean isNegated = (clauseAtom.charAt(0) == '-');
                    String justTheLetter = clauseAtom;
                    if (isNegated) {
                        justTheLetter = clauseAtom.substring(1);
                    }
                    if (justTheLetter.equals(atom)) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) {
                atomsToRemove.add(atom);
            }
        }
        for (String atomToRemove : atomsToRemove) {
            atoms.remove(atomToRemove);
        }
        
        return newClauses;
    }


    private static class MixedObjectReturnType {
        boolean isPossible;
        public ArrayList<ArrayList<String>> clauses;
        public LinkedHashMap<String, Boolean> bindings;
        public MixedObjectReturnType(boolean isPossible, ArrayList<ArrayList<String>> clauses, LinkedHashMap<String, Boolean> bindings) {
            this.isPossible = isPossible;
            this.clauses = clauses;
            this.bindings = bindings;
        }

        @Override
        public String toString() {
            return "MixedObjectReturnType [isPossible=" + isPossible + ", clauses=" + clauses + ", bindings=" + bindings + "]";
        }
    }

    public static class NumericStringComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            try {
                int i1 = Integer.parseInt(s1);
                int i2 = Integer.parseInt(s2);
                return Integer.compare(i1, i2);
            } catch (NumberFormatException e) {
                // If either s1 or s2 cannot be parsed as an integer,
                // fall back to string comparison.
                return s1.compareTo(s2);
            }
        }
    }
}
