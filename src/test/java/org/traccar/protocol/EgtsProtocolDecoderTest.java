package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;

public class EgtsProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecodeWithObjectId() throws Exception {

        var decoder = new EgtsProtocolDecoder(null);

        verifyNull(decoder, binary(
                "0100020b002300020001871c00020000010105190000ab0800006247396e615734366347467a63336476636d513daadf"));

        verifyPositions(decoder, binary(
                "0100020b004600010001b81800030001f299c0d80202101500c9c52f1100552e9c80e4ca7911f5805b00000000031800040001f299c0d80202101500cbc52f1100612e9c00dbca79116c803e00000000037c13"));

        verifyPositions(decoder, binary(
                "0100020b005e01030001ed180005000162c72c9a0202101500c4c52f1100477e9f0047c979010000ad000000000318000600017ee0710c0202101500c9c52f11003ee59f8061e97a0100801b00000000031800070001b6eeb6c00202101500c7c52f110077669d00b9707a116a015600000000031800080001b6eeb6c00202101500cdc52f11007c669d004e717a117a0158000000000318000900018b4685f70202101500c8c52f11006ee09f0027ca7c11650079000000000318000a0001f299c0d80202101500c9c52f1100552e9c80e4ca7911f5805b000000000318000b0001f299c0d80202101500cbc52f1100612e9c00dbca79116c803e000000000318000c0001731347010202101500c7c52f1100a3699a80db3c7a010000e5000000000318000d0001c85285f70202101500cbc52f1100e8979900f3497b114d0101000000000318000e0001aa4358810202101500cdc52f11002d689a80ab427a0100009300000000032b9f"));

        verifyNull(decoder, binary("0100000b0003006c430004010000acfb"));

        verifyPositions(decoder, binary(
                "0100000b0086035ddd016d18004f049579b000001e2fc11002021015001e2fc1107ac3919f59cc5c7a0b0000000000003000180050049579b00000242fc1100202101500242fc1100fb5919f2dbf5c7a0b0000000000003000180051049579b00000312fc1100202101500312fc110b899919f94cf5c7a0b00000000000030001e0052049579b00000ba62a2120202120900000003000000000000150500025c00000013070003000000000000180053049579b000004e2fc11002021015004e2fc11087ba919f8dd45c7a0b0000000000003000180054049579b00000552fc1100202101500552fc1106ecb919f2aec5c7a0b00000000000030001e0055049579b00000d562a2120202120900000003000000000000150500025c00000013070003000000000000180056049579b000005c2fc11002021015005c2fc11059d9919f54fa5c7a0b0000000000003000180057049579b000006e2fc11002021015006e2fc110309f919fc2db5c7a0b0000000000003000180058049579b00000762fc1100202101500762fc1104690919f94cf5c7a0b00000000000030001e0059049579b00000f662a2120202120900000003000000000000150500025c000000130700030000000000001e005a049579b000001463a2120202120900000003000000000000150500025c0000001307000300000000000018005b049579b00000b32fc1100202101500b32fc110c491919fa2c65c7a0b00000000000030001e005c049579b000003363a2120202120900000003000000000000150500025c0000001307000300000000000018005d049579b00000ca2fc1100202101500ca2fc11089b9919fd5f95c7a0b00000000000030001e005e049579b000005163a2120202120900000003000000000000150500025c000000130700030000000000001e005f049579b000006f63a2120202120900000003000000000000150500025c00000013070003000000000000180060049579b00000f42fc1100202101500f42fc11087ba919f43db5c7a0b0000000000003000180061049579b000000730c11002021015000730c110f9c3919fe1c65c7a0b0000000000003000180062049579b000000930c11002021015000930c1106ecb919f3ab65c7a0b00000000000030001e0063049579b000008d63a2120202120900000003000000000000150500025c00000013070003000000000000140064049579b00000ac63a2120202120900000003000000000000150500025c000000ce53"));

