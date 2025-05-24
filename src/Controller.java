import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.sqlite.JDBC;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

public class Controller extends Component  {

    @FXML
    private Button button1, button2, button3, button5, button7;

    @FXML
    private TableView<ObservableList> TableView1;

    @FXML
    private Label label1, label2, label16;

    @FXML
    private TextField textField1, textField2, textField9;

    private ObservableList<ObservableList> data;

    public String SQL = "SELECT * FROM satellite";

    private String nameKA;
    private String freq;
    private String pol;
    private String symbol_rate;
    private String umreal;
    private String comment;
    private double pt;

// Константа, в которой хранится адрес подключения
    private String CON_STR = "jdbc:sqlite:D:/database_sat.db";
    // Объект, в котором будет храниться соединение с БД
    private Connection connection;
    private  static final String CURRENTDIRECTORY = "user.dir";

    private String pathToDatabase;

    @FXML
    void initialize() {

// Проверка существования каталога database
        File theDir0 = new File(System.getProperty(CURRENTDIRECTORY),"database");
        if (!theDir0.exists())
            new File(System.getProperty(CURRENTDIRECTORY), "database").mkdir();

// Проверка существования всех нужных файлов в папке database
        File theDir1 = new File(System.getProperty(CURRENTDIRECTORY),"database/database_sat.db");
        File theDir2 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.csv");
        File theDir3 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.xlsx");
        File theDir4 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");

        if (!theDir1.exists())
            createFile1();
        if (!theDir2.exists())
            createFile2();
        if (!theDir3.exists())
            createFile3();
        if (!theDir4.exists())
            createFile4();

// Проверим есть ли файл setting.properties  в папке database
        checkingFile();

// Загружаем  setting.properties из папки database
        File theDir = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(theDir));
        } catch (IOException e) {
            e.printStackTrace();
        }

// Получить значения nameKA, ПТ, широты - lat и долготы - lon из database/setting.properties"
        nameKA = appProps.getProperty("nameKA", "Express AM7");
        pt = Double.parseDouble(appProps.getProperty("pt", "40.0"));

// Установить их в textField
        textField1.setText(String.valueOf(nameKA));
        pathToDatabase = theDir1.getPath();
        CON_STR = "jdbc:sqlite:" + pathToDatabase;

// активные поля в зависимости от радиокнопки
        textField1.setEditable(true);
        textField1.setDisable(false);

//Нажатие на кнопку 1 - Вывести все частоты из БД в таблицу - начало
        button1.setOnAction(event -> {
            data = FXCollections.observableArrayList();
            TableView1.getItems().clear();
            TableView1.getColumns().clear();
            TableView1.refresh();
            SQL = "SELECT * FROM satellite";
            System.out.println("SQL = " + SQL);
            label1.setText("SQL = " + SQL);
            textField1.setText("");
            sql();
//            select();
        });
//Нажатие на кнопку 1 - Вывести все данные из БД в таблицу - конец


//Нажатие на кнопку 2 - Очистить таблицу - начало
        button2.setOnAction(event -> {
            TableView1.getItems().clear();
            TableView1.getColumns().clear();
            TableView1.refresh();
            textField1.setText("");
            textField9.setText("");
            label1.setText("");
        });
//Нажатие на кнопку 2 - Очистить таблицу - конец


//Нажатие на кнопку 3 - Занести частоту, поляризацию и подспутниковую точку в Базу данных - начало
        button3.setOnAction(event -> {
// Проверка за заполненность полей Имя КА, частота, поляризация
                    if (textField1.getText().length() == 0) {

                    }else if (textField9.getText().length() == 0){
                        label16.setTextFill(Color.web("#FF0000"));
                        label16.setText("COMMENT *");
                    }else {
                        label16.setTextFill(Color.web("#000000"));
                        label16.setText("COMMENT *");
                        nameKA = textField1.getText().trim().replace(",", ".");
                        comment = textField9.getText().trim().replace(",", ".");
                        SQL = "UPDATE satellite SET FREQ = " + freq + ", PT = '" + pt + "'" + ", SYMBOL_RATE = '" + symbol_rate + "'" + ", COMMENT = '" + comment + "'" + " WHERE FREQ = '" + freq + "'"; // OK
                        sql2();
                        label1.setText("SQL = " + SQL);
                    }
        });
//Нажатие на кнопку 3 - Занести частоту, поляризацию и подспутниковую точку в Базу данных - конец

//Нажатие на кнопку 5 - Описание программы - начало
        button5.setOnAction(event -> {
// Справка по программе через поток - Potok1
            //Potok1.main();
        });
//Нажатие на кнопку 5 - Описание программы  - конец

//Нажатие на кнопку 7 - Найти - начало
        button7.setOnAction(event -> {

            nameKA = textField1.getText().trim();
            SQL = "SELECT * FROM satellite WHERE FREQ like '%" + nameKA + "%'";
            TableView1.getItems().clear();
            TableView1.getColumns().clear();
            TableView1.refresh();
            label1.setText("SQL = " + SQL);
            sql();

        });
//Нажатие на кнопку  7 - Найти - конец

// Этот метод нужен если не была нажата кнопка "Вывести все данные из Базы данных в таблицу" а сразу введен КА и нажата кнопка "Найти и рассчитать АЗ и УМ"
        select();
    }

