package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Server di WQ
 * @author selenegerali
 *
 */
public class Server {

	private static ThreadPoolExecutor pool;

	private static LinkedBlockingQueue<Runnable> Queue;

	public static void main(String[] args) {
		
		
		DatabaseOperation dbop= new DatabaseOperation();
		try {
			//creazione di un'istanza dell'oggetto remoto
			RegisterInterface stub=(RegisterInterface) UnicastRemoteObject.exportObject(dbop,0);
			
			//Creazione di un registry sulla porta 30000
			LocateRegistry.createRegistry(30000);
			Registry r=LocateRegistry.getRegistry(30000);
			
			//pubblicazione dello stub nel registry
			r.rebind("REGISTER_SERVER", stub);
		}
		
		catch(RemoteException e) {
			e.printStackTrace();
		}
		
		
		Queue=new LinkedBlockingQueue<Runnable>();
		pool = new ThreadPoolExecutor(50, 100, 320000, TimeUnit.MILLISECONDS, Queue);
		ServerSocket servsock;
		try {
			//creo e connetto la socket
			servsock=new ServerSocket(5789);
			while(true) {
				//il server si mette in ascolto 
				Socket sk=servsock.accept();
				//creo il thread che si occupa del singolo client
				ThreadTask thtask= new ThreadTask(dbop,sk);
				pool.execute(thtask);
			}
			
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}

	}

}
