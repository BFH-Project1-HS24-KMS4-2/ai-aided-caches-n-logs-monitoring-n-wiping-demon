package ch.bfh.tracesentry.daemon.domain.model;

import ch.bfh.tracesentry.lib.model.SearchMode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static ch.bfh.tracesentry.daemon.common.Constants.CACHE_SEARCH_STRING;
import static ch.bfh.tracesentry.daemon.common.Constants.LOG_SEARCH_STRING;

public class MerkleTree {
    private Node root;
    private final MonitoredPath monitoredPath;
    private final List<Node> linearizedNodes = new ArrayList<>();

    private MerkleTree(MonitoredPath path, Snapshot snapshot) {
        try {
            this.monitoredPath = path;
            this.buildMerkleTree(path, snapshot);
        } catch (IOException | NoSuchAlgorithmException e) {
            // we're just throwing an unchecked exception here,
            // corresponding snapshot save will roll back at transaction boundary
            throw new RuntimeException(e);
        }
    }

    private void buildMerkleTree(MonitoredPath monitoredPath, Snapshot snapshot) throws IOException, NoSuchAlgorithmException {
        File rootDir = new File(monitoredPath.getPath());
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            throw new IllegalArgumentException("The provided path is not a valid directory.");
        }

        Node root = createNodeForDirectory(rootDir, snapshot);
        this.root = root;
        buildTreeRecursively(rootDir, root, snapshot);
    }

    private Node createNodeForDirectory(File dir, Snapshot snapshot) throws NoSuchAlgorithmException {
        Node node = new Node();
        node.setPath(dir.getAbsolutePath());
        node.setSnapshot(snapshot);
        node.setHash(hash(getDirectoryHashString(dir)));
        this.linearizedNodes.add(node);

        return node;
    }

    private void buildTreeRecursively(File dir, Node parent, Snapshot snapshot) throws IOException, NoSuchAlgorithmException {
        List<Node> children = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            Node childNode;
            if (file.isDirectory()) {
                childNode = createNodeForDirectory(file, snapshot);
                buildTreeRecursively(file, childNode, snapshot);
            } else {
                var searchMode = monitoredPath.getMode();
                if (searchMode == SearchMode.LOG && containsString(file.toPath(), LOG_SEARCH_STRING)) {
                    childNode = createNodeForFile(file, snapshot);
                }else if (searchMode == SearchMode.CACHE && containsString(file.toPath(), CACHE_SEARCH_STRING)) {
                    childNode = createNodeForFile(file, snapshot);
                } else if (searchMode == SearchMode.FULL && (containsString(file.toPath(), LOG_SEARCH_STRING) || containsString(file.toPath(), CACHE_SEARCH_STRING))) {
                    childNode = createNodeForFile(file, snapshot);
                } else if (searchMode == SearchMode.PATTERN) {
                    try {
                        if (monitoredPath.getPattern() != null) {
                            var pattern = Pattern.compile(monitoredPath.getPattern());
                            if (pattern.matcher(file.getName()).find()) {
                                childNode = createNodeForFile(file, snapshot);
                            } else {
                                continue;
                            }
                        }
                    } catch (Exception ignored) {
                        continue;
                    }
                } else {
                    continue;
                }
                continue;
            }
            /*
            * TODO: all childNode assignments are redundant, refactor
            *  refactor code in SearchController too
            * */
            childNode.setParent(parent);
            children.add(childNode);
        }

        String combinedHash = combineHashes(children);
        parent.setHash(hash(combinedHash));
        parent.setChildren(children);
    }

    private boolean containsString(Path path, String value) {
        return path.getFileName().toString().toLowerCase().contains(value);
    }

    private Node createNodeForFile(File file, Snapshot snapshot) throws NoSuchAlgorithmException, IOException {
        Node fileNode = new Node();
        fileNode.setPath(file.getAbsolutePath());
        fileNode.setSnapshot(snapshot);
        fileNode.setHash(hash(new String(Files.readAllBytes(file.toPath()))));
        this.linearizedNodes.add(fileNode);

        return fileNode;
    }

    private String combineHashes(List<Node> children) {
        StringBuilder combinedHash = new StringBuilder();
        for (Node child : children) {
            combinedHash.append(child.getHash());
        }
        return combinedHash.toString();
    }

    private String getDirectoryHashString(File dir) {
        StringBuilder sb = new StringBuilder();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            sb.append(file.getName()).append(file.lastModified()).append(file.length());
        }
        return sb.toString();
    }

    public List<Node> getLinearizedNodes() {
        return linearizedNodes;
    }

    private static String hash(String val) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(val.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static MerkleTree create(MonitoredPath monitoredPath, Snapshot snapshot) {
        return new MerkleTree(monitoredPath, snapshot);
    }

    public Node getRoot() {
        return root;
    }
}
