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

import com.jfoenix.controls.JFXProgressBar;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.slf4j.LoggerFactory;
import sx.ip.api.UserApi;
import sx.ip.api.UserApiImpl;
import static sx.ip.controllers.NavController.bundle;
import sx.ip.utils.ProxyUtils;

/**
 * Login with email screen controller
 */
public class FXMLNewProxyController extends NavController implements Initializable {

    static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FXMLNewProxyController.class);

    @FXML
    Label lblBalance;

    @FXML
    AnchorPane mainAnchorPane;

    @FXML
    JFXProgressBar progressBar;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.retrieveUserBalance();
    }

    private void retrieveUserBalance() {
        Task task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                UserApi api = new UserApiImpl();
                String response = api.retrieveUserBalance();
                return response;
            }
        };
        task.setOnSucceeded((Event ev) -> {
            this.mainAnchorPane.setDisable(false);
            this.progressBar.setVisible(false);
            this.lblBalance.setText((String) task.getValue());

        });
        task.setOnFailed((Event ev) -> {
            this.progressBar.setVisible(false);
            Logger.getLogger(FXMLLoginEmailController.class.getName()).log(Level.SEVERE, null, task.getException());
            ProxyUtils.createExceptionAlert(bundle.getString("key.main.alert.error.auth.title"), null, task.getException().getMessage(), bundle.getString("key.main.dialog.exception.stack.text"), task.getException(), null);
            LOGGER.error(task.getException().getMessage(), task.getException());

        });
        Thread thread = new Thread(task);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Logger.getLogger(FXMLLoginEmailController.class.getName()).log(Level.SEVERE, null, e);
                ProxyUtils.createExceptionAlert(bundle.getString("key.main.alert.error.title"), null, e.getMessage(), bundle.getString("key.main.dialog.exception.stack.text"), e, null);
            }
        });
        this.mainAnchorPane.setDisable(true);
        this.progressBar.setVisible(true);
        thread.start();
    }

    /**
     * Method resposible for handle with the close action.
     *
     * @param event An Event representing that the button has been fired.
     */
    @FXML
    private void handleCloseAction(ActionEvent event) {
        stage.close();
    }

}
