CREATE EXTENSION IF NOT EXISTS postgis;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contact_preference') THEN
    CREATE TYPE contact_preference AS ENUM ('EMAIL', 'SMS', 'PHONE');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS asset_holders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    contact_preference contact_preference NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    drain_area GEOMETRY(POLYGON, 4326) NOT NULL,
    asset_holder_id INTEGER REFERENCES asset_holders(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO asset_holders (name, email, phone, contact_preference) VALUES
('Alice Smith', 'alice@example.com', '1234567890', 'EMAIL'),
('Bob Johnson', 'bob@example.com', '1234567891', 'SMS'),
('Carol Davis', 'carol@example.com', '1234567892', 'PHONE'),
('David Wilson', 'david@example.com', '1234567893', 'EMAIL'),
('Eva Brown', 'eva@example.com', '1234567894', 'SMS'),
('Frank Harris', 'frank@example.com', '1234567895', 'EMAIL'),
('Grace Lewis', 'grace@example.com', '1234567896', 'PHONE'),
('Henry Walker', 'henry@example.com', '1234567897', 'SMS'),
('Isla Hall', 'isla@example.com', '1234567898', 'EMAIL'),
('Jack Young', 'jack@example.com', '1234567899', 'PHONE');

INSERT INTO assets (name, drain_area, asset_holder_id) VALUES
('Big Ben',          ST_GeomFromText('POLYGON((-0.1265 51.5003, -0.1250 51.5003, -0.1250 51.5018, -0.1265 51.5018, -0.1265 51.5003))', 4326), 1),
('Tower of London',  ST_GeomFromText('POLYGON((-0.0775 51.5077, -0.0759 51.5077, -0.0759 51.5092, -0.0775 51.5092, -0.0775 51.5077))', 4326), 2),
('Buckingham Palace',ST_GeomFromText('POLYGON((-0.1445 51.5010, -0.1430 51.5010, -0.1430 51.5025, -0.1445 51.5025, -0.1445 51.5010))', 4326), 3),
('British Museum',   ST_GeomFromText('POLYGON((-0.1289 51.5190, -0.1274 51.5190, -0.1274 51.5205, -0.1289 51.5205, -0.1289 51.5190))', 4326), 4),
('St Paul''s Cathedral', ST_GeomFromText('POLYGON((-0.0998 51.5134, -0.0983 51.5134, -0.0983 51.5149, -0.0998 51.5149, -0.0998 51.5134))', 4326), 5),
('Hyde Park',        ST_GeomFromText('POLYGON((-0.1670 51.5069, -0.1655 51.5069, -0.1655 51.5084, -0.1670 51.5084, -0.1670 51.5069))', 4326), 6),
('London Eye',       ST_GeomFromText('POLYGON((-0.1209 51.5029, -0.1194 51.5029, -0.1194 51.5044, -0.1209 51.5044, -0.1209 51.5029))', 4326), 7),
('The Shard',        ST_GeomFromText('POLYGON((-0.0881 51.5041, -0.0866 51.5041, -0.0866 51.5056, -0.0881 51.5056, -0.0881 51.5041))', 4326), 8),
('Westminster Abbey',ST_GeomFromText('POLYGON((-0.1284 51.4989, -0.1269 51.4989, -0.1269 51.5004, -0.1284 51.5004, -0.1284 51.4989))', 4326), 9),
('Kensington Palace',ST_GeomFromText('POLYGON((-0.1891 51.5054, -0.1876 51.5054, -0.1876 51.5069, -0.1891 51.5069, -0.1891 51.5054))', 4326), 10);
