package fr.delthas.javamp3;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

final class Decoder {
  private static final int[] BITRATE_LAYER_I = {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448};
  private static final int[] BITRATE_LAYER_II = {0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384};
  private static final int[] BITRATE_LAYER_III = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320};
  private static final int[] SAMPLING_FREQUENCY = {44100, 48000, 32000};
  private static final float[] SCALEFACTORS = {2.00000000000000f, 1.58740105196820f, 1.25992104989487f, 1.00000000000000f, 0.79370052598410f, 0.62996052494744f, 0.50000000000000f, 0.39685026299205f, 0.31498026247372f, 0.25000000000000f, 0.19842513149602f, 0.15749013123686f, 0.12500000000000f, 0.09921256574801f, 0.07874506561843f, 0.06250000000000f, 0.04960628287401f, 0.03937253280921f, 0.03125000000000f, 0.02480314143700f, 0.01968626640461f, 0.01562500000000f, 0.01240157071850f, 0.00984313320230f, 0.00781250000000f, 0.00620078535925f, 0.00492156660115f, 0.00390625000000f, 0.00310039267963f, 0.00246078330058f, 0.00195312500000f, 0.00155019633981f, 0.00123039165029f, 0.00097656250000f, 0.00077509816991f, 0.00061519582514f, 0.00048828125000f, 0.00038754908495f, 0.00030759791257f, 0.00024414062500f, 0.00019377454248f, 0.00015379895629f, 0.00012207031250f, 0.00009688727124f, 0.00007689947814f, 0.00006103515625f, 0.00004844363562f, 0.00003844973907f, 0.00003051757813f, 0.00002422181781f, 0.00001922486954f, 0.00001525878906f, 0.00001211090890f, 0.00000961243477f, 0.00000762939453f, 0.00000605545445f, 0.00000480621738f, 0.00000381469727f, 0.00000302772723f, 0.00000240310869f, 0.00000190734863f, 0.00000151386361f, 0.00000120155435f};
  private static final float[] PRE_FRACTOR_LAYER_I;
  private static final float[] NIK_COEFFICIENTS;
  private static final float[] DI_COEFFICIENTS = {0.000000000f, -0.000015259f, -0.000015259f, -0.000015259f, -0.000015259f, -0.000015259f, -0.000015259f, -0.000030518f, -0.000030518f, -0.000030518f, -0.000030518f, -0.000045776f, -0.000045776f, -0.000061035f, -0.000061035f, -0.000076294f, -0.000076294f, -0.000091553f, -0.000106812f, -0.000106812f, -0.000122070f, -0.000137329f, -0.000152588f, -0.000167847f, -0.000198364f, -0.000213623f, -0.000244141f, -0.000259399f, -0.000289917f, -0.000320435f, -0.000366211f, -0.000396729f, -0.000442505f, -0.000473022f, -0.000534058f, -0.000579834f, -0.000625610f, -0.000686646f, -0.000747681f, -0.000808716f, -0.000885010f, -0.000961304f, -0.001037598f, -0.001113892f, -0.001205444f, -0.001296997f, -0.001388550f, -0.001480103f, -0.001586914f, -0.001693726f, -0.001785278f, -0.001907349f, -0.002014160f, -0.002120972f, -0.002243042f, -0.002349854f, -0.002456665f, -0.002578735f, -0.002685547f, -0.002792358f, -0.002899170f, -0.002990723f, -0.003082275f, -0.003173828f, 0.003250122f, 0.003326416f, 0.003387451f, 0.003433228f, 0.003463745f, 0.003479004f, 0.003479004f, 0.003463745f, 0.003417969f, 0.003372192f, 0.003280640f, 0.003173828f, 0.003051758f, 0.002883911f, 0.002700806f, 0.002487183f, 0.002227783f, 0.001937866f, 0.001617432f, 0.001266479f, 0.000869751f, 0.000442505f, -0.000030518f, -0.000549316f, -0.001098633f, -0.001693726f, -0.002334595f, -0.003005981f, -0.003723145f, -0.004486084f, -0.005294800f, -0.006118774f, -0.007003784f, -0.007919312f, -0.008865356f, -0.009841919f, -0.010848999f, -0.011886597f, -0.012939453f, -0.014022827f, -0.015121460f, -0.016235352f, -0.017349243f, -0.018463135f, -0.019577026f, -0.020690918f, -0.021789551f, -0.022857666f, -0.023910522f, -0.024932861f, -0.025909424f, -0.026840210f, -0.027725220f, -0.028533936f, -0.029281616f, -0.029937744f, -0.030532837f, -0.031005859f, -0.031387329f, -0.031661987f, -0.031814575f, -0.031845093f, -0.031738281f, -0.031478882f, 0.031082153f, 0.030517578f, 0.029785156f, 0.028884888f, 0.027801514f, 0.026535034f, 0.025085449f, 0.023422241f, 0.021575928f, 0.019531250f, 0.017257690f, 0.014801025f, 0.012115479f, 0.009231567f, 0.006134033f, 0.002822876f, -0.000686646f, -0.004394531f, -0.008316040f, -0.012420654f, -0.016708374f, -0.021179199f, -0.025817871f, -0.030609131f, -0.035552979f, -0.040634155f, -0.045837402f, -0.051132202f, -0.056533813f, -0.061996460f, -0.067520142f, -0.073059082f, -0.078628540f, -0.084182739f, -0.089706421f, -0.095169067f, -0.100540161f, -0.105819702f, -0.110946655f, -0.115921021f, -0.120697021f, -0.125259399f, -0.129562378f, -0.133590698f, -0.137298584f, -0.140670776f, -0.143676758f, -0.146255493f, -0.148422241f, -0.150115967f, -0.151306152f, -0.151962280f, -0.152069092f, -0.151596069f, -0.150497437f, -0.148773193f, -0.146362305f, -0.143264771f, -0.139450073f, -0.134887695f, -0.129577637f, -0.123474121f, -0.116577148f, -0.108856201f, 0.100311279f, 0.090927124f, 0.080688477f, 0.069595337f, 0.057617187f, 0.044784546f, 0.031082153f, 0.016510010f, 0.001068115f, -0.015228271f, -0.032379150f, -0.050354004f, -0.069168091f, -0.088775635f, -0.109161377f, -0.130310059f, -0.152206421f, -0.174789429f, -0.198059082f, -0.221984863f, -0.246505737f, -0.271591187f, -0.297210693f, -0.323318481f, -0.349868774f, -0.376800537f, -0.404083252f, -0.431655884f, -0.459472656f, -0.487472534f, -0.515609741f, -0.543823242f, -0.572036743f, -0.600219727f, -0.628295898f, -0.656219482f, -0.683914185f, -0.711318970f, -0.738372803f, -0.765029907f, -0.791213989f, -0.816864014f, -0.841949463f, -0.866363525f, -0.890090942f, -0.913055420f, -0.935195923f, -0.956481934f, -0.976852417f, -0.996246338f, -1.014617920f, -1.031936646f, -1.048156738f, -1.063217163f, -1.077117920f, -1.089782715f, -1.101211548f, -1.111373901f, -1.120223999f, -1.127746582f, -1.133926392f, -1.138763428f, -1.142211914f, -1.144287109f, 1.144989014f, 1.144287109f, 1.142211914f, 1.138763428f, 1.133926392f, 1.127746582f, 1.120223999f, 1.111373901f, 1.101211548f, 1.089782715f, 1.077117920f, 1.063217163f, 1.048156738f, 1.031936646f, 1.014617920f, 0.996246338f, 0.976852417f, 0.956481934f, 0.935195923f, 0.913055420f, 0.890090942f, 0.866363525f, 0.841949463f, 0.816864014f, 0.791213989f, 0.765029907f, 0.738372803f, 0.711318970f, 0.683914185f, 0.656219482f, 0.628295898f, 0.600219727f, 0.572036743f, 0.543823242f, 0.515609741f, 0.487472534f, 0.459472656f, 0.431655884f, 0.404083252f, 0.376800537f, 0.349868774f, 0.323318481f, 0.297210693f, 0.271591187f, 0.246505737f, 0.221984863f, 0.198059082f, 0.174789429f, 0.152206421f, 0.130310059f, 0.109161377f, 0.088775635f, 0.069168091f, 0.050354004f, 0.032379150f, 0.015228271f, -0.001068115f, -0.016510010f, -0.031082153f, -0.044784546f, -0.057617187f, -0.069595337f, -0.080688477f, -0.090927124f, 0.100311279f, 0.108856201f, 0.116577148f, 0.123474121f, 0.129577637f, 0.134887695f, 0.139450073f, 0.143264771f, 0.146362305f, 0.148773193f, 0.150497437f, 0.151596069f, 0.152069092f, 0.151962280f, 0.151306152f, 0.150115967f, 0.148422241f, 0.146255493f, 0.143676758f, 0.140670776f, 0.137298584f, 0.133590698f, 0.129562378f, 0.125259399f, 0.120697021f, 0.115921021f, 0.110946655f, 0.105819702f, 0.100540161f, 0.095169067f, 0.089706421f, 0.084182739f, 0.078628540f, 0.073059082f, 0.067520142f, 0.061996460f, 0.056533813f, 0.051132202f, 0.045837402f, 0.040634155f, 0.035552979f, 0.030609131f, 0.025817871f, 0.021179199f, 0.016708374f, 0.012420654f, 0.008316040f, 0.004394531f, 0.000686646f, -0.002822876f, -0.006134033f, -0.009231567f, -0.012115479f, -0.014801025f, -0.017257690f, -0.019531250f, -0.021575928f, -0.023422241f, -0.025085449f, -0.026535034f, -0.027801514f, -0.028884888f, -0.029785156f, -0.030517578f, 0.031082153f, 0.031478882f, 0.031738281f, 0.031845093f, 0.031814575f, 0.031661987f, 0.031387329f, 0.031005859f, 0.030532837f, 0.029937744f, 0.029281616f, 0.028533936f, 0.027725220f, 0.026840210f, 0.025909424f, 0.024932861f, 0.023910522f, 0.022857666f, 0.021789551f, 0.020690918f, 0.019577026f, 0.018463135f, 0.017349243f, 0.016235352f, 0.015121460f, 0.014022827f, 0.012939453f, 0.011886597f, 0.010848999f, 0.009841919f, 0.008865356f, 0.007919312f, 0.007003784f, 0.006118774f, 0.005294800f, 0.004486084f, 0.003723145f, 0.003005981f, 0.002334595f, 0.001693726f, 0.001098633f, 0.000549316f, 0.000030518f, -0.000442505f, -0.000869751f, -0.001266479f, -0.001617432f, -0.001937866f, -0.002227783f, -0.002487183f, -0.002700806f, -0.002883911f, -0.003051758f, -0.003173828f, -0.003280640f, -0.003372192f, -0.003417969f, -0.003463745f, -0.003479004f, -0.003479004f, -0.003463745f, -0.003433228f, -0.003387451f, -0.003326416f, 0.003250122f, 0.003173828f, 0.003082275f, 0.002990723f, 0.002899170f, 0.002792358f, 0.002685547f, 0.002578735f, 0.002456665f, 0.002349854f, 0.002243042f, 0.002120972f, 0.002014160f, 0.001907349f, 0.001785278f, 0.001693726f, 0.001586914f, 0.001480103f, 0.001388550f, 0.001296997f, 0.001205444f, 0.001113892f, 0.001037598f, 0.000961304f, 0.000885010f, 0.000808716f, 0.000747681f, 0.000686646f, 0.000625610f, 0.000579834f, 0.000534058f, 0.000473022f, 0.000442505f, 0.000396729f, 0.000366211f, 0.000320435f, 0.000289917f, 0.000259399f, 0.000244141f, 0.000213623f, 0.000198364f, 0.000167847f, 0.000152588f, 0.000137329f, 0.000122070f, 0.000106812f, 0.000106812f, 0.000091553f, 0.000076294f, 0.000076294f, 0.000061035f, 0.000061035f, 0.000045776f, 0.000045776f, 0.000030518f, 0.000030518f, 0.000030518f, 0.000030518f, 0.000015259f, 0.000015259f, 0.000015259f, 0.000015259f, 0.000015259f, 0.000015259f};
  private static final int[] SHIFT_ENDIANESS = {255, 254, 253, 252, 251, 250, 249, 248, 247, 246, 245, 244, 243, 242, 241, 240, 239, 238, 237, 236, 235, 234, 233, 232, 231, 230, 229, 228, 227, 226, 225, 224, 223, 222, 221, 220, 219, 218, 217, 216, 215, 214, 213, 212, 211, 210, 209, 208, 207, 206, 205, 204, 203, 202, 201, 200, 199, 198, 197, 196, 195, 194, 193, 192, 191, 190, 189, 188, 187, 186, 185, 184, 183, 182, 181, 180, 179, 178, 177, 176, 175, 174, 173, 172, 171, 170, 169, 168, 167, 166, 165, 164, 163, 162, 161, 160, 159, 158, 157, 156, 155, 154, 153, 152, 151, 150, 149, 148, 147, 146, 145, 144, 143, 142, 141, 140, 139, 138, 137, 136, 135, 134, 133, 132, 131, 130, 129, 128, 127, 126, 125, 124, 123, 122, 121, 120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
  private static final int[] SB_LIMIT = {27, 30, 8, 12};
  private static final int[][] NBAL = {{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2}, {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2}, {4, 4, 3, 3, 3, 3, 3, 3}, {4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}};
  private static final int[][][] QUANTIZATION_INDEX_LAYER_II = {{{0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}}, {{0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 2, 3, 4, 5, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}, {0, 1, 16}}, {{0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}}, {{0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}, {0, 1, 3, 4, 5, 6, 7}}};
  private static final int[] NLEVELS = {3, 5, 7, 9, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535};
  private static final float[] C_LAYER_II = {1.33333333333f, 1.60000000000f, 1.14285714286f, 1.77777777777f, 1.06666666666f, 1.03225806452f, 1.01587301587f, 1.00787401575f, 1.00392156863f, 1.00195694716f, 1.00097751711f, 1.00048851979f, 1.00024420024f, 1.00012208522f, 1.00006103888f, 1.00003051851f, 1.00001525902f};
  private static final float[] D_LAYER_II = {0.50000000000f, 0.50000000000f, 0.25000000000f, 0.50000000000f, 0.12500000000f, 0.06250000000f, 0.03125000000f, 0.01562500000f, 0.00781250000f, 0.00390625000f, 0.00195312500f, 0.00097656250f, 0.00048828125f, 0.00024414063f, 0.00012207031f, 0.00006103516f, 0.00003051758f};
  private static final boolean[] GROUPING_LAYER_II = {true, true, false, true, false, false, false, false, false, false, false, false, false, false, false, false, false};
  private static final int[] BITS_LAYER_II = {5, 7, 3, 10, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
  
  static {
    PRE_FRACTOR_LAYER_I = new float[16];
    for (int i = 0; i < 16; i++) {
      double pow = 0b1 << i;
      PRE_FRACTOR_LAYER_I[i] = (float) (pow / (pow - 1));
    }
    NIK_COEFFICIENTS = new float[64 * 32];
    for (int i = 0; i < 64; i++) {
      for (int k = 0; k < 32; k++) {
        double value = 1e9 * Math.cos((16 + i) * (2 * k + 1) * Math.PI / 64);
        if (value >= 0) {
          value = (int) (value + 0.5);
        } else {
          value = (int) (value - 0.5);
        }
        NIK_COEFFICIENTS[i * 32 + k] = (float) (value * 1e-9);
      }
    }
  }
  
  private Decoder() {
    throw new IllegalStateException("This class cannot be instantiated!");
  }
  
  public static Sound decode(InputStream inputStream) throws IOException {
    Buffer buffer = new Buffer(inputStream);
    buffer.lastByte = buffer.in.read();
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
    int frequency = -1;
    int stereo = -1;
    
    int[] synthOffset = null;
    float[] synthBuffer = null;
    
    int samples = 0;
    
    outer:
    while (true) {
      
      if (buffer.lastByte == -1) {
        break;
      }
      
      while (true) {
        int read;
        do {
          read = buffer.lastByte;
          buffer.lastByte = buffer.in.read();
          if (buffer.lastByte == -1) {
            break outer;
          }
        } while (read != 0b11111111);
        if ((buffer.lastByte >>> 4) != 0b1111) {
          buffer.lastByte = buffer.in.read();
          if (buffer.lastByte == -1) {
            break outer;
          }
        } else {
          break;
        }
      }
      
      buffer.current = 4;
      
      int id = read(buffer, 1);
      int layer = read(buffer, 2);
      int protectionBit = read(buffer, 1);
      int bitrateIndex = read(buffer, 4);
      int samplingFrequency = read(buffer, 2);
      int paddingBit = read(buffer, 1);
      int privateBit = read(buffer, 1);
      int mode = read(buffer, 2);
      int modeExtension = read(buffer, 2);
      read(buffer, 4);
      
      if (frequency == -1) {
        frequency = SAMPLING_FREQUENCY[samplingFrequency];
      }
      
      if (stereo == -1) {
        if (mode == 0b11 /* single_channel */) {
          stereo = 0;
          synthOffset = new int[]{64};
          synthBuffer = new float[1024];
        } else {
          stereo = 1;
          synthOffset = new int[]{64, 64};
          synthBuffer = new float[2 * 1024];
        }
      }
      
      int bound = modeExtension == 0b0 ? 4 : modeExtension == 0b01 ? 8 : modeExtension == 0b10 ? 12 : modeExtension == 0b11 ? 16 : -1;
      
      if (protectionBit == 0) {
        // TODO CRC CHECK
        read(buffer, 16);
      }
      
      if (layer == 0b11 /* layer I */) {
        float[] sampleDecoded = null;
        if (mode == 0b11 /* single_channel */) {
          sampleDecoded = samples_I(buffer, 1, -1);
        } else if (mode == 0b0 /* stereo */ || mode == 0b10 /* dual_channel */) {
          sampleDecoded = samples_I(buffer, 2, -1);
        } else if (mode == 0b01 /* intensity_stereo */) {
          sampleDecoded = samples_I(buffer, 2, bound);
        }
        if (mode == 0b11 /* single_channel */) {
          synth(baos, sampleDecoded, synthOffset, synthBuffer, 1);
        } else {
          synth(baos, sampleDecoded, synthOffset, synthBuffer, 2);
        }
      } else if (layer == 0b10 /* layer II */) {
        float[] sampleDecoded = null;
        int bitrate = BITRATE_LAYER_II[bitrateIndex];
        if (mode == 0b11 /* single_channel */) {
          sampleDecoded = samples_II(buffer, 1, -1, bitrate, frequency);
        } else if (mode == 0b0 /* stereo */ || mode == 0b10 /* dual_channel */) {
          sampleDecoded = samples_II(buffer, 2, -1, bitrate, frequency);
        } else if (mode == 0b01 /* intensity_stereo */) {
          sampleDecoded = samples_II(buffer, 2, bound, bitrate, frequency);
        }
        if (mode == 0b11 /* single_channel */) {
          samples += synth(baos, sampleDecoded, synthOffset, synthBuffer, 1);
        } else {
          samples += synth(baos, sampleDecoded, synthOffset, synthBuffer, 2);
        }
      } else if (layer == 0b01 /* layer III */) {
        // TODO
      }
      
      if (buffer.current != 0) {
        read(buffer, 8 - buffer.current);
      }
    }
    
    if (samples == 0) {
      return null;
    }
    
    return new Sound(baos.toByteArray(), frequency, stereo == 1, samples);
  }
  
  private static int read(Buffer buffer, int bits) throws IOException {
    int number = 0;
    while (bits > 0) {
      int advance = Integer.min(bits, 8 - buffer.current);
      bits -= advance;
      buffer.current += advance;
      if (bits != 0 && buffer.lastByte == -1) {
        throw new EOFException("Unexpected EOF reached in MPEG data");
      }
      number |= ((buffer.lastByte >>> (8 - buffer.current)) & (0xFF >>> (8 - advance))) << bits;
      if (buffer.current == 8) {
        buffer.current = 0;
        buffer.lastByte = buffer.in.read();
      }
    }
    return number;
  }
  
  private static float[] samples_I(Buffer buffer, int stereo, int bound) throws IOException {
    if (bound < 0) {
      bound = 32;
    }
    int[] allocation = new int[32 - bound];
    int[] allocationChannel = new int[stereo * bound];
    int[] scalefactorChannel = new int[stereo * 32];
    float[] sampleDecoded = new float[stereo * 32 * 12];
    for (int sb = 0; sb < bound; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        allocationChannel[ch * bound + sb] = read(buffer, 4);
      }
    }
    for (int sb = bound; sb < 32; sb++) {
      allocation[sb - bound] = read(buffer, 4);
    }
    for (int sb = 0; sb < bound; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocationChannel[ch * bound + sb] != 0) {
          scalefactorChannel[ch * 32 + sb] = read(buffer, 6);
        }
      }
    }
    for (int sb = bound; sb < 32; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocation[sb - bound] != 0) {
          scalefactorChannel[ch * 32 + sb] = read(buffer, 6);
        }
      }
    }
    for (int s = 0; s < 12; s++) {
      for (int sb = 0; sb < bound; sb++) {
        for (int ch = 0; ch < stereo; ch++) {
          int n = allocationChannel[ch * bound + sb];
          if (n == 0) {
            sampleDecoded[ch * 32 * 12 + sb * 12 + s] = 0;
          } else {
            int read = read(buffer, n + 1);
            float fraction = 0;
            if (((read >> n) & 0b1) == 0) {
              fraction = -1;
            }
            fraction += (float) (read & ((0b1 << n) - 1)) / (0b1 << n) + 1f / (0b1 << n);
            sampleDecoded[ch * 32 * 12 + sb * 12 + s] = SCALEFACTORS[scalefactorChannel[ch * 32 + sb]] * PRE_FRACTOR_LAYER_I[n + 1] * fraction;
          }
        }
      }
      for (int sb = bound; sb < 32; sb++) {
        int n = allocationChannel[sb - bound];
        if (n == 0) {
          sampleDecoded[0 * 32 * 12 + sb * 12 + s] = sampleDecoded[1 * 32 * 12 + sb * 12 + s] = 0;
        } else {
          int read = read(buffer, n + 1);
          float fraction = 0;
          if (((read >> n) & 0b1) == 0) {
            fraction = -1;
          }
          fraction += (float) (read & ((0b1 << n) - 1)) / (0b1 << n) + 1f / (0b1 << n);
          for (int ch = 0; ch < 2; ch++) {
            sampleDecoded[ch * 32 * 12 + sb * 12 + s] = SCALEFACTORS[scalefactorChannel[ch * 32 + sb]] * PRE_FRACTOR_LAYER_I[n + 1] * fraction;
          }
        }
      }
    }
    return sampleDecoded;
  }
  
  private static float[] samples_II(Buffer buffer, int stereo, int bound, int bitrate, int frequency) throws IOException {
    int sbIndex = 0;
    if (frequency != 48000 && (bitrate >= 96 || bitrate == 0)) {
      sbIndex = 1;
    } else if (frequency != 32000 && (bitrate > 0 && bitrate <= 48)) {
      sbIndex = 2;
    } else if (frequency == 32000 && (bitrate > 0 && bitrate <= 48)) {
      sbIndex = 3;
    }
    int sbLimit = SB_LIMIT[sbIndex];
    if (bound < 0) {
      bound = sbLimit;
    }
    int[] allocation = new int[sbLimit - bound];
    int[] allocationChannel = new int[stereo * bound];
    int[] scfsi = new int[stereo * sbLimit];
    int[] scalefactorChannel = new int[stereo * sbLimit * 3];
    float[] sampleDecoded = new float[stereo * 32 * 12 * 3];
    for (int sb = 0; sb < bound; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        allocationChannel[ch * bound + sb] = read(buffer, NBAL[sbIndex][sb]);
      }
    }
    for (int sb = bound; sb < sbLimit; sb++) {
      allocation[sb - bound] = read(buffer, NBAL[sbIndex][sb]);
    }
    for (int sb = 0; sb < bound; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocationChannel[ch * bound + sb] != 0) {
          scfsi[ch * sbLimit + sb] = read(buffer, 2);
        }
      }
    }
    for (int sb = bound; sb < sbLimit; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocation[sb - bound] != 0) {
          scfsi[ch * sbLimit + sb] = read(buffer, 2);
        }
      }
    }
    for (int sb = 0; sb < bound; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocationChannel[ch * bound + sb] != 0) {
          int offset = ch * sbLimit * 3 + sb * 3;
          if (scfsi[ch * sbLimit + sb] == 0) {
            scalefactorChannel[offset + 0] = read(buffer, 6);
            scalefactorChannel[offset + 1] = read(buffer, 6);
            scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 1) {
            scalefactorChannel[offset + 0] = scalefactorChannel[offset + 1] = read(buffer, 6);
            scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 2) {
            scalefactorChannel[offset + 0] = scalefactorChannel[offset + 1] = scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 3) {
            scalefactorChannel[offset + 0] = read(buffer, 6);
            scalefactorChannel[offset + 1] = scalefactorChannel[offset + 2] = read(buffer, 6);
          }
        }
      }
    }
    for (int sb = bound; sb < sbLimit; sb++) {
      for (int ch = 0; ch < stereo; ch++) {
        if (allocation[sb - bound] != 0) {
          int offset = ch * sbLimit * 3 + sb * 3;
          if (scfsi[ch * sbLimit + sb] == 0) {
            scalefactorChannel[offset + 0] = read(buffer, 6);
            scalefactorChannel[offset + 1] = read(buffer, 6);
            scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 1) {
            scalefactorChannel[offset + 0] = scalefactorChannel[offset + 1] = read(buffer, 6);
            scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 2) {
            scalefactorChannel[offset + 0] = scalefactorChannel[offset + 1] = scalefactorChannel[offset + 2] = read(buffer, 6);
          } else if (scfsi[ch * sbLimit + sb] == 3) {
            scalefactorChannel[offset + 0] = read(buffer, 6);
            scalefactorChannel[offset + 1] = scalefactorChannel[offset + 2] = read(buffer, 6);
          }
        }
      }
    }
    for (int gr = 0; gr < 12; gr++) {
      for (int sb = 0; sb < bound; sb++) {
        for (int ch = 0; ch < stereo; ch++) {
          int n = allocationChannel[ch * bound + sb];
          int offset = ch * 32 * 12 * 3 + sb * 12 * 3 + gr * 3;
          if (n == 0) {
            sampleDecoded[offset] = sampleDecoded[offset + 1] = sampleDecoded[offset + 2] = 0;
          } else {
            int index = QUANTIZATION_INDEX_LAYER_II[sbIndex][sb][n - 1];
            int[] sampleInt = new int[3];
            int sampleBits = BITS_LAYER_II[index];
            int nlevels = NLEVELS[index];
            if (GROUPING_LAYER_II[index]) {
              int samplecode = read(buffer, sampleBits);
              sampleInt[0] = samplecode % nlevels;
              samplecode /= nlevels;
              sampleInt[1] = samplecode % nlevels;
              samplecode /= nlevels;
              sampleInt[2] = samplecode % nlevels;
            } else {
              sampleInt[0] = read(buffer, sampleBits);
              sampleInt[1] = read(buffer, sampleBits);
              sampleInt[2] = read(buffer, sampleBits);
            }
            int msb = 0;
            while ((0b1 << msb) <= nlevels) {
              msb++;
            }
            msb--;
            for (int i = 0; i < 3; i++) {
              float sample = 0;
              if (((sampleInt[i] >> msb) & 0b1) == 0) {
                sample = -1;
              }
              sample += (float) (sampleInt[i] & ((0b1 << msb) - 1)) / (0b1 << msb);
              sample += D_LAYER_II[index];
              sample *= C_LAYER_II[index];
              sample *= SCALEFACTORS[scalefactorChannel[ch * sbLimit * 3 + sb * 3 + gr / 4]];
              sampleDecoded[offset + i] = sample;
            }
          }
        }
      }
      for (int sb = bound; sb < sbLimit; sb++) {
        int n = allocation[sb - bound];
        int offset = sb * 12 * 3 + gr * 3;
        if (n == 0) {
          for (int ch = 0; ch < stereo; ch++) {
            sampleDecoded[offset + ch * 32 * 12 * 3] = sampleDecoded[offset + ch * 32 * 12 * 3 + 1] = sampleDecoded[offset + ch * 32 * 12 * 3 + 2] = 0;
          }
        } else {
          int index = QUANTIZATION_INDEX_LAYER_II[sbIndex][sb][n - 1];
          int[] sampleInt = new int[3];
          int sampleBits = BITS_LAYER_II[index];
          int nlevels = NLEVELS[index];
          if (GROUPING_LAYER_II[index]) {
            int samplecode = read(buffer, sampleBits);
            sampleInt[0] = samplecode % nlevels;
            samplecode /= nlevels;
            sampleInt[1] = samplecode % nlevels;
            samplecode /= nlevels;
            sampleInt[2] = samplecode % nlevels;
          } else {
            sampleInt[0] = read(buffer, sampleBits);
            sampleInt[1] = read(buffer, sampleBits);
            sampleInt[2] = read(buffer, sampleBits);
          }
          int msb = 0;
          while ((0b1 << msb) <= nlevels) {
            msb++;
          }
          msb--;
          for (int i = 0; i < 3; i++) {
            float sample = 0;
            if (((sampleInt[i] >> msb) & 0b1) == 0) {
              sample = -1;
            }
            sample += (float) (sampleInt[i] & ((0b1 << msb) - 1)) / (0b1 << msb);
            sample += D_LAYER_II[index];
            sample *= C_LAYER_II[index];
            for (int ch = 0; ch < stereo; ch++) {
              sampleDecoded[offset + ch * 32 * 12 * 3 + i] = sample * SCALEFACTORS[scalefactorChannel[ch * sbLimit * 3 + sb * 3 + gr / 4]];
            }
          }
        }
      }
    }
    return sampleDecoded;
  }
  
  private static int synth(ByteArrayOutputStream os, float[] samples, int[] synthOffset, float[] synthBuffer, int stereo) {
    int size = samples.length / stereo / 32;
    float[] pcm = new float[size * 32 * stereo];
    for (int ch = 0; ch < stereo; ch++) {
      for (int s = 0; s < size; s++) {
        synthOffset[ch] = (synthOffset[ch] - 64) & 0x3ff;
        for (int i = 0; i < 64; i++) {
          float sum = 0;
          for (int k = 0; k < 32; k++) {
            sum += NIK_COEFFICIENTS[i * 32 + k] * samples[ch * 32 * size + k * size + s];
          }
          synthBuffer[ch * 1024 + synthOffset[ch] + i] = sum;
        }
        for (int j = 0; j < 32; j++) {
          float sum = 0;
          for (int i = 0; i < 16; i++) {
            int k = j + (i << 5);
            sum += DI_COEFFICIENTS[k] * synthBuffer[ch * 1024 + ((synthOffset[ch] + (k + (((i + 1) >> 1) << 6))) & 0x3FF)];
          }
          pcm[s * 32 * stereo + j * stereo + ch] = sum;
        }
      }
    }
    for (int i = 0; i < size * 32 * stereo; i++) {
      int sample = (int) (pcm[i] * 32768);
      if (sample >= 32768) {
        sample = 32767;
      } else if (sample < -32768) {
        sample = -32768;
      }
      os.write(SHIFT_ENDIANESS[sample & 0xFF]);
      os.write(SHIFT_ENDIANESS[(sample >>> 8) & 0xFF]);
    }
    return size * 32 * stereo;
  }
  
  private static class Buffer {
    public final InputStream in;
    public int current = 0;
    public int lastByte = -1;
    
    public Buffer(InputStream inputStream) {
      in = inputStream;
    }
  }
}
