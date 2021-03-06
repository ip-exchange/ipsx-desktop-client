/* 
 * Copyright 2018 IP Exchange : https://ip.sx/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sx.ip.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.NumberValidator;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.javafx.PlatformUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.LoggerFactory;
import sx.ip.IPSXDesktopClient;
import sx.ip.api.UserApi;
import sx.ip.api.UserApiImpl;
import static sx.ip.controllers.NavController.bundle;
import sx.ip.models.ProxyType;
import sx.ip.proxies.ProxyManager;
import sx.ip.proxies.ProxySettings;
import sx.ip.utils.BlankSpacesValidator;
import sx.ip.utils.CharValidator;
import sx.ip.utils.ProxyUtils;

/**
 * Main controller for the main application window
 */
public class FXMLManualProxyController extends NavController implements Initializable {
    
    /** The logger Object.  */
    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FXMLManualProxyController.class);
    
    /** The object that provides Host Services for the Application.  */
    private HostServices hostServices;
    
    /** The ProxyManager instance.  */
    private final ProxyManager manager = ProxyManager.getInstance();
    
    /** The ProxySettings instance.  */
    private ProxySettings settings;
    
    /** Simple flag to say if the proxy is active or not instance.  */
    private boolean isActivated = false;

    /** The main anchorPane instance.  */
    @FXML
    private AnchorPane anchorPane;

    /** The close button instance.  */
    @FXML
    private JFXButton btnClose;
    
    /** The activate button instance.  */
    @FXML
    private JFXButton btnActivate;
    
    /** The Protocol combobox instance.  */
    @FXML
    private JFXComboBox<ProxyType> comboProtocol;
    
    /** The agree pane instance.  */
    @FXML
    private AnchorPane agreePane;
    
    /** The proxy pane instance.  */
    @FXML
    private AnchorPane advancedPane;
    
    /** The proxy url pane instance.  */
    @FXML
    private AnchorPane pacPane;
    
    /** The host text field instance.  */
    @FXML
    private JFXTextField proxyIp;
    
    /** The proxy url text field instance.  */
    @FXML
    private JFXTextField proxyUrl;
    
    /** The port text field instance.  */
    @FXML
    private JFXTextField proxyPort;
    
    /** The user text field instance.  */
    @FXML
    private JFXTextField proxyUser;
    
    /** The password text field instance.  */
    @FXML
    private JFXPasswordField proxyPass;
    
    /** The terms button instance.  */
    @FXML
    private Hyperlink btnTerms;
    
    /** The logout button instance.  */
    @FXML
    private Hyperlink btnLogout;
    
    /** The bypass checkbox instance.  */
    @FXML
    private JFXCheckBox bypassCB;
    
    /** The agree terms checkbox instance.  */
    @FXML
    private JFXCheckBox agreeCheckBox;
    
    /** The Advanced Settings checkbox instance.  */
    @FXML
    private JFXCheckBox advancedSettings;
    
    /** The progress bar instance.  */
    @FXML
    private JFXProgressBar progressBar;
    
    /** The restart setting message instance.  */
    @FXML
    private Label restartSettingsMsg;
    
    /** The remove all setting instance.  */
    @FXML
    private Hyperlink removeAll;

    /**
     * Method responsible for set the current hostServices
     *
     * @param hostServices 
     *          The current hostServices
     */
    public FXMLManualProxyController(HostServices hostServices) {
        this.hostServices = hostServices;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //bundle = rb;
        NumberValidator numValidator = new  NumberValidator();        
        
        BlankSpacesValidator validatorIP = new BlankSpacesValidator();
        
        BlankSpacesValidator validatorURL = new BlankSpacesValidator();
        
        CharValidator validatorCharIP = new CharValidator();
        validatorCharIP.setMessage(rb.getString("key.main.validator.char"));
        
        CharValidator validatorCharURL = new CharValidator();
        validatorCharURL.setMessage(rb.getString("key.main.validator.char"));
        
        ObservableList<ProxyType> data
                = FXCollections.observableArrayList(
                        new ProxyType("SOCKS", "SOCKS"),
                        new ProxyType("HTTP & HTTPS", "HTTP_AND_HTTPS"));

        comboProtocol.getItems().addAll(data);
        comboProtocol.getSelectionModel().select(0);
        
        proxyIp.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                proxyIp.getValidators().add(validatorIP);
                if(!proxyIp.validate()){
                    btnActivate.setDisable(true);
                }else{
                    btnActivate.setDisable(false);
                }
            }else{
                proxyIp.getValidators().add(validatorCharIP);
                if(!proxyIp.validate()){
                    btnActivate.setDisable(true);
                }else{
                    btnActivate.setDisable(false);
                }
            }
        });
        
        proxyUrl.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                proxyUrl.getValidators().add(validatorURL);
                if(!proxyUrl.validate()){
                    btnActivate.setDisable(true);
                }else{
                    btnActivate.setDisable(false);
                }
            }else{
                proxyUrl.getValidators().add(validatorCharURL);
                if(!proxyUrl.validate()){
                    btnActivate.setDisable(true);
                }else{
                    btnActivate.setDisable(false);
                }
            }
        });
        
        proxyPort.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        proxyPort.getValidators().add(numValidator);
        
        proxyPort.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.isEmpty()){
                proxyPort.validate();                
            }
        });
    }

    /**
     * Method resposible for handle with the close action.
     *
     * @param event 
     *          An Event representing that the button has been fired.
     */
    @FXML
    private void handleCloseAction(ActionEvent event) {
        stage.close();
    }
    
    /**
     * Method resposible for handle with the Advanced Settings checkbox.
     *
     * @param event 
     *          An Event representing that the button has been fired.
     */
    @FXML
    private void handleAdvancedSettings(ActionEvent event) {
        advancedPane.setVisible(!advancedPane.isVisible());
        pacPane.setVisible(!pacPane.isVisible());
        
        if(advancedPane.isVisible()){
            agreePane.setLayoutY(278);
        }else{
            agreePane.setLayoutY(228);
        }
    }

    /**
     * Method resposible for handle with the activate action.
     *
     * @param event 
     *          An Event representing that the button has been fired.
     */
    @FXML
    private void handleActivateAction(ActionEvent event) {
        String host = advancedPane.isVisible() ? proxyIp.getText().trim() : proxyUrl.getText().trim();
        InputStream is = IPSXDesktopClient.class.getResourceAsStream("resources/imgs/icon.png");
        boolean res = true;
        if (agreeCheckBox.isSelected()) {
            if (advancedPane.isVisible()) {                              
                advancedProxyServer(host);
                    
            } else {
                try { 
                    if(!ProxyUtils.validateUrl(host) && !isActivated){                        
                        res = ProxyUtils.createQuestionPane(is, bundle.getString("key.main.alert.error.title"), bundle.getString("key.main.alert.error.invalid.pac.message"));
                    }
                    
                    if(res){
                        defaultProxyServer(host);                        
                    }
                } catch (IOException ex) {
                    ProxyUtils.createExceptionAlert(bundle.getString("key.main.dialog.exception.title"), null, ex.getMessage(), bundle.getString("key.main.dialog.exception.stack.text"), ex, is);
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        } else {
            ProxyUtils.createAndShowAlert(AlertType.INFORMATION, bundle.getString("key.main.alert.info.title"), null, bundle.getString("key.main.alert.info.message"), is);
            
        }
        btnActivate.setFocusTraversable(true);
    }

    /**
     * Method resposible for the user logout and transition to the sign in screen.
     *
     * @param event An Event representing that the button has been fired.
     */
    @FXML
    private void logoutAction(ActionEvent event) throws IOException {
        UserApi api = new UserApiImpl();
        try {
            if (api.logoutUser()) {
                FXMLLoader loader = new FXMLLoader(IPSXDesktopClient.class.getResource("resources/fxml/FXMLLoginEmail.fxml"), ProxyUtils.getBundle());
                //loader.setControllerFactory(new HostServicesControllerFactory(app.getHostServices()));
                NavControllerHandle.navigateTo(loader, stage, app);
            }
        } catch (UnirestException ex) {
            ProxyUtils.createExceptionAlert(bundle.getString("key.main.dialog.exception.title"), null, ex.getMessage(), bundle.getString("key.main.dialog.exception.stack.text"), ex, null);
            LOGGER.error(ex.getMessage(), ex);
            Logger.getLogger(FXMLManualProxyController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }    
    
    /**
     * Method resposible for open the browser.
     *
     * @param event 
     *          An Event representing that the Button has been fired.
     */
    @FXML
    public void openBrowser(ActionEvent event) {
        hostServices.showDocument(btnTerms.getAccessibleText());
    }

    /**
     * Method resposible for remove all settings action.
     *
     * @param event 
     *          An Event representing that the button has been fired.
     */
    @FXML
    private void removeAllSettings(ActionEvent event){
        InputStream is = IPSXDesktopClient.class.getResourceAsStream("resources/imgs/icon.png");
        if(ProxyUtils.createQuestionRemoveAll(is)){
            settings = ProxySettings.getDirectConnectionSetting();
            isActivated = false;
            handleScene(isActivated);
            startProxyThread();            
        }
    }    

    /**
     * Method responsible for control the display layout.
     *
     * @param activate 
     *          Flag to indicate if the proxy server is active or not
     */
    private void handleScene(boolean activate) {
        if (activate) {
            btnActivate.setText(bundle.getString("key.main.button.deactivate"));
            btnActivate.setStyle("-fx-background-color: #2ecc71;");
        } else {
            btnActivate.setText(bundle.getString("key.main.button.activate"));
            btnActivate.setStyle("-fx-background-color: #2aace0;");
        }
        
        agreePane.setDisable(activate);
        if (advancedPane.isVisible()) {
            comboProtocol.setDisable(activate);
            proxyIp.setDisable(activate);
            proxyPort.setDisable(activate);
            proxyPass.setDisable(activate);
            proxyUser.setDisable(activate);
        } else {
            pacPane.setDisable(activate);
        }
        
        if(!activate){
            resetPanes();
        }
        
        if(PlatformUtil.isWindows()){
            restartSettingsMsg.setText(bundle.getString("key.main.restart.message"));
        }else if(PlatformUtil.isMac()){
            restartSettingsMsg.setText(bundle.getString("key.main.restart.message.mac"));
        }else if(PlatformUtil.isLinux()){
            restartSettingsMsg.setText(bundle.getString("key.main.restart.message"));
        }
        
    }
    
    /**
     * Method responsible for reset the panes for the default state.
     */
    private void resetPanes(){
        proxyIp.setText("");
        proxyPort.setText("");
        proxyUrl.setText("");
        proxyPass.setText("");
        proxyUser.setText("");
        comboProtocol.getSelectionModel().select(0);
        advancedSettings.setSelected(false);
        advancedPane.setVisible(false);
        pacPane.setVisible(true);
        
    }
    
    /**
     * Method responsible for handle with the advanced proxy activation / deactivation.
     * 
     * @param host 
     *          The proxy host
     */
    private void advancedProxyServer(String host) {        
        String type = (comboProtocol.getValue() != null) ? comboProtocol.getValue().getValue() : null;
        Integer port;
        String autheUser = advancedPane.isVisible() ? proxyUser.getText().trim() : null;
        String authPass = advancedPane.isVisible() ? proxyPass.getText().trim() : null;
        InputStream is = IPSXDesktopClient.class.getResourceAsStream("resources/imgs/icon.png");
        if ((host != null && host.length() > 0)) {
            if (proxyPort.getText() != null && proxyPort.getText().trim().length() > 0) {
                port = Integer.valueOf(proxyPort.getText());
            
                if (type != null && port != null) {

                    if (!isActivated) {
                        settings = new ProxySettings(host, port, ProxySettings.ProxyType.valueOf(type), null, bypassCB.isSelected(), autheUser, authPass);
                        isActivated = true;                        
                    } else {
                        settings = ProxySettings.getDirectConnectionSetting();
                        isActivated = false;                        
                    }
                    
                    handleScene(isActivated);
                    startProxyThread();
                } else {
                    ProxyUtils.createAndShowAlert(AlertType.WARNING, bundle.getString("key.main.alert.warning.title"), null, bundle.getString("key.main.alert.warning.message.v1"), is);
                }
            }else{
                ProxyUtils.createAndShowAlert(AlertType.WARNING, bundle.getString("key.main.alert.warning.title"), null, bundle.getString("key.main.alert.warning.message.v3"), is);
            }
        } else {
            ProxyUtils.createAndShowAlert(AlertType.WARNING, bundle.getString("key.main.alert.warning.title"), null, bundle.getString("key.main.alert.warning.message.v1"), is);
        }
    }
    
    
    /**
     * Method responsible for handle with the default proxy activation / deactivation.
     * 
     * @param acs 
     *          The proxy Automatic Configuration Script
     */
    private void defaultProxyServer(String acs) {        
        String autheUser = advancedPane.isVisible() ? proxyUser.getText().trim() : null;
        String authPass = advancedPane.isVisible() ? proxyPass.getText().trim() : null;
        InputStream is = IPSXDesktopClient.class.getResourceAsStream("resources/imgs/icon.png");
        if ((acs != null && acs.length() > 0)) {

            if (!isActivated) {
                settings = new ProxySettings("", null, null, acs, false, autheUser, authPass);
                isActivated = true;
                
            } else {
                settings = ProxySettings.getDirectConnectionSetting();
                isActivated = false;
            }
            
            handleScene(isActivated);
            startProxyThread();

        } else {
            ProxyUtils.createAndShowAlert(AlertType.WARNING, bundle.getString("key.main.alert.warning.title"), null, bundle.getString("key.main.alert.warning.message.v2"), is);
        }
    }
    
    /**
     * Method responsible for handle with the proxy activation / deactivation
     * thread.
     */
    private void startProxyThread() {
        InputStream is = IPSXDesktopClient.class.getResourceAsStream("resources/imgs/icon.png");
        Task task = new Task<Void>() {
            @Override
            public Void call() throws ProxyManager.ProxySetupException {
                restartSettingsMsg.setVisible(false);  
                progressBar.setVisible(true);
                btnActivate.setDisable(true);
                removeAll.setDisable(true);
                manager.setProxySettings(settings);                
                return null;
            }

            @Override
            protected void done() {
                super.done();
                updateProgress(100, 100);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    Logger.getLogger(FXMLManualProxyController.class.getName()).log(Level.SEVERE, null, ex);
                }
                progressBar.setVisible(false);
                
                restartSettingsMsg.setVisible(isActivated);               
                removeAll.setDisable(false);
                if(isActivated){                    
                    btnActivate.setDisable(false); 
                }
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ProxyUtils.createExceptionAlert(bundle.getString("key.main.dialog.exception.title"), null, task.getException().getMessage(), bundle.getString("key.main.dialog.exception.stack.text"), (Exception) task.getException(), is);
                LOGGER.error(task.getException().getMessage(), task.getException());
            }
        });
        new Thread(task).start();
    }
    
    
}
