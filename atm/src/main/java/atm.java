import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList; // ArrayList using for store Transaction history 

public class atm { 
// it is our class

    private static double accountBalance = 10000; // current Account balance  is 10000
    private static String accountPIN = "882703"; // by default we are using this PIN
    private static final ArrayList<String> transactionHistory = new ArrayList<>(); // for store transactions history 

    public static void main(String[] args) { 
        // main method
        // PIN validate
        if (!validatePIN()) { 
        // if pin is wrong 
            JOptionPane.showMessageDialog(null, "many incorrect attempts..."); // Error message if many times put wrong atm pin
            System.exit(0); // here the end of Program
        }

        // Main frame interface by using GUI
        JFrame frame = new JFrame("ATM Machine Simulation"); // this is the user interface
        frame.setSize(1024, 768); //here set the Frame size height ,width
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // here using close operation for Close button for programm terminate
        frame.setLayout(null);

        //using panel
        JPanel screenPanel = new JPanel(); 
        screenPanel.setBounds(300, 50, 400, 200);
        screenPanel.setLayout(null);
        screenPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel screenLabel = new JLabel("Please Select Your Transaction", JLabel.CENTER);
        screenLabel.setFont(new Font("Arial", Font.BOLD, 18)); 
        screenLabel.setBounds(10, 10, 380, 40); 
        screenPanel.add(screenLabel); 

        //creating buttons
        String[] transactions = {"DEPOSIT", "CASH WITHDRAWAL", "FAST CASH", "MINI STATEMENT", "PIN CHANGE", "BALANCE ENQUIRY", "EXIT"}; // this is options
        JButton[] transactionButtons = new JButton[transactions.length]; // we are taking array for each button

        for (int i = 0; i < transactions.length; i++) { 
            transactionButtons[i] = new JButton(transactions[i]);
            transactionButtons[i].setFont(new Font("Arial", Font.BOLD, 12)); // set the font size of buttons
            transactionButtons[i].setBounds((i % 2) * 200 + 10, (i / 2) * 40 + 60, 180, 30);
            screenPanel.add(transactionButtons[i]);
        }

        frame.add(screenPanel);

        // for Keypad panel 
        JPanel keypadPanel = new JPanel();
        keypadPanel.setBounds(300, 300, 400, 400); 
        keypadPanel.setLayout(new GridLayout(4, 3, 10, 10));
        keypadPanel.setBorder(BorderFactory.createTitledBorder("Keypad"));

        JButton[] numberButtons = new JButton[10];// for the nombers of buttons
        for (int i = 1; i <= 9; i++) { //
            numberButtons[i] = new JButton(String.valueOf(i));
            numberButtons[i].setFont(new Font("Arial", Font.BOLD, 18));
            keypadPanel.add(numberButtons[i]);
        }

        numberButtons[0] = new JButton("0");
        numberButtons[0].setFont(new Font("Arial", Font.BOLD, 18)); 
        keypadPanel.add(new JLabel());
        keypadPanel.add(numberButtons[0]);
        keypadPanel.add(new JLabel());

        frame.add(keypadPanel); // add keypad in to frame

        // Cancel, Clear Enter buttons
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);

        JButton clearButton = new JButton("CLEAR");
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setBackground(Color.YELLOW);

        JButton enterButton = new JButton("ENTER");
        enterButton.setFont(new Font("Arial", Font.BOLD, 14));
        enterButton.setBackground(Color.GREEN);
        enterButton.setForeground(Color.WHITE);

        JPanel actionPanel = new JPanel();  
        actionPanel.setBounds(300, 720, 400, 40);  
        actionPanel.setLayout(new GridLayout(1, 3, 10, 0));  
        actionPanel.add(cancelButton);  
        actionPanel.add(clearButton);  
        actionPanel.add(enterButton); 

        frame.add(actionPanel);  

