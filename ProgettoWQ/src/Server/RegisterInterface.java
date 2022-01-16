package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote{
	public int user_registration(String username, String password) throws RemoteException, NullPointerException;

}
