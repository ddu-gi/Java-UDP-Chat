import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Chat_ReceviSend_Thread extends Thread {
	
	Socket Socket; 
	HashMap Idlist = new HashMap();
	HashMap filetrans = new HashMap();
	BufferedReader br;
	String NickName;
	String filereciver;
	long filelength;

	public Chat_ReceviSend_Thread(Socket chatSocket, HashMap Idlist, HashMap filetrans) { //생성자
		this.Socket = chatSocket;
		this.Idlist = Idlist;
		this.filetrans = filetrans;
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(chatSocket.getOutputStream(),"utf-8")), true); //소켓 스트림 writer를 pw로 선언
			br = new BufferedReader(new InputStreamReader(Socket.getInputStream(), "utf-8")); //소켓 스트림 reader를 br로 선언
			DataOutputStream dos = new DataOutputStream(Socket.getOutputStream()); //소켓의 데이터 output을 dos로 선언
			
			NickName = br.readLine(); //생성자가 호출됨과 동시에 클라이언트로부터 닉네임을 전송받음
			synchronized (Idlist) { //hashmap의 동기화를 하여 전송받은 닉네임과 소켓 스트림 writer를 입력
				Idlist.put(this.NickName, pw);
			}
			synchronized (filetrans) { //hashmap의 동기화를 하여 전송받은 닉네임과 소켓 데이터 output를 입력
				filetrans.put(this.NickName, dos);
			}
			System.out.println(NickName + "님이 접속하셨습니다.");
			broadcast(NickName + "님이 접속하셨습니다."); //현재 접속중인 사용자들이게 새로운 사용자의 접속을 알림
			ListSend(); //현재 접속중인 사용자들이게 새로운 사용자가 접속하여 리스트 변경에 대한 함수를 호출

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String contents = null; //전송된 메세지를 받을 변수
			while ((contents = br.readLine()) != null) {
				if (contents.indexOf("/to ") == 0) { // 귓속말일 경우
					whisper(contents); //whisper함수에 메세지 전달
				} 
				else if (contents.contains("changenickname|+%)@@)!(@$)")) { // 닉네임 변경일 경우					
					String Change = contents.replace("changenickname|+%)@@)!(@$)", "");
					synchronized (Idlist) {
						broadcast(NickName + "님이 대화명을 " + Change+ "(으)로 바꾸셨습니다."); //닉네임 변경에 대한 내용을 방송
						Idlist.put(Change, Idlist.get(NickName)); //변경된 닉네임을 Idlist에 다시 입력
						Idlist.remove(NickName); //기존의 닉네임을 삭제
						NickName = Change; //닉네임 변수에 변경된 닉네임 저장
						ListSend(); //변경후 리스트를 클라이언트에 전송
					}
				} 
				else if (contents.contains("FileTrans")) { //파일전송일 경우
					FileSendalarm(contents); // 파일을 받는이에게 파일전송 알림
					byte[] buf = new byte[1024]; //파일을 받고 전송에 사용될 byte buf 변수 정의
					Object obj = filetrans.get(filereciver); //파일을 받는 사람의 dos를 찾아 obj에 저장
					DataOutputStream dos = (DataOutputStream) obj; //찾은 obj로 DataOutputStream 선언 
					BufferedInputStream bis = new BufferedInputStream( new DataInputStream( Socket.getInputStream() ) ); 
					//클라이언트로부터 파일을 받을  BufferedInputStream 선언
					int readByte = 0; //읽은 바이트의 길이를 저장할 변수 선언
					long totalLength = 0; //읽은 파일의 전체 길이를 저장할 변수 선언
					while ( true ) { 
						readByte =  bis.read(buf); // 보낸이에게서 파일 받음
						totalLength += readByte; //받은 파일 길이를 전체 파일길이 변수에 더함
						/*System.out.println("받은 buf : " + buf);
						System.out.println("if전 readByte : "+ readByte);*/
						dos.write(buf, 0, readByte); // 파일을 전송받은 사람에게 전송받은 파일을 전송
						dos.flush();
						//System.out.println("if후 readByte : "+ readByte);	
					
						if( totalLength == filelength ) //입력받은 전체의 바이트 길이가 전송되고 보내야될 파일의 길이와 같으면 반복문 종료
							break;
											
					}
					//System.out.println("while종료");
		
				} 
				else { // 귓속말, 닉네임 변경, 파일전송이 아닌경우는 메시지 전달이기에 broadcast함수로 모든 사용자에게 메시지 전달
					broadcast(NickName + " : " + contents); 
				}
			}
		} catch (Exception e) {
			//System.out.println(NickName + "님이 접속을 종료하셨습니다.");
		} finally { //소켓연결이 끈기고 접속을 종료할경우
			synchronized (Idlist) { //Idlist에서 접속종료한 닉네임의 자료를 삭제
				Idlist.remove(NickName);
			}
			synchronized (filetrans) { //filetrans에서 접속종료한 닉네임의 자료를 삭제
				filetrans.remove(NickName);
			}
			broadcast(NickName + "님이 접속을 종료하셨습니다."); //모든 접속자에게 해당 접속종료 사용자에 대한 접속종료 메세지를 전송
			ListSend(); //접속종료에따른 리스트 갱신을 위해 리스트 전송
		}
	}

	private void broadcast(String msg) { //전체 사용자에게 방송
		synchronized (Idlist) { 
			Collection list = Idlist.values(); //Collection list 변수에 Idlist hashmap의 값들을 저장
			Iterator iter = list.iterator(); //list변수를 Iterator로 선언
			while (iter.hasNext()) {
				PrintWriter pw = (PrintWriter) iter.next(); //iter의 pw값을 pw로 선언
				pw.println(msg); //pw로 찾은 사용자에게 메세지 전달
				pw.flush();
			}
		}

	}

	private void whisper(String msg) { //귓속말함수
		int start = msg.indexOf(" ") + 1; // 처음으로 시작하는 공백의 다음을 start지점으로 지정
		int end = msg.indexOf(" ", start); // 처음의 공백부터 시작지점까지의 
		if (end != -1) {
			String to = msg.substring(start, end); //메세지에서 보내는유저를 잘라냄
			String getmsg = msg.substring(end + 1); //메세지에서 보낼 메시지를 잘라냄
			Object obj = Idlist.get(to); //받는사용자의 pw값을 찾아 obj에 저장
			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj; //찾은 obj로 부터 pw를 새로 선언
				pw.println(NickName + "님으로 부터의 귓속말 : " + getmsg); //보내는이의 이름과 함께 받는 이에게 메세지를 전송
				pw.flush();
			}
		}

	}

	private void FileSendalarm(String msg) { // 파일전송받을 사람에게 알림 함수
		String str[] = msg.split("[(|)]"); // 구분자 |로 나눠진 메세지를 구분
		filereciver = str[1]; //파일을 전송 받을 유저명을 저장
		String filename = str[2]; //파일의 이름을 저장
		String delim = str[0]; //파일전송인지 알수있는 프로토콜 값을 저장
		filelength = Long.parseLong(str[3]); //전송받을 파일의 길이값을 저장

		Object obj = Idlist.get(filereciver); //파일을 전송받을 유저의 pw를 찾아 obj에 저장
		if (obj != null) {
			PrintWriter pw = (PrintWriter) obj; //찾은 obj로 부터 pw를 새로 선언
			pw.println(delim + "|" + NickName + "|" + filename + "|" + filelength); // 파일전송 프로토콜과 각종 정보들을 구분자 | 로 붙여 전송
			pw.flush();
		}
	}

	private void ListSend() { //갱신된 사용자 리스트를 사용자들에 전송하는 함수
		synchronized (Idlist) {
			String nameList = null; //사용자 리스트를 저장할 변수 선언
			Collection list = Idlist.values(); //Collection list 변수에 Idlist hashmap의 값들을 저장
			Iterator valueiter = list.iterator(); //list변수를 Iterator로 선언
			int i = 0;
			int size = list.size(); //사용자수를 저장
			Set keySet = Idlist.keySet(); //키셋 선언
			Iterator keyiter = keySet.iterator(); //키셋으로부터 키들의 집합을 iterator로 선언
			ArrayList keyList = new ArrayList(); //keyList라는 ArrayList선언 
			while (keyiter.hasNext()) {
				keyList.add(keyiter.next()); //keyList라는 ArrayList에 key값을 add
				if (i == 0) { //처음 키일경우
					nameList = keyList.get(i) + "|";
				} else if (i == size - 1) { //마지막 키 일경우
					nameList += keyList.get(i);
				} else { //그 이외의 경우
					nameList += keyList.get(i) + "|";
				}
				i++;
			}
			while (valueiter.hasNext()) { 
				PrintWriter pw = (PrintWriter) valueiter.next(); //valueiter로 부터 값 pw를 찾아 pw로 선언 
				pw.println(nameList + "listsend|+%)@@)!(@$)"); //리스트 변경 프로토콜을 유저리스트에 붙여 각 사용자들에게 전송
				pw.flush();
			}
		}
	}
}
