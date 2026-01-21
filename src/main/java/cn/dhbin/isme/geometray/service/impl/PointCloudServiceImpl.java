package cn.dhbin.isme.geometray.service.impl;

import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.geometray.domain.dto.FolderInfoDto;
import cn.dhbin.isme.geometray.domain.dto.FrameDataDto;
import cn.dhbin.isme.geometray.domain.dto.PointCloudDto;
import cn.dhbin.isme.geometray.service.PointCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 点云数据服务实现类
 * 支持读取网络路径（SMB/CIFS）下的传感器数据文件
 */
@Slf4j
@Service
public class PointCloudServiceImpl implements PointCloudService {

    // 传感器文件名列表
    private static final String[] SENSOR_FILES = {
        "laser_1.txt", "laser_2.txt", "laser_3.txt",
        "laser_4.txt", "laser_5.txt", "laser_6.txt"
    };

    // 传感器颜色映射（与Python代码保持一致）
    private static final Map<String, String> SENSOR_COLORS = Map.of(
        "laser_1", "#FF0000",  // 红色
        "laser_2", "#0000FF",  // 蓝色
        "laser_3", "#00FF00",  // 绿色
        "laser_4", "#FF00FF",  // 洋红色
        "laser_5", "#00FFFF",  // 青色
        "laser_6", "#FF7F0E"   // 橙色
    );

    // 标定参数（与Python代码保持一致）
    private static final double[][][] MATRICES = {
        {{0.93250576, -0.36115509}, {0.36115509, 0.93250576}},
        {{0.96988336, 0.24356985}, {-0.24356985, 0.96988336}},
        {{0.86728317, 0.49781513}, {-0.49781513, 0.86728317}},
        {{0.84454612, -0.53548282}, {0.53548282, 0.84454612}},
        {{0.95778942, -0.28747073}, {0.28747073, 0.95778942}},
        {{0.94722178, 0.320579}, {-0.320579, 0.94722178}}
    };

    private static final double[][] DELTAS = {
        {88.50211447, 36.70282273},
        {240.75918765, 152.65948276},
        {309.40745097, 250.52613691},
        {1449.29830668, 51.24440465},
        {1472.0535824, 52.40036449},
        {1637.4765886, 182.70605235}
    };

    @Override
    public FolderInfoDto loadDataFolder(String folderPath) {
        log.info("加载数据文件夹: {}", folderPath);
        
        FolderInfoDto folderInfo = new FolderInfoDto();
        folderInfo.setFolderPath(folderPath);
        
        try {
            // 处理路径：支持本地路径和网络路径
            String processedPath = folderPath;
            
            // 如果是URL编码的路径，先解码
            try {
                processedPath = java.net.URLDecoder.decode(folderPath, "UTF-8");
            } catch (Exception e) {
                // 如果不是URL编码，使用原始路径
                processedPath = folderPath;
            }
            
            // 处理网络路径：将 / 转换为 \（Windows网络路径使用 \）
            if (processedPath.startsWith("//") || processedPath.startsWith("\\\\")) {
                // 网络路径保持原样，Java的Paths.get可以处理
                processedPath = processedPath.replace('/', '\\');
            }
            
            Path path = Paths.get(processedPath);
            
            // 检查路径是否存在
            if (!Files.exists(path)) {
                String pathType = processedPath.startsWith("\\\\") ? "网络路径" : "本地路径";
                String errorMsg = String.format("文件夹不存在: %s\n\n路径类型: %s\n\n请检查：\n1. 路径是否正确\n2. 如果是本地路径，确保数据在工业笔记本上\n3. 如果是网络路径，确保：\n   - 边缘计算机已设置共享文件夹\n   - 网络连接正常\n   - 可以在文件资源管理器中访问该路径\n\n示例：\n- 本地路径: C:\\Users\\admin\\Desktop\\laser_points\n- 网络路径: \\\\192.168.1.100\\data", 
                    path.toAbsolutePath(), pathType);
                log.error("文件夹不存在: {} (原始: {}, 处理后: {})", path.toAbsolutePath(), folderPath, processedPath);
                throw new BizException(BizResponseCode.ERR_400, errorMsg);
            }
            
            if (!Files.isDirectory(path)) {
                String errorMsg = String.format("路径不是目录: %s", path.toAbsolutePath());
                log.error("路径不是目录: {}", path.toAbsolutePath());
                throw new BizException(BizResponseCode.ERR_400, errorMsg);
            }

            // 读取所有传感器文件，收集所有帧号
            Set<Integer> allFrames = new HashSet<>();
            
            for (String sensorFile : SENSOR_FILES) {
                Path sensorPath = path.resolve(sensorFile);
                if (Files.exists(sensorPath)) {
                    Set<Integer> frames = readFrameNumbers(sensorPath);
                    allFrames.addAll(frames);
                    log.debug("传感器文件 {} 包含 {} 个帧", sensorFile, frames.size());
                } else {
                    log.warn("传感器文件不存在: {}", sensorPath);
                }
            }

            // 排序帧号
            List<Integer> sortedFrames = allFrames.stream()
                .sorted()
                .collect(Collectors.toList());

            folderInfo.setFrameNumbers(sortedFrames);
            folderInfo.setTotalFrames(sortedFrames.size());
            
            log.info("成功加载文件夹，共 {} 个帧", sortedFrames.size());
            return folderInfo;

        } catch (BizException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("加载数据文件夹失败: {}", folderPath, e);
            throw new BizException(BizResponseCode.ERR_400, "加载数据文件夹失败: " + e.getMessage());
        }
    }

