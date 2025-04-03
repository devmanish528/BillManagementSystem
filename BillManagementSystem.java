import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BillManagementSystem extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private JPanel loginPanel;
    private JPanel mainPanel;
    private JPanel createAccountPanel;

    private JTable itemsTable;
    private JButton generateBillBtn;
    private JButton cancelBtn;
    private JTextPane billTextPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton createAccountBtn;
    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JTextField phoneField; // Added for phone number field
    private JLabel billIdLabel; // Added for displaying bill ID

    private Map<String, Integer> itemPrices;

    private Connection connection;
    private String username;

    public BillManagementSystem() {
        setTitle("Bill Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        initializeLoginPanel();
        initializeMainPanel();
        initializeCreateAccountPanel();

        cardsPanel.add(loginPanel, "login");
        cardsPanel.add(mainPanel, "main");
        cardsPanel.add(createAccountPanel, "createAccount");

        add(cardsPanel);
        setVisible(true);

        // Connect to the database
        String url = "jdbc:mysql://localhost:3306/BillManagementDB";
        String user = "root";
        String password = "password@123";
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.");
            System.exit(1);
        }

        showLoginPanel(); // Show the login panel initially
    }

    private void clearSelection() {
        itemsTable.clearSelection();
    }

    private void initializeLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        loginPanel.add(usernameLabel, gbc);
        gbc.gridx = 2;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 2;
        loginPanel.add(passwordField, gbc);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneField = new JTextField(15);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(phoneLabel, gbc);
        gbc.gridx = 2;
        loginPanel.add(phoneField, gbc); // Adding phone field to the login panel

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        JButton loginButton = new JButton("Login");
        loginPanel.add(loginButton, gbc);

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardsPanel, "createAccount");
            }
        });
        gbc.gridx = 2;
        loginPanel.add(createAccountButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (authenticate(username, password)) {
                    BillManagementSystem.this.username = username;
                    cardLayout.show(cardsPanel, "main");
                } else {
                    JOptionPane.showMessageDialog(BillManagementSystem.this, "Invalid username or password!");
                }
            }
        });
    }

    private void initializeCreateAccountPanel() {
        createAccountPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel newUsernameLabel = new JLabel("New Username:");
        newUsernameField = new JTextField(15);
        createAccountPanel.add(newUsernameLabel, gbc);
        gbc.gridx = 2;
        createAccountPanel.add(newUsernameField, gbc);

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordField = new JPasswordField(15);
        gbc.gridx = 0;
        gbc.gridy = 1;
        createAccountPanel.add(newPasswordLabel, gbc);
        gbc.gridx = 2;
        createAccountPanel.add(newPasswordField, gbc);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneField = new JTextField(15);
        gbc.gridx = 0;
        gbc.gridy = 2;
        createAccountPanel.add(phoneLabel, gbc);
        gbc.gridx = 2;
        createAccountPanel.add(phoneField, gbc); // Adding phone field to the create account panel

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        createAccountBtn = new JButton("Create Account");
        createAccountPanel.add(createAccountBtn, gbc);

        createAccountBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUsername = newUsernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                String phone = phoneField.getText(); // Retrieving phone number
                if (createAccount(newUsername, newPassword, phone)) {
                    JOptionPane.showMessageDialog(BillManagementSystem.this, "Account created successfully!");
                    cardLayout.show(cardsPanel, "login");
                } else {
                    JOptionPane.showMessageDialog(BillManagementSystem.this, "Failed to create account. Please try again.");
                }
            }
        });
    }

    private boolean createAccount(String username, String password, String phone) {
        // Create a new account in the database
        String sql = "INSERT INTO Users (username, password, phone_no) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, phone); // Setting phone number in the prepared statement
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticate(String username, String password) {
        // Authenticate user against database
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If there is a result, authentication successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    private void initializeMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        initializeItems();

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateBillBtn = new JButton("Generate Bill");
        generateBillBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateBill();
                JOptionPane.showMessageDialog(BillManagementSystem.this, "Thank you for your order!");
            }
        });
        cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(80, 30));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSelection();
                billTextPane.setText("");
            }
        });
        buttonsPanel.add(generateBillBtn);
        buttonsPanel.add(cancelBtn);

        JScrollPane tableScrollPane = new JScrollPane(itemsTable);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);

        billTextPane = new JTextPane();
        billTextPane.setEditable(false);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(billTextPane, BorderLayout.SOUTH);
        mainPanel.add(buttonsPanel, BorderLayout.PAGE_END);
    }

    private void initializeItems() {
        String[] columnNames = {"Item", "Price (₹)", "Quantity"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Allow only the quantity column to be editable
            }
        };
        itemsTable = new JTable(model);
        TableColumn quantityColumn = itemsTable.getColumnModel().getColumn(2);
        quantityColumn.setCellEditor(new SpinnerEditor());

        itemPrices = new HashMap<>();
        itemPrices.put("Apple", 100);
        itemPrices.put("Banana", 40);
        itemPrices.put("Orange", 60);
        itemPrices.put("Potato", 30);
        itemPrices.put("Onion", 50);
        itemPrices.put("Tomato", 20);
        itemPrices.put("Cucumber", 25);
        itemPrices.put("Carrot", 35);
        itemPrices.put("Cabbage", 30);
        itemPrices.put("Spinach", 15);
        itemPrices.put("Capsicum", 45);
        itemPrices.put("Lemon", 10);
        itemPrices.put("Ginger", 20);
        itemPrices.put("Garlic", 30);
        itemPrices.put("Coriander", 5);
        itemPrices.put("Mint", 5);
        itemPrices.put("Rice", 80);
        itemPrices.put("Wheat Flour", 40);
        itemPrices.put("Sugar", 50);
        itemPrices.put("Salt", 10);

        for (Map.Entry<String, Integer> entry : itemPrices.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue(), 0});
        }
    }
    private void generateBill() {
        int totalAmount = 0;
        StringBuilder orderSummary = new StringBuilder("Thank you for your order, " + username + "!\nYou ordered:\n");
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String itemName = (String) model.getValueAt(i, 0);
            int price = (int) model.getValueAt(i, 1);
            int quantity = (int) model.getValueAt(i, 2);
            if (quantity > 0) {
                orderSummary.append("- ").append(itemName).append(" x ").append(quantity).append(" = ₹").append(price * quantity).append("\n");
                totalAmount += price * quantity;
            }
        }
        orderSummary.append("\nTotal Bill Amount: ₹").append(totalAmount).append("\n\nThank you for your order!");
    
        // Generate and store reference ID
        String referenceID = generateReferenceID();
        storeReferenceID(username, referenceID);
    
        // Store the bill in the database
        int billId = storeBill(referenceID, totalAmount);
        
        // Show the dialog box with bill ID
        JOptionPane.showMessageDialog(BillManagementSystem.this, orderSummary.toString() + "\n\nYour bill ID is: " + billId);
    }
    
    private int storeBill(String referenceID, int totalAmount) {
        int billId = -1;
        // Store bill in the database
        String sql = "INSERT INTO bills (username, total_amount, reference_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setInt(2, totalAmount);
            stmt.setString(3, referenceID);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    billId = generatedKeys.getInt(1);
                    storeItemsInBill(billId); // Store items related to this bill
                    System.out.println("Bill stored successfully with ID: " + billId);
                } else {
                    System.out.println("Failed to retrieve bill ID after insertion.");
                }
            } else {
                System.out.println("Failed to store the bill.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return billId;
    }
    
    private String generateReferenceID() {
        // Generate a random reference ID
        return "REF" + (int) (Math.random() * 10000);
    }

    private void storeReferenceID(String username, String referenceID) {
        // Store reference ID in the database
        String sql = "INSERT INTO References (username, reference_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, referenceID);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Reference ID stored successfully.");
            } else {
                System.out.println("Failed to store reference ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private void storeBill(String referenceID, int totalAmount) {
        // Store bill in the database
        String sql = "INSERT INTO bills (username, total_amount, reference_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setInt(2, totalAmount);
            stmt.setString(3, referenceID);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int billId = generatedKeys.getInt(1);
                    storeItemsInBill(billId); // Store items related to this bill
                    System.out.println("Bill stored successfully with ID: " + billId);
                } else {
                    System.out.println("Failed to retrieve bill ID after insertion.");
                }
            } else {
                System.out.println("Failed to store the bill.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void storeItemsInBill(int billId) {
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String itemName = (String) model.getValueAt(i, 0);
            int quantity = (int) model.getValueAt(i, 2);
            if (quantity > 0) {
                storeItemInBill(billId, itemName, quantity);
            }
        }
    }

    private void storeItemInBill(int billId, String itemName, int quantity) {
        // Store individual items and quantities in the bill_items table
        String sql = "INSERT INTO bill_items (bill_id, item_name, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setString(2, itemName);
            stmt.setInt(3, quantity);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Item '" + itemName + "' stored successfully for bill ID " + billId);
            } else {
                System.out.println("Failed to store item '" + itemName + "' for bill ID " + billId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;

        public SpinnerEditor() {
            spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }
    }

    private void showLoginPanel() {
        cardLayout.show(cardsPanel, "login");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BillManagementSystem();
            }
        });
    }
}