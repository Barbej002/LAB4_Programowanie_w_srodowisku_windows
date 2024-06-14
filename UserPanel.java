import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UserPanel extends JDialog {
    private JComboBox<String> cbEvent;
    private JTextArea taAgenda;
    private JTextField tfDate;
    private JComboBox<String> cbAttendence;
    private JComboBox<String> cbFood;
    private JButton btnOK;
    private JLabel lbEvent;
    private JPanel UserPanel;
    private User user;
    public Registration registration;
    public Event event;

    public UserPanel(JFrame parent, User user) {
        super(parent);
        this.user = user;
        setTitle("Panel użytkownika");
        setContentPane(UserPanel);
        setMinimumSize(new Dimension(500, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initializeComboBoxes();
        loadEvents();

        cbEvent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = (String) cbEvent.getSelectedItem();
                event = getEventInfo(title);
                if (event != null) {
                    taAgenda.setText(event.agenda);
                    tfDate.setText(event.date);
                } else {
                    taAgenda.setText("");
                    tfDate.setText("");
                }
            }
        });

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (event == null) {
                    JOptionPane.showMessageDialog(UserPanel.this,
                            "Proszę wybrać wydarzenie przed rejestracją.",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    addRegistration();
                }
            }
        });
        setVisible(true);
    }

    private void initializeComboBoxes() {
        cbAttendence.removeAllItems();
        cbFood.removeAllItems();

        cbAttendence.addItem("Słuchacz");
        cbAttendence.addItem("Autor");
        cbAttendence.addItem("Sponsor");
        cbAttendence.addItem("Organizator");

        cbFood.addItem("Bez szczególnych preferencji");
        cbFood.addItem("Wegetariańskie");
        cbFood.addItem("Bez glutenu");
    }

    private void loadEvents() {
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT title FROM events";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            cbEvent.removeAllItems();
            while (resultSet.next()) {
                cbEvent.addItem(resultSet.getString("title"));
            }

            resultSet.close();
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRegistration() {
        String food = (String) cbFood.getSelectedItem();
        String attendance = (String) cbAttendence.getSelectedItem();

        registration = addRegistrationToDatabase(user.id, event.id, food, attendance);
    }

    private Registration addRegistrationToDatabase(int userId, int eventId, String food, String attendance) {
        Registration registration = null;
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO registrations (id_user, id_event, food, attendance) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, eventId);
            preparedStatement.setString(3, food);
            preparedStatement.setString(4, attendance);

            int addedRows = preparedStatement.executeUpdate();

            if (addedRows > 0) {
                registration = new Registration();
                registration.food = food;
                registration.attendance = attendance;
                registration.id_user = userId;
                registration.id_event = eventId;
            }
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return registration;
    }

    private Event getEventInfo(String title) {
        Event event = null;
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM events WHERE title=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                event = new Event();
                event.agenda = resultSet.getString("agenda");
                event.date = resultSet.getString("date");
                event.id = resultSet.getInt("id");
            }
            resultSet.close();
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }
}
