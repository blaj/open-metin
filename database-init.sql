-- 1. Add extensions
CREATE EXTENSION pgcrypto;

-- 2. Create open metin authentication database
\c postgres;

CREATE database openmetin_authentication;

\c openmetin_authentication;

CREATE USER openmetin_authentication_user WITH password 'strongpassword' noinherit;
comment ON ROLE openmetin_authentication_user IS 'backend app user';

CREATE USER openmetin_authentication_liquibase_user WITH password 'strongpassword' noinherit;
comment ON ROLE openmetin_authentication_liquibase_user IS 'liquibase db user';

GRANT CREATE ON database openmetin_authentication TO openmetin_authentication_liquibase_user;

CREATE SCHEMA liquibase;

GRANT USAGE, CREATE ON SCHEMA liquibase TO openmetin_authentication_liquibase_user;
GRANT USAGE, CREATE ON SCHEMA public TO openmetin_authentication_liquibase_user;

-- 3. Create open metin game database
\c postgres;

CREATE database openmetin_game;

\c openmetin_game;

CREATE USER openmetin_game_user WITH password 'strongpassword' noinherit;
comment ON ROLE openmetin_game_user IS 'backend app user';

CREATE USER openmetin_game_liquibase_user WITH password 'strongpassword' noinherit;
comment ON ROLE openmetin_game_liquibase_user IS 'liquibase db user';

GRANT CREATE ON database openmetin_game TO openmetin_game_liquibase_user;

CREATE SCHEMA liquibase;

GRANT USAGE, CREATE ON SCHEMA liquibase TO openmetin_game_liquibase_user;
GRANT USAGE, CREATE ON SCHEMA public TO openmetin_game_liquibase_user;