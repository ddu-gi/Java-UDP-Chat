import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Chatclient extends JFrame implements ActionListener {

	Font Fonti = new Font("돋움", Font.BOLD, 13);

	static public String 사용자_닉네임 = "";
	static public String 사용자_아이피 = "";
	static public int 사용자_포트;

	JButton Login = new JButton("로그인");
	JTextField NickName = new JTextField();
	JLabel NickName_Label = new JLabel("닉네임");


	JTextField Port_Input = new JTextField();
	JLabel Port_Label = new JLabel("포트 번호");

	JTextField IP_Input = new JTextField();
	JLabel IP_Label = new JLabel("아이피 주소");

	JTextField LobbyChatInput = new JTextField();
	JTextArea List_Chat_Area = new JTextArea();
	JList Resent_List = new JList();

	JLabel User_List_Label = new JLabel("유저 목록");
	JLabel User_Chat_Label = new JLabel("채팅창");

	JScrollPane Resent_List_Scroll = new JScrollPane(Resent_List);
	JScrollPane List_Chat_AreaSP = new JScrollPane();

	JScrollBar Resent_List_ScrollBar = Resent_List_Scroll.getVerticalScrollBar();
	JScrollBar List_Chat_AreaSB = List_Chat_AreaSP.getVerticalScrollBar();

	Chatclient() {
		Set_Font();

		this.setTitle("Chatclient");
		this.setSize(200, 320);
		this.setVisible(true);
		this.setLayout(null);
		this.setResizable(false); // 창 크기 변경 x
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창닫기

		Dimension dimen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dimen1 = this.getSize();
		int xpos = (int) (dimen.getWidth() / 2 - dimen1.getWidth() / 2);
		int ypos = (int) (dimen.getHeight() / 2 - dimen1.getHeight() / 2);
		this.setLocation(xpos, ypos);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image img = toolkit.getImage("image/open.jpg");
		this.setIconImage(img);


		NickName_Label.setBounds(20, 30, 150, 21);
		NickName_Label.setVisible(true);
		this.add(NickName_Label);

		NickName = new JTextField();
		NickName.setBounds(20, 60, 150, 21);
		this.add(NickName);
		NickName.setColumns(10);

		Port_Label.setBounds(20, 90, 150, 21);
		Port_Label.setVisible(true);
		this.add(Port_Label);

		Port_Input = new JTextField();
		Port_Input.setBounds(20, 120, 150, 21);
		this.add(Port_Input);
		Port_Input.setColumns(10);

		IP_Label.setBounds(20, 150, 150, 21);
		IP_Label.setVisible(true);
		this.add(IP_Label);

		IP_Input = new JTextField();
		IP_Input.setBounds(20, 180, 150, 21);
		this.add(IP_Input);
		IP_Input.setColumns(18);

		Login.setBounds(50, 230, 100, 40);
		Login.setVisible(true);
		Login.addActionListener(this);

		Port_Input.setText("9000");
		IP_Input.setText("127.0.0.1");
		this.add(Login);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Chatclient();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == Login) {
			Play_Sound("Sound/Speech On.wav");

			if (NickName.getText().equals("") || NickName.getText().length() == 0) {
			} else if (Port_Input.getText().equals("") || Port_Input.getText().length() == 0) {
			} else if (IP_Input.getText().equals("") || IP_Input.getText().length() == 0) {
			} else {
				사용자_포트 = Integer.parseInt(Port_Input.getText().trim());
				사용자_아이피 = IP_Input.getText().trim();
				사용자_닉네임 = NickName.getText().trim();
				this.setVisible(false);
				new Login_Connect(사용자_닉네임, 사용자_아이피, 사용자_포트);
			}
		}
	}

	public class Login_Connect extends JFrame {
		private String Name_;
		private String IP_;
		private int Port_;

		private Socket socket; // 연결소켓
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		Vector User_List = new Vector();

		StringTokenizer stp;

		Login_Connect(String Name, String IP, int Port) {
			this.Name_ = Name;
			this.IP_ = IP;
			this.Port_ = Port;

			Init();
			Start();
			Net_work();
		}

		public void Init() {
			this.setSize(400, 330);
			this.setLayout(null);
			this.setVisible(true);
			this.setResizable(false);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setTitle("Java_Chtting_Client");

			Dimension dimen = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension dimen1 = this.getSize();
			int xpos = (int) (dimen.getWidth() / 2 - dimen1.getWidth() / 2);
			int ypos = (int) (dimen.getHeight() / 2 - dimen1.getHeight() / 2);
			this.setLocation(xpos, ypos);

			User_List_Label.setBounds(20, 15, 150, 30);
			this.add(User_List_Label);

			User_Chat_Label.setBounds(200, 15, 150, 30);
			this.add(User_Chat_Label);

			Resent_List_Scroll.setBounds(20, 55, 150, 200);
			this.add(Resent_List_Scroll);

			List_Chat_Area.setBounds(200, 55, 180, 170);
			this.add(List_Chat_Area);

			List_Chat_AreaSP.setViewportView(List_Chat_Area);
			List_Chat_AreaSP.setBounds(200, 55, 180, 170);
			List_Chat_AreaSP.setVisible(true);

			LobbyChatInput.setBounds(200, 235, 180, 20);
			LobbyChatInput.setColumns(40);

			this.add(LobbyChatInput);
			this.add(List_Chat_AreaSP);
		}

		public void Start() {
			LobbyChatInput.addKeyListener(new KeyListener() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == 10) {
						if (LobbyChatInput.hasFocus() && LobbyChatInput.getText().trim().length() != 0) {
							To_Server_Message(
									"Chat_Message/" + 사용자_닉네임 + "/" + LobbyChatInput.getText().trim());
							LobbyChatInput.setText("");
							LobbyChatInput.requestFocus();
						}

						else {
							LobbyChatInput.setText("");
							LobbyChatInput.requestFocus();
						}
					}
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}
			});
		}

		public void Net_work() {
			try {
				socket = new Socket(IP_, Port_);
				if (socket != null) {
					Connection();
				}
			} catch (UnknownHostException e) {
			} catch (IOException e) {
				List_Chat_Area.append("소켓 접속 에러!!\n");
			}

		}

		public void Connection() {

			try {
				is = socket.getInputStream();
				dis = new DataInputStream(is);

				os = socket.getOutputStream();
				dos = new DataOutputStream(os);

			} catch (IOException e) {
				List_Chat_Area.append("스트림 설정 에러!!\n");
			}

			User_List.add(Name_);
			Resent_List.setListData(User_List);

			try {
				dos.writeUTF(Name_);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Thread Conneting_Thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							String msg = dis.readUTF();
							In_Message(msg);
							List_Chat_AreaSB.setValue(List_Chat_AreaSB.getMaximum());
							System.out.println("서버로부터 수신된 메세지 : " + msg);
						} catch (IOException e) {
							List_Chat_Area.append("메세지 수신 에러!!\n");
							try {
								os.close();
								is.close();
								dos.close();
								dis.close();
								socket.close();
								break;
							} catch (IOException e1) {

							}
						}
					}
				}
			});
			Conneting_Thread.start();
		}

		public void To_Server_Message(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				List_Chat_Area.append("메세지 송신 에러!!\n");
			}
		}

		public void In_Message(String Str) {
			stp = new StringTokenizer(Str, "/");
			String Protocol = stp.nextToken();
			String First_Message = stp.nextToken();

			if (Protocol.equals("New_User")) {
				User_List.add(First_Message);
				Resent_List.setListData(User_List);
				Play_Sound("Sound/Speech On.wav");
				List_Chat_Area.append(First_Message + " 님께서 입장하셨습니다.\n");
			}

			else if (Protocol.equals("Old_User")) {
				User_List.add(First_Message);
				Resent_List.setListData(User_List);
			}

			else if (Protocol.equals("Chat_Message")) {
				Play_Sound("Sound/Speech On.wav");
				String context = stp.nextToken();
				List_Chat_Area.append(First_Message + " : " + context + "\n");
			}
		}
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

	public void Set_Font() {
		NickName_Label.setFont(Fonti);
		IP_Input.setFont(Fonti);
		Port_Input.setFont(Fonti);
		Login.setFont(Fonti);
		IP_Label.setFont(Fonti);
		Port_Label.setFont(Fonti);
		List_Chat_Area.setFont(Fonti);
		Resent_List.setFont(Fonti);
		User_List_Label.setFont(Fonti);
		User_Chat_Label.setFont(Fonti);
	}
}
