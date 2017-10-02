package iad.rmi.chat.imp;

import com.sun.org.apache.xpath.internal.operations.Bool;
import iad.rmi.chat.ChatConference;
import iad.rmi.chat.ChatMessage;
import iad.rmi.chat.ChatParticipant;

import javax.swing.text.html.HTMLDocument;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ChatConferenceI extends UnicastRemoteObject implements ChatConference{
    private String _name;
    private String _description;
    protected HashMap<String, ChatParticipant> _chatParticipantHashMap;
    private Boolean _isStarted;

    public ChatConferenceI(String name, String description) throws RemoteException {
        super();
        this._name = name;
        this._description = description;
        this._chatParticipantHashMap = new HashMap<>();
        this._isStarted = false;
    }

    @Override
    public String getName() throws RemoteException {
        return _name;
    }

    @Override
    public String getDescription() throws RemoteException {
        return _description;
    }

    @Override
    public boolean isStarted() throws RemoteException {
        return _isStarted;
    }

    @Override
    public void addParticipant(ChatParticipant p) throws RemoteException {
        try {
            if (!_chatParticipantHashMap.containsKey(p.getName())) {
                _chatParticipantHashMap.put(p.getName(), p);
                broadcast(new ChatMessage(getName(), p.getName() + " join " + getName()));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeParticipant(ChatParticipant p) throws RemoteException {
        if (_chatParticipantHashMap.containsKey(p.getName())) {
            _chatParticipantHashMap.remove(p.getName());
        }
    }

    @Override
    public String[] participants() throws RemoteException {
        Set<String> nameSet = _chatParticipantHashMap.keySet();
        String[] nameArray = new String[nameSet.size()];
        return nameSet.toArray(nameArray);
    }

//    @Override
//    public void broadcast(ChatMessage message) throws RemoteException {
//        Iterator it = _chatParticipantHashMap.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            ChatParticipant participant = (ChatParticipant)pair.getValue();
//            if (!Objects.equals(participant.getName(), message.getEmitter())) {
//                System.out.println(participant.getName() + " : " + message.getContent());
//                participant.process(message);
//            } else {
//                throw new RemoteException("Username already in channel");
//            }
//            it.remove();
//        }
//    }

    @Override
    public void broadcast(ChatMessage message) throws RemoteException {

        if (isStarted()) {
            Set<String> chatters = _chatParticipantHashMap.keySet();
            for (String chatter :
                    chatters) {
                //Création d'un thread pour transmettre le message à tous les participants en même temps
                Thread transmission = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //envoie du message à tous les participants sauf à l'envoyeur du message
                        try {
                            if (!Objects.equals(message.getEmitter(), _chatParticipantHashMap.get(chatter).getName())) {
                                try{
                                    _chatParticipantHashMap.get(chatter).process(message);
                                    System.out.println("message de " + message.getEmitter() + " : " + message.getContent());
                                } //exception au cas ou un client s'est brutalement déconnecté ou autre problème
                                catch (Exception e){}
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                transmission.start();
            }
        }
    }

    @Override
    public void start() throws RemoteException {
        if (!_isStarted) {
            _isStarted = true;
            broadcast(new ChatMessage(getName(), "Conference started"));
        }
    }

    @Override
    public void stop() throws RemoteException {
        if (_isStarted) {
            _isStarted = false;
            broadcast(new ChatMessage(getName(), "Conference ended"));
        }
    }

    public static void main(String[] args) {
//        System.setProperty("java.rmi.server.hostname", "172.0.0.1");
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            ChatConferenceI conf = new ChatConferenceI("JavaRMI", "Conférence sur comment créer un chat en java");
            System.out.println(conf.getName());
            registry.rebind(conf.getName(), conf);
            conf.start();

            ChatConferenceI conf2 = new ChatConferenceI("Python", "Conférence sur comment coder en python");
            registry.rebind(conf2.getName(), conf2);
            conf2.start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
