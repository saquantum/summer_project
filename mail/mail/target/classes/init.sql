DROP TABLE IF EXISTS user_email CASCADE;
DROP TABLE IF EXISTS user_whatsapp CASCADE;

CREATE TABLE user_email (
                            id SERIAL PRIMARY KEY,
                            email VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE user_whatsapp (
                            id SERIAL PRIMARY KEY,
                            phoneNumber VARCHAR(20) UNIQUE NOT NULL
);

select * from user_email;

select * from user_whatsapp;