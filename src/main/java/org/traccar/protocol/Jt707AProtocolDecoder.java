/*
 * Copyright 2012 - 2019 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.helper.BcdUtil;
import org.traccar.helper.BitUtil;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.DataConverter;
import org.traccar.model.CellTower;
import org.traccar.model.Network;
import org.traccar.model.Position;

public class Jt707AProtocolDecoder extends BaseProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jt707AProtocolDecoder.class);

    public Jt707AProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static double convertCoordinate(int raw) {
	    return raw / 1000000.0;
    }

    private List<Position> decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {

//    	LOGGER.info("Hello am starting s");
        List<Position> positions = new LinkedList<>();


        byte header = buf.readByte(); // header
		ByteBuf messageID = buf.readBytes(2);
		ByteBuf messageLength = buf.readBytes(2);

        String id = String.valueOf(Long.parseLong(ByteBufUtil.hexDump(buf.readSlice(6))));
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

	ByteBuf series = buf.readBytes(2);

        ByteBuf alarmFlag = buf.readBytes(4); // alarm flags
        int statusFlag = buf.readInt(); // statusflags

        Position position = new Position(getProtocolName());
//        position.setDeviceId(deviceSession.getDeviceId());

        int lat = buf.readInt();
        int lon = buf.readInt(); 
        int alt = buf.readShort();

//		LOGGER.info("Lat: " + lat + " Lon: " + lon + " Alt: " + alt);

		double latitude = convertCoordinate(lat);
        double longitude = convertCoordinate(lon);
        double altitude = convertCoordinate(alt);

		position.setLatitude(latitude);
//		LOGGER.info("status fla is "+statusFlag);
        if ((statusFlag & 0x4) == 0) {
            longitude = -longitude;
        }

//		LOGGER.info("Lat: " + latitude + " Lon: " + longitude + " Alt: " + altitude);



		position.setLongitude(longitude);
        position.setAltitude(altitude);

        position.setSpeed(buf.readUnsignedShort() * 0.1);
        position.setCourse(buf.readUnsignedShort());
	
        DateBuilder dateBuilder = new DateBuilder()
        	.setYear(BcdUtil.readInteger(buf, 2))
        	.setMonth(BcdUtil.readInteger(buf, 2))
        	.setDay(BcdUtil.readInteger(buf, 2))
        	.setHour(BcdUtil.readInteger(buf, 2))
        	.setMinute(BcdUtil.readInteger(buf, 2))
        	.setSecond(BcdUtil.readInteger(buf, 2));
        position.setTime(dateBuilder.getDate());

        int signal = -1;

//	LOGGER.info("Remaining bytes: " + buf.readableBytes());

	while (buf.readableBytes() >= 1) {

		int xid = (int) buf.readByte();

//		LOGGER.info("cur value is "+xid);


		switch (xid) {

			case 0x7e :
//				LOGGER.info("0x7e");
				break; // we are done :)

			case 0x01 :
//				LOGGER.info("0x01");
				buf.readByte(); // Length
				int mileage = buf.readInt();
				//LOGGER.info("mileage: " + mileage);
				//LOGGER.info("0x01 Remaining bytes: " + buf.readableBytes());
				break;

			case 0x05 : 
//				LOGGER.info("0x05");
				// Reserved, should never be called
				//LOGGER.info("OMG there's a 0x05 what do we do!");
				break;

			case 0x30 :
//				LOGGER.info("0x30");
				buf.readByte(); // Length
				//LOGGER.info("0x30 1 Remaining bytes: " + buf.readableBytes());
				// skip wireless signal strength
				signal = (int) buf.readByte();

				//LOGGER.info("0x30 2 Remaining bytes: " + buf.readableBytes());
				break;

			case 0x31 :
//				LOGGER.info("0x31");
				buf.readByte(); // Length
				int satelites = (int) buf.readByte();
				position.set(Position.KEY_SATELLITES, satelites);
//				LOGGER.info("satelites: " + satelites);
				//LOGGER.info("0x31 Remaining bytes: " + buf.readableBytes());
				break;

			case -44:
			case 0xd4 :
//				LOGGER.info("0xD4");
				buf.readByte(); // Length
				int battery = (int) buf.readByte();
              			position.set(Position.KEY_BATTERY_LEVEL, battery);
//				LOGGER.info("battery: " + battery);
				if (battery <= 10) {
        				position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
				}
				//LOGGER.info("0xd4 Remaining bytes: " + buf.readableBytes());
				break;

			case -43:
			case 0xd5 :
				//LOGGER.info("0xD5");
				buf.readByte(); // Length
				double voltage = (double) buf.readShort(); // skip battery voltage
//				LOGGER.info("battery voltage: " + voltage);
				//LOGGER.info("0xd5 Remaining bytes: " + buf.readableBytes());
				break;

			case -38:
			case 0xda :
				//LOGGER.info("0xDA");
				buf.readByte(); // Length
				int steelCutTimes = (int) buf.readShort();
				//int sensor = (int)buf.readUnsignedByte();
				int sensor = (int) buf.readByte();

        			position.set(Position.KEY_STEEL_CUT_TIMES, steelCutTimes);
        			position.set(Position.KEY_ALARM, BitUtil.check(sensor, 0)
						? Position.ALARM_STEEL_STRING_CUT : null);
        			position.set(Position.KEY_STEEL_STRING_STATUS,BitUtil.check(sensor, 0));
        			position.set(Position.KEY_MOTION, BitUtil.check(sensor, 1));
        			position.set(Position.KEY_SIM_TYPE, BitUtil.check(sensor, 2) ? "ESIM" : "SIM");
        			position.set(Position.KEY_BACK_CAP_STATUS, BitUtil.check(sensor, 3));
        			position.set(Position.KEY_STATUS, sensor);

//				LOGGER.info("SteelCutTimes: " + steelCutTimes);
//				LOGGER.info("stell status: " + (BitUtil.check(sensor, 0) ? "true" : "false"));
//				LOGGER.info("status: " + sensor);
				//LOGGER.info("0xda Remaining bytes: " + buf.readableBytes());
				break;

			case -37:
			case 0xdb :
				//LOGGER.info("0xDB");
				buf.readByte(); // Length
				// Skip DB20022 
				buf.readShort(); 
				//LOGGER.info("0xdb Remaining bytes: " + buf.readableBytes());
				break;

			case -36:
			case 0xdc :
				//LOGGER.info("0xDC");
				buf.readByte(); // Length
				buf.readInt(); // Skip internet debug status
				//LOGGER.info("0xdc Remaining bytes: " + buf.readableBytes());
				break;

			case 0xfe :
				//LOGGER.info("0xFE");
				buf.readByte(); // Length
				buf.readInt(); // Skip gps mileage
				//LOGGER.info("0xfe Remaining bytes: " + buf.readableBytes());
				break;

			case -3:
			case 0xfd :
				//LOGGER.info("0xFD");
				buf.readByte(); // Length
				int mcc = (int) buf.readShort();
				int mnc = (int) buf.readByte();
				int cellid = buf.readInt();
				int lacid = (int) buf.readShort();

				CellTower cellTower = CellTower.from(mcc, mnc, lacid, cellid);
				cellTower.setSignalStrength(signal);
				position.setNetwork(new Network(cellTower));
//				LOGGER.info("mcc is "+mcc);
//				LOGGER.info("cellId is "+cellid);
				//LOGGER.info("0xfd Remaining bytes: " + buf.readableBytes());
				break;
			default:
				// Do Nothing!!
		}
		xid = 0x0;

	}
	
	//LOGGER.info("at the end remaining bytes: " + buf.readableBytes());
        position.setProtocol("jt707a");
        positions.add(position);
//
        return positions;

    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        char first = (char) buf.getByte(0);

        if (first == '~') {
            return decodeBinary(buf, channel, remoteAddress);
        }

        return null;
    }

}
