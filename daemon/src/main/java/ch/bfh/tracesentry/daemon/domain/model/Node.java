package ch.bfh.tracesentry.daemon.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "snapshot_node")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Node parent;

    @ManyToOne
    @JoinColumn(name = "snapshot_id")
    private Snapshot snapshot;

    private String path;

    private String hash;

    private boolean hasChanged;

    private boolean deletedInNextSnapshot;

    @Transient
    private List<Node> children = new ArrayList<>();

    public Node() {

    }

    public boolean isParent() {
        return parent == null;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }


    public boolean isHasChanged() {
        return hasChanged;
    }

    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public boolean isDeletedInNextSnapshot() {
        return deletedInNextSnapshot;
    }

    public void setDeletedInNextSnapshot(boolean deletedInNextSnapshot) {
        this.deletedInNextSnapshot = deletedInNextSnapshot;
    }
}
