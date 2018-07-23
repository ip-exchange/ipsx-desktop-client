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
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import sx.ip.api.UserApi;
import sx.ip.api.UserApiImpl;
import sx.ip.utils.BlankSpacesValidator;
import sx.ip.utils.EmailValidator;
import sx.ip.utils.ProxyUtils;

/**
 *
 * @author caio
 */
public class FXMLLoginEmailController extends NavController implements Initializable{
    
    
    /** The login with email button instance.  */
    @FXML
    private Hyperlink btnReset;
    
    /** The login button instance.  */
    @FXML
    private JFXButton btnLoginEmail;
    
    /** The close button instance.  */
    @FXML
    private JFXButton btnClose;

    /** The main anchor pane instance.  */
    @FXML
    private AnchorPane mainAnchorPane;
    
    /** The email text field instance.  */
    @FXML
    private JFXTextField userEmail;
    
    /** The password text field instance.  */
    @FXML
    private JFXPasswordField userPass;
    
    /**
    * Method resposible for handling the close action.
    *
    * @param event 
    *          An Event representing that the button has been fired.
    */
    @FXML
    private void handleCloseAction(ActionEvent event) {
        stage.close();
    }
    
    /**
    * Method resposible for the transition to the login with email screen action.
    *
    * @param event
    *          An Event representing that the button has been fired.
    */
    @FXML
    private void loginAction(ActionEvent event) throws IOException{
        UserApi api = new UserApiImpl();
        
        try {
            api.logoutUser();
            boolean response = api.authUser(userEmail.getText().trim(), userPass.getText().trim());
            
            if(response){
                ProxyUtils.createAndShowAlert(Alert.AlertType.INFORMATION, bundle.getString("key.main.alert.info.title"), null, "foi", null);
                //verificação de carteira eth
            }
        } catch (UnirestException ex) {
            Logger.getLogger(FXMLLoginEmailController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void resetPassword(ActionEvent ae) throws IOException{
        //FXMLLoader loader = new FXMLLoader(IPSXDesktopClient.class.getResource("resources/fxml/FXMLResetPassword.fxml"), ProxyUtils.getBundle());
        //loader.setControllerFactory(new HostServicesControllerFactory(app.getHostServices()));
        //NavControllerHandle.navigateTo(loader, stage, app);
    }
    


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        BlankSpacesValidator blankValidatorEmail = new BlankSpacesValidator();
        blankValidatorEmail.setMessage(rb.getString("key.main.validator.empty"));
        
        EmailValidator emailValidator = new EmailValidator();
        emailValidator.setMessage(rb.getString("key.main.validator.email"));
        
        userEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()){
                userEmail.getValidators().add(blankValidatorEmail);
                if(!userEmail.validate()){
                    btnLoginEmail.setDisable(true);
                }else{
                    btnLoginEmail.setDisable(false);
                }
            }else{
                userEmail.getValidators().add(emailValidator);
                if(!userEmail.validate()){
                    btnLoginEmail.setDisable(true);
                }else{
                    btnLoginEmail.setDisable(false);
                }
            }
            
        });
        
    }


}
