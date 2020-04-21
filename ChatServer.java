import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Chatserver extends JFrame implements ActionListener {

	Vector Conneting_User = new Vector();

	ServerSocket Server;
	Socket Socket;

	JLabel ����_���� = new JLabel("���� ���� :");
	JLabel ����_��Ʈ = new JLabel("��Ʈ ��ȣ :");

	JButton ����_���� = new JButton("����");
	JButton ����_���� = new JButton("�ݱ�");

	JTextArea �����_����Ʈ = new JTextArea();
	JScrollPane �����_����Ʈ_��� = new JScrollPane();
	JTextField ��Ʈ_�Է� = new JTextField();

	JScrollBar Connect_ListSB = �����_����Ʈ_���.getVerticalScrollBar();
	StringTokenizer stp;

	Chatserver() {
		init();
		start();
	}

	public void init() {
		Font Font1 = new Font("����", Font.BOLD, 13);

		this.setTitle("Chatserver");
		this.setSize(200, 300);

		Dimension dimen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimen1 = this.getSize();
		int xpos = (int) (dimen.getWidth() / 2 - dimen1.getWidth() / 2);
		int ypos = (int) (dimen.getHeight() / 2 - dimen1.getHeight() / 2);

		this.setLocation(xpos, ypos);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false); // â ũ�� ���� x
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		����_����.setFont(Font1);
		����_����.setFont(Font1);
		����_����.setFont(Font1);
		�����_����Ʈ.setFont(Font1);
		����_��Ʈ.setFont(Font1);
		��Ʈ_�Է�.setFont(Font1);

		����_����.setBounds(40, 5, 230, 30);
		����_����.setVisible(true);
		this.add(����_����);

		����_��Ʈ.setBounds(35, 230, 200, 20);
		����_��Ʈ.setVisible(true);
		this.add(����_��Ʈ);

		��Ʈ_�Է�.setBounds(110, 230, 50, 20);
		��Ʈ_�Է�.setVisible(true);
		this.add(��Ʈ_�Է�);

		����_����.setBounds(15, 180, 80, 30);
		����_����.setVisible(true);
		this.add(����_����);

		����_����.setBounds(100, 180, 80, 30);
		����_����.setVisible(true);
		this.add(����_����);

		�����_����Ʈ.setBounds(15, 45, 165, 120);
		�����_����Ʈ.setVisible(true);
		�����_����Ʈ.setEnabled(false);
		this.add(�����_����Ʈ);

		�����_����Ʈ_���.setViewportView(�����_����Ʈ);
		�����_����Ʈ_���.setVisible(true);
		�����_����Ʈ_���.setBounds(15, 45, 165, 120);
		this.add(�����_����Ʈ_���);

		����_����.setEnabled(false);
		��Ʈ_�Է�.setText("9000");
	}

	public void start() {
		����_����.addActionListener(this);
		����_����.addActionListener(this);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Chatserver();
	}

	@Override
	public void actionPerformed(ActionEvent Click) {
		// TODO Auto-generated method stub
		if (Click.getSource() == ����_����) {
			Play_Sound("Sound/Speech On.wav");
			if (��Ʈ_�Է�.getText().equals("") || ��Ʈ_�Է�.getText().length() == 0 || ��Ʈ_�Է�.getText().length() < 4) {
				�����_����Ʈ.append("��Ʈ ��ȣ �Է����ּ���.\n");
				��Ʈ_�Է�.requestFocus();
			} else {
				�����_����Ʈ.append("������ ���Ƚ��ϴ�.\n");
				����_����.setText("���� ���� : ����");
				����_����.setEnabled(true);
				��Ʈ_�Է�.setEnabled(false);

				int Port_Number = Integer.parseInt(��Ʈ_�Է�.getText());
				Server_Start(Port_Number);
			}
		}

		if (Click.getSource() == ����_����) {
			Play_Sound("Sound/Speech Off.wav");
			�����_����Ʈ.append("������ �������ϴ�.\n");
			����_����.setText("���� ���� : ����");

			try {
				Server.close();
			} catch (IOException e) {
				�����_����Ʈ.append("������ �������� �ʽ��ϴ�.\n");
				e.printStackTrace();
			}

			����_����.setEnabled(true);
			����_����.setEnabled(false);
		}
	}

	public void Server_Start(int Port_Number) {
		try {
			Server = new ServerSocket(Port_Number);
			����_����.setEnabled(false);

			if (Server != null) {
				�����_����Ʈ.append("���� ��� ���� ���Դϴ�.\n");
				Connection_Server();
			}

		} catch (IOException e) {
			�����_����Ʈ.append("�̹� ��� ���� �����Դϴ�.");
			e.printStackTrace();
		}
	}

	public void Connection_Server() {
		Thread Connetting_Thread = new Thread(new Runnable() {

			@Override
			public void run() {
				int User_Count = 0;
				while (true) {
					try {
						�����_����Ʈ.append("����� ���� ��� ��...\n");
						Socket = Server.accept();
						�����_����Ʈ.append("����� ���� �Ϸ�!\n");

						User_Count++;
						User_Info User = new User_Info(Socket, User_Count);

						User.start();
					}

					catch (IOException e) {
						�����_����Ʈ.append("Accept ���� �߻�\n");
					}
				}
			}
		});
		Connetting_Thread.start();
	}

	public void Play_Sound(String fileName) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
			Clip clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
		} catch (Exception ex) {
		}
	}

	public class User_Info extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private String �г���;
		private int ������ȣ;
		private Socket ��������;

		User_Info(Socket S, int ID) {
			this.�������� = S;
			this.������ȣ = ID;

			User_Socket_NetWork();
		}

		public void User_Socket_NetWork() {
			try {
				is = ��������.getInputStream();
				dis = new DataInputStream(is);
				os = ��������.getOutputStream();
				dos = new DataOutputStream(os);

				�г��� = dis.readUTF();

				�����_����Ʈ.append("������ ID : " + �г��� + " " + ������ȣ + "\n");
				Broad_Cast("New_User/" + �г���);

				for (int i = 0; i < Conneting_User.size(); i++) {
					User_Info u = (User_Info) Conneting_User.elementAt(i);
					To_Client_Message("Old_User/" + u.�г���);
				}

				Conneting_User.add(this);
			} catch (Exception e) {
				�����_����Ʈ.append("��Ʈ�� ���� ����\n");
			}
		}

		public void To_Client_Message(String Str) {
			try {
				dos.writeUTF(Str);
			} catch (IOException e) {
				�����_����Ʈ.append("�޽��� �۽� ���� �߻�\n");
			}
		}

		public void Broad_Cast(String Str) {
			for (int i = 0; i < Conneting_User.size(); i++) {
				User_Info u = (User_Info) Conneting_User.elementAt(i);
				u.To_Client_Message(Str);
			}
		}

		public void In_Message(String Str) {
			stp = new StringTokenizer(Str, "/");
			String Protocol = stp.nextToken();
			String First_Message = stp.nextToken();

			if (Protocol.equals("Chat_Message")) {
				String context = stp.nextToken();
				Broad_Cast("Chat_Message/" + First_Message + "/" + context);
			}
		}

		public void run() // ������ ����
		{

			while (true) {
				try {
					String msg = dis.readUTF();
					System.out.println("Ŭ���̾�Ʈ�� ���� �޼��� : " + msg);
					In_Message(msg);
					Connect_ListSB.setValue(Connect_ListSB.getMaximum());
				} catch (IOException e) {
					try {
						dos.close();
						dis.close();
						��������.close();
						Conneting_User.removeElement(this); // �������� ���� ��ü�� ���Ϳ��� �����
						�����_����Ʈ.append(Conneting_User.size() + " : ���� ���Ϳ� ����� ����� ��\n");
						�����_����Ʈ.append("����� ���� ������ �ڿ� �ݳ�\n");
						break;
					} catch (Exception a) {
					}
				}
			}
		}
	}
}