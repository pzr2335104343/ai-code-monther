package com.rong.rongcodemother.service;

public interface ScreenshotService {

    /**
     * 生成网页截图并上传到对象存储
     * @param webUrl 需要生成截图的网页URL
     * @return 返回上传到对象存储后的URL地址
     */
    String generateAndUploadScreenshot(String webUrl);
}
