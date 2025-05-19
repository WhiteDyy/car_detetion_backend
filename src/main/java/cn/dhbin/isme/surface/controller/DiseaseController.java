package cn.dhbin.isme.surface.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/disease")
@RequiredArgsConstructor
@Tag(name = "图像检测-病害信息")
public class DiseaseController {

}
