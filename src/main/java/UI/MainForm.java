package UI;

import Classes.User;
import com.javadocmd.simplelatlng.LatLng;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;
import java.util.function.Consumer;


public class MainForm extends JFrame implements ActionListener, ItemListener, ListSelectionListener {

    private static JFrame frame;
    private JList<String> emailJList;
    private JButton addBtn;
    private JButton delBtn;
    private JButton sendReqBtn;
    private JButton cancelReqBtn;
    private JButton closeBtn;
    private JTextField emailField;
    private JPanel rootPanel;
    private JButton clearBtn;
    private JButton updateBtn;
    private JProgressBar progressBar;
    private static int progressBarCounter = 0;
    private final ArrayList<String> emailArrayList = new ArrayList<>();
    private Timer timer;
    private static final DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private boolean flag = false;
    private final LatLng startingCoordinates = new LatLng(37.4219983, -122.084);

    private final static String URL = "jdbc:mysql://localhost:3306/TheArena?useSSL=false";
    private final static String USERNAME = "root";
    private final static String PASSWORD = null;
    private static Connection connection = null;

    public MainForm() {
        initialEmailList(emailJList);
        initialProgress();
        loadList();
        addBtn.addActionListener(this);
        closeBtn.addActionListener(this);
        clearBtn.addActionListener(this);
        delBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        cancelReqBtn.addActionListener(this);
        sendReqBtn.addActionListener(this);
        emailJList.addListSelectionListener(this);
    }

    private void addEmail(String emailToAdd) {
        emailArrayList.add(emailToAdd);
        emailListModel.addElement(emailToAdd);
        emailJList.setModel(emailListModel);
        emailField.setText("");
    }

    private void deleteSelectedValue(int index) {
        emailListModel.removeElementAt(index);
        emailJList.setModel(emailListModel);
    }

    private static void initialEmailList(JList<String> emailList) {
        emailList = new JList(emailListModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setBorder(BorderFactory.createLineBorder(Color.cyan, 10));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGui();
            }
        });
    }

    private static void createGui() {
        frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("The Arena Fake Location Generator");
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        emailField.setText(emailJList.getSelectedValue());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        switch (actionEvent.getActionCommand()) {
            case "Clear":
                emailField.setText("");
                emailJList.clearSelection();
                break;
            case "Add":
                addEmail(emailField.getText());
                break;
            case "Delete":
                deleteSelectedValue(emailJList.getSelectedIndex());
                break;
            case "Update":
                updateSelectedValue(emailJList.getSelectedIndex());
                break;
            case "Send Request":
                flag = false;
                sendRequest();
                break;
            case "Cancel Request":
                flag = true;
                break;
            case "Close":
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                break;
        }
    }

    private void initialProgress() {
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBarCounter = 0;
    }

    private void sendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (flag) {
                        break;
                    } else {
                        if (progressBarCounter==100){
                            progressBarCounter = 0;
                        }
                        while (progressBarCounter < 100){
                            progressBar.setValue(progressBarCounter);
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progressBarCounter+=1;
                        }
                        JSONObject obj = new JSONObject();
                        for (String s : emailArrayList) {
                            User user = new User(s, generateFakeCoordinates(startingCoordinates));
                            obj.put("lat", user.getCoordinates().getLatitude());
                            obj.put("lng", user.getCoordinates().getLongitude());
                            obj.put("mail", user.getEmail());
                            requestManager(obj,user);
                        }
                    }
                }
            }
        }).start();
    }

    private void requestManager(JSONObject obj, User user) {

        try{
            String url = ("http://localhost:8080/TheArenaServlet/onlineUsersLocation");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type","application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(obj.toString()))
                    .uri(URI.create(url))
                    .build();

            client.send(request,HttpResponse.BodyHandlers.ofString());
        }catch (Exception ignored){

        }
    }

    public static Connection getConnection() {
        /*
         * this function is initialize the connection
         */
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private void loadList(){
        Consumer<ResultSet> con = new Consumer<ResultSet>() {
            @Override
            public void accept(ResultSet resultSet) {
                try {
                    addEmail(resultSet.getString("email"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Statement statement = null;

        try{
            connection = getConnection();
            assert connection != null;
            statement = connection.createStatement();
            ResultSet res = statement.executeQuery("select email from users join usersStatus uS on users.id = uS.userId;");
            while(res.next()) {
                con.accept(res);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    private LatLng generateFakeCoordinates(LatLng coordinates) {
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        latLng.setLatitudeLongitude(latitude + (Math.random()*0.0001), longitude + (Math.random()*0.0001));

        return latLng;
    }

    private void updateSelectedValue(int index) {
        emailListModel.set(index, emailField.getText());
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

    }
}
