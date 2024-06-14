import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class RegistrationForm extends JDialog {
    private JTextField tfName;
    private JTextField tfSurname;
    private JTextField tfLogin;
    private JTextField tfEmail;
    private JPasswordField pfPass;
    private JPasswordField pfConfPass;
    private JButton btnRegister;
    private JButton btnCancel;
    private JPanel registerPanel;
    public User user;

    public RegistrationForm(JFrame parent) {
        super(parent);
        setTitle("Utwórz nowe konto");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(450, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void registerUser() {
        String name = tfName.getText();
        String surname = tfSurname.getText();
        String email = tfEmail.getText();
        String login = tfLogin.getText();
        String password = String.valueOf(pfPass.getPassword());
        String confPass = String.valueOf(pfConfPass.getPassword());

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || login.isEmpty() || password.isEmpty() || confPass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Wprowadź wszystkie dane",
                    "Spróbuj ponownie",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.equals(confPass)) {
            JOptionPane.showMessageDialog(this,
                    "Podane hasła nie są zgodne",
                    "Spróbuj ponownie",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        user = addUserToDatabase(name, surname, login, email, password);
        if (user != null) {
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Rejestracja nie powiodła się",
                    "Spróbuj ponownie",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private User addUserToDatabase(String name, String surname, String login, String email, String password) {
        User user = null;
        final String DB_URL = "jdbc:mysql://localhost:3306/events";
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (name, surname, login, password, email) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, surname);
            preparedStatement.setString(3, login);
            preparedStatement.setString(4, password);
            preparedStatement.setString(5, email);

            int addedRows = preparedStatement.executeUpdate();
            if (addedRows > 0) {
                user = new User();
                user.name = name;
                user.surname = surname;
                user.login = login;
                user.password = password;
                user.email = email;
            }
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void main(String[] args) {
        RegistrationForm myForm = new RegistrationForm(null);
        User user = myForm.user;
        if (user != null) {
            System.out.println("Cześć, " + user.name);
        } else {
            System.out.println("Rejestracja została anulowana.");
        }
    }
}
