package cn.dhbin.isme.geometray.mapper;

import cn.dhbin.isme.geometray.domain.dto.JobsDto;
import cn.dhbin.isme.geometray.domain.entity.Jobs;
import cn.dhbin.isme.geometray.domain.requeset.JobsPageRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobsMapperTest {

    @Resource
    private JobsMapper jobsMapper;


    @Test
    void testPageDetail() {
        JobsPageRequest jobsPageRequest = new JobsPageRequest();
        // 创建分页对象
        IPage<Jobs> page = jobsPageRequest.toPage();

        // 调用分页查询方法，所有参数传null
        IPage<JobsDto> result = jobsMapper.pageDetail(
                page,
                null, null, null, null, null, null, null, null, null, null
        );

        // 断言检查
        assertNotNull(result);
        // 检查总记录数
        assertTrue(result.getTotal() >= 0);
        // 检查当前页码
        assertEquals(1, result.getCurrent());
        // 检查每页大小
        assertEquals(10, result.getSize());

        // 如果有数据，检查返回的数据内容
        if (!result.getRecords().isEmpty()) {
            for (JobsDto jobsDto : result.getRecords()) {
                System.out.println(jobsDto.toString());
            }
        }
    }

}