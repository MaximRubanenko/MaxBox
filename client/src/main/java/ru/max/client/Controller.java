package ru.max.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Data;
import messages.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Data
public class Controller implements Initializable{

    private static Network connection;
    private List<FileInfo> fileList;
    private FileChooser fileChooser;
    public final static long CAPACITY_CLOUD_IN_GB = 10;
    private FileWorker fileWorker;

    @FXML
    VBox cloudPane;

    @FXML
    TableView <FileInfo> tableView;

    @FXML
    GridPane regPane;
    @FXML
    TextField regLogin;
    @FXML
    PasswordField regPassword;
    @FXML
    PasswordField regPasswordRep;
    @FXML
    TextField regName;
    @FXML
    Label regMessage;

    @FXML
    TextField authLogin;
    @FXML
    PasswordField authPassword;
    @FXML
    GridPane authPane;
    @FXML
    Label authMessage;

    @FXML
    ProgressBar progressBar;
    @FXML
    Label fileNameMessage;

    @FXML
    VBox load_bar;
    @FXML
    VBox bar;

    @FXML
    Label fileSizeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        this.fileList = new ArrayList<>();
        this.fileChooser = new FileChooser();
        this.fileWorker = new FileWorker();
        createTableView();
       }

    @FXML
    public void clickAddFileButton() {
        File file = fileChooser.showOpenDialog(ClientApp.getStage());
        if (file != null) {
            long sizeStream = getFileList().stream().map(FileInfo::getFilename).filter((p)-> p.equals(file.getName())).count();
            fileWorker.working(file, this::setVisibleLoadInfoFile,sizeStream > 0);
        }
    }

    public void setVisibleLoadInfoFile(boolean check){
        fileNameMessage.setText("Загрузка файла.");
        fileNameMessage.setVisible(check);
        //progressBar.setVisible(check);
    }


    @FXML
    public void clickDeleteButton() {
        String delFileName = tableView.getSelectionModel().getSelectedItem().getFilename();
        if(delFileName != null){
            fileList.remove(delFileName);
            reloadFxFilesList(fileList);
            connection.send(new DelFileRequest(delFileName));
        }
    }


    @FXML
    public void changeStageToAuth() {
        Platform.runLater(() -> {
            authLogin.clear();
            authPassword.clear();
            fileList.clear();
        });
        authPane.setVisible(true);
        authMessage.setVisible(false);
        regPane.setVisible(false);
        cloudPane.setVisible(false);
    }

    public void changeStageToCloud() {
        cloudPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
    }

    @FXML
    public void changeStageToReg() {
        Platform.runLater(() -> {
            regLogin.clear();
            regPassword.clear();
            regPasswordRep.clear();
            regName.clear();
        });
        regPane.setVisible(true);
        regMessage.setVisible(false);
        authPane.setVisible(false);
        cloudPane.setVisible(false);
    }


    public void reloadFxFilesList(List <FileInfo> fileList ){
        tableView.getItems().clear();
        tableView.getItems().addAll(fileList);
        tableView.sort();
    }

    @FXML
    public void enterCloud() throws InterruptedException {
        if (connection == null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            connection = new Network(this, countDownLatch);
            new Thread(connection).start();
            countDownLatch.await();
        }
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        }else{
            String login = authLogin.getText();
            String pass = authPassword.getText();
            AbstractMessage message = new AuthMessage(login, pass);
            connection.send(message);
        }
    }

    @FXML
    public void register() throws InterruptedException {
        if (connection == null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            connection = new Network(this, countDownLatch);
            new Thread(connection).start();
            countDownLatch.await();
        }
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
        } else if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
        } else {
            AbstractMessage message = new RegUserRequest(regName.getText(),  regLogin.getText(), regPassword.getText());
            connection.send(message);
        }
    }


    public void createTableView(){
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        tableView.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        tableView.getSortOrder().add(fileTypeColumn);
    }


    public List<FileInfo> getFileList() {
        return fileList;
    }

    public static Network getConnection() {
        return connection;
    }

    public void setFileList(List<FileInfo> fileList) {
        this.fileList = fileList;
    }
}