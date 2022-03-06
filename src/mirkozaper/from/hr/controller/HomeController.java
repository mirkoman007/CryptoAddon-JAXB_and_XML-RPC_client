/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mirkozaper.from.hr.controller;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import mirkozaper.from.hr.jaxb.ArrayOfCoin;
import mirkozaper.from.hr.jaxb.ArrayOfCoin.Coin;
import mirkozaper.from.hr.soap.CryptoService;
import mirkozaper.from.hr.soap.CryptoServiceSoap;
import mirkozaper.from.hr.soap.SearchResponse;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author mirko
 */
public class HomeController implements Initializable {

    @FXML
    private Label lblTempResult;
    @FXML
    private ComboBox<String> cbCities;
    @FXML
    private Button btnCheckWeather;
    @FXML
    private TextField tfSearchValue;
    @FXML
    private Button btnSearch;
    @FXML
    private TableView<Coin> tvCoins;
    @FXML
    private Label lblJaxbError;
    @FXML
    private Label lblXmlRpcError;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbCities.getItems().addAll(GetCities());
        
        tfSearchValue.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!newValue.matches("\\d*")){
                    tfSearchValue.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        
        TableColumn<Coin,String> column1=new TableColumn<>("Name");
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Coin,String> column2=new TableColumn<>("Code");
        column2.setCellValueFactory(new PropertyValueFactory<>("code"));
        
        TableColumn<Coin,String> column3=new TableColumn<>("Circulating supply");
        column3.setCellValueFactory(new PropertyValueFactory<>("circulatingSupply"));
        
        tvCoins.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tvCoins.getColumns().add(column1);
        tvCoins.getColumns().add(column2);
        tvCoins.getColumns().add(column3);
        
    }

    @FXML
    private void CheckWeather(ActionEvent event) {
        lblXmlRpcError.setText("");
        
        try {
            XmlRpcClientConfigImpl configClient = new XmlRpcClientConfigImpl();
            configClient.setServerURL(new URL("http://localhost:8080/"));
            configClient.setEnabledForExtensions(true);

            XmlRpcClient klijent=new XmlRpcClient();
            klijent.setConfig(configClient);

            Object[] parametri=new Object[]{cbCities.getValue()};
            String rez = String.valueOf(klijent.execute("Temperature.getTemperature",parametri));
            
            rez+="°C";
            lblTempResult.setText(rez);
            
        } catch (Exception ex) {
            lblXmlRpcError.setText("Check your internet connection");
            lblTempResult.setText("");
        }
    }
    
    @FXML
    private void Search(ActionEvent event) {
        lblJaxbError.setText("");
        try {
            CryptoService cs =new CryptoService();
            CryptoServiceSoap cryptoServiceSoap = cs.getCryptoServiceSoap();
            SearchResponse.SearchResult search = cryptoServiceSoap.search(tfSearchValue.getText());
            
            ElementNSImpl eleNSImplObject = (ElementNSImpl)search.getContent().get(0);
            
            JAXBContext jc = JAXBContext.newInstance(ArrayOfCoin.class);
            Unmarshaller unmar = jc.createUnmarshaller();
            ArrayOfCoin arrayOfCoin = (ArrayOfCoin)unmar.unmarshal(eleNSImplObject);
            List<Coin> coins = arrayOfCoin.getCoin();
            
            
            tvCoins.getItems().clear();
            
            coins.forEach(c->{
                tvCoins.getItems().add(c);
            });
                        
        } catch (Exception ex) {
            lblJaxbError.setText("Check your internet connection");
            tvCoins.getItems().clear();
        }
    }

    private List<String> GetCities() {
        List<String> cities=new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new URL("https://vrijeme.hr/hrvatska_n.xml").openStream());
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Grad");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    String city = e.getElementsByTagName("GradIme").item(0).getTextContent();
                    cities.add(city);
                }
            }

        } catch (IOException | ParserConfigurationException | DOMException | SAXException e) {
            e.printStackTrace();
        }
        return cities;
    }
}
