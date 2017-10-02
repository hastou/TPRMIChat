package iad.rmi.chat.imp;

import com.sun.corba.se.impl.orbutil.threadpool.WorkQueueImpl;
import iad.rmi.chat.ChatConference;
import iad.rmi.chat.ChatMessage;
import iad.rmi.chat.ChatParticipant;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class ChatParticipantI extends UnicastRemoteObject implements ChatParticipant {
    private ChatConference _actualConference;
    private String _name;
    private Queue<ChatMessage> _messages;

    public ChatParticipantI(String name) throws RemoteException {
        super();
        _actualConference = null;
        _name = name;
        _messages = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean join(ChatConference conference) throws RemoteException {
        if (!isConnected()) {
            try {
                conference.addParticipant(this);
                _actualConference = conference;
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public void leave(ChatConference conference) throws RemoteException {
        if (isConnected()) {
            conference.removeParticipant(this);
            _actualConference = null;
        }
    }

    @Override
    public void send(String txt) throws RemoteException {
        if (isConnected()) {
            _actualConference.broadcast(new ChatMessage(getName(), txt));
        } else {
            throw new RemoteException("User not connected");
        }
    }

    @Override
    public void process(ChatMessage msg) throws RemoteException {
        if (isConnected()) {
            _messages.add(msg);
        }
    }

    @Override
    public boolean isConnected() throws RemoteException {
        return _actualConference != null;
    }

    @Override
    public String getName() throws RemoteException {
        return _name;
    }

    @Override
    public ChatMessage next() throws RemoteException {
        return _messages.poll();
    }
}
