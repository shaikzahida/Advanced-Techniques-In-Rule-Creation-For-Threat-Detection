import java.util.Vector;

class IsolatePathInfo {
    public Vector<String> getPaths(String sourceNode, String destiNode) {
        // Your logic to compute isolation paths from sourceNode to destiNode
        // For demonstration purposes, let's return a dummy Vector of isolation paths
        Vector<String> isoPaths = new Vector<>();
        isoPaths.add(sourceNode + " -> Intermediate Node -> " + destiNode);
        isoPaths.add(sourceNode + " -> Another Intermediate Node -> " + destiNode);
        return isoPaths;
    }
}
