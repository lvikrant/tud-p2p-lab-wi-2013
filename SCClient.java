import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class SCClient {
	private static final String STOP_CONNECTION = "stop";
	private static final String SERVER_HOST_NAME = "127.0.0.1";
	private static final int SERVER_PORT_NUMBER = 9999;
	String clientIdentity;

	/**
	 * ctor
	 * @param clientIdentity: client ID
	 */
	public SCClient(String clientIdentity) {
		this.clientIdentity = clientIdentity;
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	void startClient() throws InterruptedException {
		try {
			Selector selector = connectToServer();

			while (selector.select() > 0) {
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();

				while (iterator.hasNext()) {
					SelectionKey selKey = (SelectionKey) iterator.next();
					SocketChannel socketChannel = (SocketChannel) selKey
							.channel();
					iterator.remove();
					if (selKey.isConnectable()) {
						if (socketChannel.isConnectionPending()) {
							socketChannel.finishConnect();
							System.out
									.println("Client successfuly connected to the server");
						}

						Scanner scanner = new Scanner(System.in);
						String message;

						while (true) {
							System.out.println("Enter message:");
							message = scanner.nextLine();
							if (!message.equals(STOP_CONNECTION)) {
								sendMessage(socketChannel, message);
							} else {
								closeConnection(socketChannel);
								scanner.close();
								return;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(SocketChannel socketChannel, String message)
			throws IOException {
		ByteBuffer buffer;
		buffer = ByteBuffer.wrap(new String(clientIdentity
				+ ": " + message).getBytes());
		socketChannel.write(buffer);
		buffer.clear();
	}

	/**
	 * closing connection to the server
	 * @param socketChannel
	 */
	private void closeConnection(SocketChannel socketChannel) {
		System.out.println("Closing connections...");
		try {
			socketChannel.socket().close();
			System.out.println("Connection closed!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * connect to the server
	 * @return
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ClosedChannelException
	 */
	private Selector connectToServer() throws IOException,
			UnknownHostException, ClosedChannelException {
		SocketChannel connection = SocketChannel.open();
		connection.configureBlocking(false);
		connection.connect(new InetSocketAddress(InetAddress.getByName(SERVER_HOST_NAME),
				SERVER_PORT_NUMBER));
		Selector selector = Selector.open();
		connection.register(selector, SelectionKey.OP_CONNECT);
		return selector;
	}

	public static void main(String[] args) throws InterruptedException {
		SCClient client = new SCClient(args[0]);
		client.startClient();
	}
}
