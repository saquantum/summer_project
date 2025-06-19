CREATE EXTENSION IF NOT EXISTS postgis;

DROP TABLE IF EXISTS user_email CASCADE;

CREATE TABLE user_email (
                            id SERIAL PRIMARY KEY,
                            email VARCHAR(255) UNIQUE NOT NULL
);

select * from user_email