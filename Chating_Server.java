import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class Chating_Server {
	public static void main(String[] args) {
		try {
			HashMap Idlist = new HashMap(); //ä�� �г����� Ű�� ����Ͽ� ������ writer��ü�� ���� Idlist hashmap����
			HashMap filetrans = new HashMap(); //ä�� �г����� Ű�� ����Ͽ� ������ �������� ��Ʈ���� ���� filetrans hashmap����
			Scanner si = new Scanner(System.in);
			System.out.print("Chating Port �Է� : ");
			String port = si.next();
			ServerSocket ChatServerSocket = new ServerSocket(Integer.parseInt(port)); //�Էµ� port��ȣ�κ��� �������� ����
			System.out.println("ChatServerSocket ����");
			while(true){
				Calendar time = Calendar.getInstance(); //���ӵ� �ð� Ȯ�� time ��ü ����
				Socket Socket= ChatServerSocket.accept(); //Ŭ���̾�Ʈ ���Ӵ��
				System.out.println(time.getTime());
				System.out.println("ChatSocket����"+Socket.getInetAddress());
				new Chat_ReceviSend_Thread(Socket,Idlist,filetrans).start(); //���ӵ� Ŭ���̾�Ʈ�� ���ϰ� �ΰ��� hashmap�� ��� Chat_ReceviSend_Thread ����
			}
		}
		catch (Exception e) {
		}
	}
}
