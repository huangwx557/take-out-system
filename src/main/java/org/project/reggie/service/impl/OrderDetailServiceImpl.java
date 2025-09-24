package org.project.reggie.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.project.reggie.entity.OrderDetail;
import org.project.reggie.mapper.OrderDetailMapper;
import org.project.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
