CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    uuid TEXT NOT NULL,
    last_name TEXT NOT NULL,
    last_ip TEXT NOT NULL,
    locale VARCHAR(5) NOT NULL, -- en_US 5 symb.
    color VARCHAR(11) NOT NULL, -- #23456789 + [] 11 symb
    UNIQUE (uuid)
);

CREATE TABLE usid_list (
    id SERIAL PRIMARY KEY,
    player_id INTEGER REFERENCES players(id),
    usid VARCHAR(12),
    server VARCHAR(16),
    UNIQUE (usid, server)
);

CREATE TABLE admin_ranks (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100),
  permissions TEXT[]
);

CREATE TABLE admins(
    id SERIAL PRIMARY KEY,
    player_id INTEGER REFERENCES players(id),
    rank_id INTEGER REFERENCES admin_ranks(id),
    hidden BOOLEAN DEFAULT false,
    UNIQUE(player_id)
);

CREATE TABLE bans (
    id SERIAL PRIMARY KEY,
    active BOOLEAN default true,
    player_id INTEGER REFERENCES players(id) NOT NULL,
    reason VARCHAR(128) DEFAULT 'No reason provided',
    admin_id INTEGER REFERENCES players(id) NOT NULL,
    ban_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    unban_time TIMESTAMP default null
);