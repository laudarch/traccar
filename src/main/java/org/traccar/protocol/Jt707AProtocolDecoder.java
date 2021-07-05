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
import io.netty.buffer.Unpooled;
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

        List<Position> positions = new LinkedList<>();

	ByteBuf hexbuf = buf;
        byte header = buf.readByte(); // header
	ByteBuf messageID = buf.readBytes(2);
	ByteBuf messageLength = buf.readBytes(2);

        String id = String.valueOf(Long.parseLong(ByteBufUtil.hexDump(buf.readSlice(6))));
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

	ByteBuf series = buf.readBytes(2);

        ByteBuf alarmFlag = buf.readBytes(4);
        int statusFlag = buf.readInt();

        Position position = new Position(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

	position.set(Position.KEY_HEXDUMP,  ByteBufUtil.hexDump(hexbuf));

        int lat = buf.readInt();
        int lon = buf.readInt(); 
        int alt = buf.readShort();

	double latitude = convertCoordinate(lat);
        double longitude = convertCoordinate(lon);
        double altitude = convertCoordinate(alt);

	position.setLatitude(latitude);
        if ((statusFlag & 0x4) == 0) {
            longitude = -longitude;
        }
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

	while (buf.readableBytes() >= 1) {
        	int xid = (int) buf.readByte(); 

		switch (xid) {
			case 0x7e :
				break;

			case 0x01 :
				buf.readByte();
				int mileage = buf.readInt();
				break;

			case 0x05 : 
				break;

			case 0x30 :
				buf.readByte();
				signal = (int) buf.readByte();
				break;

			case 0x31 :
				buf.readByte();
				int satelites = (int) buf.readByte();
        			position.set(Position.KEY_SATELLITES, satelites);
				break;

			case -44 :
			case 0xd4 :
				buf.readByte();
				int battery = (int) buf.readByte();
              			position.set(Position.KEY_BATTERY_LEVEL, battery);

				if (battery <= 10) {
        				position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
				}
				break;

			case -43 :
			case 0xd5 :
				buf.readByte();
				double voltage = (double) buf.readShort();
				break;

			case -38 :
			case 0xda :
				buf.readByte();
				int steelCutTimes = (int) buf.readShort();
				int sensor = (int) buf.readByte();

        			position.set(Position.KEY_STEEL_CUT_TIMES, steelCutTimes);
        			position.set(Position.KEY_ALARM, BitUtil.check(sensor, 0) 
						? Position.ALARM_STEEL_STRING_CUT : null);
        			position.set(Position.KEY_MOTION, BitUtil.check(sensor, 1));
        			position.set(Position.KEY_SIM_TYPE, BitUtil.check(sensor, 2) ? "ESIM" : "SIM");
        			position.set(Position.KEY_BACK_CAP_STATUS, BitUtil.check(sensor, 3));
        			position.set(Position.KEY_STATUS, sensor);
				break;

			case -37 :
			case 0xdb :
				buf.readByte();
				buf.readShort(); 
				break;

			case -36 :
			case 0xdc :
				buf.readByte();
				buf.readInt();
				break;

			case 0xfe :
				buf.readByte(); 
				buf.readInt();
				break;

			case 0xfd :
				buf.readByte();
				int mcc = (int) buf.readShort();
				int mnc = (int) buf.readByte();
				int cellid = buf.readInt();
				int lacid = (int) buf.readShort();

				CellTower cellTower = CellTower.from(mcc, mnc, lacid, cellid);
				cellTower.setSignalStrength(-1);
				position.setNetwork(new Network(cellTower));
				break;
			default:
				// Do Nothing!!
		}
		xid = 0x0;

	}
	
        position.setProtocol("jt707a");
        positions.add(position);


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
