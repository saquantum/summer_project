CREATE EXTENSION IF NOT EXISTS postgis;

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS asset_holders CASCADE;
DROP TABLE IF EXISTS assets CASCADE;
DROP TABLE IF EXISTS weather_warnings CASCADE;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contact_preference') THEN
        CREATE TYPE contact_preference AS ENUM ('EMAIL', 'SMS', 'PHONE', 'DISCORD', 'WHATSAPP', 'TELEGRAM');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS asset_holders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    contact_preference contact_preference NOT NULL,
    last_modified TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    asset_holder_id INTEGER REFERENCES asset_holders(id) ON UPDATE CASCADE ON DELETE CASCADE,
    is_admin BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region VARCHAR(50) NOT NULL,
    drain_area GEOMETRY(POLYGON, 4326) NOT NULL,
    asset_holder_id INTEGER REFERENCES asset_holders(id) ON UPDATE CASCADE ON DELETE CASCADE,
    email VARCHAR(100),
    phone VARCHAR(20),
    contact_preference contact_preference,
    last_modified TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO asset_holders (name, email, phone, contact_preference) VALUES
('Alice Smith', 'alice@example.com', '1234567890', 'EMAIL'),
('Bob Johnson', 'bob@example.com', '1234567891', 'SMS'),
('Carol Davis', 'carol@example.com', '1234567892', 'DISCORD');

INSERT INTO users (username, password, asset_holder_id, is_admin) VALUES
('admin', 'admin', NULL, TRUE),
('user1', '123456', 1, FALSE),
('user2', '123456', 2, FALSE),
('user3', '123456', 3, FALSE);

INSERT INTO assets (name, region, drain_area, asset_holder_id) VALUES
('Big Ben',          'London & South East England', ST_GeomFromText('POLYGON((-0.1265 51.5003, -0.1250 51.5003, -0.1250 51.5018, -0.1265 51.5018, -0.1265 51.5003))', 4326), 1),
('Tower of London', 'London & South East England', ST_GeomFromText('POLYGON((-0.0775 51.5077, -0.0759 51.5077, -0.0759 51.5092, -0.0775 51.5092, -0.0775 51.5077))', 4326), 1),
('Buckingham Palace', 'London & South East England',ST_GeomFromText('POLYGON((-0.1445 51.5010, -0.1430 51.5010, -0.1430 51.5025, -0.1445 51.5025, -0.1445 51.5010))', 4326), 1),
('British Museum', 'London & South East England',  ST_GeomFromText('POLYGON((-0.1289 51.5190, -0.1274 51.5190, -0.1274 51.5205, -0.1289 51.5205, -0.1289 51.5190))', 4326), 2),
('St Paul''s Cathedral', 'London & South East England',ST_GeomFromText('POLYGON((-0.0998 51.5134, -0.0983 51.5134, -0.0983 51.5149, -0.0998 51.5149, -0.0998 51.5134))', 4326), 2),
('Hyde Park', 'London & South East England',ST_GeomFromText('POLYGON((-0.1670 51.5069, -0.1655 51.5069, -0.1655 51.5084, -0.1670 51.5084, -0.1670 51.5069))', 4326), 2),
('London Eye', 'London & South East England',ST_GeomFromText('POLYGON((-0.1209 51.5029, -0.1194 51.5029, -0.1194 51.5044, -0.1209 51.5044, -0.1209 51.5029))', 4326), 3),
('The Shard', 'London & South East England',ST_GeomFromText('POLYGON((-0.0881 51.5041, -0.0866 51.5041, -0.0866 51.5056, -0.0881 51.5056, -0.0881 51.5041))', 4326), 3),
('Westminster Abbey', 'London & South East England',ST_GeomFromText('POLYGON((-0.1284 51.4989, -0.1269 51.4989, -0.1269 51.5004, -0.1284 51.5004, -0.1284 51.4989))', 4326), 3);

