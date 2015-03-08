package com.flutterbee.biplanes;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ConnectionManager {

	ArrayList<String> myIpAddresses;
	String opponentIpAddress;
	Integer tcp_port = 9030;
	Integer udp_port = 9031;

	public MainMenuScreen mainMenuScreen;
	public GameScreen gameScreen;

	String CONNECT_INIT = "connect_init";
	String CONNECT_SUCCESS = "connect_success";

	Boolean connected;
	Boolean tryConnecting;

	Server server;
	Client client;

	Integer refresh_time = 100;

	public ConnectionManager(MainMenuScreen mainMenuScreen) {
		this.connected = false;
		this.tryConnecting = false;
		this.mainMenuScreen = mainMenuScreen;
		findMyIps();
	}

	public void findMyIps() {
		myIpAddresses = new ArrayList<String>();
		List<String> addresses = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface ni : Collections.list(interfaces)) {
				for (InetAddress address : Collections.list(ni
						.getInetAddresses())) {
					if (address instanceof Inet4Address) {
						addresses.add(address.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {

		}
		// Print the contents of our array to a string. Yeah, should have used
		// StringBuilder
		for (String str : addresses) {
			if (!str.equals("127.0.0.1"))
				this.myIpAddresses.add(str);
		}
	}

	public Boolean isServer() {
		if (server != null) {
			return true;
		} else if (client != null) {
			return false;
		} else {
			return false;
		}
	}

	public Boolean isConnected() {
		return this.connected;
	}

	public void connectionEstablished() {
		this.connected = true;
		this.tryConnecting = false;
		System.out.println("Connected to : " + this.opponentIpAddress);
	}

	public void connectionClosed() {
		this.connected = false;
		System.out.println("Connection Closed!");
	}

	public void init_server() {

		server = new Server();
		Kryo kryo = server.getKryo();
		kryo.register(Request.class);
		kryo.register(Response.class);

		server.start();
		try {
			server.bind(tcp_port, udp_port);

			System.out.println("started server ");
			server.addListener(new Listener() {
				public void received(Connection connection, Object object) {
					if (object instanceof Request) {
						Request request = (Request) object;
						if (!ConnectionManager.this.isConnected()
								&& request.text
										.equals(ConnectionManager.this.CONNECT_INIT)) {
							Response response = new Response();
							response.text = ConnectionManager.this.CONNECT_SUCCESS;
							connection.sendTCP(response);

							ConnectionManager.this.opponentIpAddress = connection
									.getRemoteAddressTCP().getAddress()
									.toString();
							ConnectionManager.this.connectionEstablished();
							ConnectionManager.this.mainMenuScreen.startGame();
						} else if (ConnectionManager.this.isConnected()) {
							// data packets
							ConnectionManager.this.gameScreen
									.setOpponentData(request.text);
							String data = ConnectionManager.this.gameScreen
									.getData();
							System.out.println("Received Data : " + data);
							Response response = new Response();
							response.text = data;
							connection.sendTCP(response);

						} else {
							// drop
						}
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void init_client(String serverIp) {
		client = new Client();
		Kryo kryo = client.getKryo();
		kryo.register(Request.class);
		kryo.register(Response.class);

		System.out.println("started client ");

		client.start();

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Response) {
					Response response = (Response) object;
					if (!ConnectionManager.this.isConnected()
							&& response.text
									.equals(ConnectionManager.this.CONNECT_SUCCESS)) {
						// connection successful
						ConnectionManager.this.opponentIpAddress = connection
								.getRemoteAddressTCP().getAddress().toString();
						ConnectionManager.this.connectionEstablished();

						ConnectionManager.this.startSendingData();
						Gdx.app.postRunnable(new Runnable() {

							@Override
							public void run() {
								ConnectionManager.this.mainMenuScreen
										.startGame();
							}
						});
					} else if (ConnectionManager.this.isConnected()) {
						// data packets
						System.out.println("Received Data : " + response.text);
						ConnectionManager.this.gameScreen
								.setOpponentData(response.text);
					} else {
						// what the hell
					}
				}
			}
		});

		ConnectionManager.this.tryConnecting = true;
		Thread thread = new ConnectThread(ConnectionManager.this, serverIp);
		thread.start();

	}

	public class ConnectThread extends Thread {
		String serverIp;
		ConnectionManager connectionManager;

		public void run() {
			while (this.connectionManager.tryConnecting) {
				System.out.println("Trying to connect to : " + this.serverIp);
				try {
					sleep(1000);

					client.connect(5000, this.serverIp,
							this.connectionManager.tcp_port,
							this.connectionManager.udp_port);

					client.setKeepAliveTCP(NORM_PRIORITY);

					Request request = new Request();
					request.text = this.connectionManager.CONNECT_INIT;
					client.sendTCP(request);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public ConnectThread(ConnectionManager connectionMananger,
				String serverIp) {
			this.connectionManager = connectionMananger;
			this.serverIp = serverIp;
		}
	}

	public class SendDataThread extends Thread {

		ConnectionManager connectionManager;

		public void run() {
			try {
				while (this.connectionManager.gameScreen == null)
					sleep(100);

				while (connectionManager.isConnected()) {
					sleep(connectionManager.refresh_time);
					String data = this.connectionManager.gameScreen.getData();
					Request request = new Request();
					request.text = data;
					this.connectionManager.client.sendTCP(request);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		public SendDataThread(ConnectionManager connectionManager) {
			this.connectionManager = connectionManager;
		}

	}

	public void startSendingData() {
		Thread thread = new SendDataThread(this);
		thread.start();
	}

	public void close() {
		this.connectionClosed();
		this.tryConnecting = false;
		if (server != null) {
			server.stop();
		}

		if (client != null) {
			client.stop();
		}
	}
}
