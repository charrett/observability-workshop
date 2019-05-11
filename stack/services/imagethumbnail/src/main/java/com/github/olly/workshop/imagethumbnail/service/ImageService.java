package com.github.olly.workshop.imagethumbnail.service;

import com.github.olly.workshop.imagethumbnail.model.Image;
import com.github.olly.workshop.imagethumbnail.service.clients.ImageHolderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageService {

    @Autowired
    MetricsService metricsService;

    @Autowired
    ImageHolderClient imageHolderClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private static final int MAX_LENGTH = 100;

    private static final Map<String, Image> CACHE = new HashMap<String, Image>();

    public Image thumbnail(String id) {
        if (CACHE.get(id) == null) {
            Image image = resolveImage(id);
            CACHE.put(id, thumbnail(image));
        }
        return CACHE.get(id);
    }

    private Image resolveImage(String id) {
        Image image = new Image();
        image.setId(id);

        ResponseEntity<byte[]> response = imageHolderClient.getImage(id);

        image.setContentType(response.getHeaders().getContentType().toString());
        image.setData(response.getBody());

        return image;
    }

    private Image thumbnail(Image image) {
        return image.withBytes(thumbnailBytes(image));
    }

    private byte[] thumbnailBytes(Image image) {
        try {
            InputStream in = new ByteArrayInputStream(image.getData());
            String formatName = image.getContentType().split("/")[1];
            final BufferedImage bufferedImage = ImageIO.read(in);

            final int scaling;
            if (bufferedImage.getHeight() > bufferedImage.getWidth()) {
                scaling = bufferedImage.getHeight() / MAX_LENGTH;
            } else {
                scaling = bufferedImage.getWidth() / MAX_LENGTH;
            }

            final int width = bufferedImage.getWidth() / scaling;
            final int height = bufferedImage.getHeight() / scaling;

            final BufferedImage thumbnail = resize(bufferedImage, width, height, !isPng(formatName));
            final byte[] imageBytes = bufferedImageToByteArray(thumbnail, formatName);

            metricsService.imageThumbnailed(image.getContentType());

            return imageBytes;
        } catch (IOException e) {
            LOGGER.error("Failed thumbnailing image", e);
        }

        return null;
    }

    private boolean isPng(String formatName) {
        return formatName.toLowerCase().equals("png");
    }

    private BufferedImage resize(BufferedImage image, int width, int height, boolean preserveAlpha) {

        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(width, height, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return scaledBI;
    }

    private byte[] bufferedImageToByteArray(BufferedImage image, String formatname) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // ISSUE: OpenJDK does not have a native JPEG encoder, so it will fail at this point
        ImageIO.write(image, formatname, baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }
}
