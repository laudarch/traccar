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
import org.traccar.model.Position;

public class Jt707AProtocolDecoder extends BaseProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jt707AProtocolDecoder.class);

    public Jt707AProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static double convertCoordinate(int raw) {
	    return raw / 1000000.0;
    }

    private void decodeStatus(Position position, ByteBuf buf) {

        int value = buf.readUnsignedByte();

        position.set(Position.KEY_IGNITION, BitUtil.check(value, 0));
        position.set(Position.KEY_DOOR, BitUtil.check(value, 6));

        value = buf.readUnsignedByte();

        position.set(Position.KEY_CHARGE, BitUtil.check(value, 0));
        position.set(Position.KEY_BLOCKED, BitUtil.check(value, 1));

        if (BitUtil.check(value, 2)) {
            position.set(Position.KEY_ALARM, Position.ALARM_SOS);
        }
        if (BitUtil.check(value, 3) || BitUtil.check(value, 4)) {
            position.set(Position.KEY_ALARM, Position.ALARM_GPS_ANTENNA_CUT);
        }
        if (BitUtil.check(value, 4)) {
            position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
        }

        value = buf.readUnsignedByte();

        if (BitUtil.check(value, 2)) {
            position.set(Position.KEY_ALARM, Position.ALARM_FATIGUE_DRIVING);
        }
        if (BitUtil.check(value, 3)) {
            position.set(Position.KEY_ALARM, Position.ALARM_TOW);
        }

        buf.readUnsignedByte(); // reserved

    }

    private List<Position> decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {

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
        position.setDeviceId(deviceSession.getDeviceId());

        int lat = buf.readInt();
        int lon = buf.readInt(); 
        int alt = buf.readShort();

	//LOGGER.info("Lat: " + lat + " Lon: " + lon + " Alt: " + alt);

	double latitude = convertCoordinate(lat);
        double longitude = convertCoordinate(lon);
        double altitude = convertCoordinate(alt);
	//LOGGER.info("Lat: " + latitude + " Lon: " + longitude + " Alt: " + altitude);

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


            /*position.set(Position.KEY_ODOMETER, buf.readUnsignedInt() * 1000);
            position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());

            buf.readUnsignedInt(); // vehicle id combined

            position.set(Position.KEY_LBS, BitUtil.check(status, 0));
            position.set(Position.KEY_ALARM, BitUtil.check(status, 1) ? Position.ALARM_GEOFENCE_ENTER : null);
            position.set(Position.KEY_ALARM, BitUtil.check(status, 2) ? Position.ALARM_GEOFENCE_EXIT : null);
            position.set(Position.KEY_ALARM, BitUtil.check(status, 3) ? Position.ALARM_STEEL_STRING_CUT : null);
            position.set(Position.KEY_ALARM, BitUtil.check(status, 4) ? Position.ALARM_VIBRATION : null);
            position.set(Position.KEY_STEEL_STRING_STATUS, BitUtil.check(status, 6));
            position.set(Position.KEY_MOTOR_LOCK_STATUS, BitUtil.check(status, 7));
            position.set(Position.KEY_ALARM, BitUtil.check(status, 8 + 3) ? Position.ALARM_LOW_BATTERY : null);
            position.set(Position.KEY_BACK_CAP_STATUS, BitUtil.check(status, 8 + 5));
            position.set(Position.KEY_ALARM, BitUtil.check(status, 8 + 6) ? Position.ALARM_FAULT : null);
            position.set(Position.KEY_STATUS, status);*/

            //int battery = buf.readUnsignedByte();
            //if (battery == 0xff) {
            //      position.set(Position.KEY_CHARGE, true);
            //} else {
            //      position.set(Position.KEY_BATTERY_LEVEL, battery);
            //}

            //CellTower cellTower = CellTower.fromCidLac(buf.readUnsignedShort(), buf.readUnsignedShort());
            //cellTower.setSignalStrength((int) buf.readUnsignedByte());
            //position.setNetwork(new Network(cellTower));

            //if (protocolVersion == 0x17) {
            //      buf.readUnsignedByte(); // geofence id
            //      buf.skipBytes(3); // reserved
            //      buf.skipBytes(buf.readableBytes() - 1);
            //}

	    position.setProtocol("jt707a");
            positions.add(position);

        //buf.readUnsignedByte(); // index

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
