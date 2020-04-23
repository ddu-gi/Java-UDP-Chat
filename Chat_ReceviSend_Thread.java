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

	public Chat_ReceviSend_Thread(Socket chatSocket, HashMap Idlist, HashMap filetrans) { //������
		this.Socket = chatSocket;
		this.Idlist = Idlist;
		this.filetrans = filetrans;
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(chatSocket.getOutputStream(),"utf-8")), true); //���� ��Ʈ�� writer�� pw�� ����
			br = new BufferedReader(new InputStreamReader(Socket.getInputStream(), "utf-8")); //���� ��Ʈ�� reader�� br�� ����
			DataOutputStream dos = new DataOutputStream(Socket.getOutputStream()); //������ ������ output�� dos�� ����
			
			NickName = br.readLine(); //�����ڰ� ȣ��ʰ� ���ÿ� Ŭ���̾�Ʈ�κ��� �г����� ���۹���
			synchronized (Idlist) { //hashmap�� ����ȭ�� �Ͽ� ���۹��� �г��Ӱ� ���� ��Ʈ�� writer�� �Է�
				Idlist.put(this.NickName, pw);
			}
			synchronized (filetrans) { //hashmap�� ����ȭ�� �Ͽ� ���۹��� �г��Ӱ� ���� ������ output�� �Է�
				filetrans.put(this.NickName, dos);
			}
			System.out.println(NickName + "���� �����ϼ̽��ϴ�.");
			broadcast(NickName + "���� �����ϼ̽��ϴ�."); //���� �������� ����ڵ��̰� ���ο� ������� ������ �˸�
			ListSend(); //���� �������� ����ڵ��̰� ���ο� ����ڰ� �����Ͽ� ����Ʈ ���濡 ���� �Լ��� ȣ��

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String contents = null; //���۵� �޼����� ���� ����
			while ((contents = br.readLine()) != null) {
				if (contents.indexOf("/to ") == 0) { // �ӼӸ��� ���
					whisper(contents); //whisper�Լ��� �޼��� ����
				} 
				else if (contents.contains("changenickname|+%)@@)!(@$)")) { // �г��� ������ ���					
					String Change = contents.replace("changenickname|+%)@@)!(@$)", "");
					synchronized (Idlist) {
						broadcast(NickName + "���� ��ȭ���� " + Change+ "(��)�� �ٲټ̽��ϴ�."); //�г��� ���濡 ���� ������ ���
						Idlist.put(Change, Idlist.get(NickName)); //����� �г����� Idlist�� �ٽ� �Է�
						Idlist.remove(NickName); //������ �г����� ����
						NickName = Change; //�г��� ������ ����� �г��� ����
						ListSend(); //������ ����Ʈ�� Ŭ���̾�Ʈ�� ����
					}
				} 
				else if (contents.contains("FileTrans")) { //���������� ���
					FileSendalarm(contents); // ������ �޴��̿��� �������� �˸�
					byte[] buf = new byte[1024]; //������ �ް� ���ۿ� ���� byte buf ���� ����
					Object obj = filetrans.get(filereciver); //������ �޴� ����� dos�� ã�� obj�� ����
					DataOutputStream dos = (DataOutputStream) obj; //ã�� obj�� DataOutputStream ���� 
					BufferedInputStream bis = new BufferedInputStream( new DataInputStream( Socket.getInputStream() ) ); 
					//Ŭ���̾�Ʈ�κ��� ������ ����  BufferedInputStream ����
					int readByte = 0; //���� ����Ʈ�� ���̸� ������ ���� ����
					long totalLength = 0; //���� ������ ��ü ���̸� ������ ���� ����
					while ( true ) { 
						readByte =  bis.read(buf); // �����̿��Լ� ���� ����
						totalLength += readByte; //���� ���� ���̸� ��ü ���ϱ��� ������ ����
						/*System.out.println("���� buf : " + buf);
						System.out.println("if�� readByte : "+ readByte);*/
						dos.write(buf, 0, readByte); // ������ ���۹��� ������� ���۹��� ������ ����
						dos.flush();
						//System.out.println("if�� readByte : "+ readByte);	
					
						if( totalLength == filelength ) //�Է¹��� ��ü�� ����Ʈ ���̰� ���۵ǰ� �����ߵ� ������ ���̿� ������ �ݺ��� ����
							break;
											
					}
					//System.out.println("while����");
		
				} 
				else { // �ӼӸ�, �г��� ����, ���������� �ƴѰ��� �޽��� �����̱⿡ broadcast�Լ��� ��� ����ڿ��� �޽��� ����
					broadcast(NickName + " : " + contents); 
				}
			}
		} catch (Exception e) {
			//System.out.println(NickName + "���� ������ �����ϼ̽��ϴ�.");
		} finally { //���Ͽ����� ����� ������ �����Ұ��
			synchronized (Idlist) { //Idlist���� ���������� �г����� �ڷḦ ����
				Idlist.remove(NickName);
			}
			synchronized (filetrans) { //filetrans���� ���������� �г����� �ڷḦ ����
				filetrans.remove(NickName);
			}
			broadcast(NickName + "���� ������ �����ϼ̽��ϴ�."); //��� �����ڿ��� �ش� �������� ����ڿ� ���� �������� �޼����� ����
			ListSend(); //�������ῡ���� ����Ʈ ������ ���� ����Ʈ ����
		}
	}

	private void broadcast(String msg) { //��ü ����ڿ��� ���
		synchronized (Idlist) { 
			Collection list = Idlist.values(); //Collection list ������ Idlist hashmap�� ������ ����
			Iterator iter = list.iterator(); //list������ Iterator�� ����
			while (iter.hasNext()) {
				PrintWriter pw = (PrintWriter) iter.next(); //iter�� pw���� pw�� ����
				pw.println(msg); //pw�� ã�� ����ڿ��� �޼��� ����
				pw.flush();
			}
		}

	}

	private void whisper(String msg) { //�ӼӸ��Լ�
		int start = msg.indexOf(" ") + 1; // ó������ �����ϴ� ������ ������ start�������� ����
		int end = msg.indexOf(" ", start); // ó���� ������� �������������� 
		if (end != -1) {
			String to = msg.substring(start, end); //�޼������� ������������ �߶�
			String getmsg = msg.substring(end + 1); //�޼������� ���� �޽����� �߶�
			Object obj = Idlist.get(to); //�޴»������ pw���� ã�� obj�� ����
			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj; //ã�� obj�� ���� pw�� ���� ����
				pw.println(NickName + "������ ������ �ӼӸ� : " + getmsg); //���������� �̸��� �Բ� �޴� �̿��� �޼����� ����
				pw.flush();
			}
		}

	}

	private void FileSendalarm(String msg) { // �������۹��� ������� �˸� �Լ�
		String str[] = msg.split("[(|)]"); // ������ |�� ������ �޼����� ����
		filereciver = str[1]; //������ ���� ���� �������� ����
		String filename = str[2]; //������ �̸��� ����
		String delim = str[0]; //������������ �˼��ִ� �������� ���� ����
		filelength = Long.parseLong(str[3]); //���۹��� ������ ���̰��� ����

		Object obj = Idlist.get(filereciver); //������ ���۹��� ������ pw�� ã�� obj�� ����
		if (obj != null) {
			PrintWriter pw = (PrintWriter) obj; //ã�� obj�� ���� pw�� ���� ����
			pw.println(delim + "|" + NickName + "|" + filename + "|" + filelength); // �������� �������ݰ� ���� �������� ������ | �� �ٿ� ����
			pw.flush();
		}
	}

	private void ListSend() { //���ŵ� ����� ����Ʈ�� ����ڵ鿡 �����ϴ� �Լ�
		synchronized (Idlist) {
			String nameList = null; //����� ����Ʈ�� ������ ���� ����
			Collection list = Idlist.values(); //Collection list ������ Idlist hashmap�� ������ ����
			Iterator valueiter = list.iterator(); //list������ Iterator�� ����
			int i = 0;
			int size = list.size(); //����ڼ��� ����
			Set keySet = Idlist.keySet(); //Ű�� ����
			Iterator keyiter = keySet.iterator(); //Ű�����κ��� Ű���� ������ iterator�� ����
			ArrayList keyList = new ArrayList(); //keyList��� ArrayList���� 
			while (keyiter.hasNext()) {
				keyList.add(keyiter.next()); //keyList��� ArrayList�� key���� add
				if (i == 0) { //ó�� Ű�ϰ��
					nameList = keyList.get(i) + "|";
				} else if (i == size - 1) { //������ Ű �ϰ��
					nameList += keyList.get(i);
				} else { //�� �̿��� ���
					nameList += keyList.get(i) + "|";
				}
				i++;
			}
			while (valueiter.hasNext()) { 
				PrintWriter pw = (PrintWriter) valueiter.next(); //valueiter�� ���� �� pw�� ã�� pw�� ���� 
				pw.println(nameList + "listsend|+%)@@)!(@$)"); //����Ʈ ���� ���������� ��������Ʈ�� �ٿ� �� ����ڵ鿡�� ����
				pw.flush();
			}
		}
	}
}
