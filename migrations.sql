CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    uuid TEXT NOT NULL UNIQUE ,
    last_name TEXT NOT NULL,
    last_ip TEXT NOT NULL,
    locale VARCHAR(30) NOT NULL, -- en_US 5 symb.
    color VARCHAR(11) NOT NULL, -- #23456789 + [] 11 symb
    last_seen TIMESTAMP DEFAULT NOW(),
    discord_id BIGINT default NULL UNIQUE,
    prefs JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE servers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(16) UNIQUE NOT NULL
);

CREATE TABLE usid_list (
    id SERIAL PRIMARY KEY,
    player_id INTEGER REFERENCES players(id),
    usid VARCHAR(12),
    server INTEGER REFERENCES servers(id),
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

CREATE TABLE logs (
    id SERIAL PRIMARY KEY,
    type VARCHAR(16) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    server_id INTEGER REFERENCES servers(id) NOT NULL,
    player_id INTEGER REFERENCES players(id)
);

CREATE TABLE statistics (
    id SERIAL PRIMARY KEY,
    player_id INTEGER references players(id) UNIQUE,
    playtime BIGINT DEFAULT 0 NOT NULL,
    blocks_build INTEGER DEFAULT 0 NOT NULL,
    blocks_broken INTEGER DEFAULT 0 NOT NULL,
    waves_survived INTEGER DEFAULT 0 NOT NULL,
    balance INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE graylist (
    id SERIAL PRIMARY KEY,
    isp VARCHAR(100)
);

CREATE TABLE connections (
    id SERIAL PRIMARY KEY,
    player_name TEXT NOT NULL,
    address VARCHAR(20) NOT NULL,
    address_udp VARCHAR(20) NOT NULL,
    server_id INTEGER REFERENCES servers(id) NOT NULL,
    player_id INTEGER REFERENCES players(id) NOT NULL
);