// Метод сохранения в properties
    void saveToPropertiesSetting() {
// Загружаем  setting.properties из папки database
        File theDir17 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        Properties appProps = new Properties();
        try {
            appProps.load(new BufferedReader(new InputStreamReader(new FileInputStream(theDir17), "UTF-8")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        nameKA = textField1.getText();

        appProps.setProperty("nameKA", String.valueOf(nameKA));
        appProps.setProperty("pt", String.valueOf(pt));

// Сохраним в setting.properties внесенные изменения из текстовых полей
        String newAppProps = "database/setting.properties";
        try {
            appProps.store(new FileWriter(newAppProps), "store");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Округление чисел: Метод Math.round()
// https://www.internet-technologies.ru/articles/kak-v-java-okruglit-chislo-do-n-znakov-posle-zapyatoy.html

    public static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public void select(){
// Получить выбранное значение - одну строку
// https://metanit.com/java/javafx/4.13.php
        TableView.TableViewSelectionModel<ObservableList> selectionModel = TableView1.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new ChangeListener<ObservableList>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList> observable, ObservableList oldValue, ObservableList newValue) {
// Проверка newValue на null, если нулевое, то ничего не делаем (иначе выскакивала ошибка)
                    if (newValue == null) {
                        // do something
                        //System.out.println("newValue = " + newValue);
                    }else {
                        label1.setText("Selected: " + newValue);
// Получаем строку со всеми значениями, разделенными запятыми
                        String text = newValue.toString();
                        //System.out.println("text = " + text);
// Выбранную строку со всеми значениями занесем в массив строк, разделенными запятыми
                        String[] words = text.split(",");
// Выберем только нужные значения, т.е. столбцы и выведем их в textField1 в зависимости от того какая была нажата кнопка и соответсвенно было значение переменной valueSelect
                        for (int i = 0; i < words.length; i++) {
                            System.out.println("i = " +i + " words[i = ]" + words[i]);
                            pt = Double.parseDouble(words[1].trim());
                            textField1.setText(words[2].trim()); // FREQ
                            textField9.setText(words[4].replace(']', ' ').trim()); // Комментарий
                            saveToPropertiesSetting();
                    }

                }
            }
        });
    }

    public void sql (){
        data = FXCollections.observableArrayList();

        try {
            DriverManager.registerDriver(new JDBC());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
// Выполняем подключение к базе данных
        try {
            this.connection = DriverManager.getConnection(CON_STR);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

// Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            ResultSet rs = statement.executeQuery(SQL);
            //System.out.println("rs = " + rs);
// Пример заполнения TableView из БД
// https://github.com/seifallah/Dynamic-TableView--Java-Fx-2.0-/blob/master/DynamicTable.java
            for(int i=0 ; i<rs.getMetaData().getColumnCount(); i++){
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i+1));
                //System.out.println("col = " + col);
                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                TableView1.getColumns().addAll(col);
            }

            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

//FINALLY ADDED TO TableView
            TableView1.setItems(data);
            connection.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

// Запрос в БД на внесение частоты и поляризации
    public void sql2 (){

//условие: пока не введены часта и поляризация расчет не будет производиться
        if (textField1.getText().length() == 0 ) {

        } else {

            data = FXCollections.observableArrayList();
            try {
                DriverManager.registerDriver(new JDBC());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
// Выполняем подключение к базе данных
            try {
                this.connection = DriverManager.getConnection(CON_STR);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

// Statement используется для того, чтобы выполнить sql-запрос
            try (Statement statement = this.connection.createStatement()) {
                statement.executeUpdate(SQL);
                connection.close();
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Error on Building Data");
            }
        }
    }

// Метод создания файла database_sat.db из папки с ресурсами в рабочую папку с программой
    void createFile1(){
        File file1 = null;
        String resource = "/database_sat.db";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("sat", ".db");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

// OK
        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/database_sat.db");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile2(){
        File file3 = null;
        String resource = "/satellite.csv";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file3 = File.createTempFile("satellite", ".csv");
                OutputStream out = new FileOutputStream(file3);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file3.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file3 = new File(res.getFile());
        }

        if (file3 != null && !file3.exists()) {
            throw new RuntimeException("Error: File " + file3 + " not found!");
        }

        File file4 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.csv");
        try {
            copyFileUsingStream(file3, file4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile3(){
        File file1 = null;
        String resource = "/satellite.xlsx";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("satellite", ".xlsx");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/satellite.xlsx");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void createFile4(){
        File file1 = null;
        String resource = "/setting.properties";
        URL res = getClass().getResource(resource);
        if (res.getProtocol().equals("jar")) {
            try {
                InputStream input = getClass().getResourceAsStream(resource);
                file1 = File.createTempFile("setting", ".properties");
                OutputStream out = new FileOutputStream(file1);
                int read;
                byte[] bytes = new byte[1024];

                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.close();
                file1.deleteOnExit();
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
                System.out.println("Exceptions.printStackTrace(ex);");
            }
        } else {
            file1 = new File(res.getFile());
        }

        if (file1 != null && !file1.exists()) {
            throw new RuntimeException("Error: File " + file1 + " not found!");
        }

        File file2 = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        try {
            copyFileUsingStream(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// Проверка существования файла setting.properties
    void checkingFile(){
        File theDir = new File(System.getProperty(CURRENTDIRECTORY),"database/setting.properties");
        if (!theDir.exists())
            createFileAppProperties();
    }

// Метод создания файла setting.properties из папки с ресурсами в рабочую папку с программой
    void createFileAppProperties(){
// Создаем файл setting.properties в папке с программой
        File dest = new File(System.getProperty(CURRENTDIRECTORY),"setting.properties");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("setting.properties");
             OutputStream out = new FileOutputStream(dest)) {
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
        }
        catch (IOException exc) {
            exc.printStackTrace();
        }
    }

 // Как скопировать файл в Java? 4 способа — примеры и код
// Способ 1: Используем потоки для копирования файла
// https://javadevblog.com/kak-skopirovat-fajl-v-java-4-sposoba-primery-i-kod.html
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}







