package org.project.reggie.service;



import com.baomidou.mybatisplus.extension.service.IService;
import org.project.reggie.entity.Orders;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 */
public interface OrdersService extends IService<Orders> {
    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

}
