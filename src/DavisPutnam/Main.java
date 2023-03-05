package DavisPutnam;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static ArrayList<String> atoms = new ArrayList<String>();
    private static Stack<State> states = new Stack<State>();

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        /* Initialization */
        URL url = Main.class.getResource("input.txt");
        Path path = Paths.get(url.toURI());
        File file = path.toFile();
        Scanner in = new Scanner(file);
        ArrayList<ArrayList<String>> clauses = new ArrayList<ArrayList<String>>();
        LinkedHashMap<String, Boolean> bindings = new LinkedHashMap<String, Boolean>();
        int stateNumber = 0;

        /* Scan the file for the Davis Putnam inputs */
        while (in.hasNextLine()) {
            /* Check if it's 0, so we stop parsing */
            String line = in.nextLine();
            if (line.equals("0")) {
                break;
            }
            /* Split each line into their own clause */
            ArrayList<String> clause = new ArrayList<String>();
            String[] tokens = line.split(" ");
            for (String t : tokens) {
                clause.add(t);
                if (t.length() > 1) {
                    String atom = t.substring(1);
                    atoms.add(atom);
                } else {
                    atoms.add(t);
                }
            }
            clauses.add(clause);
        }

        /* We have the initial stateNumber (0), clauses, and bindings. We want to keep track of States
            in case we have to "pop" and revert back.
         */
        State state = new State(stateNumber, clauses, bindings);
        states.push(state);

        /* Now, we want to run davis putnam */
        davisPutnamAlgorithm();
    }

    public static void davisPutnamAlgorithm() {
        State baseState = states.peek();
        int tries = 0;
        while (true) {
            /* Get the info from the current state */
            State currentState = states.peek();
            tries++;
            int stateNumber = currentState.getStateNumber();
            ArrayList<ArrayList<String>> clauses = currentState.getClauses();
            HashMap<String, Boolean> bindings = currentState.getBindings();
            System.out.println(currentState);
            System.out.println("Failed: ");
            System.out.println("-----------------------------");

            /* Base conditions */
            if (clauses.isEmpty()) break;

            if (tries == 10) break;

            /* Attempt to run davis putnam on the current state */
            MixedObjectReturnType results = davisPutnam(clauses, bindings);
            
            boolean resultingClausesHaveContradictions = false;
            // Check if the resulting clauses have no contradictions
            // i.e. two clauses with just one atom that are opposites (e.g. A and -A))
            for (int i = 0; i < clauses.size(); i++) {
                ArrayList<String> clause = clauses.get(i);
                if (clause.size() == 1) {
                    String atom = clause.get(0);
                    String oppositeAtom = "";
                    if (atom.length() > 1) {
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
                if (resultingClausesHaveContradictions) break;
            }

            boolean containsEmptyClause = false;
            // Check if the resulting clauses have an empty clause
            for (ArrayList<String> clause : clauses) {
                if (clause.size() == 0) {
                    containsEmptyClause = true;
                    break;
                }
            }


            // We were met with no issues running the algorithm on the current state
            if (results.isPossible && !resultingClausesHaveContradictions && !containsEmptyClause) {
                // We want to consider it a "success" and move on
                int nextStateNumber = stateNumber + 1;
                State successState = new State(nextStateNumber, results.clauses, results.bindings);
                states.push(successState);
            } else {
                // Clearly, the current state is unfeasible, so we have to "revert"
                states.pop();
                // If we have no base states, go back to the initial
                if (states.size() == 0) states.push(baseState);
            }
        }
    }

    public static MixedObjectReturnType davisPutnam(ArrayList<ArrayList<String>> clauses, HashMap<String, Boolean> bindings) {
        MixedObjectReturnType easyCaseResults = tryAndHandleEasyCase(clauses, bindings);
        if (easyCaseResults.isPossible) return easyCaseResults;
        // We have no easy case, so we perform the actual algorithm

        // We have no more atoms to check
        if (atoms.size() == 0) return new MixedObjectReturnType(true, clauses, bindings);

        // Try the "earliest" (first alphabetical atom available) to be TRUE
        String atomToTry = atoms.get(0);
        boolean atomValueToTry = true;

        /* Make copies */
        ArrayList<ArrayList<String>> copyClauses = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> clause : clauses) {
            ArrayList<String> copyClause = new ArrayList<String>();
            copyClause.addAll(clause);
            copyClauses.add(copyClause);
        }
        HashMap<String, Boolean> copyBindings = new HashMap<String, Boolean>();
        copyBindings.putAll(bindings);

        copyBindings.put(atomToTry, atomValueToTry);

        copyClauses = updateClausesFromBindings(copyClauses, copyBindings, atomToTry);

        return new MixedObjectReturnType(true, copyClauses, copyBindings);
    }

    public static MixedObjectReturnType tryAndHandleEasyCase(ArrayList<ArrayList<String>> clauses, HashMap<String, Boolean> bindings) {
        /* Make copies */
        ArrayList<ArrayList<String>> copyClauses = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> clause : clauses) {
            ArrayList<String> copyClause = new ArrayList<String>();
            copyClause.addAll(clause);
            copyClauses.add(copyClause);
        }
        HashMap<String, Boolean> copyBindings = new HashMap<String, Boolean>();
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

                if (isNegated) {
                    // We want to bind it to false
                    copyBindings.put(justTheLetter, false);
                } else {
                    // We want to bind it to true
                    copyBindings.put(justTheLetter, true);
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

    public static ArrayList<ArrayList<String>> updateClausesFromBindings(ArrayList<ArrayList<String>> clauses, HashMap<String, Boolean> bindings, String atomLetter) {
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
            if (newClause != null) newClauses.add(newClause);
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
        public HashMap<String, Boolean> bindings;
        public MixedObjectReturnType(boolean isPossible, ArrayList<ArrayList<String>> clauses, HashMap<String, Boolean> bindings) {
            this.isPossible = isPossible;
            this.clauses = clauses;
            this.bindings = bindings;
        }
    }
}
