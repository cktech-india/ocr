package com.cktech.ocr.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cktech.ocr.model.field.FieldDTO;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrService {

    private static final Logger LOG = LoggerFactory.getLogger(OcrService.class);

    private final Tesseract tesseract;
    private final FieldService fieldService;
    // Hard concurrency gate protecting the engine from memory/CPU spikes
    private final Semaphore processingGate = new Semaphore(2);

    public OcrService(
            @Value("${tesseract.datapath}") String tessDataPath,
            FieldService fieldService) {
        this.fieldService = fieldService;
        this.tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");

        // Page Segmentation Mode 11: Sparse text optimization
        tesseract.setPageSegMode(11);

        // Character Whitelist prevents parsing noise artifact patterns as data strings
        tesseract.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/ ");
    }

    public Map<String, Object> processKycDocument(MultipartFile file, String screenCode)
            throws IOException, TesseractException, InterruptedException {
        // Enforce rate limiting to block simultaneous processing crashes
        processingGate.acquire();
        try {
            // 1. Read input stream cleanly into a Buffered Memory Space
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Invalid or corrupt image stream.");
            }

            // 2. Perform Downsizing and Contrast Normalization
            BufferedImage processedImage = preprocessImage(originalImage);

            // 3. Native JNA Call to Engine
            String rawText = tesseract.doOCR(processedImage);

            // 4. Structural Clean up and Mapping via dynamic field rules
            return parseKycDetails(rawText, screenCode);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return Map.of("error", e.getMessage());
        } finally {
            processingGate.release();
        }
    }

    private BufferedImage preprocessImage(BufferedImage src) {
        int targetWidth = 1200;
        if (src.getWidth() <= targetWidth) {
            return convertToMonochrome(src);
        }

        // Calculate aspect ratio scale to avoid distorting character geometry
        int targetHeight = (int) (src.getHeight() * ((double) targetWidth / src.getWidth()));

        Image scaledImage = src.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

        // Write to TYPE_BYTE_GRAY to drop unnecessary RGB color channels instantly
        BufferedImage grayImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return applyThreshold(grayImage);
    }

    private BufferedImage convertToMonochrome(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return applyThreshold(gray);
    }

    // High performance mid-point binarization loop acting as a zero-weight OpenCV replacement
    private BufferedImage applyThreshold(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();
        BufferedImage binarized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = grayImage.getRGB(x, y);
                int luminance = (rgb >> 16) & 0xFF;
                if (luminance < 130) {
                    binarized.setRGB(x, y, 0x000000); // Sharp Text
                } else {
                    binarized.setRGB(x, y, 0xFFFFFF); // Background Cleaned
                }
            }
        }
        return binarized;
    }

    // Dynamic extraction driven entirely by field_t rules configured for the given screenCode
    private Map<String, Object> parseKycDetails(String rawText, String screenCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("screenCode", screenCode);

        List<FieldDTO> fields = fieldService.getFieldsForScreen(screenCode);

        for (FieldDTO field : fields) {
            Pattern pattern = Pattern.compile(field.getPattern());
            Matcher matcher = pattern.matcher(rawText);
            if(!result.containsKey(field.getFieldCode()) && matcher.find()) {
                result.put(field.getFieldCode(), matcher.group(0));
            }

        }

        result.put("rawExtractedText", rawText.trim());
        return result;
    }
}