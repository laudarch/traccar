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
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Jt701DProtocolDecoder extends BaseProtocolDecoder {

    public Jt701DProtocolDecoder(Protocol protocol) {
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
        int dd = BcdUtil.readInteger(buf, 2);
	int MM = BcdUtil.readInteger(buf, 2);
	int yy = BcdUtil.readInteger(buf, 2);
	int hh = BcdUtil.readInteger(buf, 2);
	int mm = BcdUtil.readInteger(buf, 2);
	int ss = BcdUtil.readInteger(buf, 2);

	DateBuilder dateBuilder = new DateBuilder()
                .setDay(dd)
                .setMonth(MM)
                .setYear(yy)
                .setHour(hh)
                .setMinute(mm)
                .setSecond(ss);
        position.setTime(dateBuilder.getDate());

	int lat = BcdUtil.readInteger(buf, 8);
	int lon = BcdUtil.readInteger(buf, 8);

        double latitude = convertCoordinate(lat);
        double longitude = convertCoordinate(lon);

        byte flags = buf.readByte();
        position.setValid((flags & 0x1) == 0x1);
        if ((flags & 0x2) == 0) {
            latitude = -latitude;
        }
        position.setLatitude(latitude);
        if ((flags & 0x4) == 0) {
            longitude = -longitude;
        }
        position.setLongitude(longitude);

        position.setSpeed(BcdUtil.readInteger(buf, 2));
        position.setCourse(buf.readUnsignedByte() * 2.0);
    }

    private void sendResponse(Channel channel, int dsn) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
	    String dsn_str = "(P69,0," + dsn + ")";
            response.writeCharSequence(dsn_str, Charset.forName("uTF-8"));
            channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
        }
    }

    private List<Position> decodeBinary(ByteBuf buf, Channel channel, SocketAddress remoteAddress) {

        List<Position> positions = new LinkedList<>();
        Position position = new Position(getProtocolName());
        int protocolVersion = 0;

	ByteBuf hexbuf = buf;
        position.set(Position.KEY_HEXDUMP,  ByteBufUtil.hexDump(hexbuf));

        char hdr = (char) buf.readByte(); // header

        String id = String.valueOf(Long.parseLong(ByteBufUtil.hexDump(buf.readSlice(5))));
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        position.setDeviceId(deviceSession.getDeviceId());
        protocolVersion = buf.readUnsignedByte();

        int deviceType = buf.readUnsignedByte(); // 0.5 = devicetype 0.5 = datatype

	// XXX: Process datatype

	int dataLen = (int)buf.readShort();

        decodeBinaryLocation(buf, position);

        position.set(Position.KEY_MILEAGE, buf.readUnsignedInt());
        position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());

        buf.readUnsignedInt(); // vehicle id (reserved)

        int status = buf.readUnsignedShort();
        position.set(Position.KEY_LBS, BitUtil.check(status, 0));
        position.set(Position.KEY_ALARM, BitUtil.check(status, 1) ? Position.ALARM_GEOFENCE_ENTER : null);
        position.set(Position.KEY_ALARM, BitUtil.check(status, 2) ? Position.ALARM_GEOFENCE_EXIT : null);
        position.set(Position.KEY_ALARM, BitUtil.check(status, 3) ? Position.ALARM_STEEL_STRING_CUT : null);
        position.set(Position.KEY_ALARM, BitUtil.check(status, 4) ? Position.ALARM_VIBRATION : null);
        position.set(Position.KEY_STEEL_STRING_STATUS, BitUtil.check(status, 6));
        position.set(Position.KEY_MOTOR_LOCK_STATUS, BitUtil.check(status, 7));
        position.set(Position.KEY_ALARM, BitUtil.check(status, 8 + 3) ? Position.ALARM_LOW_BATTERY : null);
        position.set(Position.KEY_ALARM, BitUtil.check(status, 8 + 4) ? Position.ALARM_BACK_CAP_OPEN : null);
        position.set(Position.KEY_BACK_CAP_STATUS, BitUtil.check(status, 8 + 5));
        position.set(Position.KEY_ALARM, BitUtil.check(status, 8 + 6) ? Position.ALARM_MOTOR_STUCK : null);
        position.set(Position.KEY_STATUS, status);

        int battery = buf.readUnsignedByte();
        if (battery == 0xff) {
              position.set(Position.KEY_CHARGE, true);
        } else {
              position.set(Position.KEY_BATTERY_LEVEL, battery);
        }

        CellTower cellTower = CellTower.fromCidLac(buf.readUnsignedShort(), buf.readUnsignedShort());
        cellTower.setSignalStrength((int) buf.readUnsignedByte());
        position.setNetwork(new Network(cellTower));

        buf.readUnsignedByte(); // geofence id
        buf.skipBytes(3); // reserved
        buf.skipBytes(buf.readableBytes() - 1);
        int dataSerialNumber = buf.readUnsignedByte(); // index

	sendResponse(channel, dataSerialNumber);
        positions.add(position);
        return positions;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        char first = (char) buf.getByte(0);

        if (first == '$') {
            return decodeBinary(buf, channel, remoteAddress);
        }

        return null;
    }

}
