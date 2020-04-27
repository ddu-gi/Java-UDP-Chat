import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientThread extends Thread {
	private WaitRoomDisplay client_thread_waitRoom;
	private ChatRoomDisplay client_thread_chatRoom;
	private Socket client_thread_sock;
	private DataInputStream client_thread_in;
	private DataOutputStream client_thread_out;
	private StringBuffer client_thread_buffer;
	private Thread thisThread;
	private String client_thread_loginID;
	private int client_thread_room_Number;
	private static MessageBox msgBox, loginbox, fileTransBox;

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

	public ClientThread() {
		client_thread_waitRoom = new WaitRoomDisplay(this);
		client_thread_chatRoom = null;
		try {
			client_thread_sock = new Socket(InetAddress.getLocalHost(), 2777);
			client_thread_in = new DataInputStream(client_thread_sock.getInputStream());
			client_thread_out = new DataOutputStream(client_thread_sock.getOutputStream());
			client_thread_buffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_thread_waitRoom, "연결에러", "서버에 접속할 수 없습니다.");
			msgout.show();
		}
	}

	public ClientThread(String hostaddr) {
		client_thread_waitRoom = new WaitRoomDisplay(this);
		client_thread_chatRoom = null;
		try {
			client_thread_sock = new Socket(hostaddr, 2777);
			client_thread_in = new DataInputStream(client_thread_sock.getInputStream());
			client_thread_out = new DataOutputStream(client_thread_sock.getOutputStream());
			client_thread_buffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_thread_waitRoom, "연결에러", "서버에 접속할 수 없습니다.");
			msgout.show();
		}
	}

	public void run() {
		try {
			Thread currThread = Thread.currentThread();
			while (currThread == thisThread) {
				String recvData = client_thread_in.readUTF();
				StringTokenizer st = new StringTokenizer(recvData, " : ");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case 로그인수락: {
					loginbox.dispose();
					client_thread_room_Number = 0;
					try {
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
						Vector roomInfo = new Vector();
						while (st1.hasMoreTokens()) {
							String temp = st1.nextToken();
							if (!temp.equals("empty")) {
								roomInfo.addElement(temp);
							}
						}
						client_thread_waitRoom.roomInfo.setListData(roomInfo);
						client_thread_waitRoom.message.requestFocusInWindow();
					} catch (NoSuchElementException e) {
						client_thread_waitRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 로그인거절: {
					String id;
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 사용중) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_thread_waitRoom, "이미 다른 사용자가 있습니다.", "로그인",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getloginID();
						requestlogin(id);
					} else if (errCode == 서버포화) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_thread_waitRoom, "대화방이 가득 찼습니다.", "로그인",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getloginID();
						requestlogin(id);
					}
					break;
				}
				case 대기자수정: {
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_thread_waitRoom.waiterInfo.setListData(user);
					client_thread_waitRoom.message.requestFocusInWindow();
					break;
				}
				case 방생성수락: {
					client_thread_room_Number = Integer.parseInt(st.nextToken());
					client_thread_waitRoom.hide();
					if (client_thread_chatRoom == null) {
						client_thread_chatRoom = new ChatRoomDisplay(this);
						client_thread_chatRoom.isAdmin = true;
					} else {
						client_thread_chatRoom.show();
						client_thread_chatRoom.isAdmin = true;
						client_thread_chatRoom.resetComponents();
					}
					break;
				}
				case 방생성거절: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 방수포화) {
						msgBox = new MessageBox(client_thread_waitRoom, "대화방생성", "더 이상 대화방을 생성 할 수 없습니다.");
						msgBox.show();
					}
					break;
				}
				case 대기자정보수정: {
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "'");

					Vector rooms = new Vector();
					Vector users = new Vector();
					while (st1.hasMoreTokens()) {
						String temp = st1.nextToken();
						if (!temp.equals("empty")) {
							rooms.addElement(temp);
						}
					}
					client_thread_waitRoom.roomInfo.setListData(rooms);

					while (st2.hasMoreTokens()) {
						users.addElement(st2.nextToken());
					}

					client_thread_waitRoom.waiterInfo.setListData(users);
					client_thread_waitRoom.message.requestFocusInWindow();

					break;
				}
				case 방입장수락: {
					client_thread_room_Number = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					client_thread_waitRoom.hide();
					if (client_thread_chatRoom == null) {
						client_thread_chatRoom = new ChatRoomDisplay(this);
					} else {
						client_thread_chatRoom.show();
						client_thread_chatRoom.resetComponents();
					}
					break;
				}
				case 방입장거절: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == 방가득참) {
						msgBox = new MessageBox(client_thread_waitRoom, "대화방입장", "대화방이 가득 찼습니다.");
						msgBox.show();
					} else if (errCode == 틀린비밀번호) {
						msgBox = new MessageBox(client_thread_waitRoom, "대화방입장", "비밀번호가 틀립니다.");
						msgBox.show();
					}
					break;
				}
				case 방사용자수정: {
					String id = st.nextToken();
					int code = Integer.parseInt(st.nextToken());

					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_thread_chatRoom.roomerInfo.setListData(user);
					if (code == 1) {
						client_thread_chatRoom.messages.append("### " + id + "님이 입장하셨습니다. ###\n");
					} else if (code == 2) {
						client_thread_chatRoom.messages.append("### " + id + "님이 강제퇴장 되었습니다. ###\n");
					} else {
						client_thread_chatRoom.messages.append("### " + id + "님이 퇴장하셨습니다. ###\n");
					}
					client_thread_chatRoom.message.requestFocusInWindow();
					break;
				}
				case 방퇴장수락: {
					String id = st.nextToken();
					if (client_thread_chatRoom.isAdmin)
						client_thread_chatRoom.isAdmin = false;
					client_thread_chatRoom.hide();
					client_thread_waitRoom.show();
					client_thread_waitRoom.resetComponents();
					client_thread_room_Number = 0;
					break;
				}
				case 로그아웃수락: {
					client_thread_waitRoom.dispose();
					if (client_thread_chatRoom != null) {
						client_thread_chatRoom.dispose();
					}
					release();
					break;
				}
				case 송신자수락: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							client_thread_waitRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_thread_loginID)) {
								client_thread_waitRoom.message.setText("");
								client_thread_waitRoom.message.requestFocusInWindow();
							}
							client_thread_waitRoom.message.requestFocusInWindow();
						} else {
							client_thread_chatRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_thread_loginID)) {
								client_thread_chatRoom.message.setText("");
							}
							client_thread_chatRoom.message.requestFocusInWindow();
						}

					} catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_thread_waitRoom.message.requestFocusInWindow();
						else
							client_thread_chatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 수신자수락: {
					String id = st.nextToken();
					String idTo = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							if (id.equals(client_thread_loginID)) {
								client_thread_waitRoom.message.setText("");
								client_thread_waitRoom.messages.append("귓속말<to:" + idTo + "> : " + data + "\n");
							} else {
								client_thread_waitRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
							}
							client_thread_waitRoom.message.requestFocusInWindow();
						} else {

							if (id.equals(client_thread_loginID)) {
								client_thread_chatRoom.message.setText("");
								client_thread_chatRoom.messages.append("귓속말<to:" + idTo + "> : " + data + "\n");
							} else {
								client_thread_chatRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
							}
							client_thread_chatRoom.message.requestFocusInWindow();
						}
					} catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_thread_waitRoom.message.requestFocusInWindow();
						else
							client_thread_chatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case 수신자거절: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String message = "";
					if (room_Number == 0) {
						message = "대기실에 " + id + "님이 존재하지 않습니다.";
						JOptionPane.showMessageDialog(client_thread_waitRoom, message, "귓속말 에러",
								JOptionPane.ERROR_MESSAGE);
					} else {
						message = "이 대화방에 " + id + "님이 존재하지 않습니다.";
						JOptionPane.showMessageDialog(client_thread_chatRoom, message, "귓속말 에러",
								JOptionPane.ERROR_MESSAGE);
					}
					break;
				}
				case 파일전송요청: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String message = id + "로 부터 파일전송을 수락하시겠습니까?";
					int value = JOptionPane.showConfirmDialog(client_thread_chatRoom, message, "파일수신",
							JOptionPane.YES_NO_OPTION);
					if (value == 1) {
						try {
							client_thread_buffer.setLength(0);
							client_thread_buffer.append(파일전송거절);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(client_thread_loginID);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(room_Number);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(id);
							send(client_thread_buffer.toString());
						} catch (IOException e) {
							System.out.println(e);
						}
					} else {
						StringTokenizer addr = new StringTokenizer(InetAddress.getLocalHost().toString(), "/");
						String hostname = "";
						String hostaddr = "";

						hostname = addr.nextToken();
						try {
							hostaddr = addr.nextToken();
						} catch (NoSuchElementException err) {
							hostaddr = hostname;
						}

						try {
							client_thread_buffer.setLength(0);
							client_thread_buffer.append(파일전송수락);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(client_thread_loginID);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(room_Number);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(id);
							client_thread_buffer.append(" : ");
							client_thread_buffer.append(hostaddr);
							send(client_thread_buffer.toString());
						} catch (IOException e) {
							System.out.println(e);
						}
						// 파일 수신 서버실행.
						new ReciveFile();
					}
					break;
				}
				case 파일전송거절: {
					int code = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					fileTransBox.dispose();

					if (code == 거부됨) {
						String message = id + "님이 파일수신을 거부하였습니다.";
						JOptionPane.showMessageDialog(client_thread_chatRoom, message, "파일전송",
								JOptionPane.ERROR_MESSAGE);
						break;
					} else if (code == 사용자없음) {
						String message = id + "님은 이 방에 존재하지 않습니다.";
						JOptionPane.showMessageDialog(client_thread_chatRoom, message, "파일전송",
								JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
				case 파일전송수락: {
					String id = st.nextToken();
					String addr = st.nextToken();

					fileTransBox.dispose();
					// 파일 송신 클라이언트 실행.
					new SendFile(addr);
					break;
				}
				case 강제퇴장수락: {
					client_thread_chatRoom.hide();
					client_thread_waitRoom.show();
					client_thread_waitRoom.resetComponents();
					client_thread_room_Number = 0;
					client_thread_waitRoom.messages.append("### 방장에 의해 강제퇴장 되었습니다. ###\n");
					break;
				}
				}
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			System.out.println(e);
			release();
		} catch (IOException e) {
			System.out.println(e);
			release();
		}
	}

	public void requestlogin(String id) {
		try {
			loginbox = new MessageBox(client_thread_waitRoom, "로그인", "서버에 로그인 중입니다.");
			loginbox.show();
			client_thread_loginID = id;
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(로그인요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(id);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestCreateRoom(String roomName, int 방최대사용자, int isLock, String password) {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(방생성요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(roomName);
			client_thread_buffer.append("'");
			client_thread_buffer.append(방최대사용자);
			client_thread_buffer.append("'");
			client_thread_buffer.append(isLock);
			client_thread_buffer.append("'");
			client_thread_buffer.append(password);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestEnterRoom(int room_Number, String password) {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(방입장요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(room_Number);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(password);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestQuitRoom() {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(방퇴장요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_room_Number);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestLogout() {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(로그아웃요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestSendWord(String data) {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(송신자요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_room_Number);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(data);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestSendWordTo(String data, String idTo) {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(수신자요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_room_Number);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(idTo);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(data);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestforceOut(String idTo) {
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(강제퇴장요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_room_Number);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(idTo);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void requestSendFile(String idTo) {
		fileTransBox = new MessageBox(client_thread_chatRoom, "파일전송", "상대방의 승인을 기다립니다.");
		fileTransBox.show();
		try {
			client_thread_buffer.setLength(0);
			client_thread_buffer.append(파일전송요청);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_loginID);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(client_thread_room_Number);
			client_thread_buffer.append(" : ");
			client_thread_buffer.append(idTo);
			send(client_thread_buffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void send(String sendData) throws IOException {
		client_thread_out.writeUTF(sendData);
		client_thread_out.flush();
	}

	public void release() {
		if (thisThread != null) {
			thisThread = null;
		}
		try {
			if (client_thread_out != null) {
				client_thread_out.close();
			}
		} catch (IOException e) {
		} finally {
			client_thread_out = null;
		}
		try {
			if (client_thread_in != null) {
				client_thread_in.close();
			}
		} catch (IOException e) {
		} finally {
			client_thread_in = null;
		}
		try {
			if (client_thread_sock != null) {
				client_thread_sock.close();
			}
		} catch (IOException e) {
		} finally {
			client_thread_sock = null;
		}
		System.exit(0);
	}
}
