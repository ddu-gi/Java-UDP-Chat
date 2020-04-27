import javax.swing.*;

public class ChatClient {
	public static String getloginID() {
		String loginID = "";
		try {
			while (loginID.equals("")) {
				loginID = JOptionPane.showInputDialog("�г����� ���ּ���(�ߺ��� �г��� ���� �Ұ�)");
			}
		} catch (NullPointerException e) {
			System.exit(0);
		}
		return loginID;
	}

	public static void main(String args[]) {
		String id = getloginID();
		try {
			if (args.length == 0) {
				ClientThread thread = new ClientThread();
				thread.start();
				thread.requestlogin(id);
			} else if (args.length == 1) {
				ClientThread thread = new ClientThread(args[0]);
				thread.start();
				thread.requestlogin(id);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
