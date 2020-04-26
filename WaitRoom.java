import java.util.*;

class WaitRoom {
	private static final int �ִ��_�� = 10;
	private static final int �ִ�����_�� = 50;
	private static final int �������_���� = 3001;
	private static final int ������ȭ = 3002;
	private static final int ����ȭ = 3011;
	private static final int ����ڼ���ȭ = 3021;
	private static final int Ʋ����й�ȣ = 3022;

	private static Vector ��������, roomVector;
	private static Hashtable �����ؽ�, roomHash;

	private static int userCount;
	private static int roomCount;

	static {
		�������� = new Vector(�ִ�����_��);
		roomVector = new Vector(�ִ��_��);
		�����ؽ� = new Hashtable(�ִ�����_��);
		roomHash = new Hashtable(�ִ��_��);
		userCount = 0;
		roomCount = 0;
	}

	public WaitRoom() {
	}

	public synchronized int addUser(String id, ServerThread client) {
		if (userCount == �ִ�����_��)
			return ������ȭ;

		Enumeration ids = ��������.elements();
		while (ids.hasMoreElements()) {
			String tempID = (String) ids.nextElement();
			if (tempID.equals(id))
				return �������_����;
		}
		Enumeration rooms = roomVector.elements();
		while (rooms.hasMoreElements()) {
			ChatRoom tempRoom = (ChatRoom) rooms.nextElement();
			if (tempRoom.checkUserIDs(id))
				return �������_����;
		}

		��������.addElement(id);
		�����ؽ�.put(id, client);
		client.severthread_ID = id;
		client.severthread_room_Number = 0;
		userCount++;

		return 0;
	}

	public synchronized void delUser(String id) {
		��������.removeElement(id);
		�����ؽ�.remove(id);
		userCount--;
	}

	public synchronized String getRooms() {
		StringBuffer room = new StringBuffer();
		String rooms;
		Integer roomNum;
		Enumeration enu = roomHash.keys();
		while (enu.hasMoreElements()) {
			roomNum = (Integer) enu.nextElement();
			ChatRoom tempRoom = (ChatRoom) roomHash.get(roomNum);
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
		Enumeration enu = ��������.elements();
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
		if (roomCount == �ִ��_��)
			return ����ȭ;

		roomVector.addElement(room);
		roomHash.put(new Integer(ChatRoom.room_Number), room);
		roomCount++;
		return 0;
	}

	public String getWaitRoomInfo() {
		StringBuffer roomInfo = new StringBuffer();
		roomInfo.append(getRooms());
		roomInfo.append(":");
		roomInfo.append(getUsers());
		return roomInfo.toString();
	}

	public synchronized int joinRoom(String id, ServerThread client, int room_Number, String password) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		if (room.locked()) {
			if (room.checkPassword(password)) {
				if (!room.addUser(id, client)) {
					return ����ڼ���ȭ;
				}
			} else {
				return Ʋ����й�ȣ;
			}
		} else if (!room.addUser(id, client)) {
			return ����ڼ���ȭ;
		}
		��������.removeElement(id);
		�����ؽ�.remove(id);

		return 0;
	}

	public String getRoomInfo(int room_Number) {
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getUsers();
	}

	public synchronized boolean quitRoom(String id, int room_Number, ServerThread client) {
		boolean returnValue = false;
		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		if (room.delUser(id)) {
			roomVector.removeElement(room);
			roomHash.remove(roomNum);
			roomCount--;
			returnValue = true;
		}
		��������.addElement(id);
		�����ؽ�.put(id, client);
		return returnValue;
	}

	public synchronized Hashtable getClients(int room_Number) {
		if (room_Number == 0)
			return �����ؽ�;

		Integer roomNum = new Integer(room_Number);
		ChatRoom room = (ChatRoom) roomHash.get(roomNum);
		return room.getClients();
	}
}
