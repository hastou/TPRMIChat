package iad.rmi.chat;

import iad.rmi.chat.imp.ChatConferenceI;
import iad.rmi.chat.imp.ChatParticipantI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChatClientConsole extends Thread {
	
	protected ChatConference conference;
	protected ChatParticipant participant;
	protected BufferedReader reader;
	
	public ChatClientConsole(ChatConference c, ChatParticipant p) {
		conference = c;
		participant = p;
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() {
		String line; 
		
	    while(true) {
	       try {
				printNextMessage();	
			    line = reader.readLine();
			    if (line.equals("stop")) {
			    	participant.leave(conference);
					reader.close(); 
					System.exit(0);
			    } else {
			    	participant.send(line);
			    }
		   } catch (Exception e) {
				e.printStackTrace();
		   }
	    }
	}

	private void printNextMessage() throws IOException {
		while (! reader.ready()) {
			   ChatMessage msg = null;
		        try {
					msg = participant.next();
		    	}
		    	catch(RemoteException e) {
		    		e.printStackTrace();
		    	}
				if (msg != null) {
					System.out.println(msg.getEmitter() + " -> "  + msg.getContent());
				}
		}
	}

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(args[0], 1099);
//            ChatConference conf = (ChatConference) registry.lookup("conf1");
            String remoteRefs[];
            remoteRefs = registry.list();
//            for (String ref : remoteRefs) {
//                System.out.println(ref);
//            }
            ChatConference conf = (ChatConference) registry.lookup(registry.list()[0]);
			ChatParticipant participant = new ChatParticipantI("Quentin");
            System.out.println(conf.getName());
            System.out.println(conf.getDescription());

			participant.join(conf);



			Thread.sleep(3000);

            System.out.println("Leaving ...");

			participant.leave(conf);
            System.exit(0);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

