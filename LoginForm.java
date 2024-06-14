import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JDialog {
    private JTextField tfLogin;
    private JPasswordField pfPassword;
    private JCheckBox cbShowPassword;
    private JButton btnLog;
    private JButton btnCancel;
    private JPanel loginPanel;
    public User user;

    public LoginForm(JFrame parent) {
        super(parent);
        setTitle("Zaloguj się");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(500, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnLog.addActionListener(e -> {
            String login = tfLogin.getText();
            String password = String.valueOf(pfPassword.getPassword());

            user = getAuthenticatedUser(login, password);

            if (user != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginForm.this,
                        "Nieprawidłowy login lub hasło",
                        "Spróbuj ponownie",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cbShowPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbShowPassword.isSelected()) {
                    pfPassword.setEchoChar((char) 0);
                } else {
                    pfPassword.setEchoChar('*');
                }
            }
        });
        setVisible(true);
    }

    private User getAuthenticatedUser(String login, String password) {
        User user = null;
        final String DB_URL = "jdbc:mariadb://localhost:3306/events"; // Specify your database name
        final String USERNAME = "root";
        final String PASSWORD = "";
        try {
            Class.forName("org.mariadb.jdbc.Driver");

            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE login=? AND password=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                user = new User();
                user.name = resultSet.getString("name");
                user.surname = resultSet.getString("surname");
                user.login = resultSet.getString("login");
                user.password = resultSet.getString("password");
                user.email = resultSet.getString("email");
                user.permission = resultSet.getString("permission");
                user.registrationDate = resultSet.getString("registration_date");
                user.id = resultSet.getInt("id");
            }
            resultSet.close();
            preparedStatement.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void main(String[] args) {
        LoginForm loginForm = new LoginForm(null);
        User user = loginForm.user;
        if (user != null) {
            System.out.println("Zalogowano");
            if (user.permission.equals("Admin")) {
                new AdminPanel(null);
            } else {
                new UserPanel(null, user);  // Przekazanie użytkownika do UserPanel
            }
        } else {
            System.out.println("Nieprawidłowe dane");
        }
    }
}
