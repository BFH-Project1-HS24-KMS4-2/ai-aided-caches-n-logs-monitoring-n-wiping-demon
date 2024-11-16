package ch.bfh.tracesentry.daemon.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "snapshot_node")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    Node parent;

    @ManyToOne
    @JoinColumn(name = "snapshot_id")
    Snapshot snapshot;

    String path;

    String hash;

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
}
