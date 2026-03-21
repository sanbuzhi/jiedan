package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyStaff;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店员工档案表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyStaffMapper extends BaseMapper<BeautyStaff> {

}