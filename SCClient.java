import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class SCClient {

	String identity;

	public SCClient(String pIdentity) {
		identity = pIdentity;
	}

	void whileSendingMessage() throws InterruptedException {

		try {

			SocketChannel connection = SocketChannel.open();
			connection.configureBlocking(false);
			connection.connect(new InetSocketAddress(InetAddress.getLocalHost(),9999));
			Selector selector = Selector.open();
			connection.register(selector, SelectionKey.OP_CONNECT);
			while (selector.select() > 0) {

				Set keys = selector.selectedKeys();
				Iterator it = keys.iterator();

				while (it.hasNext()) {

					SelectionKey selKey = (SelectionKey) it.next();
					SocketChannel socketChannel = (SocketChannel) selKey.channel();
					it.remove();
					if (selKey.isConnectable()) {
				
						if (socketChannel.isConnectionPending()) {
						
							socketChannel.finishConnect();
							System.out.println("Connected.");
						
						}

						ByteBuffer buff = null;
						Scanner scnr = new Scanner(System.in);
						String message;
						
						while (true) {
						
							System.out.println("Enter:");
							message = scnr.nextLine();
							if (!message.equals(".")) {
								buff = ByteBuffer.wrap(new String(identity+": "+message).getBytes());
								socketChannel.write(buff);
								buff.clear();
							} else {
								System.out.println("Closing connections...");
								try {
									socketChannel.socket().close();
									System.out.println("Connection closed!");
									return;
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
						
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {

		SCClient client = new SCClient(args[0]);
		client.whileSendingMessage();
	}

}