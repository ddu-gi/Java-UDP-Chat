import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientThread extends Thread {
	private WaitRoomDisplay client_threadwaitRoom;
	private ChatRoomDisplay client_threadchatRoom;
	private Socket client_threadsock;
	private DataInputStream client_threadin;
	private DataOutputStream client_threadout;
	private StringBuffer client_threadbuffer;
	private Thread thisThread;
	private String client_threadloginID;
	private int client_threadroom_Number;
	private static MessageBox msgBox, loginbox, fileTransBox;

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

	public ClientThread() {
		client_threadwaitRoom = new WaitRoomDisplay(this);
		client_threadchatRoom = null;
		try {
			client_threadsock = new Socket(InetAddress.getLocalHost(), 2777);
			client_threadin = new DataInputStream(client_threadsock.getInputStream());
			client_threadout = new DataOutputStream(client_threadsock.getOutputStream());
			client_threadbuffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_threadwaitRoom, "���ῡ��", "������ ������ �� �����ϴ�.");
			msgout.show();
		}
	}

	public ClientThread(String hostaddr) {
		client_threadwaitRoom = new WaitRoomDisplay(this);
		client_threadchatRoom = null;
		try {
			client_threadsock = new Socket(hostaddr, 2777);
			client_threadin = new DataInputStream(client_threadsock.getInputStream());
			client_threadout = new DataOutputStream(client_threadsock.getOutputStream());
			client_threadbuffer = new StringBuffer(4096);
			thisThread = this;
		} catch (IOException e) {
			MessageBoxLess msgout = new MessageBoxLess(client_threadwaitRoom, "���ῡ��", "������ ������ �� �����ϴ�.");
			msgout.show();
		}
	}

	public void run() {
		try {
			Thread currThread = Thread.currentThread();
			while (currThread == thisThread) {
				String recvData = client_threadin.readUTF();
				StringTokenizer st = new StringTokenizer(recvData, ":");
				int command = Integer.parseInt(st.nextToken());
				switch (command) {
				case �α��μ���: {
					loginbox.dispose();
					client_threadroom_Number = 0;
					try {
						StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
						Vector roomInfo = new Vector();
						while (st1.hasMoreTokens()) {
							String temp = st1.nextToken();
							if (!temp.equals("empty")) {
								roomInfo.addElement(temp);
							}
						}
						client_threadwaitRoom.roomInfo.setListData(roomInfo);
						client_threadwaitRoom.message.requestFocusInWindow();
					} catch (NoSuchElementException e) {
						client_threadwaitRoom.message.requestFocusInWindow();
					}
					break;
				}
				case �α��ΰ���: {
					String id;
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == �������_����) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_threadwaitRoom, "�ߺ��� ����ڰ� �ֽ��ϴ�.", "�α���",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getLoginID();
						�α��ο�û(id);
					} else if (errCode == ������ȭ) {
						loginbox.dispose();
						JOptionPane.showMessageDialog(client_threadwaitRoom, "��ȭ���� ����á���ϴ�.", "�α���",
								JOptionPane.ERROR_MESSAGE);
						id = ChatClient.getLoginID();
						�α��ο�û(id);
					}
					break;
				}
				case ����ڼ���: {
					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_threadwaitRoom.waiterInfo.setListData(user);
					client_threadwaitRoom.message.requestFocusInWindow();
					break;
				}
				case ���������: {
					client_threadroom_Number = Integer.parseInt(st.nextToken());
					client_threadwaitRoom.hide();
					if (client_threadchatRoom == null) {
						client_threadchatRoom = new ChatRoomDisplay(this);
						client_threadchatRoom.isAdmin = true;
					} else {
						client_threadchatRoom.show();
						client_threadchatRoom.isAdmin = true;
						client_threadchatRoom.resetComponents();
					}
					break;
				}
				case ���������: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == ����ȭ) {
						msgBox = new MessageBox(client_threadwaitRoom, "��ȭ�氳��", "��ȭ������Ұ�");
						msgBox.show();
					}
					break;
				}
				case �������������: {
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
					client_threadwaitRoom.roomInfo.setListData(rooms);

					while (st2.hasMoreTokens()) {
						users.addElement(st2.nextToken());
					}

					client_threadwaitRoom.waiterInfo.setListData(users);
					client_threadwaitRoom.message.requestFocusInWindow();

					break;
				}
				case ���������: {
					client_threadroom_Number = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					client_threadwaitRoom.hide();
					if (client_threadchatRoom == null) {
						client_threadchatRoom = new ChatRoomDisplay(this);
					} else {
						client_threadchatRoom.show();
						client_threadchatRoom.resetComponents();
					}
					break;
				}
				case ���������: {
					int errCode = Integer.parseInt(st.nextToken());
					if (errCode == ����ڼ���ȭ) {
						msgBox = new MessageBox(client_threadwaitRoom, "��ȭ������", "��ȭ���� ���� á���ϴ�.");
						msgBox.show();
					} else if (errCode == Ʋ����й�ȣ) {
						msgBox = new MessageBox(client_threadwaitRoom, "��ȭ������", "��й�ȣ�� Ʋ�Ƚ��ϴ�.");
						msgBox.show();
					}
					break;
				}
				case �����ڼ���: {
					String id = st.nextToken();
					int code = Integer.parseInt(st.nextToken());

					StringTokenizer st1 = new StringTokenizer(st.nextToken(), "'");
					Vector user = new Vector();
					while (st1.hasMoreTokens()) {
						user.addElement(st1.nextToken());
					}
					client_threadchatRoom.roomerInfo.setListData(user);
					if (code == 1) {
						client_threadchatRoom.messages.append("### " + id + "���� �����ϼ̽��ϴ�. ###\n");
					} else if (code == 2) {
						client_threadchatRoom.messages.append("### " + id + "���� �������� �Ǿ����ϴ�. ###\n");
					} else {
						client_threadchatRoom.messages.append("### " + id + "���� �����ϼ̽��ϴ�. ###\n");
					}
					client_threadchatRoom.message.requestFocusInWindow();
					break;
				}
				case ���������: {
					String id = st.nextToken();
					if (client_threadchatRoom.isAdmin)
						client_threadchatRoom.isAdmin = false;
					client_threadchatRoom.hide();
					client_threadwaitRoom.show();
					client_threadwaitRoom.resetComponents();
					client_threadroom_Number = 0;
					break;
				}
				case �α׾ƿ�����: {
					client_threadwaitRoom.dispose();
					if (client_threadchatRoom != null) {
						client_threadchatRoom.dispose();
					}
					release();
					break;
				}
				case �۽��ڼ���: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							client_threadwaitRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_threadloginID)) {
								client_threadwaitRoom.message.setText("");
								client_threadwaitRoom.message.requestFocusInWindow();
							}
							client_threadwaitRoom.message.requestFocusInWindow();
						} else {
							client_threadchatRoom.messages.append(id + " : " + data + "\n");
							if (id.equals(client_threadloginID)) {
								client_threadchatRoom.message.setText("");
							}
							client_threadchatRoom.message.requestFocusInWindow();
						}

					} catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_threadwaitRoom.message.requestFocusInWindow();
						else
							client_threadchatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case �����ڼ���: {
					String id = st.nextToken();
					String idTo = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					try {
						String data = st.nextToken();
						if (room_Number == 0) {
							if (id.equals(client_threadloginID)) {
								client_threadwaitRoom.message.setText("");
								client_threadwaitRoom.message.requestFocusInWindow();
							}
						}
						}catch (NoSuchElementException e) {
						if (room_Number == 0)
							client_threadwaitRoom.message.requestFocusInWindow();
						else
							client_threadchatRoom.message.requestFocusInWindow();
					}
					break;
				}
				case �������ۿ�û: {
					String id = st.nextToken();
					int room_Number = Integer.parseInt(st.nextToken());
					String message = id + "�� ���� ������ �����ðڽ��ϱ�";
					int value = JOptionPane.showConfirmDialog(client_threadchatRoom, message, "���ϼ���",
							JOptionPane.YES_NO_OPTION);
					if (value == 1) {
						try {
							client_threadbuffer.setLength(0);
							client_threadbuffer.append(�������۰���);
							client_threadbuffer.append(":");
							client_threadbuffer.append(client_threadloginID);
							client_threadbuffer.append(":");
							client_threadbuffer.append(room_Number);
							client_threadbuffer.append(":");
							client_threadbuffer.append(id);
							send(client_threadbuffer.toString());
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
							client_threadbuffer.setLength(0);
							client_threadbuffer.append(�������ۼ���);
							client_threadbuffer.append(":");
							client_threadbuffer.append(client_threadloginID);
							client_threadbuffer.append(":");
							client_threadbuffer.append(room_Number);
							client_threadbuffer.append(":");
							client_threadbuffer.append(id);
							client_threadbuffer.append(":");
							client_threadbuffer.append(hostaddr);
							send(client_threadbuffer.toString());
						} catch (IOException e) {
							System.out.println(e);
						}
						// ���� ���� ��������.
						new ReciveFile();
					}
					break;
				}
				case �������۰���: {
					int code = Integer.parseInt(st.nextToken());
					String id = st.nextToken();
					fileTransBox.dispose();

					if (code == �źε�) {
						String message = id + "���� ���ϼ����� �ź��Ͽ����ϴ�.";
						JOptionPane.showMessageDialog(client_threadchatRoom, message, "��������",
								JOptionPane.ERROR_MESSAGE);
						break;
					} else if (code == ����ھ���) {
						String message = id + "���� �� �濡 �������� �ʽ��ϴ�.";
						JOptionPane.showMessageDialog(client_threadchatRoom, message, "��������",
								JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
				case �������ۼ���: {
					String id = st.nextToken();
					String addr = st.nextToken();

					fileTransBox.dispose();
					// ���� �۽� Ŭ���̾�Ʈ ����.
					new SendFile(addr);
					break;
				}
				case ������û����: {
					client_threadchatRoom.hide();
					client_threadwaitRoom.show();
					client_threadwaitRoom.resetComponents();
					client_threadroom_Number = 0;
					client_threadwaitRoom.messages.append("### ���忡 ���� �������� �Ǿ����ϴ�. ###\n");
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

	public void �α��ο�û(String id) {
		try {
			loginbox = new MessageBox(client_threadwaitRoom, "�α���", "������ �α��� ���Դϴ�.");
			loginbox.show();
			client_threadloginID = id;
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�α��ο�û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(id);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �������û(String roomName, int roomMaxUser, int lock, String password) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�������û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(roomName);
			client_threadbuffer.append("'");
			client_threadbuffer.append(roomMaxUser);
			client_threadbuffer.append("'");
			client_threadbuffer.append(lock);
			client_threadbuffer.append("'");
			client_threadbuffer.append(password);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �������û(int room_Number, String password) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�������û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(room_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(password);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �������û() {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�������û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �α׾ƿ���û() {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�α׾ƿ���û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �۽��ڿ�û(String data) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�۽��ڿ�û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(data);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �����ڿ�û(String data, String idTo) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�����ڿ�û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			client_threadbuffer.append(":");
			client_threadbuffer.append(data);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void ���������û(String idTo) {
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(������û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void �������ۿ�û(String idTo) {
		fileTransBox = new MessageBox(client_threadchatRoom, "��������", "������ ������ ��ٸ��ϴ�.");
		fileTransBox.show();
		try {
			client_threadbuffer.setLength(0);
			client_threadbuffer.append(�������ۿ�û);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadloginID);
			client_threadbuffer.append(":");
			client_threadbuffer.append(client_threadroom_Number);
			client_threadbuffer.append(":");
			client_threadbuffer.append(idTo);
			send(client_threadbuffer.toString());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private void send(String sendData) throws IOException {
		client_threadout.writeUTF(sendData);
		client_threadout.flush();
	}

	public void release() {
		if (thisThread != null) {
			thisThread = null;
		}
		try {
			if (client_threadout != null) {
				client_threadout.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadout = null;
		}
		try {
			if (client_threadin != null) {
				client_threadin.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadin = null;
		}
		try {
			if (client_threadsock != null) {
				client_threadsock.close();
			}
		} catch (IOException e) {
		} finally {
			client_threadsock = null;
		}
		System.exit(0);
	}
}