        verifyNull(decoder, binary("0100000b00100091030072000100060000000002020003009203000009"));

    }

    @Test
    public void testDecodeWithAuth() throws Exception {

        var decoder = new EgtsProtocolDecoder(null);

        verifyNull(decoder, binary(
                "0100010b002200c06401f21700c1640171360d00010101140071360d000238363539303500000000000000000047fc"));

        verifyNull(decoder, binary(
                "0100000b002400a0d601f01900030081030000000101011600030000004238363434393530333436343333373600014cdc"));

        verifyPositions(decoder, binary(
                "0100020b009e00892401504400ed539135de0100020210180051917613009194a00028db7893bd814700000001008c00001106001a3f00130300140500001d0000041207000f000000000000130100001b0700010000ae0e00004400ee539135de010002021018005491761300c094a00011db789353814700000001008c00001106001a3f00130300140500001c0000041207000f000010000000130100001b0700010000ca0e00004b47"));

        verifyPositions(decoder, binary(
                "0100020b004f00ae1101d24400ae1191af8304000202101800d9238213008dcea08070ec789338003c00000001008200001106001a740007020014050000180000041207000f000010000000130100001b0700010000af0f0000312a"));

        verifyPositions(decoder, binary(
                "0100020b009e004b8801b04400512091473502000202101800052582130026e5a080e04979937202f20000000100cd00001106001a470010030014050000190000041207000f000010000000130100001b0700010000e80b000044005220914735020002021018000b25821300d2e4a080524979937802f20000000100ce00001106001a4100110300140500001a0000041207000f000010000000130100001b0700010000eb0b0000c776"));

        verifyPositions(decoder, binary(
                "0100020b008b01550001dc4400580291ce5006000202101800fe248213002351a000072879934d810c0000000100af00001106001a5d000c030014050000000000041207000f000000000000130100001b07000100002a0a00004400590291ce500600020210180001258213002651a000e02779934481160000000100ad00001106001a49000e030014050000000000041207000f000000000000130100001b07000100003a0a000044005a0291ce500600020210180004258213002b51a000c2277993da80160000000100ab00001106001a4b000d030014050000000000041207000f000000000000130100001b0700010000d50a000044005b0291ce500600020210180014258213004e51a00033277993dd80210000000100ab00001106001a48000f030014050000000000041207000f000000000000130100001b0700010000b30a000044005c0291ce50060002021018001f258213007a51a080c4267993fa802f0000000100ac00001106001a48000f030014050000000000041207000f000000000000130100001b0700010000390a0000c664"));

        verifyPositions(decoder, binary(
                "0100000b002700030e01211800030e8573890100845e980f0202101500f85d980fb37f50aae9653c2b193708317b00000001c51b"));

        verifyPositions(decoder, binary(
                "0100010b00a308c26401029808c3640171360d000202101800e19a7b0fcfb4c49a0bfdb87a911801b70000000010d90000180400021c0000120300000000101800f39a7b0f2fc9c39a9bf2b87a914001b50000000010da0000180400021c0000120300000000101800fa9a7b0fc663c39a21eeb87a914001b60000000010da0000180400021c0000120300000000101800069b7b0f56d8c29a26ebb87a919600ab0000000010da0000180400021c00001203000000001018000a9b7b0fb2c5c29a19f4b87a915a007d0000000010da0000180400021c0000120300000000101800089b7b0f68ccc29a21eeb87a9164008f0000000010da0000180400021c0000120300000000101800079b7b0fa0d1c29aa4ecb87a918200980000000010da0000180400021c00001203000000001018000b9b7b0f34c4c29ad3f7b87a915a00670000000010da0000180400021c00001203000000001018000f9b7b0f3dbec29aaf0fb97a91c8005e0000000010dc0000180400021c0000120300000000101800199b7b0f42bbc29a0855b97a9178006b0000000010db0000180400021c00001203000000001018001b9b7b0fc8b6c29a3c5db97a916e007e0000000010db0000180400021c00001203000000001018001a9b7b0fc4b9c29a8159b97a916e00750000000010db0000180400021c00001203000000001018001d9b7b0f94aec29a3363b97a916400930000000010db0000180400021c00001203000000001018001c9b7b0fcdb3c29a3760b97a916e008a0000000010db0000180400021c0000120300000000101800209b7b0f28a1c29af263b97a918200ba0000000010db0000180400021c00001203000000001018001f9b7b0f61a6c29af263b97a917800b30000000010db0000180400021c00001203000000001018001e9b7b0f58acc29af263b97a916400a50000000010db0000180400021c0000120300000000101800299b7b0ff26fc29ab561b97a916e00b20000000010d90000180400021c00001203000000001018002d9b7b0fd05bc29a3760b97a916e00bd0000000010d80000180400021c0000120300000000101800359b7b0f4f31c29abe5bb97a916400b50000000010d70000180400021c0000120300000000101800379b7b0f5d28c29abe5bb97a916e00b40000000010d60000180400021c0000120300000000101800369b7b0fd62cc29abe5bb97a916400bd0000000010d60000180400021c00001203000000001018003c9b7b0fca09c29a0358b97a918c00bc0000000010d50000180400021c0000120300000000101800419b7b0f38ebc19a0855b97a916e00b40000000010d60000180400021c0000120300000000101800449b7b0f4edcc19a8a53b97a916400c30000000010d60000180400021c0000120300000000101800469b7b0fded1c19acb52b97a916e00b70000000010d60000180400021c0000120300000000101800649b7b0f7a69c19a154cb97a810000bc0000000010d60000180400021c0000120300000000101800709b7b0fcc5cc19a114fb97a915000970000000010d70000180400021c0000120300000000101800729b7b0fda53c19a8a53b97a91a0007d0000000010d70000180400021c0000120300000000101800749b7b0fa24ec19a3c5db97a91a000650000000010d70000180400021c0000120300000000101800789b7b0fe74ac19aca7eb97a910e015c0000000010d80000180400021c00001203000000001018007f9b7b0f6e46c19a78e0b97a9190015c0000000010d80000180400021c0000120300000000101800869b7b0f3641c19a144eba7a9190015c0000000010d60000180400021c00001203000000001018008d9b7b0ffd3bc19a74b9ba7a9190015c0000000010d40000180400021c0000120300000000101800949b7b0fc536c19a1524bb7a9186015b0000000010d20000180400021c00001203000000001018009b9b7b0fce30c19af78dbb7a919a015c0000000010d00000180400021c0000120300000000101800a29b7b0f552cc19a93fbbb7a9172015d0000000010cd0000180400021c0000120300000000101800b09b7b0f2521c19a6ec0bc7a9186015b0000000010c90000180400021c0000120300000000101800a99b7b0f5e26c19a8759bc7a915e015b0000000010cb0000180400021c0000120300000000101800b79b7b0fa81fc19a1328bd7a9172015b0000000010c70000180400021c0000120300000000101800be9b7b0f6b1dc19ac686bd7a914a015c0000000010c60000180400021c0000120300000000101800c19b7b0fed1bc19ad2a9bd7a912201530000000010c60000180400021c0000120300000000101800c39b7b0f6223c19af4bdbd7a910401420000000010c60000180400021c0000120300000000101800c29b7b0fe91ec19a42b4bd7a910e014c0000000010c60000180400021c0000120300000000101800c59b7b0f502fc19a1acfbd7a91fa00300000000010c60000180400021c0000120300000000101800c49b7b0fdb27c19ae6c6bd7a91fa00390000000010c60000180400021c0000120300000000101800c79b7b0fb83fc19a8ad9bd7a91f000180000000010c60000180400021b0000120300000000101800c69b7b0fc536c19a52d4bd7a91f000250000000010c60000180400021b0000120300000000101800ca9b7b0fc85fc19a81dfbd7a910401030000000010c50000180400021b0000120300000000101800c89b7b0fe74ac19a86dcbd7a91fa000e0000000010c50000180400021b0000120300000000101800d29b7b0f06b7c19afbe3bd7a91a000100000000010c50000180400021c0000120300000000101800d59b7b0ff0c5c19a6beebd7a91b400410000000010c40000180400021c0000120300000000101800d49b7b0ff4c2c19af2e9bd7a91a000310000000010c40000180400021c0000120300000000101800d39b7b0f3ebcc19a79e5bd7a9196001e0000000010c40000180400021c0000120300000000101800d69b7b0f6dc7c19ae0f5bd7a91c800570000000010c40000180400021c000012030000000016b7"));

        verifyPositions(decoder, binary(
                "0100020B0025003A5701C91A003A5701CD6E68490202101700CBB4740F7617FD924364104F116A0000000000010300001EC2"),
                position("2018-03-21 05:38:19.000", true, 51.67569, 55.59189));

        verifyPositions(decoder, binary(
                "0100020B0079000000011F6A001424951CA5CB0F23B5740F020210180023B5740F0A301994DA9C524C9128000A000000100082000011040018110300120900000003150100E803001B0700010000340900001B0700420000000000001B0700430000000000001B0700440000000000001B0700450000000000001B0700460000000000008020"));

        verifyPositions(decoder, binary(
                "0100020B00F200000001D66A001224951CA5CB0FFCB4740F0202101800FCB4740F502119943D9F524C9119805C000000100084000011040018110300120900000003150100E803001B0700410000000000001B0700420000000000001B0700430000000000001B0700440000000000001B0700450000000000001B0700460000000000006A001324951CA5CB0F05B5740F020210180005B5740F222519942D9E524C9100008B000000100083000011040018110300120900000003160100E803001B0700010000310900001B0700420000000000001B0700430000000000001B0700440000000000001B0700450000000000001B070046000000000000134E"));

    }

}
