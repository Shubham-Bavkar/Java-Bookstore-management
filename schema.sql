CREATE DATABASE IF NOT EXISTS bookstore_db;
USE bookstore_db;

-- ---------- Entities ----------
CREATE TABLE publisher (
    publisher_id INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    street       VARCHAR(100),
    city         VARCHAR(50),
    pincode      VARCHAR(10)
);

-- Multivalued attribute Contact_no
CREATE TABLE publisher_phone (
    publisher_id INT,
    contact_no   VARCHAR(15),
    PRIMARY KEY (publisher_id, contact_no),
    FOREIGN KEY (publisher_id) REFERENCES publisher(publisher_id)
);

CREATE TABLE author (
    author_id  INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name  VARCHAR(50),
    email_id   VARCHAR(100),
    biography  TEXT
);

-- Book: N:1 with Publisher ("Published by")
CREATE TABLE book (
    book_id      INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    price        DECIMAL(8,2) NOT NULL,
    genre        VARCHAR(50),
    stock_count  INT DEFAULT 0,
    publisher_id INT NOT NULL,
    FOREIGN KEY (publisher_id) REFERENCES publisher(publisher_id)
);

-- "Writes": M:N between Author and Book
CREATE TABLE book_author (
    author_id INT,
    book_id   INT,
    PRIMARY KEY (author_id, book_id),
    FOREIGN KEY (author_id) REFERENCES author(author_id),
    FOREIGN KEY (book_id)   REFERENCES book(book_id)
);

CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50),
    email_id    VARCHAR(100),
    street      VARCHAR(100),
    city        VARCHAR(50),
    pincode     VARCHAR(10)
);

CREATE TABLE customer_phone (
    customer_id INT,
    phone_no    VARCHAR(15),
    PRIMARY KEY (customer_id, phone_no),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50),
    role        VARCHAR(50),
    salary      DECIMAL(10,2)
);

CREATE TABLE employee_phone (
    employee_id INT,
    phone_no    VARCHAR(15),
    PRIMARY KEY (employee_id, phone_no),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- "Places": Customer 1 : N Order   (ORDER is a reserved word, so table is "orders")
CREATE TABLE orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    order_date   DATE,
    order_amount DECIMAL(10,2),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- "Placed for": M:N between Book and Order (Quantity stored here)
CREATE TABLE order_book (
    order_id INT,
    book_id  INT,
    quantity INT NOT NULL,
    PRIMARY KEY (order_id, book_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (book_id)  REFERENCES book(book_id)
);

-- "Makes": Customer 1 : N Payment
CREATE TABLE payment (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id    INT NOT NULL,
    method         VARCHAR(30),
    status         VARCHAR(20),
    payment_date   DATE,
    payment_amount DECIMAL(10,2),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id)
);

-- "Handled by": M:N between Payment and Employee
CREATE TABLE payment_handler (
    payment_id  INT,
    employee_id INT,
    PRIMARY KEY (payment_id, employee_id),
    FOREIGN KEY (payment_id)  REFERENCES payment(payment_id),
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
);

-- ---------- Sample data ----------
INSERT INTO publisher (name, street, city, pincode) VALUES
 ('Penguin India', 'MG Road', 'Pune', '411001'),
 ('Bloomsbury',    'FC Road', 'Mumbai', '400001');
INSERT INTO publisher_phone VALUES (1, '9000000001'), (2, '9000000002');

INSERT INTO author (first_name, last_name, email_id, biography) VALUES
 ('R.K.',  'Narayan', 'rk@example.com',    'Indian novelist.'),
 ('J.K.',  'Rowling', 'jk@example.com',    'British author.');

INSERT INTO book (title, price, genre, stock_count, publisher_id) VALUES
 ('Malgudi Days',         250.00, 'Fiction', 20, 1),
 ('Harry Potter Part 1',  450.00, 'Fantasy', 15, 2);
INSERT INTO book_author VALUES (1, 1), (2, 2);

INSERT INTO customer (first_name, last_name, email_id, street, city, pincode) VALUES
 ('Amit', 'Patil', 'amit@example.com', 'Baner Road', 'Pune', '411045');
INSERT INTO customer_phone VALUES (1, '9111111111');

INSERT INTO employee (first_name, last_name, role, salary) VALUES
 ('Neha', 'Joshi', 'Cashier', 20000.00);
INSERT INTO employee_phone VALUES (1, '9222222222');
