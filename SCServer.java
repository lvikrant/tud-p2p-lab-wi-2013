import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SCServer {

	ServerSocketChannel serverSocketChannel;
	Selector selector;

	public SCServer() {

		try {

			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			InetSocketAddress addr = new InetSocketAddress(
					InetAddress.getLocalHost(), 9999);
			serverSocketChannel.socket().bind(addr);
			SelectionKey key = serverSocketChannel.register(selector,
					SelectionKey.OP_ACCEPT);
			System.out.println();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		SCServer server = new SCServer();
		server.startListening();

	}

	private void startListening() {

		System.out.println("Waiting for connection ...");
		SocketChannel client=null;
		SocketChannel connection =null;
		Set selectedKeys;
		Iterator iterator;
		
		while (true) {

			try {
				
				selector.select();
				selectedKeys = selector.selectedKeys();
				iterator = selectedKeys.iterator();
				
				while (iterator.hasNext()) {
					
					try {
						
						SelectionKey key = (SelectionKey) iterator.next();
						iterator.remove();
						
						if (key.isAcceptable()) {
							
							connection = serverSocketChannel.accept();
							connection.configureBlocking(false);
							System.out.println("-- Connection from: "+ connection.getRemoteAddress()+ "\n"
									+ "-- has been established.");
							connection.register(selector, SelectionKey.OP_READ);
							continue;
							
						}

						if (key.isReadable()) {
							
							client = (SocketChannel) key.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							client.read(buff);
							buff.flip();
							byte[] array = new byte[buff.limit()];
							buff.get(array);
							System.out.println(new String(array));
							continue;
							
						}
					}catch ( ClosedByInterruptException e)
					{
						e.printStackTrace();
						break;
					
					} catch (IOException e) {
						
						e.printStackTrace();
						break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}