package cn.dhbin.isme.geometray.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件夹信息传输对象
 */
@Data
@NoArgsConstructor
public class FolderInfoDto {

    /**
     * 文件夹路径
     */
    @JsonProperty("folderPath")
    private String folderPath;

    /**
     * 所有可用的帧号列表
     */
    @JsonProperty("frameNumbers")
    private List<Integer> frameNumbers;

    /**
     * 总帧数
     */
    @JsonProperty("totalFrames")
    private Integer totalFrames;
}

