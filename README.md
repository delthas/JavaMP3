# JavaMP3

**Currently only supports MP1 and MP2, see Status below. Support for MP3 is planned.**

## Introduction

JavaMP3 is a lightweight and fast API for decoding MP1, MP2, and MP3 files.

This API lets you:
- Decode MPEG-1/2/2.5 Layer I/II/III data (that is MP1, MP2, and MP3)
- Get a javax.sound.sampled.AudioInputStream to easily create a Clip from the decoded data and use the javax.sound.sampled API
- Get the decoded raw bytes in the format OpenAL takes so you can directly feed an OpenAL buffer with the decoded data
- Decode the data from several types of input such as InputStream, Path, byte[], byte[] with length and offset

## Install

JavaSkype requires Java >= 8 to run. You can get this library using Maven by adding this to your ```pom.xml```:

```xml
 <dependencies>
    <dependency>       
           <groupId>fr.delthas</groupId>
           <artifactId>javamp3</artifactId>
           <version>1.0.0</version>
    </dependency>
</dependencies>
```


## Quick example

This library is Object-oriented: you can use one of the several static functions in the Sound class to get a Sound object, and then call methods on it to get data and metadata. Let's have a look at how to interact with the library.


There are several ways to get a Sound object (ie decoding MPEG data) :

```java
// Getting and decoding a sound from a file
Path path = Paths.get("res","crazy_dnb.mp3");
Sound sound = Sound.createSound(path); // throws IOException if an error occured while reading the file or decoding its data

// Getting and decoding a sound from a resource file in your JAR
InputStream in = MyClass.class.getResourceAsStream("/mp3/rick_astley.mp3");
Sound sound = Sound.createSound(in) // throws IOException if an error occured while reading the resource file or decoding its data
}

// You can also decode a sound from a byte array, see the library Javadoc
```

Once you've got a Sound object, you may get the raw decoded PCM samples, or directly use the sound with the javax.sound.sampled API:


```java
// Creating a Clip from the sound and play it using the plain javax.sound.sampled API
Sound sound = /* ... */;
Clip clip = AudioSystem.getClip();
AudioInputStream stream = sound.newAudioInputStream();
clip.open(stream);
clip.start();

// Getting the raw decoded PCM samples to fill an OpenAL buffer (using LWJGL)
Sound sound = /* ... */;
int buffer = alGenBuffers();
ByteBuffer data = BufferUtils.createByteBuffer(sound.getBytes().length);
data.put(sound.getBytes()).flip(); // LWJGL needs a direct buffer, cannot simply wrap the array 
alBufferData(buffer, sound.isStereo() ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, data, sound.getSamplingFrequency());

```

## Documentation

The only public class is the Sound class.

The javadoc for the API is located at: https://mpeg.delthas.fr/

This API full supports multithread calls, there is no shared static state. For example, if trying to decode multiple MPEG files from a music folder, you are encouraged to use multiple threads.

## Building

Simply run ```maven package```.


## Status

- [X] MPEG-1 Audio Layer I Support
- [X] MPEG-1 Audio Layer II Support
- [ ] MPEG-1 Audio Layer III Support
- [ ] MPEG-2 Audio Layer I Support
- [ ] MPEG-2 Audio Layer II Support
- [ ] MPEG-2 Audio Layer II Support
- [ ] MPEG-2.5 Audio Layer I Support
- [ ] MPEG-2.5 Audio Layer II Support
- [ ] MPEG-2.5 Audio Layer III Support
- [ ] Tests

## Misceallenous

### Tech

JavaSkype uses no library except the standard Java library.:

### License

MIT
