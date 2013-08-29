package common.config;

import java.awt.Color;

public abstract class CommonConstants
{
	// the port to connect to for socket ip comms (normal comms)
	public static int SOCKET_PORT = 2222;

	public static boolean PUBLISH_THROUGH_FTP = false;

	public static int ANIMATION_TIME_MS = 4000;

	public static boolean TINY_LAF = true;

	public static boolean USEJOGL2 = true;

	public static Color background = new Color(220, 220, 50);

	// THE TYPES OF COMMUNICATION THAT CAN BE SENT AND RECIEVED************************
	public static final short COM_REQUEST_FULL_STATE_MODEL = 6;

	public static final short COM_INCOMING_FULL_STATE_MODEL = 7;

	public static final short COM_STATE_MODEL_EVENT = 8;

	public static final short COM_TRANSIENT_STATE = 9;

	public static final short COM_ADMIN_EVENT = 10;

	public static final short COM_REQUEST_PLAYER_DETAILS = 11;

	public static final short COM_INCOMING_PLAYER_DETAILS = 12;

	public static final short COM_REQUEST_SET_PLAYER_CHOICES = 13;

	public static final short COM_INCOMING_CONFIRM_PLAYER_CHOICES = 14;

	public static final short COM_REQUEST_GAME_WORLD = 15;

	public static final short COM_INCOMING_GAME_WORLD = 16;

	public static final short COM_REQUEST_LOCALE = 17;

	public static final short COM_INCOMING_LOCALE = 18;

	public static final short COM_REQUEST_AREAS = 19;

	public static final short COM_INCOMING_AREAS = 20;

	// INITIATOR IDS, positive values are reserved for clients
	public static final int ORIGIN_SERVER = -1;

	public static final int ORIGIN_PHYSICS = -2;

	public static final int ORIGIN_MODEL_EDITOR = -3;

	public static final int ORIGIN_AI = -4;

	public static final int ORIGIN_DB = -5;

	public static int DUNE_BOARD_IMAGE_X = 1188;

	public static int DUNE_BOARD_IMAGE_Y = 1178;

	public static int MESSAGE_LOG = 0;

	public static int MESSAGE_BOLD = 1;

	public static int MESSAGE_POPUP = 2;

}