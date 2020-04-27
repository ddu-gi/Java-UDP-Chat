import java.util.*;

class WaitRoom {
	private static final int 최대방수 = 10;
	private static final int 최대사용자 = 100;

	private static final int 사용중 = 9999;
	private static final int 서버포화 = 9998;
	private static final int 방수포화 = 9997;
	private static final int 방가득참 = 9996;
	private static final int 틀린비밀번호 = 9995;

	private static Vector 유저벡터, 방벡터;
	private static Hashtable 유저해쉬, 방해쉬;

	private static int 유저수;
	private static int 방수;

	static {
		유저벡터 = new Vector(최대사용자);
		방벡터 = new Vector(최대방수);
		유저해쉬 = new Hashtable(최대사용자);
		방해쉬 = new Hashtable(최대방수);
		유저수 = 0;
		방수 = 0;
	}

	public WaitRoom() {
	}

	public synchronized int addUser(String id, ServerThread client) {
		if (유저수 == 최대사용자)
			return 서버포화;

		Enumeration ids = 유저벡터.elements();
		while (ids.hasMoreElements()) {
			String tempID = (String) ids.nextElement();
			if (tempID.equals(id))
				return 사용중;
		}
		Enumeration rooms = 방벡터.elements();
		while (rooms.hasMoreElements()) {
			ChatRoom tempRoom = (ChatRoom) rooms.nextElement();
			if (tempRoom.checkUserIDs(id))
				return 사용중;
		}

		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		client.server_threadID = id;
		client.server_threadroom_Number = 0;
		유저수++;

		return 0;
	}

	public synchronized void delUser(String id) {
		유저벡터.removeElement(id);
		유저해쉬.remove(id);
		유저수--;
	}

	public synchronized String getRooms() {
		StringBuffer room = new StringBuffer();
		String rooms;
		Integer roomNum;
		Enumeration enu = 방해쉬.keys();
		while (enu.hasMoreElements()) {
			roomNum = (Integer) enu.nextElement();
			ChatRoom tempRoom = (ChatRoom) 방해쉬.get(roomNum);
			room.append(String.valueOf(roomNum));
			room.append(" = ");
			room.append(tempRoom.toString());
			room.append("'");
		}
		try {
			rooms = new String(room);
			rooms = rooms.substring(0, rooms.length() - 1);
		} catch (StringIndexOutOfBoundsException e) {
			return "empty";
		}
		return rooms;
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

	public synchronized int addRoom(ChatRoom room) {
		if (방수 == 최대방수)
			return 방수포화;

		방벡터.addElement(room);
		방해쉬.put(new Integer(ChatRoom.room_Number), room);
		방수++;
		return 0;
	}

	public String getWaitRoomInfo() {
		StringBuffer roomInfo = new StringBuffer();
		roomInfo.append(getRooms());
		roomInfo.append(" : ");
		roomInfo.append(getUsers());
		return roomInfo.toString();
	}

	public synchronized int joinRoom(String id, ServerThread client, int room_Number, String password) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		if (room.isLocked()) {
			if (room.checkPassword(password)) {
				if (!room.addUser(id, client)) {
					return 방가득참;
				}
			} else {
				return 틀린비밀번호;
			}
		} else if (!room.addUser(id, client)) {
			return 방가득참;
		}
		유저벡터.removeElement(id);
		유저해쉬.remove(id);

		return 0;
	}

	public String getRoomInfo(int room_Number) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		return room.getUsers();
	}

	public synchronized boolean quitRoom(String id, int room_Number, ServerThread client) {
		boolean returnValue = false;
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		if (room.delUser(id)) {
			방벡터.removeElement(room);
			방해쉬.remove(roomNum);
			방수--;
			returnValue = true;
		}
		유저벡터.addElement(id);
		유저해쉬.put(id, client);
		return returnValue;
	}

	public synchronized Hashtable getClients(int room_Number) {
		if (room_Number == 0)
			return 유저해쉬;

		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) 방해쉬.get(roomNum);
		return room.getClients();
	}
}