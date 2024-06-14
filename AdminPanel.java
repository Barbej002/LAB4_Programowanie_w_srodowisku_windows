import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminPanel extends JDialog {
    private JTextField wprowadźIdUżytkownikaTextField;
    private JComboBox<String> cbUser;
    private JTextField wprowadźIdWydarzeniaTextField;
    private JComboBox<String> cbEvent;
    private JTextField wprowadźIdRejestracjiTextField;
    private JComboBox<String> cbRegistration;
    private JButton OKButton;
    private JPanel adminPanel;

    public AdminPanel(JFrame parent) {
        super(parent);
        setTitle("Panel administratora");
        setContentPane(adminPanel);
        setMinimumSize(new Dimension(500, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initializeComboBoxes();

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userOption = (String) cbUser.getSelectedItem();
                String eventOption = (String) cbEvent.getSelectedItem();
                String registrationOption = (String) cbRegistration.getSelectedItem();

                if (!userOption.equals("Wybierz opcję")) {
                    int id_user = Integer.parseInt(wprowadźIdUżytkownikaTextField.getText());
                    if (userOption.equals("Dodaj")) {
                        addUser(id_user);
                    } else if (userOption.equals("Usuń")) {
                        dropUser(id_user);
                    } else if (userOption.equals("Resetuj hasło")) {
                        resetPassword(id_user);
                    }
                } else if (!eventOption.equals("Wybierz opcję")) {
                    int id_event = Integer.parseInt(wprowadźIdWydarzeniaTextField.getText());
                    if (eventOption.equals("Dodaj")) {
                        addEvent(id_event);
                    } else if (eventOption.equals("Usuń")) {
                        dropEvent(id_event);
                    } else if (eventOption.equals("Modyfikuj")) {
                        modifyEvent(id_event);
                    }
                } else if (!registrationOption.equals("Wybierz opcję")) {
                    int id_registration = Integer.parseInt(wprowadźIdRejestracjiTextField.getText());
                    if (registrationOption.equals("Potwierdź")) {
                        confirmRegistration(id_registration);
                    } else if (registrationOption.equals("Odrzuć")) {
                        dropRegistration(id_registration);
                    }
                }
            }
        });

        setVisible(true);
    }

    private void initializeComboBoxes() {
        cbUser.removeAllItems();
        cbEvent.removeAllItems();
        cbRegistration.removeAllItems();

        cbUser.addItem("Wybierz opcję");
        cbUser.addItem("Dodaj");
        cbUser.addItem("Usuń");
        cbUser.addItem("Resetuj hasło");

        cbEvent.addItem("Wybierz opcję");
        cbEvent.addItem("Dodaj");
        cbEvent.addItem("Usuń");
        cbEvent.addItem("Modyfikuj");

        cbRegistration.addItem("Wybierz opcję");
        cbRegistration.addItem("Potwierdź");
        cbRegistration.addItem("Odrzuć");
    }

    private void dropRegistration(int id_registration) {
        executeUpdate("DELETE FROM registrations WHERE id=?", id_registration, "Rejestracja odrzucona");
    }

    private void confirmRegistration(int id_registration) {
        executeUpdate("UPDATE registrations SET confirmed=true WHERE id=?", id_registration, "Rejestracja potwierdzona");
    }

    private void modifyEvent(int id_event) {
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM events WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id_event);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Załóżmy, że modyfikujemy tylko agendę wydarzenia
                String newAgenda = JOptionPane.showInputDialog(this, "Podaj nową agendę:");
                if (newAgenda != null) {
                    String updateSql = "UPDATE events SET agenda=? WHERE id=?";
                    PreparedStatement updateStatement = conn.prepareStatement(updateSql);
                    updateStatement.setString(1, newAgenda);
                    updateStatement.setInt(2, id_event);
                    int updatedRows = updateStatement.executeUpdate();
                    updateStatement.close();
                    if (updatedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Wydarzenie " + id_event + " zmodyfikowane");
                    } else {
                        JOptionPane.showMessageDialog(this, "Nie udało się zmodyfikować wydarzenia o ID: " + id_event);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nie znaleziono wydarzenia o ID: " + id_event);
            }
            resultSet.close();
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Wystąpił błąd: " + e.getMessage());
        }
    }

    private void dropEvent(int id_event) {
        executeUpdate("DELETE FROM events WHERE id=?", id_event, "Wydarzenie usunięte");
    }

    private void addEvent(int id_event) {
        JOptionPane.showMessageDialog(this, "Wydarzenie " + id_event + " dodane");
    }

    private void resetPassword(int id_user) {
        executeUpdate("UPDATE users SET password='new_password' WHERE id=?", id_user, "Hasło zresetowane");
    }

    private void dropUser(int id_user) {
        executeUpdate("DELETE FROM users WHERE id=?", id_user, "Użytkownik usunięty");
    }

    private void addUser(int id_user) {
        JOptionPane.showMessageDialog(this, "Użytkownik " + id_user + " dodany");
    }

    private void executeUpdate(String query, int id, String message) {
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, id);
            int affectedRows = preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, message);
            } else {
                JOptionPane.showMessageDialog(this, "Nie znaleziono rekordu o ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
