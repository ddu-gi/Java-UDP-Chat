import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class Chating_Server {
	public static void main(String[] args) {
		try {
			HashMap Idlist = new HashMap(); //채팅 닉네임을 키로 사용하여 소켓의 writer객체를 담을 Idlist hashmap선언
			HashMap filetrans = new HashMap(); //채팅 닉네임을 키로 사용하여 소켓의 파일전송 스트림을 담을 filetrans hashmap선언
			Scanner si = new Scanner(System.in);
			System.out.print("Chating Port 입력 : ");
			String port = si.next();
			ServerSocket ChatServerSocket = new ServerSocket(Integer.parseInt(port)); //입력된 port번호로부터 서버소켓 생성
			System.out.println("ChatServerSocket 생성");
			while(true){
				Calendar time = Calendar.getInstance(); //접속된 시간 확인 time 객체 생성
				Socket Socket= ChatServerSocket.accept(); //클라이언트 접속대기
				System.out.println(time.getTime());
				System.out.println("ChatSocket접속"+Socket.getInetAddress());
				new Chat_ReceviSend_Thread(Socket,Idlist,filetrans).start(); //접속된 클라이언트의 소켓과 두개의 hashmap을 담아 Chat_ReceviSend_Thread 실행
			}
		}
		catch (Exception e) {
		}
	}
}
