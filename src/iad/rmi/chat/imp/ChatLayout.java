package iad.rmi.chat.imp;

import iad.rmi.chat.ChatConference;
import iad.rmi.chat.ChatMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChatLayout extends BorderPane implements Initializable, EventHandler {

    @FXML
    private Button connectButton;
    @FXML
    private Label logLabel;

    @FXML
    private ListView<String> messagesListView;
    private ObservableList<String> messagesList;

    @FXML
    private ListView<String> conferenceListView;
    private ObservableList<String> conferenceList;

    @FXML
    private Label titleLabel;

    @FXML
    private Button sendMessageButton;

    @FXML
    private TextField messageInput;

    @FXML
    private Button refreshConferencesButton;

    private String selectedConference;

    private ChatConference chatConference;

    private Registry registry;

    private ChatParticipantI chatParticipantI;

    @FXML
    private Button fetchButton;

    private Thread messageGetter;
    private boolean _bRun;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conferenceList = FXCollections.observableArrayList();
        conferenceListView.setItems(conferenceList);
        messagesList = FXCollections.observableArrayList();
        messagesListView.setItems(messagesList);
        chatConference = null;
        _bRun = true;
        selectedConference = null;
        try {
            chatParticipantI = new ChatParticipantI("Bob l'Eponge");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        getConferences();
    }

    public void getConferences() {
        connectButton.setDisable(true);
        try {
            registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
            if (registry == null) {
                throw new RemoteException();
            }
            String remoteRefs[];
            conferenceList.clear();
            remoteRefs = registry.list();
            if (remoteRefs != null) {
                conferenceList.addAll(Arrays.asList(remoteRefs));
                logLabel.setText("Conferences listed");
            } else {
                logLabel.setText("No conferences");
            }
        } catch (RemoteException e) {
            logLabel.setText("Can't connect to server");
        }
    }

    private void onConferenceSelected() {
        selectedConference = conferenceListView.getSelectionModel().getSelectedItem();
        if (selectedConference != null) {
            logLabel.setText(selectedConference + " selected");
            connectButton.setDisable(false);
        }
    }

    private void connectToSelectedConference() {
        if (selectedConference != null) {
            try {
                chatConference = (ChatConference) registry.lookup(selectedConference);
                boolean success = chatParticipantI.join(chatConference);

                if (success) {
                    System.out.println(chatConference.getName());
                    System.out.println(chatConference.getDescription());
                    logLabel.setText("Successfully connected to " + chatConference.getName());
                    titleLabel.setText(chatConference.getName());
                    connectButton.setText("Disconnect");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handle(Event event) {
        if (event.getSource() == refreshConferencesButton) {
            logLabel.setText("Refreshing...");
            getConferences();
        } else if (event.getSource() == connectButton) {
            if (Objects.equals(connectButton.getText(), "Connect")) {
                logLabel.setText("Connecting ...");
                connectToSelectedConference();
            } else {
                logLabel.setText("Disconnected");
                disconnectFromConf();
            }
        } else if (event.getSource() == conferenceListView) {
            onConferenceSelected();
        } else if (event.getSource() == sendMessageButton) {
            sendMessage();
        } else if (event.getSource() == fetchButton) {
            ChatMessage m = null;
            do {
                try {
                    m = chatParticipantI.next();
                    if (m != null) {
                        messagesList.add(m.getEmitter() + " : " + m.getContent());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } while (m != null);
        }
    }

    private void sendMessage() {
        if (!Objects.equals(messageInput.getText(), "")) {
            try {
                chatParticipantI.send(messageInput.getText());
                messagesList.add(chatParticipantI.getName() + " : " + messageInput.getText());
                messageInput.setText("");
            } catch (RemoteException e) {
                logLabel.setText("Connect to conference before sending message");
            }
        }
    }

    private void disconnectFromConf() {
        try {
            if (chatParticipantI.isConnected() && chatConference != null) {
                chatParticipantI.leave(chatConference);
                titleLabel.setText("");
                messagesList.clear();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        connectButton.setText("Connect");
    }

    public void stop() {
        disconnectFromConf();
        _bRun = false;
    }

}
