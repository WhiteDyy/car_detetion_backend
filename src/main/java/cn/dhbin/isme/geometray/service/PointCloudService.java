package cn.dhbin.isme.geometray.service;

import cn.dhbin.isme.geometray.domain.dto.FolderInfoDto;
import cn.dhbin.isme.geometray.domain.dto.FrameDataDto;

/**
 * 点云数据服务接口
 */
public interface PointCloudService {

    /**
     * 加载数据文件夹，返回所有帧号信息
     *
     * @param folderPath 数据文件夹路径（可以是网络路径，如 \\192.168.1.100\data 或 /mnt/share/data）
     * @return 文件夹信息，包含所有帧号
     */
    FolderInfoDto loadDataFolder(String folderPath);

    /**
     * 获取指定帧的点云数据
     *
     * @param folderPath 数据文件夹路径
     * @param frameNumber 帧号
     * @return 帧数据，包含所有传感器的点云
     */
    FrameDataDto getFrameData(String folderPath, Integer frameNumber);
}

