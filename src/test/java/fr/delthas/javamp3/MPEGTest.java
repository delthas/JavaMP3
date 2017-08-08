package fr.delthas.javamp3;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class MPEGTest {
  
  private static void compare(String name, String refName, float threshold) throws IOException {
    double result = 0;
    int bytes = 0;
    try(InputStream is = new BufferedInputStream(MPEGTest.class.getResourceAsStream(refName)); InputStream ours = new Sound(new BufferedInputStream(MPEGTest.class.getResourceAsStream(name)))) {
      while(true) {
        int read = is.read();
        if(read == -1) {
          break;
        }
        int ref = read;
        read = is.read();
        if(read == -1) {
          break;
        }
        ref |= read << 8;
        ref = (int) ((short) ref);
        read = ours.read();
        if(read == -1) {
          Assert.fail();
        }
        int our = read;
        read = ours.read();
        if(read == -1) {
          Assert.fail();
        }
        our |= read << 8;
        our = (int) ((short) our);
        result += (our - ref) * (our - ref);
        bytes += 2;
      }
      Assert.assertEquals(ours.read(), -1);
    }
    result = Math.sqrt(result * 2 / bytes);
    Assert.assertTrue("RMS too large (discrepancy between our version and the reference one: " + result, result < threshold);
  }
  
  @Test
  public void MPEG_I_layer_I() throws IOException {
    compare("/mp1/stereo_kikuo.mp1", "/mp1/stereo_kikuo.raw", 0.05f);
    compare("/mp1/mono_kikuo.mp1", "/mp1/mono_kikuo.raw", 0.05f);
  }
  
  @Test
  public void MPEG_I_layer_II() throws IOException {
    compare("/mp2/stereo_kikuo.mp2", "/mp2/stereo_kikuo.raw", 0.05f);
    compare("/mp2/mono_kikuo.mp2", "/mp2/mono_kikuo.raw", 0.05f);
  }
  
  @Test
  public void MPEG_I_layer_III() throws IOException {
    compare("/mp3/joint_stereo_kikuo.mp3", "/mp3/joint_stereo_kikuo.raw", 5000f); // TODO more accurate joint stereo decoding
    compare("/mp3/stereo_kikuo.mp3", "/mp3/stereo_kikuo.raw", 0.05f);
    compare("/mp3/mono_kikuo.mp3", "/mp3/mono_kikuo.raw", 0.05f);
  }
}
