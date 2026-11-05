import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL  = "jdbc:mysql://localhost:3306/bookstore_db";
    static final String USER = "root";
    static final String PASS = "";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        con = DriverManager.getConnection(URL, USER, PASS);
        System.out.println("Connected to bookstore database.");

        while (true) {
            System.out.println("\n===== BOOKSTORE MANAGEMENT =====");
            System.out.println("1. View books");
            System.out.println("2. Add book");
            System.out.println("3. Add customer");
            System.out.println("4. Place order");
            System.out.println("5. View orders");
            System.out.println("6. Make payment");
            System.out.println("7. View customers");
            System.out.println("8. View payments"); 
            System.out.println("0. Exit");
            int choice = readInt("Choice: ");
            try {
                switch (choice) {
                    case 1: viewBooks();    break;
                    case 2: addBook();      break;
                    case 3: addCustomer();  break;
                    case 4: placeOrder();   break;
                    case 5: viewOrders();   break;
                    case 6: makePayment();  break;
                    case 7: viewCustomers(); break;
                    case 8: viewPayments(); break;
                    case 0: con.close(); return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    // ---------- helpers ----------
    static String read(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    static int readInt(String msg) {
        return Integer.parseInt(read(msg));
    }

    // ---------- 1. View books (Book + Publisher + Author) ----------
    static void viewBooks() throws SQLException {
        String sql = "SELECT b.book_id, b.title, b.price, b.stock_count, p.name AS publisher, "
                   + "GROUP_CONCAT(CONCAT(a.first_name,' ',a.last_name)) AS authors "
                   + "FROM book b JOIN publisher p ON b.publisher_id = p.publisher_id "
                   + "LEFT JOIN book_author ba ON b.book_id = ba.book_id "
                   + "LEFT JOIN author a ON ba.author_id = a.author_id "
                   + "GROUP BY b.book_id, p.name";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-4s %-25s %-8s %-6s %-15s %s%n",
                    "ID", "Title", "Price", "Stock", "Publisher", "Authors");
            while (rs.next()) {
                System.out.printf("%-4d %-25s %-8.2f %-6d %-15s %s%n",
                        rs.getInt("book_id"), rs.getString("title"), rs.getDouble("price"),
                        rs.getInt("stock_count"), rs.getString("publisher"), rs.getString("authors"));
            }
        }
    }

    // ---------- 2. Add book ----------
    static void addBook() throws SQLException {
        String title = read("Title: ");
        double price = Double.parseDouble(read("Price: "));
        String genre = read("Genre: ");
        int stock    = readInt("Stock count: ");
        int pubId    = readInt("Publisher id: ");
        int authorId = readInt("Author id: ");

        con.setAutoCommit(false);
        try {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO book (title, price, genre, stock_count, publisher_id) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setDouble(2, price);
            ps.setString(3, genre);
            ps.setInt(4, stock);
            ps.setInt(5, pubId);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int bookId = keys.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("INSERT INTO book_author VALUES (?,?)");
            ps2.setInt(1, authorId);
            ps2.setInt(2, bookId);
            ps2.executeUpdate();

            con.commit();
            System.out.println("Book added with id " + bookId);
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ---------- 3. Add customer ----------
    static void addCustomer() throws SQLException {
        String first  = read("First name: ");
        String last   = read("Last name: ");
        String email  = read("Email: ");
        String street = read("Street: ");
        String city   = read("City: ");
        String pin    = read("Pincode: ");
        String phone  = read("Phone: ");

        con.setAutoCommit(false);
        try {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO customer (first_name, last_name, email_id, street, city, pincode) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, email);
            ps.setString(4, street);
            ps.setString(5, city);
            ps.setString(6, pin);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int custId = keys.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("INSERT INTO customer_phone VALUES (?,?)");
            ps2.setInt(1, custId);
            ps2.setString(2, phone);
            ps2.executeUpdate();

            con.commit();
            System.out.println("Customer added with id " + custId);
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ---------- 4. Place order (Customer -> Order -> Book) ----------
    static void placeOrder() throws SQLException {
        int custId = readInt("Customer id: ");
        int bookId = readInt("Book id: ");
        int qty    = readInt("Quantity: ");

        PreparedStatement chk = con.prepareStatement("SELECT price, stock_count FROM book WHERE book_id = ?");
        chk.setInt(1, bookId);
        ResultSet rs = chk.executeQuery();
        if (!rs.next()) { System.out.println("Book not found."); return; }
        double price = rs.getDouble("price");
        int stock    = rs.getInt("stock_count");
        if (qty > stock) { System.out.println("Only " + stock + " in stock."); return; }

        con.setAutoCommit(false);
        try {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO orders (customer_id, order_date, order_amount) VALUES (?, CURDATE(), ?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, custId);
            ps.setDouble(2, price * qty);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int orderId = keys.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("INSERT INTO order_book VALUES (?,?,?)");
            ps2.setInt(1, orderId);
            ps2.setInt(2, bookId);
            ps2.setInt(3, qty);
            ps2.executeUpdate();

            PreparedStatement ps3 = con.prepareStatement(
                "UPDATE book SET stock_count = stock_count - ? WHERE book_id = ?");
            ps3.setInt(1, qty);
            ps3.setInt(2, bookId);
            ps3.executeUpdate();

            con.commit();
            System.out.println("Order placed. Order id " + orderId + ", amount = " + (price * qty));
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ---------- 5. View orders ----------
    static void viewOrders() throws SQLException {
        String sql = "SELECT o.order_id, CONCAT(c.first_name,' ',c.last_name) AS customer, "
                   + "b.title, ob.quantity, o.order_amount, o.order_date "
                   + "FROM orders o JOIN customer c ON o.customer_id = c.customer_id "
                   + "JOIN order_book ob ON o.order_id = ob.order_id "
                   + "JOIN book b ON ob.book_id = b.book_id ORDER BY o.order_id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-6s %-15s %-25s %-4s %-10s %s%n",
                    "Order", "Customer", "Book", "Qty", "Amount", "Date");
            while (rs.next()) {
                System.out.printf("%-6d %-15s %-25s %-4d %-10.2f %s%n",
                        rs.getInt("order_id"), rs.getString("customer"), rs.getString("title"),
                        rs.getInt("quantity"), rs.getDouble("order_amount"), rs.getDate("order_date"));
            }
        }
    }

    // ---------- 6. Make payment (Customer -> Payment <- Employee) ----------
    static void makePayment() throws SQLException {
        int custId   = readInt("Customer id: ");
        int empId    = readInt("Employee id handling payment: ");
        double amt   = Double.parseDouble(read("Amount: "));
        String method = read("Method (Cash/UPI/Card): ");

        con.setAutoCommit(false);
        try {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO payment (customer_id, method, status, payment_date, payment_amount) "
              + "VALUES (?,?,'Paid',CURDATE(),?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, custId);
            ps.setString(2, method);
            ps.setDouble(3, amt);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int payId = keys.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("INSERT INTO payment_handler VALUES (?,?)");
            ps2.setInt(1, payId);
            ps2.setInt(2, empId);
            ps2.executeUpdate();

            con.commit();
            System.out.println("Payment recorded. Payment id " + payId);
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    // ---------- 7. View customers ----------
    static void viewCustomers() throws SQLException {
        String sql = "SELECT c.customer_id, CONCAT(c.first_name,' ',c.last_name) AS name, c.email_id, "
                   + "CONCAT(c.street,', ',c.city,' - ',c.pincode) AS address, "
                   + "GROUP_CONCAT(cp.phone_no) AS phones "
                   + "FROM customer c LEFT JOIN customer_phone cp ON c.customer_id = cp.customer_id "
                   + "GROUP BY c.customer_id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-4s %-18s %-22s %-30s %s%n", "ID", "Name", "Email", "Address", "Phone");
            while (rs.next()) {
                System.out.printf("%-4d %-18s %-22s %-30s %s%n",
                        rs.getInt("customer_id"), rs.getString("name"), rs.getString("email_id"),
                        rs.getString("address"), rs.getString("phones"));
            }
        }
    }

    // ---------- 8. View payments with the ids of employees handling them ----------
    static void viewPayments() throws SQLException {
        String sql = "SELECT p.payment_id, CONCAT(c.first_name,' ',c.last_name) AS customer, "
                   + "p.method, p.status, p.payment_amount, p.payment_date, "
                   + "GROUP_CONCAT(ph.employee_id) AS employee_ids "
                   + "FROM payment p JOIN customer c ON p.customer_id = c.customer_id "
                   + "LEFT JOIN payment_handler ph ON p.payment_id = ph.payment_id "
                   + "GROUP BY p.payment_id, c.first_name, c.last_name "
                   + "ORDER BY p.payment_id";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-4s %-18s %-8s %-8s %-10s %-12s %s%n",
                    "ID", "Customer", "Method", "Status", "Amount", "Date", "Employee ids");
            while (rs.next()) {
                System.out.printf("%-4d %-18s %-8s %-8s %-10.2f %-12s %s%n",
                        rs.getInt("payment_id"), rs.getString("customer"), rs.getString("method"),
                        rs.getString("status"), rs.getDouble("payment_amount"),
                        rs.getDate("payment_date"), rs.getString("employee_ids"));
            }
        }
    }
}