INSERT INTO assets (name, region, drain_area, asset_holder_id) VALUES
('Stonehenge', 'South West England', ST_GeomFromText('POLYGON((-1.8263 51.1774, -1.8248 51.1774, -1.8248 51.1789, -1.8263 51.1789, -1.8263 51.1774))', 4326), 1),
('Bath Abbey', 'South West England', ST_GeomFromText('POLYGON((-2.3606 51.3814, -2.3591 51.3814, -2.3591 51.3829, -2.3606 51.3829, -2.3606 51.3814))', 4326), 1),
('Clifton Suspension Bridge', 'South West England', ST_GeomFromText('POLYGON((-2.6278 51.4545, -2.6263 51.4545, -2.6263 51.4560, -2.6278 51.4560, -2.6278 51.4545))', 4326), 1),
('St Michaels Mount', 'South West England', ST_GeomFromText('POLYGON((-5.4773 50.1181, -5.4758 50.1181, -5.4758 50.1196, -5.4773 50.1196, -5.4773 50.1181))', 4326), 2),
('Durdle Door', 'South West England', ST_GeomFromText('POLYGON((-2.2767 50.6207, -2.2752 50.6207, -2.2752 50.6222, -2.2767 50.6222, -2.2767 50.6207))', 4326), 2),
('Cheddar Gorge', 'South West England', ST_GeomFromText('POLYGON((-2.7713 51.2812, -2.7698 51.2812, -2.7698 51.2827, -2.7713 51.2827, -2.7713 51.2812))', 4326), 2),
('Tintern Abbey', 'South West England', ST_GeomFromText('POLYGON((-2.6840 51.6991, -2.6825 51.6991, -2.6825 51.7006, -2.6840 51.7006, -2.6840 51.6991))', 4326), 3),
('Lanhydrock House', 'South West England', ST_GeomFromText('POLYGON((-4.6750 50.4450, -4.6735 50.4450, -4.6735 50.4465, -4.6750 50.4465, -4.6750 50.4450))', 4326), 3),
('Exeter Cathedral', 'South West England', ST_GeomFromText('POLYGON((-3.5338 50.7220, -3.5323 50.7220, -3.5323 50.7235, -3.5338 50.7235, -3.5338 50.7220))', 4326), 3);

INSERT INTO assets (name, region, drain_area, asset_holder_id) VALUES
('Isle of Skye Museum', 'Orkney & Shetland', ST_GeomFromText('POLYGON((-6.5775 57.5938, -6.5760 57.5938, -6.5760 57.5953, -6.5775 57.5953, -6.5775 57.5938))', 4326), 3),
('Eilean Donan Castle', 'Highlands & Eilean Siar', ST_GeomFromText('POLYGON((-5.5162 57.2741, -5.5147 57.2741, -5.5147 57.2756, -5.5162 57.2756, -5.5162 57.2741))', 4326), 1),
('Loch Ness Centre', 'Highlands & Eilean Siar', ST_GeomFromText('POLYGON((-4.4775 57.3314, -4.4760 57.3314, -4.4760 57.3329, -4.4775 57.3329, -4.4775 57.3314))', 4326), 2),
('Glencoe Visitor Centre', 'Highlands & Eilean Siar', ST_GeomFromText('POLYGON((-5.1044 56.6821, -5.1029 56.6821, -5.1029 56.6836, -5.1044 56.6836, -5.1044 56.6821))', 4326), 3),
('The Kelpies', 'Grampian', ST_GeomFromText('POLYGON((-3.7525 56.0204, -3.7510 56.0204, -3.7510 56.0219, -3.7525 56.0219, -3.7525 56.0204))', 4326), 2),
('Stirling Castle', 'Central, Tayside & Fife', ST_GeomFromText('POLYGON((-3.9478 56.1225, -3.9463 56.1225, -3.9463 56.1240, -3.9478 56.1240, -3.9478 56.1225))', 4326), 1),
('Forth Bridge', 'Central, Tayside & Fife', ST_GeomFromText('POLYGON((-3.3869 56.0004, -3.3854 56.0004, -3.3854 56.0019, -3.3869 56.0019, -3.3869 56.0004))', 4326), 3),
('Kelvingrove Art Gallery', 'Strathclyde', ST_GeomFromText('POLYGON((-4.2900 55.8688, -4.2885 55.8688, -4.2885 55.8703, -4.2900 55.8703, -4.2900 55.8688))', 4326), 2),
('Edinburgh Castle', 'Dumfries, Galloway, Lothian & Borders', ST_GeomFromText('POLYGON((-3.2010 55.9486, -3.1995 55.9486, -3.1995 55.9501, -3.2010 55.9501, -3.2010 55.9486))', 4326), 1);

CREATE TABLE weather_warnings (
    warning_id INT PRIMARY KEY,
    weather_type VARCHAR,
    warning_level VARCHAR,
    warning_head_line TEXT,
    valid_from TIMESTAMPTZ,
    valid_to TIMESTAMPTZ,
    warning_impact VARCHAR,
    warning_likelihood VARCHAR,
    affected_areas TEXT,
    what_to_expect TEXT,
    warning_further_details TEXT,
    warning_update_description TEXT,
    polygon GEOMETRY(POLYGON, 4326)
);

