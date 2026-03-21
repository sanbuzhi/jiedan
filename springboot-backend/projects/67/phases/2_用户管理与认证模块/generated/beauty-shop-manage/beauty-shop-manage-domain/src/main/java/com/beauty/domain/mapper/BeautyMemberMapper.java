package com.beauty.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.beauty.domain.entity.BeautyMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 美妆小店会员档案表Mapper接口
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Mapper
public interface BeautyMemberMapper extends BaseMapper<BeautyMember> {

}