import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
	private Socket server_threadsock;
	private DataInputStream server_threadin;
	private DataOutputStream server_threadout;
	private StringBuffer server_threadbuffer;
	private WaitRoom server_threadwaitRoom;
	public String server_threadID;
	public int server_threadroom_Number;

	private static final int WAITROOM = 0;

	private static final int 로그인요청 = 1000;
	private static final int 로그인수락 = 1001;
	private static final int 로그인거절 = 1002;

	private static final int 방생성요청 = 1010;
	private static final int 방생성수락 = 1011;
	private static final int 방생성거절 = 1012;

	private static final int 방입장요청 = 1020;
	private static final int 방입장수락 = 1021;
	private static final int 방입장거절 = 1022;

	private static final int 방퇴장요청 = 1030;
	private static final int 방퇴장수락 = 1031;

	private static final int 로그아웃요청 = 1040;
	private static final int 로그아웃수락 = 1041;

	private static final int 송신자요청 = 1050;
	private static final int 송신자수락 = 1051;

	private static final int 수신자요청 = 1060;
	private static final int 수신자수락 = 1061;
	private static final int 수신자거절 = 1062;

	private static final int 강제퇴장요청 = 1070;
	private static final int 강제퇴장수락 = 1071;

	private static final int 파일전송요청 = 1080;
	private static final int 파일전송수락 = 1081;
	private static final int 파일전송거절 = 1082;

	private static final int 대기자수정 = 2000;
	private static final int 대기자정보수정 = 3000;
	private static final int 방사용자수정 = 4000;

	private static final int 사용중 = 9999;
	private static final int 서버포화 = 9998;
	private static final int 방수포화 = 9997;
	private static final int 방가득참 = 9996;
	private static final int 틀린비밀번호 = 9995;
	private static final int 거부됨 = 9994;
	private static final int 사용자없음 = 9993;

	public ServerThread(Socket sock) {
		try {
			server_threadsock = sock;
			server_threadin = new DataInputStream(sock.getInputStream());
			server_threadout = new DataOutputStream(sock.getOutputStream());
			server_threadbuffer = new StringBuffer(2048);
			server_threadwaitRoom = new WaitRoom();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void sendErrCode(int message, int errCode) throws IOException {
		server_threadbuffer.setLength(0);
		server_threadbuffer.append(message);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(errCode);
		send(server_threadbuffer.toString());
	}

	private void modifyWaitRoom() throws IOException {
		server_threadbuffer.setLength(0);
		server_threadbuffer.append(대기자정보수정);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(server_threadwaitRoom.getWaitRoomInfo());
		broadcast(server_threadbuffer.toString(), WAITROOM);
	}

	private void modifyWaitUser() throws IOException {
		String ids = server_threadwaitRoom.getUsers();
		server_threadbuffer.setLength(0);
		server_threadbuffer.append(대기자수정);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(ids);
		broadcast(server_threadbuffer.toString(), WAITROOM);
	}

	private void modify방사용자(int room_Number, String id, int code) throws IOException {
		String ids = server_threadwaitRoom.getRoomInfo(room_Number);
		server_threadbuffer.setLength(0);
		server_threadbuffer.append(방사용자수정);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(id);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(code);
		server_threadbuffer.append(" : ");
		server_threadbuffer.append(ids);
		broadcast(server_threadbuffer.toString(), room_Number);
	}

	private void send(String sendData) throws IOException {
		synchronized (server_threadout) {

			System.out.println(sendData);

			server_threadout.writeUTF(sendData);
			server_threadout.flush();
		}
	}

	private synchronized void broadcast(String sendData, int room_Number) throws IOException {
		ServerThread client;
		Hashtable clients = server_threadwaitRoom.getClients(room_Number);
		Enumeration enu = clients.keys();
		while (enu.hasMoreElements()) {
			client = (ServerThread) clients.get(enu.nextElement());
			client.send(sendData);
		}
	}

	public void run() {
		try {
			while (true) {
				String recvData = server_threadin.readUTF();

				System.out.println(recvData);

				StringTokenizer st = new StringTokenizer(recvData, " : ");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case 로그인요청: {
					server_threadroom_Number = WAITROOM;
					int result;
					server_threadID = st.nextToken();
					result = server_threadwaitRoom.addUser(server_threadID, this);
					server_threadbuffer.setLength(0);
					if (result == 0) {
						server_threadbuffer.append(로그인수락);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(server_threadwaitRoom.getRooms());
						send(server_threadbuffer.toString());
						modifyWaitUser();
						System.out.println(server_threadID + "의 연결요청 승인");
					} else {
						sendErrCode(로그인거절, result);
					}
					break;
				}
				case 방생성요청: {
					String id, roomName, password;
					int 방최대사용자, result;
					boolean isLock;

					id = st.nextToken();
					String roomInfo = st.nextToken();
					StringTokenizer room = new StringTokenizer(roomInfo, "'");
					roomName = room.nextToken();
					방최대사용자 = Integer.parseInt(room.nextToken());
					isLock = (Integer.parseInt(room.nextToken()) == 0) ? false : true;
					password = room.nextToken();

					ChatRoom chatRoom = new ChatRoom(roomName, 방최대사용자, isLock, password, id);
					result = server_threadwaitRoom.addRoom(chatRoom);
					if (result == 0) {
						server_threadroom_Number = ChatRoom.getroom_Number();
						boolean temp = chatRoom.addUser(server_threadID, this);
						server_threadwaitRoom.delUser(server_threadID);

						server_threadbuffer.setLength(0);
						server_threadbuffer.append(방생성수락);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(server_threadroom_Number);
						send(server_threadbuffer.toString());
						modifyWaitRoom();
						modify방사용자(server_threadroom_Number, id, 1);
					} else {
						sendErrCode(방생성거절, result);
					}
					break;
				}
				case 방입장요청: {
					String id, password;
					int room_Number, result;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());
					try {
						password = st.nextToken();
					} catch (NoSuchElementException e) {
						password = "0";
					}
					result = server_threadwaitRoom.joinRoom(id, this, room_Number, password);

					if (result == 0) {
						server_threadbuffer.setLength(0);
						server_threadbuffer.append(방입장수락);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(room_Number);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(id);
						server_threadroom_Number = room_Number;
						send(server_threadbuffer.toString());
						modify방사용자(room_Number, id, 1);
						modifyWaitRoom();
					} else {
						sendErrCode(방입장거절, result);
					}
					break;
				}
				case 방퇴장요청: {
					String id;
					int room_Number;
					boolean updateWaitInfo;
					id = st.nextToken();
					room_Number = Integer.parseInt(st.nextToken());

					updateWaitInfo = server_threadwaitRoom.quitRoom(id, room_Number, this);

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(방퇴장수락);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(id);
					send(server_threadbuffer.toString());
					server_threadroom_Number = WAITROOM;

					if (updateWaitInfo) {
						modifyWaitRoom();
					} else {
						modifyWaitRoom();
						modify방사용자(room_Number, id, 0);
					}
					break;
				}
				case 로그아웃요청: {
					String id = st.nextToken();
					server_threadwaitRoom.delUser(id);

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(로그아웃수락);
					send(server_threadbuffer.toString());
					modifyWaitUser();
					release();
					break;
				}
				case 송신자요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(송신자수락);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(id);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(server_threadroom_Number);
					server_threadbuffer.append(" : ");
					try {
						String data = st.nextToken();
						server_threadbuffer.append(data);
					} catch (NoSuchElementException e) {
					}

					broadcast(server_threadbuffer.toString(), room_Number);
					break;
				}
				case 수신자요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_threadwaitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_threadbuffer.setLength(0);
						server_threadbuffer.append(수신자수락);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(id);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(idTo);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(server_threadroom_Number);
						server_threadbuffer.append(" : ");
						try {
							String data = st.nextToken();
							server_threadbuffer.append(data);
						} catch (NoSuchElementException e) {
						}
						client.send(server_threadbuffer.toString());
						send(server_threadbuffer.toString());
						break;
					} else {
						server_threadbuffer.setLength(0);
						server_threadbuffer.append(수신자거절);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(idTo);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(server_threadroom_Number);
						send(server_threadbuffer.toString());
						break;
					}
				}
				case 파일전송요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_threadwaitRoom.getClients(room_Number);
					ServerThread client = null;
					if ((client = (ServerThread) room.get(idTo)) != null) {
						server_threadbuffer.setLength(0);
						server_threadbuffer.append(파일전송요청);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(id);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(server_threadroom_Number);
						client.send(server_threadbuffer.toString());
						break;
					} else {
						server_threadbuffer.setLength(0);
						server_threadbuffer.append(파일전송거절);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(사용자없음);
						server_threadbuffer.append(" : ");
						server_threadbuffer.append(idTo);
						send(server_threadbuffer.toString());
						break;
					}
				}
				case 파일전송거절: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();

					Hashtable room = server_threadwaitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(파일전송거절);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(거부됨);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(id);

					client.send(server_threadbuffer.toString());
					break;
				}
				case 파일전송수락: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					String hostaddr = st.nextToken();

					Hashtable room = server_threadwaitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(파일전송수락);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(id);
					server_threadbuffer.append(" : ");
					server_threadbuffer.append(hostaddr);

					client.send(server_threadbuffer.toString());
					break;
				}
				case 강제퇴장요청: {
					int room_Number = Integer.parseInt(st.nextToken());
					String idTo = st.nextToken();
					boolean updateWaitInfo;
					Hashtable room = server_threadwaitRoom.getClients(room_Number);
					ServerThread client = null;
					client = (ServerThread) room.get(idTo);
					updateWaitInfo = server_threadwaitRoom.quitRoom(idTo, room_Number, client);

					server_threadbuffer.setLength(0);
					server_threadbuffer.append(강제퇴장수락);
					client.send(server_threadbuffer.toString());
					client.server_threadroom_Number = 0;

					if (updateWaitInfo) {
						modifyWaitRoom();
					} else {
						modifyWaitRoom();
						modify방사용자(room_Number, idTo, 2);
					}
					break;
				}
				}
				Thread.sleep(100);
			}
		} catch (NullPointerException e) {
		} catch (InterruptedException e) {
			System.out.println(e);

			if (server_threadroom_Number == 0) {
				server_threadwaitRoom.delUser(server_threadID);
			} else {
				boolean temp = server_threadwaitRoom.quitRoom(server_threadID, server_threadroom_Number, this);
				server_threadwaitRoom.delUser(server_threadID);
			}
			release();
		} catch (IOException e) {
			System.out.println(e);

			if (server_threadroom_Number == 0) {
				server_threadwaitRoom.delUser(server_threadID);
			} else {
				boolean temp = server_threadwaitRoom.quitRoom(server_threadID, server_threadroom_Number, this);
				server_threadwaitRoom.delUser(server_threadID);
			}
			release();
		}
	}

	public void release() {
		try {
			if (server_threadin != null)
				server_threadin.close();
		} catch (IOException e1) {
		} finally {
			server_threadin = null;
		}
		try {
			if (server_threadout != null)
				server_threadout.close();
		} catch (IOException e1) {
		} finally {
			server_threadout = null;
		}
		try {
			if (server_threadsock != null)
				server_threadsock.close();
		} catch (IOException e1) {
		} finally {
			server_threadsock = null;
		}

		if (server_threadID != null) {
			System.out.println(server_threadID + "와 연결을 종료합니다.");
			server_threadID = null;
		}
	}
}
