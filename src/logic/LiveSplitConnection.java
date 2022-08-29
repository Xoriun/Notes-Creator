package logic;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;

import settings.SpeedrunSettings;

public class LiveSplitConnection
{
	private static ServerSocket serverSocket;
	private static Socket socket;
	private static Scanner APIScanner;
	private static BufferedOutputStream APIOutputStream;
	private static boolean awaitConnection = false;
	private static boolean listenToAPI = false;
	
	private static Thread serverSocketThread;
	private static Thread APIListenerThread;
	
	public static void initializeLiveSplitconnection()
	{
		serverSocketThread = new Thread("ServerSocket-Thread")
		{
			@Override
			public void run()
			{
				try
				{
					serverSocket = new ServerSocket(8888);
					serverSocket.setSoTimeout(2000);
					awaitConnection = true;
					while (awaitConnection)
					{
						try {
							socket = serverSocket.accept();
							awaitConnection = false;
						} catch (SocketTimeoutException e) {
							System.out.println("SocketTimeoutconnection while waiting for connection. Trying again!");
						}
					}
					
					listenToAPI = true;
					APIScanner = new Scanner(new InputStreamReader(socket.getInputStream() ) );
					APIOutputStream = new BufferedOutputStream(socket.getOutputStream() );
					APIListenerThread = new Thread("APIScanner-Thread")
					{
						@Override
						public void run()
						{
							while(listenToAPI)
							{
								try
								{
									parseLiveSplitMessage(APIScanner.nextLine() );
								}
								catch (NoSuchElementException e)
								{
									System.out.println("Error while ServerSocket was waiting for connection. Maybe the connection was interrupted?");
								}
							}
						}
					};
					APIListenerThread.start();
				} catch (SocketException e) {
					System.out.println("ServertSocket closed while waiting for client to connect!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		serverSocketThread.start();
	}
	
	private static void parseLiveSplitMessage(String message)
	{
		if (message != null)
		{
			switch (message)
			{
				case "start":
					SpeedrunSettings.start();
					break;
				case "split":
					SpeedrunSettings.split();
					break;
				case "undoSplit":
					SpeedrunSettings.undoSplit();
					break;
				case "skipSplit":
					SpeedrunSettings.skip();
					break;
				case "reset":
					SpeedrunSettings.reset();
					break;
				case "closeConnection":
					listenToAPI = false;
					writeToOutputStream("closedConnection");
					APIScanner.close();
					try
					{
						socket.close();
						serverSocket.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					initializeLiveSplitconnection();
					break;
				case "closedConnection":
					listenToAPI = false;
					APIScanner.close();
					try
					{
						socket.close();
						serverSocket.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		}
	}
	
	private static void writeToOutputStream(String message)
	{
		if (APIOutputStream != null && !serverSocketThread.isAlive() )
			try
			{
				APIOutputStream.write( (message + '\n').getBytes(Charset.defaultCharset() ) );
				APIOutputStream.flush();
			} catch (SocketException e)
			{
				System.out.println("Socket error while writing to output stream. Message was \"" + message + "\"");
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		else
			System.out.println("No connection has been established, could not write to stream!");
	}
	
	public static void startLiveSplitCommunication()
	{
		writeToOutputStream("startActionCommunication");
	}
	
	public static void holdLiveSplitCommunication()
	{
		writeToOutputStream("holdActionCommunication");
	}
	
	public static void shotDownAPI()
	{
		if (!awaitConnection)
			writeToOutputStream("closeConnection");
		else
			try
			{
				serverSocket.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
	}
	
	public static boolean readyToExit()
	{
		if (serverSocketThread != null && serverSocketThread.isAlive() )
			return false;
		if (APIListenerThread != null && APIListenerThread.isAlive() )
			return false;
		return true;
	}
}