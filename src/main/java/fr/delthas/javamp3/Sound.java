package fr.delthas.javamp3;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A sound object, that stores decoded PCM sound data, and some associated metadata such as sampling frequency.
 * <p>
 * To create a sound object from encoded MPEG data (MP1/MP2/MP3), use one of the {@code createSound} methods.
 * <p>
 * To get the raw decoded PCM samples (for example to feed it to an OpenAL buffer), use {@link Sound#getBytes()}.
 * To get a {@link javax.sound.sampled.Clip} from the decoded data, use {@link Sound#getAudioFormat()} or {@link Sound#newAudioInputStream()}.
 *
 * @see Sound#getBytes()
 * @see Sound#getAudioFormat()
 * @see Sound#newAudioInputStream()
 */
public final class Sound {
  private final byte[] bytes;
  private final int samplingFrequency;
  private final boolean stereo;
  private final int samplesCount;
  private AudioFormat audioFormat;
  
  Sound(byte[] bytes, int samplingFrequency, boolean stereo, int samplesCount) {
    this.bytes = bytes;
    this.samplingFrequency = samplingFrequency;
    this.stereo = stereo;
    this.samplesCount = samplesCount;
  }
  
  /**
   * Creates a new {@link Sound} from the encoded MPEG data stored in the array, starting at {@code offset} inclusive and ending at {@code offset+length} exclusive, by <b>decoding it fully</b>.
   * <p>
   * The array will <b>NOT BE COPIED</b> and will be continuously read during the decoding process. However you can change the array when this method returns since it's only used during the decoding process.
   * <p>
   * This method will return {@code null} if there are no MPEG data frames.
   * <p>
   * This method will block for the whole duration of the decoding process, which might take several seconds. You can call this method from multiple threads concurrently as there is no shared static state.
   *
   * @param data   The array containing the encoded MPEG data.
   * @param offset The start index (inclusive) of the data in the array.
   * @param length The length of the data in bytes in the array.
   * @return A new {@link Sound} containing the decoded PCM data from the encoded MPEG data, or null if there are no MPEG data frames in the MPEG data.
   * @throws IOException If there's an unexpected EOF during an MPEG frame, or if there's an error while decoding the MPEG data.
   */
  public static Sound createSound(byte[] data, int offset, int length) throws IOException {
    try (ByteArrayInputStream in = new ByteArrayInputStream(data, offset, length)) {
      return createSound_(in);
    }
  }
  
  /**
   * Creates a new {@link Sound} from the encoded MPEG data stored in the array, by <b>decoding it fully</b>.
   * <p>
   * The array will <b>NOT BE COPIED</b> and will be continuously read during the decoding process. However you can change the array when this method returns since it's only used during the decoding process.
   * <p>
   * This method will return {@code null} if there are no MPEG data frames.
   * <p>
   * This method will block for the whole duration of the decoding process, which might take several seconds. You can call this method from multiple threads concurrently as there is no shared static state.
   *
   * @param data The array containing the encoded MPEG data.
   * @return A new {@link Sound} containing the decoded PCM data from the encoded MPEG data, or null if there are no MPEG data frames in the MPEG data.
   * @throws IOException If there's an unexpected EOF during an MPEG frame, or if there's an error while decoding the MPEG data.
   */
  public static Sound createSound(byte[] data) throws IOException {
    try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
      return createSound_(in);
    }
  }
  
  /**
   * Creates a new {@link Sound} from the encoded MPEG data to be read from the input stream, by <b>decoding it fully</b>.
   * <p>
   * This method will return {@code null} if there are no MPEG data frames.
   * <p>
   * This method will block for the whole duration of the decoding process, which might take several seconds. You can call this method from multiple threads concurrently as there is no shared static state.
   *
   * @param in The input stream from which to read the encoded MPEG data.
   * @return A new {@link Sound} containing the decoded PCM data from the encoded MPEG data, or null if there are no MPEG data frames in the read MPEG data.
   * @throws IOException If an {@link IOException} is thrown when reading the stream, or if there's an unexpected EOF during an MPEG frame, or if there's an error while decoding the MPEG data.
   */
  public static Sound createSound(InputStream in) throws IOException {
    return createSound_(in);
  }
  
  /**
   * Creates a new {@link Sound} from the encoded MPEG data to be read from the file, by <b>decoding it fully</b>. You should only pass {@code .mp1}, {@code .mp2}, and {@code .mp3} files to this method are these are the only extensions for the supported audio formats for the decoding process.
   * <p>
   * This method will return {@code null} if there are no MPEG data frames.
   * <p>
   * This method will block for the whole duration of the decoding process, which might take several seconds. You can call this method from multiple threads concurrently as there is no shared static state.
   *
   * @param path The file from which to read the encoded MPEG data.
   * @return A new {@link Sound} containing the decoded PCM data from the encoded MPEG data, or null if there are no MPEG data frames in the read MPEG data.
   * @throws IOException If an {@link IOException} is thrown when reading the file, or if there's an unexpected EOF during an MPEG frame, or if there's an error while decoding the MPEG data.
   */
  public static Sound createSound(Path path) throws IOException {
    try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {
      return createSound_(in);
    }
  }
  
  private static Sound createSound_(InputStream in) throws IOException {
    return Decoder.decode(in);
  }
  
  /**
   * Returns the decoded PCM sound data, as a contiguous array of 16-bit little-endian signed samples (2 bytes per sample).
   * If the sound is in stereo mode, then the samples will be interleaved, e.g. {@code left_sample_0 (2 bytes), right_sample_0 (2 bytes), left_sample_1 (2 bytes), right_sample_1 (2 bytes), ...}
   * If the sound is in mono mode, then the samples will be contiguous, e.g. {@code sample_0 (2 bytes), sample_1 (2 bytes), ...}
   * <p>
   * <b>The bytes returned are NOT COPIED from the internal sound object byte array, to improve performance. All calls on this method will return the same array. Please copy the returned array yourself if you need to get a copy of it.</b>
   *
   * @return The decoded PCM sound data as described above.
   */
  public byte[] getBytes() {
    return bytes;
  }
  
  /**
   * Returns the sampling frequency of this sound, that is the number of samples per second, in Hertz (Hz).
   * <p>
   * For example for a 48kHz sound this would return {@code 48000}.
   *
   * @return The sampling frequency of the sound in Hertz.
   */
  public int getSamplingFrequency() {
    return samplingFrequency;
  }
  
  /**
   * Returns {@code true} if the sound is in stereo mode, that is if it has exactly two channels, and returns false otherwise, that is if it has exactly one channel.
   *
   * @return {@code true} if the sound is in stereo mode.
   */
  public boolean isStereo() {
    return stereo;
  }
  
  /**
   * Returns the number of samples of this sound.
   *
   * @return The number of samples of this sound.
   * @see #getLength()
   */
  public int getSamplesCount() {
    return samplesCount;
  }
  
  /**
   * Returns the {@link AudioFormat} of this sound, to be used with the {@link javax.sound.sampled} API.
   *
   * @return The {@link AudioFormat} of this sound.
   */
  public AudioFormat getAudioFormat() {
    if (audioFormat == null) {
      audioFormat = new AudioFormat(samplingFrequency, 16, stereo ? 2 : 1, true, false);
    }
    return audioFormat;
  }
  
  /**
   * Returns the length of this sound in seconds.
   * <p>
   * This method is equivalent to {@code (float)getSamplesCount() / getSamplingFrequency() / (isStereo() ? 2 : 1)}.
   *
   * @return The length of this sound in seconds.
   * @see #getSamplesCount()
   */
  public float getLength() {
    return (float) getSamplesCount() / getSamplingFrequency() / (isStereo() ? 2 : 1);
  }
  
  /**
   * Creates a new {@link AudioInputStream} of this sound, to be used with the {@link javax.sound.sampled} API.
   * <p>
   * This will return a new input stream each time this method is called, so you may call this multiple times and/or concurrently.
   * <p>
   * This method is equivalent to : {@code return new AudioInputStream(new ByteArrayInputStream(getBytes()), getAudioFormat(), getSamplesCount());}.
   *
   * @return A new {@link AudioInputStream} for this sound.
   */
  public AudioInputStream newAudioInputStream() {
    return new AudioInputStream(new ByteArrayInputStream(getBytes()), getAudioFormat(), getSamplesCount());
  }
}
