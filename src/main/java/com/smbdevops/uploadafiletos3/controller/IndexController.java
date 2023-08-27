package com.smbdevops.uploadafiletos3.controller;

import com.amazonaws.services.s3.model.*;
import com.smbdevops.uploadafiletos3.client.AmazonClient;
import form.AudioForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")

public class IndexController {

    private final AmazonClient amazon;

    private final List<String> listOfUploadedItems = new ArrayList<>();

    public IndexController(AmazonClient amazon) {
        this.amazon = amazon;
    }


    @GetMapping("")
    public String get(final Model model, AudioForm form) {
        model.addAttribute("items", this.listOfUploadedItems);
        model.addAttribute("dto", form);
        return "index";
    }

    @PostMapping("")
    public String post(
                       final @Validated @ModelAttribute("dto") AudioForm dto,
                       final BindingResult bindingResult, final Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "index";
        }
        final List<MultipartFile> fileList = dto.getFiles() == null ? new ArrayList<>() : new ArrayList<>(List.of(dto.getFiles()));
        if (!fileList.isEmpty()) {
            fileList.removeIf(this::verifyAudio);
        }

        if (!fileList.isEmpty()) {
            fileList.forEach(multipartFile -> {
                try {
                    this.saveImageToS3(multipartFile.getInputStream(), multipartFile.getOriginalFilename());
                    this.listOfUploadedItems.add("https://" + this.amazon.getBucketName() + ".s3." + this.amazon.getRegion() + ".amazonaws.com/" + multipartFile.getOriginalFilename());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // fallback for odd behaviors
        model.addAttribute("items", this.listOfUploadedItems);
        return "index";

    }

    private boolean verifyAudio(final MultipartFile file) {
        try {
            if (file != null && file.getResource().isReadable()) {
                // put in whatever verification on the audio you'd like -- e.g., ensure that the audio is in fact audio -- really only relevant if you're taking user-input.
            }
            return false;
        } catch (final RuntimeException exception) {
            return true;
        }
    }

    private void saveImageToS3(final InputStream inputStream, final String originalImagePath) throws IOException {
        final ObjectMetadata md = new ObjectMetadata();
        final AccessControlList acl = new AccessControlList();
        final byte[] buffer = inputStream.readAllBytes();
        final InputStream convertedInputStream = new ByteArrayInputStream(buffer);
        md.setContentLength(buffer.length);
        md.setContentType("audio/mp3");
        System.out.println("Saving to " + this.amazon.getBucketName() + "/" + originalImagePath + " using credential key: " + this.amazon.getAccessKeyId());
        final PutObjectRequest req = new PutObjectRequest(this.amazon.getBucketName(), originalImagePath, convertedInputStream, md);
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        req.setAccessControlList(acl);
        this.amazon.getS3client().putObject(req);
    }

}
