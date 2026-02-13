import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// ---------- Abstract Class ----------
abstract class ParkingSlot {
    protected int slotNumber;
    protected boolean isBooked;

    public ParkingSlot(int slotNumber) {
        this.slotNumber = slotNumber;
        this.isBooked = false;
    }

    public abstract String getSlotType();

    public void bookSlot() throws Exception {
        if (isBooked) {
            throw new Exception("Slot " + slotNumber + " is already booked!");
        }
        isBooked = true;
    }
}

// ---------- Inheritance ----------
class CarSlot extends ParkingSlot {
    public CarSlot(int slotNumber) {
        super(slotNumber);
    }

    public String getSlotType() {
        return "Car";
    }
}

class BikeSlot extends ParkingSlot {
    public BikeSlot(int slotNumber) {
        super(slotNumber);
    }

    public String getSlotType() {
        return "Bike";
    }
}

// ---------- Main Class ----------
public class ParkingSlotBookingSystem extends JFrame implements ActionListener {

    JTextField nameField, slotField;
    JComboBox<String> vehicleBox;
    JTextArea outputArea;
    JButton bookBtn;

    ParkingSlot[] slots = new ParkingSlot[10];
    Connection conn;

    public ParkingSlotBookingSystem() {

        // Initialize slots
        for (int i = 0; i < 10; i++) {
            if (i < 5)
                slots[i] = new CarSlot(i + 1);
            else
                slots[i] = new BikeSlot(i + 1);
        }

        connectDatabase();
        createTable();

        setTitle("Parking Slot Booking System");
        setSize(520, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Parking Slot Booking System", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("User Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Vehicle Type:"));
        vehicleBox = new JComboBox<>(new String[]{"Car", "Bike"});
        panel.add(vehicleBox);

        panel.add(new JLabel("Slot Number (1-10):"));
        slotField = new JTextField();
        panel.add(slotField);

        bookBtn = new JButton("Book Slot");
        bookBtn.addActionListener(this);
        panel.add(bookBtn);

        add(panel, BorderLayout.CENTER);

        outputArea = new JTextArea(6, 30);
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);
    }

    // ---------- Database Connection ----------
    private void connectDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:parking.db");
            System.out.println("Database Connected");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database connection failed!\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Create Table ----------
    private void createTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "vehicle TEXT NOT NULL," +
                    "slot INTEGER NOT NULL)";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Save Booking ----------
    private void saveBooking(String name, String vehicle, int slot) {
        try {
            String sql = "INSERT INTO bookings(name, vehicle, slot) VALUES(?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, vehicle);
            pstmt.setInt(3, slot);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Button Action ----------
    @Override
    public void actionPerformed(ActionEvent e) {

        new Thread(() -> {
            try {

                String name = nameField.getText().trim();
                if (name.isEmpty())
                    throw new Exception("Name cannot be empty!");

                int slotNo = Integer.parseInt(slotField.getText());
                if (slotNo < 1 || slotNo > 10)
                    throw new Exception("Slot must be between 1 and 10!");

                ParkingSlot slot = slots[slotNo - 1];
                slot.bookSlot();

                String vehicle = vehicleBox.getSelectedItem().toString();

                saveBooking(name, vehicle, slotNo);

                SwingUtilities.invokeLater(() -> {

                    outputArea.setText(
                            "User Name     : " + name +
                            "\nVehicle Type  : " + vehicle +
                            "\n--------------------------------" +
                            "\nSlot " + slotNo + " is BOOKED" +
                            "\nSaved in database (parking.db)"
                    );

                    JOptionPane.showMessageDialog(this,
                            "Slot " + slotNo + " is booked and saved in database!",
                            "Booking Confirmed",
                            JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (NumberFormatException ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Enter valid slot number",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE)
                );
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ParkingSlotBookingSystem().setVisible(true);
        });
    }
}
