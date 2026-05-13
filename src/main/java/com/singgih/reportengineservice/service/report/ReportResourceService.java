package com.singgih.reportengineservice.service.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Loads static report resources (CSS, logo) once at startup and caches them.
 *
 * CSS  : src/main/resources/style/main.css
 * Logo : generated programmatically from the design in image/logo-ms.svg
 *        (SVG is kept as vector source; a PNG is generated at runtime for
 *         compatibility with Flying Saucer and Apache POI).
 */
@Slf4j
@Service
public class ReportResourceService {

    private final String cssContent;
    private final String logoDataUri;

    public ReportResourceService() {
        this.cssContent  = loadClasspathText("style/main.css");
        this.logoDataUri = generateMsLogoDataUri();
        log.info("Report resources loaded — CSS: {} chars, logo PNG generated", cssContent.length());
    }

    public String getCssContent()  { return cssContent; }
    public String getLogoDataUri() { return logoDataUri; }

    // -------------------------------------------------------------------------

    private String loadClasspathText(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("Classpath resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load classpath resource: " + path, e);
        }
    }

    /** Generates a 44×44 px PNG of the "MS" logo (mirrors logo-ms.svg design). */
    private String generateMsLogoDataUri() {
        int size = 44;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(new Color(0x1a, 0x52, 0x76));
        g.fillRoundRect(0, 0, size, size, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 19));
        FontMetrics fm = g.getFontMetrics();
        String text = "MS";
        int tx = (size - fm.stringWidth(text)) / 2;
        int ty = (size + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, tx, ty);
        g.dispose();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate logo PNG", e);
        }
    }
}
