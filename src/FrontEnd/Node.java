package FrontEnd;

import java.util.ArrayList;

public class Node {
    public String name;
    public ArrayList<String> treasures;
    public ArrayList<String> neighbors;

    public Node(String name) {
        this.name = name;
        treasures = new ArrayList<>();
        neighbors = new ArrayList<>();
    }

    public void addTreasure(String treasure) {
        treasures.add(treasure);
    }

    public void addNeighbor(String neighbor) {
        neighbors.add(neighbor);
    }

    @Override
    public String toString() {
        return "Node{" +
                "name=" + name +
                ", treasures=" + treasures +
                ", neighbors=" + neighbors +
                '}';
    }
}
