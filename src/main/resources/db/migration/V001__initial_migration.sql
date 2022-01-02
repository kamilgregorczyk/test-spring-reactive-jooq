CREATE TABLE category
(
    id    BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX category_title_idx ON category (title);


CREATE TABLE recipe
(
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX recipe_title_idx ON recipe (title);

CREATE TABLE note
(
    id          BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    recipe_id   BIGINT REFERENCES recipe (id) ON DELETE CASCADE
);
CREATE INDEX note_recipe_id_idx ON note (recipe_id);

CREATE TABLE recipe_to_category
(
    id          BIGSERIAL PRIMARY KEY,
    recipe_id   BIGINT REFERENCES recipe (id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES category (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX recipe_categories_idx ON recipe_to_category (recipe_id, category_id);
