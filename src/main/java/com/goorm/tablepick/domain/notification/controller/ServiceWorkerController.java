package com.goorm.tablepick.domain.notification.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


// Firebase Service Worker 스크립트를 제공하는 컨트롤러
// PWA(Progressive Web App)와 FCM 푸시 알림 기능을 위한 Service Worker 파일 서빙
@Controller
public class ServiceWorkerController {


    // Service Worker 자바스크립트 파일을 클라이언트에 제공
    @GetMapping(value = "/firebase-messaging-sw.js", produces = "application/javascript")
    @ResponseBody
    public ResponseEntity<String> getServiceWorkerJs() throws IOException {
        // static 폴더에서 Service Worker 스크립트 파일 로드
        Resource resource = new ClassPathResource("static/firebase-messaging-sw.js");
        String content = new String(Files.readAllBytes(resource.getFile().toPath()),
                StandardCharsets.UTF_8);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        // Service Worker의 스코프 설정 (루트 경로에서 동작 가능하도록 설정)
        headers.add("Service-Worker-Allowed", "/");
        // 브라우저 캐싱 방지를 위한 헤더 설정
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        // ResponseEntity 구성 및 반환
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(content);
    }
}
