![](https://img.shields.io/travis/Delthas/JavaMP3.svg) 
![](https://img.shields.io/github/license/Delthas/JavaMP3.svg)
![](https://img.shields.io/maven-central/v/fr.delthas/javamp3.svg)
# JavaMP3

**I have no interest in maintainaing this library any more. Feel free to fork.**

---

**Currently supports MPEG-1 Layer I/II/III (that is, most MP1, MP2, and MP3 files)**

## Introduction

JavaMP3 is a lightweight (minimalist) and fast API for decoding MP1, MP2, and MP3 files.

This API lets you:
- Decode MPEG-1/2/2.5 Layer I/II/III data (that is MP1, MP2, and MP3)
- Get a javax.sound.sampled.AudioFormat to easily use the javax.sound.sampled API and play back the decoded data.
- Get the decoded raw bytes in the format OpenAL takes so you can directly feed an OpenAL buffer with the decoded data

## Install

JavaMP3 requires Java >= 8 to run. You can get this library using Maven by adding this to your ```pom.xml```:

```xml
 <dependencies>
    <dependency>       
           <groupId>fr.delthas</groupId>
           <artifactId>javamp3</artifactId>
           <version>1.0.1</version>
    </dependency>
</dependencies>
```


## Quick example

The only public class in this library is the Sound class. It is simply an InputStream that return decoded PCM sound samples as you read from the stream, decoded from the specified underlying stream.

There are also several metadata-related methods to be able to get the sound sampling frequency, stereo mode, ...

```java
// Getting a Sound from a file
Path path = Paths.get("res","crazy_dnb.mp3");
try(Sound sound = new Sound(new BufferedInputStream(Files.newInputStream(path)))) {
  // no need to buffer the SoundInputStream
  
  // get sound metadata
  System.out.println(sound.getSamplingFrequency());
  
  // let's copy the decoded data samples into a file!
  Files.copy(sound, Paths.get("/my/path/to/raw.raw"));
}


// Another example: getting and decoding a sound from a resource file in your JAR
try(Sound sound = new Sound(new BufferedInputStream(MyClass.class.getResourceAsStream("/mp3/rick_astley.mp3")))) {
  // ...
}

```

As expected from an InputStream, the creation of the stream will not block, and it is only when reading bytes from the stream, that the decoding process will take place. You may get metadata about the sound as soon as you have instantiated it.

Let's have a look at some examples on how to use the decoded raw PCM sound data samples:

```java
// Creating a Clip from the sound and play it using the plain javax.sound.sampled API
Sound sound = /* ... */;
// We use an array to store the produced sound data (bad code style, but is okay for short sounds)
// (We have to store the data in order to get the number of samples in it, because of the (dumb) Java sound API)
ByteArrayOutputStream os = new ByteArrayOutputStream();
// Read and decode the encoded sound data into the byte array output stream (blocking)
int read = sound.decodeFullyInto(os);
// A sample takes 2 bytes
int samples = read / 2;
// Java sound API stuff ...
Clip clip = AudioSystem.getClip();
AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(os.toByteArray()), sound.getAudioFormat(), samples);
clip.open(stream);
clip.start();

// Getting the raw decoded PCM samples to fill an OpenAL buffer (using LWJGL)
Sound sound = /* ... */;
// Let's store the whole decoded data in an array, because we need the number of samples; it's okay for short sounds
ByteArrayOutputStream os = new ByteArrayOutputStream();
int read = sound.decodeFullyInto(os);
// LWJGL API stuff ...
ByteBuffer data = BufferUtils.createByteBuffer(read);
data.put(os.toByteArray()).flip(); // LWJGL needs a direct buffer, cannot simply wrap the BAOS array
alBufferData(buffer, sound.isStereo() ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, data, sound.getSamplingFrequency());

```

## Documentation

The only public class is the Sound class.

The javadoc for the API is located here: [![Javadocs](http://www.javadoc.io/badge/fr.delthas/javamp3.svg)](http://www.javadoc.io/doc/fr.delthas/javamp3).

You are encouraged to read the decoded data stream in a streaming way, and/or make use of multithreaded calls (i.e. decode the sound data in a background thread if your application needs to react in real-time to user input).

## Building

Simply run ```mvn install```.


## Status

- [X] MPEG-1 Audio Layer I Support
- [X] MPEG-1 Audio Layer II Support
- [X] MPEG-1 Audio Layer III Support
- [ ] MPEG-2 Audio Layer I Support
- [ ] MPEG-2 Audio Layer II Support
- [ ] MPEG-2 Audio Layer II Support
- [ ] MPEG-2.5 Audio Layer I Support
- [ ] MPEG-2.5 Audio Layer II Support
- [ ] MPEG-2.5 Audio Layer III Support
- [X] Tests
- [ ] Fast seeking support
- [ ] Fast samples count fetching support

## Misceallenous

### Tech

JavaMP3 uses no library except the standard Java library, except for the compile-time JUnit dependency, for testing.

### License

MIT
