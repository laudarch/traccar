package org.traccar.protocol;

import org.traccar.ProtocolTest;

import org.junit.Test;

public class Jt707AProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        Jt707AProtocolDecoder decoder = new Jt707AProtocolDecoder(null);

        verifyPositions(decoder, binary(
                "7e020000437701912063450042000000000b0000000000000000000000000000000000201118170522300114310100d401ffd50200a0da0300052cdb020389dc0400000000fd09026c0100000607008bd27e"));

    }

}
