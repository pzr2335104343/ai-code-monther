package com.rong.rongcodemother.controller;

import com.rong.rongcodemother.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/static")
public class StaticResourceController {

    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8123/api/static/{deployKey}/{appVersion}[/{fileName}]
     */
    @GetMapping("/{deployKey}/{appVersion}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            @PathVariable String appVersion,
            HttpServletRequest request) {
        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

            resourcePath = resourcePath.substring(("/static/"+deployKey+'/'+appVersion).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL **/v1
            if(resourcePath.isEmpty()){
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 如果是目录访问（带斜杠），重定向到index.html **/v1/
            if (resourcePath.endsWith("/")) {
                resourcePath = "/index.html";
            }
            // 构建文件路径
            String filePath = String.format("%s/%s/%s/%s",PREVIEW_ROOT_DIR,deployKey,appVersion,resourcePath);
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 提供目录下所有文件名
     * @param deployKey 应用目录
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listDirectoryNames(@RequestParam String deployKey,HttpServletRequest request){
        try {
            // 拼接目标目录（基础目录 + 子目录）
            String fileDirPath = PREVIEW_ROOT_DIR + File.separator + deployKey;
            File fileDir = new File(fileDirPath);
            // 校验目录有效性
            if (!fileDir.exists() || !fileDir.isDirectory()) {
                throw new IllegalArgumentException("目录不存在或不是有效目录");
            }
            // 获取目录名称
            List<String> pathList = getFileNames(fileDir, fileDir.getName());
            return ResponseEntity.ok(pathList) ;
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<String> getFileNames(File fileDir,String parentPath){
        parentPath+="/";
        List<String> pathList = new ArrayList<>();
        for(File file:fileDir.listFiles()){
            pathList.add(parentPath+file.getName());
            if(file.isDirectory()){
                pathList.addAll(getFileNames(file,parentPath+file.getName()));
            }
        }
        return pathList;
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
