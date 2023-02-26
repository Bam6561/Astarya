package commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * AudioPlayerSendHandler is a component of LavaPlayer that handles
 * the bot's ability to play tracks in connected voice channels.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.1.0
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
  private final AudioPlayer audioPlayer;
  private final ByteBuffer buffer;
  private final MutableAudioFrame frame;

  public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.buffer = ByteBuffer.allocate(1024); // Allocated memory per 20ms
    this.frame = new MutableAudioFrame();
    this.frame.setBuffer(buffer);
  }

  @Override
  public boolean canProvide() {
    return audioPlayer.provide(this.frame);
  }

  @Override
  public ByteBuffer provide20MsAudio() {
    return this.buffer.flip();
  }

  @Override
  public boolean isOpus() {
    return true;
  }
}