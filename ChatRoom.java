import java.util.*;

class ChatRoom {
	public static int room_Number = 0;
	private Vector 유저벡터;
	private Hashtable 유저해쉬;
	private String roomName;
	private int 방최대사용자;
	private int 방사용자;
	private boolean isLock;
	private String password;
	private String admin;

	public ChatRoom(String roomName, int 방최대사용자, boolean isLock, String password, String admin) {
		room_Number++;
		this.roomName = roomName;
		this.방최대사용자 = 방최대사용자;
		this.방사용자 = 0;
		this.isLock = isLock;
		this.password = password;
		this.admin = admin;
		this.유저벡터 = new Vector(방최대사용자);
		this.유저해쉬 = new Hashtable(방최대사용자);
	}

	public boolean addUser(String id, ServerThread client) {
		if (방사용자 == 방최대사용자) {
			return false;
		}
		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		방사용자++;
		return true;
	}

	public boolean checkPassword(String passwd) {
		return password.equals(passwd);
	}

	public boolean checkUserIDs(String id) {
		Enumeration ids = 유저벡터.elements();
		while (ids.hasMoreElements()) {
			String tempId = (String) ids.nextElement();
			if (tempId.equals(id))
				return true;
		}
		return false;
	}

	public boolean isLocked() {
		return isLock;
	}

	public boolean delUser(String id) {
		유저벡터.removeElement(id);
		유저해쉬.remove(id);
		방사용자--;
		return 유저벡터.isEmpty();
	}

	public synchronized String getUsers() {
		StringBuffer id = new StringBuffer();
		String ids;
		Enumeration enu = 유저벡터.elements();
		while (enu.hasMoreElements()) {
			id.append(enu.nextElement());
			id.append("'");
		}
		try {
			ids = new String(id);
			ids = ids.substring(0, ids.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}
		return ids;
	}

	public Hashtable getClients() {
		return 유저해쉬;
	}

	public String toString() {
		StringBuffer room = new StringBuffer();
		room.append(roomName);
		room.append(" = ");
		room.append(String.valueOf(방사용자));
		room.append(" = ");
		room.append(String.valueOf(방최대사용자));
		room.append(" = ");
		if (isLock) {
			room.append("비공개");
		} else {
			room.append("공개");
		}
		room.append(" = ");
		room.append(admin);
		return room.toString();
	}

	public static synchronized int getroom_Number() {
		return room_Number;
	}
}
