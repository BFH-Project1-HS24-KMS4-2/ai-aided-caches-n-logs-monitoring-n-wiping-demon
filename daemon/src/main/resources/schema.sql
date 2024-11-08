create table if not exists monitored_path (
    id integer primary key autoincrement,
    path text not null unique,
    created_at date not null
);