package org.project.reggie.controller;

import lombok.extern.slf4j.Slf4j;
import org.project.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
//上传图片
public class CommonController {
    //拿到yml里保存的文件路径
    @Value("${takeOutFile.fileLocaltion}")
    private String basePath;
    
    @PostMapping("/upload")
    public R<String> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file){
        log.info("文件上传：{}", file.toString());
        
        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        //截取JPG等后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        
        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;
        
        // 创建目录
        File dir = new File(basePath);
        if(!dir.exists()){
            // 目录不存在，需要创建
            dir.mkdirs();
        }
        
        try {
            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return R.error("文件上传失败");
        }
        return R.success(fileName);
    }
    //文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            // 输入流，读入文件
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，将文件写回浏览器，展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            // 关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