INSERT INTO weather_warnings (
    warning_id,
    weather_type, warning_level, warning_head_line, valid_from, valid_to,
    warning_impact, warning_likelihood, affected_areas,
    what_to_expect, warning_further_details, warning_update_description,
    polygon
)
VALUES (
    1609,
    'THUNDERSTORM',
    'YELLOW',
    'Heavy showers and thunderstorms may lead to some disruption to transport and infrastructure.',
    to_timestamp(1749283200000 / 1000),
    to_timestamp(1749283200000 / 1000) + interval '1 year',
    '2/Low',
    '3/Likely',
    'East Midlands (Leicestershire, Northamptonshire);\n\nEast of England (Bedford, Cambridgeshire, Central Bedfordshire, Essex, Hertfordshire, Luton, Norfolk, Southend-on-Sea, Suffolk, Thurrock);\n\nLondon & South East England (Bracknell Forest, Brighton and Hove, Buckinghamshire, East Sussex, Greater London, Hampshire, Isle of Wight, Kent, Medway, Milton Keynes, Oxfordshire, Portsmouth, Reading, Slough, Southampton, Surrey, West Berkshire, West Sussex, Windsor and Maidenhead, Wokingham);\n\nSouth West England (Bath and North East Somerset, Bournemouth Christchurch and Poole, Bristol, Cornwall, Devon, Dorset, Gloucestershire, North Somerset, Plymouth, Somerset, South Gloucestershire, Swindon, Torbay, Wiltshire);\n\nWales (Blaenau Gwent, Bridgend, Caerphilly, Cardiff, Carmarthenshire, Merthyr Tydfil, Monmouthshire, Neath Port Talbot, Newport, Powys, Rhondda Cynon Taf, Swansea, Torfaen, Vale of Glamorgan);\n\nWest Midlands (Herefordshire, Shropshire, Staffordshire, Warwickshire, West Midlands Conurbation, Worcestershire)',
    'Probably some damage to a few buildings and structures from lightning strikes.\n\nThere is a good chance driving conditions will be affected by spray, standing water and/or hail, leading to longer journey times by car and bus.\n\nDelays to train services are possible.\n\nSome short term loss of power and other services is likely',
    'Frequent heavy showers and thunderstorms are expected for much of Saturday before fading from the west during the mid to late afternoon. \n\n10-15 mm of rain could fall in less than an hour, whilst some places could see 30-40 mm of rain over several hours from successive showers and thunderstorms. Frequent lightning, hail and strong, gusty winds will be additional hazards.\n\nWhat Should I Do?\n\nConsider if your location is at risk of flash flooding. If so, consider preparing a flood plan and an emergency flood kit. \n\nGive yourself the best chance of avoiding delays by checking road conditions if driving, or bus and train timetables, amending your travel plans if necessary.\n\nPeople cope better with power cuts when they have prepared for them in advance. It’s easy to do; consider gathering torches and batteries, a mobile phone power pack and other essential items.\n\nIf you find yourself outside and hear thunder, protect yourself by finding a safe enclosed shelter (such as a car). Do not shelter under or near trees, or other structures which may be struck by lightning. If you are on an elevated area move to lower ground.\n\nBe prepared for weather warnings to change quickly: when a weather warning is issued, the Met Office recommends staying up to date with the weather forecast in your area.',
    'Warning re-issued due to a technical error.',
    ST_GeomFromText('POLYGON((1.4337 52.6697, 1.7963 52.5797, 2.0435 52.3957, 2.0764 52.12, 2.0544 51.9036, 2.0105 51.761, 1.8567 51.4814, 1.4832 51.1104, 1.3293 50.9999, 1.1426 50.903, 0.3296 50.5832, -0.1538 50.4365, -0.5164 50.3595, -0.9119 50.2964, -1.1865 50.2683, -2.5873 50.1276, -3.7408 50.1065, -4.3451 50.3104, -4.6252 50.7086, -4.7296 51.2034, -4.3726 51.7338, -3.6145 52.1335, -3.2629 52.3488, -2.8235 52.4727, -2.0435 52.6964, -1.1646 52.5229, -0.7635 52.4493, -0.3516 52.4594, 0.4504 52.6531, 1.4337 52.6697))', 4326)
);