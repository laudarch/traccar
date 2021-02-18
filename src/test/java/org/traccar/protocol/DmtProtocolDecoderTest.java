package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;

public class DmtProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        DmtProtocolDecoder decoder = new DmtProtocolDecoder(null);

        verifyNull(decoder, binary(
                "0255003300001b00003335333232393032373533393235310038393931353030303030303030313330343539340000000403041910780603"));

        verifyPositions(decoder, binary(
                "0255047a003d0035020000fbb53a0f030015fbb53a0f3c210e2145deeb051e001a0901940c070302080300000000000300060f011d1003400804180005e80f062e1c3d003602000086b93a0f03001586b93a0fe5421c21bbe1be051700c30901a50d070302080300000000000300060f011d1003bb0804180005e80f062e1c"));

        verifyPositions(decoder, false, binary(
                "025504ab013d00c21a00004829900c0300154929900cbd163617b08a94c7fa003c07032c131a0302080300000000000300060f019b14037e0e0463000558140607213d00c31a0000ca29900c030015ca29900ca3033817bbb895c71401be0603b310190302080300000000000300060f019b14036d0e0463000558140607213d00c41a0000472a900c030015472a900c8d453817423e96c7fa000200040013270302080300000000000300060f019b1403840e0463000546140606213d00c51a0000c52a900c030015c52a900c184c3817c35296c724010400050016180302080300000000000300060f019b1403750e0463000547140606213d00c61a0000462b900c030015462b900cbd8a361703b495c710018c07085a10210302080300000000000300060f019b1403630e0463000546140606213d00c71a0000c52b900c030015c52b900cf6d63517455a94c7e9004c05035a10240302080300000000000300060f019b14036e0e0463000545140606213d00c81a00004b2c900c0300154b2c900c766d3517ddf093c7320107000d00102e0302080300000000000300060f019b1403750e046300054314060521"));

        verifyPositions(decoder, false, binary(
                "02551040000eaca40d00d2b8e562c51f9912f39a6bee00007e420091090903070100000000008b1065360000000000007fd401c4fcf2feffffffffffffffffee0000003f1b"));

        verifyPositions(decoder, false, binary(
                "02551080000eada40d00d2b8e58ac51f9912f39a6bee00007e42007e090709070000000000009010fc330000000000007fc201a0fc04ffffffffffffffffffe5000000c5d00eaea40d00d2b8e58ac51f9912f39a6bee00007e42007e09070207000000000000851008340000000000007fc201a0fc04ff0000000000000000e5000000c96d"));

        verifyNull(decoder, binary(
                "025500310038f90100333533333233303831363639373330003839363130313835303031383234383434363330002202010900000000"));

        verifyNull(decoder, binary(
                "0255220000"));

        verifyPositions(decoder, false, binary(
                "025504d80352000602000052185c0803001552185c0842ee19eaba2524682d000d060973112b0302080100000000000300060901421003e40604140007190b300000000c030000000de80100000ec90e00000f0700000052000702000069185c0803001569185c089ac019ea0ad223682300fb02047d152f03020801000000000003000609013f1003fc0604140007190b300000000c030000000de90100000ecb0e00000f0700000052000802000092185c0803001592185c0800a619eaa5e7226821009c0506880e250302080100000000000300060901411003f30604140007190b300000000c030000000dea0100000ef10e00000f07000000520009020000a9185c08030015a9185c0818ae19ea1e62226826001e05038e0e2203020801000000000003000609013f1003030704140007190b300000000c030000000deb0100000ef60e00000f0700000052000a020000c0185c08030015c0185c0893b619ea7fd321681a00640403860f1d0302080100000000000300060901401003ff0604140007190b300000000c030000000dec0100000ef80e00000f0700000052000b020000d7185c08030015d7185c08e08519eab7c921682300fd04035510270302080100000000000300060901401003ea0604140007190b300000000c030000000ded0100000efa0e00000f0700000052000c020000ee185c08030015ee185c08f61719ea61e221682c004c0503540f190302080100000000000300060901421003dd0604140007190b300000000c030000000dee0100000efc0e00000f0700000052000d02000005195c0803001505195c0836b518eac9f221683000fa0107740e2d03020801000000000003000609013f1003fe0604140007190b300000000c030000000def0100000efe0e00000f0700000052000e0200001d195c080300151d195c08d1b518ea2d6721682300980502870e1d0302080100000000000300060901411003ed0604140007190b300000000c030000000df00100000e000f00000f0700000052000f02000034195c0803001534195c086acd18ea742b2168400006020500132903020801000000000003000609013d10030d0704140007190b300000000c030000000df10100000e030f00000f070000005200100200004d195c080300154d195c08dfba18eab81721684e003000093b0e1e03020801000000000003000609013e1003130704140007190b300000000c030000000df20100000e050f00000f0700000052001102000065195c0803001565195c081db318ea871f216822000400080416250302080100000000000300060901401003060704140007190b300000000c030000000df30100000e090f00000f07000000"));

        verifyPositions(decoder, false, binary(
                "025504e9032f000d000000000000001501222700524553455420446172742033342e322e312e3920666c6167733d312057443d303f000e0000000000000015013214004e6f2041646d696e20706172616d7320666f756e64202d207573696e672064656661756c7473202b204175746f41504e37000f00000000000000090015000000000000000000000000000000000000000000020805000000000007000609012b1002400003700e37001000000000000000090015000000000000000000000000000000000000000000020801000000000007000609012b1002400003700e37001100000000000000090015000000000000000000000000000000000000000000020800000000000007000609012b1002400003700e37001200000000000000020015000000000000000000000000000000000000000000020800000000000006000609012b1002400003700e370013000000000000000f001500000000000000000000000000000000000000000002080000000000000200060901271002370003670e2e0014000000000000001501211300526f6c6c20646574656374656420636f735e32203c203338333535333838343700150000000000000017001500000000000000000000000000000000000000000002080000000000000200060901071002300003d60e2a00160000000000000015011d130054756d626c65722074726967676572656420636f735e32203c20302e0017000000000000001501211300526f6c6c20646574656374656420636f735e32203c203338333535333838343700180000000000000017001500000000000000000000000000000000000000000002080000000000000200060901071002300003f70e2a00190000000000000015011d130054756d626c65722074726967676572656420636f735e32203c203026001a000000000000001501190b0047534d3a20544350206261642053594e432063686172732e001b000000000000001501211300526f6c6c20646574656374656420636f735e32203c203338333535333838343a001c0000000000000017001500000000000000000000000000000000000000000002080000000000000200060c01c90f02300003e20f041f002a001d0000000000000015011d130054756d626c65722074726967676572656420636f735e32203c20302e001e000000000000001501211300526f6c6c20646574656374656420636f735e32203c203338333535333838343a001f0000000000000017001500000000000000000000000000000000000000000002080000000000000200060c01d80f02300003ff0f0418002a00200000000000000015011d130054756d626c65722074726967676572656420636f735e32203c2030"));

        verifyNull(decoder, binary(
                "025500310038f90100333533333233303831363639373330003839363130313435363839393333303030303835002202010900000000"));

        verifyPositions(decoder, binary(
                "0255043D003D004746000096D684020B001502D48402F043F4EC2A6909452B001F00050011230302080000000000000A00060F041D0001FE0F021E0005000003BF08"));

    }

}
