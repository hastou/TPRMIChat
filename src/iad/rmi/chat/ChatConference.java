package iad.rmi.chat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatConference extends Remote {
	
	public String getName() throws RemoteException;

	public String getDescription() throws RemoteException;
	
	public boolean isStarted() throws RemoteException;

	public void addParticipant(ChatParticipant p) throws RemoteException;

	public void removeParticipant(ChatParticipant p) throws RemoteException;

	public String[] participants() throws RemoteException;

	public void broadcast(ChatMessage message) throws RemoteException;

	public void start() throws RemoteException;

	public void stop() throws RemoteException;

}