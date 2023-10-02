package com.QRCode.QRCodePasswordSaver.controllers;

import java.awt.PageAttributes.MediaType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;

import org.apache.tomcat.util.file.ConfigurationSource.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.QRCode;

@RestController
@RequestMapping("/qrcode")
public class QRCodeGeneraterAndScanner {

	@PostMapping("/read")
	public ResponseEntity<String> readQrCode(@RequestPart(value = "file", required = true) MultipartFile file)
			throws NotFoundException, FileNotFoundException, IOException {
		String charset = "UTF-8";

		Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();

		hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		BinaryBitmap binaryBitmap = new BinaryBitmap(
				new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(file.getInputStream()))));

		Result result = new MultiFormatReader().decode(binaryBitmap);
		return ResponseEntity.ok(result.getText());
	}

	@PostMapping("/gen")
	public ResponseEntity<Object> genQrCode(@RequestBody String datas) throws WriterException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {// The data

		String charset = "UTF-8";

		Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<>();

		hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		File outputfile = new File("saved.png");

		BitMatrix matrix = new MultiFormatWriter().encode(new String(datas.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, 200, 200);

		MatrixToImageWriter.writeToFile(matrix, "png", outputfile);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(outputfile));
/*
		KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
		SecretKey myDesKey = keygenerator.generateKey();

		// Creating object of Cipher
		Cipher desCipher;
		desCipher = Cipher.getInstance("DES");
		System.out.print(myDesKey);
		// Encrypting text
		desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
		byte[]  bytes= desCipher.doFinal(resource.getContentAsByteArray());
		InputStreamResource resources=new InputStreamResource(new ByteArrayInputStream(bytes));
		*/
		HttpHeaders headers = new HttpHeaders();

		headers.add("Content-Disposition", "attachment; filename=saved.png");

		headers.add("Content-type", "image/png");

		ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(outputfile.length())
				.body(resource.getContentAsByteArray());
		outputfile.delete();

		return responseEntity;

	}

}
