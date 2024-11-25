CREATE TABLE IF NOT EXISTS monitored_path (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                path TEXT NOT NULL UNIQUE,
                                mode TINYINT NOT NULL,
                                pattern TEXT,
                                no_subdirs BOOLEAN NOT NULL,
                                created_at DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS snapshot (
                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                          monitored_path_id INTEGER NOT NULL,
                          timestamp TIMESTAMP NOT NULL,
                          FOREIGN KEY (monitored_path_id) REFERENCES monitored_path (id)
);

CREATE TABLE IF NOT EXISTS snapshot_node (
                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                               snapshot_id INTEGER NOT NULL,
                               parent_id INTEGER,
                               hash TEXT,
                               path TEXT NOT NULL,
                               has_changed BOOLEAN NOT NULL,
                               deleted_in_next_snapshot BOOLEAN NOT NULL,
                               FOREIGN KEY (snapshot_id) REFERENCES snapshot (id),
                               FOREIGN KEY (parent_id) REFERENCES snapshot_node(id)
);

CREATE INDEX IF NOT EXISTS idx_snapshot_id ON snapshot_node(snapshot_id);