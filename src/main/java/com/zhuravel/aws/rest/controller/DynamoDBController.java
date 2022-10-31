package com.zhuravel.aws.rest.controller;

import com.zhuravel.aws.rest.SendMessage;
import com.zhuravel.aws.rest.WriteExcel;
import com.zhuravel.aws.rest.model.FileAttributeItem;
import com.zhuravel.aws.rest.service.DynamoDBService;
import com.zhuravel.aws.rest.service.S3Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Evgenii Zhuravel created on 24.10.2022
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/")
public class DynamoDBController {

    private final DynamoDBService dbService;
    private final S3Service s3Service;

    private final SendMessage sendMsg;

    private final WriteExcel excel;

    public DynamoDBController(DynamoDBService dbService, S3Service s3Service, SendMessage sendMsg, WriteExcel excel) {
        this.dbService = dbService;
        this.s3Service = s3Service;
        this.sendMsg = sendMsg;
        this.excel = excel;
    }

    @PostMapping("/uploadFile")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        s3Service.putObject(file);
    }

    /*@PostMapping("add")
    String addItems(@RequestBody Map<String, Object> payLoad) {
        String name = "user";
        String guide = (String)payLoad.get("guide");
        String description = (String)payLoad.get("description");
        String status = (String)payLoad.get("status");

        FileAttributeItem myWork = new FileAttributeItem();
        myWork.setGuide(guide);
        myWork.setDescription(description);
        myWork.setStatus(status);
        myWork.setName(name);
        dbService.setItem(myWork);
        return "Item added";
    }*/

    @PutMapping("report/{email}")
    public String sendReport(@PathVariable String email){
        List<FileAttributeItem> theList = dbService.getAllItems();
        java.io.InputStream is = excel.exportExcel(theList);

        try {
            sendMsg.sendReport(is, email);

        }catch (IOException e) {
            e.getStackTrace();
        }
        return "Report is created";
    }

    /*@PutMapping("mod/{id}")
    public String modUser(@PathVariable String id) {
        dbService.archiveItemEC(id );
        return id ;
    }*/

    @GetMapping("files")
    public List<FileAttributeItem> getFiles() {
        return s3Service.getFiles();
    }

    @DeleteMapping("delete/{fileName}")
    public void deleteFile(@PathVariable String fileName) {
        s3Service.delete(fileName);
    }
}
