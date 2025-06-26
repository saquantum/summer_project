DROP TABLE IF EXISTS user_email CASCADE;
DROP TABLE IF EXISTS user_whatsapp CASCADE;
DROP TABLE IF EXISTS user_discord CASCADE;

CREATE TABLE user_email (
                            id SERIAL PRIMARY KEY,
                            email VARCHAR(255) UNIQUE NOT NULL,
                            uid VARCHAR(255) UNIQUE
);

CREATE TABLE user_whatsapp (
                            id SERIAL PRIMARY KEY,
                            phoneNumber VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE user_discord (
                               id SERIAL PRIMARY KEY,
                               discord VARCHAR(255) UNIQUE NOT NULL
);

select * from user_email;

select * from user_whatsapp;

select * from user_discord;