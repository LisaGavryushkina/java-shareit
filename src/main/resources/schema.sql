    DROP TABLE IF EXISTS bookings;
    DROP TABLE IF EXISTS comments;
    DROP TABLE IF EXISTS items;
    DROP TABLE IF EXISTS requests;
    DROP TABLE IF EXISTS users;

    CREATE TABLE IF NOT EXISTS users (
      id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
      name VARCHAR(255) NOT NULL,
      email VARCHAR(512) NOT NULL,
      CONSTRAINT pk_user PRIMARY KEY (id),
      CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
    );

    CREATE TABLE IF NOT EXISTS requests (
       id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
       description VARCHAR(512) NOT NULL,
       requestor_id BIGINT NOT NULL,
       created TIMESTAMP NOT NULL,
       CONSTRAINT pk_request PRIMARY KEY (id),
       CONSTRAINT fk_requests_to_users FOREIGN KEY(requestor_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS items (
      id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
      name VARCHAR(255) NOT NULL,
      description VARCHAR(512) NOT NULL,
      is_available BOOLEAN NOT NULL,
      owner_id BIGINT NOT NULL,
      request_id BIGINT,
      CONSTRAINT pk_item PRIMARY KEY (id),
      CONSTRAINT fk_items_to_users FOREIGN KEY(owner_id) REFERENCES users(id),
      CONSTRAINT fk_items_to_requests FOREIGN KEY(request_id) REFERENCES requests(id)
    );

    CREATE TABLE IF NOT EXISTS comments (
      id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
      text VARCHAR(512) NOT NULL,
      item_id BIGINT NOT NULL,
      author_id BIGINT NOT NULL,
      created TIMESTAMP NOT NULL,
      CONSTRAINT pk_comment PRIMARY KEY (id),
      CONSTRAINT fk_comments_to_users FOREIGN KEY(author_id) REFERENCES users(id),
      CONSTRAINT fk_comments_to_items FOREIGN KEY(item_id) REFERENCES items(id)
    );


    CREATE TABLE IF NOT EXISTS bookings (
      id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
      start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
      end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
      item_id BIGINT NOT NULL,
      booker_id BIGINT NOT NULL,
      status VARCHAR(50) NOT NULL,
      CONSTRAINT pk_booking PRIMARY KEY (id),
      CONSTRAINT fk_bookings_to_users FOREIGN KEY(booker_id) REFERENCES users(id),
      CONSTRAINT fk_bookings_to_items FOREIGN KEY(item_id) REFERENCES items(id)
    );