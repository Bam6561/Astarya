package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command invocation that converts an image to .jpg format.
 * <p>
 * Optionally, the image can be compressed by providing a quality value.
 *
 * @author Danny Nguyen
 * @version 1.9.2
 * @see <a href="https://github.com/Bam6561/JPGConverter">JPGConverter</a>
 * @since 1.9.1
 */
public class Jpg extends Command {
  /**
   * Associates command with its properties.
   */
  public Jpg() {
    this.name = "jpg";
    this.aliases = new String[]{"jpg"};
    this.arguments = "[0]+image(s) [1]<quality> +images(s)";
    this.help = "Converts an image to .jpg format with optional quality.";
  }

  /**
   * Converts image attachments sent with message into
   * .jpg format with an optional quality compression.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    List<Message.Attachment> attachments = ce.getMessage().getAttachments();
    if (attachments.isEmpty()) {
      ce.getChannel().sendMessage("No images sent with command.").queue();
      return;
    }
    List<Message.Attachment> imageAttachments = new ArrayList<>();
    for (Message.Attachment attachment : attachments) {
      if (attachment.isImage()) {
        imageAttachments.add(attachment);
      }
    }
    if (imageAttachments.isEmpty()) {
      ce.getChannel().sendMessage("No images sent with command.").queue();
      return;
    }

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> new JpgRequest(ce, imageAttachments).readRequest();
      case 1 -> new JpgRequest(ce, imageAttachments).readRequest(parameters[1]);
      default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Represents an image conversion request.
   *
   * @author Danny Nguyen
   * @version 1.9.1
   * @since 1.9.1
   */
  private static class JpgRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * Image attachments.
     */
    private final List<Message.Attachment> imageAttachments;

    /**
     * Downloaded images.
     */
    private final List<File> downloadedImages = new ArrayList<>();

    /**
     * Converted images to upload.
     */
    private final List<FileUpload> imagesToUpload = new ArrayList<>();

    /**
     * Associates the JPG request with its command event and image attachments.
     *
     * @param ce               command event
     * @param imageAttachments image attachments
     */
    JpgRequest(CommandEvent ce, List<Message.Attachment> imageAttachments) {
      this.ce = ce;
      this.imageAttachments = imageAttachments;
    }

    /**
     * Converts images with a 1.0 image quality.
     */
    private void readRequest() {
      convertImages(1.0f);
    }

    /**
     * Checks if the quality provided is formatted correctly before converting images.
     *
     * @param requestedQuality requested image quality
     */
    private void readRequest(String requestedQuality) {
      try {
        float quality = Float.parseFloat(requestedQuality);
        if (!(quality >= 0 || quality <= 1)) {
          ce.getChannel().sendMessage("Quality must in range of 0.0 and 1.0.").queue();
          return;
        }
        convertImages(quality);
      } catch (NumberFormatException ex) {
        ce.getChannel().sendMessage("Quality must in range of 0.0 and 1.0.").queue();
      }
    }

    /**
     * Downloads and converts images to .jpg format at the desired quality.
     *
     * @param quality image quality
     */
    private void convertImages(float quality) {
      File jpgDirectory = new File(".\\resources\\jpg\\");
      if (!jpgDirectory.exists()) {
        jpgDirectory.mkdirs();
      }

      List<CompletableFuture<File>> downloads = new ArrayList<>();
      for (Message.Attachment imageAttachment : imageAttachments) {
        File file = new File(".\\resources\\jpg\\" + imageAttachment.getFileName());
        downloads.add(imageAttachment.getProxy().downloadToFile(file));
        downloadedImages.add(file);
      }
      for (CompletableFuture<File> download : downloads) {
        download.join();
      }

      for (File downloadedImage : downloadedImages) {
        convertIntoJpg(downloadedImage, quality);
      }
      ce.getChannel().sendMessage("").addFiles(imagesToUpload).queue(
          delete -> {
            for (File file : jpgDirectory.listFiles()) {
              file.delete();
            }
          }
      );
    }

    /**
     * Converts an image file into .jpg format and compresses the image quality to the quality set.
     *
     * @param file    image being converted
     * @param quality image quality
     */
    private void convertIntoJpg(File file, float quality) {
      try {
        BufferedImage image = ImageIO.read(file);
        if (image.getColorModel().hasAlpha()) {
          image = removeAlphaChannel(image);
        }

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        File convertedImage = new File(file.getPath().substring(0, file.getPath().lastIndexOf(".")) + ".jpg");
        ImageOutputStream output = ImageIO.createImageOutputStream(convertedImage);
        writer.setOutput(output);

        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(quality);

        writer.write(null, new IIOImage(image, null, null), params);
        output.close();
        writer.dispose();

        imagesToUpload.add(FileUpload.fromData(convertedImage));
      } catch (IOException e) {
        ce.getChannel().sendMessage("Failed to convert image.").queue();
      }
    }

    /**
     * Removes an image's alpha channel.
     *
     * @param image image with alpha channel
     * @return image without alpha channel
     */
    private BufferedImage removeAlphaChannel(BufferedImage image) {
      BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
      return newImage;
    }
  }
}
