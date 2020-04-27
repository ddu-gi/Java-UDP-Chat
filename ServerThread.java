import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
	private Socket server_thread_sock;
	private DataInputStream server_thread_in;
	private DataOutputStream server_thread_out;
	private StringBuffer server_thread_buffer;
	private WaitRoom server_thread_waitRoom;
	public String server_thread_ID;
	public int server_thread_room_Number;
	private static final int WAITROOM = 0;

	private static final int �α��ο�û = 9000;
	private static final int �α��μ��� = 9001;
	private static final int �α��ΰ��� = 9002;
	private static final int �������û = 8000;
	private static final int ��������� = 8001;
	private static final int ��������� = 8002;
	private static final int �������û = 7000;
	private static final int ��������� = 7001;
	private static final int ��������� = 7002;
	private static final int �������û = 6000;
	private static final int ��������� = 6001;
	private static final int �α׾ƿ���û = 0000;
	private static final int �α׾ƿ����� = 0001;
	private static final int �۽��ڿ�û = 5000;
	private static final int �۽��ڼ��� = 5001;
	private static final int �����ڿ�û = 4000;
	private static final int �����ڼ��� = 4001;
	private static final int �����ڰ��� = 4002;
	private static final int ������û = 1111;
	private static final int ������û���� = 1112;
	private static final int �������ۿ�û = 7777;
	private static final int �������ۼ��� = 7778;
	private static final int �������۰��� = 7779;

	private static final int ����ڼ��� = 2003;
	private static final int ������������� = 2013;
	private static final int �����ڼ��� = 2023;

	private static final int �������_���� = 3001;
	private static final int ������ȭ = 5552;
	private static final int ����ȭ = 5554;
	private static final int ����ڼ���ȭ = 5555;
	private static final int Ʋ����й�ȣ = 7575;
	private static final int �źε� = 6666;
	private static final int ����ھ��� = 4444;

	public ServerThread(Socket sock) {
		try {
			server_thread_sock = sock;
			server_thread_in = new DataInputStream(sock.getInputStream());
			server_thread_out = new DataOutputStream(sock.getOutputStream());
			server_thread_buffer = new StringBuffer(2048);
			server_thread_waitRoom = new WaitRoom();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void sendErrCode(int message, int errCode) throws IOException {
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(message);
		server_thread_buffer.append(":");
		server_thread_buffer.append(errCode);
		send(server_thread_buffer.toString());
	}

	private void �������������() throws IOException {
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(�������������);
		server_thread_buffer.append(":");
		server_thread_buffer.append(server_thread_waitRoom.getWaitRoomInfo());
		broadcast(server_thread_buffer.toString(), WAITROOM);
	}

	private void ����ڼ���() throws IOException {
		String ids = server_thread_waitRoom.getUsers();
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(����ڼ���);
		server_thread_buffer.append(":");
		server_thread_buffer.append(ids);
		broadcast(server_thread_buffer.toString(), WAITROOM);
	}

	private void �����ڼ���(int room_Number, String id, int code) throws IOException {
		String ids = server_thread_waitRoom.getRoomInfo(room_Number);
		server_thread_buffer.setLength(0);
		server_thread_buffer.append(�����ڼ���);
		server_thread_buffer.append(":");
		server_thread_buffer.append(id);
		server_thread_buffer.append(":");
		server_thread_buffer.append(code);
		server_thread_buffer.append(":");
		server_thread_buffer.append(ids);
		broadcast(server_thread_buffer.toString(), room_Number);
	}

	private void send(String sendData) throws IOException {
		synchronized (server_thread_out) {

			System.out.println(sendData);

			server_thread_out.writeUTF(sendData);
			server_thread_out.flush();
		}
	}

	private synchronized void broadcast(String sendData, int room_Number) throws IOException {
		ServerThread client;
		Hashtable clients = server_thread_waitRoom.getClients(room_Number);
		Enumeration enu = clients.keys();
		while (enu.hasMoreElements()) {
			client = (ServerThread) clients.get(enu.nextElement());
			client.send(sendData);
		}
	}

	public void run() {
		try {
			while (true) {
				String recvData = server_thread_in.readUTF();

				System.out.println(recvData);

				StringTokenizer st = new StringTokenizer(recvData, ":");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case �α��ο�û: {
					server_thread_room_Number = WAITROOM;
					int result;
					server_thread_ID = st.nextToken();
					result = server_thread_waitRoom.addUser(server_thread_ID, this);
					server_thread_buffer.setLength(0);
					if (result == 0) {
						server_thread_buffer.append(�α��μ���);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_waitRoom.getRooms());
						send(server_thread_buffer.toString());
						����ڼ���();
						System.out.println(server_thread_ID + "�� �����û ����");
					} else {
						sendErrCode(�α��ΰ���, result);
					}
					break;
				}
				case �������û: {
					String id, roomName, password;
					int roomMaxUser, result;
					boolean lock;

					id = st.nextToken();
					String roomInfo = st.nextToken();
					StringTokenizer room = new StringTokenizer(roomInfo, "'");
					roomName = room.nextToken();
					roomMaxUser = Integer.parseInt(room.nextToken());
					lock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
					password = room.nextToken();

					ChatRoom chatRoom = new ChatRoom(roomName, roomMaxUser, lock, password, id);
					result = server_thread_waitRoom.addRoom(chatRoom);
					if (result == 0) {
						server_thread_room_Number = ChatRoom.getroom_Number();
						boolean temp = chatRoom.addUser(server_thread_ID, this);
						server_thread_waitRoom.delUser(server_thread_ID);

						server_thread_buffer.setLength(0);
						server_thread_buffer.append(���������);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						send(server_thread_buffer.toString());
						�������������();
						�����ڼ���(server_thread_room_Number, id, 1);
					} else {
						sendErrCode(���������, result);
					}
					break;
				}
				case �������û: {
					String id, password;
					int room_Number, result;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());
					try {
						password = st.nextToken();
					} catch (NoSuchElementException e) {
						password = "0";
					}
					result = server_thread_waitRoom.joinRoom(id, this, room_Number, password);

					if (result == 0) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(���������);
						server_thread_buffer.append(":");
						server_thread_buffer.append(room_Number);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_room_Number = room_Number;
						send(server_thread_buffer.toString());
						�����ڼ���(room_Number, id, 1);
						�������������();
					} else {
						sendErrCode(���������, result);
					}
					break;
				}
				case �������û: {
					String id;
					int room_Number;
					boolean updateWaitInfo;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());

					updateWaitInfo = server_thread_waitRoom.quitRoom(id, room_Number, this);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(���������);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					send(server_thread_buffer.toString());
					server_thread_room_Number = WAITROOM;

					if (updateWaitInfo) {
						�������������();
					} else {
						�������������();
						�����ڼ���(room_Number, id, 0);
					}
					break;
				}
				case �α׾ƿ���û: {
					String id = st.nextToken();
					server_thread_waitRoom.delUser(id);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(�α׾ƿ�����);
					send(server_thread_buffer.toString());
					����ڼ���();
					release();
					break;
				}
				case �۽��ڿ�û: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(�۽��ڼ���);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					server_thread_buffer.append(":");
					server_thread_buffer.append(server_thread_room_Number);
					server_thread_buffer.append(":");
					try {
						String data = st.nextToken();
						server_thread_buffer.append(data);
					} catch (NoSuchElementException e) {
					}

					broadcast(server_thread_buffer.toString(), room_Number);
					break;
				}
				case �����ڿ�û: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(�����ڼ���);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						server_thread_buffer.append(":");
						try {
							String data = st.nextToken();
							server_thread_buffer.append(data);
						} catch (NoSuchElementException e) {
						}
						client.send(server_thread_buffer.toString());
						send(server_thread_buffer.toString());
						break;
					} else {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(�����ڰ���);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						send(server_thread_buffer.toString());
						break;
					}
				}
				case �������ۿ�û: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(�������ۿ�û);
						server_thread_buffer.append(":");
						server_thread_buffer.append(id);
						server_thread_buffer.append(":");
						server_thread_buffer.append(server_thread_room_Number);
						client.send(server_thread_buffer.toString());
						break;
					} else {
						server_thread_buffer.setLength(0);
						server_thread_buffer.append(�������۰���);
						server_thread_buffer.append(":");
						server_thread_buffer.append(����ھ���);
						server_thread_buffer.append(":");
						server_thread_buffer.append(idTo);
						send(server_thread_buffer.toString());
						break;
					}
				}
				case �������۰���: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(�������۰���);
					server_thread_buffer.append(":");
					server_thread_buffer.append(�źε�);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);

					client.send(server_thread_buffer.toString());
					break;
				}
				case �������ۼ���: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					String hostaddr = st.nextToken();

					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(�������ۼ���);
					server_thread_buffer.append(":");
					server_thread_buffer.append(id);
					server_thread_buffer.append(":");
					server_thread_buffer.append(hostaddr);

					client.send(server_thread_buffer.toString());
					break;
				}
				case ������û: {
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					boolean updateWaitInfo;
					Hashtable room = server_thread_waitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);
					updateWaitInfo = server_thread_waitRoom.quitRoom(idTo, room_Number, client);

					server_thread_buffer.setLength(0);
					server_thread_buffer.append(������û����);
					client.send(server_thread_buffer.toString());
					client.server_thread_room_Number = 0;

					if (updateWaitInfo) {
						�������������();
					} else {
						�������������();
						�����ڼ���(room_Number, idTo, 2);
					}
					break;
				}
				}
				Thread.sleep(100);
			}
		} catch (NullPointerException e) {
		} catch (InterruptedException e) {
			System.out.println(e);

			if (server_thread_room_Number == 0) {
				server_thread_waitRoom.delUser(server_thread_ID);
			} else {
				boolean temp = server_thread_waitRoom.quitRoom(server_thread_ID, server_thread_room_Number, this);
				server_thread_waitRoom.delUser(server_thread_ID);
			}
			release();
		} catch (IOException e) {
			System.out.println(e);

			if (server_thread_room_Number == 0) {
				server_thread_waitRoom.delUser(server_thread_ID);
			} else {
				boolean temp = server_thread_waitRoom.quitRoom(server_thread_ID, server_thread_room_Number, this);
				server_thread_waitRoom.delUser(server_thread_ID);
			}
			release();
		}
	}

	public void release() {
		try {
			if (server_thread_in != null)
				server_thread_in.close();
		} catch (IOException e1) {
		} finally {
			server_thread_in = null;
		}
		try {
			if (server_thread_out != null)
				server_thread_out.close();
		} catch (IOException e1) {
		} finally {
			server_thread_out = null;
		}
		try {
			if (server_thread_sock != null)
				server_thread_sock.close();
		} catch (IOException e1) {
		} finally {
			server_thread_sock = null;
		}

		if (server_thread_ID != null) {
			System.out.println(server_thread_ID + "�� ������ �����մϴ�.");
			server_thread_ID = null;
		}
	}
}
