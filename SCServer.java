import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SCServer {
	private static final int BUFFER_SIZE = 1024;
	private static final int SERVER_PORT_NUMBER = 9999;
	private static final String SERVER_HOST_NAME = "localhost";
	ServerSocketChannel serverSocketChannel;
	Selector selector;

	public SCServer() {
		startServer();
	}

	/**
	 * start the server
	 */
	private void startServer() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			InetSocketAddress addr = new InetSocketAddress(
					InetAddress.getByName(SERVER_HOST_NAME), SERVER_PORT_NUMBER);
			serverSocketChannel.socket().bind(addr);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start listing for requests
	 */
	private void startListening() {
		System.out.println("Waiting for connection ...");
		Set<SelectionKey> selectedKeys;
		Iterator<SelectionKey> iterator;

		while (true) {
			try {
				selector.select();
				selectedKeys = selector.selectedKeys();
				iterator = selectedKeys.iterator();

				while (iterator.hasNext()) {
					handleRequest(iterator);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * handling a new incoming request
	 * 
	 * @param iterator
	 */
	private void handleRequest(Iterator<SelectionKey> iterator) {
		try {
			SelectionKey key = iterator.next();
			iterator.remove();

			if (!key.isValid()) {
				return;
			}
			if (key.isAcceptable()) {
				acceptConnection();
			} else if (key.isReadable()) {
				readFromClient(key);
			}
		} catch (ClosedByInterruptException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * reading an incoming message from the Client
	 * 
	 * @param key
	 * @throws IOException
	 */
	private void readFromClient(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);
		if (client.read(buff) < 1) {
			close(key);
		}
		buff.flip();
		byte[] array = new byte[buff.limit()];
		buff.get(array);
		System.out.println(new String(array));
	}

	/**
	 * accepting a new client connection
	 * 
	 * @throws IOException
	 * @throws ClosedChannelException
	 */
	private void acceptConnection() throws IOException, ClosedChannelException {
		SocketChannel connection;
		connection = serverSocketChannel.accept();
		connection.configureBlocking(false);
		System.out.printf(
				"-- Connection from: %s \n-- has been established.\n",
				connection.getRemoteAddress().toString());
		connection.register(selector, SelectionKey.OP_READ);
	}
	
	/**
	 * close client connection
	 * @param key
	 * @throws IOException
	 */
	private void close(SelectionKey key) throws IOException {
		System.out.printf("-- closing connection from client %s", key.channel());	
	    key.cancel();
	    key.channel().close();
	}

	public static void main(String[] args) {
		SCServer server = new SCServer();
		server.startListening();
	}
}