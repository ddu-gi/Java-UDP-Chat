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

	JLabel 서버_상태 = new JLabel("서버 상태 :");
	JLabel 서버_포트 = new JLabel("포트 번호 :");

	JButton 서버_시작 = new JButton("열기");
	JButton 서버_종료 = new JButton("닫기");

	JTextArea 사용자_리스트 = new JTextArea();
	JScrollPane 사용자_리스트_목록 = new JScrollPane();
	JTextField 포트_입력 = new JTextField();

	JScrollBar Connect_ListSB = 사용자_리스트_목록.getVerticalScrollBar();
	StringTokenizer stp;

	Chatserver() {
		init();
		start();
	}

	public void init() {
		Font Font1 = new Font("돋움", Font.BOLD, 13);

		this.setTitle("Chatserver");
		this.setSize(200, 300);

		Dimension dimen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimen1 = this.getSize();
		int xpos = (int) (dimen.getWidth() / 2 - dimen1.getWidth() / 2);
		int ypos = (int) (dimen.getHeight() / 2 - dimen1.getHeight() / 2);

		this.setLocation(xpos, ypos);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false); // 창 크기 변경 x
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		서버_상태.setFont(Font1);
		서버_시작.setFont(Font1);
		서버_종료.setFont(Font1);
		사용자_리스트.setFont(Font1);
		서버_포트.setFont(Font1);
		포트_입력.setFont(Font1);

		서버_상태.setBounds(40, 5, 230, 30);
		서버_상태.setVisible(true);
		this.add(서버_상태);

		서버_포트.setBounds(35, 230, 200, 20);
		서버_포트.setVisible(true);
		this.add(서버_포트);

		포트_입력.setBounds(110, 230, 50, 20);
		포트_입력.setVisible(true);
		this.add(포트_입력);

		서버_시작.setBounds(15, 180, 80, 30);
		서버_시작.setVisible(true);
		this.add(서버_시작);

		서버_종료.setBounds(100, 180, 80, 30);
		서버_종료.setVisible(true);
		this.add(서버_종료);

		사용자_리스트.setBounds(15, 45, 165, 120);
		사용자_리스트.setVisible(true);
		사용자_리스트.setEnabled(false);
		this.add(사용자_리스트);

		사용자_리스트_목록.setViewportView(사용자_리스트);
		사용자_리스트_목록.setVisible(true);
		사용자_리스트_목록.setBounds(15, 45, 165, 120);
		this.add(사용자_리스트_목록);

		서버_종료.setEnabled(false);
		포트_입력.setText("9000");
	}

	public void start() {
		서버_시작.addActionListener(this);
		서버_종료.addActionListener(this);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Chatserver();
	}

	@Override
	public void actionPerformed(ActionEvent Click) {
		// TODO Auto-generated method stub
		if (Click.getSource() == 서버_시작) {
			Play_Sound("Sound/Speech On.wav");
			if (포트_입력.getText().equals("") || 포트_입력.getText().length() == 0 || 포트_입력.getText().length() < 4) {
				사용자_리스트.append("포트 번호 입력해주세요.\n");
				포트_입력.requestFocus();
			} else {
				사용자_리스트.append("서버가 열렸습니다.\n");
				서버_상태.setText("서버 상태 : 열림");
				서버_종료.setEnabled(true);
				포트_입력.setEnabled(false);

				int Port_Number = Integer.parseInt(포트_입력.getText());
				Server_Start(Port_Number);
			}
		}

		if (Click.getSource() == 서버_종료) {
			Play_Sound("Sound/Speech Off.wav");
			사용자_리스트.append("서버가 닫혔습니다.\n");
			서버_상태.setText("서버 상태 : 닫힘");

			try {
				Server.close();
			} catch (IOException e) {
				사용자_리스트.append("서버가 열려있지 않습니다.\n");
				e.printStackTrace();
			}

			서버_시작.setEnabled(true);
			서버_종료.setEnabled(false);
		}
	}

	public void Server_Start(int Port_Number) {
		try {
			Server = new ServerSocket(Port_Number);
			서버_시작.setEnabled(false);

			if (Server != null) {
				사용자_리스트.append("연결 대기 상태 중입니다.\n");
				Connection_Server();
			}

		} catch (IOException e) {
			사용자_리스트.append("이미 사용 중인 소켓입니다.");
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
						사용자_리스트.append("사용자 접속 대기 중...\n");
						Socket = Server.accept();
						사용자_리스트.append("사용자 접속 완료!\n");

						User_Count++;
						User_Info User = new User_Info(Socket, User_Count);

						User.start();
					}

					catch (IOException e) {
						사용자_리스트.append("Accept 에러 발생\n");
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

		private String 닉네임;
		private int 유저번호;
		private Socket 유저소켓;

		User_Info(Socket S, int ID) {
			this.유저소켓 = S;
			this.유저번호 = ID;

			User_Socket_NetWork();
		}

		public void User_Socket_NetWork() {
			try {
				is = 유저소켓.getInputStream();
				dis = new DataInputStream(is);
				os = 유저소켓.getOutputStream();
				dos = new DataOutputStream(os);

				닉네임 = dis.readUTF();

				사용자_리스트.append("접속자 ID : " + 닉네임 + " " + 유저번호 + "\n");
				Broad_Cast("New_User/" + 닉네임);

				for (int i = 0; i < Conneting_User.size(); i++) {
					User_Info u = (User_Info) Conneting_User.elementAt(i);
					To_Client_Message("Old_User/" + u.닉네임);
				}

				Conneting_User.add(this);
			} catch (Exception e) {
				사용자_리스트.append("스트림 셋팅 에러\n");
			}
		}

		public void To_Client_Message(String Str) {
			try {
				dos.writeUTF(Str);
			} catch (IOException e) {
				사용자_리스트.append("메시지 송신 에러 발생\n");
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

		public void run() // 스레드 정의
		{

			while (true) {
				try {
					String msg = dis.readUTF();
					System.out.println("클라이언트로 부터 메세지 : " + msg);
					In_Message(msg);
					Connect_ListSB.setValue(Connect_ListSB.getMaximum());
				} catch (IOException e) {
					try {
						dos.close();
						dis.close();
						유저소켓.close();
						Conneting_User.removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
						사용자_리스트.append(Conneting_User.size() + " : 현재 벡터에 담겨진 사용자 수\n");
						사용자_리스트.append("사용자 접속 끊어짐 자원 반납\n");
						break;
					} catch (Exception a) {
					}
				}
			}
		}
	}
}