    @Override
    public FrameDataDto getFrameData(String folderPath, Integer frameNumber) {
        log.info("获取帧数据: 文件夹={}, 帧号={}", folderPath, frameNumber);
        
        FrameDataDto frameData = new FrameDataDto();
        frameData.setFrameNumber(frameNumber);
        frameData.setSensors(new ArrayList<>());

        try {
            // 处理路径：支持本地路径和网络路径
            String processedPath = folderPath;
            
            // 如果是URL编码的路径，先解码
            try {
                processedPath = java.net.URLDecoder.decode(folderPath, "UTF-8");
            } catch (Exception e) {
                // 如果不是URL编码，使用原始路径
                processedPath = folderPath;
            }
            
            // 处理网络路径：将 / 转换为 \（Windows网络路径使用 \）
            if (processedPath.startsWith("//") || processedPath.startsWith("\\\\")) {
                processedPath = processedPath.replace('/', '\\');
            }
            
            Path path = Paths.get(processedPath);

            // 遍历所有传感器
            for (int i = 0; i < SENSOR_FILES.length; i++) {
                String sensorFile = SENSOR_FILES[i];
                String sensorName = sensorFile.replace(".txt", "");
                Path sensorPath = path.resolve(sensorFile);

                if (!Files.exists(sensorPath)) {
                    log.warn("传感器文件不存在: {}", sensorPath);
                    continue;
                }

                // 读取该传感器在该帧的数据
                List<double[]> points = readSensorFrameData(sensorPath, frameNumber, i);
                
                if (points != null && !points.isEmpty()) {
                    PointCloudDto sensorData = new PointCloudDto();
                    sensorData.setSensorName(sensorName);
                    sensorData.setPoints(points);
                    sensorData.setColor(SENSOR_COLORS.get(sensorName));
                    frameData.getSensors().add(sensorData);
                }
            }

            log.info("成功获取帧数据，包含 {} 个传感器", frameData.getSensors().size());
            return frameData;

        } catch (Exception e) {
            log.error("获取帧数据失败: 文件夹={}, 帧号={}", folderPath, frameNumber, e);
            return frameData;
        }
    }

    /**
     * 读取传感器文件中的所有帧号
     */
    private Set<Integer> readFrameNumbers(Path filePath) throws IOException {
        Set<Integer> frames = new HashSet<>();
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 1) {
                    try {
                        int frameNum = Integer.parseInt(parts[0]);
                        frames.add(frameNum);
                    } catch (NumberFormatException e) {
                        // 忽略无效行
                    }
                }
            }
        }
        
        return frames;
    }

    /**
     * 读取指定传感器在指定帧的数据，并应用标定变换和Y坐标过滤
     */
    private List<double[]> readSensorFrameData(Path filePath, int frameNumber, int sensorIndex) throws IOException {
        List<double[]> rawPoints = new ArrayList<>();
        
        // 读取原始数据
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    try {
                        int frameNum = Integer.parseInt(parts[0]);
                        if (frameNum == frameNumber) {
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            rawPoints.add(new double[]{x, y});
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无效行
                    }
                }
            }
        }

        if (rawPoints.isEmpty()) {
            return Collections.emptyList();
        }

        // 应用标定变换
        double[][] matrix = MATRICES[sensorIndex];
        double[] delta = DELTAS[sensorIndex];
        
        List<double[]> transformedPoints = new ArrayList<>();
        for (double[] point : rawPoints) {
            // 矩阵变换: [x', y'] = [x, y] * matrix^T + delta
            double x = point[0] * matrix[0][0] + point[1] * matrix[0][1] + delta[0];
            double y = point[0] * matrix[1][0] + point[1] * matrix[1][1] + delta[1];
            
            // Y坐标过滤：仅保留 -200 ≤ Y ≤ 100
            if (y >= -200 && y <= 100) {
                transformedPoints.add(new double[]{x, y});
            }
        }

        return transformedPoints;
    }
}

