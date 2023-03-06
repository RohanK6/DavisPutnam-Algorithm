package DavisPutnam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class State {
    int stateNumber;
    ArrayList<ArrayList<String>> clauses;
    LinkedHashMap<String, Boolean> bindings;
    LinkedHashMap<String, Boolean> failedBindings;

    public State(int s, ArrayList<ArrayList<String>> c, LinkedHashMap<String, Boolean> b) {
        stateNumber = s;
        clauses = c;
        bindings = b;
        failedBindings = new LinkedHashMap<>();
    }

    public int getStateNumber() {
        return stateNumber;
    }

    public ArrayList<ArrayList<String>> getClauses() {
        return clauses;
    }

    public LinkedHashMap<String, Boolean> getBindings() {
        return bindings;
    }

    public void setFailedBindings(LinkedHashMap<String, Boolean> failedBindings) {
        this.failedBindings = failedBindings;
    }

    public LinkedHashMap<String, Boolean> getFailedBindings() {
        return failedBindings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Clauses for State ").append(stateNumber).append(" = {\n");
        for (int i = 0; i < clauses.size(); i++) {
            sb.append("C").append(i+1).append(": ");
            ArrayList<String> clause = clauses.get(i);
            for (int j = 0; j < clause.size(); j++) {
                sb.append(clause.get(j));
                if (j < clause.size() - 1) {
                    sb.append(" <-> ");
                }
            }
            sb.append("\n");
        }
        sb.append("}\nBindings= ").append(bindings).append("\n");
        return sb.toString();
    }

}
