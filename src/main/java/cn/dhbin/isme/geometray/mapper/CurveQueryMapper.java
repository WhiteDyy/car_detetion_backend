package cn.dhbin.isme.geometray.mapper;

import cn.dhbin.isme.geometray.domain.entity.CurveQuery;
import cn.dhbin.isme.geometray.domain.requeset.CurveQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;



public interface CurveQueryMapper extends BaseMapper<CurveQuery> {
    List<CurveQuery> queryPage(CurveQueryRequest request);
}
