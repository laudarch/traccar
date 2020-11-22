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
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.NetworkMessage;
import org.traccar.Protocol;
import org.traccar.helper.BcdUtil;
import org.traccar.helper.BitBuffer;
import org.traccar.helper.BitUtil;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.CellTower;
import org.traccar.model.Network;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Jt707AProtocolDecoder extends BaseProtocolDecoder {

    public Jt707AProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static double convertCoordinate(int raw) {
        int degrees = raw / 1000000;
        double minutes = (raw % 1000000) / 10000.0;
        return degrees + minutes / 60;
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

    static boolean isLongFormat(ByteBuf buf, int flagIndex) {
        return buf.getUnsignedByte(flagIndex) >> 4 == 0x7;
    }

    static void decodeBinaryLocation(ByteBuf buf, Position position) {

        double latitude = BcdUtil.readCoordinate(buf);
        double longitude = BcdUtil.readCoordinate(buf);
        double altitude = convertCoordinate(BcdUtil.readInteger(buf, 2));

        position.setSpeed(buf.readUnsignedShort());
        position.setCourse(buf.readUnsignedShort());
	
        DateBuilder dateBuilder = new DateBuilder()
        	.setYear(BcdUtil.readInteger(buf, 2))
        	.setMonth(BcdUtil.readInteger(buf, 2))
        	.setDay(BcdUtil.readInteger(buf, 2))
        	.setHour(BcdUtil.readInteger(buf, 2))
        	.setMinute(BcdUtil.readInteger(buf, 2))
        	.setSecond(BcdUtil.readInteger(buf, 2));

        position.setTime(dateBuilder.getDate());

	/*DateBuilder dateBuilder = new DateBuilder()
                .setDay(BcdUtil.readInteger(buf, 39))
                .setMonth(BcdUtil.readInteger(buf, 38))
                .setYear(BcdUtil.readInteger(buf, 37))
                .setHour(BcdUtil.readInteger(buf, 40))
                .setMinute(BcdUtil.readInteger(buf, 41))
                .setSecond(BcdUtil.readInteger(buf, 42));
        position.setTime(dateBuilder.getDate());*/


        /*byte flags = buf.readByte();
        position.setValid((flags & 0x1) == 0x1);
        if ((flags & 0x2) == 0) {
            latitude = -latitude;
        }*/
        position.setLatitude(latitude);
        /*if ((flags & 0x4) == 0) {
            longitude = -longitude;
        }*/
        position.setLongitude(longitude);
        position.setAltitude(altitude);

    }

    private List<Position> decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {

        List<Position> positions = new LinkedList<>();

        //buf.readByte(); // header
        buf.readBytes(5); // Skip to ID

        String id = String.valueOf(Long.parseLong(ByteBufUtil.hexDump(buf.readSlice(6))));
	//System.out.println("id is " + id);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        int series = BitUtil.from(buf.readUnsignedShort(), 2);

	//System.out.println("series is " + series);
	//System.out.println("readableBytes is " + buf.readableBytes());

        buf.readBytes(4); // Skip alarm flags
        long status = buf.readUnsignedInt();

	//System.out.println("readerIndex is " + buf.readerIndex());
        //while (buf.readableBytes() > 1) {
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            decodeBinaryLocation(buf, position);

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
        //}

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
