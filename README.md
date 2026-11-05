# Bookstore Management System

A console-based bookstore management application built with **Java (JDBC)** and **MySQL**. It is based on an ER diagram with seven entities: Author, Book, Publisher, Customer, Employee, Order and Payment.

## Database (`schema.sql`)

The `bookstore_db` database has 12 tables, which come from converting the ER diagram into a relational schema:

- **Main entities:** `publisher`, `author`, `book`, `customer`, `employee`, `orders` and `payment`. The order table is named `orders` because `ORDER` is a reserved word in SQL.
- **Multivalued phone attributes:** `publisher_phone`, `customer_phone` and `employee_phone` hold the multiple phone numbers each person or publisher can have.
- **One-to-many relationships:** these are foreign keys. A book belongs to one publisher, and a customer can have many orders and payments.
- **Many-to-many relationships:** these use link tables. `book_author` links authors and books, `order_book` links orders and books (and stores the quantity), and `payment_handler` links payments and the employees who handled them.

The script also inserts sample data: two publishers, two authors, two books, one customer and one employee.

## Application (`Main.java`)

The program connects to MySQL with JDBC and shows a menu in a loop:

| Option | What it does |
|---|---|
| 1. View books | Lists books with their publisher and authors (joins three tables) |
| 2. Add book | Inserts a book and links it to an author |
| 3. Add customer | Inserts a customer and their phone number |
| 4. Place order | Checks stock, calculates the amount, saves the order and reduces the book's stock |
| 5. View orders | Shows each order with customer, book, quantity, amount and date |
| 6. Make payment | Records a payment for a customer and links it to the employee handling it |
| 7. View customers | Lists customers with address and phone numbers |
| 8. View payments | Lists payments with the ids of the employees handling them |
| 0. Exit | Closes the connection and quits |

## Key concepts used

- `PreparedStatement` for every insert and parameterised query, which also protects against SQL injection.
- Transactions (`commit` and `rollback`) so that multi-step actions, such as placing an order, either fully succeed or are fully undone.
- SQL joins and `GROUP_CONCAT` to combine data from several tables into one readable row.

## How to run

1. Run `schema.sql` in MySQL to create the `bookstore_db` database, tables and sample data.
2. Download the MySQL Connector/J jar (`mysql-connector-j-x.x.x.jar`) and keep it in the project folder.
3. Check the connection settings at the top of `Main.java` (`URL`, `USER` and `PASS`). By default it connects to `bookstore_db` as `root` with an empty password.
4. Compile and run (on Windows, use `;` instead of `:` in the classpath):

```
javac Main.java
java -cp .:mysql-connector-j-x.x.x.jar Main
```

## Project files

- `schema.sql`: database schema and sample data
- `Main.java`: the console application
- `README.md`: this file
