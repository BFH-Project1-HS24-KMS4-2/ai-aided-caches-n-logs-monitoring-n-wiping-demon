package ch.bfh.tracesentry.daemon.domain.model;

import ch.bfh.tracesentry.daemon.search.SearchStrategyFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MerkleTree {
    private Node root;
    private final MonitoredPath monitoredPath;
    private final List<Node> linearizedNodes = new ArrayList<>();

    public MerkleTree(MonitoredPath path, Snapshot snapshot) {
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
        File[] rootFiles = rootDir.listFiles();

        if (!rootDir.exists() || !rootDir.isDirectory() || rootFiles == null) {
            throw new IllegalArgumentException("The provided path is not a valid readable directory.");
        }

        Node root = createNodeForDirectory(rootDir, snapshot, rootFiles);
        this.root = root;
        buildTreeRecursively(root, snapshot, rootFiles);
    }

    private Node createNodeForDirectory(File dir, Snapshot snapshot, File[] files) throws NoSuchAlgorithmException {
        Node node = new Node();
        node.setPath(dir.getAbsolutePath());
        node.setSnapshot(snapshot);
        node.setHash(hash(getDirectoryHashString(files)));
        this.linearizedNodes.add(node);

        return node;
    }

    private void buildTreeRecursively(Node parent, Snapshot snapshot, File[] files) throws NoSuchAlgorithmException, NullPointerException {
        List<Node> children = new ArrayList<>();
        for (File file : files) {
            Node childNode;
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (monitoredPath.isNoSubdirs() || !file.canRead() || childFiles == null) {
                    continue;
                }
                childNode = createNodeForDirectory(file, snapshot, childFiles);
                buildTreeRecursively(childNode, snapshot, childFiles);
            } else {
                if (SearchStrategyFactory.create(monitoredPath.getMode(), monitoredPath.compilePattern()).matches(file.toPath())) {
                    try {
                        childNode = createNodeForFile(file, snapshot);
                    } catch (IOException ignored) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            childNode.setParent(parent);
            children.add(childNode);
        }

        String combinedHash = combineHashes(children);
        parent.setHash(hash(combinedHash));
        parent.setChildren(children);
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

    private String getDirectoryHashString(File[] contents) {
        StringBuilder sb = new StringBuilder();
        for (File file : contents) {
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

    public Node getRoot() {
        return root;
    }
}
