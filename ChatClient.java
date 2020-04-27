import javax.swing.*;

public class ChatClient {
	public static String getLoginID() {
		String LoginID = "";
		try {
			while (LoginID.equals("")) {
				LoginID = JOptionPane.showInputDialog("�г����� ���ּ���(�ߺ��� �г��� ���� �Ұ�)");
			}
		} catch (NullPointerException e) {
			System.exit(0);
		}
		return LoginID;
	}

	public static void main(String args[]) {
		String id = getLoginID();
		try {
			if (args.length == 0) {
				ClientThread thread = new ClientThread();
				thread.start();
				thread.�α��ο�û(id);
			} else if (args.length == 1) {
				ClientThread thread = new ClientThread(args[0]);
				thread.start();
				thread.�α��ο�û(id);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
