import java.util.Vector;

class PathInfo {
    public Vector<String> getPath(String sourceName, String destiName) {
        // Your logic to compute paths from sourceName to destiName
        // For demonstration purposes, let's return a dummy Vector of paths
        Vector<String> paths = new Vector<>();
        paths.add(sourceName + " -> " + destiName);
        paths.add(sourceName + " -> Intermediate Node -> " + destiName);
        return paths;
    }
}