        transactionButtons[0].addActionListener(e -> deposit(frame)); 
        transactionButtons[1].addActionListener(e -> withdraw(frame));
        transactionButtons[2].addActionListener(e -> fastCash(frame)); 
        transactionButtons[3].addActionListener(e -> miniStatement(frame));
        transactionButtons[4].addActionListener(e -> changePIN(frame));
        transactionButtons[5].addActionListener(e -> balanceInquiry(frame));
        transactionButtons[6].addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private static boolean validatePIN() { // PIN validation method
        int attempts = 0; //count attempt for pin 
        while (attempts < 3) { // only three chances for attempt the pin
            String enteredPIN = JOptionPane.showInputDialog(null, "Enter YOUR Account PIN");
            if (enteredPIN == null) { // if the user do cancle 
                return false; //  validation fail
            }
            if (enteredPIN.equals(accountPIN)) { // if pin is right
                return true; // Validation pass
            }
            attempts++; // count wrong attemps
            JOptionPane.showMessageDialog(null, "Incorrect PIN. Remaining Attemps is: " + (3 - attempts));
        }
        return false;
    }

//    private static void deposit(JFrame frame) {
//        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to deposit:");
//        try {
//            double amount = Double.parseDouble(amountStr); //change string into numbers
//            if (amount > 0) { 
//                accountBalance += amount; //update balance
//                transactionHistory.add("Deposited: " + amount);
//                JOptionPane.showMessageDialog(frame, "Deposit successful! New balance: " + accountBalance);
//            } else {
//                JOptionPane.showMessageDialog(frame, "Invalid amount.");
//            }
//        } catch (NumberFormatException e) {
//            JOptionPane.showMessageDialog(frame, "Invalid input.");
//        }
//    }
private static void deposit(JFrame frame) {
    Connection conn = DatabaseConnection.connect();
    if (conn == null) return;

    String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to deposit:");
    try {
        double amount = Double.parseDouble(amountStr);
        if (amount > 0) {
            // Update balance in the database
            String updateBalanceQuery = "UPDATE users SET balance = balance + ? WHERE pin = ?";
            var pstmt = conn.prepareStatement(updateBalanceQuery);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, accountPIN);
            pstmt.executeUpdate();

            // Add transaction to history
            String insertTransactionQuery = "INSERT INTO transactions (user_id, transaction_type, amount) " +
                                             "VALUES ((SELECT user_id FROM users WHERE pin = ?), 'Deposit', ?)";
            pstmt = conn.prepareStatement(insertTransactionQuery);
            pstmt.setString(1, accountPIN);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Deposit successful!");
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid amount.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(frame, "Invalid input.");
        e.printStackTrace();
    }
}

    private static void withdraw(JFrame frame) {
        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to withdraw:");
        try {
            double amount = Double.parseDouble(amountStr); //change String in to number
            if (amount > 0 && amount <= accountBalance) { //check Valid amount & balance
                accountBalance -= amount;
                transactionHistory.add("Withdrew: $" + amount);
                JOptionPane.showMessageDialog(frame, "Withdrawal successful! New balance: " + accountBalance);
            } else {
                JOptionPane.showMessageDialog(frame, "insufficient amount.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input.");
        }
    }

    private static void fastCash(JFrame frame) { 
        double[] amounts = {50, 100, 200, 500}; 
        String options = "";
        for (int i = 0; i < amounts.length; i++) {
            options += (i + 1) + ". $" + amounts[i] + "\n";
        }
        String choiceStr = JOptionPane.showInputDialog(frame, "Select Fast Cash Amount:\n" + options);
        try {
            int choice = Integer.parseInt(choiceStr);
            if (choice >= 1 && choice <= amounts.length) { 
                double amount = amounts[choice - 1]; // select Amount for fastcash withdraw
                if (amount <= accountBalance) {
                    accountBalance -= amount;
                    transactionHistory.add("Fast Cash: " + amount);
                    JOptionPane.showMessageDialog(frame, "Fast Cash successful! New balance: " + accountBalance);
                } else {
                    JOptionPane.showMessageDialog(frame, "Insufficient balance.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid choice.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input.");
        }
    }

    private static void miniStatement(JFrame frame) {
        if (transactionHistory.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No transactions available.");
        } else {
            StringBuilder statement = new StringBuilder("Transaction History:\n");
            for (String transaction : transactionHistory) { //adding of each transactions
                statement.append(transaction).append("\n");
            }
            JOptionPane.showMessageDialog(frame, statement.toString()); // show Statement
        }
    }

    private static void changePIN(JFrame frame) {
        String oldPIN = JOptionPane.showInputDialog(frame, "Enter current PIN:");
        if (oldPIN.equals(accountPIN)) { // checking the Old PIN
            String newPIN = JOptionPane.showInputDialog(frame, "Enter new PIN:");
            String confirmPIN = JOptionPane.showInputDialog(frame, "Confirm new PIN:");
            if (newPIN.equals(confirmPIN)) { // here check the new pin and confirm pin is equally same
                accountPIN = newPIN;
                transactionHistory.add("PIN changed successfully.");
                JOptionPane.showMessageDialog(frame, "PIN changed successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "PINs do not match.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Incorrect current PIN.");
        }
    }

    private static void balanceInquiry(JFrame frame) { // for using Balance inquiry
        JOptionPane.showMessageDialog(frame, "Your current balance is: " + accountBalance); // show Balance
        transactionHistory.add("Balance inquiry: " + accountBalance);
    }
}   